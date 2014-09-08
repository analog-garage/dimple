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

import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableDoubleArray;

/**
 * Solvers are responsible for managing memory required for messages.
 * It is recommended that the variable solver class manage the messages.
 * 
 * The template SVariable extends the SDiscreteVariableDoubleArray,
 * which takes care of message memory management.
 * 
 * @author shershey
 *
 */
public class SVariable extends SDiscreteVariableDoubleArray
{
	public SVariable(Variable var)
	{
		super(var);
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
	protected void doUpdateEdge(int outPortNum)
	{
		double [] output = _outputMessages[outPortNum];
		
		System.arraycopy(_input, 0, output, 0, output.length);
		
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
	 * This method calculates the belief for a given variable.
	 * This toy solver simply sums together all messages and the input.
	 */
	@Override
	public double[] getBelief()
	{
		double [] output = new double[_input.length];
		
		System.arraycopy(_input, 0, output, 0, output.length);
		
		for (int i = 0; i < _inputMessages.length; i++)
		{
			for (int j = 0; j < output.length; j++)
			{
				output[j] += _inputMessages[i][j];
			}
		}
		return output;
	}
	/**
	 * This method is called to initialize the input.
	 */
	@Override
	public double[] resetInputMessage(Object message)
	{
		final double[] result = (double[])message;
		Arrays.fill(result, 0);
		return result;
	}


}
