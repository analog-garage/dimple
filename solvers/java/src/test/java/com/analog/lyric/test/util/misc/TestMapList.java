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

package com.analog.lyric.test.util.misc;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.analog.lyric.collect.tests.CollectionTester;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.util.misc.IGetId;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.MapList;

public class TestMapList
{

	private static class Value implements IGetId
	{
		private long _id;
		
		private Value(long id)
		{
			_id = id;
		}
		
		@Deprecated
		@Override
		public long getId()
		{
			return getGlobalId();
		}
		
		@Override
		public int getLocalId()
		{
			return (int)_id;
		}
		
		@Override
		public long getGlobalId()
		{
			return _id;
		}
		
		@Override
		public UUID getUUID()
		{
			return Ids.makeUUID(0, _id);
		}
	}
	
	@Test
	public void test()
	{
		MapList<Value> maplist = new MapList<Value>();
		testMapList(maplist);
	}
	
	private void testMapList(IMapList<Value> maplist)
	{
		final Value v1 = new Value(1);
		final Value v2 = new Value(2);
		final Value v3 = new Value(3);
		
		assertTrue(maplist.add(v1));
		assertTrue(maplist.add(v1));
		assertSame(v1, maplist.getByKey(1));
		assertSame(v1, maplist.getByIndex(0));
		assertSame(v1, maplist.getByIndex(1));
		assertEquals(2, maplist.size());
		assertInvariants(maplist);
		
		maplist.clear();
		assertFalse(maplist.addAll(new ArrayList<Value>()));
		ArrayList<Value> list = new ArrayList<Value>(2);
		list.add(v1);
		list.add(v2);
		list.add(v3);
		assertTrue(maplist.addAll(list));
		assertEquals(3, maplist.size());
		assertTrue(maplist.addAll(list));
		assertEquals(6, maplist.size());
		assertTrue(maplist.contains(v2));
		assertTrue(maplist.contains(v3));
		assertInvariants(maplist);
		
		maplist.clear();
		assertEquals(0, maplist.size());
		assertFalse(maplist.contains(v1));
		assertInvariants(maplist);
		
		assertFalse(maplist.remove(v1));
		assertFalse(maplist.remove("bogus"));
		
		maplist.addAll((Value[])null); // doesn't blow up
		maplist.addAll(new Value[] { v3, v2, v1});
		assertEquals(3, maplist.size());
		assertSame(v3, maplist.getByIndex(0));
		assertInvariants(maplist);
		
		assertTrue(maplist.remove(v2));
		assertFalse(maplist.remove(v2));
		assertFalse(maplist.contains(v2));
		assertSame(v1, maplist.getByIndex(1));
		assertInvariants(maplist);
		
		maplist.clear();
		maplist.addAll(list);
		assertTrue(maplist.containsAll(list));
		assertEquals(list.size(), maplist.size());
		assertInvariants(maplist);
		
		Value v4 = new Value(4);
		assertTrue(maplist.add(v4));
		assertTrue(maplist.removeAll(list));
		assertFalse(maplist.removeAll(list));
		assertEquals(1, maplist.size());
		
		assertTrue(maplist.addAll(list));
		assertSame(list.get(0), maplist.getByIndex(1));
		
		assertTrue(maplist.retainAll(list));
		assertFalse(maplist.retainAll(list));
		assertEquals(list.size(), maplist.size());
		assertInvariants(maplist);
	}

	public static <T extends IGetId> void assertInvariants(IMapList<T> maplist)
	{
		CollectionTester<T> collectionTester = new CollectionTester<T>();
		collectionTester.validateCollection(maplist);
		
		List<T> values = maplist.values();
		assertEquals(maplist.size(), values.size());
		for (int i = 0, endi = values.size(); i <endi; ++ i)
		{
			T value = values.get(i);
			long id = value.getGlobalId();
			
			assertSame(value, maplist.getByIndex(i));
			assertSame(value, maplist.getByKey(id));
			assertTrue(maplist.contains(value));
		}
	}
}
