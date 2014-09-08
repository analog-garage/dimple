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
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.util.misc.Matlab;



/*
 * This class provides vectors of Variables to allow matlab to set multiple inputs
 * simultaneously.  This is important for performance reasons.  (MATLAB is slow to
 * manipulate lots of objects)
 */
@Matlab
public class PDiscreteVariableVector extends PVariableVector
{
	/*---------------
	 * Construction
	 */
	
	public PDiscreteVariableVector()
	{
	}

	public PDiscreteVariableVector(Node node)
	{
		super(new Node[] {node});
	}
	
	public PDiscreteVariableVector(Node [] nodes)
	{
		super(nodes);
	}
	
	public PDiscreteVariableVector(String varType, PDiscreteDomain domain, int numElements)
	{
		Node [] nodes  = new Node[numElements];
		
		for (int i = 0; i < numElements; i++)
		{
			Discrete v = new Discrete(domain.getModelerObject(), varType);
			nodes[i] = v;
		}
		
		setNodes(nodes);
	}
	
	public PDiscreteVariableVector(Variable [] variables)
	{
		super(variables);
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public boolean isDiscrete()
	{
		return true;
	}
	
	/*---------------------------
	 * PDiscreteVariable methods
	 */
	
	private Discrete getDiscreteVariable(int index)
	{
		return (Discrete)getModelerNode(index);
	}
	
	public void setInput(int [] indices,double [][] inputs)
	{
		for (int i = 0; i < indices.length; i++)
		{
			int index = indices[i];
			getDiscreteVariable(index).setInput(inputs[i]);
		}
	}
	
	public void setFixedValueIndices(int[] indices, int[] fixedValueIndices)
	{
		for (int i = 0; i < indices.length; i++)
			getDiscreteVariable(indices[i]).setFixedValueIndex(fixedValueIndices[i]);
	}
	public int[] getFixedValueIndices(int[] indices)
	{
		int[] output = new int[indices.length];
		for (int i = 0; i < indices.length; i++)
			output[i] = getDiscreteVariable(indices[i]).getFixedValueIndex();
		
		return output;
	}
	public boolean[] hasFixedValue(int[] indices)
	{
		boolean[] output = new boolean[indices.length];
		for (int i = 0; i < indices.length; i++)
			output[i] = getDiscreteVariable(indices[i]).hasFixedValue();
		
		return output;
	}
	
	public double [][] getDiscreteBeliefs(int [] indices)
	{
		double [][] beliefs = new double[indices.length][];
		
		for (int i = 0; i < indices.length; i++)
		{
			Discrete df = getDiscreteVariable(indices[i]);
			beliefs[i] = df.getBelief();
		}
		return beliefs;
		
	}

	public Object [] getBeliefs(int [] indices)
	{
		Object [] beliefs = new Object[indices.length];
		
		for (int i = 0; i < indices.length; i++)
			beliefs[i] = getDiscreteVariable(indices[i]).getBelief();
		return beliefs;
	}
	
	public int[] getValueIndices(int[] indices)
	{
		int[] valueIndices = new int[indices.length];
		
		// Since this is specifically for MATLAB, add one so indices are suitable for one-based addressing
		for (int i = 0; i < indices.length; i++)
			valueIndices[i] = getDiscreteVariable(indices[i]).getValueIndex() + 1;
		return valueIndices;
	}
	
	public double [][] getInput(int [] indices)
	{
		double [][] output = new double[size()][];
		for (int i = 0; i < indices.length; i++)
			output[i] = getDiscreteVariable(indices[i]).getInput();
		
		return output;
	}

	
	@Override
	public PNodeVector createNodeVector(Node [] nodes)
	{
		return new PDiscreteVariableVector(nodes);
	}
	
	@Override
	public PDomain getDomain()
	{
		return new PDiscreteDomain(getDiscreteVariable(0).getDiscreteDomain());
	}
	

}
