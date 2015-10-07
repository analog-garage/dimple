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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.BetaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsBetaEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.BetaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;

public class CustomBernoulli extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable GibbsDiscrete[] _outputVariables;
	private int _numParameterEdges;
	private int _constantOutputZeroCount;
	private int _constantOutputOneCount;
	private boolean _hasConstantOutputs;
	private static final int NUM_PARAMETERS = 1;
	private static final int PARAMETER_INDEX = 0;
	
	public CustomBernoulli(Factor factor, GibbsSolverGraph parent)
	{
		super(factor, parent);
	}

	@Override
	public @Nullable GibbsSolverEdge<?> createEdge(EdgeState edge)
	{
		if (edge.getFactorToVariableEdgeNumber() < _numParameterEdges)
		{
			return new GibbsBetaEdge();
		}
		
		return super.createEdge(edge);
	}
	
	@Override
	public void updateEdgeMessage(EdgeState modelEdge, GibbsSolverEdge<?> solverEdge)
	{
		final int portNum = modelEdge.getFactorToVariableEdgeNumber();
		
		if (portNum < _numParameterEdges)
		{
			// Port is the parameter input
			// Determine sample alpha and beta parameters

			@SuppressWarnings("null")
			BetaParameters outputMsg = (BetaParameters)solverEdge.factorToVarMsg;

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
			super.updateEdgeMessage(modelEdge, solverEdge);
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
		final Factor factor = _model;
		FactorFunction factorFunction = factor.getFactorFunction();
		Bernoulli specificFactorFunction = (Bernoulli)factorFunction;
		boolean hasFactorFunctionConstants = factor.hasConstants();
		boolean hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();

		final int prevNumParameterEdges = _numParameterEdges;
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		List<? extends Variable> siblings = factor.getSiblings();
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
			_numParameterEdges = factor.hasConstantAtIndex(PARAMETER_INDEX) ? 0 : 1;
			_hasConstantOutputs = factor.hasConstantAtOrAboveIndex(PARAMETER_INDEX + 1);
		}

		
		// Pre-compute statistics associated with any constant output values
		_constantOutputZeroCount = 0;
		_constantOutputOneCount = 0;
		if (_hasConstantOutputs)
		{
			final List<Value> constantValues = factor.getConstantValues();
			int[] constantIndices = factor.getConstantIndices();
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
				{
					int outputValue = constantValues.get(i).getInt();
					if (outputValue == 0)
						_constantOutputZeroCount++;
					else
						_constantOutputOneCount++;
				}
			}
		}

	
		// Save output variables and add to the statistics any output variables that have fixed values
		int numVariableOutputs = 0;		// First, determine how many output variables are not fixed
		final int nEdges = getSiblingCount();
		for (int edge = _numParameterEdges; edge < nEdges; edge++)
			if (!(siblings.get(edge).hasFixedValue()))
				numVariableOutputs++;
		final GibbsDiscrete[] outputVariables = _outputVariables = new GibbsDiscrete[numVariableOutputs];
		for (int edge = _numParameterEdges, index = 0; edge < nEdges; edge++)
		{
			final GibbsDiscrete outputVariable = (GibbsDiscrete)getSibling(edge);
			final int outputValue = outputVariable.getKnownDiscreteIndex();
			
			if (outputValue >= 0)
			{
				if (outputValue == 0)
					_constantOutputZeroCount++;
				else
					_constantOutputOneCount++;
				_hasConstantOutputs = true;
			}
			else
				outputVariables[index++] = outputVariable;
		}
		
		if (_numParameterEdges != prevNumParameterEdges)
		{
			removeSiblingEdgeState();
		}
	}
}
