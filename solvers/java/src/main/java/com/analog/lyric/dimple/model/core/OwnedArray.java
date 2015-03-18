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

package com.analog.lyric.dimple.model.core;

import static java.util.Objects.*;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ReleasableIterators;
import com.google.common.collect.UnmodifiableIterator;


/**
 * Maintains owned nodes of a single type for a FactorGraph
 * <p>
 * This class holds the nodes in a simple array indexed by the local id
 * of the node, which is assigned when the node is added to this collection.
 * There may be holes in the array, but only if a node is removed.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@NotThreadSafe
abstract class OwnedArray<T extends Node> extends AbstractCollection<T>
{
	/*-------
	 * State
	 */
	
	private @Nullable T[] _nodes;
	private int _size;
	private int _end;
	
	/*--------------
	 * Construction
	 */
	
	OwnedArray()
	{
	}

	/*--------------------
	 * Collection methods
	 */

	@NonNullByDefault(false)
	@Override
	public boolean add(T node)
	{
		if (containsNode(node))
		{
			return false;
		}
		
		int index = allocate();
		requireNonNull(_nodes)[index] = node;
		node.setLocalId(index|idTypeMask());
		return true;
	}
	
	@NonNullByDefault(false)
	@Override
	public boolean addAll(Collection<? extends T> nodes)
	{
		ensureCapacity(capacity() + nodes.size());
		final T[] array = requireNonNull(_nodes);
		
		final int prevSize = _size;
		final int typeMask = idTypeMask();
		
		for (T node : nodes)
		{
			if (!containsNode(node))
			{
				int index = _end;
				_end = index + 1;
				++_size;
				array[index] = node;
				node.setLocalId(index|typeMask);
			}
		}
		
		return _size != prevSize;
	}
	
	@Override
	public void clear()
	{
		if (_size > 0)
		{
			Arrays.fill(_nodes, 0, _end, null);
		}
		_size = _end = 0;
	}
	
	@Override
	public boolean contains(@Nullable Object obj)
	{
		if (obj instanceof Node)
		{
			return containsNode((Node)obj);
		}
		
		return false;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		if (_size == 0)
		{
			return ReleasableIterators.emptyIterator();
		}
		
		final T[] array = requireNonNull(_nodes);

		return new UnmodifiableIterator<T>() {

			private int _next = 0;
			private final T[] _array = array;
				
			@Override
			public boolean hasNext()
			{
				for (; _next < _end; ++_next)
				{
					if (_array[_next] != null)
					{
						return true;
					}
				}
				
				return false;
			}

			@Override
			public @Nullable T next()
			{
				for (; _next < _end; ++_next)
				{
					final T node = _array[_next];
					if (node != null)
					{
						++_next;
						return node;
					}
				}
				
				return null;
			}
		};
	}
	
	@Override
	public boolean remove(@Nullable Object obj)
	{
		if (obj instanceof Node)
		{
			return removeNode((Node)obj);
		}
		
		return false;
	}
	
	@Override
	public int size()
	{
		return _size;
	}
	
	/*--------------------
	 * OwnedArray methods
	 */
	
	int capacity()
	{
		final T[] nodes = _nodes;
		return nodes != null ? nodes.length : 0;
	}
	
	void capacity(int capacity)
	{
		if (capacity >= _end)
		{
			_nodes = resize(_nodes, capacity);
		}
	}
	
	boolean containsNode(Node node)
	{
		return node == getByLocalId(node.getLocalId());
	}
	
	void ensureCapacity(int newCapacity)
	{
		if (newCapacity > capacity())
		{
			int nextPowerOfTwo = Integer.highestOneBit(newCapacity);
			if (nextPowerOfTwo < newCapacity)
			{
				nextPowerOfTwo <<= 1;
			}
			capacity(nextPowerOfTwo);
		}
	}
	
	@SuppressWarnings("null")
	T get(int n)
	{
		return _nodes[n];
	}
	
	/**
	 * Return's nth node in array (skipping null entries).
	 */
	T getNth(int n)
	{
		if (n >= 0 && n < _size)
		{
			final T[] nodes = requireNonNull(_nodes);
			
			int nNulls = _end - _size;
			if (nNulls == 0)
			{
				return nodes[n];
			}

			for (T node : nodes)
			{
				if (node != null && --n < 0)
				{
					return node;
				}
			}
		}
		
		throw new IndexOutOfBoundsException();
	}
	
	@Nullable T getByLocalId(int localId)
	{
		final T[] nodes = _nodes;
		if (nodes != null)
		{
			int index = NodeId.indexFromLocalId(localId);
			if (index < _end)
			{
				return nodes[index];
			}
		}
		return null;
	}
	
	boolean removeNode(Node node)
	{
		final T[] nodes = _nodes;
		if (nodes != null)
		{
			final int id = node.getLocalId();
			final int index = NodeId.indexFromLocalId(id);
			if (index < _end && nodes[index] == node)
			{
				nodes[index] = null;
				--_size;
				return true;
			}
		}
		return false;
	}
	
	/*------------------
	 * Abstract methods
	 */
	
	abstract int idTypeMask();
	abstract T[] resize(@Nullable T[] array, int length);
	
	/*-----------------
	 * Private methods
	 */
	
	/**
	 * Allocates a slot for node and returns its index.
	 */
	private int allocate()
	{
		++_size;
		int index = _end++;
		ensureCapacity(_end);
		return index;
	}
}
