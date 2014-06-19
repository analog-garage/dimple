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

package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

import com.analog.lyric.util.misc.Nullable;

@Immutable
public class Option<T> implements IOption<T>
{
	/*-------
	 * State
	 */
	
	private final IOptionKey<T> _key;
	private final T _value;
	
	/*--------------
	 * Construction
	 */
	
	public Option(IOptionKey<T> key)
	{
		this(key, key.defaultValue());
	}
	
	public Option(IOptionKey<T> key, T value)
	{
		_key = key;
		_value = key.type().cast(value);
	}
	
	/*-----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof Option)
		{
			Option<?> that = (Option<?>)other;
			return _key == that._key && _value.equals(that._value);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return _key.hashCode() * 11 + _value.hashCode();
	}
	
	@Override
	public String toString()
	{
		return String.format("%s=%s", _key.toString(), _value.toString());
	}
	
	/*-----------------
	 * IOption methods
	 */
	
	@Override
	public final IOptionKey<T> key()
	{
		return _key;
	}

	@Override
	public final T value()
	{
		return _value;
	}
}
