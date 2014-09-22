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

import com.analog.lyric.dimple.factorfunctions.Poisson;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class CustomPoisson extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable Object[] _outputMsgs;
	private @Nullable GibbsDiscrete _outputVariable;
	private int _lambdaParameterEdge;
	private int _constantOutputValue;
	private boolean _hasConstantOutput;
	private static final int NO_PORT = -1;
	private static final int OUTPUT_INDEX_FIXED_LAMBDA = 0;		// If lambda is in constructor then output is first index (0)
	private static final int LAMBDA_PARAMETER_INDEX = 0;		// If lambda is not in constructor then lambda is first index (0)
	private static final int OUTPUT_INDEX = 1;					// If lambda is not in constructor then output is second index (1)

	public CustomPoisson(Factor factor)
	{
		super(factor);
	}

	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum == _lambdaParameterEdge)
		{
			// Port is the probability-parameter input
			// Determine sample alpha and beta parameters

			GammaParameters outputMsg = (GammaParameters)_outputMsgs[portNum];

			// Get the current value of the output count
			int outputValue = _hasConstantOutput ? _constantOutputValue : _outputVariable.getCurrentSampleIndex();
			
			outputMsg.setAlphaMinusOne(outputValue);
			outputMsg.setBeta(1);
		}
		else
			super.updateEdgeMessage(portNum);
	}
	
	
	@Override
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		if (isPortLambdaParameter(portNumber))						// Conjugate sampler if edge is lambda-parameter input
			availableSamplers.add(GammaSampler.factory);			// Parameter inputs have conjugate Gamma distribution
		return availableSamplers;
	}
	
	public boolean isPortLambdaParameter(int portNumber)
	{
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber == _lambdaParameterEdge);
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
		Poisson specificFactorFunction = (Poisson)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		List<? extends Variable> siblings = _factor.getSiblings();
		_lambdaParameterEdge = NO_PORT;
		_hasConstantOutput = false;
		_constantOutputValue = -1;
		_outputVariable = null;
		if (specificFactorFunction.hasConstantLambdaParameter())		// Lambda parameter is constructor constant
		{
			_hasConstantOutput = factorFunction.isConstantIndex(OUTPUT_INDEX_FIXED_LAMBDA);
			if (_hasConstantOutput)
			{
				_constantOutputValue =
					requireNonNull((Integer)factorFunction.getConstantByIndex(OUTPUT_INDEX_FIXED_LAMBDA));
			}
			else
			{
				int outputEdge = factorFunction.getEdgeByIndex(OUTPUT_INDEX_FIXED_LAMBDA);
				_outputVariable = (GibbsDiscrete)((siblings.get(outputEdge)).getSolver());
			}
		}
		else	// Variable or constant lambda parameter
		{
			if (!factorFunction.isConstantIndex(LAMBDA_PARAMETER_INDEX))
				_lambdaParameterEdge = factorFunction.getEdgeByIndex(LAMBDA_PARAMETER_INDEX);
			
			_hasConstantOutput = factorFunction.isConstantIndex(OUTPUT_INDEX);
			if (_hasConstantOutput)
			{
				_constantOutputValue = requireNonNull((Integer)factorFunction.getConstantByIndex(OUTPUT_INDEX));
			}
			else
			{
				int outputEdge = factorFunction.getEdgeByIndex(OUTPUT_INDEX);
				_outputVariable = (GibbsDiscrete)((siblings.get(outputEdge)).getSolver());
			}
		}
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		final Object[] outputMsgs = _outputMsgs = new Object[_numPorts];
		if (_lambdaParameterEdge != NO_PORT)
			outputMsgs[_lambdaParameterEdge] = new GammaParameters();
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
		_outputMsgs[thisPortNum] = ((CustomPoisson)other)._outputMsgs[otherPortNum];
	}
	
}
