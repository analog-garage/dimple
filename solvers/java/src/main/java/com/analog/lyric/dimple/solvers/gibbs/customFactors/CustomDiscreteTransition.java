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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.SDiscreteVariable;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class CustomDiscreteTransition extends SRealFactor implements IRealJointConjugateFactor
{
	private Object[] _outputMsgs;
	private SDiscreteVariable _yVariable;
	private SDiscreteVariable _xVariable;
	private boolean _hasConstantY;
	private boolean _hasConstantX;
	private int _xDimension;
	private int _yDimension;
	private int _startingParameterEdge;
	private int _yPort = -1;
	private int _xPort = -1;
	private int _constantYValue;
	private int _constantXValue;
	private int[] _parameterXIndices;
	private static final int NUM_DISCRETE_VARIABLES = 2;
	private static final int Y_INDEX = 0;
	private static final int X_INDEX = 1;
	private static final int NO_PORT = -1;

	public CustomDiscreteTransition(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum >= _startingParameterEdge)
		{
			// Port is a parameter input
			// Determine sample alpha parameter vector for the current input x

			DirichletParameters outputMsg = (DirichletParameters)_outputMsgs[portNum];
			
			// Clear the output counts
			outputMsg.fill(0);

			// Get the parameter coordinates
			int parameterIndex = portNum - _startingParameterEdge;
			int parameterXIndex = _parameterXIndices[parameterIndex];
			
			// Get the sample values (indices of the discrete value, which corresponds to the value as well)
			int xIndex = _hasConstantX ? _constantXValue : _xVariable.getCurrentSampleIndex();
			int yIndex = _hasConstantY ? _constantYValue : _yVariable.getCurrentSampleIndex();
			
			if (xIndex == parameterXIndex)
			{
				// This edge corresponds to the current input state, so count is 1
				outputMsg.increment(yIndex);
			}
		}
		else
			super.updateEdgeMessage(portNum);
	}
	
	
	@Override
	public Set<IRealJointConjugateSamplerFactory> getAvailableRealJointConjugateSamplers(int portNumber)
	{
		Set<IRealJointConjugateSamplerFactory> availableSamplers = new HashSet<IRealJointConjugateSamplerFactory>();
		if (isPortParameter(portNumber))						// Conjugate sampler if edge is a parameter input
			availableSamplers.add(DirichletSampler.factory);	// Parameter inputs have conjugate Dirichlet distribution
		return availableSamplers;
	}
	
	public boolean isPortParameter(int portNumber)
	{
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber >= _startingParameterEdge);
	}

	
	
	@Override
	public void initialize()
	{
		super.initialize();
				
		// Determine what parameters are constants or edges, and save the state
		determineParameterConstantsAndEdges();
	}
	
	
	private void determineParameterConstantsAndEdges()
	{
		// Get the factor function and related state
		FactorFunction factorFunction = _factor.getFactorFunction();
		FactorFunctionWithConstants constantFactorFunction = null;
		boolean hasFactorFunctionConstants = false;
		if (factorFunction instanceof FactorFunctionWithConstants)	// In case the factor function is wrapped, get the specific factor function within
		{
			hasFactorFunctionConstants = true;
			constantFactorFunction = (FactorFunctionWithConstants)factorFunction;
			factorFunction = constantFactorFunction.getContainedFactorFunction();
		}

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_startingParameterEdge = 0;
		int numParameterEdges = _numPorts - NUM_DISCRETE_VARIABLES;
		List<INode> siblings = _factor.getSiblings();
		if (hasFactorFunctionConstants)
		{
			// Factor function has constants, figure out which are parameters and which are discrete variables
			int[] constantIndices = constantFactorFunction.getConstantIndices();
			Object[] constantValues = constantFactorFunction.getConstants();
			int numConstants = constantIndices.length;
			_hasConstantY = false;
			_hasConstantX = false;
			_yPort = NO_PORT;
			_xPort = NO_PORT;
			for (int i = 0; i < numConstants; i++)
			{
				if (constantIndices[i] == 0)
				{
					_hasConstantY = true;
					_constantYValue = FactorFunctionUtilities.toInteger(constantValues[i]);
				}
				else if (constantIndices[i] == 1)
				{
					_hasConstantX = true;
					_constantXValue = FactorFunctionUtilities.toInteger(constantValues[i]);
				}
				else	// Parameter is constant
				{
					numParameterEdges--;
				}
			}
			
			// Create a mapping between the edge connecting parameters and the XY coordinates in the parameter array
			_parameterXIndices = new int[numParameterEdges];
			int constantIndex = 0;
			int parameterEdgeIndex = 0;
			for (int x = 0; x < _xDimension; x++)	// Column scan order
			{
				int parameterIndex = x;
				if (constantIndices[constantIndex] - NUM_DISCRETE_VARIABLES == parameterIndex)
				{
					// Parameter is constant
					constantIndex++;
				}
				else
				{
					// Parameter is variable
					_parameterXIndices[parameterEdgeIndex] = x;
					parameterEdgeIndex++;
				}
			}
		}
		else	// Factor function has no constants
		{
			_hasConstantY = false;
			_hasConstantX = false;
			
			// Create a mapping between the edge connecting parameters and the X coordinates in the parameter array
			_parameterXIndices = new int[numParameterEdges];
			for (int x = 0, parameterEdgeIndex = 0; x < numParameterEdges; x++)
				_parameterXIndices[parameterEdgeIndex++] = x;
		}
		
		
		if (_hasConstantY)
		{
			_yPort = NO_PORT;
			_yVariable = null;
			_yDimension = 1;
		}
		else	// Y is a variable
		{
			_yPort = Y_INDEX;
			Discrete yVar = ((Discrete)siblings.get(_yPort));
			_yVariable = (SDiscreteVariable)yVar.getSolver();
			_yDimension = yVar.getDomain().size();
			_startingParameterEdge++;
		}
		
		if (_hasConstantX)
		{
			_xPort = NO_PORT;
			_xVariable = null;
		}
		else	// X is a variable
		{
			_xPort = _hasConstantY ? X_INDEX - 1 : X_INDEX;
			_xVariable = (SDiscreteVariable)(((VariableBase)siblings.get(_xPort)).getSolver());
			_startingParameterEdge++;
		}
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		_outputMsgs = new Object[_numPorts];
		for (int port = _startingParameterEdge; port < _numPorts; port++)	// Only parameter edges
			_outputMsgs[port] = new DirichletParameters(_yDimension);
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
		_outputMsgs[thisPortNum] = ((CustomDiscreteTransition)other)._outputMsgs[otherPortNum];
	}
}
