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


public class PRealJointVariableVector extends PVariableVector
{
	public PRealJointVariableVector(IPNode [] nodes)
	{
		super(nodes);
	}
	
	public PRealJointVariableVector(String varType, PRealJointDomain domain, int numElements) 
	{
		IPNode [] nodes = new IPNode[numElements];
		
		for (int i = 0; i < numElements; i++)
		{
			//TODO: do we really want that here?
			//int id = NodeId.getNext();
			
			PRealJointVariable v = new PRealJointVariable(varType,domain);
			nodes[i] = v;
		}
		setNodes(nodes);
	}
	
	public PRealJointVariableVector(PVariableBase [] variables)
	{
		super(variables);
	}
		
	public PRealJointVariable getRealJointVariable(int index)
	{
		return (PRealJointVariable)getNode(index);
	}
	
	public void setInput(int [] indices, Object input) 
	{
		for (int i = 0; i < indices.length; i++)
			getRealJointVariable(indices[i]).setInput(input);
	}

	public Object [] getBeliefs(int [] indices) 
	{
		Object [] beliefs = new Object[indices.length];
		
		for (int i = 0; i < indices.length; i++)
		{
			beliefs[i] = getRealJointVariable(indices[i]).getBelief();
		}
		return beliefs;
	}
	
	@Override
	public PNodeVector createNodeVector(IPNode[] nodes) 
	{
		return new PRealJointVariableVector(nodes);
	}

}
