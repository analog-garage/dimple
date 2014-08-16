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

package com.analog.lyric.collect;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import com.google.common.collect.Interner;

/**
 * An {@link Interner} that weakly caches interned values.
 * <p>
 * This duplicates the functionality of {@linkplain com.google.common.collect.Interners#newWeakInterner
 * Interners.newWeakInterner()} from the Guava library. It is really only needed to work around a bug
 * in MATLAB.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@ThreadSafe
public class WeakInterner<E> implements Interner<E>
{
	private final WeakHashMap<E,WeakReference<E>> _interned = new WeakHashMap<>();

	/**
	 * Create a new interner for type {@code T}
	 * @since 0.07
	 */
	public static <T> WeakInterner<T> create()
	{
		return new WeakInterner<T>();
	}
	
	@NonNullByDefault(false)
	@Override
	public E intern(E value)
	{
		synchronized(this)
		{
			final WeakReference<E> ref = _interned.get(value);
			if (ref != null)
			{
				E internedValue = ref.get();
				if (internedValue != null)
				{
					return internedValue;
				}
			}
			
			_interned.put(value, new WeakReference<E>(value));
		}
		
		return value;
	}
	
}
