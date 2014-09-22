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

import static java.util.Objects.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.BetaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.BetaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class CustomBernoulli extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable Object[] _outputMsgs;
	private @Nullable GibbsDiscrete[] _outputVariables;
	private int _numParameterEdges;
	private int _constantOutputZeroCount;
	private int _constantOutputOneCount;
	private boolean _hasConstantOutputs;
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

			@SuppressWarnings("null")
			BetaParameters outputMsg = (BetaParameters)_outputMsgs[portNum];

			final GibbsDiscrete[] outputVariables = requireNonNull(_outputVariables);
			
			// Start with the ports to variable outputs
			int numZeros = 0;
			for (int i = 0; i < outputVariables.length; i++)
			{
				int outputIndex = outputVariables[i].getCurrentSampleIndex();
				if (outputIndex == 0)
					numZeros++;
			}
			int numOnes = outputVariables.length - numZeros;
			
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
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber < _numParameterEdges);
	}

	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Determine what parameters are constants or edges, and save the state
		determineConstantsAndEdges();
				}
	
	
	private void determineConstantsAndEdges()
	{
		// Get the factor function and related state
		FactorFunction factorFunction = _factor.getFactorFunction();
		Bernoulli specificFactorFunction = (Bernoulli)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		boolean hasFactorFunctionConstants = factorFunction.hasConstants();
		boolean hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		List<? extends Variable> siblings = _factor.getSiblings();
		_numParameterEdges = NUM_PARAMETERS;
		_hasConstantOutputs = false;
		if (hasFactorFunctionConstructorConstants)
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

		
		// Pre-compute statistics associated with any constant output values
		_constantOutputZeroCount = 0;
		_constantOutputOneCount = 0;
		if (_hasConstantOutputs)
		{
			Object[] constantValues = factorFunction.getConstants();
			int[] constantIndices = factorFunction.getConstantIndices();
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
				{
					int outputValue = FactorFunctionUtilities.toInteger(constantValues[i]);
					if (outputValue == 0)
						_constantOutputZeroCount++;
					else
						_constantOutputOneCount++;
	}
			}
		}

	
		// Save output variables and add to the statistics any output variables that have fixed values
		int numVariableOutputs = 0;		// First, determine how many output variables are not fixed
		for (int edge = _numParameterEdges; edge < _numPorts; edge++)
			if (!(siblings.get(edge).hasFixedValue()))
				numVariableOutputs++;
		final GibbsDiscrete[] outputVariables = _outputVariables = new GibbsDiscrete[numVariableOutputs];
		for (int edge = _numParameterEdges, index = 0; edge < _numPorts; edge++)
		{
			Discrete outputVariable = (Discrete)siblings.get(edge);
			if (outputVariable.hasFixedValue())
			{
				int outputValue = outputVariable.getFixedValueIndex();
				if (outputValue == 0)
					_constantOutputZeroCount++;
				else
					_constantOutputOneCount++;
				_hasConstantOutputs = true;
			}
			else
				outputVariables[index++] = (GibbsDiscrete)outputVariable.getSolver();
		}
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		final Object[] outputMsgs = _outputMsgs = new Object[_numPorts];
		for (int port = 0; port < _numParameterEdges; port++)	// Only parameter edges
			outputMsgs[port] = new BetaParameters();
	}
	
	@SuppressWarnings("null")
	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outputMsgs[portIndex];
	}
	
	@SuppressWarnings("null")
	@Override
	public void moveMessages(@NonNull ISolverNode other, int thisPortNum, int otherPortNum)
	{
		super.moveMessages(other, thisPortNum, otherPortNum);
		_outputMsgs[thisPortNum] = ((CustomBernoulli)other)._outputMsgs[otherPortNum];
	}
}
