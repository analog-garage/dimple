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

package com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate;

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.Beta;
import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.BetaParameters;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverEdgeState;
import com.analog.lyric.math.DimpleRandomGenerator;


public class BetaSampler implements IRealConjugateSampler
{
	private final BetaParameters _parameters = new BetaParameters();

	@Override
	public final double nextSample(ISolverEdgeState[] edges, @Nullable IUnaryFactorFunction input)
	{
		aggregateParameters(_parameters, edges, input);
		return nextSample(_parameters);
	}
	
	@Override
	public final void aggregateParameters(IParameterizedMessage aggregateParameters, ISolverEdgeState[] edges,
		@Nullable IUnaryFactorFunction input)
	{
		double alphaMinusOne = 0;
		double betaMinusOne = 0;
		
		if (input != null)
		{
			if (input instanceof BetaParameters)
			{
				BetaParameters betaInput = (BetaParameters)input;
				alphaMinusOne += betaInput.getAlphaMinusOne();
				betaMinusOne += betaInput.getBetaMinusOne();
			}
			else
			{
				Beta betaInput = (Beta)input;
				alphaMinusOne += betaInput.getAlphaMinusOne();
				betaMinusOne += betaInput.getBetaMinusOne();
			}
		}
		
		final int numEdges = edges.length;
		for (int i = 0; i < numEdges; i++)
		{
			// The message from each neighboring factor is an array with elements (alpha, beta)
			BetaParameters message = requireNonNull((BetaParameters)edges[i].getFactorToVarMsg());
			alphaMinusOne += message.getAlphaMinusOne();
			betaMinusOne += message.getBetaMinusOne();
		}
		
		// Set the output
		BetaParameters parameters = (BetaParameters)aggregateParameters;
		parameters.setAlphaMinusOne(alphaMinusOne);
		parameters.setBetaMinusOne(betaMinusOne);
	}

	public final double nextSample(BetaParameters parameters)
	{
		double alphaMinusOne = parameters.getAlphaMinusOne();
		double betaMinusOne = parameters.getBetaMinusOne();
		return DimpleRandomGenerator.randBeta.nextDouble(alphaMinusOne + 1, betaMinusOne + 1);
	}
	
	@Override
	public IParameterizedMessage createParameterMessage()
	{
		return new BetaParameters();
	}
	
	
	// A static factory that creates a sampler of this type
	public static final IRealConjugateSamplerFactory factory = new IRealConjugateSamplerFactory()
	{
		@Override
		public IRealConjugateSampler create() {return new BetaSampler();}
		
		@Override
		public boolean isCompatible(@Nullable IUnaryFactorFunction factorFunction)
		{
			if (factorFunction == null)
				return true;
			else if (factorFunction instanceof Beta || factorFunction instanceof BetaParameters)
				return true;
			else
				return false;
		}
		
		@Override
		public boolean isCompatible(RealDomain domain)
		{
			return (domain.getLowerBound() <= 0) && (domain.getUpperBound() >= 1);
		}

	};
}
