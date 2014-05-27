/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.gibbs.samplers.block;

import java.util.HashSet;
import java.util.Set;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater;
import com.analog.lyric.dimple.solvers.core.proposalKernels.BlockProposal;
import com.analog.lyric.dimple.solvers.core.proposalKernels.BlockProposalKernelRegistry;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.GibbsNeighbors;
import com.analog.lyric.dimple.solvers.gibbs.ISolverNodeGibbs;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;
import com.analog.lyric.dimple.solvers.gibbs.SFactorGraph;
import com.analog.lyric.math.DimpleRandomGenerator;

/**
 * @since 0.06
 * @author jeffb
 */
public class BlockMHSampler implements IBlockSampler, IBlockInitializer
{
	private IBlockProposalKernel _proposalKernel;
	private VariableBase[] _variables;
	private ISolverVariableGibbs[] _sVariables;
	private SFactorGraph _sRootGraph;
	private Domain[] _domains;
	private int _numVariables;
	private Set<ISolverNodeGibbs> _neighbors;
	
	
	public BlockMHSampler() {}
	public BlockMHSampler(IBlockProposalKernel proposalKernel)
	{
		_proposalKernel = proposalKernel;
	}
	
	
	@Override
	public void attachNodes(INode[] nodes)			// Nodes must all be variables
	{
		_numVariables = nodes.length;
		_variables = new VariableBase[_numVariables];
		_sVariables = new ISolverVariableGibbs[_numVariables];
		_domains = new Domain[_numVariables];
		for (int i = 0; i < _numVariables; i++)
		{
			VariableBase variable = (VariableBase)nodes[i];
			_variables[i] = variable;
			_sVariables[i] = (ISolverVariableGibbs)variable.getSolver();
			_domains[i] = variable.getDomain();
		}
		_sRootGraph = (SFactorGraph)_sVariables[0].getRootGraph();
		
		// Pre-determine neighbors that will need to be scored
		_neighbors = new HashSet<ISolverNodeGibbs>();
		for (int i = 0; i < _numVariables; i++)
		{
			GibbsNeighbors neighbors = GibbsNeighbors.create(_sVariables[i]);
			if (neighbors == null)	// No deterministic dependents, neighbors are same as siblings
			{
				for (Factor f : _variables[i].getSiblings())
					_neighbors.add((ISolverNodeGibbs)f.getSolver());
			}
			else	// Has deterministic dependents
			{
				for (ISolverNodeGibbs n : neighbors)
					_neighbors.add(n);
			}
		}
	}
	
	@Override
	public void update()
	{
		if (_proposalKernel == null)
			throw new DimpleException("Must specify a block proposal kernel. No default is defined.");

		final Value[] sampleValue = new Value[_numVariables];
		for (int i = 0; i < _numVariables; i++)
			sampleValue[i] = _sVariables[i].getCurrentSampleValue().clone();
		
		final BlockProposal proposal = _proposalKernel.next(sampleValue, _domains);
		final Value[] proposalValue = proposal.value;

		// Get the potential for the current sample value
		final double LPrevious = getCurrentSampleScore();

		// Get the potential for the proposed sample value
		final double LProposed = getSampleScore(proposalValue);


		// Accept or reject
		double rejectionThreshold = Math.exp(LPrevious - LProposed + proposal.forwardEnergy - proposal.reverseEnergy);
		if (Double.isNaN(rejectionThreshold))	// Account for invalid forward or reverse proposals
		{
			if (LProposed != Double.POSITIVE_INFINITY && proposal.forwardEnergy != Double.POSITIVE_INFINITY)
				rejectionThreshold = Double.POSITIVE_INFINITY;
			else
				rejectionThreshold = 0;
		}
		if (DimpleRandomGenerator.rand.nextDouble() < rejectionThreshold)
			setNextSampleValue(proposalValue);		// Accept
		else
			setNextSampleValue(sampleValue);		// Reject
	}

	@Override
	public INode[] getNodeList()
	{
		return getVariableList();
	}
	
	@Override
	public VariableBase[] getVariableList()
	{
		return _variables.clone();
	}

	public void setProposalKernel(IBlockProposalKernel proposalKernel)
	{
		_proposalKernel = proposalKernel;
	}
	public void setProposalKernel(String proposalKernelName)
	{
		_proposalKernel = BlockProposalKernelRegistry.get(proposalKernelName);
	}
	public IBlockProposalKernel getProposalKernel()
	{
		return _proposalKernel;
	}
	public String getProposalKernelName()
	{
		if (_proposalKernel != null)
			return _proposalKernel.getClass().getSimpleName();
		else
			return "";
	}


	public double getSampleScore(Value[] sampleValues)
	{
		// WARNING: Side effect is that the current sample value changes to this sample value
		// Could change back but less efficient to do this, since we'll be updating the sample value anyway
		setCurrentSample(sampleValues);

		return getCurrentSampleScore();
	}

	public double getCurrentSampleScore()
	{
		double score = 0;
		
		for (ISolverVariableGibbs v : _sVariables)
			score += v.getPotential();
		
		for (ISolverNodeGibbs n : _neighbors)
			score += n.getPotential();
			
		return score;
	}

	public void setNextSampleValue(Value[] sampleValues)
	{
		setCurrentSample(sampleValues);	// TODO: Optimize by skipping if no change
	}

	public void setCurrentSample(Value[] sampleValues)
	{
		_sRootGraph.deferDeterministicUpdates();
		for (int i = 0; i < _numVariables; i++)
			_sVariables[i].setCurrentSample(sampleValues[i]);
		_sRootGraph.processDeferredDeterministicUpdates();
	}

	// Make a new block updater of the same type, but with different variables
	@Override
	public IBlockUpdater create()
	{
		return new BlockMHSampler(_proposalKernel);
	}
	
	
	// This sampler can be used as a block initializer, which is used to initialize blocks of variables that may
	// not be easily initialized by initializing each variable independently
	@Override
	public void initialize()
	{
		if (_proposalKernel == null)
			throw new DimpleException("Must specify a block proposal kernel. No default is defined.");

		final Value[] sampleValue = new Value[_numVariables];
		for (int i = 0; i < _numVariables; i++)
			sampleValue[i] = _sVariables[i].getCurrentSampleValue().clone();
		
		final BlockProposal proposal = _proposalKernel.next(sampleValue, _domains);
		final Value[] proposalValue = proposal.value;

		setNextSampleValue(proposalValue);
	}

}
