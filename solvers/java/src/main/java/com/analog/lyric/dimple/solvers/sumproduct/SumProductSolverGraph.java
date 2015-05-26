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

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.Map;
import java.util.Random;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.factorfunctions.ComplexNegate;
import com.analog.lyric.dimple.factorfunctions.ComplexSubtract;
import com.analog.lyric.dimple.factorfunctions.ComplexSum;
import com.analog.lyric.dimple.factorfunctions.FiniteFieldAdd;
import com.analog.lyric.dimple.factorfunctions.FiniteFieldMult;
import com.analog.lyric.dimple.factorfunctions.FiniteFieldProjection;
import com.analog.lyric.dimple.factorfunctions.LinearEquation;
import com.analog.lyric.dimple.factorfunctions.MatrixRealJointVectorProduct;
import com.analog.lyric.dimple.factorfunctions.Multiplexer;
import com.analog.lyric.dimple.factorfunctions.MultivariateNormal;
import com.analog.lyric.dimple.factorfunctions.Negate;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.Product;
import com.analog.lyric.dimple.factorfunctions.RealJointNegate;
import com.analog.lyric.dimple.factorfunctions.RealJointSubtract;
import com.analog.lyric.dimple.factorfunctions.RealJointSum;
import com.analog.lyric.dimple.factorfunctions.Subtract;
import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.factorfunctions.core.CustomFactorFunctionWrapper;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.FiniteFieldVariable;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.options.DimpleOptions;
import com.analog.lyric.dimple.solvers.core.BPSolverGraph;
import com.analog.lyric.dimple.solvers.core.NoSolverEdge;
import com.analog.lyric.dimple.solvers.core.ParameterEstimator;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.interfaces.ISolverEdgeState;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;
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
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateApproach;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateCostOptimizer;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomComplexGaussianPolynomial;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomFiniteFieldAdd;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomFiniteFieldConstantMult;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomFiniteFieldMult;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomFiniteFieldProjection;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianLinear;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianLinearEquation;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianNegate;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianProduct;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianSubtract;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomGaussianSum;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultiplexer;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultivariateGaussianNegate;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultivariateGaussianProduct;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultivariateGaussianSubtract;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultivariateGaussianSum;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomMultivariateNormalConstantParameters;
import com.analog.lyric.dimple.solvers.sumproduct.customFactors.CustomNormalConstantParameters;
import com.analog.lyric.dimple.solvers.sumproduct.sampledfactor.SampledFactor;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.options.IOptionKey;

/**
 * Solver representation of factor graph under Sum-Product solver.
 * 
 * @since 0.07
 */
public class SumProductSolverGraph extends BPSolverGraph<ISolverFactor,ISolverVariable,ISolverEdgeState>
{
	private double _damping = 0;
	private @Nullable IFactorTable _currentFactorTable = null;
	private static Random _rand = new Random();


	public SumProductSolverGraph(FactorGraph factorGraph, @Nullable ISolverFactorGraph parent)
	{
		super(factorGraph, parent);
		setMultithreadingManager(new MultiThreadingManager(this));
		
		// Set default Gibbs options for sampled factors.
		setOption(GibbsOptions.numSamples, SampledFactor.DEFAULT_SAMPLES_PER_UPDATE);
		setOption(GibbsOptions.burnInScans, SampledFactor.DEFAULT_BURN_IN_SCANS_PER_UPDATE);
		setOption(GibbsOptions.scansPerSample, SampledFactor.DEFAULT_SCANS_PER_SAMPLE);
	}

	@Override
	public boolean hasEdgeState()
	{
		return true;
	}

	@Override
	public ISolverEdgeState createEdgeState(EdgeState edge)
	{
		final ISolverFactor sfactor = getSolverFactor(edge.getFactor(_model));

		ISolverEdgeState sedge = sfactor.createEdge(edge);
		
		if (sedge != null)
		{
			return sedge;
		}
		
		final Variable var = edge.getVariable(_model);
		
		if (var instanceof Discrete)
		{
			return new SumProductDiscreteEdge((Discrete)var);
		}
		else if (var instanceof Real)
		{
			return new SumProductNormalEdge();
		}
		else if (var instanceof RealJoint)
		{
			return new SumProductMultivariateNormalEdge((RealJoint)var);
		}
		
		return NoSolverEdge.INSTANCE;
		
	}
	
	@SuppressWarnings("deprecation") // TODO remove when S* classes removed
	@Override
	public ISolverVariable createVariable(Variable var)
	{
		if (var instanceof FiniteFieldVariable)
			return new SFiniteFieldVariable((FiniteFieldVariable)var, this);
		else if (var instanceof RealJoint)
			return new SRealJointVariable((RealJoint)var, this);
		else if (var instanceof Real)
			return new SRealVariable((Real)var, this);
		else if (var instanceof Discrete)
		{
			return new SDiscreteVariable((Discrete)var, this);
		}
		
		throw unsupportedVariableType(var);
	}

	

	@SuppressWarnings("deprecation") // TODO remove when STableFactor removed
	@Override
	public ISolverFactor createFactor(Factor factor)
	{
		FactorFunction factorFunction = factor.getFactorFunction().getContainedFactorFunction();	// In case it's wrapped
		String factorName = factorFunction.getName();
		boolean noFF = factorFunction instanceof CustomFactorFunctionWrapper;
		boolean hasConstants = factor.getFactorFunction().hasConstants();
		
		if (factor.isDiscrete())	// Factor contains only discrete variables
		{
			// First see if any custom factor should be created
			if (((factorFunction instanceof FiniteFieldAdd) || (noFF && factorName.equals("finiteFieldAdd"))) && !hasConstants)		// "finiteFieldAdd" for backward compatibility
				return new CustomFiniteFieldAdd(factor, this);
			else if ((factorFunction instanceof FiniteFieldMult) || (noFF && factorName.equals("finiteFieldMult")))					// "finiteFieldMult" for backward compatibility
			{
				if (hasConstants)
					return new CustomFiniteFieldConstantMult(factor, this);
				else
					return new CustomFiniteFieldMult(factor, this);
			}
			else if ((factorFunction instanceof FiniteFieldProjection) || (noFF && factorName.equals("finiteFieldProjection")))		// "finiteFieldProjection" for backward compatibility
				return new CustomFiniteFieldProjection(factor, this);
			else if ((factorFunction instanceof Multiplexer) || (noFF && factorName.equals("multiplexerCPD")))	// "multiplexerCPD" for backward compatibility
				return new CustomMultiplexer(factor, this);															// Currently only supports discrete variables
			else	// No custom factor exists, so create a generic one
			{
				// For discrete case, create a table factor
				return new STableFactor(factor, this);
			}
		}
		else	// Factor includes at least one continuous variable
		{
			// First see if any custom factor should be created
			if ((factorFunction instanceof Sum) && CustomGaussianSum.isFactorCompatible(factor))
				return new CustomGaussianSum(factor, this);
			else if ((factorFunction instanceof Subtract) && CustomGaussianSubtract.isFactorCompatible(factor))
				return new CustomGaussianSubtract(factor, this);
			else if ((factorFunction instanceof Negate) && CustomGaussianNegate.isFactorCompatible(factor))
				return new CustomGaussianNegate(factor, this);
			else if ((factorFunction instanceof Product) && CustomGaussianProduct.isFactorCompatible(factor))
				return new CustomGaussianProduct(factor, this);
			else if ((factorFunction instanceof ComplexSum) && CustomMultivariateGaussianSum.isFactorCompatible(factor))
				return new CustomMultivariateGaussianSum(factor, this);
			else if ((factorFunction instanceof ComplexSubtract) && CustomMultivariateGaussianSubtract.isFactorCompatible(factor))
				return new CustomMultivariateGaussianSubtract(factor, this);
			else if ((factorFunction instanceof ComplexNegate) && CustomMultivariateGaussianNegate.isFactorCompatible(factor))
				return new CustomMultivariateGaussianNegate(factor, this);
			else if ((factorFunction instanceof RealJointSum) && CustomMultivariateGaussianSum.isFactorCompatible(factor))
				return new CustomMultivariateGaussianSum(factor, this);
			else if ((factorFunction instanceof RealJointSubtract) && CustomMultivariateGaussianSubtract.isFactorCompatible(factor))
				return new CustomMultivariateGaussianSubtract(factor, this);
			else if ((factorFunction instanceof RealJointNegate) && CustomMultivariateGaussianNegate.isFactorCompatible(factor))
				return new CustomMultivariateGaussianNegate(factor, this);
			else if ((factorFunction instanceof MatrixRealJointVectorProduct) && CustomMultivariateGaussianProduct.isFactorCompatible(factor))
				return new CustomMultivariateGaussianProduct(factor, this);
			else if ((factorFunction instanceof Normal) && CustomNormalConstantParameters.isFactorCompatible(factor))
				return new CustomNormalConstantParameters(factor, this);
			else if ((factorFunction instanceof MultivariateNormal) && CustomMultivariateNormalConstantParameters.isFactorCompatible(factor))
				return new CustomMultivariateNormalConstantParameters(factor, this);
			else if ((factorFunction instanceof LinearEquation) && CustomGaussianLinearEquation.isFactorCompatible(factor))
				return new CustomGaussianLinearEquation(factor, this);
			else if (noFF && factorName.equals("add"))								// For backward compatibility
			{
				if (isMultivariate(factor))
					return new CustomMultivariateGaussianSum(factor, this);
				else
					return new CustomGaussianSum(factor, this);
			}
			else if (noFF && factorName.equals("constmult"))						// For backward compatibility
			{
				if (isMultivariate(factor))
					return new CustomMultivariateGaussianProduct(factor, this);
				else
					return new CustomGaussianProduct(factor, this);
			}
			else if (noFF && factorName.equals("linear"))							// For backward compatibility
				return new CustomGaussianLinear(factor, this);
			else if (noFF && factorName.equals("polynomial"))						// For backward compatibility
				return new CustomComplexGaussianPolynomial(factor, this);
			else	// No custom factor exists, so create a generic one
			{
				// For non-discrete factor that doesn't have a custom factor, create a sampled factor
				return new SampledFactor(factor, this);
			}
		}
	}
	
	@SuppressWarnings("deprecation") // TODO remove when SFactorGraph removed
	@Override
	public ISolverFactorGraph createSubgraph(FactorGraph subgraph)
	{
		return new SFactorGraph(subgraph, this);
	}
	
	// This should return true only for custom factors that do not have a corresponding FactorFunction of the same name
	@Override
	public boolean customFactorExists(String funcName)
	{
		if (funcName.equals("finiteFieldAdd"))															// For backward compatibility
			return true;
		else if (funcName.equals("finiteFieldMult"))													// For backward compatibility
			return true;
		else if (funcName.equals("finiteFieldProjection"))												// For backward compatibility
			return true;
		else if (funcName.equals("multiplexerCPD"))														// For backward compatibility; should use "Multiplexer" instead
			return true;
		else if (funcName.equals("add"))																// For backward compatibility
			return true;
		else if (funcName.equals("constmult"))															// For backward compatibility
			return true;
		else if (funcName.equals("linear"))																// For backward compatibility
			return true;
		else if (funcName.equals("polynomial"))															// For backward compatibility
			return true;
		else
			return false;
	}
	

	private boolean isMultivariate(Factor factor)
	{
		if (factor.getSiblingCount() > 0 && (factor.getSibling(0) instanceof RealJoint))
			return true;
		else
			return false;
	}


	public static Random getRandom()
	{
		return _rand;
	}
	
	public void setSeed(long seed)
	{
		_rand = new Random(seed);				// Used for parameter estimation
		DimpleRandomGenerator.setSeed(seed);	// Used for sampled factors
	}
	

	/*
	 * Set the global solver damping parameter.  We have to go through all factor graphs
	 * and update the damping parameter on all existing table functions in that graph.
	 */
	public void setDamping(double damping)
	{
		_damping = damping;
		setOption(BPOptions.damping, damping);
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

	private final ISFactorGraphToOptimizedUpdateAdapter _optimizedUpdateAdapter = new SFactorGraphToOptimizedUpdateAdapter(this);

	private static class SFactorGraphToOptimizedUpdateAdapter implements ISFactorGraphToOptimizedUpdateAdapter
	{
		final private SumProductSolverGraph _sumProductSolverGraph;
		
		SFactorGraphToOptimizedUpdateAdapter(SumProductSolverGraph sumProductSolverGraph)
		{
			_sumProductSolverGraph = sumProductSolverGraph;
		}
		
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
			final int size = factorTable.countNonZeroWeights();
			final int dimensions = factorTable.getDimensions();
			// Coefficients determined experimentally
			double executionTime = 1.73280131035;
			executionTime += -46.4751637511 * (size - 254722.59319) / 9956266.39996;
			executionTime += 342.15344018 * (dimensions * size - 1877809.77842) / 219896210.353;
			result.put(CostType.EXECUTION_TIME, executionTime);
			return result;
		}

		@Override
		public Costs estimateCostOfOptimizedUpdate(IFactorTable factorTable, final double sparseThreshold)
		{
			final Costs costs = FactorUpdatePlan.estimateOptimizedUpdateCosts(factorTable, this, sparseThreshold);
			double dmf = costs.get(CostType.DENSE_MARGINALIZATION_SIZE);
			double smf = costs.get(CostType.SPARSE_MARGINALIZATION_SIZE);
			double fo = costs.get(CostType.OUTPUT_SIZE);
			double mem_cost = costs.get(CostType.MEMORY) * 1024.0 * 1024.0 * 1024.0;
			final double size = factorTable.countNonZeroWeights();
			// Coefficients determined experimentally
			double executionTime = 0.08;
			executionTime += 1.24138327837 * (size - 254722.59319) / 9956266.39996;
			executionTime += 2.18296909944 * (dmf - 316560.301654) / 39676196.0;
			executionTime += 0.883232752009 * (smf - 421208.789658) / 19914546.0;
			executionTime += 1.60951456134 * (fo - 4453.26298626) / 1836974.0;
			executionTime += 1.08967345943 * (mem_cost - 6742787.05688) / 416754545.21;
			executionTime += -0.447862077999 * Math.pow((size - 254722.59319) / 9956266.39996, 2.0);
			executionTime += -0.585003946613 * Math.pow((mem_cost - 6742787.05688) / 416754545.21, 2.0);
			final Costs result = new Costs();
			result.put(CostType.MEMORY, costs.get(CostType.MEMORY));
			result.put(CostType.EXECUTION_TIME, executionTime);
			return result;
		}

		@Override
		public ISolverFactorGraph getSolverGraph()
		{
			return _sumProductSolverGraph;
		}
		
		@Override
		public int getWorkers(ISolverFactorGraph sfactorGraph)
		{
			SumProductSolverGraph sfg = (SumProductSolverGraph) sfactorGraph;
			if (sfg.useMultithreading())
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
			_sumProductSolverGraph._factorTableUpdateSettings = optionsValueByFactorTable;
		}


		@Override
		public double[] getSparseValues(IFactorTable factorTable)
		{
			return factorTable.getWeightsSparseUnsafe();
		}

		@Override
		public double[] getDenseValues(IFactorTable factorTable)
		{
			return factorTable.getWeightsDenseUnsafe();
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
			return new TableFactorEngineOptimized.SparseMarginalizationStep(tableWrapper, this, inPortNum, dimension,
				g_factorTable, g_and_msg_indices);
		}

		@Override
		public IMarginalizationStep createDenseMarginalizationStep(TableWrapper tableWrapper,
			int inPortNum,
			int dimension,
			IFactorTable g_factorTable)
		{
			return new TableFactorEngineOptimized.DenseMarginalizationStep(tableWrapper, this, inPortNum, dimension,
				g_factorTable);
		}

		@Override
		public IOptionKey<UpdateApproach> getUpdateApproachOptionKey()
		{
			return BPOptions.updateApproach;
		}

		@Override
		public IOptionKey<Double> getOptimizedUpdateSparseThresholdKey()
		{
			return BPOptions.optimizedUpdateSparseThreshold;
		}

		@Override
		public IOptionKey<Double> getAutomaticExecutionTimeScalingFactorKey()
		{
			return BPOptions.automaticExecutionTimeScalingFactor;
		}

		@Override
		public IOptionKey<Double> getAutomaticMemoryAllocationScalingFactorKey()
		{
			return BPOptions.automaticMemoryAllocationScalingFactor;
		}
	}
	
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

	/**
	 * @deprecated Will be removed in a future release. Instead set {@link GibbsOptions#numSamples} on
	 * this object using {@link #setOption}.
	 */
	@Deprecated
	public void setSampledFactorSamplesPerUpdate(int samplesPerUpdate)
	{
		setOption(GibbsOptions.numSamples, samplesPerUpdate);
	}
	
	/**
	 * @deprecated Will be removed in a future release. Instead get {@link GibbsOptions#numSamples}
	 * on from this object using {@link #getOption}.
	 */
	@Deprecated
	public int getSampledFactorSamplesPerUpdate()
	{
		return getOptionOrDefault(GibbsOptions.numSamples);
	}
	
	/**
	 * @deprecated Will be removed in a future release. Instead set {@link GibbsOptions#burnInScans} on
	 * this object using {@link #setOption}.
	 */
	@Deprecated
	public void setSampledFactorBurnInScansPerUpdate(int burnInScans)
	{
		setOption(GibbsOptions.burnInScans, burnInScans);
	}

	/**
	 * @deprecated Will be removed in a future release. Instead set {@link GibbsOptions#burnInScans} on
	 * this object using {@link #setOption}.
	 */
	@Deprecated
	public int getSampledFactorBurnInScansPerUpdate()
	{
		return getOptionOrDefault(GibbsOptions.burnInScans);
	}

	/**
	 * @deprecated Will be removed in a future release. Instead set {@link GibbsOptions#scansPerSample} on
	 * this object using {@link #setOption}.
	 */
	@Deprecated
	public void setSampledFactorScansPerSample(int scansPerSample)
	{
		setOption(GibbsOptions.scansPerSample, scansPerSample);
	}

	/**
	 * @deprecated Will be removed in a future release. Instead set {@link GibbsOptions#scansPerSample} on
	 * this object using {@link #setOption}.
	 */
	@Deprecated
	public int getSampledFactorScansPerSample()
	{
		return getOptionOrDefault(GibbsOptions.scansPerSample);
	}

	@Override
	public void baumWelch(IFactorTable [] fts, int numRestarts, int numSteps)
	{
		ParameterEstimator pe = new ParameterEstimator.BaumWelch(_model, fts, SumProductSolverGraph.getRandom());
		pe.run(numRestarts, numSteps);
	}
	
	class GradientDescent extends ParameterEstimator
	{
		private double _scaleFactor;

		public GradientDescent(FactorGraph fg, IFactorTable[] tables, Random r, double scaleFactor)
		{
			super(fg, tables, r);
			_scaleFactor = scaleFactor;
		}

		@Override
		public void runStep(FactorGraph fg)
		{
			//_factorGraph.solve();
			for (IFactorTable ft : getTables())
			{
				double [] weights = ft.getWeightsSparseUnsafe();
			      //for each weight
				for (int i = 0; i < weights.length; i++)
				{
			           //calculate the derivative
					double derivative = calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(ft, i);
					
			        //move the weight in that direction scaled by epsilon
					ft.setWeightForSparseIndex(weights[i] - weights[i]*derivative*_scaleFactor,i);
				}
			}
		}
		
	}
	
	public void pseudoLikelihood(IFactorTable [] fts,
			Variable [] vars,
			Object [][] data,
			int numSteps,
			double stepScaleFactor)
	{
		
	}
	
	public static @Nullable int [][] convertObjects2Indices(Variable [] vars, Object [][] data)
	{
		
		return null;
	}

	
	@Override
	public void estimateParameters(IFactorTable [] fts, int numRestarts, int numSteps, double stepScaleFactor)
	{
		new GradientDescent(_model, fts, getRandom(), stepScaleFactor).run(numRestarts, numSteps);
	}

	
	@SuppressWarnings("null")
	public double calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(IFactorTable ft, int weightIndex)
	{
		//BFE = InternalEnergy - BetheEntropy
		//InternalEnergy = Sum over all factors (Internal Energy of Factor)
		//                   + Sum over all variables (Internal Energy of Variable)
		//BetheEntropy = Sum over all factors (BetheEntropy(factor))
		//                  + sum over all variables (BetheEntropy(variable)
		//So derivative of BFE = Sum over all factors that contain the weight
		//                                              (derivative of Internal Energy of Factor
		//                                              - derivative of BetheEntropy of Factor)
		//
		
		_currentFactorTable = ft;
		
		final SolverNodeMapping solvers = getSolverMapping();
		for (Factor f : _model.getFactors())
		{
			((SumProductTableFactor)solvers.getSolverFactor(f)).initializeDerivativeMessages(ft.sparseSize());
		}
		for (Variable vb : _model.getVariablesFlat())
		{
			((SumProductDiscrete)solvers.getSolverVariable(vb)).initializeDerivativeMessages(ft.sparseSize());
		}
		
		setCalculateDerivative(true);
		
		double result = 0;
		try
		{
			_model.solve();
			for (Factor f : _model.getFactors())
			{
				SumProductTableFactor stf = (SumProductTableFactor)solvers.getSolverFactor(f);
				result += stf.calculateDerivativeOfInternalEnergyWithRespectToWeight(weightIndex);
				result -= stf.calculateDerivativeOfBetheEntropyWithRespectToWeight(weightIndex);
						
			}
			for (Variable v : _model.getVariablesFlat())
			{
				SumProductDiscrete sv = (SumProductDiscrete)solvers.getSolverVariable(v);
				result += sv.calculateDerivativeOfInternalEnergyWithRespectToWeight(weightIndex);
				result += sv.calculateDerivativeOfBetheEntropyWithRespectToWeight(weightIndex);
			}
		}
		finally
		{
			setCalculateDerivative(false);
		}
		
		return result;
	}
	
	@SuppressWarnings("null")
	public void setCalculateDerivative(boolean val)
	{
		for (ISolverFactor sfactor : getSolverFactorsRecursive())
		{
			SumProductTableFactor stf = (SumProductTableFactor)sfactor;
			stf.setUpdateDerivative(val);
		}
		for (ISolverVariable svar : getSolverVariablesRecursive())
		{
			SumProductDiscrete sv = (SumProductDiscrete)svar;
			sv.setCalculateDerivative(val);
		}
	}
	
	
	// REFACTOR: make this package-protected?
	public @Nullable IFactorTable getCurrentFactorTable()
	{
		return _currentFactorTable;
	}


	@Override
	public void initialize()
	{
		super.initialize();
		UpdateCostOptimizer optimizer = new UpdateCostOptimizer(_optimizedUpdateAdapter);
		optimizer.optimize(this);
		final SolverNodeMapping solvers = getSolverMapping();
		for (Factor f : getModelObject().getFactors())
		{
			ISolverFactor sf = solvers.getSolverFactor(f);
			if (sf instanceof SumProductTableFactor)
			{
				SumProductTableFactor tf = (SumProductTableFactor)sf;
				IFactorTable table = tf.getFactorTableIfComputed();
				if (table != null)
				{
					tf.getFactorTable().getIndicesSparseUnsafe();
					tf.getFactorTable().getWeightsSparseUnsafe();
				}
				tf.setupTableFactorEngine();
			}
		}
		
		//
		// Update options
		//
		
		Long seed = getOption(DimpleOptions.randomSeed);
		if (seed != null)
		{
			setSeed(seed);
		}
		
		_damping = getOptionOrDefault(BPOptions.damping);
	}

	/*
	 * 
	 */
	@Override
	protected void doUpdateEdge(int edge)
	{
	}
	
	@Override
	protected String getSolverName()
	{
		return "sum-product";
	}

	public double computeUnnormalizedLogLikelihood()
	{
		double Z = 0.0;
		
		for (ISolverFactor sfactor : getSolverFactorsRecursive())
		{
			SumProductTableFactor tableFactor = (SumProductTableFactor)sfactor;
			Z += tableFactor.computeUnnormalizedLogLikelihood();
		}
		
		for (ISolverVariable svar : getSolverVariablesRecursive())
		{
			SumProductDiscrete sdiscrete = (SumProductDiscrete)svar;
			Z += sdiscrete.computeUnnormalizedLogLikelihood();
		}
		
		return Z;
	}
}
