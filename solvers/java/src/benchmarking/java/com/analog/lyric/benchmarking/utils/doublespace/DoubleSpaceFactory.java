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

package com.analog.lyric.benchmarking.utils.doublespace;

import com.analog.lyric.benchmarking.utils.functional.GeneratorFunction;

/**
 * Methods for creating DoubleSpace objects.
 */
public class DoubleSpaceFactory
{

	/**
	 * Constructs a DoubleSpace whose entries are copied from a supplied
	 * DoubleSpace.
	 * 
	 * @param s
	 *            The space to copy.
	 */
	public static DoubleSpace copy(DoubleSpace s)
	{
		return new BottomDoubleSpace(s);
	}

	/**
	 * Constructs a DoubleSpace with given dimensions.
	 * 
	 * @param dimensions
	 *            The dimensions of the desired space.
	 */
	public static DoubleSpace create(int... dimensions)
	{
		return new BottomDoubleSpace(dimensions);
	}

	/**
	 * Generates a BottomDoubleSpace given a generator function and dimensions.
	 * The generator function is applied to each tuple of the domain of the
	 * space, and the generated values are stored.
	 * 
	 * @param generator
	 *            The generator function.
	 * @param dims
	 *            The dimensions of the desired space.
	 * @return The generated DoubleSpace.
	 */
	public static DoubleSpace generate(GeneratorFunction generator, int... dims)
	{
		final DoubleSpace result = create(dims);
		for (final int[] coordinates : result.coordinatesSet())
		{
			result.put(generator.apply(coordinates), coordinates);
		}
		return result;
	}

}
