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

package com.analog.lyric.collect.tests;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import com.analog.lyric.collect.ReleasableArrayIterator;
import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.collect.ReleasableIterators;
import com.analog.lyric.collect.SingleIterator;
import com.analog.lyric.collect.UnmodifiableReleasableIterator;
import com.analog.lyric.util.test.ExceptionTester;

public class TestReleasableIterators
{
	@Test
	public void testUnmodifiableReleasableIterator()
	{
		ReleasableIterator<?> iter = new UnmodifiableReleasableIterator<Object>() {
			@Override
			public void release()
			{
			}

			@Override
			public boolean hasNext()
			{
				return false;
			}

			@Override
			public @Nullable Object next()
			{
				return null;
			}
		};
		
		expectThrow(UnsupportedOperationException.class, iter, "remove");
	}

	@Test
	public void testReleaseableArrayIterator()
	{
		Integer[] digits = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		
		ReleasableArrayIterator<Integer> iter = ReleasableArrayIterator.create(digits);
		assertNotSame(iter, ReleasableArrayIterator.create(digits));
		for (int i = 0; i < 10; ++i)
		{
			assertTrue(iter.hasNext());
			assertEquals(i, iter.next().intValue());
		}
		assertFalse(iter.hasNext());
		
		ExceptionTester.expectThrow(UnsupportedOperationException.class, iter, "remove");
		
		iter.release();
		assertSame(iter, ReleasableArrayIterator.create(digits));
	}
	
	@Test
	public void testEmptyIterator()
	{
		ReleasableIterator<String> iter = ReleasableIterators.emptyIterator();
		
		assertFalse(iter.hasNext());
		assertNull(iter.next());
		expectThrow(UnsupportedOperationException.class, iter, "remove");
		iter.release(); // does nothing
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testSingleIterator()
	{
		// Note that this is not a ReleasableIterator.
		
		Iterator<String> iter = new SingleIterator<>("foo");
		assertTrue(iter.hasNext());
		assertEquals("foo", iter.next());
		assertFalse(iter.hasNext());
		assertNull(iter.next());
	}
}
