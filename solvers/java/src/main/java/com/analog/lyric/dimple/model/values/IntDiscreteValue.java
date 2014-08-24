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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;

public abstract class IntDiscreteValue extends DiscreteValue
{
	private static final long serialVersionUID = 1L;

	private final TypedDiscreteDomain<Integer> _domain;
	int _value;
	
	/*--------------
	 * Construction
	 */
	
	IntDiscreteValue(TypedDiscreteDomain<Integer> domain, int value)
	{
		_domain = domain;
		_value = value;
	}
	
	IntDiscreteValue(IntDiscreteValue other)
	{
		this(other._domain, other._value);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return String.valueOf(_value);
	}
	
	/*---------------
	 * Value methods
	 */

	@Override
	public TypedDiscreteDomain<Integer> getDomain()
	{
		return _domain;
	}

	@Override
	public abstract IntDiscreteValue clone();

	@Override
	public final boolean getBoolean()
	{
		return getInt() != 0;
	}
	
	@Override
	public final double getDouble()
	{
		return getInt();
	}
	
	@Override
	public final void setDouble(double value)
	{
		// FIXME is this what we want?
		setInt((int)Math.round(value));
	}
	
	@Override
	public final int getInt()
	{
		return _value;
	}
	
	@Override
	public void setInt(int value)
	{
		_value = value;
	}
	
	@Override
	public void setBoolean(boolean value)
	{
		_value = value ? 1 : 0;
	}
	
	@Override
	public final Integer getObject()
	{
		return getInt();
	}

	@Override
	public final void setObject(@Nullable Object value)
	{
		setInt(FactorFunctionUtilities.toInteger(value));
	}

	@Override
	public void setFrom(Value value)
	{
		setInt(value.getInt());
	}
	
	@Override
	public final boolean valueEquals(Value other)
	{
		return _value == other.getInt();
	}
}
