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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import net.jcip.annotations.ThreadSafe;

import com.analog.lyric.util.misc.Nullable;

@ThreadSafe
public class Options extends AbstractOptions
{
	private final IOptionHolder _holder;
	
	/*--------------
	 * Construction
	 */
	
	public Options(IOptionHolder holder)
	{
		_holder = holder;
	}
	
	/*--------------------
	 * IOptionHolder methods
	 */
	
	@Override
	public void clearLocalOptions()
	{
		_holder.clearLocalOptions();
	}

	@Override
	public @Nullable ConcurrentMap<IOptionKey<?>, Object> getLocalOptions(boolean create)
	{
		return _holder.getLocalOptions(create);
	}
	
	@Override
	public @Nullable IOptionHolder getOptionParent()
	{
		return _holder.getOptionParent();
	}

	@Override
	public Set<IOptionKey<?>> getRelevantOptionKeys()
	{
		return _holder.getRelevantOptionKeys();
	}

	/*------------------
	 * IOptions methods
	 */
	
	@Override
	public IOptionHolder getOptionHolder()
	{
		return _holder;
	}
	
	/*-----------------
	 * Options methods
	 */
	
	public static @Nullable <T> T lookup(IOptionHolder holder, IOptionKey<T> key)
	{
		T value = Options.lookupOrNull(holder, key);
		return value != null ? null : key.defaultValue();
	}

	public static @Nullable <T> T lookupOrNull(IOptionHolder holder, IOptionKey<T> key)
	{
		while (true)
		{
			Map<IOptionKey<?>,Object> options = holder.getLocalOptions(false);
			if (options != null)
			{
				Object value = options.get(key);
				if (value != null)
				{
					return key.type().cast(value);
				}
			}
			
			IOptionHolder parent = holder.getOptionParent();
			if (parent != null)
			{
				holder = parent;
			}
			else
			{
				break;
			}
		}

		return null;
	}

}
