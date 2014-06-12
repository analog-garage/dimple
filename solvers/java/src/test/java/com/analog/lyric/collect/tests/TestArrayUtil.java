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

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.IKeyed;
import com.google.common.collect.Ordering;

public class TestArrayUtil
{
	@Test
	public void allocateArrayOfType()
	{
		String[] s3 = ArrayUtil.allocateArrayOfType(String.class, null, 3);
		assertSame(String[].class, s3.getClass());
		assertEquals(3, s3.length);
		
		Object[] o3 = ArrayUtil.allocateArrayOfType(Number.class, (Object[])s3, 3);
		assertSame(Number[].class, o3.getClass());
		assertEquals(3, o3.length);
		
		Object[] o2 = ArrayUtil.allocateArrayOfType(Double.class,  o3, 2);
		assertSame(o3, o2);
		
		Number[] n4 = ArrayUtil.allocateArrayOfType(Number.class, (Number[])o3, 4);
		assertEquals(4, n4.length);
	}
	
	@Test
	public void allFuzzyEqual()
	{
		assertTrue(ArrayUtil.allFuzzyEqual(new double[0], 0.0));
		assertTrue(ArrayUtil.allFuzzyEqual(new double[] { 1.234} , 0.0));
		assertTrue(ArrayUtil.allFuzzyEqual(new double[] { 1.04, 1.0, .95 }, .1));
		assertFalse(ArrayUtil.allFuzzyEqual(new double[] { 1.05, 1.0, .8999, 1.045 }, .1));
	}
	
	@Test
	public void subsetFuzzyEqual()
	{
		assertTrue(ArrayUtil.subsetFuzzyEqual(new double[] { 1.0, 2.0, 3.0 }, new int[] { 1 }, 0.0));
		assertTrue(ArrayUtil.subsetFuzzyEqual(new double[] { 1.0, 2.0, 3.0 }, new int[] { 1, 2 }, 1.1));
		assertFalse(ArrayUtil.subsetFuzzyEqual(new double[] { 1.0, 2.0, 3.0 }, new int[] { 0, 2 }, 1.1));
	}
	
	@Test
	public void cloneArray()
	{
		assertNull(ArrayUtil.cloneArray((double[])null));
		assertSame(ArrayUtil.EMPTY_DOUBLE_ARRAY, ArrayUtil.cloneArray(new double[0]));
		double[] d = new double[] { 1.2345, 2.234234, 4.5 };
		double[] d2 = ArrayUtil.cloneArray(d);
		assertNotSame(d, d2);
		assertArrayEquals(d, d2, 0.0);

		assertNull(ArrayUtil.cloneArray((int[])null));
		assertSame(ArrayUtil.EMPTY_INT_ARRAY, ArrayUtil.cloneArray(new int[0]));
		int[] i = new int[] { 1, 3, 45, 52 };
		int[] i2 = ArrayUtil.cloneArray(i);
		assertNotSame(i, i2);
		assertArrayEquals(i, i2);
		
		assertNull(ArrayUtil.cloneArray((int[][])null));
		assertSame(ArrayUtil.EMPTY_INT_ARRAY_ARRAY, ArrayUtil.cloneArray(new int[0][]));
		int ii[][] = new int[][] { new int[] { 1, 2}, new int[] { 3, 4} };
		int ii2[][] = requireNonNull(ArrayUtil.cloneArray(ii));
		assertArrayEquals(ii, ii2);
		assertNotSame(ii, ii2);
		assertSame(ii[0], ii2[0]);
	}

	@Test
	public void contractSortedIndexList()
	{
		assertArrayEquals(new int[] { 0, 1, 3 },
			ArrayUtil.contractSortedIndexList(new int[] { 0, 1, 3 }, new int[] { 4, 5 }));
		assertArrayEquals(new int[] {},
			ArrayUtil.contractSortedIndexList(new int[] {}, new int[] { 4, 5 }));
		assertArrayEquals(new int[] { 0, 1 },
			ArrayUtil.contractSortedIndexList(new int[] { 0, 1, 3 }, new int[] { 1, 2 }));
		assertArrayEquals(new int[] { 0, 1 },
			ArrayUtil.contractSortedIndexList(new int[] { 0, 1, 3 }, new int[] { 3, 4, 5}));
		assertArrayEquals(new int[] { 0, 2 },
			ArrayUtil.contractSortedIndexList(new int[] { 0, 1, 3 }, new int[] { 0 }));
		assertArrayEquals(new int[] { 0, 1, 2, 4 },
			ArrayUtil.contractSortedIndexList(new int[] { 0, 1, 3, 5 }, new int[] { 2 }));
	}
	
	@Test
	public void copyArrayForInsert()
	{
		int i[] = new int[] { 1, 2, 3, 4 };
		assertArrayEquals(i, ArrayUtil.copyArrayForInsert(i, 2, 0));
		assertArrayEquals(new int[] { 1, 2, 0, 0, 0, 3, 4}, ArrayUtil.copyArrayForInsert(i, 2, 3));
		assertArrayEquals(new int[] { 0, 0 }, ArrayUtil.copyArrayForInsert((int[])null, 0, 2));
		try
		{
			ArrayUtil.copyArrayForInsert(i, 5, 1);
			fail("expected exception");
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
		}
		
		double d[] = new double[] { 1, 2, 3, 4 };
		assertArrayEquals(d, ArrayUtil.copyArrayForInsert(d, 2, 0), 0.0);
		assertArrayEquals(new double[] { 1, 2, 0, 0, 0, 3, 4}, ArrayUtil.copyArrayForInsert(d, 2, 3), 0.0);
		assertArrayEquals(new double[] { 0, 0 }, ArrayUtil.copyArrayForInsert((double[])null, 0, 2), 0.0);
		
		int ii[][] = new int[][] { new int[] {1}, new int[] {2,3}, new int[] {4, 5, 6} };
		assertArrayEquals(ii, ArrayUtil.copyArrayForInsert(ii, 2, 0));
		assertArrayEquals(new int[][] { ii[0], null, null, ii[1], ii[2] }, ArrayUtil.copyArrayForInsert(ii, 1, 2));
		assertArrayEquals(new int[][] { null, null }, ArrayUtil.copyArrayForInsert((int[][])null, 0, 2));
	}
	
	@Test
	public void isSorted()
	{
		final Random rand = new Random(123);
		
		Integer[] ia = new Integer[42];
		for (int i = 0; i < ia.length; ++i)
		{
			ia[i] = rand.nextInt();
		}
		
		assertFalse(ArrayUtil.isSorted(ia));
		Arrays.sort(ia);
		assertTrue(ArrayUtil.isSorted(ia));
		assertTrue(ArrayUtil.isSorted(new Integer[0]));
		assertTrue(ArrayUtil.isSorted(new Integer[] { 23 }));

		Comparator<Integer> reverse = Ordering.natural().reverse();
		assertFalse(ArrayUtil.isSorted(ia, reverse));
		Arrays.sort(ia, reverse);
		assertTrue(ArrayUtil.isSorted(ia, reverse));
		assertTrue(ArrayUtil.isSorted(new Integer[] { 23 }, reverse));
	}
	
	@Test
	public void linearSearch()
	{
		class Foo implements IKeyed<Integer>
		{
			int key;
			
			Foo(int i) { key = i; }
			
			@Override
			public Integer getKey()
			{
				return key;
			}
		}
		
		Foo[] array = new Foo[] { new Foo(1), new Foo(3), new Foo(1), new Foo(5), new Foo(3) };
		
		assertEquals(-1, ArrayUtil.linearSearch(array, 42));
		assertEquals(0, ArrayUtil.linearSearch(array, 1));
		assertEquals(1, ArrayUtil.linearSearch(array, 3));
		assertEquals(3, ArrayUtil.linearSearch(array,  5));
		assertEquals(2, ArrayUtil.linearSearch(array, 1, 1, array.length));
		assertEquals(-1, ArrayUtil.linearSearch(array, 1, 1, 1));
	}
	
	@Test
	public void toIntArray()
	{
		//
		// Test toIntArray
		//
		
		assertNull(ArrayUtil.toIntArray("hi"));
		assertNull(ArrayUtil.toIntArray(new Object[] { "barf" }));
		assertNull(ArrayUtil.toIntArray(new Object[] { 1, 2.5 }));
	
		assertArrayEquals(new int[] { 1, 2, 3 }, ArrayUtil.toIntArray(new Object[] { 1, 2.0, 3L }));
		
		int[] ints = new int[] { 23, 42, 123 };
		assertSame(ints, ArrayUtil.toIntArray(ints));
	}
	
	@Test
	public void toArray()
	{
		//
		// Test toArray
		//
		
		assertNull(ArrayUtil.toArray("hi"));
		assertArrayEquals(new Object[] { 42.0, 23.0, 2.3 }, ArrayUtil.toArray(new double[] { 42, 23, 2.3 }));
		
		Object[] objs = new Object[] { "foo", 42 };
		assertSame(objs, ArrayUtil.toArray(objs));
	}
}
