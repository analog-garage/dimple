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
import com.analog.lyric.dimple.solvers.core.proposalKernels.ProposalKernelOptionKey;
import com.analog.lyric.dimple.solvers.core.proposalKernels.UniformDiscreteProposalKernel;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.options.IOptionHolder;

/**
 * Single-variable Metropolis-Hastings sampler.
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class MHSampler extends AbstractGenericSampler implements IMCMCSampler
{
	protected @Nullable IProposalKernel _proposalKernel;
	protected boolean _useDiscreteKernel;
	protected boolean _explicitKernel = false;
	
	/**
	 * Option specifies which discrete proposal kernel to use for MHSampler
	 * <p>
	 * Default value is {@link UniformDiscreteProposalKernel}.
	 * <p>
	 * @since 0.07
	 */
	public static final ProposalKernelOptionKey discreteProposalKernel =
		new ProposalKernelOptionKey(MHSampler.class, "discreteProposalKernel", UniformDiscreteProposalKernel.class);
	
	/**
	 * Option specifies which real proposal kernel to use for MHSampler
	 * <p>
	 * Default value is {@link NormalProposalKernel}.
	 * <p>
	 * @since 0.07
	 */
	public static final ProposalKernelOptionKey realProposalKernel =
		new ProposalKernelOptionKey(MHSampler.class, "realProposalKernel", NormalProposalKernel.class);
	
	@Override
	public void initialize(Domain variableDomain)
	{
		_useDiscreteKernel = variableDomain.isDiscrete();
	}
	
	@Override
	public void configureFromOptions(IOptionHolder optionHolder)
	{
		IProposalKernel kernel = _proposalKernel;
		
		if (kernel == null || !_explicitKernel)
		{
			ProposalKernelOptionKey key = _useDiscreteKernel ? discreteProposalKernel : realProposalKernel;
			_proposalKernel = kernel = key.instantiateIfDifferent(optionHolder, kernel);
		}
		
		kernel.configureFromOptions(optionHolder);
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
		_explicitKernel = true;
	}
	
	public void setProposalKernel(String proposalKernelName)
	{
		setProposalKernel(DimpleEnvironment.active().proposalKernels().instantiate(proposalKernelName));
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
