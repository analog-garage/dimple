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

package com.analog.lyric.dimple.model.repeated;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import org.eclipse.jdt.annotation.Nullable;

/*
 * This class represents one stream of Nested Factor Graphs.
 */
public class FactorGraphStream
{

	ArrayList<ArrayList<BlastFromThePastFactor>> _blastFromThePastChains = new ArrayList<ArrayList<BlastFromThePastFactor>>();
	ArrayList<FactorGraph> _nestedGraphs = new ArrayList<FactorGraph>();
	ArrayList<VariableStreamBase> _variableStreams = new ArrayList<VariableStreamBase>();
	private int _bufferSize = 0;
	private Object [] _args;
	private FactorGraph _graph;
	private FactorGraph _repeatedGraph;
	private FactorGraph _parameterFactorGraph;

	/*
	 * The constructor adds Factors and Variables and BlastFromThePastFactors.
	 */
	public FactorGraphStream(FactorGraph fg,
			FactorGraph repeatedGraph,
			int bufferSize,
			Object ... args)
	{
		//Save arguments so that we can increase buffer size later
		_args = args;
		_graph = fg;
		_repeatedGraph = repeatedGraph;
		_parameterFactorGraph = new FactorGraph();
		_parameterFactorGraph.setSolverFactory(_graph.getFactorGraphFactory());

		//The following few lines of code retrieve the unique variable streams
		HashSet<VariableStreamBase> variableStreams = new HashSet<VariableStreamBase>();

		//Find unique variable streams
		for (int i = 0; i < args.length; i++)
		{
			if (args[i] instanceof IVariableStreamSlice)
			{
				VariableStreamBase vsb = ((IVariableStreamSlice)args[i]).getStream();
				variableStreams.add(vsb);
			}
		}

		for (VariableStreamBase vsb : variableStreams)
			_variableStreams.add(vsb);

		//Here we build up the nested graphs.
		setBufferSize(bufferSize);


		//Setup blast from past factors
		
		//Get first Factor Graph's ports
		FactorGraph firstGraph = _nestedGraphs.get(0);

		ArrayList<Port> ports = firstGraph.getPorts();

		
		//For each port
		for (Port p : ports)
		{
		
			//figure out which variable stream this is connected to
			Variable var = (Variable)p.getSibling();
			VariableStreamBase vsb = getVariableStream(var);

			if (vsb == null)
			{
				//This is a parameter
				//Add BlastFrom the Past Factor and save it
				
				//Find out if we've encountered this parameter before
				if (! _parameter2blastFromThePastHandler.containsKey(var))
				{
					BlastFromThePastFactor f = _graph.addBlastFromPastFactor(var, p.getSiblingPort());
					ParameterBlastFromThePastHandler pbftph = new ParameterBlastFromThePastHandler(
							var,_parameterFactorGraph,f);
					_parameter2blastFromThePastHandler.put(var,pbftph);
				}
				
				_parameter2blastFromThePastHandler.get(var).addBlastFromThePast(p.getSiblingPort());
				
			}
			else
			{
				//This is not a parameter
				
				//Retrieve the index of this variable within the stream
				int index = vsb.indexOf(var);
			
				//Set next port to this port
				Port nextPort = p;

				if (index > 0 )
				{
					ArrayList<BlastFromThePastFactor> bfc = new ArrayList<BlastFromThePastFactor>();
					_blastFromThePastChains.add(bfc);
					
					//For each variable before this one
					for (int i = index-1; i >= 0; i--)
					{
						//add blast from the past
						Variable var2 = vsb.get(i);
	
						//Initalize the input msg
						BlastFromThePastFactor f = fg.addBlastFromPastFactor(var2,nextPort.getSiblingPort());
	
						bfc.add(f);
						
						//Set the next port
						nextPort = f.getPorts().get(0);
					}
				}

			}
		}

	}

	public FactorGraph getParameterFactorGraph()
	{
		
		return _parameterFactorGraph;
	}

	public int getBufferSize()
	{
		return _nestedGraphs.size();
	}

	public void setBufferSize(int size)
	{
		if (size > _bufferSize)
		{
			for (int i = 0; i < size-_bufferSize; i++)
				addStep();

			_bufferSize = size;
		}
		else if (size < _bufferSize)
		{
			for (int i = _bufferSize-1; i >= size; i--)
			{
				_graph.remove(_nestedGraphs.get(i));
				_nestedGraphs.remove(i);
			}
			for (VariableStreamBase v : _variableStreams)
				v.cleanupUnusedVariables();
		}
	}

	private HashMap<Variable, ParameterBlastFromThePastHandler> _parameter2blastFromThePastHandler = new HashMap<Variable, FactorGraphStream.ParameterBlastFromThePastHandler>();
	
	private class ParameterBlastFromThePastHandler
	{
		private Variable _otherVar;
		private Variable _myVar;
		private FactorGraph _fg;
		private BlastFromThePastFactor _mainFlastFromThePast;
		private ArrayList<BlastFromThePastFactor> _allBlastFromThePasts = new ArrayList<BlastFromThePastFactor>();
		
		public ParameterBlastFromThePastHandler(Variable var,FactorGraph fg,
				BlastFromThePastFactor originalPlastFromPast)
		{
			_otherVar = var;
			_myVar = _otherVar.clone();
			_myVar.setInputObject(null);
			_fg = fg;
			_mainFlastFromThePast = originalPlastFromPast;
			Port factorPort = originalPlastFromPast.getPorts().get(0);
			//   create a data structure to represent it
			//   Add a blast from the past for this variable
			//   Create a Factor Graph for this variable (maybe share with others)
			//   Add a blast to the past to be paired with the blast from the past
			addBlastFromThePast(factorPort.getSiblingPort());
		}
		
		public void addBlastFromThePast(Port p)
		{
			_allBlastFromThePasts.add(_fg.addBlastFromPastFactor( _myVar, p));
		}
		
		public void advance()
		{
			final ISolverFactor sfactor = requireNonNull(_mainFlastFromThePast.getSolver());
			
			for (BlastFromThePastFactor f : _allBlastFromThePasts)
				f.advance();
			
			Object belief = _myVar.getBeliefObject();
			sfactor.setOutputMsg(0, Objects.requireNonNull(belief));
			
		}
	}

	public void advance()
	{
	
		//Deal with parameters
		//for each parameter
			//Get data structure associated with that parameter
			//Tell that data structure to advance
		for (ParameterBlastFromThePastHandler h : _parameter2blastFromThePastHandler.values())
		{
			h.advance();
		}
		
		//For each blast from the past chain
		for (ArrayList<BlastFromThePastFactor> al : _blastFromThePastChains)
		{
			//For each blast from the past
			for (BlastFromThePastFactor bfp : al)
			{
				//Get new message
				bfp.advance();
			}
		}

		//For each graph in list of nested graphs
		for (int j = 0; j < _nestedGraphs.size()-1; j++)
		{
			//Tell it to move all factor messages to left
			final ISolverFactorGraph otherGraph = requireNonNull(_nestedGraphs.get(j+1).getSolver());
			requireNonNull(_nestedGraphs.get(j).getSolver()).moveMessages(otherGraph);
		}

		//Newest nested graph should initialiaze its messages
		_nestedGraphs.get(_nestedGraphs.size()-1).recreateMessages();
		

	}



	public boolean hasNext()
	{
        for (VariableStreamBase s : _variableStreams)
        {
                if (!s.hasNext())
                        return false;
        }
        return true;
        
	}

	private void addStep()
	{
		Variable [] boundaryVariables = new Variable[_args.length];
		for (int j = 0; j < _args.length; j++)
		{
			if (_args[j] instanceof IVariableStreamSlice)
				boundaryVariables[j] = ((IVariableStreamSlice)_args[j]).get(_nestedGraphs.size(),true);
			else
				boundaryVariables[j] = (Variable)_args[j];
		}
		//Add nested graph
		FactorGraph ng = _graph.addFactor(_repeatedGraph, boundaryVariables);
		_nestedGraphs.add(ng);
	}

	private @Nullable VariableStreamBase getVariableStream(Variable var)
	{
		for (int i = 0; i < _variableStreams.size(); i++)
		{
			if (_variableStreams.get(i).contains(var))
				return _variableStreams.get(i);
		}
		return null;
	}

	
	public ArrayList<FactorGraph> getNestedGraphs()
	{
		return _nestedGraphs;
	}
}
