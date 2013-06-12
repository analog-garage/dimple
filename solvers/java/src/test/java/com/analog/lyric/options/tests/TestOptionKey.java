package com.analog.lyric.options.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.util.test.SerializationTester;

public class TestOptionKey
{
	public static final IOptionKey<Boolean> YES =
		new BooleanOptionKey(TestOptionKey.class, "YES");
	
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
		public Object lookup(IOptionHolder holder)
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
	
	@Test
	public void test()
	{
		assertOptionInvariants(YES);
		
		for (Option key : Option.values())
		{
			assertOptionInvariants(key);
		}
	}

	void assertOptionInvariants(IOptionKey<?> key)
	{
		assertNotNull(key.getDeclaringClass());
		assertNotNull(key.name());
		assertNotNull(key.type());
		assertTrue(key.type().isInstance(key.defaultValue()));
		assertEquals(key.toString(), key.name());
		
		IOptionKey<?> key3 = OptionKey.inClass(key.getDeclaringClass(), key.name());
		assertSame(key3, key);
		
		IOptionKey<?> key4 = OptionKey.forQualifiedName(OptionKey.qualifiedName(key));
		assertSame(key, key4);
		
		IOptionKey<?> key2 = SerializationTester.clone(key);
		assertSame(key2, key);
		
		assertEquals(key.getDeclaringClass().getName() + "." + key.name(), OptionKey.qualifiedName(key));
	}
}
