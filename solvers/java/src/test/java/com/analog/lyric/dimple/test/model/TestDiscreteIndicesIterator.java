package com.analog.lyric.dimple.test.model;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.DiscreteIndicesIterator;
import com.analog.lyric.dimple.model.domains.DomainList;

public class TestDiscreteIndicesIterator
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
