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

package com.analog.lyric.dimple.test.model;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.DiscreteIndicesIterator;
import com.analog.lyric.dimple.model.domains.DomainList;
import com.analog.lyric.dimple.test.DimpleTestBase;

public class TestDiscreteIndicesIterator extends DimpleTestBase
{

	@Test
	public void test()
	{
		DiscreteIndicesIterator iter = new DiscreteIndicesIterator(2, 3);
		
		int[][] expected = new int[][] {
			new int[] {0,0},
			new int[] {1,0},
			new int[] {0,1},
			new int[] {1,1},
			new int[] {0,2},
			new int[] {1,2}
		};
		
		for (int i = 0; i < 2; ++i)
		{
			for (int[] curExpected : expected)
			{
				assertTrue(iter.hasNext());
				assertArrayEquals(curExpected, iter.next());
			}
			assertFalse(iter.hasNext());
			iter.reset();
		}
		
		int[] indices = new int[2];
		iter = new DiscreteIndicesIterator(new int[] { 2, 3}, indices);
		for (int[] curExpected : expected)
		{
			assertTrue(iter.hasNext());
			assertSame(indices, iter.next());
			assertArrayEquals(curExpected, indices);
		}
		assertFalse(iter.hasNext());
		
		DiscreteDomain d2 = DiscreteDomain.range(1,2);
		DiscreteDomain d3 = DiscreteDomain.range(1,3);
		DomainList<DiscreteDomain> dl2x3 = DomainList.create(d2, d3);
		
		iter = new DiscreteIndicesIterator(dl2x3, indices);
		for (int[] curExpected : expected)
		{
			assertTrue(iter.hasNext());
			assertSame(indices, iter.next());
			assertArrayEquals(curExpected, indices);
		}
		assertFalse(iter.hasNext());
		
		iter = new DiscreteIndicesIterator(dl2x3);
		for (int[] curExpected : expected)
		{
			assertTrue(iter.hasNext());
			assertArrayEquals(curExpected, iter.next());
		}
		assertFalse(iter.hasNext());
		
		iter = new DiscreteIndicesIterator(2,1,3);
		expected = new int[][] {
			new int[] {0,0,0},
			new int[] {1,0,0},
			new int[] {0,0,1},
			new int[] {1,0,1},
			new int[] {0,0,2},
			new int[] {1,0,2}
		};
		
		for (int i = 0; i < 2; ++i)
		{
			for (int[] curExpected : expected)
			{
				assertTrue(iter.hasNext());
				assertArrayEquals(curExpected, iter.next());
			}
			assertFalse(iter.hasNext());
			iter.reset();
		}

		iter = new DiscreteIndicesIterator();
		assertTrue(iter.hasNext());
		assertEquals(0, iter.next().length);
		assertFalse(iter.hasNext());
		iter.reset();
		assertTrue(iter.hasNext());
		assertEquals(0, iter.next().length);
		assertFalse(iter.hasNext());

		expectThrow(UnsupportedOperationException.class, ".*DiscreteIndicesIterator\\.remove.*", iter, "remove");
	}

}
