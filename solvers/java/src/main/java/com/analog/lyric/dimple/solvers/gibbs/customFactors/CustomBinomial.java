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

import com.analog.lyric.dimple.factorfunctions.Binomial;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionBase;
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

public class CustomBinomial extends SRealFactor implements IRealConjugateFactor
{
	private IRealConjugateSampler[] _conjugateSampler;
	private Object[] _outputMsgs;
	private SDiscreteVariable _outputVariable;
	private SDiscreteVariable _NParameterVariable;
	private int _probabilityParameterEdge;
	private int _constantNParameter;
	private int _constantOutputValue;
	private boolean _hasConstantNParameter;
	private boolean _hasConstantOutput;
	private static final int NO_PORT = -1;

	public CustomBinomial(Factor factor)
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
			// Output port must be the probability-parameter input
			// Determine sample alpha and beta parameters

			BetaParameters outputMsg = (BetaParameters)_outputMsgs[outPortNum];

			// Get the current values of N and the output count
			int N = _hasConstantNParameter ? _constantNParameter : _NParameterVariable.getCurrentSampleIndex();
			int numOnes = _hasConstantOutput ? _constantOutputValue : _outputVariable.getCurrentSampleIndex();
			int numZeros = N - numOnes;
			
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
		if (isPortProbabilityParameter(portNumber))					// Conjugate sampler if edge is probability-parameter input
			availableSamplers.add(BetaSampler.factory);				// Parameter inputs have conjugate Beta distribution
		return availableSamplers;
	}
	
	public boolean isPortProbabilityParameter(int portNumber)
	{
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber == _probabilityParameterEdge);
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
		Binomial specificFactorFunction = (Binomial)factorFunction;

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		boolean hasFactorFunctionConstructorConstantNParameter = specificFactorFunction.hasConstantNParameter();
		ArrayList<INode> siblings = _factor.getSiblings();
		int possibleEdgeIndex = 0;
		int actualEdgeIndex = 0;
		
		// Figure out the N parameter (constructor constant, constant, or variable)
		if (hasFactorFunctionConstructorConstantNParameter)		// N parameter is constructor constant
		{
			_hasConstantNParameter = true;
			_constantNParameter = specificFactorFunction.getN();
			_NParameterVariable = null;
		}
		else	// Variable or constant N parameter
		{
			if (hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(possibleEdgeIndex))	// N parameter is constant
			{
				_hasConstantNParameter = true;
				_constantNParameter = (Integer)constantFactorFunction.getConstantByIndex(possibleEdgeIndex);
				_NParameterVariable = null;
			}
			else	// N parameter is variable
			{
				_hasConstantNParameter = false;
				_constantNParameter = -1;
				_NParameterVariable = (SDiscreteVariable)(((VariableBase)siblings.get(actualEdgeIndex++)).getSolver());
			}
			possibleEdgeIndex++;
		}
		
		
		// Figure out the probability parameter (constant or variable)
		if (hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(possibleEdgeIndex))	// probability parameter is constant
			_probabilityParameterEdge = NO_PORT;
		else	// probability parameter is variable
			_probabilityParameterEdge = actualEdgeIndex++;
		possibleEdgeIndex++;
		
		// Figure out the output (constant or variable)
		if (hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(possibleEdgeIndex))	// output is constant
		{
			_hasConstantOutput = true;
			_constantOutputValue = (Integer)constantFactorFunction.getConstantByIndex(possibleEdgeIndex);
			_outputVariable = null;
		}
		else	// N parameter is variable
		{
			_hasConstantOutput = false;
			_constantOutputValue = -1;
			_outputVariable = (SDiscreteVariable)(((VariableBase)siblings.get(actualEdgeIndex++)).getSolver());
		}
	
	}
	
	
	@Override
	public void createMessages() 
	{
		super.createMessages();
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		_outputMsgs = new Object[_numPorts];
		if (_probabilityParameterEdge != NO_PORT)
			_outputMsgs[_probabilityParameterEdge] = new BetaParameters();
	}
	
	@Override
	public Object getOutputMsg(int portIndex) 
	{
		return _outputMsgs[portIndex];
	}
	

}
