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
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class SVariableBase implements ISolverVariable
{
	protected VariableBase _var;
	
	public SVariableBase(VariableBase var)
	{
		_var = var;
	}
	
	public void connectPort(Port p) 
	{
		
	}

	
	public void initialize() 
	{
		
	}

	public void remove(Factor factor)
	{
		
	}
	
	
	public ISolverFactorGraph getParentGraph()
	{
		ISolverFactorGraph graph = null;
		FactorGraph mgraph = _var.getParentGraph();
		if(mgraph != null)
		{
			graph = mgraph.getSolver();
		}
		return graph;
	}
	public ISolverFactorGraph getRootGraph()
	{
		ISolverFactorGraph graph = null;
		FactorGraph mgraph = _var.getRootGraph();
		if(mgraph != null)
		{
			graph = mgraph.getSolver();
		}
		return graph;
	}
	

	@Override
	public double getEnergy()  
	{
		throw new DimpleException("not supported");
	}


	@Override
	public void setGuess(Object guess)  
	{
		throw new DimpleException("not supported");
	}


	@Override
	public Object getGuess()  
	{
		throw new DimpleException("not supported");
	}

    public double getInternalEnergy()
    {
    	throw new DimpleException("not supported");
    }
    
    public double getBetheEntropy() 
    {
    	throw new DimpleException("not supported");    	
    }
    
    public double getScore()
    {    	
    	throw new DimpleException("not supported");    	
    }
	

	@Override
	public void update()  
	{
		for (int i = 0; i < _var.getPorts().size(); i++)
		{
			updateEdge(i);
		}
		
	}

	
}
