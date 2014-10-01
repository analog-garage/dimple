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

import com.analog.lyric.dimple.factorfunctions.NegativeExpGamma;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.NegativeExpGammaSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class CustomNegativeExpGamma extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable Object[] _outputMsgs;
	private @Nullable GibbsReal[] _outputVariables;
	private @Nullable GibbsReal _alphaVariable;
	private @Nullable GibbsReal _betaVariable;
	private boolean _hasConstantAlpha;
	private boolean _hasConstantBeta;
	private boolean _hasConstantOutputs;
	private int _numParameterEdges;
	private int _alphaParameterPort = -1;
	private int _betaParameterPort = -1;
	private int _constantOutputCount;
	private double _constantAlphaValueMinusOne;
	private double _constantBetaValue;
	private double _constantOutputSum;
	private static final int NUM_PARAMETERS = 2;
	private static final int ALPHA_PARAMETER_INDEX = 0;
	private static final int BETA_PARAMETER_INDEX = 1;
	private static final int NO_PORT = -1;

	public CustomNegativeExpGamma(Factor factor)
	{
		super(factor);
	}

	@SuppressWarnings("null")
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
			for (int i = 0; i < _outputVariables.length; i++)
				sum += Math.exp(_outputVariables[i].getCurrentSample());
			int count = _outputVariables.length;

			// Include any constant outputs also
			if (_hasConstantOutputs)
			{
				sum += _constantOutputSum;
				count += _constantOutputCount;
			}

			// Get the current alpha value
			double alpha = _hasConstantAlpha ? _constantAlphaValueMinusOne + 1 : _alphaVariable.getCurrentSample();

			outputMsg.setAlphaMinusOne(count * alpha);		// Sample alpha
			outputMsg.setBeta(sum);							// Sample beta
		}
		else if (portNum >= _numParameterEdges)
		{
			// Port is directed output
			GammaParameters outputMsg = (GammaParameters)_outputMsgs[portNum];
			outputMsg.setAlphaMinusOne(_hasConstantAlpha ? _constantAlphaValueMinusOne : _alphaVariable.getCurrentSample() - 1);
			outputMsg.setBeta(_hasConstantBeta ? _constantBetaValue : _betaVariable.getCurrentSample());
		}
		else
			super.updateEdgeMessage(portNum);
	}
	
	
	@Override
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		if (isPortBetaParameter(portNumber))						// Port is beta parameter, which has a conjugate Gamma distribution
			availableSamplers.add(GammaSampler.factory);
		else if (!isPortAlphaParameter(portNumber))					// No supported conjugate sampler for alpha parameter
			availableSamplers.add(NegativeExpGammaSampler.factory);	// So port is output, which has a NegativeExpGamma distribution
		return availableSamplers;
	}
	
	public boolean isPortAlphaParameter(int portNumber)
	{
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber == _alphaParameterPort);
	}

	public boolean isPortBetaParameter(int portNumber)
	{
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber == _betaParameterPort);
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
		NegativeExpGamma specificFactorFunction = (NegativeExpGamma)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		boolean hasFactorFunctionConstants = factorFunction.hasConstants();
		boolean hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		List<? extends Variable> siblings = _factor.getSiblings();
		_hasConstantAlpha = false;
		_hasConstantBeta = false;
		_alphaParameterPort = NO_PORT;
		_betaParameterPort = NO_PORT;
		_alphaVariable = null;
		_betaVariable = null;
		_constantAlphaValueMinusOne = 0;
		_constantBetaValue = 0;
		_numParameterEdges = 0;
		if (hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_hasConstantAlpha = true;
			_hasConstantBeta = true;
			_constantAlphaValueMinusOne = specificFactorFunction.getAlphaMinusOne();
			_constantBetaValue = specificFactorFunction.getBeta();
		}
		else	// Variable or constant parameters
		{
			_hasConstantAlpha = factorFunction.isConstantIndex(ALPHA_PARAMETER_INDEX);
			if (_hasConstantAlpha)	// Constant mean
				_constantAlphaValueMinusOne = FactorFunctionUtilities.toDouble(factorFunction.getConstantByIndex(ALPHA_PARAMETER_INDEX)) - 1;
			else					// Variable mean
			{
				_alphaParameterPort = factorFunction.getEdgeByIndex(ALPHA_PARAMETER_INDEX);
				_alphaVariable = (GibbsReal)((siblings.get(_alphaParameterPort)).getSolver());
				_numParameterEdges++;
			}
			
			_hasConstantBeta = factorFunction.isConstantIndex(BETA_PARAMETER_INDEX);
			if (_hasConstantBeta)	// Constant precision
				_constantBetaValue = FactorFunctionUtilities.toDouble(factorFunction.getConstantByIndex(BETA_PARAMETER_INDEX));
			else 						// Variable precision
			{
				_betaParameterPort = factorFunction.getEdgeByIndex(BETA_PARAMETER_INDEX);
				_betaVariable = (GibbsReal)((siblings.get(_betaParameterPort)).getSolver());
				_numParameterEdges++;
			}
		}
		
		
		// Pre-compute statistics associated with any constant output values
		_hasConstantOutputs = false;
		if (hasFactorFunctionConstants)
		{
			Object[] constantValues = factorFunction.getConstants();
			int[] constantIndices = factorFunction.getConstantIndices();
			_constantOutputCount = 0;
			_constantOutputSum = 0;
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
				{
					_constantOutputSum += Math.exp(-(Double)constantValues[i]);
					_constantOutputCount++;
		}
			}
			_hasConstantOutputs = true;
		}
		

		// Save output variables and add to the statistics any output variables that have fixed values
		int numVariableOutputs = 0;		// First, determine how many output variables are not fixed
		for (int edge = _numParameterEdges; edge < _numPorts; edge++)
			if (!(siblings.get(edge).hasFixedValue()))
				numVariableOutputs++;
		final GibbsReal[] outputVariables = _outputVariables = new GibbsReal[numVariableOutputs];
		for (int edge = _numParameterEdges, index = 0; edge < _numPorts; edge++)
		{
			Real outputVariable = (Real)siblings.get(edge);
			if (outputVariable.hasFixedValue())
			{
				_constantOutputSum += Math.exp(-outputVariable.getFixedValue());
				_hasConstantOutputs = true;
			}
			else
				outputVariables[index++] = (GibbsReal)outputVariable.getSolver();
		}
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		final Object[] outputMsgs = _outputMsgs = new Object[_numPorts];
		for (int port = 0; port < _numPorts; port++)
			if (port != _alphaParameterPort)
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
		_outputMsgs[thisPortNum] = ((CustomNegativeExpGamma)other)._outputMsgs[otherPortNum];
	}
}
