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

import java.util.ArrayList;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.FactorBase;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.VariableBase;



/*
 * This class provides vectors of Variables to allow matlab to set multiple inputs 
 * simultaneously.  This is important for performance reasons.  (MATLAB is slow to
 * manipulate lots of objects)
 */
public class PVariableVector extends PNodeVector
{
	
	public PVariableVector() 
	{
		
	}
	
	public PVariableVector(IPNode [] nodes)
	{
		super(nodes);
		
		for (int i = 0; i < nodes.length; i++)
		{
			if (! (nodes[i] instanceof PVariableBase))
				throw new DimpleException("expected VariableBases");
		}
	}
	

	PVariableBase getVariable(int index)
	{
		return (PVariableBase)getNode(index);
	}
	
	//package-private
	VariableBase [] getVariableArray() 
	{
		IPNode[] vars = this.getNodes();
		VariableBase [] realVars;
		if (vars.length == 0)
			realVars = new VariableBase[0];
		else if (vars[0] instanceof PRealVariable)								// Assumes all variables are of the same class
			realVars = new Real[vars.length];
		else if (vars[0] instanceof PDiscreteVariable)
			realVars = new Discrete[vars.length];
		else if (vars[0] instanceof PRealJointVariable)
			realVars = new RealJoint[vars.length];
		else
			throw new DimpleException("ack!");
		
		for (int i = 0; i < realVars.length; i++)
		{
			realVars[i] = (VariableBase)vars[i].getModelerObject();
		}
		return realVars;
	}
	
	
	public String getModelerClassName() 
	{
		if (size() > 0)
			return getVariable(0).getModelerClassName();
		else
			return "";
	}
	
	//TODO: support all getFactors variants
	
	public PFactorBase [] getFactors(int relativeNestingDepth) 
	{
		ArrayList<PFactorBase> retval = new ArrayList<PFactorBase>();
		
		for (VariableBase v : getVariableArray())
		{
			FactorBase [] funcs = v.getFactors(relativeNestingDepth);
			PFactorBase [] tmp = PHelpers.convertToFactors(funcs);
			for (PFactorBase f : tmp)
				retval.add(f);
		}
		
		PFactorBase [] realRetVal = new PFactorBase[retval.size()];
		retval.toArray(realRetVal);
		return realRetVal;
		
	}
	
	public void setGuess(Object [] guess) 
	{
		for (int i = 0; i < guess.length; i++)
		{
			((PVariableBase)getNodes()[i]).setGuess(guess[i]);
		}
	}
	
	public Object [] getGuess() 
	{
		Object [] retval = new Object[size()];
		VariableBase [] vars = getVariableArray();
		for (int i = 0; i < vars.length; i++)
		{
			retval[i] = vars[i].getGuess();
		}
		return retval;
	}
	
	public boolean isDiscrete()
	{
		return getVariable(0).isDiscrete();
	}

	public boolean isReal()
	{
		return getVariable(0).isReal();
	}
	
	public PDomain getDomain()
	{
		return getVariable(0).getDomain();
	}
	
	public boolean isJoint()
	{
		return getVariable(0).isJoint();
	}
	
	public boolean isVariable()
	{
		return true;
	}
	public boolean isFactor()
	{
		return false;
	}
	
	public boolean isGraph()
	{
		return false;
	}

	public PVariableBase [] getVariables()
	{
		IPNode [] nodes = getNodes();
		PVariableBase [] retval = new PVariableBase[nodes.length];
		for (int i = 0; i < retval.length; i++)
			retval[i] = (PVariableBase)nodes[i];
		return retval;
	}

	@Override
	public PNodeVector createNodeVector(IPNode[] nodes) 
	{
		return new PVariableVector(nodes);
	}

}
