/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.template;

import java.util.Arrays;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

/**
 * Solvers are responsible for managing memory required for messages.
 * It is recommended that the variable solver class manage the messages.
 * 
 * @author shershey
 *
 */
public class SVariable extends SDiscreteVariableBase
{
	private double [] _input;
	private double [][] _inputMessages = new double[0][];
	private double [][] _outputMessages = new double[0][];
	
	public SVariable(VariableBase var) 
	{
		super(var);
	}

	/**
	 * This method is called by the model object when the solver object is first created
	 * or any time input or fixedValue changes.  The method can be ignored if the solver
	 * wants to pull down the input object or fixedValue when it needs it, but, for  performance
	 * reasons, it'soften good to cache the input.
	 */
	@Override
	public void setInputOrFixedValue(Object input, Object fixedValue,
			boolean hasFixedValue) 
	{
		//Don't support fixed value with this example solver.
		if (hasFixedValue)
			throw new DimpleException("Not supported");
		
		if (_input == null)
			_input = new double[_var.asDiscreteVariable().getDiscreteDomain().size()];
		else
			_input = (double[])input;
	}

	/**
	 * This method is responsible for creating the messages associated with 
	 * an edge connecting the variable to the specified factor.  It must return
	 * an object array where the first item is a double array for the input message
	 * and the second item is a double array for the output message.
	 *
	 */
	@Override
	public Object[] createMessages(ISolverFactor factor) 
	{
		//Retrieve the variable port associated with this factor
		int portNum = _var.getPortNum(factor.getModelObject());
		
		//Resize the message arrays.
		int newArraySize = Math.max(_inputMessages.length,portNum + 1);
		_inputMessages = Arrays.copyOf(_inputMessages,newArraySize);
		_outputMessages = Arrays.copyOf(_outputMessages,newArraySize);
		
		//Get the domain length and create a message of the right size 
		int domainLength = _var.asDiscreteVariable().getDiscreteDomain().size();
		_inputMessages[portNum] = new double[domainLength];
		_outputMessages[portNum] = new double[domainLength];

		/**
		 * Return the new messages so that the factor can cache them.
		 */
		return new Object [] {_inputMessages[portNum],_outputMessages[portNum]};
	}

	/**
	 * This method is called to initialize the input.
	 */
	@Override
	public Object resetInputMessage(Object message) 
	{
		double [] retval = (double[])_input;
		for (int i = 0; i < retval.length; i++)
			retval[i] = 0;
		return retval;
	}

	/**
	 * This is the main work horse of the variable message passing.
	 * This is a toy solver that simply adds all input messages and the input
	 * to create an output message.
	 *  
	 * Developers can also override update() if they want to speed up computation
	 * when terms can be reused.
	 */
	@Override
	public void updateEdge(int outPortNum) 
	{
		double [] output = _outputMessages[outPortNum];
		
		for (int i = 0; i < output.length; i++)
		{
			output[i] = _input[i];
		}
		
		for (int i = 0; i < _inputMessages.length; i++)
		{
			if (i != outPortNum)
			{
				for (int j = 0; j < output.length; j++)
				{
					output[j] += _inputMessages[i][j];
				}
			}
		}
	}

	/**
	 * This method is called during initialize() to reset the input and output
	 * messages.
	 */
	@Override
	public void resetEdgeMessages(int portNum) 
	{
		double [] tmp = (double[])_inputMessages[portNum];
		
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = 0;
		
		tmp = (double[])_outputMessages[portNum];

		for (int i = 0; i < tmp.length; i++)
			tmp[i] = 0;
		
	}

	/**
	 * This method is used for introspection
	 */
	@Override
	public Object getInputMsg(int portIndex) 
	{
		return _inputMessages[portIndex];
	}

	/**
	 * This method is used for introspection
	 */
	@Override
	public Object getOutputMsg(int portIndex) 
	{
		return _outputMessages[portIndex];
	}

	/**
	 * This method must be implemented if the solver is to support streaming graphs.
	 */
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum) 
	{
		throw new DimpleException("rolled up graphs not supported");
	}

	/**
	 * This method calculates the belief for a given variable.
	 * This toy solver simply sums together all messages and the input. 
	 */
	@Override
	public double[] getBelief() 
	{
		double [] output = new double[_input.length];
		
		for (int i = 0; i < output.length; i++)
		{
			output[i] = _input[i];
		}
		
		for (int i = 0; i < _inputMessages.length; i++)
		{
			for (int j = 0; j < output.length; j++)
			{
				output[j] += _inputMessages[i][j];
			}
		}
		return output;		
	}

}
