/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.model.values.Value;

/**
 * Holds prior and condition values for a solver variable.
 * <p>
 * This is primarily intended for internal use in solver variable implementations.
 * <p>
 * Note that {@link #release()} can be used to return instance for reuse.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 * @see SVariableBase#getPriorAndCondition
 */
public final class PriorAndCondition extends AbstractList<IDatum>
{
	/*------
	 * State
	 */

	/**
	 * Array holding prior in position 0 and condition in position 1.
	 */
	private final IDatum[] _data = new IDatum[2];
	
	/**
	 * Bit mask indicating positions of non-null elements.
	 */
	private int _mask;
	
	private static final AtomicReference<PriorAndCondition> _reusableInstance = new AtomicReference<>();

	/*---------------
	 * Construction
	 */

	private PriorAndCondition()
	{
	}

	/**
	 * Returns an instance with given prior and condition values.
	 * <p>
	 * If the object is to be used within a single function call, consider
	 * {@linkplain #release releasing} it for reuse by the next caller.
	 * <p>
	 * Typically it is easier to use the {@linkplain SVariableBase#getPriorAndCondition() getPriorAndCondition()}
	 * method when working with solver variable instances.
	 * <p>
	 * @since 0.08
	 */
	public static PriorAndCondition create(@Nullable IDatum prior, @Nullable IDatum condition)
	{
		PriorAndCondition instance = _reusableInstance.getAndSet(null);
		if (instance == null)
		{
			instance = new PriorAndCondition();
		}
		instance._data[0] = prior;
		instance._data[1] = condition;
		instance._mask = (prior == null ? 0 : 1) | (condition == null ? 0 : 1) << 1;
		return instance;
	}
	
	/**
	 * Returns this instance for reuse.
	 * <p>
	 * Returns instance so that it may be returned by the next call to {@link #create}.
	 * This will avoid having to allocate a new instance.
	 * <p>
	 * @since 0.08
	 * @return null (can be used to assign back to variable to prevent further use)
	 */
	public @Nullable PriorAndCondition release()
	{
		Arrays.fill(_data, null);
		_mask = -2; // size will be -1
		_reusableInstance.set(this);
		return null;
	}
	
	/*--------------
	 * List methods
	 */
	
	@Override
	public IDatum get(int index)
	{
		// Flip the low bit of the mask to give an offset of 0 or 1.
		// This means that this may return a null rather than throwing an out of bounds exception.
		return _data[(1&_mask^1) + index];
	}
	
	@Override
	public int size()
	{
		// This only works because _mask only can have its lower two bits set.
		return _mask + 1 >> 1;
	}

	/*---------------
	 * Local methods
	 */
	
	/**
	 * Evaluates energy of prior and condition for given value.
	 * <p>
	 * This is the sum of the energies for the passed in {@code value} for the
	 * {@link #prior()} and {@link #condition()}, except that if the prior is a
	 * {@link Value} instance, the condition will not be used (this mimics the behavior
	 * of {@link com.analog.lyric.dimple.data.DataStack#computeTotalEnergy() DataStack.computeTotalEnergy})
	 * <p>
	 * @since 0.08
	 */
	public double evalEnergy(Value value)
	{
		double energy = 0.0;
		for (IDatum datum : _data)
		{
			if (datum != null)
			{
				energy += datum.evalEnergy(value);
				if (datum instanceof Value)
				{
					break;
				}
			}
		}
		return energy;
	}
	
	public @Nullable IDatum prior()
	{
		return _data[0];
	}
	
	public @Nullable IDatum condition()
	{
		return _data[1];
	}
	
	/**
	 * Returns first of {@link #prior} or {@link #condition} that is a {@link Value} instance.
	 * @since 0.08
	 */
	public @Nullable Value value()
	{
		for (IDatum datum : _data)
		{
			if (datum instanceof Value)
			{
				return (Value)datum;
			}
		}
		
		return null;
	}
}
