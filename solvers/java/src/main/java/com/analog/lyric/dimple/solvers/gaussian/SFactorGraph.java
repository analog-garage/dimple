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

package com.analog.lyric.dimple.solvers.gaussian;

import java.util.Random;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.core.hybridSampledBP.HybridSampledBPFactor;
import com.analog.lyric.dimple.solvers.gaussian.customFactors.GaussianAdd;
import com.analog.lyric.dimple.solvers.gaussian.customFactors.GaussianConstMult;
import com.analog.lyric.dimple.solvers.gaussian.customFactors.GaussianLinear;
import com.analog.lyric.dimple.solvers.gaussian.customFactors.MultivariateGaussianAdd;
import com.analog.lyric.dimple.solvers.gaussian.customFactors.MultivariateGaussianConstMult;
import com.analog.lyric.dimple.solvers.gaussian.customFactors.Polynomial;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class SFactorGraph extends SFactorGraphBase
{
	private Random _random;
	private int _numSamples;
	private int _maxNumTries;

	public SFactorGraph(com.analog.lyric.dimple.model.core.FactorGraph factorGraph)
	{
		super(factorGraph);
		
		_numSamples = 100;
		_maxNumTries = Integer.MAX_VALUE;
		_random = new Random();
		
	}

	public void setSeed(long seed)
	{
		_random.setSeed(seed);
	}
	
	public int getMaxNumTries()
	{
		return _maxNumTries;
	}
	
	public void setMaxNumTries(int maxNumTries)
	{
		for (Factor f : _factorGraph.getNonGraphFactors())
		{
			ISolverFactor s = f.getSolver();
			
			if (s instanceof HybridSampledBPFactor)
			{
				((HybridSampledBPFactor)s).setMaxNumTries(maxNumTries);
			}
		}
		_maxNumTries = maxNumTries;
		
	}
	
	public int getNumSamples()
	{
		return _numSamples;
	}
	
	public void setNumSamples(int numSamples)
	{
		for (Factor f : _factorGraph.getNonGraphFactors())
		{
			ISolverFactor s = f.getSolver();
			
			if (s instanceof HybridSampledBPFactor)
			{
				((HybridSampledBPFactor)s).setNumSamples(numSamples);
			}
		}
		_numSamples = numSamples;
	}
	
	private boolean isMultivariate(com.analog.lyric.dimple.model.factors.Factor factor)
	{
		
		if (factor.getSiblingCount() > 0 && (factor.getSibling(0) instanceof RealJoint))
			return true;
		else
			return false;
	}
	
	public ISolverFactor createCustomFactor(com.analog.lyric.dimple.model.factors.Factor factor)
	{
		String funcName = factor.getModelerFunctionName();
		if (funcName.equals("add"))
		{
			if (isMultivariate(factor))
				return new MultivariateGaussianAdd(factor);
			else
				return new GaussianAdd(factor);
		}
		else if (funcName.equals("Sum"))
		{
			return new GaussianAdd(factor);
		}
		else if (funcName.equals("ComplexSum"))
		{
			return new MultivariateGaussianAdd(factor);
		}
		else if (funcName.equals("constmult"))
		{
			if (isMultivariate(factor))
				return new MultivariateGaussianConstMult(factor);
			else
				return new GaussianConstMult(factor);
		}
		else if (funcName.equals("Product"))
		{
			return new GaussianConstMult(factor);
		}
		else if (funcName.equals("ComplexProduct"))
		{
			return new MultivariateGaussianConstMult(factor);
		}
		else if (funcName.equals("polynomial"))
		{
			return new Polynomial(factor);
		}
		else if (funcName.equals("linear"))
			return new GaussianLinear(factor);
		else
			throw new DimpleException("Not implemented");
	}
	

	@Override
	public ISolverFactor createFactor(Factor factor)
	{
		if (customFactorExists(factor))
			return createCustomFactor(factor);
		else if (factor.isDiscrete())
			return new com.analog.lyric.dimple.solvers.sumproduct.STableFactor(factor);
		else
		{
			HybridSampledBPFactor sf = new HybridSampledGaussianFactor(factor,_random);
			sf.setNumSamples(_numSamples);
			sf.setMaxNumTries(_maxNumTries);
			return sf;
		}
	}

	@Override
	public ISolverVariable createVariable(VariableBase var)
	{
		//TODO: error check to make sure it's real?
		if (var.getDomain() instanceof RealJointDomain)
			return new MultivariateVariable(var);
		if (!(var instanceof Real))
			return new com.analog.lyric.dimple.solvers.sumproduct.SVariable(var);
		else
			return new SVariable(var);
	}

	@Override
	public boolean customFactorExists(String funcName)
	{
		if (funcName.equals("add"))
			return true;
		else if (funcName.equals("constmult"))
			return true;
		else if (funcName.equals("polynomial"))
			return true;
		else if (funcName.equals("multivariateadd"))
			return true;
		else if (funcName.equals("multivariateconstmult"))
			return true;
		else if (funcName.equals("linear"))
			return true;
		else if (funcName.equals("ComplexSum"))
			return true;
		else if (funcName.equals("ComplexProduct"))
			return true;
		else
			return false;
	}
	
	
	// For internal use, we check more than just the name
	// This allows detection of factors used by overloaded operators to be used if used in the right way
	public boolean customFactorExists(Factor factor)
	{
		String funcName = factor.getModelerFunctionName();
		if (customFactorExists(funcName))
			return true;
		else if (funcName.equals("Sum"))
			return GaussianAdd.isFactorCompatible(factor);
		else if (funcName.equals("Product"))
			return GaussianConstMult.isFactorCompatible(factor);
		else
			return false;
	}



}
