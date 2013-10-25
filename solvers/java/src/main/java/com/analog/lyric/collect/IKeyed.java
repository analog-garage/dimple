package com.analog.lyric.collect;

/**
 * Simple interface for element with an inherent key.
 * 
 * @since 0.05
 * @author Christopher Barber
 * @see KeyedPriorityQueue
 */
public interface IKeyed<K>
{
	/**
	 * Returns key for this element.
	 */
	public K getKey();
}
