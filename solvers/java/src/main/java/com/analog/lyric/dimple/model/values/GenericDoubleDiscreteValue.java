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

import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;

public class GenericDoubleDiscreteValue extends DoubleDiscreteValue
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private int _index;
	
	/*--------------
	 * Construction
	 */
	
	GenericDoubleDiscreteValue(TypedDiscreteDomain<Double> domain)
	{
		super(domain, domain.getElement(0));
		_index = 0;
	}
	
	GenericDoubleDiscreteValue(GenericDoubleDiscreteValue other)
	{
		super(other);
		_index = other._index;
	}
	
	@Override
	public GenericDoubleDiscreteValue clone()
	{
		return new GenericDoubleDiscreteValue(this);
	}

	/*---------------
	 * Value methods
	 */
	
	@Override
	public final int getIndex()
	{
		return _index;
	}

	@Override
	public void setIndex(int index)
	{
		_index = index;
		_value = getDomain().getElement(index);
	}

	@Override
	public void setDouble(double value)
	{
		_value = value;
		_index = getDomain().getIndex(value);
	}
	
	@Override
	public void setBoolean(boolean value)
	{
		_value = value ? 1.0 : 0.0;
		_index = getDomain().getIndex(_value);
	}

	@Override
	public void setFrom(Value other)
	{
		if (other instanceof GenericDoubleDiscreteValue)
		{
			GenericDoubleDiscreteValue discrete = (GenericDoubleDiscreteValue)other;
			if (discrete.getDomain().equals(getDomain()))
			{
				_value = discrete._value;
				_index = discrete._index;
				return;
			}
		}
		setDouble(other.getDouble());
	}
}
