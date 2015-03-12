/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for {@link DoubleArrayCache} et al.
 * @since 0.08
 * @author Christopher Barber
 */
public class TestArrayCache
{
	@Test
	public void testDoubleArrayCache()
	{
		DoubleArrayCache cache = new DoubleArrayCache(2);
		assertEquals(2, cache.maxInstancesPerSize());
		
		assertSame(ArrayUtil.EMPTY_DOUBLE_ARRAY, cache.allocateAtLeast(0));
		assertSame(ArrayUtil.EMPTY_DOUBLE_ARRAY, cache.allocateAtLeast(-1));
		
		double[] array1 = cache.allocateAtLeast(3);
		assertTrue(array1.length >= 3);
		
		double[] array2 = cache.allocateAtLeast(3);
		assertTrue(array1.length >= 3);
		assertNotSame(array1, array2);
		
		double[] array3 = cache.allocateAtLeast(3);
		assertEquals(array3.length, 3);
		assertNotSame(array1, array3);
		assertNotSame(array2, array3);
		
		cache.release(array1);
		
		double[] array4 = cache.allocateAtLeast(3);
		assertSame(array1, array4);
		
		cache.release(array4);
		cache.release(array2);
		cache.release(array3);
		cache.release(array2.clone());
		
		assertEquals(DoubleArrayCache.DEFAULT_MAX_INSTANCES, new DoubleArrayCache().maxInstancesPerSize());
	}

	@Test
	public void testIntArrayCache()
	{
		IntArrayCache cache = new IntArrayCache(2);
		assertEquals(2, cache.maxInstancesPerSize());
		
		assertSame(ArrayUtil.EMPTY_INT_ARRAY, cache.allocateAtLeast(0));
		assertSame(ArrayUtil.EMPTY_INT_ARRAY, cache.allocateAtLeast(-1));
		
		int[] array1 = cache.allocateAtLeast(3);
		assertTrue(array1.length >= 3);
		
		int[] array2 = cache.allocateAtLeast(3);
		assertTrue(array1.length >= 3);
		assertNotSame(array1, array2);
		
		int[] array3 = cache.allocateAtLeast(3);
		assertEquals(array3.length, 3);
		assertNotSame(array1, array3);
		assertNotSame(array2, array3);
		
		cache.release(array1);
		
		int[] array4 = cache.allocateAtLeast(3);
		assertSame(array1, array4);
		
		cache.release(array4);
		cache.release(array2);
		cache.release(array3);
		cache.release(array2.clone());
		
		assertEquals(IntArrayCache.DEFAULT_MAX_INSTANCES, new IntArrayCache().maxInstancesPerSize());
	}
}
