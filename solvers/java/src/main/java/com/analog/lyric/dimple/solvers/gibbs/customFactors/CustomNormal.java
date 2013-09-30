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
import java.util.HashSet;
import java.util.Set;

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionBase;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.NormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.NormalSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class CustomNormal extends SRealFactor implements IRealConjugateFactor
{
	private IRealConjugateSampler[] _conjugateSampler;
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
				// Output port is directed output
				outputMsg.setMean(_hasConstantMean ? _constantMeanValue : _meanVariable.getCurrentSample());
				outputMsg.setPrecision(_hasConstantPrecision ? _constantPrecisionValue : _precisionVariable.getCurrentSample());
			}
			else
			{
				// Output port must be the mean-parameter input
				// Determine sample mean and precision
				
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
				
				// Get the current precision
				double precision = _hasConstantPrecision ? _constantPrecisionValue : _precisionVariable.getCurrentSample();
				
				outputMsg.setMean(sum / count);				// Sample mean
				outputMsg.setPrecision(precision * count);	// Sample precision
			}
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
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		if (isPortPrecisionParameter(portNumber))
			availableSamplers.add(GammaSampler.factory);	// Precision parameter has Gamma conjugate distribution
		else
			availableSamplers.add(NormalSampler.factory);	// Either mean parameter or output, which have Normal distribution
		return availableSamplers;
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
					double outputValue = (Double)constantValues[i];
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
		Normal specificFactorFunction = (Normal)factorFunction;
		
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();
		ArrayList<INode> siblings = _factor.getSiblings();
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
		
		// Save output variables
		_outputVariables = new SRealVariable[_numOutputEdges];
		for (int i = 0; i < _numOutputEdges; i++)
			_outputVariables[i] = (SRealVariable)(((VariableBase)siblings.get(i + _numParameterEdges)).getSolver());
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
			else
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
		_outputMsgs[thisPortNum] = ((CustomNormal)other)._outputMsgs[otherPortNum];
	}
}
