/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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
	public PRealJointVariableVector(String varType, PRealJointDomain domain, int numElements) 
	{
		_variables = new PRealJointVariable[numElements];
		
		for (int i = 0; i < numElements; i++)
		{
			//TODO: do we really want that here?
			//int id = NodeId.getNext();
			
			PRealJointVariable v = new PRealJointVariable(varType,domain);
			_variables[i] = v;
		}
	}
	
	public PRealJointVariableVector(PVariableBase [] variables)
	{
		super(variables);
	}
		
	public void setInput(int [] indices, Object input) 
	{
		for (int i = 0; i < indices.length; i++)
			((PRealJointVariable)_variables[indices[i]]).setInput(input);
	}

	public Object [] getBeliefs(int [] indices) 
	{
		Object [] beliefs = new Object[indices.length];
		
		for (int i = 0; i < indices.length; i++)
		{
			beliefs[i] = ((PRealJointVariable)_variables[indices[i]]).getBelief();
		}
		return beliefs;
	}

}
