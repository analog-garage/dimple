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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsGammaEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsNormalEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.NormalSampler;

public class CustomNormal extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable GibbsReal[] _outputVariables;
	private @Nullable GibbsReal _meanVariable;
	private @Nullable GibbsReal _precisionVariable;
	private boolean _hasConstantMean;
	private boolean _hasConstantPrecision;
	private boolean _hasConstantOutputs;
	private int _numParameterEdges;
	private int _meanParameterPort = NO_PORT;
	private int _precisionParameterPort = NO_PORT;
	private int _constantOutputCount;
	private double _constantMeanValue;
	private double _constantPrecisionValue;
	private double _constantOutputSum;
	private double _constantOutputSumOfSquares;
	private static final int NUM_PARAMETERS = 2;
	private static final int MEAN_PARAMETER_INDEX = 0;
	private static final int PRECISION_PARAMETER_INDEX = 1;
	private static final int NO_PORT = -1;
	
	public CustomNormal(Factor factor, GibbsSolverGraph parent)
	{
		super(factor, parent);
	}

	@Override
	public GibbsSolverEdge<?> createEdge(EdgeState edge)
	{
		final int portNum = edge.getFactorToVariableEdgeNumber();
		
		if (portNum == _precisionParameterPort)
		{
			return new GibbsGammaEdge();
		}
		else
		{
			return new GibbsNormalEdge();
		}
	}
	
	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(EdgeState modelEdge, GibbsSolverEdge<?> solverEdge)
	{
		final int portNum = modelEdge.getFactorToVariableEdgeNumber();
		if (portNum == _meanParameterPort)
		{
			// Port is the mean-parameter input
			// Determine sample mean and precision

			NormalParameters outputMsg = (NormalParameters)solverEdge.factorToVarMsg;

			// Start with the ports to variable outputs
			double sum = 0;
			for (int i = 0; i < _outputVariables.length; i++)
				sum += _outputVariables[i].getCurrentSample();
			int count = _outputVariables.length;

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

			GammaParameters outputMsg = (GammaParameters)solverEdge.factorToVarMsg;

			// Get the current mean
			double mean = _hasConstantMean ? _constantMeanValue : _meanVariable.getCurrentSample();

			// Start with the ports to variable outputs
			double sum = 0;
			for (int i = 0; i < _outputVariables.length; i++)
			{
				double value = _outputVariables[i].getCurrentSample();
				double diff = value - mean;
				sum += diff*diff;
			}
			int count = _outputVariables.length;

			// Include any constant outputs also
			if (_hasConstantOutputs)
			{
				// Sum of (outputValue - mean)^2 using pre-computed sum and sum of squares
				sum += _constantOutputCount*mean*mean + _constantOutputSumOfSquares - 2 * mean * _constantOutputSum;
				count += _constantOutputCount;
			}

			outputMsg.setAlphaMinusOne(0.5 * count);		// Sample alpha
			outputMsg.setBeta(0.5 * sum);			// Sample beta
		}
		else
		{
			// Port is directed output
			NormalParameters outputMsg = (NormalParameters)solverEdge.factorToVarMsg;

			outputMsg.setMean(_hasConstantMean ? _constantMeanValue : _meanVariable.getCurrentSample());
			outputMsg.setPrecision(_hasConstantPrecision ? _constantPrecisionValue : _precisionVariable.getCurrentSample());
		}
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
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber == _precisionParameterPort);
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
		final Factor factor = _model;
		FactorFunction factorFunction = factor.getFactorFunction();
		Normal specificFactorFunction = (Normal)factorFunction;
		boolean hasFactorFunctionConstants = factor.hasConstants();
		boolean hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();

		final int prevNumParameterEdges = _numParameterEdges;
		final int prevPrecisionParameterPort = _precisionParameterPort;
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		List<? extends Variable> siblings = _model.getSiblings();
		_hasConstantMean = false;
		_hasConstantPrecision = false;
		_meanParameterPort = NO_PORT;
		_precisionParameterPort = NO_PORT;
		_meanVariable = null;
		_precisionVariable = null;
		_constantMeanValue = 0;
		_constantPrecisionValue = 0;
		_numParameterEdges = 0;
		if (hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_hasConstantMean = true;
			_hasConstantPrecision = true;
			_constantMeanValue = specificFactorFunction.getMean();
			_constantPrecisionValue = specificFactorFunction.getPrecision();
		}
		else	// Variable or constant parameters
		{
			_hasConstantMean = factor.hasConstantAtIndex(MEAN_PARAMETER_INDEX);
			if (_hasConstantMean)	// Constant mean
				_constantMeanValue =
					requireNonNull(factor.getConstantValueByIndex(MEAN_PARAMETER_INDEX)).getDouble();
			else					// Variable mean
			{
				_meanParameterPort = factor.argIndexToSiblingNumber(MEAN_PARAMETER_INDEX);
				_meanVariable = (GibbsReal)getSibling(_meanParameterPort);
				_numParameterEdges++;
			}
			
			_hasConstantPrecision = factor.hasConstantAtIndex(PRECISION_PARAMETER_INDEX);
			if (_hasConstantPrecision)	// Constant precision
				_constantPrecisionValue =
				requireNonNull(factor.getConstantValueByIndex(PRECISION_PARAMETER_INDEX)).getDouble();
			else 						// Variable precision
			{
				_precisionParameterPort = factor.argIndexToSiblingNumber(PRECISION_PARAMETER_INDEX);
				_precisionVariable = (GibbsReal)getSibling(_precisionParameterPort);
				_numParameterEdges++;
			}
		}
		
		// Pre-compute statistics associated with any constant output values
		_hasConstantOutputs = false;
		_constantOutputCount = 0;
		_constantOutputSum = 0;
		_constantOutputSumOfSquares = 0;
		if (hasFactorFunctionConstants)
		{
			final List<Value> constantValues = factor.getConstantValues();
			int[] constantIndices = factor.getConstantIndices();
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
				{
					double outputValue = constantValues.get(i).getDouble();
					_constantOutputSum += outputValue;
					_constantOutputSumOfSquares += outputValue*outputValue;
					_constantOutputCount++;
				}
			}
			_hasConstantOutputs = true;
		}
	
		// Save output variables and add to the statistics any output variables that have fixed values
		int numVariableOutputs = 0;		// First, determine how many output variables are not fixed
		final int nEdges = getSiblingCount();
		for (int edge = _numParameterEdges; edge < nEdges; edge++)
			if (!(siblings.get(edge).hasFixedValue()))
				numVariableOutputs++;
		final GibbsReal[] outputVariables = _outputVariables = new GibbsReal[numVariableOutputs];
		for (int edge = _numParameterEdges, index = 0; edge < nEdges; edge++)
		{
			final GibbsReal outputVariable = (GibbsReal)getSibling(edge);
			final double knownValue = outputVariable.getKnownReal();
			
			if (knownValue == knownValue) // !NaN
			{
				double outputValue = knownValue;
				_constantOutputSum += outputValue;
				_constantOutputSumOfSquares += outputValue*outputValue;
				_constantOutputCount++;
				_hasConstantOutputs = true;
			}
			else
				outputVariables[index++] = outputVariable;
		}
		
		if (_precisionParameterPort != prevPrecisionParameterPort ||
			_numParameterEdges != prevNumParameterEdges)
		{
			// If anything changes, blow away the existing edge state.
			removeSiblingEdgeState();
		}
	}
}
