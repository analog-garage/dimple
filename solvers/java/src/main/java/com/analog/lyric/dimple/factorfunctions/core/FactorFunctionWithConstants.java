/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;

import com.analog.lyric.dimple.model.DimpleException;


public class FactorFunctionWithConstants extends FactorFunction 
{
	private FactorFunctionBase _factorFunction;
	private Object[] _constants;
	private int[] _constantIndices;
	
	public FactorFunctionWithConstants(FactorFunctionBase factorFunction,
			Object[] constants, int[] constantIndices) 
	{
		super(factorFunction.getName());
		_factorFunction = factorFunction;
		_constants = constants;
		_constantIndices = constantIndices;
		
		if (_constantIndices.length > 1)
			for (int i = 1; i < _constantIndices.length; i++)
			{
				if (_constantIndices[i] <= _constantIndices[i-1])
					throw new DimpleException("constants must be provided in ascending index order");
			}
		
		if (_constantIndices.length != _constants.length)
			throw new DimpleException("need to specify the constants and their locations");
	}
	

	public Object[] getConstants()
	{
		return _constants;
	}
	
	public int[] getConstantIndices()
	{
		return _constantIndices;
	}
	
	public boolean isConstantIndex(int index)
	{
		for (int i = 0; i < _constantIndices.length; i++)
			if (_constantIndices[i] == index)
				return true;
		return false;
	}
	
	public Object getConstantByIndex(int index)
	{
		for (int i = 0; i < _constantIndices.length; i++)
			if (_constantIndices[i] == index)
				return _constants[i];
		return null;
	}
	

	// Wrap the methods of the actual factor function...

	@Override
	public double evalEnergy(Object... arguments)
	{		
		return _factorFunction.evalEnergy(expandInputList(arguments));
	}
	
	@Override
	public boolean isDirected()
	{
		return _factorFunction.isDirected();
	}
	
	@Override
	public int[] getDirectedToIndices(int numEdges)
	{
		// Add the constants to the total number of edges
		int[] directedToIndices = _factorFunction.getDirectedToIndices(numEdges + _constantIndices.length);
		return contractIndexList(directedToIndices);	// Remove the constant indices
	}
	
	@Override
	public int[] getDirectedToIndices()
	{
		int[] directedToIndices = _factorFunction.getDirectedToIndices();
		return contractIndexList(directedToIndices);	// Remove the constant indices
	}

	@Override
	public boolean isDeterministicDirected()
	{
		return _factorFunction.isDeterministicDirected();
	}
	
	@Override
	public void evalDeterministicFunction(Object... arguments)
	{
		Object[] expandedArgumentList = expandInputList(arguments);
		_factorFunction.evalDeterministicFunction(expandedArgumentList);
		
		// Replace the original argument list entries, leaving out constant indices
		int numExpandedArguments = expandedArgumentList.length;
		int numConsts = _constantIndices.length;
		for (int iExp = 0, iOrig = 0, iConst = 0; iExp < numExpandedArguments; iExp++)
		{
			if (iExp != ((iConst >= numConsts) ? -1 : _constantIndices[iConst]))
				arguments[iOrig++] = expandedArgumentList[iExp];
			else
				iConst++;
		}
	}
	
	
	
	
	// Expand list of inputs to include the constants
	// Assumes constant index list is already sorted
	protected Object[] expandInputList(Object... input)
	{
		Object [] expandedInputs = new Object[input.length + _constantIndices.length];
		
		int curConstantIndexIndex = 0;
		int curInputIndex = 0;
		
		for (int i = 0; i < expandedInputs.length; i++)
		{
			if (curConstantIndexIndex < _constantIndices.length && _constantIndices[curConstantIndexIndex] == i)
			{
				//insert constant
				expandedInputs[i] = _constants[curConstantIndexIndex];
				curConstantIndexIndex++;
			}
			else
			{
				if (curInputIndex >= input.length)
					throw new DimpleException("incorrect number of arguments");
				
				expandedInputs[i] = input[curInputIndex];
				curInputIndex++;
			}
		}
		
		return expandedInputs;
	}
	

	// Contract a list of indices to exclude the constant indices and renumber the others accordingly
	protected int[] contractIndexList(int[] indexList)
	{
		int originalLength = indexList.length;
		int numConstantIndices = _constantIndices.length;
		Arrays.sort(indexList);		// Side effect of sorting indexList, but ok in this context
		
		// For each constant index, scan the list (probably a more efficient way to do this)
		// Assumes constant index list is already sorted
		ArrayList<Integer> contractedList = new ArrayList<Integer>();
		int iConst = 0;
		int iList = 0;
		int listIndex;
		int constantIndex = _constantIndices[iConst];
		while (iList < originalLength)
		{
			listIndex = indexList[iList];
			if (iConst < numConstantIndices)
				constantIndex = _constantIndices[iConst];
			if (listIndex == constantIndex)
			{
				// Skip this list index entry
				iList++;
			}
			else if (listIndex < constantIndex || iConst >= numConstantIndices)
			{
				// Add this entry
				contractedList.add(listIndex - iConst);
				iList++;
			}
			else if (listIndex > constantIndex)
			{
				// Move to the next constant if there is one
				iConst++;
			}
		}
		
		// Convert contracted list back to an int[]
		int contractListSize = contractedList.size();
		int[] result = new int[contractListSize];
		for (int i = 0; i < contractListSize; i++)
			result[i] = contractedList.get(i);
		return result;
	}
	
	
	// Get the contained factor function
	public FactorFunctionBase getContainedFactorFunction()
	{
		return _factorFunction;
	}

}
