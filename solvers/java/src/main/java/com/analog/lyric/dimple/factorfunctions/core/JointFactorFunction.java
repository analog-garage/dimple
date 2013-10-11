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

package com.analog.lyric.dimple.factorfunctions.core;

import java.util.ArrayList;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;

public class JointFactorFunction extends FactorFunction
{
	private FactorFunctionBase _factor1;
	private FactorFunctionBase _factor2;
	private ArrayList<Integer> _inputsForFf1;
	private ArrayList<Integer> _inputsForFf2;
	private int _newNumInputs;

	public JointFactorFunction(String name,FactorFunctionBase ff1, FactorFunctionBase ff2,
			ArrayList<Integer> inputsForFf1, ArrayList<Integer> inputsForFf2)
	{
		super(name);
		
		_newNumInputs = 0;
		for (int i = 0; i < inputsForFf1.size(); i++)
			if (_newNumInputs < (inputsForFf1.get(i)+1))
				_newNumInputs = inputsForFf1.get(i)+1;
		for (int i = 0; i < inputsForFf2.size(); i++)
			if (_newNumInputs < (inputsForFf2.get(i)+1))
				_newNumInputs = inputsForFf2.get(i)+1;
		
		_factor1 = ff1;
		_factor2 = ff2;
		_inputsForFf1 = inputsForFf1;
		_inputsForFf2 = inputsForFf2;
		
	}

	@Override
	public double eval(Object... input)
	{
		//Make sure length of inputs is correct
		if (input.length != _newNumInputs)
			throw new DimpleException("expected " + _newNumInputs + " args");
		
		//map inputs to input for factor function 1 and factor function 2
		Object [] input1 = new Object[_inputsForFf1.size()];
		for (int i = 0; i < _inputsForFf1.size(); i++)
			input1[i] = input[_inputsForFf1.get(i)];
		
		Object [] input2 = new Object[_inputsForFf2.size()];
		for (int i = 0; i < _inputsForFf2.size(); i++)
			input2[i] = input[_inputsForFf2.get(i)];
		
		
		double prod =  _factor1.eval(input1)*_factor2.eval(input2);
		return prod;
	}
	
	@Override
	public double evalEnergy(Object... input)
	{
		//Make sure length of inputs is correct
		if (input.length != _newNumInputs)
			throw new DimpleException("expected " + _newNumInputs + " args");
		
		//map inputs to input for factor function 1 and factor function 2
		Object [] input1 = new Object[_inputsForFf1.size()];
		for (int i = 0; i < _inputsForFf1.size(); i++)
			input1[i] = input[_inputsForFf1.get(i)];
		
		Object [] input2 = new Object[_inputsForFf2.size()];
		for (int i = 0; i < _inputsForFf2.size(); i++)
			input2[i] = input[_inputsForFf2.get(i)];
		
		return _factor1.evalEnergy(input1) + _factor2.evalEnergy(input2);
	}

	@Override
	protected IFactorTable createTableForDomains(JointDomainIndexer domains)
	{
		// TODO: provide a more efficient implementation
		return super.createTableForDomains(domains);
	}
}
