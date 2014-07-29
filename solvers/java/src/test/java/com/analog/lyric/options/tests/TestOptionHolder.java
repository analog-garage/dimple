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

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.options.IOption;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.IntegerOptionKey;
import com.analog.lyric.options.LocalOptionHolder;
import com.analog.lyric.options.StatelessOptionHolder;
import com.analog.lyric.options.StringOptionKey;
import com.analog.lyric.util.misc.Nullable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * 
 * @since 0.06
 * @author CBarber
 */
public class TestOptionHolder
{
	public final static IntegerOptionKey NOT_SET = new IntegerOptionKey(TestOptionHolder.class, "NOT_SET", 666);
	
	public final static StringOptionKey K = new StringOptionKey(TestOptionHolder.class, "K");
	
	public final static IntegerOptionKey I = new IntegerOptionKey(TestOptionHolder.class, "I");
	
	@Test
	public void test()
	{
		IOptionHolder holder = new StatelessOptionHolder(){};
		assertInvariants(holder);
		
		assertNull(holder.getOptionParent());
		holder.clearLocalOptions(); // doesn't do anything
		holder.unsetOption(K); // also doesn't do anything
		assertNull(holder.getLocalOption(K));
		expectThrow(UnsupportedOperationException.class, holder, "setOption", K, "foo");
		assertEquals(0, Iterables.size(holder.getLocalOptions()));
		
		holder = new LocalOptionHolder();
		assertInvariants(holder);
		assertNull(holder.getOptionParent());
		holder.unsetOption(K);

		assertNull(holder.getLocalOption(K));
		assertNull(holder.getOption(K));
		assertEquals(1, Iterators.size(holder.getOptionDelegates()));
		assertEquals(0, Iterables.size(holder.getLocalOptions()));
		
		holder.setOption(K, "hi");
		assertEquals("hi", holder.getLocalOption(K));
		assertEquals("hi", holder.getOption(K));
		assertInvariants(holder);
		
		holder.setOption(K, "bye");
		assertEquals("bye", holder.getOption(K));
		
		holder.unsetOption(K);
		assertEquals("", holder.getOptionOrDefault(K));
		assertNull(holder.getOption(K));
		
		holder.setOption(K, "k");
		holder.setOption(I, 42);
		assertEquals("k", holder.getOption(K));
		assertEquals((Integer)42, holder.getOption(I));
		assertInvariants(holder);
		assertEquals(2, Iterables.size(holder.getLocalOptions()));
		holder.clearLocalOptions();
		assertNull(holder.getOption(K));
		assertNull(holder.getOption(I));
		assertEquals(0, Iterables.size(holder.getLocalOptions()));
		
		final IOptionHolder parent = holder;
		IOptionHolder child = new LocalOptionHolder() {
			@Override
			public @Nullable IOptionHolder getOptionParent()
			{
				return parent;
			}
		};
		
		assertInvariants(child);
		assertNull(child.getOption(K));
		parent.setOption(K, "bar");
		assertEquals("bar", child.getOption(K));
		assertInvariants(child);
		child.setOption(K, "gag");
		assertEquals("gag", child.getOption(K));
		assertInvariants(child);
	}
	
	private void assertInvariants(IOptionHolder holder)
	{
		Set<IOptionKey<?>> seen = new HashSet<IOptionKey<?>>();
		for (IOption<?> option : holder.getLocalOptions())
		{
			assertTrue(seen.add(option.key()));
			assertEquals(option.value(), holder.getLocalOption(option.key()));
			assertEquals(option.value(), holder.getOption(option.key()));
			assertEquals(option.value(), holder.getOptionOrDefault(option.key()));
		}
		
		assertNull(holder.getLocalOption(NOT_SET));
		assertNull(holder.getOption(NOT_SET));
		assertEquals(NOT_SET.defaultValue(), holder.getOptionOrDefault(NOT_SET));
		
		ReleasableIterator<? extends IOptionHolder> delegates = holder.getOptionDelegates();
		assertTrue(delegates.hasNext());
		assertSame(holder, delegates.next());
		while (delegates.hasNext())
		{
			IOptionHolder delegate = delegates.next();
			assertNotSame(delegate, holder);
			for (IOption<?> option : delegate.getLocalOptions())
			{
				if (seen.add(option.key()))
				{
					assertEquals(option.value(), delegate.getLocalOption(option.key()));
					assertEquals(option.value(), holder.getOption(option.key()));
				}
				else
				{
					// Option is set, but value will come from previous delegate.
					assertNotNull(holder.getOption(option.key()));
				}
			}
		}
		delegates.release();
	}
}
