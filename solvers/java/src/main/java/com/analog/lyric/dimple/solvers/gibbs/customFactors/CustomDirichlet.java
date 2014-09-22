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

import com.analog.lyric.dimple.factorfunctions.Dirichlet;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DirichletParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealJoint;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.IBlockInitializer;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.math.DimpleRandomGenerator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class CustomDirichlet extends GibbsRealFactor implements IRealJointConjugateFactor
{
	private @Nullable Object[] _outputMsgs;
	private @Nullable double[] _constantAlphaMinusOne;
	private @Nullable GibbsRealJoint _alphaVariable;
	private int _dimension;
	private int _numParameterEdges;
	private boolean _hasConstantParameters;
	private static final int PARAMETER_INDEX = 0;
	
	public CustomDirichlet(Factor factor)
	{
		super(factor);
	}

	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum >= _numParameterEdges)
		{
			// Output port must be an output variable

			DirichletParameters outputMsg = (DirichletParameters)_outputMsgs[portNum];
			
			if (_hasConstantParameters)
				outputMsg.setAlphaMinusOne(_constantAlphaMinusOne);
			else	// Variable parameters
				outputMsg.setAlphaMinusOne(minusOne(_alphaVariable.getCurrentSample()));
		}
		else
			super.updateEdgeMessage(portNum);
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

	
	
	@SuppressWarnings("null")
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Determine what parameters are constants or edges, and save the state
		determineConstantsAndEdges();
		
		// Create a block initializer to initialize the neighboring variables
		((GibbsSolverGraph)_factor.getRootGraph().getSolver()).addBlockInitializer(new CustomDirichlet.BlockInitializer());
	}
	
	
	private void determineConstantsAndEdges()
	{
		// Get the factor function and related state
		FactorFunction factorFunction = _factor.getFactorFunction();
		Dirichlet specificFactorFunction = (Dirichlet)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		if (specificFactorFunction.hasConstantParameters())
		{
			_hasConstantParameters = true;
			_numParameterEdges = 0;
			final double[] constantAlphaMinusOne = _constantAlphaMinusOne =
				specificFactorFunction.getAlphaMinusOneArray();
			_alphaVariable = null;
			_dimension = constantAlphaMinusOne.length;
		}
		else // Variable or constant parameter
		{
			_hasConstantParameters = factorFunction.isConstantIndex(PARAMETER_INDEX);
			if (_hasConstantParameters)
			{
				_numParameterEdges = 0;
				@SuppressWarnings("null")
				final double[] constantAlphaMinusOne = _constantAlphaMinusOne =
					minusOne((double[])factorFunction.getConstantByIndex(PARAMETER_INDEX));
				_alphaVariable = null;
				_dimension = constantAlphaMinusOne.length;
			}
			else	// Parameter is a variable
			{
				_numParameterEdges = 1;
				_constantAlphaMinusOne = null;
				List<? extends Variable> siblings = _factor.getSiblings();
				_alphaVariable =
					(GibbsRealJoint)((siblings.get(factorFunction.getEdgeByIndex(PARAMETER_INDEX))).getSolver());
				_dimension = requireNonNull(_alphaVariable).getDimension();
			}
		}
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		final Object[] outputMsgs = _outputMsgs = new Object[_numPorts];
		for (int port = _numParameterEdges; port < _numPorts; port++)	// Only output edges
			outputMsgs[port] = new DirichletParameters(_dimension);
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
		_outputMsgs[thisPortNum] = ((CustomDirichlet)other)._outputMsgs[otherPortNum];
	}
	
	
	private double[] minusOne(double[] in)
	{
		double[] out = new double[in.length];
		for (int i = 0; i < in.length; i++)
			out[i] = in[i] - 1;
		return out;
	}
	
	
	
	public class BlockInitializer implements IBlockInitializer
	{
		@Override
		public void initialize()
		{
			int numOutputEdges = _numPorts - _numParameterEdges;
			if (numOutputEdges > 0)
			{
				List<? extends Variable> siblings = _factor.getSiblings();
				double[] value = new double[_dimension];
				for (int edge = _numParameterEdges; edge < _numPorts; edge++)
				{
					// Sample uniformly from the simplex
					double sum = 0;
					for (int i = 0; i < _dimension; i++)
					{
						double v = -Math.log(DimpleRandomGenerator.rand.nextDouble());	// Sample from an exponential distribution
						value[i] = v;
						sum += v;
					}
					for (int i = 0; i < _dimension; i++)
						value[i] /= sum;												// Normalize

					// Set the output variable value
					GibbsRealJoint svar = ((GibbsRealJoint)((siblings.get(edge)).getSolver()));
					requireNonNull(svar).setCurrentSample(value);
				}
			}
		}
	}

}
