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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.analog.lyric.util.misc.Nullable;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class OptionHolder extends AbstractOptionHolder
{
	private volatile @Nullable ConcurrentMap<IOptionKey<?>,Object> _localOptions = null;
	
	@Override
	public void clearLocalOptions()
	{
		_localOptions = null;
	}
	
	@Override
	public ConcurrentMap<IOptionKey<?>, Object> createLocalOptions()
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
	
	@Override
	public @Nullable ConcurrentMap<IOptionKey<?>, Object> getLocalOptions(boolean create)
	{
		return create ? createLocalOptions() : _localOptions;
	}
}
