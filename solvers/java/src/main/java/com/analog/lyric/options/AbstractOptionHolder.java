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

package com.analog.lyric.options;

import java.io.Serializable;

import com.analog.lyric.collect.ReleasableIterator;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Provides default implementation of some {@link IOptionHolder} methods.
 * <p>
 * @see LocalOptionHolder
 * @see StatelessOptionHolder
 * @since 0.07
 * @author Christopher Barber
 */
public abstract class AbstractOptionHolder implements IOptionHolder
{
	@Override
	public ReleasableIterator<? extends IOptionHolder> getOptionDelegates()
	{
		return OptionParentIterator.create(this);
	}
	
	@Override
	public @Nullable IOptionHolder getOptionParent()
	{
		return null;
	}

	@Override
	public <T extends Serializable> T getOptionOrDefault(IOptionKey<T> key)
	{
		final T value = getOptionAndSource(key, null);
		return value != null ? value : key.defaultValue();
	}

	@Override
	@Nullable
	public <T extends Serializable> T getOption(IOptionKey<T> key)
	{
		return getOptionAndSource(key, null);
	}

	/*------------------------------
	 * AbstractOptionHolder methods
	 */
	
	/**
	 * Returns value and source of option with given key if set, else null.
	 * <p>
	 * @param key is a non-null option key.
	 * @param source if non-null with length at least one, the first element will
	 * be set to the object whose {@linkplain #getLocalOption(IOptionKey) local option}
	 * setting produced the return value. Nothing will be written if this method returns null.
	 * @see #getOption
	 * @since 0.07
	 */
	@Nullable
	public <T extends Serializable> T getOptionAndSource(IOptionKey<T> key, @Nullable IOptionHolder[] source)
	{
		T result = null;
		final ReleasableIterator<? extends IOptionHolder> delegates = getOptionDelegates();
		
		while (delegates.hasNext())
		{
			final IOptionHolder delegate = delegates.next();
		
			result = delegate.getLocalOption(key);
			if (result != null)
			{
				if (source != null && source.length > 0)
				{
					source[0] = delegate;
				}
				break;
			}
		}
		
		delegates.release();

		return result;
	}
}
