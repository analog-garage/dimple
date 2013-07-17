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


public class Real extends VariableBase
{
	// Constructors...
	public Real()  
	{
		this(new RealDomain(), null, "Real");
	}
	public Real(RealDomain domain)  
	{
		this(domain, null, "Real");
	}
	public Real(Object input)  
	{
		this(new RealDomain(), input, "Real");
	}
	public Real(RealDomain domain, Object input) 
	{
		this(domain, input, "Real");
	}
    public Real(RealDomain domain, String modelerClassName)
    {
    	this(domain, null, modelerClassName);
    }
	public Real(RealDomain domain, Object input, String modelerClassName) 
	{
		super(domain, modelerClassName);
		setInputObject(input);
	}

	
	public RealDomain getRealDomain()
	{
		return (RealDomain)getDomain();
	}
	public Object getInput() 
	{
		return getInputObject();
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


}
