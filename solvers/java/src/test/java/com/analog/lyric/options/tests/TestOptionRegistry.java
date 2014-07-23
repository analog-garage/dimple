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

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.regex.Pattern;

import org.junit.Test;

import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.IntegerOptionKey;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.options.OptionKeys;
import com.analog.lyric.options.OptionRegistry;
import com.analog.lyric.options.StringOptionKey;
import com.analog.lyric.util.misc.Nullable;

/**
 * Test for {@link OptionRegistry} class
 * 
 * @since 0.06
 * @author CBarber
 */
public class TestOptionRegistry
{
	@Test
	public void testRegistry()
	{
		OptionRegistry registry = new OptionRegistry();
		assertEquals(0, registry.size());
		assertInvariants(registry);
		
		registry.addFromClass(getClass());
		assertInvariants(registry);
		assertEquals(5, registry.size());
		
		assertEquals(2, registry.getAllMatching(".*\\.S\\d").size());
		assertEquals(1, registry.getAllMatching(Pattern.compile(".*\\.S2")).size());
		assertEquals(1,	registry.getAllStartingWith(EnumOptions.class.getName()).size());
		
		registry.clear();
		assertInvariants(registry);
		assertEquals(0, registry.size());
		
		registry.addFromQualifiedName(FieldOptions.I23.qualifiedName());
		registry.add(EnumOptions.E1);
		assertEquals(2, registry.size());
		assertSame(FieldOptions.I23, registry.get(FieldOptions.I23.qualifiedName()));
		assertSame(EnumOptions.E1, registry.get(OptionKey.qualifiedName(EnumOptions.E1)));
		assertInvariants(registry);
		
		registry.remove(FieldOptions.I23);
		assertEquals(1, registry.size());
		assertNull(registry.get(FieldOptions.I23.qualifiedName()));
		assertSame(EnumOptions.E1, registry.get(OptionKey.qualifiedName(EnumOptions.E1)));
		assertInvariants(registry);
	}
	
	@Test
	public void testOptionKeys()
	{
		testOptionKeys(Object.class);
		testOptionKeys(getClass());
		testOptionKeys(EnumOptions.class, EnumOptions.E1);
		testOptionKeys(FieldOptions.class, FieldOptions.I23, FieldOptions.I42, FieldOptions.S1, FieldOptions.S2);
	}
	
	private void testOptionKeys(Class<?> declaringClass, IOptionKey<?> ... expected)
	{
		OptionKeys keys = OptionKeys.declaredInClass(declaringClass);
		assertSame(keys, OptionKeys.declaredInClass(declaringClass));
		
		assertSame(declaringClass, keys.declaringClass());
		
		final int size = keys.size();
		
		assertEquals(expected.length, size);

		for (int i = 0; i < size; ++i)
		{
			IOptionKey<?> key = keys.get(i);
			assertSame(key, keys.get(key.name()));
		}
		
		assertNull(keys.get("does not exist"));
		expectThrow(IndexOutOfBoundsException.class, keys, "get", -1);
		expectThrow(IndexOutOfBoundsException.class, keys, "get", size);
	}
	
	private void assertInvariants(OptionRegistry registry)
	{
		SortedMap<String, IOptionKey<?>> sortedMap = registry.asSortedMap();
		for (Entry<String, IOptionKey<?>> entry : sortedMap.entrySet())
		{
			String name = entry.getKey();
			IOptionKey<?> optionKey = entry.getValue();
			
			assertSame(optionKey, registry.get(name));
			assertEquals(name, OptionKey.qualifiedName(optionKey));
		}
		
		assertNull(registry.get("this-key-does-not-exist"));
		assertNull(registry.remove("this-key-does-not-exist"));
		
		SortedMap<String,IOptionKey<?>> map2 = registry.getAllMatching(".*");
		assertEquals(sortedMap, map2);
		
		assertTrue(registry.getAllMatching("does-not-exist").isEmpty());
	}

	@SuppressWarnings("null")
	public static enum EnumOptions implements IOptionKey<Object>
	{
		E1("e1");
	
		private final Object _defaultValue;
		
		private EnumOptions(Object defaultValue)
		{
			_defaultValue = defaultValue;
		}
		
		@Override
		public Class<Object> type()
		{
			return Object.class;
		}

		@Override
		public Object defaultValue()
		{
			return _defaultValue;
		}

		@Override
		public @Nullable Object lookup(IOptionHolder holder)
		{
			return holder.options().lookup(this);
		}

		/*
		 * 
		 */
		@Override
		public void set(IOptionHolder holder, Object value)
		{
			holder.options().set(this, value);
		}

		@Override
		public void unset(IOptionHolder holder)
		{
			holder.options().unset(this);
		}
	}
	
	static class PackageProtectedFieldOptions
	{
		public static final IOptionKey<String> S42 = new StringOptionKey(FieldOptions.class, "S42", "42");
	}
	
	public static class FieldOptions
	{
		public static final OptionKey<String> S1 = new StringOptionKey(FieldOptions.class, "S1", "s");
		public static final OptionKey<String> S2 = new StringOptionKey(FieldOptions.class, "S2", "s");
		public static final OptionKey<Integer> I42 = new IntegerOptionKey(FieldOptions.class, "I42", 42);
		public static final OptionKey<Integer> I23 = new IntegerOptionKey(FieldOptions.class, "I23", 23);
		
		//
		// These should not be registered:
		//
		
		public static IOptionKey<String> NOT_FINAL =
			new StringOptionKey(FieldOptions.class, "NOT_FINAL", "x");
		
		protected static final IOptionKey<String> NOT_PUBLIC =
			new StringOptionKey(FieldOptions.class, "NOT_PUBLIC", "x");
		
		public final static String NOT_AN_OPTION = "NOT_AN_OPTION";
	}
}
