/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

package com.analog.lyric.dimple.solvers.core.swedish;

import java.util.ArrayList;
import java.util.Random;

import com.analog.lyric.dimple.FactorFunctions.core.SwedishFactorFunction;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorBase;

public class SwedishFactor extends SFactorBase 
{

	private int _numSamples;
	private int _maxNumTries;
	
	private Random _random;

	private SwedishDistributionGenerator [] _distGenerator;
	private SwedishFactorFunction _factorFunction;
	private SwedishSampler [] _samplers;
	
	public SwedishFactor(Factor factor, Random random)  
	{
		super(factor);
		
		_numSamples = 1;
		_maxNumTries = Integer.MAX_VALUE;
		
		if (! (factor.getFactorFunction() instanceof SwedishFactorFunction))
			throw new DimpleException("only support general factors that have been provided SwedishFactorFunctions");
		
		_factorFunction = (SwedishFactorFunction)factor.getFactorFunction();
		
		_factorFunction.attachRandom(random);
		
		_distGenerator = new SwedishDistributionGenerator[factor.getPorts().size()];
		_samplers = new SwedishSampler[factor.getPorts().size()];
		
		for (int i = 0; i < factor.getPorts().size(); i++)
		{
			_samplers[i] = _factorFunction.generateSampler(factor.getPorts().get(i));
			_distGenerator[i] = _factorFunction.generateDistributionGenerator(factor.getPorts().get(i));
		}
		
		_random = random;
	}
	
	public void setMaxNumTries(int numTries)
	{
		_maxNumTries = numTries;
	}

	public int getMaxNumTries()
	{
		return _maxNumTries;
	}
	
	public void setNumSamples(int numSamples)
	{
		_numSamples = numSamples;
	}

	public int getNumSamples()
	{
		return _numSamples;
	}
	
	@Override
	public void updateEdge(int outPortNum)  
	{
		
		Object [] inputs = null;
		ArrayList<Object> samples = new ArrayList<Object>();
		
		int numTries = 0;
		
		//For some number of samples
		for (int i = 0 ; i < _numSamples; i++)
		{
			boolean sampleAccepted = false;
			
			//Until a sample is accepted
			while (!sampleAccepted)
			{
				inputs = new Object [_factor.getPorts().size()-1];
				
				int index = 0;
				//For each input message (mean/variance)
				for (int j = 0; j < _factor.getPorts().size(); j++)
				{					
					//Generate a new sample using the specified mean/variance
					if (outPortNum != j)
					{
						
						inputs[index] = _samplers[j].generateSample();
						index++;
					}
					
				}
				//Call the first user-method for the given output edge, which returns a value H (the function that returns Hz(x,y)/max(Hz) in the example above)
				double H = _factorFunction.acceptanceRatio(outPortNum,inputs);
				
				//Choose a random number U from 0 to 1
				//If U < H, then accept the new set of input edge sample values, and break
				//Otherwise, continue
				if (_random.nextDouble() < H)
				{
					sampleAccepted = true;
				}
								
				if (numTries == _maxNumTries)
					throw new DimpleException("Failed to get desired number of samples");
				
				numTries++;

			}
			
			
			
			//Call the second user-method for the given output edge using the accepted input values are arguments, which returns a sample Z (the function that returns Z ~ p(z|x,y) in the example above)
			Object sample = _factorFunction.generateSample(outPortNum,inputs);
			
			//Add the sample Z to a list of output sample values
			samples.add(sample);
		}
		
		
		//For all output sample values
		//Calculate the sample mean
		//Calculate the sample variance
		//Set the output message to these values
		_distGenerator[outPortNum].generateDistributionInPlace(samples);
		
	}
	
	@Override
	public void initialize() 
	{
		for (SwedishSampler s : _samplers)
			s.initialize();
		for (SwedishDistributionGenerator s : _distGenerator)
			s.initialize();
	}
	
}
