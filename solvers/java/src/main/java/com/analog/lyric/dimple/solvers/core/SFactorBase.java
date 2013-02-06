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

import java.util.ArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
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
		
		
	public Factor getFactor()
	{
		return _factor;
	}


	@Override
	public void update()  
	{
		for (int i = 0; i < _factor.getSiblings().size(); i++)
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
		ArrayList<INode> ports = _factor.getSiblings();
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
	
	public void moveMessages(ISolverNode other)
	{
		if (getModelObject().getSiblings().size() != other.getModelObject().getSiblings().size())
			throw new DimpleException("cannot move messages on nodes with different numbers of ports");			
		
		for (int i = 0; i < getModelObject().getSiblings().size(); i++)
		{
			moveMessages(other, i,i);
			
			INode thisVariable = getModelObject().getSiblings().get(i);
			INode otherVariable = other.getModelObject().getSiblings().get(i);
			int thisIndex = getModelObject().getSiblingPortIndex(i);
			int otherIndex = other.getModelObject().getSiblingPortIndex(i);
			thisVariable.getSolver().moveMessages(otherVariable.getSolver(), thisIndex,otherIndex);
		}
	}
	
	@Override
	public void setInputMsg(int portIndex, Object obj) {
		throw new DimpleException("Not supported by " + this);
	}
	
}
