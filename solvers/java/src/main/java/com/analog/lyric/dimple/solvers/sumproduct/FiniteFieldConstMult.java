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
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.solvers.core.SFactorBase;



public class FiniteFieldConstMult extends SFactorBase
{
	
	private int _constant;
	private int _dlogConstant;
	private FiniteFieldVariable _varInput;
	private FiniteFieldVariable _varOutput;
	private Port _varInputPort;
	private Port _varOutputPort;
	
	public FiniteFieldConstMult(Factor factor)  
	{
		super(factor);
		
		FactorFunctionWithConstants ff = (FactorFunctionWithConstants)_factor.getFactorFunction();
		int [] constIndices = ff.getConstantIndices();
		Object [] constants = ff.getConstants();
		if (constIndices.length != 1)
			throw new DimpleException("expected one constant");

		
		
		VariableList variables = factor.getVariables();
		
		if (variables.size() != 2)
			throw new DimpleException("finiteFieldMult expects two variable arguments");
		
		
		ArrayList<Port> ports = _factor.getPorts();

		_varInput = (FiniteFieldVariable)variables.getByIndex(0).getSolver();
		_varInputPort = ports.get(0);

		_varOutput = (FiniteFieldVariable)variables.getByIndex(1).getSolver();
		
		_varOutputPort = ports.get(1);
		assignConstant((int)(double)(Double)constants[0]);

		
		
		if (_varInput.getTables().getPoly() != _varOutput.getTables().getPoly())
		{
			throw new DimpleException("Variables have different primitive polynomials.  This is not currently supported");
		}
		
		//Make sure primitive polynomials match
		//TODO: make sure this is possible before casting
	}
	
	private void assignConstant(int val) 
	{
		//TODO: error check before casting
		int poly = val;
	
		
		_constant = poly; //FiniteFieldVariable.convertDoubleArray2poly(poly);
	
		if (_constant == 0)
			throw new DimpleException("Multiplication by zero not supported");
		
		_dlogConstant = _varInput.getTables().getDlogTable()[_constant];
	}

	public int getConstant()
	{
		return -1;
	}
	
	public void updateEdge(int outPortNum) 
	{
		switch (outPortNum)
		{
		case 0:
			//we want to update the multiply input
			updateMultInputEdge();
			break;
		case 1:
			//we want to update the multiply output
			updateMultOutputEdge();
			break;
		default:
			throw new DimpleException("unexpected port num");
		}
	}
	
	private void updateMultInputEdge()
	{
		//TODO: can I get rid of cast?
		double [] inputMsg = (double[])_varInputPort.getOutputMsg();
		double [] outputMsg = (double[])_varOutputPort.getInputMsg();
		
		int [] outputDlogTable = _varOutput.getTables().getDlogTable();
		int [] inputPowerTable = _varInput.getTables().getPowerTable();
		
		int dlogSum = 0;
		int length = inputMsg.length-1;
		int index = 0;
		
		//TODO: special case multiplication by constant of zero.		
		//TODO: check same size?
		
		inputMsg[0] = outputMsg[0];
		
		
		for (int i = 1; i < inputMsg.length; i++)
		{
			dlogSum = (outputDlogTable[i]-_dlogConstant+length)%length;
			index = inputPowerTable[dlogSum];
			inputMsg[index] = outputMsg[i];
		}

	}
	
	private void updateMultOutputEdge()
	{
		//TODO: can I get rid of cast?
		double [] inputMsg = (double[])_varInputPort.getInputMsg();
		double [] outputMsg = (double[])_varOutputPort.getOutputMsg();
		int [] inputDlogTable = _varInput.getTables().getDlogTable();
		int [] outputPowerTable = _varOutput.getTables().getPowerTable();
		
		int dlogSum = 0;
		int length = inputMsg.length-1;
		int index = 0;
		
		//TODO: special case multiplication by constant of zero.		
		//TODO: check same size?
		
		outputMsg[0] = inputMsg[0];
		
		for (int i = 1; i < inputMsg.length; i++)
		{			
			dlogSum = (inputDlogTable[i]+_dlogConstant)%length;
			index = outputPowerTable[dlogSum];
			outputMsg[index] = inputMsg[i];
		}
		
	}
	

	@Override
	public void initialize() 
	{
		
	}

	@Override
	public double getEnergy()  
	{
		throw new DimpleException("getEnergy not yet supported for FiniteFieldConstMult");
	}
	
}
