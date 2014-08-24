/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.RealDomain;

/**
 * A holder for a real (i.e. {@code double} value).
 */
public class RealValue extends Value
{
	private static final long serialVersionUID = 1L;

	protected double _value;
	
	/*--------------
	 * Construction
	 */
	
	public static RealValue create()
	{
		return new RealValue(0.0);
	}
	
	public static RealValue create(double value)
	{
		return new RealValue(value);
	}
	
	RealValue(double value)
	{
		_value = value;
	}

	RealValue(RealValue that)
	{
		this(that._value);
	}

	/*----------------
	 * Object methods
	 */
	
	@Override
	public RealValue clone()
	{
		return new RealValue(this);
	}

	@Override
	public String toString()
	{
		return String.valueOf(_value);
	}
	
	/*---------------
	 * Value methods
	 */
	
	@Override
	public void setFrom(Value value)
	{
		_value = value.getDouble();
	}
	
	@Override
	public RealDomain getDomain()
	{
		return RealDomain.unbounded();
	}
	
	@Override
	public @NonNull Double getObject()
	{
		return _value;
	}
	
	@Override
	public void setObject(@Nullable Object value)
	{
		_value = ((Number)requireNonNull(value)).doubleValue();
	}
	
	@Override
	public double getDouble()
	{
		return _value;
	}
	
	@Override
	public void setDouble(double value)
	{
		_value = value;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns {@link #getDouble()} rounded.
	 */
	@Override
	public int getInt()
	{
		// TODO: is this the semantics we want here?
		return (int)Math.round(_value);
	}
	
	@Override
	public void setInt(int value)
	{
		_value = value;
	}
	
	@Override
	public void setBoolean(boolean value)
	{
		_value = value ? 1.0 : 0.0;
	}
	
	@Override
	public boolean valueEquals(Value other)
	{
		return _value == other.getDouble();
	}
}
