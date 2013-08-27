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

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionBase;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.SRealJointVariable;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.NormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.NormalSampler;

public class CustomNormal extends SRealConjugateFactor
{
	private IRealConjugateSampler[] _conjugateSampler;
	private Object[] _outputMsgs;
	private SRealVariable _meanVariable;
	private SRealVariable _precisionVariable;
	private boolean _hasConstantMean;
	private boolean _hasConstantPrecision;
	private boolean _hasConstantOutputs;
	private int _numOutputEdges;
	private int _numParameterEdges;
	private int _constantOutputCount;
	private double _constantMeanValue;
	private double _constantPrecisionValue;
	private double _constantOutputSum;
	private double _constantOutputSumOfSquares;
	private static final int NUM_PARAMETERS = 2;
	private static final int MEAN_PARAMETER_INDEX = 0;
	private static final int PRECISION_PARAMETER_INDEX = 1;
	
	public CustomNormal(Factor factor)
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
			NormalParameters outputMsg = (NormalParameters)_outputMsgs[outPortNum];
			if (outPortNum >= _numParameterEdges)
			{
				// Output port is directed output of Normal
				outputMsg.setMean(_hasConstantMean ? _constantMeanValue : _meanVariable.getCurrentSample());
				outputMsg.setPrecision(_hasConstantPrecision ? _constantPrecisionValue : _precisionVariable.getCurrentSample());
			}
			else
			{
				// Output port must be the mean-parameter input of Normal
				// Determine sample mean and precision
				
				// Start with the ports to variable outputs
				ArrayList<INode> siblings = _factor.getSiblings();
				double sum = 0;
				for (int port = _numParameterEdges; port < _numPorts; port++)
					sum += ((SRealVariable)(((VariableBase)siblings.get(port)).getSolver())).getCurrentSample();
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
		}
		else if (conjugateSampler instanceof GammaSampler)
		{
			// Output port is precision-parameter input of Normal
			// Determine sample alpha and beta
			
			GammaParameters outputMsg = (GammaParameters)_outputMsgs[outPortNum];
			
			// Get the current mean
			double mean = _hasConstantMean ? _constantMeanValue : _meanVariable.getCurrentSample();
			
			// Start with the ports to variable outputs
			ArrayList<INode> siblings = _factor.getSiblings();
			double sum = 0;
			for (int port = _numParameterEdges; port < _numPorts; port++)
			{
				double value = ((SRealVariable)(((VariableBase)siblings.get(port)).getSolver())).getCurrentSample();
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
		else
			availableSamplers.add(NormalSampler.factory);	// Either mean parameter or output, which have Normal distribution
		return availableSamplers;
	}
	
	public boolean isPortPrecisionParameter(int portNumber)
	{
		// This doesn't use the state values set up in initialize() since this may be called prior to initialize
		
		// Get the Normal factor function and related state
		FactorFunctionBase factorFunction = _factor.getFactorFunction();
		FactorFunctionWithConstants constantFactorFunction = null;
		boolean hasFactorFunctionConstants = false;
		if (factorFunction instanceof FactorFunctionWithConstants)	// In case the factor function is wrapped, get the Normal factor function within
		{
			hasFactorFunctionConstants = true;
			constantFactorFunction = (FactorFunctionWithConstants)factorFunction;
			factorFunction = constantFactorFunction.getContainedFactorFunction();
		}
		Normal normalFactorFunction = (Normal)(factorFunction);

		// Test whether or not the specified port is the precision parameter
		if (normalFactorFunction.hasConstantParameters())
		{
			return false;	// Port must be an output since all parameters are constant
		}
		else if (hasFactorFunctionConstants)
		{
			if (constantFactorFunction.isConstantIndex(PRECISION_PARAMETER_INDEX))
				return false;	// Precision parameter is constant, so it can't be a port
			else if (constantFactorFunction.isConstantIndex(MEAN_PARAMETER_INDEX))
				return (portNumber == (PRECISION_PARAMETER_INDEX - 1));	// This is precision only if port is precision parameter shifted by one since mean parameter is constant
			else if (portNumber == PRECISION_PARAMETER_INDEX)
				return true;	// This is the precision parameter since mean is also a port
		}
		else if (portNumber == PRECISION_PARAMETER_INDEX)
		{
			return true;	// No parameters are constant, so this is the right port
		}
		return false;		// No constants, but the specified port is not the precision port
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
		
		
		// Get the Normal factor function and related state
		FactorFunctionBase factorFunction = _factor.getFactorFunction();
		FactorFunctionWithConstants constantFactorFunction = null;
		boolean hasFactorFunctionConstants = false;
		if (factorFunction instanceof FactorFunctionWithConstants)	// In case the factor function is wrapped, get the Normal factor function within
		{
			hasFactorFunctionConstants = true;
			constantFactorFunction = (FactorFunctionWithConstants)factorFunction;
			factorFunction = constantFactorFunction.getContainedFactorFunction();
		}
		Normal normalFactorFunction = (Normal)(factorFunction);
		
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		boolean hasFactorFunctionConstructorConstants = normalFactorFunction.hasConstantParameters();
		if (hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_hasConstantMean = true;
			_hasConstantPrecision = true;
			_constantMeanValue = normalFactorFunction.getMean();
			_constantPrecisionValue = normalFactorFunction.getPrecision();
			_numParameterEdges = 0;
		}
		else // Variable or constant parameters
		{
			_numParameterEdges = 0;
			ArrayList<INode> siblings = _factor.getSiblings();
			if (hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(MEAN_PARAMETER_INDEX))
			{
				_hasConstantMean = true;
				_constantMeanValue = (Double)constantFactorFunction.getConstantByIndex(MEAN_PARAMETER_INDEX);
				_meanVariable = null;
			}
			else
			{
				_hasConstantMean = false;
				int meanEdgeIndex = _numParameterEdges++;
				_meanVariable = (SRealVariable)(((VariableBase)siblings.get(meanEdgeIndex)).getSolver());
			}
			if (hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(PRECISION_PARAMETER_INDEX))
			{
				_hasConstantPrecision = true;
				_constantPrecisionValue = (Double)constantFactorFunction.getConstantByIndex(PRECISION_PARAMETER_INDEX);
				_precisionVariable = null;
			}
			else
			{
				_hasConstantPrecision = false;
				int precisionEdgeIndex = _numParameterEdges++;
				_precisionVariable = (SRealVariable)(((VariableBase)siblings.get(precisionEdgeIndex)).getSolver());
			}
		}
		_numOutputEdges = _numPorts - _numParameterEdges;
		
		
		// Pre-compute statistics associated with any constant output values
		_hasConstantOutputs = false;
		if (hasFactorFunctionConstants)
		{
			Object[] constantValues = constantFactorFunction.getConstants();
			int[] constantIndices = constantFactorFunction.getConstantIndices();
			_constantOutputCount = 0;
			_constantOutputSum = 0;
			_constantOutputSumOfSquares = 0;
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
				{
					double outputValue = (Double)constantValues[i];
					_constantOutputSum += outputValue;
					_constantOutputSumOfSquares += outputValue*outputValue;
					_constantOutputCount++;
				}
			}
			_hasConstantOutputs = true;
		}
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
