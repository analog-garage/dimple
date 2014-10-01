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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.CategoricalEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.CategoricalUnnormalizedParameters;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.NegativeExpGammaSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class CustomCategoricalUnnormalizedOrEnergyParameters extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable Object[] _outputMsgs;
	private @Nullable GibbsDiscrete[] _outputVariables;
	private @Nullable FactorFunction _factorFunction;
	private int _parameterDimension;
	private int _numParameterEdges;
	private @Nullable int[] _constantOutputCounts;
	private boolean _hasConstantOutputs;
	private boolean _useEnergyParameters;

	public CustomCategoricalUnnormalizedOrEnergyParameters(Factor factor)
	{
		super(factor);
	}

	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum < _numParameterEdges)
		{
			// Port is a parameter input
			// Determine sample alpha and beta parameters
			// NOTE: This case works for either CategoricalUnnormalizedParameters or CategoricalEnergyParameters factor functions
			// since the actual parameter value doesn't come into play in determining the message in this direction

			GammaParameters outputMsg = (GammaParameters)_outputMsgs[portNum];

			// The parameter being updated corresponds to this value
			int parameterIndex = _factorFunction.getIndexByEdge(portNum);

			// Start with the ports to variable outputs
			int count = 0;
			for (int i = 0; i < _outputVariables.length; i++)
			{
				int outputIndex = _outputVariables[i].getCurrentSampleIndex();
				if (outputIndex == parameterIndex)
					count++;
			}
			
			// Include any constant outputs also
			if (_hasConstantOutputs)
				count += _constantOutputCounts[parameterIndex];
			
			outputMsg.setAlphaMinusOne(count);		// Sample alpha
			outputMsg.setBeta(0);					// Sample beta
		}
		else
			super.updateEdgeMessage(portNum);
	}
	
	
	@Override
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		if (isPortParameter(portNumber))					// Conjugate sampler if edge is a parameter input
			if (_useEnergyParameters)
				availableSamplers.add(NegativeExpGammaSampler.factory);	// Parameter inputs have conjugate negative exp-Gamma distribution
			else
				availableSamplers.add(GammaSampler.factory);			// Parameter inputs have conjugate Gamma distribution
		return availableSamplers;
	}
	
	public boolean isPortParameter(int portNumber)
	{
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber < _numParameterEdges);
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
		FactorFunction containedFactorFunction = factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		_factorFunction = factorFunction;
		boolean hasFactorFunctionConstants = factorFunction.hasConstants();
		boolean hasFactorFunctionConstructorConstants;
		if (containedFactorFunction instanceof CategoricalUnnormalizedParameters)
		{
			CategoricalUnnormalizedParameters specificFactorFunction = (CategoricalUnnormalizedParameters)containedFactorFunction;
			hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();
			_parameterDimension = specificFactorFunction.getDimension();
			_useEnergyParameters = false;
		}
		else if (containedFactorFunction instanceof CategoricalEnergyParameters)
		{
			CategoricalEnergyParameters specificFactorFunction = (CategoricalEnergyParameters)containedFactorFunction;
			hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();
			_parameterDimension = specificFactorFunction.getDimension();
			_useEnergyParameters = true;
		}
		else
			throw new DimpleException("Invalid factor function");

		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		List<? extends Variable> siblings = _factor.getSiblings();
		_numParameterEdges = _parameterDimension;
		_hasConstantOutputs = false;
		if (hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_numParameterEdges = 0;
			_hasConstantOutputs = hasFactorFunctionConstants;
		}
		else if (hasFactorFunctionConstants)
		{
			_hasConstantOutputs = factorFunction.hasConstantAtOrAboveIndex(_parameterDimension);
			int numConstantParameters = factorFunction.numConstantsInIndexRange(0, _parameterDimension - 1);
			_numParameterEdges = _parameterDimension - numConstantParameters;
		}

	
		// Pre-compute statistics associated with any constant output values
		_constantOutputCounts = null;
		if (_hasConstantOutputs)
		{
			Object[] constantValues = factorFunction.getConstants();
			int[] constantIndices = factorFunction.getConstantIndices();
			final int[] constantOutputCounts = _constantOutputCounts = new int[_parameterDimension];
			for (int i = 0; i < constantIndices.length; i++)
			{
				if (hasFactorFunctionConstructorConstants || constantIndices[i] >= _parameterDimension)
				{
					int outputValue = FactorFunctionUtilities.toInteger(constantValues[i]);
					constantOutputCounts[outputValue]++;	// Histogram among constant outputs
	}
			}
		}
	
	
		// Save output variables and add to the statistics any output variables that have fixed values
		int numVariableOutputs = 0;		// First, determine how many output variables are not fixed
		for (int edge = _numParameterEdges; edge < _numPorts; edge++)
			if (!(siblings.get(edge).hasFixedValue()))
				numVariableOutputs++;
		final GibbsDiscrete[] outputVariables = _outputVariables = new GibbsDiscrete[numVariableOutputs];
		for (int edge = _numParameterEdges, index = 0; edge < _numPorts; edge++)
		{
			Discrete outputVariable = (Discrete)siblings.get(edge);
			if (outputVariable.hasFixedValue())
			{
				int outputValue = outputVariable.getFixedValueIndex();
				int[] constantOutputCounts = _constantOutputCounts;
				if (constantOutputCounts == null)
					constantOutputCounts = _constantOutputCounts = new int[_parameterDimension];
				constantOutputCounts[outputValue]++;	// Histogram among constant outputs
				_hasConstantOutputs = true;
			}
			else
				outputVariables[index++] = (GibbsDiscrete)outputVariable.getSolver();
		}
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		final Object[] outputMsgs = _outputMsgs = new Object[_numPorts];
		for (int port = 0; port < _numParameterEdges; port++)	// Only parameter edges
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
		_outputMsgs[thisPortNum] = ((CustomCategoricalUnnormalizedOrEnergyParameters)other)._outputMsgs[otherPortNum];
	}
}
