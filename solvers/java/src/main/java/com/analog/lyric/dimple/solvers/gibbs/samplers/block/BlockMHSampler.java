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

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.proposalKernels.BlockProposal;
import com.analog.lyric.dimple.solvers.core.proposalKernels.BlockProposalKernelRegistry;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.GibbsVariableBlock;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariableBlock;
import com.analog.lyric.math.DimpleRandomGenerator;

/**
 * Metropolis-Hastings block initializer for {@link GibbsVariableBlock}s.
 * @since 0.06
 * @author jeffb
 */
public class BlockMHSampler implements IBlockMCMCSampler
{
	/*-------
	 * State
	 */
	
	@Nullable
	protected IBlockProposalKernel _proposalKernel;
	
	/*--------------
	 * Construction
	 */
	
	public BlockMHSampler()
	{
		this(null);
	}
	
	public BlockMHSampler(@Nullable IBlockProposalKernel proposalKernel)
	{
		_proposalKernel = proposalKernel;
	}
	
	/*-----------------------
	 * IBlockUpdater methods
	 */
	
	@Override
	public BlockMHSampler copy(Map<Object,Object> old2newobjs)
	{
		return new BlockMHSampler(_proposalKernel);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Only for use in Gibbs solver graphs. Will return false if {@code sblock} is not a {@link GibbsVariableBlock}.
	 */
	@Override
	public boolean update(ISolverVariableBlock sblock)
	{
		if (!(sblock instanceof GibbsVariableBlock))
		{
			return false;
		}
		
		GibbsVariableBlock block = (GibbsVariableBlock)sblock;
		
		final IBlockProposalKernel proposalKernel = _proposalKernel;
		if (proposalKernel == null)
			throw new DimpleException("Must specify a block proposal kernel. No default is defined.");

		final Value[] sampleValue = block.updateStart();
		
		final BlockProposal proposal = proposalKernel.next(sampleValue, block.getDomains());
		final Value[] proposalValue = proposal.value;

		// Get the potential for the current sample value
		final double LPrevious = block.getCurrentSampleScore();

		// Get the potential for the proposed sample value
		final double LProposed = block.getSampleScore(proposalValue);


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
			block.updateFinish(proposalValue);
		}
		else
		{
			block.updateReject();
		}
		
		return true;
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

}
