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
	protected ArrayList<Integer> _sampleIndexArray;
	protected int _bestSampleIndex;
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
		_input = (double[])getDefaultMessage(null);
	}

	public Object getDefaultMessage(Port port)
	{
		int domainLength = _varDiscrete.getDiscreteDomain().getElements().length;
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
		updateCache();

		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		int messageLength = _input.length;
		double minEnergy = Double.POSITIVE_INFINITY;

		double[] conditionalProbability = new double[messageLength];

		// Compute the conditional probability (initially in energy representation before converting to probability)
		for (int index = 0; index < messageLength; index++)
		{
			double out = _input[index];						// Sum of the input prior...
			for (int port = 0; port < _numPorts; port++)
				out += _inPortMsgs[port][index];			// Plus each input message value
			
			if (out < minEnergy) minEnergy = out;			// For normalization

			conditionalProbability[index] = out;			// Initially in energy representation before converting to probability
		}

		// Convert to probability representation
		for (int index = 0; index < messageLength; index++)
		{
			double temperedValue = (conditionalProbability[index] - minEnergy) * _beta;
			double out = Math.exp(-temperedValue);
			conditionalProbability[index] = out;
		}

		// Sample from the conditional distribution
		setCurrentSampleIndex(Utilities.sampleFromMultinomial(conditionalProbability, GibbsSolverRandomGenerator.rand));
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
		double[] probPriors = new double[messageLength];
		for (int i = 0; i < messageLength; i++)
			probPriors[i] = Math.exp(-(_input[i] - minEnergy));
		
		setCurrentSampleIndex(Utilities.sampleFromMultinomial(probPriors, GibbsSolverRandomGenerator.rand));
	}

	public void updateBelief()
	{
		_beliefHistogram[_sampleIndex]++;
	}

	public Object getBelief() 
	{
		updateCache();
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
		int domainLength = domain.size();
		int valueIndex = -1;
		for (int i = 0; i < domainLength; i++)
		{
			if (domain.getElements()[i].equals(value))
			{
				valueIndex = i;
				break;
			}
		}
		if (valueIndex == -1)
			throw new DimpleException("Value is not in the domain of this variable");
		
		setCurrentSampleIndex(valueIndex);
	}
	public final void setCurrentSampleIndex(int index)
    {
		updateCache();

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
		//values.  We can't do the same thing as the tableFunction (update the cache here)
		//because the function init gets called after variable init.  If we updated the cache
		//here, the table function init would replace the arrays for the outgoing message
		//and our update functions would update stale messages.
		//System.out.println("Variable init");
		_initCalled = true;

		_bestSampleIndex = -1;
		int messageLength = _varDiscrete.getDiscreteDomain().getElements().length;
		for (int i = 0; i < messageLength; i++) 
			_beliefHistogram[i] = 0;
	}

	private void updateCache()
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
		}
	}
	
}
