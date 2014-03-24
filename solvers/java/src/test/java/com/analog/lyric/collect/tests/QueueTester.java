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
