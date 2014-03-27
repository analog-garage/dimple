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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * Maintains a double value along with an integer "stamp" that can be updated atomically.
 * <p>
 * This is a slightly more efficient alternative to {@code AtomicStampedReference<Double>}
 * and unlike that class is serializable.
 * 
 * @see AtomicStampedReference
 * @since 0.05
 */
@ThreadSafe
public class AtomicStampedDouble implements Serializable
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	@Immutable
	protected static class Pair implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final double _value;
		private final int _stamp;
		
		private Pair(double value, int stamp)
		{
			_value = value;
			_stamp = stamp;
		}
	}
	
	/**
	 * Holds the current value/stamp pair.
	 */
	protected final AtomicReference<Pair> _atomicRef;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs with given value and stamp.
	 */
	public AtomicStampedDouble(double value, int stamp)
	{
		_atomicRef = new AtomicReference<Pair>(new Pair(value, stamp));
	}
	
	/**
	 * Constructs with given value and stamp set to zero.
	 */
	public AtomicStampedDouble(double value)
	{
		this(value, 0);
	}
	
	/**
	 * Constructs with value and stamp both set to zero.
	 */
	public AtomicStampedDouble()
	{
		this(0,0);
	}
	
	/**
	 * Copies value and stamp from another instance.
	 */
	public AtomicStampedDouble(AtomicStampedDouble that)
	{
		_atomicRef = new AtomicReference<Pair>(that._atomicRef.get());
	}
	
	/*---------
	 * Methods
	 */
	
	/**
	 * Attempts to change the stamp if value matches {@code expectedValue}.
	 * 
	 * @return false if current value does not match {@code expectedValue} or if value
	 * or stamp is changed in another thread during this call.
	 * <p>
	 * @see #compareAndSet
	 */
	public final boolean attemptStamp(double expectedValue, int newStamp)
	{
		Pair pair = _atomicRef.get();
		return Double.doubleToRawLongBits(expectedValue) == Double.doubleToRawLongBits(pair._value) &&
			(newStamp == pair._stamp ||	_atomicRef.compareAndSet(pair, new Pair(expectedValue, newStamp)));
	}
	
	/**
	 * Attempts to change the value and stamp atomically if current values match old values.
	 * 
	 * @return false if current value or stamp does not match expected values or if
	 * value or stamp is changed in another thread during this call.
	 * <p>
	 * @see #attemptStamp
	 * @see #setAndIncrementStamp(double)
	 */
	public final boolean compareAndSet(double expectedValue, double newValue, int expectedStamp, int newStamp)
	{
		Pair pair = _atomicRef.get();
		return expectedStamp == pair._stamp &&
			Double.doubleToRawLongBits(expectedValue) == Double.doubleToRawLongBits(pair._value) &&
			((newValue == expectedValue && expectedStamp == newStamp) ||
				_atomicRef.compareAndSet(pair, new Pair(newValue, newStamp)));
	}
	
	/**
	 * Gets the current value.
	 */
	public final double get()
	{
		return _atomicRef.get()._value;
	}

	/**
	 * Returns the current value and writes into {@code stampHolder} the current stamp value.
	 * @param stampHolder must have lenght of at least one.
	 */
	public final double get(int[] stampHolder)
	{
		Pair pair = _atomicRef.get();
		stampHolder[0] = pair._stamp;
		return pair._value;
	}
	
	/**
	 * Returns the current stamp value.
	 */
	public final int getStamp()
	{
		return _atomicRef.get()._stamp;
	}
	
	/**
	 * Atomically sets the value and its associated stamp.
	 */
	public final void set(double value, int stamp)
	{
		Pair pair = _atomicRef.get();
		if (pair._value != value || pair._stamp != stamp)
		{
			_atomicRef.set(new Pair(value, stamp));
		}
	}
	
	/**
	 * Sets the value and atomically increments the stamp if the value changed.
	 * 
	 * @return false if the value was not set because it was set by another thread
	 * 
	 * @see #compareAndSet
	 */
	public final boolean setAndIncrementStamp(double value)
	{
		Pair pair = _atomicRef.get();
		
		if (Double.doubleToRawLongBits(value) != Double.doubleToRawLongBits(pair._value))
		{
			return _atomicRef.compareAndSet(pair, new Pair(value, pair._stamp + 1));
		}
		
		return true;
	}
	
}
