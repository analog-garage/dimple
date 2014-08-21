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

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.ClassOptionKey;
import com.analog.lyric.options.DoubleListOptionKey;
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.EnumOptionKey;
import com.analog.lyric.options.GenericOptionKey;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.IntegerListOptionKey;
import com.analog.lyric.options.IntegerOptionKey;
import com.analog.lyric.options.LongOptionKey;
import com.analog.lyric.options.OptionDoubleList;
import com.analog.lyric.options.OptionIntegerList;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.options.OptionKeys;
import com.analog.lyric.options.OptionStringList;
import com.analog.lyric.options.OptionValidationException;
import com.analog.lyric.options.StringListOptionKey;
import com.analog.lyric.options.StringOptionKey;
import com.analog.lyric.util.test.SerializationTester;

public class TestOptionKey
{
	public static final BooleanOptionKey YES =
		new BooleanOptionKey(TestOptionKey.class, "YES");
	
	public static final IOptionKey<Double> P =
		new DoubleOptionKey(TestOptionKey.class, "P");
	
	public static final IOptionKey<Integer> I =
		new IntegerOptionKey(TestOptionKey.class, "I");
	
	public static final IOptionKey<String> S =
		new StringOptionKey(TestOptionKey.class, "S");
	
	public static final IOptionKey<String> G =
		new GenericOptionKey<String>(TestOptionKey.class, "G", String.class, "g");
	
	public static final StringListOptionKey SL0 =
		new StringListOptionKey(TestOptionKey.class, "SL0");
	
	public static final StringListOptionKey SL2 =
		new StringListOptionKey(TestOptionKey.class, "SL2", "a", "b");
	
	public static final DoubleListOptionKey DL0 =
		new DoubleListOptionKey(TestOptionKey.class, "DL0");
	
	public static final DoubleListOptionKey DL2 =
		new DoubleListOptionKey(TestOptionKey.class, "DL2", 2.3, 4.5);
	
	public static final IntegerListOptionKey IL0 =
		new IntegerListOptionKey(TestOptionKey.class, "IL0");
	
	public static final IntegerListOptionKey IL2 =
		new IntegerListOptionKey(TestOptionKey.class, "IL2", 23, 45);

	public static final IntegerOptionKey DIGIT =
		new IntegerOptionKey(TestOptionKey.class, "DIGIT", 0, 0, 9);
	
	public static final DoubleOptionKey PROB =
		new DoubleOptionKey(TestOptionKey.class, "PROB", .5, 0.0, 1.0);
	
	public static final LongOptionKey LONG_BOUNDED =
		new LongOptionKey(TestOptionKey.class, "LONG_BOUNDED", 2L, 0L, 0xFFFFFFFFFFL);
	
	public static final ClassOptionKey<CharSequence> CLASS =
		new ClassOptionKey<>(TestOptionKey.class, "CLASS", CharSequence.class, String.class);
	
	public static enum Color
	{
		RED, GREEN, BLUE;
	}
	
	public static final EnumOptionKey<Color> COLOR =
		new EnumOptionKey<Color>(TestOptionKey.class, "COLOR", Color.class, Color.RED);
	
	@SuppressWarnings("null")
	public static enum Option implements IOptionKey<Serializable>
	{
		A(42),
		B("barf"),
		C(1.0);
		
		private final Serializable _defaultValue;
		
		private Option(Serializable defaultValue) { _defaultValue = defaultValue; }

		@Override
		public Serializable convertValue(Object value)
		{
			return type().cast(value);
		}
		
		@Override
		public Class<Serializable> type()
		{
			return Serializable.class;
		}

		@Override
		public Serializable defaultValue()
		{
			return _defaultValue;
		}

		@Override
		public @Nullable Serializable getOrDefault(IOptionHolder holder)
		{
			return holder.getOptionOrDefault(this);
		}

		@Override
		public @Nullable Serializable get(IOptionHolder holder)
		{
			return holder.getOption(this);
		}

		@Override
		public void set(IOptionHolder holder, Serializable value)
		{
			holder.setOption(this, value);
		}

		@Override
		public void unset(IOptionHolder holder)
		{
			holder.unsetOption(this);
		}

		@Override
		public Serializable validate(Serializable value, IOptionHolder optionHolder)
		{
			return type().cast(value);
		}
		
	}
	
	private static enum NotAnOptionKey
	{
		INSTANCE;
	}
	
	@Test
	public void test()
	{
		for (IOptionKey<?> key : OptionKeys.declaredInClass(getClass()).values())
		{
			assertOptionInvariants(key);
		}
		
		for (Option key : Option.values())
		{
			assertOptionInvariants(key);
		}
		
		// Test IntegerOptionKey
		assertEquals((Integer)3, DIGIT.validate(3, null));
		assertEquals((Integer)0, DIGIT.validate(0, null));
		assertEquals((Integer)9, DIGIT.validate(9, null));
		expectThrow(OptionValidationException.class, DIGIT, "validate", -1, null);
		expectThrow(OptionValidationException.class, DIGIT, "validate", 10, null);
		assertEquals(0, DIGIT.lowerBound());
		assertEquals(9, DIGIT.upperBound());
		assertEquals((Integer)42, I.convertValue(42.0));
		try
		{
			I.convertValue(42.5);
			fail("exception expected");
		}
		catch (IllegalArgumentException ex)
		{
		}
		try
		{
			I.convertValue("42.5");
			fail("exception expected");
		}
		catch (ClassCastException ex)
		{
		}
		
		// Test LongOptionKey
		assertEquals(0L, LONG_BOUNDED.lowerBound());
		assertEquals(0xFFFFFFFFFFL, LONG_BOUNDED.upperBound());
		assertEquals((Long)LONG_BOUNDED.lowerBound(), LONG_BOUNDED.validate(LONG_BOUNDED.lowerBound(), null));
		assertEquals((Long)LONG_BOUNDED.upperBound(), LONG_BOUNDED.validate(LONG_BOUNDED.upperBound(), null));
		expectThrow(OptionValidationException.class, LONG_BOUNDED, "validate", LONG_BOUNDED.lowerBound() - 1, null);
		expectThrow(OptionValidationException.class, LONG_BOUNDED, "validate", LONG_BOUNDED.upperBound() + 1, null);
		assertEquals((Long)23L, LONG_BOUNDED.convertValue(23.0));
		expectThrow(IllegalArgumentException.class, LONG_BOUNDED, "convertValue", (Object)23.4);
		
		// Test DoubleOptionKey
		assertEquals(0.0, PROB.validate(0.0, null), 0.0);
		assertEquals(1.0, PROB.validate(1.0, null), 0.0);
		assertEquals(.3, PROB.validate(.3, null), 0.0);
		expectThrow(OptionValidationException.class, PROB, "validate", -0.0001, null);
		expectThrow(OptionValidationException.class, PROB, "validate", 1.0001, null);
		expectThrow(OptionValidationException.class, PROB, "validate", Double.NaN, null);
		assertEquals(0.0, PROB.lowerBound(), 0.0);
		assertEquals(1.0, PROB.upperBound(), 0.0);
		
		// Test EnumOptionKey
		assertEquals(Color.RED, COLOR.defaultValue());
		assertEquals(Color.RED, COLOR.convertValue("RED"));
		assertEquals(Color.BLUE, COLOR.convertValue("BLUE"));
		expectThrow(IllegalArgumentException.class, COLOR, "convertValue", "gag");
		expectThrow(IllegalArgumentException.class, COLOR, "convertValue", "blue"); // case-sensitive
		
		// Test ClassOptionKey
		assertEquals(String.class, CLASS.defaultValue());
		assertEquals(CharSequence.class, CLASS.superClass());
		assertEquals(String.class, CLASS.convertValue("java.lang.String"));
		expectThrow(ClassCastException.class, CLASS, "convertValue", new int[] {2});
		assertSame(Object.class, CLASS.convertValue(Object.class)); // Does not validate
		expectThrow(OptionValidationException.class, "Could not construct class.*", CLASS, "convertValue", "bogus");
		expectThrow(OptionValidationException.class, ".*is not a subclass.*", CLASS, "validate", Object.class, null);
		
		// Test list keys
//		ExampleOptionHolder holder = new ExampleOptionHolder();
		assertTrue(SL0.defaultValue().isEmpty());
		assertArrayEquals(new Object[] { "a", "b" }, SL2.defaultValue().toArray());
		
		
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
		
		expectThrow(DimpleException.class, "'frob' is not a canonical option key name",
			OptionKey.class, "forCanonicalName", "frob");
		
		expectThrow(DimpleException.class, ".*ClassNotFoundException.*",
			OptionKey.class, "forCanonicalName", "no.such.package.NoSuchClass");
	}

	<T extends Serializable> void assertOptionInvariants(IOptionKey<T> key)
	{
		Class<T> type = key.type();
		Class<?> declaringClass = key.getDeclaringClass();
		assertNotNull(key.name());
		assertNotNull(type);
		assertTrue(type.isInstance(key.defaultValue()));
		assertEquals(key.defaultValue(), key.convertValue(key.defaultValue()));
		assertEquals(key.defaultValue(), key.validate(key.defaultValue(), null));
		if (declaringClass.isEnum())
		{
			assertEquals(key.name(), key.toString());
		}
		else
		{
			assertEquals(OptionKey.qualifiedName(key), key.toString());
		}
		
		IOptionKey<?> key3 = OptionKey.inClass(key.getDeclaringClass(), key.name());
		assertSame(key3, key);
		
		IOptionKey<?> key4 = OptionKey.forCanonicalName(OptionKey.canonicalName(key));
		assertSame(key, key4);
		
		IOptionKey<?> key2 = SerializationTester.clone(key);
		assertSame(key2, key);
		
		assertEquals(declaringClass.getName() + "." + key.name(), OptionKey.canonicalName(key));
		assertEquals(declaringClass.getSimpleName() + "." + key.name(), OptionKey.qualifiedName(key));
		if (key instanceof OptionKey)
		{
			assertEquals(OptionKey.canonicalName(key), ((OptionKey<?>)key).canonicalName());
			assertEquals(OptionKey.qualifiedName(key), ((OptionKey<?>)key).qualifiedName());
		}
		
		
		ExampleOptionHolder holder = new ExampleOptionHolder();
		assertEquals(key.defaultValue(), key.getOrDefault(holder));
		
		for (Object newValue : new Object[]
			{ "foo", 7, .314159, new OptionStringList("foo", "bar"), new OptionDoubleList(1.324, 234234.2),
			new OptionIntegerList(2,3,4,5)}
		)
		{
			if (type.isInstance(newValue))
			{
				key.set(holder, type.cast(newValue));
				assertEquals(newValue, key.get(holder));
				assertEquals(newValue, key.getOrDefault(holder));
				key.unset(holder);
				assertNull(key.get(holder));
				assertEquals(key.defaultValue(), key.getOrDefault(holder));
				if (key instanceof OptionKey)
				{
					((OptionKey<?>)key).convertAndSet(holder, newValue);
					assertEquals(newValue, key.get(holder));
					assertEquals(newValue, key.getOrDefault(holder));
					key.unset(holder);
				}
			}
		}
	}
	
}
