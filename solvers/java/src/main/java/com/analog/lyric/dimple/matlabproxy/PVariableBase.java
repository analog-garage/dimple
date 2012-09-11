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

import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.RealJointDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class PVariableBase implements IPNode, Cloneable
{
	protected VariableBase _variable;	
	
	public void update() 
	{
		_variable.update();
	}
	
	public void updateEdge(int portNum) 
	{
		_variable.updateEdge(portNum);
	}
	
	public VariableBase getModelerObject()
	{
		return _variable;
	}

	public int getId()
	{
		return _variable.getId();
	}
	
	public void setGuess(Object guess) 
	{
		_variable.setGuess(guess);
	}
	
	public Object getGuess() 
	{
		return _variable.getGuess();
	}
	
	public void setProperty(String key,Object value)
	{
		_variable.setProperty(key, value);
	}
	
	public boolean equals(Object obj) 
	{ 
		try {return _variable == ((PVariableBase)obj)._variable;}
		catch (Exception e) {}
		return false;
	}
	
	public int hashCode()
	{
		return _variable.hashCode();
	}
	
	public String toString()
	{
		return _variable.toString();
	}
	
	public ISolverVariable getSolver()
	{
		return _variable.getSolver();
	}
	
	public PFactorBase [] getFactors(int relativeNestingDepth) 
	{
		return PHelpers.convertToFactors(_variable.getFactors(relativeNestingDepth));
	}
	
	
	public Port[] getPorts()
	{
		Port [] retval = new Port [_variable.getPorts().size()];
		for (int i = 0; i < retval.length; i++)
		{
			retval[i] = _variable.getPorts().get(i);
		}
		return retval;
	}
	
	public int getPortNum(PFactor f) 
	{
		return _variable.getPortNum(f.getModelerObject());
	}
	
	public PFactorGraph getParentGraph() 
	{
		PFactorGraph graph = null;
		FactorGraph mgraph = _variable.getParentGraph();
		if(mgraph != null)
		{
				graph = new PFactorGraph(mgraph);
		}
		return graph;
	}
	
	public PFactorGraph getRootGraph() 
	{
		PFactorGraph graph = null;
		FactorGraph mgraph = _variable.getRootGraph();
		if(mgraph != null)
		{
			graph = new PFactorGraph(mgraph);
		}
		return graph;
	}
	
	public boolean hasParentGraph()
	{
		return _variable.hasParentGraph();
	}

	//Name functions
	public UUID getUUID()
	{
		return _variable.getUUID();
	}
	public void setUUID(UUID newUUID) 
	{
		_variable.setUUID(newUUID);
	}
	public String getName()
	{
		return _variable.getName();
	}
	public String getExplicitName()
	{
		return _variable.getExplicitName();
	}
	public String getQualifiedName()
	{
		return _variable.getQualifiedName();
	}
	public void setName(String name) 
	{
		_variable.setName(name);
	}
	public String getLabel()
	{
		return _variable.getLabel();
	}
	public String getQualifiedLabel()
	{
		return _variable.getQualifiedName();
	}	
	public String getModelerClassName()
	{
		return _variable.getModelerClassName();
	}
	
	public PDomain getDomain()
	{
		if (_variable.getDomain().isDiscrete())
			return new PDiscreteDomain((DiscreteDomain)_variable.getDomain());
		else if (_variable.getDomain() instanceof RealJointDomain)
			return new PRealJointDomain((RealJointDomain)_variable.getDomain());
		else
			return new PRealDomain((RealDomain)_variable.getDomain());
	}

	
	public abstract boolean isDiscrete();
	public abstract boolean isReal();
	public abstract boolean isJoint();
	
	@Override
	public boolean isFactor() 
	{
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isVariable() 
	{
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isGraph() 
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setLabel(String name) 
	{
		_variable.setLabel(name);
	}

	public double getScore()
	{
		return _variable.getScore();
	}
	
	public double getInternalEnergy()
	{
		return _variable.getInternalEnergy();
	}
	
	public double getBetheEntropy()
	{
		return _variable.getBetheEntropy();
	}

}
