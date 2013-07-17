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
    public Real(int id, String modelerClassName, RealDomain domain)
    {
    	super(id, modelerClassName, domain);
    }

    public Real(int id, String modelerClassName, RealDomain domain,Object input)
    {
    	super(id, modelerClassName, domain);
    	setInputObject(input);
    }
	
	public Real(double lower, double upper)
	{
		this(new RealDomain(lower,upper));
	}
	
	public Real()  
	{
		this(new RealDomain(), null);
	}

	public Real(RealDomain domain)  
	{
		this(domain, null);
	}
	public Real(Object input)  
	{
		this(new RealDomain(), input);
	}
	
	public Real(RealDomain domain, Object input) 
	{
		this(NodeId.getNext(), domain, input, "Real");
	}

	public Real(int id, RealDomain domain, Object input, String modelerClassName) 
	{
		//this(id,new RealDomain[]{domain},input,modelerClassName);
		super(id,modelerClassName,domain);
		//super(id, modelerClassName);
		//_domain = domain;
		setInputObject(input);

	}
	
//	public Real(int id, RealDomain [] domains, Object input, String modelerClassName) 
//	{
//		super(id,modelerClassName,domains);
//		//super(id, modelerClassName);
//		//_domain = domain;
//		_input = input;
//	}
	
	public RealDomain getRealDomain()
	{
		return (RealDomain)getDomain();
	}
	public Object getInput() 
	{
		return getInputObject();
	}
	
	
	// Fix the variable to a specific value
	public final double getFixedValue()
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
