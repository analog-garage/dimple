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

import com.analog.lyric.dimple.factorfunctions.Binomial;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.BetaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.IBlockInitializer;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.BetaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.math.DimpleRandomGenerator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class CustomBinomial extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable Object[] _outputMsgs;
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

	public CustomBinomial(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum == _probabilityParameterEdge)
		{
			// Port is the probability-parameter input
			// Determine sample alpha and beta parameters

			@SuppressWarnings("null")
			BetaParameters outputMsg = (BetaParameters)_outputMsgs[portNum];

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
			super.updateEdgeMessage(portNum);
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
		((GibbsSolverGraph)_factor.getRootGraph().getSolver()).addBlockInitializer(new CustomBinomial.BlockInitializer());
	}
	
	
	private void determineConstantsAndEdges()
	{
		// Get the factor function and related state
		FactorFunction factorFunction = _factor.getFactorFunction();
		Binomial specificFactorFunction = (Binomial)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		List<? extends Variable> siblings = _factor.getSiblings();
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
			
			if (!factorFunction.isConstantIndex(P_PARAMETER_INDEX_FIXED_N))
				_probabilityParameterEdge = factorFunction.getEdgeByIndex(P_PARAMETER_INDEX_FIXED_N);
			
			_hasConstantOutput = factorFunction.isConstantIndex(OUTPUT_INDEX_FIXED_N);
			if (_hasConstantOutput)
				_constantOutputValue = requireNonNull((Integer)factorFunction.getConstantByIndex(OUTPUT_INDEX_FIXED_N));
			else
			{
				int outputEdge = factorFunction.getEdgeByIndex(OUTPUT_INDEX_FIXED_N);
				_outputVariable = (GibbsDiscrete)((siblings.get(outputEdge)).getSolver());
			}
		}
		else	// Variable or constant N parameter
		{
			_hasConstantNParameter = factorFunction.isConstantIndex(N_PARAMETER_INDEX);
			if (_hasConstantNParameter)
			{
				_constantNParameterValue =
				requireNonNull((Integer)factorFunction.getConstantByIndex(N_PARAMETER_INDEX));
			}
			else
			{
				int nParameterEdge = factorFunction.getEdgeByIndex(N_PARAMETER_INDEX);
				_NParameterVariable = (GibbsDiscrete)((siblings.get(nParameterEdge)).getSolver());
			}
			
			if (!factorFunction.isConstantIndex(P_PARAMETER_INDEX))
				_probabilityParameterEdge = factorFunction.getEdgeByIndex(P_PARAMETER_INDEX);
			
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
		if (_probabilityParameterEdge != NO_PORT)
			outputMsgs[_probabilityParameterEdge] = new BetaParameters();
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
		_outputMsgs[thisPortNum] = ((CustomBinomial)other)._outputMsgs[otherPortNum];
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
					nextIndex = DimpleRandomGenerator.rand.nextInt(N + 1);
				else
					nextIndex = 0;
				
				// Set the output variable value
				_outputVariable.setCurrentSampleIndex(nextIndex);
			}
		}
	}
	
}
