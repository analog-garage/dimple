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

package com.analog.lyric.dimple.model.variables;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.Supers;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.UnaryJointRealFactorFunction;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;

@SuppressWarnings("deprecation")
public class RealJoint extends VariableBase
{
	// Constructors...
	public RealJoint(int size)
	{
		this(RealJointDomain.create(size));
	}
	public RealJoint(RealJointDomain domain)
	{
		this(domain, "RealJoint");
	}
	public RealJoint(RealJointDomain domain, String modelerClassName)
	{
		super(domain, modelerClassName);
	}
	
	protected RealJoint(RealJoint that)
	{
		super(that);
	}
	
	@Override
	public RealJoint clone()
	{
		return new RealJoint(this);
	}

	@Override
	public RealJointDomain getDomain()
	{
		return (RealJointDomain)super.getDomain();
	}
	
	public RealJointDomain getRealDomain()
	{
		return getDomain();
	}
	
	public @Nullable Object getInput()
	{
		return getInputObject();
	}
	
	
	public double[] getValue()
	{
		return (double[])requireSolver("getValue").getValue();
	}

	
	/**
	 * @deprecated use {@link #getPriorValue()} instead
	 */
	// Fix the variable to a specific value
	@Deprecated
	public final double[] getFixedValue()
	{
		Object tmp = getFixedValueObject();
		if (tmp == null)
			throw new DimpleException("Fixed Value is not set");
		else
			return (double[])tmp;
	}
	
	/**
	 * @deprecated instead use {@link #setPrior(Object)}
	 */
	@Deprecated
	public void setFixedValue(double[] fixedValue)
	{
		// Verify that the fixed value is in the domain of the variable
		if (!getDomain().inDomain(fixedValue))
			throw new DimpleException("Attempt to set fixed value outside of variable domain.");
	
		setFixedValueObject(fixedValue);
	}

	public void setInput(@Nullable FactorFunction[] input)
	{
		setInputObject(input);
	}
	
	/**
	 * @deprecated instead {@link #setPrior(Object)} to {@link MultivariateNormalParameters} instance.
	 */
	@Deprecated
	public void setInput(double [] means, double [][] covar)
	{
		setInputObject(new MultivariateNormalParameters(means,covar));
	}

	/**
	 * @deprecated instead use {@link #setPrior(Object)}
	 */
	@Deprecated
	public void setInput(@Nullable MultivariateNormalParameters msg)
	{
		setInputObject(msg);
	}

	@Deprecated
	@Override
	public void setInputObject(@Nullable Object value)
	{
		if (value instanceof Object[])
		{
			final Object[] inputArray = (Object[])value;
			final int n = getDomain().getDimensions();
			if (inputArray.length != n)
				throw new DimpleException("Number of Inputs must equal the variable dimension");
			
			Normal[] normals = Supers.narrowArrayOf(Normal.class, inputArray);
			
			if (normals != null)
			{
				double[] mean = new double[n];
				double[] variance = new double[n];
				for (int i = 0; i < n; i++)
				{
					mean[i] = normals[i].getMean();
					variance[i] = normals[i].getVariance();
				}
				value = new MultivariateNormalParameters(mean, variance);
			}
			else
			{
				IUnaryFactorFunction[] functions = Supers.narrowArrayOf(IUnaryFactorFunction.class, inputArray);
				if (functions != null)
				{
					value = new UnaryJointRealFactorFunction(functions);
				}
			}
		}
		
		super.setInputObject(value);
	}
	
	public @Nullable MultivariateNormalParameters getBelief()
	{
		return (MultivariateNormalParameters)getBeliefObject();
	}
}
