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

import com.analog.lyric.dimple.factorfunctions.Beta;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.solvers.core.SolverRandomGenerator;


public class BetaSampler implements IRealConjugateSampler
{
	private final BetaParameters _parameters = new BetaParameters();

	@Override
	public final double nextSample(Port[] ports, FactorFunction input)
	{
		aggregateParameters(_parameters, ports, input);
		return nextSample(_parameters);
	}
	
	@Override
	public final void aggregateParameters(IParameterizedMessage aggregateParameters, Port[] ports, FactorFunction input)
	{
		double alpha = 0;
		double beta = 0;
		
		if (input != null)
		{
			alpha += ((Beta)input).getAlpha();
			beta += ((Beta)input).getBeta();
		}
		
		int numPorts = ports.length;
		for (int port = 0; port < numPorts; port++)
		{
			// The message from each neighboring factor is an array with elements (alpha, beta)
			BetaParameters message = (BetaParameters)(ports[port].getOutputMsg());
			alpha += message.getAlpha();
			beta += message.getBeta();
		}
		
		// Set the output
		BetaParameters parameters = (BetaParameters)aggregateParameters;
		parameters.setAlpha(alpha);
		parameters.setBeta(beta);
	}

	public final double nextSample(BetaParameters parameters)
	{
		double alpha = parameters.getAlpha();
		double beta = parameters.getBeta();
		return SolverRandomGenerator.randBeta.nextDouble(alpha, beta);
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
		public boolean isCompatible(FactorFunction factorFunction)
		{
			if (factorFunction == null)
				return true;
			else if (factorFunction instanceof Beta)
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
