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

import static com.analog.lyric.collect.Selection.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

/**
 * Tests for {@code Selection} class
 * <p>
 * @since 0.06
 * @author CBarber
 */
public class TestSelection
{
	private Random _rand = new Random(42);

	@Test
	public void test()
	{
		final int iterations = 5;
		
		for (int i = 0; i < iterations; ++i)
		{
			double[] array = randomArray();
			testCase(array);
		}
		
	}
	
	private void testCase(double[] array)
	{
		double[] unsortedArray = array.clone();
		double[] sortedArray = array.clone();
		Arrays.sort(sortedArray);
		
		Double[] unsortedArray2 = new Double[array.length];
		for (int i = array.length; --i>=0;)
		{
			unsortedArray2[i] = unsortedArray[i];
		}
		
		for (int k = 1; k <= array.length; ++k)
		{
			double expected = sortedArray[k-1];
			assertEquals(expected, select(unsortedArray, k-1), 0.0);
			assertEquals(expected, select(unsortedArray2, k-1), 0.0);
			
			int[] indices = quickfindFirstKindices(unsortedArray, k);
			Arrays.sort(indices);
			assertEquals(k, indices.length);
			for (int i = 0; i < indices.length; ++i)
			{
				if (i > 0)
				{
					assertTrue(indices[i] > indices[i-1]);
				}
				final int index = indices[i];
				assertTrue(unsortedArray[index] <= expected);
			}

			expected = sortedArray[sortedArray.length - k];
			indices = quickfindLastKindices(unsortedArray, k);
			Arrays.sort(indices);
			assertEquals(k, indices.length);
			for (int i = 0; i < indices.length; ++i)
			{
				if (i > 0)
				{
					assertTrue(indices[i] > indices[i-1]);
				}
				final int index = indices[i];
				assertTrue(unsortedArray[index] >= expected);
			}
		}
		
		// Make sure that array was not mutated.
		assertArrayEquals(array, unsortedArray, 0.0);
	}
	
	private double[] randomArray()
	{
		final int n = _rand.nextInt(99) + 1;
		final double[] array = new double[n];
		for (int i = n; --i >= 0;)
		{
			array[i] = _rand.nextDouble();
		}
		return array;
	}
}
