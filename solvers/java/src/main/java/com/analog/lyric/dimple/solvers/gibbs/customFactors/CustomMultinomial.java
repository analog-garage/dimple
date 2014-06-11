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

import com.analog.lyric.dimple.factorfunctions.Multinomial;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.schedulers.IGibbsScheduler;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DirichletParameters;
import com.analog.lyric.dimple.solvers.gibbs.SDiscreteVariable;
import com.analog.lyric.dimple.solvers.gibbs.SFactorGraph;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.SRealJointVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.BlockMHSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class CustomMultinomial extends SRealFactor implements IRealJointConjugateFactor, MultinomialBlockProposal.ICustomMultinomial
{
	private Object[] _outputMsgs;
	private SDiscreteVariable[] _outputVariables;
	private SDiscreteVariable _NVariable;
	private SRealJointVariable _alphaVariable;
	private int _dimension;
	private int _alphaParameterEdge;
	private int _constantN;
	private double[] _constantAlpha;
	private int[] _constantOutputCounts;
	private boolean _hasConstantN;
	private boolean _hasConstantAlpha;
	private boolean _hasConstantOutputs;
	private boolean[] _hasConstantOutput;
	private static final int NO_PORT = -1;
	private static final int ALPHA_PARAMETER_INDEX_FIXED_N = 0;	// If N is in constructor then alpha is first index (0)
	private static final int OUTPUT_MIN_INDEX_FIXED_N = 1;		// If N is in constructor then output starts at second index (1)
	private static final int N_PARAMETER_INDEX = 0;				// If N is not in constructor then N is the first index (0)
	private static final int ALPHA_PARAMETER_INDEX = 1;			// If N is not in constructor then alpha is second index (1)
	private static final int OUTPUT_MIN_INDEX = 2;				// If N is not in constructor then output starts at third index (2)
	
	public CustomMultinomial(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum == _alphaParameterEdge)
		{
			// Output port is the joint alpha parameter input
			// Determine sample alpha vector of the conjugate Dirichlet distribution
			
			DirichletParameters outputMsg = (DirichletParameters)_outputMsgs[portNum];

			// Clear the output counts
			outputMsg.setNull(_dimension);

			// Get the current output counts
			if (!_hasConstantOutputs)
			{
				for (int i = 0; i < _dimension; i++)
					outputMsg.add(i, _outputVariables[i].getCurrentSampleIndex());
			}
			else	// Some or all outputs are constant
			{
				for (int i = 0, iVar = 0, iConst = 0; i < _dimension; i++)
					outputMsg.add(i, _hasConstantOutput[i] ? _constantOutputCounts[iConst++] : _outputVariables[iVar++].getCurrentSampleIndex());
			}
		}
		else
			super.updateEdgeMessage(portNum);
	}
	
	
	@Override
	public Set<IRealJointConjugateSamplerFactory> getAvailableRealJointConjugateSamplers(int portNumber)
	{
		Set<IRealJointConjugateSamplerFactory> availableSamplers = new HashSet<IRealJointConjugateSamplerFactory>();
		if (isPortAlphaParameter(portNumber))						// Conjugate sampler if edge is alpha parameter input
			availableSamplers.add(DirichletSampler.factory);		// Parameter inputs have conjugate Dirichlet distribution
		return availableSamplers;
	}
	
	public boolean isPortAlphaParameter(int portNumber)
	{
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber == _alphaParameterEdge);
	}

	// For MultinomialBlockProposal.ICustomMultinomial interface
	public final double[] getCurrentAlpha()
	{
		return (_hasConstantAlpha ? _constantAlpha : _alphaVariable.getCurrentSample()).clone();
	}
	public final boolean isAlphaEnergyRepresentation()
	{
		return false;
	}
	public final boolean hasConstantN()
	{
		return _hasConstantN;
	}
	public final int getN()
	{
		return _constantN;
	}

	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		
		// Determine what parameters are constants or edges, and save the state
		determineConstantsAndEdges();
		
		
		// Create a block schedule entry with a BlockMHSampler and a MultinomialBlockProposal kernel
		BlockMHSampler blockSampler = new BlockMHSampler(new MultinomialBlockProposal(this));
		INode[] nodeList = new INode[_outputVariables.length + (_hasConstantN ? 0 : 1)];
		int nodeIndex = 0;
		if (!_hasConstantN)
			nodeList[nodeIndex++] = _NVariable.getModelObject();
		for (int i = 0; i < _outputVariables.length; i++, nodeIndex++)
			nodeList[nodeIndex] = _outputVariables[i].getModelObject();
		BlockScheduleEntry blockScheduleEntry = new BlockScheduleEntry(blockSampler, nodeList);
		
		// Add the block updater to the schedule
		FactorGraph rootGraph = _factor.getRootGraph();
		if (rootGraph.hasCustomSchedule())	// If there's a custom schedule
		{
			FixedSchedule schedule = (FixedSchedule)rootGraph.getSchedule();	// A custom schedule is a fixed schedule
			schedule.addBlockScheduleEntry(blockScheduleEntry);					// Add the block schedule entry, which replaces node entries for the nodes in the block
		}
		else	// There's a scheduler to create the schedule
		{
			IScheduler scheduler = rootGraph.getScheduler();	// Assumes scheduler for Gibbs solver is flattened to root graph
			if (scheduler instanceof IGibbsScheduler)
				((IGibbsScheduler)scheduler).addBlockScheduleEntry(blockScheduleEntry);
		}
			
		// Use the block sampler to initialize the neighboring variables
		((SFactorGraph)rootGraph.getSolver()).addBlockInitializer(blockSampler);
	}
	
	
	private void determineConstantsAndEdges()
	{
		FactorFunction factorFunction = _factor.getFactorFunction();
		Multinomial specificFactorFunction = (Multinomial)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped

		
		// Pre-determine whether or not the parameters are constant
		List<? extends VariableBase> siblings = _factor.getSiblings();
		int alphaParameterIndex;
		int outputMinIndex;
		_constantN = -1;
		_NVariable = null;
		if (specificFactorFunction.hasConstantNParameter())		// N parameter is constructor constant
		{
			_hasConstantN = true;
			_constantN = specificFactorFunction.getN();
			alphaParameterIndex = ALPHA_PARAMETER_INDEX_FIXED_N;
			outputMinIndex = OUTPUT_MIN_INDEX_FIXED_N;
		}
		else	// Variable or constant N parameter
		{
			_hasConstantN = factorFunction.isConstantIndex(N_PARAMETER_INDEX);
			if (_hasConstantN)
				_constantN = (Integer)factorFunction.getConstantByIndex(N_PARAMETER_INDEX);
			else
				_NVariable = (SDiscreteVariable)((siblings.get(factorFunction.getEdgeByIndex(N_PARAMETER_INDEX))).getSolver());
			alphaParameterIndex = ALPHA_PARAMETER_INDEX;
			outputMinIndex = OUTPUT_MIN_INDEX;
		}
		
		// Save the alpha parameter constant or variables
		_hasConstantAlpha = false;
		_constantAlpha = null;
		_alphaVariable = null;
		_alphaParameterEdge = NO_PORT;
		if (factorFunction.isConstantIndex(alphaParameterIndex))
		{
			_hasConstantAlpha = true;
			_constantAlpha = (double[])factorFunction.getConstantByIndex(alphaParameterIndex);
		}
		else
		{
			_alphaParameterEdge = factorFunction.getEdgeByIndex(alphaParameterIndex);
			_alphaVariable = (SRealJointVariable)((siblings.get(_alphaParameterEdge)).getSolver());
		}
		
		// Save the output constant or variables as well
		int numOutputEdges = _numPorts - factorFunction.getEdgeByIndex(outputMinIndex);
		_outputVariables = new SDiscreteVariable[numOutputEdges];
		_hasConstantOutputs = factorFunction.hasConstantAtOrAboveIndex(outputMinIndex);
		_constantOutputCounts = null;
		_hasConstantOutput = null;
		_dimension = -1;
		if (_hasConstantOutputs)
		{
			int numConstantOutputs = factorFunction.numConstantsAtOrAboveIndex(outputMinIndex);
			_dimension = numOutputEdges + numConstantOutputs;
			_hasConstantOutput = new boolean[_dimension];
			_constantOutputCounts = new int[numConstantOutputs];
			for (int i = 0, index = outputMinIndex; i < _dimension; i++, index++)
			{
				if (factorFunction.isConstantIndex(index))
				{
					_hasConstantOutput[i] = true;
					_constantOutputCounts[i] = (Integer)factorFunction.getConstantByIndex(index);
				}
				else
				{
					_hasConstantOutput[i] = false;
					int outputEdge = factorFunction.getEdgeByIndex(index);
					_outputVariables[i] = (SDiscreteVariable)((siblings.get(outputEdge)).getSolver());
				}
			}
		}
		else	// No constant outputs
		{
			_dimension = numOutputEdges;
			for (int i = 0, index = outputMinIndex; i < _dimension; i++, index++)
			{
				int outputEdge = factorFunction.getEdgeByIndex(index);
				_outputVariables[i] = (SDiscreteVariable)((siblings.get(outputEdge)).getSolver());
			}
		}

	}
	
	
	@Override
	public void createMessages() 
	{
		super.createMessages();
		determineConstantsAndEdges();	// Call this here since initialize may not have been called yet
		_outputMsgs = new Object[_numPorts];
		if (_alphaParameterEdge != NO_PORT)
			_outputMsgs[_alphaParameterEdge] = new DirichletParameters();
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
		_outputMsgs[thisPortNum] = ((CustomMultinomial)other)._outputMsgs[otherPortNum];
	}
	
	
	

	
}
