/*******************************************************************************
*   Copyright 2013-2015 Analog Devices, Inc.
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
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.ExchangeableDirichlet;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DirichletParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDirichletEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;

public class CustomExchangeableDirichlet extends GibbsRealFactor implements IRealJointConjugateFactor
{
	private double _constantAlphaMinusOne;
	private @Nullable GibbsReal _alphaVariable;
	private int _dimension;
	private int _numParameterEdges;
	private boolean _hasConstantParameters;
	private static final int PARAMETER_INDEX = 0;
	
	public CustomExchangeableDirichlet(Factor factor, GibbsSolverGraph parent)
	{
		super(factor, parent);
	}

	@Override
	public @Nullable GibbsSolverEdge<?> createEdge(EdgeState edge)
	{
		if (edge.getFactorToVariableEdgeNumber() >= _numParameterEdges)
		{
			return new GibbsDirichletEdge(_dimension);
		}
		
		return null;
	}
	
	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(EdgeState modelEdge, GibbsSolverEdge<?> solverEdge)
	{
		final int portNum = modelEdge.getFactorToVariableEdgeNumber();
		if (portNum >= _numParameterEdges)
		{
			// Output port must be an output variable

			DirichletParameters outputMsg = (DirichletParameters)solverEdge.factorToVarMsg;
			
			if (_hasConstantParameters)
				outputMsg.fillAlphaMinusOne(_constantAlphaMinusOne);
			else	// Variable parameters
				outputMsg.fillAlphaMinusOne(_alphaVariable.getCurrentSample() - 1);
		}
		else
			super.updateEdgeMessage(modelEdge, solverEdge);
	}
	
	
	@Override
	public Set<IRealJointConjugateSamplerFactory> getAvailableRealJointConjugateSamplers(int portNumber)
	{
		Set<IRealJointConjugateSamplerFactory> availableSamplers = new HashSet<IRealJointConjugateSamplerFactory>();
		if (isPortOutputVariable(portNumber))
			availableSamplers.add(DirichletSampler.factory);	// Output variables have Dirichlet distribution
		return availableSamplers;
	}
	
	public boolean isPortOutputVariable(int portNumber)
	{
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber >= _numParameterEdges);
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
		ExchangeableDirichlet specificFactorFunction = (ExchangeableDirichlet)factorFunction;
		_dimension = specificFactorFunction.getDimension();

		final int prevNumParameterEdges = _numParameterEdges;
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		if (specificFactorFunction.hasConstantParameters())
		{
			_hasConstantParameters = true;
			_numParameterEdges = 0;
			_constantAlphaMinusOne = specificFactorFunction.getAlphaMinusOne();
			_alphaVariable = null;
		}
		else // Variable or constant parameter
		{
			_hasConstantParameters = factor.hasConstantAtIndex(PARAMETER_INDEX);
			if (_hasConstantParameters)
			{
				_numParameterEdges = 0;
				_constantAlphaMinusOne =
					requireNonNull(factor.getConstantValueByIndex(PARAMETER_INDEX)).getDouble() - 1;
				_alphaVariable = null;
			}
			else	// Parameter is a variable
			{
				_numParameterEdges = 1;
				_constantAlphaMinusOne = 0;
				_alphaVariable = (GibbsReal)getSibling(PARAMETER_INDEX);
			}
		}
		
		if (_numParameterEdges != prevNumParameterEdges)
		{
			removeSiblingEdgeState();
		}
	}
}
