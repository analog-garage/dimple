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

package com.analog.lyric.dimple.options;

import java.util.Arrays;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.events.EventSourceIterator;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.LocalOptionHolder;
import com.analog.lyric.options.OptionDoubleList;
import com.analog.lyric.util.misc.Nullable;

/**
 * Base class for dimple objects that can hold options and generate events.
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public abstract class DimpleOptionHolder extends LocalOptionHolder implements IDimpleEventSource
{
	/*-----------------------
	 * IOptionHolder methods
	 */
	
	@Override
	public ReleasableIterator<IDimpleEventSource> getOptionDelegates()
	{
		return EventSourceIterator.create(this);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default, the option parent is the same as the {@linkplain #getEventParent event parent}.
	 */
	@Override
	public @Nullable IOptionHolder getOptionParent()
	{
		return getEventParent();
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	/**
	 * Returns list of not all zero doubles from option settings.
	 * <p>
	 * <ol>
	 * <li>Look up the option value for {@code listKey} using {@link #getOption}:
	 * <ul>
	 * <li>If non-null and contains at least one non-zero value, it's values will be returned.
	 * <li>If non-null and is empty or contains all zeros, an empty array will be returned.
	 * </ul>
	 * <li>Otherwise, look up the option value for {@code singleKey} using {@link #getOptionOrDefault}:
	 * <ul>
	 * <li>If zero, an empty array will be returned.
	 * <li>If non-zero, an array of length {@code size} containing only this value will be returned.
	 * </ul>
	 * </ol>
	 * <p>
	 * This can be used in situations in which there is a global parameter, such as damping, that
	 * may be given specific values for different edges of a node.
	 * <p>
	 * @param listKey is the primary lookup key for the list.
	 * @param singleKey is a secondary lookup key to be used if {@code listKey} not set.
	 * @param size is the size of list to return when replicating value from {@code singleKey}.
	 * @param array is the array into which the result should be written. This will only be used
	 * if it is non-null and of exactly the correct length.
	 * @since 0.07
	 */
	protected double[] getReplicatedNonZeroListFromOptions(
		IOptionKey<OptionDoubleList> listKey,
		IOptionKey<Double> singleKey,
		int size,
		@Nullable double[] array)
	{
    	OptionDoubleList list = getOption(listKey);
    	if (list != null)
    	{
    		final int listSize = list.size();
    		boolean hasNonZero = false;
    		for (int i = 0; i < listSize; ++i)
    		{
    			if (list.get(i) != 0.0)
    			{
    				hasNonZero = true;
    				break;
    			}
    		}
    		if (!hasNonZero)
    		{
    			return ArrayUtil.EMPTY_DOUBLE_ARRAY;
    		}
    		if (array == null || array.length != listSize)
    		{
    			array = new double[listSize];
    		}
    		for (int i = 0; i < listSize; ++i)
    		{
    			array[i] = list.get(i);
    		}
    	}
    	else
    	{
    		double d = getOptionOrDefault(singleKey);
    		if (d == 0.0)
    		{
    			return ArrayUtil.EMPTY_DOUBLE_ARRAY;
    		}
    		if (array == null || array.length != size)
    		{
    			array = new double[size];
    		}
    		Arrays.fill(array, d);
    	}
    	
    	return array;
    }
}
