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

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorBase;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FactorList;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Node;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.schedulers.dependencyGraph.DependencyGraphNode;
import com.analog.lyric.dimple.schedulers.dependencyGraph.ScheduleDependencyGraph;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.core.multithreading.StaticDependencyGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverBlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.dimple.solvers.sumproduct.SFactorGraph;
import com.analog.lyric.util.misc.MapList;

import edu.uci.ics.jung.graph.ArchetypeGraph;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.graph.event.GraphEventType;
import edu.uci.ics.jung.utils.UserDataContainer;
import edu.uci.ics.jung.utils.UserDataContainer.CopyAction;

public abstract class SFactorGraphBase  extends SNode implements ISolverFactorGraph
{
	protected FactorGraph _factorGraph;
	protected int _numIterations = 1;		// Default number of iterations unless otherwise specified
	private MultiThreadingManager _multithreader; // = new MultiThreadingManager();

	public SFactorGraphBase(FactorGraph fg)
	{
		super(fg);
		_factorGraph = fg;
		_multithreader = new MultiThreadingManager(fg);
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
	public void initialize()
	{

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
		if (_multithreader.getNumThreads() == 1)
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
	
	//TODO: should this live in the model?
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

			//TODO:
//			// See if there are any running sub-threads; in which case, interrupt those too
//			if (_solverSubThreads != null)
//				for (Thread thread : _solverSubThreads)
//					if (thread != null)
//						thread.interrupt();
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
	 * For multi-threaded execution
	 * 
	 ***********************************************/

	//protected int _numThreads = 1;			// Default number of CPU threads to run on

	public int getNumThreads()
	{
		return _multithreader.getNumThreads();
	}


	
	
//	// When running with multiple threads, prepares the dependency graph ahead of calling solve or iterate
//	// Primarily for testing: allows testing execution time of solve or iterate without including the time to create the dependency graph
//	// When called with no arguments, uses the current _numIterations (used by solve); otherwise can specify a number of iterations (for using iterate)
//	public void prepareForMultiThreading()  {prepareForMultiThreading(_numIterations);}
// 	public void prepareForMultiThreading(int numIters)
//	{
//		// Create a schedule dependency graph, but only if necessary
//		if (
//				(_scheduleDependencyGraph == null) ||
//				!_factorGraph.isUpToDateSchedulePresent() ||
//				(_graphVersionIdWhenLastBuilt != _factorGraph.getVersionId()) ||
//				(_scheduleVersionIdWhenLastBuilt != _factorGraph.getScheduleVersionId()) ||
//				(_iterationsWhenLastBuilt != numIters))
//		{
//			_scheduleDependencyGraph = new ScheduleDependencyGraph(_factorGraph, numIters);
//			_graphVersionIdWhenLastBuilt = _factorGraph.getVersionId();
//			_scheduleVersionIdWhenLastBuilt = _factorGraph.getScheduleVersionId();
//			_iterationsWhenLastBuilt = numIters;
//		}
//	}
 	
	//TODO: put this somewhere else
 	public void saveDependencyGraph(String fileName)
 	{
 		StaticDependencyGraph dg = new StaticDependencyGraph(getModelObject());
 		dg.createDotFile(fileName);
 	}
 	
//
// 	protected void iterateMultiThreaded3(int numIters)
// 	{
// 		NewDependencyGraph dg = getDependencyGraph();
// 		
// 		for (int j = 0; j < numIters; j++)
// 		{
//	 		
//	 		//protected LinkedBlockingQueue<DependencyGraphNode<IScheduleEntry>> _workQueue = new LinkedBlockingQueue<DependencyGraphNode<IScheduleEntry>>();
//	 		LinkedBlockingQueue<NewDependencyGraphNode> workQueue = new LinkedBlockingQueue<SFactorGraphBase.NewDependencyGraphNode>();
//	 		for (NewDependencyGraphNode dgn : dg.getInitialEntries())
//	 		{
//	 			workQueue.add(dgn);
//	 		}
//	 		
//	 		int numThreads = getNumThreads();
//	 		
//			ArrayList<Callable<Object>> workers = new ArrayList<Callable<Object>>();
//	
//			AtomicInteger nodesLeft = new AtomicInteger(dg.getNumNodes());
//	 		for (int i = 0; i < numThreads; i++)
//	 		{
//	 			
//	 			workers.add(new SFactorGraphThread2(workQueue, dg.getNumNodes(), nodesLeft));
//	 		}
//	 		
//	 		try {
//				_service.invokeAll(workers);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
// 		}
// 	}
// 	
//
//	// Run iterations using multi-threading (called by iterate if multi-threading is in use)
//	protected void iterateMultiThreaded2(int numIters)
//	{
//		prepareForMultiThreading(numIters);	// Create _scheduleDependencyGraph if not already up-to-date
//		_numScheduleEntriesRemaining = _scheduleDependencyGraph.size();
//		_scheduleDependencyGraph.initialize();
//
//		// Initialize the work queue
//		_workQueue.clear();
//		for (DependencyGraphNode<IScheduleEntry> entry : _scheduleDependencyGraph.getRootList())	// Start with the roots
//			_workQueue.add(entry);
//
//		// Create and start the threads
//		_solverSubThreads = new Thread[_numThreads];
//		for (int i = 0; i < _numThreads; i++)
//		{
//			_solverSubThreads[i] = new Thread(new SFactorGraphThread(this));
//			_solverSubThreads[i].start();
//		}
//
//		// Wait for notification of completion, then stop all the sub-threads
//		_waitNotify.doWait();
//		for (Thread thread : _solverSubThreads)
//			thread.interrupt();
//
//		// If there was an exception in any of the sub-threads, pass it along
//		if (_subThreadException != null)
//			throw new DimpleException(_subThreadException.getMessage());
//	}




//
//
//	private class SFactorGraphThread2 implements Callable
//	{
//
//		private LinkedBlockingQueue<NewDependencyGraphNode> _workQueue;
//		private int _numNodes;
//		private AtomicInteger _nodesDone;
//		
//		public SFactorGraphThread2(LinkedBlockingQueue<NewDependencyGraphNode> 
//			workQueue,
//			int numNodes,
//			AtomicInteger nodesDone)
//		{
//			_workQueue = workQueue;
//			_numNodes = numNodes;
//			_nodesDone = nodesDone;
//		}
//		
//		@Override
//		public Object call() throws Exception 
//		{
//			while(true)
//			{
//				NewDependencyGraphNode entry = _workQueue.take();	// Pull the next entry from the work queue (blocking if there are none)
//				
//				if (entry instanceof Poison)
//				{
//					_workQueue.add(entry);
//					break;
//				}
//				
//				entry.node.update();
//				
//				int nodesLeft = _nodesDone.decrementAndGet();
//				
//				if (nodesLeft == 0)
//				{
//					_workQueue.add(new Poison());
//					break;
//				}
//				
//				//if count is zero,
//					//interrupt all the other threads and return
//				
//				for (int i = 0; i < entry.dependents.size(); i++)
//				{
//					NewDependencyGraphNode dependent = entry.dependents.get(i);
//					synchronized (dependent)
//					{
//						dependent.numDependenciesLeft--;
//						if (dependent.numDependenciesLeft == 0)
//						{
//							_workQueue.add(dependent);
//							dependent.numDependenciesLeft = dependent.numDependencies;
//							
//						}
//					}
//					
//				}
//				//for each dependency
//				   //decrement
//					//if zero
//						//add to queue
//				
////				IScheduleEntry scheduleEntry = entry.getObject();
////				INode node = (scheduleEntry instanceof NodeScheduleEntry) ? ((NodeScheduleEntry)scheduleEntry).getNode() : ((EdgeScheduleEntry)scheduleEntry).getNode();
////				synchronized (node)												// Synchronize on the node: don't allow updating the same node in more than one thread at the same time
////				{
////					scheduleEntry.update();										// Run it
////				}
//				//_parentGraph.scheduleEntryCompleted(entry);						// Tell the main thread that it's done
//				
//				
//			}
//			return null;
//
//		}
//		
//	}

 	
	// Notifier, to notify from the thread that completes the final entry to the main task to tell it that we're done
	// Adapted from http://tutorials.jenkov.com/java-concurrency/thread-signaling.html

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

	
	
	public void setNumThreads(int numThreads)
	{
		_multithreader.setNumThreads(numThreads);

	}
		
	public void solverepeated(int num)
	{
		for (int i = 0; i < num; i++)
		{
			solve();
		}
	}
//	
//
//	public void printPhases(ArrayList<MapList> phases)
//	{
//		for (int i = 0; i < phases.size(); i++)
//		{
//			System.out.println("phase: " + i);
//			for (int j = 0; j < phases.get(i).size(); j++)
//			{
//				System.out.println(phases.get(i).getByIndex(j));
//			}
//		}
//	}
//	
//	
//	
//	public void solve3()
//	{
//		//initialize
//		getModelObject().initialize();
//		
//		//figure out dependency graph
//		NewDependencyGraph dg = createDependencyGraph();
//		//dg.print();
//
//		//create thread pool
//		int numThreads = getNumThreads();
//		int iters = getNumIterations();		
//		ExecutorService service = Executors.newFixedThreadPool(numThreads);		
//		
//		for (int i = 0; i < iters; i++)
//		{
//			doOneIteration(numThreads, service,dg);
//		}
//		
//		//One thread puts items on the blocking queues
//		//Other threads take items off of the blocking queues
//	}
//	
//	
//	public void doOneIteration(int numThreads, ExecutorService service,NewDependencyGraph dg)
//	{
//		LinkedBlockingDeque<NewDependencyGraphNode> doneQueue = new LinkedBlockingDeque<SFactorGraph.NewDependencyGraphNode>();
//		int totalItemsOfWork = dg.getNumNodes();
//		int executedItems = 0;
//		LinkedBlockingDeque<NewDependencyGraphNode> [] workQueues = new LinkedBlockingDeque[numThreads];
//		for (int i = 0; i < workQueues.length; i++)
//		{
//			workQueues[i] = new LinkedBlockingDeque<SFactorGraph.NewDependencyGraphNode>();
//		}
//		
////		for (int i = 0; i < numThreads; i++)
////		{
////			service.submit(new Solve3Worker(doneQueue, workQueues[i]));
////		}
//		
//		ArrayList<NewDependencyGraphNode> dgns = dg.getInitialEntries();
//		
//		ArrayList<Callable<Object>> workers = new ArrayList<Callable<Object>>();
//				
//		for (int i = 0; i < dgns.size(); i++)
//		{
//			workQueues[i%numThreads].add(dgns.get(i));
//		}
//		for (int i = 0; i < workQueues.length; i++)
//		{
//			workers.add(new Solve3Worker(workQueues,i));
//			//workQueues[i].add(new Poison());
//		}
//		
//		//TODO: need way to determine when to add poison.
////		
//		try {
//			service.invokeAll(workers);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
////		for (int i = 0; i < dgns.size(); i++)
////		{
////			DependencyGraphNode dgn = dgns.get(i);
////			for (int j = 0; j < dgn.dependents.size(); j++)
////			{
////				dgn.dependents.get(j).numDependenciesLeft--;
////			}
////		}
//		
////		while (true)
////		{
////			DependencyGraphNode current = null;
////			try {
////				current = doneQueue.take();
////			} catch (InterruptedException e) {
////				e.printStackTrace();
////			}
////			
////			
////			current.numDependenciesLeft = current.numDependencies;
////			
////			executedItems++;
////			if (executedItems == totalItemsOfWork)
////			{
////				//add poison;
////				for (int i = 0; i < workQueues.length; i++)
////					workQueues[i].add(new Poison());
////				break;
////			}
////
////			
////			for (int i = 0; i < current.dependents.size(); i++)
////			{
////				DependencyGraphNode d = current.dependents.get(i);
////				d.numDependenciesLeft--;
////				if (d.numDependenciesLeft == 0)
////				{
////					//add to the work queues
////					int minSize = Integer.MAX_VALUE;
////					int minIndex = 0;
////					for (int j= 0; j < workQueues.length; j++)
////					{
////						int sz = workQueues[j].size() ;
////						if (sz < minSize)
////						{
////							minSize = sz;
////							minIndex = j;
////						}
////					}
////					workQueues[minIndex].add(d);
////				}
////			}
////		}
////		
//		//TODO: Wait for all work queues.
//		
//	}
//	
	
//
//	
//	
//	public class NewDependencyGraph
//	{
//
//	}
//	
//
//	
//	public void solve2(int method)
//	{
//		getModelObject().initialize();
//		
//		FactorList factors = getModelObject().getFactors();
//		VariableList variables = getModelObject().getVariables();
//		
//		int numThreads = getNumThreads();
//		int iters = getNumIterations();
//		
//		ExecutorService service = Executors.newFixedThreadPool(numThreads);
//		
//		System.out.println("updating method: " + method);
//		
//		for (int i = 0; i < iters; i++)
//		{
//			switch (method)
//			{
//			case 1:
//				throw new DimpleException("nah");
//			case 2:
//				updateNodes2(service,variables,numThreads);
//				updateNodes2(service,factors,numThreads);
//				break;
//			case 3:
//				updateNodes3(service,variables,numThreads);
//				updateNodes3(service,factors,numThreads);
//				break;
//			case 4:
//				updateNodes4(service,variables,numThreads,false);
//				updateNodes4(service,factors,numThreads,false);
//				break;
//			case 5:
//				updateNodes4(service,variables,numThreads,true);
//				updateNodes4(service,factors,numThreads,true);
//				break;
//			default:
//				throw new DimpleException("not supported: "+ i);
//			}
//		}
//		
//		
//		//solve();
//	}
//	
//	
//	public void updateNodes4(ExecutorService service, MapList nodes, int numThreads, boolean stealing)
//	{
//		ConcurrentLinkedQueue<Node> [] deques = new ConcurrentLinkedQueue[numThreads];
//		for (int i = 0; i < deques.length; i++)
//			deques[i] = new ConcurrentLinkedQueue<Node>();
//		
//		//for (int i = 0; i < deques.length; )
//		//Add stuff to the deques
//		int numNodes = nodes.size();
//		
//		ArrayList<Callable<Object>> ll = new ArrayList<Callable<Object>>(numThreads);
//		int nodesPerThread = nodes.size() / numThreads;
//		
//		for (int i = 0; i < numThreads; i++)
//		{
//			ll.add(new UpdateSegmentDeque(nodes, i, deques, stealing));
//		}
//				
//		try {
//			service.invokeAll(ll);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//		}
//	}
//
//
//	public void updateNodes3(ExecutorService service, MapList nodes, int numThreads)
//	{
//		ConcurrentLinkedQueue<Node> q = new ConcurrentLinkedQueue<Node>();
//		
//		for (Object o : nodes)
//		{
//			q.add((Node)o);
//		}
//		
//		ArrayList<Callable<Object>> ll = new ArrayList<Callable<Object>>(numThreads);
//		int nodesPerThread = nodes.size() / numThreads;
//		
//		for (int i = 0; i < numThreads; i++)
//		{
//			ll.add(new DoStuffFromQueue(q));
//		}
//		
//
//		try {
//			service.invokeAll(ll);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//		}
//		
//		
//	}
//	
//	
//	public void updateNodes1(ExecutorService service, MapList nodes, int numThreads)
//	{
//		
//	}
//
//	
//	/*
//	 * 1) Units of work.  No faster.
//	 */
//	public void updateNodes2(ExecutorService service, MapList nodes, int numThreads)
//	{
//		//1) Units of work?
//		//2) Divided up by thread
//		//3) Single linked blocing queue
//		//4) Multiple liked blocking dequeues?
//		//First try single LinkBlockingQueue since it's easier
//		
//		//Add stuff to the deques
//		int numNodes = nodes.size();
//		
//		ArrayList<Callable<Object>> ll = new ArrayList<Callable<Object>>(numThreads);
//		int nodesPerThread = nodes.size() / numThreads;
//		
//		for (int i = 0; i < numThreads; i++)
//		{
//			int first = i*nodesPerThread;
//			
//			if (i == numThreads-1)
//				ll.add(new UpdateSegment(nodes, first, nodes.size()-1));
//			else
//				ll.add(new UpdateSegment(nodes, first, first + nodesPerThread - 1));
//		}
//		
//		
//		try {
//			service.invokeAll(ll);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//		}
//		
//		//service.invokeAll(ll);
//		
//		//Start the threads
//		//Each thread goes on until it runs out of stuff
//		//Search the other dequeues
//		//If nothing found tell the barrier it's done
//		//This thread will wait on the barrier
//		//When barrier is reached, add more work to the dequeus
//		//repeat until done
//		
//	}
//	
//	
//	
//	public NewDependencyGraph getDependencyGraph()
//	{
//		long id = getModelObject().getVersionId();
//		//create dependency graph
//		if (id != _cacheVersionId)
//		{
//			_cacheVersionId = id;
//			_cachedDependencyGraph = createDependencyGraph();
//		}
//		
//		return _cachedDependencyGraph;
//	}
//	
//	public ArrayList<MapList> getPhases()
//	{
//		return getDependencyGraph().getPhases();
//	}
//	
//	
//	private int _multiThreadMode = 1;
//	
	
	public void setMultiThreadMode(int mode)
	{
		_multithreader.setThreadingMode(mode);
	}


//
//	public void saveDependencyGraph(String fileName)
//	{
//		NewDependencyGraph dg = getDependencyGraph();
//		dg.createDotFile(fileName);
// 
////		
////		Graph g;
////		Layout l = new FRLayout( g );
////		Renderer r = new PluggableRenderer();
////		VisualizationViewer vv = new VisualizationViewer( layout, renderer );
////		JFrame jf = new JFrame();
////		jf.getContentPane().add ( vv );
//	}
	
//	
//	public void solve4()
//	{
//		
//		getModelObject().initialize();
//
//		
//		iterateMultiThreaded(numIters);
//		//printPhases(phases);
////		for (int i = 0; i < phases.size(); i++)
////		{
////			System.out.println("phase: " + i);
////			for (int j = 0; j < phases.get(i).size(); j++)
////			{
////				System.out.println(phases.get(i).getByIndex(j));
////			}
////		}
//		
//		int numThreads = getNumThreads();
//		int iters = getNumIterations();
//		
//
//
//		
//		
//		//service.shutdown();
//		//Figure out discrete steps
//		//for each iteration
//		//    walk through the steps and do the work stealing multi threading solution
//	}
//	
	

}
