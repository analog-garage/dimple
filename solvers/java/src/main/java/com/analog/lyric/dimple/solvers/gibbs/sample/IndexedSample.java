package com.analog.lyric.dimple.solvers.gibbs.sample;

import java.util.AbstractList;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.NotThreadSafe;

import com.google.common.primitives.Ints;


// REFACTOR: move and rename
@NotThreadSafe
public class IndexedSample implements Comparable<IndexedSample>
{
	/*-------
	 * State
	 */
	
	private int _index;
	private ObjectSample _value;
	
	/*--------------
	 * Construction
	 */
	
	public IndexedSample(int index, ObjectSample value)
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
		
		return other instanceof IndexedSample && _index == ((IndexedSample)other)._index;
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
	public int compareTo(IndexedSample other)
	{
		return Ints.compare(_index, other._index);
	}
	
	/*-----------------------
	 * IndexedSample methods
	 */
	
	public int getIndex()
	{
		return _index;
	}
	
	public ObjectSample getValue()
	{
		return _value;
	}
	
	/*----------------
	 * Nested classes
	 */
	
	public static class SingleList extends AbstractList<IndexedSample>
	{
		private final IndexedSample _value;

		private final static AtomicReference<SingleList> _cachedInstance = new AtomicReference<SingleList>();

		/*--------------
		 * Construction
		 */
		
		private SingleList(int index, ObjectSample value)
		{
			_value = new IndexedSample(index, value);
		}
		
		public static SingleList create(int index, ObjectSample value)
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
		public IndexedSample get(int i)
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
