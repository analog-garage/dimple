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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
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
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
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
	private IntArrayList _argids;

	/**
	 * Mapping from sibling number offset in {@code _siblingEdges} to corresponding offset in {@link _factorArguments}
	 * <p>
	 * The entries from [0, {@link #getSiblingCount()} - 1] maps edges to factor arguments. Entries after that map
	 * constants to factor arguments.
	 * <p>
	 * Computed lazily.
	 */
	private int[] _edgeToArgNumber = NOT_YET_SET;
	
	/**
	 * Mapping from {@link _factorArguments} offset to sibling number offset in {@code _siblingEdges}. Offsets
	 * corresponding to constants, which have no corresponding edge, will instead map to offsets after the
	 * edges (i.e. the same format used by
	 * {@link JointDomainReindexer#createPermuter(JointDomainIndexer, JointDomainIndexer, int[])}
	 * <p>
	 * Computed lazily
	 */
	private int[] _argToEdge = NOT_YET_SET;
	
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
	
	/**
	 * @category internal
	 */
	@Internal
	public Factor(FactorFunction factorFunc)
	{
		super(Ids.INITIAL_FACTOR_ID);
		
		_factorFunction = factorFunc;
		_modelerFunctionName = factorFunc.getName();
		_argids = _siblingEdges;
	}

	protected Factor(Factor that)
	{
		super(that);
		_modelerFunctionName = that._modelerFunctionName;
		_factorFunction = that._factorFunction; // clone?
		// Note that this does not copy the constants or the edges
		_argids = _siblingEdges;
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
	protected void setArguments(int[] argids)
	{
		_argids = new IntArrayList(argids);
		forgetConstantInfo();
	}
	
	public IFactorTable getFactorTable()
	{
		throw new UnsupportedOperationException("Factor tables only available on DiscreteFactors");
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
			int[] to = requireNonNull(_factorFunction.getDirectedToIndices(getArgumentCount()));
			setDirectedTo(filterConstantArgIndices(to));
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
		final int nEdges = getSiblingCount();
		IFactorTable oldFactorTable = null;
		
		final FactorGraph parent = requireNonNull(getParentGraph());
		
		// Visit in reverse order so that disconnect is safe.
		
		int[] valueIndices = ArrayUtil.EMPTY_INT_ARRAY;
		IntArrayList factorArguments = _argids;
		int nRemoved = 0;
		
		for (int i = nEdges; --i>=0;)
		{
			final EdgeState edge = getSiblingEdgeState(i);
			final Variable var = edge.getVariable(parent);
			Value value = var.getPriorValue();
			if (value != null)
			{
				if (factorArguments == _argids)
				{
					valueIndices = new int[factorArguments.size()];
					Arrays.fill(valueIndices, -1);
					factorArguments = factorArguments.copy();
					factorArguments.trimToSize();
					if (isDiscrete())
					{
						// Before disconnecting siblings, force the factor table
						// to be instantiated if discrete, since it may depend on
						// the original edges.
						oldFactorTable = getFactorTable();
					}
				}
				final int argIndex = siblingNumberToArgIndex(i);
				valueIndices[argIndex] = value.getIndex();
				factorArguments.set(argIndex, parent.addConstant(value).getLocalId());
				removeSiblingEdge(edge);
				++nRemoved;
			}
		}
		
		if (nRemoved > 0)
		{
			setArguments(factorArguments.elements());
			
			if (_factorFunction instanceof TableFactorFunction)
			{
				IFactorTable newTable = requireNonNull(oldFactorTable).createTableConditionedOn(valueIndices);
				setFactorFunction(TableFactorFunction.forFactor(this, newTable));
			}
			else
			{
				setFactorFunction(_factorFunction);
			}
		}
		
		return nRemoved;
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
	
	/**
	 * The domains of the {@link #getArgument(int) factor arguments} in order.
	 * <p>
	 * Similar to {@link #getDomainList()} but for factor arguments instead of sibling
	 * variables. If factor does not {@linkplain #hasConstants() have constants}, this is
	 * the same as {@link #getDomainList()}. If there are constants, they will be represented
	 * using single-element {@link DiscreteDomain}s.
	 * <p>
	 * @since 0.08
	 */
	public DomainList<?> getArgumentDomains()
	{
		int nArgs = getArgumentCount();
		Domain [] domains = new Domain[nArgs];
		
		for (int i = 0; i < nArgs; i++)
		{
			IConstantOrVariable arg = getArgument(i);
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
			setDirectedTo(filterConstantArgIndices(requireNonNull(func.getDirectedToIndices(getArgumentCount()))));
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
	
	public int getArgumentCount()
	{
		return _argids.size();
	}
	
	@SuppressWarnings("null")
	public IConstantOrVariable getArgument(int argIndex)
	{
		final FactorGraph graph = requireParentGraph();
		int id = _argids.get(argIndex);
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
		final int nArgs = getArgumentCount();
		
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
		final int nArgs = getArgumentCount();
		
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

	/**
	 * Evaluates energy for factor for values computed from its variables and constants.
	 * <p>
	 * Builds a {@link Value} list using {@code v2v} mapping for variable arguments, and
	 * the constant value directly for constant arguments, and then computes the energy
	 * using the underlying {@linkplain #getFactorFunction() factor function}.
	 * <p>
	 * For use internally in solver implementations.
	 * @since 0.08
	 */
	public double evalEnergy(IVariableToValue v2v)
	{
		return _factorFunction.evalEnergy(fillInArgumentValues(v2v, null));
	}
	
	/**
	 * Fills in array of values corresponding to factor's arguments.
	 * <p>
	 * For use internally in solver implementations.
	 * <p>
	 * @param v2v maps {@link Variable} instances to corresponding {@link Value}
	 * @param values array to fill in. Only used if non-null and has length equal to
	 * {@link #getArgumentCount() the number of factor arguments}.
	 * @return filled in array
	 * @since 0.08
	 * @category internal
	 */
	@Internal
	public Value[] fillInArgumentValues(IVariableToValue v2v, @Nullable Value[] values)
	{
		final int nargs = getArgumentCount();
		if (values == null || values.length != nargs)
		{
			values = new Value[nargs];
		}
		for (int i = 0; i < nargs; ++i)
		{
			IConstantOrVariable arg = getArgument(i);
			
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
		if (_argids != _siblingEdges)
		{
			final int edgeNumber = edge.getFactorToVariableEdgeNumber();
			_argids.add(edgeNumber);
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
		if (_argids != _siblingEdges)
		{
			final int edgeNumber = edge.getFactorToVariableEdgeNumber();
			int argumentIndex;
			if (_edgeToArgNumber != NOT_YET_SET)
			{
				argumentIndex = _edgeToArgNumber[edgeNumber];
			}
			else
			{
				argumentIndex = -1;
				for (int sibling = 0; sibling <= edgeNumber; )
				{
					++argumentIndex;
					if (Ids.typeIndexFromLocalId(_argids.get(argumentIndex)) != Ids.CONSTANT_TYPE)
					{
						++sibling;
					}
				}
			}
			_argids.remove(argumentIndex);
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
		if (_argids != _siblingEdges)
		{
			final int newEdgeNumber = newEdge.getFactorToVariableEdgeNumber();
			final int argumentIndex = siblingNumberToArgIndex(newEdgeNumber);
			_argids.set(argumentIndex, newEdgeNumber);
		}
	}

	/*----------------------------------------------
	 * Formerly FactorFunctionWithConstants methods
	 */
	
	/**
	 * True if factor has any constant arguments.
	 * @since 0.08
	 * @see #getConstants
	 */
	public final boolean hasConstants()
	{
		return _argids != _siblingEdges;
	}

	/**
	 * Returns a read-only list view of the arguments to the factor.
	 * <p>
	 * The arguments will contain either {@link Variable}s or {@link Constant}s.
	 * @since 0.08
	 * @see #getArgument(int)
	 */
	public List<IConstantOrVariable> getArguments()
	{
		return new AbstractList<IConstantOrVariable>() {

			@Override
			public IConstantOrVariable get(int index)
			{
				return getArgument(index);
			}

			@Override
			public int size()
			{
				return _argids.size();
			}
		};
	}
	
	/**
	 * The number of constant arguments to this factor.
	 * @since 0.08
	 * @see #getConstants()
	 */
	public final int getConstantCount()
	{
		return _argids.size() - _siblingEdges.size();
	}
	
	/**
	 * Returns a read-only view of the constant arguments.
	 * @since 0.08
	 * @see #getArguments()
	 * @see #getConstantValues()
	 */
	public final List<Constant> getConstants()
	{
		if (!computeConstantInfo())
			return Collections.emptyList();
		
		final int nEdges = getSiblingCount();
		final FactorGraph graph = requireParentGraph();
		
		return new AbstractList<Constant> () {
			@Override
			public Constant get(int index)
			{
				final int argNumber = _edgeToArgNumber[index + nEdges];
				return requireNonNull(graph.getConstantByLocalId(_argids.get(argNumber)));
			}

			@Override
			public int size()
			{
				return getConstantCount();
			}
		};
	}
	
	/**
	 * Returns a read-only view of the constant argument values.
	 * @since 0.08
	 * @see #getArguments()
	 * @see #getConstants()
	 */
	public List<Value> getConstantValues()
	{
		if (!computeConstantInfo())
			return Collections.emptyList();
		
		final int nEdges = getSiblingCount();
		final FactorGraph graph = requireParentGraph();
		
		return new AbstractList<Value> () {
			@Override
			public Value get(int index)
			{
				final int argNumber = _edgeToArgNumber[index + nEdges];
				return requireNonNull(graph.getConstantByLocalId(_argids.get(argNumber))).value();
			}

			@Override
			public int size()
			{
				return getConstantCount();
			}
		};
	}
	
	/**
	 * Returns copy of the factor argument indices that refer to constants.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @since 0.08
	 */
	public int[] getConstantIndices()
	{
		return computeConstantInfo() ?
			Arrays.copyOfRange(_edgeToArgNumber, _siblingEdges.size(), _edgeToArgNumber.length)
			: ArrayUtil.EMPTY_INT_ARRAY;
	}
	
	/**
	 * True if there is a constant argument at given index.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @since 0.08
	 */
	public boolean hasConstantAtIndex(int index)
	{
		return Ids.typeIndexFromLocalId(_argids.get(index)) == Ids.CONSTANT_TYPE;
	}
	
	/**
	 * True if there is a constant argument with given value type at given index.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @param index
	 * @param type is the type of the constant value's Object type, not the subclass of
	 * {@link Value}. E.g., use {@code Real.class} rather than {@code RealValue.class}.
	 * @since 0.08
	 */
	public boolean hasConstantAtIndexOfType(int index, Class<?> type)
	{
		Value value = getConstantValueByIndex(index);
		return value != null && type.isInstance(value.getObject());
	}
	
	public boolean hasConstantsInIndexRange(int minIndex, int maxIndex)
	{
		return numConstantsInIndexRange(minIndex, maxIndex) > 0;
	}
	
	/**
	 * True if there are constants with argument index not less than given index.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @since 0.08
	 * @see #numConstantsAtOrAboveIndex(int)
	 */
	public boolean hasConstantAtOrAboveIndex(int index)
	{
		return numConstantsAtOrAboveIndex(index) > 0;
	}
	
	/**
	 * True if there are constants with argument index not greater than given index.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @since 0.08
	 * @see #numConstantsAtOrBelowIndex(int)
	 */
	public boolean hasConstantAtOrBelowIndex(int index)
	{
		return numConstantsAtOrBelowIndex(index) > 0;
	}

	/**
	 * Returns the number of constants with argument index not less than given index.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @since 0.08
	 */
	public int numConstantsAtOrAboveIndex(int index)
	{
		if (!computeConstantInfo())
			return 0;
		
		final int nEdges = _siblingEdges.size();
		final int x = _argToEdge[index];
		final int nConstants = getConstantCount();
		
		if (x < nEdges)
		{
			// An edge index
			return nConstants + x - index;
		}
		else
		{
			// A constant
			return nConstants + nEdges - x;
		}
	}
	
	/**
	 * Returns the number of constants with argument index not greater than given index.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @since 0.08
	 */
	public int numConstantsAtOrBelowIndex(int index)
	{
		if (!computeConstantInfo())
			return 0;
		
		final int nEdges = _siblingEdges.size();
		final int x = _argToEdge[index];
		if (x < nEdges)
		{
			// An edge index
			return index - x;
		}
		else
		{
			// A constant
			return 1 + x - nEdges;
		}
	}
	
	/**
	 * Returns the number of constants with argument index in given inclusive range.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @since 0.08
	 */
	public int numConstantsInIndexRange(int minIndex, int maxIndex)
	{
		int n = numConstantsAtOrBelowIndex(maxIndex);
		
		if (n > 0 && minIndex > 0)
		{
			n -= numConstantsAtOrBelowIndex(minIndex - 1);
		}
		
		return n;
	}
	
	/**
	 * Returns {@link Constant#value} for given factor argument {@code index} or null if not a constant.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @since 0.08
	 */
	public @Nullable Value getConstantValueByIndex(int index)
	{
		FactorGraph graph = _parentGraph;
		if (graph != null)
		{
			Constant constant = graph.getConstantByLocalId(_argids.get(index));
			if (constant != null)
			{
				return constant.value();
			}
		}
		return null;
	}
	
	/**
	 * Returns sibling edge number corresponding to given factor argument index.
	 * <p>
	 * If index refers to a sibling edge, this will return the corresponding sibling number that. If index
	 * refers to a constant argument, this will return the {@linkplain #getSiblingCount() number of sibling edges}
	 * plus the index of the constant in the ordered list of indexes.
	 * <p>
	 * {@link #siblingNumberToArgIndex(int)} is the inverse of this method.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @param index must be non-negative and less than the {@linkplain #getArgumentCount() number of factor arguments}.
	 * <p>
	 * @since 0.08
	 * @see #getArgument(int)
	 */
	public int argIndexToSiblingNumber(int index)
	{
		return computeConstantInfo() ? _argToEdge[index] : index;
	}
	
	/**
	 * @category internal
	 * @since 0.08
	 */
	@Internal
	public int[] getArgIndexToToSiblingNumberMapping()
	{
		return computeConstantInfo() ? _argToEdge.clone() : ArrayUtil.EMPTY_INT_ARRAY;
	}
	
	/**
	 * Filters out constant factor argument indices.
	 * <p>
	 * This is primarily intended for use by solver implementations.
	 * <p>
	 * @param indices contains factor argument indices in any order.
	 * @return copy of {@code indices} after removing any indices that refer to constants. If
	 * the factor has no constants, this simply returns the original indices.
	 * @since 0.08
	 */
	public int[] filterConstantArgIndices(int[] indices)
	{
		if (hasConstants())
		{
			// Filter out constant indices
			IntArrayList tmp = new IntArrayList(getSiblingCount());
			for (int index : indices)
			{
				if (!hasConstantAtIndex(index))
				{
					tmp.add(argIndexToSiblingNumber(index));
				}
			}
			tmp.trimToSize();
			indices = tmp.elements();
		}
		
		return indices;
	}
	
	/**
	 * Returns factor argument index corresponding to given sibling edge number.
	 * <p>
	 * If {@code siblingNumber} is less than {@link #getSiblingCount}, this returns the corresponding
	 * factor argument index for that sibling edge. If it is larger than that, then this will return
	 * the factor argument index for the nth constant where n is {@code siblingNumber} minus the
	 * sibling count.
	 * <p>
	 * {@link #argIndexToSiblingNumber(int)} is the inverse of this method.
	 * <p>
	 * @param siblingNumber must be non-negative and less than the {@linkplain #getArgumentCount() number of
	 * factor arguments}
	 * @since 0.08
	 */
	public int siblingNumberToArgIndex(int siblingNumber)
	{
		return computeConstantInfo() ? _edgeToArgNumber[siblingNumber] : siblingNumber;
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private void forgetConstantInfo()
	{
		_edgeToArgNumber = NOT_YET_SET;
		_argToEdge = NOT_YET_SET;
	}
	
	/**
	 * Compute fields related to constant argument indexing
	 * @return true if factor has constants
	 * @since 0.08
	 */
	private boolean computeConstantInfo()
	{
		if (_edgeToArgNumber == NOT_YET_SET)
		{
			final IntArrayList factorArguments = _argids;
			if (factorArguments == _siblingEdges)
			{
				// There are no constants.
				_edgeToArgNumber = ArrayUtil.EMPTY_INT_ARRAY;
				return false;
			}
			
			final int nEdges = _siblingEdges.size();
			final int nArgs = factorArguments.size();
			final int[] edgeToArgNumber = new int[nArgs];
			final int[] argToEdgeNumber = new int[nArgs];
			for (int edgei = 0, argi = 0, constanti = nEdges; argi < nArgs; ++argi)
			{
				if (Ids.typeIndexFromLocalId(_argids.get(argi)) == Ids.CONSTANT_TYPE)
				{
					edgeToArgNumber[constanti] = argi;
					argToEdgeNumber[argi] = constanti;
					++constanti;
				}
				else
				{
					argToEdgeNumber[argi] = edgei;
					edgeToArgNumber[edgei] = argi;
					++edgei;
				}
			}
			
			_edgeToArgNumber = edgeToArgNumber;
			_argToEdge = argToEdgeNumber;
			
			return true;
		}
		
		return _edgeToArgNumber.length != 0;
	}
}
