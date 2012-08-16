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

import com.analog.lyric.dimple.model.NodeId;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;



/*
 * This class provides vectors of Reals to allow matlab to set multiple inputs 
 * simultaneously.  This is important for performance reasons.  (MATLAB is slow to
 * manipulate lots of objects)
 */
public class PRealVariableVector extends PVariableVector
{
	
	public PRealVariableVector(String varType, PRealDomain domain, Object input, int numElements) 
	{
		_variables = new PRealVariable[numElements];
		
		for (int i = 0; i < numElements; i++)
		{
			//TODO: do we really want that here?
			int id = NodeId.getNext();
			
			Real v = new Real(id,varType,(RealDomain)domain.getModelerObject(),input);
			_variables[i] = new PRealVariable(v);
		}
	}
	
	public PRealVariableVector(PVariableBase [] variables)
	{
		super(variables);
	}
	
	public Object [] getBeliefs(int [] indices) 
	{
		Object [] beliefs = new Object[indices.length];
		
		for (int i = 0; i < indices.length; i++)
		{
			beliefs[i] = ((PRealVariable)_variables[indices[i]]).getBelief();
		}
		return beliefs;
	}

	
	public void setInput(int [] indices, Object factorFunction) 
	{
		for (int i = 0; i < indices.length; i++)
			((PRealVariable)_variables[indices[i]]).setInput(factorFunction);
	}

	
	
	public Object[] getInput(int[] indices) 
	{
		Object[] output = new Object[_variables.length];
		for (int i = 0; i < indices.length; i++)
			output[i] = ((PRealVariable)_variables[indices[i]]).getInput();
		
		return output;
	}

}
