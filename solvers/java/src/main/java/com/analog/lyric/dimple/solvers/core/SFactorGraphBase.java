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


import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.factors.FactorList;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.interfaces.ISolverBlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class SFactorGraphBase  extends SNode implements ISolverFactorGraph
{
	protected FactorGraph _factorGraph;
	protected int _numIterations = 1;		// Default number of iterations unless otherwise specified
	private MultiThreadingManager _multithreader; // = new MultiThreadingManager();
	private boolean _useMultithreading = false;

	public SFactorGraphBase(FactorGraph fg)
	{
		super(fg);
		_factorGraph = fg;
	}

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
	 * The default implementation simply returns {@link VariableBase#getSolver()}, which
	 * assumes that the {@code variable}'s model is currenty attached to this solver graph.
	 * Subclasses may override this to return a more precise type or to support solvers that
	 * can still be used when they are detached from the model.
	 */
	@Override
	public ISolverVariable getSolverVariable(VariableBase variable)
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
			ISolverFactor sf = myFactors.getByIndex(i).getSolver();
			sf.moveMessages(otherFactors.getByIndex(i).getSolver());
		}
		
		VariableList myVars = _factorGraph.getVariablesFlat();
		VariableList otherVars = sother._factorGraph.getVariablesFlat();
		
		for (int i = 0; i < myVars.size(); i++)
		{
			ISolverVariable sv = myVars.getByIndex(i).getSolver();
			sv.moveNonEdgeSpecificState(otherVars.getByIndex(i).getSolver());
		}
		
	}


	@Override
	public boolean customFactorExists(String funcName)
	{
		return false;
	}


	@Override
	public void setNumIterations(int numIter)
	{
		_numIterations = numIter;
	}
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

	public void iterate()
	{
		iterate(1);
	}


	@Override
	public void iterate(int numIters)
	{
		if (_multithreader == null || ! _useMultithreading)
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
			_multithreader.iterate(numIters);
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
	public ISolverFactorGraph getParentGraph()
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
	public ISolverFactorGraph getRootGraph()
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
		
		//Sum up factor internal energy
		for (Factor f : _factorGraph.getFactorsFlat())
			sum += f.getBetheEntropy();
		
		//The following would be unnecessary if we implemented inputs as single node factors
		for (VariableBase v : _factorGraph.getVariablesFlat())
			sum -= v.getBetheEntropy() * (v.getFactors().length - 1);
		
		return sum;
	}

	@Override
	public double getScore()
	{
		
		double energy = 0;

		for (VariableBase v : getModel().getVariablesTop())
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
		for (VariableBase v : _factorGraph.getVariablesFlat())
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

	// For running as a thread, which allows the solver to be interrupted.
	// This is backward compatible with versions of the modeler that call solve() directly.
	private Thread _thread;
	private Exception _exception = null;	// For throwing exceptions back up to client when solve is running in a thread

	@Override
	public void startContinueSolve()
	{
		_thread = new Thread(new Runnable()
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
		_thread.start();
	}

	@Override
	public void startSolveOneStep()
	{
		_thread = new Thread(new Runnable()
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
		_thread.start();
	}
	
	@Override
	public void startSolver()
	{
		_thread = new Thread(new Runnable()
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
		_thread.start();
	}
	@Override
	public void interruptSolver()
	{
		if (_thread != null)
		{
			System.out.println(">>> Interrupting solver");
			_thread.interrupt();

		}
	}
	@Override
	public boolean isSolverRunning()
	{
		if (_exception != null)
		{
			Exception e = _exception;
			_exception = null;				// Clear the exception; the exception should happen only once; no exception if this is called again
			throw new DimpleException(e);						// Pass the exception up to the client
		}
		else if (_thread != null)
			return _thread.isAlive();
		else
			return false;
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

	public void useMultithreading(boolean use)
	{
		if (_multithreader == null)
			throw new DimpleException("Multithreading is not currently supported by this solver.");		
		else
			_useMultithreading = true;
	}
	
	public boolean useMultithreading()
	{
		return _useMultithreading;
	}
	
	
	public MultiThreadingManager getMultithreadingManager()
	{
		if (_multithreader == null)
			throw new DimpleException("Multithreading is not currently supported by this solver.");		
		else
			return _multithreader;
	} 	 	
	
	protected void setMultithreadingManager(MultiThreadingManager manager)
	{
		_multithreader = manager;
	}



	/***********************************************
	 * 
	 * Stuff for rolled up graphs
	 * 
	 ***********************************************/

	@Override
	public void initialize()
	{
		
	}

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
	public Object getInputMsg(int portIndex)
	{
		throw new DimpleException("Not supported by " + this);
	}

	@Override
	public Object getOutputMsg(int portIndex) {
		throw new DimpleException("Not supported by " + this);
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
	public String getMatlabSolveWrapper()
	{
		return null;
	}


	

}
