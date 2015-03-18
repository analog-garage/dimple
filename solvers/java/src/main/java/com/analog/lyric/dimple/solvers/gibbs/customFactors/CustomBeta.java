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
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.Beta;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.BetaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsBetaEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.BetaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;

public class CustomBeta extends GibbsRealFactor implements IRealConjugateFactor
{
	private @Nullable GibbsReal _alphaVariable;
	private @Nullable GibbsReal _betaVariable;
	private boolean _hasConstantAlpha;
	private boolean _hasConstantBeta;
	private boolean _hasFactorFunctionConstructorConstants;
	private int _numParameterEdges;
	private int _alphaParameterPort = -1;
	private int _betaParameterPort = -1;
	private double _constantAlphaMinusOneValue;
	private double _constantBetaMinusOneValue;
	private static final int ALPHA_PARAMETER_INDEX = 0;
	private static final int BETA_PARAMETER_INDEX = 1;
	private static final int NO_PORT = -1;

	public CustomBeta(Factor factor, GibbsSolverGraph parent)
	{
		super(factor, parent);
	}

	@Override
	public @Nullable GibbsSolverEdge<?> createEdge(EdgeState edge)
	{
		if (edge.getFactorToVariableEdgeNumber() >= _numParameterEdges)
		{
			return new GibbsBetaEdge();
		}
		
		return super.createEdge(edge);
	}
	
	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(EdgeState modelEdge, GibbsSolverEdge<?> solverEdge)
	{
		final int portNum = modelEdge.getFactorToVariableEdgeNumber();
		if (portNum >= _numParameterEdges)
		{
			// Port is directed output
			BetaParameters outputMsg = (BetaParameters)solverEdge.factorToVarMsg;
			outputMsg.setAlphaMinusOne(_hasConstantAlpha ? _constantAlphaMinusOneValue : _alphaVariable.getCurrentSample() - 1);
			outputMsg.setBetaMinusOne(_hasConstantBeta ? _constantBetaMinusOneValue : _betaVariable.getCurrentSample() - 1);
		}
		else
			super.updateEdgeMessage(modelEdge, solverEdge);
	}
	
	
	@Override
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		if (isPortOutputVariable(portNumber))
			availableSamplers.add(BetaSampler.factory);		// Output variables have Beta distribution
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
		FactorFunction factorFunction = _model.getFactorFunction();
		Beta specificFactorFunction = (Beta)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		_hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();

		final int prevNumParameterEdges = _numParameterEdges;
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_hasConstantAlpha = false;
		_hasConstantBeta = false;
		_alphaParameterPort = NO_PORT;
		_betaParameterPort = NO_PORT;
		_alphaVariable = null;
		_betaVariable = null;
		_constantAlphaMinusOneValue = 0;
		_constantBetaMinusOneValue = 0;
		_numParameterEdges = 0;
		if (_hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_hasConstantAlpha = true;
			_hasConstantBeta = true;
			_constantAlphaMinusOneValue = specificFactorFunction.getAlphaMinusOne();
			_constantBetaMinusOneValue = specificFactorFunction.getBetaMinusOne();
		}
		else	// Variable or constant parameters
		{
			_hasConstantAlpha = factorFunction.isConstantIndex(ALPHA_PARAMETER_INDEX);
			if (_hasConstantAlpha)	// Constant mean
				_constantAlphaMinusOneValue = FactorFunctionUtilities.toDouble(factorFunction.getConstantByIndex(ALPHA_PARAMETER_INDEX)) - 1;
			else					// Variable mean
			{
				_alphaParameterPort = factorFunction.getEdgeByIndex(ALPHA_PARAMETER_INDEX);
				_alphaVariable = (GibbsReal)getSibling(_alphaParameterPort);
				_numParameterEdges++;
			}
			
			_hasConstantBeta = factorFunction.isConstantIndex(BETA_PARAMETER_INDEX);
			if (_hasConstantBeta)	// Constant precision
				_constantBetaMinusOneValue = FactorFunctionUtilities.toDouble(factorFunction.getConstantByIndex(BETA_PARAMETER_INDEX)) - 1;
			else 						// Variable precision
			{
				_betaParameterPort = factorFunction.getEdgeByIndex(BETA_PARAMETER_INDEX);
				_betaVariable = (GibbsReal)getSibling(_betaParameterPort);
				_numParameterEdges++;
			}
		}
		
		if (_numParameterEdges != prevNumParameterEdges)
		{
			removeSiblingEdgeState();
		}
	}
}
