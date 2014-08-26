/*******************************************************************************
*   Copyright 2012-2014 Analog Devices, Inc.
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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.util.misc.Internal;


public class FactorFunctionWithConstants extends FactorFunction
{
	private FactorFunction _factorFunction;
	private Object[] _constants;
	private Value[] _constantValues;
	private int[] _constantIndices;
	private int[] _indexToEdgeOrConstant;
	private int[] _edgeToIndex;
	private int _largestConstantIndex;
	private int _smallestConstantIndex;
	public final static int NO_PORT = -1;
	
	@Internal
	public FactorFunctionWithConstants(FactorFunction factorFunction, Object[] constants, int[] constantIndices)
	{
		super(factorFunction.getName());
		_factorFunction = factorFunction;
		_constants = constants;
		_constantIndices = constantIndices;
		int numConstants = _constants.length;
		
		if (_constantIndices.length != _constants.length)
			throw new DimpleException("need to specify the constants and their locations");

		if (numConstants > 1)
		{
			for (int i = 1; i < numConstants; i++)
			{
				if (_constantIndices[i] <= _constantIndices[i-1])
					throw new DimpleException("constants must be provided in ascending index order");
			}
		}

		_constantValues = new Value[numConstants];
		for (int i = numConstants; --i>=0;)
		{
			_constantValues[i] = Value.create(_constants[i]);
		}
		
		// Map edges to indices, where an edge is an actually attached variable that isn't a constant
		_smallestConstantIndex = _constantIndices[0];
		_largestConstantIndex = _constantIndices[numConstants-1];
		_indexToEdgeOrConstant = new int[_largestConstantIndex - _smallestConstantIndex + 1];
		_edgeToIndex = new int[(_largestConstantIndex - _smallestConstantIndex + 1) - numConstants];
		for (int index = _smallestConstantIndex, edge = _smallestConstantIndex, constantIndex = 0; index <= _largestConstantIndex; index++)
		{
			if (index == _constantIndices[constantIndex])
			{
				_indexToEdgeOrConstant[index - _smallestConstantIndex] = ~constantIndex;		// Encode constant index as negative (complement) value
				constantIndex++;
			}
			else
			{
				_indexToEdgeOrConstant[index - _smallestConstantIndex] = edge;
				_edgeToIndex[edge - _smallestConstantIndex] = index;
				edge++;
			}
		}
	}
	

	@Override
	public final boolean hasConstants()
	{
		return true;
	}
	
	@Override
	public final int getConstantCount()
	{
		return _constants.length;
	}
	
	@Override
	public final Object[] getConstants()
	{
		return _constants;
	}
	
	@Override
	public final int[] getConstantIndices()
	{
		return _constantIndices;
	}
	
	@Override
	public final boolean isConstantIndex(int index)
	{
		if (index < _smallestConstantIndex || index > _largestConstantIndex)	// Index beyond the ends of the list of constants
			return false;
		else
			return (_indexToEdgeOrConstant[index - _smallestConstantIndex] < 0);		// Negative value is a constant
	}
	
	@Override
	public boolean hasConstantsInIndexRange(int minIndex, int maxIndex)
	{
		return numConstantsInIndexRange(minIndex, maxIndex) > 0;
	}
	
	@Override
	public final boolean hasConstantAtOrAboveIndex(int index)
	{
		return _largestConstantIndex >= index;
	}
	
	@Override
	public final boolean hasConstantAtOrBelowIndex(int index)
	{
		return _smallestConstantIndex <= index;
	}
	
	@Override
	public final int numConstantsInIndexRange(int minIndex, int maxIndex)
	{
		if (maxIndex < _smallestConstantIndex || minIndex > _largestConstantIndex)	// Range beyond the ends of the list of constants
			return 0;
		else
		{
			int numConstants = 0;
			for (int index = minIndex; index <= maxIndex; index++)
				if (isConstantIndex(index))
					numConstants++;
			return numConstants;
		}
	}
	
	@Override
	public final int numConstantsAtOrAboveIndex(int index)
	{
		return numConstantsInIndexRange(index, _largestConstantIndex);
	}
	
	@Override
	public final int numConstantsAtOrBelowIndex(int index)
	{
		return numConstantsInIndexRange(0, index);
	}
	
	@Override
	public final @Nullable Object getConstantByIndex(int index)
	{
		if (index < _smallestConstantIndex || index > _largestConstantIndex)	// Index beyond the ends of the list of constants
			return null;
		else
		{
			int edgeOrConstant = _indexToEdgeOrConstant[index - _smallestConstantIndex];
			return (edgeOrConstant < 0) ? _constants[~edgeOrConstant] : null;	// Negative value is a constant (complement of the index)
		}
	}
	
	@Override
	public final int getEdgeByIndex(int index)
	{
		if (index < _smallestConstantIndex)										// Index below the lower end of the list of constants
			return index;
		else if (index > _largestConstantIndex)									// Index above the upper end of the list of constants
			return index - _constants.length;
		else
		{
			int edgeOrConstant = _indexToEdgeOrConstant[index - _smallestConstantIndex];
			return (edgeOrConstant < 0) ? NO_PORT : edgeOrConstant;				// Negative value is a constant (complement of the index)
		}
	}
	
	@Override
	public @Nullable int[] getEdgesByIndexRange(int minIndex, int maxIndex)
	{
		int numEdges = maxIndex - minIndex + 1 - numConstantsInIndexRange(minIndex, maxIndex);
		if (numEdges < 1)
			return null;
		int[] edges = new int[numEdges];
		for (int index = minIndex, i = 0; index <= maxIndex; index++)
		{
			int edge = getEdgeByIndex(index);
			if (edge != NO_PORT)
				edges[i++] = edge;
		}
		return edges;
	}
	
	@Override
	public final int getIndexByEdge(int edge)
	{
		if (edge < _smallestConstantIndex)										// Edge below the lower end of the list of constants
			return edge;
		else if (edge > _largestConstantIndex - _constants.length)				// Edge above the upper end of the list of constants
			return edge + _constants.length;
		else
			return _edgeToIndex[edge - _smallestConstantIndex];
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
	public @Nullable int[] getDirectedToIndices(int numEdges)
	{
		// Add the constants to the total number of edges
		int[] directedToIndices = _factorFunction.getDirectedToIndices(numEdges + _constants.length);
		if (directedToIndices != null)
		{
			directedToIndices = contractIndexList(directedToIndices);	// Remove the constant indices
		}
		return directedToIndices;
	}
	
	@Override
	public @Nullable int[] getDirectedToIndices()
	{
		int[] directedToIndices = _factorFunction.getDirectedToIndices();
		if (directedToIndices != null)
		{
			directedToIndices = contractIndexList(directedToIndices);	// Remove the constant indices
		}
		return directedToIndices;
	}

	@Override
	public @Nullable int[] getDirectedToIndicesForInput(Factor factor, int inputEdge)
	{
		int[] directedToIndices = _factorFunction.getDirectedToIndicesForInput(factor, getIndexByEdge(inputEdge));
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
	public void evalDeterministic(Value[] arguments)
	{
		Value[] expandedArgumentList = expandValues(arguments);
		_factorFunction.evalDeterministic(expandedArgumentList);
		
		// Replace the original argument list entries, leaving out constant indices
		int numExpandedArguments = expandedArgumentList.length;
		int numConsts = _constants.length;
		for (int iExp = 0, iOrig = 0, iConst = 0; iExp < numExpandedArguments; iExp++)
		{
			if (iExp != ((iConst >= numConsts) ? -1 : _constantIndices[iConst]))
				arguments[iOrig++].setFrom(expandedArgumentList[iExp]);
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
		int constantLength = _constants.length;
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
	
	// Expand list of inputs to include the constants
	// Assumes constant index list is already sorted
	private Object[] expandInputList(Object... input)
	{
		int inputLength = input.length;
		int constantLength = _constants.length;
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
	
	private Value[] expandValues(Value[] values)
	{
		int inputLength = values.length;
		int constantLength = _constants.length;
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
	@Override
	public FactorFunction getContainedFactorFunction()
	{
		return _factorFunction;
	}

}
