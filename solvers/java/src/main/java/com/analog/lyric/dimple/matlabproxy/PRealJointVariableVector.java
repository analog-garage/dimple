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
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.Variable;
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
			RealJoint v = new RealJoint(domain.getModelerObject(), varType);
			nodes[i] = v;
		}
		setNodes(nodes);
	}
	
	public PRealJointVariableVector(Variable [] variables)
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

	public Object[] getInput(int[] indices)
	{
		Object[] output = new Object[size()];
		for (int i = 0; i < indices.length; i++)
			output[i] = getRealJointVariable(indices[i]).getInput();
		
		return output;
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
	
	public double[][] getValues(int[] indices)
	{
		double[][] values = new double[indices.length][];
		
		for (int i = 0; i < indices.length; i++)
			values[i] = getRealJointVariable(indices[i]).getValue();
		
		return values;
	}
	
	public void setFixedValues(int[] indices, double[][] fixedValues)
	{
		for (int i = 0; i < indices.length; i++)
			getRealJointVariable(indices[i]).setFixedValue(fixedValues[i]);
	}
	public double[][] getFixedValues(int[] indices)
	{
		double[][] output = new double[indices.length][];
		for (int i = 0; i < indices.length; i++)
			output[i] = getRealJointVariable(indices[i]).getFixedValue();
		
		return output;
	}
	public boolean[] hasFixedValue(int[] indices)
	{
		boolean[] output = new boolean[indices.length];
		for (int i = 0; i < indices.length; i++)
			output[i] = getRealJointVariable(indices[i]).hasFixedValue();
		
		return output;
	}
	
	// Set guess when there's just one variable
	public void setGuess(double[] guess)
	{
		getVariable(0).setGuess(guess);
	}
	
	// Set guess for multiple variables
	public void setGuess(double[][] guess)
	{
		for (int i = 0; i < guess.length; i++)
		{
			((RealJoint)getVariable(i)).setGuess(guess[i]);
		}
	}
	

	
	@Override
	public PNodeVector createNodeVector(Node[] nodes)
	{
		return new PRealJointVariableVector(nodes);
	}

}
