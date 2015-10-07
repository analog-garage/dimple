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

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.CustomFactorFunctionWrapper;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.matlabproxy.repeated.IPVariableStreamSlice;
import com.analog.lyric.dimple.matlabproxy.repeated.PFactorGraphStream;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.repeated.FactorGraphStream;
import com.analog.lyric.dimple.model.repeated.IVariableStreamSlice;
import com.analog.lyric.dimple.model.variables.Constant;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.IConstantOrVariable;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.SchedulerBase;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.util.misc.FactorGraphDiffs;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.Matlab;

@Matlab(wrapper="FactorGraph")
public class PFactorGraphVector extends PFactorBaseVector
{
	/*--------------
	 * Construction
	 */
	
	public PFactorGraphVector(FactorGraph f)
	{
		super(new Node [] {f});
	}
	
	public PFactorGraphVector(Node [] nodes)
	{
		super(nodes);
	}

	/*-----------------
	 * PObject methods
	 */

	@Override
	public boolean isDiscrete()
	{
		for (Factor f : getGraph().getFactors())
			if (!f.isDiscrete())
				return false;
		
		return true;
	}
	
	@Override
	public boolean isGraph() {
		return true;
	}

	/*---------------------
	 * PNodeVector methods
	 */
	
	@Override
	public PFactorGraphVector createNodeVector(Node[] nodes) {
		return new PFactorGraphVector(nodes);
	}
	
	/*-----------------------------
	 * PFactorGraphVector methods
	 */

	protected FactorGraph getGraph()
	{
		if (size() != 1)
			throw new DimpleException("operation not supported");
		return (FactorGraph)getModelerNode(0);
	}
	
	public @Nullable String getMatlabSolveWrapper()
	{
		ISolverFactorGraph solverGraph = getGraph().getSolver();
		return solverGraph != null ? solverGraph.getMatlabSolveWrapper() : null;
	}
	
	public int getNumSteps()
	{
		return getGraph().getNumSteps();
	}
	
	public void setNumSteps(int numSteps)
	{
		getGraph().setNumSteps(numSteps);
	}
	
	public void setNumStepsInfinite(boolean val)
	{
		getGraph().setNumStepsInfinite(val);
	}
	public boolean getNumStepsInfinite()
	{
		return getGraph().getNumStepsInfinite();
	}
	
	public PFactorGraphVector addGraph(PFactorGraphVector childGraph, PVariableVector varVector)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	return new PFactorGraphVector(getGraph().addGraph(childGraph.getGraph(), varVector.getVariableArray()));
		
		
	}
	
	@Deprecated
	public boolean customFactorExists(String funcName)
	{
		return getGraph().customFactorExists(funcName);
	}
	
	public PFactorVector createFactor(PFactorTable factorTable, Object [] vars)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	Factor f = getGraph().addFactor(new TableFactorFunction("table", factorTable.getModelerObject()),
    		PHelpers.convertToMVariablesAndConstants(vars));
    	
    	if (f.isDiscrete())
    		return new PDiscreteFactorVector((DiscreteFactor) f);
    	else
    		return new PFactorVector(f);
		
	}


	public PFactorVector createFactor(FactorFunction factorFunction, Object [] vars)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	Factor f = getGraph().addFactor(factorFunction,PHelpers.convertToMVariablesAndConstants(vars));
    	
    	if (f.isDiscrete())
    		return new PDiscreteFactorVector((DiscreteFactor) f);
    	else
    		return new PFactorVector(f);
		
	}
	
	public PFactorVector createFactor(PFactorFunction factorFunction, Object [] vars)
	{
    	
    	FactorFunction ff = factorFunction.getModelerObject();
    	return createFactor(ff,vars);
	}
	
	
	
    public void solve()
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	getGraph().solve();
    }
    
    public void startContinueSolve()
    {
    	getSolverGraph().continueSolve();
    }
    
    public void continueSolve()
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	getGraph().continueSolve();
    	
    }
    
    public void solveOneStep()
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	getGraph().solveOneStep();
    }
    
    public void startSolveOneStep()
    {
    	getSolverGraph().startSolveOneStep();
    }
    
    public boolean isSolverRunning()
    {
    	return getGraph().isSolverRunning();
    }
  
    
    public void startSolver()
    {
    	getSolverGraph().startSolver();
    }
    
	public PVariableVector getVariableVector(int relativeNestingDepth,int forceIncludeBoundaryVariables)
	{
    	if (isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

		PVariableVector tmp =  PHelpers.convertToVariableVector(getGraph().getVariables(relativeNestingDepth,forceIncludeBoundaryVariables!=0));
		return tmp;
	}
	
    public PFactorBaseVector getFactors(int relativeNestingDepth)
    {
    	return getFactors(getGraph().getFactors(relativeNestingDepth));
    }

    public PFactorBaseVector getFactors(IMapList<FactorBase> factors)
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");
    	
    	return PHelpers.convertToFactorVector(factors.toArray(new Node[factors.size()]));
    }
    
	public int [][] getAdjacencyMatrix()
	{
		return getGraph().getAdjacencyMatrix();
	}

    //Returns an adjacency matrix with the given nesting depth.
	public int [][] getAdjacencyMatrix(int relativeNestingDepth)
	{
		return getGraph().getAdjacencyMatrix(relativeNestingDepth);
	}
	
	//Returns an adjacency matrix of the given objects.
	public int [][] getAdjacencyMatrix(Object [] objects)
	{
		ArrayList<Node> alNodes = new ArrayList<Node>();
		
		for (int i = 0; i < objects.length; i++)
		{
			PNodeVector tmp = (PNodeVector)objects[i];
			for (int j= 0; j < tmp.size(); j++)
			{
				alNodes.add(tmp.getModelerNode(j));
			}
				
		}
		
		Node [] array = new Node[alNodes.size()];
		for (int i =0 ; i < array.length; i++)
			array[i] = alNodes.get(i);
		
		return getGraph().getAdjacencyMatrix(array);
	}
	
	public void interruptSolver()
	{
		getSolverGraph().interruptSolver();
	}
	

	/**
	 * Add multiple factors.
	 * <p>
	 * @param factor is used as a prototype. It's factor function and constants should be copied.
	 * @param vars should actually contain PNodeVectors. Each one contains the full set of variables
	 * from which the factor arguments will be chosen.
	 * @param indices
	 */
	public PNodeVector addFactorVectorized(PFactorVector factor, Object [] vars, Object [] indices)
	{
		PNodeVector [] nodes = PHelpers.convertObjectArrayToNodeVectorArray(vars);
		int [][][] intIndices = PHelpers.extractIndicesVectorized(indices);
		
		PNodeVector [][] args = PHelpers.extractVectorization(nodes, intIndices);
		
		final Factor modelFactor = factor.getFactor(0);
		final int nArgs = modelFactor.getArgumentCount();
		Object[] argsWithConstants = null;
		if (modelFactor.hasConstants())
		{
			argsWithConstants = new Object[nArgs];
			for (int j = 0; j < nArgs; ++j)
			{
				IConstantOrVariable arg = modelFactor.getArgument(j);
				if (arg instanceof Constant)
				{
					argsWithConstants[j] = arg;
				}
			}
		}
		
		Node [] retval = new Node[args.length];
		for (int i = 0; i < args.length; i++)
		{
			Object[] argsi = args[i];
			if (argsWithConstants != null)
			{
				for (int j = 0; j < argsi.length; ++j)
				{
					argsWithConstants[modelFactor.siblingNumberToArgIndex(j)] = argsi[j];
				}
				argsi = argsWithConstants;
			}
			
			retval[i] = createFactor(factor.getFactorFunction(),argsi).getFactor(0);
		}
	
		return PHelpers.convertToFactorVector(retval);
	}
	
	public void addBoundaryVariables(Object [] vars)
	{
		for (Object var : vars)
		{
			PVariableVector varvec = (PVariableVector)var;
			getGraph().addBoundaryVariables(varvec.getVariableArray());
		}
	}
	
	public PFactorGraphVector addGraphVectorized(PFactorGraphVector graph, Object [] vars, Object [] indices)
	{
		PNodeVector [] nodes = PHelpers.convertObjectArrayToNodeVectorArray(vars);
		int [][][] intIndices = PHelpers.extractIndicesVectorized(indices);
		PNodeVector [][] args = PHelpers.extractVectorization(nodes, intIndices);
		
		PVariableVector varVector = new PVariableVector();
		
		Node [] retval = new Node[args.length];
		for (int i = 0; i < args.length; i++)
		{
			varVector = (PVariableVector)varVector.concat(args[i]);

			retval[i] = addGraph(graph,varVector).getModelerNode(0);
		}
	
		return new PFactorGraphVector(retval);

	}
	
	public void setSolver(@Nullable IFactorGraphFactory<?> solver)
	{
		getGraph().setSolverFactory(solver);
	}

	public PFactorVector createCustomFactor(String funcName,PVariableVector varVector)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");
    	
		Variable [] vars = varVector.getVariableArray();
		Factor f = getGraph().addFactor(new CustomFactorFunctionWrapper(funcName), vars);
		return new PFactorVector(f);
	}

	
	public PFactorVector createCustomFactor(String funcName, Object [] variables)
	{
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	Factor f = getGraph().addFactor(new CustomFactorFunctionWrapper(funcName),PHelpers.convertToMVariablesAndConstants(variables));
		return new PFactorVector(f);
	}

	public PFactorGraphVector [] getNestedGraphs()
	{
		Collection<FactorGraph> graphs = getGraph().getOwnedGraphs();
		
		PFactorGraphVector [] retval = new PFactorGraphVector[graphs.size()];
		
		int i = 0;
		for (FactorGraph g : graphs)
		{
			retval[i] = new PFactorGraphVector(g);
			i++;
		}

		return retval;
	}

	public boolean isForest(int relativeNestingDepth)
	{
		return getGraph().isForest(relativeNestingDepth);
	}
	
	public boolean isTree(int relativeNestingDepth)
	{
		return getGraph().isTree(relativeNestingDepth);
	}

	public PNodeVector [] depthFirstSearch(PNodeVector root, int searchDepth, int relativeNestingDepth)
	{
		if (root.size() != 1)
			throw new DimpleException("choose one root");
		
		IMapList<INode> nodes = getGraph().depthFirstSearch(root.getModelerNode(0), searchDepth,relativeNestingDepth);
		
		PNodeVector [] retval = new PNodeVector[nodes.size()];
		
		
		for (int i = 0; i < retval.length; i++)
		{
			retval[i] = PHelpers.wrapObject(nodes.getByIndex(i));
		}
		
		return retval;
	}
	
	public PFactorGraphStream addRepeatedFactor(PFactorGraphVector nestedGraph, int bufferSize,Object ... vars)
	{
		//Object [] arr = new Object[vars.length];
		ArrayList<Object> al = new ArrayList<Object>();
				
		for (int i = 0; i < vars.length; i++)
		{
			if (vars[i] instanceof PVariableVector)
			{
				PVariableVector pvv = (PVariableVector)vars[i];
				if (pvv.size() != 1)
					throw new DimpleException("only support one var for now");
				al.add(pvv.getModelerNode(0));
			}
			else if (vars[i] instanceof IPVariableStreamSlice)
			{
				IVariableStreamSlice<?> [] slices = ((IPVariableStreamSlice)vars[i]).getModelerObjects();
				for (int j = 0; j < slices.length; j++)
					al.add(slices[j]);
			}
			else
			{
				throw new DimpleException("when this happen?");
				//arr[i] = vars[i];
			}
		}
		
		Object [] newarray = al.toArray();
	
		FactorGraphStream rfg = getGraph().addRepeatedFactorWithBufferSize(nestedGraph.getGraph(), bufferSize, newarray);
		return new PFactorGraphStream(rfg);
	}

    public void baumWelch(Object [] factorsAndTables,int numRestarts,int numSteps)
    {
		Object [] mfandt = new Object[factorsAndTables.length];
		for (int i = 0; i < factorsAndTables.length; i++)
		{
			if (factorsAndTables[i] instanceof PFactorTable)
				mfandt[i] = ((PFactorTable)factorsAndTables[i]).getModelerObject();
			else if (factorsAndTables[i] instanceof PFactorVector)
			{
				PFactorVector pfv = (PFactorVector)factorsAndTables[i];
				if (pfv.size() != 1)
					throw new DimpleException("for now we only support factor vectors with a single factor");
				
				mfandt[i] = pfv.getModelerNode(0);
			}
			else
				throw new DimpleException("Unsupported argument to estimateParameters");
		}
		this.getGraph().baumWelch(mfandt,numRestarts,numSteps);
    }

	
	public void estimateParameters(Object [] factorsAndTables,int numRestarts,int numSteps, double stepScaleFactor)
	{
		Object [] mfandt = new Object[factorsAndTables.length];
		for (int i = 0; i < factorsAndTables.length; i++)
		{
			if (factorsAndTables[i] instanceof PFactorTable)
				mfandt[i] = ((PFactorTable)factorsAndTables[i]).getModelerObject();
			else if (factorsAndTables[i] instanceof PFactorVector)
			{
				PFactorVector pfv = (PFactorVector)factorsAndTables[i];
				if (pfv.size() != 1)
					throw new DimpleException("for now we only support factor vectors with a single factor");
				
				mfandt[i] = pfv.getModelerNode(0);
			}
			else
				throw new DimpleException("Unsupported argument to estimateParameters");
		}
		this.getGraph().estimateParameters(mfandt,numRestarts,numSteps,stepScaleFactor);
	}

	public void advance()
	{
		getGraph().advance();
	}

	
	public boolean hasNext()
	{
		return getGraph().hasNext();
	}
	
	
	public boolean isAncestorOf(Object o)
	{
		if (! (o instanceof PNodeVector))
			return false;
		
		PNodeVector pn = (PNodeVector)o;
		if (pn.size() != 1)
			throw new DimpleException("only support variable of size 1");
		
		return getGraph().isAncestorOf(pn.getModelerNode(0));
	}
		
	public void removeFactor(PFactorVector factor)
	{
		Node [] factors = factor.getModelerNodes();
		
		for (Node n : factors)
			getGraph().remove((Factor)n);
	}
	
	public void initialize()
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	getGraph().initialize();
    }
    

    
    public PFactorVector [] getNonGraphFactors(int relativeNestingDepth)
    {
    	return getNonGraphFactors(getGraph().getNonGraphFactors(relativeNestingDepth));
    }
    
    public PFactorVector [] getNonGraphFactors(IMapList<Factor> factors)
    {
    	if (getGraph().isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");
    	
    	
    	return PHelpers.convertFactorListToFactors(factors.values());
    }

    
	public boolean hasParentGraph()
	{
		return getGraph().hasParentGraph();
	}
	

	public @Nullable PFactorGraphVector getParentGraph()
	{
		FactorGraph mgraph = getGraph().getParentGraph();
		if(mgraph != null)
			return new PFactorGraphVector(mgraph);
		else
			return null;
	}
	
	public PFactorGraphVector getRootGraph()
	{
		return new PFactorGraphVector(getGraph().getRootGraph());
	}
	
	private ISolverFactorGraph getSolverGraph()
	{
		ISolverFactorGraph solverGraph = getGraph().getSolver();
		if (solverGraph == null)
		{
			throw new DimpleException("Solver not set.");
		}
		return solverGraph;
	}

	public @Nullable PVariableVector getVariableByName(String name)
	{
		Variable mo = getGraph().getVariableByName(name);
		if (mo != null)
			return (PVariableVector)PHelpers.wrapObject(mo);
		else
			return null;
	}
	
	public @Nullable PFactorVector getFactorByName(String name)
	{
		Factor mo = getGraph().getFactorByName(name);
		if(mo != null)
			return (PFactorVector)PHelpers.wrapObject(mo);
		else
			return null;
	}
	
	public @Nullable PFactorGraphVector getGraphByName(String name)
	{
		FactorGraph mo = getGraph().getGraphByName(name);
		if(mo != null)
			return new PFactorGraphVector(mo);
		else
			return null;
	}
	
	public @Nullable PVariableVector getVariableByUUID(UUID uuid)
	{
		Variable mo = getGraph().getVariableByUUID(uuid);
		if(mo != null)
			return (PVariableVector)PHelpers.wrapObject(mo);
		else
			return null;
	}
	
	public @Nullable PFactorVector getFactorByUUID(UUID uuid)
	{
		Factor mo = getGraph().getFactorByUUID(uuid);
		if(mo != null)
			return (PFactorVector) PHelpers.wrapObject(mo);
		else
			return null;
	}
	
	public @Nullable PFactorGraphVector getGraphByUUID(UUID uuid)
	{
		FactorGraph mo = getGraph().getGraphByUUID(uuid);
		if(mo != null)
			return (PFactorGraphVector)PHelpers.wrapObject(mo);
		else
			return null;
	}
	
	@SuppressWarnings("deprecation")
	public void setScheduler(@Nullable Object obj)
	{
		final FactorGraph graph  = getGraph();
		
    	if (graph.isSolverRunning())
    		throw new DimpleException("No changes allowed while the solver is running.");

    	graph.setScheduler(obj != null ? SchedulerBase.instantiate(graph.getEnvironment(), obj) : null);
	}
	
	@SuppressWarnings("deprecation")
	public @Nullable PScheduler getScheduler()
	{
		IScheduler scheduler = getGraph().getScheduler();
		return scheduler != null ? new PScheduler(scheduler) : null;
	}
	
		
	public PFactorGraphStream [] getFactorGraphStreams()
	{
		PFactorGraphStream [] retval = new PFactorGraphStream[getGraph().getFactorGraphStreams().size()];
		
		for (int i = 0; i < retval.length; i++)
		{
			FactorGraphStream fgs = getGraph().getFactorGraphStreams().get(i);
			retval[i] = new PFactorGraphStream(fgs);
		}
		
		return retval;
	}
	
	public FactorGraphDiffs getFactorGraphDiffsByName(PFactorGraphVector b)
	{
		return FactorGraphDiffs.getFactorGraphDiffs(
				   getGraph(),
				   b.getGraph(),
				   false,
				   true);
	}
	
	public PFactorVector joinFactors(Object [] factors)
	{
		//convert Object [] to Factor array
		Factor [] facs = new Factor[factors.length];
		
		for (int i = 0; i < factors.length; i++)
		{
			//TODO: error check?
			facs[i] = (Factor)PHelpers.convertToNode(factors[i]);
		}
		
		Factor f = getGraph().join(facs);
		return (PFactorVector)PHelpers.wrapObject(f);
	}
	
	public PVariableVector joinVariables(Object [] variables)
	{
		Variable [] vars = new Variable[variables.length];
		
		for (int i = 0; i < variables.length; i++)
		{
			if (! (variables[i] instanceof PVariableVector))
				throw new DimpleException("only variable bases supported");
			
			vars[i] = (Variable)PHelpers.convertToNode(variables[i]);
			
		}
		return (PVariableVector)PHelpers.wrapObject(getGraph().join(vars));

	}

	
	public PVariableVector split(PVariableVector variable, @Nullable Object [] factors)
	{
		Factor [] pfactors = {};
		
		if (factors != null)
			pfactors = PHelpers.convertObjectArrayToFactors(factors);
		Node n = PHelpers.convertToNode(variable);

		if (n instanceof Discrete)
			return new PDiscreteVariableVector(getGraph().split((Variable)n,pfactors));
		else
			return new PRealVariableVector((Real)(getGraph().split((Variable)n,pfactors)));
			
	}
	
	public double getBetheFreeEnergy()
	{
		return getGraph().getBetheFreeEnergy();
	}

	
	// For operating collectively on groups of variables that are not already part of a variable vector
	@Deprecated
	public int defineVariableGroup(Object[] variables)
	{
		return addVariableBlock(variables).getLocalId();
	}
	
	public PVariableBlock addVariableBlock(Object[] variables)
	{
		return addVariableBlock(getGraph(), variables);
	}

	static PVariableBlock addVariableBlock(@Nullable FactorGraph graph, Object[] variables)
	{
		ArrayList<Variable> variableList = new ArrayList<Variable>();
		for (int i = 0; i < variables.length; i++)
		{
			Variable[] modelerVariables = ((PVariableVector)variables[i]).getModelerVariables();
			for (int j = 0; j < modelerVariables.length; j++)
				variableList.add(modelerVariables[j]);
		}
		
		if (graph == null)
		{
			// Choose graph from variables. If all come from the same graph, use that graph, otherwise
			// use the root graph.
			graph = variableList.get(0).getParentGraph();
			for (int i = 1, n = variableList.size(); i < n; ++i)
			{
				Variable var = variableList.get(i);
				if (graph != var.getParentGraph())
				{
					graph = var.getRootGraph();
					break;
				}
			}
		}

		return new PVariableBlock(requireNonNull(graph).addVariableBlock(variableList));
	}
}
