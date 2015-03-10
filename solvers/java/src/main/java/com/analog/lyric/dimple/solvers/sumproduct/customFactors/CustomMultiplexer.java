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

package com.analog.lyric.dimple.solvers.sumproduct.customFactors;


import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNull;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.STableFactorDoubleArray;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductDiscreteEdge;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;

/*
 * The Multiplexer factor is a directed factor
 *    a z(1) z(2) ...
 *    \ |   /    /
 *       y
 *  such that P(Y=y|a,z(1),z(2),...) = Identity(y == z(a))
 * 
 *  The following custom factor provides optimized inference for the
 *  Multiplexer factor function
 */
public class CustomMultiplexer extends STableFactorDoubleArray
{
	private int _yDomainSize;
	private int _aDomainSize;
	
	//Create a mapping between a yIndex and all the possible zs that could
	//have been selected to achieve that value of y
	private ArrayList<int []> [] _yIndex2zIndices;
	
	//Create a mapping between a z index and the y
	private int [][] _zIndices2yIndex;

	@SuppressWarnings("unchecked")
	public CustomMultiplexer(Factor factor, SumProductSolverGraph parent)
	{
		super(factor, parent);
		
		final int nVars = factor.getSiblingCount();
		if (nVars < 2)
			throw new DimpleException("Must specify at least Y and A");
		
		final Variable y = factor.getSibling(0);
		final Variable a = factor.getSibling(1);
		
		final DiscreteDomain yDomain = y.asDiscreteVariable().getDiscreteDomain();
		
		_yDomainSize = yDomain.size();
		_aDomainSize = a.asDiscreteVariable().getDiscreteDomain().size();
		
		if (_aDomainSize+2 != nVars)
			throw new DimpleException("Must specify " + _aDomainSize + " Zs");
		
		//calculate the list of z index pairs for each y
		_yIndex2zIndices = new ArrayList [_yDomainSize];
		
		//Generate the mapping from Ys to Zs
		for (int i = 0; i < _yDomainSize; i++)
		{
			_yIndex2zIndices[i] = new ArrayList<int[]>();
			
			for (int j = 0; j < _aDomainSize; j++)
			{
				DiscreteDomain zDomain = factor.getSibling(2+j).asDiscreteVariable().getDiscreteDomain();
				
				for (int k = 0, end = zDomain.size(); k < end; k++)
				{
					if (yDomain.getElement(i).equals(zDomain.getElement(k)))
					{
						_yIndex2zIndices[i].add(new int [] {j,k});
						break;
					}
				}
			}
		}
		
		_zIndices2yIndex = new int[_aDomainSize][];
		
		//Generate the mappings from zs to Y
		for (int i = 0; i < _aDomainSize; i++)
		{
			DiscreteDomain zDomain = factor.getSibling(2+i).asDiscreteVariable().getDiscreteDomain();

			_zIndices2yIndex[i] = new int [zDomain.size()];
			
			for (int j = 0; j < _zIndices2yIndex[i].length; j++)
			{
				_zIndices2yIndex[i][j] = -1;
				for (int k = 0; k < _yDomainSize; k++)
				{
					if (yDomain.getElement(k).equals(zDomain.getElement(j)))
					{
						_zIndices2yIndex[i][j] = k;
						break;
					}
				}
			}
		}
		
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		if (outPortNum == 0)
			updateToY();
		else if (outPortNum == 1)
			updateToA();
		else
			updateToZ(outPortNum-2);
	}
	
	@Override
	protected boolean createFactorTableOnInit()
	{
		return false;
	}
	
	@Override
	protected void setTableRepresentation(@NonNull IFactorTable table)
	{
	}

	public void updateToA()
	{
		//p(a=x) = sum_{z in za} p(y=z)p(za=z)
		
		double total = 0;
	
		final double[] yWeights = getSiblingEdgeState(0).varToFactorMsg.representation();
		final double[] aWeights = getSiblingEdgeState(1).factorToVarMsg.representation();
			
		for (int i = 0; i < _aDomainSize; i++)
		{
			final double[] zWeights = getSiblingEdgeState(i+2).varToFactorMsg.representation();
			double sm = 0;
			
			for (int j = 0; j < _zIndices2yIndex[i].length; j++)
			{
				int yIndex = _zIndices2yIndex[i][j];
				sm += yWeights[yIndex] * zWeights[j];
			}
			
			aWeights[i] = sm;
			total += sm;
		}
				
		//normalize
		for (int i = 0; i < _aDomainSize; i++)
			aWeights[i] /= total;
		
	}
	
	public void updateToY()
	{
		//P(Y=y) = sum_{a} p(a)p(za=y)
		
		double [] outMsg = getSiblingEdgeState(0).factorToVarMsg.representation();
		double [] aInputMsg = getSiblingEdgeState(1).varToFactorMsg.representation();
		
		double total = 0;
		
		for (int i = 0; i < _yDomainSize; i++)
		{
			ArrayList<int []> zIndices = _yIndex2zIndices[i];
			double sm = 0;
			
			for (int [] tmp : zIndices)
			{
				int a = tmp[0];
				int z = tmp[1];
				sm += aInputMsg[a] * getSiblingEdgeState(a+2).varToFactorMsg.getWeight(z);
			}
			
			outMsg[i] = sm;
			total += sm;
		}
		
		//normalize
		for (int i = 0; i < _yDomainSize; i++)
			outMsg[i] /= total;
		
	}
	
	public void updateToZ(int index)
	{
		//TODO: Can we optimize update all edges to calculate this once
		//      and then subtract off parts for each Z?
		
		//P(Zi=x) = p(a=i)p(y=x) + sum_{j not i} sum_{z in za} p(aj)p(y=z)p(za=z)
		
		double [] zBelief = getSiblingEdgeState(index+2).factorToVarMsg.representation();
		double [] yWeights = getSiblingEdgeState(0).varToFactorMsg.representation();
		double [] aWeights = getSiblingEdgeState(1).varToFactorMsg.representation();
		
		double offset = 0;
		
		for (int j = 0; j < _aDomainSize; j++)
		{
			if (j != index)
			{
				final double a = aWeights[j];
				final int[] zIndices2yIndex = _zIndices2yIndex[j];
				final double[] zWeights = getSiblingEdgeState(j+2).varToFactorMsg.representation();
				
				for (int k = 0, nk = zIndices2yIndex.length; k < nk; k++)
				{
					int yIndex = zIndices2yIndex[k];
					offset += a * yWeights[yIndex] * zWeights[k];
				}
			}
		}
		
		
		double total = 0;
		
		for (int i = 0; i < _zIndices2yIndex[index].length; i++)
		{
			int yIndex = _zIndices2yIndex[index][i];
			zBelief[i] = aWeights[index] * yWeights[yIndex] + offset;
			total += zBelief[i];
		}
		
		//normalize
		for (int i = 0; i < zBelief.length; i++)
			zBelief[i] /= total;
		
	}
	
	@SuppressWarnings("null")
	@Override
	public SumProductDiscreteEdge getSiblingEdgeState(int siblingIndex)
	{
		return (SumProductDiscreteEdge)super.getSiblingEdgeState(siblingIndex);
	}
}
