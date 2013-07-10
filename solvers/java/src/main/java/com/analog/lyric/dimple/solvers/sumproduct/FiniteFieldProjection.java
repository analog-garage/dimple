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

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.ArrayList;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.VariableList;



public class FiniteFieldProjection extends FiniteFieldFactor
{

	private FiniteFieldVariable _ffVar;
	private int [] _portIndex2bitIndex;
	private int [] _bit2port;
	
	public FiniteFieldProjection(Factor factor)
	{
		super(factor);
		
	
		VariableList variables = factor.getVariables();
		
		ArrayList<INode> ports = _factor.getSiblings();
		
		//First variable is the FiniteFieldVariable
		//Other variables should be bits.
		_ffVar = (FiniteFieldVariable)variables.getByIndex(0).getSolver();
		_portIndex2bitIndex = new int[ports.size()];
		
		for (int i = 0; i < ports.size(); i++)
			_portIndex2bitIndex[i] = -1;
		
		if (variables.size() <= 1)
			throw new DimpleException("need to specify at least one bit for projection");
		
				
		//get constant value and make sure it's in range
		FactorFunctionWithConstants ff = (FactorFunctionWithConstants)_factor.getFactorFunction();
		int [] constIndices = ff.getConstantIndices();
		Object [] constants = ff.getConstants();
		if (constIndices.length != 1)
			throw new DimpleException("expected one constant to specify the array of bit positions");

		double [] domain = (double[])constants[0];
		
		if (variables.size() != 1+domain.length)
			throw new DimpleException("expect finite field variable, bit positions, and bits");

		
		_bit2port = new int[ports.size()-1];
		
		for (int i = 1; i < variables.size(); i++)
		{
			//TODO: error check
			int index = (int)domain[i-1];
			
			if (index < 0 || index >= ports.size()-1)
				throw new DimpleException("index out of range");
			if (_bit2port[index] != 0)
				throw new DimpleException("Tried to set index twice");
			
			//get Variable and make sure it's a bit.
			Discrete bit = (Discrete)variables.getByIndex(i);
			
			Object [] bitDomain = bit.getDiscreteDomain().getElements();
			if (bitDomain.length != 2 || (Double)bitDomain[0] != 0 || (Double)bitDomain[1] != 1)
				throw new DimpleException("expected bit");
			
			_bit2port[index] = i;
			_portIndex2bitIndex[i] = index;
		}
	}

	@Override
	public void updateEdge(int outPortNum)
	{
		if (outPortNum == 0)
			updateFiniteField();
		else
		{
			if (outPortNum >= 1)
				updateBit(outPortNum);
		}
	}
	
	public void updateFiniteField()
	{
		//for every value of the finite field
		//TODO: cast shouldn't be necessary
		double [] outputs = _outputMsgs[0];
		int numBits = _ffVar.getNumBits();

		double prod;
		double [][] inputMsgs = new double[numBits][];
		for (int i = 0; i < _inputMsgs.length-1; i++)
		{
			if (_inputMsgs[i+1] != null)
				inputMsgs[i] = _inputMsgs[i+1];
		}
		
		//Multiply bit probabilities
		double sum = 0;
		
		for (int i = 0, end = ((Discrete)_ffVar.getVariable()).getDiscreteDomain().size(); i < end; i++)
		{
			prod = 1;
			for (int j = 0; j < numBits; j++)
			{
				int p = _bit2port[j];
				
				if (p != 0)
				{
					if (((i >> j) & 1) == 1)
					{
						//is one
						prod *= inputMsgs[j][1];
					}
					else
					{
						prod *= inputMsgs[j][0];
					}
				}
				
			}
			outputs[i] = prod;
			sum += prod;
		}
		
		//normalize
		for (int i = 0; i < outputs.length; i++)
			outputs[i] /= sum;

	}
	
	public void updateBit(int portNum)
	{
		
		//get output msg for bit
		double [] outputs = _outputMsgs[portNum];
		
		//init to 1 for each
		outputs[0] = 0;
		outputs[1] = 0;
				
		int bit = _portIndex2bitIndex[portNum];
		
		//Iterate each value of finite field
		double [] inputs = _inputMsgs[0];
		
		for (int i = 0; i < inputs.length; i++)
		{
			//extract value of bit of interest
			if (((i >> bit) & 1) == 1)
			{
				//bit was one
				outputs[1] += inputs[i];
			}
			else
			{
				//bit was zero
				outputs[0] += inputs[i];
			}
			
			
		}
		
		//normalize
		double sum = outputs[0]+outputs[1];
		outputs[0] /= sum;
		outputs[1] /= sum;
		
		
	}
}
