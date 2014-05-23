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

package com.analog.lyric.dimple.test.solvers.gibbs;

import java.util.Random;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.proposalKernels.BlockProposal;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.math.Utilities;

/**
 * 
 * @since 0.06
 * @author jbernst2
 */
public class TrivialNonuniformBlockProposer implements IBlockProposalKernel
{
	private double[] _weights;
	private int[] _domainSizes;
	private int[] _domainProducts;
	private int _numDomains;
	private Random _random;
	
	public TrivialNonuniformBlockProposer(double[] weights, int[] domainSizes)
	{
		_domainSizes = domainSizes;
		_weights = weights;
		_random = new Random(DimpleRandomGenerator.rand.nextLong());
		_numDomains = _domainSizes.length;
		
		_domainProducts = new int[_numDomains];
		_domainProducts[0] = 1;
		for (int i = 1; i < _numDomains; i++)
			_domainProducts[i] = _domainProducts[i-1] * _domainSizes[i-1];
	}
	
	@Override
	public BlockProposal next(Value[] currentValue, Domain[] variableDomain)
	{
		// Sample value randomly give the weights
		int newIndex = Utilities.sampleFromMultinomial(_weights, _random);
		int[] newArray = indexToArray(newIndex);
		
		int currentIndex = arrayToIndex(currentValue);

		Value[] newValue = new Value[_numDomains];
		for (int i = 0; i < _numDomains; i++)
		{
			Domain domain = variableDomain[i];
			if (domain.isDiscrete())
			{
				DiscreteDomain discreteDomain = domain.asDiscrete();
				Value v = Value.create(discreteDomain);
				v.setIndex(newArray[i]);
				newValue[i] = v;
			}
			else
			{
				throw new DimpleException("Not supported");
			}
		}
		
		double proposalForwardEnergy = -Math.log(_weights[newIndex]);
		double proposalReverseEnergy = -Math.log(_weights[currentIndex]);
		return new BlockProposal(newValue, proposalForwardEnergy, proposalReverseEnergy);
	}

	@Override
	public void setParameters(Object... parameters)
	{
	}

	@Override
	public Object[] getParameters()
	{
		return null;
	}
	
	
	protected int[] indexToArray(int index)
	{
		int[] newArray = new int[_numDomains];
		for (int i = 0; i < _numDomains; i++)
		{
			int divisor = _domainProducts[_numDomains-i-1];
			newArray[i] = index / divisor;
			index = index % divisor;
		}
		return newArray;
	}

	protected int arrayToIndex(int[] array)
	{
		int index = 0;
		for (int i = 0; i < _numDomains; i++)
			index += array[i] * _domainProducts[_numDomains-i-1];
		return index;
	}
	protected int arrayToIndex(Value[] array)
	{
		int index = 0;
		for (int i = 0; i < _numDomains; i++)
			index += array[i].getIndex() * _domainProducts[_numDomains-i-1];
		return index;
	}

	
	// Just a double check
	public static void main(String[] args)
	{
		int[] domainSizes = new int[]{2, 5, 3, 6, 4};
		int product = 2 * 5 * 3 * 6 * 4;
		double[] weights = new double[product];
		for (int i = 0; i < product; i++)
			weights[i] = DimpleRandomGenerator.rand.nextDouble();
		TrivialNonuniformBlockProposer t = new TrivialNonuniformBlockProposer(weights, domainSizes);
		for (int i = 0; i < product; i++)
		{
			int[] array = t.indexToArray(i);
			int j = t.arrayToIndex(array);
			if (i != j)
				throw new DimpleException("!");
		}
	}
}
