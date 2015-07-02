/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import static java.lang.String.*;
import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;

/**
 * A real value view of a {@link RealJointValue}.
 * <p>
 * This object behaves like a {@link RealValue} but refers to a single component
 * of a separate {@link RealJointValue} object.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class RealJointComponentValue extends Value
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	protected RealJointValue _realJoint;
	protected int _index;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct for specified joint value and index.
	 * 
	 * @param value is an existing {@link RealJointValue} to which this will be linked.
	 * @param index is a positive index into {@code value}'s double array.
	 * @since 0.08
	 */
	public RealJointComponentValue(RealJointValue value, int index)
	{
		_realJoint = value;
		setComponentIndex(index);
	}
	
	/**
	 * Construct for specified joint value and zero index.
	 * 
	 * @param value is an existing {@link RealJointValue} to which this will be linked.
	 * @since 0.08
	 */
	public RealJointComponentValue(RealJointValue value)
	{
		_realJoint = value;
		_index = 0;
	}
	
	protected RealJointComponentValue(RealJointComponentValue other)
	{
		_realJoint = other._realJoint;
		_index = other._index;
	}
	
	@Override
	public Value clone()
	{
		return new RealJointComponentValue(this);
	}

	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return String.valueOf(getDouble());
	}
	
	/*---------------
	 * Value methods
	 */
	
	@Override
	public Domain getDomain()
	{
		// TODO: if Real/RealJoint start storing domains, we should pass them on here.
		return RealDomain.unbounded();
	}

	@Override
	public int getInt()
	{
		return (int)Math.round(getDouble());
	}
	
	@Override
	public double getDouble()
	{
		return _realJoint.getValue(_index);
	}
	
	@Override
	public Double getObject()
	{
		return _realJoint.getValue(_index);
	}
	
	@Override
	public void setBoolean(boolean value)
	{
		setDouble(value ? 1.0 : 0.0);
	}
	
	@Override
	public void setDouble(double value)
	{
		_realJoint.setValue(_index, value);
	}
	
	@Override
	public void setFrom(Value value)
	{
		setDouble(value.getDouble());
	}
	
	@Override
	public void setInt(int value)
	{
		setDouble(value);
	}

	@Override
	public void setObject(@Nullable Object value)
	{
		_realJoint.setValue(_index, ((Number)requireNonNull(value)).doubleValue());
	}

	@Override
	public boolean valueEquals(Value other)
	{
		return getDouble() == other.getDouble();
	}
	
	/*---------------
	 * Local methods
	 */
	
	/**
	 * Returns the current component index into the {@linkplain #getRealJoint() underlying
	 * real-joint value}'s array.
	 * @since 0.08
	 */
	public int getComponentIndex()
	{
		return _index;
	}
	
	/**
	 * The underlying {@link RealJointValue}
	 * @since 0.08
	 */
	public RealJointValue getRealJoint()
	{
		return _realJoint;
	}

	/**
	 * Changes the {@linkplain #getComponentIndex() component index to specified value}.
	 * @param index must be non-negative and less than size of the {@linkplain #getRealJoint() underlying
	 * real-joint value}'s array.
	 * @throws IndexOutOfBoundsException if {@code index} is not in valid range.
	 * @since 0.08
	 */
	public void setComponentIndex(int index)
	{
		if (index < 0 || index >= _realJoint.getDoubleArray().length)
			throw new IndexOutOfBoundsException(format("%d", index));
		_index = index;
	}
	
	/**
	 * Resets the underlying {@link RealJointValue} and sets {@linkplain #getComponentIndex() component index} to zero.
	 * @since 0.08
	 */
	public void setRealJoint(RealJointValue value)
	{
		_realJoint = value;
		_index = 0;
	}

	/**
	 * Resets the underlying {@link RealJointValue} and {@linkplain #getComponentIndex() component index}.
	 * @since 0.08
	 * @throws IndexOutOfBoundsException if {@code index} is not valid for {@code value}'s array.
	 */
	public void setRealJoint(RealJointValue value, int index)
	{
		_realJoint = value;
		setComponentIndex(index);
	}
}
