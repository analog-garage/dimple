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

package com.analog.lyric.dimple.parameters;

import java.util.concurrent.atomic.AtomicIntegerArray;

import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;
import com.google.common.util.concurrent.AtomicDoubleArray;

@ThreadSafe
public abstract class ParameterListN<Key extends IParameterKey>
	extends AbstractParameterList<Key>
{
	private static final long serialVersionUID = 1L;
	
	protected final AtomicDoubleArray _values;
	protected final AtomicIntegerArray _fixedMask;
	
	/*--------------
	 * Construction
	 */
	
	protected ParameterListN(ParameterListN<Key> that)
	{
		AtomicDoubleArray thoseValues = that._values;
		int size = thoseValues.length();
		_values = new AtomicDoubleArray(size);
		for (int i = 0; i < size; ++i)
		{
			_values.set(i, thoseValues.get(i));
		}
		
		AtomicIntegerArray thatFixedMask = that._fixedMask;
		int fixedMaskSize = thatFixedMask.length();
		_fixedMask = new AtomicIntegerArray(fixedMaskSize);
		for (int i = 0; i < fixedMaskSize; ++i)
		{
			_fixedMask.set(i, thatFixedMask.get(i));
		}
	}
	
	protected ParameterListN(double ... values)
	{
		_values = new AtomicDoubleArray(values);
		
		int maskSize = maskSize(values.length);
		_fixedMask = new AtomicIntegerArray(maskSize);
	}
	
	protected ParameterListN(boolean fixed, double ... values)
	{
		this(values);
		if (fixed)
		{
			setAllFixed(fixed);
		}
	}
	
	protected ParameterListN(int size, double defaultValue)
	{
		_values = new AtomicDoubleArray(size);
		for (int i = 0; i < size; ++i)
		{
			_values.set(i, defaultValue);
		}
		
		_fixedMask = new AtomicIntegerArray(maskSize(size));
	}
	
	protected ParameterListN(int size)
	{
		this(size, Double.NaN);
	}
	
	@Override
	public abstract ParameterListN<Key> clone();
	
	/*------------------------------
	 * IFactorParameterList methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation does not support individual parameter sharing and will return false.
	 */
	@Override
	public boolean canShare()
	{
		return false;
	}
	
	@Override
	public final double get(int index)
	{
		return _values.get(index);
	}
	
	@Override
	public @Nullable SharedParameterValue getSharedValue(int index)
	{
		assertIndexInRange(index);
		return null;
	}

	@Override
	public final boolean isFixed(int index)
	{
		assertIndexInRange(index);
		int bit = 1 << (index & 31);
		int arrayIndex = index >>> 5;
		
		return (_fixedMask.get(arrayIndex) & bit) != 0;
	}
	
	@Override
	public boolean isShared(int index)
	{
		assertIndexInRange(index);
		return false;
	}
	
	@Override
	public void set(int index, double value)
	{
		assertNotFixed(index);
		_values.set(index, value);
		valueChanged(index);
	}

	@Override
	public final void setAllFixed(boolean fixed)
	{
		int mask = fixed ? -1 : 0;
		for (int i = 0, end = _fixedMask.length(); i < end; ++i)
		{
			_fixedMask.set(i, mask);
		}
	}
	
	@Override
	public final void setFixed(int index, boolean fixed)
	{
		final int bit = 1 << (index & 31);
		final int arrayIndex = index >>> 5;
		
		// Keep trying until bit is set correctly without changing any other bit.
		while (true)
		{
			int prevMask = _fixedMask.get(arrayIndex);
			int newMask = fixed ? prevMask|bit : prevMask&~bit;
			if (prevMask == newMask || _fixedMask.compareAndSet(arrayIndex, prevMask, newMask))
			{
				break;
			}
		}
	}
	
	@Override
	public final void setShared(int index, boolean shared)
	{
		if (shared)
		{
			throw new UnsupportedOperationException(
				String.format("%s does no support shared parameter values.", getClass().getName()));
		}
	}
	
	@Override
	public final void setSharedValue(int index, @Nullable SharedParameterValue value)
	{
		setShared(index, value != null);
	}

	@Override
	public final int size()
	{
		return _values.length();
	}
	
	/*-----------------
	 * Private methods
	 */

	private static int maskSize(int size)
	{
		return (size + 31) / 32;
	}
}
