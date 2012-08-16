/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

package com.analog.lyric.dimple.model.repeated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;

public class FactorGraphStream
{
	private FactorGraph _graph;
	private FactorGraph _repeatedGraph;
	private int _bufferSize;
	
	private ArrayList<IVariableStreamSlice> _streamSlices = new ArrayList<IVariableStreamSlice>();
	private ArrayList<FactorGraph> _factorGraphs = new ArrayList<FactorGraph>();
	
	private Object [] _args;
	private HashSet<VariableBase> _parameterVariables = new HashSet<VariableBase>();
	private HashMap<VariableBase,BlastFromThePastFactor> _var2blastFromPast = new HashMap<VariableBase, BlastFromThePastFactor>();
	
	private ArrayList<BlastFromThePastFactor> _blastFromPastsToRemove = new ArrayList<BlastFromThePastFactor>();
	
	//private HashSet<VariableBase> _parameter
	//private Arraylist<>
	
	//contains graph that is to be repeated
	public FactorGraphStream(FactorGraph fg, FactorGraph repeatedGraph,int bufferSize,Object ... args) 
	{
		_graph = fg;
		_repeatedGraph = repeatedGraph;
		_bufferSize = 0;
		
		//TODO: go through args and make sure they are variables and infinite
		//variable streams
		
		//Make sure they match the number of args in the repeatedGraph
		
		VariableList boundaryVars = repeatedGraph.getBoundaryVariables();
		
		if (boundaryVars.size() != args.length)
			throw new DimpleException("must specify correct number of variables");
		
		_args = checkArgs(args);
		
		setBufferSize(bufferSize);
		
	}
	
	public int getBufferSize()
	{
		return _bufferSize;
	}
	
	public void setBufferSize(int size) 
	{
		if (size <= 0)
			throw new DimpleException("buffer size must be > 0");
		
		if (size > _bufferSize)
		{
			for (int i = 0; i < size-_bufferSize; i++)
				addFactorGraph();
		}
		else if (size < _bufferSize)
		{
			double howmuch = _bufferSize - size;
			//remove graphs
			for (int i = _bufferSize-1; i >= size; i--)
			{
				FactorGraph graph = _factorGraphs.get(i);
				_graph.remove(graph);
				_factorGraphs.remove(i);
			}
			for (IVariableStreamSlice s : _streamSlices)
			{
				s.backup(howmuch);
			}
			
		}
			
		_bufferSize = size;
	}
	
	private Object [] checkArgs(Object [] args) 
	{
		Object [] retval = new Object[args.length];
		
		for (int j = 0; j < args.length; j++)
		{
			if (args[j] instanceof VariableBase)
			{
				retval[j] = args[j];
				_parameterVariables.add((VariableBase)args[j]);
			}
			else if (args[j] instanceof IVariableStreamSlice)
			{
				IVariableStreamSlice vss = (IVariableStreamSlice)args[j];
				vss = vss.copy();
				retval[j] = vss;
				
				//TODO: make sure unique?
				_streamSlices.add(vss);
			}
			else
				throw new DimpleException("expected VariableBase or IVariableStreamSlice");
		}
		
		return retval;
	}
	
	private void addFactorGraph() 
	{
		//get the appropriate arguments.
		VariableBase [] boundaryVariables = new VariableBase[_args.length];
		
		for (int j = 0; j < _args.length; j++)
		{
			if (_args[j] instanceof VariableBase)
				boundaryVariables[j] = (VariableBase)_args[j];
			else if (_args[j] instanceof IVariableStreamSlice)
			{
				IVariableStreamSlice vss = (IVariableStreamSlice)_args[j];
				boundaryVariables[j] = vss.getNext();
				
				//TODO: make sure unique?
				//_streamSlices.add(vss);
			}
			else
				throw new DimpleException("expected VariableBase or IVariableStreamSlice");
		}
		
		//add the factor graph
		FactorGraph result = _graph.addFactor(_repeatedGraph, boundaryVariables);
		

		
		_factorGraphs.add(result);
	}
	
	public void reset() 
	{
		//reset variable stream bases
		HashSet<VariableStreamBase> streams = new HashSet<VariableStreamBase>();
		
		for (IVariableStreamSlice slice :  _streamSlices)
			streams.add(slice.getStream());
		
		for (VariableStreamBase stream : streams)
			stream.reset();
		
		//remove blast from pasts
		for (BlastFromThePastFactor f : _blastFromPastsToRemove)
			_graph.remove(f);
		
		for (BlastFromThePastFactor f : _var2blastFromPast.values())
			_graph.remove(f);
		
		_blastFromPastsToRemove.clear();
		_var2blastFromPast.clear();
	}
	
	private void updateBlastFromPastForParameter(VariableBase v,FactorGraph graphToRemove) 
	{
		//we have to know which edges are being deleted
		ArrayList<Port> ports2delete = new ArrayList<Port>();
		for (Port p : v.getPorts())
		{
			INode node = p.getConnectedNode(0);
			FactorGraph parentGraph = node.asFactorGraph();
			if (parentGraph == graphToRemove)
			{
				ports2delete.add(p);
				//this is one of the edges we will be deleting
			}
		}
		
		//TODO: don't yet let override

		//if not yet created, create FactorGraph to compute blastFromPast message
		//set messages to the ports we're going to delete as well as the blast from past (if exists)
		//calculate
		//if necessary, create blast from past
		//set message
		
		FactorGraph tmpFg = new FactorGraph();
		tmpFg.setSolverFactory(tmpFg.getFactorGraphFactory());
		VariableBase tmpVar = v.clone();
		
		//TODO: is this hacky?
		v.setInputObject(v.getSolver().getDefaultMessage(null));
		//v.setInputObject(v.getSolver().get)
		
		tmpFg.initialize();
		
		for (Port p : ports2delete)
			tmpFg.addBlastFromPastFactor(p.getInputMsg(), tmpVar);
		
		BlastFromThePastFactor factor = null;
		if (_var2blastFromPast.containsKey(v))
		{
			factor = _var2blastFromPast.get(v);
			tmpFg.addBlastFromPastFactor(factor.getPorts().get(0).getOutputMsg(), tmpVar);
		}
		
		//TODO: what about old blast for past var
		
		tmpFg.solve(false);
		
		Object msg = tmpVar.getBeliefObject();
		
		if (factor == null)
		{
			factor =  _graph.addBlastFromPastFactor(msg, v);
			_var2blastFromPast.put(v,factor);
		}
		else
			factor.setOutputMsg(msg);
		
	}
	
	
	public void advance() 
	{
		//TODO: have to be able to find parameter variables so that we can
		//      update blastfrompast factor for these
		
		
		
		//TODO: how do the various variable streams know when to release data?
		if (!hasNext())
			throw new DimpleException("don't yet have data to advance");
		
		for (BlastFromThePastFactor f : _blastFromPastsToRemove)
			_graph.remove(f);
		
		_blastFromPastsToRemove.clear();
		
		//Add new variables as needed
		//Add new Factor Graph
		//TODO: Includes adding data. Separate this eventually.
		addFactorGraph();
		
		HashSet<VariableBase> torelease = new HashSet<VariableBase>();
		
		//free unused variables
		for (IVariableStreamSlice ss : _streamSlices)
		{
			ArrayList<VariableBase> tmp = ss.releaseFirst();
			torelease.addAll(tmp);
		}
		
		//Make sure constants are in place
		//How do we know where we need to connect constants?
		VariableList boundaryVariables = _factorGraphs.get(0).getBoundaryVariables();
		
		//for each boundary variables
		for (VariableBase v : boundaryVariables)
		{
			if (_parameterVariables.contains(v))
			{
				//update the blastFromPast
				updateBlastFromPastForParameter(v,_factorGraphs.get(0));

			}
			else
			{
				Port [] pa = new Port[v.getPorts().size()];
				v.getPorts().toArray(pa);
				
				//for each edge
				for (Port p : pa)
				{
					//if factor belongs to the graph we're about to delete
					if (p.getConnectedNode().getParentGraph() == _factorGraphs.get(0))
					{
						//if this variable is only connected to this factor
						
						//Create a constant factor
						if (!torelease.contains(v))
						{
							Object o = p.getInputMsg();
							BlastFromThePastFactor bpf = _graph.addBlastFromPastFactor(o,v);
							_blastFromPastsToRemove.add(bpf);
						}
					}
				}
			}
		}
		
		
		
		//remove connections to old Factor Graph
		_graph.remove(_factorGraphs.get(0));

		_factorGraphs.remove(0);
		
		//TODO: we haven't actually removed old variables - memory leak
		
		//TODO: the message from the constant factor is somehow uniform distribution.
		
	}
	
	
	
	public boolean hasNext() 
	{
		for (IVariableStreamSlice s : _streamSlices)
		{
			if (!s.hasNext())
				return false;
		}
		return true;
	}
	
}
