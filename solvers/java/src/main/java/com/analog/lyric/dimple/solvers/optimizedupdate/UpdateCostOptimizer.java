/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.solvers.optimizedupdate;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.schedulers.IScheduledActivity;
import com.analog.lyric.dimple.schedulers.ScheduleVisitor;
import com.analog.lyric.dimple.schedulers.ScheduleVisitorFactorFilter;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.util.misc.Internal;

/**
 * Sets up a factor graph's choice of normal or optimized update algorithm, and relevant settings,
 * for each of the factor tables used by the graph. The choice is based on the options set on the
 * graph and its factors. For those factor tables that use automatic approach selection, this class
 * estimates the cost of each approach and chooses that with the lowest cost.
 * <p>
 * Only those factors that have all of their edges updated together according to the schedule are
 * considered for the optimized update algorithm, as the optimized update algorithm does not apply
 * to edge-by-edge updates.
 * <p>
 * For cost estimation, when multithreading is enabled on the graph, allocation costs are multiplied
 * by the lesser of the worker count or the quantity of factors sharing a factor table, and
 * execution time costs are divided by the same quantity.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public final class UpdateCostOptimizer
{
	private final ISFactorGraphToOptimizedUpdateAdapter _sFactorGraphAdapter;
	
	public UpdateCostOptimizer(ISFactorGraphToOptimizedUpdateAdapter sFactorGraphAdapter)
	{
		_sFactorGraphAdapter = sFactorGraphAdapter;
	}
	
	private static class UpdateCollector implements IScheduledActivity
	{
		final Map<IFactorTable, FactorTableUpdateSettings> _settingsByFactorTable;
		
		UpdateCollector(final Map<IFactorTable, FactorTableUpdateSettings> settingsByFactorTable)
		{
			_settingsByFactorTable = settingsByFactorTable;
		}

		@Override
		public void updateEdge(INode node, int portNum)
		{
			// Edge updates are ignored.
		}

		private <T extends Serializable> T valueOrDefault(@Nullable T t, OptionKey<T> key)
		{
			return t != null ? t : key.defaultValue();
		}

		@Override
		public void update(INode node)
		{
			Factor factor = node.asFactor();
			if (factor != null && factor.hasFactorTable())
			{
				final IFactorTable factorTable = factor.getFactorTable();
				final STableFactorBase sTableFactor = (STableFactorBase) factor.getSolver();
				if (sTableFactor != null)
				{
					// This class fetches options from sTableFactor, and keeps track of the
					// most-specific level from which an option value was obtained over multiple
					// getOption invocations.
					final class OptionGetter {
						private int _minLevel = Integer.MAX_VALUE;
						
						private final int[] _level = new int[1];
						
						@Nullable <T extends Serializable> T getOption(IOptionKey<T> key)
						{
							_level[0] = Integer.MAX_VALUE;
							T t = sTableFactor.getOptionUptoDepth(key, _level[0], _level);
							if (_level[0] < _minLevel)
								_minLevel = _level[0];
							return t;
						}
						
						int getMinLevel()
						{
							return _minLevel;
						}
					}
					
					OptionGetter optionGetter = new OptionGetter();
					UpdateApproach approach = optionGetter.getOption(UpdateOptions.updateApproach);
					Double sparseThreshold = optionGetter.getOption(UpdateOptions.optimizedUpdateSparseThreshold);
					Double executionTimeScalingFactor = optionGetter.getOption(UpdateOptions.automaticExecutionTimeScalingFactor);
					Double memoryAllocationScalingFactor = optionGetter.getOption(UpdateOptions.automaticMemoryAllocationScalingFactor);
					final int minLevel = optionGetter.getMinLevel();

					boolean updateFactorTableSettings = false;
					FactorTableUpdateSettings factorTableUpdateSettings = _settingsByFactorTable.get(factorTable);
					if (factorTableUpdateSettings == null)
					{
						factorTableUpdateSettings = new FactorTableUpdateSettings();
						_settingsByFactorTable.put(factorTable, factorTableUpdateSettings);
						updateFactorTableSettings = true;
					}
					else if (minLevel < factorTableUpdateSettings.getLevel())
					{
						updateFactorTableSettings = true;
					}
					else if (minLevel != Integer.MAX_VALUE && minLevel == factorTableUpdateSettings.getLevel())
					{
						// If the options for the factor are set and there are previously-found
						// settings for the factor table, they are from the same source level,
						// and they have different values, then issue a warning, and ignore the
						// "newer" values.
						final boolean different =
							factorTableUpdateSettings.getApproach() != valueOrDefault(approach, UpdateOptions.updateApproach)
								|| factorTableUpdateSettings.getSparseThreshold() != valueOrDefault(sparseThreshold,
									UpdateOptions.optimizedUpdateSparseThreshold)
								|| factorTableUpdateSettings.getExecutionTimeScalingFactor() != valueOrDefault(
									executionTimeScalingFactor, UpdateOptions.automaticExecutionTimeScalingFactor)
								|| factorTableUpdateSettings.getMemoryAllocationScalingFactor() != valueOrDefault(
									memoryAllocationScalingFactor,
									UpdateOptions.automaticMemoryAllocationScalingFactor);
						if (different)
						{
							// TODO: Make this warning good and helpful.
							DimpleEnvironment
								.logWarning("Different options values set on factors that share a factor table at the same specificity level.");
						}
					}
					// Implicitly ignore the case where the factor options are specified at a
					// less-specific level than previously.

					factorTableUpdateSettings.setCount(factorTableUpdateSettings.getCount() + 1);

					if (updateFactorTableSettings)
					{
						factorTableUpdateSettings.setLevel(minLevel);
						factorTableUpdateSettings.setApproach(valueOrDefault(approach, UpdateOptions.updateApproach));
						factorTableUpdateSettings.setSparseThreshold(valueOrDefault(sparseThreshold,
							UpdateOptions.optimizedUpdateSparseThreshold));
						factorTableUpdateSettings.setExecutionTimeScalingFactor(valueOrDefault(executionTimeScalingFactor,
							UpdateOptions.automaticExecutionTimeScalingFactor));
						factorTableUpdateSettings.setMemoryAllocationScalingFactor(valueOrDefault(memoryAllocationScalingFactor,
							UpdateOptions.automaticMemoryAllocationScalingFactor));
					}
				}
			}
		}
	}

	public void optimize(final FactorGraph factorGraph)
	{
		final int workers = _sFactorGraphAdapter.getWorkers(factorGraph);
		final Map<IFactorTable, FactorTableUpdateSettings> settingsByFactorTable = new IdentityHashMap<>();
		_sFactorGraphAdapter.putFactorTableUpdateSettings(settingsByFactorTable);
		final ISchedule schedule = factorGraph.getSchedule();
		final IScheduledActivity updatesCollector = new UpdateCollector(settingsByFactorTable);
		ScheduleVisitor.visit(schedule, ScheduleVisitorFactorFilter.create(updatesCollector));
		for (Entry<IFactorTable, FactorTableUpdateSettings> entry : settingsByFactorTable.entrySet())
		{
			final IFactorTable factorTable = entry.getKey();
			final FactorTableUpdateSettings factorTableUpdateSettings = entry.getValue();
			if (factorTableUpdateSettings != null)
			{
				UpdateApproach updateApproach = factorTableUpdateSettings.getApproach();
				FactorUpdatePlan updatePlan = null;
				if (updateApproach == UpdateApproach.UPDATE_APPROACH_OPTIMIZED
					|| (updateApproach == UpdateApproach.UPDATE_APPROACH_AUTOMATIC &&
						chooseUpdateApproach(workers, factorTable, factorTableUpdateSettings)))
				{
					updatePlan =
						FactorUpdatePlan.create(factorTable, _sFactorGraphAdapter,
							factorTableUpdateSettings.getSparseThreshold());
				}
				factorTableUpdateSettings.setOptimizedUpdatePlan(updatePlan);
			}
		}
	}

	private boolean chooseUpdateApproach(final int workers,
		final IFactorTable factorTable,
		final FactorTableUpdateSettings factorTableUpdateSettings)
	{
		boolean createOptimizedUpdatePlan = false;
		if (factorTable.getDimensions() > 1)
		{
			Costs optimizedCosts =
				_sFactorGraphAdapter.estimateCostOfOptimizedUpdate(factorTable,
					factorTableUpdateSettings.getSparseThreshold());
			Costs normalCosts = _sFactorGraphAdapter.estimateCostOfNormalUpdate(factorTable);
			// Multiply the execution time costs by the quantity of factors using the table
			optimizedCosts.put(CostType.ACCESSES,
				factorTableUpdateSettings.getCount() * optimizedCosts.get(CostType.ACCESSES));
			normalCosts.put(CostType.ACCESSES, factorTableUpdateSettings.getCount() * normalCosts.get(CostType.ACCESSES));
			// For the optimized algorithm only, multiply the allocation costs by the lesser
			// of the core count or the quantity of factors using the table.
			int scale = Math.min(workers, factorTableUpdateSettings.getCount());
			optimizedCosts.put(CostType.ALLOCATED_BYTES, optimizedCosts.get(CostType.ALLOCATED_BYTES) * scale);
			// For both algorithms, divide the execution time costs by the same scaling
			// factor.
			optimizedCosts.put(CostType.ACCESSES, optimizedCosts.get(CostType.ACCESSES) / scale);
			normalCosts.put(CostType.ACCESSES, normalCosts.get(CostType.ACCESSES) / scale);
			createOptimizedUpdatePlan =
				computeCost(optimizedCosts, factorTableUpdateSettings.getExecutionTimeScalingFactor(),
					factorTableUpdateSettings.getMemoryAllocationScalingFactor()) <= computeCost(normalCosts,
					factorTableUpdateSettings.getExecutionTimeScalingFactor(),
					factorTableUpdateSettings.getMemoryAllocationScalingFactor());
		}
		factorTableUpdateSettings.setAutomaticUpdateApproach(createOptimizedUpdatePlan ?
			UpdateApproach.UPDATE_APPROACH_OPTIMIZED : UpdateApproach.UPDATE_APPROACH_NORMAL);
		return createOptimizedUpdatePlan;
	}

	private static double computeCost(final Costs costs,
		final double executionTimeScalingFactor,
		final double memoryAllocationScalingFactor)
	{
		final double accesses = costs.get(CostType.ACCESSES);
		final double allocations = costs.get(CostType.ALLOCATED_BYTES);
		final double j = accesses * executionTimeScalingFactor + allocations * memoryAllocationScalingFactor;
		return j;
	}
}
