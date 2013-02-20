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

package com.analog.lyric.dimple.solvers.particleBP;


import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;


public class SRealFactor extends SFactorBase
{	
	protected Factor _realFactor;
	protected int _numPorts;
	protected double [][] _inPortMsgs;
	protected double [][] _outMsgArray;
	protected Object[][] _variableDomains;
	protected Object[] _variableValues;
	protected int[] _variableIndices;
	protected int[] _variableDomainLengths;
	protected boolean[] _realVariable;
	protected boolean _moreCombinations;
	protected double _beta = 1;


	
	public SRealFactor(Factor factor)
	{
		super(factor);
		_realFactor = factor;
	}
	

	public double getMarginalPotential(double value, int outPortIndex)
	{
		FactorFunction factorFunction = _realFactor.getFactorFunction();

        double marginal = 0;        
        initializeVariableCombinations();
		_variableValues[outPortIndex] = value;	// Use the specified value for the output port
        while (true)
        {
        	double prob = 1;
			try {prob = factorFunction.eval(_variableValues);} catch (Exception e) {e.printStackTrace(); System.exit(1);}
			if (_beta != 1) prob = Math.pow(prob, _beta);

        	for (int inPortNum = 0; inPortNum < _numPorts; inPortNum++)
        		if (inPortNum != outPortIndex)
        			prob *= _inPortMsgs[inPortNum][_variableIndices[inPortNum]];

        	marginal += prob;
        	
        	nextVariableCombination(outPortIndex, value);
        	if (!_moreCombinations) break;
        }
        
        // FIXME: Should do bounds checking
	    return -Math.log(marginal);
	}
	
	
	public void updateEdge(int outPortNum)
	{
		FactorFunction factorFunction = _realFactor.getFactorFunction();

        double[] outputMsgs = _outMsgArray[outPortNum];
    	int outputMsgLength = outputMsgs.length;
        for (int i = 0; i < outputMsgLength; i++) outputMsgs[i] = 0;
        
        initializeVariableCombinations();
        while (true)
        {
        	double prob = 1;
			try {prob = factorFunction.eval(_variableValues);} catch (Exception e) {e.printStackTrace(); System.exit(1);}
			if (_beta != 1) prob = Math.pow(prob, _beta);

        	for (int inPortNum = 0; inPortNum < _numPorts; inPortNum++)
        		if (inPortNum != outPortNum)
        			prob *= _inPortMsgs[inPortNum][_variableIndices[inPortNum]];

        	outputMsgs[_variableIndices[outPortNum]] += prob;
        	
        	nextVariableCombination();
        	if (!_moreCombinations) break;
        }
        
        double sum = 0; 
    	for (int i = 0; i < outputMsgLength; i++) sum += outputMsgs[i];
    	for (int i = 0; i < outputMsgLength; i++) outputMsgs[i] /= sum;
	}
	
	
	public void update()
	{
		FactorFunction factorFunction = _realFactor.getFactorFunction();

		for (int outPortNum = 0; outPortNum < _numPorts; outPortNum++)
		{
			double[] outputMsgs = _outMsgArray[outPortNum];
			int outputMsgLength = outputMsgs.length;
			for (int i = 0; i < outputMsgLength; i++) outputMsgs[i] = 0;

			initializeVariableCombinations();
			while (true)
			{
				double prob = 1;
				try {prob = factorFunction.eval(_variableValues);} catch (Exception e) {e.printStackTrace(); System.exit(1);}
				if (_beta != 1) prob = Math.pow(prob, _beta);

				for (int inPortNum = 0; inPortNum < _numPorts; inPortNum++)
					if (inPortNum != outPortNum)
						prob *= _inPortMsgs[inPortNum][_variableIndices[inPortNum]];

				outputMsgs[_variableIndices[outPortNum]] += prob;

				nextVariableCombination();
				if (!_moreCombinations) break;
			}

			double sum = 0; 
			for (int i = 0; i < outputMsgLength; i++) sum += outputMsgs[i];
			for (int i = 0; i < outputMsgLength; i++) outputMsgs[i] /= sum;
		}
	}
	
	
	
	protected void initializeVariableCombinations()
	{
		_moreCombinations = true;
		for (int iPort = 0; iPort < _numPorts; iPort++)
		{
			_variableIndices[iPort] = 0;
			_variableValues[iPort] = _variableDomains[iPort][0];
		}
	}
	
	// Walk through the dynamically generated equivalent of a combo table
	// Updates _variableIndices, _variableValues, and _moreCombinations
	// Uses _variableDomains
	protected void nextVariableCombination()
	{
		// Increment indices
		_moreCombinations = false;
		for (int i = 0; i < _numPorts; i++)
		{
			int newIndex = _variableIndices[i] + 1;

			if (newIndex >= _variableDomainLengths[i])
			{
				_variableIndices[i] = 0;
			}
			else
			{
				_variableIndices[i] = newIndex;
				_moreCombinations = true;
				break;
			}
		}
		
		// Get values for indices
		for (int i = 0; i < _numPorts; i++)
			_variableValues[i] =_variableDomains[i][_variableIndices[i]];
		
	}
	
	
	// Walk through the dynamically generated equivalent of a combo table
	// Skip the index of the exception variable and replace its value with the exceptionValue argument
	// Updates _variableIndices, _variableValues, and _moreCombinations
	// Uses _variableDomains
	protected void nextVariableCombination(int exceptionIndex, double exceptionValue)
	{
		// Increment indices
		_moreCombinations = false;
		for (int i = 0; i < _numPorts; i++)
		{
			if (i != exceptionIndex)
			{
				int newIndex = _variableIndices[i] + 1;

				if (newIndex >= _variableDomainLengths[i])
				{
					_variableIndices[i] = 0;
				}
				else
				{
					_variableIndices[i] = newIndex;
					_moreCombinations = true;
					break;
				}
			}
		}
		
		// Get values for indices
		for (int i = 0; i < _numPorts; i++)
			_variableValues[i] =_variableDomains[i][_variableIndices[i]];
		_variableValues[exceptionIndex] = exceptionValue;
	}
	
	
    public void setBeta(double beta)	// beta = 1/temperature
    {
    	_beta = beta;
    }

    public void initialize()  
	{
		super.initialize();

	}

	@Override
	public void createMessages() 
	{
		_numPorts = _factor.getSiblings().size();
    	_inPortMsgs = new double[_numPorts][];
    	_outMsgArray = new double[_numPorts][];
		_variableDomains = new Object[_numPorts][];
		_variableValues = new Object[_numPorts];
		_variableIndices = new int[_numPorts];
		_variableDomainLengths = new int[_numPorts];
		_realVariable = new boolean[_numPorts];

		
		for (int iPort = 0; iPort < _numPorts; iPort++)
	    {
	    	VariableBase var = _factor.getVariables().getByIndex(iPort);
	    	Object [] messages = var.getSolver().createMessages(this);

	    	// Is the variable connected to the port real or discrete
	    	if (var instanceof Real)
	    	{
	    		ParticleBPSolverVariableToFactorMessage tmp = (ParticleBPSolverVariableToFactorMessage)messages[1];
	    		_realVariable[iPort] = true;
	    		_variableDomains[iPort] = tmp.particleValues;
	    		_inPortMsgs[iPort] = tmp.messageValues;
	    	}
	    	else
	    	{
	    		_realVariable[iPort] = true;
	    		_variableDomains[iPort] = ((Discrete)var).getDiscreteDomain().getElements();
	    		_inPortMsgs[iPort] = (double[])messages[1];
	    	}
	    	
    		_outMsgArray[iPort] = (double[])messages[0];
	    	_variableDomainLengths[iPort] = _variableDomains[iPort].length;
	    }
	}


	@Override
	public void initializeEdge(int portNum) 
	{
	}


	@Override
	public Object getInputMsg(int portIndex) 
	{
		// TODO Auto-generated method stub
		return _inPortMsgs[portIndex];
	}


	@Override
	public Object getOutputMsg(int portIndex) 
	{
		// TODO Auto-generated method stub
		return _outMsgArray[portIndex];
	}


	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum) 
	{
		throw new DimpleException("Not supported by this: " + this);
		
	}


}
