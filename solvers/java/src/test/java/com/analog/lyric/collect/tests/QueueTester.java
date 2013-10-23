package com.analog.lyric.collect.tests;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;
import java.util.Queue;

public class QueueTester<T> extends CollectionTester<T>
{
	public void validateQueue(Queue<T> queue)
	{
		validateCollection(queue);
		
		if (queue.isEmpty())
		{
			assertNull(queue.peek());
			assertNull(queue.poll());
			
			try
			{
				queue.element();
				fail("should not get here");
			}
			catch (NoSuchElementException ex)
			{
				// expected
			}
			try
			{
				queue.remove();
				fail("should not get here");
			}
			catch (NoSuchElementException ex)
			{
				// expected
			}
		}
		else
		{
			assertEquals(queue.element(), queue.peek());
		}
	}
}
