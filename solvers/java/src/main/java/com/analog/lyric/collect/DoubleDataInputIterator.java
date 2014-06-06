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

import java.io.DataInput;
import java.io.IOException;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * {@link PrimitiveIterator.OfDouble} implementation based on an underlying
 * {@link DataInput} object.
 * <p>
 * May not be thread safe if the underlying {@link DataInput} object is not.
 */
@ThreadSafe
public class DoubleDataInputIterator extends AbstractPrimitiveDataInputIterator<Double>
	implements PrimitiveIterator.OfDouble
{
	/*-------
	 * State
	 */
	
	@GuardedBy("this")
	private double _value;
	
	/*--------------
	 * Construction
	 */
	
	public DoubleDataInputIterator(DataInput input)
	{
		super(input);
	}

	/*---------------------------
	 * PrimitiveIterator methods
	 */
	
	@Override
	public final Double next()
	{
		return nextDouble();
	}

	@Override
	public synchronized double nextDouble()
	{
		assertValueWasRead();
		return _value;
	}

	@Override
	protected boolean readNext() throws IOException
	{
		_value = _input.readDouble();
		return true;
	}
}
