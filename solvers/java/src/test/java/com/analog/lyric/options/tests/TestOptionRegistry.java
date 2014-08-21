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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import com.analog.lyric.options.AmbiguousOptionNameException;
import com.analog.lyric.options.GenericOptionKey;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.IntegerOptionKey;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.options.OptionKeys;
import com.analog.lyric.options.OptionRegistry;
import com.analog.lyric.options.StringOptionKey;
import com.google.common.collect.Iterables;

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
		assertTrue(registry.autoLoadKeys());
		assertEquals(0, registry.size());
		assertInvariants(registry);
		
		assertEquals(4, registry.addFromClasses(getClass()));
		assertEquals(0, registry.addFromClasses(getClass()));
		assertInvariants(registry);
		assertEquals(4, registry.size());
		
		assertEquals(2, registry.getAllMatching(".*\\.S\\d").size());
		assertEquals(1, registry.getAllMatching(Pattern.compile(".*\\.S2")).size());
		
		assertNull(registry.get("SomeOptions.A"));
		
		StringOptionKey FooA = com.analog.lyric.options.tests.foo.SomeOptions.A;
		StringOptionKey FooB = com.analog.lyric.options.tests.foo.SomeOptions.B;
		StringOptionKey FooD = com.analog.lyric.options.tests.foo.SomeOptions.D;
		
		StringOptionKey BarA = com.analog.lyric.options.tests.bar.SomeOptions.A;
//		StringOptionKey BarB = com.analog.lyric.options.tests.bar.SomeOptions.B;
		StringOptionKey BarC = com.analog.lyric.options.tests.bar.SomeOptions.C;
		
		// Test autoloading
		assertEquals(FooA, registry.get(FooA.canonicalName()));
		assertEquals(FooA, registry.get("SomeOptions.A"));
		// Other options in same class also get loaded
		assertEquals(FooB, registry.get("SomeOptions.B"));
		assertEquals(FooD, registry.get("SomeOptions.D"));
		assertInvariants(registry);
		
		assertEquals(BarC, registry.get(BarC.canonicalName()));
		assertEquals(BarC, registry.get("SomeOptions.C"));
		assertEquals(FooD, registry.get("SomeOptions.D"));
		assertNull(registry.get("SomeOptions.doesNotExist"));
		try
		{
			registry.get("SomeOptions.A");
			fail("should not get here");
		}
		catch (AmbiguousOptionNameException ex)
		{
			// In order in which they were added...
			assertArrayEquals(new Object[] { FooA, BarA }, ex.ambiguousKeys().toArray());
		}
		assertInvariants(registry);

		expectThrow(NoSuchElementException.class, "Unknown option key 'DoesNot.exist'",
			registry, "asKey", "DoesNot.exist");
		expectThrow(IllegalArgumentException.class, "Expected String or IOptionKey instead of 'Integer'",
			registry, "asKey", 42);
		
		assertNull(registry.get("this.class.does.not.exist"));
		
		assertEquals(false, registry.add(OptionKeys.declaredInClass(BarA.getDeclaringClass())));

		registry = new OptionRegistry(false);
		assertInvariants(registry);
		assertEquals(0, registry.size());
		
		assertNull(registry.get(FooA.canonicalName()));
		
	}
	
	@Test
	public void testOptionKeys()
	{
		testOptionKeys(Object.class);
		testOptionKeys(getClass());
		testOptionKeys(FieldOptions.class, FieldOptions.I23, FieldOptions.I42, FieldOptions.S1, FieldOptions.S2);
	}
	
	private void testOptionKeys(Class<?> declaringClass, IOptionKey<?> ... expected)
	{
		OptionKeys keys = OptionKeys.declaredInClass(declaringClass);
		assertSame(keys, OptionKeys.declaredInClass(declaringClass));
		assertSame(keys, keys.clone());
		
		assertSame(declaringClass, keys.declaringClass());
		
		assertEquals(expected.length, keys.values().size());

		Set<String> keySet = keys.keySet();
		Set<Map.Entry<String,IOptionKey<?>>> entrySet = keys.entrySet();
		assertEquals(keySet.size(), entrySet.size());
		
		for (Map.Entry<String, IOptionKey<?>> entry : entrySet)
		{
			String name = entry.getKey();
			IOptionKey<?> key = keys.get(name);
			assertNotNull(key);
			assertTrue(keySet.contains(name));
			assertTrue(keys.containsValue(key));
			assertTrue(keys.containsKey(name));
			
			@SuppressWarnings("unchecked")
			IOptionKey<?> key2 =
				new GenericOptionKey<Serializable>(key.getDeclaringClass(), key.name(), (Class<Serializable>) key.type(), key.defaultValue());
			assertFalse(keys.containsValue(key2));
		}
		
		assertFalse(keys.containsKey("does not exist"));
		assertFalse(keys.containsValue("does not exist"));
		assertNull(keys.get("does not exist"));

		Map<String,IOptionKey<?>> map = new HashMap<String,IOptionKey<?>>();
		map.putAll(keys);
		assertEquals(map, keys);
		assertEquals(map.hashCode(), keys.hashCode());
	}
	
	private void assertInvariants(OptionRegistry registry)
	{
		ArrayList<IOptionKey<?>> all1 = new ArrayList<>();
		Iterables.addAll(all1, registry);
		assertEquals(all1.size(), registry.size());
		
		for (IOptionKey<?> key : registry)
		{
			assertSame(key, registry.get(OptionKey.canonicalName(key)));
			assertSame(key, registry.asKey(key));
			assertSame(key, registry.asKey(OptionKey.canonicalName(key)));
			try
			{
				assertEquals(key, registry.get(OptionKey.qualifiedName(key)));
			}
			catch (AmbiguousOptionNameException ex)
			{
				assertEquals(OptionKey.qualifiedName(key), ex.optionName());
				assertNotEquals(1, ex.ambiguousKeys().size());
				assertTrue(ex.ambiguousKeys().indexOf(key) >= 0);
			}
		}
		
		assertNull(registry.get("this-key-does-not-exist"));
		
		ArrayList<IOptionKey<?>> all2 = registry.getAllMatching(".*");
		assertEquals(all1, all2);
		
		ArrayList<IOptionKey<?>> all3 = new ArrayList<>();
		Collection<OptionKeys> allKeys = registry.getOptionKeys();
		for (OptionKeys keys : allKeys)
		{
			for (IOptionKey<?> key : keys.values())
			{
				all3.add(key);
			}
		}
		assertEquals(all1, all3);
		
		assertTrue(registry.getAllMatching("does-not-exist").isEmpty());
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
		
		// An alias
		public static final OptionKey<String> S1A = S1;
		
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
