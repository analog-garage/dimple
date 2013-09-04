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
import java.util.Collection;

import com.analog.lyric.dimple.factorfunctions.DiscreteTransition;
import com.analog.lyric.dimple.factorfunctions.LogDiscreteTransition;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionBase;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.SDiscreteVariable;
import com.analog.lyric.dimple.solvers.gibbs.SRealJointVariable;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;

public class CustomDiscreteTransition extends SRealConjugateFactor
{
	private IRealConjugateSampler[] _conjugateSampler;
	private Object[] _outputMsgs;
	private SDiscreteVariable _yVariable;
	private SDiscreteVariable _xVariable;
	private boolean _hasConstantY;
	private boolean _hasConstantX;
	private int _xDimension;
	private int _yDimension;
	private int _numParameters;
	private int _numParameterEdges;
	private int _startingParameterEdge;
	private int _yPort = -1;
	private int _xPort = -1;
	private int _constantYValue;
	private int _constantXValue;
	private int[] _parameterXIndices;
	private int[] _parameterYIndices;
	private static final int NUM_DISCRETE_VARIABLES = 2;
	private static final int Y_INDEX = 0;
	private static final int X_INDEX = 1;
	private static final int NO_PORT = -1;

	public CustomDiscreteTransition(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		IRealConjugateSampler conjugateSampler = _conjugateSampler[outPortNum];
		if (conjugateSampler == null)
			super.updateEdgeMessage(outPortNum);
		else if (conjugateSampler instanceof GammaSampler)
		{
			// Output port must be a parameter input
			// Determine sample alpha and beta parameters
			// NOTE: This class works for either DiscreteTransition or LogDiscreteTransition factor functions
			// since the actual parameter value doesn't come into play in determining the message in this direction

			GammaParameters outputMsg = (GammaParameters)_outputMsgs[outPortNum];
			
			// Get the parameter coordinates
			int parameterIndex = outPortNum - _startingParameterEdge;
			int parameterXIndex = _parameterXIndices[parameterIndex];
			int parameterYIndex = _parameterYIndices[parameterIndex];
			
			// Get the sample values (indices of the discrete value, which corresponds to the value as well)
			int xIndex = _hasConstantX ? _constantXValue : _xVariable.getCurrentSampleIndex();
			int yIndex = _hasConstantY ? _constantYValue : _yVariable.getCurrentSampleIndex();
			
			if (xIndex == parameterXIndex && yIndex == parameterYIndex)
			{
				// This edge corresponds to the current state, so count is 1
				outputMsg.setAlpha(1);			// Sample alpha
				outputMsg.setBeta(0);			// Sample beta
			}
			else
			{
				// This edge does not correspond to the current state
				outputMsg.setAlpha(0);			// Sample alpha
				outputMsg.setBeta(0);			// Sample beta
			}
		}
		else
			super.updateEdgeMessage(outPortNum);
	}
	
	
	@Override
	public Collection<IRealConjugateSamplerFactory> getAvailableSamplers(int portNumber)
	{
		Collection<IRealConjugateSamplerFactory> availableSamplers = new ArrayList<IRealConjugateSamplerFactory>();
		if (isPortParameter(portNumber))					// Conjugate sampler if edge is a parameter input
			availableSamplers.add(GammaSampler.factory);	// Parameter inputs have conjugate Gamma distribution
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
		
		// Determine if any ports can use a conjugate sampler
		_conjugateSampler = new IRealConjugateSampler[_numPorts];
		for (int port = 0; port < _numPorts; port++)
		{
			INode var = _factor.getSiblings().get(port);
			if (var instanceof Real)
				_conjugateSampler[port] = ((SRealVariable)var.getSolver()).getConjugateSampler();
			else if (var instanceof RealJoint)
				_conjugateSampler[port] = ((SRealJointVariable)var.getSolver()).getConjugateSampler();
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
		if (factorFunction instanceof DiscreteTransition)
		{
			DiscreteTransition specificFactorFunction = (DiscreteTransition)factorFunction;
			_xDimension = specificFactorFunction.getXDimension();
			_yDimension = specificFactorFunction.getYDimension();
			_numParameters = specificFactorFunction.getNumParameters();
		}
		else	// LogDiscreteTransition
		{
			LogDiscreteTransition specificFactorFunction = (LogDiscreteTransition)factorFunction;
			_xDimension = specificFactorFunction.getXDimension();
			_yDimension = specificFactorFunction.getYDimension();
			_numParameters = specificFactorFunction.getNumParameters();
		}

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_numParameterEdges = _numParameters;
		_startingParameterEdge = 0;
		ArrayList<INode> siblings = _factor.getSiblings();
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
					_numParameterEdges--;
				}
			}
			
			if (_hasConstantY)
			{
				_yPort = NO_PORT;
				_yVariable = null;
			}
			else	// Y is a variable
			{
				_yPort = Y_INDEX;
				_yVariable = (SDiscreteVariable)(((VariableBase)siblings.get(_yPort)).getSolver());
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
			
			// Create a mapping between the edge connecting parameters and the XY coordinates in the parameter array 
			_parameterXIndices = new int[_numParameterEdges];
			_parameterYIndices = new int[_numParameterEdges];
			int constantIndex = 0;
			int parameterEdgeIndex = 0; 
			for (int x = 0; x < _xDimension; x++)	// Column scan order
			{
				for (int y = 0; y < _yDimension; y++)
				{
					int parameterIndex = x*_yDimension + y;
					if (constantIndices[constantIndex] - NUM_DISCRETE_VARIABLES == parameterIndex)
					{
						// Parameter is constant
						constantIndex++;
					}
					else
					{
						// Parameter is variable
						_parameterXIndices[parameterEdgeIndex] = x;
						_parameterYIndices[parameterEdgeIndex] = y;
						parameterEdgeIndex++;
					}
				}
			}
		}
		else	// Factor function has no constants
		{
			_hasConstantY = false;
			_hasConstantX = false;
			_yPort = Y_INDEX;
			_xPort = X_INDEX;
			_yVariable = (SDiscreteVariable)(((VariableBase)siblings.get(_yPort)).getSolver());
			_xVariable = (SDiscreteVariable)(((VariableBase)siblings.get(_xPort)).getSolver());
			_numParameterEdges = _numParameters;
			_startingParameterEdge = NUM_DISCRETE_VARIABLES;
			
			// Create a mapping between the edge connecting parameters and the XY coordinates in the parameter array 
			_parameterXIndices = new int[_numParameterEdges];
			_parameterYIndices = new int[_numParameterEdges];
			for (int x = 0, parameterEdgeIndex = 0; x < _xDimension; x++)	// Column scan order
			{
				for (int y = 0; y < _yDimension; y++, parameterEdgeIndex++)
				{
					_parameterXIndices[parameterEdgeIndex] = x;
					_parameterYIndices[parameterEdgeIndex] = y;
				}
			}
		}
	}
	
	
	@Override
	public void createMessages() 
	{
		super.createMessages();
		_outputMsgs = new Object[_numPorts];
		for (int i = 0; i < _numPorts; i++)
			_outputMsgs[i] = new GammaParameters();
	}
	
	@Override
	public Object getOutputMsg(int portIndex) 
	{
		return _outputMsgs[portIndex];
	}

}
