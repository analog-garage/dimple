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

import com.analog.lyric.dimple.factorfunctions.Gamma;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.util.misc.NonNull;

public class CustomGamma extends SRealFactor implements IRealConjugateFactor
{
	private Object[] _outputMsgs;
	private SRealVariable[] _outputVariables;
	private SRealVariable _alphaVariable;
	private SRealVariable _betaVariable;
	private boolean _hasConstantAlpha;
	private boolean _hasConstantBeta;
	private boolean _hasConstantOutputs;
	private boolean _hasFactorFunctionConstants;
	private boolean _hasFactorFunctionConstructorConstants;
	private int _numOutputEdges;
	private int _numParameterEdges;
	private int _alphaParameterPort = -1;
	private int _betaParameterPort = -1;
	private int _constantOutputCount;
	private double _constantAlphaMinusOneValue;
	private double _constantBetaValue;
	private double _constantOutputSum;
	private static final int NUM_PARAMETERS = 2;
	private static final int ALPHA_PARAMETER_INDEX = 0;
	private static final int BETA_PARAMETER_INDEX = 1;
	private static final int NO_PORT = -1;

	public CustomGamma(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum == _betaParameterPort)
		{
			// Port is the beta-parameter input
			// Determine sample alpha and beta parameters
			GammaParameters outputMsg = (GammaParameters)_outputMsgs[portNum];
			
			// Start with the ports to variable outputs
			double sum = 0;
			for (int i = 0; i < _numOutputEdges; i++)
				sum += _outputVariables[i].getCurrentSample();
			int count = _numOutputEdges;
			
			// Include any constant outputs also
			if (_hasConstantOutputs)
			{
				sum += _constantOutputSum;
				count += _constantOutputCount;
			}
			
			// Get the current alpha value
			double alpha = _hasConstantAlpha ? _constantAlphaMinusOneValue + 1 : _alphaVariable.getCurrentSample();
			
			outputMsg.setAlphaMinusOne(count * alpha);			// Sample alpha
			outputMsg.setBeta(sum);								// Sample beta
		}
		else if (portNum >= _numParameterEdges)
		{
			// Port is directed output
			GammaParameters outputMsg = (GammaParameters)_outputMsgs[portNum];
			outputMsg.setAlphaMinusOne(_hasConstantAlpha ? _constantAlphaMinusOneValue : _alphaVariable.getCurrentSample() - 1);
			outputMsg.setBeta(_hasConstantBeta ? _constantBetaValue : _betaVariable.getCurrentSample());
		}
		else
			super.updateEdgeMessage(portNum);
	}
	
	
	@Override
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		if (!isPortAlphaParameter(portNumber))				// No supported conjugate sampler for alpha parameter
			availableSamplers.add(GammaSampler.factory);	// Either beta parameter or output, which have Gamma distribution
		return availableSamplers;
	}
	
	public boolean isPortAlphaParameter(int portNumber)
	{
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber == _alphaParameterPort);
	}

	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		
		// Determine what parameters are constants or edges, and save the state
		determineParameterConstantsAndEdges();
		
		
		// Pre-compute statistics associated with any constant output values
		_hasConstantOutputs = false;
		if (_hasFactorFunctionConstants)
		{
			FactorFunction factorFunction = _factor.getFactorFunction();
			Object[] constantValues = factorFunction.getConstants();
			int[] constantIndices = factorFunction.getConstantIndices();
			_constantOutputCount = 0;
			_constantOutputSum = 0;
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (_hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
				{
					_constantOutputSum += (Double)constantValues[i];
					_constantOutputCount++;
				}
			}
			_hasConstantOutputs = true;
		}
	}
	
	
	private void determineParameterConstantsAndEdges()
	{
		// Get the factor function and related state
		FactorFunction factorFunction = _factor.getFactorFunction();
		Gamma specificFactorFunction = (Gamma)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		_hasFactorFunctionConstants = factorFunction.hasConstants();
		_hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		List<? extends VariableBase> siblings = _factor.getSiblings();
		_hasConstantAlpha = false;
		_hasConstantBeta = false;
		_alphaParameterPort = NO_PORT;
		_betaParameterPort = NO_PORT;
		_alphaVariable = null;
		_betaVariable = null;
		_constantAlphaMinusOneValue = 0;
		_constantBetaValue = 0;
		_numParameterEdges = 0;
		if (_hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_hasConstantAlpha = true;
			_hasConstantBeta = true;
			_constantAlphaMinusOneValue = specificFactorFunction.getAlphaMinusOne();
			_constantBetaValue = specificFactorFunction.getBeta();
		}
		else	// Variable or constant parameters
		{
			_hasConstantAlpha = factorFunction.isConstantIndex(ALPHA_PARAMETER_INDEX);
			if (_hasConstantAlpha)	// Constant mean
				_constantAlphaMinusOneValue = FactorFunctionUtilities.toDouble(factorFunction.getConstantByIndex(ALPHA_PARAMETER_INDEX)) - 1;
			else					// Variable mean
			{
				_alphaParameterPort = factorFunction.getEdgeByIndex(ALPHA_PARAMETER_INDEX);
				_alphaVariable = (SRealVariable)((siblings.get(_alphaParameterPort)).getSolver());
				_numParameterEdges++;
			}
			
			_hasConstantBeta = factorFunction.isConstantIndex(BETA_PARAMETER_INDEX);
			if (_hasConstantBeta)	// Constant precision
				_constantBetaValue = FactorFunctionUtilities.toDouble(factorFunction.getConstantByIndex(BETA_PARAMETER_INDEX));
			else 						// Variable precision
			{
				_betaParameterPort = factorFunction.getEdgeByIndex(BETA_PARAMETER_INDEX);
				_betaVariable = (SRealVariable)((siblings.get(_betaParameterPort)).getSolver());
				_numParameterEdges++;
			}
		}
		_numOutputEdges = _numPorts - _numParameterEdges;
		
		// Save output variables
		_outputVariables = new SRealVariable[_numOutputEdges];
		for (int i = 0; i < _numOutputEdges; i++)
			_outputVariables[i] = (SRealVariable)((siblings.get(i + _numParameterEdges)).getSolver());
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		_outputMsgs = new Object[_numPorts];
		for (int port = 0; port < _numPorts; port++)
			if (port != _alphaParameterPort)
				_outputMsgs[port] = new GammaParameters();
	}
	
	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outputMsgs[portIndex];
	}
	
	@Override
	public void moveMessages(@NonNull ISolverNode other, int thisPortNum, int otherPortNum)
	{
		super.moveMessages(other, thisPortNum, otherPortNum);
		_outputMsgs[thisPortNum] = ((CustomGamma)other)._outputMsgs[otherPortNum];
	}
}
