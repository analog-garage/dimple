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

package com.analog.lyric.dimple.model.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionBase;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.JointFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.factors.FactorList;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.repeated.FactorGraphStream;
import com.analog.lyric.dimple.model.repeated.IVariableStreamSlice;
import com.analog.lyric.dimple.model.repeated.VariableStreamBase;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.schedulers.DefaultScheduler;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.util.misc.FactorGraphDiffs;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.Internal;
import com.analog.lyric.util.misc.MapList;



public class FactorGraph extends FactorBase
{
	private VariableList _ownedVariables = new VariableList();
	private VariableList _boundaryVariables = new VariableList();
	private MapList<FactorBase> _ownedFactors = new MapList<FactorBase>();
	private ArrayList<FactorGraph> _ownedSubGraphs = new ArrayList<FactorGraph>();
	private ISchedule _schedule = null;
	private IScheduler _associatedScheduler = null;
	private IScheduler _solverSpecificDefaultScheduler = null;
	private long _versionId = 0;
	private long _scheduleVersionId = 0;
	private long _scheduleAssociatedGraphVerisionId = -1;
	private IFactorGraphFactory _solverFactory;
	private ISolverFactorGraph _solverFactorGraph;
	private JointFactorCache _jointFactorCache = new JointFactorCache();
	private HashSet<VariableStreamBase> _variableStreams = new HashSet<VariableStreamBase>();
	private ArrayList<FactorGraphStream> _factorGraphStreams = new ArrayList<FactorGraphStream>();
	private int _numSteps = 1;
	private boolean _numStepsInfinite = true;

	//new identity related members
	private HashMap<String, Object> _name2object = new HashMap<String, Object>();
	private HashMap<UUID, Object> _UUID2object = new HashMap<UUID, Object>();



	/***************************************************************
	 * 
	 * Constructors
	 * 
	 ******************************************************************/

	public FactorGraph()
	{
		this(new VariableBase[0], "");
	}
	public FactorGraph(String name)
	{
		this(null, name);
	}
	public FactorGraph(VariableBase ... boundaryVariables)
	{
		this(boundaryVariables, "");
	}

	public FactorGraph(VariableBase[] boundaryVariables, String name)
	{
		this(boundaryVariables, name, Model.getInstance().getDefaultGraphFactory());
	}



	public FactorGraph(VariableBase[] boundaryVariables, String name, IFactorGraphFactory solver)
		{
		if (boundaryVariables != null)
			addBoundaryVariables(boundaryVariables);

		if(name == ""){name = null;}
		setName(name);

		if(solver != null)
			setSolverFactory(solver);
		}

	@Override
	public final FactorGraph asFactorGraph()
	{
		return this;
	}

	@Override
	public final boolean isFactorGraph()
	{
		return true;
	}

	@Override
	public String getClassLabel()
	{
		return "Graph";
	}


	/***************************************************************
	 * 
	 * Solver stuff
	 * @stuff
	 * 
	 ******************************************************************/

	private void setSolverFactorySubGraph(IFactorGraphFactory factory)
	{
		_solverFactory = factory;
		_solverFactorGraph = factory != null ? factory.createFactorGraph(this) : null;

	}
	private void setSolverFactorySubGraphRecursive(IFactorGraphFactory factory)
	{
		setSolverFactorySubGraph(factory);
		for (FactorGraph fg : getNestedGraphs())
			fg.setSolverFactorySubGraphRecursive(factory);

	}

	public void setSolverFactory(IFactorGraphFactory factory)
	{

		setSolverFactorySubGraph(factory);

		for (VariableBase var : getVariablesFlat())
			var.createSolverObject(_solverFactorGraph);

		for (FactorGraph fg : getNestedGraphs())
			fg.setSolverFactorySubGraphRecursive(factory);

		for (Factor f : getNonGraphFactorsFlat())
			f.createSolverObject(_solverFactorGraph);

		if (_solverFactorGraph != null)
		{
			_solverFactorGraph.postSetSolverFactory();
		}
	}



	/***************************************************************
	 * 
	 * Tables and Functions
	 * 
	 ******************************************************************/

	/** */
	public static boolean allDomainsAreDiscrete(VariableBase [] vars)
	{
		for (int i = 0; i < vars.length; i++)
		{
			if (!vars[i].getDomain().isDiscrete())
				return false;
		}

		return true;
	}

    public BlastFromThePastFactor addBlastFromPastFactor(VariableBase var,Port factorPort)
    {

            setVariableSolver(var);

            BlastFromThePastFactor f;
            f = new BlastFromThePastFactor(NodeId.getNext(), var,factorPort);

            addFactor(f,new VariableBase[]{var});

    		if (_solverFactorGraph != null)
    			f.createSolverObject(_solverFactorGraph);

            return f;

    }

	public FactorGraphStream addRepeatedFactor(FactorGraph nestedGraph, Object ... vars)
	{
		return addRepeatedFactorWithBufferSize(nestedGraph,1, vars);
	}


	public FactorGraphStream addRepeatedFactorWithBufferSize(FactorGraph nestedGraph, int bufferSize,Object ... vars)
	{

		FactorGraphStream fgs = new FactorGraphStream(this, nestedGraph, bufferSize, vars);
		_factorGraphStreams.add(fgs);
		for (Object v : vars)
		{
			if (v instanceof IVariableStreamSlice)
			{
				_variableStreams.add(((IVariableStreamSlice) v).getStream());
			}
		}
		return fgs;
	}

	public void setNumStepsInfinite(boolean inf)
	{
		_numStepsInfinite = inf;
	}
	public boolean getNumStepsInfinite()
	{
		return _numStepsInfinite;
	}

	public void setNumSteps(int numSteps)
	{
		_numSteps = numSteps;
	}
	public int getNumSteps()
	{
		return _numSteps;
	}


	public void advance()
	{

		for (VariableStreamBase vs : _variableStreams)
		{
			vs.advanceState();
		}
		for (FactorGraphStream s : _factorGraphStreams)
		{
			s.advance();
		}

		getSolver().postAdvance();

	}

	public boolean hasNext()
	{
		if (_factorGraphStreams.size() == 0)
			return false;

		for (FactorGraphStream s : _factorGraphStreams)
			if (!s.hasNext())
				return false;

		return true;
	}

	public ArrayList<FactorGraphStream> getFactorGraphStreams()
	{
		return _factorGraphStreams;
	}

	public Factor addFactor(int [][] indices, double [] weights, Discrete ... vars)
	{
		return addFactor(FactorTable.create(indices, weights, vars),vars);
	}

	public Factor addFactor(IFactorTable ft, VariableBase ... vars)
	{
		return addFactor(new TableFactorFunction("TableFactorFunction",ft),vars);
	}

	public Factor addFactor(FactorFunction factorFunction, VariableBase ... vars)
	{
		return addFactor(factorFunction,(Object[])vars);
	}

	public Factor addFactor(FactorFunction factorFunction, Object ... vars)
	{
		int numConstants = 0;

		for (int i = 0; i < vars.length; i++)
		{
			if (!(vars[i] instanceof VariableBase))
				numConstants++;
		}

		VariableBase [] newvars = new VariableBase[vars.length - numConstants];
		Object [] constants = new Object[numConstants];
		int [] constantIndices = new int[numConstants];

		int constantIndex = 0;
		int varIndex = 0;

		for (int i = 0; i < vars.length; i++)
		{
			if (vars[i] instanceof VariableBase)
			{
				newvars[varIndex] = (VariableBase)vars[i];
				varIndex++;
			}
			else
			{
				constants[constantIndex] = vars[i];
				constantIndices[constantIndex] = i;
				constantIndex++;
			}
		}


		if (numConstants == 0)
		{
			return addFactorNoConstants(factorFunction,newvars);
		}
		else
		{
			return addFactorNoConstants(new FactorFunctionWithConstants(factorFunction, constants, constantIndices),newvars);
		}

	}

	private Factor addFactorNoConstants(FactorFunction factorFunction, VariableBase ... vars)
	{
		if (vars.length == 0)
			throw new DimpleException("must pass at least one variable to addFactor");

		for (VariableBase v : vars)
			setVariableSolver(v);

		//TODO: where did the name go?

		Factor f;
		if (allDomainsAreDiscrete(vars))
			f = new DiscreteFactor(NodeId.getNext(),factorFunction,vars);
		else
			f = new Factor(NodeId.getNext(),factorFunction,vars);

		addFactor(f,vars);

		if (_solverFactorGraph != null)
		{
			f.createSolverObject(_solverFactorGraph);
			_solverFactorGraph.postAddFactor(f);
		}



		return f;

	}


	private void setVariableSolver(VariableBase v)
	{
		if (_solverFactorGraph != null)
		{
			//check to see if variable belongs to this graph
			if (!variableBelongs(v))
			{
				if (v.getSiblings().size() > 0)
					throw new DimpleException("Can't connect a variable to multiple graphs");

				v.createSolverObject(_solverFactorGraph);
			}
		}
	}

	private boolean variableBelongs(VariableBase v)
	{
		if (_ownedVariables.contains(v))
			return true;
		if (_boundaryVariables.contains(v))
			return true;

		for (FactorGraph fg : getNestedGraphs())
			if (fg.variableBelongs(v))
				return true;

		return false;

	}

	public void removeVariables(VariableBase ... variables)
	{
		for (VariableBase v : variables)
		{
			remove(v);
		}
	}

	public void remove(VariableBase v)
	{
		if (v.getSiblings().size() != 0)
			throw new DimpleException("can only remove a variable if it is no longer connected to a factor");

		if (!_ownedVariables.contains(v))
			throw new DimpleException("can only currently remove variables that are owned");
		_ownedVariables.remove(v);
		if (_ownedVariables.contains(v))
			throw new DimpleException("eh?");
		v.createSolverObject(null);
		v.setParentGraph(null);
		removeNode(v);

	}
		
	public void addBoundaryVariables(VariableBase ... vars)
	{
		for (VariableBase v : vars)
		{
			setVariableSolver(v);
	
			//if (_boundaryVariables.contains(v))
			//	throw new DimpleException("ERROR name [" + v.getName() + "] already a boundary variable");
	
			if (_ownedVariables.contains(v))
				_ownedVariables.remove(v);
	
			_boundaryVariables.add(v);
	
	
			UUID uuid = v.getUUID();
			String explicitName = v.getExplicitName();
	
			if(explicitName != null && getObjectByName(explicitName) != null && v.getParentGraph() != this)
			{
				throw new DimpleException("ERROR name [" + explicitName + "] already in graph");
	}
	
	
			if(explicitName != null)
			{
				_name2object.put(explicitName, v);
			}
	
			_UUID2object.put(uuid, v);
	
			//being the root, at least for the moment,
			//I'm this variable's owner, if it has no other
			if(v.getParentGraph() == null)
			{
				v.setParentGraph(this);
			}
		}

	}

	
	public void addVariables(VariableBase... variables)
	{
		for (VariableBase v : variables)
		{
			if (_boundaryVariables.contains(v))
			{
				throw new DimpleException("Cannot take ownership of boundary variable [" + v.getLabel() + "]");
			}
			if(v.hasParentGraph())
			{
				throw new DimpleException("Variable [" + v.getLabel() + "] already owned by graph [" + v.getParentGraph().getLabel() + "]");
			}
			addOwnedVariable(v);
			v.createSolverObject(_solverFactorGraph);

		}
	}

	/*
	 * Joining factors replaces all the original factors with one joint factor.
	 * We take the cartesian product of the rows of the tables such that the
	 * variables values are consistent.
	 */
	public Factor join(Factor ... factors)
	{
		if (factors.length < 2)
			throw new DimpleException("need at least two factors for join");

		Factor current = factors[0];
		for (int i = 1; i < factors.length; i++)
			current = current.join(factors[i]);

		_versionId++;

		return current;
	}


	@Internal
	public JointFactorFunction getJointFactorFunction(
			FactorFunctionBase ff1,
			FactorFunctionBase ff2,
			ArrayList<Integer> map1,
			ArrayList<Integer> map2
			)
	{

		return _jointFactorCache.get(ff1, ff2, map1, map2);
	}

	/*
	 * This class is used to speed up the time it takes to create graphs
	 * that join similar factors multiple times.  In addition to speeding
	 * up graph creation, this will reduce memory usage.
	 */
	public class JointFactorCache
	{
		private HashMap<ArrayList<Object>,JointFactorFunction> _cache = new HashMap<ArrayList<Object>, JointFactorFunction>();

		/*
		 * We have to use both the factor functions and the mappings as keys.
		 * The mappings relate the arguments of the joint factor to the original
		 * arguments of each individual factor.
		 */
		public JointFactorFunction get(FactorFunctionBase f1,
				FactorFunctionBase f2,
				ArrayList<Integer> map1, ArrayList<Integer> map2)
		{

			ArrayList<Object> key = new ArrayList<Object>();
			key.add(f1);
			key.add(f2);
			key.add(map1.clone());
			key.add(map2.clone());

			if (!_cache.containsKey(key))
			{
				String newname = f1.getName()+"_"+f2.getName();
				JointFactorFunction jff = new JointFactorFunction(newname, f1, f2, map1, map2);
				_cache.put(key, jff);
			}

			return _cache.get(key);
		}
	}

	/*
	 * Joining variables creates one joint and discards the originals and modifies
	 * factors to refer to the joints.
	 */
	public VariableBase join(VariableBase ... variables)
	{
		if (variables.length < 2)
			throw new DimpleException("need at least two variables");

		//If these variables weren't previously part of the graph, add them.
		for (int i = 0; i < variables.length; i++)
		{
			if (variables[i].getParentGraph()==null)
				addVariables(variables[i]);
		}

		//Create a hash of all factors affected.
		HashSet<Factor> factors = new HashSet<Factor>();

		//Go through variables and find affected factors.
		for (VariableBase v : variables)
		{
			for (int i = 0; i < v.getSiblings().size(); i++)
			{
				Factor f = (Factor)v.getConnectedNodeFlat(i);
				factors.add(f);
			}
		}

		//Create joint variable
		VariableBase joint = variables[0].createJointNoFactors(variables);

		//Variables must first be part of the graph before the factor can join them.
		addVariables(joint);

		//Reattach the variable too, just in case.
		joint.createSolverObject(_solverFactorGraph);

		//go through each factor that was connected to any of the variables and tell it to join those variables
		for (Factor f : factors)
		{
			f.replaceVariablesWithJoint(variables, joint);

			//reattach to the solver now that the factor graph has changed
			f.createSolverObject(_solverFactorGraph);
		}


		//Remove the original variables
		removeVariables(variables);

		//update the version Id so that we can recalculate the schedule
		_versionId++;

		return joint;
	}

	/*
	 * Splitting a variable creates a copy and an equals node between the two.
	 */
	public VariableBase split(VariableBase variable)
	{
		return split(variable,new Factor[]{});
	}

	/*
	 * Splitting a variable creates a copy and an equals node between the two.
	 * The Factor array specifies which factors should connect to the new variable.
	 * All factors left out of hte array remain pointing to the original variable.
	 */
	public VariableBase split(VariableBase variable,Factor ... factorsToBeMovedToCopy)
	{
		return variable.split(this,factorsToBeMovedToCopy);
	}


	private FactorBase addFactor(FactorBase function, VariableBase[] variables)
	{
		// Add variables to owned variable list if not a boundary variable
		for (VariableBase v : variables)
		{

			if (!_boundaryVariables.contains(v))
			{
				addOwnedVariable(v);
			}

		}

		addNameAndUUID(function);
		function.setParentGraph(this);

		_ownedFactors.add(function);
		_versionId++;							// The graph has changed
		return function;
	}


	public boolean customFactorExists(String funcName)
	{
		if (_solverFactorGraph != null)
			return _solverFactorGraph.customFactorExists(funcName);
		else
			return false;
	}
	/***************************************************************
	 * 
	 * Scheduling
	 * @Scheduling
	 * 
	 ******************************************************************/



	public void setSchedule(ISchedule schedule)
	{
		schedule.attach(this);
		_schedule = schedule;
		_scheduleVersionId++;
		_scheduleAssociatedGraphVerisionId = _versionId;

	}
	public ISchedule getSchedule()
	{
		_createScheduleIfNeeded();
		return _schedule;
	}

	// This association is maintained by a FactorGraph object so that it can use
	// it when a graph is cloned in the copy constructor used by addGraph.
	public void associateScheduler(IScheduler scheduler)
	{
		_associatedScheduler = scheduler;
	}

	public void setScheduler(IScheduler scheduler)
	{
		associateScheduler(scheduler);		// Associate the scheduler with this graph
		_createSchedule();					// Create the schedule using this scheduler
	}

	public IScheduler getAssociatedScheduler()
	{
		return _associatedScheduler;
	}

	// Allow a specific scheduler to set a default scheduler that overrides the normal default
	// This would be used if the client doesn't otherwise specify a schedule
	public void setSolverSpecificDefaultScheduler(IScheduler scheduler)
	{
		_solverSpecificDefaultScheduler = scheduler;
	}

	private void _createSchedule()
	{
		IScheduler scheduler = getAssociatedScheduler();				// Get the scheduler associated with this graph
		if (scheduler != null)											// Use the scheduler defined for the graph if there is one
			setSchedule(scheduler.createSchedule(this));
		else if (_solverSpecificDefaultScheduler != null)				// Otherwise, use the default scheduler specified by the solver derived class, if there is one (optional solver-specific default scheduler)
			setSchedule(_solverSpecificDefaultScheduler.createSchedule(this));
		else															// Use default scheduler if no other is specified
			setSchedule(new DefaultScheduler().createSchedule(this));
	}

	private void _createScheduleIfNeeded()
	{
		// If there's no schedule yet, or if it isn't up-to-date, then create one
		// Otherwise, don't bother to spend the time since there's already a perfectly good schedule
		if (!isUpToDateSchedulePresent())
			_createSchedule();
	}


	// This allows the caller to determine if the scheduler will be run in solve
	public boolean isUpToDateSchedulePresent()
	{
		return _schedule != null && _scheduleAssociatedGraphVerisionId == _versionId;
	}




	/***************************************************************
	 * 
	 * Nested Graphs
	 * 
	 ******************************************************************/

	public FactorGraph addFactor(FactorGraph subGraph, VariableBase ... boundaryVariables)
	{
		return addGraph(subGraph,boundaryVariables);
	}

	// Add a subgraph
	public FactorGraph addGraph(FactorGraph subGraph, VariableBase ... boundaryVariables)
	{

		//TODO: helper function
		//TODO: or do I do this below constructor?
		for (VariableBase v : boundaryVariables)
			setVariableSolver(v);

		//copy the graph
		FactorGraph subGraphCopy = new FactorGraph(boundaryVariables, subGraph,this);

		if (_solverFactory != null)
		{
			subGraphCopy.setSolverFactory(_solverFactory);
		}

		//tell us about it
		addNameAndUUID(subGraphCopy);
		_ownedFactors.add(subGraphCopy);
		_ownedSubGraphs.add(subGraphCopy);

		//tell us about it and it about us
		//subGraphCopy._setParentGraph(this);


		for (VariableBase v : boundaryVariables)			// Add variables to owned variable list if not a boundary variable
			if (!_boundaryVariables.contains(v))
			{
				addOwnedVariable(v);
			}

		_versionId++;							// The graph has changed

		return subGraphCopy;
	}


	private void _setParentGraph(FactorGraph parentGraph)
	{
		boolean noLongerRoot = parentGraph != null && getParentGraph() == null;
		setParentGraph(parentGraph);

		//If we were root, and are no longer,
		//		stop references names/UUIDs of boundary variables.
		if(noLongerRoot)
		{
			for(VariableBase v : _boundaryVariables)
			{
				_UUID2object.remove(v.getUUID());
				String explicitName = v.getExplicitName();
				if(explicitName != null)
				{
					_name2object.remove(explicitName);
				}
				if(v.getParentGraph() == this)
				{
					v.setParentGraph(null);
				}
			}
		}
	}


	private FactorGraph(VariableBase[] boundaryVariables,
			FactorGraph templateGraph,
			FactorGraph parentGraph)
			{
		this(boundaryVariables,
				templateGraph,
				parentGraph,
				false,
				new HashMap<Object, Object>());
			}

	// Copy constructor -- create a graph incorporating all of the variables, functions, and sub-graphs of the template graph
	private FactorGraph(VariableBase[] boundaryVariables,
			FactorGraph templateGraph,
			FactorGraph parentGraph,
			boolean copyToRoot,
			HashMap<Object, Object> old2newObjs)
			{
		this(boundaryVariables,
				templateGraph.getExplicitName(),
				null);

		// Copy owned variables
		for (VariableBase vTemplate : templateGraph._ownedVariables)
		{
			VariableBase vCopy = vTemplate.clone();

			//old2newIds.put(vTemplate.getId(), vCopy.getId());
			old2newObjs.put(vTemplate,vCopy);
			addOwnedVariable(vCopy);
		}

		// Check boundary variables for consistency
		if (boundaryVariables == null)
		{
			throw new DimpleException("Sub-graph missing boundary variables to connect with parent graph.");
		}
		if (boundaryVariables.length != templateGraph._boundaryVariables.size())
		{
			throw new DimpleException(String.format("Boundary variable list does not have the same length (%d) as template graph (%d)\nTemplate graph:[%s]"
					, boundaryVariables.length
					, templateGraph._boundaryVariables.size()
					, templateGraph.toString()));
		}

		{
			int i = 0;
			for (VariableBase vTemplate : templateGraph._boundaryVariables)
			{
				VariableBase vBoundary = boundaryVariables[i++];
				if (!vBoundary.getDomain().equals(vTemplate.getDomain()))
					throw new DimpleException("Boundary variable does not have the same domain as template graph.  Index: " + (i-1));

				//old2newIds.put(vTemplate.getId(), vBoundary.getId());
				old2newObjs.put(vTemplate,vBoundary);
			}
		}

		for (FactorBase fb : templateGraph._ownedFactors)
		{
			FactorGraph subGraph = fb.asFactorGraph();
			if (subGraph != null)
			{

				VariableBase[] vBoundary = new VariableBase[subGraph._boundaryVariables.size()];
				{
					int i = 0;
					for (VariableBase v : subGraph._boundaryVariables)
						vBoundary[i++] = (VariableBase)old2newObjs.get(v);
				}
				FactorGraph newGraph = addGraph(subGraph, vBoundary);	// Add the graph using the appropriate boundary variables
				old2newObjs.put(subGraph,newGraph);


			}
			else
			{
				Factor fTemplate = fb.asFactor();
				Factor fCopy = fTemplate.clone();
				old2newObjs.put(fTemplate,fCopy);

				addNameAndUUID(fCopy);
				fCopy.setParentGraph(this);
				_ownedFactors.add(fCopy);
				for (INode n : fTemplate.getSiblings())
				{
					VariableBase vTemplate = (VariableBase)n;
					VariableBase var = (VariableBase)old2newObjs.get(vTemplate);
					fCopy.getSiblings().add(var);
					if (templateGraph._boundaryVariables.contains(vTemplate))
					{
						var.connect(fCopy);		// Boundary variable in template graph: connect port to corresponding boundary variable in this graph
					}
					else
						var.connect(fCopy);			// Owned variable in template graph: connect port to new copy of template variable
				}
			}
		}

		// If there's a scheduler associated with the template graph, copy it
		// for use to this graph. But leave the creation of a schedule for later
		// when the parent graph's schedule is created.
		_associatedScheduler = templateGraph._associatedScheduler;

		_setParentGraph(parentGraph);

		//Now that we've copied the graph, let's copy the Schedule if it's
		//already been created.
		if (templateGraph._schedule != null)
		{
			ISchedule scheduleCopy = copyToRoot ?
					templateGraph._schedule.copyToRoot(old2newObjs) :
						templateGraph._schedule.copy(old2newObjs);
					setSchedule(scheduleCopy);
		}

		//setSolverFactory(templateGraph.getFactorGraphFactory());
	}

	public FactorGraph copyRoot()
	{
		return copyRoot(new HashMap<Object, Object>());
	}
	public FactorGraph copyRoot(HashMap<Object, Object> old2newObjs)
	{
		FactorGraph root = getRootGraph();

		Collection<VariableBase> rootBoundaryVariablesCollection = root.getBoundaryVariables().values();
		int numBoundaryVariables = rootBoundaryVariablesCollection.size();
		VariableBase[] rootBoundaryVariables
		= rootBoundaryVariablesCollection.toArray(new VariableBase[numBoundaryVariables]);
		VariableBase[] boundaryVariables = new VariableBase[numBoundaryVariables];
		for(int i = 0; i < numBoundaryVariables; ++i)
		{
			boundaryVariables[i] = rootBoundaryVariables[i].clone();
		}

		FactorGraph rootCopy = new FactorGraph(boundaryVariables,
				root,
				null,
				true,
				old2newObjs);

		return rootCopy;
	}



	private long _portVersionId = -1;
	private ArrayList<Port> _ports;

	@Override
	public ArrayList<Port> getPorts()
	{
		if (_portVersionId == _versionId && _ports != null)
			return _ports;

		ArrayList<Port> _ports = new ArrayList<Port>();

		FactorList factors = getNonGraphFactorsFlat();

		//for each boundary variable
		for (FactorBase f : factors)
		{
			for (int i = 0; i < f.getSiblings().size(); i++)
			{
				if (_boundaryVariables.contains(f.getSiblings().get(i)))
				{
					_ports.add(new Port(f,i));
				}
			}
		}
		_portVersionId = _versionId;
		return _ports;
	}

	@Override
	public ArrayList<INode> getSiblings()
	{
		ArrayList<Port> ports = getPorts();
		_siblings = new ArrayList<INode>();
		for (Port p : ports)
			_siblings.add(p.node.getSiblings().get(p.index));
		return _siblings;

	}


	/***************************************************************
	 * 
	 * Operations on FactorGraph
	 * 
	 ******************************************************************/

	/*
	 * This method tries to optimize the BetheFreeEnergy by searching over the space of
	 * FactorTable values.
	 * 
	 * numRestarts - Determines how many times to randomly initialize the FactorTable parameters
	 *               Because the BetheFreeEnergy function is not convex, random restarts can help
	 *               find a better optimum.
	 * numSteps - How many times to change each parameter
	 * 
	 * stepScaleFactor - What to mutliply the gradient by for each step.
	 */
	public void estimateParameters(Object [] factorsAndTables,int numRestarts,int numSteps, double stepScaleFactor)
	{
		HashSet<IFactorTable> sfactorTables = new HashSet<IFactorTable>();
		for (Object o : factorsAndTables)
		{
			if (o instanceof Factor)
			{
				Factor f = (Factor)o;
				sfactorTables.add(f.getFactorTable());
			}
			else if (o instanceof IFactorTable)
			{
				sfactorTables.add((IFactorTable)o);
			}
		}

		IFactorTable [] factorTables = new IFactorTable[sfactorTables.size()];
		int i = 0;
		for (IFactorTable ft : sfactorTables)
		{
			factorTables[i] = ft;
			i++;
		}
		estimateParameters(factorTables,numRestarts,numSteps,stepScaleFactor);

	}

	public void baumWelch(Object [] factorsAndTables,int numRestarts,int numSteps)
	{
		HashSet<IFactorTable> sfactorTables = new HashSet<IFactorTable>();
		for (Object o : factorsAndTables)
		{
			if (o instanceof Factor)
			{
				Factor f = (Factor)o;
				sfactorTables.add(f.getFactorTable());
			}
			else if (o instanceof IFactorTable)
			{
				sfactorTables.add((IFactorTable)o);
			}
		}

		IFactorTable [] factorTables = new IFactorTable[sfactorTables.size()];
		int i = 0;
		for (IFactorTable ft : sfactorTables)
		{
			factorTables[i] = ft;
			i++;
		}
		baumWelch(factorTables,numRestarts,numSteps);
	}

	public void baumWelch(IFactorTable [] tables,int numRestarts,int numSteps)
	{
		getSolver().baumWelch(tables, numRestarts, numSteps);

	}

	public void estimateParameters(IFactorTable [] tables,int numRestarts,int numSteps, double stepScaleFactor)
	{
		getSolver().estimateParameters(tables, numRestarts, numSteps,  stepScaleFactor);

	}



	private void addOwnedVariable(VariableBase variable)
	{
		//Only insert if not already there.
		if(addNameAndUUID(variable))
		{
			//Tell variable about us...
			variable.setParentGraph(this);
			//...and us about the variable
			_ownedVariables.add(variable);
		}
	}

	@Override
	public void initialize(int portNum)
	{
		throw new DimpleException("not supported");
	}

	public void recreateMessages()
	{
		for (VariableBase v : getVariablesFlat())
			v.getSolver().createNonEdgeSpecificState();

		for (Factor f : getNonGraphFactorsFlat() )
			f.getSolver().createMessages();

	}

	@Override
	public void initialize()
	{
		for (VariableBase v : _ownedVariables)
			v.initialize();
		if (!hasParentGraph())			// Initialize boundary variables only if there's no parent to do it
			for (VariableBase v : _boundaryVariables)
				v.initialize();

		for (Factor f : getNonGraphFactorsTop())
			f.initialize();
		for (FactorGraph g : getNestedGraphs())
			g.initialize();


		if (_solverFactorGraph != null)
			_solverFactorGraph.initialize();
	}

	private void checkSolverIsSet()
	{
		if (_solverFactorGraph == null)
			throw new DimpleException("solver needs to be set first");
	}

	public void solve()
	{
		checkSolverIsSet();
		_solverFactorGraph.solve();
	}

	public void solveOneStep()
	{
		checkSolverIsSet();
		_solverFactorGraph.solveOneStep();
	}


	public void continueSolve()
	{
		checkSolverIsSet();
		_solverFactorGraph.continueSolve();
	}

	public void remove(FactorGraph factorGraph)
	{
		VariableList varList = factorGraph.getVariablesFlat();
		IMapList<FactorBase> factors = factorGraph.getFactorsTop();

		VariableList boundary = factorGraph.getBoundaryVariables();

		VariableBase [] arr = new VariableBase[varList.size()];
		for (int i = 0; i < arr.length; i++)
			arr[i] = varList.getByIndex(i);

		for (FactorBase f : factors)
		{
			FactorGraph subgraph = f.asFactorGraph();
			if (subgraph != null)
				factorGraph.remove(subgraph);
			else
				factorGraph.remove(f.asFactor());
		}

		removeVariables(arr);
		removeNode(factorGraph);
		_ownedFactors.remove(factorGraph);
		_ownedSubGraphs.remove(factorGraph);

		for (VariableBase v : boundary)
		{
			if (v.getSiblings().size() == 0)
				remove(v);
		}

	}

	private void removeNode(Node n)
	{
		_UUID2object.remove(n.getUUID());
		String explicitName = n.getExplicitName();
		if(explicitName != null)
		{
			_name2object.remove(explicitName);
		}

	}

	public void remove(Factor factor)
	{
		//_ownedFactors;
		if (!getNonGraphFactorsTop().contains(factor))
			throw new DimpleException("Cannot delete factor.  It is not a member of this graph");

		_ownedFactors.remove(factor);
		removeNode(factor);

		for (VariableBase var : factor.getVariables())
		{
			var.remove(factor);
		}

		_versionId++;							// The graph has changed
	}

	/*********************************************************
	 * 
	 * Graph algorithms
	 * 
	 *********************************************************/

	public boolean isTree()
	{
		return isTreeFlat();
	}

	public boolean isTreeFlat()
	{
		return isTree(Integer.MAX_VALUE);
	}

	public boolean isTreeTop()
	{
		return isTree(0);
	}

	@SuppressWarnings("unchecked")
	public boolean isTree(int relativeNestingDepth)
	{
		FactorGraph g = this;
		// Get all the nodes in the graph and all sub-graphs--both variables and
		// functions (not including boundary variables unless this graph has no
		// parents, since those will be updated only in that case)
		MapList<FactorBase> allIncludedFunctions = g.getFactors(relativeNestingDepth);
		VariableList allIncludedVariables = g.getVariables(relativeNestingDepth);

		// Determine the total number of edges in the graph, including
		// all sub-graphs. Since this is a bipartite graph, we can just count
		// all ports associated with the variables in the graph
		int numEdges = 0;
		for (VariableBase v : allIncludedVariables)
			numEdges += v.getSiblings().size();


		// Determine the total number of vertices (variable and function nodes)
		// in the graph, including all sub-graphs
		int numVertices = allIncludedVariables.size() + allIncludedFunctions.size();

		//If there are no variables or functions, this is definitely a tree
		if (numVertices == 0)
			return true;

		// If the number of edges is greater than the number of vertices minus 1, there must be cycles
		// If the number of edges is less than the number of vertices minus 1, it must not be connected
		if (numEdges != numVertices - 1) return false;

		// If it has the right number of edges, the either it's a tree or it
		// isn't a connected graph, and could either be a 'forest' or have cycles.
		// Though it could be a 'forest', which could have a fast schedule,
		// creating a custom schedule for this case isn't currently support.

		// First, for a list of all included nodes
		@SuppressWarnings("rawtypes")
		MapList allIncludedNodes = new MapList();
		allIncludedNodes.addAll(allIncludedFunctions);
		allIncludedNodes.addAll(allIncludedVariables);

		//If this graph has no variable or factors, let's consider it a tree.
		if (allIncludedNodes.size() == 0)
			return true;

		// First, pick a node arbitrarily;
		INode n =  (INode)allIncludedNodes.getByIndex(0);

		FactorGraphWalker walker =
			new FactorGraphWalker(this, n).maxRelativeNestingDepth(relativeNestingDepth);
		while (walker.next() != null)
		{
			if (walker.getCycleCount() > 0)
			{
				return false;
			}
		}

		// No cycles were found, but we might not have visited all of the nodes in the
		// graph. If we have, its a tree.
		return walker.getVisitedNodesSize() == allIncludedNodes.size();
	}

	public int [][] getAdjacencyMatrix()
	{
		return getAdjacencyMatrixFlat();
	}

	public int [][] getAdjacencyMatrix(int relativeNestingDepth)
	{
		MapList<INode> nodes = getNodes(relativeNestingDepth);
		INode [] array = new INode[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
			array[i] = nodes.getByIndex(i);

		return getAdjacencyMatrix(array);
	}

	public int [][] getAdjacencyMatrixFlat()
	{
		return getAdjacencyMatrix(Integer.MAX_VALUE);
	}

	public int [][] getAdjacencyMatrixTop()
	{
		return getAdjacencyMatrix(0);
	}

	public int [][] getAdjacencyMatrix(MapList<INode> nodes)
	{
		INode [] inodes = new INode[nodes.size()];
		nodes.toArray(inodes);
		return getAdjacencyMatrix(inodes);
	}


	public int [][] getAdjacencyMatrix(INode [] nodes)
	{
		int [][] retval = new int[nodes.length][];

		HashMap<INode,Integer> node2index = new HashMap<INode, Integer>();

		for (int i = 0; i < nodes.length; i++)
		{
			node2index.put(nodes[i],i);
			retval[i] = new int[nodes.length];
			if (nodes[i].getRootGraph() != this.getRootGraph())
				throw new DimpleException("expected nodes that are part of this graph");
		}

		for (int i = 0; i < nodes.length; i++)
		{
			for (int k = 0; k < nodes[i].getSiblings().size(); k++)
			{
				ArrayList<INode> connectedNodes = nodes[i].getConnectedNodeAndParents(k);

				for (INode n : connectedNodes)
				{
					if (node2index.containsKey(n))
					{
						int j = node2index.get(n);
						retval[i][j] = 1;
						retval[j][i] = 1;
					}
				}
			}
		}

		return retval;

	}


	@SuppressWarnings("all")
	protected MapList<INode> depthFirstSearchRecursive(
		INode node,
		INode previousNode,
		MapList<INode> foundNodes,
		IMapList<INode> nodeList,
		int currentDepth,
		int maxDepth,
		int relativeNestingDepth)
	{

		foundNodes.add(node);						// This node has been found

		if (currentDepth < maxDepth)
		{
			//Collection<Port> ports = node.getPorts();	// Get all the edges from this node



			for (int i = 0; i < node.getSiblings().size(); i++)
			{
				INode nextNode = node.getConnectedNode(relativeNestingDepth,i);

				int nextNodeNestingDepth = nextNode.getDepth();
				int thisNodeNestingDepth = node.getDepth();

				//Deal with overflow
				int newRelativeNestingDepth = relativeNestingDepth - (nextNodeNestingDepth - thisNodeNestingDepth);

				if (newRelativeNestingDepth < 0)
					newRelativeNestingDepth = 0;

				if (nextNode != previousNode)					// Don't go backwards in the search
					if (nodeList.contains(nextNode))			// Only edges that lead to nodes inside this graph
						if (!foundNodes.contains(nextNode))		// If found-list doesn't already contain the next node
							foundNodes.addAll(depthFirstSearchRecursive(nextNode, node, foundNodes, nodeList,currentDepth+1,maxDepth,newRelativeNestingDepth));
			}
		}
		return foundNodes;
	}

	public IMapList<INode> depthFirstSearch(INode root)
	{
		return depthFirstSearchFlat(root);
	}


	public IMapList<INode> depthFirstSearch(INode root, int searchDepth)
	{
		return depthFirstSearch(root,searchDepth,Integer.MAX_VALUE);
	}


	public MapList<INode> depthFirstSearch(INode root, int searchDepth,int relativeNestingDepth)
	{
		MapList<INode> tmp = new MapList<INode>();

		if (root.getParentGraph() == null)
		{
			tmp.add(root);
			return tmp;
		}
		else
		{
			int rootDepth = root.getDepth();

			//TODO: check for overflow
			int offset = rootDepth-this.getDepth()-1;
			int newDepth = offset+relativeNestingDepth;
			if (offset > 0 && relativeNestingDepth > 0 && newDepth < 0)
				newDepth = Integer.MAX_VALUE;

			IMapList<INode> nodes = this.getNodes(newDepth);

			if (!nodes.contains(root))
				throw new DimpleException("can't search from " + root.getLabel() + " it is not a member of the graph to the specified nesting depth");
			//getNodes(relativeNestingLevel);

			//MapList<INode> tmp = new MapList<INode>();
			return depthFirstSearchRecursive(root, null, tmp, nodes, 0, searchDepth, relativeNestingDepth);
		}
	}

	public IMapList<INode> depthFirstSearchFlat(INode root, int searchDepth)
	{
		return depthFirstSearch(root, searchDepth, Integer.MAX_VALUE);
	}

	public IMapList<INode> depthFirstSearchFlat(INode root)
	{
		return 	depthFirstSearchFlat(root,Integer.MAX_VALUE);

	}

	public IMapList<INode> depthFirstSearchTop(INode root, int searchDepth)
	{
		return depthFirstSearch(root, searchDepth, 0);
	}

	public IMapList<INode> depthFirstSearchTop(INode root)
	{
		return 	depthFirstSearchFlat(root,0);

	}

	public boolean isAncestorOf(INode node)
	{
		if (node == null || node.getParentGraph() == null)
			return false;

		while (node != null)
		{
			node = node.getParentGraph();

			if (node == this)
				return true;
		}

		return false;
	}


	/***********************************************
	 * 
	 * Introspection
	 * 
	 ***********************************************/

	@Override
	public ISolverFactorGraph getSolver()
	{
		return _solverFactorGraph;
	}

	/**
	 * Returns count of variables that would be returned by {@link #getVariables()}.
	 */
	public int getVariableCount()
	{
		return getVariableCount(Integer.MAX_VALUE);
	}

	/**
	 * Returns count of variables that would be returned by {@link #getVariables(int)}.
	 */
	public int getVariableCount(int relativeNestingDepth)
	{
		int count = 0;

		// include boundary variables only if this is the root node
		if (getParentGraph() == null)
		{
			count += _boundaryVariables.size();
		}

		count += _ownedVariables.size();

		if (relativeNestingDepth > 0)
		{
			for (FactorGraph fg : getNestedGraphs())
			{
				count += fg.getVariableCount(relativeNestingDepth-1);
			}
		}

		return count;
	}

	/**
	 * Returns list of all variables in the graph including those in nested graphs.
	 */
	public VariableList getVariables()
	{
		return getVariablesFlat();
	}

	/**
	 * Returns list of all variables in the graph including those in nested graphs down to
	 * specified {@code relativeNestingDepth} below this graph, where a nesting depth of zero
	 * indicates that nested graphs should not be included.
	 */
	public VariableList getVariables(int relativeNestingDepth)
	{
		return getVariables(relativeNestingDepth,false);
	}

	public VariableList getVariables(int relativeNestingDepth,boolean forceIncludeBoundaryVariables)
	{
		VariableList retval = new VariableList();

		//include boundary variables only if this is the root node
		if (getParentGraph()==null || forceIncludeBoundaryVariables)
			retval.addAll(_boundaryVariables);

		retval.addAll(_ownedVariables);

		if (relativeNestingDepth > 0)
		{
			for (FactorGraph g : getNestedGraphs())
			{
				VariableList tmp = g.getVariables(relativeNestingDepth-1);
				retval.addAll(tmp);
			}
		}


		return retval;
	}

	public VariableList getVariablesFlat()
	{
		return getVariablesFlat(false);
	}

	public VariableList getVariablesFlat(boolean forceIncludeBoundaryVariables)
	{
		return getVariables(Integer.MAX_VALUE,forceIncludeBoundaryVariables);
	}

	public VariableList getVariablesTop()
	{
		return getVariablesTop(false);
	}

	public VariableList getVariablesTop(boolean forceIncludeBoundaryVariables)
	{
		return getVariables(0,forceIncludeBoundaryVariables);
	}


	public VariableList getBoundaryVariables()
	{
		return _boundaryVariables;
	}


	public boolean isBoundaryVariable(VariableBase mv)
	{
		return _boundaryVariables.contains(mv);
	}

	public VariableBase getVariable(int id)
	{
		VariableBase v;
		v = _ownedVariables.getByKey(id);
		if (v != null) return v;
		v = _boundaryVariables.getByKey(id);
		if (v != null) return v;
		for (FactorGraph g : getNestedGraphs())
		{
			v = g.getVariable(id);
			if (v != null) return v;
		}
		return null;
	}

	public FactorList getNonGraphFactors()
	{
		return getNonGraphFactorsFlat();
	}

	public FactorList getNonGraphFactors(int relativeNestingDepth)
	{
		FactorList f = new FactorList();

		for (FactorBase fb : _ownedFactors)
		{
			FactorGraph subgraph = fb.asFactorGraph();
			if (subgraph != null)
			{
				if (relativeNestingDepth > 0)
				{
					f.addAll((subgraph).getNonGraphFactors(relativeNestingDepth-1));
				}
			}
			else
			{
				f.add(fb.asFactor());
			}
		}

		//f.add(_ownedNonGraphFactors);

		return f;
	}

	public FactorList getNonGraphFactorsFlat()
	{
		return getNonGraphFactors(Integer.MAX_VALUE);
	}

	public FactorList getNonGraphFactorsTop()
	{
		return getNonGraphFactors(0);
	}

	/**
	 * Returns count of variables that would be returned by {@link #getFactors()}.
	 */
	public int getFactorCount()
	{
		return getFactorCount(Integer.MAX_VALUE);
	}

	/**
	 * Returns count of variables that would be returned by {@link #getFactors(int)}.
	 */
	public int getFactorCount(int relativeNestingDepth)
	{
		int count = 0;

		if (relativeNestingDepth <= 0)
		{
			count += _ownedFactors.size();
		}
		else
		{
			for (FactorBase f : _ownedFactors)
			{
				FactorGraph subgraph = f.asFactorGraph();
				if (subgraph != null)
				{
					count += subgraph.getFactorCount(relativeNestingDepth-1);
				}
				else
				{
					++count;
				}
			}
		}

		return count;
	}

	public MapList<FactorBase> getFactors(int relativeNestingDepth)
	{
		return this.getFactors(relativeNestingDepth, new MapList<FactorBase>());
	}

	public MapList<FactorBase> getFactors(int relativeNestingDepth, MapList<FactorBase> factors)
	{
		for (FactorBase f : _ownedFactors)
		{
			FactorGraph subgraph = f.asFactorGraph();
			if (subgraph != null)
			{
				if (relativeNestingDepth > 0)
				{
					factors.addAll(subgraph.getFactors(relativeNestingDepth-1, factors));
				}
				else
				{
					factors.add(f);
				}

			}
			else
			{
				factors.add(f);
			}
		}

		return factors;
	}

	public FactorList getFactors()
	{
		return getFactorsFlat();
	}

	public FactorList getFactorsFlat()
	{
		return getNonGraphFactorsFlat();
	}

	public IMapList<FactorBase> getFactorsTop()
	{
		return getFactors(0);
	}

	public Factor getFactor(int id)
	{
		Factor f;
		f = getNonGraphFactorsTop().getByKey(id);
		if (f != null) return f;
		for (FactorGraph g : getNestedGraphs())
		{
			f = g.getFactor(id);
			if (f != null) return f;
		}
		return null;
	}

	INode getFirstNode()
	{
		INode node = null;

		if (this._ownedFactors.size() > 0)
		{
			node = this._ownedFactors.getByIndex(0);
		}
		else if (this._ownedVariables.size() > 0)
		{
			node = this._ownedVariables.getByIndex(0);
		}
		else if (!this.hasParentGraph() && this._boundaryVariables.size() > 0)
		{
			node = this._boundaryVariables.getByIndex(0);
		}

		return node;
	}

	public IMapList<INode> getNodes()
	{
		return getNodesFlat();
	}

	public MapList<INode> getNodes(int relativeNestingDepth)
	{
		IMapList<FactorBase> factors = getFactors(relativeNestingDepth);
		VariableList vars = getVariables(relativeNestingDepth);

		MapList<INode> retval = new MapList<INode>();

		for (VariableBase v : vars)
			retval.add(v);

		for (FactorBase fb : factors)
			retval.add(fb);

		return retval;
	}

	public IMapList<INode> getNodesFlat()
	{
		return getNodes(Integer.MAX_VALUE);
	}

	public IMapList<INode> getNodesTop()
	{
		return getNodes(0);
	}

	public ArrayList<FactorGraph> getNestedGraphs()
	{
		return _ownedSubGraphs;
	}

	public long getVersionId()
	{
		return _versionId;
	}

	public long getScheduleVersionId()
	{
		return _scheduleVersionId;
	}


	/***************************************************************
	 * 
	 * Names
	 * 
	 ******************************************************************/
	private boolean addNameAndUUID(INameable nameable)
	{
		boolean added = false;
		UUID uuid = nameable.getUUID();
		String explicitName = nameable.getExplicitName();

		if(_UUID2object.get(uuid) == null)
		{
			//(true or exception...)
			added = true;

			//Check + insert name if there is one
			if(explicitName != null)
			{
				if(_name2object.get(explicitName) != null)
				{
					throw new DimpleException("ERROR variable name " + explicitName + " already in graph");
				}

				_name2object.put(explicitName, nameable);
			}

			_UUID2object.put(uuid, nameable);
		}

		return added;
	}

	public void setChildUUID(INameable child, UUID newUUID)
	{
		INameable childFound = (INameable) getObjectByUUID(child.getUUID());

		//If it's not our child, bad
		if(childFound == null)
		{
			throw new DimpleException("ERROR child UUID not found");
		}
		//If new name already here, bad
		else if(getObjectByUUID(newUUID) != null)
		{
			throw new DimpleException("ERROR UUID already present in parent");
		}

		//remove old UUID
		_UUID2object.remove(child.getUUID());
		//add new UUID, if there is one
		_UUID2object.put(newUUID, childFound);
	}
	public void setChildName(INameable child, String newName)
	{
		INameable childFound = (INameable) getObjectByUUID(child.getUUID());

		//If it's not our child, bad
		if(childFound == null)
		{
			throw new DimpleException("ERROR child UUID not found");
		}
		//If new name already here, bad
		else if(getObjectByName(newName) != null)
		{
			throw new DimpleException("ERROR name already present in parent");
		}

		//remove old name, if there was one
		String oldExplicitName = childFound.getExplicitName();
		if(oldExplicitName != null)
		{
			_name2object.remove(oldExplicitName);
		}

		//add new name, if there is one
		if(newName != null)
		{
			_name2object.put(newName, childFound);
		}
	}

	private Object getObjectByNameOrUUIDWithoutRecurse(String string)
	{
		//try first as a simple name; qualified names won't be found
		//	'.' is prevented from being part of a simple name
		Object o = _name2object.get(string);

		//If not found, try as if string is a string version of a UUID
		if(o == null)
		{
			try
			{
				//Convert, if possible...
				UUID uuid = UUID.fromString(string);
				//...search
				INameable nameable = (INameable) _UUID2object.get(uuid);

				//finally enforce 1-and-only-1 name (explicit OR UUID)
				//we enforce this to not have to guarantee that the opposite
				//either at every level of the hierarchy)

				if(nameable != null && nameable.getExplicitName() == null)
				{
					o = nameable;
				}
				//else we were asked for UUID by name, and actually found it,
				//but the object has an explicit name. (See comment above).
			}
			//just quietly swallow case where name couldn't be UUID
			catch(IllegalArgumentException e){}
		}//else o already there

		return o;
	}

	public Object getObjectByName(String name)
	{
		Object o = null;

		if(name != null && !name.equals(""))
		{
			//Names can be of the forms:
			//	object_name
			//	root_name.object_name
			//  root_name.subgraph_name0.subgraph_nameN.object_name
			//
			//  names are either the explicitly set name, or
			//		if none was set, the UUID as a string.
			//
			//	object_name can refer to a variable, function or subgraph.
			//	root_name can refers to name of this graph
			//	subgraphN name refers to name of child graph



			//Try name as a simple, unqualified name.
			//If it is qualified, it cannot be found, as
			//the '.' character cannot be explicitly inserted as part of a name
			o = getObjectByNameOrUUIDWithoutRecurse(name);
			if(o == null)
			{
				//See if this is a qualified name...
				int qualifierIdx = name.indexOf(".");
				if(qualifierIdx != -1)
				{
					//First qualifier can be our name. In that case,
					//strip it, repeat search
					String baseQualifier = name.substring(0, qualifierIdx);
					String remainder = name.substring(qualifierIdx + 1, name.length());
					if(baseQualifier.equals(getName()))
					{
						o = getObjectByName(remainder);
					}
					//otherwise see if it is a child name
					else
					{
						//int childQualifierIdx = name.indexOf(".", qualifierIdx + 1);
						//if(childQualifierIdx != -1)
						{
							//String childName = name.substring(qualifierIdx + 1, childQualifierIdx);
							//Object child = getObjectByNameOrUUIDWithoutRecurse(childName);
							Object child = getObjectByNameOrUUIDWithoutRecurse(baseQualifier);

							//if we found something see if it is a
							if(child != null && child instanceof FactorGraph)
							{
								//found the child!. Strip off child and search in child
								//String remainder = name.substring(childQualifierIdx + 1, name.length());
								FactorGraph fgChild = (FactorGraph) child;
								o = fgChild.getObjectByName(remainder);
							}//else child isn't a subgraph, so can't search in it
						}//else first part is only qualifier, and it doesn't match us!
					}//end of search-children-branch
				}//else is a simple name (and not found)
			}//else found the object!
		}//else null or empty string

		return o;
	}

	public Object getObjectByUUID(UUID uuid)
	{
		return _UUID2object.get(uuid);
	}

	public VariableBase 	getVariableByName(String name)
	{
		VariableBase v = null;
		Object o = getObjectByName(name);
		if(o instanceof VariableBase)
		{
			v = (VariableBase) o;
		}

		return v;
	}
	public Factor 	 	getFactorByName(String name)
	{
		Factor f = null;
		Object o = getObjectByName(name);
		if(o instanceof Factor)
		{
			f = (Factor) o;
		}
		return f;
	}
	public FactorGraph getGraphByName(String name)
	{
		FactorGraph fg = null;
		Object o = getObjectByName(name);
		if(o instanceof FactorGraph)
		{
			fg = (FactorGraph) o;
		}
		return fg;
	}
	public VariableBase getVariableByUUID(UUID uuid)
	{
		VariableBase v = null;
		Object o = getObjectByUUID(uuid);
		if (o instanceof VariableBase)
		{
			v = (VariableBase) o;
		}
		return v;
	}
	public Factor  	getFactorByUUID(UUID uuid)
	{
		Factor f = null;
		Object o = getObjectByUUID(uuid);
		if(o != null &&
				o instanceof Factor)
		{
			f = (Factor) o;
		}
		return f;
	}
	public FactorGraph getGraphByUUID(UUID uuid)
	{
		FactorGraph fg = null;
		Object o = getObjectByUUID(uuid);
		if(o != null &&
				o instanceof FactorGraph)
		{
			fg = (FactorGraph) o;
		}
		return fg;
	}



	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Debugging functions
	@Override
	public String toString()
	{

		int ownedVariables = getVariablesTop().size();
		int boundaryVariables = getBoundaryVariables().size();
		int childVariables = getVariablesFlat().size() - ownedVariables - boundaryVariables;
		int ownedFunctions = getNonGraphFactorsTop().size();
		int childFunctions = getNonGraphFactorsFlat().size() - ownedFunctions;
		int subGraphs = getNestedGraphs().size();
		String s = String.format("FactorGraph [%s] Variables:(o:%d  b:%d  ch:%d)  Functions:(o:%d ch:%d) Graphs:%d"
				,getQualifiedLabel()
				,ownedVariables
				,boundaryVariables
				,childVariables
				,ownedFunctions
				,childFunctions
				,subGraphs);
		return s;

		//return getLabel();
	}

	public String getNodeString()
	{
		return getNodeString(0);
	}

	private String getTabString(int numTabs)
	{
		String s = "";
		for(int i = 0; i < numTabs; ++i)
		{
			s += "\t";
		}
		return s;
	}

	private String getNodeString(int tabDepth)
	{
		String tabString = getTabString(tabDepth);
		//graph itself
		StringBuilder sb = new StringBuilder(tabString + 	"------Nodes------\n");

		//functions
		sb.append(tabString);
		//		sb.append("Functions:\n");
		FactorList fList = getNonGraphFactorsTop();
		for(Factor fn : fList)
		{
			sb.append(tabString);
			sb.append("\t");
			sb.append(fn.getQualifiedLabel());
			sb.append("\n");
		}

		//boundary variables
		sb.append(tabString);
		sb.append("Boundary variables:\n");
		VariableList vList = getBoundaryVariables();
		for(VariableBase v : vList)
		{
			sb.append(tabString);
			sb.append("\t");
			sb.append(v.getQualifiedLabel());
			sb.append("\n");
		}

		//owned variables
		sb.append(tabString);
		sb.append("Owned variables:\n");
		vList = getVariablesTop();
		for(VariableBase v : vList)
		{
			sb.append(tabString);
			sb.append("\t");
			sb.append(v.getQualifiedLabel());
			sb.append("\n");
		}

		//child graphs
		sb.append(tabString);
		sb.append("Sub graphs:\n");
		for(FactorGraph g : getNestedGraphs())
		{
			sb.append(tabString);
			sb.append(g.toString());
			sb.append("\n");
			sb.append(g.getNodeString(tabDepth + 1));
		}
		return sb.toString();
	}

	public String getAdjacencyString()
	{
		StringBuilder sb = new StringBuilder("------Adjacency------\n");
		FactorList allFunctions = getNonGraphFactorsFlat();
		sb.append(String.format("\n--Functions (%d)--\n", allFunctions.size()));
		for(Factor fn : allFunctions)
		{
			String fnName = fn.getLabel();
			if(fn.getParentGraph().getParentGraph() != null)
			{
				fnName = fn.getQualifiedLabel();
			}
			if(fnName == null)
			{
				fnName = Integer.toString(fn.getId());
			}
			sb.append(String.format("fn  [%s]\n", fnName));

			for(int i = 0; i < fn.getSiblings().size(); i++)
			{
				VariableBase v = (VariableBase)fn.getSiblings().get(i);

				String vName = v.getLabel();
				if(v.getParentGraph() != null && // can happen with boundary variables
						v.getParentGraph().getParentGraph() != null)
				{
					vName = v.getQualifiedLabel();
				}
				if(vName == null)
				{
					vName = Integer.toString(v.getId());
				}
				sb.append(String.format("\t-> [%s]\n", vName));
			}
		}
		VariableList allVariables = getVariablesFlat();
		sb.append(String.format("--Variables (%d)--\n", allVariables.size()));
		for(VariableBase v : allVariables)
		{
			String vName = v.getLabel();
			if(v.getParentGraph() != null && //can happen with boundary variables
					v.getParentGraph().getParentGraph() != null)
			{
				vName = v.getQualifiedLabel();
			}
			if(vName == null)
			{
				vName = Integer.toString(v.getId());
			}
			sb.append(String.format("var [%s]\n", vName));

			for(int i = 0; i < v.getSiblings().size(); i++)
			{
				Factor fn = v.getFactors()[i];
				String fnName = fn.getLabel();
				if(fn.getParentGraph().getParentGraph() != null)
				{
					fnName = fn.getQualifiedLabel();
				}
				if(fnName == null)
				{
					fnName = Integer.toString(fn.getId());
				}
				sb.append(String.format("\t-> [%s]\n", fnName));
			}
		}

		return sb.toString();
	}

	public String getDegreeString()
	{
		StringBuilder sb = new StringBuilder("------Degrees------\n");
		HashMap<Integer, ArrayList<INode>> variablesByDegree = getVariablesByDegree();
		HashMap<Integer, ArrayList<INode>> factorsByDegree = getFactorsByDegree();
		sb.append("Variables:\n");
		for(Entry<Integer, ArrayList<INode>> entry : variablesByDegree.entrySet())
		{
			sb.append(String.format("\tdegree:%02d  count:%03d\n", entry.getKey(), entry.getValue().size()));
		}
		sb.append("Factors:\n");
		for(Entry<Integer, ArrayList<INode>> entry : factorsByDegree.entrySet())
		{
			sb.append(String.format("\tdegree:%02d  count:%03d\n", entry.getKey(), entry.getValue().size()));
		}
		sb.append("-------------------\n");
		return sb.toString();
	}
	public String getDomainSizeString()
	{
		StringBuilder sb = new StringBuilder("------Domains------\n");
		TreeMap<Integer, ArrayList<VariableBase>> variablesByDomain = getVariablesByDomainSize();
		for(Entry<Integer, ArrayList<VariableBase>> entry : variablesByDomain.entrySet())
		{
			sb.append(String.format("\tdomain:[%03d]  count:%03d\n", entry.getKey(), entry.getValue().size()));
		}
		sb.append("-------------------\n");
		return sb.toString();
	}

	public String getDomainString()
	{
		StringBuilder sb = new StringBuilder("------Domains------\n");
		TreeMap<Integer, ArrayList<VariableBase>> variablesByDomain = getVariablesByDomainSize();
		for(Entry<Integer, ArrayList<VariableBase>> entry : variablesByDomain.entrySet())
		{
			for(VariableBase vb : entry.getValue())
			{
				sb.append(String.format("\t[%-20s]  Domain [%-40s]\n", vb.getLabel(), vb.getDomain().toString()));
			}
		}
		sb.append("-------------------\n");
		return sb.toString();
	}

	public String getFullString()
	{
		StringBuilder sb = new StringBuilder(toString() + "\n");
		sb.append(getNodeString());
		sb.append(getAdjacencyString());
		sb.append(getDegreeString());
		return sb.toString();
	}


	public boolean isSolverRunning()
	{
		return _solverFactorGraph != null && _solverFactorGraph.isSolverRunning();
	}

	//TODO: should these only be on solver?
	@Override
	public void update()
	{
		throw new DimpleException("Not supported");
	}
	@Override
	public void updateEdge(int outPortNum)
	{
		throw new DimpleException("Not supported");
	}

	@Override
	public FactorGraph getRootGraph()
	{
		FactorGraph root = super.getRootGraph();
		if(root == null)
		{
			root = this;
		}
		return root;
	}

	public void clearNames()
	{
		//If root, clear boundary variables
		if(!hasParentGraph())
		{
			for(VariableBase v : getBoundaryVariables().values())
			{
				v.setName(null);
			}
		}

		@SuppressWarnings("all")
		IMapList<INode> owned = getNodesTop();
		for(INode node : owned.values())
		{
			node.setName(null);
		}

		for(FactorGraph graph : getNestedGraphs())
		{
			graph.clearNames();
		}
	}

	public void setNamesByStructure()
	{
		setNamesByStructure("bv",
				"v",
				"f",
				"graph",
		"subGraph");
	}
	public void setNamesByStructure(String boundaryString,
			String ownedString,
			String factorString,
			String rootGraphString,
			String childGraphString)
			{

		//If root, set boundary variables
		if(!hasParentGraph())
		{
			ArrayList<VariableBase> boundaryVariables = (ArrayList<VariableBase>)getBoundaryVariables().values();
			for(int i = 0; i < boundaryVariables.size(); ++i)
			{
				boundaryVariables.get(i).setName(String.format("%s%d", boundaryString, i));
			}

			if(getExplicitName() != null)
			{
				setName(rootGraphString);
			}
		}

		ArrayList<VariableBase> ownedVariables = (ArrayList<VariableBase>)getVariablesTop().values();
		for(int i = 0; i < ownedVariables.size(); ++i)
		{
			ownedVariables.get(i).setName(String.format("%s%d", ownedString, i));
		}

		ArrayList<Factor> ownedFactors = (ArrayList<Factor>)getNonGraphFactorsTop().values();
		for(int i = 0; i < ownedFactors.size(); ++i)
		{
			ownedFactors.get(i).setName(String.format("%s%d", factorString, i));
		}

		ArrayList<FactorGraph> subGraphs = getNestedGraphs();
		for(int i = 0; i < subGraphs.size(); ++i)
		{
			subGraphs.get(i).setName(String.format("%s%d", childGraphString, i));

			subGraphs.get(i).setNamesByStructure(boundaryString,
					ownedString,
					factorString,
					rootGraphString,
					childGraphString);
		}
			}

	static public HashMap<Integer, ArrayList<INode>> getNodesByDegree(ArrayList<INode> nodes)
	{
		HashMap<Integer, ArrayList<INode>> nodesByDegree = new HashMap<Integer, ArrayList<INode>>();
		for(INode node : nodes)
		{
			int degree = node.getSiblings().size();
			if(!nodesByDegree.containsKey(degree))
			{
				ArrayList<INode> degreeNNodes = new ArrayList<INode>();
				nodesByDegree.put(degree, degreeNNodes);
			}
			nodesByDegree.get(degree).add(node);
		}
		return nodesByDegree;
	}
	public HashMap<Integer, ArrayList<INode>> getNodesByDegree()
	{
		ArrayList<INode> nodes = new ArrayList<INode>();
		nodes.addAll(getNonGraphFactorsFlat());
		nodes.addAll(getVariablesFlat());
		return getNodesByDegree(nodes);
	}
	public HashMap<Integer, ArrayList<INode>> getVariablesByDegree()
	{
		ArrayList<INode> nodes = new ArrayList<INode>();
		nodes.addAll(getVariablesFlat());
		return getNodesByDegree(nodes);
	}
	public HashMap<Integer, ArrayList<INode>> getFactorsByDegree()
	{
		ArrayList<INode> nodes = new ArrayList<INode>();
		nodes.addAll(getNonGraphFactorsFlat().values());
		return getNodesByDegree(nodes);
	}
	public TreeMap<Integer, ArrayList<VariableBase>> getVariablesByDomainSize()
	{
		ArrayList<VariableBase> variables = new ArrayList<VariableBase>();
		variables.addAll(getVariablesFlat());
		TreeMap<Integer, ArrayList<VariableBase>> variablesByDomain = new TreeMap<Integer, ArrayList<VariableBase>>();
		for(VariableBase vb : variables)
		{
			if(!(vb.getDomain() instanceof DiscreteDomain))
			{
				throw new DimpleException("whoops");
			}
			DiscreteDomain domain = (DiscreteDomain) vb.getDomain();
			int size = domain.size();
			if(!variablesByDomain.containsKey(size))
			{
				ArrayList<VariableBase> variablesForDomain = new ArrayList<VariableBase>();
				variablesByDomain.put(size, variablesForDomain);
			}
			variablesByDomain.get(size).add(vb);
		}
		return variablesByDomain;
	}
	public IFactorGraphFactory getFactorGraphFactory()
	{
		return _solverFactory;
	}

	public TreeSet<Edge> getEdges()
	{
		TreeSet<Edge> edges = new TreeSet<Edge>();
		FactorList factors = getNonGraphFactorsFlat();
		for(Factor factor : factors)
		{
			//ArrayList<Port> ports = factor.getPorts();
			for (int i = 0; i < factor.getSiblings().size(); i++)
			//for(Port port : ports)
			{
				INode variable = factor.getConnectedNodeFlat(i);
				edges.add(new Edge(factor, variable));
				edges.add(new Edge(variable, factor));
			}
		}
		return edges;
	}
	@Override
	public double getScore()
	{
		return getSolver().getScore();
	}

	public double getBetheFreeEnergy()
	{
		return getSolver().getBetheFreeEnergy();
	}

	@Override
	public double getInternalEnergy()
	{
		return getSolver().getInternalEnergy();
	}

	@Override
	public double getBetheEntropy()
	{
		return getSolver().getBetheEntropy();
	}


	// For operating collectively on groups of variables that are not already part of a variable vector
	protected HashMap<Integer, ArrayList<VariableBase>> _variableGroups;
	protected int _variableGroupID = 0;
	public int defineVariableGroup(ArrayList<VariableBase> variableList)
	{
		if (_variableGroups == null) _variableGroups = new HashMap<Integer, ArrayList<VariableBase>>();
		_variableGroups.put(_variableGroupID, variableList);
		return _variableGroupID++;
	}

	public ArrayList<VariableBase> getVariableGroup(int variableGroupID)
	{
		return _variableGroups.get(variableGroupID);
	}



	/****************************
	 * addFactor stuff
	 ***************************/

	/*******************
	 * Serialization
	 ********************/

	public String serializeToXML(String FgName, String targetDirectory)
	{
		com.analog.lyric.dimple.model.core.xmlSerializer toXML
		= new com.analog.lyric.dimple.model.core.xmlSerializer();

		return toXML.serializeToXML(this, FgName, targetDirectory);
	}

	static public FactorGraph deserializeFromXML(String docName) throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return deserializeFromXML(docName, null);
	}
	static public FactorGraph deserializeFromXML(String docName, IFactorGraphFactory solver) throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		com.analog.lyric.dimple.model.core.xmlSerializer x
		= new com.analog.lyric.dimple.model.core.xmlSerializer();
		return x.deserializeFromXML(docName, solver);
	}


	/*********************
	 * FactorGraphDiffs
	 * *******************/

	public FactorGraphDiffs getFactorGraphDiffs(FactorGraph b, boolean quickExit, boolean byName)
	{
		return FactorGraphDiffs.getFactorGraphDiffs(
				this,
				b,
				quickExit,
				byName);
	}
	public FactorGraphDiffs getFactorGraphDiffsByName(FactorGraph b)
	{
		return FactorGraphDiffs.getFactorGraphDiffs(
				this,
				b,
				false,
				true);
	}
	public FactorGraphDiffs getFactorGraphDiffsByUUID(FactorGraph b)
	{
		return FactorGraphDiffs.getFactorGraphDiffs(
				this,
				b,
				false,
				false);
	}

}

