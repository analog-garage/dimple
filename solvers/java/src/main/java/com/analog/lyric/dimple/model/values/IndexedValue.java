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

package com.analog.lyric.dimple.model.values;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.collect.IKeyed;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import com.google.common.collect.Iterators;
import com.google.common.primitives.Ints;

/**
 * Represents an association between an index and a {@link Value} object.
 * <p>
 * For purposes of comparison only the {@link #getIndex()} value is considered.
 */
@NotThreadSafe
public class IndexedValue implements Comparable<IndexedValue>, IKeyed<Integer>
{
	/*-------
	 * State
	 */
	
	private int _index;
	private Value _value;
	
	/*--------------
	 * Construction
	 */
	
	public IndexedValue(int index, Value value)
	{
		_index = index;
		_value = value;
	}

	/*----------------
	 * Object methods
	 */
	
	/**
	 * True if {@code other} is an {@code IndexedValue} with same {@link #getIndex()} value.
	 */
	@Override
	public boolean equals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		return other instanceof IndexedValue && _index == ((IndexedValue)other)._index;
	}

	/**
	 * Hash code is based soley on {@code #getIndex()} value.
	 */
	
	@Override
	public int hashCode()
	{
		return Ints.hashCode(_index);
	}
	
	/*--------------------
	 * Comparable methods
	 */
	
	/**
	 * Comparison based on integer comparison of {@link #getIndex()} values.
	 */
	@Override
	@NonNullByDefault(false)
	public int compareTo(IndexedValue other)
	{
		return Ints.compare(_index, other._index);
	}
	
	/*----------------
	 * IKeyed methods
	 */
	
	/**
	 * Returns {@link #getIndex()}.
	 */
	@Override
	public Integer getKey()
	{
		return _index;
	}
	
	/*----------------------
	 * IndexedValue methods
	 */
	
	public int getIndex()
	{
		return _index;
	}
	
	public Value getValue()
	{
		return _value;
	}
	
	/*----------------
	 * Nested classes
	 */
	
	public static class SingleList extends AbstractList<IndexedValue>
	{
		private final IndexedValue _value;

		private final static AtomicReference<SingleList> _cachedInstance = new AtomicReference<SingleList>();

		/*--------------
		 * Construction
		 */
		
		private SingleList(int index, Value value)
		{
			_value = new IndexedValue(index, value);
		}
		
		public static SingleList create(int index, Value value)
		{
			SingleList list = _cachedInstance.getAndSet(null);
			if (list == null)
			{
				list = new SingleList(index, value);
			}
			else
			{
				list._value._index = index;
				list._value._value = value;
			}
			return list;
		}
		
		public void release()
		{
			_cachedInstance.set(this);
		}
		
		/*------------------
		 * Iterable methods
		 */
		
		@Override
		public Iterator<IndexedValue> iterator()
		{
			return Iterators.singletonIterator(_value);
		}
		
		/*--------------
		 * List methods
		 */
		
		@Override
		public IndexedValue get(int i)
		{
			if (i != 0)
				throw new IndexOutOfBoundsException();
			return _value;
		}

		@Override
		public int size()
		{
			return 1;
		}
	}
}
