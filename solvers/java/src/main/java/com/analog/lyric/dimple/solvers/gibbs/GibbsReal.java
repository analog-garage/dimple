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

import static com.analog.lyric.dimple.solvers.gibbs.GibbsSolverVariableEvent.*;
import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.DoubleArrayList;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SRealVariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.IRealConjugateFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.RealConjugateSamplerRegistry;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IGenericSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IMCMCSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IRealSamplerClient;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.MHSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.util.misc.Internal;
import com.google.common.primitives.Doubles;

/*
 * WARNING: Whenever editing this class, also make the corresponding edit to SRealJointVariable.
 * The two are nearly identical, but unfortunately couldn't easily be shared due to the class hierarchy
 *
 */

/**
 * Real-valued solver variable for Gibbs solver.
 * @since 0.07
 */
public class GibbsReal extends SRealVariableBase
	implements ISolverVariableGibbs, ISolverRealVariableGibbs, IRealSamplerClient
{
	/*-----------
	 * Constants
	 */
	
	/**
	 * Bits in {@link #_flags} reserved by this class and its superclasses.
	 */
	@SuppressWarnings("hiding")
	protected final static int RESERVED_FLAGS = 0xFFFF0003;

	@SuppressWarnings("hiding")
	protected static final int EVENT_MASK = 0x03;

	/*-------
	 * State
	 */
	
	public static final String DEFAULT_REAL_SAMPLER_NAME = "SliceSampler";
	
	private Real _varReal;
	private RealValue _outputMsg;
	private @Nullable Object[] _inputMsg = null;
	private double _sampleValue = 0;
	private double _initialSampleValue = 0;
	private boolean _initialSampleValueSet = false;
	private @Nullable FactorFunction _input;
	private RealDomain _domain;
	private @Nullable IMCMCSampler _sampler = null;
	private @Nullable IRealConjugateSampler _conjugateSampler = null;
	private boolean _samplerSpecificallySpecified = false;
	private @Nullable DoubleArrayList _sampleArray;
	private double _sampleSum;
	private double _sampleSumSquare;
	private long _sampleCount;
	private double _bestSampleValue;
	private double _beta = 1;
	private boolean _holdSampleValue = false;
	private boolean _visited = false;
	private long _updateCount;
	private long _rejectCount;
	private long _scoreCount;

	/**
	 * List of neighbors for sample scoring. Instantiated during initialization.
	 */
	private @Nullable GibbsNeighbors _neighbors = null;

	/*--------------
	 * Construction
	 */
	
	// Primary constructor
	public GibbsReal(Variable var)
	{
		super(var);

		if (!(var.getDomain() instanceof RealDomain))
			throw new DimpleException("expected real domain");

		_varReal = (Real)_var;
		_domain = (RealDomain)var.getDomain();
		_outputMsg = createDefaultMessage();
	}

	// Alternative constructor for creating from a joint domain
	public GibbsReal(Variable var, Real realVar, RealDomain domain)
	{
		super(var);

		_varReal = realVar;
		_domain = domain;
		_outputMsg = createDefaultMessage();
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	protected void doUpdateEdge(int outPortNum)
	{
		throw new DimpleException("Method not supported in Gibbs sampling solver.");
	}

	@Override
	public final void update()
	{
		// Don't bother to re-sample deterministic dependent variables (those that are the output of a directional deterministic factor)
		if (getModelObject().isDeterministicOutput()) return;

		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		// Also return if the variable is set to a fixed value
		if (_var.hasFixedValue()) return;

		final int updateEventFlags = GibbsSolverVariableEvent.getVariableUpdateEventFlags(this);
		Value oldValue = null;
		double oldSampleScore = 0.0;

		switch (updateEventFlags)
		{
		case UPDATE_EVENT_SCORED:
			// TODO: non-conjugate samplers already compute sample scores, so we shouldn't have to do here.
			oldSampleScore = getCurrentSampleScore();
			//$FALL-THROUGH$
		case UPDATE_EVENT_SIMPLE:
			oldValue = RealValue.create(_sampleValue);
			break;
		}

		// Get the next sample value from the sampler
		boolean rejected = false;
		_updateCount++;
		final IRealConjugateSampler conjugateSampler = _conjugateSampler;
		if (conjugateSampler == null)
		{
			// Use MCMC sampler
			RealValue nextSample = RealValue.create(_sampleValue);
			rejected = !Objects.requireNonNull(_sampler).nextSample(nextSample, this);
			if (rejected) _rejectCount++;
		}
		else
		{
			// Use conjugate sampler, first update the messages from all factors
			// Factor messages represent the current distribution parameters from each factor
			int numPorts = _var.getSiblingCount();
			Port[] ports = new Port[numPorts];
			for (int portIndex = 0; portIndex < numPorts; portIndex++)
			{
				Factor factorNode = requireNonNull(_var.getSibling(portIndex));
				ISolverFactor factor = requireNonNull(factorNode.getSolver());
				int factorPortNumber = factorNode.getPortNum(_var);
				ports[portIndex] = factorNode.getPort(factorPortNumber);
				((ISolverFactorGibbs)factor).updateEdgeMessage(factorPortNumber);	// Run updateEdgeMessage for each neighboring factor
			}
			double nextSampleValue = conjugateSampler.nextSample(ports, _input);
			if (nextSampleValue != _sampleValue)	// Would be exactly equal if not changed since last value tested
				setCurrentSample(nextSampleValue);
		}
		
		switch (updateEventFlags)
		{
		case UPDATE_EVENT_SCORED:
			// TODO: non-conjugate samplers already compute sample scores, so we shouldn't have to do here.
			raiseEvent(new GibbsScoredVariableUpdateEvent(this, Objects.requireNonNull(oldValue), oldSampleScore,
				RealValue.create(_sampleValue), getCurrentSampleScore(), rejected ? 1 : 0));
			break;
		case UPDATE_EVENT_SIMPLE:
			raiseEvent(new GibbsVariableUpdateEvent(this, Objects.requireNonNull(oldValue),
				RealValue.create(_sampleValue), rejected ? 1 : 0));
			break;
		}
	}
	
	/*---------------------------
	 * SolverEventSource methods
	 */
	
	@Override
	protected int getEventMask()
	{
		return EVENT_MASK | super.getEventMask();
	}
	
	/*-------------------------
	 * ISolverVariable methods
	 */
	
	// IRealSampleScorer methods...
	// The following methods are for the IRealSampleScorer interface, meant to be called by a sampler
	// These are not intended for other purposes
	@Override
	public final double getSampleScore(Value sampleValue)
	{
		return getSampleScore(sampleValue.getDouble());
	}
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
		double sampleScore = Double.POSITIVE_INFINITY;
		_scoreCount++;
		
		computeScore:
		{
			if (!_domain.inDomain(_sampleValue))
				break computeScore; // outside the domain

			double potential = 0;

			// Sum up the potentials from the input and all connected factors
			final FactorFunction input = _input;
			if (input != null)
			{
				potential = input.evalEnergy(new Object[]{_sampleValue});
				if (!Doubles.isFinite(potential))
				{
					break computeScore;
				}
			}

			ReleasableIterator<ISolverNodeGibbs> scoreNodes = getSampleScoreNodes();
			while (scoreNodes.hasNext())
			{
				final ISolverNodeGibbs node = scoreNodes.next();
				potential += node.getPotential();
				if (!Doubles.isFinite(potential))
				{
					break computeScore;
				}
			}
			scoreNodes.release();

			sampleScore = potential * _beta;	// Incorporate current temperature
		}
		
		return sampleScore;
	}
	
	@Override
	public final void setNextSampleValue(Value sampleValue)
	{
		setNextSampleValue(sampleValue.getDouble());
	}
	@Override
	public final void setNextSampleValue(double sampleValue)
	{
		if (sampleValue != _sampleValue)
			setCurrentSample(sampleValue);
	}

	// TODO move to local methods?
	// For conjugate samplers
	public final @Nullable IRealConjugateSampler getConjugateSampler()
	{
		return _conjugateSampler;
	}
	
	/*----------------------------------
	 * ISolverRealVariableGibbs methods
	 * TODO: move below ISolverVariableGibbs methods
	 */
	
	@Override
	public final void getAggregateMessages(IParameterizedMessage outputMessage, int outPortNum, ISampler conjugateSampler)
	{
		int numPorts = _var.getSiblingCount();
		Port[] ports = new Port[numPorts - 1];
		for (int port = 0, i = 0; port < numPorts; port++)
		{
			if (port != outPortNum)
			{
				Factor factorNode = requireNonNull(_var.getSibling(port));
				ISolverFactor factor = requireNonNull(factorNode.getSolver());
				int factorPortNumber = factorNode.getPortNum(_var);
				ports[i++] = factorNode.getPorts().get(factorPortNumber);
				((ISolverFactorGibbs)factor).updateEdgeMessage(factorPortNumber);	// Run updateEdgeMessage for each neighboring factor
			}
		}
		((IRealConjugateSampler)conjugateSampler).aggregateParameters(outputMessage, ports, _input);
	}
	
	/*--------------------------
	 * ISolverNodeGibbs methods
	 */
	
	@Override
	public boolean setVisited(boolean visited)
	{
		boolean changed = _visited ^ visited;
		_visited = visited;
		return changed;
	}

	/*------------------------------
	 * ISolverVariableGibbs methods
	 */
	
	@Override
	public RealValue getCurrentSampleValue()
	{
		return _outputMsg;
	}
	
	@Override
	public ReleasableIterator<ISolverNodeGibbs> getSampleScoreNodes()
	{
		return GibbsNeighbors.iteratorFor(_neighbors, this);
	}

	@Override
	public void randomRestart(int restartCount)
	{
		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		// If the variable is the output of a directed deterministic factor, then don't modify the value--it should already be set correctly
		if (getModelObject().isDeterministicOutput()) return;

		// If the variable has a fixed value, then set the current sample to that value and return
		if (_var.hasFixedValue())
		{
			setCurrentSample(_varReal.getFixedValue());
			return;
		}
		if (_initialSampleValueSet && restartCount == 0)
		{
			setCurrentSample(_initialSampleValue);
			return;
		}


		// If there are inputs, see if there's an available conjugate sampler
		IRealConjugateSampler inputConjugateSampler = null;		// Don't use the global conjugate sampler since other factors might not be conjugate
		final FactorFunction input = _input;
		if (input != null)
			inputConjugateSampler = RealConjugateSamplerRegistry.findCompatibleSampler(input);

		// Determine if there are bounds
		double hi = _domain.getUpperBound();
		double lo = _domain.getLowerBound();

		if (inputConjugateSampler != null)
		{
			// Sample from the input if there's an available sampler
			double sampleValue = inputConjugateSampler.nextSample(new Port[0], input);
			
			// If there are also bounds, clip at the bounds
			if (sampleValue > hi) sampleValue = hi;
			if (sampleValue < lo) sampleValue = lo;
			setCurrentSample(sampleValue);
		}
		else
		{
			// No input or no available sampler, so if bounded, sample uniformly from the bounds
			if (hi < Double.POSITIVE_INFINITY && lo > Double.NEGATIVE_INFINITY)
				setCurrentSample(DimpleRandomGenerator.rand.nextDouble() * (hi - lo) + lo);
			else if (hi < _sampleValue)
				setCurrentSample(hi);
			else if (lo > _sampleValue)
				setCurrentSample(lo);
		}
	}

	@Override
	public final void updateBelief()
	{
		// Update the sums for computing moments
		final double currentSampleValue = _sampleValue;
		_sampleSum += currentSampleValue;
		_sampleSumSquare += currentSampleValue * currentSampleValue;
		_sampleCount++;
	}

	@Override
	public Object getBelief()
	{
		return 0d;
	}
	
	public final double getSampleMean()
	{
		return _sampleSum / _sampleCount;
	}
	
	public final double getSampleVariance()
	{
		return (_sampleSumSquare - (_sampleSum * (_sampleSum / _sampleCount)) ) / (_sampleCount - 1);
	}

	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue, boolean hasFixedValue)
	{
		if (input == null)
			_input = null;
		else
			_input = (FactorFunction)input;

		if (hasFixedValue)
			setCurrentSampleForce(FactorFunctionUtilities.toDouble(fixedValue));
	}

	@SuppressWarnings("null")
	@Override
	public void postAddFactor(@Nullable Factor f)
	{
	}

	@Override
	public final double getScore()
	{
		if (_var.hasFixedValue())
			return 0;
		
		final FactorFunction input = _input;
		if (input == null)
			return 0;
		else if (_guessWasSet)
			return input.evalEnergy(_guessValue);
		else
			return input.evalEnergy(_sampleValue);
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

	/**
	 * @deprecated Instead set {@link GibbsOptions#saveAllSamples} to true using {@link #setOption}.
	 */
	@Deprecated
	@Override
	public final void saveAllSamples()
	{
		_sampleArray = new DoubleArrayList();
		setOption(GibbsOptions.saveAllSamples, true);
	}

	/**
	 * @deprecated Instead set {@link GibbsOptions#saveAllSamples} to false using {@link #setOption}.
	 */
	@Deprecated
    @Override
	public void disableSavingAllSamples()
    {
    	_sampleArray = null;
		setOption(GibbsOptions.saveAllSamples, false);
    }
    
	@Override
	public final void saveCurrentSample()
	{
		final DoubleArrayList sampleArray = _sampleArray;
		if (sampleArray != null)
			sampleArray.add(_sampleValue);
	}

	@Override
	public final void saveBestSample()
	{
		_bestSampleValue = _sampleValue;
	}
	
	// TODO move to ISolverNodeGibbs
	@Override
	public final double getPotential()
	{
		if (_var.hasFixedValue())
			return 0;
		
		final FactorFunction input = _input;
		if (input == null)
			return 0;
		else
			return input.evalEnergy(new Object[]{_sampleValue});
	}
	
	@Override
	public final boolean hasPotential()
	{
		return !_var.hasFixedValue() && _input != null;
	}

    @Override
	public final void setCurrentSample(Object value)
	{
		setCurrentSample(FactorFunctionUtilities.toDouble(value));
	}
    
    @Override
    public final void setCurrentSample(Value value)
    {
    	setCurrentSample(value.getDouble());
    }
    
    /*---------------
     * Local methods
     */
    
	public final void setCurrentSample(double value)
	{
		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		// Also return if the variable is set to a fixed value
		if (_var.hasFixedValue()) return;
		
		setCurrentSampleForce(value);
	}
	
	// Sets the sample regardless of whether the value is fixed or held
	private final void setCurrentSampleForce(double value)
	{
		final GibbsNeighbors neighbors = _neighbors;
		final boolean hasDeterministicDependents = neighbors != null && neighbors.hasDeterministicDependents();
		
		RealValue oldValue = null;
		if (hasDeterministicDependents)
		{
			oldValue = _outputMsg.clone();
		}
		
		_sampleValue = value;
		_outputMsg.setDouble(_sampleValue);
		
		// If this variable has deterministic dependents, then set their values
		if (hasDeterministicDependents)
		{
			requireNonNull(neighbors).update(requireNonNull(oldValue));
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

	@Override
	public final double[] getAllSamples()
	{
		final DoubleArrayList sampleArray = _sampleArray;
		if (sampleArray == null)
		{
			return ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		
		return Arrays.copyOf(sampleArray.elements(), sampleArray.size());
	}
	
	@Override
	public final double getRejectionRate()
	{
		return (_updateCount > 0) ? (double)_rejectCount / (double)_updateCount : 0;
	}
	
	@Override
	public final double getNumScoresPerUpdate()
	{
		return (_updateCount > 0) ? (double)_scoreCount / (double)_updateCount : 0;
	}
	
	@Override
	public final void resetRejectionRateStats()
	{
		_updateCount = 0;
		_rejectCount = 0;
		_scoreCount = 0;
	}
	
	@Override
	public final long getUpdateCount()
	{
		return _updateCount;
	}
	
	@Override
	public final long getRejectionCount()
	{
		return _rejectCount;
	}

	// This is meant for internal use, not as a user accessible method
	@Internal
	public final @Nullable DoubleArrayList _getSampleArrayUnsafe()
	{
		return _sampleArray;
	}

	public final void setAndHoldSampleValue(double value)
	{
		releaseSampleValue();
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
	/**
	 * @deprecated Will be removed in future release. Instead set corresponding option
	 * for desired proposal kernel (e.g. {@link NormalProposalKernel#standardDeviation}.
	 */
	@SuppressWarnings("null")
	@Deprecated
	public final void setProposalStandardDeviation(double stdDev)
	{
		if (_sampler instanceof MHSampler)
			((MHSampler)_sampler).getProposalKernel().setParameters(stdDev);
	}

	/**
	 * @deprecated Will be removed in future release. Instead lookup corresponding option
	 * for desired proposal kernel (e.g. {@link NormalProposalKernel#standardDeviation}.
	 */
	@SuppressWarnings("null")
	@Deprecated
	public final double getProposalStandardDeviation()
	{
		if (_sampler instanceof MHSampler)
			return (Double)((MHSampler)_sampler).getProposalKernel().getParameters()[0];
		else
			return 0;
	}
	
	/**
	 * @deprecated Will be removed in future release. Instead set appropriate options
	 * for proposal kernel using {@link #setOption}.
	 */
	@SuppressWarnings("null")
	@Deprecated
	public final void setProposalKernelParameters(Object... parameters)
	{
		if (_sampler instanceof MHSampler)
			((MHSampler)_sampler).getProposalKernel().setParameters(parameters);
	}
	
	// FIXME: REMOVE
	/**
	 * @deprecated Will be removed in future release. Instead set corresponding option
	 * for sampler (e.g. {@link MHSampler#realProposalKernel}).
	 */
	@SuppressWarnings("null")
	@Deprecated
	public final void setProposalKernel(IProposalKernel proposalKernel)					// IProposalKernel object
	{
		if (_sampler instanceof MHSampler)
			((MHSampler)_sampler).setProposalKernel(proposalKernel);
	}

	/**
	 * @deprecated Will be removed in future release. Instead lookup corresponding option
	 * for sampler (e.g. {@link MHSampler#realProposalKernel}).
	 */
	@SuppressWarnings("null")
	@Deprecated
	public final void setProposalKernel(String proposalKernelName)						// Name of proposal kernel
	{
		if (_sampler instanceof MHSampler)
			((MHSampler)_sampler).setProposalKernel(proposalKernelName);
	}
	
	/**
	 * @deprecated Will be removed in future release. Instead get kernel directly
	 * from {@linkplain #getSampler() sampler}.
	 */
	@SuppressWarnings("null")
	@Deprecated
	public final @Nullable IProposalKernel getProposalKernel()
	{
		if (_sampler instanceof MHSampler)
			return ((MHSampler)_sampler).getProposalKernel();
		else
			return null;
	}
	
	/**
	 * @deprecated Will be removed in future release. Instead use {@link GibbsOptions#realSampler}
	 * option.
	 */
	@Deprecated
	public final void setDefaultSampler(String samplerName)
	{
		GibbsOptions.realSampler.convertAndSet(this, samplerName);
	}
	
	/**
	 * @deprecated Will be removed in future release. Instead use {@link GibbsOptions#realSampler}
	 * option.
	 */
	@Deprecated
	public final String getDefaultSamplerName()
	{
		return getOptionOrDefault(GibbsOptions.realSampler).getSimpleName();
	}

	/**
	 * Sets sampler to be used for this variable.
	 * <p>
	 * In general, it is usually easier to configure the sampler using the
	 * {@link GibbsOptions#realSampler} option. This method should only be
	 * required when the sampler class is not registered with the
	 * {@linkplain DimpleEnvironment#genericSamplers() generic sampler registry}
	 * for the current environment.
	 * <p>
	 * @param sampler is a non-null sampler.
	 */
	public final void setSampler(ISampler sampler)
	{
		_sampler = (IMCMCSampler)sampler;
		_samplerSpecificallySpecified = true;
	}

	/**
	 * @deprecated Will be removed in future release. Instead set sampler by setting
	 * {@link GibbsOptions#realSampler} option using {@link #setOption}.
	 */
	@Deprecated
	public final void setSampler(String samplerName)
	{
		GibbsOptions.realSampler.convertAndSet(this, samplerName);
		IMCMCSampler sampler = (IMCMCSampler) GibbsOptions.realSampler.instantiateIfDifferent(this, _sampler);
		_sampler = sampler;
		_samplerSpecificallySpecified = true;
		sampler.initializeFromVariable(this);
	}
	
	@Override
	public final @Nullable ISampler getSampler()
	{
		if (_samplerSpecificallySpecified)
		{
			Objects.requireNonNull(_sampler).initializeFromVariable(this);
			return _sampler;
		}
		else
		{
			initialize();	// To determine the appropriate sampler
			if (_conjugateSampler == null)
			{
				Objects.requireNonNull(_sampler).initializeFromVariable(this);
				return _sampler;
			}
			else
			{
				return _conjugateSampler;
			}
		}
	}
	public final String getSamplerName()
	{
		ISampler sampler = getSampler();
		if (sampler != null)
			return sampler.getClass().getSimpleName();
		else
			return "";
	}

	public final void setInitialSampleValue(double initialSampleValue)
	{
		_initialSampleValue = initialSampleValue;
		_initialSampleValueSet = true;
	}

	public final double getInitialSampleValue()
	{
		return _initialSampleValue;
	}

	// TODO move to ISolverVariableGibbs
	@Override
	public final void setBeta(double beta)	// beta = 1/temperature
	{
		_beta = beta;
	}



	public RealValue createDefaultMessage()
	{
		return Value.create(_domain, _var.hasFixedValue() ? _varReal.getFixedValue() : _initialSampleValue);
	}

	// TODO Move to ISolverVariable
	@Override
	public Object resetInputMessage(Object message)
	{
		((RealValue)message).setObject(_var.hasFixedValue() ? _varReal.getFixedValue() : _initialSampleValue);
		return message;
	}

	// TODO move to ISolverNode
	@Override
	public void resetEdgeMessages(int portNum)
	{
	}

	// TODO move to ISolverNode
	@Override
	public @Nullable Object getInputMsg(int portIndex)
	{
		return _inputMsg;
	}

	// TODO move to ISolverNode
	@Override
	public @Nullable Object getOutputMsg(int portIndex)
	{
		return _outputMsg;
	}

	// TODO move to ISolverNode
	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		Object[] inputMsg = _inputMsg;
		if (inputMsg == null)
			inputMsg = _inputMsg = new Object[_var.getSiblingCount()];
		inputMsg[portIndex] = obj;
	}

	// TODO move to ISolverNode
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		
	}
	
	// TODO move to ISolverNode
	@Override
	public void initialize()
	{
		final boolean saveAllSamples = getOptionOrDefault(GibbsOptions.saveAllSamples);
		
		super.initialize();

		// We actually only need to change this if the model has changed in the vicinity of this variable,
		// but that may not be worth the trouble to figure out.
		_neighbors = GibbsNeighbors.create(this);
		
		// Unless this is a dependent of a deterministic factor, then set the starting sample value
		if (!getModelObject().isDeterministicOutput())
		{
			double initialSampleValue = _var.hasFixedValue() ? _varReal.getFixedValue() : _initialSampleValue;
			if (!_holdSampleValue)
				setCurrentSampleForce(initialSampleValue);
		}
		
		// Clear out sample state
		_bestSampleValue = _sampleValue;
		DoubleArrayList sampleArray = null;
		if (saveAllSamples)
		{
			sampleArray = _sampleArray;
			if (sampleArray == null)
			{
				sampleArray = new DoubleArrayList();
			}
			else
			{
				sampleArray.clear();
			}
		}
		_sampleArray = sampleArray;
		
		// Clear out the Belief statistics
		_sampleSum = 0;
		_sampleSumSquare = 0;
		_sampleCount = 0;

		//
		// Determine which sampler to use
		//
		
		_conjugateSampler = null;

		if (!_samplerSpecificallySpecified)
		{
			IOptionHolder[] source = new IOptionHolder[1];
			Class<? extends IGenericSampler> samplerClass = getOptionAndSource(GibbsOptions.realSampler, source);
			if (samplerClass == null || source[0] != this && source[0] != _var)
			{
				if (getOptionOrDefault(GibbsOptions.enableAutomaticConjugateSampling))
				{
					// See if there's an available conjugate sampler, and if so, use it
					_conjugateSampler = findConjugateSampler();
				}
			}
			
			if (_conjugateSampler == null)
			{
				if (samplerClass == null)
				{
					samplerClass = GibbsOptions.realSampler.defaultValue();
				}
				
				IMCMCSampler sampler = _sampler;
				if (sampler == null || sampler.getClass() != samplerClass)
				{
					try
					{
						_sampler = sampler = (IMCMCSampler)samplerClass.newInstance();
					}
					catch (InstantiationException | IllegalAccessException ex)
					{
						throw new RuntimeException(ex);
					}
				}

				sampler.initializeFromVariable(this);
				
				resetRejectionRateStats();
			}
		}
	}

	// TODO move to ISolverVariable
	@Override
	public Object[] createMessages(ISolverFactor factor)
	{
		return new Object[] {null,_outputMsg};
	}
	
	// TODO move to ISolverVariable
	@Override
	public void createNonEdgeSpecificState()
	{
		_outputMsg = createDefaultMessage();
		_sampleValue = _outputMsg.getDouble();
	    _bestSampleValue = _sampleValue;
	    if (_sampleArray != null)
			saveAllSamples();
	}
	
	// TODO move to ISolverVariable
	@Override
    public void moveNonEdgeSpecificState(ISolverNode other)
    {
		GibbsReal ovar = ((GibbsReal)other);
		_outputMsg = ovar._outputMsg;
		_sampleValue = ovar._sampleValue;
		_initialSampleValue = ovar._initialSampleValue;
		_initialSampleValueSet = ovar._initialSampleValueSet;
		_sampleArray = ovar._sampleArray;
		_bestSampleValue = ovar._bestSampleValue;
		_beta = ovar._beta;
		_holdSampleValue = ovar._holdSampleValue;
		_sampleSum = ovar._sampleSum;
		_sampleSumSquare = ovar._sampleSumSquare;
		_sampleCount = ovar._sampleCount;
		
		// Field values intentionally NOT moved:
		// _sampler
		// _conjugateSampler
		// _samplerSpecificallySpecified
		// _updateCount
		// _rejectCount
    }
	
	// Find a single conjugate sampler consistent with all neighboring factors and the Input
	public @Nullable IRealConjugateSampler findConjugateSampler()
	{
		Set<IRealConjugateSamplerFactory> availableSamplerFactories = findConjugateSamplerFactories();
		if (availableSamplerFactories.isEmpty())
			return null;	// No available conjugate sampler
		else
			return availableSamplerFactories.iterator().next().create();	// Get the first one and create the sampler
	}

	// Find the set of all available conjugate samplers, but don't create it yet
	public Set<IRealConjugateSamplerFactory> findConjugateSamplerFactories()
	{
		return findConjugateSamplerFactories(_var.getSiblings());
	}
	
	// Find the set of available conjugate samplers consistent with a specific set of neighboring factors (as well as the Input)
	public Set<IRealConjugateSamplerFactory> findConjugateSamplerFactories(List<? extends Factor> factors)
	{
		Set<IRealConjugateSamplerFactory> commonSamplers = new HashSet<IRealConjugateSamplerFactory>();

		// Check all the adjacent factors to see if they all support a common conjugate factor
		int numFactors = factors.size();
		for (int i = 0; i < numFactors; i++)
		{
			INode factorNode = factors.get(i);
			ISolverNode factor = factorNode.getSolver();
			if (!(factor instanceof IRealConjugateFactor))
			{
				commonSamplers.clear();	// At least one connected factor does not support conjugate sampling
				return commonSamplers;
			}
			int factorPortNumber = factorNode.getPortNum(_var);
			Set<IRealConjugateSamplerFactory> availableSamplers = ((IRealConjugateFactor)factor).getAvailableRealConjugateSamplers(factorPortNumber);
			if (i == 0)  // First time through
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
		}
		
		// Next, check conjugate samplers are also compatible with the input and the domain of this variable
		for (IRealConjugateSamplerFactory sampler : commonSamplers)
			if (!sampler.isCompatible(_input) || !sampler.isCompatible(_domain))
				commonSamplers.remove(sampler);	// Remove samplers not supported so remove it
		
		return commonSamplers;
	}

}
