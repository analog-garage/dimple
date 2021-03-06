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
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;

@SuppressWarnings("deprecation") // FIXME - remove suppression when VariableBase is removed
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

	/**
	 * @deprecated as of release 0.08 use {@link #Real(RealDomain)} instead.
	 */
	@Deprecated
	public Real(RealDomain domain, String modelerClassName)
	{
		super(domain, modelerClassName);
	}
	

	@SuppressWarnings("deprecation")
	protected Real(Real that)
	{
		super(that);
	}
	
	@Override
	public Real clone()
	{
		return new Real(this);
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
	
	/*--------------------
	 * Deprecated methods
	 */
	
	/**
	 * @deprecated use {@link #getPriorValue()} instead
	 */
	@Deprecated
	public double getFixedValue()
	{
		Object tmp = getFixedValueObject();
		if (tmp == null)
			throw new DimpleException("Fixed Value is not set");
		else
			return (Double)tmp;
	}
	
	/**
	 * @deprecated use {@link #setPrior(Object)} instead
	 */
	@Deprecated
	public void setFixedValue(double fixedValue)
	{
		// Verify that the fixed value is in the domain of the variable
		if (!getDomain().inDomain(fixedValue))
			throw new DimpleException("Attempt to set fixed value outside of variable domain.");
	
		setFixedValueObject(fixedValue);
	}

	@Deprecated
	@Override
	public void setInputObject(@Nullable Object value)
	{
		if (value instanceof double[])
		{
			// HACK: Array containing [mean, std]
			double[] array = (double[])value;
			if (array.length != 2)
			{
				throw new IllegalArgumentException("Expected array of length 2");
			}
			final double mean = array[0];
			final double std = array[1];
			NormalParameters normal = new NormalParameters();
			normal.setMean(mean);
			normal.setStandardDeviation(std);
			value = normal;
		}
		super.setInputObject(value);
	}
	
	/**
	 * @deprecated instead use {@link #setPrior(Object)}
	 */
	@Deprecated
	public void setInput(@Nullable FactorFunction input)
	{
		setInputObject(input);
	}
	
	/**
	 * @deprecated instead use {@link #setPrior(Object)} with a {@link NormalParameters} instance.
	 */
	@Deprecated
	public void setInput(@Nullable double[] input)
	{
		setInputObject(input);
	}

}
