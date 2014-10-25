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
import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.CDFSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IDiscreteDirectSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IDiscreteSamplerClient;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IGenericSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IMCMCSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
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
	
	private boolean _visited = false;

	private double[][] _inPortMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
	private DiscreteValue _outputMsg;
	private @Nullable long[] _beliefHistogram;
	private double[] _input;
	private double[] _conditional;
	private @Nullable IntArrayList _sampleIndexArray;
	private int _bestSampleIndex;
	private @Nullable DiscreteValue _initialSampleValue = null;
	private double _beta = 1;
	private final Discrete _varDiscrete;
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
	public GibbsDiscrete(Discrete var)
	{
		super(var);
		_varDiscrete = var;
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
	public final void update()
	{
		// Don't bother to re-sample deterministic dependent variables (those that are the output of a directional deterministic factor)
		if (_var.isDeterministicOutput()) return;

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
			oldValue = _outputMsg.clone();
			break;
		}

		final int messageLength = _input.length;
		final int numPorts = _var.getSiblingCount();
		double minEnergy = Double.POSITIVE_INFINITY;

		// Compute the conditional probability
		if (!_var.isDeterministicInput())
		{
			// Update all the neighboring factors
			// If there are no deterministic dependents, then it should be faster to have
			// each neighboring factor update its entire message to this variable than the alternative, below
			for (int port = 0; port < numPorts; port++)
			{
				((ISolverFactorGibbs)_var.getSibling(port).requireSolver("update")).updateEdgeMessage(_var.getSiblingPortIndex(port));
			}
			
			// Sum up the messages to get the conditional distribution
			for (int index = 0; index < messageLength; index++)
			{

				double out = _input[index];						// Sum of the input prior...
				for (int port = 0; port < numPorts; port++)
				{
					double tmp = _inPortMsgs[port][index];
					out += tmp;			// Plus each input message value
				}
				out *= _beta;									// Apply tempering

				if (out < minEnergy) minEnergy = out;			// For normalization

				_conditional[index] = out;						// Save in log domain representation
			}
		}
		else	// There are deterministic dependents, so must account for these
		{
			// TODO: SPEED UP
			for (int index = 0; index < messageLength; index++)
			{
				setCurrentSampleIndex(index);
				double out = _input[index];						// Sum of the input prior...
				ReleasableIterator<ISolverNodeGibbs> scoreNodes = getSampleScoreNodes();
				while (scoreNodes.hasNext())
				{
					out += scoreNodes.next().getPotential();
				}
				scoreNodes.release();
				
				out *= _beta;									// Apply tempering

				if (out < minEnergy) minEnergy = out;			// For normalization

				_conditional[index] = out;						// Save in log domain representation
			}
		}
		
		// Sample from the conditional distribution
		boolean rejected = false;
		_updateCount++;
		if (_sampler instanceof IDiscreteDirectSampler)
		{
			((IDiscreteDirectSampler)Objects.requireNonNull(_sampler)).nextSample(_outputMsg.clone(),
				_conditional, minEnergy, this);
		}
		else if (_sampler instanceof IMCMCSampler)
		{
			rejected = !((IMCMCSampler)Objects.requireNonNull(_sampler)).nextSample(_outputMsg.clone(), this);
			if (rejected) _rejectCount++;
		}
		
		switch (updateEventFlags)
		{
		case UPDATE_EVENT_SCORED:
			// TODO: non-conjugate samplers already compute sample scores, so we shouldn't have to do here.
			raiseEvent(new GibbsScoredVariableUpdateEvent(this, Objects.requireNonNull(oldValue), oldSampleScore,
				_outputMsg.clone(), getCurrentSampleScore(), rejected ? 1 : 0));
			break;
		case UPDATE_EVENT_SIMPLE:
			raiseEvent(new GibbsVariableUpdateEvent(this, Objects.requireNonNull(oldValue),
				_outputMsg.clone(), rejected ? 1 : 0));
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
		
		// If the variable has a fixed value, then set the current sample to that value and return
		if (_var.hasFixedValue())
		{
			setCurrentSampleIndex(_varDiscrete.getFixedValueIndex());
			return;
		}
		
		final DiscreteValue initialSampleValue = _initialSampleValue;
		if (initialSampleValue != null && restartCount == 0)
		{
			setCurrentSample(initialSampleValue);
			return;
		}

		// Convert the prior back to probabilities to sample from the prior
		int messageLength = _input.length;
		double minEnergy = Double.POSITIVE_INFINITY;
		for (int i = 0; i < messageLength; i++)
			if (_input[i] < minEnergy)
				minEnergy = _input[i];
		
		if (_sampler instanceof CDFSampler)
			((CDFSampler)Objects.requireNonNull(_sampler)).nextSample(_outputMsg, _input, minEnergy, this);
		else	// If the actual sampler isn't a CDF sampler, make a CDF sampler to use for random restart
		{
			IDiscreteDirectSampler sampler = new CDFSampler();
			sampler.initializeFromVariable(this);
			sampler.nextSample(_outputMsg, _input, minEnergy, this);
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
			double potential = _input[_outputMsg.getIndex()];

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
		if (sampleIndex != _outputMsg.getIndex())
			setCurrentSampleIndex(sampleIndex);
	}
	
	@SuppressWarnings("null")
	@Override
	public void updateBelief()
	{
		_beliefHistogram[_outputMsg.getIndex()]++;
	}

	@SuppressWarnings("null")
	@Override
	public double[] getBelief()
	{
		int domainLength = _input.length;
		double[] outBelief = new double[domainLength];

		if (_var.hasFixedValue())	// If there's a fixed value set, use that to generate the belief
		{
			Arrays.fill(outBelief, 0);
			outBelief[_varDiscrete.getFixedValueIndex()] = 1;
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
			for (int i = 0; i < domainLength; i++)
				outBelief[i] = _input[i];		// Disconnected variable that has never been updated
		}
		
		return outBelief;
	}

	
	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue, boolean hasFixed)
	{
		if (input == null)
		{
			_input = createDefaultMessage();
		}
		else
		{
			double[] vals = (double[])input;
			if (vals.length != _varDiscrete.getDiscreteDomain().size())
				throw new DimpleException("Prior size must match domain length");
			
			// Convert to energy values
			_input = new double[vals.length];
			for (int i = 0; i < vals.length; i++)
				_input[i] = -Math.log(vals[i]);
		}
		
		if (hasFixed)
		{
			// FIXME: is this correct? Why does it ignore fixedValue argument?
			setCurrentSampleIndexForce(requireNonNull((Integer)_var.getFixedValueObject()));
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
    		sampleIndexArray.add(_outputMsg.getIndex());
    	}
    }
    
    @Override
	public final void saveBestSample()
    {
    	_bestSampleIndex = _outputMsg.getIndex();
    }
    
    // TODO: move to ISolverNodeGibbs
	@Override
	public final double getPotential()
	{
		if (!_var.hasFixedValue())
			return _input[_outputMsg.getIndex()];
		else
			return 0;
	}
	
	@Override
	public final boolean hasPotential()
	{
		return !_var.hasFixedValue();
	}
	
	// TODO move to ISolverNode
	@Override
	public final double getScore()
	{
		if (!_var.hasFixedValue())
			return _input[getGuessIndex()];
		else
			return 0;	// If the value is fixed, ignore the guess
	}
	
	@Override
	public final void setCurrentSample(Object value)
	{
		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		// Also return if the variable is set to a fixed value
		if (_var.hasFixedValue()) return;
		

		DiscreteDomain domain = (DiscreteDomain)_var.getDomain();
		int valueIndex = domain.getIndex(value);
		if (valueIndex < 0)
			throw new DimpleException("Value is not in the domain of this variable");
		
		setCurrentSampleIndexForce(valueIndex);
	}
	
	@Override
	public final void setCurrentSample(Value value)
	{
		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		// Also return if the variable is set to a fixed value
		if (_var.hasFixedValue()) return;
		
		final GibbsNeighbors neighbors = _neighbors;
		boolean hasDeterministicDependents = neighbors != null && neighbors.hasDeterministicDependents();

		DiscreteValue oldValue = null;
		if (hasDeterministicDependents)
		{
			oldValue = _outputMsg.clone();
		}
		
		// Send the sample value to all output ports
		_outputMsg.setFrom(value);
				
		// If this variable has deterministic dependents, then set their values
		if (hasDeterministicDependents)
		{
			requireNonNull(neighbors).update(requireNonNull(oldValue));
		}
	}
	
	/*---------------
	 * Local methods
	 */
	
	public final void setCurrentSampleIndex(int index)
    {
		// If the sample value is being held, don't modify the value
		if (_holdSampleValue) return;
		
		// Also return if the variable is set to a fixed value
		if (_var.hasFixedValue()) return;
		
		setCurrentSampleIndexForce(index);
    }
	
	// Sets the sample regardless of whether the value is fixed or held
	private final void setCurrentSampleIndexForce(int index)
	{
		final GibbsNeighbors neighbors = _neighbors;
		boolean hasDeterministicDependents = neighbors != null && neighbors.hasDeterministicDependents();

		DiscreteValue oldValue = null;
		if (hasDeterministicDependents)
		{
			oldValue = _outputMsg.clone();
		}
		
		// Send the sample value to all output ports
		_outputMsg.setIndex(index);
				
		// If this variable has deterministic dependents, then set their values
		if (hasDeterministicDependents)
		{
			Objects.requireNonNull(neighbors).update(Objects.requireNonNull(oldValue));
		}
	}
    
    public final Object getCurrentSample()
    {
    	return _outputMsg.getObject();
    }
    public final int getCurrentSampleIndex()
    {
    	return _outputMsg.getIndex();
    }
    
    public final Object getBestSample()
    {
    	return _varDiscrete.getDiscreteDomain().getElement(_bestSampleIndex);
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
    	DiscreteDomain domain = _varDiscrete.getDiscreteDomain();
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
		_initialSampleValue = Value.create(_varDiscrete.getDomain(), initialSampleValue);
	}
	public final void setInitialSampleIndex(int initialSampleIndex)
	{
		DiscreteValue val = _initialSampleValue = Value.create(_varDiscrete.getDomain());
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
		DiscreteDomain domain = _varDiscrete.getDomain();
		_outputMsg = Value.create(domain);
		_outputMsg = (DiscreteValue)resetOutputMessage(_outputMsg);

		if (_sampleIndexArray != null)
			saveAllSamples();

		_beliefHistogram = new long[domain.size()];
		_bestSampleIndex = -1;
	}
	
	// TODO move to ISolverVariable
	@Override
	public Object[] createMessages(ISolverFactor factor)
	{
		int portNum = _var.getPortNum(Objects.requireNonNull(factor.getModelObject()));
		int length = _inPortMsgs.length;
		length = Math.max(portNum+1, length);
    	
	    _inPortMsgs = Arrays.copyOf(_inPortMsgs, length);
    	_inPortMsgs[portNum] = createDefaultMessage();
    	
	    _conditional = new double[_input.length];
		
		return new Object []{_inPortMsgs[portNum],_outputMsg};
	}

	public double [] createDefaultMessage()
	{
		double[] retVal = new double[((Discrete)_var).getDiscreteDomain().size()];
		return (double[])resetInputMessage(retVal);
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
		ds.setIndex(_var.hasFixedValue() ? _varDiscrete.getFixedValueIndex() : 0);	// Normally zero, but use fixed value if one has been set
		return ds;
	}

	// TODO move to ISolverNode
	@Override
	public void resetEdgeMessages(int portNum)
	{
		_inPortMsgs[portNum] = (double[])resetInputMessage(_inPortMsgs[portNum]);
		if (!_holdSampleValue)
			_outputMsg = (DiscreteValue)resetOutputMessage(_outputMsg);
	}

	// TODO move to ISolverNode
	@Override
	public Object getInputMsg(int portIndex)
	{
		return _inPortMsgs[portIndex];
	}

	// TODO move to ISolverNode
	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outputMsg;
	}

	// TODO move to ISolverNode
	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		_inPortMsgs[portIndex] = (double[])obj;
	}

	// TODO move to ISolverNode
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		GibbsDiscrete ovar = ((GibbsDiscrete)other);
		_inPortMsgs[thisPortNum] = ovar._inPortMsgs[otherPortNum];
	}
	
	// TODO move to ISolverVariable
	@Override
    public void moveNonEdgeSpecificState(ISolverNode other)
    {
		GibbsDiscrete ovar = ((GibbsDiscrete)other);
		_outputMsg = ovar._outputMsg;
		_sampleIndexArray = ovar._sampleIndexArray;
		_beliefHistogram = ovar._beliefHistogram;
		_outputMsg = ovar._outputMsg;
		_conditional = ovar._conditional;
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
		
		if (_var.hasFixedValue())
			setCurrentSampleIndexForce(requireNonNull((Integer)_var.getFixedValueObject()));
		else
			setCurrentSampleIndexForce(_outputMsg.getIndex());
		
		IGenericSampler sampler = _sampler;
		if (sampler == null || !_samplerSpecificallySpecified)
		{
			// If not specifically specified, use the default sampler
			sampler = _sampler = GibbsOptions.discreteSampler.instantiate(this);
		}
		sampler.initializeFromVariable(this);

		resetRejectionRateStats();
	}
}
