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

package com.analog.lyric.collect.tests;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.collect.DoubleArrayCache;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestSoftIntObjectCache
{
	@Test
	public void test()
	{
		DoubleArrayCache doubles = new DoubleArrayCache();
		
		assertNull(doubles.remove(3));
		double[] d3 = doubles.allocate(3);
		assertNull(doubles.remove(3));
		
		assertEquals(3, d3.length);
		assertNotSame(d3, doubles.allocate(3));
		
		assertTrue(doubles.release(d3));
		assertSame(d3, doubles.remove(3));
		
		assertTrue(doubles.put(3, d3));
		assertFalse(doubles.release(d3));
		
		assertSame(d3, doubles.allocate(3));
		doubles.release(d3);
		
		doubles.clear();
		assertNotSame(d3, doubles.remove(3));
		
		expectThrow(IllegalArgumentException.class, "Length 4 does not match.*", doubles, "put", 4, d3);
	}
}
