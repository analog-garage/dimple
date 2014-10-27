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

package com.analog.lyric.dimple.solvers.core;


import static java.util.Objects.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleThread;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.factors.FactorList;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.options.SolverOptions;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverBlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class SFactorGraphBase  extends SNode implements ISolverFactorGraph
{
	protected FactorGraph _factorGraph;
	protected int _numIterations = 1;		// Default number of iterations unless otherwise specified
	private @Nullable MultiThreadingManager _multithreader; // = new MultiThreadingManager();
	private boolean _useMultithreading = false;

	/*--------------
	 * Construction
	 */
	
	public SFactorGraphBase(FactorGraph fg)
	{
		super(fg);
		_factorGraph = fg;
	}

	/*----------------------------
	 * ISolverEventSource methods
	 */
	
	@Override
	public SFactorGraphBase getContainingSolverGraph()
	{
		return this;
	}
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	public FactorGraph getModel()
	{
		return _factorGraph;
	}
	
	@Override
	public FactorGraph getModelObject()
	{
		return _factorGraph;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Default implementation simply uses {@code factory} to generate a new solver graph.
	 */
	@Override
	public @Nullable ISolverFactorGraph createSubGraph(FactorGraph subgraph, @Nullable IFactorGraphFactory<?> factory)
	{
		return factory != null ? factory.createFactorGraph(subgraph) : null;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation simply returns {@link Factor#getSolver()}, which
	 * assumes that the {@code factor}'s model is currently attached to this solver graph.
	 * Subclasses may override this to return a more precise type or to support solvers that
	 * can still be used when they are detached from the model.
	 */
	@Override
	public @Nullable ISolverFactor getSolverFactor(Factor factor)
	{
		return factor.getSolver();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation simply returns {@link Variable#getSolver()}, which
	 * assumes that the {@code variable}'s model is currently attached to this solver graph.
	 * Subclasses may override this to return a more precise type or to support solvers that
	 * can still be used when they are detached from the model.
	 */
	@Override
	public @Nullable ISolverVariable getSolverVariable(Variable variable)
	{
		return variable.getSolver();
	}
	
	@Override
	public void moveMessages(ISolverNode other)
	{
		SFactorGraphBase sother = (SFactorGraphBase)other;
		FactorList otherFactors = sother._factorGraph.getFactorsFlat();
		FactorList myFactors = _factorGraph.getFactorsFlat();
		
		if (otherFactors.size() != myFactors.size())
			throw new DimpleException("Graphs dont' match");
		
		for (int i = 0; i < myFactors.size(); i++)
		{
			ISolverFactor sf = requireNonNull(myFactors.getByIndex(i).getSolver());
			sf.moveMessages(Objects.requireNonNull(otherFactors.getByIndex(i).getSolver()));
		}
		
		VariableList myVars = _factorGraph.getVariablesFlat();
		VariableList otherVars = sother._factorGraph.getVariablesFlat();
		
		for (int i = 0; i < myVars.size(); i++)
		{
			ISolverVariable sv = requireNonNull(myVars.getByIndex(i).getSolver());
			sv.moveNonEdgeSpecificState(Objects.requireNonNull(otherVars.getByIndex(i).getSolver()));
		}
		
	}


	@Override
	public boolean customFactorExists(String funcName)
	{
		return false;
	}


	/**
	 * Sets number of solver iterations.
	 * <p>
	 * Sets {@link #getNumIterations()} and {@link BPOptions#iterations} option
	 * to specified value.
	 */
	@Override
	public void setNumIterations(int numIter)
	{
		setOption(BPOptions.iterations, numIter);
		_numIterations = numIter;
	}
	
	/**
	 * Number of solver iterations
	 * <p>
	 * This is set from {@link BPOptions#iterations} during {@link #initialize}.
	 * <p>
	 * This value is not meaningful to all solvers.
	 */
	@Override
	public int getNumIterations()
	{
		return _numIterations;
	}

	@Override
	public void update()
	{
		for (IScheduleEntry entry : _factorGraph.getSchedule())
		{
			entry.update();
		}

	}
	@Override
	public void updateEdge(int outPortNum)
	{
		throw new DimpleException("Not supported");
	}

	@Override
	public void iterate()
	{
		iterate(1);
	}


	@Override
	public void iterate(int numIters)
	{
		final MultiThreadingManager multithreader = _multithreader;
		if (multithreader == null || ! _useMultithreading)
		{
			// *** Single thread
			for (int iterNum = 0; iterNum < numIters; iterNum++)
			{
				update();
				
				// Allow interruption (if the solver is run as a thread); currently interruption is allowed only between iterations, not within a single iteration
				if (Thread.interrupted())
					return;
			}
		}
		else
		{
			// *** Multiple threads
			multithreader.iterate(numIters);
		}
	}
	
	@Override
	public void solveOneStep()
	{
		iterate(_numIterations);
	}
	
	
	@Override
	public void solve()
	{
			
		_factorGraph.initialize();
		
		solveOneStep();
		continueSolve();

	}

	@Override
	public void continueSolve()
	{
		
		int i = 0;
		int maxSteps = _factorGraph.getNumSteps();
		boolean infinite = _factorGraph.getNumStepsInfinite();
		
		while (getModel().hasNext())
		{
			if (!infinite && i >= maxSteps)
				break;
			
			getModel().advance();
			solveOneStep();
			
			i++;
		}
	}


	@Override
	public @Nullable ISolverFactorGraph getParentGraph()
	{
		ISolverFactorGraph graph = null;
		FactorGraph mgraph = _factorGraph.getParentGraph();
		if(mgraph != null)
		{
			graph = mgraph.getSolver();
		}
		return graph;
	}
	@Override
	public @Nullable ISolverFactorGraph getRootGraph()
	{
		return _factorGraph.getRootGraph().getSolver();
	}

	@Override
	public double getBetheFreeEnergy()
	{
		return getInternalEnergy() - getBetheEntropy();
	}
	
	@Override
	public void estimateParameters(IFactorTable[] tables, int numRestarts,
			int numSteps, double stepScaleFactor) {
		throw new DimpleException("not supported by this solver");
		
	}
	
	@Override
	public void baumWelch(IFactorTable [] tables,int numRestarts,int numSteps)
	{
		throw new DimpleException("not supported by this solver");
	}

	
	@Override
	public double getBetheEntropy()
	{
		double sum = 0;
		
		// Sum up factor entropy
		for (Factor f : _factorGraph.getFactorsFlat())
			sum += f.getBetheEntropy();
		
		// The following would be unnecessary if we implemented inputs as single node factors
		for (Variable v : _factorGraph.getVariablesFlat())
			sum -= v.getBetheEntropy() * (v.getSiblingCount() - 1);
		
		return sum;
	}

	@Override
	public double getScore()
	{
		
		double energy = 0;

		// FIXME: get*Top() methods copy all the objects into a new collection.
		// That should not be necessary.
		
		for (Variable v : getModel().getVariablesTop())
			energy += v.getScore();

		for (FactorBase f : getModel().getFactorsTop())
			energy += f.getScore();

		return energy;
		
	}
	
	@Override
	public double getInternalEnergy()
	{
		double sum = 0;
		
		//Sum up factor internal energy
		for (Factor f : _factorGraph.getFactorsFlat())
			sum += f.getInternalEnergy();
		
		//The following would be unnecessary if we implemented inputs as single node factors
		for (Variable v : _factorGraph.getVariablesFlat())
			sum += v.getInternalEnergy();
		
		return sum;
	}


	@Override
	public ISolverBlastFromThePastFactor createBlastFromThePast(BlastFromThePastFactor f)
	{
		return new SBlastFromThePast(f);
	}
	
	/***********************************************
	 * 
	 * Threading for Ctrl+C
	 * 
	 ***********************************************/

	// FIXME: this is not really thread safe! There is nothing to prevent you from calling
	// these methods before the previous thread is done.
	
	// For running as a thread, which allows the solver to be interrupted.
	// This is backward compatible with versions of the modeler that call solve() directly.
	private volatile @Nullable Thread _thread;
	private @Nullable Exception _exception = null;	// For throwing exceptions back up to client when solve is running in a thread

	@Override
	public void startContinueSolve()
	{
		final Thread thread = _thread = new DimpleThread(new Runnable()
		{

			@Override
			public void run() {
				try
				{
					continueSolve();
				}
				catch (Exception e)
				{
					_exception = e;					// Pass any exceptions to the main thread so they can be passed to the client
				}
			}
			
		}
		);
		thread.start();
	}

	@Override
	public void startSolveOneStep()
	{
		final Thread thread = _thread = new DimpleThread(new Runnable()
		{

			@Override
			public void run() {
				try
				{
					solveOneStep();
				}
				catch (Exception e)
				{
					_exception = e;					// Pass any exceptions to the main thread so they can be passed to the client
				}
			}
			
		}
		);
		thread.start();
	}
	
	@Override
	public void startSolver()
	{
		final Thread thread = _thread = new DimpleThread(new Runnable()
		{

			@Override
			public void run() {
				try
				{
					solve();
				}
				catch (Exception e)
				{
					_exception = e;					// Pass any exceptions to the main thread so they can be passed to the client
				}
			}
			
		}
		);
		thread.start();
	}
	@Override
	public void interruptSolver()
	{
		final Thread thread = _thread;
		if (thread != null)
		{
			System.out.println(">>> Interrupting solver");
			thread.interrupt();
		}
	}
	@Override
	public boolean isSolverRunning()
	{
		final Exception e = _exception;
		if (e != null)
		{
			_exception = null;				// Clear the exception; the exception should happen only once; no exception if this is called again
			throw new DimpleException(e);						// Pass the exception up to the client
		}
		else
		{
			final Thread thread = _thread;
			if (thread != null)
				return thread.isAlive();
			else
				return false;
		}
	}

	// Allow interruption (if the solver is run as a thread)
	protected void interruptCheck() throws InterruptedException
	{
		try {Thread.sleep(0);}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw e;
		}
	}




	/***********************************************
	 * 
	 * For multi-threaded computation
	 * 
	 ***********************************************/

	@Override
	public void useMultithreading(boolean use)
	{
		if (_multithreader == null)
			throw new DimpleException("Multithreading is not currently supported by this solver.");
		else
			_useMultithreading = use;
		setOption(SolverOptions.enableMultithreading, use);
	}
	
	@Override
	public boolean useMultithreading()
	{
		return _useMultithreading;
	}
	
	
	public MultiThreadingManager getMultithreadingManager()
	{
		final MultiThreadingManager multithreader = _multithreader;
		if (multithreader == null)
			throw new DimpleException("Multithreading is not currently supported by this solver.");
		else
			return multithreader;
	}
	
	protected void setMultithreadingManager(@Nullable MultiThreadingManager manager)
	{
		_multithreader = manager;
	}

	/***********************************************
	 * 
	 * Initialization methods
	 * 
	 ***********************************************/

	/**
	 * Initialize solver graph.
	 * <p>
	 * Default implementation does the following:
	 * <ul>
	 * <li>Initializes {@linkplain #getNumIterations() iterations} and multithreading from options.
	 * <li>Invokes {@linkplain ISolverNode#initialize() initialize} on contents of graph in this order
	 * <ol>
	 * <li>owned solver variables
	 * <li>boundary solver variables (only if this is the root solver graph)
	 * <li>solver factors
	 * <li>solver subgraphs
	 * </ol>
	 * </ul>
	 */
	@Override
	public void initialize()
	{
		_numIterations = getOptionOrDefault(BPOptions.iterations);
		_useMultithreading = getOptionOrDefault(SolverOptions.enableMultithreading);
		
		FactorGraph fg = _factorGraph;
		for (int i = 0, end = fg.getOwnedVariableCount(); i < end; ++i)
		{
			fg.getOwnedVariable(i).requireSolver("initialize").initialize();
		}
		if (!fg.hasParentGraph())
		{
			for (int i = 0, end = fg.getBoundaryVariableCount(); i <end; ++i)
			{
				fg.getBoundaryVariable(i).requireSolver("initialize").initialize();
			}
		}
		for (Factor f : fg.getNonGraphFactorsTop())
			f.requireSolver("initialize").initialize();
		for (FactorGraph g : fg.getNestedGraphs())
			g.requireSolver("initialize").initialize();
	}
	
	/***********************************************
	 * 
	 * Stuff for rolled up graphs
	 * 
	 ***********************************************/

	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPortNum)
	{
		throw new DimpleException("Not supported");
		
	}
	
	@Override
	public void resetEdgeMessages(int portNum)
	{
		throw new DimpleException("Not supported");
	}

	@Override
	public @Nullable Object getInputMsg(int portIndex)
	{
		return null;
	}

	@Override
	public @Nullable Object getOutputMsg(int portIndex)
	{
		return null;
	}
	
	@Override
	public void setInputMsg(int portIndex, Object obj) {
		throw new DimpleException("Not supported by " + this);
	}
	
	@Override
	public void postAdvance()
	{
		
	}
	@Override
	public void postAddFactor(Factor f)
	{
		
	}
	
	@Override
	public void postSetSolverFactory()
	{
		
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * The default implementation always returns null.
	 */
	@Override
	public @Nullable String getMatlabSolveWrapper()
	{
		return null;
	}


	@Override
	public boolean checkAllEdgesAreIncludedInSchedule()
	{
		return true;	// By default assume all edges must be included unless told otherwise; TODO: should this be the default?
	}
}
