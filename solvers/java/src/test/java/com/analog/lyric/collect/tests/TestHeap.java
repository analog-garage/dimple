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

package com.analog.lyric.collect.tests;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.collect.AbstractHeap;
import com.analog.lyric.collect.BinaryHeap;
import com.analog.lyric.collect.IHeap;
import com.analog.lyric.collect.IHeap.IEntry;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class TestHeap
{
	private static class Element
	{
		private double _expectedPriority;
		
		Element(double expectedPriority)
		{
			_expectedPriority = expectedPriority;
		}
	}
	
	@Test
	public void test()
	{
		testHeap(new BinaryHeap<Element>());
		
		IHeap<Element> crappyHeap = new CrappyHeap<Element>();
		assertFalse(crappyHeap.deferOrderingForBulkAdd(42));
		assertFalse(crappyHeap.deferOrderingForBulkChange(42));
		testHeap(crappyHeap);
	}
	
	private void testHeap(IHeap<Element> heap)
	{
		Random rand = new Random(42);
		
		assertTrue(heap.isEmpty());
		assertInvariants(heap);
		assertNull(heap.pollEntry());
		assertNull(heap.poll());
		
		final int nElements = 32;
		final Element[] elements = new Element[nElements];
		for (int i = 0; i < nElements; ++i)
		{
			elements[i] = new Element(rand.nextDouble());
		}
		
		expectThrow(IllegalArgumentException.class, heap, "offer", elements[0], Double.NaN);
		
		//
		// Trivial test with one element
		//
		
		heap.offer(elements[0], elements[0]._expectedPriority);
		assertEquals(1, heap.size());
		assertInvariants(heap);
		
		IEntry<Element> entry = heap.peekEntry();
		assertNotNull(entry);
		assertSame(elements[0], entry.getElement());
		assertSame(elements[0], heap.peek());
		assertTrue(heap.isOrdered());
		
		IEntry<Element> bogusEntry = new IEntry<Element>() {
			@Override
			public @NonNull IEntry<Element> clone()
			{
				return this;
			}
			
			@Override
			public @NonNull Element getElement()
			{
				return elements[0];
			}

			@Override
			public double getPriority()
			{
				return elements[0]._expectedPriority;
			}

			@Override
			public boolean isOwned()
			{
				return true;
			}
		};
		assertFalse(heap.containsEntry(bogusEntry));
		assertFalse(heap.removeEntry(bogusEntry));
		
		assertSame(entry, heap.pollEntry());
		assertTrue(heap.isEmpty());
		assertFalse(entry.isOwned());
		assertFalse(heap.containsEntry(entry));
		assertInvariants(heap);
		
		
		//
		// Clear
		//
		
		for (Element element : elements)
		{
			heap.offer(element, element._expectedPriority);
		}
		assertInvariants(heap);
		heap.clear();
		assertTrue(heap.isEmpty());
		assertInvariants(heap);
		
		//
		// Simple add/remove test.
		//
		
		heap.ensureCapacity(nElements);

		offerAll(heap, elements);
		assertInvariants(heap);
		assertEquals(nElements, heap.size());
		pollAll(heap);
		
		if (heap.deferOrdering())
		{
			// Try again without deferring ordering
			assertTrue(heap.deferOrdering(false));
			
			offerAll(heap, elements);
			assertInvariants(heap);
			assertEquals(nElements, heap.size());
			pollAll(heap);
		}
		
		//
		// Test deferral
		//
		
		offerRange(heap, elements, 0, nElements / 4);
		heap.deferOrdering(false);
		assertFalse(heap.deferOrdering());
		assertInvariants(heap);
		
		// Defer with most elements not yet added
		heap.deferOrdering(true);
		offerRange(heap, elements, nElements / 4, nElements);
		assertInvariants(heap);
		assertEquals(nElements, heap.size());
		pollAll(heap);
		
		offerRange(heap, elements, 0, 3 * (nElements / 4));
		heap.deferOrdering(false);
		assertFalse(heap.deferOrdering());
		assertInvariants(heap);
		
		// Defer with most elements already added
		heap.deferOrdering(true);
		offerRange(heap, elements, 3 * (nElements / 4), nElements);
		assertInvariants(heap);
		assertEquals(nElements, heap.size());
		pollAll(heap);
		
		//
		// Test removal
		//
		
		offerAll(heap, elements);
		assertTrue(heap.remove(elements[0]));
		assertFalse(heap.remove(elements[0]));
		assertFalse(heap.contains(elements[0]));
		assertEquals(nElements - 1, heap.size());
		assertInvariants(heap);
		pollAll(heap);
		
		offerAll(heap, elements);
		entry = heap.entryForElement(elements[0]);
		assertNotNull(entry);
		assertTrue(entry.isOwned());
		assertTrue(heap.removeEntry(entry));
		assertFalse(entry.isOwned());
		assertFalse(heap.removeEntry(entry));
		assertEquals(nElements - 1, heap.size());
		assertFalse(heap.containsEntry(entry));
		assertFalse(heap.contains(entry.getElement()));
		assertInvariants(heap);
		
		for (int i = 1; i < nElements; ++i)
		{
			Element element = elements[i];
			assertTrue(heap.remove(element));
			assertFalse(heap.contains(element));
		}
		assertTrue(heap.isEmpty());
		
		//
		// Test priority change
		//
		
		offerAll(heap, elements);
		assertInvariants(heap);
		
		heap.deferOrdering(false);
		for (Element element : elements)
		{
			entry = heap.entryForElement(element);
			element._expectedPriority = rand.nextDouble();
			heap.changePriority(Objects.requireNonNull(entry), element._expectedPriority);
			assertTrue(heap.isOrdered());
		}
		assertInvariants(heap);
		pollAll(heap);
		
		if (heap.deferOrdering(true))
		{
			offerAll(heap, elements);
			for (Element element : elements)
			{
				entry = heap.entryForElement(element);
				element._expectedPriority = rand.nextDouble();
				heap.changePriority(Objects.requireNonNull(entry), element._expectedPriority);
				assertFalse(heap.isOrdered());
			}
			assertInvariants(heap);
			pollAll(heap);
		}
		
		//
		// All same priority
		//
		
		for (Element element : elements)
		{
			heap.offer(element, 42);
		}
		assertInvariants(heap, false);
		pollAll(heap);
		
		heap.clear();

		//
		// Test cloning
		//
		
		offerAll(heap, elements);
		
		IHeap<Element> heap2 = heap.clone();
		assertInvariants(heap2);
		assertEquals(heap.size(), heap2.size());
		
		Iterator<? extends IEntry<Element>> entries1 = heap.entryIterator();
		Iterator<? extends IEntry<Element>> entries2 = heap2.entryIterator();
		
		for (int i = 0; i < nElements; ++i)
		{
			assertTrue(entries1.hasNext());
			assertTrue(entries2.hasNext());
			IEntry<Element> entry1 = entries1.next();
			IEntry<Element> entry2 = entries2.next();
			
			assertSame(entry1.getElement(), entry2.getElement());
			assertEquals(entry1.getPriority(), entry2.getPriority(), 0.0);
			assertNotSame(entry1, entry2);
			
			assertFalse(heap.containsEntry(entry2));
			assertFalse(heap2.containsEntry(entry1));
			
			assertFalse(heap.changePriority(entry2, 42));
			assertFalse(heap.removeEntry(entry2));
		}
		assertFalse(entries1.hasNext());
		assertFalse(entries2.hasNext());
		
		heap2.clear();
		assertFalse(heap2.containsEntry(Objects.requireNonNull(heap.peekEntry())));
		
		//
		// Test merge
		//
		
		heap.clear();
		heap2.clear();
		
		// Randomly distribute elements between two heaps.
		for (Element element : elements)
		{
			if (rand.nextBoolean())
			{
				heap.offer(element, element._expectedPriority);
			}
			else
			{
				heap2.offer(element, element._expectedPriority);
			}
		}
		
		assertInvariants(heap);
		assertInvariants(heap2);
		
		int size1 = heap.size();
		int size2 = heap2.size();
		assertEquals(nElements, size1 + size2);
		
		heap.merge(heap2);
		assertTrue(heap2.isEmpty());
		assertInvariants(heap);
		assertInvariants(heap2);
		pollAll(heap);
	}
	
	private void offerAll(IHeap<Element> heap, Element ... elements)
	{
		for (Element element : elements)
		{
			heap.offer(element, element._expectedPriority);
			if (heap.deferOrdering())
			{
				assertFalse(heap.isOrdered());
			}
			else
			{
				assertTrue(heap.isOrdered());
			}
		}
	}
	
	private void offerRange(IHeap<Element> heap, Element[] elements, int start, int end)
	{
		for (int i = start; i < end; ++i)
		{
			Element element = elements[i];
			heap.offer(element,  element._expectedPriority);
		}
	}
	
	private void pollAll(IHeap<Element> heap)
	{
		IEntry<Element> prevEntry = null;
		
		boolean usePollEntry = true;
		
		while (!heap.isEmpty())
		{
			IEntry<Element> nextEntry = heap.peekEntry();
			assertNotNull(nextEntry);
			assertSame(nextEntry.getElement(), heap.peek());
			assertTrue(heap.isOrdered());
		
			if (usePollEntry)
			{
				assertSame(nextEntry, heap.pollEntry());
				if (prevEntry != null)
				{
					assertTrue(prevEntry.getPriority() <= nextEntry.getPriority());
				}
			}
			else
			{
				assertSame(nextEntry.getElement(), heap.poll());
				assertNotSame(nextEntry, heap.peekEntry());
			}
			
			usePollEntry = !usePollEntry;
			prevEntry = nextEntry;
		}
	}
	
	private void assertInvariants(IHeap<Element> heap)
	{
		assertInvariants(heap, true);
	}
	
	private void assertInvariants(IHeap<Element> heap, boolean checkPriority)
	{
		final int size = heap.size();
		assertTrue(size >= 0);
		assertEquals(size == 0, heap.isEmpty());
		
		if (!heap.deferOrdering())
		{
			assertTrue(heap.isOrdered());
		}
		
		if (heap.isEmpty())
		{
			assertNull(heap.peek());
			assertNull(heap.peekEntry());
			assertTrue(heap.isOrdered());
		}
		else if (!heap.deferOrdering())
		{
			// Don't peek if ordering is deferred to avoid changing state.
			IEntry<Element> entry = heap.peekEntry();
			requireNonNull(entry);
			assertSame(heap.peek(), entry.getElement());
		}
		
		Iterator<? extends IEntry<Element>> entries = heap.entryIterator();
		Iterator<Element> elements = heap.iterator();
		int count = 0;
		
		expectThrow(UnsupportedOperationException.class, elements, "remove");
		
		while (entries.hasNext())
		{
			++count;
			assertTrue(elements.hasNext());
			IEntry<Element> entry = entries.next();
			Element element = elements.next();
			assertSame(element, entry.getElement());
			if (checkPriority)
			{
				assertEquals(element._expectedPriority, entry.getPriority(), 0.0);
			}
			assertTrue(entry.isOwned());
			
			assertTrue(heap.containsEntry(entry));
			assertTrue(heap.contains(element));
			
			IEntry<Element> entry2 = heap.entryForElement(element);
			requireNonNull(entry2);
			assertSame(element, entry2.getElement());
		}
		assertFalse(elements.hasNext());
		assertEquals(size, count);
	}
	
	/**
	 * Crappy IHeap implementation for testing default methods in AbstractHeap
	 */
	public static class CrappyHeap<E> extends AbstractHeap<E>
	{
		private static class Entry<E> extends AbstractHeap.AbstractEntry<E>
		{
			private boolean _owned = true;
			
			protected Entry(E element, double priority)
			{
				super(element, priority);
			}

			@Override
			public boolean isOwned()
			{
				return _owned;
			}

			@Override
			public Entry<E> clone()
			{
				return new Entry<E>(getElement(), _priority);
			}
			
			private void setPriority(double priority)
			{
				_priority = priority;
			}
		}
		
		private List<Entry<E>> _entries = new ArrayList<Entry<E>>();
		
		@Override
		public boolean changePriority(IEntry<E> entry, double priority)
		{
			if (_entries.contains(entry))
			{
				((Entry<?>)entry).setPriority(priority);
				return true;
			}
			return false;
		}

		@Override
		public Iterator<? extends com.analog.lyric.collect.IHeap.IEntry<E>> entryIterator()
		{
			return _entries.iterator();
		}

		@Override
		public Entry<E> offer(E element, double priority)
			throws IllegalArgumentException
		{
			if (Double.isNaN(priority))
			{
				throw new IllegalArgumentException();
			}
			Entry<E> entry = new Entry<E>(element, priority);
			_entries.add(entry);
			return entry;
		}

		/*
		 * 
		 */
		@Override
		public @Nullable Entry<E> peekEntry()
		{
			Entry<E> first = null;
			if (!_entries.isEmpty())
			{
				first = _entries.get(0);
				for (Entry<E> entry : _entries)
				{
					if (entry.getPriority() < first.getPriority())
					{
						first = entry;
					}
				}
			}
			return first;
		}

		/*
		 * 
		 */
		@Override
		public @Nullable Entry<E> pollEntry()
		{
			Entry<E> entry = peekEntry();
			if (entry != null)
			{
				removeEntry(entry);
			}
			return entry;
		}

		@Override
		public boolean removeEntry(com.analog.lyric.collect.IHeap.IEntry<E> entry)
		{
			boolean removed = _entries.remove(entry);
			if (removed)
			{
				((Entry<?>)entry)._owned = false;
			}
			return removed;
		}

		@Override
		public void clear()
		{
			_entries.clear();
		}
		
		@Override
		public CrappyHeap<E> clone()
		{
			CrappyHeap<E> clone = new CrappyHeap<E>();
			for (Entry<E> entry : _entries)
			{
				clone.offer(entry.getElement(), entry.getPriority());
			}
			return clone;
		}

		@Override
		public int size()
		{
			return _entries.size();
		}
	}
}
