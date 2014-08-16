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

import com.analog.lyric.collect.Supers;
import com.analog.lyric.collect.UnmodifiableReleasableIterator;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Iterates over {@link OptionKeys} from starting declaring class up through its parents.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
class HierarchicalOptionKeyIterator extends UnmodifiableReleasableIterator<OptionKeys>
{
	private static final AtomicReference<HierarchicalOptionKeyIterator> _reusableInstance = new AtomicReference<>();
	
	private @Nullable Class<?> _next = null;
	
	/*--------------
	 * Construction
	 */
	
	private HierarchicalOptionKeyIterator()
	{
	}
	
	static HierarchicalOptionKeyIterator create(Class<? extends OptionKeyDeclarer> declarer)
	{
		HierarchicalOptionKeyIterator iter = _reusableInstance.getAndSet(null);
		if (iter == null)
		{
			iter = new HierarchicalOptionKeyIterator();
		}
		if (Supers.isStrictSubclassOf(declarer, OptionKeyDeclarer.class))
		{
			iter._next = declarer;
		}
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
	public @Nullable OptionKeys next()
	{
		@Nullable Class<?> declarer = _next;
		if (declarer == null)
		{
			return null;
		}
		
		Class<?> superClass = declarer.getSuperclass();
		_next = superClass != OptionKeyDeclarer.class ? superClass : null;
		
		return OptionKeys.declaredInClass(declarer);
	}

	@Override
	public void release()
	{
		_next = null;
		_reusableInstance.set(this);
	}
	
}