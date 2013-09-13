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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Callable;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FactorList;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Node;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.solvers.core.ParameterEstimator;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.MapList;

public class SFactorGraph extends SFactorGraphBase
{
	private double _damping = 0;
	private IFactorTable _currentFactorTable = null;

	public SFactorGraph(com.analog.lyric.dimple.model.FactorGraph factorGraph)
	{
		super(factorGraph);
		
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
