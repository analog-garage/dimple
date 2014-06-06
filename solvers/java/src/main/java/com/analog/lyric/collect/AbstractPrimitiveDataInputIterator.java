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
import java.util.NoSuchElementException;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Partial implementation of {@link PrimitiveIterator} based on a {@link DataInput} object.
 */
@ThreadSafe
public abstract class AbstractPrimitiveDataInputIterator<T> implements PrimitiveIterator<T>
{
	/*-------
	 * State
	 */
	
	/**
	 * The underlying source of primitive values.
	 */
	protected final DataInput _input;
	
	/**
	 * Enumerates the current state of the iterator:
	 * <dl>
	 * <dt>NOT_READ</dt><dd>Next value not yet read from input source</dd>
	 * <dt>READ</dt><dd>Next value is stored in a local field</dd>
	 * <dt>EOS</dt><dd>End of stream - no more values are available</dd>
	 * </dl>
	 */
	private static enum ReadState { NOT_READ, READ, EOS }
	
	/**
	 * The current state of the iterator.
	 */
	@GuardedBy("this")
	private ReadState _state = ReadState.NOT_READ;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct based on given underlying {@code input}.
	 */
	protected AbstractPrimitiveDataInputIterator(DataInput input)
	{
		_input = input;
	}
	
	/*------------------
	 * Iterator methods
	 */
	
	/**
	 * Indicates if another value is available.
	 * <p>
	 * @throws RuntimeException thrown by underlying {@link DataInput} object. Once this throws an exception subsequent
	 * calls will simply return false.
	 * <p>
	 * @return true if {@link #next()} (or its primitive variant)
	 * will return a value without throwing an exception.
	 */
	@Override
	public synchronized boolean hasNext()
	{
		boolean result = false;
		
		switch (_state)
		{
		case NOT_READ:
			try
			{
				_state = ReadState.EOS;
				result = readNext();
				if (result)
				{
					_state = ReadState.READ;
				}
			}
			catch (IOException ex)
			{
				result = false;
			}
			break;
		case READ:
			result = true;
			break;
		case EOS:
			result = false;
			break;
		}
		
		return result;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("PrimitiveIterator.remove");
	}
	
	/*--------------------------------------------
	 * AbstractPrimitiveDataInputIterator methods
	 */
	
	/**
	 * This should be invoked by subclass implementations of {@link #next()}
	 * or their primitive variant. This will cause the next call to {@link #hasNext()}
	 * to read next value using {@link #readNext()}.
	 * <p>
	 * @throws NoSuchElementException if {@link #hasNext()} returns false.
	 */
	protected void assertValueWasRead()
	{
		if (!hasNext())
		{
			throw new NoSuchElementException();
		}
		_state = ReadState.NOT_READ;
	}
	
	/**
	 * Implementations should read next value from {@link #_input} into a primitive field of
	 * the correct type.
	 * <p>
	 * @throws IOException
	 */
	protected abstract boolean readNext() throws IOException;
}
