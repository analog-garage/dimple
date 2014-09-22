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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransitionEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransitionUnnormalizedParameters;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.NegativeExpGammaSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class CustomDiscreteTransitionUnnormalizedOrEnergyParameters extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable Object[] _outputMsgs;
	private @Nullable GibbsDiscrete _yVariable;
	private @Nullable GibbsDiscrete _xVariable;
	private boolean _hasConstantY;
	private boolean _hasConstantX;
	private boolean _useEnergyParameters;
	private int _xDimension;
	private int _yDimension;
	private int _numParameters;
	private int _numParameterEdges;
	private int _startingParameterEdge;
	private int _yPort = -1;
	private int _xPort = -1;
	private int _constantYValue;
	private int _constantXValue;
	private @Nullable int[] _parameterXIndices;
	private @Nullable int[] _parameterYIndices;
	private static final int NUM_DISCRETE_VARIABLES = 2;
	private static final int Y_INDEX = 0;
	private static final int X_INDEX = 1;
	private static final int NO_PORT = -1;

	public CustomDiscreteTransitionUnnormalizedOrEnergyParameters(Factor factor)
	{
		super(factor);
	}

	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum >= _startingParameterEdge)
		{
			// Port is a parameter input
			// Determine sample alpha and beta parameters
			// NOTE: This class works for either DiscreteTransitionIndepenentParameters or DiscreteTransitionEnergyParameters factor functions
			// since the actual parameter value doesn't come into play in determining the message in this direction

			GammaParameters outputMsg = (GammaParameters)_outputMsgs[portNum];
			
			// Get the parameter coordinates
			int parameterEdgeOffset = portNum - _startingParameterEdge;
			int parameterXIndex = _parameterXIndices[parameterEdgeOffset];
			int parameterYIndex = _parameterYIndices[parameterEdgeOffset];
			
			// Get the sample values (indices of the discrete value, which corresponds to the value as well)
			int xIndex = _hasConstantX ? _constantXValue : _xVariable.getCurrentSampleIndex();
			int yIndex = _hasConstantY ? _constantYValue : _yVariable.getCurrentSampleIndex();
			
			if (xIndex == parameterXIndex && yIndex == parameterYIndex)
			{
				// This edge corresponds to the current state, so count is 1
				outputMsg.setAlphaMinusOne(1);			// Sample alpha
				outputMsg.setBeta(0);					// Sample beta
			}
			else
			{
				// This edge does not correspond to the current state
				outputMsg.setAlphaMinusOne(0);			// Sample alpha
				outputMsg.setBeta(0);					// Sample beta
			}
		}
		else
			super.updateEdgeMessage(portNum);
	}
	
	
	@Override
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		if (isPortParameter(portNumber))					// Conjugate sampler if edge is a parameter input
			if (_useEnergyParameters)
				availableSamplers.add(NegativeExpGammaSampler.factory);	// Parameter inputs have conjugate negative exp-Gamma distribution
			else
				availableSamplers.add(GammaSampler.factory);			// Parameter inputs have conjugate Gamma distribution
		return availableSamplers;
	}
	
	public boolean isPortParameter(int portNumber)
	{
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber >= _startingParameterEdge);
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
		FactorFunction containedFactorFunction = factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		if (containedFactorFunction instanceof DiscreteTransitionUnnormalizedParameters)
		{
			DiscreteTransitionUnnormalizedParameters specificFactorFunction = (DiscreteTransitionUnnormalizedParameters)containedFactorFunction;
			_xDimension = specificFactorFunction.getXDimension();
			_yDimension = specificFactorFunction.getYDimension();
			_numParameters = specificFactorFunction.getNumParameters();
			_useEnergyParameters = false;
		}
		else if (containedFactorFunction instanceof DiscreteTransitionEnergyParameters)
		{
			DiscreteTransitionEnergyParameters specificFactorFunction = (DiscreteTransitionEnergyParameters)containedFactorFunction;
			_xDimension = specificFactorFunction.getXDimension();
			_yDimension = specificFactorFunction.getYDimension();
			_numParameters = specificFactorFunction.getNumParameters();
			_useEnergyParameters = true;
		}
		else
			throw new DimpleException("Invalid factor function");

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_yPort = NO_PORT;
		_xPort = NO_PORT;
		_yVariable = null;
		_xVariable = null;
		_constantYValue = -1;
		_constantXValue = -1;
		_startingParameterEdge = 0;
		List<? extends Variable> siblings = _factor.getSiblings();

		_hasConstantY = factorFunction.isConstantIndex(Y_INDEX);
		if (_hasConstantY)
			_constantYValue = FactorFunctionUtilities.toInteger(factorFunction.getConstantByIndex(Y_INDEX));
		else					// Variable Y
		{
			_yPort = factorFunction.getEdgeByIndex(Y_INDEX);
			Discrete yVar = ((Discrete)siblings.get(_yPort));
			_yVariable = (GibbsDiscrete)yVar.getSolver();
			_yDimension = yVar.getDomain().size();
			_startingParameterEdge++;
		}
		
		
		_hasConstantX = factorFunction.isConstantIndex(X_INDEX);
		if (_hasConstantX)
			_constantXValue = FactorFunctionUtilities.toInteger(factorFunction.getConstantByIndex(X_INDEX));
		else					// Variable X
		{
			_xPort = factorFunction.getEdgeByIndex(X_INDEX);
			Discrete xVar = ((Discrete)siblings.get(_xPort));
			_xVariable = (GibbsDiscrete)xVar.getSolver();
			_startingParameterEdge++;
		}
		
		// Create a mapping between the edge connecting parameters and the XY coordinates in the parameter array
		int numParameterConstants = factorFunction.numConstantsAtOrAboveIndex(NUM_DISCRETE_VARIABLES);
		_numParameterEdges = _numParameters - numParameterConstants;
		final int[] parameterXIndices = _parameterXIndices = new int[_numParameterEdges];
		final int[] parameterYIndices = _parameterYIndices = new int[_numParameterEdges];
		if (numParameterConstants > 0)
		{
			int[] constantIndices = factorFunction.getConstantIndices();
			int constantIndex = 0;
			int parameterEdgeOffset = 0;
			for (int x = 0; x < _xDimension; x++)	// Column scan order
			{
				for (int y = 0; y < _yDimension; y++)
				{
					int parameterOffset = x*_yDimension + y;
					if (constantIndices[constantIndex] - NUM_DISCRETE_VARIABLES == parameterOffset)
					{
						// Parameter is constant
						constantIndex++;
					}
					else
					{
						// Parameter is variable
						parameterXIndices[parameterEdgeOffset] = x;
						parameterYIndices[parameterEdgeOffset] = y;
						parameterEdgeOffset++;
					}
				}
			}
		}
		else	// No constant parameters
		{
			for (int x = 0, parameterEdgeOffset = 0; x < _xDimension; x++)	// Column scan order
			{
				for (int y = 0; y < _yDimension; y++, parameterEdgeOffset++)
				{
					parameterXIndices[parameterEdgeOffset] = x;
					parameterYIndices[parameterEdgeOffset] = y;
				}
			}
		}
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		final Object[] outputMsgs = _outputMsgs = new Object[_numPorts];
		for (int port = _startingParameterEdge; port < _numPorts; port++)	// Only parameter edges
			outputMsgs[port] = new GammaParameters();
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
		_outputMsgs[thisPortNum] = ((CustomDiscreteTransitionUnnormalizedOrEnergyParameters)other)._outputMsgs[otherPortNum];
	}
}
