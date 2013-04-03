package com.analog.lyric.dimple.FactorFunctions.core;

import com.analog.lyric.dimple.model.DimpleException;

public class FactorFunctionUtilities
{
	public static final double [][] toDouble2DArray(Object value)
	{
		return (double[][])value;
	}

	public static final double [] toDoubleArray(Object value)
	{
		return (double[])value;
	}
	
	
	public static final double toDouble(Object value)
	{
		double out = 0;
    	if (value instanceof Double)
    		out = (Double)value;
    	else if (value instanceof Integer)
    		out = (Integer)value;
    	else if (value instanceof Boolean)
    		out = (Boolean)value ? 1 : 0;
    	else
    		throw new DimpleException("Invalid value type");
    	return out;
	}
	
	public static final int toInteger(Object value)
	{
		int out = 0;
    	if (value instanceof Double)
    		out = (int)Math.round((Double)value);
    	else if (value instanceof Integer)
    		out = (Integer)value;
    	else if (value instanceof Boolean)
    		out = (Boolean)value ? 1 : 0;
    	else
    		throw new DimpleException("Invalid value type");
    	return out;
	}
	
	public static final boolean toBoolean(Object value)
	{
		boolean out;
    	if (value instanceof Double)
    		out = (Math.round((Double)value) != 0);
    	else if (value instanceof Integer)
    		out = ((Integer)value != 0);
    	else if (value instanceof Boolean)
    		out = (Boolean)value;
    	else
    		throw new DimpleException("Invalid value type");
    	return out;
	}
	
	
	public static final int[] getListOfIndices(int startingIndex, int endingIndex)
	{
    	if (startingIndex > endingIndex)
    		throw new DimpleException("Insufficient number of indices");
    	int[] indices = new int[endingIndex - startingIndex + 1];
    	for (int i = 0, index = startingIndex; index <= endingIndex; i++, index++)
    		indices[i] = index;
		return indices;
	}

}
