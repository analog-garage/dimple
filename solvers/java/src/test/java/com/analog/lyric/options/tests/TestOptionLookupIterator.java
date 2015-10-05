/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.OptionLookupIterator;
import com.analog.lyric.options.StringOptionKey;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestOptionLookupIterator
{
	public static final StringOptionKey key = new StringOptionKey(TestOptionLookupIterator.class, "key", "default");
	
	@Test
	public void test()
	{
		IOptionHolder root = new ExampleOptionHolder();
		IOptionHolder child = new ExampleOptionHolder(root);
		assertSame(root, child.getOptionParent());
		
		OptionLookupIterator<String> iter = OptionLookupIterator.create(child, key);
		assertSame(key, iter.key());
		
		OptionLookupIterator<String> iter2 = OptionLookupIterator.create(child, key);
		assertNotSame(iter, iter2);
		assertSame(key, iter2.key());
		iter2.release();
		
		assertTrue(iter.hasNext());
		assertNull(iter.lastSource());
		assertEquals("default", iter.next());
		assertNull(iter.lastSource());
		assertFalse(iter.hasNext());
		
		iter.release();
		expectThrow(NullPointerException.class, iter, "key");
		
		root.setOption(key, "root");
		child.setOption(key, "child");
		
		iter2 = OptionLookupIterator.create(child, key);
		assertSame(iter, iter2);
		assertNull(iter2.lastSource());
		assertSame(key, iter2.key());
		
		assertTrue(iter2.hasNext());
		assertEquals("child", iter2.next());
		assertSame(child, iter2.lastSource());
		assertTrue(iter2.hasNext());
		assertEquals("root", iter2.next());
		assertSame(root, iter2.lastSource());
		assertTrue(iter2.hasNext());
		assertEquals("default", iter2.next());
		assertNull(iter2.lastSource());
		assertNull(iter2.next());
	}
}
