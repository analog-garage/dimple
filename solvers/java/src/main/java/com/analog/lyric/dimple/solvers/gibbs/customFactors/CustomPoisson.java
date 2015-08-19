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
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.Poisson;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsGammaEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;

public class CustomPoisson extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable GibbsDiscrete _outputVariable;
	private int _lambdaParameterEdge;
	private int _constantOutputValue;
	private boolean _hasConstantOutput;
	private static final int NO_PORT = -1;
	private static final int OUTPUT_INDEX_FIXED_LAMBDA = 0;		// If lambda is in constructor then output is first index (0)
	private static final int LAMBDA_PARAMETER_INDEX = 0;		// If lambda is not in constructor then lambda is first index (0)
	private static final int OUTPUT_INDEX = 1;					// If lambda is not in constructor then output is second index (1)

	public CustomPoisson(Factor factor, GibbsSolverGraph parent)
	{
		super(factor, parent);
	}

	@Override
	public @Nullable GibbsSolverEdge<?> createEdge(EdgeState edge)
	{
		if (edge.getFactorToVariableEdgeNumber() == _lambdaParameterEdge)
		{
			return new GibbsGammaEdge();
		}
		
		return null;
	}
	
	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(EdgeState modelEdge, GibbsSolverEdge<?> solverEdge)
	{
		final int portNum = modelEdge.getFactorToVariableEdgeNumber();
		if (portNum == _lambdaParameterEdge)
		{
			// Port is the probability-parameter input
			// Determine sample alpha and beta parameters

			GammaParameters outputMsg = (GammaParameters)solverEdge.factorToVarMsg;

			// Get the current value of the output count
			int outputValue = _hasConstantOutput ? _constantOutputValue : _outputVariable.getCurrentSampleIndex();
			
			outputMsg.setAlphaMinusOne(outputValue);
			outputMsg.setBeta(1);
		}
		else
			super.updateEdgeMessage(modelEdge, solverEdge);
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
		final Factor factor = _model;
		FactorFunction factorFunction = factor.getFactorFunction();
		Poisson specificFactorFunction = (Poisson)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped

		final int prevLambdaParameterEdge = _lambdaParameterEdge;
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_lambdaParameterEdge = NO_PORT;
		_hasConstantOutput = false;
		_constantOutputValue = -1;
		_outputVariable = null;
		if (specificFactorFunction.hasConstantLambdaParameter())		// Lambda parameter is constructor constant
		{
			_hasConstantOutput = factor.hasConstantAtIndex(OUTPUT_INDEX_FIXED_LAMBDA);
			if (_hasConstantOutput)
			{
				_constantOutputValue =
					requireNonNull(factor.getConstantValueByIndex(OUTPUT_INDEX_FIXED_LAMBDA)).getInt();
			}
			else
			{
				int outputEdge = factor.argIndexToSiblingNumber(OUTPUT_INDEX_FIXED_LAMBDA);
				_outputVariable = (GibbsDiscrete)getSibling(outputEdge);
			}
		}
		else	// Variable or constant lambda parameter
		{
			if (!factor.hasConstantAtIndex(LAMBDA_PARAMETER_INDEX))
				_lambdaParameterEdge = factor.argIndexToSiblingNumber(LAMBDA_PARAMETER_INDEX);
			
			_hasConstantOutput = factor.hasConstantAtIndex(OUTPUT_INDEX);
			if (_hasConstantOutput)
			{
				_constantOutputValue = requireNonNull(factor.getConstantValueByIndex(OUTPUT_INDEX)).getInt();
			}
			else
			{
				int outputEdge = factor.argIndexToSiblingNumber(OUTPUT_INDEX);
				_outputVariable = (GibbsDiscrete)getSibling(outputEdge);
			}
		}
		
		if (_lambdaParameterEdge != prevLambdaParameterEdge)
		{
			removeSiblingEdgeState();
		}
	}
}
