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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.BetaParameters;
import com.analog.lyric.dimple.solvers.gibbs.SDiscreteVariable;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.BetaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class CustomBernoulli extends SRealFactor implements IRealConjugateFactor
{
	private Object[] _outputMsgs;
	private SDiscreteVariable[] _outputVariables;
	private int _numParameterEdges;
	private int _numOutputEdges;
	private int _constantOutputZeroCount;
	private int _constantOutputOneCount;
	private boolean _hasConstantOutputs;
	private boolean _hasFactorFunctionConstructorConstants;
	private static final int NUM_PARAMETERS = 1;
	private static final int PARAMETER_INDEX = 0;
	
	public CustomBernoulli(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum < _numParameterEdges)
		{
			// Port is the parameter input
			// Determine sample alpha and beta parameters

			BetaParameters outputMsg = (BetaParameters)_outputMsgs[portNum];

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
			
			outputMsg.setAlphaMinusOne(numOnes);
			outputMsg.setBetaMinusOne(numZeros);
		}
		else
			super.updateEdgeMessage(portNum);
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
		
		
		// Determine what parameters are constants or edges, and save the state
		determineParameterConstantsAndEdges();
		

		// Pre-compute statistics associated with any constant output values
		_constantOutputZeroCount = 0;
		_constantOutputOneCount = 0;
		if (_hasConstantOutputs)
		{
			FactorFunction	factorFunction = _factor.getFactorFunction();
			Object[] constantValues = factorFunction.getConstants();
			int[] constantIndices = factorFunction.getConstantIndices();
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (_hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
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
		FactorFunction factorFunction = _factor.getFactorFunction();
		Bernoulli specificFactorFunction = (Bernoulli)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		boolean hasFactorFunctionConstants = factorFunction.hasConstants();
		_hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_numParameterEdges = NUM_PARAMETERS;
		_hasConstantOutputs = false;
		if (_hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameter provided in the factor-function constructor
			_numParameterEdges = 0;
			_hasConstantOutputs = hasFactorFunctionConstants;
		}
		else if (hasFactorFunctionConstants)
		{
			// Factor function has constants, figure out which are parameters and which are discrete variables
			_numParameterEdges = factorFunction.isConstantIndex(PARAMETER_INDEX) ? 0 : 1;
			_hasConstantOutputs = factorFunction.hasConstantAtOrAboveIndex(PARAMETER_INDEX + 1);
		}
		_numOutputEdges = _numPorts - _numParameterEdges;

		
		// Save output variables
		List<? extends VariableBase> siblings = _factor.getSiblings();
		_outputVariables = new SDiscreteVariable[_numOutputEdges];
		for (int i = 0; i < _numOutputEdges; i++)
			_outputVariables[i] = (SDiscreteVariable)((siblings.get(i + _numParameterEdges)).getSolver());
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
	
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		super.moveMessages(other, thisPortNum, otherPortNum);
		_outputMsgs[thisPortNum] = ((CustomBernoulli)other)._outputMsgs[otherPortNum];
	}
}
