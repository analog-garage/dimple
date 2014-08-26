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
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.util.misc.Internal;

/**
 * Chooses the normal or optimized update algorithm for each of the factors in a factor graph,
 * based on a cost computed from an estimate of the execution time and memory allocation used by
 * each of the algorithms.
 * <p>
 * Only those factors that have all of their edges updated together according to the schedule are
 * considered, as the optimized update algorithm does not apply to edge-by-edge updates.
 * <p>
 * When multithreading is enabled on the graph, allocation costs are multiplied by the lesser of the
 * worker count or the quantity of factors sharing a factor table, and execution time costs are
 * divided by the same quantity.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public final class UpdateCostOptimizer
{
	/**
	 * TODO
	 * Chooses the normal or optimized update algorithm for the graph's factors. For those factors
	 * Applies the resulting decision to each
	 * factor table's
	 * "automatic optimization decision" setting.
	 * 
	 * @since 0.07
	 */
	public static void optimize(final FactorGraph factorGraph,
		final ISFactorGraphToCostOptimizerAdapter sFactorGraphAdapter)
	{
		final int workers = sFactorGraphAdapter.getWorkers(factorGraph);

		final Map<IFactorTable, FactorTableUpdateSettings> settingsByFactorTable = new IdentityHashMap<>();
		sFactorGraphAdapter.putFactorTableUpdateSettings(settingsByFactorTable);

		final ISchedule schedule = factorGraph.getSchedule();
		final int[] level = new int[1];
		final IScheduledActivity updatesCollector = new IScheduledActivity() {

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
					IFactorTable factorTable = factor.getFactorTable();
					STableFactorBase sTableFactor = (STableFactorBase) factor.getSolver();
					if (sTableFactor != null)
					{
						int minLevel = Integer.MAX_VALUE;

						level[0] = Integer.MAX_VALUE;
						UpdateApproach approach =
							sTableFactor.getOptionUptoDepth(UpdateOptions.updateApproach, level[0], level);
						if (level[0] < minLevel)
							minLevel = level[0];
						level[0] = Integer.MAX_VALUE;
						Double sparseThreshold =
							sTableFactor.getOptionUptoDepth(UpdateOptions.optimizedUpdateSparseThreshold, level[0],
								level);
						if (level[0] < minLevel)
							minLevel = level[0];
						level[0] = Integer.MAX_VALUE;
						Double executionTimeScalingFactor =
							sTableFactor.getOptionUptoDepth(UpdateOptions.automaticExecutionTimeScalingFactor,
								level[0], level);
						if (level[0] < minLevel)
							minLevel = level[0];
						level[0] = Integer.MAX_VALUE;
						Double memoryAllocationScalingFactor =
							sTableFactor.getOptionUptoDepth(UpdateOptions.automaticMemoryAllocationScalingFactor,
								level[0], level);
						if (level[0] < minLevel)
							minLevel = level[0];

						boolean updateFactorTableSettings = false;
						FactorTableUpdateSettings factorTableUpdateSettings = settingsByFactorTable.get(factorTable);
						if (factorTableUpdateSettings == null)
						{
							factorTableUpdateSettings = new FactorTableUpdateSettings();
							settingsByFactorTable.put(factorTable, factorTableUpdateSettings);
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
		};
		ScheduleVisitor.visit(schedule, ScheduleVisitorFactorFilter.create(updatesCollector));

		for (Entry<IFactorTable, FactorTableUpdateSettings> entry : settingsByFactorTable.entrySet())
		{
			final IFactorTable factorTable = entry.getKey();
			final FactorTableUpdateSettings optionsValue = entry.getValue();
			boolean createOptimizedUpdatePlan = false;
			if (optionsValue.getApproach() == UpdateApproach.UPDATE_APPROACH_OPTIMIZED)
			{
				createOptimizedUpdatePlan = true;
			}
			if (optionsValue.getApproach() == UpdateApproach.UPDATE_APPROACH_AUTOMATIC)
			{
				if (factorTable.getDimensions() > 1)
				{
					Costs optimizedCosts =
						sFactorGraphAdapter.estimateCostOfOptimizedUpdate(factorTable,
							optionsValue.getSparseThreshold());
					Costs normalCosts = sFactorGraphAdapter.estimateCostOfNormalUpdate(factorTable);
					// Multiply the execution time costs by the quantity of factors using the table
					optimizedCosts.put(CostType.ACCESSES,
						optionsValue.getCount() * optimizedCosts.get(CostType.ACCESSES));
					normalCosts.put(CostType.ACCESSES, optionsValue.getCount() * normalCosts.get(CostType.ACCESSES));
					// For the optimized algorithm only, multiply the allocation costs by the lesser
					// of the core count or the quantity of factors using the table.
					int scale = Math.min(workers, optionsValue.getCount());
					optimizedCosts.put(CostType.ALLOCATED_BYTES, optimizedCosts.get(CostType.ALLOCATED_BYTES) * scale);
					// For both algorithms, divide the execution time costs by the same scaling
					// factor.
					optimizedCosts.put(CostType.ACCESSES, optimizedCosts.get(CostType.ACCESSES) / scale);
					normalCosts.put(CostType.ACCESSES, normalCosts.get(CostType.ACCESSES) / scale);
					createOptimizedUpdatePlan =
						computeCost(optimizedCosts, optionsValue.getExecutionTimeScalingFactor(),
							optionsValue.getMemoryAllocationScalingFactor()) <= computeCost(normalCosts,
							optionsValue.getExecutionTimeScalingFactor(),
							optionsValue.getMemoryAllocationScalingFactor());
				}
				optionsValue.setAutomaticOptimizationDecision(createOptimizedUpdatePlan);
			}
			FactorUpdatePlan updatePlan = null;
			if (createOptimizedUpdatePlan)
			{
				final ITableWrapperAdapter tableWrapperAdapter =
					sFactorGraphAdapter.getTableWrapperAdapter(optionsValue.getSparseThreshold());
				updatePlan = FactorUpdatePlan.create(factorTable, tableWrapperAdapter);
			}
			optionsValue.setOptimizedUpdatePlan(updatePlan);
		}
	}

	/**
	 * TODO
	 * 
	 * @since 0.07
	 */
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
