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

import com.analog.lyric.collect.Supers;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.util.misc.Matlab;



/*
 * This class provides vectors of Variables to allow matlab to set multiple inputs
 * simultaneously.  This is important for performance reasons.  (MATLAB is slow to
 * manipulate lots of objects)
 */
@Matlab
public class PVariableVector extends PNodeVector
{
	/*--------------
	 * Construction
	 */
	
	public PVariableVector()
	{
		
	}
	
	public PVariableVector(Node [] nodes)
	{
		super(nodes);
		
		for (int i = 0; i < nodes.length; i++)
		{
			if (! (nodes[i] instanceof Variable))
				throw new DimpleException("expected Variables");
		}
	}
	
	/*------------------
	 * PObject methods
	 */
	
	@Override
	public boolean isVariable()
	{
		return true;
	}

	/*---------------------
	 * PNodeVector methods
	 */
	
	@Override
	public PNodeVector createNodeVector(Node[] nodes)
	{
		return new PVariableVector(nodes);
	}
	
	/*-------------------------
	 * PVariableVector methods
	 */
	
	public Variable getVariable(int index)
	{
		return (Variable)getModelerNode(index);
	}
	
	public Variable [] getVariableArray()
	{
		return (Variable[]) Supers.narrowArrayOf(Variable.class, 1, getModelerNodes());
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
		
		for (Variable v : getVariableArray())
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
		Variable [] vars = getVariableArray();
		for (int i = 0; i < vars.length; i++)
		{
			retval[i] = vars[i].getGuess();
		}
		return retval;
	}
	
	public PDomain getDomain()
	{
		return PHelpers.wrapDomain(getVariable(0).getDomain());
	}
	
	Variable [] getModelerVariables()
	{
		Node [] nodes = getModelerNodes();
		Variable [] retval = new Variable[nodes.length];
		for (int i = 0; i < retval.length; i++)
			retval[i] = (Variable)nodes[i];
		return retval;
	}

}
