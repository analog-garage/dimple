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

package com.analog.lyric.benchmarking.utils.doublespace.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;

public class Utilities
{
	private static Function<Object, String> stringify = new Function<Object, String>()
	{
		@Override
		public String apply(Object o)
		{
			if (o instanceof Object[])
			{
				return Arrays.deepToString((Object[]) o);
			}
			else if (o instanceof int[])
			{
				return "[" + Ints.join(",  ", (int[]) o) + "]";
			}
			else if (o instanceof boolean[])
			{
				return "[" + Booleans.join(",  ", (boolean[]) o) + "]";
			}
			else if (o instanceof short[])
			{
				return "[" + Shorts.join(",  ", (short[]) o) + "]";
			}
			else if (o instanceof char[])
			{
				return "[" + Chars.join(",  ", (char[]) o) + "]";
			}
			else if (o instanceof long[])
			{
				return "[" + Longs.join(",  ", (long[]) o) + "]";
			}
			else if (o instanceof float[])
			{
				return "[" + Floats.join(",  ", (float[]) o) + "]";
			}
			else if (o instanceof double[])
			{
				return "[" + Doubles.join(",  ", (double[]) o) + "]";
			}
			else
			{
				return o.toString();
			}
		}
	};

	private static <T> boolean compare(T a, T b)
	{
		if (a instanceof Object[])
		{
			return Arrays.deepEquals((Object[]) a, (Object[]) b);
		}
		else
		{
			final Object[] box_a = new Object[] {
				a
			};
			final Object[] box_b = new Object[] {
				b
			};
			return Arrays.deepEquals(box_a, box_b);
		}
	}

	public static <T> void compareIterables(Iterable<T> expected, Iterable<T> actual)
	{
		boolean elementsEqual = true;
		final Iterator<T> it_expected = expected.iterator();
		final Iterator<T> it_actual = actual.iterator();
		String message = "equal elements";
		while (it_expected.hasNext() && it_actual.hasNext())
		{
			final T an_expected = it_expected.next();
			final T an_actual = it_actual.next();
			elementsEqual &= compare(an_expected, an_actual);
		}
		elementsEqual &= it_expected.hasNext() == it_actual.hasNext();
		if (!elementsEqual)
		{
			final String expected_string = Iterables.toString(Iterables.transform(expected, stringify));
			final String actual_string = Iterables.toString(Iterables.transform(actual, stringify));
			message = String.format("Exected \"%s\",  actual \"%s\"", expected_string, actual_string);
		}
		assertThat(message, elementsEqual, equalTo(true));
	}

}
