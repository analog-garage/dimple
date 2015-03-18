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
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.Dirichlet;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DirichletParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDirichletEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealJoint;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.IBlockInitializer;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;
import com.analog.lyric.math.DimpleRandomGenerator;

public class CustomDirichlet extends GibbsRealFactor implements IRealJointConjugateFactor
{
	private @Nullable double[] _constantAlphaMinusOne;
	private @Nullable GibbsRealJoint _alphaVariable;
	private int _dimension;
	private int _numParameterEdges;
	private boolean _hasConstantParameters;
	private static final int PARAMETER_INDEX = 0;
	
	public CustomDirichlet(Factor factor, GibbsSolverGraph parent)
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
		
		return super.createEdge(edge);
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
				outputMsg.setAlphaMinusOne(_constantAlphaMinusOne);
			else	// Variable parameters
				outputMsg.setAlphaMinusOne(minusOne(_alphaVariable.getCurrentSample()));
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

	
	
	@SuppressWarnings("null")
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Determine what parameters are constants or edges, and save the state
		determineConstantsAndEdges();
		
		// Create a block initializer to initialize the neighboring variables
		((GibbsSolverGraph)getRootSolverGraph()).addBlockInitializer(new CustomDirichlet.BlockInitializer());
	}
	
	
	private void determineConstantsAndEdges()
	{
		// Get the factor function and related state
		FactorFunction factorFunction = _model.getFactorFunction();
		Dirichlet specificFactorFunction = (Dirichlet)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped

		final int prevNumParameterEdges = _numParameterEdges;
		
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
				_alphaVariable = (GibbsRealJoint)getSibling(factorFunction.getEdgeByIndex(PARAMETER_INDEX));
				_dimension = requireNonNull(_alphaVariable).getDimension();
			}
		}
		
		if (_numParameterEdges != prevNumParameterEdges)
		{
			removeSiblingEdgeState();
		}
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
			final int nEdges = getSiblingCount();
			int numOutputEdges = nEdges - _numParameterEdges;
			if (numOutputEdges > 0)
			{
				double[] value = new double[_dimension];
				for (int edge = _numParameterEdges; edge < nEdges; edge++)
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
					GibbsRealJoint svar = (GibbsRealJoint)getSibling(edge);
					svar.setCurrentSample(value);
				}
			}
		}
	}

}
