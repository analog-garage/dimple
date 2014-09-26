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

package com.analog.lyric.dimple.solvers.particleBP;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.dimple.solvers.sumproduct.SDiscreteVariable;
import com.analog.lyric.dimple.solvers.sumproduct.STableFactor;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductTableFactor;
import com.analog.lyric.math.DimpleRandomGenerator;

/**
 * Solver-specific factor graph for Particle BP solver.
 * <p>
 * <em>Previously was com.analog.lyric.dimple.solvers.particleBP.SFactorGraph</em>
 *  <p>
 * @since 0.07
 */
@SuppressWarnings("deprecation") // TODO remove when SDiscreteVariable removed
public class ParticleBPSolverGraph extends SFactorGraphBase
{
	protected int _numIterationsBetweenResampling = 1;
//	protected int _defaultNumParticles = 1;
	protected boolean _temper = false;
	protected double _initialTemperature;
	protected double _temperingDecayConstant;
	protected double _temperature;
	protected final double LOG2 = Math.log(2);
	
	protected ParticleBPSolverGraph(FactorGraph factorGraph)
	{
		super(factorGraph);
	}

	@Override
	public boolean customFactorExists(String funcName)
	{
		return false;
	}


	@SuppressWarnings("deprecation") // TODO remove when S*Factor classes removed.
	@Override
	public ISolverFactor createFactor(Factor factor)
	{
		if (factor.isDiscrete())
			return new STableFactor(factor);
		else
			return new SRealFactor(factor);
	}
	

	@SuppressWarnings("deprecation") // TODO remove when S*Variable classes removed.
	@Override
	public ISolverVariable createVariable(Variable var)
	{
		if (var.getModelerClassName().equals("Real"))
		{
			ParticleBPReal v = new SRealVariable(var);
//			v.setNumParticles(_defaultNumParticles);
			return v;
		}
		else
			return new SDiscreteVariable(var);
	}

	@Override
	public void initialize()
	{
		_temper = getOptionOrDefault(ParticleBPOptions.enableAnnealing);
		_initialTemperature = getOptionOrDefault(ParticleBPOptions.initialTemperature);
		_numIterationsBetweenResampling = getOptionOrDefault(ParticleBPOptions.iterationsBetweenResampling);
		_temperingDecayConstant = 1 - LOG2/getOptionOrDefault(ParticleBPOptions.annealingHalfLife);
		
		super.initialize();

		if (_temper) setTemperature(_initialTemperature);
		
		for (Factor f : getModelObject().getFactors())
		{
			ISolverFactor sf = f.getSolver();
			if (sf instanceof SumProductTableFactor)
			{
				SumProductTableFactor tf = (SumProductTableFactor)sf;
				tf.setupTableFactorEngine();
			}
		}
		
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
				for (Variable v : vars)
				{
					ISolverVariable vs = v.getSolver();
					if (vs instanceof ParticleBPReal)
						((ParticleBPReal)vs).resample();
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
			if (fs instanceof ParticleBPRealFactor)
				((ParticleBPRealFactor)fs).setBeta(beta);
		}
		
		for (Variable v : _factorGraph.getVariables())
		{
			ISolverVariable vs = v.getSolver();
			if (vs instanceof ParticleBPReal)
				((ParticleBPReal)vs).setBeta(beta);
		}

		
		// TODO: discrete factors could have temperatures too
	}
	public double getTemperature() {return _temperature;}
	
	// Sets the random seed for the Particle BP solver.  This allows runs of the solver to be repeatable.
	public void setSeed(long seed)
	{
		DimpleRandomGenerator.setSeed(seed);
	}
	

	// Set the number of particle values globally for all real variables
	public void setNumParticles(int numParticles)
	{
		setOption(ParticleBPOptions.numParticles, numParticles);
	}

	// Set the number of re-sampling updates per particle when re-sampling the particle values, globally for all real variables
	public void setResamplingUpdatesPerParticle(int updatesPerParticle)
	{
		setOption(ParticleBPOptions.resamplingUpdatesPerParticle, updatesPerParticle);
	}
	
	
	// Set/get the number of iterations between resamplings
	public void setNumIterationsBetweenResampling(int numIterationsBetweenResampling)
	{
		setOption(ParticleBPOptions.iterationsBetweenResampling, numIterationsBetweenResampling);
		_numIterationsBetweenResampling = numIterationsBetweenResampling;
	}
	
	public int getNumIterationsBetweenResampling() {return _numIterationsBetweenResampling;}
	
	/**
	 * @deprecated Instead set {@link ParticleBPOptions#initialTemperature} option using {@link #setOption}.
	 */
	@Deprecated
	public void setInitialTemperature(double initialTemperature)
	{
		setOption(ParticleBPOptions.initialTemperature, initialTemperature);
		setTempering(true);
		_initialTemperature = initialTemperature;
	}
	
	/**
	 * @deprecated Instead get {@link ParticleBPOptions#initialTemperature} option using {@link #getOption}.
	 */
	@Deprecated
	public double getInitialTemperature() {return _initialTemperature;}
	
	/**
	 * @deprecated Instead set {@link ParticleBPOptions#annealingHalfLife} option using {@link #setOption}.
	 */
	@Deprecated
	public void setTemperingHalfLifeInIterations(double temperingHalfLifeInIterations)
	{
		setOption(ParticleBPOptions.annealingHalfLife, temperingHalfLifeInIterations);
		setTempering(true);
		_temperingDecayConstant = 1 - LOG2/temperingHalfLifeInIterations;
	}
	
	/**
	 * @deprecated Instead get {@link ParticleBPOptions#annealingHalfLife} option using {@link #getOption}.
	 */
	@Deprecated
	public double getTemperingHalfLifeInIterations() {return LOG2/(1 - _temperingDecayConstant);}
	
	/**
	 * @deprecated Instead set {@link ParticleBPOptions#enableAnnealing} option using {@link #setOption}.
	 */
	@Deprecated
	protected void setTempering(boolean temper)
	{
		setOption(ParticleBPOptions.enableAnnealing, temper);
		_temper = temper;
	}

	/**
	 * @deprecated Instead set {@link ParticleBPOptions#enableAnnealing} option to true using {@link #setOption}.
	 */
	@Deprecated
	public final void enableTempering()
	{
		setTempering(true);
	}

	/**
	 * @deprecated Instead set {@link ParticleBPOptions#enableAnnealing} option to false using {@link #setOption}.
	 */
	@Deprecated
	public final void disableTempering()
	{
		setTempering(false);
	}
	
	/**
	 * @deprecated Instead get {@link ParticleBPOptions#enableAnnealing} option using {@link #getOption}.
	 */
	@Deprecated
	public boolean isTemperingEnabled()
	{
		return _temper;
	}

	/*
	 * 
	 */
	@Override
	protected void doUpdateEdge(int edge)
	{
	}
	
}
