/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core.hybridSampledBP;

import java.util.ArrayList;
import java.util.Random;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.HybridSampledBPFactorFunction;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class HybridSampledBPFactor extends SFactorBase
{
	protected Random _random;

	private int _numSamples;
	private int _maxNumTries;
	
	private HybridSampledBPDistributionGenerator [] _distGenerator;
	private HybridSampledBPFactorFunction _factorFunction;
	private HybridSampledBPSampler [] _samplers;
	
	
	public abstract HybridSampledBPSampler generateSampler(Port p);
	public abstract HybridSampledBPDistributionGenerator generateDistributionGenerator(Port p);

	
	public HybridSampledBPFactor(Factor factor, Random random)
	{
		super(factor);
		
		_random = random;
		_numSamples = 1;
		_maxNumTries = Integer.MAX_VALUE;
		
		if (! (factor.getFactorFunction() instanceof HybridSampledBPFactorFunction))
			throw new DimpleException("only support general factors that have been provided HybridSampledBPFactorFunctions");
		
		_factorFunction = (HybridSampledBPFactorFunction)factor.getFactorFunction();
		
		_factorFunction.attachRandom(random);
		
		int nSiblings = factor.getSiblingCount();
		_distGenerator = new HybridSampledBPDistributionGenerator[nSiblings];
		_samplers = new HybridSampledBPSampler[nSiblings];
		
		for (int i = 0; i < nSiblings; i++)
		{
			_samplers[i] = generateSampler(new Port(factor,i));
			_distGenerator[i] = generateDistributionGenerator(new Port(factor,i));
		}
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
				int nSiblings = _factor.getSiblingCount();
				inputs = new Object [nSiblings-1];
				
				int index = 0;
				//For each input message (mean/variance)
				for (int j = 0; j < nSiblings; j++)
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
	public void resetEdgeMessages(int i)
	{
		_samplers[i].initialize();
		_distGenerator[i].initialize();
	}
	
	@Override
	public void createMessages()
	{
		final Factor factor = _factor;
		for (int i = 0, nVars = factor.getSiblingCount(); i < nVars; i++)
		{
		
			ISolverVariable var = factor.getSibling(i).getSolver();
			Object [] messages = var.createMessages(this);
			_samplers[i].createMessage(messages[1]);
			_distGenerator[i].createMessage(messages[0]);
		
		}
	}


	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPortNum)
	{
		HybridSampledBPFactor s = (HybridSampledBPFactor)other;
		_samplers[portNum].moveMessages(s._samplers[otherPortNum]);
		_distGenerator[portNum].moveMessages(s._distGenerator[otherPortNum]);
	}

	@Override
	public Object getInputMsg(int portIndex)
	{
		return _samplers[portIndex].getInputMsg();
	}

	@Override
	public Object getOutputMsg(int portIndex) {
		return _distGenerator[portIndex].getOutputMsg();
	}
}
