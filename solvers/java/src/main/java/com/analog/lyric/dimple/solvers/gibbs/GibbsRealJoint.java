/*******************************************************************************
 *   Copyright 2013 Analog Devices, Inc.
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

import static com.analog.lyric.dimple.environment.DimpleEnvironment.*;
import static com.analog.lyric.dimple.solvers.gibbs.GibbsSolverVariableEvent.*;
import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.UnaryJointRealFactorFunction;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.RealJointValue;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.solvers.core.PriorAndCondition;
import com.analog.lyric.dimple.solvers.core.SRealJointVariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.IRealJointConjugateFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.RealConjugateSamplerRegistry;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.RealJointConjugateSamplerRegistry;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IGenericSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IMCMCSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IRealSamplerClient;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.MHSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverEdgeState;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;
import com.analog.lyric.math.DimpleRandom;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.util.misc.Internal;
import com.analog.lyric.util.misc.Matlab;
import com.google.common.primitives.Doubles;

/*
 * WARNING: Whenever editing this class, also make the corresponding edit to SRealVariable.
 * The two are nearly identical, but unfortunately couldn't easily be shared due to the class hierarchy
 */

/**
 * RealJoint-valued solver variable for Gibbs solver.
 * @since 0.07
 */
public class GibbsRealJoint extends SRealJointVariableBase
	implements ISolverVariableGibbs, ISolverRealVariableGibbs, IRealSamplerClient
{
	/*-----------
	 * Constants
	 */
	
	@SuppressWarnings("hiding")
	protected static final int EVENT_MASK = 0x03;

	/*-------
	 * State
	 */
	
	private final class CurrentSample extends RealJointValue
	{
		private static final long serialVersionUID = 1L;

		CurrentSample(RealJointDomain domain)
		{
			super(domain);
			reset();
		}
		
		@Override
		public void setValue(double[] value)
		{
			// If the sample value is being held, don't modify the value
			if (_holdSampleValue) return;
			
			// Also return if the variable is set to a fixed value
			if (_model.hasFixedValue()) return;

			setCurrentSampleForce(value);
		}

		@Override
		public final void setValue(int index, double value)
		{
			if (value == _value[index])
			{
				return;
			}
			
			boolean hasDeterministicDependents = getModelObject().isDeterministicInput();

			RealJointValue oldValue = null;
			if (hasDeterministicDependents)
			{
				oldValue = _currentSample.clone();
				oldValue.setValue(oldValue.getValue().clone());
			}
			
			_value[index] = value;
			_currentSample.setValue(index, value);
			
			if (hasDeterministicDependents)
			{
				// If this variable has deterministic dependents, then set their values
				setDeterministicDependentValues(Objects.requireNonNull(oldValue));
			}
		}
		
		private final void setValueForce(double[] value)
		{
			// FIXME - check for changed value.
			
			boolean hasDeterministicDependents = getModelObject().isDeterministicInput();
			
			RealJointValue oldValue = null;
			if (hasDeterministicDependents)
			{
				oldValue = Value.create(getDomain(), _value);
			}
			
			_value = value.clone();
			
			if (hasDeterministicDependents)
			{
				// If this variable has deterministic dependents, then set their values
				setDeterministicDependentValues(Objects.requireNonNull(oldValue));
			}
		}
		
		private void reset()
		{
			double[] knownValue = getKnownRealJoint();
			_value = knownValue != null ? knownValue.clone() : _initialSampleValue.clone();
		}
	}
	
	private final CurrentSample _currentSample;
	private RealJointValue _prevSample; // Used only by BlastFromThePast factors
	private boolean _repeatedVariable;
	private @Nullable Object[] _inputMsg = null;
	private double[] _initialSampleValue;
	private boolean _initialSampleValueSet = false;
	private RealJointDomain _domain;
	private @Nullable IMCMCSampler _sampler = null;
	private @Nullable IRealJointConjugateSampler _conjugateSampler = null;
	private boolean _samplerSpecificallySpecified = false;
	private @Nullable ArrayList<double[]> _sampleArray;
	private @Nullable double[] _sampleSum;
	private @Nullable double[][] _sampleSumSquare;
	private long _sampleCount;
	private double[] _bestSampleValue;
	private double _beta = 1;
	private boolean _holdSampleValue = false;
	private int _numRealVars;
	private int _tempIndex = 0;
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

	public GibbsRealJoint(RealJoint var, GibbsSolverGraph parent)
	{
		super(var, parent);

		_domain = var.getDomain();
		
		_numRealVars = _domain.getNumVars();
		_initialSampleValue = new double[_numRealVars];
		_bestSampleValue = new double[_numRealVars];
		_prevSample = _currentSample = new CurrentSample(_domain);
		resetCurrentSample();
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
	public GibbsSolverGraph getParentGraph()
	{
		return (GibbsSolverGraph)_parent;
	}
	
	@Override
	public ISolverFactorGibbs getSibling(int edge)
	{
		return (ISolverFactorGibbs)super.getSibling(edge);
	}

	@Override
	public final void update()
	{
		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		final RealJoint model = _model;
		
		// Don't bother to re-sample deterministic dependent variables (those that are the output of a directional deterministic factor)
		if (model.isDeterministicOutput()) return;

		// Also return if the variable is set to a fixed value
		if (model.hasFixedValue()) return;

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
			oldValue = _currentSample.clone();
			break;
		}

		// Get the next sample value from the sampler
		int rejectCount = 0;
		IRealJointConjugateSampler conjugateSampler = _conjugateSampler;
		if (conjugateSampler == null)
		{
			// Use MCMC sampler
			RealValue nextSample = RealValue.create();
			IMCMCSampler sampler = Objects.requireNonNull(_sampler);
			for (int i = 0; i < _numRealVars; i++)
			{
				_tempIndex = i;		// Save this to be used by the call-back from sampler
				nextSample.setDouble(_currentSample.getValue(i));
				if (!sampler.nextSample(nextSample, this))
				{
					++rejectCount;
				}
				_updateCount++;	// Updates count each real variable when using an MCMC sampler
				_rejectCount += rejectCount;
			}
		}
		else
		{
			// Use conjugate sampler, first update the messages from all factors
			// Factor messages represent the current distribution parameters from each factor
			final int numEdges = model.getSiblingCount();
			final ISolverEdgeState[] sedges = new ISolverEdgeState[numEdges];
			final FactorGraph fg = model.requireParentGraph();
			final SolverNodeMapping solvers = getSolverMapping();
			final ISolverFactorGraph sfg = solvers.getSolverGraph(fg);
			for (int portIndex = 0; portIndex < numEdges; portIndex++)
			{
				final int edgeIndex = model.getSiblingEdgeIndex(portIndex);
				final EdgeState edge = requireNonNull(fg.getGraphEdgeState(edgeIndex));
				final GibbsSolverEdge<?> sedge = requireNonNull((GibbsSolverEdge<?>)sfg.getSolverEdge(edgeIndex));
				final ISolverFactorGibbs factor = (ISolverFactorGibbs)sfg.getSolverFactorForEdge(edge);
				
				sedges[portIndex] =  sedge;
				factor.updateEdgeMessage(edge, sedge);	// Run updateEdgeMessage for each neighboring factor
			}
			PriorAndCondition inputs = getPriorAndCondition();
			setCurrentSample(conjugateSampler.nextSample(sedges, inputs));
			inputs.release();
			_updateCount++;
		}

		switch (updateEventFlags)
		{
		case UPDATE_EVENT_SCORED:
			// TODO: non-conjugate samplers already compute sample scores, so we shouldn't have to do here.
			raiseEvent(new GibbsScoredVariableUpdateEvent(this, Objects.requireNonNull(oldValue), oldSampleScore,
				_currentSample.clone(), getCurrentSampleScore(), rejectCount));
			break;
		case UPDATE_EVENT_SIMPLE:
			raiseEvent(new GibbsVariableUpdateEvent(this, Objects.requireNonNull(oldValue),
				_currentSample.clone(),rejectCount));
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
		setCurrentSample(_tempIndex, sampleValue);

		return getCurrentSampleScore();
	}

	@Override
	public final double getCurrentSampleScore()
	{
		double sampleScore = Double.POSITIVE_INFINITY;
		_scoreCount++;

		computeScore:
		{
			if (!_domain.inDomain(_currentSample.getValue()))
				break computeScore; // outside the domain

			// Sum up the potentials from the prior, condition and all connected factors
			
			PriorAndCondition known = getPriorAndCondition();
			double potential = known.evalEnergy(_currentSample);
			known = known.release();

			if (!Doubles.isFinite(potential))
			{
				break computeScore;
			}

			ReleasableIterator<ISolverNodeGibbs> scoreNodes = getSampleScoreNodes();
			while (scoreNodes.hasNext())
			{
				potential += scoreNodes.next().getPotential();
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
		setCurrentSample(_tempIndex, sampleValue);
	}

	// TODO move to local methods?
	// For conjugate samplers
	public final @Nullable IRealJointConjugateSampler getConjugateSampler()
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
		final RealJoint model = _model;
		final FactorGraph fg = model.requireParentGraph();
		final SolverNodeMapping solvers = getSolverMapping();
		final ISolverFactorGraph sfg = solvers.getSolverGraph(fg);
		final int numEdges = model.getSiblingCount();
		final ISolverEdgeState[] sedges = new ISolverEdgeState[numEdges - 1];
		for (int port = 0, i = 0; port < numEdges; port++)
		{
			if (port != outPortNum)
			{
				final int edgeIndex = model.getSiblingEdgeIndex(port);
				final EdgeState edgeState = requireNonNull(fg.getGraphEdgeState(edgeIndex));
				final GibbsSolverEdge<?> sedge = requireNonNull((GibbsSolverEdge<?>)sfg.getSolverEdge(edgeIndex));
				final ISolverFactorGibbs factor = (ISolverFactorGibbs)sfg.getSolverFactorForEdge(edgeState);
				
				sedges[i++] = sfg.getSolverEdge(edgeIndex);
				factor.updateEdgeMessage(edgeState, sedge);	// Run updateEdgeMessage for each neighboring factor
			}
		}
		PriorAndCondition inputs = getPriorAndCondition();
		((IRealJointConjugateSampler)conjugateSampler).aggregateParameters(outputMessage, sedges, inputs);
		inputs.release();
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
	public RealJointValue getCurrentSampleValue()
	{
		return _currentSample;
	}
	
	@Internal
	@Override
	public RealJointValue getPrevSampleValue()
	{
		return _prevSample;
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
		double[] knownValue = getKnownRealJoint();
		if (knownValue != null)
		{
			setCurrentSample(knownValue);
			return;
		}
		if (_initialSampleValueSet && restartCount == 0)
		{
			setCurrentSample(_initialSampleValue);
			return;
		}

		final DimpleRandom rand = activeRandom();
		
		// If the variable has a prior, sample from that (bounded by the domain)
		final IUnaryFactorFunction priorJoint = _model.getPriorFunction();
		
		List<? extends IUnaryFactorFunction> priorArray = null;
		if (priorJoint instanceof UnaryJointRealFactorFunction)
		{
			priorArray = ((UnaryJointRealFactorFunction)priorJoint).realFunctions();
		}
		else if (priorJoint instanceof MultivariateNormalParameters)
		{
			priorArray = ((MultivariateNormalParameters)priorJoint).getDiagonalNormals();
		}

		if (priorArray != null)
		{
			for (int i = 0; i < _numRealVars; i++)
			{
				RealDomain realDomain = _domain.getRealDomain(i);
				IUnaryFactorFunction prior = priorArray.get(i);

				// If there are inputs, see if there's an available conjugate sampler
				IRealConjugateSampler inputConjugateSampler = null;		// Don't use the global conjugate sampler since other factors might not be conjugate
				if (prior != null)
					inputConjugateSampler = RealConjugateSamplerRegistry.findCompatibleSampler(prior);

				// Determine if there are bounds
				double hi = realDomain.getUpperBound();
				double lo = realDomain.getLowerBound();

				if (inputConjugateSampler != null)
				{
					// Sample from the input if there's an available sampler
					double sampleValue = inputConjugateSampler.nextSample(new ISolverEdgeState[0],
						Collections.singletonList(prior));

					// If there are also bounds, clip at the bounds
					if (sampleValue > hi) sampleValue = hi;
					if (sampleValue < lo) sampleValue = lo;
					setCurrentSample(i, sampleValue);
				}
				else
				{
					// No available sampler, so if bounded, sample uniformly from the bounds
					if (hi < Double.POSITIVE_INFINITY && lo > Double.NEGATIVE_INFINITY)
						setCurrentSample(i, activeRandom().nextDouble() * (hi - lo) + lo);
				}
			}
		}
		else if (priorJoint != null)		// Input is a joint input
		{
			// Don't use the global conjugate sampler since other factors might not be conjugate
			IRealJointConjugateSampler inputConjugateSampler =
				RealJointConjugateSamplerRegistry.findCompatibleSampler(priorJoint);
			if (inputConjugateSampler != null)
			{
				double[] sampleValue =
					inputConjugateSampler.nextSample(new ISolverEdgeState[0], Collections.singletonList(priorJoint));
				
				// Clip if necessary
				for (int i = 0; i < _numRealVars; i++)
				{
					// Determine if there are bounds
					RealDomain realDomain = _domain.getRealDomain(i);
					double hi = realDomain.getUpperBound();
					double lo = realDomain.getLowerBound();

					// If there are also bounds, clip at the bounds
					if (sampleValue[i] > hi) sampleValue[i] = hi;
					if (sampleValue[i] < lo) sampleValue[i] = lo;
				}
				setCurrentSample(sampleValue);
			}
			else	// No available conjugate sampler
			{
				for (int i = 0; i < _numRealVars; i++)
				{
					// Determine if there are bounds
					RealDomain realDomain = _domain.getRealDomain(i);
					double hi = realDomain.getUpperBound();
					double lo = realDomain.getLowerBound();

					// No available sampler, so if bounded, sample uniformly from the bounds
					if (hi < Double.POSITIVE_INFINITY && lo > Double.NEGATIVE_INFINITY)
						setCurrentSample(i, rand.nextDouble() * (hi - lo) + lo);
					else if (hi < _currentSample.getValue(i))
						setCurrentSample(i, hi);
					else if (lo > _currentSample.getValue(i))
						setCurrentSample(i, lo);
				}
			}
		}
		else	// There are no inputs
		{
			for (int i = 0; i < _numRealVars; i++)
			{
				// Determine if there are bounds
				RealDomain realDomain = _domain.getRealDomain(i);
				double hi = realDomain.getUpperBound();
				double lo = realDomain.getLowerBound();

				// If bounded, sample uniformly from the bounds, otherwise leave current sample value
				if (hi < Double.POSITIVE_INFINITY && lo > Double.NEGATIVE_INFINITY)
					setCurrentSample(i, rand.nextDouble() * (hi - lo) + lo);
			}
		}
	}

	@Override
	public final void updateBelief()
	{
		if (_sampleSum != null)
		{
			// Update the sums for computing moments
			for (int i = 0; i < _numRealVars; i++)
			{
				final double vi = _currentSample.getValue(i);
				requireNonNull(_sampleSum)[i] += vi;
				for (int j = i; j < _numRealVars; j++)
				{
					final double vj = _currentSample.getValue(j);
					requireNonNull(_sampleSumSquare)[i][j] += vi * vj;
				}
			}
		}
		_sampleCount++;
	}

	@Override
	public void updatePriorAndCondition()
	{
		Value value = getKnownValue();
		if (value != null)
		{
			setCurrentSampleForce(value.getDoubleArray());
		}
	}

	@Override
	public Object getBelief()
	{
		return 0d;
	}
	
	@Matlab
	public final double[] getSampleMean()
	{
		if (_sampleSum != null)
		{
			final double[] mean = new double[_numRealVars];
			for (int i = 0; i < _numRealVars; i++)
				mean[i] = requireNonNull(_sampleSum)[i] / requireNonNull(_sampleCount);
			return mean;
		}
		else
		{
			throw new DimpleException("The sample mean is only computed if the option GibbsOptions.computeRealJointBeliefMoments has been set to true");
		}
	}
	
	@Matlab
	public final double[][] getSampleCovariance()
	{
		if (_sampleSum != null)
		{
			// For all sample values, compute the covariance matrix
			// For now, use the naive algorithm; could be improved
			final double[][] covariance = new double[_numRealVars][_numRealVars];
			final double sampleCount = _sampleCount;
			final double sampleCountMinusOne = (sampleCount - 1);
			for (int i = 0; i < _numRealVars; i++)
			{
				for (int j = i; j < _numRealVars; j++)
				{
					final double sumi = requireNonNull(_sampleSum)[i];
					final double sumj = requireNonNull(_sampleSum)[j];
					final double sumij = requireNonNull(_sampleSumSquare)[i][j];
					final double value = (sumij - sumi * (sumj / sampleCount) ) / sampleCountMinusOne;
					covariance[i][j] = value;
					covariance[j][i] = value;	// Fill in lower triangular half
				}
			}
			return covariance;
		}
		else
		{
			throw new DimpleException("The sample covariance is only computed if the option GibbsOptions.computeRealJointBeliefMoments has been set to true");
		}

	}

	@SuppressWarnings("null")
	@Override
	public void postAddFactor(@Nullable Factor f)
	{
		// Set the default sampler
	}

	@Override
	public double[] getGuess()
	{
		if (_guessWasSet)
			return _guessValue;
		
		final double[] knownValue = getKnownRealJoint();
		if (knownValue != null)
			return knownValue;
		else
			return _currentSample.getValue();
	}
	
	/**
	 * @deprecated Instead set {@link GibbsOptions#saveAllSamples} to true using {@link #setOption}.
	 */
	@Deprecated
	@Override
	public final void saveAllSamples()
	{
		_sampleArray = new ArrayList<double[]>();
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
		final ArrayList<double[]> sampleArray = _sampleArray;
		if (sampleArray != null)
			sampleArray.add(_currentSample.getValue().clone());
	}

	@Override
	public final void saveBestSample()
	{
		_bestSampleValue = _currentSample.getValue().clone();
	}
	
	@Override
	public final double getPotential()
	{
		return evalPriorAndConditionEnergy(_currentSample);
	}
	
	@Override
	public final boolean hasPotential()
	{
		return canHavePriorAndConditionEnergy();
	}

	@Override
	public final void setCurrentSample(Object value)
	{
		setCurrentSample((double[])value);
	}
	
	@Override
	public final void setCurrentSample(Value value)
	{
		setCurrentSample(value.getDoubleArray());
	}
	
    /*---------------
     * Local methods
     */
    
	public final void setCurrentSample(double[] value)
	{
		_currentSample.setValue(value);
	}
	
	// Sets the sample regardless of whether the value is fixed or held
	private final void setCurrentSampleForce(double[] value)
	{
		_currentSample.setValueForce(value);
	}
	
	// Set a specific element of the sample value
    public final void setCurrentSample(int index, Object value)
    {
    	setCurrentSample(index, FactorFunctionUtilities.toDouble(value));
    }
    
	public final void setCurrentSample(int index, double value)
	{
		_currentSample.setValue(index, value);
	}
	
	private final void setDeterministicDependentValues(RealJointValue oldValue)
	{
		final GibbsNeighbors neighbors = _neighbors;
		if (neighbors != null)
			neighbors.update(oldValue);
	}

	@Matlab
	public final double[] getCurrentSample()
	{
		return _currentSample.getValue();
	}

	@Matlab
	public final double[] getBestSample()
	{
		return _bestSampleValue;
	}

	@Matlab
	@Override
	public final double[][] getAllSamples()
	{
		final ArrayList<double[]> sampleArray = _sampleArray;
		if (sampleArray == null)
		{
			return ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
		}
		int length = sampleArray.size();
		double[][] retval = new double[length][];
		for (int i = 0; i < length; i++)
			retval[i] = sampleArray.get(i);
		return retval;
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
	public final @Nullable List<double[]> _getSampleArrayUnsafe()
	{
		return _sampleArray;
	}

	public final void setAndHoldSampleValue(double[] value)
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
	
	// There should be a way to call these directly via the samplers
	// If so, they should be removed from here since this makes this sampler-specific
	@SuppressWarnings("null")
	@Deprecated
	public final void setProposalKernel(IProposalKernel proposalKernel)					// IProposalKernel object
	{
		if (_sampler instanceof MHSampler)
			((MHSampler)_sampler).setProposalKernel(proposalKernel);
	}
	@SuppressWarnings("null")
	@Deprecated
	public final void setProposalKernel(String proposalKernelName)						// Name of proposal kernel
	{
		if (_sampler instanceof MHSampler)
			((MHSampler)_sampler).setProposalKernel(proposalKernelName);
	}
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
	@Matlab
	@Deprecated
	public final void setSampler(String samplerName)
	{
		GibbsOptions.realSampler.convertAndSet(this, samplerName);
		IMCMCSampler sampler = (IMCMCSampler) GibbsOptions.realSampler.instantiateIfDifferent(this, _sampler);
		_sampler = sampler;
		_samplerSpecificallySpecified = true;
		sampler.initializeFromVariable(this);
	}
	
	@Matlab
	@Override
	public final @Nullable ISampler getSampler()
	{
		if (_samplerSpecificallySpecified)
		{
			Objects.requireNonNull(_sampler).initializeFromVariable(this);
			return _sampler;
		}
		else if (_model.hasParentGraph())
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
		else
			return null;
	}
	
	@Matlab
	public final String getSamplerName()
	{
		ISampler sampler = getSampler();
		if (sampler != null)
			return sampler.getClass().getSimpleName();
		else
			return "";
	}

	@Matlab
	public final void setInitialSampleValue(double[] initialSampleValue)
	{
		_initialSampleValue = initialSampleValue;
		_initialSampleValueSet = true;
	}

	@Matlab
	public final double[] getInitialSampleValue()
	{
		return _initialSampleValue;
	}

	// TODO move to ISolverVariableGibbs
	@Override
	public final void setBeta(double beta)	// beta = 1/temperature
	{
		_beta = beta;
	}
	


	public void resetCurrentSample()
	{
		final double[] knownValue = getKnownRealJoint();
		_currentSample.setValue(knownValue != null ? knownValue.clone() : _initialSampleValue.clone());
	}

	@Deprecated
	@Override
	public @Nullable Object getInputMsg(int portIndex)
	{
		return _inputMsg;
	}

	@Deprecated
	@Override
	public @Nullable Object getOutputMsg(int portIndex)
	{
		return _currentSample;
	}

	@Deprecated
	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		Object[] inputMsg = _inputMsg;
		if (inputMsg == null)
			inputMsg = new Object[_model.getSiblingCount()];
		inputMsg[portIndex] = obj;
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
		
		if (!getModelObject().isDeterministicOutput())
		{
			final double[] knownValue = getKnownRealJoint();
			double[] initialSampleValue = knownValue != null ? knownValue : _initialSampleValue;
			if (!_holdSampleValue)
				setCurrentSampleForce(initialSampleValue);
		}
		
		// Clear out sample state
		_bestSampleValue = _currentSample.getValue();
		ArrayList<double[]> sampleArray = null;
		if (saveAllSamples)
		{
			sampleArray = _sampleArray;
			if (sampleArray == null)
			{
				sampleArray = new ArrayList<double[]>();
			}
			else
			{
				sampleArray.clear();
			}
		}
		_sampleArray = sampleArray;
		
		// Clear out the Belief statistics
		if (getOptionOrDefault(GibbsOptions.computeRealJointBeliefMoments))
		{
			if (_sampleSum == null)
				_sampleSum = new double[_numRealVars];
			if (_sampleSumSquare == null)
				_sampleSumSquare = new double[_numRealVars][_numRealVars];
	
			Arrays.fill(_sampleSum, 0);
			for (int i = 0; i < _numRealVars; i++)
				Arrays.fill(requireNonNull(_sampleSumSquare)[i], 0);
		}
		else
		{
			_sampleSum = null;
			_sampleSumSquare = null;
		}
		_sampleCount = 0;

		updatePriorAndCondition();
		
		//
		// Determine which sampler to use
		//
		
		_conjugateSampler = null;

		if (!_samplerSpecificallySpecified)
		{
			IOptionHolder[] source = new IOptionHolder[1];
			Class<? extends IGenericSampler> samplerClass = getOptionAndSource(GibbsOptions.realSampler, source);
			if (samplerClass == null || source[0] != this && source[0] != _model)
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
	public void createNonEdgeSpecificState()
	{
		resetCurrentSample();
	    _bestSampleValue = _currentSample.getValue();
	    if (_sampleArray != null)
			saveAllSamples();
	}
	
	// TODO move to ISolverVariable
	@Override
    public void moveNonEdgeSpecificState(ISolverNode other)
    {
		GibbsRealJoint ovar = ((GibbsRealJoint)other);
		ovar._prevSample = _currentSample;
		ovar._repeatedVariable = true;
		if (!_repeatedVariable)
		{
			if (_prevSample == _currentSample)
			{
				// If not already pointing at a different value object, then this must be the
				// the first variable in the stream, so make a copy of its state.
				_prevSample = _currentSample.clone();
			}
			else
			{
				_prevSample.setFrom(_currentSample);
			}
		}
		Value fixedValue = _model.getPriorValue();
		if (fixedValue != null)
			_currentSample.setValueForce(fixedValue.getDoubleArray());
		else
			_currentSample.setFrom(ovar._currentSample);
		_initialSampleValue = ovar._initialSampleValue;
		_initialSampleValueSet = ovar._initialSampleValueSet;
		_sampleArray = ovar._sampleArray;
		_bestSampleValue = ovar._bestSampleValue;
		_beta = ovar._beta;
		_holdSampleValue = ovar._holdSampleValue;
		_numRealVars = ovar._numRealVars;
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
	
	// Get the dimension of the joint variable
	public int getDimension()
	{
		return _numRealVars;
	}

	
	// Find a single conjugate sampler consistent with all neighboring factors and the Input
	public @Nullable IRealJointConjugateSampler findConjugateSampler()
	{
		Set<IRealJointConjugateSamplerFactory> availableSamplerFactories = findConjugateSamplerFactories();
		if (availableSamplerFactories.isEmpty())
			return null;	// No available conjugate sampler
		else
			return availableSamplerFactories.iterator().next().create();	// Get the first one and create the sampler
	}

	// Find the set of all available conjugate samplers, but don't create it yet
	public Set<IRealJointConjugateSamplerFactory> findConjugateSamplerFactories()
	{
		return findConjugateSamplerFactories(_model.getSiblingEdgeState());
	}
	
	// Find the set of available conjugate samplers consistent with a specific set of neighboring factors (as well as the Input)
	public Set<IRealJointConjugateSamplerFactory> findConjugateSamplerFactories(Collection<EdgeState> edges)
	{
		final Set<IRealJointConjugateSamplerFactory> commonSamplers = new HashSet<>();
		final FactorGraph fg = _model.requireParentGraph();
		final SolverNodeMapping solvers = getSolverMapping();
		
		// Check all the adjacent factors to see if they all support a common conjugate factor
		for (EdgeState edgeState : edges)
		{
			ISolverNode factor = solvers.getSolverFactor(edgeState.getFactor(fg));
			if (!(factor instanceof IRealJointConjugateFactor))
			{
				commonSamplers.clear();	// At least one connected factor does not support conjugate sampling
				return commonSamplers;
			}
			int factorPortNumber = edgeState.getFactorToVariableEdgeNumber();
			Set<IRealJointConjugateSamplerFactory> availableSamplers =
				((IRealJointConjugateFactor)factor).getAvailableRealJointConjugateSamplers(factorPortNumber);
			
			if (commonSamplers.isEmpty())
			{
				commonSamplers.addAll(availableSamplers);
			}
			else
			{
				commonSamplers.retainAll(availableSamplers);
			}
		}
		
		// Next, check conjugate samplers are also compatible with the input and the domain of this variable
		final IUnaryFactorFunction inputJoint = _model.getPriorFunction();
		IUnaryFactorFunction condition = getConditionFunction();
		Iterator<IRealJointConjugateSamplerFactory> iter = commonSamplers.iterator();
		while (iter.hasNext())
		{
			IRealJointConjugateSamplerFactory sampler = iter.next();
			if (!sampler.isCompatible(inputJoint) || !sampler.isCompatible(condition) || !sampler.isCompatible(_domain))
			{
				iter.remove();
			}
		}
		
		return commonSamplers;
	}
	
	@SuppressWarnings("null")
	@Override
	public GibbsSolverEdge<?> getSiblingEdgeState(int siblingIndex)
	{
		return (GibbsSolverEdge<?>)getSiblingEdgeState_(siblingIndex);
	}
}
