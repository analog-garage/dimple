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
import static org.junit.Assert.*;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.analog.lyric.collect.Tuple;
import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.util.test.SerializationTester;

/**
 * Test for {@code Tuple*} classes.
 */
public class TestTuples
{

	@Test
	public void test()
	{
		Tuple2<String,Integer> t2 = Tuple2.create("foo", 42);
		assertInvariants(t2);
		assertEquals("foo", t2.first);
		assertEquals(42, t2.second.intValue());
		assertEquals("foo", t2.getKey());
		assertEquals(42, t2.getValue().intValue());
		expectThrow(UnsupportedOperationException.class, t2, "setValue", "");
		
		Tuple2<String,Integer> e2 =
			new Tuple2<String, Integer>(new AbstractMap.SimpleEntry<String, Integer>("foo", 42));
		assertInvariants(e2);
		assertEquals(t2, e2);
	}
	
	private void assertInvariants(Tuple tuple)
	{
		assertTrue(tuple.size() >= 0);
		
		Iterator<Object> iter = tuple.iterator();
		for (int i = 0; i < tuple.size(); ++i)
		{
			assertTrue(iter.hasNext());
			assertEquals(iter.next(), tuple.get(i));
			
			expectThrow(UnsupportedOperationException.class, tuple, "set", i, null);
		}
		assertFalse(iter.hasNext());
		expectThrow(IndexOutOfBoundsException.class, tuple, "get", -1);
		expectThrow(IndexOutOfBoundsException.class, tuple, "get", tuple.size());
		
		List<Object> tupleCopy = SerializationTester.clone(tuple);
		assertEquals(tupleCopy, tuple);
		assertEquals(tupleCopy.hashCode(), tuple.hashCode());
		assertEquals(tuple.size(), tupleCopy.size());
		for (int i = 0; i < tuple.size(); ++i)
		{
			assertEquals(tuple.get(i), tupleCopy.get(i));
		}
	}
}
