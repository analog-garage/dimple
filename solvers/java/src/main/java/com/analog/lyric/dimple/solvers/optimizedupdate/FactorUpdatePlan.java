/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.solvers.optimizedupdate;

import java.util.ArrayList;
import java.util.List;

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.math.Utilities;
import com.analog.lyric.util.misc.Internal;

/**
 * A plan for updating a factor. Each factor table has a unique plan, but factors that share a
 * factor table share a plan. A plan consists of a list of steps to perform to an actual factor,
 * which provides the actual edge messages and other per-factor properties.
 * 
 * @since 0.06
 * @author jking
 */
@Internal
public final class FactorUpdatePlan
{
	private final List<IUpdateStep> _steps;

	private FactorUpdatePlan(List<IUpdateStep> steps)
	{
		_steps = steps;
	}

	/**
	 * Constructs an update plan for a given factor table. The same plan may be applied to any
	 * factor that uses the same factor table.
	 * 
	 * @since 0.06
	 */
	public static FactorUpdatePlan
		create(final IFactorTable factorTable, final ISFactorGraphToOptimizedUpdateAdapter tableWrapperAdapter, double sparseThreshold)
	{
		final int n = factorTable.getDimensions();
		// Initial capacity is just an estimate.
		final List<IUpdateStep> steps = new ArrayList<IUpdateStep>((int) (n * Utilities.log2(n)));
		ITreeBuilder<TableWrapper> builder = new TreeBuilder(steps, tableWrapperAdapter, sparseThreshold);
		TreeWalker<TableWrapper> treeWalker = new TreeWalker<TableWrapper>(factorTable);
		treeWalker.accept(builder);
		return new FactorUpdatePlan(steps);
	}

	private static class TreeBuilder implements ITreeBuilder<TableWrapper>
	{
		private final List<IUpdateStep> _steps;

		private final ISFactorGraphToOptimizedUpdateAdapter _tableWrapperHelper;
		
		private final double _sparseThreshold;

		public TreeBuilder(List<IUpdateStep> steps, final ISFactorGraphToOptimizedUpdateAdapter tableWrapperAdapter, double sparseThreshold)
		{
			_steps = steps;
			_tableWrapperHelper = tableWrapperAdapter;
			_sparseThreshold = sparseThreshold;
		}

		@Override
		public TableWrapper createRootT(IFactorTable rootFactorTable)
		{
			return new TableWrapper(rootFactorTable, _tableWrapperHelper, _sparseThreshold);
		}

		@Override
		public TableWrapper buildMarginalize(TableWrapper g, int portNum, int localDimension)
		{
			final IMarginalizationStep marginalizeStep = g.createMarginalizationStep(portNum, localDimension);
			_steps.add(marginalizeStep);
			return marginalizeStep.getAuxiliaryTable();
		}

		@Override
		public void buildOutput(TableWrapper g, int portNum)
		{
			IUpdateStep outputStep = g.createOutputStep(portNum);
			_steps.add(outputStep);
		}
	}

	/**
	 * Applies an update plan to a factor. The factor must use the same factor table as used to
	 * construct the plan.
	 * 
	 * @since 0.06
	 */
	public void apply(ISTableFactorSupportingOptimizedUpdate tableFactor)
	{
		for (final IUpdateStep step : _steps)
		{
			step.apply(tableFactor);
		}
	}

	/**
	 * Estimates the costs of applying the optimized factor update algorithm to a factor.
	 * <p>
	 * The estimation assumes that the content of sparse tables is distributed randomly throughout
	 * the table, rather than examining the actual locations of the data. Using this assumption,
	 * the density of each auxiliary table is computed without actually populating their data.
	 * 
	 * @param factorTable A factor table that describes the factor.
	 * @return The estimated costs.
	 * @since 0.07
	 */
	public static Costs estimateOptimizedUpdateCosts(IFactorTable factorTable,
		final ISFactorGraphToOptimizedUpdateAdapter tableWrapperHelper,
		final double sparseThrehsold)
	{
		final Costs costs = new Costs();
		ITreeBuilder<CostEstimationTableWrapper> builder =
			new EstimationTreeBuilder(costs, tableWrapperHelper, sparseThrehsold);
		TreeWalker<CostEstimationTableWrapper> treeWalker = new TreeWalker<CostEstimationTableWrapper>(factorTable);
		treeWalker.accept(builder);
		return costs;
	}

	private static class EstimationTreeBuilder implements ITreeBuilder<CostEstimationTableWrapper>
	{
		private final ISFactorGraphToOptimizedUpdateAdapter _tableWrapperHelper;

		private final Costs _result;

		private final double _sparseThreshold;

		public EstimationTreeBuilder(Costs result,
			ISFactorGraphToOptimizedUpdateAdapter tableWrapperHelper,
			double sparseThreshold)
		{
			_result = result;
			_tableWrapperHelper = tableWrapperHelper;
			_sparseThreshold = sparseThreshold;
		}

		@Override
		public CostEstimationTableWrapper createRootT(IFactorTable rootFactorTable)
		{
			return new CostEstimationTableWrapper(rootFactorTable, _tableWrapperHelper, _sparseThreshold);
		}

		@Override
		public CostEstimationTableWrapper
			buildMarginalize(CostEstimationTableWrapper g, int portNum, int localDimension)
		{
			final IMarginalizationStepEstimator marginalizeStep = g.createMarginalizationStep(portNum, localDimension);
			_result.add(marginalizeStep.estimateCosts());
			return marginalizeStep.getAuxiliaryTable();
		}

		@Override
		public void buildOutput(CostEstimationTableWrapper g, int portNum)
		{
			IUpdateStepEstimator outputStep = g.createOutputStep(portNum);
			_result.add(outputStep.estimateCosts());
		}
	}
}
