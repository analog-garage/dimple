package com.analog.lyric.collect.tests;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.collect.DynamicHeap;
import com.analog.lyric.collect.IDynamicPriorityQueue;
import com.analog.lyric.collect.IDynamicPriorityQueue.IEntry;

public class TestDynamicPriorityQueue
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
		testQueue(new DynamicHeap<Element>());
	}
	
	private void testQueue(IDynamicPriorityQueue<Element> queue)
	{
		Random rand = new Random(42);
		
		assertTrue(queue.isEmpty());
		assertInvariants(queue);
		assertNull(queue.pollEntry());
		assertNull(queue.poll());
		
		final int nElements = 32;
		final Element[] elements = new Element[nElements];
		for (int i = 0; i < nElements; ++i)
		{
			elements[i] = new Element(rand.nextDouble());
		}
		
		expectThrow(IllegalArgumentException.class, queue, "offer", elements[0], Double.NaN);
		
		//
		// Trivial test with one element
		//
		
		queue.offer(elements[0], elements[0]._expectedPriority);
		assertEquals(1, queue.size());
		assertInvariants(queue);
		
		IEntry<Element> entry = queue.peekEntry();
		assertNotNull(entry);
		assertSame(elements[0], entry.getElement());
		assertSame(elements[0], queue.peek());
		assertTrue(queue.isOrdered());
		
		IEntry<Element> bogusEntry = new IEntry<Element>() {
			@Override
			public IEntry<Element> clone()
			{
				return null;
			}
			
			@Override
			public Element getElement()
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
		assertFalse(queue.containsEntry(bogusEntry));
		assertFalse(queue.removeEntry(bogusEntry));
		
		assertSame(entry, queue.pollEntry());
		assertTrue(queue.isEmpty());
		assertFalse(entry.isOwned());
		assertFalse(queue.containsEntry(entry));
		assertInvariants(queue);
		
		//
		// Clear
		//
		
		for (Element element : elements)
		{
			queue.offer(element, element._expectedPriority);
		}
		assertInvariants(queue);
		queue.clear();
		assertTrue(queue.isEmpty());
		assertInvariants(queue);
		
		//
		// Simple add/remove test.
		//
		
		queue.ensureCapacity(nElements);

		offerAll(queue, elements);
		assertInvariants(queue);
		assertEquals(nElements, queue.size());
		pollAll(queue);
		
		if (queue.deferOrdering())
		{
			// Try again without deferring ordering
			assertTrue(queue.deferOrdering(false));
			
			offerAll(queue, elements);
			assertInvariants(queue);
			assertEquals(nElements, queue.size());
			pollAll(queue);
		}
		
		//
		// Test deferral
		//
		
		offerRange(queue, elements, 0, nElements / 4);
		queue.deferOrdering(false);
		assertFalse(queue.deferOrdering());
		assertInvariants(queue);
		
		// Defer with most elements not yet added
		queue.deferOrdering(true);
		offerRange(queue, elements, nElements / 4, nElements);
		assertInvariants(queue);
		assertEquals(nElements, queue.size());
		pollAll(queue);
		
		offerRange(queue, elements, 0, 3 * (nElements / 4));
		queue.deferOrdering(false);
		assertFalse(queue.deferOrdering());
		assertInvariants(queue);
		
		// Defer with most elements already added
		queue.deferOrdering(true);
		offerRange(queue, elements, 3 * (nElements / 4), nElements);
		assertInvariants(queue);
		assertEquals(nElements, queue.size());
		pollAll(queue);
		
		//
		// Test removal
		//
		
		offerAll(queue, elements);
		assertTrue(queue.remove(elements[0]));
		assertFalse(queue.remove(elements[0]));
		assertFalse(queue.contains(elements[0]));
		assertEquals(nElements - 1, queue.size());
		assertInvariants(queue);
		pollAll(queue);
		
		offerAll(queue, elements);
		entry = queue.entryForElement(elements[0]);
		assertNotNull(entry);
		assertTrue(entry.isOwned());
		assertTrue(queue.removeEntry(entry));
		assertFalse(entry.isOwned());
		assertFalse(queue.removeEntry(entry));
		assertEquals(nElements - 1, queue.size());
		assertFalse(queue.containsEntry(entry));
		assertFalse(queue.contains(entry.getElement()));
		assertInvariants(queue);
		
		for (int i = 1; i < nElements; ++i)
		{
			Element element = elements[i];
			assertTrue(queue.remove(element));
			assertFalse(queue.contains(element));
		}
		assertTrue(queue.isEmpty());
		
		//
		// Test priority change
		//
		
		offerAll(queue, elements);
		assertInvariants(queue);
		
		queue.deferOrdering(false);
		for (Element element : elements)
		{
			entry = queue.entryForElement(element);
			element._expectedPriority = rand.nextDouble();
			queue.changePriority(entry, element._expectedPriority);
			assertTrue(queue.isOrdered());
		}
		assertInvariants(queue);
		pollAll(queue);
		
		if (queue.deferOrdering(true))
		{
			offerAll(queue, elements);
			for (Element element : elements)
			{
				entry = queue.entryForElement(element);
				element._expectedPriority = rand.nextDouble();
				queue.changePriority(entry, element._expectedPriority);
				assertFalse(queue.isOrdered());
			}
			assertInvariants(queue);
			pollAll(queue);
		}
		
		//
		// All same priority
		//
		
		for (Element element : elements)
		{
			queue.offer(element, 42);
		}
		assertInvariants(queue, false);
		pollAll(queue);
		
		queue.clear();

		//
		// Test cloning
		//
		
		offerAll(queue, elements);
		
		IDynamicPriorityQueue<Element> queue2 = queue.clone();
		assertInvariants(queue2);
		assertEquals(queue.size(), queue2.size());
		
		Iterator<? extends IEntry<Element>> entries1 = queue.entryIterator();
		Iterator<? extends IEntry<Element>> entries2 = queue2.entryIterator();
		
		for (int i = 0; i < nElements; ++i)
		{
			assertTrue(entries1.hasNext());
			assertTrue(entries2.hasNext());
			IEntry<Element> entry1 = entries1.next();
			IEntry<Element> entry2 = entries2.next();
			
			assertSame(entry1.getElement(), entry2.getElement());
			assertEquals(entry1.getPriority(), entry2.getPriority(), 0.0);
			assertNotSame(entry1, entry2);
			
			assertFalse(queue.containsEntry(entry2));
			assertFalse(queue2.containsEntry(entry1));
			
			assertFalse(queue.changePriority(entry2, 42));
			assertFalse(queue.removeEntry(entry2));
		}
		assertFalse(entries1.hasNext());
		assertFalse(entries2.hasNext());
		
		queue2.clear();
		assertFalse(queue2.containsEntry(queue.peekEntry()));
	}
	
	private void offerAll(IDynamicPriorityQueue<Element> queue, Element ... elements)
	{
		for (Element element : elements)
		{
			queue.offer(element, element._expectedPriority);
			if (queue.deferOrdering())
			{
				assertFalse(queue.isOrdered());
			}
			else
			{
				assertTrue(queue.isOrdered());
			}
		}
	}
	
	private void offerRange(IDynamicPriorityQueue<Element> queue, Element[] elements, int start, int end)
	{
		for (int i = start; i < end; ++i)
		{
			Element element = elements[i];
			queue.offer(element,  element._expectedPriority);
		}
	}
	
	private void pollAll(IDynamicPriorityQueue<Element> queue)
	{
		IEntry<Element> prevEntry = null;
		
		while (!queue.isEmpty())
		{
			IEntry<Element> nextEntry = queue.peekEntry();
			assertNotNull(nextEntry);
			assertSame(nextEntry.getElement(), queue.peek());
			assertTrue(queue.isOrdered());
		
			assertSame(nextEntry, queue.pollEntry());
			if (prevEntry != null)
			{
				assertTrue(prevEntry.getPriority() <= nextEntry.getPriority());
			}
			prevEntry = nextEntry;
		}
	}
	
	private void assertInvariants(IDynamicPriorityQueue<Element> queue)
	{
		assertInvariants(queue, true);
	}
	
	private void assertInvariants(IDynamicPriorityQueue<Element> queue, boolean checkPriority)
	{
		final int size = queue.size();
		assertTrue(size >= 0);
		assertEquals(size == 0, queue.isEmpty());
		
		if (!queue.deferOrdering())
		{
			assertTrue(queue.isOrdered());
		}
		
		if (queue.isEmpty())
		{
			assertNull(queue.peek());
			assertNull(queue.peekEntry());
			assertTrue(queue.isOrdered());
		}
		else if (!queue.deferOrdering())
		{
			// Don't peek if ordering is deferred to avoid changing state.
			IEntry<Element> entry = queue.peekEntry();
			assertSame(queue.peek(), entry.getElement());
		}
		
		Iterator<? extends IEntry<Element>> entries = queue.entryIterator();
		Iterator<Element> elements = queue.iterator();
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
			
			assertTrue(queue.containsEntry(entry));
			assertTrue(queue.contains(element));
			
			IEntry<Element> entry2 = queue.entryForElement(element);
			assertSame(element, entry2.getElement());
		}
		assertFalse(elements.hasNext());
		assertEquals(size, count);
	}
}
