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

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Test;

import com.analog.lyric.collect.UniquePriorityQueue;
import com.google.common.collect.Ordering;

public class TestKeyedPriorityQueue
{
	private static class NotComparable
	{
	}
	
	@Test
	public void test()
	{
		UniquePriorityQueue<Integer> uiq = new UniquePriorityQueue<Integer>();
		assertInvariants(uiq);
		assertTrue(uiq.isEmpty());
		assertNull(uiq.comparator());
	
		assertTrue(uiq.offer(42));
		assertTrue(uiq.offer(23));
		assertFalse(uiq.offer(42));
		assertEquals(2, uiq.size());
		assertInvariants(uiq);
		
		assertTrue(uiq.add(64));
		assertFalse(uiq.add(64));
		assertEquals(3, uiq.size());
		
		assertEquals((Integer)23, uiq.peek());
		assertEquals((Integer)23, uiq.remove());
		assertEquals((Integer)42, uiq.element());
		assertEquals((Integer)42, uiq.poll());
		assertEquals(1, uiq.size());
		assertEquals((Integer)64, uiq.poll());
		assertTrue(uiq.isEmpty());
		
		for (int i = 20; --i >= 0;)
		{
			assertTrue(uiq.add(i));
		}
		assertInvariants(uiq);
		assertEquals(20, uiq.size());
		
		for (int i = 0; i < 20; i += 2)
		{
			assertTrue(uiq.remove(i));
			assertFalse(uiq.remove(i));
		}
		assertEquals(10, uiq.size());
		
		uiq.clear();
		assertTrue(uiq.isEmpty());
		
		Random rand = new Random(42);
		
		int count = 0;
		for (int i = 0; i < 100; ++i)
		{
			if (uiq.offer(rand.nextInt(Integer.MAX_VALUE)))
			{
				++count;
			}
		}
		assertEquals(count, uiq.size());
		assertInvariants(uiq);
		
		int prev = -1;
		for (Integer val = null; (val = uiq.poll()) != null; prev = val.intValue())
		{
			assertTrue(val.intValue() > prev);
		}
		assertTrue(uiq.isEmpty());
	
		Collection<Integer> ic = new TreeSet<Integer>(Collections.reverseOrder());
		for (int i = 0; i < 100; ++i)
		{
			ic.add(i);
		}
		
		uiq = new UniquePriorityQueue<Integer>(ic);
		assertEquals(ic.size(), uiq.size());
		assertTrue(ic.containsAll(ic));
		assertNotNull(uiq.comparator());
		assertInvariants(uiq);
		
		prev = Integer.MAX_VALUE;
		for (Integer val = null; (val = uiq.poll()) != null; prev = val.intValue())
		{
			assertTrue(val.intValue() < prev);
		}
		assertTrue(uiq.isEmpty());
		
		uiq.addAll(ic);
		for (int i = 0; i < 100; ++i)
		{
			if (uiq.offer(rand.nextInt(Integer.MAX_VALUE)))
			{
				++count;
			}
		}
		assertTrue(uiq.size() > ic.size());
		assertTrue(uiq.containsAll(ic));
		assertInvariants(uiq);
		count = uiq.size();
		assertTrue(uiq.removeAll(ic));
		assertFalse(uiq.removeAll(ic));
		assertEquals(count - ic.size(), uiq.size());
		for (Integer i : ic)
		{
			assertFalse(uiq.contains(i));
		}
		
		Iterator<Integer> iteri = uiq.iterator();
		while (iteri.hasNext())
		{
			Integer i = iteri.next();
			assertNotNull(i);
			iteri.remove();
			assertFalse(uiq.contains(i));
		}
		try
		{
			iteri.remove();
			fail("expected IllegalStateException");
		}
		catch (IllegalStateException ex)
		{
		}
		assertTrue(uiq.isEmpty());
		
		// Turn of normal ordering and ensure that values come out in FIFO order
		uiq = new UniquePriorityQueue<Integer>(10, Ordering.allEqual());
		int[] values = new int[100];
		for (int i = 0; i < 100; ++i)
		{
			values[i] = rand.nextInt(Integer.MAX_VALUE);
			if (uiq.offer(values[i]))
			{
				++count;
			}
		}
		assertInvariants(uiq);
		for (int value : values)
		{
			assertEquals(value, uiq.remove().intValue());
		}
		assertTrue(uiq.isEmpty());
			
		UniquePriorityQueue<Object> uoq = new UniquePriorityQueue<Object>(3);
		try
		{
			uoq.add(new NotComparable());
			fail("ClassCastException expected");
		}
		catch (ClassCastException ex)
		{
		}
		uoq.add(4.2);
		uoq.add(2.3);
		assertEquals(2.3, uoq.remove());
		assertTrue(uoq.remove(4.2));
		assertTrue(uoq.isEmpty());
	}
	
	public static <E> void assertInvariants(UniquePriorityQueue<E> queue)
	{
		QueueTester<E> tester = new QueueTester<E>();
		tester.validateQueue(queue);
	}

}
