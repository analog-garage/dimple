package com.analog.lyric.dimple.model.values;

import java.util.AbstractList;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.NotThreadSafe;

import com.google.common.primitives.Ints;

@NotThreadSafe
public class IndexedValue implements Comparable<IndexedValue>
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
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		return other instanceof IndexedValue && _index == ((IndexedValue)other)._index;
	}

	@Override
	public int hashCode()
	{
		return Ints.hashCode(_index);
	}
	
	/*--------------------
	 * Comparable methods
	 */
	
	@Override
	public int compareTo(IndexedValue other)
	{
		return Ints.compare(_index, other._index);
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
