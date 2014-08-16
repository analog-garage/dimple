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

package com.analog.lyric.collect;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Contains static methods that convert a null parameter into
 * an immutable non-null instance.
 */
public final class NotNull
{
	/**
	 * Returns {@code l} if non-null, otherwise returns an immutable empty list.
	 */
	public static <T> List<T> list(@Nullable List<T> l)
	{
		if (l != null)
		{
			return l;
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Returns {@code m} if non-null, otherwise returns an immutable empty map.
	 */
	public static <K,V> Map<K,V> map(@Nullable Map<K,V> m)
	{
		if (m != null)
		{
			return m;
		}
		
		return Collections.emptyMap();
	}
	
	/**
	 * Returns {@code s} if non-null, otherwise returns an immutable empty set.
	 */
	public static <T> Set<T> set(@Nullable Set<T> s)
	{
		if (s != null)
		{
			return s;
		}
		
		return Collections.emptySet();
	}
}
