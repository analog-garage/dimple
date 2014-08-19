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

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.Proposal;
import com.analog.lyric.dimple.solvers.core.proposalKernels.UniformDiscreteProposalKernel;
import com.analog.lyric.math.DimpleRandomGenerator;

public class MHSampler implements IMCMCSampler
{
	protected @Nullable IProposalKernel _proposalKernel;

	@Override
	public void initialize(Domain variableDomain)
	{
		if (_proposalKernel == null)
		{
			// Set default proposal kernel
			if (variableDomain.isDiscrete())
			{
				_proposalKernel = new UniformDiscreteProposalKernel();
			}
			else
			{
				_proposalKernel = new NormalProposalKernel();
			}
		}
	}
	
	@Override
	public boolean nextSample(Value sampleValue, ISamplerClient samplerClient)
	{
		final Proposal proposal = Objects.requireNonNull(_proposalKernel).next(sampleValue, samplerClient.getDomain());
		final Value proposalValue = proposal.value;

		// Get the potential for the current sample value
		final double LPrevious = samplerClient.getCurrentSampleScore();

		// Get the potential for the proposed sample value
		final double LProposed = samplerClient.getSampleScore(proposalValue);


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
			samplerClient.setNextSampleValue(proposalValue);		// Accept
			return true;
		}
		else
		{
			samplerClient.setNextSampleValue(sampleValue);			// Reject
			return false;
		}
	}


	public void setProposalKernel(IProposalKernel proposalKernel)
	{
		_proposalKernel = proposalKernel;
	}
	public void setProposalKernel(String proposalKernelName)
	{
		_proposalKernel = DimpleEnvironment.active().proposalKernels().instantiate(proposalKernelName);
	}
	public @Nullable IProposalKernel getProposalKernel()
	{
		return _proposalKernel;
	}
	public String getProposalKernelName()
	{
		final IProposalKernel proposalKernel = _proposalKernel;
		if (proposalKernel != null)
			return proposalKernel.getClass().getSimpleName();
		else
			return "";
	}


}
