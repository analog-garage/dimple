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

package com.analog.lyric.dimple.solvers.gibbs.customFactors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionBase;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.SDiscreteVariable;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.BetaParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.BetaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;

public class CustomBernoulli extends SRealFactor implements IRealConjugateFactor
{
	private IRealConjugateSampler[] _conjugateSampler;
	private Object[] _outputMsgs;
	private SDiscreteVariable[] _outputVariables;
	private int _numParameterEdges;
	private int _numOutputEdges;
	private int _constantOutputZeroCount;
	private int _constantOutputOneCount;
	private boolean _hasConstantOutputs;
	private static final int NUM_PARAMETERS = 1;
	
	public CustomBernoulli(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		IRealConjugateSampler conjugateSampler = _conjugateSampler[outPortNum];
		if (conjugateSampler == null)
			super.updateEdgeMessage(outPortNum);
		else if (conjugateSampler instanceof BetaSampler)
		{
			// Output port must be the parameter input
			// Determine sample alpha and beta parameters

			BetaParameters outputMsg = (BetaParameters)_outputMsgs[outPortNum];

			// Start with the ports to variable outputs
			int numZeros = 0;
			for (int i = 0; i < _numOutputEdges; i++)
			{
				int outputIndex = _outputVariables[i].getCurrentSampleIndex();
				if (outputIndex == 0)
					numZeros++;
			}
			int numOnes = (_numPorts - _numParameterEdges) - numZeros;
			
			// Include any constant outputs also
			if (_hasConstantOutputs)
			{
				numZeros += _constantOutputZeroCount;
				numOnes += _constantOutputOneCount;
			}
			
			outputMsg.setAlpha(numOnes);		// Sample alpha
			outputMsg.setBeta(numZeros);		// Sample beta
		}
		else
			super.updateEdgeMessage(outPortNum);
	}
	
	
	@Override
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		if (isPortParameter(portNumber))					// Conjugate sampler if edge is parameter input
			availableSamplers.add(BetaSampler.factory);		// Parameter inputs have conjugate Beta distribution
		return availableSamplers;
	}
	
	public boolean isPortParameter(int portNumber)
	{
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber < _numParameterEdges);
	}

	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Determine if any ports can use a conjugate sampler
		_conjugateSampler = new IRealConjugateSampler[_numPorts];
		for (int port = 0; port < _numPorts; port++)
		{
			INode var = _factor.getSiblings().get(port);
			if (var instanceof Real)
				_conjugateSampler[port] = ((SRealVariable)var.getSolver()).getConjugateSampler();
			else
				_conjugateSampler[port] = null;
		}
		
		
		// Determine what parameters are constants or edges, and save the state
		determineParameterConstantsAndEdges();
		

		// Pre-compute statistics associated with any constant output values
		_constantOutputZeroCount = 0;
		_constantOutputOneCount = 0;
		if (_hasConstantOutputs)
		{
			FactorFunctionWithConstants	constantFactorFunction = (FactorFunctionWithConstants)(_factor.getFactorFunction());
			Object[] constantValues = constantFactorFunction.getConstants();
			int[] constantIndices = constantFactorFunction.getConstantIndices();
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (constantIndices[i] >= NUM_PARAMETERS)
				{
					int outputValue = FactorFunctionUtilities.toInteger(constantValues[i]);
					if (outputValue == 0)
						_constantOutputZeroCount++;
					else
						_constantOutputOneCount++;
				}
			}
		}
	}
	
	
	private void determineParameterConstantsAndEdges()
	{
		// Get the factor function and related state
		FactorFunctionBase factorFunction = _factor.getFactorFunction();
		FactorFunctionWithConstants constantFactorFunction = null;
		boolean hasFactorFunctionConstants = false;
		if (factorFunction instanceof FactorFunctionWithConstants)	// In case the factor function is wrapped, get the specific factor function within
		{
			hasFactorFunctionConstants = true;
			constantFactorFunction = (FactorFunctionWithConstants)factorFunction;
			factorFunction = constantFactorFunction.getContainedFactorFunction();
		}
		
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_numParameterEdges = NUM_PARAMETERS;
		_hasConstantOutputs = false;
		if (hasFactorFunctionConstants)
		{
			// Factor function has constants, figure out which are parameters and which are discrete variables
			int[] constantIndices = constantFactorFunction.getConstantIndices();
			int numConstants = constantIndices.length;
			for (int i = 0; i < numConstants; i++)
			{
				if (constantIndices[i] < NUM_PARAMETERS)
					_numParameterEdges = 0;				// Constant is a parameter
				else
					_hasConstantOutputs = true;			// Constant is an output
			}
		}
		_numOutputEdges = _numPorts - _numParameterEdges;

		
		// Save output variables
		ArrayList<INode> siblings = _factor.getSiblings();
		_outputVariables = new SDiscreteVariable[_numOutputEdges];
		for (int i = 0; i < _numOutputEdges; i++)
			_outputVariables[i] = (SDiscreteVariable)(((VariableBase)siblings.get(i + _numParameterEdges)).getSolver());
	}
	
	
	@Override
	public void createMessages() 
	{
		super.createMessages();
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		_outputMsgs = new Object[_numPorts];
		for (int port = 0; port < _numParameterEdges; port++)	// Only parameter edges
			_outputMsgs[port] = new BetaParameters();
	}
	
	@Override
	public Object getOutputMsg(int portIndex) 
	{
		return _outputMsgs[portIndex];
	}
	

}
