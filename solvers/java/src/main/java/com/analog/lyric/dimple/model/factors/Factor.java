/*******************************************************************************
*   Copyright 2012-2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model.factors;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.IntArrayList;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.NodeId;
import com.analog.lyric.dimple.model.core.NodeType;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.DomainList;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.options.DimpleOptions;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.util.misc.Internal;

public class Factor extends FactorBase implements Cloneable
{
	/**
	 * Sentinel value for {@link #_directedTo} indicating it has not yet been set.
	 */
	private static final int[] NOT_YET_SET = new int[0];
	
	private String _modelerFunctionName = "";
	protected @Nullable ISolverFactor _solverFactor = null;
	private FactorFunction _factorFunction;
	protected @Nullable int [] _directedTo = NOT_YET_SET;
	@Nullable int [] _directedFrom = null;
	
	
	@Override
	public final Factor asFactor()
	{
		return this;
	}
	
	@Override
	public final boolean isFactor()
	{
		return true;
	}
	
	@Override
	public NodeType getNodeType()
	{
		return NodeType.FACTOR;
	}
	
	public String getModelerFunctionName()
	{
		return _modelerFunctionName;
	}
	
	@Internal
	@Deprecated
	public Factor(int id,FactorFunction factorFunc, Variable [] variables)
	{
		this(factorFunc, variables);
	}
	
	@Internal
	public Factor(FactorFunction factorFunc, Variable[] variables)
	{
		super(NodeId.INITIAL_FACTOR_ID);
		
		_factorFunction = factorFunc;
		_modelerFunctionName = factorFunc.getName();
		
		for (int i = 0; i < variables.length; i++)
			addVariable(variables[i]);
	}
	
//	public Factor(int id,VariableBase [] variables, String modelerFunctionName)
//	{
//		super(id);
//
//		_modelerFunctionName = modelerFunctionName;
//		for (int i = 0; i < variables.length; i++)
//		{
//			connect(variables[i]);
//			variables[i].connect(this);
//		}
//	}

	public IFactorTable getFactorTable()
	{
		return getFactorFunction().getFactorTable(this);
	}
	
	public boolean hasFactorTable()
	{
		return getFactorFunction().factorTableExists(this);
	}

	public boolean isDiscrete()
	{
		for (INode p : getSiblings())
		{
			Variable vb = (Variable)p;
			if (! vb.getDomain().isDiscrete())
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	public FactorFunction getFactorFunction()
	{
		return _factorFunction;
	}
	
	public void setFactorFunction(FactorFunction function)
	{
		_factorFunction = function;
		if (_factorFunction.isDirected())
		{
			// Automatically set direction if inherent in factor function
			setDirectedTo(Objects.requireNonNull(_factorFunction.getDirectedToIndices(getSiblingCount())));
		}
	}
	
	protected void addVariable(Variable variable)
	{
		connect(variable);
		variable.connect(this);
	}
	

	
	@Override
	public String getLabel()
	{
		String name = getOption(DimpleOptions.label);
		if(name == null)
		{
			name = _name;
			if(name == null)
			{
				name = getModelerFunctionName() + "_" + getLocalId();
			}
		}
		return name;
	}

	
	@Override
	public @Nullable ISolverFactor getSolver()
	{
		return _solverFactor;
	}

	@Override
	public String getClassLabel()
    {
    	return "Factor";
    }
	
	public void createSolverObject(@Nullable ISolverFactorGraph factorGraph)
	{
		if (factorGraph != null)
		{
			ISolverFactor factor = _solverFactor = factorGraph.createFactor(this);
			factor.createMessages();
		}
		else
		{
			_solverFactor = null;
		}
	}
	
	/**
	 * Removes edges to all variables that have fixed values and modifies the
	 * factor function to incorporate those values.
	 * <p>
	 * For {@link DiscreteFactor}s, this will replace the factor function with one using a newly
	 * generated factor table. For other factor types this wraps the factor function with
	 * {@link FactorFunctionWithConstants}.
	 * </p>
	 * @return the number of variable edges that were removed.
	 * 
	 * @see Variable#hasFixedValue()
	 */
	public final int removeFixedVariables()
	{
		final int[] oldDirectedTo = getDirectedTo();
		final int nEdges = getSiblingCount();
		final ArrayList<Variable> constantVariables = new ArrayList<Variable>(nEdges);
		final IntArrayList constantIndices = new IntArrayList(nEdges);
		IFactorTable oldFactorTable = null;
		
		// Visit in reverse order so that disconnect is safe.
		for (int i = nEdges; --i>=0;)
		{
			final Variable var = getSibling(i);
			if (var.hasFixedValue())
			{
				if (constantIndices.isEmpty() && isDiscrete())
				{
					// Before disconnecting siblings, force the factor table
					// to be instantiated if discrete, since it may depend on
					// the original edges.
					oldFactorTable = getFactorTable();
				}
				var.remove(this);
				constantVariables.add(var);
				constantIndices.add(i);
				disconnect(i);
			}
		}
		
		final int nRemoved = nEdges - constantIndices.size();
		
		if (nRemoved > 0)
		{
			constantIndices.trimToSize();
			final FactorFunction oldFunction = getFactorFunction();
			final FactorFunction newFunction =
				removeFixedVariablesImpl(oldFunction, oldFactorTable, constantVariables, constantIndices.elements());
			
			int[] newDirectedTo = null;
			if (oldDirectedTo != null && !oldFunction.isDirected())
			{
				// If factor function is not inherently directed, then update the directed indices.
				newDirectedTo = ArrayUtil.contractSortedIndexList(oldDirectedTo, constantIndices.elements());
			}

			setFactorFunction(newFunction);
			
			if (newDirectedTo != null)
			{
				setDirectedTo(newDirectedTo);
			}
		}
		
		return nRemoved;
	}
	
	/**
	 * Replaces factor function conditioned on a set of variables with fixed values that
	 * have already been removed from the factor. This should only be called from within
	 * {@link #removeFixedVariables()}.
	 * <p>
	 * This is invoked after the fixed value variables have already been disconnected from the
	 * factor, but before the factor function has been replaced.
	 * <p>
	 * @param constantVariables lists the variables
	 * @param constantIndices
	 */
	protected FactorFunction removeFixedVariablesImpl(
		FactorFunction oldFunction,
		@Nullable IFactorTable oldFactorTable,
		ArrayList<Variable> constantVariables,
		int[] constantIndices)
	{
		final Object[] constantValues = new Object[constantVariables.size()];
		for (int i = constantValues.length; --i>=0;)
		{
			final Variable variable = constantVariables.get(i);
			if (variable.getDomain().isDiscrete())
			{
				// FIXME: clean up fixed value methods so there is a base class method for getting
				// the actual fixed value as opposed to its representation.
				constantValues[i] = variable.asDiscreteVariable().getFixedValue();
			}
			else
			{
				constantValues[i] = variable.getFixedValueObject();
			}
		}
		
		return new FactorFunctionWithConstants(oldFunction,	constantValues,	constantIndices);
	}
	
	public void replace(Variable oldVariable, Variable newVariable)
	{
		replaceSibling(oldVariable, newVariable);
	}
	
	public DomainList<?> getDomainList()
	{
		int numVariables = getSiblingCount();
		
		Domain [] domains = new Domain[numVariables];
		
		for (int i = 0; i < numVariables; i++)
		{
			domains[i] = getSibling(i).getDomain();
		}
		
		return DomainList.create(getDirectedTo(), domains);
	}
	

	@Override
	public Factor clone()
	{
		/*******
		 * NOTE: Any derived class that defines instance variables that are
		 * objects (rather than primitive types) must implement clone(), which
		 * must first call super.clone(), and then deep-copy those instance
		 * variables to the clone.
		 *******/
		Factor f = (Factor) super.clone();
		
		//TODO: cloning solver factor?
		
		return f;
	}

	@Override
	public void initialize(int portNum)
	{
		final ISolverFactor sfactor = _solverFactor;
		if (sfactor != null)
			sfactor.resetEdgeMessages(portNum);
	}
    
	/**
	 * Model-specific initialization for factors.
	 * <p>
	 * Assumes that model variables in same graph have already been initialized.
	 * Does NOT invoke solver factor initialize.
	 */
	@Override
	public void initialize()
	{
		if (_factorFunction.isDirected())	// Automatically set direction if inherent in factor function
		{
			setDirectedTo(Objects.requireNonNull(_factorFunction.getDirectedToIndices(getSiblingCount())));
			if (_factorFunction.isDeterministicDirected())
			{
				final int[] directedTo = Objects.requireNonNull(_directedTo);
				for (int to : directedTo)
				{
					getSibling(to).setDeterministicOutput();
				}
				if (directedTo.length > 0)
				{
					final int[] directedFrom = requireNonNull(_directedFrom);
					for (int from : directedFrom)
					{
						getSibling(from).setDeterministicInput();
					}
				}
			}
		}
	}
	
	@Override
	public void update()
	{
		requireSolver("update").update();
	}

	@Override
	public void updateEdge(int outPortNum)
	{
		requireSolver("updateEdge").updateEdge(outPortNum);
	}
	
	@Internal
	public ISolverFactor requireSolver(String method)
	{
		return requireSolver(method, _solverFactor);
	}
	
	protected <T extends ISolverFactor> T requireSolver(String method, @Nullable T solverFactor)
	{
		if (solverFactor == null)
		{
			throw new DimpleException("solver must be set before using '%s'.", method);
		}
		return solverFactor;
	}
	
	@Internal
	public void replaceVariablesWithJoint(Variable [] variablesToJoin, Variable newJoint)
	{
		throw new DimpleException("not implemented");
	}
	
	@Override
	public double getScore()
	{
		return requireSolver("getScore").getScore();
	}
	
	@Override
	public double getInternalEnergy()
	{
		return requireSolver("getInternalEnergy").getInternalEnergy();
		
	}

	@Override
	public double getBetheEntropy()
	{
		return requireSolver("getBetheEntropy").getBetheEntropy();
		
	}


	public boolean isDirected()
	{
		ensureDirectedToSet();
		return _directedTo != null;
	}

	private void ensureDirectedToSet()
	{
		if (_directedTo == NOT_YET_SET)
		{
			_directedTo = null;
			if (canBeDirected())
			{
				setFactorFunction(getFactorFunction());
			}
		}
	}
	
	/**
	 * Return true if factor supports setting direction through one of the
	 * {@link #setDirectedTo} methods.
	 * <p>
	 * Returns true by default.
	 * <p>
	 * @since 0.07
	 */
	@Internal
	protected boolean canBeDirected()
	{
		return true;
	}
	
	public @Nullable int [] getDirectedTo()
	{
		ensureDirectedToSet();
		return _directedTo;
	}
	public @Nullable int [] getDirectedFrom()
	{
		ensureDirectedToSet();
		return _directedFrom;
	}
	
	public VariableList getDirectedToVariables()
	{
		VariableList vl = null;
		
		final int[] directedTo = getDirectedTo();
		if (directedTo != null)
		{
			vl = new VariableList(directedTo.length);
			for (int i = 0; i < directedTo.length; i++)
			{
				vl.add(getSibling(directedTo[i]));
			}
		}
		else
		{
			vl = new VariableList();
		}
		
		return vl;

	}

	public final void setDirectedTo(VariableList vl)
	{
		int [] directedTo = new int[vl.size()];
		for (int i = 0; i < directedTo.length; i++)
			directedTo[i] = getPortNum(vl.getByIndex(i));
		
		setDirectedTo(directedTo);

	}

	public final void setDirectedTo(Variable ... variables)
	{
		int [] directedTo = new int[variables.length];
		for (int i = 0; i < directedTo.length; i++)
			directedTo[i] = getPortNum(variables[i]);
		setDirectedTo(directedTo);
	}
	
	public void setDirectedTo(int [] directedTo)
	{
		if (!canBeDirected())
		{
			throw new UnsupportedOperationException(String.format("'%s' does not support setting direction",
				getClass().getSimpleName()));
		}
		
		final JointDomainIndexer curDomains = getDomainList().asJointDomainIndexer();

		BitSet toSet = new BitSet(directedTo.length);
		
		final int nVariables = getSiblingCount();
		final int[] directedFrom = _directedFrom = new int[nVariables-directedTo.length];
		
		boolean sort = false;
		int prev = -1;
		for (int toVarIndex : directedTo)
		{
			if (toSet.get(toVarIndex) || toVarIndex > nVariables)
				throw new DimpleException("invalid edge");
			if (toVarIndex < prev)
			{
				sort = true;
			}
			prev = toVarIndex;
			toSet.set(toVarIndex);
		}

		for (int i = 0, fromVarIndex = 0;
			(fromVarIndex = toSet.nextClearBit(fromVarIndex)) < nVariables;
			++fromVarIndex, ++i)
		{
			directedFrom[i] = fromVarIndex;
		}
		
		if (sort)
		{
			Arrays.sort(directedTo);
		}
		
		_directedTo = directedTo;
		
		notifyConnectionsChanged();

		if (curDomains != null)
		{
			JointDomainIndexer newDomains = getDomainList().asJointDomainIndexer();
			if (!curDomains.equals(newDomains))
			{
				getFactorFunction().convertFactorTable(curDomains, newDomains);
			}
		}

		ISolverFactor sfactor = _solverFactor;
		if (sfactor != null)
		{
			sfactor.setDirectedTo(directedTo);
		}
	}
	
	/**
	 * Makes factor undirected.
	 * 
	 * @since 0.07
	 */
	public void setUndirected()
	{
		_directedTo = null;
		_directedFrom = null;
		if (hasFactorTable())
		{
			getFactorTable().setDirected(null);
		}
	}
	
	public boolean isDirectedTo(int edge)
	{
		final int[] to = getDirectedTo();
		
		if (to == null)
			return false;

		// Assume _directedTo is sorted:
		
		final int toRange = to.length - 1;
		final int first = to[0];
		final int last = to[toRange];
		
		if (last - first == toRange)
		{
			return edge <= last && edge >= first;
		}
		
		return Arrays.binarySearch(to, edge) >= 0;
	}
}
