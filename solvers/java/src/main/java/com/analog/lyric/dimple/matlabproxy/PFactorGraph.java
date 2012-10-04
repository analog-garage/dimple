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

package com.analog.lyric.dimple.matlabproxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.analog.lyric.dimple.FactorFunctions.NopFactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorBase;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.model.repeated.FactorGraphStream;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.util.misc.FactorGraphDiffs;
import com.analog.lyric.util.misc.MapList;

/*
 * This proxy wraps the Solver FactorGraphBase class
 */
public class PFactorGraph extends PFactorBase
{
	protected FactorGraph _graph;

	/**
	 * Allow access to underlying FactorGraph with its richer accessors.
	 * @return underlying graph
	 */
	public FactorGraph getGraph(){return _graph;}
	
	
	public PFactorGraph addGraph(PFactorGraph childGraph, PVariableVector varVector) 
	{
		//TODO: should I use parent implementation here?
		
		//FactorGraph fg = addGraph(childGraph, varVector.getVariables());
    	if (_graph.isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");

    	//new FactorGraph(fg)
    	return new PFactorGraph(_graph.addGraph(childGraph._graph, com.analog.lyric.dimple.matlabproxy.PHelpers.convertToMVariables(varVector.getVariables())));
		
		
	}
	
	/*
     *Returns the list of nested Graphs.
     */
	public PFactorGraph [] getNestedGraphs() 
	{
		Collection<FactorGraph> graphs = _graph.getNestedGraphs();
		
		PFactorGraph [] retval = new PFactorGraph[graphs.size()]; 
		
		int i = 0;
		for (FactorGraph g : graphs)
		{
			retval[i] = new PFactorGraph(g);
			i++;
		}

		return retval;
	}
	
	public int [][] getAdjacencyMatrix() 
	{
		return _graph.getAdjacencyMatrix();
	}

    //Returns an adjacency matrix with the given nesting depth.
	public int [][] getAdjacencyMatrix(int relativeNestingDepth) 
	{
		return _graph.getAdjacencyMatrix(relativeNestingDepth);
	}
	
	//Returns an adjacency matrix of the given objects.
	public int [][] getAdjacencyMatrix(Object [] objects) 
	{
		ArrayList<INode> alNodes = new ArrayList<INode>();
		
		for (int i = 0; i < objects.length; i++)
		{
			if (objects[i] instanceof PVariableVector)
			{
				PVariableVector tmp = (PVariableVector)objects[i];
				for (int j= 0; j < tmp.size(); j++)
				{
					alNodes.add(tmp.getVariable(j).getModelerObject());
				}
			}
			else
			{
				alNodes.add(((IPNode)objects[i]).getModelerObject());
			}
				
		}
		
		INode [] array = new INode[alNodes.size()];
		for (int i =0 ; i < array.length; i++)
			array[i] = alNodes.get(i);
		
		return _graph.getAdjacencyMatrix(array);
	}
	

	public boolean isTree(int relativeNestingDepth) 
	{
		return _graph.isTree(relativeNestingDepth);
	}


	public Object [] depthFirstSearch(IPNode root, int searchDepth, int relativeNestingDepth) 
	{
		MapList<INode> nodes = _graph.depthFirstSearch(root.getModelerObject(), searchDepth,relativeNestingDepth);
		
		Object [] retval = new Object[nodes.size()];
		
		
		for (int i = 0; i < retval.length; i++)
		{
			retval[i] = com.analog.lyric.dimple.matlabproxy.PHelpers.wrapObject(nodes.getByIndex(i));
		}
		
		return retval;
	}
	
	public PFactor createFactor(PFactorTable factorTable, Object [] vars) 
	{
    	if (_graph.isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");

    	Factor f = getModelerObject().addFactor(new TableFactorFunction("table", factorTable.getModelerObject()),PHelpers.convertToMVariablesAndConstants(vars));
    	
    	if (f.isDiscrete())
    		return new PDiscreteFactor((DiscreteFactor) f);
    	else 
    		return new PFactor(f);
		
	}	


	public PFactor createFactor(FactorFunction factorFunction, Object [] vars) 
	{
    	if (_graph.isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");

    	Factor f = getModelerObject().addFactor(factorFunction,PHelpers.convertToMVariablesAndConstants(vars));
    	
    	if (f.isDiscrete())
    		return new PDiscreteFactor((DiscreteFactor) f);
    	else 
    		return new PFactor(f);
		
	}	
	
	public PFactor createFactor(PFactorFunction factorFunction, Object [] vars) 
	{
    	
    	FactorFunction ff = factorFunction.getModelerObject();
    	
    	return createFactor(ff,vars);
    	
   
    	//return new PFactor(getModelerObject().createFactor(factorFunction, PHelpers.convertToMVariables(variables), funcName));
	}
	

	
	public PFactor createCustomFactor(String funcName,PVariableVector varVector) 
	{
		PFactor f = createCustomFactor(funcName, varVector.getVariables());
		return new PFactor(f);
	}

	
	public PFactor createCustomFactor(String funcName, Object [] variables) 
	{
    	if (_graph.isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");

		return new PFactor(_graph.addFactor(new NopFactorFunction(funcName),PHelpers.convertToMVariablesAndConstants(variables)));
	}

	public void reset() 
	{
		_graph.reset();
	}
	
	public PFactorGraphStream addRepeatedFactor(PFactorGraph nestedGraph, int bufferSize,Object ... vars) 
	{
		Object [] arr = new Object[vars.length];
		for (int i = 0; i < arr.length; i++)
		{
			if (vars[i] instanceof PVariableVector)
			{
				PVariableBase [] pvars = ((PVariableVector)vars[i]).getVariables();
				if (pvars.length != 1)
					throw new DimpleException("only support one var for now");
				
				arr[i] = pvars[0].getModelerObject();
			}
			else if (vars[i] instanceof IPVariableStreamSlice)
			{
				arr[i] = ((IPVariableStreamSlice)vars[i]).getModelerObject();
			}
			else
			{
				throw new DimpleException("when this happen?");
				//arr[i] = vars[i];
			}
		}
		
		FactorGraphStream rfg = this._graph.addRepeatedFactor(nestedGraph.getModelerObject(), bufferSize, arr);
		
		return new PFactorGraphStream(rfg);
	}

	public void estimateParameters(Object [] factorsAndTables,int numRestarts,int numSteps, double stepScaleFactor)
	{
		Object [] mfandt = new Object[factorsAndTables.length];
		for (int i = 0; i < factorsAndTables.length; i++)
		{
			if (factorsAndTables[i] instanceof PFactorTable)
				mfandt[i] = ((PFactorTable)factorsAndTables[i]).getModelerObject();
			else if (factorsAndTables[i] instanceof PFactor)
				mfandt[i] = ((PFactor)factorsAndTables[i]).getModelerObject();
			else
				throw new DimpleException("Unsupported argument to estimateParameters");
		}
		this._graph.estimateParameters(mfandt,numRestarts,numSteps,stepScaleFactor);
	}
	
	public void advance() 
	{
		_graph.advance();
	}
	
	public boolean hasNext() 
	{
		return _graph.hasNext();
	}
	
	public PVariableVector getVariableVector(int relativeNestingDepth,int forceIncludeBoundaryVariables) 
	{
		return getVariableVector(getModelerObject().getVariables(relativeNestingDepth,forceIncludeBoundaryVariables!=0));
	}
	
	
	private PVariableVector getVariableVector(VariableList vars) 
	{
    	if (getModelerObject().isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");

		PVariableBase [] ivars = new PVariableBase[vars.size()];
		int i = 0;
		for (VariableBase v : vars)
		{
			
			if (v instanceof Real)
				ivars[i] = new PRealVariable((Real)v);
			else if (v instanceof RealJoint)
				ivars[i] = new PRealJointVariable((RealJoint)v);
			else
				ivars[i] = new PDiscreteVariable((Discrete)v);
			
			i++;
		}
		
		if (ivars.length > 0)
			if (!ivars[0].isDiscrete())
				return new PRealVariableVector(ivars);
			else
				return new PDiscreteVariableVector(ivars);
		else
			return new PVariableVector();
		
	}
	
	public boolean isAncestorOf(Object o)
	{
		if (! (o instanceof IPNode))
			return false;
		
		return _graph.isAncestorOf(((IPNode)o).getModelerObject());
	}

	public IPNode wrapSolverObject(Object o) 
	{
		IPNode wrapped = null;
		if(o instanceof FactorGraph)
		{
			wrapped = new PFactorGraph((FactorGraph)o);
		}
		else if(o instanceof Real)
		{
			wrapped = new PRealVariable((Real)o);
		}
		else if(o instanceof Discrete)
		{
			wrapped = new PDiscreteVariable((Discrete)o);
		}
		else if(o instanceof Factor)
		{
			wrapped = new PFactor((Factor)o);					
		}
		else
		{
			throw new DimpleException("ERROR solver class [" + o.getClass().toString() + "] has no modeler wrapper");
		}
			
		return wrapped;
	}
	
	public PVariableVector 	getVariableVectorByName(String name) 
	{
		PVariableBase iv = (PVariableBase) getVariableByName(name);
		PVariableVector vv = null;
		if (iv != null)
		{
			if (iv.isDiscrete())
				return new PDiscreteVariableVector(new PVariableBase[]{iv});
			else
				return new PRealVariableVector(new PVariableBase[]{iv});
		}
		
		return vv;
	}
	
	public PVariableVector 	getVariableVectorByUUID(UUID uuid) 
	{
		PVariableBase iv = (PVariableBase) getVariableByUUID(uuid);
		PVariableVector vv = null;
		if(iv != null)
		{
			return new PVariableVector(new PVariableBase[]{iv});
		}
		return vv;
	}
	
	
	public PFactorGraph(FactorGraph fg) 
	{
		_graph = fg;
		//throw new Exception ("HEY the above implementation passes current tests, but loses efficiency on every client factor graph copy");
		
	}

	public double getScore() 
	{
		return _graph.getScore();
	}
	public double getBetheFreeEnergy()
	{
		return _graph.getBetheFreeEnergy();
	}
	public double getInternalEnergy()
	{
		return _graph.getInternalEnergy();
	}
	public double getBetheEntropy()
	{
		return _graph.getBetheEntropy();
	}
	
	
	/*
	 * Let's the user specify a fixed schedule.  Expects a list of items
	 * where each item is one of the following:
	 * -A Variable
	 * -A Factor
	 * -An edge (specified with a list of two connected nodes)
	 * 
	 * TODO: Push this down
	 */
	public void setSchedule(Object [] schedule) 
	{
		
		IScheduleEntry [] entries = new IScheduleEntry[schedule.length];
		
		//Convert schedule to a list of nodes and edges
		for (int i = 0; i < schedule.length; i++)
		{
			Object obj = schedule[i];
			
			if (obj instanceof Object [])
			{
				Object [] objArray = (Object[])obj;
				if (objArray.length != 2)
				{
					throw new DimpleException("length of array containing edge must be 2");
				}
				
				//Should be an edge
				INode node1 = getVariableOrFactorNode(objArray[0]);
				INode node2 = getVariableOrFactorNode(objArray[1]);
				int portNum = node1.getPortNum(node2);
				entries[i] = new EdgeScheduleEntry(node1, portNum);
			}
			else
			{
				if (obj instanceof PFactorGraph)
				{
					FactorGraph graph = ((PFactorGraph)obj).getModelerObject();
					
					entries[i] = new SubScheduleEntry(graph.getSchedule());
				}
				else
					entries[i] = new NodeScheduleEntry(getVariableOrFactorNode(obj));
			}
		}

		getModelerObject().setSchedule(new FixedSchedule(entries));
	}
	
	public void removeFactor(PFactor factor) 
	{
		_graph.remove(factor.getModelerObject());
	}
	
	
	public String serializeToXML(String FgName, String targetDirectory) 
	{
		return _graph.serializeToXML(FgName, targetDirectory);
	}

	public void initialize() 
    {
    	if (_graph.isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");

    	_graph.initialize();
    }
    
    public void solve(boolean initialize) 
    {
    	if (_graph.isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");

    	_graph.solve(initialize);
    }

    public PFactorBase [] getFactors(int relativeNestingDepth) 
    {
    	return getFactors(getModelerObject().getFactors(relativeNestingDepth));
    }

    public PFactorBase [] getFactors(MapList<FactorBase> factors) 
    {
    	if (_graph.isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");
    	
    	
    	return PHelpers.convertFactorBaseListToFactors(factors.values());
    }
    
    public PFactorBase [] getNonGraphFactors(int relativeNestingDepth) 
    {
    	return getNonGraphFactors(getModelerObject().getNonGraphFactors(relativeNestingDepth));
    }
    
    public PFactorBase [] getNonGraphFactors(MapList<Factor> factors) 
    {
    	if (_graph.isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");
    	
    	
    	return PHelpers.convertFactorListToFactors(factors.values());
    }

	/********
	 * OVerrides
	 */
	
	public UUID getUUID()
	{
		return _graph.getUUID();
	}
	public void setUUID(UUID newUUID) 
	{
		_graph.setUUID(newUUID);
	}
	public String getName()
	{
		return _graph.getName();
	}
	public String getExplicitName()
	{
		return _graph.getExplicitName();
	}
	public String getQualifiedName()
	{
		return _graph.getQualifiedName();
	}
	public void setName(String name) 
	{
		_graph.setName(name);
	}
	public String getLabel()
	{
		return _graph.getLabel();
	}
	public String getQualifiedLabel()
	{
		return _graph.getQualifiedName();
	}
	public String getNodeString()
	{
		return _graph.getNodeString();
	}
	public String getDegreeString()
	{
		return _graph.getDegreeString();
	}
	public String getAdjacencyString()
	{
		return _graph.getAdjacencyString();
	}
	public String getFullString()
	{
		return _graph.getFullString();
	}
    
	


	public void update() 
    {
    	_graph.update();
    }
	public void updateEdge(int outPortNum) 
	{		
    	_graph.updateEdge(outPortNum);
	}
	public Port[] getPorts()
	{
		return (Port[])_graph.getPorts().toArray();
	}
	public boolean hasParentGraph()
	{
		return _graph.hasParentGraph();
	}
	public IPNode[] getConnectedNodes()
	{
		return null;
	}
	

	public PFactorGraph getParentGraph() 
	{
		PFactorGraph graph = null;
		FactorGraph mgraph = _graph.getParentGraph();
		if(mgraph != null)
		{
				graph = new PFactorGraph(mgraph);
		}
		return graph;
	}
	
	public PFactorGraph getRootGraph() 
	{
		return new PFactorGraph(_graph.getRootGraph());
	}

	
	@Override
	public int getId() 
	{
		return _graph.getId();
	}
	
	
	@Override
	public FactorGraph getModelerObject() {
		// TODO Auto-generated method stub
		return _graph;
	}
	
	
	
	
	//Name functions
	public Object getObjectByName(String name) 
	{
		Object so = _graph.getObjectByName(name);
		Object mo = null;
		if(so != null)
		{
			mo = wrapSolverObject(so);
		}
		return mo;
	}
	
	public Object getObjectByUUID(UUID uuid) 
	{
		Object so = _graph.getObjectByUUID(uuid);
		Object mo = null;
		if(so != null)
		{
			mo = wrapSolverObject(so);
		}
		return mo;
	}

	public PVariableBase 	getVariableByName(String name) 
	{
		PVariableBase co = null;
		VariableBase mo = _graph.getVariableByName(name);
		if(mo != null)
		{
			co = (PVariableBase) wrapSolverObject(mo);
		}
		return co;
	}
	public PFactor 	 	getFactorByName(String name) 
	{
		PFactor co = null;
		Factor mo = _graph.getFactorByName(name);
		if(mo != null)
		{
			co =  (PFactor) wrapSolverObject(mo);
		}
		return co;
	}
	
	public PFactorGraph	getGraphByName(String name) 
	{
		PFactorGraph co = null;
		FactorGraph mo = _graph.getGraphByName(name);
		if(mo != null)
		{
			co =  (PFactorGraph) wrapSolverObject(mo);
		}
		return co;
	}
	public PVariableBase 	getVariableByUUID(UUID uuid) 
	{
		PVariableBase co = null;
		Discrete mo = _graph.getVariableByUUID(uuid);
		if(mo != null)
		{
			co =  (PVariableBase) wrapSolverObject(mo);
		}
		return co;
	}
	public PFactor  		getFactorByUUID(UUID uuid) 
	{
		PFactor co = null;
		Factor mo = _graph.getFactorByUUID(uuid);
		if(mo != null)
		{
			co =  (PFactor) wrapSolverObject(mo);
		}
		return co;
	}
	public PFactorGraph 	getGraphByUUID(UUID uuid) 
	{
		PFactorGraph co = null;
		FactorGraph mo = _graph.getGraphByUUID(uuid);
		if(mo != null)
		{
			co =  (PFactorGraph) wrapSolverObject(mo);
		}
		return co;
	}
	
	public void setSolver(IFactorGraphFactory solver) 
	{
		getModelerObject().setSolverFactory(solver);
	}
	
	

	
	public ISolverFactorGraph getSolver() 
	{
		return getModelerObject().getSolver();
	}
	
	
	
	public boolean customFactorExists(String funcName) 
	{
    	if (_graph.isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");

		return getModelerObject().customFactorExists(funcName);
	}
	
	public void setScheduler(IScheduler scheduler) 
	{
    	if (_graph.isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");

    	_graph.setScheduler(scheduler);
	}
	
	

	private INode getVariableOrFactorNode(Object obj) 
	{
		if (obj instanceof PVariableBase)
		{
			return ((PVariableBase)obj).getModelerObject();
		}
		else if (obj instanceof PFactor)
		{
			return ((PFactor)obj).getModelerObject();
		}
		else
		{
			throw new DimpleException("unexpected type: " + obj);
		}

	}
	
	public PFactorGraphStream [] getFactorGraphStreams()
	{
		PFactorGraphStream [] retval = new PFactorGraphStream[_graph.getFactorGraphStreams().size()];
		
		for (int i = 0; i < retval.length; i++)
		{
			retval[i] = new PFactorGraphStream(_graph.getFactorGraphStreams().get(i));
		}
		
		return retval;
	}
	
	public FactorGraphDiffs getFactorGraphDiffsByName(PFactorGraph b) 
	{
		return FactorGraphDiffs.getFactorGraphDiffs(
				   this.getModelerObject(), 
				   b.getModelerObject(),
				   false,
				   true);
	}	
	
	public PFactor joinFactors(Object [] factors) 
	{
		//convert Object [] to Factor array
		Factor [] facs = new Factor[factors.length];
		
		for (int i = 0; i < factors.length; i++)
		{
			//TODO: error check?
			facs[i] = ((PFactor)factors[i]).getModelerObject();
		}
		
		Factor f = getModelerObject().join(facs);
		
		if (f.isDiscrete())
			return new PDiscreteFactor((DiscreteFactor)f);
		else
			return new PFactor(f);
	}
	
	public PVariableVector joinVariables(Object [] variables) 
	{
		VariableBase [] vars = new VariableBase[variables.length];
		
		for (int i = 0; i < variables.length; i++)
		{
			if (! (variables[i] instanceof PVariableBase))
				throw new DimpleException("only variable bases supported");
			
			vars[i] = ((PVariableBase)variables[i]).getModelerObject();
			
		}
		
		//TODO: better checking
		if (vars[0] instanceof Discrete)
			return new PDiscreteVariableVector(new PVariableBase[]{PHelpers.convertToVariable(getModelerObject().join(vars))});
		else
			return new PRealVariableVector(new PVariableBase[]{PHelpers.convertToVariable(getModelerObject().join(vars))});

	}

	public PVariableVector split(PVariableBase variable, Object [] factors) 
	{
		PFactor [] pfactors = {};
		if (factors != null)
			pfactors = PHelpers.convertObjectArrayToFactors(factors);
		
		if (variable.getModelerObject() instanceof Discrete)
			return new PDiscreteVariableVector(new PVariableBase[]{PHelpers.convertToVariable(getModelerObject().split(
					variable.getModelerObject(),
					PHelpers.convertToFactors(pfactors)))});
		else
			return new PRealVariableVector(new PVariableBase[]{PHelpers.convertToVariable(getModelerObject().split(
					variable.getModelerObject(),
					PHelpers.convertToFactors(pfactors)))});
	}
	

	@Override
	public void setLabel(String name) 
	{
		_graph.setLabel(name);
	}
	
	@Override
	public boolean isGraph() 
	{
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean isDiscrete()
	{
		for (Factor f : _graph.getFactorsFlat())
			if (!f.isDiscrete())
				return false;
		
		return true;
	}

}
	
