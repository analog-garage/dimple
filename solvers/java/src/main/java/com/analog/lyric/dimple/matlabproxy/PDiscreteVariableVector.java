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
	public PDiscreteVariableVector(String varType, PDiscreteDomain domain, int numElements) 
	{
		_variables = new PDiscreteVariable[numElements];
		
		for (int i = 0; i < numElements; i++)
		{
			//TODO: do we really want that here?
			int id = NodeId.getNext();

			Discrete v = new Discrete(id, (DiscreteDomain)domain.getModelerObject(),varType);
			_variables[i] = new PDiscreteVariable(v);
		}
	}
	
	public PDiscreteVariableVector(PVariableBase [] variables)
	{
		super(variables);
	}
	
	public void setInput(int [] indices,double [][] inputs) 
	{		
		for (int i = 0; i < indices.length; i++)
		{
			int index = indices[i];
			((PDiscreteVariable)_variables[index]).setInput(inputs[i]);
		}
	}
	
	
	public Object [] getBeliefs(int [] indices) 
	{
		Object [] beliefs = new Object[indices.length];
		
		for (int i = 0; i < indices.length; i++)
		{
			beliefs[i] = ((PDiscreteVariable)_variables[indices[i]]).getBelief();
		}
		return beliefs;
	}

	
	public double [][] getInput(int [] indices) 
	{
		double [][] output = new double[_variables.length][];
		for (int i = 0; i < indices.length; i++)
			output[i] = (double[])((PDiscreteVariable)_variables[indices[i]]).getInput();
		
		return output;
	}

}
