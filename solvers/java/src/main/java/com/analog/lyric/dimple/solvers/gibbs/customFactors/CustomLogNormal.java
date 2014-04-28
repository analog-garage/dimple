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

import com.analog.lyric.dimple.factorfunctions.LogNormal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.NormalSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class CustomLogNormal extends SRealFactor implements IRealConjugateFactor
{
	private Object[] _outputMsgs;
	private SRealVariable[] _outputVariables;
	private SRealVariable _meanVariable;
	private SRealVariable _precisionVariable;
	private boolean _hasConstantMean;
	private boolean _hasConstantPrecision;
	private boolean _hasConstantOutputs;
	private boolean _hasFactorFunctionConstants;
	private boolean _hasFactorFunctionConstructorConstants;
	private int _numOutputEdges;
	private int _numParameterEdges;
	private int _meanParameterPort = -1;
	private int _precisionParameterPort = -1;
	private int _constantOutputCount;
	private double _constantMeanValue;
	private double _constantPrecisionValue;
	private double _constantOutputSum;
	private double _constantOutputSumOfSquares;
	private static final int NUM_PARAMETERS = 2;
	private static final int MEAN_PARAMETER_INDEX = 0;
	private static final int PRECISION_PARAMETER_INDEX = 1;
	private static final int NO_PORT = -1;

	public CustomLogNormal(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum == _meanParameterPort)
		{
			// Port is the mean-parameter input
			// Determine sample mean and precision

			NormalParameters outputMsg = (NormalParameters)_outputMsgs[portNum];
				
			// Start with the ports to variable outputs
			double sum = 0;
			for (int i = 0; i < _numOutputEdges; i++)
				sum += Math.log(_outputVariables[i].getCurrentSample());
			int count = _numOutputEdges;

			// Include any constant outputs also
			if (_hasConstantOutputs)
			{
				sum += _constantOutputSum;
				count += _constantOutputCount;
			}

			// Get the current precision
			double precision = _hasConstantPrecision ? _constantPrecisionValue : _precisionVariable.getCurrentSample();

			outputMsg.setMean(sum / count);				// Sample mean
			outputMsg.setPrecision(precision * count);	// Sample precision
		}
		else if (portNum == _precisionParameterPort)
		{
			// Port is precision-parameter input
			// Determine sample alpha and beta
			
			GammaParameters outputMsg = (GammaParameters)_outputMsgs[portNum];
			
			// Get the current mean
			double mean = _hasConstantMean ? _constantMeanValue : _meanVariable.getCurrentSample();
			
			// Start with the ports to variable outputs
			List<? extends VariableBase> siblings = _factor.getSiblings();
			double sum = 0;
			for (int port = _numParameterEdges; port < _numPorts; port++)
			{
				double value = Math.log(((SRealVariable)((siblings.get(port)).getSolver())).getCurrentSample());
				double diff = value - mean;
				sum += diff*diff;
			}
			int count = _numOutputEdges;
			
			// Include any constant outputs also
			if (_hasConstantOutputs)
			{
				// Sum of (outputValue - mean)^2 using pre-computed sum and sum of squares
				sum += _constantOutputCount*mean*mean + _constantOutputSumOfSquares - 2 * mean * _constantOutputSum;
				count += _constantOutputCount;
			}
			
			outputMsg.setAlphaMinusOne(0.5 * count);		// Sample alpha
			outputMsg.setBeta(0.5 * sum);					// Sample beta
		}
		else
			super.updateEdgeMessage(portNum);
	}
	
	
	@Override
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		if (isPortPrecisionParameter(portNumber))
			availableSamplers.add(GammaSampler.factory);	// Precision parameter has Gamma conjugate distribution
		else if (isPortMeanParameter(portNumber))
			availableSamplers.add(NormalSampler.factory);	// Mean parameter has a Normal conjugate distribution
		return availableSamplers;
	}
	
	public boolean isPortMeanParameter(int portNumber)
	{
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber == _meanParameterPort);
	}
	
	public boolean isPortPrecisionParameter(int portNumber)
	{
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber == _precisionParameterPort);
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
			_constantOutputSumOfSquares = 0;
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (_hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
				{
					double outputValue = Math.log((Double)constantValues[i]);
					_constantOutputSum += outputValue;
					_constantOutputSumOfSquares += outputValue*outputValue;
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
		LogNormal specificFactorFunction = (LogNormal)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		_hasFactorFunctionConstants = factorFunction.hasConstants();
		_hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		List<? extends VariableBase> siblings = _factor.getSiblings();
		_hasConstantMean = false;
		_hasConstantPrecision = false;
		_meanParameterPort = NO_PORT;
		_precisionParameterPort = NO_PORT;
		_meanVariable = null;
		_precisionVariable = null;
		_constantMeanValue = 0;
		_constantPrecisionValue = 0;
		_numParameterEdges = 0;
		if (_hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_hasConstantMean = true;
			_hasConstantPrecision = true;
			_constantMeanValue = specificFactorFunction.getMean();
			_constantPrecisionValue = specificFactorFunction.getPrecision();
		}
		else	// Variable or constant parameters
		{
			_hasConstantMean = factorFunction.isConstantIndex(MEAN_PARAMETER_INDEX);
			if (_hasConstantMean)	// Constant mean
				_constantMeanValue = FactorFunctionUtilities.toDouble(factorFunction.getConstantByIndex(MEAN_PARAMETER_INDEX));
			else					// Variable mean
			{
				_meanParameterPort = factorFunction.getEdgeByIndex(MEAN_PARAMETER_INDEX);
				_meanVariable = (SRealVariable)((siblings.get(_meanParameterPort)).getSolver());
				_numParameterEdges++;
			}
			
			_hasConstantPrecision = factorFunction.isConstantIndex(PRECISION_PARAMETER_INDEX);
			if (_hasConstantPrecision)	// Constant precision
				_constantPrecisionValue = FactorFunctionUtilities.toDouble(factorFunction.getConstantByIndex(PRECISION_PARAMETER_INDEX));
			else 						// Variable precision
			{
				_precisionParameterPort = factorFunction.getEdgeByIndex(PRECISION_PARAMETER_INDEX);
				_precisionVariable = (SRealVariable)((siblings.get(_precisionParameterPort)).getSolver());
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
			if (port == _precisionParameterPort)
				_outputMsgs[port] = new GammaParameters();
			else if (port == _meanParameterPort)
				_outputMsgs[port] = new NormalParameters();
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
		_outputMsgs[thisPortNum] = ((CustomLogNormal)other)._outputMsgs[otherPortNum];
	}
}
