/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.solvers.optimizedupdate;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.schedulers.IScheduledActivity;
import com.analog.lyric.dimple.schedulers.ScheduleVisitor;
import com.analog.lyric.dimple.schedulers.ScheduleVisitorFactorFilter;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
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
	 * Chooses the normal or optimized update algorithm for the graph's factor tables that have their optimized update
	 * approach set to automatic, and that have degree > 1. Applies the resulting decision to each factor table's
	 * "automatic optimization decision" setting.
	 * 
	 * @since 0.07
	 */
	public static void optimize(final FactorGraph factorGraph, final ISFactorGraphToCostOptimizerAdapter sFactorGraphAdapter)
	{
		final int workers = sFactorGraphAdapter.getWorkers(factorGraph);
		for (Entry<IFactorTable, Integer> entry : collectFactorTableUpdates(factorGraph).entrySet())
		{
			final IFactorTable factorTable = entry.getKey();
			final UpdateSettings settings = sFactorGraphAdapter.getFactorTableUpdateSettings(factorTable);
			if (settings.getUpdateApproach() == UpdateApproach.UPDATE_APPROACH_AUTOMATIC)
			{
				final Integer count = entry.getValue();
				final boolean automaticOptimizationDecision;
				if (factorTable.getDimensions() > 1)
				{
					Costs optimizedCosts = sFactorGraphAdapter.estimateCostOfOptimizedUpdate(factorTable);
					Costs normalCosts = sFactorGraphAdapter.estimateCostOfNormalUpdate(factorTable);
					// Multiply the execution time costs by the quantity of factors using the table
					optimizedCosts.put(CostType.ACCESSES, count * optimizedCosts.get(CostType.ACCESSES));
					normalCosts.put(CostType.ACCESSES, count * normalCosts.get(CostType.ACCESSES));
					// For the optimized algorithm only, multiply the allocation costs by the lesser
					// of the core count or the quantity of factors using the table.
					int scale = Math.min(workers, count);
					optimizedCosts.put(CostType.ALLOCATED_BYTES, optimizedCosts.get(CostType.ALLOCATED_BYTES) * scale);
					// For both algorithms, divide the execution time costs by the same scaling factor.
					optimizedCosts.put(CostType.ACCESSES, optimizedCosts.get(CostType.ACCESSES) / scale);
					normalCosts.put(CostType.ACCESSES, normalCosts.get(CostType.ACCESSES) / scale);
					automaticOptimizationDecision =	computeCost(optimizedCosts, settings) <= computeCost(normalCosts, settings);
				}
				else
				{
					automaticOptimizationDecision = false;
				}
				settings.setAutomaticOptimizationDecision(automaticOptimizationDecision);
			}
		}
	}

	/**
	 * Walks the schedule and returns a map from factor table to count of how many times update
	 * is applied to a factor using that factor table.
	 * 
	 * @since 0.07
	 */
	private static Map<IFactorTable, Integer> collectFactorTableUpdates(final FactorGraph factorGraph)
	{
		final Map<IFactorTable, Integer> factorTableUpdates = new IdentityHashMap<IFactorTable, Integer>();
		final ISchedule schedule = factorGraph.getSchedule();
		final IScheduledActivity updatesCollector = new IScheduledActivity() {
			
			@Override
			public void updateEdge(INode node, int portNum)
			{
				// Edge updates are ignored.
			}
			
			@Override
			public void update(INode node)
			{
				Factor factor = node.asFactor();
				if (factor != null && factor.hasFactorTable())
				{
					IFactorTable factorTable = factor.getFactorTable();
					Integer count = factorTableUpdates.get(factorTable);
					if (count == null)
					{
						count = 0;
					}
					count += 1;
					factorTableUpdates.put(factorTable, count);
				}
			}
		};
		ScheduleVisitor.visit(schedule, ScheduleVisitorFactorFilter.create(updatesCollector));
		return factorTableUpdates;
	}

	/**
	 * Returns a linear combination of costs with scaling factors from settings.
	 * 
	 * @since 0.07
	 */
	private static double computeCost(final Costs costs, final UpdateSettings settings)
	{
		final double accesses = costs.get(CostType.ACCESSES);
		final double allocations = costs.get(CostType.ALLOCATED_BYTES);
		final double executionTimeScale = settings.getAutomaticExecutionTimeScalingFactor();
		final double allocationScale = settings.getAutomaticMemoryAllocationScalingFactor();
		final double j = accesses * executionTimeScale + allocations * allocationScale;
		return j;
	}

}
