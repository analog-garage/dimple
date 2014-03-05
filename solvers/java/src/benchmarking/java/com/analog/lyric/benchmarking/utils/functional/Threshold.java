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

/**
 * Implements a transformation that sets values below a threshold to 0.0, and
 * those above or equal to it to 1.0.
 */
public class Threshold implements TransformFunction
{
	private final double _threshold;

	/**
	 * Constructs the Threshold transformer.
	 * 
	 * @param threshold
	 *            The threshold value.
	 */
	public Threshold(double threshold)
	{
		_threshold = threshold;
	}

	@Override
	public double apply(double v)
	{
		return v >= _threshold ? 1.0 : 0.0;
	}
}
