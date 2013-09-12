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
