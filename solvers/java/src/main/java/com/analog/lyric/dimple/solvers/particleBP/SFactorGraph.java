package com.analog.lyric.dimple.solvers.particleBP;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.dimple.solvers.sumproduct.STableFactor;
import com.analog.lyric.dimple.solvers.sumproduct.SVariable;


public class SFactorGraph extends SFactorGraphBase 
{
	protected int _numIterationsBetweenResampling = 1;
	protected int _defaultResamplingUpdatesPerParticle = 1;
	protected int _defaultNumParticles = 1;
	protected boolean _temper = false;
	protected double _initialTemperature;
	protected double _temperingDecayConstant;
	protected double _temperature;
	protected final double LOG2 = Math.log(2);
	
	// Arguments for the constructor
	public static class Arguments
	{
		public boolean temper = false;
		public double initialTemperature = 1;
		public double temperingHalfLifeInSamples = 1;
	}
	
	protected SFactorGraph(FactorGraph factorGraph, Arguments arguments)  
	{
		super(factorGraph);
//		setNumSamples(arguments.numSamples);
//		setUpdatesPerSample(arguments.updatesPerSample);
//		setBurnInUpdates(arguments.burnInUpdates);
//		setTempering(arguments.temper);
//		configureInitialTemperature(arguments.initialTemperature);
//		configureTemperingHalfLifeInSamples(arguments.temperingHalfLifeInSamples);
//		_factorGraph.setSolverSpecificDefaultScheduler(new GibbsDefaultScheduler());	// Override the common default scheduler
	}

	@Override
	public boolean customFactorExists(String funcName) 
	{
		return false;
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
		{
			SRealVariable v = new SRealVariable(var);
			v.setNumParticles(_defaultNumParticles);
			v.setResamplingUpdatesPerParticle(_defaultResamplingUpdatesPerParticle);
			return v;
		}
		else
			return new SVariable(var);
	}

	@Override
	public void initialize() 
	{
		super.initialize();
//		_minPotential = Double.MAX_VALUE;
		if (_temper) setTemperature(_initialTemperature);
	}
	
	@Override
	public void iterate(int numIters) 
	{
		VariableList vars = _factorGraph.getVariables();
		
		int iterationsBeforeResampling = 1;
		for (int iterNum = 0; iterNum < numIters; iterNum++)
		{
			if (--iterationsBeforeResampling <= 0)
			{
				for (VariableBase v : vars)
				{
					ISolverVariable vs = v.getSolver();
					if (vs instanceof SRealVariable)
						((SRealVariable)vs).resample();	
				}
				iterationsBeforeResampling = _numIterationsBetweenResampling;
			}
			
			update();
			
			if (_temper)
			{
				_temperature *= _temperingDecayConstant;
				setTemperature(_temperature);
			}

			// Allow interruption (if the solver is run as a thread)
			// Currently interruption is allowed only between iterations, not within a single iteration
			try {Thread.sleep(0);}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}
		}

	}
	

	// Set/get the current temperature for all variables in the graph (for tempering)
	public void setTemperature(double T)
	{
		_temperature = T;
		double beta = 1/T;

		// All real factors have temperatures
		for (Factor f : _factorGraph.getNonGraphFactors())
		{
			ISolverFactor fs = f.getSolver();
			if (fs instanceof SRealFactor)
				((SRealFactor)fs).setBeta(beta);	
		}
		
		for (VariableBase v : _factorGraph.getVariables())
		{
			ISolverVariable vs = v.getSolver();
			if (vs instanceof SRealVariable)
				((SRealVariable)vs).setBeta(beta);	
		}

		
		// TODO: discrete factors could have temperatures too
	}
	public double getTemperature() {return _temperature;}
	
	// Sets the random seed for the Particle BP solver.  This allows runs of the solver to be repeatable.
	public void setSeed(long seed)
	{
		ParticleBPSolverRandomGenerator.rand.setSeed(seed);
	}
	

	// Set the number of particle values globally for all real variables
	public void setNumParticles(int numParticles)
	{
		_defaultNumParticles = numParticles;
		for (VariableBase v : _factorGraph.getVariables())
		{
			ISolverVariable vs = v.getSolver();
			if (vs instanceof SRealVariable)
				((SRealVariable)vs).setNumParticles(numParticles);	
		}
	}

	// Set the number of re-sampling updates per particle when re-sampling the particle values, globally for all real variables
	public void setResamplingUpdatesPerParticle(int updatesPerParticle)
	{
		_defaultResamplingUpdatesPerParticle = updatesPerParticle;
		for (VariableBase v : _factorGraph.getVariables())
		{
			ISolverVariable vs = v.getSolver();
			if (vs instanceof SRealVariable)
				((SRealVariable)vs).setResamplingUpdatesPerParticle(updatesPerParticle);	
		}
	}
	
	
	// Set/get the number of iterations between resamplings
	public void setNumIterationsBetweenResampling(int numIterationsBetweenResampling) {_numIterationsBetweenResampling = numIterationsBetweenResampling;}
	public int getNumIterationsBetweenResampling() {return _numIterationsBetweenResampling;}
	
	// Set/get the initial temperature when using tempering
	public void setInitialTemperature(double initialTemperature) {_temper = true; _initialTemperature = initialTemperature;}
	public double getInitialTemperature() {return _initialTemperature;}
	protected void configureInitialTemperature(double initialTemperature) {_initialTemperature = initialTemperature;}	// Don't automatically enable tempering
	
	// Set/get the tempering half-life -- the number of *samples* for the temperature to decrease by half
	public void setTemperingHalfLifeInIterations(double temperingHalfLifeInIterations) {_temper = true; _temperingDecayConstant = 1 - LOG2/temperingHalfLifeInIterations;}
	public double getTemperingHalfLifeInIterations() {return LOG2/(1 - _temperingDecayConstant);}
	protected void configureTemperingHalfLifeInIterations(double temperingHalfLifeInIterations) {_temperingDecayConstant = 1 - LOG2/temperingHalfLifeInIterations;}	// Don't automatically enable tempering
	
	// Enable or disable the use of tempering
	protected void setTempering(boolean temper) {_temper = temper;}
	public void enableTempering() {_temper = true;}
	public void disableTempering() {_temper = false;}
	public boolean isTemperingEnabled() {return _temper;}
	
}
