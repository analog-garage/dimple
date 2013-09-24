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

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FactorList;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Node;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.solvers.core.ParameterEstimator;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.MapList;
import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

public class SFactorGraph extends SFactorGraphBase
{
	private double _damping = 0;
	private IFactorTable _currentFactorTable = null;

	public SFactorGraph(com.analog.lyric.dimple.model.FactorGraph factorGraph)
	{
		super(factorGraph);
		
	}
	
	public void solve5()
	{
		getModelObject().initialize();
		MapList nodes = getModelObject().getNodes();
	}
	
	public void solverepeated(int num)
	{
		for (int i = 0; i < num; i++)
		{
			solve();
		}
	}
	
	public void solve4repeated(int num)
	{
		for (int i = 0; i < num; i++)
		{
			solve4();
		}
	}
	
	private long _cacheVersionId = -2;
	private DependencyGraph _cachedDependencyGraph;
	ExecutorService _service; // = Executors.newFixedThreadPool(numThreads);
	
	@Override
	public void setNumThreads(int numThreads)
	{
		super.setNumThreads(numThreads);
		if (_service != null)
			_service.shutdown();
		_service = Executors.newFixedThreadPool(_numThreads);
	}

	public void printPhases(ArrayList<MapList> phases)
	{
		for (int i = 0; i < phases.size(); i++)
		{
			System.out.println("phase: " + i);
			for (int j = 0; j < phases.get(i).size(); j++)
			{
				System.out.println(phases.get(i).getByIndex(j));
			}
		}
	}
	
	public void solve4()
	{
		
		getModelObject().initialize();

		long id = getModelObject().getVersionId();
		//create dependency graph
		if (id != _cacheVersionId)
		{
			_cacheVersionId = id;
			_cachedDependencyGraph = createDependencyGraph();
		}
		ArrayList<MapList> phases = _cachedDependencyGraph.getPhases();
		printPhases(phases);
//		for (int i = 0; i < phases.size(); i++)
//		{
//			System.out.println("phase: " + i);
//			for (int j = 0; j < phases.get(i).size(); j++)
//			{
//				System.out.println(phases.get(i).getByIndex(j));
//			}
//		}
		
		int numThreads = getNumThreads();
		int iters = getNumIterations();
		

		for (int i = 0; i < iters; i++)
		{
			for (int j = 0; j < phases.size(); j++)
			{				
				
				updateNodes4(_service, phases.get(j), numThreads, true);
			}
		}
		
		
		//service.shutdown();
		//Figure out discrete steps
		//for each iteration
		//    walk through the steps and do the work stealing multi threading solution
	}
	
	public void solve3()
	{
		//initialize
		getModelObject().initialize();
		
		//figure out dependency graph
		DependencyGraph dg = createDependencyGraph();
		//dg.print();

		//create thread pool
		int numThreads = getNumThreads();
		int iters = getNumIterations();		
		ExecutorService service = Executors.newFixedThreadPool(numThreads);		
		
		for (int i = 0; i < iters; i++)
		{
			doOneIteration(numThreads, service,dg);
		}
		
		//One thread puts items on the blocking queues
		//Other threads take items off of the blocking queues
	}
	
	
	public class Solve3Worker implements Callable
	{
		//private LinkedBlockingDeque<DependencyGraphNode> [] _doneQueues;
		private LinkedBlockingDeque<DependencyGraphNode> [] _workQueues;
		private int _me;
		private AtomicInteger _numNodes;
		
		public Solve3Worker(LinkedBlockingDeque<DependencyGraphNode> [] workQueues,
				int me //,
//				AtomicInteger numNodes
				)
		{
			_workQueues = workQueues;
			_me = me;
	//		_numNodes = numNodes;
		}
		
		@Override
		public Object call() throws Exception 
		{
			DependencyGraphNode dgn = _workQueues[_me].poll();

			while (dgn != null)
			{
				//DependencyGraphNode dgn = _workQueue.take();

				
				if (dgn instanceof Poison)
					break;
				
//				int value = _numNodes.decrementAndGet();
//				
//				if (value == 0)
//				{
//					for (int i = 0; i < _workQueues.length; i++)
//						_workQueues[i].add(new Poison());
//				}
				
				dgn.scheduleEntry.update();
				
				for (int i = 0; i < dgn.dependents.size(); i++)
				{
//					DependencyGraphNode dependent = dgn.dependents.get(i);
//					if (dependent.decrementAndCheckReady())
//					{
//						_workQueues[_me].add(dgn.dependents.get(i));
//					}
				}
				//_doneQueue.add(dgn);
				dgn = _workQueues[_me].poll();
			}
			
			
			return null;
		}
	}
	
	public class Poison extends DependencyGraphNode
	{

		
	}
	
	public void doOneIteration(int numThreads, ExecutorService service,DependencyGraph dg)
	{
		LinkedBlockingDeque<DependencyGraphNode> doneQueue = new LinkedBlockingDeque<SFactorGraph.DependencyGraphNode>();
		int totalItemsOfWork = dg.getNumNodes();
		int executedItems = 0;
		LinkedBlockingDeque<DependencyGraphNode> [] workQueues = new LinkedBlockingDeque[numThreads];
		for (int i = 0; i < workQueues.length; i++)
		{
			workQueues[i] = new LinkedBlockingDeque<SFactorGraph.DependencyGraphNode>();
		}
		
//		for (int i = 0; i < numThreads; i++)
//		{
//			service.submit(new Solve3Worker(doneQueue, workQueues[i]));
//		}
		
		ArrayList<DependencyGraphNode> dgns = dg.getInitialEntries();
		
		ArrayList<Callable<Object>> workers = new ArrayList<Callable<Object>>();
				
		for (int i = 0; i < dgns.size(); i++)
		{
			workQueues[i%numThreads].add(dgns.get(i));
		}
		for (int i = 0; i < workQueues.length; i++)
		{
			workers.add(new Solve3Worker(workQueues,i));
			//workQueues[i].add(new Poison());
		}
		
		//TODO: need way to determine when to add poison.
//		
		try {
			service.invokeAll(workers);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		for (int i = 0; i < dgns.size(); i++)
//		{
//			DependencyGraphNode dgn = dgns.get(i);
//			for (int j = 0; j < dgn.dependents.size(); j++)
//			{
//				dgn.dependents.get(j).numDependenciesLeft--;
//			}
//		}
		
//		while (true)
//		{
//			DependencyGraphNode current = null;
//			try {
//				current = doneQueue.take();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			
//			current.numDependenciesLeft = current.numDependencies;
//			
//			executedItems++;
//			if (executedItems == totalItemsOfWork)
//			{
//				//add poison;
//				for (int i = 0; i < workQueues.length; i++)
//					workQueues[i].add(new Poison());
//				break;
//			}
//
//			
//			for (int i = 0; i < current.dependents.size(); i++)
//			{
//				DependencyGraphNode d = current.dependents.get(i);
//				d.numDependenciesLeft--;
//				if (d.numDependenciesLeft == 0)
//				{
//					//add to the work queues
//					int minSize = Integer.MAX_VALUE;
//					int minIndex = 0;
//					for (int j= 0; j < workQueues.length; j++)
//					{
//						int sz = workQueues[j].size() ;
//						if (sz < minSize)
//						{
//							minSize = sz;
//							minIndex = j;
//						}
//					}
//					workQueues[minIndex].add(d);
//				}
//			}
//		}
//		
		//TODO: Wait for all work queues.
		
	}
	
	public class NodeLastUpdates
	{
		public INode node;
		public DependencyGraphNode lastUpdate;
		public DependencyGraphNode [] lastInputUpdates;
		public DependencyGraphNode [] lastOutputUpdates;
		
		public NodeLastUpdates(INode n)
		{
			node = n;
			int sz = n.getSiblings().size();
			lastInputUpdates = new DependencyGraphNode[sz];
			lastOutputUpdates = new DependencyGraphNode[sz];
			lastUpdate = null;
		}
	}
	

	public class DependencyGraphNode
	{
		public INode node;
		public ArrayList<DependencyGraphNode> dependents = new ArrayList<SFactorGraph.DependencyGraphNode>();
		public  int numDependencies;
		public int numDependenciesLeft;
		public IScheduleEntry scheduleEntry;
		public ArrayList<Integer> inports = new ArrayList<Integer>();
		public ArrayList<Integer> outports = new ArrayList<Integer>();
		
		
		public void print()
		{
			System.out.println("node: " + node.toString());
		}
		
		public DependencyGraphNode()
		{
			
		}
		
		public ArrayList<DependencyGraphNode> pretendUpdateAndReturnAvailableDependencies()
		{
			ArrayList<DependencyGraphNode> retval = new ArrayList<SFactorGraph.DependencyGraphNode>();
	
			for (int i = 0; i < dependents.size(); i++)
			{
				dependents.get(i).numDependenciesLeft--;
				
				if (dependents.get(i).numDependenciesLeft == 0)
					retval.add(dependents.get(i));
				
				if (dependents.get(i).numDependenciesLeft < 0)
				{
					System.out.println("Ack");
					throw new DimpleException("Ack");
				}
				
			}
			
			return retval;
		}
		
		public DependencyGraphNode(IScheduleEntry scheduleEntry,
				LastUpdateGraph lastUpdateGraph)
		{
			this.scheduleEntry = scheduleEntry;
			
			ArrayList<Integer> inputports = new ArrayList<Integer>();
			ArrayList<Integer> outputports = new ArrayList<Integer>();
			
			if (scheduleEntry instanceof NodeScheduleEntry)
			{
				NodeScheduleEntry nse = (NodeScheduleEntry)scheduleEntry;
				this.node = nse.getNode();
				for (int i = 0; i < this.node.getSiblings().size(); i++)
				{
					inputports.add(i);
					outputports.add(i);
				}
			}
			else if (scheduleEntry instanceof EdgeScheduleEntry)
			{
				EdgeScheduleEntry ese = (EdgeScheduleEntry)scheduleEntry;
				this.node = ese.getNode();
				outputports.add(ese.getPortNum());
				for (int i = 0; i < this.node.getSiblings().size(); i++)
				{
					if (i != ese.getPortNum())
					{
						inputports.add(i);
					}
				}
			}
			else
			{
				throw new DimpleException("ack");
			}
			
			NodeLastUpdates nlu =  lastUpdateGraph.getNodeLastUpdates(this.node);
			
			DependencyGraphNode dgn = null;
			
			int numDependencies = 0;
			
			//Update dependents
			//for each input
			for (int i = 0; i < inputports.size(); i++)
			{
				//  get the last guy to update this
				//  add me to dependent list (increment num dependencies)
				int portNum = inputports.get(i);
				dgn  = nlu.lastInputUpdates[portNum];
				
				if (dgn != null)
				{
					dgn.dependents.add(this);
					
					numDependencies++;
				}
				
			}
			
			//for each output
			//  get the last guy to use this
			//  add me to the dependent list (increment num dependencies)
			for (int i = 0; i < outputports.size(); i++)
			{
				int portNum = outputports.get(i);
				dgn = nlu.lastOutputUpdates[portNum];
				
				if (dgn != null)
				{
					dgn.dependents.add(this);					
					numDependencies++;
				}
			}
			
			//for the last guy to update this node
			//  add me to the dependent list (increment num dependencies)
			//nlu.lastUpdate = this;
			this.numDependencies = numDependencies;
			this.numDependenciesLeft = numDependencies;
			this.inports = inputports;
			this.outports = outputports;
		}
		
		
	}
	
	public class LastUpdateGraph
	{
		private HashMap<INode,NodeLastUpdates> _node2updates = new HashMap<INode, SFactorGraph.NodeLastUpdates>();
		
		public LastUpdateGraph(FactorGraph fg)
		{
			MapList<INode> nodes = fg.getNodes();
			//TODO: hashtable
		
			for (INode n : nodes)
			{
				_node2updates.put(n, new NodeLastUpdates(n));
			}

		}
		
		public NodeLastUpdates getNodeLastUpdates(INode n)
		{
			return _node2updates.get(n);
		}
		
		public void update(DependencyGraphNode dgn)
		{
			NodeLastUpdates nlu = _node2updates.get(dgn.node);
			nlu.lastUpdate = dgn;
			
			for (int i = 0; i < dgn.inports.size(); i++)
			{
				int portnum = dgn.inports.get(i);
				INode sibling = dgn.node.getSiblings().get(portnum);
				int siblingportnum = dgn.node.getSiblingPortIndex(portnum);
				getNodeLastUpdates(sibling).lastOutputUpdates[siblingportnum] = dgn;
			}
			
			for (int i = 0; i < dgn.outports.size(); i++)
			{
				int portnum = dgn.outports.get(i);
				INode sibling = dgn.node.getSiblings().get(portnum);
				int siblingportnum = dgn.node.getSiblingPortIndex(portnum);
				getNodeLastUpdates(sibling).lastInputUpdates[siblingportnum] = dgn;
			}
			
		}
	}
	
	public class DependencyGraph
	{
		private int _numNodes;
		private ArrayList<DependencyGraphNode> _initialEntries;
		private ArrayList<MapList> _phases;
		
		public DependencyGraph(FactorGraph fg)
		{
			LastUpdateGraph lug = new LastUpdateGraph(fg);
			ArrayList<DependencyGraphNode> initialEntries = new ArrayList<DependencyGraphNode>();
			
			ISchedule schedule = getModelObject().getSchedule();
			for (IScheduleEntry se : schedule)
			{
				DependencyGraphNode dgn = new DependencyGraphNode(se,lug);
				lug.update(dgn);
				_numNodes++;
				
				if (dgn.numDependencies == 0)
					initialEntries.add(dgn);
			}
			
			_initialEntries = initialEntries;
		}
		
		public ArrayList<MapList> getPhases()
		{
			if (_phases == null)
			{
				ArrayList<ArrayList<DependencyGraphNode>> tmpPhases = new ArrayList<ArrayList<DependencyGraphNode>>();
				
				tmpPhases.add(new ArrayList<SFactorGraph.DependencyGraphNode>());
				
				for (int i = 0; i < _initialEntries.size(); i++)
				{
					tmpPhases.get(0).add(_initialEntries.get(i));
				}
				
				int numNodesDone = _initialEntries.size();
				
				ArrayList<DependencyGraphNode> justFinished = tmpPhases.get(0);
				
				while (numNodesDone != _numNodes)
				{
					ArrayList<DependencyGraphNode> newStuff = new ArrayList<SFactorGraph.DependencyGraphNode>();
					
					for (DependencyGraphNode n : justFinished)
					{
						ArrayList<DependencyGraphNode> tmp = n.pretendUpdateAndReturnAvailableDependencies();
						newStuff.addAll(tmp);
					}
					
					tmpPhases.add(newStuff);
					justFinished = newStuff;
					numNodesDone += newStuff.size();
				}
					
				ArrayList<MapList> realRetval = new ArrayList<MapList>();
				
				for (int i = 0; i < tmpPhases.size(); i++)
				{
					MapList ml = new MapList();
					for (DependencyGraphNode dgn : tmpPhases.get(i))
					{
						ml.add(dgn.node);
					}
					realRetval.add(ml);
				}
				_phases = realRetval;
			}
			
			return _phases;
		}
		
		public int getNumNodes()
		{
			return _numNodes;
		}
		
		public ArrayList<DependencyGraphNode> getInitialEntries()
		{
			return _initialEntries;
		}
		
		public void print()
		{
			System.out.println("initial entries: ");
			for (int i = 0; i < _initialEntries.size(); i++)
				_initialEntries.get(i).print();
			
		}
	}
	
	public DependencyGraph createDependencyGraph()
	{
		return new DependencyGraph(getModelObject());
		
	}
	
	public void solve2(int method)
	{
		getModelObject().initialize();
		
		FactorList factors = getModelObject().getFactors();
		VariableList variables = getModelObject().getVariables();
		
		int numThreads = getNumThreads();
		int iters = getNumIterations();
		
		ExecutorService service = Executors.newFixedThreadPool(numThreads);
		
		System.out.println("updating method: " + method);
		
		for (int i = 0; i < iters; i++)
		{
			switch (method)
			{
			case 1:
				throw new DimpleException("nah");
			case 2:
				updateNodes2(service,variables,numThreads);
				updateNodes2(service,factors,numThreads);
				break;
			case 3:
				updateNodes3(service,variables,numThreads);
				updateNodes3(service,factors,numThreads);
				break;
			case 4:
				updateNodes4(service,variables,numThreads,false);
				updateNodes4(service,factors,numThreads,false);
				break;
			case 5:
				updateNodes4(service,variables,numThreads,true);
				updateNodes4(service,factors,numThreads,true);
				break;
			default:
				throw new DimpleException("not supported: "+ i);
			}
		}
		
		
		//solve();
	}
	
	public class UpdateSegmentDeque implements Callable
	{
		private ConcurrentLinkedQueue<Node> [] _deques;
		private int _which;
		private MapList _nodes;
		private boolean _stealing;
		
		public UpdateSegmentDeque(MapList nodes, int which, ConcurrentLinkedQueue<Node> [] deques, boolean stealing)
		{
			_which = which;
			_deques = deques;
			_nodes= nodes;
			_stealing = stealing;
		}
		
		@Override
		public Object call() throws Exception 
		{			
			int which = _which;
			int nodesPerThread = _nodes.size() / _deques.length;
			int first = _which*nodesPerThread;
			int last = first + nodesPerThread - 1;
			if (which == _deques.length - 1)
				last = _nodes.size()-1;
			
			for (int i = first; i <= last; i++)
			{
				Object tmp = _nodes.getByIndex(i);
				Node ntmp = (Node)tmp;
				_deques[which].add(ntmp);
			}
			
			Node n = _deques[which].poll();
			
		
			while (n != null)
			{	
				n.update();
				
				n = _deques[which].poll();
				
				if (n == null && _stealing)
				{
					for (int i = 0; i < _deques.length; i++)
					{
						n = _deques[i].poll();
						if (n != null)
							break;
					}
				}
				
			}	
			return null;
		}
		
	}
	
	public void updateNodes4(ExecutorService service, MapList nodes, int numThreads, boolean stealing)
	{
		ConcurrentLinkedQueue<Node> [] deques = new ConcurrentLinkedQueue[numThreads];
		for (int i = 0; i < deques.length; i++)
			deques[i] = new ConcurrentLinkedQueue<Node>();
		
		//for (int i = 0; i < deques.length; )
		//Add stuff to the deques
		int numNodes = nodes.size();
		
		ArrayList<Callable<Object>> ll = new ArrayList<Callable<Object>>(numThreads);
		int nodesPerThread = nodes.size() / numThreads;
		
		for (int i = 0; i < numThreads; i++)
		{
			ll.add(new UpdateSegmentDeque(nodes, i, deques, stealing));
		}
				
		try {
			service.invokeAll(ll);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}


	public void updateNodes3(ExecutorService service, MapList nodes, int numThreads)
	{
		ConcurrentLinkedQueue<Node> q = new ConcurrentLinkedQueue<Node>();
		
		for (Object o : nodes)
		{
			q.add((Node)o);
		}
		
		ArrayList<Callable<Object>> ll = new ArrayList<Callable<Object>>(numThreads);
		int nodesPerThread = nodes.size() / numThreads;
		
		for (int i = 0; i < numThreads; i++)
		{
			ll.add(new DoStuffFromQueue(q));
		}
		

		try {
			service.invokeAll(ll);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		
	}
	
	public class DoStuffFromQueue implements Callable
	{

		private ConcurrentLinkedQueue<Node> _queue;
		
		public DoStuffFromQueue(ConcurrentLinkedQueue<Node> queue)
		{
			_queue = queue;
		}
		
		@Override
		public Object call() throws Exception 
		{
			Node n = _queue.poll();
			
			while (n != null)
			{
				n.update();
				n = _queue.poll();
			}
			return null;
		}
		
	}
	
	public void updateNodes1(ExecutorService service, MapList nodes, int numThreads)
	{
		
	}

	
	/*
	 * 1) Units of work.  No faster.
	 */
	public void updateNodes2(ExecutorService service, MapList nodes, int numThreads)
	{
		//1) Units of work?
		//2) Divided up by thread
		//3) Single linked blocing queue
		//4) Multiple liked blocking dequeues?
		//First try single LinkBlockingQueue since it's easier
		
		//Add stuff to the deques
		int numNodes = nodes.size();
		
		ArrayList<Callable<Object>> ll = new ArrayList<Callable<Object>>(numThreads);
		int nodesPerThread = nodes.size() / numThreads;
		
		for (int i = 0; i < numThreads; i++)
		{
			int first = i*nodesPerThread;
			
			if (i == numThreads-1)
				ll.add(new UpdateSegment(nodes, first, nodes.size()-1));
			else
				ll.add(new UpdateSegment(nodes, first, first + nodesPerThread - 1));
		}
		
		
		try {
			service.invokeAll(ll);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		//service.invokeAll(ll);
		
		//Start the threads
		//Each thread goes on until it runs out of stuff
		//Search the other dequeues
		//If nothing found tell the barrier it's done
		//This thread will wait on the barrier
		//When barrier is reached, add more work to the dequeus
		//repeat until done
		
	}
	
	public class UpdateSegment implements Callable
	{
		private MapList _mapList;
		private int _first;
		private int _last;
		
		public UpdateSegment(MapList list,int first, int last)
		{
			_mapList = list;
			_first = first;
			_last = last;
		}
		
		@Override
		public Object call() throws Exception 
		{
			for (int i = _first; i <= _last; i++)
				((Node)_mapList.getByIndex(i)).update();
			//_node.update();
			return null;
		}
		
	}
	
	public class UpdateNode implements Callable
	{
		private Node _node;
		public UpdateNode(Node n)
		{
			_node = n;
		}
		
		@Override
		public Object call() throws Exception 
		{
			_node.update();
			return null;
		}
		
	}
	

	public ISolverFactor createCustomFactor(com.analog.lyric.dimple.model.Factor factor)
	{
		String funcName = factor.getFactorFunction().getName();
		if (funcName.equals("finiteFieldMult"))
		{
			//VariableList variables = factor.getVariables();
			
			if (factor.getFactorFunction() instanceof FactorFunctionWithConstants)
				return new FiniteFieldConstMult(factor);
			else
				return new FiniteFieldMult(factor);
		}
		else if (funcName.equals("finiteFieldAdd"))
		{
			return new FiniteFieldAdd(factor);
		}
		else if (funcName.equals("finiteFieldProjection"))
		{
			return new FiniteFieldProjection(factor);
		}
		else if (funcName.equals("multiplexerCPD"))
			return new MultiplexerCPD(factor);
		else
			throw new DimpleException("Not implemented");
	}
	
	@Override
	public ISolverVariable createVariable(VariableBase var)
	{
		if (var.getModelerClassName().equals("FiniteFieldVariable"))
			return new FiniteFieldVariable(var);
		else
			return new SVariable(var);
	}

	@Override
	public boolean customFactorExists(String funcName)
	{
		if (funcName.equals("finiteFieldMult"))
			return true;
		else if (funcName.equals("finiteFieldAdd"))
			return true;
		else if (funcName.equals("finiteFieldProjection"))
			return true;
		else if (funcName.equals("multiplexerCPD"))
			return true;
		else
			return false;
	}


	private static Random _rand = new Random();

	public static Random getRandom()
	{
		return _rand;
	}
	
	public void setSeed(long seed)
	{
		_rand = new Random(seed);
	}
	
	@Override
	public ISolverFactor createFactor(Factor factor)
	{
		if (customFactorExists(factor.getFactorFunction().getName()))
		{
			return createCustomFactor(factor);
		}
		else
		{
	
			STableFactor tf = new STableFactor(factor);
			if (_damping != 0)
				setDampingForTableFunction(tf);
			return tf;
		}
	}
	

	/*
	 * Set the global solver damping parameter.  We have to go through all factor graphs
	 * and update the damping parameter on all existing table functions in that graph.
	 */
	public void setDamping(double damping)
	{
		_damping = damping;
		for (Factor f : _factorGraph.getNonGraphFactors())
		{
			STableFactor tf = (STableFactor)f.getSolver();
			setDampingForTableFunction(tf);
		}
	}
	
	public double getDamping()
	{
		return _damping;
	}

	/*
	 * This method applies the global damping parameter to all of the table function's ports
	 * and all of the variable ports connected to it.  This might cause problems in the future
	 * when we support different damping parameters per edge.
	 */
	protected void setDampingForTableFunction(STableFactor tf)
	{
		
		for (int i = 0; i < tf.getFactor().getSiblings().size(); i++)
		{
			tf.setDamping(i,_damping);
			VariableBase var = (VariableBase)tf.getFactor().getConnectedNodesFlat().getByIndex(i);
			for (int j = 0; j < var.getSiblings().size(); j++)
			{
				SVariable svar = (SVariable)var.getSolver();
				svar.setDamping(j,_damping);
			}
		}

	}
	
	@Override
	public void baumWelch(IFactorTable [] fts, int numRestarts, int numSteps)
	{
		ParameterEstimator pe = new ParameterEstimator.BaumWelch(_factorGraph, fts, SFactorGraph.getRandom());
		pe.run(numRestarts, numSteps);
	}
	
	
	public class GradientDescent extends ParameterEstimator
	{
		private double _scaleFactor;

		public GradientDescent(FactorGraph fg, IFactorTable[] tables, Random r, double scaleFactor)
		{
			super(fg, tables, r);
			_scaleFactor = scaleFactor;
		}

		@Override
		public void runStep(FactorGraph fg)
		{
			//_factorGraph.solve();
			for (IFactorTable ft : getTables())
			{
				double [] weights = ft.getWeightsSparseUnsafe();
			      //for each weight
				for (int i = 0; i < weights.length; i++)
				{
			           //calculate the derivative
					double derivative = calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(ft, i);
					
			        //move the weight in that direction scaled by epsilon
					ft.setWeightForSparseIndex(weights[i] - weights[i]*derivative*_scaleFactor,i);
				}
			}
		}
		
	}
	
	public void pseudoLikelihood(IFactorTable [] fts,
			VariableBase [] vars,
			Object [][] data,
			int numSteps,
			double stepScaleFactor)
	{
		
	}
	
	public static int [][] convertObjects2Indices(VariableBase [] vars, Object [][] data)
	{
		
		return null;
	}

	
	@Override
	public void estimateParameters(IFactorTable [] fts, int numRestarts, int numSteps, double stepScaleFactor)
	{
		new GradientDescent(_factorGraph, fts, getRandom(), stepScaleFactor).run(numRestarts, numSteps);
	}

	
	public double calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(IFactorTable ft,
			int weightIndex)
	{
		//BFE = InternalEnergy - BetheEntropy
		//InternalEnergy = Sum over all factors (Internal Energy of Factor)
		//                   + Sum over all variables (Internal Energy of Variable)
		//BetheEntropy = Sum over all factors (BetheEntropy(factor))
		//                  + sum over all variables (BetheEntropy(variable)
		//So derivative of BFE = Sum over all factors that contain the weight
		//                                              (derivative of Internal Energy of Factor
		//                                              - derivative of BetheEntropy of Factor)
		//
		
		_currentFactorTable = ft;
		
				
		for (Factor f : _factorGraph.getFactorsFlat())
		{
			((STableFactor)f.getSolver()).initializeDerivativeMessages(ft.sparseSize());
		}
		for (VariableBase vb : _factorGraph.getVariablesFlat())
			((SVariable)vb.getSolver()).initializeDerivativeMessages(ft.sparseSize());
		
		setCalculateDerivative(true);
		
		double result = 0;
		try
		{
			_factorGraph.solve();
			for (Factor f : _factorGraph.getFactorsFlat())
			{
				STableFactor stf = (STableFactor)f.getSolver();
				result += stf.calculateDerivativeOfInternalEnergyWithRespectToWeight(weightIndex);
				result -= stf.calculateDerivativeOfBetheEntropyWithRespectToWeight(weightIndex);
						
			}
			for (VariableBase v : _factorGraph.getVariablesFlat())
			{
				SVariable sv = (SVariable)v.getSolver();
				result += sv.calculateDerivativeOfInternalEnergyWithRespectToWeight(weightIndex);
				result += sv.calculateDerivativeOfBetheEntropyWithRespectToWeight(weightIndex);
			}
		}
		finally
		{
			setCalculateDerivative(false);
		}
		
		return result;
	}
	
	public void setCalculateDerivative(boolean val)
	{
		for (Factor f : _factorGraph.getFactorsFlat())
		{
			STableFactor stf = (STableFactor)f.getSolver();
			stf.setUpdateDerivative(val);
		}
		for (VariableBase vb : _factorGraph.getVariablesFlat())
		{
			SVariable sv = (SVariable)vb.getSolver();
			sv.setCalculateDerivative(val);
		}
	}
	
	
	// REFACTOR: make this package-protected?
	public IFactorTable getCurrentFactorTable()
	{
		return _currentFactorTable;
	}



	

}
