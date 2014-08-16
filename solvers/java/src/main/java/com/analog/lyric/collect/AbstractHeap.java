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

import java.util.AbstractCollection;
import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Base implementation of {@link IHeap} with default implementation
 * of some methods.
 *
 * @author Christopher Barber
 * @since 0.05
 */
public abstract class AbstractHeap<E>
	extends AbstractCollection<E>
	implements IHeap<E>
{
	/**
	 * Base implementation for heap entries.
	 *
	 * @since 0.05
	 */
	public abstract static class AbstractEntry<E> implements IEntry<E>
	{
		private final E _element;
		protected double _priority;
		
		protected AbstractEntry(E element, double priority)
		{
			_element = element;
			_priority = priority;
		}
		
		protected AbstractEntry(AbstractEntry<E> that)
		{
			this(that._element, that._priority);
		}
		
		@Override
		public abstract AbstractEntry<E> clone();
		
		@Override
		public final E getElement()
		{
			return _element;
		}
		
		@Override
		public final double getPriority()
		{
			return _priority;
		}
	}
	
	@Override
	public abstract AbstractHeap<E> clone();
	
	/*------------------
	 * Iterable methods
	 */
	
	@Override
	public Iterator<E> iterator()
	{
		final Iterator<? extends IEntry<E>> entries = entryIterator();
		
		return new Iterator<E>() {
			@Override
			public boolean hasNext()
			{
				return entries.hasNext();
			}

			@Override
			public E next()
			{
				return entries.next().getElement();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
	
	/*--------------------
	 * Collection methods
	 */
	
	@Override
	public abstract void clear();
	
	@Override
	public boolean contains(@Nullable Object object)
	{
		return entryForElement(object) != null;
	}
	
	@Override
	public boolean remove(@Nullable Object object)
	{
		IEntry<E> entry = entryForElement(object);
		return entry != null ? removeEntry(entry) : false;
	}
	
	/*---------------
	 * IHeap methods
	 */
	
	@Override
	public boolean containsEntry(IEntry<E> entry)
	{
		if (entry.isOwned())
		{
			Iterator<? extends IEntry<E>> iter = entryIterator();
			while (iter.hasNext())
			{
				if (entry == iter.next())
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public @Nullable IEntry<E> entryForElement(@Nullable Object element)
	{
		Iterator<? extends IEntry<E>> iter = entryIterator();
		while (iter.hasNext())
		{
			IEntry<E> entry = iter.next();
			if (entry.getElement().equals(element))
			{
				return entry;
			}
		}
		
		return null;
	}
	
	@Override
	public void ensureCapacity(int capacity)
	{
	}
	
	@Override
	public boolean isOrdered()
	{
		return true;
	}
	
	@Override
	public boolean deferOrdering()
	{
		return false;
	}
	
	@Override
	public boolean deferOrdering(boolean defer)
	{
		return !defer;
	}
	
	@Override
	public boolean deferOrderingForBulkAdd(int n)
	{
		return false;
	}
	
	@Override
	public boolean deferOrderingForBulkChange(int n)
	{
		return false;
	}
	
	@Override
	public boolean merge(IHeap<E> other)
	{
		Iterator<? extends IEntry<E>> entries = other.entryIterator();
		while (entries.hasNext())
		{
			IEntry<E> entry = entries.next();
			offer(entry.getElement(), entry.getPriority());
		}
		other.clear();
		
		return false;
	}
	
	@Override
	public @Nullable E peek()
	{
		final IEntry<E> entry = peekEntry();
		return entry != null ? entry.getElement() : null;
	}

	@Override
	public @Nullable E poll()
	{
		final IEntry<E> entry = pollEntry();
		return entry != null ? entry.getElement() : null;
	}

}
