package com.analog.lyric.collect.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.collect.ArrayUtil;

public class TestArrayUtil
{
	@Test
	public void test()
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
		
		//
		// Test toArray
		//
		
		assertNull(ArrayUtil.toArray("hi"));
		assertArrayEquals(new Object[] { 42.0, 23.0, 2.3 }, ArrayUtil.toArray(new double[] { 42, 23, 2.3 }));
		
		Object[] objs = new Object[] { "foo", 42 };
		assertSame(objs, ArrayUtil.toArray(objs));
	}
}
