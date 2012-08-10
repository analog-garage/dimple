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
