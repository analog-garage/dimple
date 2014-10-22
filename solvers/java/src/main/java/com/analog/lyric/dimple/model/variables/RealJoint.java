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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
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

	
	// Fix the variable to a specific value
	public final double[] getFixedValue()
	{
		Object tmp = getFixedValueObject();
		if (tmp == null)
			throw new DimpleException("Fixed Value is not set");
		else
			return (double[])tmp;
	}
	
	@Override
	public @Nullable Object getFixedValueAsObject()
	{
		return hasFixedValue() ? getFixedValue() : null;
	}
	
	public void setFixedValue(double[] fixedValue)
	{
		// Verify that the fixed value is in the domain of the variable
		if (!getDomain().inDomain(fixedValue))
			throw new DimpleException("Attempt to set fixed value outside of variable domain.");
	
		setFixedValueObject(fixedValue);
	}

	@Override
	public void setFixedValueFromObject(@Nullable Object value)
	{
		if (value != null)
		{
			setFixedValue((double[])value);
		}
		else if (hasFixedValue())
		{
			setInputOrFixedValue(null, _input);
		}
	}
	
	public void setInput(@Nullable FactorFunction[] input)
	{
		setInputObject(input);
	}
	
	public void setInput(double [] means, double [][] covar)
	{
		setInputObject(new MultivariateNormalParameters(means,covar));
	}

	public void setInput(@Nullable MultivariateNormalParameters msg)
	{
		setInputObject(msg);
	}
	
	public @Nullable MultivariateNormalParameters getBelief()
	{
		return (MultivariateNormalParameters)getBeliefObject();
	}
}
