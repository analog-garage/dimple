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

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.RealJointDomain;

public class RealJointValue extends Value
{
	private static final long serialVersionUID = 1L;

	protected double[] _value;
	
	/*--------------
	 * Construction
	 */
	
	RealJointValue(double[] value)
	{
		_value = value;
	}

	RealJointValue(RealJointDomain domain)
	{
		_value = new double[domain.getDimensions()];
	}

	RealJointValue(RealJointValue that)
	{
		_value = that._value.clone();
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public RealJointValue clone()
	{
		return new RealJointValue(this);
	}
	
	@Override
	public String toString()
	{
		return Arrays.toString(_value);
	}
	
	/*---------------
	 * Value methods
	 */
	
	@Override
	public RealJointDomain getDomain()
	{
		return RealJointDomain.create(_value.length);
	}
	
	@Override
	public double[] getDoubleArray()
	{
		return _value;
	}
	
	@Override
	public @NonNull double[] getObject()
	{
		return _value;
	}
	
	@Override
	public void setObject(@Nullable Object value)
	{
		_value = (double[])Objects.requireNonNull(value);
	}
	
	public final double[] getValue() {return _value;}
	public final void setValue(double[] value) {_value = value;}

	
	// Get/set a specific element of the sample
	public double getValue(int index) {return _value[index];}
	public void setValue(int index, double value) {_value[index] = value;}
	
	@Override
	public boolean valueEquals(Value other)
	{
		Object otherObj = other.getObject();
		return otherObj instanceof double[] && Arrays.equals(_value, (double[])otherObj);
	}
}
