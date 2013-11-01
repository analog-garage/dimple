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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Dirichlet;
import com.analog.lyric.dimple.factorfunctions.ExchangeableDirichlet;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.solvers.core.SolverRandomGenerator;


public class DirichletSampler implements IRealJointConjugateSampler
{
	@Override
	public double[] nextSample(Port[] ports, FactorFunction input)
	{
		int numPorts = ports.length;
		int dimension = 0;
		if (numPorts > 0)
			dimension = ((double[])(ports[0].getOutputMsg())).length;
		else if (input != null)
			if (input instanceof Dirichlet)
				dimension = ((Dirichlet)input).getDimension();
			else // ExchangeableDirichlet
				dimension = ((ExchangeableDirichlet)input).getDimension();
		else
			throw new DimpleException("Both port and input arguments are empty");
		
		double[] alpha = new double[dimension];
		double[] sample = new double[dimension];
		
		if (input != null)
		{
			double[] inputParameters;
			if (input instanceof Dirichlet)
				inputParameters = ((Dirichlet)input).getParameters();
			else // ExchangeableDirichlet
				inputParameters = ((ExchangeableDirichlet)input).getParameters();
			if (inputParameters.length != dimension)
				throw new DimpleException("All inputs to Dirichlet sampler must have the same number of dimensions");
			for (int i = 0; i < dimension; i++)
				alpha[i] += inputParameters[i];
		}
		
		for (int port = 0; port < numPorts; port++)
		{
			// The message from each neighboring factor is an array with elements (alpha, beta)
			double[] message = (double[])(ports[port].getOutputMsg());
			if (message.length != dimension)
				throw new DimpleException("All inputs to Dirichlet sampler must have the same number of dimensions");
			for (int i = 0; i < dimension; i++)
				alpha[i] += message[i];
		}
		
		// Sample from a series of Gamma distributions, then normalize to sum to 1
		double sum = 0;
		for (int i = 0; i < dimension; i++)
			sample[i] = SolverRandomGenerator.randGamma.nextDouble(alpha[i], 1);
		for (int i = 0; i < dimension; i++)
			sum += sample[i];
		for (int i = 0; i < dimension; i++)
			sample[i] /= sum;
		return sample;
	}


	// A static factory that creates a sampler of this type
	public static final IRealJointConjugateSamplerFactory factory = new IRealJointConjugateSamplerFactory()
	{
		@Override
		public IRealJointConjugateSampler create() {return new DirichletSampler();}
		
		@Override
		public boolean isCompatible(FactorFunction factorFunction)
		{
			if (factorFunction == null)
				return true;
			else if (factorFunction instanceof Dirichlet)
				return true;
			else if (factorFunction instanceof ExchangeableDirichlet)
				return true;
			else
				return false;
		}
		
		@Override
		public boolean isCompatible(RealJointDomain domain)
		{
			for (RealDomain d : domain.getRealDomains())
			{
				if (d.getLowerBound() > 0 || d.getUpperBound() < 1)
					return false;
			}
			return true;
		}
	};
}
