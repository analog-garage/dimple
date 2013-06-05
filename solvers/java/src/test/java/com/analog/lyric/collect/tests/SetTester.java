/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

import java.util.Iterator;
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
		
		try
		{
			assertFalse(set.retainAll(set));
		}
		catch (UnsupportedOperationException ex)
		{
			// Ignore - ok for set to not implement retainAll
		}
		
		assertEquals(size, set.size());
	}
	
	public void assertSetEquals(Set<T> set1, Set<T> set2)
	{
		assertEquals(set1.size(), set2.size());
		for (T element : set1)
		{
			assertTrue(set2.contains(element));
		}
		for (T element : set2)
		{
			assertTrue(set1.contains(element));
		}
	}
	
	public void assertSetOrderedEquals(Set<T> set1, Set<T> set2)
	{
		assertSetEquals(set1, set2);
		Iterator<T> set1Iter = set1.iterator();
		for (T element : set2)
		{
			assertEquals(element, set1Iter.next());
		}
		assertFalse(set1Iter.hasNext());
	}
}
