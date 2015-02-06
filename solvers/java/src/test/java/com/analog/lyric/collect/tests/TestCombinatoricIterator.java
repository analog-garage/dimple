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

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.collect.CombinatoricIterator;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestCombinatoricIterator
{
	@Test
	public void test()
	{
		String[] first = new String[] { "a", "b" };
		String[] second = new String[] { "x", "y", "z" };
		CombinatoricIterator<String> iter = new CombinatoricIterator<>(String.class, first, second);
		assertTrue(iter.hasNext());
		String[] values = iter.next();
		assertArrayEquals(new String[] { "a", "x" }, values);
		String[] values2 = iter.next();
		assertArrayEquals(new String[] { "b", "x" }, values);
		assertSame(values, values2);
		assertArrayEquals(new String[] { "a", "y" }, iter.next());
		assertArrayEquals(new String[] { "b", "y" }, iter.next());
		assertArrayEquals(new String[] { "a", "z" }, iter.next());
		assertArrayEquals(new String[] { "b", "z" }, iter.next());
		assertFalse(iter.hasNext());
		assertArrayEquals(new String[] { "b", "z" }, iter.next());
		
		iter.reset();
		assertTrue(iter.hasNext());
		assertArrayEquals(new String[] { "a", "x" }, iter.next());
		
		// Test dimension with only one element
		iter = new CombinatoricIterator<>(String.class, new String[] { "one" }, second);
		assertTrue(iter.hasNext());
		assertArrayEquals(new String[] { "one", "x" }, iter.next());
		assertArrayEquals(new String[] { "one", "y" }, iter.next());
		assertArrayEquals(new String[] { "one", "z" }, iter.next());
		assertFalse(iter.hasNext());
		
		iter = new CombinatoricIterator<>(String.class, first, new String[] { "one" });
		assertTrue(iter.hasNext());
		assertArrayEquals(new String[] { "a", "one" }, iter.next());
		assertArrayEquals(new String[] { "b", "one" }, iter.next());
		assertFalse(iter.hasNext());
	}
}
