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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.analog.lyric.dimple.factorfunctions.Multiplexer;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.gibbs.ISolverRealVariableGibbs;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealJoint;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class CustomMultiplexer extends GibbsRealFactor implements IRealConjugateFactor, IRealJointConjugateFactor
{
	private @Nullable ISampler[] _conjugateSampler;
	private @Nullable Object[] _outputMsgs;
	private @Nullable GibbsDiscrete _selectorVariable;
	private @Nullable ISolverRealVariableGibbs _outputVariable;
	private int _outputPortNumber;
	private int _selectorPortNumber;
	private int _firstInputPortNumber;
	private int _outputVariableSiblingPortIndex;
	private @Nullable int[] _inputPortMap;
	private boolean _incompatibleWithConjugateSampling = false;
	private boolean _hasFactorFunctionConstants;
	private boolean _hasConstantSelector;
	private int _selectorConstantValue;
	private static final int OUTPUT_INDEX = 0;
	private static final int SELECTOR_INDEX = 1;
	private static final int FIRST_INPUT_PORT_INDEX = 2;
	private static final int NO_PORT = -1;

	public CustomMultiplexer(Factor factor)
	{
		super(factor);
	}

	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum >= _firstInputPortNumber)
		{
			// Port is an input port
			int selector = _hasConstantSelector ? _selectorConstantValue : _selectorVariable.getCurrentSampleIndex();	// Get the current selector value
			if (portNum == _inputPortMap[selector])
			{
				// Port is the currently selected input port
				// Get the aggregated message from the variable connected to the output port
				_outputVariable.getAggregateMessages((IParameterizedMessage)_outputMsgs[portNum], _outputVariableSiblingPortIndex, _conjugateSampler[portNum]);
			}
			else
			{
				// Port is not the currently selected input port
				((IParameterizedMessage)_outputMsgs[portNum]).setNull();
			}
		}
		else
			super.updateEdgeMessage(portNum);
	}
	
	
	// For Real variable inputs and outputs
	@SuppressWarnings("null")
	@Override
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		if (_incompatibleWithConjugateSampling)
			return availableSamplers;
		if (isPortInputVariable(portNumber))
		{
			// If an input variable, then check conjugacy for the output variable among all of its neighbors except this factor
			GibbsReal outputVariable = ((GibbsReal)_outputVariable);
			List<Factor> outputNeighboringFactors = new ArrayList<Factor>();
			for (Factor n : outputVariable.getModelObject().getSiblings())
				if (!n.equals(_factor))		// Don't include this factor to test conjugacy
					outputNeighboringFactors.add(n);
			availableSamplers.addAll(outputVariable.findConjugateSamplerFactories(outputNeighboringFactors));
		}
		return availableSamplers;
	}

	// For RealJoint variable inputs and outputs
	@SuppressWarnings("null")
	@Override
	public Set<IRealJointConjugateSamplerFactory> getAvailableRealJointConjugateSamplers(int portNumber)
	{
		Set<IRealJointConjugateSamplerFactory> availableSamplers = new HashSet<IRealJointConjugateSamplerFactory>();
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		if (_incompatibleWithConjugateSampling)
			return availableSamplers;
		if (isPortInputVariable(portNumber))
		{
			// If an input variable, then check conjugacy for the output variable among all of its neighbors except this factor
			GibbsRealJoint outputVariable = ((GibbsRealJoint)_outputVariable);
			List<Factor> outputNeighboringFactors = new ArrayList<Factor>();
			for (Factor n : outputVariable.getModelObject().getSiblings())
				if (!n.equals(_factor))		// Don't include this factor to test conjugacy
					outputNeighboringFactors.add(n);
			availableSamplers.addAll(outputVariable.findConjugateSamplerFactories(outputNeighboringFactors));
		}
		return availableSamplers;
	}

	public boolean isPortInputVariable(int portNumber)
	{
		return (portNumber >= _firstInputPortNumber);
	}

	
	@SuppressWarnings("null")
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Determine if any ports can use a conjugate sampler
		final ISampler[] conjugateSampler = _conjugateSampler = new ISampler[_numPorts];
		for (int port = 0; port < _numPorts; port++)
		{
			INode var = _factor.getSibling(port);
			int varPortNum = _factor.getSiblingPortIndex(port);
			if (var instanceof Real)
			{
				GibbsReal svar = (GibbsReal)var.getSolver();
				conjugateSampler[port] = svar.getConjugateSampler();
				
				if (conjugateSampler[port] != null)
				{
					// Create message and tell the variable to use it
					_outputMsgs[port] = ((IRealConjugateSampler)conjugateSampler[port]).createParameterMessage();
					svar.setInputMsg(varPortNum, _outputMsgs[port]);
				}
			}
			else if (var instanceof RealJoint)
			{
				GibbsRealJoint svar = (GibbsRealJoint)var.getSolver();
				conjugateSampler[port] = svar.getConjugateSampler();

				if (conjugateSampler[port] != null)
				{
					// Create message and tell the variable to use it
					_outputMsgs[port] = ((IRealJointConjugateSampler)conjugateSampler[port]).createParameterMessage();
					svar.setInputMsg(varPortNum, _outputMsgs[port]);
				}
			}
			else
				conjugateSampler[port] = null;
		}
		
		
		// Determine what parameters are constants or edges, and save the state
		determineConstantsAndEdges();
		
		
		// Set up _inputPortMap, which maps the selector value to port index
		int numInputEdges = _numPorts - _firstInputPortNumber;
		final int[] inputPortMap = _inputPortMap = new int[numInputEdges];
		if (_hasFactorFunctionConstants)
		{
			FactorFunction factorFunction = _factor.getFactorFunction();
			int numConstants = factorFunction.getConstantIndices().length;
			
			int numIndices = _numPorts + numConstants;
			for (int index = 0, port = 0, selectorIndex = 0; index < numIndices; index++)
			{
				if (factorFunction.isConstantIndex(index))
				{
					if (index > FIRST_INPUT_PORT_INDEX)
						selectorIndex++;
				}
				else
				{
					if (port > _firstInputPortNumber)
						inputPortMap[selectorIndex++] = port++;
					else
						port++;
				}
			}
		}
		else	// No constants
		{
			for (int i = 0; i < numInputEdges; i++)
				inputPortMap[i] = i + _firstInputPortNumber;
		}
	}
	
	
	private void determineConstantsAndEdges()
	{
		// Get the factor function and related state
		FactorFunction factorFunction = _factor.getFactorFunction();
		Multiplexer specificFactorFunction = (Multiplexer)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		_hasFactorFunctionConstants = factorFunction.hasConstants();
		if (specificFactorFunction.hasSmoothing())
		{
			_incompatibleWithConjugateSampling = true;
			return;
		}
		if (factorFunction.isConstantIndex(OUTPUT_INDEX))
		{
			_incompatibleWithConjugateSampling = true;
			return;
		}

		_selectorPortNumber = NO_PORT;
		_selectorVariable = null;
		_selectorConstantValue = -1;
		
		_outputPortNumber = factorFunction.getEdgeByIndex(OUTPUT_INDEX);	// Must be a variable if not returned already
		ISolverVariable outputVariable = _factor.getSibling(_outputPortNumber).getSolver();
		if (outputVariable instanceof ISolverRealVariableGibbs)
			_outputVariable = (ISolverRealVariableGibbs)outputVariable;
		_outputVariableSiblingPortIndex = _factor.getSiblingPortIndex(_outputPortNumber);

		_hasConstantSelector = factorFunction.isConstantIndex(SELECTOR_INDEX);
		if (_hasConstantSelector)
		{
			_selectorConstantValue = requireNonNull((Integer)factorFunction.getConstantByIndex(SELECTOR_INDEX));
			_firstInputPortNumber = FIRST_INPUT_PORT_INDEX - 1;
		}
		else
		{
			_selectorPortNumber = factorFunction.getEdgeByIndex(SELECTOR_INDEX);
			_selectorVariable = (GibbsDiscrete)(_factor.getSibling(_selectorPortNumber).getSolver());
			_firstInputPortNumber = FIRST_INPUT_PORT_INDEX;
		}
	}
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		_outputMsgs = new Object[_numPorts];
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
		_outputMsgs[thisPortNum] = ((CustomMultiplexer)other)._outputMsgs[otherPortNum];
	}

}
