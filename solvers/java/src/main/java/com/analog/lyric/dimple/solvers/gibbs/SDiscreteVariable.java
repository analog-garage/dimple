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

package com.analog.lyric.dimple.solvers.gibbs;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math.random.RandomGenerator;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableBase;
import com.analog.lyric.dimple.solvers.core.Utilities;



public class SDiscreteVariable extends SDiscreteVariableBase implements ISolverVariableGibbs
{
    protected double[][] _inPortMsgs = null;
    protected int[][] _outPortMsgs = null;
    protected int _numPorts;
	protected long[] _beliefHistogram;
	protected int _sampleIndex;
	protected double[] _input;
	protected double[] _conditional;
	protected double[] _samplerScratch;
	protected ArrayList<Integer> _sampleIndexArray;
	protected int _bestSampleIndex;
	protected int _lengthRoundedUp;
	protected double _beta = 1;
	protected Discrete _varDiscrete;
	protected boolean _initCalled = true;
	protected boolean _holdSampleValue = false;

	public SDiscreteVariable(VariableBase var) 
	{
		super(var);
		_varDiscrete = (Discrete)_var;
		_beliefHistogram = new long[((Discrete)var).getDiscreteDomain().getElements().length];
		initialize();
		initializeInputs();
	}

	public void initializeInputs()
	{
		_input = (double[])getDefaultMessage(null);

	}
	
	public Object getDefaultMessage(Port port)
	{
		DiscreteDomain dd = ((Discrete)_var).getDiscreteDomain();
		Object [] elements = dd.getElements();
		int domainLength = elements.length;
		double[] retVal = new double[domainLength];
		Arrays.fill(retVal, 0);
		return retVal;
	}


	public void updateEdge(int outPortNum)
	{
		throw new DimpleException("Method not supported in Gibbs sampling solver.");
	}

	public void update()
	{
		ensureCacheUpdated();

		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		int messageLength = _input.length;
		double minEnergy = Double.POSITIVE_INFINITY;

		// Compute the conditional probability
		for (int index = 0; index < messageLength; index++)
		{
			double out = _input[index];						// Sum of the input prior...
			for (int port = 0; port < _numPorts; port++)
				out += _inPortMsgs[port][index];			// Plus each input message value
			out *= _beta;									// Apply tempering
			
			if (out < minEnergy) minEnergy = out;			// For normalization

			_conditional[index] = out;						// Save in log domain representation
		}

		// Sample from the conditional distribution
		setCurrentSampleIndex(generateSample(_conditional, minEnergy));
	}
	
	public void randomRestart()
	{
		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;

		// Convert the prior back to probabilities to sample from the prior
		int messageLength = _input.length;
		double minEnergy = Double.POSITIVE_INFINITY;
		for (int i = 0; i < messageLength; i++)
			if (_input[i] < minEnergy)
				minEnergy = _input[i];
		
	    _lengthRoundedUp = Utilities.nextPow2(_input.length);
	    _samplerScratch = new double[_lengthRoundedUp];
		setCurrentSampleIndex(generateSample(_input, minEnergy));
	}

	public void updateBelief()
	{
		_beliefHistogram[_sampleIndex]++;
	}

	public Object getBelief() 
	{
		ensureCacheUpdated();
		int domainLength = _input.length;
		double[] outBelief = new double[domainLength];
		long sum = 0;
		for (int i = 0; i < domainLength; i++)
			sum+= _beliefHistogram[i];
		if (sum != 0)
		{
			for (int i = 0; i < domainLength; i++)
				outBelief[i] = (double)_beliefHistogram[i]/(double)sum;
		}
		else
		{
			for (int i = 0; i < domainLength; i++)
				outBelief[i] = ((double[])_input)[i];		// Disconnected variable that has never been updated
		}
		
		return outBelief;
	}

	public void setInput(Object priors)
	{
		double[] vals = (double[])priors;
		if (vals.length != _varDiscrete.getDiscreteDomain().getElements().length)
			throw new DimpleException("Prior size must match domain length");
		
		// Convert to energy values
		_input = new double[vals.length];
		for (int i = 0; i < vals.length; i++)
			_input[i] = -Math.log(vals[i]);
	}
	
    public final void saveAllSamples()
    {
    	_sampleIndexArray = new ArrayList<Integer>();
    }
    
    public final void saveCurrentSample()
    {
    	if (_sampleIndexArray != null)
    		_sampleIndexArray.add(_sampleIndex);
    }
    
    public final void saveBestSample()
    {
    	_bestSampleIndex = _sampleIndex;
    }
    
	public final double getPotential()
	{
		return _input[_sampleIndex];
	}
	
	public final double getScore()
	{
		return _input[getGuessIndex()];
	}
	
	public final void setCurrentSample(Object value)
	{
		DiscreteDomain domain = (DiscreteDomain)_var.getDomain();
		int valueIndex = domain.getIndex(value);
		if (valueIndex == -1)
			throw new DimpleException("Value is not in the domain of this variable");
		
		setCurrentSampleIndex(valueIndex);
	}
	public final void setCurrentSampleIndex(int index)
    {
		ensureCacheUpdated();

		// Sample from the conditional distribution
		_sampleIndex = index;

		// Send the sample value to all output ports
		int numPorts = _outPortMsgs.length;
		for (int port = 0; port < numPorts; port++) 
			_outPortMsgs[port][0] = _sampleIndex;
    }
    
    public final Object getCurrentSample()
    {
    	return _varDiscrete.getDiscreteDomain().getElements()[_sampleIndex];
    }
    public final int getCurrentSampleIndex()
    {
    	return _sampleIndex;
    }
    
    public final Object getBestSample()
    {
    	return _varDiscrete.getDiscreteDomain().getElements()[_bestSampleIndex];
    }
    public final int getBestSampleIndex()
    {
    	return _bestSampleIndex;
    }

    public final Object[] getAllSamples()
    {
    	int length = _sampleIndexArray.size();
    	Object[] domain = _varDiscrete.getDiscreteDomain().getElements();
    	Object[] retval = new Object[length];
    	for (int i = 0; i < length; i++)
    		retval[i] = domain[_sampleIndexArray.get(i)];
    	return retval;
    }
    public final int[] getAllSampleIndices()
    {
    	int length = _sampleIndexArray.size();
    	int[] retval = new int[length];
    	for (int i = 0; i < length; i++)
    		retval[i] = _sampleIndexArray.get(i);
    	return retval;
    }

	public final void setAndHoldSampleValue(Object value)
	{
		setCurrentSample(value);
		holdSampleValue();
	}
	
	public final void setAndHoldSampleIndex(int index)
	{
		setCurrentSampleIndex(index);
		holdSampleValue();
	}
	
	public final void holdSampleValue()
	{
		_holdSampleValue = true;
	}
	
	public final void releaseSampleValue()
	{
		_holdSampleValue = false;
	}
	
    
    public final void setBeta(double beta)	// beta = 1/temperature
    {
    	_beta = beta;
    }
	
    
	public void initialize()
	{
		super.initialize();
		
		//Flag that init was called so that we can update the cache next time we need cached
		//values.  We can't do the same thing as the STableFactor (update the cache here)
		//because the function init gets called after variable init.  If we updated the cache
		//here, the table function init would replace the arrays for the outgoing message
		//and our update functions would update stale messages.
		_initCalled = true;

		_bestSampleIndex = -1;
		int messageLength = _varDiscrete.getDiscreteDomain().getElements().length;
		for (int i = 0; i < messageLength; i++) 
			_beliefHistogram[i] = 0;
	}

	protected void updateMessageCache()
	{
		if (_initCalled)
		{
			_initCalled = false;
	    	ArrayList<Port> ports = _var.getPorts();
	    	_numPorts= ports.size();
		    _inPortMsgs = new double[_numPorts][];
		    _outPortMsgs = new int[_numPorts][];
		    
		    for (int port = 0; port < _numPorts; port++)
		    {
		    	_inPortMsgs[port] = (double[])ports.get(port).getInputMsg();
		    	_outPortMsgs[port] = (int[])ports.get(port).getOutputMsg();
		    }
		    
		    _lengthRoundedUp = Utilities.nextPow2(_input.length);
		    _samplerScratch = new double[_lengthRoundedUp];
		    _conditional = new double[_input.length];
		}
	}
	
	private final int generateSample(double[] energy, double minEnergy)
	{
		RandomGenerator rand = GibbsSolverRandomGenerator.rand;
		int length = energy.length;
		int sampleIndex;

		// Special-case lengths 2, 3, and 4 for speed
		if (length == 2)
		{
			sampleIndex = (rand.nextDouble() * (1 + Math.exp(energy[1]-energy[0])) > 1) ? 0 : 1;
		}
		else if (length == 3)
		{
			double cumulative1 = Math.exp(minEnergy-energy[0]);
			double cumulative2 = cumulative1 + Math.exp(minEnergy-energy[1]);
			double sum = cumulative2 + Math.exp(minEnergy-energy[2]);
			double randomValue = sum * rand.nextDouble();
			sampleIndex = (randomValue > cumulative2) ? 2 : (randomValue > cumulative1) ? 1 : 0;
		}
		else if (length == 4)
		{
			double cumulative1 = Math.exp(minEnergy-energy[0]);
			double cumulative2 = cumulative1 + Math.exp(minEnergy-energy[1]);
			double cumulative3 = cumulative2 + Math.exp(minEnergy-energy[2]);
			double sum = cumulative3 + Math.exp(minEnergy-energy[3]);
			double randomValue = sum * rand.nextDouble();
			sampleIndex = (randomValue > cumulative2) ? ((randomValue > cumulative3) ? 3 : 2) : ((randomValue > cumulative1) ? 1 : 0);
		}
		else	// For all other lengths
		{
			// Calculate cumulative conditional probability (unnormalized)
			double sum = 0;
			double[] samplerScratch = _samplerScratch;
			samplerScratch[0] = 0;
			for (int m = 1; m < length; m++)
			{
				sum += expApprox(minEnergy-energy[m-1]);
				samplerScratch[m] = sum;
			}
			sum += expApprox(minEnergy-energy[length-1]);
			for (int m = length; m < _lengthRoundedUp; m++)
				samplerScratch[m] = Double.POSITIVE_INFINITY;

			int half = _lengthRoundedUp >> 1;
			while (true)
			{
				// Sample from the distribution using a binary search.
				double randomValue = sum * rand.nextDouble();
				sampleIndex = 0;
				for (int bitValue = half; bitValue > 0; bitValue >>= 1)
				{
					int testIndex = sampleIndex | bitValue;
					if (randomValue > samplerScratch[testIndex]) sampleIndex = testIndex;
				}

				// Rejection sampling, since the approximation of the exponential function is so coarse
				double logp = minEnergy-energy[sampleIndex];
				if (rand.nextDouble()*expApprox(logp) <= Math.exp(logp)) break;
			}
		}

		return sampleIndex;
	}

	// This is an approximation to the exponential function; inputs must be non-positive
	// To facilitate subsequent rejection sampling, the error versus the correct exponential function needs to be always positive
	// This is true except for very large negative inputs, for values just as the output approaches zero
	// To ensure rejection is never in an infinite loop, this must reach 0 for large negative inputs before the Math.exp function does
	private final double expApprox(double value)
	{
		// Convert input to base2 log, then convert integer part into IEEE754 exponent
		final long expValue = ((long)(1512775.395195186 * value) + 0x3FF00000) << 32;	// 1512775.395195186 = 2^20/log(2)
		return Double.longBitsToDouble(expValue & ~(expValue >> 63));	// Clip result if negative and convert to a double
	}
	
}
