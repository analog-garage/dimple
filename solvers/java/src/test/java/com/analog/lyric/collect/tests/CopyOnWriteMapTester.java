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

import static org.junit.Assert.*;

import java.util.Map;

import com.analog.lyric.collect.CopyOnWriteMap;

public class CopyOnWriteMapTester<K,V> extends MapTester<K, V>
{
	private static enum BogusKey { INSTANCE; }
	
	@Override
	public void validateMap(Map<K,V> map)
	{
		if (map instanceof CopyOnWriteMap)
		{
			validateCopyOnWriteMap((CopyOnWriteMap<K,V>)map);
		}
		else
		{
			super.validateMap(map);
		}
	}
	
	public void validateCopyOnWriteMap(CopyOnWriteMap<K,V> map)
	{
		super.validateMap(map);
		
		Map<K,V> originalMap = map.originalMap();
		assertNotNull(originalMap);
		
		if (!map.wasCopied())
		{
			assertMapEquals(originalMap, map);
			
			if (map.isEmpty())
			{
				// If map is empty, clearing it should not cause it
				// to make a copy of the original map.
				map.clear();
			}
			
			// Removing a non-existent key should also not
			// cause a copy to be made.
			assertNull(map.remove(BogusKey.INSTANCE));
			
			assertFalse(map.wasCopied());
		}
	}
}
