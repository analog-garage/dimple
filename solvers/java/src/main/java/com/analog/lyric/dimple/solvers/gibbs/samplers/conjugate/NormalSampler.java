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

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.solvers.core.SolverRandomGenerator;

public class NormalSampler implements IRealConjugateSampler
{
	private final double MAX_SIGMA = 1e12;
	
	@Override
	public double nextSample(Port[] ports, FactorFunction input)
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
			NormalParameters message = (NormalParameters)(ports[port].getOutputMsg());
			double mean = message.getMean();
			double precision = message.getPrecision();
			
			totalMeanPrecisionProduct += mean * precision;
			totalPrecision += precision;
		}
		
		double finalMean = (totalPrecision > 0) ? totalMeanPrecisionProduct / totalPrecision : 0;
		
		return nextSample(finalMean, totalPrecision);
	}
	
	public double nextSample(double mean, double precision)
	{
		if (precision > 0)
			return mean + SolverRandomGenerator.rand.nextGaussian() / Math.sqrt(precision);
		else
			return mean + SolverRandomGenerator.rand.nextGaussian() * MAX_SIGMA;
	}

	// A static factory that creates a sampler of this type
	public static final IRealConjugateSamplerFactory factory = new IRealConjugateSamplerFactory()
	{
		@Override
		public IRealConjugateSampler create() {return new NormalSampler();}
		
		@Override
		public boolean isCompatible(FactorFunction factorFunction)
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
