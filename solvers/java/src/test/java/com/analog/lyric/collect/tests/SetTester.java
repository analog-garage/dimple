package com.analog.lyric.collect.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Set;

public class SetTester<T> extends CollectionTester<T>
{
	public void validateSet(Set<T> set)
	{
		validateCollection(set);
		
		try
		{
			for (T elt : set)
			{
				assertFalse(set.add(elt));
			}
		}
		catch (UnsupportedOperationException ex) {}
		
		int size = set.size();
		
		assertFalse(set.retainAll(set));
		assertEquals(size, set.size());
	}
}
