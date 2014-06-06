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


public class DoubleArrayIterable
	extends AbstractPrimitiveArrayIterable<Double>
	implements PrimitiveIterable.OfDouble
{
	protected final double[] _array;
	
	public DoubleArrayIterable(double[] array, int start, int end)
	{
		super(start, end);
		assert(end <= array.length);
		_array = array;
	}
	
	public DoubleArrayIterable(double[] array)
	{
		this(array, 0, array.length);
	}

	@Override
	public DoubleArrayIterator iterator()
	{
		return new DoubleArrayIterator(_array, _start, _end);
	}
}
