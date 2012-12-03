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
import java.util.Iterator;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.schedulers.GibbsDefaultScheduler;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;


public class SFactorGraph extends SFactorGraphBase //implements ISolverFactorGraph
{
	protected ISchedule _schedule;
	protected Iterator<IScheduleEntry> _scheduleIterator;
	protected int _numSamples;
	protected int _updatesPerSample;
	protected int _burnInUpdates;
	protected int _numRandomRestarts = 0;
	protected boolean _temper = false;
	protected double _initialTemperature;
	protected double _temperingDecayConstant;
	protected double _temperature;
	protected double _minPotential = Double.MAX_VALUE;
	protected boolean _firstSample = true;
	protected final double LOG2 = Math.log(2);
	
	
	// Arguments for the constructor
	public static class Arguments
	{
		public int numSamples = 1;
		public int updatesPerSample = 1;
		public int burnInUpdates = 0;
		public boolean temper = false;
		public double initialTemperature = 1;
		public double temperingHalfLifeInSamples = 1;
	}
	
	protected SFactorGraph(FactorGraph factorGraph, Arguments arguments)  
	{
		super(factorGraph);
		setNumSamples(arguments.numSamples);
		setUpdatesPerSample(arguments.updatesPerSample);
		setBurnInUpdates(arguments.burnInUpdates);
		setTempering(arguments.temper);
		configureInitialTemperature(arguments.initialTemperature);
		configureTemperingHalfLifeInSamples(arguments.temperingHalfLifeInSamples);
		_factorGraph.setSolverSpecificDefaultScheduler(new GibbsDefaultScheduler());	// Override the common default scheduler
	}


	@Override
	public ISolverFactor createFactor(Factor factor)  
	{
		if (factor.isDiscrete())
			return new STableFactor(factor);
		else
			return new SRealFactor(factor);
	}

	@Override
	public ISolverVariable createVariable(VariableBase var)  
	{
		if (var.getModelerClassName().equals("Real"))
			return new SRealVariable(var);
		else
			return new SDiscreteVariable(var);
	}

	@Override
	public void initialize() 
	{
		super.initialize();
		_schedule = _factorGraph.getSchedule();
		_scheduleIterator = _schedule.iterator();
		_minPotential = Double.POSITIVE_INFINITY;
		_firstSample = true;
		if (_temper) setTemperature(_initialTemperature);
	}
	
	@Override
	public void solve(boolean initialize) 
	{
		if (initialize)
			initialize();
		
		for (int restartCount = 0; restartCount < _numRandomRestarts + 1; restartCount++)
		{
			burnIn();
			for (int iter = 0; iter < _numSamples; iter++) 
				oneSample();
		}
	}
	
	
	public final void burnIn()
	{
		randomRestart();
		iterate(_burnInUpdates);
	}
	
	// Run more samples without initializing, burn-in, or random-restarts
	// This is like iterate, except that while iterate just updates runs a specified number of single-variable updates,
	// this runs a specified number of entire samples, where the size of a sample has already been defined in terms of
	// number of either updates or scans.
	public void sample() {sample(1);}
	public void sample(int numSamples)
	{
		for (int sample = 0; sample < numSamples; sample++) 
			oneSample();
	}

	// Note that the iterate() method for the Gibbs solver means do the
	// specified number of single-variable updates, regardless of other parameter settings.
	// The iterate() method behaves differently than for other solvers due to the fact that the
	// update() method for Gibbs-specific schedules will update only a single variable.
	// Also, multithreaded operation for Gibbs is not supported
	@Override
	public void iterate(int numIters) 
	{
		for (int iterNum = 0; iterNum < numIters; iterNum++)
		{
			if (!_scheduleIterator.hasNext())
				_scheduleIterator = _schedule.iterator();	// Wrap-around the schedule if reached the end

			_scheduleIterator.next().update();
		}
		
		// Allow interruption (if the solver is run as a thread); currently interruption is allowed only between iterations, not within a single iteration
		try {interruptCheck();}
		catch (InterruptedException e) {return;}
	}

	
	protected void oneSample() 
	{
		iterate(_updatesPerSample);
		for (VariableBase v : _factorGraph.getVariables())
		{
			ISolverVariableGibbs vs = (ISolverVariableGibbs)(v.getSolver());
			vs.updateBelief();
			vs.saveCurrentSample();		// Note that the first sample saved is one full sample after burn in, not immediately after burn in (in case the burn in is zero)
		}
		
		// Save the best sample value seen so far
		double totalPotential = getTotalPotential();
		if (totalPotential < _minPotential || _firstSample)
		{
			for (VariableBase v : _factorGraph.getVariables())
				((ISolverVariableGibbs)(v.getSolver())).saveBestSample();
			_minPotential = totalPotential;
			_firstSample = false;
		}
		
		// If tempering, reduce the temperature
		if (_temper)
		{
			_temperature *= _temperingDecayConstant;
			setTemperature(_temperature);
		}
	}
	
	public void randomRestart()
	{
		for (VariableBase v : _factorGraph.getVariables())
			((ISolverVariableGibbs)v.getSolver()).randomRestart();
		
		if (_temper) setTemperature(_initialTemperature);	// Reset the temperature, if tempering
	}
	
	

	// Get the total potential over all factors of the graph given the current sample values (including input priors on variables)
	public double getTotalPotential()
	{
		double totalPotential = 0;
		for (Factor f : _factorGraph.getNonGraphFactors())
			totalPotential += ((ISolverFactorGibbs)(f.getSolver())).getPotential();
		for (VariableBase v : _factorGraph.getVariables())		// Variables contribute too because they have inputs, which are factors
			totalPotential += ((ISolverVariableGibbs)(v.getSolver())).getPotential();
		return totalPotential;
	}
	
	// Before running, calling this method instructs the solver to save all sample values for all variables in the graph
	public void saveAllSamples() 
	{
		for (VariableBase v : _factorGraph.getVariables())
			((ISolverVariableGibbs)(v.getSolver())).saveAllSamples();		
	}
	
	// Set/get the current temperature for all variables in the graph (for tempering)
	public void setTemperature(double T)
	{
		_temperature = T;
		double beta = 1/T;
		for (VariableBase v : _factorGraph.getVariables())
			((ISolverVariableGibbs)(v.getSolver())).setBeta(beta);		
	}
	public double getTemperature() {return _temperature;}
	
	// Sets the random seed for the Gibbs solver.  This allows runs of the solver to be repeatable.
	public void setSeed(long seed)
	{
		GibbsSolverRandomGenerator.rand.setSeed(seed);
	}
	
	// Set/get the number of samples to be run when solving the graph (post burn-in)
	public void setNumSamples(int numSamples) {_numSamples = numSamples;}
	public int getNumSamples() {return _numSamples;}
	
	// Set/get the number of single-variable updates between samples
	public void setUpdatesPerSample(int updatesPerSample) {_updatesPerSample = updatesPerSample;}
	public int getUpdatesPerSample() {return _updatesPerSample;}
	
	// Set the number of scans between samples as an alternative means of specifying the sample rate
	// A scan is an update of the number of variables equal to the total number of variables in the graph
	// Note that this is relative to the number of variables in the graph at the time of this call,
	// which might be different from the number of variables in the graph when it is solved
	public void setScansPerSample(int scansPerSample) {_updatesPerSample = scansPerSample * _factorGraph.getVariables().size();}
	
	// Set/get the number of single-variable updates for the burn-in period prior to collecting samples
	public void setBurnInUpdates(int burnInUpdates) {_burnInUpdates = burnInUpdates;}
	public int getBurnInUpdates() {return _burnInUpdates;}
	
	// Set/get the number of random-restarts
	public void setNumRestarts(int numRestarts) {_numRandomRestarts = numRestarts;}
	public int getNumRestarts() {return _numRandomRestarts;}
	
	// Set the number of scans for burn-in as an alternative means of specifying the burn-in period
	// Note that this is relative to the number of variables in the graph at the time of this call,
	// which might be different from the number of variables in the graph when it is solved
	public void setBurnInScans(int burnInScans) {_burnInUpdates = burnInScans * _factorGraph.getVariables().size();}

	// Set/get the initial temperature when using tempering
	public void setInitialTemperature(double initialTemperature) {_temper = true; _initialTemperature = initialTemperature;}
	public double getInitialTemperature() {return _initialTemperature;}
	protected void configureInitialTemperature(double initialTemperature) {_initialTemperature = initialTemperature;}	// Don't automatically enable tempering
	
	// Set/get the tempering half-life -- the number of *samples* for the temperature to decrease by half
	public void setTemperingHalfLifeInSamples(double temperingHalfLifeInSamples) {_temper = true; _temperingDecayConstant = 1 - LOG2/temperingHalfLifeInSamples;}
	public double getTemperingHalfLifeInSamples() {return LOG2/(1 - _temperingDecayConstant);}
	protected void configureTemperingHalfLifeInSamples(double temperingHalfLifeInSamples) {_temperingDecayConstant = 1 - LOG2/temperingHalfLifeInSamples;}	// Don't automatically enable tempering
	
	// Enable or disable the use of tempering
	protected void setTempering(boolean temper) {_temper = temper;}
	public void enableTempering() {_temper = true;}
	public void disableTempering() {_temper = false;}
	public boolean isTemperingEnabled() {return _temper;}
	
	
	
	// Helpers for operating on pre-specified groups of variables in the graph
	public double[] getVariableSampleValues(int variableGroupID)
	{
		ArrayList<VariableBase> variableList = _factorGraph.getVariableGroup(variableGroupID);
		int numVariables = variableList.size();
		double[] result = new double[numVariables];
		for (int i = 0; i < numVariables; i++)
		{
			ISolverVariable var = variableList.get(i).getSolver();
			if (var instanceof SDiscreteVariable)
				result[i] = (Double)((SDiscreteVariable)var).getCurrentSample();
			else if (var instanceof SRealVariable)
				result[i] = ((SRealVariable)var).getCurrentSample();
			else
				throw new DimpleException("Invalid variable class");
		}
		return result;
	}
	public void setAndHoldVariableSampleValues(int variableGroupID, Object[] values) {setAndHoldVariableSampleValues(variableGroupID, (double[])values[0]);}	// Due to the way MATLAB passes objects
	public void setAndHoldVariableSampleValues(int variableGroupID, double[] values)
	{
		ArrayList<VariableBase> variableList = _factorGraph.getVariableGroup(variableGroupID);
		int numVariables = variableList.size();
		if (numVariables != values.length) throw new DimpleException("Number of values must match the number of variables");
		for (int i = 0; i < numVariables; i++)
		{
			ISolverVariable var = variableList.get(i).getSolver();
			if (var instanceof SDiscreteVariable)
				((SDiscreteVariable)var).setAndHoldSampleValue(values[i]);
			else if (var instanceof SRealVariable)
				((SRealVariable)var).setAndHoldSampleValue((Double)values[i]);
			else
				throw new DimpleException("Invalid variable class");
		}
	}
	public void holdVariableSampleValues(int variableGroupID)
	{
		ArrayList<VariableBase> variableList = _factorGraph.getVariableGroup(variableGroupID);
		int numVariables = variableList.size();
		for (int i = 0; i < numVariables; i++)
		{
			ISolverVariable var = variableList.get(i).getSolver();
			if (var instanceof SDiscreteVariable)
				((SDiscreteVariable)var).holdSampleValue();
			else if (var instanceof SRealVariable)
				((SRealVariable)var).holdSampleValue();
			else
				throw new DimpleException("Invalid variable class");
		}
	}
	public void releaseVariableSampleValues(int variableGroupID)
	{
		ArrayList<VariableBase> variableList = _factorGraph.getVariableGroup(variableGroupID);
		int numVariables = variableList.size();
		for (int i = 0; i < numVariables; i++)
		{
			ISolverVariable var = variableList.get(i).getSolver();
			if (var instanceof SDiscreteVariable)
				((SDiscreteVariable)var).releaseSampleValue();
			else if (var instanceof SRealVariable)
				((SRealVariable)var).releaseSampleValue();
			else
				throw new DimpleException("Invalid variable class");
		}
	}

	
	
	@Override
	public void setNumThreads(int numThreads)
	{
		if (numThreads != 1)
			throw new DimpleException("Multithreading is not currently supported by this solver.");
	}

	// 'Iterations' are not defined for Gibbs since that term is ambiguous.  Instead, set the number of samples using setNumSamples().
	@Override
	public void setNumIterations(int numIter) 
	{
		throw new DimpleException("The length of a run in the Gibbs solver is not specified by a number of 'iterations', but by the number of 'samples'");
	}
	
}
