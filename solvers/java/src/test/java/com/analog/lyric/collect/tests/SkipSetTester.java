/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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
