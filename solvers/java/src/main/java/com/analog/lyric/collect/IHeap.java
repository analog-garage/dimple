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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Priority queue supporting dynamically changeable priorities of type double.
 * <p>
 * Unlike {@link java.util.PriorityQueue}, the priorities are stored separately
 * from the elements and ordering is based directly on the double values with
 * no intervening comparator. An element with the lowest value will be at the
 * head of the queue.
 * <p>
 * @author Christopher Barber
 * @since 0.05
 */
public interface IHeap<E> extends Collection<E>, Cloneable
{
	/*------------------
	 * Internal classes
	 */
	
	/**
	 * Interface for {@link IHeap} entry.
	 * @since 0.05
	 */
	public interface IEntry<E> extends Cloneable
	{
		/**
		 * Returns a shallow copy of this entry.
		 */
		public IEntry<E> clone();

		/**
		 * Returns non-null element contained in this entry. The element
		 * is guaranteed to not change as long as entry {@link #isOwned()}.
		 */
		public E getElement();

		/**
		 * Priority of this entry, which may change.
		 * @see IHeap#changePriority(IEntry, double)
		 */
		public double getPriority();
		
		/**
		 * True if entry is currently a member of a queue.
		 */
		public boolean isOwned();
	}
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Returns a copy of the queue, cloning all of its entries.
	 */
	public IHeap<E> clone();
	
	/*---------------
	 * Local methods
	 */
	
	/**
	 * Change priority of given entry in queue.
	 * <p>
	 * When changing more than half of the priorities in bulk, it may
	 * be more efficient to defer reordering.
	 * <p>
	 * @return false if entry was not in the queue.
	 * @see #deferOrdering(boolean)
	 * 
	 * @throws IllegalArgumentException if priority is not a number.
	 */
	public boolean changePriority(IEntry<E> entry, double priority);
	
	/**
	 * True if entry is contained in this queue.
	 */
	public boolean containsEntry(IEntry<E> entry);
	
	/**
	 * If true, then operations that might affect the order of the queue will not
	 * cause it to be reordered until required by some operation such as {@link #peek()}
	 * or {@link #poll()}, after which this attribute will automatically be set to false.
	 * <p>
	 * Implementations are not required to implement deferral.
	 * <p>
	 * @see #deferOrdering(boolean)
	 * @see #isOrdered()
	 */
	public boolean deferOrdering();
	
	/**
	 * Defers or undefers ordering of queue.
	 * <p>
	 * When set to true and the implementation supports it, reordering of the queue will be
	 * deferred until the invocation of the first method that requires it, namely one of the
	 * peek or poll methods or this method with {@code defer}
	 * set to false. When supported, this will lead to faster amortized reordering when making
	 * bulk changes affecting the majority of the entries in the queue.
	 * <p>
	 * When set to false, the queue will immediately be ordered.
	 * <p>
	 * @return true if value of attribute was set to specified value. Will always return false if {@code defer}
	 * is true and implementation does not support deferral.
	 * @see #deferOrderingForBulkAdd
	 * @see #deferOrderingForBulkChange
	 */
	public boolean deferOrdering(boolean defer);
	
	/**
	 * Defer ordering given {@code n} entries to be added to the queue.
	 * <p>
	 * Invokes {@link #deferOrdering(boolean)} if {@code n} is large enough to
	 * expect an amortized performance benefit.
	 * <p>
	 * @param n is the number of entries the caller intends to add to the queue.
	 * @return true if {@link #deferOrdering()} was enabled.
	 */
	public boolean deferOrderingForBulkAdd(int n);
	
	/**
	 * Defer ordering given {@code n} entries to change their priorities.
	 * <p>
	 * Invokes {@link #deferOrdering(boolean)} if {@code n} is large enough to
	 * expect an amortized performance benefit.
	 * <p>
	 * @param n is the number of entries the caller intends to invoke {@link #changePriority(IEntry, double)} on.
	 * @return true if {@link #deferOrdering()} was enabled.
	 */
	public boolean deferOrderingForBulkChange(int n);
	
	/**
	 * Return an entry containing given element, not necessarily the one
	 * with the lowest priority, or else null.
	 * <p>
	 * Assume O({@link #size}) time complexity unless implementation documents
	 * otherwise.
	 */
	public @Nullable IEntry<E> entryForElement(@Nullable Object element);
	
	/**
	 * Ensure that queue has sufficient capacity for the specified number of elements.
	 * <p>
	 * For some implementations, calling this before adding many elements can avoid the
	 * cost of incrementally growing the capacity as elements are added.
	 */
	public void ensureCapacity(int capacity);
	
	/**
	 * Returns an iterator over the heap entries not necessarily in heap order.
	 */
	public Iterator<? extends IEntry<E>> entryIterator();
	
	/**
	 * Is queue currently in correct partial order.
	 * <p>
	 * This should only be false if {@link #deferOrdering()} is true.
	 */
	public boolean isOrdered();
	
	/**
	 * Destructively merges contents of {@code other} heap into this one.
	 * <p>
	 * Removes all elements from {@code other} and adds them to this heap. Underlying
	 * entries will only be transferred if class of {@code other} is compatible with
	 * this class (typically meaning it is the same class).
	 * <p>
	 * @return true if entry objects were transferred.
	 */
	public boolean merge(IHeap<E> other);
	
	/**
	 * Adds instance of {@code element} to the queue with given {@code priority} and
	 * returns the entry created for it, which may be used to relatively efficiently
	 * remove it or change its priority. Unless the implementation does not support it,
	 * the same element may be added multiple times with different priorities.
	 * 
	 * @throws IllegalArgumentException if priority is not a number.
	 */
	public IEntry<E> offer(E element, double priority) throws IllegalArgumentException;
	
	/**
	 * Returns element that will be returned by {@link #poll()} without removing it from the queue.
	 * <p>
	 * @see #peekEntry()
	 * @see #poll()
	 */
	public @Nullable E peek();
	
	/**
	 * Returns entry that will be returned by {@link #pollEntry()} without removing it from the queue.
	 * <p>
	 * @see #peek()
	 * @see #pollEntry()
	 */
	public @Nullable IEntry<E> peekEntry();
	
	/**
	 * Removes element with the lowest priority from the queue.
	 * <p>
	 * @see #pollEntry()
	 * @see #peek()
	 */
	public @Nullable E poll();
	
	/**
	 * Removes and returns entry with the lowest priority from the queue.
	 * <p>
	 * @see #poll()
	 * @see #peekEntry()
	 */
	public @Nullable IEntry<E> pollEntry();
	
	/**
	 * Remove given entry from queue.
	 * @return false if entry was not contained in the queue.
	 */
	public boolean removeEntry(IEntry<E> entry);
}
