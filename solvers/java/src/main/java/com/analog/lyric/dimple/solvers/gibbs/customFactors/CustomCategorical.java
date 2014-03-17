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

import com.analog.lyric.dimple.factorfunctions.Categorical;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DirichletParameters;
import com.analog.lyric.dimple.solvers.gibbs.SDiscreteVariable;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class CustomCategorical extends SRealFactor implements IRealJointConjugateFactor
{
	private Object[] _outputMsgs;
	private SDiscreteVariable[] _outputVariables;
	private int _parameterDimension;
	private int _numParameterEdges;
	private int _numOutputEdges;
	private int[] _constantOutputCounts;
	private boolean _hasConstantOutputs;
	private boolean _hasFactorFunctionConstructorConstants;
	private static final int NUM_PARAMETERS = 1;
	private static final int PARAMETER_INDEX = 0;
	
	public CustomCategorical(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum < _numParameterEdges)
		{
			// Output port is a joint parameter input
			// Determine sample alpha vector of the conjugate Dirichlet distribution
			// Note: This case works for the Categorical factor function (which has joint parameters)
			
			DirichletParameters outputMsg = (DirichletParameters)_outputMsgs[portNum];
			
			// Clear the output counts
			outputMsg.fill(0);
			
			// Start with the ports to variable outputs
			for (int i = 0; i < _numOutputEdges; i++)
			{
				int outputIndex = _outputVariables[i].getCurrentSampleIndex();
				outputMsg.increment(outputIndex);	// Increment the statistics
			}

			// Include any constant outputs also
			if (_hasConstantOutputs)
				outputMsg.add(_constantOutputCounts);
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
		return (portNumber < _numParameterEdges);
	}

	
	
	@Override
	public void initialize()
	{
		super.initialize();
		

		// Determine what parameters are constants or edges, and save the state
		determineParameterConstantsAndEdges();
		
		
		// Pre-compute statistics associated with any constant output values
		_constantOutputCounts = null;
		if (_hasConstantOutputs)
		{
			FactorFunction factorFunction = _factor.getFactorFunction();
			Object[] constantValues = factorFunction.getConstants();
			int[] constantIndices = factorFunction.getConstantIndices();
			_constantOutputCounts = new int[_parameterDimension];
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (_hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
				{
					int outputValue = FactorFunctionUtilities.toInteger(constantValues[i]);
					_constantOutputCounts[outputValue]++;	// Histogram among constant outputs
				}
			}
		}
	}
	
	
	private void determineParameterConstantsAndEdges()
	{
		// Get the factor function and related state
		FactorFunction factorFunction = _factor.getFactorFunction();
		Categorical specificFactorFunction = (Categorical)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		_hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();

		
		// Pre-determine whether or not the parameters are constant
		List<? extends VariableBase> siblings = _factor.getSiblings();
		if (_hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_numParameterEdges = 0;
			_hasConstantOutputs = factorFunction.hasConstants();
			_parameterDimension = specificFactorFunction.getDimension();
		}
		else
		{
			boolean hasConstantParameters = factorFunction.isConstantIndex(PARAMETER_INDEX);
			_numParameterEdges = hasConstantParameters ? 0 : 1;
			_hasConstantOutputs = factorFunction.hasConstantAtOrAboveIndex(PARAMETER_INDEX + 1);
			if (hasConstantParameters)
			{
				double[] constantParameters = (double[])factorFunction.getConstantByIndex(PARAMETER_INDEX);
				_parameterDimension = constantParameters.length;
			}
			else
			{
				_parameterDimension = (((RealJoint)siblings.get(PARAMETER_INDEX))).getRealDomain().getNumVars();
			}
		}
		_numOutputEdges = _numPorts - _numParameterEdges;

		
		// Save output variables
		_outputVariables = new SDiscreteVariable[_numOutputEdges];
		for (int i = 0; i < _numOutputEdges; i++)
			_outputVariables[i] = (SDiscreteVariable)((siblings.get(i + _numParameterEdges)).getSolver());
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		_outputMsgs = new Object[_numPorts];
		for (int port = 0; port < _numParameterEdges; port++)	// Only parameter edges
			_outputMsgs[port] = new DirichletParameters(_parameterDimension);
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
		_outputMsgs[thisPortNum] = ((CustomCategorical)other)._outputMsgs[otherPortNum];
	}
}
