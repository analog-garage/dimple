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

import com.analog.lyric.dimple.factorfunctions.Dirichlet;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionBase;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.SRealJointVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class CustomDirichlet extends SRealFactor implements IRealJointConjugateFactor
{
	private IRealJointConjugateSampler[] _conjugateSampler;
	private Object[] _outputMsgs;
	private double[] _alpha;
	private SRealJointVariable _alphaVariable;
	private int _dimension;
	private int _numParameterEdges;
	private boolean _hasConstantParameters;
	private static final int PARAMETER_INDEX = 0;
	
	public CustomDirichlet(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		IRealJointConjugateSampler conjugateSampler = _conjugateSampler[outPortNum];
		if (conjugateSampler == null)
			super.updateEdgeMessage(outPortNum);
		else if (conjugateSampler instanceof DirichletSampler)
		{
			// Output port must be an output variable

			double[] outputMsg = (double[])_outputMsgs[outPortNum];
			
			if (_hasConstantParameters)
				System.arraycopy(_alpha, 0, outputMsg, 0, _dimension);
			else	// Variable parameters
				System.arraycopy(_alphaVariable.getCurrentSample(), 0, outputMsg, 0, _dimension);
		}
		else
			super.updateEdgeMessage(outPortNum);
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
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber >= _numParameterEdges);
	}

	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Determine if any ports can use a conjugate sampler
		_conjugateSampler = new IRealJointConjugateSampler[_numPorts];
		for (int port = 0; port < _numPorts; port++)
		{
			INode var = _factor.getSiblings().get(port);
			if (var instanceof RealJoint)
				_conjugateSampler[port] = ((SRealJointVariable)var.getSolver()).getConjugateSampler();
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
		boolean hasFactorFunctionConstants = false;
		if (factorFunction instanceof FactorFunctionWithConstants)	// In case the factor function is wrapped, get the specific factor function within
		{
			hasFactorFunctionConstants = true;
			constantFactorFunction = (FactorFunctionWithConstants)factorFunction;
			factorFunction = constantFactorFunction.getContainedFactorFunction();
		}
		Dirichlet specificFactorFunction = (Dirichlet)factorFunction;
				
		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		boolean hasFactorFunctionConstructorConstants = specificFactorFunction.hasConstantParameters();
		if (hasFactorFunctionConstructorConstants)
		{
			_hasConstantParameters = true;
			_numParameterEdges = 0;
			_alpha = specificFactorFunction.getParameters();
			_alphaVariable = null;
			_dimension = _alpha.length;
		}
		else // Variable or constant parameter
		{
			if (hasFactorFunctionConstants && constantFactorFunction.isConstantIndex(PARAMETER_INDEX))
			{
				_hasConstantParameters = true;
				_numParameterEdges = 0;
				_alpha = (double[])constantFactorFunction.getConstantByIndex(PARAMETER_INDEX);
				_alphaVariable = null;
				_dimension = _alpha.length;
			}
			else	// Parameter is a variable
			{
				_hasConstantParameters = false;
				_numParameterEdges = 1;
				_alpha = null;
				ArrayList<INode> siblings = _factor.getSiblings();
				_alphaVariable = (SRealJointVariable)(((VariableBase)siblings.get(PARAMETER_INDEX)).getSolver());
				_dimension = _alphaVariable.getDimension();
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
			_outputMsgs[port] = new double[_dimension];
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
		_outputMsgs[thisPortNum] = ((CustomDirichlet)other)._outputMsgs[otherPortNum];
	}
}
