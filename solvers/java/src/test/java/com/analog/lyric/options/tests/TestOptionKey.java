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

import org.junit.Test;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.GenericOptionKey;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.IntegerOptionKey;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.options.StringOptionKey;
import com.analog.lyric.util.misc.Nullable;
import com.analog.lyric.util.test.SerializationTester;

public class TestOptionKey
{
	public static final IOptionKey<Boolean> YES =
		new BooleanOptionKey(TestOptionKey.class, "YES");
	
	public static final IOptionKey<Double> P =
		new DoubleOptionKey(TestOptionKey.class, "P");
	
	public static final IOptionKey<Integer> I =
		new IntegerOptionKey(TestOptionKey.class, "I");
	
	public static final IOptionKey<String> S =
		new StringOptionKey(TestOptionKey.class, "S");
	
	public static final IOptionKey<String> G =
		new GenericOptionKey<String>(TestOptionKey.class, "G", String.class, "g");
	
	@SuppressWarnings("null")
	public static enum Option implements IOptionKey<Object>
	{
		A(42),
		B("barf"),
		C(1.0);
		
		private final Object _defaultValue;
		
		private Option(Object defaultValue) { _defaultValue = defaultValue; }

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
	
	private static enum NotAnOptionKey
	{
		INSTANCE;
	}
	
	@Test
	public void test()
	{
		assertOptionInvariants(YES);
		assertOptionInvariants(P);
		assertOptionInvariants(I);
		assertOptionInvariants(S);
		assertOptionInvariants(G);
		
		for (Option key : Option.values())
		{
			assertOptionInvariants(key);
		}
		
		OptionKey<String> rogueKey = new StringOptionKey(Object.class, "foo");
		assertNull(OptionKey.getCanonicalInstance(rogueKey));
		
		OptionKey<String> rogueKey2 = SerializationTester.clone(rogueKey);
		assertNotSame(rogueKey2, rogueKey);
		assertSame(rogueKey.getDeclaringClass(), rogueKey2.getDeclaringClass());
		assertEquals(rogueKey.name(), rogueKey2.name());
		assertEquals(rogueKey.defaultValue(), rogueKey2.defaultValue());
		
		//
		// Error cases
		//
		
		expectThrow(DimpleException.class, "Error loading option key 'INSTANCE'.*",
			OptionKey.class, "inClass",	NotAnOptionKey.class, "INSTANCE");
		
		expectThrow(DimpleException.class, "Error loading option key 'NOT_AN_OPTION'.*",
			OptionKey.class, "inClass",	Option.class, "NOT_AN_OPTION");
		
		expectThrow(DimpleException.class, "'frob' is not a qualified option key name",
			OptionKey.class, "forQualifiedName", "frob");
		
		expectThrow(DimpleException.class, ".*ClassNotFoundException.*",
			OptionKey.class, "forQualifiedName", "no.such.package.NoSuchClass");
	}

	<T> void assertOptionInvariants(IOptionKey<T> key)
	{
		Class<T> type = key.type();
		Class<?> declaringClass = key.getDeclaringClass();
		assertNotNull(key.name());
		assertNotNull(type);
		assertTrue(type.isInstance(key.defaultValue()));
		assertEquals(key.toString(), key.name());
		
		IOptionKey<?> key3 = OptionKey.inClass(key.getDeclaringClass(), key.name());
		assertSame(key3, key);
		
		IOptionKey<?> key4 = OptionKey.forQualifiedName(OptionKey.qualifiedName(key));
		assertSame(key, key4);
		
		IOptionKey<?> key2 = SerializationTester.clone(key);
		assertSame(key2, key);
		
		assertEquals(declaringClass.getName() + "." + key.name(), OptionKey.qualifiedName(key));
		if (key instanceof OptionKey)
		{
			assertEquals(OptionKey.qualifiedName(key), ((OptionKey<?>)key).qualifiedName());
		}
		
		ExampleOptionHolder holder = new ExampleOptionHolder();
		assertEquals(key.defaultValue(), key.lookup(holder));
		
		for (Object newValue : new Object[] {"foo", 42, 3.14159})
		{
			if (type.isInstance(newValue))
			{
				key.set(holder, type.cast(newValue));
				assertEquals(newValue, key.lookup(holder));
				key.unset(holder);
				assertEquals(key.defaultValue(), key.lookup(holder));
			}
		}
	}
	
}
