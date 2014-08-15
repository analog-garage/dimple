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

import java.util.Random;

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
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.options.DimpleOptions;
import com.analog.lyric.dimple.solvers.core.ParameterEstimator;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
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
import com.analog.lyric.util.misc.Nullable;

public class SFactorGraph extends SFactorGraphBase
{
	private double _damping = 0;
	private @Nullable IFactorTable _currentFactorTable = null;
	private static Random _rand = new Random();
	private boolean _defaultOptimizedUpdateEnabled;


	public SFactorGraph(FactorGraph factorGraph)
	{
		super(factorGraph);
		setMultithreadingManager(new MultiThreadingManager(getModelObject()));
		
		// Set default Gibbs options for sampled factors.
		setOption(GibbsOptions.numSamples, SampledFactor.DEFAULT_SAMPLES_PER_UPDATE);
		setOption(GibbsOptions.burnInScans, SampledFactor.DEFAULT_BURN_IN_SCANS_PER_UPDATE);
		setOption(GibbsOptions.scansPerSample, SampledFactor.DEFAULT_SCANS_PER_SAMPLE);
	}
	

	@Override
	public ISolverVariable createVariable(VariableBase var)
	{
		if (var.getModelerClassName().equals("FiniteFieldVariable"))
			return new SFiniteFieldVariable(var);
		else if (var instanceof RealJoint)
			return new SRealJointVariable(var);
		else if (var instanceof Real)
			return new SRealVariable(var);
		else
			return new SDiscreteVariable(var);
	}

	

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
				return new CustomFiniteFieldAdd(factor);
			else if ((factorFunction instanceof FiniteFieldMult) || (noFF && factorName.equals("finiteFieldMult")))					// "finiteFieldMult" for backward compatibility
			{
				if (hasConstants)
					return new CustomFiniteFieldConstantMult(factor);
				else
					return new CustomFiniteFieldMult(factor);
			}
			else if ((factorFunction instanceof FiniteFieldProjection) || (noFF && factorName.equals("finiteFieldProjection")))		// "finiteFieldProjection" for backward compatibility
				return new CustomFiniteFieldProjection(factor);
			else if ((factorFunction instanceof Multiplexer) || (noFF && factorName.equals("multiplexerCPD")))	// "multiplexerCPD" for backward compatibility
				return new CustomMultiplexer(factor);															// Currently only supports discrete variables
			else	// No custom factor exists, so create a generic one
			{
				// For discrete case, create a table factor
				return new STableFactor(factor);
			}
		}
		else	// Factor includes at least one continuous variable
		{
			// First see if any custom factor should be created
			if ((factorFunction instanceof Sum) && CustomGaussianSum.isFactorCompatible(factor))
				return new CustomGaussianSum(factor);
			else if ((factorFunction instanceof Subtract) && CustomGaussianSubtract.isFactorCompatible(factor))
				return new CustomGaussianSubtract(factor);
			else if ((factorFunction instanceof Negate) && CustomGaussianNegate.isFactorCompatible(factor))
				return new CustomGaussianNegate(factor);
			else if ((factorFunction instanceof Product) && CustomGaussianProduct.isFactorCompatible(factor))
				return new CustomGaussianProduct(factor);
			else if ((factorFunction instanceof ComplexSum) && CustomMultivariateGaussianSum.isFactorCompatible(factor))
				return new CustomMultivariateGaussianSum(factor);
			else if ((factorFunction instanceof ComplexSubtract) && CustomMultivariateGaussianSubtract.isFactorCompatible(factor))
				return new CustomMultivariateGaussianSubtract(factor);
			else if ((factorFunction instanceof ComplexNegate) && CustomMultivariateGaussianNegate.isFactorCompatible(factor))
				return new CustomMultivariateGaussianNegate(factor);
			else if ((factorFunction instanceof RealJointSum) && CustomMultivariateGaussianSum.isFactorCompatible(factor))
				return new CustomMultivariateGaussianSum(factor);
			else if ((factorFunction instanceof RealJointSubtract) && CustomMultivariateGaussianSubtract.isFactorCompatible(factor))
				return new CustomMultivariateGaussianSubtract(factor);
			else if ((factorFunction instanceof RealJointNegate) && CustomMultivariateGaussianNegate.isFactorCompatible(factor))
				return new CustomMultivariateGaussianNegate(factor);
			else if ((factorFunction instanceof MatrixRealJointVectorProduct) && CustomMultivariateGaussianProduct.isFactorCompatible(factor))
				return new CustomMultivariateGaussianProduct(factor);
			else if ((factorFunction instanceof Normal) && CustomNormalConstantParameters.isFactorCompatible(factor))
				return new CustomNormalConstantParameters(factor);
			else if ((factorFunction instanceof MultivariateNormal) && CustomMultivariateNormalConstantParameters.isFactorCompatible(factor))
				return new CustomMultivariateNormalConstantParameters(factor);
			else if ((factorFunction instanceof LinearEquation) && CustomGaussianLinearEquation.isFactorCompatible(factor))
				return new CustomGaussianLinearEquation(factor);
			else if (noFF && factorName.equals("add"))								// For backward compatibility
			{
				if (isMultivariate(factor))
					return new CustomMultivariateGaussianSum(factor);
				else
					return new CustomGaussianSum(factor);
			}
			else if (noFF && factorName.equals("constmult"))						// For backward compatibility
			{
				if (isMultivariate(factor))
					return new CustomMultivariateGaussianProduct(factor);
				else
					return new CustomGaussianProduct(factor);
			}
			else if (noFF && factorName.equals("linear"))							// For backward compatibility
				return new CustomGaussianLinear(factor);
			else if (noFF && factorName.equals("polynomial"))						// For backward compatibility
				return new CustomComplexGaussianPolynomial(factor);
			else	// No custom factor exists, so create a generic one
			{
				// For non-discrete factor that doesn't have a custom factor, create a sampled factor
				return new SampledFactor(factor);
			}
		}
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
		setOption(SumProductOptions.damping, damping);
	}
	
	public double getDamping()
	{
		return _damping;
	}

	/**
	 * Indicates if this solver supports the optimized update algorithm.
	 * 
	 * @return True if this solver does support the optimized update algorithm.
	 * @since 0.06
	 */
	public boolean isOptimizedUpdateSupported()
	{
		return true;
	}

	/**
	 * Gets the default optimized update algorithm enable. Factors for which the enable is not explicitly set use this value.
	 * 
	 * @since 0.06
	 */
	public boolean getDefaultOptimizedUpdateEnabled()
	{
		return _defaultOptimizedUpdateEnabled;
	}
	
	/**
	 * Sets the default optimized update algorithm enable. Factors for which the enable is not explicitly set use this value.
	 * 
	 * @since 0.06
	 */
	public void setDefaultOptimizedUpdateEnabled(boolean value)
	{
		_defaultOptimizedUpdateEnabled = value;
		setOption(SumProductOptions.enableOptimizedUpdate, value);
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
		ParameterEstimator pe = new ParameterEstimator.BaumWelch(_factorGraph, fts, SFactorGraph.getRandom());
		pe.run(numRestarts, numSteps);
	}
	
	
	public class GradientDescent extends ParameterEstimator
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
			VariableBase [] vars,
			Object [][] data,
			int numSteps,
			double stepScaleFactor)
	{
		
	}
	
	public static @Nullable int [][] convertObjects2Indices(VariableBase [] vars, Object [][] data)
	{
		
		return null;
	}

	
	@Override
	public void estimateParameters(IFactorTable [] fts, int numRestarts, int numSteps, double stepScaleFactor)
	{
		new GradientDescent(_factorGraph, fts, getRandom(), stepScaleFactor).run(numRestarts, numSteps);
	}

	
	@SuppressWarnings("null")
	public double calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(IFactorTable ft,
			int weightIndex)
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
		
				
		for (Factor f : _factorGraph.getFactorsFlat())
		{
			((STableFactor)f.getSolver()).initializeDerivativeMessages(ft.sparseSize());
		}
		for (VariableBase vb : _factorGraph.getVariablesFlat())
			((SDiscreteVariable)vb.getSolver()).initializeDerivativeMessages(ft.sparseSize());
		
		setCalculateDerivative(true);
		
		double result = 0;
		try
		{
			_factorGraph.solve();
			for (Factor f : _factorGraph.getFactorsFlat())
			{
				STableFactor stf = (STableFactor)f.getSolver();
				result += stf.calculateDerivativeOfInternalEnergyWithRespectToWeight(weightIndex);
				result -= stf.calculateDerivativeOfBetheEntropyWithRespectToWeight(weightIndex);
						
			}
			for (VariableBase v : _factorGraph.getVariablesFlat())
			{
				SDiscreteVariable sv = (SDiscreteVariable)v.getSolver();
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
		for (Factor f : _factorGraph.getFactorsFlat())
		{
			STableFactor stf = (STableFactor)f.getSolver();
			stf.setUpdateDerivative(val);
		}
		for (VariableBase vb : _factorGraph.getVariablesFlat())
		{
			SDiscreteVariable sv = (SDiscreteVariable)vb.getSolver();
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
		TableFactorEngineOptimized.clearUpdatePlans(this);
		
		super.initialize();
		for (Factor f : getModelObject().getFactors())
		{
			ISolverFactor sf = f.getSolver();
			if (sf instanceof STableFactor)
			{
				STableFactor tf = (STableFactor)sf;
				tf.getFactorTable().getIndicesSparseUnsafe();
				tf.getFactorTable().getWeightsSparseUnsafe();
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
		
		_damping = getOptionOrDefault(SumProductOptions.damping);
		
		_defaultOptimizedUpdateEnabled = getOptionOrDefault(SumProductOptions.enableOptimizedUpdate);
	}


	/*
	 * 
	 */
	@Override
	protected void doUpdateEdge(int edge)
	{
	}
	


}
