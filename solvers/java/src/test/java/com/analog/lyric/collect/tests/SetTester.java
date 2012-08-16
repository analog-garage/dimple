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
