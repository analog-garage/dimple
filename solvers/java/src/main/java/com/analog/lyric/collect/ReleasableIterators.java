/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public class ReleasableIterators
{

	private static final class Wrapper<T> implements ReleasableIterator<T>
	{
		private @Nullable Iterator<T> _iter;

		private Wrapper(Iterator<T> iter)
		{
			_iter = iter;
		}

		private static final ThreadLocal<Wrapper<?>> instance = new ThreadLocal<Wrapper<?>>();

		static <T> Wrapper<T> make(Iterator<T> iter)
		{
			@SuppressWarnings("unchecked")
			Wrapper<T> wrapper = (Wrapper<T>)Wrapper.instance.get();
			if (wrapper != null)
			{
				Wrapper.instance.set(null);
				wrapper.reset(iter);
			}
			else
			{
				wrapper = new Wrapper<T>(iter);
			}

			return wrapper;
		}

		public void reset(Iterator<T> iter)
		{
			this._iter = iter;
		}

		@SuppressWarnings("null")
		@Override
		public boolean hasNext()
		{
			return this._iter.hasNext();
		}

		@SuppressWarnings("null")
		@Override
		public T next()
		{
			return this._iter.next();
		}

		@SuppressWarnings("null")
		@Override
		public void remove()
		{
			this._iter.remove();
		}

		@Override
		public void release()
		{
			if (instance.get() == null)
			{
				_iter = null;
				instance.set(this);
			}
		}
	}

	private static enum EmptyReleasableIterator implements ReleasableIterator<Object>
	{
		INSTANCE;

		@Override
		public boolean hasNext()
		{
			return false;
		}

		@NonNullByDefault(false)
		@Override
		public Object next()
		{
			return null;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("remove");
		}

		@Override
		public void release()
		{
		}
	}
	
	/**
	 * Returns a canonical empty releasable iterator.
	 * @since 0.07
	 */
	@SuppressWarnings("unchecked")
	public static <T> ReleasableIterator<T> emptyIterators()
	{
		return (ReleasableIterator<T>) EmptyReleasableIterator.INSTANCE;
	}
	
	/**
	 * Returns a {@link ReleasableIterator} for given collection.
	 * <p>
	 * This should only be used for situations in which the caller may encounter
	 * some collections that return {@link ReleasableIterator}s and some that do
	 * not and you want to avoid an explicit {@code instanceof} check.
	 */
	public static <T> ReleasableIterator<T> iteratorFor(Collection<T> c)
	{
		Iterator<T> iter = c.iterator();

		if (iter instanceof ReleasableIterator)
		{
			return (ReleasableIterator<T>)iter;
		}

		return Wrapper.make(iter);
	}
}
