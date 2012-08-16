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

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SVariableBase;
import com.analog.lyric.dimple.solvers.core.Utilities;



public class SDiscreteVariable extends SVariableBase implements ISolverVariableGibbs
{
	protected long[] _beliefHistogram;
	protected int _sampleIndex;
	protected double [] _priors;
	protected ArrayList<Integer> _sampleIndexArray;
	protected int _bestSampleIndex;
	protected double _beta = 1;
	protected Discrete _varDiscrete;

	public SDiscreteVariable(VariableBase var) 
	{
		super(var);
		_varDiscrete = (Discrete)_var;
		_beliefHistogram = new long[((Discrete)var).getDiscreteDomain().getElements().length];
		initialize();
		_priors = (double[]) getDefaultMessage(null);
	}

	public Object getDefaultMessage(Port port)
	{
		int domainLength = _varDiscrete.getDiscreteDomain().getElements().length;
		double [] retVal = new double[domainLength];
		double val = 1.0/domainLength;
		for (int i = 0; i < domainLength; i++)
			retVal[i] = val;
		return retVal;
	}


	public void updateEdge(int outPortNum)
	{
		// TODO: This should throw the exception, but that would propagate to
		// the base class and all other derived classes, this is the quick and dirty way.
		new DimpleException("Method not supported in Gibbs sampling solver.").printStackTrace();
	}

	public void update()
	{
		final double minLog = -100;
		double[] priors = (double[])_priors;
		int M = priors.length;
		int D = _var.getPorts().size();
		double maxLog = Double.NEGATIVE_INFINITY;

		double[][] inPortMsgs = new double[D][];
		for (int d = 0; d < D; d++) 
			inPortMsgs[d] = (double[])_var.getPorts().get(d).getInputMsg();
		
		double[] conditionalProbability = new double[M];

		for (int m = 0; m < M; m++)
		{
			double prior = priors[m];
			double out = (prior == 0) ? minLog : Math.log(prior);

			for (int d = 0; d < D; d++)
			{
				double tmp = inPortMsgs[d][m];
				out += (tmp == 0) ? minLog : Math.log(tmp);
			}
			if (out > maxLog) maxLog = out;
			conditionalProbability[m] = out;
		}


		for (int m = 0; m < M; m++)
		{
			double temperedValue = (conditionalProbability[m] - maxLog) * _beta;
			double out = Math.exp(temperedValue);
			conditionalProbability[m] = out;
		}

		_sampleIndex = Utilities.sampleFromMultinomial(conditionalProbability,GibbsSolverRandomGenerator.rand);

		for (int d = 0; d < D; d++) 
			_var.getPorts().get(d).setOutputMsg(_sampleIndex);
	}

	public void updateBelief()
	{
		_beliefHistogram[_sampleIndex]++;
	}

	public Object getBelief() 
	{
		double[] outBelief = new double[_varDiscrete.getDiscreteDomain().getElements().length];
		long sum = 0;
		for (int i = 0; i < _varDiscrete.getDiscreteDomain().getElements().length; i++) sum+= _beliefHistogram[i];
		if (sum != 0)
			for (int i = 0; i < _varDiscrete.getDiscreteDomain().getElements().length; i++) outBelief[i] = (double)_beliefHistogram[i]/(double)sum;
		else
			for (int i = 0; i < _varDiscrete.getDiscreteDomain().getElements().length; i++) outBelief[i] = ((double[])_priors)[i];		// Disconnected variable that has never been updated

		return outBelief;
	}

	public void setInput(Object priors) 
	{
		double[] vals = (double[])priors;
		if (vals.length != _varDiscrete.getDiscreteDomain().getElements().length)
		{
			throw new DimpleException("Prior size must match domain length");
		}
		_priors = vals;
	}
	
    public void saveAllSamples()
    {
    	_sampleIndexArray = new ArrayList<Integer>();
    }
    
    public void saveCurrentSample()
    {
    	if (_sampleIndexArray != null)
    		_sampleIndexArray.add(_sampleIndex);
    }
    
    public void saveBestSample()
    {
    	_bestSampleIndex = _sampleIndex;
    }
    
	public double getPotential()
	{
		return -Math.log(_priors[_sampleIndex]);
	}

    public Object[] AllSamples() {return getAllSamples();}
    public Object[] getAllSamples()
    {
    	int length = _sampleIndexArray.size();
    	Object[] domain = _varDiscrete.getDiscreteDomain().getElements();
    	Object[] retval = new Object[length];
    	for (int i = 0; i < length; i++)
    		retval[i] = domain[_sampleIndexArray.get(i)];
    	return retval;
    }
    public int[] AllSampleIndices() {return getAllSampleIndices();}
    public int[] getAllSampleIndices()
    {
    	int length = _sampleIndexArray.size();
    	int[] retval = new int[length];
    	for (int i = 0; i < length; i++)
    		retval[i] = _sampleIndexArray.get(i);
    	return retval;
    }

    public Object Sample() {return getCurrentSample();}
    public Object getCurrentSample()
    {
    	return _varDiscrete.getDiscreteDomain().getElements()[_sampleIndex];
    }
    public int SampleIndex() {return getCurrentSampleIndex();}
    public int getCurrentSampleIndex()
    {
    	return _sampleIndex;
    }
    
    public Object BestSample() {return getBestSample();}
    public Object getBestSample()
    {
    	return _varDiscrete.getDiscreteDomain().getElements()[_bestSampleIndex];
    }
    public int BestSampleIndex() {return getBestSampleIndex();}
    public int getBestSampleIndex()
    {
    	return _bestSampleIndex;
    }
    
    public void setBeta(double beta)	// beta = 1/temperature
    {
    	_beta = beta;
    }
	
    
	public void initialize()
	{
		_bestSampleIndex = -1;
		for (int i = 0; i < _varDiscrete.getDiscreteDomain().getElements().length; i++) 
			_beliefHistogram[i] = 0;
	}
	
	public double getEnergy()  
	{
		throw new DimpleException("getEnergy not yet supported for gibbs");
	}
	
	public Object getGuess() 
	{
		throw new DimpleException("get and set guess not supported for this solver");
	}
	
	public void setGuess(Object guess) 
	{
		throw new DimpleException("get and set guess not supported for this solver");
	}


}
