package com.analog.lyric.collect.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import org.junit.Test;

import com.analog.lyric.collect.Comparators;

public class TestComparators
{
	@Test
	public void test()
	{
		//
		// Test Comparators.fromCollection
		//
		
		assertNull(Comparators.fromCollection(new ArrayList<Object>()));
		assertNull(Comparators.fromCollection(new TreeSet<Integer>()));
		
		Comparator<? super Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer arg0, Integer arg1)
			{
				return arg1.compareTo(arg0);
			}
		};
		
		TreeSet<Integer> set = new TreeSet<Integer>(comparator);
		assertSame(comparator, Comparators.fromCollection(set));
		
		//
		// Test Comparators.lexicalIntArray
		//
		
		Comparator<int[]> lexical = Comparators.lexicalIntArray();
		assertTrue(lexical.compare(new int[] {1,2,3}, new int[] {1,3,2}) < 0);
		assertTrue(lexical.compare(new int[] {1,3,2}, new int[] {1,2,3}) > 0);
		assertEquals(0, lexical.compare(new int[] {1,2,3,4}, new int[] {1,2,3,4}));
		assertTrue(lexical.compare(new int[] {4,3,2}, new int[] {20, 1}) > 0);
		
		//
		// Test Comparators.reverseLexicalIntArray
		//
		
		Comparator<int[]> rlexical = Comparators.reverseLexicalIntArray();
		assertTrue(rlexical.compare(new int[] {1,2,3}, new int[] {1,3,2}) > 0);
		assertTrue(rlexical.compare(new int[] {1,3,2}, new int[] {1,2,3}) < 0);
		assertEquals(0, rlexical.compare(new int[] {1,2,3,4}, new int[] {1,2,3,4}));
		assertTrue(rlexical.compare(new int[] {4,3,2}, new int[] {20, 1}) > 0);
	}
}
