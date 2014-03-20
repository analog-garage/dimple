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

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.analog.lyric.collect.AtomicStampedDouble;
import com.analog.lyric.util.test.SerializationTester;

public class TestAtomicStampedDouble
{

	@Test
	public void test()
	{
		int[] stampHolder = new int[1];
		
		//
		// Single threaded tests
		//
		
		// Construction
		AtomicStampedDouble d1 = new AtomicStampedDouble();
		assertEquals(0.0, d1.get(), 0.0);
		assertEquals(0, d1.getStamp());
		stampHolder[0] = -1;
		assertEquals(0.0, d1.get(stampHolder), 0.0);
		assertEquals(0, stampHolder[0]);
		
		d1 = new AtomicStampedDouble(3.145);
		assertEquals(3.145, d1.get(), 0.0);
		assertEquals(0, d1.getStamp());
		stampHolder[0] = -1;
		assertEquals(3.145, d1.get(stampHolder), 0.0);
		assertEquals(0, stampHolder[0]);
	
		d1 = new AtomicStampedDouble(42, 23);
		assertEquals(42, d1.get(), 0.0);
		assertEquals(23, d1.getStamp());
		stampHolder[0] = -1;
		assertEquals(42, d1.get(stampHolder), 0.0);
		assertEquals(23, stampHolder[0]);
		
		// Copying
		AtomicStampedDouble d2 = new AtomicStampedDouble(d1);
		assertEquals(d1.get(), d2.get(), 0.0);
		assertEquals(d1.getStamp(), d2.getStamp());
		
		// Serialization
		d2 = SerializationTester.clone(d1);
		assertEquals(d1.get(), d2.get(), 0.0);
		assertEquals(d1.getStamp(), d2.getStamp());
		
		// set
		d1.set(1.2, 2);
		assertEquals(1.2, d1.get(), 0.0);
		assertEquals(2, d1.getStamp());
		d1.set(1.2, 2);
		assertEquals(1.2, d1.get(), 0.0);
		assertEquals(2, d1.getStamp());
		d1.set(1.2, 3);
		assertEquals(1.2, d1.get(), 0.0);
		assertEquals(3, d1.getStamp());
		d1.set(1.3, 3);
		assertEquals(1.3, d1.get(), 0.0);
		assertEquals(3, d1.getStamp());
		
		// setAndIncrementStamp
		d1.setAndIncrementStamp(123);
		assertEquals(123, d1.get(), 0.0);
		assertEquals(4, d1.getStamp());
		
		// attemptStamp
		assertFalse(d1.attemptStamp(42, 9));
		assertEquals(123, d1.get(), 0.0);
		assertEquals(4, d1.getStamp());
		assertTrue(d1.attemptStamp(123, 9));
		assertEquals(123, d1.get(), 0.0);
		assertEquals(9, d1.getStamp());
		
		// compareAndSet
		assertFalse(d1.compareAndSet(1.23, 1.45, 9, 11));
		assertEquals(123, d1.get(), 0.0);
		assertEquals(9, d1.getStamp());
		assertFalse(d1.compareAndSet(123, 1.45, 3, 11));
		assertEquals(123, d1.get(), 0.0);
		assertEquals(9, d1.getStamp());
		assertTrue(d1.compareAndSet(123, 1.45, 9, 11));
		assertEquals(1.45, d1.get(), 0.0);
		assertEquals(11, d1.getStamp());
		
		//
		// Multithreaded tests
		//
		
		final int nThreads = Runtime.getRuntime().availableProcessors();
		int iterations = 10000;
		CountDownLatch startRandomSet = new CountDownLatch(1);
		RandomSetTester[] setTesters = new RandomSetTester[nThreads];
		for (int i = 0; i < nThreads; ++i)
		{
			setTesters[i] = new RandomSetTester(d1, i * iterations, iterations, startRandomSet);
			setTesters[i].start();
		}
		
		startRandomSet.countDown(); // Wave the start flag for the test threads
		
		for (int i = 0; i < nThreads; ++i)
		{
			try
			{
				setTesters[i].join();
			}
			catch (InterruptedException ex)
			{
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}

	private static class RandomSetTester extends Thread
	{
		private final AtomicStampedDouble _d;
		private final int _iterations;
		private final CountDownLatch _startLatch;
		private final int _startValue;
		
		RandomSetTester(AtomicStampedDouble d, int value, int iterations, CountDownLatch startLatch)
		{
			_d = d;
			_iterations = iterations;
			_startLatch = startLatch;
			_startValue = value;
		}
		
		@Override
		public void run()
		{
			try
			{
				_startLatch.await();
			}
			catch (InterruptedException ex)
			{
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			
			int[] stampHolder = new int[1];
	
			for (int i = _iterations, val = _startValue; --i>=0; ++val)
			{
				_d.set(val, val);
				double val2 = _d.get(stampHolder);
				assertEquals((int)val2, stampHolder[0]);
				
				_d.compareAndSet(val, val, val-1, val-1);
				
				val2 = _d.get(stampHolder);
				assertEquals((int)val2, stampHolder[0]);
			}
		}
	}
}
