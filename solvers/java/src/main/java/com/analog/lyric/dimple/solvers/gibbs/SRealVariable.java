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

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SVariableBase;

public class SRealVariable extends SVariableBase implements ISolverVariableGibbs
{
	protected double _sampleValue;
	protected double _initialSampleValue = 0;
	protected FactorFunction _input;
	protected RealDomain _domain;
	protected ArrayList<Double> _sampleArray;
	protected double _bestSampleValue;
	protected double _beta = 1;
	protected double _proposalStdDev = 1;


	public SRealVariable(VariableBase var)  
	{
		super(var);

		if (!(var.getDomain() instanceof RealDomain))
			throw new DimpleException("expected real domain");

		_domain = (RealDomain)var.getDomain();
		initialize();
	}

	public Object getDefaultMessage(Port port)
	{
		return 0d;
	}


	public void updateEdge(int outPortNum)
	{
		// TODO: This should throw the exception, but that would propagate to
		// the base class and all other derived classes, this is the quick and dirty way.
		new DimpleException("Method not supported in Gibbs sampling solver.").printStackTrace();
	}

	public void update()
	{
		int numPorts = _var.getPorts().size();
		double _lowerBound = _domain.getLowerBound();
		double _upperBound = _domain.getUpperBound();

		double proposalDelta = _proposalStdDev * GibbsSolverRandomGenerator.rand.nextGaussian();
		double proposalValue = _sampleValue + proposalDelta;

		// If outside the bounds, then reject
		if ((proposalValue >= _lowerBound) && (proposalValue <= _upperBound))
		{

			double LPrevious = 0;
			double LProposed = 0;

			// Sum up the potentials from the input and all connected factors
			if (_input != null)
			{
				try
				{
					LPrevious = -Math.log(_input.eval(new Object[]{_sampleValue}));
					LProposed = -Math.log(_input.eval(new Object[]{proposalValue}));
				}
				catch (Exception e) {e.printStackTrace(); System.exit(1);}
			}
			for (int portIndex = 0; portIndex < numPorts; portIndex++)
			{
				INode factorNode = _var.getPorts().get(portIndex).getConnectedNode();
				int factorPortNumber = 0;
				try {factorPortNumber = factorNode.getPortNum(_var);} catch (Exception e) {e.printStackTrace(); System.exit(1);}
				ISolverRealFactorGibbs factor = (ISolverRealFactorGibbs)(_var.getPorts().get(portIndex).getConnectedNode().getSolver());
				LPrevious += factor.getConditionalPotential(_sampleValue, factorPortNumber);
				LProposed += factor.getConditionalPotential(proposalValue, factorPortNumber);
			}

			// Temper
			LPrevious *= _beta;
			LProposed *= _beta;

			// Accept or reject
			double rejectionThreshold = Math.exp(LPrevious - LProposed);	// Note, no Hastings factor if Gaussian proposal distribution
			if (GibbsSolverRandomGenerator.rand.nextDouble() < rejectionThreshold)
				_sampleValue = proposalValue;
		}

		for (int d = 0; d < numPorts; d++) 
			_var.getPorts().get(d).setOutputMsg((Double)_sampleValue);
	}

	public void updateBelief()
	{
		// TODO -- not clear if it's practical to compute beliefs for real variables, or if so, how they should be represented
	}

	public Object getBelief() 
	{
		// TODO -- not clear if it's practical to compute beliefs for real variables, or if so, how they should be represented
		return 0d;
	}

	public void setInput(Object input) 
	{
		_input = (FactorFunction)input;
	}

	/*
	public void setDomain(RealDomain domain)
	{
		_domain = domain;
	}
	 */

	public void saveAllSamples()
	{
		_sampleArray = new ArrayList<Double>();
	}

	public void saveCurrentSample()
	{
		if (_sampleArray != null)
			_sampleArray.add(_sampleValue);
	}

	public void saveBestSample()
	{
		_bestSampleValue = _sampleValue;
	}

	public double getPotential()
	{
		if (_input != null)
		{
			try {return -Math.log(_input.eval(new Object[]{_sampleValue}));}
			catch (Exception e) {e.printStackTrace(); return 0;}
		}
		else
			return 0;
	}


	public double[] AllSamples() {return getAllSamples();}
	public double[] getAllSamples()
	{
		int length = _sampleArray.size();
		double[] retval = new double[length];
		for (int i = 0; i < length; i++)
			retval[i] = _sampleArray.get(i);
		return retval;
	}

	public double Sample() {return getCurrentSample();}
	public double getCurrentSample()
	{
		return _sampleValue;
	}

	public double BestSample() {return getBestSample();}
	public double getBestSample()
	{
		return _bestSampleValue;
	}

	public void setProposalStandardDeviation(double stdDev) {_proposalStdDev = stdDev;}
	public double getProposalStandardDeviation() {return _proposalStdDev;}

	public void setInitialSampleValue(double initialSampleValue) {_initialSampleValue = initialSampleValue;}
	public double getInitialSampleValue() {return _initialSampleValue;}


	public void setBeta(double beta)	// beta = 1/temperature
	{
		_beta = beta;
	}

	public void initialize()
	{
		_sampleValue = _initialSampleValue;
		_bestSampleValue = _initialSampleValue;
		if (_sampleArray != null) _sampleArray.clear();
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
