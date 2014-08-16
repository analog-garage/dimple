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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import com.google.common.collect.Ordering;

@NonNullByDefault(false) // to avoid clashes with methods overridden from standard library
public class SkipSet<E> extends AbstractSkipList<E>
	implements Set<E>, ReleasableIterableCollection<E>, Cloneable
{
    /*
     * Construction
     */
    
	public SkipSet(@NonNull Comparator<? super E> comparator)
	{
		super(comparator, (short)1);
	}
	
	/**
	 * @since 0.05
	 */
	public static @NonNull <T extends Comparable<T>> SkipSet<T> create()
	{
		return create(Ordering.natural());
	}
	
	/**
	 * @since 0.05
	 */
	public static @NonNull <T> SkipSet<T> create(@NonNull Comparator<? super T> comparator)
	{
		return new SkipSet<T>(comparator);
	}
	
	/**
	 * @since 0.05
	 */
	public static @NonNull <T extends Comparable<T>> SkipSet<T> create(@NonNull Collection<? extends T> collection)
	{
		final SkipSet<T> set = create();
		set.addAll(collection);
		return set;
	}
	
	/**
	 * @since 0.05
	 */
	public static @NonNull <T> SkipSet<T> create(@NonNull SortedSet<T> set)
	{
		final SkipSet<T> result = create(set.comparator());
		result.addAll(set);
		return result;
	}
	
	/**
	 * @since 0.05
	 */
	public static @NonNull <T> SkipSet<T> create(@NonNull SkipSet<T> set)
	{
		final SkipSet<T> result = create(set.comparator());
		result.addAll(set);
		return result;
	}
	
	/*----------------
	 * Object methods
	 */
	
	/**
	 * Returns a copy of the set.
	 * <p>
	 * SUBCLASS NOTE: this does not use the default {@link Object#clone} implementation. Therefore subclasses
	 * should not assume that fields introduced by the subclass will be cloned automatically.
	 * @since 0.05
	 */
	@Override
	public SkipSet<E> clone()
	{
		return create(this);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof Set)
		{
			Set<?> otherSet = (Set<?>)other;
			
			if (otherSet.size() == size())
			{
				// TODO: special case if other is a SkipSet or a SortedSet with the same comparator

				for (Object[] node = this.getNextNode(this.head); node != null; node = this.getNextNode(node))
				{
					if (!otherSet.contains(node[0]))
					{
						return false;
					}
				}

				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		// Implements computation described in Set.hashCode()
		int hash = 0;
		for (Object[] node = this.getNextNode(this.head); node != null; node = this.getNextNode(node))
		{
			hash += node[0].hashCode();
		}
		return hash;
	}
	
    /*
     * Set methods
     */
    
	@Override
	public boolean add(E value)
	{
		final int prevSize = this.size();
		this.addNode(value);
		return this.size() != prevSize;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		boolean changed = false;
		
		java.util.Iterator<? extends E> iter = c.iterator();
		
		// TODO: add special case for when c is a SkipSet or SortedSet with same comparator.
		
		while (iter.hasNext())
		{
			E val = iter.next();
			changed |= this.add(val);
		}
		
		if (iter instanceof ReleasableIterator)
		{
			((ReleasableIterator<? extends E>)iter).release();
		}
		
		return changed;
	}
	
	@Override
    public boolean contains(Object searchValue)
    {
		try
		{
			@SuppressWarnings("unchecked")
			E val = (E)searchValue;
			return this.contains2(val);
		}
		catch (ClassCastException ex)
		{
			return false;
		}
    }

	@Override
	public boolean containsAll(Collection<?> collection)
	{
		try
		{
			@SuppressWarnings("unchecked")
			Collection<? extends E> c = (Collection<? extends E>) collection;
			return this.containsAll2(c);
		}
		catch (ClassCastException ex)
		{
			return false;
		}
	}
	
	public static class Iterator<E> extends KeyIterator<E> implements ReleasableIterator<E>
	{
		private static final ThreadLocal<KeyIterator<?>> reusableInstance = new ThreadLocal<KeyIterator<?>>();

		public Iterator(@Nullable SkipSet<E> set)
		{
			super(set);
		}
		
		protected static <T> Iterator<T> make(@Nullable SkipSet<T> set)
		{
			@SuppressWarnings("unchecked")
			@Nullable Iterator<T> iter = (Iterator<T>)Iterator.reusableInstance.get();
			
			if (iter != null)
			{
				Iterator.reusableInstance.set(null);
				iter.reset(set);
			}
			else
			{
				iter = new Iterator<T>(set);
			}
			
			return iter;
		}
		
		@Override
		public void release()
		{
			if (Iterator.reusableInstance.get() == null)
			{
				this.reset(null);
				Iterator.reusableInstance.set(this);
			}
		}

		public void reset(@Nullable SkipSet<E> set)
		{
			super.reset(set);
		}
	}
	
	@Override
	public @NonNull Iterator<E> iterator()
	{
		return Iterator.make(this);
	}
	
	@Override
	public boolean remove(Object value)
	{
		try
		{
			@SuppressWarnings("unchecked")
			E val = (E)value;
			return this.remove2(val);
		}
		catch (ClassCastException ex)
		{
			return false;
		}
	}

	/** Like {@link #remove} but argument must be of type {@code E}. */
	public boolean remove2(E value)
	{
		return super.removeNode(value) != null;
	}
	

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean changed = false;
		
		java.util.Iterator<?> iter = c.iterator();
		
		while (iter.hasNext())
		{
			Object val = iter.next();
			changed |= this.remove(val);
		}
		
		if (iter instanceof ReleasableIterator)
		{
			((ReleasableIterator<?>)iter).release();
		}

		return changed;
	}
	
	@Override
	public boolean retainAll(Collection<?> c)
	{
		boolean changed = false;
		Iterator<E> iter = this.iterator();
		for (E elt = iter.next(); elt != null; elt = iter.next())
		{
			if (!c.contains(elt))
			{
				iter.remove();
				changed = true;
			}
		}
		iter.release();
		return changed;
	}
	
	@Override
	public Object[] toArray()
	{
		return this.toArray(new Object[this.size()]);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a)
	{
		T[] array = a;
		if (array.length < this.size())
		{
			array = (T[]) Array.newInstance(a.getClass().getComponentType(), this.size());
		}
		
		Object[] node = this.head;
		int i = 0;
		
		while (true)
		{
			Object[] next = this.getNextNode(node);
			if (next == null)
			{
				break;
			}
			node = next;
			array[i++] = (T)this.getNodeKey(node);
		}
		
		return array;
	}
	
	/*
	 * SortedSet methods - this class does not explicitly implement this interface since the subset methods are missing.
	 */

	/**
	 * Returns first element of set.
	 * @throws NoSuchElementException if set is empty.
	 */
	public E first()
	{
		final Object[] node = this.firstNode();
		if (node == null)
		{
			throw new NoSuchElementException();
		}
		return this.getNodeKey(node);
	}
	
	/**
	 * Returns last element of set.
	 * @throws NoSuchElementException if set is empty.
	 */
	public E last()
	{
		final Object[] node = this.lastNode();
		if (node == null)
		{
			throw new NoSuchElementException();
		}
		return this.getNodeKey(node);
	}
	
	/*
	 * NavigableSet methods - this class does not explicitly implement this interface since methods are missing.
	 */
	
	/**
	 * Returns lowest value in set that is greater than or equal to {@code value} or null.
	 * This is faster than {@link #floor} method.
	 */
	public @Nullable E ceiling(E value)
	{
		Object[] node = this.findCeilingNode(value);
		return node == null ? null : this.getNodeKey(node);
	}
	
	/**
	 * Returns greatest value in set that is less than or equal to {@code value} or null.
	 */
	public @Nullable E floor(E value)
	{
		Object[] node = this.findFloorNode(value);
		return node == null ? null : this.getNodeKey(node);
	}
	
	public @Nullable E higher(E key)
	{
		Object[] node = this.findHigherNode(key);
		return node == null ? null : this.getNodeKey(node);
	}
	
	public @Nullable E lower(E value)
	{
		return this.getNodeKey(this.findLowerNode(value));
	}
	
	public @Nullable E pollFirst()
	{
		Object[] node = this.pollFirstNode();
		return node == null ? null : this.getNodeKey(node);
	}
	
	public @Nullable E pollLast()
	{
		Object[] node = this.pollLastNode();
		return node == null ? null : this.getNodeKey(node);
	}
	
	/*
	 * SkipSet methods
	 */
	
	/** Like {@link #contains} but argument must be of type {@code E}. */
    public boolean contains2(E searchValue)
    {
    	return this.containsNode(searchValue);
    }
	
	/** Like {@link #remove} but argument must be of type {@link Collection}{@code <? extends E>}. */
	public boolean containsAll2(Collection<? extends E> c)
	{
		boolean containsAll = true;
		
		java.util.Iterator<? extends E> iter = c.iterator();
		
		while (iter.hasNext())
		{
			E val = iter.next();
			if (!this.contains2(val))
			{
				containsAll = false;
				break;
			}
		}
		
		if (iter instanceof ReleasableIterator)
		{
			((ReleasableIterator<? extends E>)iter).release();
		}

		
		return containsAll;
	}

}

