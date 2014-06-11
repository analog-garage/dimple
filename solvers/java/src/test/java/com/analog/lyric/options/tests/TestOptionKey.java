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

import org.junit.Test;

import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.util.misc.Nullable;
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
