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

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.KeyedPriorityQueue;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.Beta;
import com.analog.lyric.dimple.factorfunctions.Binomial;
import com.analog.lyric.dimple.factorfunctions.Categorical;
import com.analog.lyric.dimple.factorfunctions.CategoricalEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.CategoricalUnnormalizedParameters;
import com.analog.lyric.dimple.factorfunctions.Dirichlet;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransition;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransitionEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransitionUnnormalizedParameters;
import com.analog.lyric.dimple.factorfunctions.ExchangeableDirichlet;
import com.analog.lyric.dimple.factorfunctions.Gamma;
import com.analog.lyric.dimple.factorfunctions.LogNormal;
import com.analog.lyric.dimple.factorfunctions.Multinomial;
import com.analog.lyric.dimple.factorfunctions.MultinomialEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.MultinomialUnnormalizedParameters;
import com.analog.lyric.dimple.factorfunctions.Multiplexer;
import com.analog.lyric.dimple.factorfunctions.NegativeExpGamma;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.Poisson;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.DirectedNodeSorter;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.repeated.FactorGraphStream;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.schedulers.GibbsDefaultScheduler;
import com.analog.lyric.dimple.schedulers.schedule.IGibbsSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomBernoulli;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomBeta;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomBinomial;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomCategorical;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomCategoricalUnnormalizedOrEnergyParameters;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomDirichlet;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomDiscreteTransition;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomDiscreteTransitionUnnormalizedOrEnergyParameters;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomExchangeableDirichlet;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomGamma;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomLogNormal;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomMultinomial;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomMultinomialUnnormalizedOrEnergyParameters;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomMultiplexer;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomNegativeExpGamma;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomNormal;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomPoisson;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.BlockMHSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.IBlockInitializer;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.IBlockMCMCSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverBlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.math.DimpleRandomGenerator;

import cern.colt.list.DoubleArrayList;

/**
 * Solver-specific factor graph for Gibbs solver.
 * <p>
 * <em>Previously was com.analog.lyric.dimple.solvers.gibbs.SFactorGraph</em>
 *  <p>
 * @since 0.07
 */
public class GibbsSolverGraph extends SFactorGraphBase //implements ISolverFactorGraph
{
	private @Nullable IGibbsSchedule _schedule;
	private @Nullable Iterator<IScheduleEntry> _scheduleIterator;
	private @Nullable ArrayList<IBlockInitializer> _blockInitializers;
	private int _numSamples = GibbsOptions.numSamples.defaultIntValue();
	private int _updatesPerSample = GibbsOptions.scansPerSample.defaultIntValue();
	private int _burnInUpdates = 0;
	private int _scansPerSample = 1;
	private int _burnInScans = GibbsOptions.burnInScans.defaultIntValue();
	private int _numRandomRestarts = GibbsOptions.numRandomRestarts.defaultIntValue();
	private boolean _temper = false;
	private double _initialTemperature;
	private double _temperingDecayConstant;
	private double _temperature;
	private double _minPotential = Double.MAX_VALUE;
	private boolean _firstSample = true;
	private @Nullable DoubleArrayList _scoreArray;
	
	private static final double LOG2 = Math.log(2);
	
	/**
	 * Priority queue of deterministic factors whose outputs should be
	 * reevaluated. Lazily created.
	 */
	private @Nullable KeyedPriorityQueue<ISolverFactorGibbs, SFactorUpdate> _deferredDeterministicFactorUpdates = null;
	
	/**
	 * The number of requests to defer update of deterministic directed factor outputs.
	 * If greater than zero, {@link #scheduleDeterministicDirectedUpdate(ISolverFactorGibbs, int)} will
	 * defer execution until later. This counter may be greater than one as a result of recursive
	 * calls.
	 */
	private int _deferDeterministicFactorUpdatesCounter = 0;

	protected GibbsSolverGraph(FactorGraph factorGraph)
	{
		super(factorGraph);
		_factorGraph.setSolverSpecificDefaultScheduler(new GibbsDefaultScheduler());	// Override the common default scheduler
	}

	@SuppressWarnings("deprecation")
	@Override
	public ISolverVariable createVariable(Variable var)
	{
		if (var.getModelerClassName().equals("RealJoint") || var.getModelerClassName().equals("Complex"))
			return new SRealJointVariable(var);
		if (var.getModelerClassName().equals("Real"))
			return new SRealVariable(var);
		else
			return new SDiscreteVariable((Discrete)var);
	}
	
	
	// Note, customFactorExists is intentionally not overridden and therefore returns false
	// This is because all of the custom factors for this solver also exist as FactorFunctions,
	// and therefore we still want the MATLAB code to create a factor with the specified FactorFunctions.
	@SuppressWarnings("deprecation") // TODO: remove when S*Factor classes removed.
	@Override
	public ISolverFactor createFactor(Factor factor)
	{
		FactorFunction factorFunction = factor.getFactorFunction().getContainedFactorFunction();	// In case it's wrapped
		
		// First see if any custom factor should be created
		if (factorFunction instanceof Normal)
			return new CustomNormal(factor);
		else if (factorFunction instanceof Gamma)
			return new CustomGamma(factor);
		else if (factorFunction instanceof NegativeExpGamma)
			return new CustomNegativeExpGamma(factor);
		else if (factorFunction instanceof LogNormal)
			return new CustomLogNormal(factor);
		else if (factorFunction instanceof DiscreteTransition)
			return new CustomDiscreteTransition(factor);
		else if (factorFunction instanceof DiscreteTransitionUnnormalizedParameters)
			return new CustomDiscreteTransitionUnnormalizedOrEnergyParameters(factor);
		else if (factorFunction instanceof DiscreteTransitionEnergyParameters)
			return new CustomDiscreteTransitionUnnormalizedOrEnergyParameters(factor);
		else if (factorFunction instanceof Categorical)
			return new CustomCategorical(factor);
		else if (factorFunction instanceof CategoricalUnnormalizedParameters)
			return new CustomCategoricalUnnormalizedOrEnergyParameters(factor);
		else if (factorFunction instanceof CategoricalEnergyParameters)
			return new CustomCategoricalUnnormalizedOrEnergyParameters(factor);
		else if (factorFunction instanceof Dirichlet)
			return new CustomDirichlet(factor);
		else if (factorFunction instanceof ExchangeableDirichlet)
			return new CustomExchangeableDirichlet(factor);
		else if (factorFunction instanceof Beta)
			return new CustomBeta(factor);
		else if (factorFunction instanceof Bernoulli)
			return new CustomBernoulli(factor);
		else if (factorFunction instanceof Binomial)
			return new CustomBinomial(factor);
		else if (factorFunction instanceof Multinomial)
			return new CustomMultinomial(factor);
		else if (factorFunction instanceof MultinomialUnnormalizedParameters)
			return new CustomMultinomialUnnormalizedOrEnergyParameters(factor);
		else if (factorFunction instanceof MultinomialEnergyParameters)
			return new CustomMultinomialUnnormalizedOrEnergyParameters(factor);
		else if (factorFunction instanceof Poisson)
			return new CustomPoisson(factor);
		else if (factorFunction instanceof Multiplexer)
			return new CustomMultiplexer(factor);
		else if (factor.isDiscrete())			// No custom factor exists, so create a generic one
			return new STableFactor(factor);	// Generic discrete factor
		else
			return new GibbsRealFactor(factor);		// Generic real factor
	}
	

	@Override
	public ISolverBlastFromThePastFactor createBlastFromThePast(BlastFromThePastFactor factor)
	{
		//TODO: catch case where the factor is directed
		if (factor.isDirected() || factor.getFactorFunction().isDeterministicDirected())
			throw new DimpleException("not yet supported");
		
		if (factor.isDiscrete())
			return new GibbsTableFactorBlastFromThePast(factor);
		else
			return new GibbsRealFactorBlastFromThePast(factor);
	}
	
	@Override
	public @Nullable ISolverFactorGibbs getSolverFactor(Factor factor)
	{
		return (ISolverFactorGibbs)super.getSolverFactor(factor);
	}
	
	@Override
	public @Nullable ISolverVariableGibbs getSolverVariable(Variable variable)
	{
		return (ISolverVariableGibbs)super.getSolverVariable(variable);
	}
	
	@Override
	public void initialize()
	{
		_numSamples = getOptionOrDefault(GibbsOptions.numSamples);
		_numRandomRestarts = getOptionOrDefault(GibbsOptions.numRandomRestarts);
		_scansPerSample = getOptionOrDefault(GibbsOptions.scansPerSample);
		_burnInScans = getOptionOrDefault(GibbsOptions.burnInScans);
		final boolean saveAllScores = getOptionOrDefault(GibbsOptions.saveAllScores);
		_temper = getOptionOrDefault(GibbsOptions.enableAnnealing);
		_initialTemperature = getOptionOrDefault(GibbsOptions.initialTemperature);
		_temperingDecayConstant = 1 - LOG2/getOptionOrDefault(GibbsOptions.annealingHalfLife);
		
		// Make sure the schedule is created before factor initialization to allow custom factors to modify the schedule if needed
		final IGibbsSchedule schedule = _schedule = (IGibbsSchedule)_factorGraph.getSchedule();

		FactorGraph fg = _factorGraph;
		Map<Node,Integer> nodeOrder = DirectedNodeSorter.orderDirectedNodes(fg);
		for (Factor factor : fg.getFactorsFlat())
		{
			ISolverFactorGibbs sfactor = requireNonNull(getSolverFactor(factor));
			Integer order = nodeOrder.get(factor);
			sfactor.setTopologicalOrder(order != null ? order : 0);
		}
		
		// Same as SFactorGraphBase.initialize() but with deferral of deterministic updates
		_blockInitializers = null;
		deferDeterministicUpdates();
		for (int i = 0, end = fg.getOwnedVariableCount(); i < end; ++i)
			fg.getOwnedVariable(i).requireSolver("initialize").initialize();
		if (!fg.hasParentGraph())
			for (int i = 0, end = fg.getBoundaryVariableCount(); i <end; ++i)
				fg.getBoundaryVariable(i).requireSolver("initialize").initialize();
		processDeferredDeterministicUpdates();
		for (Factor f : fg.getNonGraphFactorsTop())
			f.requireSolver("initialize").initialize();
		for (FactorGraph g : fg.getNestedGraphs())
			g.requireSolver("initialize").initialize();
		deferDeterministicUpdates();
		final ArrayList<IBlockInitializer> blockInitializers = _blockInitializers;
		if (blockInitializers != null)
			for (IBlockInitializer b : blockInitializers)	// After initializing all variables and factors, invoke any block initializers
				b.initialize();
		processDeferredDeterministicUpdates();

		_scheduleIterator = schedule.iterator();
		_minPotential = Double.POSITIVE_INFINITY;
		_firstSample = true;
		
		setUpdatesPerSampleFromScans();
		setBurnInUpdatesFromScans();
		
		if (_temper) setTemperature(_initialTemperature);
		
		DoubleArrayList scoreArray = null;
		if (saveAllScores)
		{
			scoreArray = _scoreArray;
			if (scoreArray == null)
			{
				scoreArray = new DoubleArrayList();
			}
			else
			{
				scoreArray.clear();
			}
		}
		_scoreArray = scoreArray;
	}

	/**
	 * Does one round of Gibbs sampling.
	 * <p>
	 * Performs the equivalent of:
	 * <blockquote>
	 * <pre>
	 * for (int restart = 0; restart &lt;= {@link #getNumRestarts()}; ++restart)
	 * {
	 *     {@linkplain #burnIn(int) burnIn(restart)};
	 *     for (int i = 0; i &lt; {@link #getNumSamples()}; ++i)
	 *     {
	 *         {@link #sample()};
	 *     }
	 * }
	 * </pre>
	 * </blockquote>
	 * 
	 */
	@Override
	public void solveOneStep()
	{
		_minPotential = Double.POSITIVE_INFINITY;
		_firstSample = true;
		
		for (int restartCount = 0; restartCount <= _numRandomRestarts; restartCount++)
		{
			burnIn(restartCount);
			for (int iter = 0; iter < _numSamples; iter++)
				oneSample();
		}
	}
	
	/**
	 * Perform initial burn in.
	 * <p>
	 * This invokes {@link #burnIn(int)} with value zero.
	 */
	public final void burnIn()
	{
		burnIn(0);
	}
	
	/**
	 * Perform burn-in phase.
	 * <p>
	 * This consists of randomly reinitializing values of variables in the graph
	 * that do not have fixed values and then performing {@link #getBurnInUpdates()}
	 * variable updates.
	 * <p>
	 * Burn-in is required for most graphs to ensure that the samples will be closer to the
	 * real distribution.
	 * <p>
	 * @param restartCount is a non-negative number indicating which random restart is
	 * executing. This will be zero for the initial burn-in phase.
	 */
	public final void burnIn(int restartCount)
	{
		randomRestart(restartCount);
		iterate(_burnInUpdates);
	}
	
	/**
	 * Generate one sample.
	 * <p>
	 * Simply invokes {@link #sample(int)} with value one.
	 */
	public void sample()
	{
		sample(1);
	}
	
	/**
	 * Run more samples without initializing, burn-in, or random-restarts
	 * <p>
	 * This is like {@link #iterate}, except that while iterate just updates runs a specified number
	 * of single-variable updates, this runs a specified number of entire samples, where the size of
	 * a sample has already been defined in terms of number of either updates or scans.
	 * <p>
	 * @param numSamples is a positive number indicating the number of samples to generate.
	 */
	public void sample(int numSamples)
	{
		for (int sample = 0; sample < numSamples; sample++)
			oneSample();
	}

	/**
	 * Performs specified number of single variable updates
	 *<p>
	 * Note that the iterate() method for the Gibbs solver means do the
	 * specified number of single-variable updates, regardless of other parameter settings.
	 * The iterate() method behaves differently than for other solvers due to the fact that the
	 * {@link #update()} method for Gibbs-specific schedules will update only a single variable.
	 * Also, multithreaded operation for Gibbs is not supported
	 */
	@Override
	public void iterate(int numUpdates)
	{
		Iterator<IScheduleEntry> scheduleIterator = Objects.requireNonNull(_scheduleIterator);
		final ISchedule schedule = Objects.requireNonNull(_schedule);
		
		for (int iterNum = 0; iterNum < numUpdates; iterNum++)
		{
			if (!scheduleIterator.hasNext())
				scheduleIterator = _scheduleIterator = schedule.iterator();	// Wrap-around the schedule if reached the end

			scheduleIterator.next().update();
		}
		
		// Allow interruption (if the solver is run as a thread); currently interruption is allowed only between iterations, not within a single iteration
		try {interruptCheck();}
		catch (InterruptedException e) {return;}
	}

	
	@SuppressWarnings("null")
	protected void oneSample()
	{
		iterate(_updatesPerSample);
		for (Variable v : _factorGraph.getVariables())
		{
			ISolverVariableGibbs vs = getSolverVariable(v);
			vs.updateBelief();
			vs.saveCurrentSample();		// Note that the first sample saved is one full sample after burn in, not immediately after burn in (in case the burn in is zero)
		}
		
		// Save the best sample value seen so far
		double totalPotential = getTotalPotential();
		if (totalPotential < _minPotential || _firstSample)
		{
			for (Variable v : _factorGraph.getVariables())
				getSolverVariable(v).saveBestSample();
			_minPotential = totalPotential;
			_firstSample = false;
		}
		
		// If requested save score value for each sample
		final DoubleArrayList scoreArray = _scoreArray;
		if (scoreArray != null)
		{
			scoreArray.add(totalPotential);
		}
		
		// If tempering, reduce the temperature
		if (_temper)
		{
			_temperature *= _temperingDecayConstant;
			setTemperature(_temperature);
		}
	}
	
	
	@SuppressWarnings("null")
	@Override
	public void postAdvance()
	{
		//In the case of rolled up graphs, we make sure we randomly restart
		//the variables that are added to the end of the chain.
		for (FactorGraphStream fgs : getModel().getFactorGraphStreams())
		{
			
			FactorGraph ng = fgs.getNestedGraphs().get(fgs.getNestedGraphs().size()-1);
			for (Variable vb : ng.getBoundaryVariables())
			{
				getSolverVariable(vb).randomRestart(0);
			}
		}
	}
	
	@SuppressWarnings("null")
	public void randomRestart(int restartCount)
	{
		deferDeterministicUpdates();
		
		for (Variable v : _factorGraph.getVariables())
			getSolverVariable(v).randomRestart(restartCount);
		
		final ArrayList<IBlockInitializer> blockInitializers = _blockInitializers;
		if (blockInitializers != null)
			for (IBlockInitializer b : blockInitializers)	// Also invoke any block initializers
				b.initialize();

		processDeferredDeterministicUpdates();
		
		if (_temper) setTemperature(_initialTemperature);	// Reset the temperature, if tempering
	}
	
	

	// Get the total potential over all factors of the graph given the current sample values (including input priors on variables)
	@SuppressWarnings("null")
	public double getTotalPotential()
	{
		double totalPotential = 0;
		for (Factor f : _factorGraph.getNonGraphFactors())
			totalPotential += getSolverFactor(f).getPotential();
		for (Variable v : _factorGraph.getVariables())		// Variables contribute too because they have inputs, which are factors
			totalPotential += getSolverVariable(v).getPotential();
		return totalPotential;
	}
	
	/**
	 * @deprecated Instead set {@link GibbsOptions#saveAllSamples} to true using {@link #setOption}.
	 */
	@Deprecated
	@SuppressWarnings("null")
	public void saveAllSamples()
	{
		setOption(GibbsOptions.saveAllSamples, true);
	}
	
	/**
	 * @deprecated Instead set {@link GibbsOptions#saveAllSamples} to false using {@link #setOption}.
	 */
	@Deprecated
	@SuppressWarnings("null")
	public void disableSavingAllSamples()
	{
		setOption(GibbsOptions.saveAllSamples, false);
	}
	
	/**
	 * @deprecated Instead set {@link GibbsOptions#saveAllScores} to true using {@link #setOption}.
	 */
	@Deprecated
	public void saveAllScores()
	{
		_scoreArray = new DoubleArrayList();
		setOption(GibbsOptions.saveAllScores, true);
	}
	
	/**
	 * @deprecated Instead set {@link GibbsOptions#saveAllScores} to false using {@link #setOption}.
	 */
	@Deprecated
	public void disableSavingAllScores()
	{
		_scoreArray = null;
		setOption(GibbsOptions.saveAllScores, false);
	}
	
	/**
	 * If the score had been saved, return the array of score values, otherwise null.
	 */
	public final @Nullable double[] getAllScores()
	{
		final DoubleArrayList scoreArray = _scoreArray;
		if (scoreArray != null)
		{
			return Arrays.copyOf(scoreArray.elements(), scoreArray.size());
		}
		else
			return null;
	}
	
	/**
	 * Get the rejection rate of the sampler for variables and block entries for which it applies
	 * @return rejection rate
	 * @since 0.07
	 */
	public final double getRejectionRate()
	{
		long updateCount = 0;
		long rejectCount = 0;
		
		// Accumulate the rejection statistics for all variables
		for (Variable v : _factorGraph.getVariables())
		{
			ISolverVariableGibbs variable = requireNonNull(getSolverVariable(v));
			updateCount += variable.getUpdateCount();
			rejectCount += variable.getRejectionCount();
		}
		
		// Accumulate the rejection statistics for any BlockMHSamplers in the schedule
		ISchedule schedule = _factorGraph.getSchedule();
		for (IScheduleEntry s : schedule)
		{
			if (s instanceof BlockScheduleEntry)
			{
				IBlockUpdater b = ((BlockScheduleEntry)s).getBlockUpdater();
				if (b instanceof IBlockMCMCSampler)
				{
					updateCount += ((BlockMHSampler)b).getUpdateCount();
					rejectCount += ((BlockMHSampler)b).getRejectionCount();
				}
			}
		}
		
		return (updateCount > 0) ? (double)rejectCount / (double)updateCount : 0;
	}
	
	/**
	 * Clear the rejection rate statistics
	 * @since 0.07
	 */
	public final void resetRejectionRateStats()
	{
		// Reset the rejection statistics for all variables
		for (Variable v : _factorGraph.getVariables())
			requireNonNull(getSolverVariable(v)).resetRejectionRateStats();
		
		// Reset the rejection statistics for any BlockMHSamplers in the schedule
		ISchedule schedule = _factorGraph.getSchedule();
		for (IScheduleEntry s : schedule)
		{
			if (s instanceof BlockScheduleEntry)
			{
				IBlockUpdater b = ((BlockScheduleEntry)s).getBlockUpdater();
				if (b instanceof IBlockMCMCSampler)
					((BlockMHSampler)b).resetRejectionRateStats();
			}
		}
	}
	
	
	// Set/get the current temperature for all variables in the graph (for tempering)
	@SuppressWarnings("null")
	public void setTemperature(double T)
	{
		_temperature = T;
		double beta = 1/T;
		for (Variable v : _factorGraph.getVariables())
			getSolverVariable(v).setBeta(beta);
	}
	public double getTemperature() {return _temperature;}
	
	// Sets the random seed for the Gibbs solver.  This allows runs of the solver to be repeatable.
	public void setSeed(long seed)
	{
		DimpleRandomGenerator.setSeed(seed);
	}
	
	/**
	 * Sets the number of samples to generate per restart.
	 * <p>
	 * Sets the value of {@link #getNumSamples()} and the corresponding {@link GibbsOptions#numSamples}
	 * option to the specified value.
	 * <p>
	 * @param numSamples must be a positive integer.
	 * @deprecated Instead set {@link GibbsOptions#numSamples} option on this object or its corresponding
	 * model object using {@link #setOption}.
	 */
	@Deprecated
	public void setNumSamples(int numSamples)
	{
		setOption(GibbsOptions.numSamples, numSamples);
		_numSamples = numSamples;
	}
	
	/**
	 * Number of samples to generate per restart.
	 * <p>
	 * Set automatically from the {@link GibbsOptions#numSamples} option during {@link #initialize}.
	 */
	public int getNumSamples()
	{
		return _numSamples;
	}
	
	/**
	 * @deprecated This method will be removed in a future release.
	 */
	@Deprecated
	public int getUpdatesPerSample()
	{
		return _updatesPerSample;
	}
	
	/**
	 * @deprecated This method will be removed in a future release.
	 */
	@Deprecated
	public void setUpdatesPerSample(int updatesPerSample)
	{
		// TODO: when this method is removed, change the range of scansPerSample to [0,max]
		_updatesPerSample = updatesPerSample;
		setOption(GibbsOptions.scansPerSample, -1);
		_scansPerSample = -1;	// Samples specified in updates rather than scans
	}
	
	/**
	 * Sets the number of full updates of all of the variables to perform for each sample.
	 * <p>
	 * This sets the value of the corresponding {@link GibbsOptions#scansPerSample} option.
	 * <p>
	 * @param scansPerSample must be a positive integer.
	 * @deprecated Instead set {@link GibbsOptions#scansPerSample} option on this object or its
	 * corresponding model graph using {@link #setOption}.
	 */
	@Deprecated
	public void setScansPerSample(int scansPerSample)
	{
		if (scansPerSample < 1)
			throw new DimpleException("Scans per sample must be greater than 0.");
		
		setOption(GibbsOptions.scansPerSample, scansPerSample);
		_scansPerSample = scansPerSample;
		
		setUpdatesPerSampleFromScans();
	}
	
	/**
	 * Updates the value of {@link _updatesPerSample} based on {@link _scansPerSample} and
	 * the current number of variables in the graph.
	 */
	private void setUpdatesPerSampleFromScans()
	{
		if (_scansPerSample > 0)
		{
			final IGibbsSchedule schedule = _schedule;
			_updatesPerSample = _scansPerSample * (schedule != null ? schedule.size() : _factorGraph.getVariableCount());
		}
	}
	
	/**
	 * @deprecated This method will be removed in a future release.
	 */
	@Deprecated
	public int getBurnInUpdates()
	{
		return _burnInUpdates;
	}
	
	/**
	 * @deprecated This method will be removed in a future release.
	 */
	@Deprecated
	public void setBurnInUpdates(int burnInUpdates)
	{
		// TODO: when this method is removed, change the range of burnInScans to [0,max]
		_burnInUpdates = burnInUpdates;
		_burnInScans = -1;		// Burn-in specified in updates rather than scans
		setOption(GibbsOptions.burnInScans, -1);
	}
	
	// Set the number of scans for burn-in as an alternative means of specifying the burn-in period
	/**
	 * Sets the number of updates of all of the variables to perform during the burn-in period.
	 * <p>
	 * This simply sets the value of the {@link GibbsOptions#burnInScans} option on this object.
	 * <p>
	 * @param burnInScans is a non-negative number.
	 * @deprecated Instead set {@link GibbsOptions#burnInScans} option on this object or its corresponding
	 * model graph using {@link #setOption}.
	 */
	@Deprecated
	public void setBurnInScans(int burnInScans)
	{
		setOption(GibbsOptions.burnInScans, burnInScans);
		_burnInScans = burnInScans;
		
		setBurnInUpdatesFromScans();
	}

	/**
	 * Updates the value of {@link _burnInUpdates} based on {@link _burnInScans} and
	 * the current number of variables in the graph.
	 */
	private void setBurnInUpdatesFromScans()
	{
		if (_burnInScans > 0)
		{
			final IGibbsSchedule schedule = _schedule;
			_burnInUpdates = _burnInScans * (schedule != null ? schedule.size() : _factorGraph.getVariableCount());
		}
	}

	/**
	 * Sets number of random restarts.
	 * <p>
	 * Sets the value of {@link #getNumRestarts()} and the corresponding {@link GibbsOptions#numRandomRestarts}
	 * option.
	 * @param numRestarts must be a positive integer.
	 * @deprecated Instead set {@link GibbsOptions#numRandomRestarts} option on this object or its corresponding
	 * model graph using {@link #setOption}.
	 */
	@Deprecated
	public void setNumRestarts(int numRestarts)
	{
		setOption(GibbsOptions.numRandomRestarts, numRestarts);
		_numRandomRestarts = numRestarts;
	}
	
	/**
	 * Number of random restarts to perform during solve.
	 * <p>
	 * This is automatically set from {@link GibbsOptions#numRandomRestarts} option during
	 * {@link #initialize}.
	 */
	public int getNumRestarts()
	{
		return _numRandomRestarts;
	}
	
	// Set the default sampler for Real (and RealJoint) variables
	public void setDefaultRealSampler(String samplerName)
	{
		GibbsOptions.realSampler.convertAndSet(this, samplerName);
	}
	public String getDefaultRealSampler()
	{
		return getOptionOrDefault(GibbsOptions.realSampler).getSimpleName();
	}

	// Set the default sampler for Discrete variables
	public void setDefaultDiscreteSampler(String samplerName)
	{
		GibbsOptions.discreteSampler.convertAndSet(this, samplerName);
	}
	
	public String getDefaultDiscreteSampler()
	{
		return getOptionOrDefault(GibbsOptions.discreteSampler).getSimpleName();
	}

	/**
	 * @deprecated Instead set {@link GibbsOptions#initialTemperature} option using {@link #setOption}.
	 */
	@Deprecated
	public void setInitialTemperature(double initialTemperature)
	{
		setOption(GibbsOptions.initialTemperature, initialTemperature);
		setTempering(true);
		_initialTemperature = initialTemperature;
	}
	
	/**
	 * @deprecated Instead get {@link GibbsOptions#initialTemperature} option using {@link #getOption}.
	 */
	@Deprecated
	public double getInitialTemperature() {return _initialTemperature;}
	
	/**
	 * @deprecated Instead set {@link GibbsOptions#annealingHalfLife} option using {@link #setOption}.
	 */
	@Deprecated
	public void setTemperingHalfLifeInSamples(double temperingHalfLifeInSamples)
	{
		setOption(GibbsOptions.annealingHalfLife, temperingHalfLifeInSamples);
		setTempering(true);
		_temperingDecayConstant = 1 - LOG2/temperingHalfLifeInSamples;
	}
	
	/**
	 * @deprecated Instead get {@link GibbsOptions#annealingHalfLife} option using {@link #getOption}.
	 */
	@Deprecated
	public double getTemperingHalfLifeInSamples() {return LOG2/(1 - _temperingDecayConstant);}
	
	/**
	 * @deprecated Instead set {@link GibbsOptions#enableAnnealing} option using {@link #setOption}.
	 */
	@Deprecated
	protected void setTempering(boolean temper)
	{
		setOption(GibbsOptions.enableAnnealing, temper);
		_temper = temper;
	}

	/**
	 * @deprecated Instead set {@link GibbsOptions#enableAnnealing} option to true using {@link #setOption}.
	 */
	@Deprecated
	public final void enableTempering()
	{
		setTempering(true);
	}

	/**
	 * @deprecated Instead set {@link GibbsOptions#enableAnnealing} option to false using {@link #setOption}.
	 */
	@Deprecated
	public final void disableTempering()
	{
		setTempering(false);
	}
	
	/**
	 * @deprecated Instead get {@link GibbsOptions#enableAnnealing} option using {@link #getOption}.
	 */
	@Deprecated
	public boolean isTemperingEnabled()
	{
		return _temper;
	}
	
	// Helpers for operating on pre-specified groups of variables in the graph
	public double[] getVariableSampleValues(int variableGroupID)
	{
		ArrayList<Variable> variableList = _factorGraph.getVariableGroup(variableGroupID);
		if (variableList == null)
		{
			return ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		
		int numVariables = variableList.size();
		double[] result = new double[numVariables];
		for (int i = 0; i < numVariables; i++)
		{
			ISolverVariable var = variableList.get(i).getSolver();
			if (var instanceof GibbsDiscrete)
				result[i] = (Double)((GibbsDiscrete)var).getCurrentSample();
			else if (var instanceof GibbsReal)
				result[i] = ((GibbsReal)var).getCurrentSample();
			else
				throw new DimpleException("Invalid variable class");
		}
		return result;
	}
	public void setAndHoldVariableSampleValues(int variableGroupID, Object[] values) {setAndHoldVariableSampleValues(variableGroupID, (double[])values[0]);}	// Due to the way MATLAB passes objects
	public void setAndHoldVariableSampleValues(int variableGroupID, double[] values)
	{
		ArrayList<Variable> variableList = _factorGraph.getVariableGroup(variableGroupID);
		if (variableList != null)
		{
			int numVariables = variableList.size();
			if (numVariables != values.length) throw new DimpleException("Number of values must match the number of variables");
			for (int i = 0; i < numVariables; i++)
			{
				ISolverVariable var = variableList.get(i).getSolver();
				if (var instanceof GibbsDiscrete)
					((GibbsDiscrete)var).setAndHoldSampleValue(values[i]);
				else if (var instanceof GibbsReal)
					((GibbsReal)var).setAndHoldSampleValue(values[i]);
				else
					throw new DimpleException("Invalid variable class");
			}
		}
	}
	public void holdVariableSampleValues(int variableGroupID)
	{
		ArrayList<Variable> variableList = _factorGraph.getVariableGroup(variableGroupID);
		if (variableList != null)
		{
			int numVariables = variableList.size();
			for (int i = 0; i < numVariables; i++)
			{
				ISolverVariable var = variableList.get(i).getSolver();
				if (var instanceof GibbsDiscrete)
					((GibbsDiscrete)var).holdSampleValue();
				else if (var instanceof GibbsReal)
					((GibbsReal)var).holdSampleValue();
				else
					throw new DimpleException("Invalid variable class");
			}
		}
	}
	public void releaseVariableSampleValues(int variableGroupID)
	{
		ArrayList<Variable> variableList = _factorGraph.getVariableGroup(variableGroupID);
		if (variableList != null)
		{
			int numVariables = variableList.size();
			for (int i = 0; i < numVariables; i++)
			{
				ISolverVariable var = variableList.get(i).getSolver();
				if (var instanceof GibbsDiscrete)
					((GibbsDiscrete)var).releaseSampleValue();
				else if (var instanceof GibbsReal)
					((GibbsReal)var).releaseSampleValue();
				else
					throw new DimpleException("Invalid variable class");
			}
		}
	}
	
	// 'Iterations' are not defined for Gibbs since that term is ambiguous.  Instead, set the number of samples using setNumSamples().
	@Override
	public void setNumIterations(int numIter)
	{
		throw new DimpleException("The length of a run in the Gibbs solver is not specified by a number of 'iterations', but by the number of 'samples'");
	}
	
	@SuppressWarnings("null")
	@Override
	public void postAddFactor(Factor f)
	{
		deferDeterministicUpdates();
		
		for (int i = 0, nvars = f.getSiblingCount(); i < nvars; ++i)
		{
			getSolverVariable(f.getSibling(i)).postAddFactor(f);
		}
		
		processDeferredDeterministicUpdates();
	}
	
	@SuppressWarnings("null")
	@Override
	public void postSetSolverFactory()
	{
		deferDeterministicUpdates();

		for(Variable vb : getModel().getVariablesFlat())
		{
			getSolverVariable(vb).postAddFactor(null);
		}

		processDeferredDeterministicUpdates();
	}

	@Override
	public @Nullable String getMatlabSolveWrapper()
	{
		return null;
	}

	@Override
	public void resetEdgeMessages(int portNum) {
	}

	@Override
	public @Nullable Object getInputMsg(int portIndex) {
		return null;
	}

	@Override
	public @Nullable Object getOutputMsg(int portIndex) {
		return null;
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum)
	{
	}

	void scheduleDeterministicDirectedUpdate(ISolverFactorGibbs sfactor, int changedVariableIndex, Value oldValue)
	{
		if (_deferDeterministicFactorUpdatesCounter > 0)
		{
			if (_deferredDeterministicFactorUpdates == null)
			{
				_deferredDeterministicFactorUpdates =
					new KeyedPriorityQueue<ISolverFactorGibbs, SFactorUpdate>(11,
						SFactorUpdate.DeterministicOrder.INSTANCE);
			}
			SFactorUpdate update = requireNonNull(_deferredDeterministicFactorUpdates).get(sfactor);
			if (update == null)
			{
				update = new SFactorUpdate(sfactor);
				requireNonNull(_deferredDeterministicFactorUpdates).offer(update);
			}
			update.addVariableUpdate(changedVariableIndex, oldValue);
		}
		else
		{
			final Factor factor = requireNonNull(sfactor.getModelObject());
			final int nEdges = factor.getSiblingCount();
			IndexedValue.SingleList oldValues = null;
			if (factor.getFactorFunction().updateDeterministicLimit(nEdges) > 0)
			{
				oldValues = IndexedValue.SingleList.create(changedVariableIndex, oldValue);
			}
			deferDeterministicUpdates();
			sfactor.updateNeighborVariableValuesNow(oldValues);
			if (oldValues != null)
			{
				oldValues.release();
			}
			processDeferredDeterministicUpdates();
		}
	}
	
	public void processDeferredDeterministicUpdates()
	{
		if (--_deferDeterministicFactorUpdatesCounter <= 0)
		{
			_deferDeterministicFactorUpdatesCounter = 1;
			final KeyedPriorityQueue<ISolverFactorGibbs, SFactorUpdate> deferredUpdates =
				_deferredDeterministicFactorUpdates;
			if (deferredUpdates != null)
			{
				SFactorUpdate update = null;
				while ((update = deferredUpdates.poll()) != null)
				{
					update.performUpdate();
				}
			}
			_deferDeterministicFactorUpdatesCounter = 0;
		}
	}
	
	public void deferDeterministicUpdates()
	{
		++_deferDeterministicFactorUpdatesCounter;
	}
	
	@Override
	public boolean checkAllEdgesAreIncludedInSchedule()
	{
		return false;
	}
	
	public void addBlockInitializer(IBlockInitializer blockInitializer)
	{
		ArrayList<IBlockInitializer> blockInitializers = _blockInitializers;
		
		if (blockInitializers == null)
			blockInitializers = _blockInitializers = new ArrayList<IBlockInitializer>();

		blockInitializers.add(blockInitializer);
	}
	
	public void removeBlockInitializer(IBlockInitializer blockInitializer)
	{
		final ArrayList<IBlockInitializer> blockInitializers = _blockInitializers;

		if (blockInitializers != null)
			blockInitializers.remove(blockInitializer);
	}

	/*
	 * 
	 */
	@Override
	protected void doUpdateEdge(int edge)
	{
	}
}
