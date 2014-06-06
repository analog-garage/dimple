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


/**
 * Iterables supporting unboxed iteration of primitive types.
 */
public interface PrimitiveIterable<T> extends Iterable<T>
{
	@Override
	public PrimitiveIterator<T> iterator();
	
	public static interface OfDouble extends PrimitiveIterable<Double>
	{
		// FIXME: I don't think this @NotNull should be necessary? JDT bug?
		@Override
		public PrimitiveIterator.OfDouble iterator();
	}

	public static interface OfFloat extends PrimitiveIterable<Float>
	{
		@Override
		public PrimitiveIterator.OfFloat iterator();
	}
	
	public static interface OfInt extends PrimitiveIterable<Integer>
	{
		@Override
		public PrimitiveIterator.OfInt iterator();
	}
	
	public static interface OfLong extends PrimitiveIterable<Long>
	{
		@Override
		public PrimitiveIterator.OfLong iterator();
	}
}
