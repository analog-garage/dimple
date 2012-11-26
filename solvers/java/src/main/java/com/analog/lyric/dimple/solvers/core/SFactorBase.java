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

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

public abstract class SFactorBase implements ISolverFactor 
{
	protected Factor _factor;
	
	public SFactorBase(Factor factor)
	{
		_factor = factor;
	}
	
	public Factor getFactor()
	{
		return _factor;
	}

	public void initialize()  
	{
		
	}

	public void update()  
	{
		for (int i = 0; i < _factor.getPorts().size(); i++)
			updateEdge(i);
		
	}
	
	public Object getDefaultMessage(Port port) 
	{
		com.analog.lyric.dimple.model.VariableBase var = (com.analog.lyric.dimple.model.VariableBase)port.getConnectedNode();
		SVariableBase v = (SVariableBase)var.getSolver();
		return v.getDefaultMessage(port);
	}

	public double [] getBelief() 
	{
		throw new DimpleException("not supported");
	}

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
	public int[][] getPossibleBeliefIndices()  {
		// TODO Auto-generated method stub
		throw new DimpleException("not implemented");
	}
	
	public double getInternalEnergy()
	{
		throw new DimpleException("getInternalEnergy not yet supported");
		
	}
	public double getBetheEntropy()
	{
		throw new DimpleException("getBetheEntropy not yet supported");		
	}
	
    public double getScore()
    {    	
    	throw new DimpleException("not supported");    	
    }

	public void connectPort(Port p) 
	{
		
	}
}
