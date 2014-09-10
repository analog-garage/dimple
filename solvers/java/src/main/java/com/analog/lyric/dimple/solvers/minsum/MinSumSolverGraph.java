/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.solvers.minsum;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.factorfunctions.core.CustomFactorFunctionWrapper;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.dimple.solvers.minsum.customFactors.CustomXor;
import com.analog.lyric.dimple.solvers.optimizedupdate.CostEstimationTableWrapper;
import com.analog.lyric.dimple.solvers.optimizedupdate.CostType;
import com.analog.lyric.dimple.solvers.optimizedupdate.Costs;
import com.analog.lyric.dimple.solvers.optimizedupdate.FactorTableUpdateSettings;
import com.analog.lyric.dimple.solvers.optimizedupdate.FactorUpdatePlan;
import com.analog.lyric.dimple.solvers.optimizedupdate.IMarginalizationStep;
import com.analog.lyric.dimple.solvers.optimizedupdate.IMarginalizationStepEstimator;
import com.analog.lyric.dimple.solvers.optimizedupdate.ISFactorGraphToOptimizedUpdateAdapter;
import com.analog.lyric.dimple.solvers.optimizedupdate.IUpdateStep;
import com.analog.lyric.dimple.solvers.optimizedupdate.IUpdateStepEstimator;
import com.analog.lyric.dimple.solvers.optimizedupdate.TableWrapper;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateCostOptimizer;

/**
 * Solver-specific factor graph for min-sum solver.
 * <p>
 * <em>Previously was com.analog.lyric.dimple.solvers.minsum.SFactorGraph</em>
 *  <p>
 * @since 0.07
 */
public class MinSumSolverGraph extends SFactorGraphBase
{
	protected double _damping = 0;

	public MinSumSolverGraph(FactorGraph factorGraph)
	{
		super(factorGraph);
		setMultithreadingManager(new MultiThreadingManager(getModelObject()));
	}

	@Override
	public void initialize()
	{
		UpdateCostOptimizer optimizer = new UpdateCostOptimizer(_optimizedUpdateAdapter);
		optimizer.optimize(_factorGraph);
		_damping = getOptionOrDefault(MinSumOptions.damping);
		super.initialize();
	}
	
	@Override
	public ISolverVariable createVariable(com.analog.lyric.dimple.model.variables.Variable var)
	{
		if (!var.getDomain().isDiscrete())
			throw new DimpleException("only support discrete variables");
		
		return new SVariable(var);
	}


	@Override
	public ISolverFactor createFactor(Factor factor)
	{
		FactorFunction factorFunction = factor.getFactorFunction().getContainedFactorFunction();	// In case it's wrapped
		String factorName = factorFunction.getName();
		boolean noFF = factorFunction instanceof CustomFactorFunctionWrapper;

		
		// First see if any custom factor should be created
		if (factorFunction instanceof Xor)
		{
			return new CustomXor(factor);
		}
		else if (noFF && (factorName.equals("CustomXor") || factorName.equals("customXor")))		// For backward compatibility
		{
			return new CustomXor(factor);
		}else			// No custom factor exists, so create a generic one
		{
			return new STableFactor(factor);
		}
	}
	
	// For backward compatibility only; preferable to use "Xor" factor function, which can
	// be evaluated for scoring or other purposes, but still uses the custom factor.  This may be removed at some point.
	// This should return true only for custom factors that do not have a corresponding FactorFunction of the same name
	@Override
	public boolean customFactorExists(String funcName)
	{
		if (funcName.equals("CustomXor") || funcName.equals("customXor"))
			return true;
		else
			return false;
	}

	/*
	 * Set the global solver damping parameter.  We have to go through all factor graphs
	 * and update the damping parameter on all existing table functions in that graph.
	 */
	public void setDamping(double damping)
	{
		setOption(MinSumOptions.damping, damping);
		_damping = damping;
	}
	
	public double getDamping()
	{
		return _damping;
	}

	/**
	 * Indicates if this solver supports the optimized update algorithm.
	 * 
	 * @since 0.06
	 */
	public boolean isOptimizedUpdateSupported()
	{
		return true;
	}

	/*
	 * 
	 */
	@Override
	protected void doUpdateEdge(int edge)
	{
	}

	private final ISFactorGraphToOptimizedUpdateAdapter _optimizedUpdateAdapter = new ISFactorGraphToOptimizedUpdateAdapter() {

		@Override
		public IUpdateStepEstimator createSparseOutputStepEstimator(CostEstimationTableWrapper tableWrapper)
		{
			return new TableFactorEngineOptimized.SparseOutputStepEstimator(tableWrapper);
		}

		@Override
		public IUpdateStepEstimator createDenseOutputStepEstimator(CostEstimationTableWrapper tableWrapper)
		{
			return new TableFactorEngineOptimized.DenseOutputStepEstimator(tableWrapper);
		}

		@Override
		public IMarginalizationStepEstimator
			createSparseMarginalizationStepEstimator(CostEstimationTableWrapper tableWrapper,
				int inPortNum,
				int dimension,
				CostEstimationTableWrapper g)
		{
			return new TableFactorEngineOptimized.SparseMarginalizationStepEstimator(tableWrapper, inPortNum,
				dimension, g);
		}

		@Override
		public IMarginalizationStepEstimator
			createDenseMarginalizationStepEstimator(CostEstimationTableWrapper tableWrapper,
				int inPortNum,
				int dimension,
				CostEstimationTableWrapper g)
		{
			return new TableFactorEngineOptimized.DenseMarginalizationStepEstimator(tableWrapper, inPortNum,
				dimension, g);
		}

		@Override
		public Costs estimateCostOfNormalUpdate(IFactorTable factorTable)
		{
			Costs result = new Costs();
			int numPorts = factorTable.getDimensions();
			for (int outPortNum = 0; outPortNum < numPorts; outPortNum++)
			{
				result.add(estimateCost_updateEdge(factorTable, outPortNum));
			}
			return result;
		}

		private Costs estimateCost_updateEdge(IFactorTable factorTable, int outPortNum)
		{
			long accesses = 0;
			int nonZeroEntries = factorTable.countNonZeroWeights();
			int numPorts = factorTable.getDimensions();
			int outputMsg_length = factorTable.getDomainIndexer().getDomainSize(outPortNum);
			// 1. set each output message entry to +infinity
			// 2. read each output message value to find the minimum
			// 3. read each output message, subtract the minimum, and
			// 4. rewrite it
			accesses += 4 * outputMsg_length;
			// for each table entry:
			// 1. read the entry's indices
			accesses += nonZeroEntries;
			// 2. read the entry's value
			accesses += nonZeroEntries;
			// 3. for each port:
			// 3.1. read the input message from the port
			// 3.2. read the index for the port
			// 3.3. read the input message value at the port's index
			// 3.4. read the output message from each port
			// 3.5. read the output message index for the port
			// 3.6. read the input message for the port
			// 3.7. read the value from the input message
			// 3.8. read the value from the output message
			accesses += nonZeroEntries * numPorts * 8;
			// 3.9. if smaller, store the new output message value
			// ignored in the estimate, but much smaller than the above values
			Costs result = new Costs();
			result.put(CostType.ACCESSES, (double) accesses);
			return result;
		}

		@Override
		public Costs estimateCostOfOptimizedUpdate(IFactorTable factorTable, final double sparseThreshold)
		{
			return FactorUpdatePlan.estimateCosts(factorTable, this, sparseThreshold);
		}

		@Override
		public int getWorkers(FactorGraph factorGraph)
		{
			MinSumSolverGraph sfg = (MinSumSolverGraph) factorGraph.getSolver();
			if (sfg != null && sfg.useMultithreading())
			{
				return sfg.getMultithreadingManager().getNumWorkers();
			}
			else
			{
				return 1;
			}
		}

		@Override
		public void
			putFactorTableUpdateSettings(Map<IFactorTable, FactorTableUpdateSettings> optionsValueByFactorTable)
		{
			_factorTableUpdateSettings = optionsValueByFactorTable;
		}

		@Override
		public double[] getSparseValues(IFactorTable factorTable)
		{
			return factorTable.getEnergiesSparseUnsafe();
		}

		@Override
		public double[] getDenseValues(IFactorTable factorTable)
		{
			return factorTable.getEnergiesDenseUnsafe();
		}

		@Override
		public IUpdateStep createSparseOutputStep(int outPortNum, TableWrapper tableWrapper)
		{
			return new TableFactorEngineOptimized.SparseOutputStep(outPortNum, tableWrapper);
		}

		@Override
		public IUpdateStep createDenseOutputStep(int outPortNum, TableWrapper tableWrapper)
		{
			return new TableFactorEngineOptimized.DenseOutputStep(outPortNum, tableWrapper);
		}

		@Override
		public IMarginalizationStep createSparseMarginalizationStep(TableWrapper tableWrapper,
			int inPortNum,
			int dimension,
			IFactorTable g_factorTable,
			Tuple2<int[][], int[]> g_and_msg_indices)
		{
			return new TableFactorEngineOptimized.SparseMarginalizationStep(tableWrapper, this, inPortNum,
				dimension, g_factorTable, g_and_msg_indices);
		}

		@Override
		public IMarginalizationStep createDenseMarginalizationStep(TableWrapper tableWrapper,
			int inPortNum,
			int dimension,
			IFactorTable g_factorTable)
		{
			return new TableFactorEngineOptimized.DenseMarginalizationStep(tableWrapper, this, inPortNum,
				dimension, g_factorTable);
		}
	};
	
	private @Nullable Map<IFactorTable, FactorTableUpdateSettings> _factorTableUpdateSettings;

	@Nullable FactorTableUpdateSettings getFactorTableUpdateSettings(Factor factor)
	{
		final Map<IFactorTable, FactorTableUpdateSettings> map = _factorTableUpdateSettings;
		FactorTableUpdateSettings result = null;
		if (map != null && factor.hasFactorTable())
		{
			IFactorTable factorTable = factor.getFactorTable();
			result = map.get(factorTable);
		}
		return result;
	}

}
