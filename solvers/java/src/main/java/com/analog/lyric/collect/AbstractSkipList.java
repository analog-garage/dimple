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

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class for collections based on a skip list.
 * <p>
 * This class is intended to be subclassed to implement various types of
 * collections based on a skip list. There is no reason to declare this
 * type directly in interfaces.
 * 
 * @author Christopher Barber
 *
 * @param <K> is the type of the key contained in the list that is used to
 * determine its order.
 * 
 * @see SkipSet
 * @see SkipMap
 */
public abstract class AbstractSkipList<K>
{

	/*
	 * State
	 */
	
	/*
	 * Every node in the list is represented as an array of Objects where
	 * the first slot contains the key, the slots from minDepthOffset to
	 * maxDepthOffset contain pointers to the next node in the list at each
	 * depth. Additional slots after the first may be used to store values.
	 * 
	 * The current implementation uses a scale factor of 4 at each level
	 * as described in generateNewNodeDepth method, which should result in
	 * an average of 1 1/3 next pointers per node. Including the Java per-array
	 * overhead of 3 words, the average memory is 5 1/3 words per node if the
	 * nodes contain only a key (6 1/3 if you add a single value). By comparison
	 * Java's TreeMap.Entry takes up 8 words including the Object overhead of 2.
	 * 
	 * We could theoretically knock a word per node off by replacing the
	 * Object[] representation with class for each possible depth with
	 * exactly that many next pointers, but it is probably not worth the
	 * trouble.
	 * 
	 * TODO: we may want to add prev pointers at depth 0 to allow for constant time
	 * access to the tail of the list, and linear order reverse iteration. Adding
	 * full multi-level prev pointers would probably overly complicate the logic
	 * but would allow for some speedup in locating nodes.
	 */
	
	/** Comparator used to order elements in the list by key. */
	protected final Comparator<? super K> comparator;
	
	/** Head node points to first nodes in list at every depth. Key slot is null. */
	protected Object[] head;
	
	/** Offset of next pointer with lowest depth. */
	protected final short minDepthOffset;
	
	/** Offset of next pointer with greatest depth currently in the list. */
	protected short maxDepthOffset;
	
	/** The current size of the list */
	private int size;
	
	/*
	 * Private methods
	 */
	
	private static final ThreadLocal<Object[]> tempPrecursorNode = new ThreadLocal<Object[]>();
	
	final static Object[] allocatePrecursorNode()
	{
		Object[] precursor = AbstractSkipList.tempPrecursorNode.get();
		if (precursor == null)
		{
			precursor = new Object[20];
		}
		else
		{
			AbstractSkipList.tempPrecursorNode.set(null);
		}
		return precursor;
	}
	
	/**
	 * Computes precursor node for first node in the list.
	 * @see #makePrecursorNode
	 */
	private final Object[] makeFirstPrecursorNode()
	{
		Object[] precursor = allocatePrecursorNode();
		
		Object[] firstNode = this.firstNode();
		
		final int minOffset = this.minDepthOffset;
		int maxOffset = minOffset;
		
		if (firstNode != null)
		{
			maxOffset = Math.min(this.getNodeMaxDepthOffset(firstNode) + 1, this.maxDepthOffset);
		}
		Arrays.fill(precursor, minOffset, maxOffset + 1, this.head);
		
		return precursor;
	}
	
	/**
	 * Computes an abstract "precursor" node for given {@code key}. This
	 * returns a node that is not actually in the list and whose next
	 * pointer slots at each level points at the node that immediately
	 * precedes the lowest node with key greater than or equal to {@code key}.
	 * Return node for reuse using {@link #releasePrecursorNode}.
	 */
	private final Object[] makePrecursorNode(@Nullable K key)
	{
		Object[] precursor = allocatePrecursorNode();
		
		final Comparator<? super K> c = this.comparator;

		Object[] node = this.head;

		for (int i = this.maxDepthOffset; i >= this.minDepthOffset; --i)
		{
			while (true)
			{
				final @Nullable Object[] nextAtLevel = this.getNextNodeAtDepthOffset(node, i);
				if (nextAtLevel == null || c.compare(this.getNodeKey(nextAtLevel), key) >= 0)
				{
					break;
				}
				node = nextAtLevel;
			}
			this.setNextNodeAtDepthOffset(precursor, i, node);
		}
		return precursor;
	}

	/**
	 * Release a node previously returned by {@link #makePrecursorNode}.
	 * The {@code node} must not be used after calling this function.
	 * This will null out next pointer slots up to the current value
	 * of {@link #maxDepthOffset}.
	 */
	private void releasePrecursorNode(Object[] node)
	{
		Arrays.fill(node, this.minDepthOffset, this.maxDepthOffset + 1, null);
		AbstractSkipList.tempPrecursorNode.set(node);
	}

	private final void removeNextNode(Object[] precursor)
	{
		Object[] x = this.getNextNode(precursor);
		assert(x != null);
		Object[] node = this.getNextNode(x);
		assert(node != null);
		short maxOffset = this.maxDepthOffset;

		for (int i = this.minDepthOffset; i <= maxOffset; ++i)
		{
			Object[] prev = this.getNextNodeAtDepthOffset(precursor, i);
			assert(prev != null);
			if (this.getNextNodeAtDepthOffset(prev, i) != node)
			{
				break;
			}
			this.setNextNodeAtDepthOffset(prev, i, this.getNextNodeAtDepthOffset(node, i));
		}

		while (maxOffset > this.minDepthOffset && this.getNextNodeAtDepthOffset(this.head, maxOffset) == null)
		{
			this.setNextNodeAtDepthOffset(precursor, maxOffset, null);
			--maxOffset;
		}
		if (this.maxDepthOffset != maxOffset)
		{
			this.maxDepthOffset = maxOffset;
			// Shrink head node.
			this.head = Arrays.copyOf(this.head, maxOffset + 1);
		}
		
		--this.size;
	}

	/*
	 * Construction
	 */

	protected AbstractSkipList(Comparator<? super K> comparator, short minDepth)
	{
		this.comparator = comparator;
		this.minDepthOffset = minDepth;
		this.maxDepthOffset = minDepth;
		this.size = 0;
		this.head = this.makeNode(0, null);
	}

	/*
	 * Public methods
	 */
	
	/** Removes all elements from the list. */
	public void clear()
	{
		if (this.head.length > this.minDepthOffset + 1)
		{
			this.head = new Object[this.minDepthOffset + 1];
		}
		else
		{
			this.head[this.minDepthOffset] = null;
		}
		this.size = 0;
		this.maxDepthOffset = this.minDepthOffset;
	}

	public Comparator<? super K> comparator()
	{
		return this.comparator;
	}

	/** True if {@link #size()} is zero */
	public final boolean isEmpty()
	{
		return this.size == 0;
	}

	/** The number of elements currently in the list. */
	public final int size() {
		return this.size;
	}

	/*
	 * Protected methods
	 */
	
	/** Finds node with given {@code key}, adding a new one if missing, and returns it. */
	protected final Object[] addNode(@Nullable K key)
	{
		final Object[] precursor = this.makePrecursorNode(key);
		final Object[] x = this.getNextNode(precursor);
		assert(x != null);
		Object[] node = this.getNextNode(x);

		if (node == null || comparator.compare(this.getNodeKey(node), key) != 0)
		{
			short depth = generateNewNodeDepth(size);
			node = this.makeNode(depth, key);
			final short maxOffset = (short)(depth + this.minDepthOffset);

			if (maxOffset > maxDepthOffset)
			{
				Object[] curHead = this.head;
				if (maxOffset >= curHead.length)
				{
					// Grow the head node to fit.
					this.head = Arrays.copyOf(curHead, maxOffset + 1);
					// And replace it in precursor node.
					for (int i = this.minDepthOffset; i < curHead.length; ++i)
					{
						if (this.getNextNodeAtDepthOffset(precursor, i) == curHead)
						{
							this.setNextNodeAtDepthOffset(precursor, i, this.head);
						}
					}
					curHead = this.head;
				}
				
				for (int i = maxDepthOffset + 1; i <= maxOffset; i++)
				{
					this.setNextNodeAtDepthOffset(precursor, i, curHead);
				}
				maxDepthOffset = maxOffset;
			}

			for (int i = this.minDepthOffset; i <= maxOffset; i++)
			{
				Object[] prevNode = this.getNextNodeAtDepthOffset(precursor, i);
				assert(prevNode != null);
				this.setNextNodeAtDepthOffset(node, i, this.getNextNodeAtDepthOffset(prevNode, i));
				this.setNextNodeAtDepthOffset(prevNode, i, node);
			}
			++this.size;
		}
		
		this.releasePrecursorNode(precursor);

		return node;
	}
	
	protected final boolean containsNode(K key)
	{
		Object[] node = this.findCeilingNode(key);
		return node != null ? key.equals(this.getNodeKey(node)) : false;
	}
	
	/**
	 * Returns the node containing the least element in the set with value greater than or
	 * equal to {@code value}. Returns null if there is no such value.
	 */
	protected final @Nullable Object[] findCeilingNode(K key)
	{
		return this.getNextNode(this.findLowerNode(key));
	}

	protected final @Nullable Object[] findFloorNode(K key)
	{
		Object[] node = this.findLowerNode(key);
		
		while (true)
		{
			node = this.getNextNode(node);
			if (node == null || this.comparator.compare(key, this.getNodeKey(node)) >= 0)
			{
				break;
			}
		}
		
		return node;
	}

	protected final @Nullable Object[] findHigherNode(K key)
	{
		Object[] node = this.findCeilingNode(key);
		while (node != null && this.comparator.compare(key, this.getNodeKey(node)) == 0)
		{
			node = this.getNextNode(node);
		}
		return node;
	}

	/**
	 * Returns the node containing the greatest value strictly less than {@code value}.
	 * Returns the {@link #head} node if there is no such value in the set.
	 */
	protected final Object[] findLowerNode(K key)
	{
		Object[] precursor = this.makePrecursorNode(key);
		Object[] node = this.getNextNode(precursor);
		this.releasePrecursorNode(precursor);
		assert(node != null);
		return node;
	}

	protected final @Nullable Object[] firstNode()
	{
		if (this.isEmpty())
		{
			throw new NoSuchElementException();
		}
		
		return this.getNextNode(head);
	}
	
	static private Random random = new Random();
	
	/**
	 * Returns a new randomly generated depth to be used for a new node.
	 * <p>
	 * This version produces a logarithmically distributed random value such that
	 * the number of nodes at level n is expected to be 4 times the number at level
	 * n + 1 and the expected depth of the resulting list is log4(size).
	 * <p>
	 * The factor 4 was chosen to minimize the average number of compares required
	 * to locate an element in the list. Assuming a logarithmic distribution with base b,
	 * it should require an average of between 1 and b + 1 compares at each level. So
	 * the expected number of compares is:
	 * <p>
	 *  (b + 2)/2 * log<sub>b</sub>(size)
	 * <p>
	 * so we want to find the minimum value of b &gt; 1 of
	 * <p>
	 * (b + 2) / 2ln(b)
	 * <p>
	 * which is ~4.3. Since this is close to 4, and log4 can be calculated efficiently
	 * for integers. That is what we use here.
	 */
	protected static short generateNewNodeDepth(int size)
	{
		// maxDepth ~ log4(curSize)
		final int maxDepth = (32 - Integer.numberOfLeadingZeros(size)) / 2;
		short depth = 0;
		
		if (maxDepth != 0)
		{
			final int r = random.nextInt((2 << maxDepth) - 1);
			final int inverseDepth = (32 - Integer.numberOfLeadingZeros(r)) / 2;
			depth = (short)(maxDepth - inverseDepth);
		}
		
		return depth;
	}
	
	/** Returns the next node in the list after {@code node}. */
	protected final @Nullable Object[] getNextNode(Object[] node)
	{
		return (Object[])node[this.minDepthOffset];
	}
	
	/** Returns the next node in the list at the given depth offset. */
	protected final @Nullable Object[] getNextNodeAtDepthOffset(Object[] node, int depthOffset)
	{
		return (Object[])node[depthOffset];
	}
	
	protected final @Nullable Object[] getNode(K key)
	{
		Object[]node = this.findCeilingNode(key);
		return node != null && key.equals(this.getNodeKey(node)) ? node : null;
	}
	
	/** Gets the key from the {@code node} */
	protected final K getNodeKey(Object[] node)
	{
		@SuppressWarnings("unchecked")
		K key = (K)node[0];
		return key;
	}
	
	/** Returns the offset of the next pointer with the greatest depth in {@code node} */
	protected final short getNodeMaxDepthOffset(Object[] node)
	{
		return (short)(node.length - 1);
	}
	
	protected final @Nullable Object[] lastNode()
	{
		Object[] node = null;
		
		if (this.size > 0)
		{
			node = this.head;
			for (int level = this.maxDepthOffset; level >= this.minDepthOffset; --level)
			{
				while (true)
				{
					@Nullable Object[] next = this.getNextNodeAtDepthOffset(node, level);
					
					if (next != null)
					{
						node = next;
					}
					else
					{
						break;
					}
				}
			}
		}

		return node;
	}
	
	protected final Object[] makeNode(int maxDepth, @Nullable K key)
	{
		Object[] node = new Object[maxDepth + this.minDepthOffset + 1];
		node[0] = key;
		return node;
	}

	protected final @Nullable Object[] pollFirstNode()
	{
		Object[] firstNode = this.getNextNode(this.head);
		
		if (firstNode != null)
		{
			Object[] precursor = this.makeFirstPrecursorNode();
			this.removeNextNode(precursor);
			this.releasePrecursorNode(precursor);
		}
		
		return firstNode;
	}
	
	protected final @Nullable Object[] pollLastNode()
	{
		// TODO: implement pollLastNode more efficiently

		Object[] lastNode = this.lastNode();
		if (lastNode != null)
		{
			this.removeNode(this.getNodeKey(lastNode));
		}

		return lastNode;
	}

	/**
	 * Remove and return node with given {@code key}. Returns null if {@code key} is null or
	 * no such key in list.
	 */
	protected final @Nullable Object[] removeNode(@Nullable K key)
	{
		@Nullable Object[] node = null;
		
		if (key != null)
		{
			final Object[] precursor = this.makePrecursorNode(key);
			final Object[] x = this.getNextNode(precursor);
			assert(x != null);
			node = this.getNextNode(x);

			if (node != null)
			{
				if (comparator.compare(this.getNodeKey(node), key) == 0)
				{
					this.removeNextNode(precursor);
				}
				else
				{
					node = null;
				}
			}
			this.releasePrecursorNode(precursor);
		}

		return node;
	}

	/**
	 * Sets the next node in the list at the given depth offset. The {@code nextNode}
	 * must either be null or have {@link #getNodeMaxDepthOffset} no less than
	 * {@code depthOffset}.
	 */
	protected final void setNextNodeAtDepthOffset(Object[] node, int depthOffset, @Nullable Object[] nextNode)
	{
		assert(nextNode == null || nextNode.length > depthOffset);
		node[depthOffset] = nextNode;
	}
	
	/*
	 * Iterators
	 */
	
	/**
	 * A reusable iterator for fast iteration over keys of a {@link AbstractSkipList}.
	 * <p>
	 * Iterating over the entire collection costs O(n) in the size of the collection.
	 */
	public static class KeyIterator<K> implements java.util.Iterator<K>
	{
		/*
		 * State
		 */
		
		/** The underlying set to be iterated over. This may be null. */
		protected @Nullable AbstractSkipList<K> list;
		
		/** The next node to be returned by {@link #next}. Null when there are no more nodes. */
		private @Nullable Object[] nextNode;
		
		/** The last key returned by {@link #next}. Could be null. */
		private @Nullable K lastKey;
		
		/*
		 * Construction/initialization methods
		 */
		
		/**
		 * Constructs iterator over given {@code list}, which may be null.
		 */
		public KeyIterator(@Nullable AbstractSkipList<K> list)
		{
			this.reset(list);
		}
		
		/**
		 * Resets iterator back to beginning of set.
		 */
		public void reset()
		{
			final @Nullable AbstractSkipList<K> list2 = this.list;
			this.nextNode = list2 == null ? null : list2.getNextNode(list2.head);
			this.lastKey = null;
		}
		
		/**
		 * Resets iterator to beginning of {@code newList}, which may be null.
		 */
		public void reset(@Nullable AbstractSkipList<K> newList)
		{
			this.list = newList;
			this.reset();
		}
		
		/*
		 * java.util.Iterator methods
		 */
		
		/**
		 * Returns true if {@link #next} method will return a non-null value.
		 */
		@Override
		public boolean hasNext()
		{
			return this.nextNode != null;
		}

		/**
		 * Returns the next element in the iteration or null if at the end of the list.
		 * It is not necessary to invoke {@link #hasNext} before calling this method.
		 */
		@Override
		public @Nullable K next()
		{
			final AbstractSkipList<K> list2 = list;
			K key = null;
			if (list2 != null)
			{
				Object[] n = this.nextNode;
				if (n != null)
				{
					key = list2.getNodeKey(n);
					this.nextNode = list2.getNextNode(n);
				}
				this.lastKey = key;
			}
			return key;
		}

		/**
		 * Removes the entry that was most recently returned by the {@link #next} method.
		 * Costs O(log n).
		 */
		@Override
		public void remove()
		{
			final @Nullable AbstractSkipList<K> list2 = this.list;
			if (list2 == null || this.lastKey == null)
			{
				throw new IllegalStateException();
			}
				
			list2.removeNode(this.lastKey);
			this.lastKey = null;
		}

	}

}
