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

package com.analog.lyric.dimple.solvers.gibbs.customFactors;

import static java.util.Objects.*;

import java.util.Arrays;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.proposalKernels.BlockProposal;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel;
import com.analog.lyric.math.DimpleRandomGenerator;
import org.eclipse.jdt.annotation.Nullable;

/**
 * 
 * @since 0.06
 * @author jeffb
 * 
 * This is a block proposal generator to support block updates for multinomial factors for the
 * output and N (total count) variables.
 * Proposals are made from the prior distribution given the current value of the alpha parameters,
 * taking no account of other neighboring factors.
 * This can be used in the context of the BlockMHSampler to generate samples for the variables in this block.
 */
public class MultinomialBlockProposal implements IBlockProposalKernel
{
	private ICustomMultinomial _customFactor;
	private boolean _hasConstantN;
	private int _constantN;
	
	public MultinomialBlockProposal(ICustomMultinomial customFactor)
	{
		_customFactor = customFactor;
		_hasConstantN = customFactor.hasConstantN();
		_constantN = customFactor.getN();
	}
	
	
	// Make proposal
	@Override
	public BlockProposal next(Value[] currentValue, Domain[] variableDomain)
	{
		double proposalForwardEnergy = 0;
		double proposalReverseEnergy = 0;
		int argumentIndex = 0;
		int argumentLength = currentValue.length;
		Value[] newValue = new Value[argumentLength];
		for (int i = 0; i < argumentLength; i++)
			newValue[i] = Value.create(variableDomain[i]);
		
		// Get the current alpha values
		double[] alpha;
		double[] alphaEnergy;
		double alphaSum = 0;
		if (_customFactor.isAlphaEnergyRepresentation())
		{
			alphaEnergy = _customFactor.getCurrentAlpha();
			alpha = new double[alphaEnergy.length];
			for (int i = 0; i < alphaEnergy.length; i++)
			{
				alpha[i] = Math.exp(-alphaEnergy[i]);
				alphaSum += alpha[i];
			}
		}
		else
		{
			alpha = _customFactor.getCurrentAlpha();
			alphaEnergy = new double[alpha.length];
			for (int i = 0; i < alpha.length; i++)
			{
				alphaEnergy[i] = -Math.log(alpha[i]);
				alphaSum += alpha[i];
			}
		}
		if (alphaSum == 0)	// Shouldn't happen, but can during initialization
		{
			Arrays.fill(alpha, 1);
			Arrays.fill(alphaEnergy, 0);
			alphaSum = alpha.length;
		}

		int nextN = _constantN;
		if (!_hasConstantN)
		{
			// If N is variable, sample N uniformly
			int previousN = currentValue[argumentIndex].getIndex();
			int NDomainSize = requireNonNull(variableDomain[0].asDiscrete()).size();
			nextN = DimpleRandomGenerator.rand.nextInt(NDomainSize);
			newValue[argumentIndex].setIndex(nextN);
			argumentIndex++;
			
			// Add this portion of -log p(x_proposed -> x_previous)
			proposalReverseEnergy += -org.apache.commons.math3.special.Gamma.logGamma(previousN + 1) + previousN * Math.log(alphaSum);
			
			// Add this portion of -log p(x_previous -> x_proposed)
			proposalForwardEnergy += -org.apache.commons.math3.special.Gamma.logGamma(nextN + 1) + nextN * Math.log(alphaSum);
		}

		
		// Given N and alpha, resample the outputs
		// Multinomial formed by successively sampling from a binomial and subtracting each count from the total
		// FIXME: Assumes all outputs are variable (no constant outputs)
		int remainingN = nextN;
		int alphaIndex = 0;
		for (; argumentIndex < argumentLength; argumentIndex++, alphaIndex++)
		{
			double alphai = alpha[alphaIndex];
			double alphaEnergyi = alphaEnergy[alphaIndex];
			int previousX = currentValue[argumentIndex].getIndex();
			int nextX;
			if (argumentIndex < argumentLength - 1)
				nextX = DimpleRandomGenerator.randomBinomial(remainingN, alphai/alphaSum);
			else	// Last value
				nextX = remainingN;
			newValue[argumentIndex].setIndex(nextX);
			remainingN -= nextX;	// Subtract the sample value from the remaining total count
			alphaSum -= alphai;		// Subtract this alpha value from the sum used for normalization
			
			double previousXNegativeLogAlphai;
			double nextXNegativeLogAlphai;
			if (alphai == 0 && previousX == 0)
				previousXNegativeLogAlphai = 0;
			else
				previousXNegativeLogAlphai = previousX * alphaEnergyi;
			if (alphai == 0 && nextX == 0)
				nextXNegativeLogAlphai = 0;
			else
				nextXNegativeLogAlphai = nextX * alphaEnergyi;
			
			// Add this portion of -log p(x_proposed -> x_previous)
			proposalReverseEnergy += previousXNegativeLogAlphai + org.apache.commons.math3.special.Gamma.logGamma(previousX + 1);
			
			// Add this portion of -log p(x_previous -> x_proposed)
			proposalForwardEnergy += nextXNegativeLogAlphai + org.apache.commons.math3.special.Gamma.logGamma(nextX + 1);
		}
		
		return new BlockProposal(newValue, proposalForwardEnergy, proposalReverseEnergy);
	}
	
	@Override
	public void setParameters(Object... parameters) {}
	@Override
	public @Nullable Object[] getParameters() {return null;}
	
	
	// Interface to be used by the custom factor using this proposal, allowing this class to access additional information needed
	public interface ICustomMultinomial
	{
		// Get the current value of the alpha parameters
		public double[] getCurrentAlpha();
		
		// Whether the alpha parameters are represented in energy or (not necessarily normalized) probability representation
		public boolean isAlphaEnergyRepresentation();
		
		// Whether or not the N parameter is constant
		public boolean hasConstantN();
		
		// If the N parameter is constant, the value of N
		public int getN();
	}
	
}
