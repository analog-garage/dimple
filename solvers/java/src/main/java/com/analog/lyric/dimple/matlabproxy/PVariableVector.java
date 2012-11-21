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
import com.analog.lyric.dimple.model.Node;
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
	
	public PVariableVector(Node [] nodes)
	{
		super(nodes);
		
		for (int i = 0; i < nodes.length; i++)
		{
			if (! (nodes[i] instanceof VariableBase))
				throw new DimpleException("expected VariableBases");
		}
	}
	

	private VariableBase getVariable(int index)
	{
		return (VariableBase)getModelerNode(index);
	}
	
	//package-private
	VariableBase [] getVariableArray() 
	{
		Node [] vars = this.getModelerNodes();
		VariableBase [] realVars;
		if (vars.length == 0)
			realVars = new VariableBase[0];
		else if (vars[0] instanceof Real)								// Assumes all variables are of the same class
			realVars = new Real[vars.length];
		else if (vars[0] instanceof Discrete)
			realVars = new Discrete[vars.length];
		else if (vars[0] instanceof RealJoint)
			realVars = new RealJoint[vars.length];
		else
			throw new DimpleException("ack!");
		
		for (int i = 0; i < realVars.length; i++)
		{
			realVars[i] = (VariableBase)vars[i];
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
	
	public PFactorVector getFactors(int relativeNestingDepth) 
	{
		
		ArrayList<Node> retval = new ArrayList<Node>();
		
		for (VariableBase v : getVariableArray())
		{
			FactorBase [] funcs = v.getFactors(relativeNestingDepth);
			for (FactorBase fb : funcs)
				retval.add(fb);
		}
		
		Node [] realRetVal = new Node[retval.size()];
		retval.toArray(realRetVal);
		return PHelpers.convertToFactorVector(realRetVal);
		
	}
	
	public void setGuess(double [] guess) 
	{
		for (int i = 0; i < guess.length; i++)
		{
			getVariable(i).setGuess(Double.valueOf(guess[i]));
		}
	}
	
	public void setGuess(Object [] guess) 
	{
		for (int i = 0; i < guess.length; i++)
		{
			getVariable(i).setGuess(guess[i]);
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
		return (getVariable(0) instanceof Discrete);
	}

	public boolean isReal()
	{
		return (getVariable(0) instanceof Real);
	}
	
	public PDomain getDomain()
	{
		return PHelpers.wrapDomain(getVariable(0).getDomain());
	}
	
	public boolean isJoint()
	{
		return (getVariable(0) instanceof RealJoint);
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

	VariableBase [] getModelerVariables()
	{
		Node [] nodes = getModelerNodes();
		VariableBase [] retval = new VariableBase[nodes.length];
		for (int i = 0; i < retval.length; i++)
			retval[i] = (VariableBase)nodes[i];
		return retval;
	}

	@Override
	public PNodeVector createNodeVector(Node[] nodes) 
	{
		return new PVariableVector(nodes);
	}
	
	public void setProperty(String key,Object value)
	{
		for (VariableBase vb : getVariableArray())
			vb.setProperty(key, value);
	}


}
