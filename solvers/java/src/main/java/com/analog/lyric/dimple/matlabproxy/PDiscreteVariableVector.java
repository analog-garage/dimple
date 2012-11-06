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

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.NodeId;



/*
 * This class provides vectors of Variables to allow matlab to set multiple inputs 
 * simultaneously.  This is important for performance reasons.  (MATLAB is slow to
 * manipulate lots of objects)
 */
public class PDiscreteVariableVector extends PVariableVector
{
	public PDiscreteVariableVector(IPNode [] nodes)
	{
		super(nodes);
	}
	
	public PDiscreteVariableVector(String varType, PDiscreteDomain domain, int numElements) 
	{
		IPNode [] nodes  = new IPNode[numElements];
		
		for (int i = 0; i < numElements; i++)
		{
			//TODO: do we really want that here?
			int id = NodeId.getNext();

			Discrete v = new Discrete(id, (DiscreteDomain)domain.getModelerObject(),varType);
			nodes[i] = new PDiscreteVariable(v);
		}
		
		setNodes(nodes);
	}
	
	public PDiscreteVariableVector(PVariableBase [] variables)
	{
		super(variables);
	}
	
	PDiscreteVariable getDiscreteVariable(int index)
	{
		return (PDiscreteVariable)getNode(index);
	}
	
	public void setInput(int [] indices,double [][] inputs) 
	{		
		for (int i = 0; i < indices.length; i++)
		{
			int index = indices[i];
			getDiscreteVariable(index).setInput(inputs[i]);
		}
	}
	
	
	public Object [] getBeliefs(int [] indices) 
	{
		Object [] beliefs = new Object[indices.length];
		
		for (int i = 0; i < indices.length; i++)
			beliefs[i] = getDiscreteVariable(indices[i]).getBelief();
		return beliefs;
	}

	
	public double [][] getInput(int [] indices) 
	{
		double [][] output = new double[size()][];
		for (int i = 0; i < indices.length; i++)
			output[i] = (double[])getDiscreteVariable(indices[i]).getInput();
		
		return output;
	}

	
	@Override
	public PNodeVector createNodeVector(IPNode[] nodes) 
	{
		return new PDiscreteVariableVector(nodes);
	}
	
}
