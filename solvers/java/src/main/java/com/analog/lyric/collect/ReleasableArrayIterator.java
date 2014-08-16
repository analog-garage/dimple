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

import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link ReleasableIterator} that visits all of the elements of an array.
 * 
 * @since 0.05
 */
@NotThreadSafe
public final class ReleasableArrayIterator<T> extends UnmodifiableReleasableIterator<T>
{
	private @Nullable T[] _array;
	private int _size;
	private int _index;

	/*---------------
	 * Construction
	 */
	
	private static final AtomicReference<ReleasableArrayIterator<?>> _reusableInstance =
		new AtomicReference<ReleasableArrayIterator<?>>();
	
	/**
	 * Allocates a new instance for given {@code array}.
	 * <p>
	 * This method is thread safe.
	 */
	static public <T2> ReleasableArrayIterator<T2> create(T2[] array)
	{
		@SuppressWarnings("unchecked")
		ReleasableArrayIterator<T2> iter = (ReleasableArrayIterator<T2>) _reusableInstance.getAndSet(null);
		if (iter == null)
		{
			iter = new ReleasableArrayIterator<T2>();
		}
		iter.reset(array);
		return iter;
	}
	
	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		return _index < _size;
	}

	@SuppressWarnings("null")
	@Override
	public T next()
	{
		return _array[_index++];
	}

	/*----------------------------
	 * ReleasableIterator methods
	 */
	
	@Override
	public void release()
	{
		_array = null;
		_reusableInstance.lazySet(this);
	}
	
	void reset(T[] array)
	{
		_array = array;
		_size = array.length;
		_index = 0;
	}
}
