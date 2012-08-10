package com.analog.lyric.collect;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains static methods that convert a null parameter into
 * an immutable non-null instance.
 */
public final class NonNull
{
	/**
	 * Returns {@code l} if non-null, otherwise returns an immutable empty list.
	 */
	public static <T> List<T> list(List<T> l)
	{
		if (l == null)
		{
			l = Collections.emptyList();
		}
		return l;
	}
	
	/**
	 * Returns {@code m} if non-null, otherwise returns an immutable empty map.
	 */
	public static <K,V> Map<K,V> map(Map<K,V> m)
	{
		if (m == null)
		{
			m = Collections.emptyMap();
		}
		return m;
	}
	
	/**
	 * Returns {@code s} if non-null, otherwise returns an immutable empty set.
	 */
	public static <T> Set<T> set(Set<T> s)
	{
		if (s == null)
		{
			s = Collections.emptySet();
		}
		return s;
	}
}
