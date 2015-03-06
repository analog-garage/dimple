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

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.IntArrayList;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.CDFSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IDiscreteDirectSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IDiscreteSamplerClient;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IGenericSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IMCMCSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.util.misc.Internal;
import com.google.common.primitives.Doubles;


/**
 * Solver variable for Discrete variables in Gibbs solver.
 * 
 * @since 0.07
 */
public class GibbsDiscrete extends SDiscreteVariableBase implements ISolverVariableGibbs, IDiscreteSamplerClient
{
	/*-----------
	 * Constants
	 */
	
	/**
	 * Bits in {@link #_flags} reserved by this class and its superclasses.
	 */
	@SuppressWarnings("hiding")
	protected static final int RESERVED_FLAGS = 0xFFFF0003;
	
	@SuppressWarnings("hiding")
	protected static final int EVENT_MASK = 0x03;

	public static final String DEFAULT_DISCRETE_SAMPLER_NAME = "CDFSampler";
	
	/*-------
	 * State
	 */
	
	private final class CurrentSample extends DiscreteValue
	{
		private static final long serialVersionUID = 1L;

		private final DiscreteValue _value;
		
		CurrentSample(DiscreteDomain domain)
		{
			_value = Value.create(domain);
		}
		
		CurrentSample(CurrentSample other)
		{
			_value = other._value.clone();
		}

		@Override
		public DiscreteValue clone()
		{
			return new CurrentSample(this);
		}

		@Override
		public DiscreteDomain getDomain()
		{
			return _value.getDomain();
		}

		@Override
		public int getIndex()
		{
			return _value.getIndex();
		}

		@Override
		public @NonNull Object getObject()
		{
			return _value.getObject();
		}

		@Override
		public final void setFrom(Value value)
		{
			if (fixed() || _value.valueEquals(value))
			{
				return;
			}

			final GibbsNeighbors neighbors = _neighbors;
			boolean hasDeterministicDependents = neighbors != null && neighbors.hasDeterministicDependents();

			DiscreteValue oldValue = null;
			if (hasDeterministicDependents)
			{
				oldValue = _value.clone();
			}
			
			_value.setFrom(value);
					
			// If this variable has deterministic dependents, then set their values
			if (hasDeterministicDependents)
			{
				requireNonNull(neighbors).update(requireNonNull(oldValue));
			}
		}
		
		@Override
		public void setIndex(int index)
		{
			if (!fixed() && index != _value.getIndex())
			{
				setIndexForce(index);
			}
		}

		@Override
		public void setObject(@Nullable Object obj)
		{
			if (fixed())
			{
				return;
			}

			final GibbsNeighbors neighbors = _neighbors;
			boolean hasDeterministicDependents = neighbors != null && neighbors.hasDeterministicDependents();

			DiscreteValue oldValue = null;
			if (hasDeterministicDependents)
			{
				oldValue = _value.clone();
			}

			_value.setObject(obj);
			
			// If this variable has deterministic dependents, then set their values
			if (hasDeterministicDependents && !_value.valueEquals(requireNonNull(oldValue)))
			{
				Objects.requireNonNull(neighbors).update(oldValue);
			}
		}
		
		private void setIndexForce(int index)
		{
			final GibbsNeighbors neighbors = _neighbors;
			boolean hasDeterministicDependents = neighbors != null && neighbors.hasDeterministicDependents();

			DiscreteValue oldValue = null;
			if (hasDeterministicDependents)
			{
				oldValue = _value.clone();
			}
			
			_value.setIndex(index);
					
			// If this variable has deterministic dependents, then set their values
			if (hasDeterministicDependents)
			{
				Objects.requireNonNull(neighbors).update(Objects.requireNonNull(oldValue));
			}
		}

		private final boolean fixed()
		{
			return _holdSampleValue || _model.hasFixedValue();
		}
		
	} // CurrentSample
	
	private boolean _visited = false;

	private final CurrentSample _currentSample;
	private DiscreteValue _prevSample;
	private boolean _repeatedVariable;
	private @Nullable long[] _beliefHistogram;
	private DiscreteEnergyMessage _input;
	private @Nullable IntArrayList _sampleIndexArray;
	private int _bestSampleIndex;
	private @Nullable DiscreteValue _initialSampleValue = null;
	private double _beta = 1;
	private boolean _holdSampleValue = false;
	private @Nullable IGenericSampler _sampler;
	private boolean _samplerSpecificallySpecified = false;
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
	
	@SuppressWarnings("null")
	public GibbsDiscrete(Discrete var, GibbsSolverGraph parent)
	{
		super(var, parent);
		_prevSample = _currentSample = new CurrentSample(_model.getDomain());
		_input = createDefaultMessage();
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
	public ISolverFactorGibbs getSibling(int edge)
	{
		return (ISolverFactorGibbs)super.getSibling(edge);
	}

	@Override
	public final void update()
	{
		// Don't bother to re-sample deterministic dependent variables (those that are the output of a directional deterministic factor)
		if (_model.isDeterministicOutput()) return;

		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		// Also return if the variable is set to a fixed value
		if (_model.hasFixedValue()) return;

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

		final int messageLength = _input.size();
		final int numPorts = _model.getSiblingCount();
		double minEnergy = Double.POSITIVE_INFINITY;
		
		// Conditional probability in log domain
		final double[] conditional = DimpleEnvironment.doubleArrayCache.allocate(messageLength);

		// Compute the conditional probability
		if (!_model.isDeterministicInput())
		{
			
			
			// Update all the neighboring factors
			// If there are no deterministic dependents, then it should be faster to have
			// each neighboring factor update its entire message to this variable than the alternative, below
			for (int port = 0; port < numPorts; port++)
			{
				getSibling(port).updateEdgeMessage(_model.getSiblingPortIndex(port));
			}
			
			// Sum up the messages to get the conditional distribution
			for (int index = 0; index < messageLength; index++)
			{

				double out = _input.getEnergy(index);						// Sum of the input prior...
				for (int port = 0; port < numPorts; port++)
				{
					double tmp = getDiscreteEdge(port).factorToVarMsg.getEnergy(index);
					out += tmp;			// Plus each input message value
				}
				out *= _beta;									// Apply tempering

				if (out < minEnergy) minEnergy = out;			// For normalization

				conditional[index] = out;						// Save in log domain representation
			}
		}
		else	// There are deterministic dependents, so must account for these
		{
			// TODO: SPEED UP
			for (int index = 0; index < messageLength; index++)
			{
				setCurrentSampleIndex(index);
				double out = _input.getEnergy(index);						// Sum of the input prior...
				ReleasableIterator<ISolverNodeGibbs> scoreNodes = getSampleScoreNodes();
				while (scoreNodes.hasNext())
				{
					out += scoreNodes.next().getPotential();
				}
				scoreNodes.release();
				
				out *= _beta;									// Apply tempering

				if (out < minEnergy) minEnergy = out;			// For normalization

				conditional[index] = out;						// Save in log domain representation
			}
		}
		
		// Sample from the conditional distribution
		_updateCount++;
		boolean rejected = false;
		
		if (minEnergy < Double.POSITIVE_INFINITY)
		{
			if (_sampler instanceof IDiscreteDirectSampler)
			{
				((IDiscreteDirectSampler)Objects.requireNonNull(_sampler)).nextSample(_currentSample.clone(),
					conditional, minEnergy, this);
			}
			else if (_sampler instanceof IMCMCSampler)
			{
				rejected = !((IMCMCSampler)Objects.requireNonNull(_sampler)).nextSample(_currentSample.clone(), this);
			}
		}
		else
		{
			rejected = true;
		}
		
		DimpleEnvironment.doubleArrayCache.release(conditional);
		
		if (rejected) _rejectCount++;
		
		switch (updateEventFlags)
		{
		case UPDATE_EVENT_SCORED:
			// TODO: non-conjugate samplers already compute sample scores, so we shouldn't have to do here.
			raiseEvent(new GibbsScoredVariableUpdateEvent(this, Objects.requireNonNull(oldValue), oldSampleScore,
				_currentSample.clone(), getCurrentSampleScore(), rejected ? 1 : 0));
			break;
		case UPDATE_EVENT_SIMPLE:
			raiseEvent(new GibbsVariableUpdateEvent(this, Objects.requireNonNull(oldValue),
				_currentSample.clone(), rejected ? 1 : 0));
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

	/*-------------------------------
	 * ISolverVariableGibbs methods
	 */
	
	@Override
	public DiscreteValue getCurrentSampleValue()
	{
		return _currentSample;
	}
	
	@Internal
	@Override
	public DiscreteValue getPrevSampleValue()
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
		
		// If the variable has a fixed value, then set the current sample to that value and return
		if (_model.hasFixedValue())
		{
			setCurrentSampleIndex(_model.getFixedValueIndex());
			return;
		}
		
		final DiscreteValue initialSampleValue = _initialSampleValue;
		if (initialSampleValue != null && restartCount == 0)
		{
			setCurrentSample(initialSampleValue);
			return;
		}

		// Convert the prior back to probabilities to sample from the prior
		double minEnergy = _input.minEnergy();
		
		if (_sampler instanceof CDFSampler)
			((CDFSampler)Objects.requireNonNull(_sampler)).nextSample(_currentSample, _input.representation(), minEnergy, this);
		else	// If the actual sampler isn't a CDF sampler, make a CDF sampler to use for random restart
		{
			IDiscreteDirectSampler sampler = new CDFSampler();
			sampler.initializeFromVariable(this);
			sampler.nextSample(_currentSample, _input.representation(), minEnergy, this);
	}
	}

	// TODO - move up to ISolverVariable
	@Override
	public final double getSampleScore(Value sampleValue)
	{
		return getSampleScore(((DiscreteValue)sampleValue).getIndex());
	}
	@Override
	public final double getSampleScore(int sampleIndex)
	{
		// WARNING: Side effect is that the current sample value changes to this sample value
		// Could change back but less efficient to do this, since we'll be updating the sample value anyway
		setCurrentSampleIndex(sampleIndex);

		return getCurrentSampleScore();
	}
	@Override
	public final double getCurrentSampleScore()
	{
		double sampleScore = Double.POSITIVE_INFINITY;
		_scoreCount++;
		
		computeScore:
		{
			double potential = _input.getEnergy(_currentSample.getIndex());

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
		setNextSampleIndex(((DiscreteValue)sampleValue).getIndex());
	}
	@Override
	public final void setNextSampleIndex(int sampleIndex)
	{
		if (sampleIndex != _currentSample.getIndex())
			setCurrentSampleIndex(sampleIndex);
	}
	
	@SuppressWarnings("null")
	@Override
	public void updateBelief()
	{
		_beliefHistogram[_currentSample.getIndex()]++;
	}

	@SuppressWarnings("null")
	@Override
	public double[] getBelief()
	{
		int domainLength = _input.size();
		double[] outBelief = new double[domainLength];

		if (_model.hasFixedValue())	// If there's a fixed value set, use that to generate the belief
		{
			Arrays.fill(outBelief, 0);
			outBelief[_model.getFixedValueIndex()] = 1;
			return outBelief;
		}
		
		// Otherwise, compute the belief
		long sum = 0;
		for (int i = 0; i < domainLength; i++)
		{
			sum+= _beliefHistogram[i];
		}
		if (sum != 0)
		{
			for (int i = 0; i < domainLength; i++)
				outBelief[i] = (double)_beliefHistogram[i]/(double)sum;
		}
		else
		{
			System.arraycopy(_input.representation(), 0, outBelief, 0, domainLength);
		}
		
		return outBelief;
	}

	
	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue)
	{
		if (input == null)
		{
			_input = createDefaultMessage();
		}
		else
		{
			double[] vals = (double[])input;
			if (vals.length != _model.getDiscreteDomain().size())
				throw new DimpleException("Prior size must match domain length");
			
			// Convert to energy values
			for (int i = 0; i < vals.length; i++)
			{
				_input.setWeight(i,  vals[i]);
			}
		}
		
		if (fixedValue != null)
		{
			// FIXME: is this correct? Why does it ignore fixedValue argument?
			setCurrentSampleIndexForce(requireNonNull(_model.getFixedValueObject()));
		}
	}
	
	@SuppressWarnings("null")
	@Override
	public void postAddFactor(@Nullable Factor f)
	{
	}
	
	/**
	 * @deprecated Instead set {@link GibbsOptions#saveAllSamples} to true using {@link #setOption}.
	 */
	@Deprecated
    @Override
	public final void saveAllSamples()
    {
    	_sampleIndexArray = new IntArrayList();
    	setOption(GibbsOptions.saveAllSamples, true);
    }
    
	/**
	 * @deprecated Instead set {@link GibbsOptions#saveAllSamples} to false using {@link #setOption}.
	 */
	@Deprecated
    @Override
	public void disableSavingAllSamples()
    {
    	_sampleIndexArray = null;
    	setOption(GibbsOptions.saveAllSamples, false);
    }
    
    @Override
	public final void saveCurrentSample()
    {
    	final IntArrayList sampleIndexArray = _sampleIndexArray;
    	if (sampleIndexArray != null)
    	{
    		sampleIndexArray.add(_currentSample.getIndex());
    	}
    }
    
    @Override
	public final void saveBestSample()
    {
    	_bestSampleIndex = _currentSample.getIndex();
    }
    
    // TODO: move to ISolverNodeGibbs
	@Override
	public final double getPotential()
	{
		if (!_model.hasFixedValue())
			return _input.getEnergy(_currentSample.getIndex());
		else
			return 0;
	}
	
	@Override
	public final boolean hasPotential()
	{
		return !_model.hasFixedValue();
	}
	
	// TODO move to ISolverNode
	@Override
	public final double getScore()
	{
		if (!_model.hasFixedValue())
			return _input.getEnergy(getGuessIndex());
		else
			return 0;	// If the value is fixed, ignore the guess
	}
	
	@Override
	public final void setCurrentSample(Object value)
	{
		_currentSample.setObject(value);
	}
	
	@Override
	public final void setCurrentSample(Value value)
	{
		_currentSample.setFrom(value);
	}
	
	/*---------------
	 * Local methods
	 */
	
	public final void setCurrentSampleIndex(int index)
    {
		_currentSample.setIndex(index);
    }
	
	// Sets the sample regardless of whether the value is fixed or held
	private final void setCurrentSampleIndexForce(int index)
	{
		_currentSample.setIndexForce(index);
	}
    
    public final Object getCurrentSample()
    {
    	return _currentSample.getObject();
    }
    public final int getCurrentSampleIndex()
    {
    	return _currentSample.getIndex();
    }
    
    public final Object getBestSample()
    {
    	return _model.getDiscreteDomain().getElement(_bestSampleIndex);
    }
    public final int getBestSampleIndex()
    {
    	return _bestSampleIndex;
    }

    @Override
	public final Object[] getAllSamples()
    {
    	final IntArrayList sampleIndexArray = _sampleIndexArray;
    	
		if (sampleIndexArray == null)
		{
			return ArrayUtil.EMPTY_OBJECT_ARRAY;
		}
		int length = sampleIndexArray.size();
    	DiscreteDomain domain = _model.getDiscreteDomain();
    	Object[] retval = new Object[length];
    	for (int i = 0; i < length; i++)
    		retval[i] = domain.getElement(sampleIndexArray.get(i));
    	return retval;
    }
    public final int[] getAllSampleIndices()
    {
    	final IntArrayList sampleIndexArray = _sampleIndexArray;

    	if (sampleIndexArray == null)
    	{
    		return ArrayUtil.EMPTY_INT_ARRAY;
    	}
    	
    	return Arrays.copyOf(sampleIndexArray.elements(), sampleIndexArray.size());
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

	public final void setAndHoldSampleValue(Object value)
	{
		releaseSampleValue();
		setCurrentSample(value);
		holdSampleValue();
	}
	
	public final void setAndHoldSampleIndex(int index)
	{
		releaseSampleValue();
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
	
	public final void setInitialSampleValue(Object initialSampleValue)
	{
		_initialSampleValue = Value.create(_model.getDomain(), initialSampleValue);
	}
	public final void setInitialSampleIndex(int initialSampleIndex)
	{
		DiscreteValue val = _initialSampleValue = Value.create(_model.getDomain());
		val.setIndex(initialSampleIndex);
	}

	public final @Nullable Object getInitialSampleValue()
	{
		final DiscreteValue val = _initialSampleValue;
		return val != null ? val.getObject() : null;
	}
	public final int getInitialSampleIndex()
	{
		final DiscreteValue val = _initialSampleValue;
		return val != null ? val.getIndex() : -1;
	}
	
    // TODO: move to ISolverVariableGibbs
    
    @Override
	public final void setBeta(double beta)	// beta = 1/temperature
    {
    	_beta = beta;
    }
	
    // TODO move to bottom

	/**
	 * @deprecated Will be removed in future release. Instead use {@link GibbsOptions#discreteSampler}
	 * option.
	 */
    @Deprecated
	public final void setDefaultSampler(String samplerName)
    {
    	GibbsOptions.discreteSampler.convertAndSet(this, samplerName);
    }
    
	/**
	 * @deprecated Will be removed in future release. Instead use {@link GibbsOptions#discreteSampler}
	 * option.
	 */
    @Deprecated
	public final String getDefaultSamplerName()
    {
    	return getOptionOrDefault(GibbsOptions.discreteSampler).getSimpleName();
    }
    
	/**
	 * Sets sampler to be used for this variable.
	 * <p>
	 * In general, it is usually easier to configure the sampler using the
	 * {@link GibbsOptions#discreteSampler} option. This method should only be
	 * required when the sampler class is not registered with the
	 * {@linkplain DimpleEnvironment#genericSamplers() generic sampler registry}
	 * for the current environment.
	 * <p>
	 * @param sampler is a non-null sampler.
	 */
    public final void setSampler(ISampler sampler)
    {
    	_sampler = (IGenericSampler)sampler;
    	_samplerSpecificallySpecified = true;
    }
    
	/**
	 * @deprecated Will be removed in future release. Instead set sampler by setting
	 * {@link GibbsOptions#discreteSampler} option using {@link #setOption}.
	 */
    @Deprecated
   public final void setSampler(String samplerName)
    {
    	GibbsOptions.discreteSampler.convertAndSet(this, samplerName);
    	_sampler = GibbsOptions.discreteSampler.instantiate(this);
    	_samplerSpecificallySpecified = true;
    }
    
    @Override
    public final ISampler getSampler()
    {
    	IGenericSampler sampler = _sampler;
    	
    	if (sampler == null || !_samplerSpecificallySpecified)
    	{
    		IGenericSampler newSampler = GibbsOptions.discreteSampler.instantiate(this);
    		if (newSampler != sampler)
    		{
    			newSampler.initializeFromVariable(this);
    		}
			sampler = newSampler;
    	}
    	
    	return sampler;
    }
	
	public final String getSamplerName()
	{
		final ISampler sampler = getSampler();
		return sampler.getClass().getSimpleName();
	}

	
	// TODO move to ISolverVariable
	@Override
	public void createNonEdgeSpecificState()
	{
		resetOutputMessage(_currentSample);

		if (_sampleIndexArray != null)
			saveAllSamples();

		_beliefHistogram = new long[_model.getDomain().size()];
		_bestSampleIndex = -1;
	}
	
	public DiscreteEnergyMessage createDefaultMessage()
	{
		return new DiscreteEnergyMessage(_model.getDiscreteDomain().size());
	}

	// TODO move to ISolverVariable
	@Override
	public Object resetInputMessage(Object message)
	{
		double [] retval = (double[])message;
		Arrays.fill(retval, 0);
		return retval;
	}
	
	// TODO move to ISolverVariable
	@Override
	public Object resetOutputMessage(Object message)
	{
		DiscreteValue ds = (DiscreteValue)message;
		ds.setIndex(_model.hasFixedValue() ? _model.getFixedValueIndex() : 0);	// Normally zero, but use fixed value if one has been set
		return ds;
	}

	// TODO move to ISolverNode
	@Override
	public double[] getInputMsg(int portIndex)
	{
		// FIXM - return DiscreteMessage
		return getDiscreteEdge(portIndex).factorToVarMsg.representation();
	}

	// TODO move to ISolverNode
	@Override
	public DiscreteValue getOutputMsg(int portIndex)
	{
		return _currentSample;
	}

	// TODO move to ISolverNode
	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		final IParameterizedMessage message = getEdge(portIndex).factorToVarMsg;
		
		if (obj instanceof IParameterizedMessage)
		{
			message.setFrom((IParameterizedMessage)obj);
		}
		else if (obj instanceof double[])
		{
			double[] target  = ((DiscreteMessage)message).representation();
			System.arraycopy(obj, 0, target, 0, target.length);
		}
	}

	// TODO move to ISolverNode
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		final GibbsDiscrete sother = (GibbsDiscrete) other;

		final GibbsSolverEdge<?> thisEdge = getEdge(thisPortNum);
		final GibbsSolverEdge<?> otherEdge = sother.getEdge(otherPortNum);
		
		thisEdge.factorToVarMsg.setFrom(otherEdge.factorToVarMsg);
		otherEdge.reset();
	}
	
	// TODO move to ISolverVariable
	@Override
    public void moveNonEdgeSpecificState(ISolverNode other)
    {
		GibbsDiscrete ovar = ((GibbsDiscrete)other);
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
		_currentSample.setFrom(ovar._currentSample);
		_sampleIndexArray = ovar._sampleIndexArray;
		_beliefHistogram = ovar._beliefHistogram;
		_bestSampleIndex = ovar._bestSampleIndex;
		_initialSampleValue = ovar._initialSampleValue;
		_beta = ovar._beta;
		_sampler = ovar._sampler;
		_samplerSpecificallySpecified = ovar._samplerSpecificallySpecified;
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
		
		// Clear out sample state
		_bestSampleIndex = -1;
		IntArrayList sampleIndexArray = null;
		if (saveAllSamples)
		{
			sampleIndexArray = _sampleIndexArray;
			if (sampleIndexArray == null)
			{
				sampleIndexArray = new IntArrayList();
			}
			else
			{
				sampleIndexArray.clear();
			}
		}
		_sampleIndexArray = sampleIndexArray;
		
		Arrays.fill(_beliefHistogram, 0);
		
		if (_model.hasFixedValue())
			setCurrentSampleIndexForce(requireNonNull(_model.getFixedValueObject()));
		else
			setCurrentSampleIndexForce(_currentSample.getIndex());
		
		IGenericSampler sampler = _sampler;
		if (sampler == null || !_samplerSpecificallySpecified)
		{
			// If not specifically specified, use the default sampler
			sampler = _sampler = GibbsOptions.discreteSampler.instantiate(this);
		}
		sampler.initializeFromVariable(this);

		resetRejectionRateStats();
	}
	
	@SuppressWarnings("null")
	@Override
	protected GibbsSolverEdge<?> getEdge(int siblingIndex)
	{
		return (GibbsSolverEdge<?>)super.getEdge(siblingIndex);
	}
	
	@SuppressWarnings("null")
	GibbsDiscreteEdge getDiscreteEdge(int siblingIndex)
	{
		return (GibbsDiscreteEdge)super.getEdge(siblingIndex);
	}
	
}
