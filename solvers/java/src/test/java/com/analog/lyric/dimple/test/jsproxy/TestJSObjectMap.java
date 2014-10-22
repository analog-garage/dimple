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

package com.analog.lyric.dimple.test.jsproxy;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.applet.Applet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.analog.lyric.dimple.jsproxy.IJSObject;
import com.analog.lyric.dimple.jsproxy.JSObjectMap;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestJSObjectMap extends JSTestBase
{
	@Test
	public void test()
	{
		IJSObject jsobj = createJSObject();
		if (jsobj != null)
		{
			assertInvariants(jsobj);

			jsobj.setMember("name", "Bob");
			jsobj.setMember("count", 42);
			assertInvariants(jsobj, "name", "count");
		}
	}
	
	@SuppressWarnings("null")
	private void assertInvariants(IJSObject jsobj, String ... names)
	{
		JSObjectMap map = new DummyJSObjectMap(state.applet, jsobj);
		
		assertEquals(map,map);
		assertNotEquals(map, "foo");
		assertNotEquals(map, new DummyJSObjectMap(state.applet, createJSObject()));
			
		assertFalse(map.containsKey(42));
		assertFalse(map.containsKey("no such key"));

		Set<String> memberNames = new TreeSet<>();
		for (String name : names)
		{
			memberNames.add(name);
		}
		
		for (String name : memberNames)
		{
			assertTrue(map.containsKey(name));
			assertNotNull(map.get(name));
			
			assertEquals(jsobj.getMember(name), map.get(name));
		}
		
		assertEquals(map.size(), memberNames.size());
		
		assertEquals(memberNames, map.keySet());
		
		Set<Map.Entry<String,Object>> entries = map.entrySet();
		assertEquals(memberNames.size(), entries.size());
		for (Map.Entry<String,Object> entry : entries)
		{
			String name = entry.getKey();
			assertEquals(jsobj.getMember(name), map.get(name));
		}
		
		JSObjectMap map2 = new JSObjectMap(state.applet, jsobj);
		assertEquals(map, map2);
		assertEquals(map.hashCode(), map2.hashCode());
		assertEquals(map.toString(), map2.toString());
		
		expectThrow(UnsupportedOperationException.class, map, "put", "key", "value");
	}
	
	private static class DummyJSObjectMap extends JSObjectMap
	{
		DummyJSObjectMap(Applet applet, IJSObject obj)
		{
			super(applet, obj);
		}
		
		@Override
		protected IJSObject getWindow()
		{
			return _obj;
		}
	}
}
