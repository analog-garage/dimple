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

package com.analog.lyric.collect;

import java.util.Map;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;

// TODO: Create additional Tuple classes as needed...

/**
 * A simple immutable tuple of two values.
 * 
 * @since 0.05
 * @author Christopher Barber
 */
@Immutable
public class Tuple2<T1,T2> extends Tuple implements Map.Entry<T1,T2>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	public final T1 first;
	public final T2 second;
	
	/*--------------
	 * Construction
	 */
	
	public Tuple2(T1 first, T2 second)
	{
		this.first = first;
		this.second = second;
	}
	
	public Tuple2(Map.Entry<T1,T2> other)
	{
		this(other.getKey(), other.getValue());
	}
	
	/**
	 * Constructs a new tuple with given elements.
	 * <p>
	 * This is often more convenient to use than the corresponding constructor
	 * because the type specifiers do not have to be explicitly specified. For example:
	 * <p>
	 * <pre>
	 *     {@code Tuple2<Foo,Bar> x = Tuple2.create(foo,bar);}
	 * </pre>
	 */
	public static <X1,X2> Tuple2<X1,X2> create(X1 first, X2 second)
	{
		return new Tuple2<X1, X2>(first, second);
	}
	
	/*--------------
	 * List methods
	 */
	
	@Override
	public Object get(int index)
	{
		switch (index)
		{
		case 0:
			return first;
		case 1:
			return second;
		default:
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public final int size()
	{
		return 2;
	}
	
	/*--------------------
	 * Map.Entry methods
	 */

	/**
	 * Returns {@link #first}
	 */
	@Override
	public final T1 getKey()
	{
		return first;
	}

	/**
	 * Returns {@link #second}
	 */
	@Override
	public final T2 getValue()
	{
		return second;
	}

	/**
	 * Not supported.
	 */
	@Override
	public final @Nullable T2 setValue(@Nullable T2 value)
	{
		throw new UnsupportedOperationException("tuples are immutable");
	}

}
