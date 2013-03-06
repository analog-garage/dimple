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

import java.util.concurrent.LinkedBlockingQueue;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorBase;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FactorList;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.schedulers.dependencyGraph.DependencyGraphNode;
import com.analog.lyric.dimple.schedulers.dependencyGraph.ScheduleDependencyGraph;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.solvers.interfaces.ISolverBlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public abstract class SFactorGraphBase  extends SNode implements ISolverFactorGraph, Runnable
{
	protected FactorGraph _factorGraph;
	protected int _numIterations = 1;		// Default number of iterations unless otherwise specified

	public SFactorGraphBase(FactorGraph fg)
	{
		super(fg);
		_factorGraph = fg;
	}

	public FactorGraph getModel()
	{
		return _factorGraph;
	}

	public void moveMessages(ISolverNode other)
	{
		SFactorGraphBase sother = (SFactorGraphBase)other;
		FactorList otherFactors = sother._factorGraph.getFactorsFlat();
		FactorList myFactors = _factorGraph.getFactorsFlat();
		
		if (otherFactors.size() != myFactors.size())
			throw new DimpleException("Graphs dont' match");
		
		for (int i = 0; i < myFactors.size(); i++)
		{
			ISolverFactor sf = (ISolverFactor)myFactors.getByIndex(i).getSolver();
			sf.moveMessages(otherFactors.getByIndex(i).getSolver());
		}
		
	}


	public boolean customFactorExists(String funcName) 
	{
		return false;
	}

	public void initialize() 
	{

	}

	public void setNumIterations(int numIter) 
	{
		_numIterations = numIter;
	}
	public int getNumIterations()
	{
		return _numIterations;
	}

	public void update() 
	{
		for (IScheduleEntry entry : _factorGraph.getSchedule())
		{
			entry.update();
		}

	}
	public void updateEdge(int outPortNum) 
	{
		throw new DimpleException("Not supported");
	}

	public void iterate() 
	{
		iterate(1);
	}


	public void iterate(int numIters) 
	{
		if (_numThreads == 1)	
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
			iterateMultiThreaded(numIters);
		}
	}
	
	public void solveOneTimeStep()
	{
		iterate(_numIterations);
	}
	
	public void solve(boolean initialize) 
	{
		if (initialize)
			_factorGraph.initialize();
			
		solveOneTimeStep();
		
		int i = 0;
		int maxSteps = _factorGraph.getNumSteps();
		boolean infinite = _factorGraph.getNumStepsInfinite();
		while (getModel().hasNext())
		{
			if (!infinite && i >= maxSteps)
				break;
			getModel().advance();
			
			solveOneTimeStep();
			
			i++;
		}

	}



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
	public ISolverFactorGraph getRootGraph()
	{
		return _factorGraph.getRootGraph().getSolver();
	}

	public double getBetheFreeEnergy()
	{
		return getInternalEnergy() - getBetheEntropy();
	}
	
	@Override
	public void estimateParameters(FactorTable[] tables, int numRestarts,
			int numSteps, double stepScaleFactor) {
		throw new DimpleException("not supported by this solver");
		
	}
	
	@Override
	public void baumWelch(FactorTable [] tables,int numRestarts,int numSteps)
	{
		throw new DimpleException("not supported by this solver");		
	}

	
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

	public double getScore()
	{
		
		double energy = 0;

		for (VariableBase v : getModel().getVariablesTop())
			energy += v.getScore();

		for (FactorBase f : getModel().getFactorsTop())
			energy += f.getScore();

		return energy;
		
	}
	
	//TODO: should this live in the model?
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
	private boolean _initialize = true;		// Indicates whether to initialize when calling solve via startSolver
	private Exception _exception = null;	// For throwing exceptions back up to client when solve is running in a thread

	public void run()
	{
		try 
		{
			solve(_initialize);
		}
		catch (Exception e) 
		{
			_exception = e;					// Pass any exceptions to the main thread so they can be passed to the client
		}
	}
	public void startSolver()  {startSolver(true);}
	public void startSolver(boolean initialize) 
	{
		_initialize = initialize; 			// Let run() know whether to initialize or not
		_thread = new Thread(this);
		_thread.start();
	}
	public void interruptSolver()
	{
		if (_thread != null)
		{
			System.out.println(">>> Interrupting solver");
			_thread.interrupt();

			// See if there are any running sub-threads; in which case, interrupt those too
			if (_solverSubThreads != null)
				for (Thread thread : _solverSubThreads)
					if (thread != null)
						thread.interrupt();
		}
	}
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
	 * For multi-threaded execution
	 * 
	 ***********************************************/

	protected int _numThreads = 1;			// Default number of CPU threads to run on
	protected ScheduleDependencyGraph _scheduleDependencyGraph;
	protected LinkedBlockingQueue<DependencyGraphNode<IScheduleEntry>> _workQueue = new LinkedBlockingQueue<DependencyGraphNode<IScheduleEntry>>();
	protected int _numScheduleEntriesRemaining;
	protected Thread[] _solverSubThreads;
	protected WaitNotify _waitNotify = new WaitNotify();
	protected Exception _subThreadException = null;
	protected long _graphVersionIdWhenLastBuilt = -1;
	protected long _scheduleVersionIdWhenLastBuilt = -1;
	protected long _iterationsWhenLastBuilt = -1;

	public int getNumThreads()
	{
		return _numThreads;
	}

	public void setNumThreads(int numThreads)
	{
		_numThreads = numThreads;
	}

	// When running with multiple threads, prepares the dependency graph ahead of calling solve or iterate
	// Primarily for testing: allows testing execution time of solve or iterate without including the time to create the dependency graph
	// When called with no arguments, uses the current _numIterations (used by solve); otherwise can specify a number of iterations (for using iterate)
	public void prepareForMultiThreading()  {prepareForMultiThreading(_numIterations);}
	public void prepareForMultiThreading(int numIters) 
	{
		// Create a schedule dependency graph, but only if necessary
		if (
				(_scheduleDependencyGraph == null) ||
				!_factorGraph.isUpToDateSchedulePresent() ||
				(_graphVersionIdWhenLastBuilt != _factorGraph.getVersionId()) ||
				(_scheduleVersionIdWhenLastBuilt != _factorGraph.getScheduleVersionId()) ||
				(_iterationsWhenLastBuilt != numIters))
		{
			_scheduleDependencyGraph = new ScheduleDependencyGraph(_factorGraph, numIters);
			_graphVersionIdWhenLastBuilt = _factorGraph.getVersionId();
			_scheduleVersionIdWhenLastBuilt = _factorGraph.getScheduleVersionId();
			_iterationsWhenLastBuilt = numIters;
		}
	}


	// Run iterations using multi-threading (called by iterate if multi-threading is in use)
	protected void iterateMultiThreaded(int numIters) 
	{
		prepareForMultiThreading(numIters);	// Create _scheduleDependencyGraph if not already up-to-date
		_numScheduleEntriesRemaining = _scheduleDependencyGraph.size();
		_scheduleDependencyGraph.initialize();

		// Initialize the work queue
		_workQueue.clear();
		for (DependencyGraphNode<IScheduleEntry> entry : _scheduleDependencyGraph.getRootList())	// Start with the roots
			_workQueue.add(entry);

		// Create and start the threads
		_solverSubThreads = new Thread[_numThreads];
		for (int i = 0; i < _numThreads; i++)
		{
			_solverSubThreads[i] = new Thread(new SFactorGraphThread(this));
			_solverSubThreads[i].start();
		}

		// Wait for notification of completion, then stop all the sub-threads
		_waitNotify.doWait();
		for (Thread thread : _solverSubThreads)
			thread.interrupt();

		// If there was an exception in any of the sub-threads, pass it along
		if (_subThreadException != null)
			throw new DimpleException(_subThreadException.getMessage());
	}

	// Callback from the threads; must be synchronized
	protected synchronized void scheduleEntryCompleted(DependencyGraphNode<IScheduleEntry> entry)
	{
		// Mark this entry completed
		entry.markCompleted();

		// Are we done?
		if (--_numScheduleEntriesRemaining == 0)
		{
			_waitNotify.doNotify();		// Notify the parent thread that we're done
			return;
		}

		// Run through the dependents of this node and see if they are ready to add to the work queue
		for (DependencyGraphNode<IScheduleEntry> dependentEntry : entry.getDependents())
			if (dependentEntry.allDependenciesMet())	// If all dependencies have now been met, the add it to the work queue
				_workQueue.add(dependentEntry);
	}


	// Get the work queue
	protected LinkedBlockingQueue<DependencyGraphNode<IScheduleEntry>> getWorkQueue()
	{
		return _workQueue;
	}


	// This is the class defining the sub-threads
	private class SFactorGraphThread implements Runnable
	{
		SFactorGraphBase _parentGraph;

		public SFactorGraphThread(SFactorGraphBase graph)
		{
			_parentGraph = graph;
		}

		@Override
		public void run()
		{
			LinkedBlockingQueue<DependencyGraphNode<IScheduleEntry>> workQueue = _parentGraph.getWorkQueue();
			try
			{
				while(true)
				{
					DependencyGraphNode<IScheduleEntry> entry = workQueue.take();	// Pull the next entry from the work queue (blocking if there are none)
					IScheduleEntry scheduleEntry = entry.getObject();
					INode node = (scheduleEntry instanceof NodeScheduleEntry) ? ((NodeScheduleEntry)scheduleEntry).getNode() : ((EdgeScheduleEntry)scheduleEntry).getNode();
					synchronized (node)												// Synchronize on the node: don't allow updating the same node in more than one thread at the same time
					{
						scheduleEntry.update();										// Run it
					}
					_parentGraph.scheduleEntryCompleted(entry);						// Tell the main thread that it's done
				}
			}
			catch (InterruptedException e) {return;}
			catch (Exception e)
			{
				_subThreadException = e;
				_waitNotify.doNotify();		// Notify the parent thread that we're done (not really done, but it's an exception)
				Thread.currentThread().interrupt();
				return;
			}
		}
	}


	// Notifier, to notify from the thread that completes the final entry to the main task to tell it that we're done
	// Adapted from http://tutorials.jenkov.com/java-concurrency/thread-signaling.html
	protected class MonitorObject {}
	protected class WaitNotify
	{
		MonitorObject _monitorObject = new MonitorObject();
		boolean _wasSignalled = false;

		public void doWait() 
		{
			synchronized(_monitorObject)
			{
				while(!_wasSignalled)
				{
					try{_monitorObject.wait();}
					catch(InterruptedException e){throw new DimpleException(e.getMessage());}
				}
				_wasSignalled = false;
			}
		}

		public void doNotify()
		{
			synchronized (_monitorObject)
			{
				_wasSignalled = true;
				_monitorObject.notify();
			}
		}
	}

	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPortNum) 
	{
		throw new DimpleException("Not supported");
		
	}
	
	@Override
	public void initializeEdge(int portNum)
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
	
}
