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
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DirichletParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class CustomDiscreteTransition extends GibbsRealFactor implements IRealJointConjugateFactor
{
	private @Nullable Object[] _outputMsgs;
	private @Nullable GibbsDiscrete _yVariable;
	private @Nullable GibbsDiscrete _xVariable;
	private @Nullable FactorFunction _factorFunction;
	private boolean _hasConstantY;
	private boolean _hasConstantX;
	private int _yDimension;
	private int _startingParameterEdge;
	private int _yPort = -1;
	private int _xPort = -1;
	private int _constantYValue;
	private int _constantXValue;
	private static final int NUM_DISCRETE_VARIABLES = 2;
	private static final int Y_INDEX = 0;
	private static final int X_INDEX = 1;
	private static final int NO_PORT = -1;

	public CustomDiscreteTransition(Factor factor)
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
			// Determine sample alpha parameter vector for the current input x

			DirichletParameters outputMsg = (DirichletParameters)_outputMsgs[portNum];
			
			// Clear the output counts
			outputMsg.setNull();

			// Get the parameter coordinates
			int parameterXIndex = _factorFunction.getIndexByEdge(portNum) - NUM_DISCRETE_VARIABLES;
			
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
		_factorFunction = factorFunction;

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_yPort = NO_PORT;
		_xPort = NO_PORT;
		_yVariable = null;
		_xVariable = null;
		_constantYValue = -1;
		_constantXValue = -1;
		_yDimension = 1;
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
		
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		final Object[] outputMsgs = _outputMsgs = new Object[_numPorts];
		for (int port = _startingParameterEdge; port < _numPorts; port++)	// Only parameter edges
			outputMsgs[port] = new DirichletParameters(_yDimension);
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
		_outputMsgs[thisPortNum] = ((CustomDiscreteTransition)other)._outputMsgs[otherPortNum];
	}
}
