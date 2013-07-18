package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.google.common.primitives.Ints;

public class DeterministicFactorTable extends NewFactorTableBase implements INewFactorTableBase, IFactorTable
{
	private static final long serialVersionUID = 1L;

	private final int[] _outputValues;
	
	/*--------------
	 * Construction
	 */
	
	public DeterministicFactorTable(BitSet directedFrom, DiscreteDomain ... domains)
	{
		super(directedFrom, domains);
		_outputValues = new int[getInputIndexSize()];
	}
	
	public DeterministicFactorTable(DeterministicFactorTable that)
	{
		super(that);
		_outputValues = that._outputValues.clone();
	}
	
	/*----------------
	 * Object methods
	 */

	@Override
	public DeterministicFactorTable clone()
	{
		return new DeterministicFactorTable(this);
	}
	
	/*-----------------------------
	 * INewFactorTableBase methods
	 */
	
	@Override
	public void evalDeterministic(Object[] arguments)
	{
		int outputIndex = _outputValues[inputIndexFromArguments(arguments)];
		outputIndexToArguments(outputIndex, arguments);
	}
	
	@Override
	public final double getEnergy(int i)
	{
		return i >= 0 && i < size() ? 0.0 : Double.POSITIVE_INFINITY;
	}
	
	@Override
	public final double getWeight(int i)
	{
		return i >= 0 && i < size() ? 1.0 : 0.0;
	}
	
	@Override
	public boolean isDeterministicDirected()
	{
		return true;
	}

	@Override
	public boolean isNormalized()
	{
		return true;
	}
	
	@Override
	public final int locationFromJointIndex(int joint)
	{
		int outputSize = getOutputIndexSize();
		int location = joint / outputSize;
		int outputIndex = joint - location * outputSize;
		if (outputIndex == _outputValues[location])
		{
			return location;
		}
		else
		{
			return -1-location;
		}
	}
	
	@Override
	public final int locationToJointIndex(int location)
	{
		return _outputValues[location] + location * getOutputIndexSize();
	}
	
	@Override
	public void normalize()
	{
	}

	@Override
	public int size()
	{
		return _outputValues.length;
	}
	
	/*-----------------------
	 * IFactorTable methods
	 */
	
	@Override
	public void change(int[][] indices, double[] weights)
	{
		if (indices.length != weights.length)
		{
			throw new IllegalArgumentException(
				String.format("indices and weights lenghts differ (%d vs %d)", indices.length, weights.length));
		}
		
		final int nTos = _outputIndices.length;
		int[] newValues = new int[size()];
		Arrays.fill(newValues,  -1);
		for (int i = 0, endi = indices.length; i < endi; ++i)
		{
			if (weights[i] != 0.0)
			{
				int[] row = indices[i];
				int valueIndex = inputIndexFromIndices(row) * nTos;
				for (int j = 0, endj = nTos; j < endj; ++j, ++ valueIndex)
				{
					newValues[valueIndex] = row[_outputIndices[j]];
				}
			}
		}
	
		if (Ints.indexOf(newValues, -1) >= 0)
		{
			throw new DimpleException("change would be non-deterministic");
		}
		
		for (int i = newValues.length; --i>=0;)
		{
			_outputValues[i] = newValues[i];
		}
	}

	@Override
	public void changeIndices(int[][] indices)
	{
		final int nTos = _outputIndices.length;
		for (int i = 0, endi = indices.length; i < endi; ++i)
		{
			int[] row = indices[i];
			int valueIndex = inputIndexFromIndices(row) * nTos;
			for (int j = 0, endj = nTos; j < endj; ++j, ++ valueIndex)
			{
				_outputValues[valueIndex] = row[_outputIndices[j]];
			}
		}
	}

	@Override
	public void set(int[] indices, double value)
	{
		throw DimpleException.unsupported("set");
	}

	@Override
	public void changeWeights(double[] values)
	{
		throw DimpleException.unsupported("changeWeights");
	}

	@Override
	public double[] getWeights()
	{
		double[] weights = new double[size()];
		Arrays.fill(weights, 1.0);
		return weights;
	}

	@Override
	public void normalize(int[] directedTo)
	{
	}

	@Override
	public void changeWeight(int index, double weight)
	{
		throw DimpleException.unsupported("changeWeight");
	}

	@Override
	public DeterministicFactorTable copy()
	{
		return new DeterministicFactorTable(this);
	}

	@Override
	public void copy(IFactorTable that)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public IFactorTable createTableWithNewVariables(DiscreteDomain[] newDomains)
	{
		// TODO Auto-generated method stub
		// What does this mean for a directed table?
		return null;
	}

	@Override
	public double evalAsFactorFunction(Object... arguments)
	{
		return evalWeight(arguments);
	}

	@Override
	public void evalDeterministicFunction(Object ... arguments)
	{
		evalDeterministic(arguments);
	}

	@Override
	public double[] getPotentials()
	{
		return new double[size()];
	}

	@Override
	public int getWeightIndexFromTableIndices(int[] indices)
	{
		return locationFromIndices(indices);
	}

	@Override
	public IFactorTable joinVariablesAndCreateNewTable(int[] varIndices,
		int[] indexToJointIndex,
		DiscreteDomain[] allDomains,
		DiscreteDomain jointDomain)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void normalize(int[] directedTo, int[] directedFrom)
	{
	}

	@Override
	public void randomizeWeights(Random rand)
	{
	}

	@Override
	public void setDirected(int[] directedTo, int[] directedFrom)
	{
		throw DimpleException.unsupported("setDirected");
	}
	
	/*----------------------------------
	 * DeterministicFactorTable methods
	 */

	/*-----------------
	 * Private methods
	 */
	
	private int locationFromInputOutputIndexes(int inputIndex, int outputIndex)
	{
		int computedOutput = _outputValues[inputIndex];
		
		if (outputIndex == computedOutput)
		{
			return inputIndex;
		}

		int expectedJointIndex = outputIndex + inputIndex * getOutputIndexSize();
		return -1 - expectedJointIndex;
	}
	
}
