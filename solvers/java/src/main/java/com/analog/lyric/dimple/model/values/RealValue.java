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

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.DoubleDiscreteDomain;
import com.analog.lyric.dimple.model.domains.IntDiscreteDomain;
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
	
	/**
	 * Creates a new instance with initial value of zero.
	 */
	public static RealValue create()
	{
		return new RealValue(0.0);
	}

	/**
	 * Creates a new instance with given initial value.
	 */
	public static RealValue create(double value)
	{
		return new RealValue(value);
	}
	
	protected RealValue(double value)
	{
		_value = value;
	}

	protected RealValue(RealValue that)
	{
		this(that._value);
	}
	
	/**
	 * Create an array of RealValues holding elements of a discrete domain.
	 * <p>
	 * @param domain a discrete domain that must contain only numeric elements.
	 * @since 0.08
	 * @throws ClassCastException if a domain element is not a subclass of {@link Number}.
	 */
	public static RealValue[] createFromDiscreteDomain(DiscreteDomain domain)
	{
		final int n = domain.size();
		final RealValue[] values = new RealValue[n];

		// Special cases are to avoid boxing of primitives
		if (domain instanceof DoubleDiscreteDomain)
		{
			final DoubleDiscreteDomain ddomain = (DoubleDiscreteDomain)domain;
			for (int i = 0; i < n; ++i)
			{
				values[i] = new RealValue(ddomain.getDoubleElement(i));
			}
		}
		else if (domain instanceof IntDiscreteDomain)
		{
			final IntDiscreteDomain idomain = (IntDiscreteDomain)domain;
			for (int i = 0; i < n; ++i)
			{
				values[i] = new RealValue(idomain.getIntElement(i));
			}
		}
		else
		{
			for (int i = 0; i < n; ++i)
			{
				values[i] = new RealValue(((Number)domain.getElement(i)).doubleValue());
			}
		}
		
		
		return values;
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
		setDouble(value.getDouble());
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
		setDouble(((Number)requireNonNull(value)).doubleValue());
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
		setDouble(value);
	}
	
	@Override
	public void setBoolean(boolean value)
	{
		setDouble(value ? 1.0 : 0.0);
	}
	
	@Override
	public boolean valueEquals(Value other)
	{
		return _value == other.getDouble();
	}
}
