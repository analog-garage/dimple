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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Array-based binary heap implementation of {@link IHeap}.
 * <p>
 * Insertion, removal from head of the queue, and changing priority is O(log({@link #size})).
 * Bulk insertion and priority changes with deferred reordering is O(n + {@link #size}).
 * 
 * @author Christopher Barber
 * @since 0.0.5
 */
public class BinaryHeap<E> extends AbstractHeap<E>
{
	/**
	 * A heap entry.
	 *
	 * @see BinaryHeap#offer
	 * @since 0.05
	 */
	public static class Entry<E> extends AbstractEntry<E>
	{
		private int _offset;
		
		protected Entry(E element, double priority)
		{
			super(element, priority);
			_offset = -1;
		}

		protected Entry(Entry<E> that)
		{
			super(that);
			_offset = that._offset;
		}
		
		@Override
		public Entry<E> clone()
		{
			return new Entry<E>(this);
		}
		
		@Override
		public final boolean isOwned()
		{
			return _offset >= 0;
		}
	}
	
	private final ArrayList<Entry<E>> _heap;
	private int _orderedUpto;
	private boolean _deferOrdering;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct empty heap.
	 */
	public BinaryHeap()
	{
		this(16);
	}
	
	/**
	 * Construct empty heap with initial space for {@code capacity} elements.
	 */
	public BinaryHeap(int capacity)
	{
		_heap = new ArrayList<Entry<E>>(capacity);
		_deferOrdering = true;
		_orderedUpto = 0;
	}
	
	/**
	 * Construct a copy of the heap.
	 */
	public BinaryHeap(BinaryHeap<E> that)
	{
		@SuppressWarnings("unchecked")
		final ArrayList<Entry<E>> heap = (ArrayList<Entry<E>>) that._heap.clone();
		_heap = heap;
		_deferOrdering = that._deferOrdering;
		_orderedUpto = that._orderedUpto;
		for (int i = 0, end = heap.size(); i < end; ++i)
		{
			heap.set(i, heap.get(i).clone());
		}
	}
	
	public static <T> BinaryHeap<T> create()
	{
		return new BinaryHeap<T>();
	}
	
	public static <T> BinaryHeap<T> create(int capacity)
	{
		return new BinaryHeap<T>(capacity);
	}

	@Override
	public BinaryHeap<E> clone()
	{
		return new BinaryHeap<E>(this);
	}

	/*--------------------
	 * Collection methods
	 */
	
	@Override
	public void clear()
	{
		_heap.clear();
		_deferOrdering = true;
		_orderedUpto = 0;
	}
	
	@Override
	public final int size()
	{
		return _heap.size();
	}

	/*-------------------------------
	 * IDynamicPriorityQueue methods
	 */
	
	@Override
	public boolean changePriority(IEntry<E> oldEntry, double priority)
	{
		checkPriority(priority);
		
		if (!containsEntry(oldEntry))
		{
			return false;
		}
		
		final Entry<E> entry = (Entry<E>)oldEntry;
		final double oldPriority = entry._priority;
		entry._priority = priority;

		if (_deferOrdering)
		{
			_orderedUpto = Math.min(entry._offset, _orderedUpto);
		}
		else
		{
			heapAdjust(entry, oldPriority);
		}
		
		return true;
	}
	
	@Override
	public boolean containsEntry(IEntry<E> entry)
	{
		return entry instanceof Entry && containsEntry((Entry<E>)entry);
	}
	
	/**
	 * @see #containsEntry(IEntry)
	 */
	public boolean containsEntry(Entry<E> entry)
	{
		final int offset = entry._offset;
		return offset >= 0 && offset < _heap.size() &&  entry == _heap.get(offset);
	}
	
	@Override
	public final boolean deferOrdering()
	{
		return _deferOrdering;
	}
	
	@Override
	public boolean deferOrdering(boolean defer)
	{
		if (defer != _deferOrdering)
		{
			_deferOrdering = defer;

			if (!defer)
			{
				heapify();
			}
		}
		
		return true;
	}
	
	@Override
	public boolean deferOrderingForBulkAdd(int n)
	{
		return _deferOrdering || n >= _heap.size() && deferOrdering(true);
	}
	
	@Override
	public boolean deferOrderingForBulkChange(int n)
	{
		return _deferOrdering || n >= _heap.size() / 2 && deferOrdering(true);
	}

	@Override
	public void ensureCapacity(int capacity)
	{
		_heap.ensureCapacity(capacity);
	}
	
	@Override
	public @Nullable Entry<E> entryForElement(@Nullable Object element)
	{
		for (int i = 0, end = _heap.size(); i < end; ++i)
		{
			Entry<E> entry = _heap.get(i);
			if (entry.getElement().equals(element))
			{
				return entry;
			}
		}
		
		return null;
	}
	
	@Override
	public Iterator<? extends IEntry<E>> entryIterator()
	{
		return _heap.iterator();
	}

	@Override
	public final boolean isOrdered()
	{
		return _orderedUpto >= _heap.size();
	}
	
	@Override
	public boolean merge(IHeap<E> other)
	{
		if (!(other instanceof BinaryHeap))
		{
			return super.merge(other);
		}
		
		BinaryHeap<E> that = (BinaryHeap<E>)other;
		
		final int prevSize = _heap.size();

		_heap.addAll(that._heap);
		for (int i = prevSize, end = _heap.size(); i < end; ++i)
		{
			_heap.get(i)._offset = i;
		}
		that.clear();
		
		heapify();
		
		return true;
	}
	
	@Override
	public Entry<E> offer(E element, double priority)
	{
		checkPriority(priority);
		final Entry<E> entry = new Entry<E>(element, priority);
		entry._offset = _heap.size();
		_heap.add(entry);
		if (!_deferOrdering)
		{
			heapRaise(entry);
			_orderedUpto = _heap.size();
		}
		
		return entry;
	}

	@Override
	public @Nullable Entry<E> peekEntry()
	{
		Entry<E> entry = null;
		
		if (_heap.size() > 0)
		{
			heapify();
			entry =_heap.get(0);
		}
		
		return entry;
	}

	@Override
	public @Nullable Entry<E> pollEntry()
	{
		Entry<E> entry = null;

		final ArrayList<Entry<E>> heap = _heap;
		final int size = heap.size();
		
		switch (size)
		{
		case 0:
			break;
			
		case 1:
			entry = heap.remove(0);
			entry._offset = -1;
			_orderedUpto = 0;
			_deferOrdering = true;
			break;
			
		default:
			heapify();
			entry = heap.get(0);
			removeEntryNoCheck(entry);
			break;
		}
		
		return entry;
	}

	@Override
	public boolean removeEntry(IEntry<E> entry)
	{
		return entry instanceof Entry && removeEntry((Entry<E>)entry);
	}
	
	/**
	 * @see #removeEntry(IEntry)
	 */
	public boolean removeEntry(Entry<E> entry)
	{
		if (containsEntry(entry))
		{
			removeEntryNoCheck(entry);
			return true;
		}
		
		return false;
	}
	
	/*---------
	 * Private
	 */
	
	private void checkPriority(double priority)
	{
		if (Double.isNaN(priority))
		{
			throw new IllegalArgumentException("priority is not a number");
		}
	}
	
	private void heapAdjust(Entry<E> entry, double oldPriority)
	{
		final double priority = entry._priority;
		
		if (priority < oldPriority)
		{
			heapRaise(entry);
		}
		else if (priority > oldPriority)
		{
			heapify(entry._offset);
		}
	}
	
	/**
	 * Raise entry in heap until its priority is lower than everything below it.
	 * Only appropriate to call if entry's children are known to have higher
	 * priority.
	 */
	private void heapRaise(Entry<E> entry)
	{
		final double priority = entry._priority;
		for (int i = entry._offset; i > 0;)
		{
			final int parenti = (i - 1) / 2;
			final Entry<E> parent = _heap.get(parenti);
			
			if (parent._priority <= priority)
			{
				break;
			}
			
			heapSet(i, parent);
			heapSet(parenti, entry);
			
			i = parenti;
		}
	}
	
	/**
	 * Reorder heap.
	 */
	private void heapify()
	{
		final int orderedUpto = _orderedUpto;
		final int size = _heap.size();
		
		if (orderedUpto < size)
		{
			final int halfSize = size / 2;
			
			if (orderedUpto <= halfSize)
			{
				// Less than half the heap is already ordered, so do a full reorder.
				for (int i = halfSize; --i >= 0;)
				{
					heapify(i);
				}
			}
			else
			{
				// Most of the heap is already ordered and the unordered entries are
				// all leaf nodes, so we can just raise them in order.
				for (int i = orderedUpto; i < size; ++i)
				{
					heapRaise(_heap.get(i));
				}
			}
			
			
			_orderedUpto = size;
		}
		
		_deferOrdering = false;
	}
	
	/**
	 * Builds min heap rooted at index i given that i's children are already
	 * min heaps
	 */
	private void heapify(int i)
	{
		final ArrayList<Entry<E>> heap = _heap;
		final int end = _heap.size();
		
		while (true)
		{
			final Entry<E> entry = heap.get(i);
			final int l = i * 2 + 1;
			final int r = l + 1;
			double p = entry._priority;
			
			int min = i;
			
			if (l < end)
			{
				final double lp = heap.get(l)._priority;
				if (lp < p)
				{
					min = l;
					p = lp;
				}
				if (r < end)
				{
					if (heap.get(r)._priority < p)
					{
						min = r;
					}
				}
			}
			if (min == i)
			{
				break;
			}
			heapSet(i, heap.get(min));
			heapSet(min, entry);
			i = min;
		}
	}
	
	/**
	 * Set i'th slot of heap array to given entry and
	 * update entry's {@link #_offset} accordingly.
	 */
	private void heapSet(int i, Entry<E> entry)
	{
		entry._offset = i;
		_heap.set(i, entry);
	}

	private void removeEntryNoCheck(Entry<E> entry)
	{
		final int size = _heap.size();
		
		Entry<E> lastEntry = _heap.remove(size - 1);
		if (entry != lastEntry)
		{
			heapSet(entry._offset, lastEntry);
			if (_deferOrdering)
			{
				_orderedUpto = Math.min(entry._offset, _orderedUpto);
			}
			else
			{
				heapAdjust(lastEntry, entry._priority);
				_orderedUpto = size - 1;
			}
		}
		else
		{
			_orderedUpto = Math.min(entry._offset, _orderedUpto);
		}
		
		if (_heap.isEmpty())
		{
			_deferOrdering = true;
		}
		
		entry._offset = -1;
	}
	
}
