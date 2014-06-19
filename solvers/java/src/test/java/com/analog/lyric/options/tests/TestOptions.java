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

package com.analog.lyric.options.tests;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.options.AbstractOptions;
import com.analog.lyric.options.IOption;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.IOptions;
import com.analog.lyric.options.IntegerOptionKey;
import com.analog.lyric.options.Option;
import com.analog.lyric.options.Options;
import com.analog.lyric.options.StringOptionKey;

/**
 * Tests for {@link Options} and {@link AbstractOptions}
 * @since 0.06
 * @author CBarber
 */
public class TestOptions
{
	public final IOptionKey<String> X =
		new StringOptionKey(TestOptions.class, "X", "foo");
	
	public final IOptionKey<Integer> Y =
		new IntegerOptionKey(TestOptions.class, "Y", 42);
	
	@Test
	public void test()
	{
		ExampleOptionHolder holder = new ExampleOptionHolder();
		Options options = new Options(holder);
		
		options.clearLocalOptions(); // shouldn't do anything
		assertSame(holder, options.getOptionHolder());
		assertNull(options.getOptionParent());
		assertNull(options.getLocalOptions(false));
		assertTrue(options.getRelevantOptionKeys().isEmpty());
		assertFalse(options.containsKey(X));
		assertFalse(options.containsValue("x"));
		assertNull(options.get(X));
		assertNull(options.get((Object)X));
		assertTrue(options.isEmpty());
		assertNull(options.remove(X));
		assertNull(options.getOption(X));
		assertNull(options.lookupOption(X));
		assertNull(options.lookupOrNull(X));
		assertEquals(X.defaultValue(), options.lookup(X));
		assertInvariants(options);
		
		ExampleOptionHolder holder2 = new ExampleOptionHolder(holder);
		assertSame(holder, holder2.getOptionParent());
		Options options2 = new Options(holder2);
		assertSame(holder2, options2.getOptionHolder());
		assertSame(holder, options2.getOptionParent());
		assertNull(options2.getLocalOptions(false));
		assertTrue(options2.getRelevantOptionKeys().isEmpty());
		assertNull(options.lookupOrNull(X));
		assertInvariants(options2);
		
		assertNull(options.put(X, "bar"));
		assertEquals("bar", options.lookup(X));
		assertEquals("bar", options2.lookup(X));
		assertEquals("bar", options.lookupOrNull(X));
		assertEquals("bar", options2.lookupOrNull(X));
		assertInvariants(options);
		
		assertNull(options2.put(X,  "baz"));
		assertEquals("bar", options.lookup(X));
		assertEquals("baz", options2.lookup(X));
		assertInvariants(options2);
		
		options.unset(X);
		assertEquals("foo", options.lookup(X));
		assertEquals("baz", options2.lookup(X));
		assertFalse(options.containsKey(X));
		assertInvariants(options);
		
		options.set(X, "frob");
		assertEquals("frob", options.lookup(X));
		assertInvariants(options);
		options2.clear();
		assertEquals("frob", options.lookup(X));
		assertInvariants(options2);
		assertTrue(options.containsValue("frob"));
		assertFalse(options2.containsValue("frob"));
		
		Option<String> option = new Option<>(X, "gag");
		options2.setOption(option);
		assertEquals("gag", options2.lookup(X));
		
		Map<IOptionKey<?>,Object> localOptions = options.createLocalOptions();
		assertNotNull(localOptions);
		assertSame(localOptions, options.getLocalOptions(false));
		
		Map<IOptionKey<?>, Object> map = new HashMap<IOptionKey<?>,Object>();
		map.put(X, "x");
		map.put(Y, 23);
		
		options2.putAll(map);
		assertEquals("x", options2.get(X));
		assertEquals((Object)23, options2.get(Y));
		assertInvariants(options2);
	}
	
	@Test
	public void testOption()
	{
		Option<String> option = new Option<>(X);
		assertTrue(option.equals(option));
		assertFalse(option.equals(null));
		assertEquals(X, option.key());
		assertEquals("foo", option.value());
		assertEquals("X=foo", option.toString());
		
		Option<?> option2 = new Option<>(X, "bar");
		assertNotEquals(option, option2);
		assertNotEquals(option.hashCode(), option2.hashCode());
		assertEquals("X=bar", option2.toString());
		
		option2 = new Option<>(Y);
		assertNotEquals(option, option2);
		assertNotEquals(option.hashCode(), option2.hashCode());
		
		option2 = new Option<>(X, "foo");
		assertEquals(option, option2);
		assertEquals(option.hashCode(), option2.hashCode());
	}
	
	private void assertInvariants(IOptions options)
	{
		assertSame(options, options.options());
		assertEquals(options.isEmpty(), options.size() == 0);
		
		Set<IOptionKey<?>> keys = options.keySet();
		assertEquals(options.size(), keys.size());
		for (IOptionKey<?> key : keys)
		{
			assertTrue(options.containsKey(key));
		}
		
		Collection<Object> values = options.values();
		assertEquals(options.size(), values.size());
		for (Object value : values)
		{
			assertTrue(options.containsValue(value));
		}
		
		Set<Map.Entry<IOptionKey<?>, Object>> entries = options.entrySet();
		assertEquals(options.size(), entries.size());
		for (Map.Entry<IOptionKey<?>, Object> entry : entries)
		{
			assertTrue(options.containsKey(entry.getKey()));
			assertEquals(options.get(entry.getKey()), entry.getValue());
			assertEquals(options.get((Object)entry.getKey()), entry.getValue());
			
			IOption<?> option = options.getOption(entry.getKey());
			assertNotNull(option);
			assertSame(entry.getKey(), option.key());
			assertEquals(entry.getValue(), option.value());
			assertEquals(option, options.lookupOption(entry.getKey()));
		}
	}
}
