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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.solvers.gaussian.MultivariateMsg;

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

	
	public RealJointDomain getRealDomain()
	{
		return (RealJointDomain)getDomain();
	}
	public Object getInput() 
	{
		return getInputObject();
	}
	
	
	public double[] getValue()
	{
		return (double[])_solverVariable.getValue();
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
	
	public void setFixedValue(double[] fixedValue) 
	{
		// Verify that the fixed value is in the domain of the variable
		if (!((RealJointDomain)getDomain()).inDomain(fixedValue))
			throw new DimpleException("Attempt to set fixed value outside of variable domain.");
	
		setFixedValueObject(fixedValue);
	}
	
	public void setInput(FactorFunction[] input)
	{
		setInputObject(input);
	}
	
	public void setInput(double [] means, double [][] covar)
	{
		setInputObject(new MultivariateMsg(means,covar));
	}

	public void setInput(MultivariateMsg msg)
	{
		setInputObject(msg);
	}
	
	public MultivariateMsg getBelief()
	{
		return (MultivariateMsg)getBeliefObject();
	}
}
