/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model.values;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;

/**
 * Base class for {@link DiscreteValue} implementations for
 * discrete domains whose elements are doubles.
 */
public abstract class DoubleDiscreteValue extends DiscreteValue
{
	private static final long serialVersionUID = 1L;

	final TypedDiscreteDomain<Double> _domain;
	double _value;
	
	/*--------------
	 * Construction
	 */

	DoubleDiscreteValue(TypedDiscreteDomain<Double> domain, double value)
	{
		_domain = domain;
		_value = value;
	}
	
	DoubleDiscreteValue(DoubleDiscreteValue other)
	{
		this(other._domain, other._value);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public abstract DoubleDiscreteValue clone();

	@Override
	public String toString()
	{
		return String.valueOf(_value);
	}
	
	/*---------------
	 * Value methods
	 */
	
	@Override
	public TypedDiscreteDomain<Double> getDomain()
	{
		return _domain;
	}

	/**
	 * Returns true if value is not zero.
	 */
	@Override
	public final boolean getBoolean()
	{
		return _value != 0.0;
	}
	
	@Override
	public final double getDouble()
	{
		return _value;
	}
	
	@Override
	public void setDouble(double value)
	{
		_value = value;
	}
	
	@Override
	public int getInt()
	{
		return _domain.hasIntCompatibleValues() ? (int)_value : (int)Math.round(_value);
	}
	
	@Override
	public final void setInt(int value)
	{
		setDouble(value);
	}
	
	@Override
	public void setBoolean(boolean value)
	{
		_value = value ? 1.0 : 0.0;
	}
	
	@Override
	public @NonNull Double getObject()
	{
		return _value;
	}

	@Override
	public final void setObject(@Nullable Object value)
	{
		setDouble(FactorFunctionUtilities.toDouble(value));
	}

	@Override
	public void setFrom(Value other)
	{
		setDouble(other.getDouble());
	}
	
	@Override
	public final boolean valueEquals(Value other)
	{
		return _value == other.getDouble();
	}
}
