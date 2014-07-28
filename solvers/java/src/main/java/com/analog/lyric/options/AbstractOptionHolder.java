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
import com.analog.lyric.util.misc.Nullable;

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
		final T value = getOption(key);
		return value != null ? value : key.defaultValue();
	}

	@Override
	@Nullable
	public <T extends Serializable> T getOption(IOptionKey<T> key)
	{
		T result = null;
		final ReleasableIterator<? extends IOptionHolder> delegates = getOptionDelegates();
		
		while (delegates.hasNext())
		{
			final IOptionHolder delegate = delegates.next();
		
			result = delegate.getLocalOption(key);
			if (result != null)
			{
				break;
			}
		}
		
		delegates.release();

		return result;
	}
}
