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

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.analog.lyric.collect.NonNullListIndices;
import com.analog.lyric.collect.PrimitiveIterator;
import com.google.common.collect.Iterators;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestNonNullListIndices
{
	@Test
	public void test()
	{
		List<String> list = Arrays.asList("foo", null, "bar", null, null, "baz");
		NonNullListIndices indices = new NonNullListIndices(list);
		
		assertSame(list, indices.getList());

		assertArrayEquals(new Object[] { 0, 2, 5 }, Iterators.toArray(indices.iterator(), Integer.class));
		
		PrimitiveIterator.OfInt iter = indices.iterator();
		assertTrue(iter.hasNext());
		assertEquals(0, iter.nextInt());
		assertEquals(2, iter.nextInt());
		assertEquals(5, iter.nextInt());
		assertFalse(iter.hasNext());
		
		expectThrow(NoSuchElementException.class, iter, "nextInt");
		expectThrow(UnsupportedOperationException.class, iter, "remove");
	}
}
