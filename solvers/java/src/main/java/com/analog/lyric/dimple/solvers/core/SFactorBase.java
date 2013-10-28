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

package com.analog.lyric.dimple.solvers.core;

import java.util.List;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class SFactorBase extends SNode implements ISolverFactor
{
	protected Factor _factor;
	
	public SFactorBase(Factor factor)
	{
		super(factor);
		_factor = factor;
		
		
	}
		
	@Override
	public Factor getModelObject()
	{
		return _factor;
	}
	
	public Factor getFactor()
	{
		return _factor;
	}


	@Override
	public void update()
	{
		for (int i = 0, end = _factor.getSiblingCount(); i < end; i++)
			updateEdge(i);
	}
	
	@Override
	public double [] getBelief()
	{
		throw new DimpleException("not supported");
	}


	@Override
	public ISolverFactorGraph getParentGraph()
	{
		ISolverFactorGraph graph = null;
		FactorGraph mgraph = _factor.getParentGraph();
		if(mgraph != null)
		{
			graph = mgraph.getSolver();
		}
		return graph;
	}

	@Override
	public ISolverFactorGraph getRootGraph()
	{
		ISolverFactorGraph graph = null;
		FactorGraph mgraph = _factor.getRootGraph();
		if(mgraph != null)
		{
			graph = mgraph.getSolver();
		}
		return graph;
	}

	@Override
    public double getScore()
    {
		List<INode> ports = _factor.getSiblings();
		int numPorts = ports.size();
	    Object[] values = new Object[numPorts];

	    for (int port = 0; port < numPorts; port++)
	    {
	    	INode neighbor = _factor.getConnectedNodeFlat(port);
	    	values[port] = ((ISolverVariable)neighbor.getSolver()).getGuess();
	    }
	    
	    return _factor.getFactorFunction().evalEnergy(values);
    }

	@Override
	public int[][] getPossibleBeliefIndices()
	{
		throw new DimpleException("not implemented");
	}
	
	@Override
	public double getInternalEnergy()
	{
		throw new DimpleException("getInternalEnergy not yet supported");
	}
	
	@Override
	public double getBetheEntropy()
	{
		throw new DimpleException("getBetheEntropy not yet supported");
	}
	
	@Override
	public void moveMessages(ISolverNode other)
	{
		Factor factor = getModelObject();
		INode otherFactor = other.getModelObject();
		
		int nSiblings = factor.getSiblingCount();
		
		if (nSiblings != otherFactor.getSiblingCount())
			throw new DimpleException("cannot move messages on nodes with different numbers of ports");
		
		for (int i = 0; i < nSiblings; i++)
		{
			moveMessages(other, i,i);
			
			INode thisVariable = factor.getSibling(i);
			INode otherVariable = otherFactor.getSibling(i);
			int thisIndex = factor.getSiblingPortIndex(i);
			int otherIndex = otherFactor.getSiblingPortIndex(i);
			thisVariable.getSolver().moveMessages(otherVariable.getSolver(), thisIndex,otherIndex);
		}
	}
	
	@Override
	public void setDirectedTo(int [] indices)
	{
		
	}


}
