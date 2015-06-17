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

package com.analog.lyric.dimple.data;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.IFactorGraphChild;

import net.jcip.annotations.Immutable;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
@Immutable
public class DataEntry<K extends IFactorGraphChild, T extends IDatum> implements Map.Entry<K, T>
{
	/*-------
	 * State
	 */
	
	private final K _key;
	private final @Nullable T _value;
	
	/*--------------
	 * Construction
	 */
	
	public DataEntry(K key, @Nullable T value)
	{
		_key = key;
		_value = value;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (obj instanceof Map.Entry)
		{
			final Map.Entry<?, ?> entry = (Map.Entry<?,?>)obj;
			return _key == entry.getKey() && Objects.equals(_value, entry.getValue());
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return _key.hashCode() ^ Objects.hashCode(_value);
	}
	
	/*---------------
	 * Entry methods
	 */
	
	@Override
	public K getKey()
	{
		return _key;
	}

	@Override
	public @Nullable T getValue()
	{
		return _value;
	}

	@Override
	public T setValue(@Nullable T value) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("setValue");
	}
}
