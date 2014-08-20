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

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.math.DimpleRandomGenerator;

public class NormalSampler implements IRealConjugateSampler
{
	private final double MAX_SIGMA = 1e12;
	private final NormalParameters _parameters = new NormalParameters();
	
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
		double totalPrecision = 0;
		double totalMeanPrecisionProduct = 0;
		
		if (input != null)
		{
			double mean = ((Normal)input).getMean();
			double precision = ((Normal)input).getPrecision();
			
			totalMeanPrecisionProduct += mean * precision;
			totalPrecision += precision;
		}
		
		int numPorts = ports.length;
		for (int port = 0; port < numPorts; port++)
		{
			// The message from each neighboring factor is an array with elements (mean, precision)
			NormalParameters message = requireNonNull((NormalParameters)(ports[port].getOutputMsg()));
			double mean = message.getMean();
			double precision = message.getPrecision();
			
			totalMeanPrecisionProduct += mean * precision;
			totalPrecision += precision;
		}
		
		double finalMean = (totalPrecision > 0) ? totalMeanPrecisionProduct / totalPrecision : 0;

		// Set the output
		NormalParameters parameters = (NormalParameters)aggregateParameters;
		parameters.setMean(finalMean);
		parameters.setPrecision(totalPrecision);
	}
	
	public final double nextSample(NormalParameters parameters)
	{
		double mean = parameters.getMean();
		double precision = parameters.getPrecision();
		if (precision > 0)
			return mean + DimpleRandomGenerator.rand.nextGaussian() / Math.sqrt(precision);
		else
			return mean + DimpleRandomGenerator.rand.nextGaussian() * MAX_SIGMA;
	}

	@Override
	public IParameterizedMessage createParameterMessage()
	{
		return new NormalParameters();
	}

	
	// A static factory that creates a sampler of this type
	public static final IRealConjugateSamplerFactory factory = new IRealConjugateSamplerFactory()
	{
		@Override
		public IRealConjugateSampler create() {return new NormalSampler();}
		
		@Override
		public boolean isCompatible(@Nullable FactorFunction factorFunction)
		{
			if (factorFunction == null)
				return true;
			else if (factorFunction instanceof Normal)
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
