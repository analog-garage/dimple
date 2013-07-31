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

package com.analog.lyric.dimple.model;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class Real extends VariableBase
{
    // Constructors...
	public Real()  
	{
		this(new RealDomain(), "Real");
	}
	public Real(double lower, double upper)
	{
		this(new RealDomain(lower,upper));
	}
	public Real(RealDomain domain)  
	{
		this(domain, "Real");
	}
	public Real(RealDomain domain, String modelerClassName) 
	{
		super(domain, modelerClassName);
	}

	
	public RealDomain getRealDomain()
	{
		return (RealDomain)getDomain();
	}
	public Object getInput() 
	{
		return getInputObject();
	}
	
	
	public double getValue()
	{
		return (Double)_solverVariable.getValue();
	}
	
	// Fix the variable to a specific value
	public double getFixedValue()
	{
		Object tmp = getFixedValueObject();
		if (tmp == null)
			throw new DimpleException("Fixed Value is not set");
		else
			return (Double)tmp;
	}
	
	public void setFixedValue(double fixedValue) 
	{
		// Verify that the fixed value is in the domain of the variable
		if (!((RealDomain)getDomain()).inDomain(fixedValue))
			throw new DimpleException("Attempt to set fixed value outside of variable domain.");
	
		setFixedValueObject(fixedValue);
	}

	public void setInput(FactorFunction input)
	{
		setInputObject(input);
	}
	
	public void setInput(double[] input)
	{
		setInputObject(input);
	}

}
