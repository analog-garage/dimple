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
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater;
import com.analog.lyric.dimple.solvers.core.proposalKernels.BlockProposal;
import com.analog.lyric.dimple.solvers.core.proposalKernels.BlockProposalKernelRegistry;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.GibbsNeighbors;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.ISolverNodeGibbs;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.util.misc.Internal;

/**
 * @since 0.06
 * @author jeffb
 */
public class BlockMHSampler implements IBlockMCMCSampler, IBlockInitializer
{
	private @Nullable IBlockProposalKernel _proposalKernel;
	private Variable[] _variables = new Variable[0];
	private @Nullable ISolverVariableGibbs[] _sVariables;
	private @Nullable GibbsSolverGraph _sRootGraph;
	private @Nullable Domain[] _domains;
	private int _numVariables = 0;
	private @Nullable Set<ISolverNodeGibbs> _neighbors;
	private long _updateCount;
	private long _rejectCount;
	
	
	public BlockMHSampler()
	{
		this(null);
	}
	
	public BlockMHSampler(@Nullable IBlockProposalKernel proposalKernel)
	{
		_proposalKernel = proposalKernel;
	}
	
	@Override
	public void attachNodes(@NonNull INode[] nodes)			// Nodes must all be variables
	{
		_numVariables = nodes.length;
		_variables = new Variable[_numVariables];
		final ISolverVariableGibbs[] sVariables = _sVariables = new ISolverVariableGibbs[_numVariables];
		final Domain[] domains = _domains = new Domain[_numVariables];
		for (int i = 0; i < _numVariables; i++)
		{
			Variable variable = (Variable)nodes[i];
			_variables[i] = variable;
			sVariables[i] = (ISolverVariableGibbs)variable.getSolver();
			domains[i] = variable.getDomain();
		}
		_sRootGraph = (GibbsSolverGraph)sVariables[0].getRootGraph();
		
		// Pre-determine neighbors that will need to be scored
		final Set<ISolverNodeGibbs> neighborSet = _neighbors = new HashSet<ISolverNodeGibbs>();
		for (int i = 0; i < _numVariables; i++)
		{
			GibbsNeighbors neighbors = GibbsNeighbors.create(sVariables[i]);
			if (neighbors == null)	// No deterministic dependents, neighbors are same as siblings
			{
				for (Factor f : _variables[i].getSiblings())
					neighborSet.add((ISolverNodeGibbs)f.getSolver());
			}
			else	// Has deterministic dependents
			{
				for (ISolverNodeGibbs n : neighbors)
					neighborSet.add(n);
			}
		}
	}
	
	@Override
	public void update()
	{
		final IBlockProposalKernel proposalKernel = _proposalKernel;
		if (proposalKernel == null)
			throw new DimpleException("Must specify a block proposal kernel. No default is defined.");

		final Value[] sampleValue = new Value[_numVariables];
		if (_numVariables > 0)
		{
			final ISolverVariableGibbs[] sVariables = Objects.requireNonNull(_sVariables);
			for (int i = 0; i < _numVariables; i++)
				sampleValue[i] = sVariables[i].getCurrentSampleValue().clone();
		}
		
		final BlockProposal proposal = proposalKernel.next(sampleValue, Objects.requireNonNull(_domains));
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
		{
			setNextSampleValue(proposalValue);		// Accept
		}
		else
		{
			setNextSampleValue(sampleValue);		// Reject
			_rejectCount++;
		}
		_updateCount++;
	}

	@Override
	public @NonNull INode[] getNodeList()
	{
		return getVariableList();
	}
	
	@Override
	public Variable[] getVariableList()
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
	public @Nullable IBlockProposalKernel getProposalKernel()
	{
		return _proposalKernel;
	}
	public String getProposalKernelName()
	{
		final IBlockProposalKernel proposalKernel = _proposalKernel;
		if (proposalKernel != null)
			return proposalKernel.getClass().getSimpleName();
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
		
		if (_numVariables > 0)
		{
			for (ISolverVariableGibbs v : Objects.requireNonNull(_sVariables))
				score += v.getPotential();
		
			for (ISolverNodeGibbs n : Objects.requireNonNull(_neighbors))
				score += n.getPotential();
		}
			
		return score;
	}

	public void setNextSampleValue(Value[] sampleValues)
	{
		setCurrentSample(sampleValues);	// TODO: Optimize by skipping if no change
	}

	public void setCurrentSample(Value[] sampleValues)
	{
		if (_numVariables > 0)
		{
			final GibbsSolverGraph sRootGraph = Objects.requireNonNull(_sRootGraph);
			final ISolverVariableGibbs[] sVariables = Objects.requireNonNull(_sVariables);
			sRootGraph.deferDeterministicUpdates();
			for (int i = 0; i < _numVariables; i++)
			{
				sVariables[i].setCurrentSample(sampleValues[i]);
			}
			sRootGraph.processDeferredDeterministicUpdates();
		}
	}
	
	/**
	 * Get the rejection rate of the sampler
	 * @return rejection rate
	 * @since 0.07
	 */
	@Override
	public final double getRejectionRate()
	{
		return (_updateCount > 0) ? (double)_rejectCount / (double)_updateCount : 0;
	}
	
	/**
	 * Clear the rejection rate statistics
	 * @since 0.07
	 */
	@Override
	public final void resetRejectionRateStats()
	{
		_updateCount = 0;
		_rejectCount = 0;
	}
	
	@Override
	@Internal
	public final long getUpdateCount()
	{
		return _updateCount;
	}
	
	@Override
	@Internal
	public final long getRejectionCount()
	{
		return _rejectCount;
	}


	// Make a new block updater of the same type, but with different variables
	@Override
	public @NonNull IBlockUpdater create()
	{
		return new BlockMHSampler(_proposalKernel);
	}
	
	
	// This sampler can be used as a block initializer, which is used to initialize blocks of variables that may
	// not be easily initialized by initializing each variable independently
	@Override
	public void initialize()
	{
		final IBlockProposalKernel proposalKernel = _proposalKernel;
		if (proposalKernel == null)
			throw new DimpleException("Must specify a block proposal kernel. No default is defined.");

		final Value[] sampleValue = new Value[_numVariables];
		if (_numVariables > 0)
		{
			final ISolverVariableGibbs[] sVariables = Objects.requireNonNull(_sVariables);
			for (int i = 0; i < _numVariables; i++)
				sampleValue[i] = sVariables[i].getCurrentSampleValue().clone();
		}
		
		final BlockProposal proposal = proposalKernel.next(sampleValue, Objects.requireNonNull(_domains));
		final Value[] proposalValue = proposal.value;

		setNextSampleValue(proposalValue);
		
		resetRejectionRateStats();
	}

}
