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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.FiniteFieldDomain;
import com.analog.lyric.dimple.model.domains.FiniteFieldNumber;

public class FiniteFieldValue extends DiscreteValue
{
	private static final long serialVersionUID = 1L;

	private FiniteFieldNumber _value;

	public FiniteFieldValue(FiniteFieldNumber value)
	{
		_value = value;
	}
	
	/**
	 * @since 0.07
	 */
	public FiniteFieldValue(FiniteFieldDomain domain)
	{
		_value = new FiniteFieldNumber(0, domain);
	}

	public FiniteFieldValue(FiniteFieldValue other)	// Copy constructor
	{
		_value = other.getObject().clone();
	}

	@Override
	public DiscreteValue clone()
	{
		return new FiniteFieldValue(this._value);
	}

	@Override
	public DiscreteDomain getDomain()
	{
		return _value.getDomain();
	}
	
	@Override
	public FiniteFieldNumber getFiniteField()
	{
		return _value;
	}
	
	@Override
	public void setFiniteField(FiniteFieldNumber value)
	{
		_value = value;
	}
	
	@Override
	public @NonNull FiniteFieldNumber getObject()
	{
		return _value;
	}

	@Override
	public void setObject(@Nullable Object value)
	{
		_value = (FiniteFieldNumber)Objects.requireNonNull(value);
	}

	@Override
	public int getIndex()
	{
		return _value.intValue();
	}

	@Override
	public void setIndex(int index)
	{
		_value = _value.cloneWithNewValue(index);
	}
	
	@Override
	public int getInt()
	{
		return _value.intValue();
	}

}
