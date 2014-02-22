/*******************************************************************************
*   Copyright 2012-2013 Analog Devices, Inc.
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
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.util.misc.Internal;


public class FactorFunctionWithConstants extends FactorFunction
{
	private FactorFunction _factorFunction;
	private Object [] _constants;
	private Value[] _constantValues;
	private int [] _constantIndices;
	
	@Internal
	public FactorFunctionWithConstants(FactorFunction factorFunction,
			Object [] constants, int [] constantIndices)
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

		_constantValues = new Value[_constants.length];
		for (int i = _constantValues.length; --i>=0;)
		{
			_constantValues[i] = Value.create(_constants[i]);
		}
	}
	

	@Override
	public int getConstantCount()
	{
		return _constants.length;
	}
	
	public Object [] getConstants()
	{
		return _constants;
	}
	
	public int [] getConstantIndices()
	{
		return _constantIndices;
	}
	
	public boolean isConstantIndex(int index)
	{
		return Arrays.binarySearch(_constantIndices,  index) >= 0;
	}
	
	@Override
	public Object getConstantByIndex(int index)
	{
		final int i = Arrays.binarySearch(_constantIndices,  index);
		return i >= 0 ? _constants[i] : null;
	}

	// Wrap the methods of the actual factor function...

	@Override
	public double evalEnergy(Object... arguments)
	{
		return _factorFunction.evalEnergy(expandInputList(arguments));
	}
	
	@Override
	public double evalEnergy(Value[] values)
	{
		return _factorFunction.evalEnergy(expandValues(values));
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
	public int[] getDirectedToIndicesForInput(Factor factor, int inputEdge)
	{
		int[] directedToIndices = _factorFunction.getDirectedToIndicesForInput(factor, expandInputEdge(inputEdge));
		if (directedToIndices != null)
		{
			directedToIndices = contractIndexList(directedToIndices);
		}
		return directedToIndices;
	}
	
	@Override
	public boolean isDeterministicDirected()
	{
		return _factorFunction.isDeterministicDirected();
	}
	
	@Override
	public void evalDeterministic(Object[] arguments)
	{
		Object[] expandedArgumentList = expandInputList(arguments);
		_factorFunction.evalDeterministic(expandedArgumentList);
		
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
	
	@Override
	public int updateDeterministicLimit(int numEdges)
	{
		return _factorFunction.updateDeterministicLimit(numEdges);
	}
	
	@Override
	public boolean updateDeterministic(Value[] values, Collection<IndexedValue> oldValues, AtomicReference<int[]> changedOutputsHolder)
	{
		int constantLength = _constantIndices.length;
		Value[] expandedValues = expandValues(values);

		// Adjust indexes of old values to account for inserted constants.
		ArrayList<IndexedValue> expandedOldValues = new ArrayList<IndexedValue>(oldValues);
		Collections.sort(expandedOldValues);
		
		for (int i = 0, ci = 0, offset = 0, endi = expandedOldValues.size(); i < endi; ++i)
		{
			IndexedValue oldValue = expandedOldValues.get(i);
			int oldIndex = oldValue.getIndex();
			for(; ci < constantLength && oldIndex >= _constantIndices[ci]; ++ci)
			{
				++offset;
			}
			if (offset > 0)
			{
				expandedOldValues.set(i, new IndexedValue(oldIndex + offset, oldValue.getValue()));
			}
		}
		
		boolean incremental =
			_factorFunction.updateDeterministic(expandedValues,  expandedOldValues, changedOutputsHolder);
		
		int[] changedOutputs = changedOutputsHolder.get();
		if (changedOutputs != null)
		{
			final int lowestConstantIndex = _constantIndices[0];
			
			// Need to adjust the output indexes if they come after the constant indexes.
			for (int oi = changedOutputs.length; --oi >= 0;)
			{
				int outputIndex = changedOutputs[oi];
				if (outputIndex >= lowestConstantIndex)
				{
					int offset = Arrays.binarySearch(_constantIndices, outputIndex);
					if (offset < 0)
					{
						offset = -1 - offset;
					}
					changedOutputs[oi] -= offset;
				}
			}
		}
		
		return incremental;
	}
	
	protected int expandInputEdge(int inputEdge)
	{
		final int[] constantIndices = _constantIndices;
		final int constantLength = constantIndices.length;

		int low = 0, high = constantLength;
		while (low < high)
		{
			final int mid = (high - low) / 2;
			
			// The offset at which the constant would be inserted in the
			// contracted version of list. Equal to the constant offset
			// in the expanded list minus its position in the list.
			final int insertPoint = constantIndices[mid] - mid;
			
			if (inputEdge >= insertPoint)
			{
				low = mid + 1;
			}
			else
			{
				high = mid;
			}
		}
		
		return inputEdge + low;
	}
	
	// Expand list of inputs to include the constants
	// Assumes constant index list is already sorted
	protected Object[] expandInputList(Object... input)
	{
		int inputLength = input.length;
		int constantLength = _constantIndices.length;
		int expandedLength = inputLength + constantLength;
		Object[] expandedInputs = new Object[expandedLength];
		
		int ei = 0, vi = 0;
		for (int ci = 0; ci < constantLength; ++ ci)
		{
			final int constantIndex = _constantIndices[ci];
			final int nonConstantLength = constantIndex - ei;
			System.arraycopy(input, vi, expandedInputs, ei, nonConstantLength);
			vi += nonConstantLength;
			ei = constantIndex + 1;
			expandedInputs[constantIndex] = _constants[ci];
		}
		System.arraycopy(input, vi, expandedInputs, ei, inputLength - vi);
		
		return expandedInputs;
	}
	
	protected Value[] expandValues(Value[] values)
	{
		int inputLength = values.length;
		int constantLength = _constantIndices.length;
		int expandedLength = inputLength + constantLength;
		Value[] expandedValues = new Value[expandedLength];
		
		int ei = 0, vi = 0;
		for (int ci = 0; ci < constantLength; ++ ci)
		{
			final int constantIndex = _constantIndices[ci];
			final int nonConstantLength = constantIndex - ei;
			System.arraycopy(values, vi, expandedValues, ei, nonConstantLength);
			vi += nonConstantLength;
			ei = constantIndex + 1;
			expandedValues[constantIndex] = _constantValues[ci];
		}
		System.arraycopy(values, vi, expandedValues, ei, inputLength - vi);
		
		return expandedValues;
	}

	/**
	 *  Contract a list of indices to exclude the constant indices and renumber the others accordingly.
	 */
	public int[] contractIndexList(int[] indexList)
	{
		Arrays.sort(indexList);
		return ArrayUtil.contractSortedIndexList(indexList, _constantIndices);
	}
	
	// Get the contained factor function
	public FactorFunction getContainedFactorFunction()
	{
		return _factorFunction;
	}

}
