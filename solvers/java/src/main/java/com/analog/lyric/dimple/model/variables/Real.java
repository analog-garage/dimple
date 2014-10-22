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
import com.analog.lyric.dimple.model.domains.RealDomain;


@SuppressWarnings("deprecation")
public class Real extends VariableBase
{
	// Constructors...
	public Real()
	{
		this(RealDomain.unbounded(), "Real");
	}
	public Real(double lower, double upper)
	{
		this(RealDomain.create(lower,upper));
	}
	public Real(RealDomain domain)
	{
		this(domain, "Real");
	}
	public Real(RealDomain domain, String modelerClassName)
	{
		super(domain, modelerClassName);
	}

	@Override
	public RealDomain getDomain()
	{
		return (RealDomain)super.getDomain();
	}
	
	public RealDomain getRealDomain()
	{
		return getDomain();
	}
	
	public @Nullable Object getInput()
	{
		return getInputObject();
	}
	
	
	public double getValue()
	{
		return (Double)requireSolver("getValue").getValue();
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
	
	@Override
	public final @Nullable Object getFixedValueAsObject()
	{
		return getFixedValueObject();
	}
	
	public void setFixedValue(double fixedValue)
	{
		// Verify that the fixed value is in the domain of the variable
		if (!getDomain().inDomain(fixedValue))
			throw new DimpleException("Attempt to set fixed value outside of variable domain.");
	
		setFixedValueObject(fixedValue);
	}

	@Override
	public final void setFixedValueFromObject(@Nullable Object value)
	{
		if (value != null)
		{
			setFixedValue((double)value);
		}
		else if (hasFixedValue())
		{
			setInputOrFixedValue(null, _input);
		}
	}
	
	public void setInput(@Nullable FactorFunction input)
	{
		setInputObject(input);
	}
	
	public void setInput(@Nullable double[] input)
	{
		setInputObject(input);
	}

}
