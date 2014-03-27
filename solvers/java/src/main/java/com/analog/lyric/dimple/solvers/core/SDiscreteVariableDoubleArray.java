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

package com.analog.lyric.dimple.solvers.core;

import java.util.Arrays;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

/**
 * @since 0.05
 */
public abstract class SDiscreteVariableDoubleArray extends SDiscreteVariableBase
{
	protected double [] _input;
	protected double [][] _inputMessages = new double[0][];
	protected double [][] _outputMessages = new double[0][];
	
	public SDiscreteVariableDoubleArray(VariableBase var)
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
		if (input == null)
		{
			_input = createDefaultMessage();
		}
		else
		{
	    	double[] vals = (double[])input;
	    	if (vals.length != _var.asDiscreteVariable().getDiscreteDomain().size())
	    		throw new DimpleException("length of priors does not match domain");
	    	
	    	_input = vals;
		}
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
		
		_inputMessages[portNum] = createDefaultMessage();
		_outputMessages[portNum] = createDefaultMessage();

		/**
		 * Return the new messages so that the factor can cache them.
		 */
		return new Object [] {_inputMessages[portNum],_outputMessages[portNum]};
	}

	

	/**
	 * This method is called during initialize() to reset the input and output
	 * messages.
	 */
	@Override
	public void resetEdgeMessages(int portNum)
	{
		resetInputMessage(_inputMessages[portNum]);
		resetInputMessage(_outputMessages[portNum]);
		
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
		SDiscreteVariableDoubleArray sother = (SDiscreteVariableDoubleArray)other;
		_inputMessages[thisPortNum] = sother._inputMessages[otherPortNum];
		_outputMessages[thisPortNum] = sother._outputMessages[otherPortNum];

	}

	public double [] createDefaultMessage()
	{
		//TODO: both variable and factor do this.  Why doesn't factor just ask variable?
		int domainLength = _var.asDiscreteVariable().getDiscreteDomain().size();
    	double[] retVal = new double[domainLength];
    	return (double[])resetInputMessage(retVal);
    }
	

	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		_inputMessages[portIndex] = (double[])obj;
		
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		double [] tmp = (double[])obj;
		for (int i = 0; i <tmp.length; i++)
			_inputMessages[portIndex][i] = tmp[i];
	}
	
	@Override
	public void setOutputMsgValues(int portIndex, Object obj)
	{
		double [] tmp = (double[])obj;
		for (int i = 0; i <tmp.length; i++)
			_outputMessages[portIndex][i] = tmp[i];
	}


}
