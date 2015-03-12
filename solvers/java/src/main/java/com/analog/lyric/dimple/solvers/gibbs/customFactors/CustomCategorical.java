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

import com.analog.lyric.dimple.factorfunctions.Categorical;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DirichletParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDirichletEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;

public class CustomCategorical extends GibbsRealFactor implements IRealJointConjugateFactor
{
	private @Nullable GibbsDiscrete[] _outputVariables;
	private int _parameterDimension;
	private int _numParameterEdges;
	private @Nullable int[] _constantOutputCounts;
	private boolean _hasConstantOutputs;
	private static final int NUM_PARAMETERS = 1;
	private static final int PARAMETER_INDEX = 0;
	
	public CustomCategorical(Factor factor, GibbsSolverGraph parent)
	{
		super(factor, parent);
	}

	@Override
	public @Nullable GibbsSolverEdge<?> createEdge(EdgeState edge)
	{
		if (edge.getFactorToVariableIndex() < _numParameterEdges)
		{
			return new GibbsDirichletEdge(_parameterDimension);
		}
		
		return super.createEdge(edge);
	}
	
	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum < _numParameterEdges)
		{
			// Output port is a joint parameter input
			// Determine sample alpha vector of the conjugate Dirichlet distribution
			// Note: This case works for the Categorical factor function (which has joint parameters)
			
			DirichletParameters outputMsg = (DirichletParameters)getSiblingEdgeState(portNum).factorToVarMsg;
			
			// Clear the output counts
			outputMsg.setNull();
			
			// Start with the ports to variable outputs
			for (int i = 0; i < _outputVariables.length; i++)
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
		FactorFunction factorFunction = _model.getFactorFunction();
		Categorical specificFactorFunction = (Categorical)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		boolean hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();

		final int prevNumParameterEdges = _numParameterEdges;
		
		// Pre-determine whether or not the parameters are constant
		List<? extends Variable> siblings = _model.getSiblings();
		if (hasFactorFunctionConstructorConstants)
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
				_parameterDimension = requireNonNull(constantParameters).length;
			}
			else
			{
				_parameterDimension = (((RealJoint)siblings.get(PARAMETER_INDEX))).getRealDomain().getNumVars();
			}
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
				if (hasFactorFunctionConstructorConstants || constantIndices[i] >= NUM_PARAMETERS)
				{
					int outputValue = FactorFunctionUtilities.toInteger(constantValues[i]);
					constantOutputCounts[outputValue]++;	// Histogram among constant outputs
				}
			}
		}
	
	
		// Save output variables and add to the statistics any output variables that have fixed values
		int numVariableOutputs = 0;		// First, determine how many output variables are not fixed
		final int nEdges = getSiblingCount();
		for (int edge = _numParameterEdges; edge < nEdges; edge++)
			if (!(siblings.get(edge).hasFixedValue()))
				numVariableOutputs++;
		final GibbsDiscrete[] outputVariables = _outputVariables = new GibbsDiscrete[numVariableOutputs];
		for (int edge = _numParameterEdges, index = 0; edge < nEdges; edge++)
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
		
		if (_numParameterEdges != prevNumParameterEdges)
		{
			removeSiblingEdgeState();
		}
	}
}
