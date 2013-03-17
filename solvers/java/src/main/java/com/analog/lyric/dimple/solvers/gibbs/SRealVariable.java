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
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SRealVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class SRealVariable extends SRealVariableBase implements ISolverVariableGibbs
{
	protected Real _varReal;
	protected ObjectSample _outputMsg;
	protected double _sampleValue;
	protected double _initialSampleValue = 0;
	protected FactorFunction _input;
	protected RealDomain _domain;
	protected ArrayList<Double> _sampleArray;
	protected double _bestSampleValue;
	protected double _beta = 1;
	protected double _proposalStdDev = 1;
	protected boolean _holdSampleValue = false;
	protected boolean _isDeterministicDepdentent = false;
	protected boolean _hasDeterministicDependents = false;



	public SRealVariable(VariableBase var)  
	{
		super(var);

		if (!(var.getDomain() instanceof RealDomain))
			throw new DimpleException("expected real domain");

		_varReal = (Real)_var;
		_domain = (RealDomain)var.getDomain();
	}


	public void updateEdge(int outPortNum)
	{
		throw new DimpleException("Method not supported in Gibbs sampling solver.");
	}

	public void update()
	{
		// Don't bother to re-sample deterministic dependent variables (those that are the output of a directional deterministic factor)
		if (_isDeterministicDepdentent) return;

		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		// Also return if the variable is set to a fixed value
		if (_var.hasFixedValue()) return;

		
		int numPorts = _var.getSiblings().size();
		double _lowerBound = _domain.getLowerBound();
		double _upperBound = _domain.getUpperBound();

		double proposalDelta = _proposalStdDev * GibbsSolverRandomGenerator.rand.nextGaussian();
		double proposalValue = _sampleValue + proposalDelta;
		double previousSampleValue = _sampleValue;

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
			// Get the potential over all the factors given the previous sample value
			for (int portIndex = 0; portIndex < numPorts; portIndex++)
			{
				INode factorNode = _var.getSiblings().get(portIndex);
				ISolverFactorGibbs factor = (ISolverFactorGibbs)(factorNode.getSolver());
				int factorPortNumber = factorNode.getPortNum(_var);
				LPrevious += factor.getConditionalPotential(factorPortNumber);
			}
			// Get the potential over all the factors given the proposed sample value
			setCurrentSample(proposalValue);
			for (int portIndex = 0; portIndex < numPorts; portIndex++)
			{
				INode factorNode = _var.getSiblings().get(portIndex);
				ISolverFactorGibbs factor = (ISolverFactorGibbs)(factorNode.getSolver());
				int factorPortNumber = factorNode.getPortNum(_var);
				LProposed += factor.getConditionalPotential(factorPortNumber);
			}


			// Temper
			LPrevious *= _beta;
			LProposed *= _beta;

			// Accept or reject
			double rejectionThreshold = Math.exp(LPrevious - LProposed);	// Note, no Hastings factor if Gaussian proposal distribution
			if (GibbsSolverRandomGenerator.rand.nextDouble() < rejectionThreshold)
				setCurrentSample(proposalValue);		// Accept
			else
				setCurrentSample(previousSampleValue);	// Reject
		}
	}

	public void randomRestart()
	{
		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;

		// If the variable has a fixed value, then set the current sample to that value and return
		if (_var.hasFixedValue())
		{
			setCurrentSample(_varReal.getFixedValue());
			return;
		}

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
		if (input == null)
			_input = null;
		else
			_input = (FactorFunction)input;
	}
	
	public final double getScore()
	{
		if (_var.hasFixedValue())
			return 0;
		else if (_guessWasSet)
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
	
	public double getConditionalPotential(int portIndex)
	{
		double result = getPotential();		// Start with the local potential
		
		// Propagate the request through the other neighboring factors and sum up the results
		ArrayList<INode> siblings = _var.getSiblings();
		int numPorts = siblings.size();
		for (int port = 0; port < numPorts; port++)	// Plus each input message value
			if (port != portIndex)
				result += ((ISolverFactorGibbs)siblings.get(port).getSolver()).getConditionalPotential(_var.getSiblingPortIndex(port));

		    return result;
	}

	public final double getPotential()
	{
		if (_var.hasFixedValue())
			return 0;
		else if (_input == null)
			return 0;
		else
			return _input.evalEnergy(new Object[]{_sampleValue});
	}

    public final void setCurrentSample(Object value) {setCurrentSample(FactorFunctionUtilities.toDouble(value));}
	public final void setCurrentSample(double value)
	{
		_sampleValue = value;
		_outputMsg.value = _sampleValue;
		
		// If this variable has deterministic dependents, then set their values
		if (_hasDeterministicDependents)
		{
			ArrayList<INode> siblings = _var.getSiblings();
			int numPorts = siblings.size();
			for (int port = 0; port < numPorts; port++)	// Plus each input message value
			{
				Factor f = (Factor)siblings.get(port);
				if (f.getFactorFunction().isDeterministicDirected() && !f.isDirectedTo(_var))
					((ISolverFactorGibbs)f.getSolver()).updateNeighborVariableValue(_var.getSiblingPortIndex(port));
			}
		}
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

	public final void setAndHoldSampleValue(double value)
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
	
	// Determine whether or not this variable is a deterministic dependent variable; that is, one that corresponds
	// to the output of a directed deterministic factor
	public boolean isDeterministicDependent()
	{
		for (INode f : _var.getSiblings())
		{
			Factor factor = (Factor)f;
			if (factor.getFactorFunction().isDeterministicDirected() && factor.isDirectedTo(_var))
				return true;
		}
		return false;
	}
	
	// Determine whether or not this variable has variables that are deterministic dependents of this variable
	public boolean hasDeterministicDependents()
	{
		for (INode f : _var.getSiblings())
		{
			Factor factor = (Factor)f;
			if (factor.getFactorFunction().isDeterministicDirected() && !factor.isDirectedTo(_var))
				return true;
		}
		return false;
	}



	public ObjectSample createDefaultMessage() 
	{
		return new ObjectSample(_initialSampleValue);
	}

	@Override
	public Object resetInputMessage(Object message) 
	{
		((ObjectSample)message).value = _initialSampleValue;
		return message;
	}

	@Override
	public void initializeEdge(int portNum) 
	{
	}

	@Override
	public Object getInputMsg(int portIndex) 
	{
		throw new DimpleException("Not supported by: " + this);	

	}

	@Override
	public Object getOutputMsg(int portIndex) 
	{
		return _outputMsg;	
	}

	@Override
	public void setInputMsg(int portIndex, Object obj) 
	{
				
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum) 
	{
		//SDiscreteVariable ovar = ((SDiscreteVariable)other);
		//_inPortMsgs[thisPortNum] = ovar._inPortMsgs[otherPortNum];
			
		
	}
	

	public void initialize()
	{
		super.initialize();

		if (!_holdSampleValue)
			setCurrentSample(_initialSampleValue);
		_bestSampleValue = _initialSampleValue;
		if (_sampleArray != null) _sampleArray.clear();
		
		_isDeterministicDepdentent = isDeterministicDependent();
		_hasDeterministicDependents = hasDeterministicDependents();
	}

	@Override
	public Object [] createMessages(ISolverFactor factor) 
	{
		return new Object [] {null,_outputMsg};
	}
	
	public void createNonEdgeSpecificState()
	{
		_outputMsg = createDefaultMessage();
	}
	
	@Override
    public void moveNonEdgeSpecificState(ISolverNode other)
    {
		SRealVariable ovar = ((SRealVariable)other);
		_outputMsg = ovar._outputMsg;
		_sampleValue = ovar._sampleValue;
		_initialSampleValue = ovar._initialSampleValue;
		_sampleArray = ovar._sampleArray;
		_bestSampleValue = ovar._bestSampleValue;
		_beta = ovar._beta;
		_proposalStdDev = ovar._proposalStdDev;
		_holdSampleValue = ovar._holdSampleValue;
    }

}
