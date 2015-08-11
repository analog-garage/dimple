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

package com.analog.lyric.dimple.solvers.sumproduct.customFactors;

import java.util.List;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.sumproduct.SFiniteFieldFactor;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductFiniteFieldVariable;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;



@SuppressWarnings("deprecation") // TODO remove when SFiniteFieldFactor removed
public class CustomFiniteFieldConstantMult extends SFiniteFieldFactor
{
	
	private int _constant;
	private int _dlogConstant;
	private SumProductFiniteFieldVariable _varInput;
	private SumProductFiniteFieldVariable _varOutput;
	//private Port _varInputPort;
	//private Port _varOutputPort;
	
	public CustomFiniteFieldConstantMult(Factor factor, SumProductSolverGraph parent)
	{
		super(factor, parent);
		
		final List<Value> constants = _model.getConstantValues();
		if (constants.size() != 1)
			throw new DimpleException("expected one constant");

		if (factor.getSiblingCount() != 2)
			throw new DimpleException("finiteFieldMult expects two variable arguments");
		
		//ArrayList<Port> ports = _factor.getPorts();

		_varInput = (SumProductFiniteFieldVariable)getSibling(0);
		//_varInputPort = ports.get(0);

		_varOutput = (SumProductFiniteFieldVariable)getSibling(1);
		
		//_varOutputPort = ports.get(1);
		assignConstant(constants.get(0).getInt());

		
		
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
	
	@Override
	public void doUpdateEdge(int outPortNum)
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
		double [] inputMsg = getSiblingEdgeState(0).factorToVarMsg.representation();
		double [] outputMsg = getSiblingEdgeState(1).varToFactorMsg.representation();
		
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
		double [] inputMsg = getSiblingEdgeState(0).varToFactorMsg.representation();
		double [] outputMsg = getSiblingEdgeState(1).factorToVarMsg.representation();
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
}
