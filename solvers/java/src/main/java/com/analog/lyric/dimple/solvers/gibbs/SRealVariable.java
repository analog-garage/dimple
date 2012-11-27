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
import com.analog.lyric.dimple.solvers.core.SRealVariableBase;

public class SRealVariable extends SRealVariableBase implements ISolverVariableGibbs
{
	protected double _sampleValue;
	protected double _initialSampleValue = 0;
	protected FactorFunction _input;
	protected RealDomain _domain;
	protected ArrayList<Double> _sampleArray;
	protected double _bestSampleValue;
	protected double _beta = 1;
	protected double _proposalStdDev = 1;
	protected boolean _holdSampleValue = false;


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
		return _initialSampleValue;
	}


	public void updateEdge(int outPortNum)
	{
		throw new DimpleException("Method not supported in Gibbs sampling solver.");
	}

	public void update()
	{
		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;

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
				LPrevious = _input.evalEnergy(new Object[]{_sampleValue});
				LProposed = _input.evalEnergy(new Object[]{proposalValue});
			}
			for (int portIndex = 0; portIndex < numPorts; portIndex++)
			{
				INode factorNode = _var.getPorts().get(portIndex).getConnectedNode();
				int factorPortNumber = factorNode.getPortNum(_var);
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
				setCurrentSample(proposalValue);
		}
	}
	
	public void randomRestart()
	{
		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;

		// TODO -- sample from the prior if specified, not just the bounds
		double hi = _domain.getUpperBound();
		double lo = _domain.getLowerBound();
		if (hi < Double.POSITIVE_INFINITY && lo > Double.NEGATIVE_INFINITY)
			setCurrentSample(GibbsSolverRandomGenerator.rand.nextDouble() * (hi - lo) + lo);
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
	
	public final double getScore()
	{
		if (_guessWasSet)
			return _input.evalEnergy(_guessValue);
		else
			throw new DimpleException("This solver doesn't provide a default value. Must set guesses for all variables.");
	}

	public final void saveAllSamples()
	{
		_sampleArray = new ArrayList<Double>();
	}

	public final void saveCurrentSample()
	{
		if (_sampleArray != null)
			_sampleArray.add(_sampleValue);
	}

	public final void saveBestSample()
	{
		_bestSampleValue = _sampleValue;
	}

	public final double getPotential()
	{
		if (_input != null)
			return _input.evalEnergy(new Object[]{_sampleValue});
		else
			return 0;
	}

	public final void setCurrentSample(double value)
	{
		_sampleValue = value;
		
		ArrayList<Port> ports = _var.getPorts();
		int numPorts = ports.size();
		for (int port = 0; port < numPorts; port++) 
			ports.get(port).setOutputMsg((Double)_sampleValue);
	}

	public final double getCurrentSample()
	{
		return _sampleValue;
	}

	public final double getBestSample()
	{
		return _bestSampleValue;
	}
	
	public final double[] getAllSamples()
	{
		int length = _sampleArray.size();
		double[] retval = new double[length];
		for (int i = 0; i < length; i++)
			retval[i] = _sampleArray.get(i);
		return retval;
	}
	
	public final void setAndHoldSampleValue(Double value)
	{
		setCurrentSample(value);
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


	public final void setProposalStandardDeviation(double stdDev) {_proposalStdDev = stdDev;}
	public final double getProposalStandardDeviation() {return _proposalStdDev;}

	public final void setInitialSampleValue(double initialSampleValue) {_initialSampleValue = initialSampleValue;}
	public final double getInitialSampleValue() {return _initialSampleValue;}


	public final void setBeta(double beta)	// beta = 1/temperature
	{
		_beta = beta;
	}

	public void initialize()
	{
		if (!_holdSampleValue)
			setCurrentSample(_initialSampleValue);
		_bestSampleValue = _initialSampleValue;
		if (_sampleArray != null) _sampleArray.clear();
	}

}
