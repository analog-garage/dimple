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

import com.analog.lyric.dimple.factorfunctions.LogNormal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionBase;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.SRealJointVariable;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.NormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.NormalSampler;

public class CustomLogNormal extends SRealConjugateFactor
{
	private IRealConjugateSampler[] _conjugateSampler;
	private Object[] _outputMsgs;
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
	public void updateEdgeMessage(int outPortNum)
	{
		IRealConjugateSampler conjugateSampler = _conjugateSampler[outPortNum];
		if (conjugateSampler == null)
			super.updateEdgeMessage(outPortNum);
		else if (conjugateSampler instanceof NormalSampler)
		{
			// Output port must be the mean-parameter input
			// Determine sample mean and precision

			NormalParameters outputMsg = (NormalParameters)_outputMsgs[outPortNum];
				
			// Start with the ports to variable outputs
			ArrayList<INode> siblings = _factor.getSiblings();
			double sum = 0;
			for (int port = _numParameterEdges; port < _numPorts; port++)
				sum += Math.log(((SRealVariable)(((VariableBase)siblings.get(port)).getSolver())).getCurrentSample());
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
		else if (conjugateSampler instanceof GammaSampler)
		{
			// Output port is precision-parameter input
			// Determine sample alpha and beta
			
			GammaParameters outputMsg = (GammaParameters)_outputMsgs[outPortNum];
			
			// Get the current mean
			double mean = _hasConstantMean ? _constantMeanValue : _meanVariable.getCurrentSample();
			
			// Start with the ports to variable outputs
			ArrayList<INode> siblings = _factor.getSiblings();
			double sum = 0;
			for (int port = _numParameterEdges; port < _numPorts; port++)
			{
				double value = Math.log(((SRealVariable)(((VariableBase)siblings.get(port)).getSolver())).getCurrentSample());
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
			
			outputMsg.setAlpha(0.5 * count);		// Sample alpha
			outputMsg.setBeta(0.5 * sum);			// Sample beta
		}
		else
			super.updateEdgeMessage(outPortNum);
	}
	
	
	@Override
	public Collection<IRealConjugateSamplerFactory> getAvailableSamplers(int portNumber)
	{
		Collection<IRealConjugateSamplerFactory> availableSamplers = new ArrayList<IRealConjugateSamplerFactory>();
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
		_hasConstantOutputs = false;
		if (_hasFactorFunctionConstants)
		{
			FactorFunctionWithConstants	constantFactorFunction = (FactorFunctionWithConstants)(_factor.getFactorFunction());
			Object[] constantValues = constantFactorFunction.getConstants();
			int[] constantIndices = constantFactorFunction.getConstantIndices();
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
		FactorFunctionBase factorFunction = _factor.getFactorFunction();
		FactorFunctionWithConstants constantFactorFunction = null;
		_hasFactorFunctionConstants = false;
		if (factorFunction instanceof FactorFunctionWithConstants)	// In case the factor function is wrapped, get the specific factor function within
		{
			_hasFactorFunctionConstants = true;
			constantFactorFunction = (FactorFunctionWithConstants)factorFunction;
			factorFunction = constantFactorFunction.getContainedFactorFunction();
		}
		LogNormal specificFactorFunction = (LogNormal)factorFunction;
		
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();
		if (_hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_hasConstantMean = true;
			_hasConstantPrecision = true;
			_meanParameterPort = NO_PORT;
			_precisionParameterPort = NO_PORT;
			_constantMeanValue = specificFactorFunction.getMean();
			_constantPrecisionValue = specificFactorFunction.getPrecision();
			_numParameterEdges = 0;
		}
		else // Variable or constant parameters
		{
			_numParameterEdges = 0;
			ArrayList<INode> siblings = _factor.getSiblings();
			if (_hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(MEAN_PARAMETER_INDEX))
			{
				_hasConstantMean = true;
				_meanParameterPort = NO_PORT;
				_constantMeanValue = (Double)constantFactorFunction.getConstantByIndex(MEAN_PARAMETER_INDEX);
				_meanVariable = null;
			}
			else
			{
				_hasConstantMean = false;
				_meanParameterPort = _numParameterEdges++;
				_meanVariable = (SRealVariable)(((VariableBase)siblings.get(_meanParameterPort)).getSolver());
			}
			if (_hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(PRECISION_PARAMETER_INDEX))
			{
				_hasConstantPrecision = true;
				_precisionParameterPort = NO_PORT;
				_constantPrecisionValue = (Double)constantFactorFunction.getConstantByIndex(PRECISION_PARAMETER_INDEX);
				_precisionVariable = null;
			}
			else
			{
				_hasConstantPrecision = false;
				_precisionParameterPort = _numParameterEdges++;
				_precisionVariable = (SRealVariable)(((VariableBase)siblings.get(_precisionParameterPort)).getSolver());
			}
		}
		_numOutputEdges = _numPorts - _numParameterEdges;
	}
	

	@Override
	public void createMessages() 
	{
		super.createMessages();
		_outputMsgs = new Object[_numPorts];
		for (int i = 0; i < _numPorts; i++)
			if (isPortPrecisionParameter(i))
				_outputMsgs[i] = new GammaParameters();
			else
				_outputMsgs[i] = new NormalParameters();
	}
	
	@Override
	public Object getOutputMsg(int portIndex) 
	{
		return _outputMsgs[portIndex];
	}
	

}
