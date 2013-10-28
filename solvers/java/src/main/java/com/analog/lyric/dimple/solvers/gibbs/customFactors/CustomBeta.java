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

import com.analog.lyric.dimple.factorfunctions.Beta;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionBase;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.BetaParameters;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.BetaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class CustomBeta extends SRealFactor implements IRealConjugateFactor
{
	private IRealConjugateSampler[] _conjugateSampler;
	private Object[] _outputMsgs;
	private SRealVariable _alphaVariable;
	private SRealVariable _betaVariable;
	private boolean _hasConstantAlpha;
	private boolean _hasConstantBeta;
	private boolean _hasFactorFunctionConstants;
	private boolean _hasFactorFunctionConstructorConstants;
	private int _numParameterEdges;
	private int _alphaParameterPort = -1;
	private int _betaParameterPort = -1;
	private double _constantAlphaValue;
	private double _constantBetaValue;
	private static final int ALPHA_PARAMETER_INDEX = 0;
	private static final int BETA_PARAMETER_INDEX = 1;
	private static final int NO_PORT = -1;

	public CustomBeta(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		IRealConjugateSampler conjugateSampler = _conjugateSampler[outPortNum];
		if (conjugateSampler == null)
			super.updateEdgeMessage(outPortNum);
		else if (conjugateSampler instanceof BetaSampler)
		{
			// Output port is directed output
			BetaParameters outputMsg = (BetaParameters)_outputMsgs[outPortNum];
			outputMsg.setAlpha(_hasConstantAlpha ? _constantAlphaValue : _alphaVariable.getCurrentSample());
			outputMsg.setBeta(_hasConstantBeta ? _constantBetaValue : _betaVariable.getCurrentSample());
		}
		else
			super.updateEdgeMessage(outPortNum);
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
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber >= _numParameterEdges);
	}

	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Determine if any ports can use a conjugate sampler
		_conjugateSampler = new IRealConjugateSampler[_numPorts];
		for (int port = 0; port < _numPorts; port++)
		{
			INode var = _factor.getSibling(port);
			if (var instanceof Real)
				_conjugateSampler[port] = ((SRealVariable)var.getSolver()).getConjugateSampler();
			else
				_conjugateSampler[port] = null;
		}
		
		
		// Determine what parameters are constants or edges, and save the state
		determineParameterConstantsAndEdges();
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
		Beta specificFactorFunction = (Beta)factorFunction;
		
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		_hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();
		if (_hasFactorFunctionConstructorConstants)
		{
			// The factor function has fixed parameters provided in the factor-function constructor
			_hasConstantAlpha = true;
			_hasConstantBeta = true;
			_alphaParameterPort = NO_PORT;
			_betaParameterPort = NO_PORT;
			_constantAlphaValue = specificFactorFunction.getAlpha();
			_constantBetaValue = specificFactorFunction.getBeta();
			_numParameterEdges = 0;
		}
		else // Variable or constant parameters
		{
			_numParameterEdges = 0;
			List<INode> siblings = _factor.getSiblings();
			if (_hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(ALPHA_PARAMETER_INDEX))
			{
				_hasConstantAlpha = true;
				_alphaParameterPort = NO_PORT;
				_constantAlphaValue = (Double)constantFactorFunction.getConstantByIndex(ALPHA_PARAMETER_INDEX);
				_alphaVariable = null;
			}
			else
			{
				_hasConstantAlpha = false;
				_alphaParameterPort = _numParameterEdges++;
				_alphaVariable = (SRealVariable)(((VariableBase)siblings.get(_alphaParameterPort)).getSolver());
			}
			if (_hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(BETA_PARAMETER_INDEX))
			{
				_hasConstantBeta = true;
				_betaParameterPort = NO_PORT;
				_constantBetaValue = (Double)constantFactorFunction.getConstantByIndex(BETA_PARAMETER_INDEX);
				_betaVariable = null;
			}
			else
			{
				_hasConstantBeta = false;
				_betaParameterPort = _numParameterEdges++;
				_betaVariable = (SRealVariable)(((VariableBase)siblings.get(_betaParameterPort)).getSolver());
			}
		}
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		_outputMsgs = new Object[_numPorts];
		for (int port = _numParameterEdges; port < _numPorts; port++)	// Only output edges
			_outputMsgs[port] = new BetaParameters();
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
		_outputMsgs[thisPortNum] = ((CustomBeta)other)._outputMsgs[otherPortNum];
	}
}
