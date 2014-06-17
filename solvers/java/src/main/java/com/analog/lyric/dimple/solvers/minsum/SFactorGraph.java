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
import com.analog.lyric.dimple.solvers.optimizedupdate.FactorUpdatePlan;
import com.analog.lyric.dimple.solvers.optimizedupdate.IMarginalizationStepEstimator;
import com.analog.lyric.dimple.solvers.optimizedupdate.ISFactorGraphToCostEstimationTableWrapperAdapter;
import com.analog.lyric.dimple.solvers.optimizedupdate.ISFactorGraphToCostOptimizerAdapter;
import com.analog.lyric.dimple.solvers.optimizedupdate.IUpdateStepEstimator;
import com.analog.lyric.dimple.solvers.optimizedupdate.SFactorGraphOptimizedUpdateImpl;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateApproach;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateCostOptimizer;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateSettings;


public class SFactorGraph extends SFactorGraphBase
{
	protected double _damping = 0;
	final SFactorGraphOptimizedUpdateImpl _optimizedUpdateImpl = new SFactorGraphOptimizedUpdateImpl(this);

	public SFactorGraph(com.analog.lyric.dimple.model.core.FactorGraph factorGraph)
	{
		super(factorGraph);
		setMultithreadingManager(new MultiThreadingManager(getModelObject()));
	}
	
	@Override
	public void initialize()
	{
		_optimizedUpdateImpl.clearOptimizedUpdatePlans();
		_damping = getOptionOrDefault(MinSumOptions.damping);
		super.initialize();
		UpdateCostOptimizer.optimize(_factorGraph, _costOptimizerHelper);
	}
	
	@Override
	public ISolverVariable createVariable(com.analog.lyric.dimple.model.variables.VariableBase var)
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

	/**
	 * Gets the update algorithm approach.
	 * 
	 * @since 0.07
	 */
	public UpdateApproach getUpdateApproach()
	{
		return _optimizedUpdateImpl.getUpdateApproach();
	}

	/**
	 * Sets the update algorithm approach.
	 * 
	 * @since 0.07
	 */
	public void setUpdateApproach(UpdateApproach approach)
	{
		_optimizedUpdateImpl.setUpdateApproach(approach);
	}

	/**
	 * Sets the optimized update sparse threshold. The optimized update algorithm uses auxiliary
	 * factor tables during update. This density setting determines whether it uses sparse or dense
	 * representations for them. Sparse representations often offer superior execution time, but use
	 * more memory because indices are stored. The automatic update approach considers the impact of
	 * this setting when estimating update cost for the optimized update algorithm.
	 * 
	 * @param value A density, below which the system uses a sparse representation for auxiliary
	 *        factor tables.
	 * @since 0.07
	 */
	public void setOptimizedUpdateSparseThreshold(double value)
	{
		_optimizedUpdateImpl.setOptimizedUpdateSparseThreshold(value);
	}

	/**
	 * Gets the optimized update sparse threshold.
	 * 
	 * @see #setOptimizedUpdateSparseThreshold(double)
	 * @since 0.07
	 */
	public double getOptimizedUpdateSparseThreshold()
	{
		return _optimizedUpdateImpl.getOptimizedUpdateSparseThreshold();
	}

	/**
	 * When the update approach is automatic, the system chooses which update algorithm to use by
	 * estimating the execution time and memory allocation of each. The memory allocation estimate
	 * is scaled by this factor in the cost estimation.
	 * 
	 * @since 0.07
	 */
	public void setAutomaticMemoryAllocationScalingFactor(double value)
	{
		_optimizedUpdateImpl.setAutomaticMemoryAllocationScalingFactor(value);
	}

	/**
	 * @see #setAutomaticMemoryAllocationScalingFactor(double)
	 * @since 0.07
	 */
	public double getAutomaticMemoryAllocationScalingFactor()
	{
		return _optimizedUpdateImpl.getAutomaticMemoryAllocationScalingFactor();
	}

	/**
	 * When the update approach is automatic, the system chooses which update algorithm to use by
	 * estimating the execution time and memory allocation of each. The execution time estimate
	 * is scaled by this factor in the cost estimation.
	 * 
	 * @since 0.07
	 */
	public void setAutomaticExecutionTimeScalingFactor(double value)
	{
		_optimizedUpdateImpl.setAutomaticExecutionTimeScalingFactor(value);
	}

	/**
	 * @see #setAutomaticExecutionTimeScalingFactor(double)
	 * @since 0.07
	 */
	public double getAutomaticExecutionTimeScalingFactor()
	{
		return _optimizedUpdateImpl.getAutomaticExecutionTimeScalingFactor();
	}
	
	@Override
	protected void doUpdateEdge(int edge)
	{
	}

	private final ISFactorGraphToCostOptimizerAdapter _costOptimizerHelper = new ISFactorGraphToCostOptimizerAdapter() {

		private final ISFactorGraphToCostEstimationTableWrapperAdapter _helper = new ISFactorGraphToCostEstimationTableWrapperAdapter() {

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
			public UpdateSettings getFactorTableUpdateSettings(IFactorTable factorTable)
			{
				return _optimizedUpdateImpl.getUpdateSettingsForFactorTable(factorTable);
			}
		};

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
		public Costs estimateCostOfOptimizedUpdate(IFactorTable factorTable)
		{
			return FactorUpdatePlan.estimateCosts(factorTable, _helper);
		}

		@Override
		public int getWorkers(FactorGraph factorGraph)
		{
			SFactorGraph sfg = (SFactorGraph) factorGraph.getSolver();
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
		public UpdateSettings getFactorTableUpdateSettings(IFactorTable factorTable)
		{
			return _optimizedUpdateImpl.getUpdateSettingsForFactorTable(factorTable);
		}

	};
	
}
