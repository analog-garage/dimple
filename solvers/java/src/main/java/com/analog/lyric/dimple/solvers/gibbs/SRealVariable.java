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
import java.util.Set;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SRealVariableBase;
import com.analog.lyric.dimple.solvers.core.SolverRandomGenerator;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.IRealConjugateFactor;
import com.analog.lyric.dimple.solvers.gibbs.sample.RealSample;
import com.analog.lyric.dimple.solvers.gibbs.samplers.IRealSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.RealConjugateSamplerRegistry;
import com.analog.lyric.dimple.solvers.gibbs.samplers.mcmc.IRealMCMCSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.mcmc.ISampleScorer;
import com.analog.lyric.dimple.solvers.gibbs.samplers.mcmc.MHSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.mcmc.RealMCMCSamplerRegistry;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;


/**** WARNING: Whenever editing this class, also make the corresponding edit to SRealJointVariable.
 * The two are nearly identical, but unfortunately couldn't easily be shared due to the class hierarchy
 *
 */

public class SRealVariable extends SRealVariableBase implements ISolverVariableGibbs, ISampleScorer
{
	public static final String DEFAULT_REAL_SAMPLER_NAME = "SliceSampler";
	
	private Real _varReal;
	private RealSample _outputMsg;
	private double _sampleValue = 0;
	private double _initialSampleValue = 0;
	private FactorFunction _input;
	private RealDomain _domain;
	private String _defaultSamplerName = DEFAULT_REAL_SAMPLER_NAME;
	private IRealMCMCSampler _sampler = null;
	private IRealConjugateSampler _conjugateSampler = null;
	private boolean _samplerSpecificallySpecified = false;
	private ArrayList<Double> _sampleArray;
	private double _bestSampleValue;
	private double _beta = 1;
	private boolean _holdSampleValue = false;
	private boolean _isDeterministicDepdentent = false;
	private boolean _hasDeterministicDependents = false;


	// Primary constructor
	public SRealVariable(VariableBase var)  
	{
		super(var);

		if (!(var.getDomain() instanceof RealDomain))
			throw new DimpleException("expected real domain");

		_varReal = (Real)_var;
		_domain = (RealDomain)var.getDomain();
	}

	// Alternative constructor for creating from a joint domain
	public SRealVariable(VariableBase var, Real realVar, RealDomain domain)  
	{
		super(var);

		_varReal = realVar;
		_domain = domain;
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

		// Get the next sample value from the sampler
		double nextSampleValue;
		if (_conjugateSampler == null)
		{
			// Use MCMC sampler
			nextSampleValue = _sampler.nextSample(this);
		}
		else
		{
			// Use conjugate sampler, first update the messages from all factors
			// Factor messages represent the current distribution parameters from each factor
			ArrayList<INode> siblings = _var.getSiblings();
			int numPorts = siblings.size();
			Port[] ports = new Port[numPorts];
			for (int portIndex = 0; portIndex < numPorts; portIndex++)
			{
				INode factorNode = siblings.get(portIndex);
				ISolverNode factor = factorNode.getSolver();
				int factorPortNumber = factorNode.getPortNum(_var);
				ports[portIndex] = factorNode.getPorts().get(factorPortNumber);
				((SRealFactor)factor).updateEdgeMessage(factorPortNumber);	// Run updateEdgeMessage for each neighboring factor
			}
			nextSampleValue = _conjugateSampler.nextSample(ports, _input);
		}
		if (nextSampleValue != _sampleValue)	// Would be exactly equal if not changed since last value tested
			setCurrentSample(nextSampleValue);
	}
	
	
	// ISampleScorer methods...
	// The following methods are for the ISampleScorer interface, meant to be called by a sampler
	// These are not intended for other purposes
	@Override
	public final double getSampleScore(double sampleValue)
	{
		// WARNING: Side effect is that the current sample value changes to this sample value
		// Could change back but less efficient to do this, since we'll be updating the sample value anyway
		setCurrentSample(sampleValue);

		return getCurrentSampleScore();
	}
	@Override
	public final double getCurrentSampleScore()
	{
		if (!_domain.inDomain(_sampleValue))
			return Double.POSITIVE_INFINITY;		// Outside the domain
			
		double potential = 0;

		// Sum up the potentials from the input and all connected factors
		if (_input != null)
			potential = _input.evalEnergy(new Object[]{_sampleValue});
		ArrayList<INode> siblings = _var.getSiblings();
		int numPorts = siblings.size();
		for (int portIndex = 0; portIndex < numPorts; portIndex++)
		{
			INode factorNode = siblings.get(portIndex);
			ISolverFactorGibbs factor = (ISolverFactorGibbs)(factorNode.getSolver());
			int factorPortNumber = factorNode.getPortNum(_var);
			potential += factor.getConditionalPotential(factorPortNumber);
		}
		
		return potential * _beta;	// Incorporate current temperature
	}
	@Override
	public final double getCurrentSampleValue()
	{
		return _sampleValue;
	}

	
	// For conjugate samplers
	public final IRealConjugateSampler getConjugateSampler()
	{
		return _conjugateSampler;
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

		// If there are inputs, see if there's an available conjugate sampler
		IRealConjugateSampler inputConjugateSampler = null;		// Don't use the global conjugate sampler since other factors might not be conjugate
		if (_input != null)
			inputConjugateSampler = RealConjugateSamplerRegistry.findCompatibleSampler(_input);

		// Determine if there are bounds
		double hi = _domain.getUpperBound();
		double lo = _domain.getLowerBound();

		if (inputConjugateSampler != null)
		{
			// Sample from the input if there's an available sampler
			double sampleValue = inputConjugateSampler.nextSample(new Port[0], _input);
			
			// If there are also bounds, clip at the bounds
			if (sampleValue > hi) sampleValue = hi;
			if (sampleValue < lo) sampleValue = lo;
			setCurrentSample(sampleValue);
		}
		else
		{
			// No input or no available sampler, so if bounded, sample uniformly from the bounds
			if (hi < Double.POSITIVE_INFINITY && lo > Double.NEGATIVE_INFINITY)
				setCurrentSample(SolverRandomGenerator.rand.nextDouble() * (hi - lo) + lo);
		}
	}

	public void updateBelief()
	{
		// TODO -- not clear if it's practical to compute beliefs for real variables, or if so, how they should be represented
	}

	public Object getBelief() 
	{
		return 0d;
	}

	@Override
	public void setInputOrFixedValue(Object input,Object fixedValue, boolean hasFixedValue) 
	{
		if (input == null)
			_input = null;
		else
			_input = (FactorFunction)input;

		if (hasFixedValue)
			setCurrentSample((Double)fixedValue);
	}

	
	@Override 
	public void updateDirectedCache()
	{
		_hasDeterministicDependents = hasDeterministicDependents();
		_isDeterministicDepdentent = isDeterministicDependent();
	}
	
	@Override
	public void postAddFactor(Factor f)
	{
		// Update the direction information
		updateDirectedCache();
		
		// Set the fixed value is there is one
		if (_var.hasFixedValue())
		{
			setCurrentSample((Double)_var.getFixedValueObject());
		}
		else
		{
			setCurrentSample(_sampleValue);
		}
		
		// Get the default sampler
		_defaultSamplerName = ((SFactorGraph)_var.getRootGraph().getSolver()).getDefaultRealSampler();
	}

	
	@Override
	public final double getScore()
	{
		if (_var.hasFixedValue())
			return 0;
		else if (_input == null)
			return 0;
		else if (_guessWasSet)
			return _input.evalEnergy(_guessValue);
		else
			return _input.evalEnergy(_sampleValue);
	}
	
	@Override
	public Object getGuess()
	{
		if (_guessWasSet)
			return Double.valueOf(_guessValue);
		else if (_var.hasFixedValue())
			return Double.valueOf(_varReal.getFixedValue());
		else
			return Double.valueOf(_sampleValue);
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
		_outputMsg.setValue(_sampleValue);
		
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
		if (_sampleArray == null)
			throw new DimpleException("No samples saved. Must call saveAllSamples on variable or entire graph prior to solving");
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




	// FIXME: REMOVE
	// There should be a way to call these directly via the samplers
	// If so, they should be removed from here since this makes this sampler-specific
	public final void setProposalStandardDeviation(double stdDev)
	{
		if (_sampler instanceof MHSampler)
			((MHSampler)_sampler).getProposalKernel().setParameters(stdDev);
	}
	public final double getProposalStandardDeviation()
	{
		if (_sampler instanceof MHSampler)
			return (Double)((MHSampler)_sampler).getProposalKernel().getParameters()[0];
		else
			return 0;
	}
	// Set the proposal kernel parameters more generally
	public final void setProposalKernelParameters(Object... parameters)
	{
		if (_sampler instanceof MHSampler)
			((MHSampler)_sampler).getProposalKernel().setParameters(parameters);
	}
	
	// FIXME: REMOVE
	// There should be a way to call these directly via the samplers
	// If so, they should be removed from here since this makes this sampler-specific
	public final void setProposalKernel(IProposalKernel proposalKernel)					// IProposalKernel object
	{
		if (_sampler instanceof MHSampler)
			((MHSampler)_sampler).setProposalKernel(proposalKernel);
	}
	public final void setProposalKernel(String proposalKernelName)						// Name of proposal kernel
	{
		if (_sampler instanceof MHSampler)
			((MHSampler)_sampler).setProposalKernel(proposalKernelName);
	}
	public final IProposalKernel getProposalKernel()
	{
		if (_sampler instanceof MHSampler)
			return ((MHSampler)_sampler).getProposalKernel();
		else
			return null;
	}
	
	// Set/get the sampler to be used for this variable
	public final void setDefaultSampler(String samplerName)
	{
		_defaultSamplerName = samplerName;
	}
	public final String getDefaultSamplerName()
	{
		return _defaultSamplerName;
	}
	public final void setSampler(IRealMCMCSampler sampler)
	{
		_sampler = sampler;
		_samplerSpecificallySpecified = true;
	}
	public final void setSampler(String samplerName)
	{
		_sampler = RealMCMCSamplerRegistry.get(samplerName);
		_samplerSpecificallySpecified = true;
	}
	public final IRealSampler getSampler()
	{
		if (_samplerSpecificallySpecified)
			return _sampler;
		else
		{
			initialize();	// To determine the appropriate sampler
			if (_conjugateSampler == null)
				return _sampler;
			else
				return _conjugateSampler;
		}
	}
	public final String getSamplerName()
	{
		IRealSampler sampler = getSampler();
		if (sampler != null)
			return sampler.getClass().getSimpleName();
		else
			return "";
	}

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



	public RealSample createDefaultMessage() 
	{
		if (_var.hasFixedValue())
			return new RealSample(_varReal.getFixedValue());
		else
			return new RealSample(_initialSampleValue);
	}

	@Override
	public Object resetInputMessage(Object message) 
	{
		((RealSample)message).setObject(_var.hasFixedValue() ? _varReal.getFixedValue() : _initialSampleValue);
		return message;
	}

	@Override
	public void resetEdgeMessages(int portNum) 
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
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum) 
	{
		
	}
	

	public void initialize()
	{
		super.initialize();

		// Unless this is a dependent of a deterministic factor, then set the starting sample value
		if (!_isDeterministicDepdentent)
		{
			double initialSampleValue = _var.hasFixedValue() ? _varReal.getFixedValue() : _initialSampleValue;
			if (!_holdSampleValue)
				setCurrentSample(initialSampleValue);
		}
		
		// Clear out sample state
		_bestSampleValue = _sampleValue;
		if (_sampleArray != null) _sampleArray.clear();
		
		// Determine which sampler to use
		if (_samplerSpecificallySpecified)
			_conjugateSampler = null;		// A sampler was specified and already created, use that one (don't use a conjugate sampler)s
		else
		{
			_conjugateSampler = findConjugateSampler();		// See if there's an available conjugate sampler, and if so, use it
			if (_conjugateSampler == null)
				_sampler = RealMCMCSamplerRegistry.get(_defaultSamplerName);	// If not, use the default sampler
		}
	}

	@Override
	public Object[] createMessages(ISolverFactor factor) 
	{
		return new Object[] {null,_outputMsg};
	}
	
	public void createNonEdgeSpecificState()
	{
		_outputMsg = createDefaultMessage();
		_sampleValue = _outputMsg.getValue();
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
		_defaultSamplerName = ovar._defaultSamplerName;
		_sampler = ovar._sampler;
		_conjugateSampler = ovar._conjugateSampler;
		_samplerSpecificallySpecified = ovar._samplerSpecificallySpecified;
		_holdSampleValue = ovar._holdSampleValue;
    }
	
	
	public IRealConjugateSampler findConjugateSampler()
	{
		// Check all the adjacent factors to see if they all support a common cojugate factor
		ArrayList<INode> siblings = _var.getSiblings();
		int numPorts = siblings.size();
		ArrayList<IRealConjugateSamplerFactory> commonSamplers = new ArrayList<IRealConjugateSamplerFactory>();
		for (int portIndex = 0; portIndex < numPorts; portIndex++)
		{
			INode factorNode = siblings.get(portIndex);
			ISolverNode factor = factorNode.getSolver();
			if (!(factor instanceof IRealConjugateFactor))
				return null;	// At least one connected factor does not support conjugate sampling
			int factorPortNumber = factorNode.getPortNum(_var);
			Set<IRealConjugateSamplerFactory> availableSamplers = ((IRealConjugateFactor)factor).getAvailableRealConjugateSamplers(factorPortNumber);
			if (commonSamplers.isEmpty())  // First time through
				commonSamplers.addAll(availableSamplers);
			else
			{
				// Remove any samplers not supported by this factor
				ArrayList<IRealConjugateSamplerFactory> unavailableSamplers = new ArrayList<IRealConjugateSamplerFactory>();
				for (IRealConjugateSamplerFactory sampler : commonSamplers)
					if (!availableSamplers.contains(sampler))
						unavailableSamplers.add(sampler);
				commonSamplers.removeAll(unavailableSamplers);
			}
			if (commonSamplers.isEmpty())
				return null;	// No common samplers found
		}
		
		// Next, check that this conjugate sampler is also compatible with the input and the domain of this variable
		for (IRealConjugateSamplerFactory sampler : commonSamplers)
			if (sampler.isCompatible(_input) && sampler.isCompatible(_domain))
				return sampler.create();	// Create and return the sampler
		
		// Input wasn't compatible with any of the samplers supported by adjacent factors
		return null;
	}

}
