package com.analog.lyric.collect;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

public final class Comparators
{
	private Comparators() {} // Prevent instantiation
	
	/**
	 * Returns comparator used by collection.
	 * 
	 * <If collection is a {@link SortedSet} returns {@link SortedSet#comparator()}, else returns null.
	 */
	public static <T> Comparator<? super T> fromCollection(Collection<? extends T> collection)
	{
		if (collection instanceof SortedSet)
		{
			return (Comparator<? super T>) ((SortedSet<? extends T>)collection).comparator();
		}
		return null;
	}
}
