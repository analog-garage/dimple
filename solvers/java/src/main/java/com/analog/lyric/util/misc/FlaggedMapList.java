package com.analog.lyric.util.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import cern.colt.map.OpenIntIntHashMap;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.math.Utilities;
import com.google.common.primitives.Ints;

public class FlaggedMapList<T extends IGetId> implements IFlaggedMapList<T>
{
	/*-------
	 * State
	 */
	
	/**
	 * Maps ids to index in array plus one (so that the value zero is reserved to indicate a missing value
	 * in accordance with the behavior of {@link OpenIntIntHashMap#get}.
	 */
	private final OpenIntIntHashMap _idToIndex;
	
	private final ArrayList<T> _values;
	// TODO: this uses a byte per boolean. Either make this an explicit array of bytes and
	// allow use of all 8 bits per element, or convert to a long[] and use shifts for removal.
	private boolean[] _flags;
	
	// TODO: defer O(n) removal cost until a method that requires access by index is called.
	// This would amortize the cost when multiple removals are made.
	
	/*--------------
	 * Construction
	 */
	
	public FlaggedMapList()
	{
		this(16);
	}
	
	public FlaggedMapList(int initialCapacity)
	{
		_idToIndex = new OpenIntIntHashMap(initialCapacity);
		_values = new ArrayList<T>(initialCapacity);
		_flags = ArrayUtil.EMPTY_BOOLEAN_ARRAY;
	}
	
	/*--------------------
	 * Collection methods
	 */
	
	@Override
	public boolean add(T value)
	{
		int id = value.getId();
		if (_idToIndex.containsKey(id))
		{
			return false;
		}
		
		int index = _values.size();
		_values.add(value);
		_idToIndex.put(id, index + 1);
		return true;
	}

	// REFACTOR: move to AbstractMapList
	@Override
	public boolean addAll(Collection<? extends T> values)
	{
		ensureCapacity(size() + values.size());
		
		boolean changed = false;
		for (T t : values)
		{
			if (add(t))
				changed = true;
		}
		return changed;
	}

	@Override
	public void clear()
	{
		_idToIndex.clear();
		_values.clear();
		_flags = ArrayUtil.EMPTY_BOOLEAN_ARRAY;
	}

	// REFACTOR: AbstractMapList
	@Override
	public boolean contains(Object value)
	{
		return (value instanceof IGetId) ? contains((IGetId)value) : false ;
	}

	@Override
	public boolean containsAll(Collection<?> objects)
	{
		for (Object object : objects)
		{
			if (!contains(object))
			{
				return false;
			}
		}
		
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return _values.isEmpty();
	}

	@Override
	public Iterator<T> iterator()
	{
		return _values.iterator();
	}

	@Override
	public boolean remove(Object value)
	{
		boolean removed = false;
		
		if (value instanceof IGetId)
		{
			int id = ((IGetId)value).getId();
			int index = _idToIndex.get(id) - 1;
			
			if (index >= 0)
			{
				removed = true;
	
				_idToIndex.removeKey(id);
				_values.remove(index);
				// Need to update all of the indexes.
				for (int i = index, endi = _values.size(); i < endi; ++i)
				{
					_idToIndex.put(_values.get(i).getId(), i + 1);
				}
				if (_flags.length > index)
				{
					System.arraycopy(_flags, index + 1, _flags, index, _values.size() - index -1);
				}
			}
		}
		
		return removed;
	}

	// REFACTOR: AbstractMapList
	@Override
	public boolean removeAll(Collection<?> arg0)
	{
		boolean changed = false;
		for (Object o : arg0)
		{
			if(remove(o))
				changed = true;
		}
		return changed;
	}

	// REFACTOR: AbstractMapList
	@Override
	public boolean retainAll(Collection<?> keep)
	{
		boolean changed = false;
		for (int i = size(); --i >= 0;)
		{
			T value = getByIndex(i);
			if (!keep.contains(value))
			{
				remove(value);
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public int size()
	{
		return _idToIndex.size();
	}

	@Override
	public Object[] toArray()
	{
		return _values.toArray();
	}

	@Override
	public <T2> T2[] toArray(T2[] array)
	{
		return _values.toArray(array);
	}

	/*------------------
	 * IMapList methods
	 */
	
	// REFACTOR: AbstractMapList
	@Override
	public void addAll(T[] nodes)
	{
		if (nodes != null)
		{
			ensureCapacity(size() + nodes.length);
			for (T n : nodes) add(n);
		}
	}

	@Override
	public boolean contains(IGetId node)
	{
		return _idToIndex.containsKey(node.getId());
	}

	@Override
	public void ensureCapacity(int minCapacity)
	{
		_idToIndex.ensureCapacity(minCapacity);
		_values.ensureCapacity(minCapacity);
	}

	@Override
	public T getByKey(int id)
	{
		int index = _idToIndex.get(id) - 1;
		return index >= 0 ? _values.get(index) : null;
	}

	@Override
	public T getByIndex(int index)
	{
		return _values.get(index);
	}

	@Override
	public List<T> values()
	{
		return _values;
	}

	/*-------------------------
	 * IFlaggedMapList methods
	 */

	@Override
	public void clearFlags()
	{
		_flags = ArrayUtil.EMPTY_BOOLEAN_ARRAY;
	}

	@Override
	public boolean isFlagged(T node)
	{
		int index = _idToIndex.get(node.getId()) - 1;
		return index >= 0 && isFlagged(index);
	}

	@Override
	public boolean isFlagged(int index)
	{
		return _flags.length > index && _flags[index];
	}
	
	@Override
	public void setFlag(T node, boolean flag)
	{
		int id = node.getId();
		int index = _idToIndex.get(id) - 1;
		setFlag(index, flag);
	}
	
	@Override
	public void setFlag(int index, boolean flag)
	{
		_values.get(index); // Call for bounds check on index
		if (_flags.length <= index)
		{
			_flags = Arrays.copyOf(_flags, Utilities.nextPow2(index + 1));
		}
		_flags[index] = flag;
	}

	public void setFlags(boolean flag, int ... indices)
	{
		if (indices.length > 0)
		{
			int maxIndex = Ints.max(indices);
			if (maxIndex >= _flags.length)
			{
				_flags = Arrays.copyOf(_flags, Utilities.nextPow2(maxIndex + 1));
			}
			for (int index : indices)
			{
				_flags[index] = flag;
			}
		}
	}
}
