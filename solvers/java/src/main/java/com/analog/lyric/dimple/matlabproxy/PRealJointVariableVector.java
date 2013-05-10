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

import com.analog.lyric.dimple.model.Node;
import com.analog.lyric.dimple.model.NodeId;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.util.misc.Matlab;

@Matlab
public class PRealJointVariableVector extends PVariableVector
{
	/*--------------
	 * Construction
	 */
	
	public PRealJointVariableVector(Node [] nodes)
	{
		super(nodes);
	}
	
	public PRealJointVariableVector(String varType, PRealJointDomain domain, int numElements)
	{
		Node [] nodes = new Node[numElements];
		
		for (int i = 0; i < numElements; i++)
		{
			//TODO: do we really want that here?
			//int id = NodeId.getNext();
			RealJoint v = new RealJoint(NodeId.getNext(),domain.getModelerObject(),varType);
			nodes[i] = v;
		}
		setNodes(nodes);
	}
	
	public PRealJointVariableVector(VariableBase [] variables)
	{
		super(variables);
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public boolean isJoint()
	{
		return true;
	}
	
	/*-----------------------------------
	 * PRealJointVariableVector methods
	 */
		
	private RealJoint getRealJointVariable(int index)
	{
		return (RealJoint)getModelerNode(index);
	}
	
	public void setInput(int [] indices, Object input)
	{
		for (int i = 0; i < indices.length; i++)
			getRealJointVariable(indices[i]).setInputObject(input);
	}

	public Object [] getBeliefs(int [] indices)
	{
		Object [] beliefs = new Object[indices.length];
		
		for (int i = 0; i < indices.length; i++)
		{
			beliefs[i] = getRealJointVariable(indices[i]).getBeliefObject();
		}
		return beliefs;
	}
	
	@Override
	public PNodeVector createNodeVector(Node[] nodes)
	{
		return new PRealJointVariableVector(nodes);
	}

}
