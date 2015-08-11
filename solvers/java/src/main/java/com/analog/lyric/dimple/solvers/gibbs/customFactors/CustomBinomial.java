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

import static com.analog.lyric.dimple.environment.DimpleEnvironment.*;
import static java.util.Objects.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.Binomial;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.BetaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsBetaEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.IBlockInitializer;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.BetaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;

public class CustomBinomial extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable GibbsDiscrete _outputVariable;
	private @Nullable GibbsDiscrete _NParameterVariable;
	private int _probabilityParameterEdge;
	private int _constantNParameterValue;
	private int _constantOutputValue;
	private boolean _hasConstantNParameter;
	private boolean _hasConstantOutput;
	private static final int NO_PORT = -1;
	private static final int P_PARAMETER_INDEX_FIXED_N = 0;		// If N is in constructor then P is first index (0)
	private static final int OUTPUT_INDEX_FIXED_N = 1;			// If N is in constructor then output is second index (1)
	private static final int N_PARAMETER_INDEX = 0;				// If N is not in constructor then N is first index (0)
	private static final int P_PARAMETER_INDEX = 1;				// If N is not in constructor then P is second index (1)
	private static final int OUTPUT_INDEX = 2;					// If N is not in constructor then output is third index (2)

	public CustomBinomial(Factor factor, GibbsSolverGraph parent)
	{
		super(factor, parent);
	}

	@Override
	public @Nullable GibbsSolverEdge<?> createEdge(EdgeState edge)
	{
		if (edge.getFactorToVariableEdgeNumber() == _probabilityParameterEdge)
		{
			return new GibbsBetaEdge();
		}

		return super.createEdge(edge);
	}
	
	@Override
	public void updateEdgeMessage(EdgeState modelEdge, GibbsSolverEdge<?> solverEdge)
	{
		final int portNum = modelEdge.getFactorToVariableEdgeNumber();
		
		if (portNum == _probabilityParameterEdge)
		{
			// Port is the probability-parameter input
			// Determine sample alpha and beta parameters

			@SuppressWarnings("null")
			BetaParameters outputMsg = (BetaParameters)solverEdge.factorToVarMsg;

			// Get the current values of N and the output count
			@SuppressWarnings("null")
			int N = _hasConstantNParameter ? _constantNParameterValue : _NParameterVariable.getCurrentSampleIndex();
			@SuppressWarnings("null")
			int numOnes = _hasConstantOutput ? _constantOutputValue : _outputVariable.getCurrentSampleIndex();
			int numZeros = N - numOnes;
			
			outputMsg.setAlphaMinusOne(numOnes);		// Sample alpha
			outputMsg.setBetaMinusOne(numZeros);		// Sample beta
		}
		else
			super.updateEdgeMessage(modelEdge, solverEdge);
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
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber == _probabilityParameterEdge);
	}

	
	
	@SuppressWarnings("null")
	@Override
	public void initialize()
	{
		super.initialize();
				
		// Determine what parameters are constants or edges, and save the state
		determineConstantsAndEdges();
		
		// Create a block initializer to initialize the neighboring variables
		((GibbsSolverGraph)getRootSolverGraph()).addBlockInitializer(new CustomBinomial.BlockInitializer());
	}
	
	
	private void determineConstantsAndEdges()
	{
		// Get the factor function and related state
		final Factor factor = _model;
		FactorFunction factorFunction = factor.getFactorFunction();
		Binomial specificFactorFunction = (Binomial)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped

		final int prevProbabilityParameterEdge = _probabilityParameterEdge;
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_hasConstantNParameter = false;
		_hasConstantOutput = false;
		_constantNParameterValue = -1;
		_constantOutputValue = -1;
		_NParameterVariable = null;
		_outputVariable = null;
		_probabilityParameterEdge = NO_PORT;
		if (specificFactorFunction.hasConstantNParameter())		// N parameter is constructor constant
		{
			_hasConstantNParameter = true;
			_constantNParameterValue = specificFactorFunction.getN();
			
			if (!factor.isConstantIndex(P_PARAMETER_INDEX_FIXED_N))
				_probabilityParameterEdge = factor.getEdgeByIndex(P_PARAMETER_INDEX_FIXED_N);
			
			_hasConstantOutput = factor.isConstantIndex(OUTPUT_INDEX_FIXED_N);
			if (_hasConstantOutput)
				_constantOutputValue =
					requireNonNull(factor.getConstantValueByIndex(OUTPUT_INDEX_FIXED_N)).getInt();
			else
			{
				int outputEdge = factor.getEdgeByIndex(OUTPUT_INDEX_FIXED_N);
				_outputVariable = (GibbsDiscrete)getSibling(outputEdge);
			}
		}
		else	// Variable or constant N parameter
		{
			_hasConstantNParameter = factor.isConstantIndex(N_PARAMETER_INDEX);
			if (_hasConstantNParameter)
			{
				_constantNParameterValue =
					requireNonNull(factor.getConstantValueByIndex(N_PARAMETER_INDEX)).getInt();
			}
			else
			{
				int nParameterEdge = factor.getEdgeByIndex(N_PARAMETER_INDEX);
				_NParameterVariable = (GibbsDiscrete)getSibling(nParameterEdge);
			}
			
			if (!factor.isConstantIndex(P_PARAMETER_INDEX))
				_probabilityParameterEdge = factor.getEdgeByIndex(P_PARAMETER_INDEX);
			
			_hasConstantOutput = factor.isConstantIndex(OUTPUT_INDEX);
			if (_hasConstantOutput)
			{
				_constantOutputValue = requireNonNull(factor.getConstantValueByIndex(OUTPUT_INDEX)).getInt();
			}
			else
			{
				int outputEdge = factor.getEdgeByIndex(OUTPUT_INDEX);
				_outputVariable = (GibbsDiscrete)getSibling(outputEdge);
			}
		}
		
		if (_probabilityParameterEdge != prevProbabilityParameterEdge)
		{
			removeSiblingEdgeState();
		}
	}
	
	public class BlockInitializer implements IBlockInitializer
	{
		@SuppressWarnings("null")
		@Override
		public void initialize()
		{
			if (!_hasConstantOutput)
			{
				// If output is variable, sample uniformly
				int N = _hasConstantNParameter ? _constantNParameterValue : _NParameterVariable.getCurrentSampleIndex();
				int nextIndex;
				if (N > 0)
					nextIndex = activeRandom().nextInt(N + 1);
				else
					nextIndex = 0;
				
				// Set the output variable value
				_outputVariable.setCurrentSampleIndex(nextIndex);
			}
		}
	}
	
}
