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

import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.util.misc.Matlab;



/*
 * This class provides vectors of Reals to allow matlab to set multiple inputs
 * simultaneously.  This is important for performance reasons.  (MATLAB is slow to
 * manipulate lots of objects)
 */
@Matlab
public class PRealVariableVector extends PVariableVector
{
	/*--------------
	 * Construction
	 */
	
	public PRealVariableVector(Real real)
	{
		super(new Node [] {real});
	}

	public PRealVariableVector(Node [] nodes)
	{
		super(nodes);
	}
	
	public PRealVariableVector(String varType, PRealDomain domain, int numElements)
	{
		Node [] nodes = new Node[numElements];
		
		for (int i = 0; i < numElements; i++)
		{
			Real v = new Real(domain.getModelerObject(), varType);
			nodes[i] = v;
		}
		setNodes(nodes);
	}
	
	public PRealVariableVector(Variable [] variables)
	{
		super(variables);
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public boolean isReal()
	{
		return true;
	}
	
	/*---------------------
	 * PNodeVector methods
	 */

	@Override
	public PNodeVector createNodeVector(Node[] nodes)
	{
		return new PRealVariableVector(nodes);
	}

	/*-----------------------------
	 * PRealVariableVector methods
	 */
	
	private Real getRealVariable(int index)
	{
		return (Real)getModelerNode(index);
	}
	
	public Object [] getBeliefs(int [] indices)
	{
		Object [] beliefs = new Object[indices.length];
		
		for (int i = 0; i < indices.length; i++)
		{
			beliefs[i] = getRealVariable(indices[i]).getBeliefObject();
		}
		return beliefs;
	}
	
	public double[] getValues(int[] indices)
	{
		double[] values = new double[indices.length];
		
		for (int i = 0; i < indices.length; i++)
			values[i] = getRealVariable(indices[i]).getValue();
		
		return values;
	}

	
	public void setInput(int [] indices, Object factorFunction)
	{
		for (int i = 0; i < indices.length; i++)
			getRealVariable(indices[i]).setInputObject(factorFunction);
	}

	
	public void setFixedValues(int[] indices, double[] fixedValues)
	{
		for (int i = 0; i < indices.length; i++)
			getRealVariable(indices[i]).setFixedValue(fixedValues[i]);
	}
	public double[] getFixedValues(int[] indices)
	{
		double[] output = new double[indices.length];
		for (int i = 0; i < indices.length; i++)
			output[i] = getRealVariable(indices[i]).getFixedValue();
		
		return output;
	}
	public boolean[] hasFixedValue(int[] indices)
	{
		boolean[] output = new boolean[indices.length];
		for (int i = 0; i < indices.length; i++)
			output[i] = getRealVariable(indices[i]).hasFixedValue();
		
		return output;
	}

	
	
	public Object[] getInput(int[] indices)
	{
		Object[] output = new Object[size()];
		for (int i = 0; i < indices.length; i++)
			output[i] = getRealVariable(indices[i]).getInput();
		
		return output;
	}
	
}
