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

package com.analog.lyric.dimple.solvers.gibbs.samplers.generic;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.SolverRandomGenerator;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.Proposal;
import com.analog.lyric.dimple.solvers.core.proposalKernels.ProposalKernelRegistry;
import com.analog.lyric.dimple.solvers.core.proposalKernels.UniformDiscreteProposalKernel;

public class MHSampler implements IMCMCSampler
{
	protected IProposalKernel _proposalKernel;

	@Override
	public void initialize(Domain variableDomain)
	{
		if (_proposalKernel == null)
		{
			// Set default proposal kernel
			if (variableDomain instanceof DiscreteDomain)
				_proposalKernel = new UniformDiscreteProposalKernel();
			else
				_proposalKernel = new NormalProposalKernel();
		}
	}
	
	@Override
	public void nextSample(Value sampleValue, ISamplerClient samplerClient)
	{
		final Proposal proposal = _proposalKernel.next(sampleValue, samplerClient.getDomain());
		final Value proposalValue = proposal.value;

		// Get the potential for the current sample value
		final double LPrevious = samplerClient.getCurrentSampleScore();

		// Get the potential for the proposed sample value
		final double LProposed = samplerClient.getSampleScore(proposalValue);


		// Accept or reject
		final double rejectionThreshold = Math.exp(LPrevious - LProposed + proposal.hastingsTerm);
		if (SolverRandomGenerator.rand.nextDouble() < rejectionThreshold)
			samplerClient.setNextSampleValue(proposalValue);		// Accept
		else
			samplerClient.setNextSampleValue(sampleValue);			// Reject
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
		if (_proposalKernel != null)
			return _proposalKernel.getClass().getSimpleName();
		else
			return "";
	}


}
