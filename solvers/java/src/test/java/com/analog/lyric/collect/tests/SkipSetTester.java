package com.analog.lyric.collect.tests;

import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.analog.lyric.collect.SkipSet;

class SkipSetTester<T> extends SetTester<T>
{
	@Override
	public void validateSet(Set<T> set)
	{
		if (set instanceof SkipSet)
		{
			this.validateSkipSet((SkipSet<T>)set);
		}
		else
		{
			super.validateSet(set);
		}
	}
	
	public void validateSkipSet(SkipSet<T> set)
	{
		super.validateSet(set);
		
		Comparator<? super T> comparator = set.comparator();
		
		if (set.isEmpty())
		{
			try
			{
				set.last();
				fail("expected NoSuchElementException");
			}
			catch (NoSuchElementException ex)
			{
			}
			
			try
			{
				set.first();
				fail("expected NoSuchElementException");
			}
			catch (NoSuchElementException ex)
			{
			}
			
			assertEquals(null, set.pollFirst());
			assertEquals(null, set.pollLast());
		}
		else
		{
			assertEquals(null, set.higher(set.last()));
			assertEquals(null, set.lower(set.first()));
		}
		
		T prev = null;
		for (T elt : set)
		{
			assertTrue(set.contains2(elt));
			
			assertEquals(elt, set.ceiling(elt));
			assertEquals(elt, set.floor(elt));
			
			if (prev == null)
			{
				assertEquals(elt, set.first());
			}
			else
			{
				assertTrue(comparator.compare(prev, elt) < 0);
				assertEquals(elt, set.higher(prev));
				assertEquals(prev, set.lower(elt));
			}
			prev = elt;
		}
		
		if (prev != null)
		{
			assertEquals(prev, set.last());
		}
	}
}
