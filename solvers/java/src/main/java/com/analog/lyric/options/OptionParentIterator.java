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

import java.util.concurrent.atomic.AtomicReference;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.util.misc.Nullable;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class OptionParentIterator implements ReleasableIterator<IOptionHolder>
{
	private @Nullable IOptionHolder _next;
	
	private final static AtomicReference<OptionParentIterator> reusableInstance = new AtomicReference<>();
	
	private OptionParentIterator()
	{
	}
	
	public static OptionParentIterator create(IOptionHolder holder)
	{
		OptionParentIterator iter = reusableInstance.getAndSet(null);
		if (iter == null)
		{
			iter = new OptionParentIterator();
		}
		iter.reset(holder);
		return iter;
	}

	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		return _next != null;
	}

	@Override
	public @Nullable IOptionHolder next()
	{
		IOptionHolder result = _next;
		if (result != null)
		{
			_next = result.getOptionParent();
		}
		return result;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("Iterator.remove");
	}

	@Override
	public void release()
	{
		_next = null;
		reusableInstance.set(this);
	}
	
	public void reset(IOptionHolder holder)
	{
		_next = holder.getOptionParent();
	}
}
