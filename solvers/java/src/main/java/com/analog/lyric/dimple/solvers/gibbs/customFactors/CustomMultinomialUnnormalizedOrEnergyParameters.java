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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.MultinomialEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.MultinomialUnnormalizedParameters;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.dimple.schedulers.schedule.IGibbsSchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsGammaEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverEdge;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.GibbsVariableBlock;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.BlockMHInitializer;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.GammaSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.NegativeExpGammaSampler;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;

public class CustomMultinomialUnnormalizedOrEnergyParameters extends GibbsRealFactor
	implements IRealConjugateFactor, MultinomialBlockProposal.ICustomMultinomial
{
	private @Nullable GibbsDiscrete[] _outputVariables;
	private @Nullable GibbsDiscrete _NVariable;
	private @Nullable GibbsReal[] _alphaVariables;
	private int _dimension;
	private int _alphaParameterMinIndex;
	private int _alphaParameterMinEdge;
	private int _alphaParameterMaxEdge;
	private int _constantN;
	private @Nullable int[] _constantOutputCounts;
	private boolean _hasConstantN;
	private boolean _hasConstantOutputs;
	private boolean _hasConstantAlphas;
	private @Nullable boolean[] _hasConstantAlpha;
	private @Nullable double[] _constantAlpha;
	private boolean _useEnergyParameters;
	private static final int ALPHA_PARAMETER_MIN_INDEX_FIXED_N = 0;	// If N is in constructor then alpha is first index (0)
	private static final int N_PARAMETER_INDEX = 0;					// If N is not in constructor then N is the first index (0)
	private static final int ALPHA_PARAMETER_MIN_INDEX = 1;			// If N is not in constructor then alpha is second index (1)

	public CustomMultinomialUnnormalizedOrEnergyParameters(Factor factor, GibbsSolverGraph parent)
	{
		super(factor, parent);
	}

	@Override
	public @Nullable GibbsSolverEdge<?> createEdge(EdgeState edge)
	{
		final int portNum = edge.getFactorToVariableEdgeNumber();
		
		if (portNum >= _alphaParameterMinEdge && portNum <= _alphaParameterMaxEdge)
		{
			return new GibbsGammaEdge();
		}
		
		return null;
	}
	
	@SuppressWarnings("null")
	@Override
	public void updateEdgeMessage(EdgeState modelEdge, GibbsSolverEdge<?> solverEdge)
	{
		final int portNum = modelEdge.getFactorToVariableEdgeNumber();
		if (portNum >= _alphaParameterMinEdge && portNum <= _alphaParameterMaxEdge)
		{
			// Output port is a parameter input
			// Determine sample alpha and beta parameters
			// NOTE: This case works for either MultinomialUnnormalizedParameters or MultinomialEnergyParameters factor functions
			// since the actual parameter value doesn't come into play in determining the message in this direction

			GammaParameters outputMsg = (GammaParameters)solverEdge.factorToVarMsg;

			// The parameter being updated corresponds to this value
			int parameterOffset = _model.getIndexByEdge(portNum) - _alphaParameterMinIndex;

			// Get the count from the corresponding output
			int count = _hasConstantOutputs ? _constantOutputCounts[parameterOffset] : _outputVariables[parameterOffset].getCurrentSampleIndex();
						
			outputMsg.setAlphaMinusOne(count);		// Sample alpha
			outputMsg.setBeta(0);					// Sample beta
		}
		else
			super.updateEdgeMessage(modelEdge, solverEdge);
	}
	
	
	@Override
	public Set<IRealConjugateSamplerFactory> getAvailableRealConjugateSamplers(int portNumber)
	{
		Set<IRealConjugateSamplerFactory> availableSamplers = new HashSet<IRealConjugateSamplerFactory>();
		if (isPortAlphaParameter(portNumber))					// Conjugate sampler if edge is alpha parameter input
			if (_useEnergyParameters)
				availableSamplers.add(NegativeExpGammaSampler.factory);	// Parameter inputs have conjugate negative exp-Gamma distribution
			else
				availableSamplers.add(GammaSampler.factory);			// Parameter inputs have conjugate Gamma distribution
		return availableSamplers;
	}
	
	public boolean isPortAlphaParameter(int portNumber)
	{
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber >= _alphaParameterMinEdge && portNumber <= _alphaParameterMaxEdge);
	}


	// For MultinomialBlockProposal.ICustomMultinomial interface
	@SuppressWarnings("null")
	@Override
	public final double[] getCurrentAlpha()
	{
		double[] alphas = new double[_dimension];
		if (_hasConstantAlphas)
		{
			for (int i = 0; i < _dimension; i++)
				alphas[i] = _hasConstantAlpha[i] ? _constantAlpha[i] : _alphaVariables[i].getCurrentSample();
		}
		else	// Only variable alphas
		{
			for (int i = 0; i < _dimension; i++)
				alphas[i] = _alphaVariables[i].getCurrentSample();
		}

		return alphas;
	}
	@Override
	public final boolean isAlphaEnergyRepresentation()
	{
		return _useEnergyParameters;
	}
	@Override
	public final boolean hasConstantN()
	{
		return _hasConstantN;
	}
	@Override
	public final int getN()
	{
		return _constantN;
	}

	
	
	@SuppressWarnings("null")
	@Override
	public void initialize()
	{
		super.initialize();
		
		
		// Determine what parameters are constants or edges, and save the state
		determineConstantsAndEdges();
		
		
		// Create a block schedule entry with a BlockMHSampler and a MultinomialBlockProposal kernel
		Variable[] nodeList = new Variable[_outputVariables.length + (_hasConstantN ? 0 : 1)];
		int nodeIndex = 0;
		if (!_hasConstantN)
			nodeList[nodeIndex++] = _NVariable.getModelObject();
		for (int i = 0; i < _outputVariables.length; i++, nodeIndex++)
			nodeList[nodeIndex] = _outputVariables[i].getModelObject();

		GibbsSolverGraph parent = getParentGraph();
		VariableBlock block = getParentGraph().getModel().addVariableBlock(nodeList);
		GibbsVariableBlock sblock = requireNonNull(parent.getSolverVariableBlock(block, true));
		BlockMHInitializer blockSampler = new BlockMHInitializer(sblock, new MultinomialBlockProposal(this));
		BlockScheduleEntry blockScheduleEntry = new BlockScheduleEntry(blockSampler, block);
		
		// Add the block updater to the schedule
		GibbsSolverGraph rootGraph = (GibbsSolverGraph)parent.getRootSolverGraph(); // FIXME don't assume root
		IGibbsSchedule schedule = rootGraph.getSchedule(); // Assumes scheduler for Gibbs solver is flattened to root graph
		schedule.addBlockScheduleEntry(blockScheduleEntry);
			
		// Use the block sampler to initialize the neighboring variables
		rootGraph.addBlockInitializer(blockSampler);
	}
	
	
	private void determineConstantsAndEdges()
	{
		final int prevAlphaParameterMinEdge = _alphaParameterMinEdge;
		final int prevAlphaParameterMaxEdge = _alphaParameterMaxEdge;
		
		final Factor factor = _model;
		FactorFunction factorFunction = factor.getFactorFunction();
		FactorFunction containedFactorFunction = factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		boolean hasFactorFunctionConstructorConstantN;
		if (containedFactorFunction instanceof MultinomialUnnormalizedParameters)
		{
			MultinomialUnnormalizedParameters specificFactorFunction = (MultinomialUnnormalizedParameters)containedFactorFunction;
			hasFactorFunctionConstructorConstantN = specificFactorFunction.hasConstantNParameter();
			_dimension = specificFactorFunction.getDimension();
			_constantN = specificFactorFunction.getN();
			_useEnergyParameters = false;
		}
		else if (containedFactorFunction instanceof MultinomialEnergyParameters)
		{
			MultinomialEnergyParameters specificFactorFunction = (MultinomialEnergyParameters)containedFactorFunction;
			hasFactorFunctionConstructorConstantN = specificFactorFunction.hasConstantNParameter();
			_dimension = specificFactorFunction.getDimension();
			_constantN = specificFactorFunction.getN();
			_useEnergyParameters = true;
		}
		else
			throw new DimpleException("Invalid factor function");
		
		// Pre-determine whether or not the parameters are constant
		List<? extends Variable> siblings = factor.getSiblings();
		_NVariable = null;
		_hasConstantOutputs = false;
		_outputVariables = null;
		_alphaParameterMinIndex = hasFactorFunctionConstructorConstantN ? ALPHA_PARAMETER_MIN_INDEX_FIXED_N : ALPHA_PARAMETER_MIN_INDEX;
		int alphaParameterMaxIndex = _alphaParameterMinIndex + _dimension - 1;
		int outputMinIndex = alphaParameterMaxIndex + 1;
		if (hasFactorFunctionConstructorConstantN)
			_hasConstantN = true;
		else	// Variable or constant N
		{
			_hasConstantN = factor.isConstantIndex(N_PARAMETER_INDEX);
			if (_hasConstantN)
				_constantN = requireNonNull(factor.getConstantValueByIndex(N_PARAMETER_INDEX)).getInt();
			else
				_NVariable = (GibbsDiscrete)getSibling(factor.getEdgeByIndex(N_PARAMETER_INDEX));
		}
		
		final SolverNodeMapping solvers = getSolverMapping();
		
		// Save the alpha parameter constant or variables as well
		_hasConstantAlphas = false;
		_hasConstantAlpha = null;
		_constantAlpha = null;
		final GibbsReal[] alphaVariables = _alphaVariables = new GibbsReal[_dimension];
		_alphaParameterMinEdge = factor.getEdgeByIndex(_alphaParameterMinEdge);
		_alphaParameterMaxEdge = factor.getEdgeByIndex(alphaParameterMaxIndex);
		if (factor.hasConstantsInIndexRange(_alphaParameterMinIndex, alphaParameterMaxIndex))	// Some constant alphas
		{
			_hasConstantAlphas = true;
			final boolean[] hasConstantAlpha = _hasConstantAlpha = new boolean[_dimension];
			final double[] constantAlpha = _constantAlpha = new double[_dimension];
			for (int i = 0, index = _alphaParameterMinIndex; i < _dimension; i++, index++)
			{
				if (factor.isConstantIndex(index))
				{
					hasConstantAlpha[i] = true;
					constantAlpha[i] = requireNonNull(factor.getConstantValueByIndex(index)).getDouble();
				}
				else
				{
					hasConstantAlpha[i] = false;
					int alphaEdge = factor.getEdgeByIndex(index);
					alphaVariables[i] = (GibbsReal)solvers.getSolverVariable(siblings.get(alphaEdge));
				}
			}
		}
		else	// No constant alphas
		{
			for (int i = 0, index = _alphaParameterMinIndex; i < _dimension; i++, index++)
			{
				int alphaEdge = factor.getEdgeByIndex(index);
				alphaVariables[i] = (GibbsReal)solvers.getSolverVariable(siblings.get(alphaEdge));
			}
		}

		
		// Save the output constant or variables as well
		final int nEdges = getSiblingCount();
		int numOutputEdges = nEdges - factor.getEdgeByIndex(outputMinIndex);
		_hasConstantOutputs = factor.hasConstantAtOrAboveIndex(outputMinIndex);
		final GibbsDiscrete[] outputVariables = _outputVariables = new GibbsDiscrete[numOutputEdges];
		_hasConstantOutputs = factor.hasConstantAtOrAboveIndex(outputMinIndex);
		_constantOutputCounts = null;
		if (_hasConstantOutputs)
		{
			int numConstantOutputs = factor.numConstantsAtOrAboveIndex(outputMinIndex);
			final int[] constantOutputCounts = _constantOutputCounts = new int[numConstantOutputs];
			for (int i = 0, index = outputMinIndex; i < _dimension; i++, index++)
			{
				if (factor.isConstantIndex(index))
				{
					constantOutputCounts[i] = requireNonNull(factor.getConstantValueByIndex(index)).getInt();
				}
				else
				{
					int outputEdge = factor.getEdgeByIndex(index);
					outputVariables[i] = (GibbsDiscrete)solvers.getSolverVariable(siblings.get(outputEdge));
				}
			}
		}
		else	// No constant outputs
		{
			for (int i = 0, index = outputMinIndex; i < _dimension; i++, index++)
			{
				int outputEdge = factor.getEdgeByIndex(index);
				outputVariables[i] = (GibbsDiscrete)solvers.getSolverVariable(siblings.get(outputEdge));
			}
		}
		
		if (_alphaParameterMaxEdge != prevAlphaParameterMaxEdge ||
			_alphaParameterMinEdge != prevAlphaParameterMinEdge)
		{
			removeSiblingEdgeState();
		}
	}
}
