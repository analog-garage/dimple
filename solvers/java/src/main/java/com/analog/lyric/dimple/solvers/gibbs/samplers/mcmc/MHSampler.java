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

package com.analog.lyric.dimple.solvers.gibbs.samplers.mcmc;

import com.analog.lyric.dimple.solvers.core.SolverRandomGenerator;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.Proposal;
import com.analog.lyric.dimple.solvers.core.proposalKernels.ProposalKernelRegistry;

public class MHSampler implements IRealMCMCSampler
{
	protected IProposalKernel _proposalKernel = new NormalProposalKernel();		// Normal proposal kernel by default

	@Override
	public double nextSample(ISampleScorer sampleScorer)
	{
		double currentSampleValue = sampleScorer.getCurrentSampleValue();
		Proposal proposal = _proposalKernel.next(currentSampleValue);
		double proposalValue = (Double)proposal.value;

		// Get the potential for the current sample value
		double LPrevious = sampleScorer.getCurrentSampleScore();

		// Get the potential for the proposed sample value
		double LProposed = sampleScorer.getSampleScore(proposalValue);


		// Accept or reject
		double rejectionThreshold = Math.exp(LPrevious - LProposed + proposal.hastingsTerm);
		if (SolverRandomGenerator.rand.nextDouble() < rejectionThreshold)
			return proposalValue;		// Accept
		else
			return currentSampleValue;	// Reject
	}


	public void setProposalKernel(IProposalKernel proposalKernel)
	{
		_proposalKernel = proposalKernel;
	}
	public void setProposalKernel(String proposalKernelName)
	{
		_proposalKernel = ProposalKernelRegistry.get(proposalKernelName);
	}
	public IProposalKernel getProposalKernel()
	{
		return _proposalKernel;
	}
	public String getProposalKernelName()
	{
		return _proposalKernel.getClass().getSimpleName();
	}


}
