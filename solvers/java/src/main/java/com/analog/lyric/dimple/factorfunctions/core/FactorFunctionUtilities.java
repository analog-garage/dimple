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

package com.analog.lyric.dimple.factorfunctions.core;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.google.common.math.DoubleMath;
import com.google.common.primitives.Primitives;

public class FactorFunctionUtilities
{
	public static final double [][] toDouble2DArray(@Nullable Object value)
	{
		return (double[][])Objects.requireNonNull(value);
	}

	public static final double[] toDoubleArray(@Nullable Object value)
	{
		if (value instanceof double[])
			return (double[])value;
		else if (value instanceof int[])
		{
			int[] vIn = (int[])value;
			double[] vOut = new double[vIn.length];
			for (int i = 0; i < vIn.length; i++)
				vOut[i] = vIn[i];
			return vOut;
		}
		else
			throw new DimpleException("Invalid input type");
	}
	
	public static final int[] toIntArray(@Nullable Object value)
	{
		if (value instanceof int[])
			return (int[])value;
		else if (value instanceof double[])
		{
			double[] vIn = (double[])value;
			int[] vOut = new int[vIn.length];
			for (int i = 0; i < vIn.length; i++)
				vOut[i] = (int)vIn[i];
			return vOut;
		}
		else
			throw new DimpleException("Invalid input type");
	}

	
	public static final double toDouble(@Nullable Object value)
	{
		double out = 0;
    	if (value instanceof Number)
    	{
    		out = ((Number)value).doubleValue();
    	}
    	else if (value instanceof Boolean)
    	{
    		out = (Boolean)value ? 1 : 0;
    	}
    	else
    		throw new DimpleException("Invalid value type '%s'", value != null ? value.getClass() : "null");
    	return out;
	}
	
	public static final int toInteger(@Nullable Object value)
	{
		int out = 0;
    	if (value instanceof Number)
    	{
    		double d = ((Number)value).doubleValue();
    		out = DoubleMath.isMathematicalInteger(d) ? (int)d : (int)Math.round(d);
    	}
    	else if (value instanceof Boolean)
    	{
    		out = (Boolean)value ? 1 : 0;
    	}
    	else
    		throw new DimpleException("Invalid value type");
    	return out;
	}
	
	public static final boolean toBoolean(@Nullable Object value)
	{
		boolean out;
    	if (value instanceof Number)
    	{
    		out = ((Number)value).doubleValue() != 0.0;
    	}
    	else if (value instanceof Boolean)
    	{
    		out = (Boolean)value;
    	}
    	else
    		throw new DimpleException("Invalid value type");
    	return out;
	}
	
	/**
	 * Converts boolean {@code value} to specified {@code toClass},
	 * which must be a primitive type.
	 */
	public static Object booleanToClass(boolean value, Class<?> toClass)
	{
		toClass = Primitives.wrap(toClass);
		if (toClass == Boolean.class)
		{
			return value;
		}
		
		byte intValue = value ? (byte)1 : (byte)0;
			
		if (toClass == Integer.class)
		{
			return Integer.valueOf(intValue);
		}
		else if (toClass == Double.class)
		{
			return Double.valueOf(intValue);
		}
		else if (toClass == Long.class)
		{
			return Long.valueOf(intValue);
		}
		else if (toClass == Float.class)
		{
			return Float.valueOf(intValue);
	}
		else if (toClass == Short.class)
		{
			return Short.valueOf(intValue);
		}
		else if (toClass == Byte.class)
		{
			return Byte.valueOf(intValue);
		}
		else if (toClass == Character.class)
		{
			return Character.valueOf((char)intValue);
		}
		else
		{
			throw new DimpleException("Invalid type");
		}
	}
	
	/**
	 * Returns list of integer indexes from {@code startingIndex} to {@code endingIndex} inclusive.
	 * <p>
	 * If {@code startingIndex} is greater than {@code endingIndex}, this returns an empty
	 * array.
	 */
	public static final int[] getListOfIndices(int startingIndex, int endingIndex)
	{
		final int size = endingIndex - startingIndex + 1;
		if (size <= 0)
		{
			return ArrayUtil.EMPTY_INT_ARRAY;
		}
		
    	int[] indices = new int[size];
    	for (int i = 0, index = startingIndex; index <= endingIndex; i++, index++)
    		indices[i] = index;
		return indices;
	}

}
