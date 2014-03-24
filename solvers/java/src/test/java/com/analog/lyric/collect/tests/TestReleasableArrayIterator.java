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

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.collect.ReleasableArrayIterator;
import com.analog.lyric.util.test.ExceptionTester;

public class TestReleasableArrayIterator
{

	@Test
	public void test()
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
}
