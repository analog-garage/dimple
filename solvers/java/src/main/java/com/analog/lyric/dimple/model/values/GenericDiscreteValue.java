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

import java.util.Objects;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class GenericDiscreteValue extends DiscreteValue
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	protected final DiscreteDomain _domain;
	protected Object _value;
	protected int _index;

	/*--------------
	 * Construction
	 */
	
	GenericDiscreteValue(Object value, DiscreteDomain domain, int index)
	{
		this._domain = domain;
		this._value = value;
		this._index = index;
	}
	
	GenericDiscreteValue(DiscreteDomain domain, int index)
	{
		this(domain.getElement(index), domain, index);
	}
	
	GenericDiscreteValue(DiscreteDomain domain)
	{
		this(domain, 0);
	}
	
	GenericDiscreteValue(GenericDiscreteValue that)
	{
		this(that._value, that._domain, that._index);
	}
	
	@Override
	public GenericDiscreteValue clone()
	{
		return new GenericDiscreteValue(this);
	}
	
	/*---------------
	 * Value methods
	 */
	
	@Override
	public void setFrom(Value other)
	{
		if (_domain.equals(other.getDomain()))
		{
			_value = Objects.requireNonNull(other.getObject());
			_index = other.getIndex();
		}
		else
		{
			setObject(other.getObject());
		}
	}
	
	@Override
	public DiscreteDomain getDomain()
	{
		return _domain;
	}
	
	@Override
	public final @NonNull Object getObject()
	{
		return _value;
	}
	
	@Override
	public final void setObject(@Nullable Object value)
	{
		Objects.requireNonNull(value);
		_index = _domain.getIndex(value);
		_value = value;
	}
	
	@Override
	public final int getIndex() {return _index;}
	
	@Override
	public final void setIndex(int index)
	{
		_index = index;
		_value = _domain.getElement(index);
	}
	
	@Override
	public final boolean valueEquals(Value other)
	{
		if (_domain.equals(other.getDomain()))
		{
			return _index == other.getIndex();
		}
		
		return super.valueEquals(other);
	}
}
