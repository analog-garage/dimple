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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;

import net.jcip.annotations.Immutable;

/**
 * Immutable version of {@link DiscreteValue}
 * @since 0.08
 * @author Christopher Barber
 */
@Immutable
final class ConstantDiscreteValue extends DiscreteValue
{
	private static final long serialVersionUID = 1L;

	private final DiscreteDomain _domain;
	private final int _index;
	
	public ConstantDiscreteValue(DiscreteDomain domain, int index)
	{
		_domain = domain;
		_index = index;
	}
	
	@Override
	public DiscreteValue clone()
	{
		return this;
	}
	
	@Override
	public DiscreteValue immutableClone()
	{
		return this;
	}

	@Override
	public DiscreteValue mutableClone()
	{
		return Value.createWithIndex(_domain, _index);
	}
	
	@Override
	public DiscreteDomain getDomain()
	{
		return _domain;
	}

	@Override
	public int getIndex()
	{
		return _index;
	}

	@Override
	public double getDouble()
	{
		return _domain.getDoubleElement(_index);
	}
	
	@Override
	public int getInt()
	{
		return _domain.getIntElement(_index);
	}
	
	@Override
	public Object getObject()
	{
		return _domain.getElement(_index);
	}

	@Override
	public boolean isMutable()
	{
		return false;
	}
	
	@Override
	public void setIndex(int index)
	{
		throw notMutable();
	}
	
	@Override
	public void setObject(@Nullable Object value)
	{
		throw notMutable();
	}
	
	@Override
	public boolean valueEquals(Value other)
	{
		if (getDomain().equals(other.getDomain()))
		{
			return getIndex() == other.getIndex();
		}
		
		return super.valueEquals(other);
	}

}
