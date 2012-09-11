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

package com.analog.lyric.dimple.matlabproxy;

import java.util.UUID;

import com.analog.lyric.dimple.FactorFunctions.NopFactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.matlabproxy.PHelpers;
import com.analog.lyric.dimple.matlabproxy.PVariableVector;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;


/*
 * This proxy wraps the Solver Function class
 */
public class PFactor extends PFactorBase
{
	public PVariableVector getConnectedVariableVector()
	{
		return PHelpers.convertToVariableVector(getConnectedVariables());
	}
	
	protected Factor _factor;
	//can't really recover on de-serialization
	//could reconstruct table function if that were serialized
	private FactorFunction _factorFunction;

	
	
	public PFactor(Factor impl)
	{
		this(impl, new NopFactorFunction("Undefined"));
	}
	
	public PFactor(Factor impl, FactorFunction factorFunction)
	{
		_factor = impl;
		_factorFunction = factorFunction;
	}

	public PFactor(PFactor other)
	{
		_factor = other._factor;
		_factorFunction = other._factorFunction;
	}
	
	public boolean isDiscrete()
	{
		return _factor.isDiscrete();
	}
	
	
	public Factor getModelerObject()
	{
		return _factor;
	}
	
	
	public String toString()
	{
		return _factor.toString();
	}
	public boolean equals(Object obj) 
	{ 
		try 
		{
			return _factor == ((PFactor)obj)._factor;
		}
		catch (Exception e)
		{
		  
		}

		return false;
	}
	
	public int hashCode()
	{
		return _factor.hashCode();
	}
	
	public int getId()
	{
		return _factor.getId();
	}

	public PVariableBase[] getConnectedVariables() 
	{
		return PHelpers.convertToVariables(_factor.getVariables());
	}
	//Name functions
	public String getFactorFunctionName()
	{
		return _factorFunction.getName();
	}
	public UUID getUUID()
	{
		return _factor.getUUID();
	}
	public void setUUID(UUID newUUID) 
	{
		_factor.setUUID(newUUID);
	}
	public String getName()
	{
		return _factor.getName();
	}
	public String getExplicitName()
	{
		return _factor.getExplicitName();
	}
	public String getQualifiedName()
	{
		return _factor.getQualifiedName();
	}
	public void setName(String name) 
	{
		_factor.setName(name);
	}
	public String getLabel()
	{
		return _factor.getLabel();
	}
	public String getQualifiedLabel()
	{
		return _factor.getQualifiedName();
	}
	public String getModelerFunctionName()
	{
		return _factor.getModelerFunctionName();
	}
	public ISolverFactor getSolver()
	{
		return _factor.getSolver();
	}
	
	public double [] getBelief() 
	{
		return _factor.getBelief();
	}
	
	/*
	public int[][] getPossibleBeliefIndices() 
	{
		return _factor.getPossibleBeliefIndices();
	}
	*/
	
	public Port [] getPorts()
	{
		Port [] retval = new Port [_factor.getPorts().size()];
		for (int i = 0; i < retval.length; i++)
		{
			retval[i] = _factor.getPorts().get(i);
		}
		return retval;
	}
    public IPNode[] getConnectedNodes()
    {
    	return getConnectedVariables();
    }
	
	public void update() 
	{
		_factor.update();
	}
	
	public void updateEdge(int portNum) 
	{
		_factor.updateEdge(portNum);
	}
	
	public int getPortNum(PVariableBase var) 
	{
		return _factor.getPortNum(var.getModelerObject());
	}
	
	public PFactorGraph getParentGraph() 
	{
		PFactorGraph graph = null;
		FactorGraph mgraph = _factor.getParentGraph();
		if(mgraph != null)
		{
				graph = new PFactorGraph(mgraph);
		}
		return graph;
	}
	public PFactorGraph getRootGraph() 
	{
		PFactorGraph graph = null;
		FactorGraph mgraph = _factor.getRootGraph();
		if(mgraph != null)
		{
				graph = new PFactorGraph(mgraph);
		}
		return graph;
	}
	public boolean hasParentGraph() 
	{
		return _factor.hasParentGraph();
	}

	@Override
	public void setLabel(String name) 
	{
		_factor.setLabel(name);
	}
	
	public double getInternalEnergy()
	{
		return _factor.getInternalEnergy();
	}
	
	public double getBetheEntropy()
	{
		return _factor.getBetheEntropy();
	}
	
	public double getScore()
	{
		return _factor.getScore();
	}

}
