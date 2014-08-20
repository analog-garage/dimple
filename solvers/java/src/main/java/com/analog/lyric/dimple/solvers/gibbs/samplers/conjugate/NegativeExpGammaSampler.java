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

import com.analog.lyric.dimple.factorfunctions.NegativeExpGamma;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.math.DimpleRandomGenerator;


public class NegativeExpGammaSampler implements IRealConjugateSampler
{
	private final GammaParameters _parameters = new GammaParameters();
	
	@Override
	public final double nextSample(Port[] ports, @Nullable FactorFunction input)
	{
		aggregateParameters(_parameters, ports, input);
		return nextSample(_parameters);
	}
	
	@Override
	public final void aggregateParameters(IParameterizedMessage aggregateParameters, Port[] ports,
		@Nullable FactorFunction input)
	{
		double alphaMinusOne = 0;
		double beta = 0;
		
		if (input != null)
		{
			alphaMinusOne += ((NegativeExpGamma)input).getAlphaMinusOne();
			beta += ((NegativeExpGamma)input).getBeta();
		}
		
		int numPorts = ports.length;
		for (int port = 0; port < numPorts; port++)
		{
			// The message from each neighboring factor is an array with elements (alpha, beta)
			GammaParameters message = requireNonNull((GammaParameters)(ports[port].getOutputMsg()));
			alphaMinusOne += message.getAlphaMinusOne();
			beta += message.getBeta();
		}
		
		// Set the output
		GammaParameters parameters = (GammaParameters)aggregateParameters;
		parameters.setAlphaMinusOne(alphaMinusOne);
		parameters.setBeta(beta);
	}

	public final double nextSample(GammaParameters parameters)
	{
		double alphaMinusOne = parameters.getAlphaMinusOne();
		double beta = parameters.getBeta();
		return -Math.log(DimpleRandomGenerator.randGamma.nextDouble(alphaMinusOne + 1, beta));
	}
	
	@Override
	public IParameterizedMessage createParameterMessage()
	{
		return new GammaParameters();
	}

	
	// A static factory that creates a sampler of this type
	public static final IRealConjugateSamplerFactory factory = new IRealConjugateSamplerFactory()
	{
		@Override
		public IRealConjugateSampler create() {return new NegativeExpGammaSampler();}
		
		@Override
		public boolean isCompatible(@Nullable FactorFunction factorFunction)
		{
			if (factorFunction == null)
				return true;
			else if (factorFunction instanceof NegativeExpGamma)
				return true;
			else
				return false;
		}
		
		@Override
		public boolean isCompatible(RealDomain domain)
		{
			return (domain.getLowerBound() == Double.NEGATIVE_INFINITY) && (domain.getUpperBound() == Double.POSITIVE_INFINITY);
		}

	};
}
