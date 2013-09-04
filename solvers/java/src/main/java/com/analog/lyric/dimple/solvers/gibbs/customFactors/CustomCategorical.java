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

import com.analog.lyric.dimple.factorfunctions.Categorical;
import com.analog.lyric.dimple.factorfunctions.LogCategorical;
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
import com.analog.lyric.dimple.solvers.gibbs.samplers.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.IRealConjugateSamplerFactory;

public class CustomCategorical extends SRealConjugateFactor
{
	private IRealConjugateSampler[] _conjugateSampler;
	private Object[] _outputMsgs;
	private int _numParameters;
	private int _numParameterEdges;
	private int[] _parameterIndices;
	private int[] _constantOutputCounts;
	private boolean _hasConstantParameters;
	private boolean _hasConstantOutputs;
	
	public CustomCategorical(Factor factor)
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
			// NOTE: This class works for either Categorical or LogCategorical factor functions
			// since the actual parameter value doesn't come into play in determining the message in this direction

			GammaParameters outputMsg = (GammaParameters)_outputMsgs[outPortNum];

			// The parameter being updated corresponds to this value
			int parameterIndex = _hasConstantParameters ? _parameterIndices[outPortNum] : outPortNum;

			// Start with the ports to variable outputs
			int count = 0;
			ArrayList<INode> siblings = _factor.getSiblings();
			for (int port = _numParameterEdges; port < _numPorts; port++)
			{
				int outputIndex = ((SDiscreteVariable)(((VariableBase)siblings.get(port)).getSolver())).getCurrentSampleIndex();
				if (outputIndex == parameterIndex)
					count++;
			}
			
			// Include any constant outputs also
			if (_hasConstantOutputs)
				count += _constantOutputCounts[parameterIndex];
			
			outputMsg.setAlpha(count);		// Sample alpha
			outputMsg.setBeta(0);			// Sample beta
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
		return (portNumber < _numParameterEdges);
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
		
		
		// Pre-compute statistics associated with any constant output values
		_constantOutputCounts = null;
		if (_hasConstantOutputs)
		{
			FactorFunctionWithConstants	constantFactorFunction = (FactorFunctionWithConstants)(_factor.getFactorFunction());
			Object[] constantValues = constantFactorFunction.getConstants();
			int[] constantIndices = constantFactorFunction.getConstantIndices();
			_constantOutputCounts = new int[_numParameters];
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (constantIndices[i] >= _numParameters)
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
		FactorFunctionBase factorFunction = _factor.getFactorFunction();
		FactorFunctionWithConstants constantFactorFunction = null;
		boolean hasFactorFunctionConstants = false;
		if (factorFunction instanceof FactorFunctionWithConstants)	// In case the factor function is wrapped, get the specific factor function within
		{
			hasFactorFunctionConstants = true;
			constantFactorFunction = (FactorFunctionWithConstants)factorFunction;
			factorFunction = constantFactorFunction.getContainedFactorFunction();
		}
		if (factorFunction instanceof Categorical)
		{
			Categorical specificFactorFunction = (Categorical)factorFunction;
			_numParameters = specificFactorFunction.getDimension();
		}
		else	// LogCategorical
		{
			LogCategorical specificFactorFunction = (LogCategorical)factorFunction;
			_numParameters = specificFactorFunction.getDimension();
		}
		
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_numParameterEdges = _numParameters;
		_hasConstantParameters = false;
		_hasConstantOutputs = false;
		_parameterIndices = null;
		if (hasFactorFunctionConstants)
		{
			// Factor function has constants, figure out which are parameters and which are discrete variables
			int[] constantIndices = constantFactorFunction.getConstantIndices();
			int numConstants = constantIndices.length;
			int numConstantParameters = 0;
			for (int i = 0; i < numConstants; i++)
			{
				if (constantIndices[i] < _numParameters)
					numConstantParameters++;		// Constant is a parameter
				else
					_hasConstantOutputs = true;		// Constant is an output
			}
			_numParameterEdges = _numParameters - numConstantParameters;
			
			if (numConstantParameters > 0)
			{
				// There are constant parameters, so create a mapping from edges to indices for the remaining variable parameters
				_hasConstantParameters = true;
				for (int i = 0, constantIndex = 0, variableIndex = 0; i < _numParameters; i++)
				{
					if (constantIndices[constantIndex] == i)
						constantIndex++;
					else
						_parameterIndices[i] = variableIndex++;
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
			if (isPortParameter(i))
				_outputMsgs[i] = new GammaParameters();
	}
	
	@Override
	public Object getOutputMsg(int portIndex) 
	{
		return _outputMsgs[portIndex];
	}
	

}
