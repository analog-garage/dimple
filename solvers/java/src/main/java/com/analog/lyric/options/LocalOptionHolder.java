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

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.UnmodifiableIterator;

/**
 * Provides standard implementation of {@link IOptionHolder} methods.
 * <p>
 * Extends {@link AbstractOptionHolder} with local options and additional
 * methods for getting and setting options on this object.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class LocalOptionHolder extends AbstractOptionHolder
{
	/*-------
	 * State
	 */
	
	private volatile @Nullable ConcurrentMap<IOptionKey<?>,Object> _localOptions = null;
	
	/*---------------
	 * IOptionHolder
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation removes the underlying local option map.
	 */
	@Override
	public void clearLocalOptions()
	{
		_localOptions = null;
	}
	
	@Override
	public Collection<IOption<? extends Serializable>> getLocalOptions()
	{
		final ConcurrentMap<IOptionKey<?>,Object> localOptions = _localOptions;
		
		if (localOptions == null)
		{
			return Collections.emptyList();
		}

		return new AbstractCollection<IOption<? extends Serializable>>() {

			@Override
			public Iterator<IOption<? extends Serializable>> iterator()
			{
				return new UnmodifiableIterator<IOption<? extends Serializable>>() {

					final Iterator<Map.Entry<IOptionKey<?>,Object>> _iter = localOptions.entrySet().iterator();
					
					@Override
					public boolean hasNext()
					{
						return _iter.hasNext();
					}

					@Override
					public IOption<? extends Serializable> next()
					{
						final Map.Entry<IOptionKey<?>,Object> entry = _iter.next();
						return Option.create(entry.getKey(), entry.getValue());
					}
				};
			}

			@Override
			public int size()
			{
				return localOptions.size();
			}
			
		};
	}
	
	@Override
	@Nullable
	public <T extends Serializable> T getLocalOption(IOptionKey<T> key)
	{
		Map<IOptionKey<?>,Object> options = getLocalOptionMap(false);
		if (options != null)
		{
			final Object value = options.get(key);
			if (value != null)
			{
				return key.type().cast(value);
			}
		}

		return null;
	}
	
	@Override
	public <T extends Serializable> void setOption(IOptionKey<T> key, T value)
	{
		createLocalOptionMap().put(key, key.validate(value, this));
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns true.
	 */
	@Override
	public boolean supportsLocalOptions()
	{
		return true;
	}
	
	@Override
	public void unsetOption(IOptionKey<?> key)
	{
		Map<IOptionKey<?>,Object> map = getLocalOptionMap(false);
		if (map != null)
		{
			map.remove(key);
		}
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	protected ConcurrentMap<IOptionKey<?>, Object> createLocalOptionMap()
	{
		ConcurrentMap<IOptionKey<?>, Object> localOptions = _localOptions;

		// This uses the double-checked locking idiom. It is only guaranteed to be correct
		// if the underlying variable is volatile. The second check inside the synchronized
		// block is necessary because another thread could have created the options and
		// added an option before this thread gets to hold the lock.
		
		if (localOptions == null)
		{
			synchronized(this)
			{
				localOptions = _localOptions;
				if (localOptions == null)
				{
					_localOptions = localOptions = new ConcurrentHashMap<IOptionKey<?>, Object>();
				}
			}
		}

		return localOptions;
	}
	
	protected @Nullable ConcurrentMap<IOptionKey<?>, Object> getLocalOptionMap(boolean create)
	{
		return create ? createLocalOptionMap() : _localOptions;
	}

}
