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

package com.analog.lyric.benchmarking.utils.functional;

import com.analog.lyric.benchmarking.utils.doublespace.DoubleSpace;

/**
 * Methods to perform various functional operations with DoubleSpace objects.
 */
public class Functions
{

	/**
	 * Addition binary operation.
	 */
	public static BinaryOp add = new BinaryOp()
	{

		@Override
		public double apply(double a, double b)
		{
			return a + b;
		}

	};

	/**
	 * Compose two transform operations.
	 * 
	 * @param g
	 *            The second transform to apply..
	 * @param f
	 *            The first transform to apply.
	 * @return A transform that composes the supplied transforms.
	 */
	public static TransformFunction compose(final TransformFunction g, final TransformFunction f)
	{
		return new TransformFunction()
		{
			@Override
			public double apply(double x)
			{
				return g.apply(f.apply(x));
			}
		};
	}

	/**
	 * Returns the maximum double in a DoubleSpace.
	 */
	public static double max(DoubleSpace s)
	{
		final Double max = s.fold(Double.MIN_VALUE, new FoldFunction<Double>()
		{
			@Override
			public Double apply(Double acc, double value)
			{
				if (acc > value)
				{
					return acc;
				}
				else
				{
					return value;
				}
			}
		});
		return max;
	}

	/**
	 * Returns the minimum double in a DoubleSpace.
	 */
	public static double min(DoubleSpace s)
	{
		final Double min = s.fold(Double.MAX_VALUE, new FoldFunction<Double>()
		{
			@Override
			public Double apply(Double acc, double value)
			{
				if (acc < value)
				{
					return acc;
				}
				else
				{
					return value;
				}
			}
		});
		return min;
	}

	/**
	 * Normalizes the values within a DoubleSpace so that the minimum value maps
	 * to 0.0 and the maximum value maps to 1.0.
	 * 
	 * @return Returns the supplied DoubleSpace, but with its values normalized.
	 */
	public static DoubleSpace normalize(DoubleSpace s)
	{
		final double min = min(s);
		final double max = max(s);
		final double scale = max - min;
		return s.transform(new TransformFunction()
		{

			@Override
			public double apply(double v)
			{
				return (v - min) / scale;
			}
		});
	}
}
