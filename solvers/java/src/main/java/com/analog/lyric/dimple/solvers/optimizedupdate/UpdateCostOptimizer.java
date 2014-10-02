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
	
	private class UpdateCollector implements IScheduledActivity
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

		private <T extends Serializable> T valueOrDefault(@Nullable T t, IOptionKey<T> key)
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
					IOptionKey<UpdateApproach> updateApproachKey = _sFactorGraphAdapter.getUpdateApproachOptionKey();
					UpdateApproach approach = optionGetter.getOption(updateApproachKey);
					
					IOptionKey<Double> optimizedUpdateSparseThresholdKey = _sFactorGraphAdapter.getOptimizedUpdateSparseThresholdKey();
					Double sparseThreshold = optionGetter.getOption(optimizedUpdateSparseThresholdKey);
					
					IOptionKey<Double> automaticExecutionTimeScalingFactorKey = _sFactorGraphAdapter.getAutomaticExecutionTimeScalingFactorKey();
					Double executionTimeScalingFactor = optionGetter.getOption(automaticExecutionTimeScalingFactorKey);
					
					IOptionKey<Double> automaticMemoryAllocationScalingFactorKey = _sFactorGraphAdapter.getAutomaticMemoryAllocationScalingFactorKey();
					Double memoryAllocationScalingFactor = optionGetter.getOption(automaticMemoryAllocationScalingFactorKey);
					
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
							factorTableUpdateSettings.getApproach() != valueOrDefault(approach, updateApproachKey)
								|| factorTableUpdateSettings.getSparseThreshold() != valueOrDefault(sparseThreshold,
									optimizedUpdateSparseThresholdKey)
								|| factorTableUpdateSettings.getExecutionTimeScalingFactor() != valueOrDefault(
									executionTimeScalingFactor, automaticExecutionTimeScalingFactorKey)
								|| factorTableUpdateSettings.getMemoryAllocationScalingFactor() != valueOrDefault(
									memoryAllocationScalingFactor,
									automaticMemoryAllocationScalingFactorKey);
						if (different)
						{
							DimpleEnvironment.logWarning(
									"Options that influence the factor update algorithm and the optimized update " +
									"algorithm are set inconsistently on multiple factors that share a factor table. " +
									"Options from factor '%s' are being ignored.",
									factor.getLabel());
						}
					}
					// Implicitly ignore the case where the factor options are specified at a
					// less-specific level than previously.

					factorTableUpdateSettings.setCount(factorTableUpdateSettings.getCount() + 1);

					if (updateFactorTableSettings)
					{
						factorTableUpdateSettings.setLevel(minLevel);
						factorTableUpdateSettings.setApproach(valueOrDefault(approach, updateApproachKey));
						factorTableUpdateSettings.setSparseThreshold(valueOrDefault(sparseThreshold,
							optimizedUpdateSparseThresholdKey));
						factorTableUpdateSettings.setExecutionTimeScalingFactor(valueOrDefault(executionTimeScalingFactor,
							automaticExecutionTimeScalingFactorKey));
						factorTableUpdateSettings.setMemoryAllocationScalingFactor(valueOrDefault(memoryAllocationScalingFactor,
							automaticMemoryAllocationScalingFactorKey));
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
				if (updateApproach == UpdateApproach.OPTIMIZED && factorTable.getDimensions() > 1
					|| (updateApproach == UpdateApproach.AUTOMATIC &&
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
			optimizedCosts.put(CostType.EXECUTION_TIME,
				factorTableUpdateSettings.getCount() * optimizedCosts.get(CostType.EXECUTION_TIME));
			normalCosts.put(CostType.EXECUTION_TIME, factorTableUpdateSettings.getCount() * normalCosts.get(CostType.EXECUTION_TIME));
			// For the optimized algorithm only, multiply the allocation costs by the lesser
			// of the core count or the quantity of factors using the table.
			int scale = Math.min(workers, factorTableUpdateSettings.getCount());
			optimizedCosts.put(CostType.MEMORY, optimizedCosts.get(CostType.MEMORY) * scale);
			// For both algorithms, divide the execution time costs by the same scaling
			// factor.
			optimizedCosts.put(CostType.EXECUTION_TIME, optimizedCosts.get(CostType.EXECUTION_TIME) / scale);
			normalCosts.put(CostType.EXECUTION_TIME, normalCosts.get(CostType.EXECUTION_TIME) / scale);
			final double optimizedCost =
				computeCost(optimizedCosts, factorTableUpdateSettings.getExecutionTimeScalingFactor(),
					factorTableUpdateSettings.getMemoryAllocationScalingFactor());
			final double normalCost =
				computeCost(normalCosts, factorTableUpdateSettings.getExecutionTimeScalingFactor(),
					factorTableUpdateSettings.getMemoryAllocationScalingFactor());
			createOptimizedUpdatePlan = optimizedCost <= normalCost;
		}
		factorTableUpdateSettings.setAutomaticUpdateApproach(createOptimizedUpdatePlan ?
			UpdateApproach.OPTIMIZED : UpdateApproach.NORMAL);
		return createOptimizedUpdatePlan;
	}

	private static double computeCost(final Costs costs,
		final double executionTimeScalingFactor,
		final double memoryAllocationScalingFactor)
	{
		final double executionTime = costs.get(CostType.EXECUTION_TIME);
		final double memory = costs.get(CostType.MEMORY);
		final double j = executionTime * executionTimeScalingFactor + memory * memoryAllocationScalingFactor;
		return j;
	}
}
