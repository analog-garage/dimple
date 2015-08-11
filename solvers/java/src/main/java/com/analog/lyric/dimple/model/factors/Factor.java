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

import static java.lang.String.*;
import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.EdgeDirection;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorPort;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.model.core.NodeType;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.DomainList;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Constant;
import com.analog.lyric.dimple.model.variables.IConstantOrVariable;
import com.analog.lyric.dimple.model.variables.IVariableToValue;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.options.DimpleOptions;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.util.misc.Internal;
import com.google.common.collect.Sets;

import cern.colt.list.IntArrayList;

public class Factor extends FactorBase implements Cloneable
{
	// TODO rearrange methods
	
	/*-------
	 * State
	 */
	
	/**
	 * Sentinel value for {@link #_directedTo} indicating it has not yet been set.
	 */
	private static final int[] NOT_YET_SET = new int[0];
	
	@Deprecated
	private String _modelerFunctionName = "";
	private FactorFunction _factorFunction;
	protected @Nullable int [] _directedTo = NOT_YET_SET;
	@Nullable int [] _directedFrom = null;
	
	/**
	 * Represents arguments to factor function.
	 * <p>
	 * Includes all of the edge ids found in {@link _siblingEdges} in the same order, but with additional constant
	 * ids inserted. If there are no constants, this field will simply point to {@link _siblingEdges}.
	 */
	private IntArrayList _factorArguments;

	/**
	 * Mapping from sibling number offset in {@code _siblingEdges} to corresponding offset in {@link _factorArguments}
	 * <p>
	 * Computed lazily.
	 */
	private int[] _siblingToFactorArgumentNumber = NOT_YET_SET;
	
	/**
	 * If non-null provides a cumulative count of constants in {@link _factorArguments} upto a
	 * given factor function argument index. This will be one greater than the size of {@link _factorArguments}.
	 * <p>
	 * Computed lazily
	 */
	private int[] _constantsBeforeFactorArgument = NOT_YET_SET;
	
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
	
	/*--------------
	 * Construction
	 */
	
	@Internal
	public Factor(FactorFunction factorFunc)
	{
		super(Ids.INITIAL_FACTOR_ID);
		
		_factorFunction = factorFunc;
		_modelerFunctionName = factorFunc.getName();
		_factorArguments = _siblingEdges;
	}
	
	protected Factor(Factor that)
	{
		super(that);
		_modelerFunctionName = that._modelerFunctionName;
		_factorFunction = that._factorFunction; // clone?
		// Note that this does not copy the constants or the edges
		_factorArguments = _siblingEdges;
		int[] directedTo = _directedTo = that._directedTo;
		if (directedTo != null && directedTo != NOT_YET_SET && directedTo != ArrayUtil.EMPTY_INT_ARRAY)
		{
			_directedTo = directedTo.clone();
		}
		int[] directedFrom = _directedFrom = that._directedFrom;
		if (directedFrom != null && directedFrom != ArrayUtil.EMPTY_INT_ARRAY)
		{
			_directedFrom = directedFrom.clone();
		}
	}
	
	/**
	 * @category internal
	 */
	@Override
	@Internal
	protected void setFactorArguments(int[] argids)
	{
		_factorArguments = new IntArrayList(argids);
		forgetConstantInfo();
	}
	
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
			int[] to = requireNonNull(_factorFunction.getDirectedToIndices(getFactorArgumentCount()));
			setDirectedTo(getEdgesFromIndices(to));
		}
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
	public FactorPort getPort(int siblingNumber)
	{
		return new FactorPort(this, siblingNumber);
	}
	
	@Override
	public @Nullable ISolverFactor getSolver()
	{
		final FactorGraph fg = getParentGraph();
		if (fg != null)
		{
			final ISolverFactorGraph sfg = fg.getSolver();
			if (sfg != null)
			{
				return sfg.getSolverFactor(this, false);
			}
		}
		return null;
	}
	
	@Override
	public String getClassLabel()
    {
    	return "Factor";
    }
	
	/**
	 * @category internal
	 */
	@Internal
	public void createSolverObject(@Nullable ISolverFactorGraph factorGraph)
	{
		if (factorGraph != null)
		{
			factorGraph.getSolverFactor(this, true);
		}
	}
	
	/**
	 * @category internal
	 */
	@Deprecated
	@Internal
	public void setSolver(@Nullable ISolverFactor sfactor)
	{
		throw new UnsupportedOperationException("Factor.setSolver no longer supported");
	}
	
	/**
	 * Removes edges to all variables that have fixed values and replace with constants.
	 * <p>
	 * For {@link DiscreteFactor}s, this will replace the factor function with one using a newly
	 * generated factor table.
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
		
		final FactorGraph parent = requireNonNull(getParentGraph());
		
		// Visit in reverse order so that disconnect is safe.
		
		// FIXME Constant - this is not correct now that constants are in factors
		// but there aren't currently any tests that exercise this code for the
		// non-discrete case.
		
		for (int i = nEdges; --i>=0;)
		{
			final EdgeState edge = getSiblingEdgeState(i);
			final Variable var = edge.getVariable(parent);
			if (var.hasFixedValue())
			{
				if (constantIndices.isEmpty() && isDiscrete())
				{
					// Before disconnecting siblings, force the factor table
					// to be instantiated if discrete, since it may depend on
					// the original edges.
					oldFactorTable = getFactorTable();
				}
				removeSiblingEdge(edge);
				constantVariables.add(var);
				constantIndices.add(i);
			}
		}
		
		final int nRemoved = constantIndices.size();
		
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
			final Value value = variable.getPriorValue();
			constantValues[i] = value != null ? value.getObject() : null;
		}
		
		return new FactorFunctionWithConstants(oldFunction,	constantValues,	constantIndices);
	}
	
	/**
	 * The domains of the adjacent variables in order.
	 */
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
	
	public DomainList<?> getDomainListWithConstants()
	{
		int nArgs = getFactorArgumentCount();
		Domain [] domains = new Domain[nArgs];
		
		for (int i = 0; i < nArgs; i++)
		{
			IConstantOrVariable arg = getFactorArgument(i);
			if (arg instanceof Constant)
			{
				// Make a single-element discrete domain for the constant value.
				domains[i] = DiscreteDomain.create(requireNonNull(((Constant)arg).value().getObject()));
			}
			else
			{
				domains[i] = ((Variable)arg).getDomain();
			}
		}
		
		return DomainList.create(getDirectedTo(), domains);
	}
	
	@Override
	public Factor clone()
	{
		return new Factor(this);
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
		final FactorFunction func = _factorFunction;
		if (func.isDirected())	// Automatically set direction if inherent in factor function
		{
			setDirectedTo(getEdgesFromIndices(requireNonNull(func.getDirectedToIndices(getFactorArgumentCount()))));
			if (_factorFunction.isDeterministicDirected())
			{
				final int[] directedTo = requireNonNull(_directedTo);
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
	@Internal
	public ISolverFactor requireSolver(String method)
	{
		return requireSolver(method, getSolver());
	}
	
	protected <T extends ISolverFactor> T requireSolver(String method, @Nullable T solverFactor)
	{
		if (solverFactor == null)
		{
			throw new NullPointerException(String.format("solver must be set before using '%s'.", method));
		}
		return solverFactor;
	}
	
	@Internal
	public void replaceVariablesWithJoint(Variable [] variablesToJoin, Variable newJoint)
	{
		throw new DimpleException("not implemented");
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

	/**
	 * Describes direction of given edge.
	 * <p>
	 * @throws IndexOutOfBoundsException if {@code edgeNumber} is negative or not less than {@link #getSiblingCount()}.
	 * @since 0.08
	 */
	public EdgeDirection getEdgeDirection(int edgeNumber)
	{
		getSiblingEdgeIndex(edgeNumber); // call this to do a range check
		
		ensureDirectedToSet();
		if (_directedTo == null)
		{
			return EdgeDirection.UNDIRECTED;
		}
		else if (isDirectedTo(edgeNumber))
		{
			return EdgeDirection.FROM_FACTOR;
		}
		else
		{
			return EdgeDirection.TO_FACTOR;
		}
	}
	
	public int getFactorArgumentCount()
	{
		return _factorArguments.size();
	}
	
	@SuppressWarnings("null")
	public IConstantOrVariable getFactorArgument(int i)
	{
		final FactorGraph graph = requireParentGraph();
		int id = _factorArguments.get(i);
		int index = Ids.indexFromLocalId(id);

		switch (Ids.typeIndexFromLocalId(id))
		{
		case Ids.UNKNOWN_TYPE:
		case Ids.EDGE_TYPE:
			return graph.getGraphEdgeState(index).getVariable(graph);
			
		case Ids.CONSTANT_TYPE:
			return graph.getConstantByLocalId(id);
			
		default:
			throw new IllegalStateException();
		}
	}
	
	/**
	 * Sets all edges to specified variables as output edges.
	 * @param variables
	 * @since 0.08
	 */
	public final void setDirectedTo(Set<Variable> variables)
	{
		final IntArrayList directedTo = new IntArrayList(variables.size());
		
		for (int i = 0, n = getSiblingCount(); i < n; ++i)
		{
			if (variables.contains(getSibling(i)))
			{
				directedTo.add(i);
			}
		}
		
		directedTo.trimToSize();
		setDirectedTo(directedTo.elements());
	}
	
	/**
	 * Sets all edges to specified variables as output edges.
	 */
	public final void setDirectedTo(VariableList vl)
	{
		setDirectedTo(Sets.newHashSet(vl));
	}

	/**
	 * Sets all edges to specified variables as output edges.
	 */
	public final void setDirectedTo(Variable ... variables)
	{
		setDirectedTo(Sets.newHashSet(variables));
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

		// FIXME - don't force creation
		ISolverFactor sfactor = getSolver();
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
	
	/**
	 * Evaluates energy for factor for given values.
	 * <p>
	 * Calls underlying {@linkplain #getFactorFunction() factor function} after inserting
	 * any constant values held by the factor.
	 * <p>
	 * @param values specifies the values of the sibling variables to be evaluated.
	 * @since 0.08
	 */
	public double evalEnergy(Value[] values)
	{
		final int nEdges = getSiblingCount();
		final int nArgs = getFactorArgumentCount();
		
		if (values.length != getSiblingCount())
		{
			throw new IllegalArgumentException("Values length does not match number of edges");
		}
	
		if (nEdges != nArgs)
		{
			// Fill in constant values
			Value[] tmp = new Value[nArgs];
			for (int i = 0, j = 0; i < nArgs; ++i)
			{
				Value value = getConstantValueByIndex(i);
				tmp[i] = value != null ? value : values[j++];
			}
			values = tmp;
		}
		
		return _factorFunction.evalEnergy(values);
	}
	
	/**
	 * Evaluates energy for factor for given values.
	 * <p>
	 * Calls underlying {@linkplain #getFactorFunction() factor function} after inserting
	 * any constant values held by the factor.
	 * <p>
	 * @param values specifies the values of the sibling variables to be evaluated.
	 * @since 0.08
	 */
	public double evalEnergy(Object[] values)
	{
		final int nEdges = getSiblingCount();
		final int nArgs = getFactorArgumentCount();
		
		if (values.length != getSiblingCount())
		{
			throw new IllegalArgumentException("Values length does not match number of edges");
		}
	
		if (nEdges != nArgs)
		{
			// Fill in constant values
			Value[] tmp = new Value[nArgs];
			for (int i = 0, j = 0; i < nArgs; ++i)
			{
				Value value = getConstantValueByIndex(i);
				if (value != null)
				{
					tmp[i] = value;
				}
				else
				{
					Variable var = getSibling(j);
					tmp[i] = Value.create(var.getDomain(), values[j]);
					++j;
				}
			}
			values = tmp;
		}
		
		return _factorFunction.evalEnergy(values);
	}

	// FIXME document
	public double evalEnergy(IVariableToValue v2v)
	{
		return _factorFunction.evalEnergy(fillFactorArguments(v2v, null));
	}
	
	// FIXME document
	public Value[] fillFactorArguments(IVariableToValue v2v, @Nullable Value[] values)
	{
		final int nargs = getFactorArgumentCount();
		if (values == null || values.length != nargs)
		{
			values = new Value[nargs];
		}
		for (int i = 0; i < nargs; ++i)
		{
			IConstantOrVariable arg = getFactorArgument(i);
			
			if (arg instanceof Constant)
			{
				values[i] = ((Constant)arg).value();
			}
			else
			{
				Variable var = (Variable)arg;
				Value value = v2v.varToValue(var);
				if (value == null)
				{
					throw new IllegalStateException(format("There is no value for %s", var));
				}
				values[i] = value;
			}
		}
		return values;
	}
	
	/*-------------------
	 * Protected methods
	 */

	/**
	 * @category internal
	 */
	@Internal
	@Override
	protected void addSiblingEdgeState(EdgeState edge)
	{
		super.addSiblingEdgeState(edge);
		if (_factorArguments != _siblingEdges)
		{
			final int edgeNumber = edge.getFactorToVariableEdgeNumber();
			_factorArguments.add(edgeNumber);
			forgetConstantInfo();
		}
	}
	
	/**
	 * @category internal
	 */
	@Internal
	@Override
	protected void removeSiblingEdgeState(EdgeState edge)
	{
		if (_factorArguments != _siblingEdges)
		{
			final int edgeNumber = edge.getFactorToVariableEdgeNumber();
			int argumentIndex;
			if (_siblingToFactorArgumentNumber != NOT_YET_SET)
			{
				argumentIndex = _siblingToFactorArgumentNumber[edgeNumber];
			}
			else
			{
				argumentIndex = 0;
				for (int sibling = 0; sibling <= edgeNumber; ++argumentIndex)
				{
					if (Ids.typeIndexFromLocalId(_factorArguments.get(argumentIndex)) != Ids.CONSTANT_TYPE)
					{
						++sibling;
					}
				}
			}
			_factorArguments.remove(argumentIndex);
			forgetConstantInfo();
		}
		super.removeSiblingEdgeState(edge);
	}
	
	/**
	 * @category internal
	 */
	@Override
	@Internal
	protected void replaceSiblingEdgeState(EdgeState oldEdge, EdgeState newEdge)
	{
		super.replaceSiblingEdgeState(oldEdge, newEdge);
		if (_factorArguments != _siblingEdges)
		{
			final int newEdgeNumber = newEdge.getFactorToVariableEdgeNumber();
			final int argumentIndex = getIndexByEdge(newEdgeNumber);
			_factorArguments.set(argumentIndex, newEdgeNumber);
		}
	}

	/*----------------------------------------------
	 * Formerly FactorFunctionWithConstants methods
	 */
	
	// TODO - rename these methods as needed given the change in context
	
	@SuppressWarnings("deprecation")
	public boolean hasConstants()
	{
		return _factorArguments != _siblingEdges
			// FIXME
			|| _factorFunction.hasConstants();
	}
	
	@SuppressWarnings("deprecation")
	public int getConstantCount()
	{
		// FIXME
		if (_factorArguments == _siblingEdges)
		{
			return _factorFunction.getConstantCount();
		}
		
		return _factorArguments.size() - _siblingEdges.size();
	}
	
	@SuppressWarnings("deprecation")
	public List<Value> getConstantValues()
	{
		if (_factorArguments == _siblingEdges || _parentGraph == null)
		{
			// FIXME
			return _factorFunction.getConstantValues();
//			return Collections.emptyList();
		}
		
		final int n = getConstantCount();
		FactorGraph graph = requireParentGraph();
		final Value[] constants = new Value[n];
		for (int i = 0, j = 0; j < n; ++i)
		{
			Constant constant = graph.getConstantByLocalId(_factorArguments.get(i));
			if (constant != null)
			{
				constants[j++] = constant.value();
			}
		}
		
		return Arrays.asList(constants);
	}
	
	@SuppressWarnings("deprecation")
	public int[] getConstantIndices()
	{
		// FIXME
		if (_factorArguments == _siblingEdges)
		{
			return _factorFunction.getConstantIndices();
		}

		if (!hasConstants())
		{
			return ArrayUtil.EMPTY_INT_ARRAY;
		}
		
		final int n = getConstantCount();
		int[] indices = new int[n];
		for (int i = 0, j = 0; j < n; ++i)
		{
			if (Ids.typeIndexFromLocalId(_factorArguments.get(i)) == Ids.CONSTANT_TYPE)
			{
				indices[j++] = i;
			}
		}
		
		return indices;
	}
	
	@SuppressWarnings("deprecation")
	public boolean isConstantIndex(int index)
	{
		// FIXME
		if (_factorArguments == _siblingEdges)
		{
			return _factorFunction.isConstantIndex(index);
		}

		return Ids.typeIndexFromLocalId(_factorArguments.get(index)) == Ids.CONSTANT_TYPE;
	}
	
	public boolean hasConstantOfType(int index, Class<?> type)
	{
		Value value = getConstantValueByIndex(index);
		return value != null && type.isInstance(value.getObject());
	}
	
	public boolean hasConstantsInIndexRange(int minIndex, int maxIndex)
	{
		return numConstantsInIndexRange(minIndex, maxIndex) > 0;
	}
	
	public boolean hasConstantAtOrAboveIndex(int index)
	{
		return numConstantsAtOrAboveIndex(index) > 0;
	}
	
	public boolean hasConstantAtOrBelowIndex(int index)
	{
		return numConstantsAtOrBelowIndex(index) > 0;
	}

	@SuppressWarnings("deprecation")
	public int numConstantsAtOrAboveIndex(int index)
	{
		// FIXME - remove when FactorFunctionWithConstants is removed
		if (_factorArguments == _siblingEdges)
		{
			return _factorFunction.numConstantsAtOrAboveIndex(index);
		}
		
		return computeConstantInfo() ? getConstantCount() - _constantsBeforeFactorArgument[index] : 0;
	}
	
	@SuppressWarnings("deprecation")
	public int numConstantsAtOrBelowIndex(int index)
	{
		// FIXME - remove when FactorFunctionWithConstants is removed
		if (_factorArguments == _siblingEdges)
		{
			return _factorFunction.numConstantsAtOrBelowIndex(index);
		}
		
		return computeConstantInfo() ? _constantsBeforeFactorArgument[index + 1] : 0;
	}
	
	@SuppressWarnings("deprecation")
	public int numConstantsInIndexRange(int minIndex, int maxIndex)
	{
		// FIXME - remove when FactorFunctionWithConstants is removed
		if (_factorArguments == _siblingEdges)
		{
			return _factorFunction.numConstantsInIndexRange(minIndex, maxIndex);
		}
		
		if (computeConstantInfo())
		{
			int[] constantCounts = _constantsBeforeFactorArgument;
			return constantCounts[maxIndex + 1] - constantCounts[minIndex];
		}

		return 0;
	}
	
	@SuppressWarnings("deprecation")
	/**
	 * Returns {@link Constant#value} for given factor argument {@code index} or null if not a constant.
	 * @since 0.08
	 */
	public @Nullable Value getConstantValueByIndex(int index)
	{
		// FIXME - remove when FactorFunctionWithConstants is removed
		if (_factorArguments == _siblingEdges)
		{
			return _factorFunction.getConstantValueByIndex(index);
		}
		
		FactorGraph graph = _parentGraph;
		if (graph != null)
		{
			Constant constant = graph.getConstantByLocalId(_factorArguments.get(index));
			if (constant != null)
			{
				return constant.value();
			}
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	/**
	 * Returns sibling index corresponding to given factor argument index.
	 * <p>
	 * This should only be used if {@code index} does not refer to a constant.
	 * @since 0.08
	 */
	public int getEdgeByIndex(int index)
	{
		// FIXME - remove when FactorFunctionWithConstants is removed
		if (_factorArguments == _siblingEdges)
		{
			return _factorFunction.getEdgeByIndex(index);
		}
		
		return computeConstantInfo() ? index - _constantsBeforeFactorArgument[index] : index;
	}
	
	public int[] getEdgesFromIndices(int[] indices)
	{
		if (hasConstants())
		{
			// Filter out constant indices
			IntArrayList tmp = new IntArrayList(getSiblingCount());
			for (int index : indices)
			{
				if (!isConstantIndex(index))
				{
					tmp.add(getEdgeByIndex(index));
				}
			}
			tmp.trimToSize();
			indices = tmp.elements();
		}
		
		return indices;
	}
	
	@SuppressWarnings("deprecation")
	public int getIndexByEdge(int edge)
	{
		// FIXME - remove when FactorFunctionWithConstants is removed
		if (_factorArguments == _siblingEdges)
		{
			return _factorFunction.getIndexByEdge(edge);
		}

		return computeConstantInfo() ? _siblingToFactorArgumentNumber[edge] : edge;
	}
	
	private void forgetConstantInfo()
	{
		_constantsBeforeFactorArgument = NOT_YET_SET;
		_siblingToFactorArgumentNumber = NOT_YET_SET;
	}
	
	private boolean computeConstantInfo()
	{
		if (_constantsBeforeFactorArgument == NOT_YET_SET)
		{
			final IntArrayList factorArguments = _factorArguments;
			if (factorArguments == _siblingEdges)
			{
				// There are no constants.
				_constantsBeforeFactorArgument = ArrayUtil.EMPTY_INT_ARRAY;
				return false;
			}
			
			final int nArgs = factorArguments.size();
			final int[] constantsBeforeFactorArgument = new int[nArgs + 1];
			final int[] siblingToFactorArgumentNumber= new int[_siblingEdges.size()];
			int nConstants = 0;
			for (int sibling = 0, arg = 0; arg < nArgs; ++arg)
			{
				constantsBeforeFactorArgument[arg] = nConstants;
				if (Ids.typeIndexFromLocalId(_factorArguments.get(arg)) == Ids.CONSTANT_TYPE)
				{
					++nConstants;
				}
				else
				{
					siblingToFactorArgumentNumber[sibling++] = arg;
				}
			}
			constantsBeforeFactorArgument[nArgs] = nConstants;
			
			_constantsBeforeFactorArgument = constantsBeforeFactorArgument;
			_siblingToFactorArgumentNumber = siblingToFactorArgumentNumber;
			
			return true;
		}
		
		return _constantsBeforeFactorArgument.length != 0;
	}
}
