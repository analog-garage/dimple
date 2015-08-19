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

package com.analog.lyric.dimple.model.core;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ExtendedArrayList;
import com.analog.lyric.collect.IndexedArrayList;
import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.data.DataLayer;
import com.analog.lyric.dimple.data.GenericDataLayer;
import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Uniform;
import com.analog.lyric.dimple.factorfunctions.core.CustomFactorFunctionWrapper;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.JointFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.JointFactorFunction.Functions;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.core.Edge.Type;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.factors.FactorList;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.repeated.FactorGraphStream;
import com.analog.lyric.dimple.model.repeated.IVariableStreamSlice;
import com.analog.lyric.dimple.model.repeated.VariableStreamBase;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Constant;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.IConstantOrVariable;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.schedulers.CustomScheduler;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.SchedulerOptionKey;
import com.analog.lyric.dimple.schedulers.schedule.EmptySchedule;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.options.IOption;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.Option;
import com.analog.lyric.util.misc.FactorGraphDiffs;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.Internal;
import com.analog.lyric.util.misc.MapList;
import com.analog.lyric.util.misc.Matlab;
import com.analog.lyric.util.test.Helpers;
import com.google.common.base.Predicate;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import cern.colt.list.IntArrayList;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;


@NotThreadSafe
public class FactorGraph extends FactorBase
{
	/*-----------
	 * Constants
	 */

	 /**
	 * Flags that are reserved for use by this class and should not be
	 * used by subclasses when invoking {@link #setFlags(int)} or {@link #clearFlags()}.
	 */
	@SuppressWarnings("hiding")
	protected static final int RESERVED = 0xFFFFFF00;
	
	private static final int FACTOR_ADD_EVENT               = 0xFFFF0100;
	private static final int FACTOR_REMOVE_EVENT            = 0xFFFF0200;
	private static final int VARIABLE_ADD_EVENT             = 0xFFFF0400;
	private static final int VARIABLE_REMOVE_EVENT          = 0xFFFF0800;
	private static final int SUBGRAPH_ADD_EVENT             = 0xFFFF1000;
	private static final int SUBGRAPH_REMOVE_EVENT          = 0xFFFF2000;
	private static final int BOUNDARY_VARIABLE_ADD_EVENT    = 0xFFFF4000;
	private static final int BOUNDARY_VARIABLE_REMOVE_EVENT = 0xFFFF8000;
	
	private static final int EVENT_MASK                     = 0x0000FF00;
	
	/*-------
	 * State
	 */
	
	/**
	 * Environment for this graph determined at construction. Unless the constructor
	 * specifies a different one, this will be the value of {@link DimpleEnvironment.active}
	 * when the object was constructed.
	 */
	private final DimpleEnvironment _env;
	
	/**
	 * Unique identifier for this graph within its environment.
	 */
	private final int _graphId;
	
	private volatile @Nullable IDimpleEventSource _eventAndOptionParent;
	
	private final OwnedVariables _ownedVariables = new OwnedVariables();

	private final IndexedArrayList<Variable> _boundaryVariables = new IndexedArrayList<>();
	
	/**
	 * Factors and subgraphs contained directly by this graph. Does not include
	 * factors and subgraphs contained in subgraphs of this graph.
	 */
	private final OwnedFactors _ownedFactors = new OwnedFactors();
	
	private final OwnedGraphs _ownedSubGraphs = new OwnedGraphs();
	
	private final OwnedVariableBlocks _ownedVariableBlocks = new OwnedVariableBlocks();
	
	private final OwnedConstants _ownedConstants = new OwnedConstants();

	/**
	 * Holds state common to entire tree of FactorGraphs.
	 */
	private static class GraphTreeState
	{
		private @Nullable DataLayer<? extends IDatum> _defaultConditioningLayer = null;
		
		/**
		 * Counter that is modified whenever a structural change is made anywhere in the graph tree.
		 */
		private long _globalStructureVersion = 0;
		
		/**
		 * List of all graphs in the graph tree that shares a common root, which will the first element.
		 * Each graph is indexed by its graph tree index.
		 */
		private final ExtendedArrayList<FactorGraph> _graphs;
		
		/**
		 * Number of non-null graph entries in {@link #_graphs} list.
		 */
		private int _nGraphs = 0;
		
		/**
		 * The highest numbered index of any non-null entry of {@links _graphs} list.
		 */
		private int _maxGraphTreeIndex = 0;
		
		private GraphTreeState(FactorGraph root)
		{
			_graphs = new ExtendedArrayList<FactorGraph>(1);
			addGraph(root);
		}
		
		private GraphTreeState addGraph(FactorGraph graph)
		{
			final int index = _graphs.size();
			graph._graphTreeIndex = index;
			_maxGraphTreeIndex = index;
			_graphs.add(graph);
			++_nGraphs;
			return this;
		}
		
		private void removeGraph(FactorGraph graph)
		{
			int index = graph._graphTreeIndex;
			assert(graph == _graphs.get(index));
			_graphs.set(index, null);
			if (index == _maxGraphTreeIndex)
			{
				// If at end of list, we can reclaim slots at end immediately without affecting other indexes.
				do
				{
					_graphs.remove(index);
					// There always has to be a graph in this list, so this has to terminate.
				} while (_graphs.get(--index) == null);
				_maxGraphTreeIndex = index;
				_graphs.setSize(index+1);
			}
			--_nGraphs;
		}
	}

	private GraphTreeState _graphTreeState;
	
	/**
	 * Index of this graph within {@code _graphTreeState._graphs}.
	 */
	private int _graphTreeIndex = -1;
	
	/**
	 * Incremented for every change to the structure of this graph.
	 */
	private long _structureVersion = 0;
	
	/**
	 * If not equal to _structureVersion, indicates that the graph siblings list is out-of-date.
	 */
	private long _siblingVersionId = -1;

	// TODO : some state only needs to be in root graph. Put it in common object.
	
	private @Nullable IFactorGraphFactory<?> _solverFactory;
	private @Nullable ISolverFactorGraph _solverFactorGraph;
	private @Nullable LoadingCache<Functions, JointFactorFunction> _jointFactorCache = null;
	private final HashSet<VariableStreamBase<?>> _variableStreams = new HashSet<>();
	private final ArrayList<FactorGraphStream> _factorGraphStreams = new ArrayList<FactorGraphStream>();
	private int _numSteps = 1;
	private boolean _numStepsInfinite = true;
	
	//new identity related members
	private final HashMap<String, Node> _name2object = new HashMap<>();
	
	private final EdgeStateList _edges;
	
	/**
	 * Edges defining the graph's siblings.
	 * <p>
	 * This replaces the sibling indexes from Node, which cannot be used for this due to the
	 * fact the edges may not be a member of this graph's edge list.
	 */
	private final ArrayList<EdgeState> _graphSiblings = new ArrayList<>();
	
	/*--------------
	 * Edge classes
	 */
	
	// Note that because we expect there to be potentially many edges, we go to the effort of
	// of providing implementations that require less memory. We assume that Java will use 8-byte
	// alignment, so we aim to have the size be a multiple of that.
	
	/**
	 * Edge connecting factor and variable owned by same graph.
	 */
	@Immutable
	private static abstract class LocalEdgeState extends EdgeState
	{
		static LocalEdgeState create(int edgeIndex, int factorIndex, int variableIndex)
		{
			if (edgeIndex <= SmallLocalEdgeState.EDGE_MASK &&
				factorIndex <= SmallLocalEdgeState.NODE_MASK &&
				variableIndex <= SmallLocalEdgeState.NODE_MASK)
			{
				return new SmallLocalEdgeState(edgeIndex, factorIndex, variableIndex);
			}
			return new FullLocalEdgeState(edgeIndex, factorIndex, variableIndex);
		}

		@Override
		public String toString()
		{
			return String.format("[LocalEdgeState #%d: %d - %d]", factorEdgeIndex(), factorIndex(), variableIndex());
		}
		
		@Override
		public final Factor getFactor(FactorGraph fg)
		{
			return fg._ownedFactors.get(factorIndex());
		}
		
		@Override
		public FactorGraph getFactorParent(FactorGraph graph)
		{
			return graph;
		}
		
		@Override
		public final Variable getVariable(FactorGraph fg)
		{
			return fg._ownedVariables.get(variableIndex());
		}

		@Override
		public FactorGraph getVariableParent(FactorGraph graph)
		{
			return graph;
		}
		
		@Override
		public final boolean isLocal()
		{
			return true;
		}

		@Override
		public Type type(FactorGraph graph)
		{
			return Edge.Type.LOCAL;
		}
		
		@Override
		public final int variableLocalId()
		{
			return Ids.localIdFromParts(Ids.VARIABLE_TYPE, variableIndex());
		}
	}
	
	@Immutable
	private static final class SmallLocalEdgeState extends LocalEdgeState
	{
		final private long _data;
		
		private static final int EDGE_BITS = 26;
		private static final int NODE_BITS = 19;
		private static final int EDGE_MASK = (1 << EDGE_BITS) - 1;
		private static final int NODE_MASK = (1 << NODE_BITS) - 1;
		private static final int FACTOR_OFFSET = EDGE_BITS;
		private static final int VARIABLE_OFFSET = EDGE_BITS + NODE_BITS;
		
		private SmallLocalEdgeState(int edgeIndex, int factorIndex, int variableIndex)
		{
			_data = edgeIndex | (long)factorIndex << FACTOR_OFFSET | (long)variableIndex << VARIABLE_OFFSET;
		}
		
		@Override
		public int edgeIndex(Node node)
		{
			return (int)_data & EDGE_MASK;
		}
		
		@Override
		public int edgeIndexInParent(FactorGraph graph)
		{
			return (int)_data & EDGE_MASK;
		}

		@Override
		public int factorEdgeIndex()
		{
			return (int)_data & EDGE_MASK;
		}
		
		@Override
		public int variableEdgeIndex()
		{
			return (int)_data & EDGE_MASK;
		}

		@Override
		public int factorIndex()
		{
			return (int)(_data >>> FACTOR_OFFSET) & NODE_MASK;
		}

		@Override
		public int variableIndex()
		{
			return (int)(_data >>> VARIABLE_OFFSET) & NODE_MASK;
		}
	}
	
	@Immutable
	private static final class FullLocalEdgeState extends LocalEdgeState
	{
		final private int _edgeIndex;
		final private int _factorIndex;
		final private int _variableIndex;
		
		private FullLocalEdgeState(int edgeIndex, int factorOffset, int variableOffset)
		{
			_edgeIndex = edgeIndex;
			_factorIndex = factorOffset;
			_variableIndex = variableOffset;
		}
		
		@Override
		public int edgeIndex(Node node)
		{
			return _edgeIndex;
		}

		@Override
		public int edgeIndexInParent(FactorGraph graph)
		{
			return _edgeIndex;
		}
		
		@Override
		public int factorEdgeIndex()
		{
			return _edgeIndex;
		}
		
		@Override
		public int variableEdgeIndex()
		{
			return _edgeIndex;
		}
		
		@Override
		public int factorIndex()
		{
			return _factorIndex;
		}
		
		@Override
		public int variableIndex()
		{
			return _variableIndex;
		}
	}
	

	/**
	 * Edge connecting factor owned by this graph to one of its boundary variables.
	 */
	static abstract class BoundaryEdge extends EdgeState
	{
		final Factor _factor;
		
		private BoundaryEdge(Factor factor)
		{
			_factor = factor;
		}
		
		@Override
		public String toString()
		{
			return String.format("[BoundaryEdge %d/%d: %d (%s) - %d (%s)]",
				factorEdgeIndex(), variableEdgeIndex(),
				factorIndex(), _factor.getQualifiedName(),
				variableIndex(), getVariable().getQualifiedName());
		}
		
		static BoundaryEdge create(Factor factor, int factorEdgeIndex, int boundaryIndex, int variableEdgeIndex)
		{
			if (factorEdgeIndex <= SmallBoundaryEdge.EDGE_MASK &&
				variableEdgeIndex <= SmallBoundaryEdge.EDGE_MASK &&
				boundaryIndex <= SmallBoundaryEdge.VARIABLE_MASK)
			{
				return new SmallBoundaryEdge(factor, factorEdgeIndex, boundaryIndex, variableEdgeIndex);
			}
			return new FullBoundaryEdge(factor, factorEdgeIndex,  boundaryIndex, variableEdgeIndex);
		}
		
		@Override
		public final Factor getFactor(FactorGraph fg)
		{
			return _factor;
		}

		@Override
		public FactorGraph getFactorParent(FactorGraph graph)
		{
			return requireNonNull(_factor.getParentGraph());
		}
		
		final Variable getVariable()
		{
			final FactorGraph fg = requireNonNull(_factor.getParentGraph());
			return fg._boundaryVariables.get(variableIndex());
		}
		
		@Override
		public final Variable getVariable(FactorGraph fg)
		{
			return getVariable();
		}
		
		@Override
		public FactorGraph getVariableParent(FactorGraph graph)
		{
			return requireNonNull(getVariable().getParentGraph());
		}

		@Override
		public final int factorLocalId()
		{
			return _factor.getLocalId();
		}
		
		@Override
		public final int factorIndex()
		{
			return Ids.indexFromLocalId(_factor.getLocalId());
		}
		
		@Override
		public final boolean isLocal()
		{
			return false;
		}
		
		@Override
		public Type type(FactorGraph graph)
		{
			return getFactorParent(graph) == graph ? Edge.Type.OUTER : Edge.Type.INNER;
		}

		@Override
		public int variableLocalId()
		{
			return Ids.localIdFromParts(Ids.BOUNDARY_VARIABLE_TYPE, variableIndex());
		}
	}
	
	private static final class SmallBoundaryEdge extends BoundaryEdge
	{
		private final int _data;
		
		private static final int EDGE_BITS = 11;
		private static final int EDGE_MASK = (1 << EDGE_BITS) - 1;
		private static final int VARIABLE_BITS = 10;
		private static final int VARIABLE_MASK = (1 << VARIABLE_BITS) - 1;
		private static final int FACTOR_EDGE_OFFSET = VARIABLE_BITS;
		private static final int VARIABLE_EDGE_OFFSET = FACTOR_EDGE_OFFSET + EDGE_BITS;
		
		private SmallBoundaryEdge(Factor factor, int factorEdgeIndex, int boundaryIndex, int variableEdgeIndex)
		{
			super(factor);
			_data = boundaryIndex | factorEdgeIndex<<FACTOR_EDGE_OFFSET | variableEdgeIndex<<VARIABLE_EDGE_OFFSET;
		}

		@Override
		public int edgeIndex(Node node)
		{
			return node.isVariable() ? variableEdgeIndex() : factorEdgeIndex();
		}

		@Override
		public int edgeIndexInParent(FactorGraph graph)
		{
			return _factor.getParentGraph() == graph ? factorEdgeIndex() : variableEdgeIndex();
		}
		
		@Override
		public int factorEdgeIndex()
		{
			return (_data >>> FACTOR_EDGE_OFFSET) & EDGE_MASK;
		}

		@Override
		public int variableEdgeIndex()
		{
			// Mask not needed because these are the high-end bits
			return _data >>> VARIABLE_EDGE_OFFSET;
		}

		@Override
		public int variableIndex()
		{
			return _data & VARIABLE_MASK;
		}
	}
	
	private static final class FullBoundaryEdge extends BoundaryEdge
	{
		private final int _factorEdgeIndex;
		private final int _boundaryIndex;
		private final int _variableEdgeIndex;
		
		private FullBoundaryEdge(Factor factor, int factorEdgeIndex, int boundaryIndex, int variableEdgeIndex)
		{
			super(factor);
			_factorEdgeIndex = factorEdgeIndex;
			_boundaryIndex = boundaryIndex;
			_variableEdgeIndex = variableEdgeIndex;
		}

		@Override
		public int edgeIndex(Node node)
		{
			return node.isVariable() ? _variableEdgeIndex : _factorEdgeIndex;
		}

		@Override
		public int edgeIndexInParent(FactorGraph graph)
		{
			return _factor.getParentGraph() == graph ? _factorEdgeIndex : _variableEdgeIndex;
		}
		
		@Override
		public int factorEdgeIndex()
		{
			return _factorEdgeIndex;
		}
		
		@Override
		public int variableEdgeIndex()
		{
			return _variableEdgeIndex;
		}

		@Override
		public int variableIndex()
		{
			return _boundaryIndex;
		}
	}
	
	/*--------------
	 * Construction
	 */

	public FactorGraph()
	{
		this(new Variable[0], "");
	}
	public FactorGraph(@Nullable String name)
	{
		this(null, name);
	}
	/**
	 * @since 0.07
	 */
	public FactorGraph(@Nullable Variable ... boundaryVariables)
	{
		this(boundaryVariables, "");
	}

	/**
	 * @since 0.07
	 */
	public FactorGraph(@Nullable Variable[] boundaryVariables, @Nullable String name)
	{
		this(boundaryVariables, name, Model.getInstance().getDefaultGraphFactory());
	}

	/**
	 * @since 0.07
	 */
	public FactorGraph(
		@Nullable Variable[] boundaryVariables,
		@Nullable String name,
		@Nullable IFactorGraphFactory<?> solver)
	{
		this(null, boundaryVariables, name, solver);
	}
	
	private FactorGraph(
		@Nullable GraphTreeState rootState,
		@Nullable Variable[] boundaryVariables,
		@Nullable String name,
		@Nullable IFactorGraphFactory<?> solver)
	{
		super(Ids.INITIAL_GRAPH_ID);
		
		_env = DimpleEnvironment.active();
		_graphId = _env.factorGraphs().registerIdForFactorGraph(this);
		_eventAndOptionParent = _env;
		_graphTreeState = rootState != null ? rootState.addGraph(this) : new GraphTreeState(this);
		
		_edges = new EdgeStateList(this);
		
		if (boundaryVariables != null)
		{
			addBoundaryVariables(boundaryVariables);
		}

		if ("".equals(name))
		{
			name = null;
		}
		setName(name);

		if (solver != null)
		{
			setSolverFactory(solver);
		}
		
		notifyListenerChanged();
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
	public NodeType getNodeType()
	{
		return NodeType.GRAPH;
	}
	
	@Override
	public String getClassLabel()
	{
		return "Graph";
	}
	
	/**
	 * Unique identifier of graph within its {@linkplain #getEnvironment() environment}.
	 * <p>
	 * Will be in the range from {@link Ids#GRAPH_ID_MIN} to {@link Ids#GRAPH_ID_MAX}.
	 * <p>
	 * @since 0.08
	 */
	public int getGraphId()
	{
		return _graphId;
	}

	@Override
	public long getGlobalId()
	{
		if (getParentGraph() == null)
		{
			// If there is no parent graph, then use the graph id as the id.
			return  Ids.globalIdFromParts(0, Ids.GRAPH_TYPE, _graphId);
		}
		else
		{
			return super.getGlobalId();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * For root graphs (that have no {@linkplain #getParentGraph() parent graph})
	 * the implicitly generated name will be computed by {@link Ids#defaultNameForGraphId(int)}
	 * using the value of {@link #getGraphId()}.
	 */
	@Override
	public String getName()
	{
		String name = _name;
		if (name != null)
		{
			return name;
		}
		
		if (getParentGraph() == null)
		{
			return Ids.defaultNameForGraphId(_graphId);
		}
		else
		{
			return Ids.defaultNameForLocalId(getLocalId());
		}
	}
	
	/*--------------------------
	 * IDimpleEnvironmentHolder
	 */
	
	@Override
	public DimpleEnvironment getEnvironment()
	{
		return _env;
	}
	
	/*---------------------
	 * IDimpleOptionHolder
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * By default this returns {@link #getParentGraph()} if non null, and otherwise returns
	 * the value of {@link #getEnvironment()} (which means that the environment is the default
	 * root of the event source hierarchy), but it may be also be set to another value using
	 * {@link #setEventAndOptionParent(IDimpleEventSource)}.
	 * <p>
	 * @see #setParentGraph(FactorGraph)
	 * @since 0.07
	 */
	@Override
	public @Nullable IDimpleEventSource getOptionParent()
	{
		return _eventAndOptionParent;
	}
	
	/*-------------------
	 * IDimpleEventSource
	 */
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public FactorGraph getContainingGraph()
	{
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * By default this returns {@link #getParentGraph()} if non null, and otherwise returns
	 * the value of {@link #getEnvironment()} (which means that the environment is the default
	 * root of the event source hierarchy), but it may be also be set to another value using
	 * {@link #setEventAndOptionParent(IDimpleEventSource)}.
	 * <p>
	 * @see #setParentGraph(FactorGraph)
	 */
	@Override
	public @Nullable IDimpleEventSource getEventParent()
	{
		return _eventAndOptionParent;
	}
	
	@Override
	public void notifyListenerChanged()
	{
		final IDimpleEventListener listener = getEventListener();
		
		int eventMask = 0;
		
		if (listener != null)
		{
			if (listener.isListeningFor(FactorAddEvent.class, this))
			{
				eventMask |= FACTOR_ADD_EVENT;
			}
			if (listener.isListeningFor(FactorRemoveEvent.class,  this))
			{
				eventMask |= FACTOR_REMOVE_EVENT;
			}
			if (listener.isListeningFor(VariableAddEvent.class, this))
			{
				eventMask |= VARIABLE_ADD_EVENT;
			}
			if (listener.isListeningFor(VariableRemoveEvent.class, this))
			{
				eventMask |= VARIABLE_REMOVE_EVENT;
			}
			if (listener.isListeningFor(BoundaryVariableAddEvent.class, this))
			{
				eventMask |= BOUNDARY_VARIABLE_ADD_EVENT;
			}
			if (listener.isListeningFor(BoundaryVariableRemoveEvent.class, this))
			{
				eventMask |= BOUNDARY_VARIABLE_REMOVE_EVENT;
			}
			if (listener.isListeningFor(SubgraphAddEvent.class, this))
			{
				eventMask |= SUBGRAPH_ADD_EVENT;
			}
			if (listener.isListeningFor(SubgraphRemoveEvent.class, this))
			{
				eventMask |= SUBGRAPH_REMOVE_EVENT;
			}
		}
		
		setFlagValue(EVENT_MASK, eventMask);
	}
	
	/**
	 * Sets the option/event parent object to specified value.
	 * <p>
	 * Sets the value returned by {@link #getOptionParent()}/{@link #getEventParent()} to {@code parent},
	 * which may be null. Note that this will override any previous value set by
	 * {@link #setEventAndOptionParent(IDimpleEventSource)}.
	 * <p>
	 * @param parent
	 * @since 0.07
	 */
	public void setEventAndOptionParent(@Nullable IDimpleEventSource parent)
	{
		_eventAndOptionParent = parent;
	}
	
	/*---------------
	 * Node methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * In addition to setting the value of @link #getParentGraph()}, this will also
	 * reset the value of {@link #getOptionParent()} and {@link #getEventParent()} to
	 * the new parent graph if non-null and otherwise the value of {@link #getEnvironment()}.
	 */
	@Override
	protected void setParentGraph(@Nullable FactorGraph parentGraph)
	{
		super.setParentGraph(parentGraph);
		if (parentGraph != null)
		{
			_eventAndOptionParent = parentGraph;
		}
		else
		{
			_eventAndOptionParent = _env;
		}
	}
	
	/*==============
	 * Data methods
	 */
	
	/**
	 * The default conditioning layer to be used with this model, if any.
	 * <p>
	 * The same object is returned for all factor graphs in the graph tree.
	 * 
	 * @since 0.08
	 * @see #createDefaultConditioningLayer()
	 * @see #setDefaultConditioningLayer(DataLayer)
	 */
	public @Nullable DataLayer<? extends IDatum> getDefaultConditioningLayer()
	{
		return _graphTreeState._defaultConditioningLayer;
	}
	
	/**
	 * Returns the default conditioning layer to be used with this model, creating a new one if necessary.
	 * <p>
	 * This is similar to {@link #getDefaultConditioningLayer()}, but instead of returning null
	 * if there is no layer, this will  create a new sparse {@link GenericDataLayer}
	 * and set it on the {@linkplain #getRootGraph() root graph} using {@link #setDefaultConditioningLayer(DataLayer)}.
	 * 
	 * @since 0.08
	 */
	public DataLayer<? extends IDatum> createDefaultConditioningLayer()
	{
		DataLayer<? extends IDatum> layer = getDefaultConditioningLayer();
		if (layer == null)
		{
			setDefaultConditioningLayer(layer = new GenericDataLayer(getRootGraph()));
		}
		return layer;
	}

	/**
	 * Sets the {@linkplain #getDefaultConditioningLayer() default conditioning layer} for this model.
	 * <p>
	 * This may only be set on the root graph for the graph tree and will be shared by all subgraphs.
	 * <p>
	 * If the graph has a designated solver graph ({@link #getSolver()}), this will be set as the
	 * conditioning layer for that solver.
	 * <p>
	 * @throws IllegalStateException if this is not the root graph.
	 * @since 0.08
	 * @see #createDefaultConditioningLayer()
	 */
	public void setDefaultConditioningLayer(@Nullable DataLayer<? extends IDatum> layer)
	{
		if (this != getRootGraph())
		{
			throw new IllegalStateException("The default conditioning layer may only be set on the root graph.");
		}
		
		_graphTreeState._defaultConditioningLayer = layer;
		
		ISolverFactorGraph sfg = getSolver();
		if (sfg != null)
		{
			sfg.setConditioningLayer(layer);
		}
	}
	
	/*==============
	 * Solver stuff
	 */
	
	private @Nullable ISolverFactorGraph setSolverFactorySubGraph(@Nullable ISolverFactorGraph parentSolverGraph,
		@Nullable IFactorGraphFactory<?> factory)
	{
		_solverFactory = factory;
		return _solverFactorGraph = parentSolverGraph != null ? parentSolverGraph.getSolverSubgraph(this) : null;
	}

	private void setSolverFactorySubGraphRecursive(@Nullable ISolverFactorGraph parentSolverGraph,
		@Nullable IFactorGraphFactory<?> factory)
	{
		ISolverFactorGraph solverGraph = setSolverFactorySubGraph(parentSolverGraph, factory);
		for (FactorGraph fg : _ownedSubGraphs)
			fg.setSolverFactorySubGraphRecursive(solverGraph, factory);
	}

	/**
	 * Creates solver graph and attaches to this model.
	 * <p>
	 * Same as {@link #setSolverFactory(IFactorGraphFactory)} but does not accept a null
	 * factory and returns a non-null solver graph.
	 * <p>
	 * @param factory
	 * @since 0.06
	 */
	public <SG extends ISolverFactorGraph> SG createSolver(IFactorGraphFactory<SG> factory)
	{
		return Objects.requireNonNull(setSolverFactory(Objects.requireNonNull(factory)));
	}
	
	/**
	 * Sets solver for graph.
	 * 
	 * @param factory is used to construct the solver graph and will be used to create solver
	 * state for any nodes subsequently added to the graph. If null, then the solver will be
	 * cleared from the graph.
	 * @return The newly created solver graph or else null if {@code factory} was null.
	 */
	public @Nullable <SG extends ISolverFactorGraph> SG setSolverFactory(@Nullable IFactorGraphFactory<SG> factory)
	{
		_solverFactory = factory;

		final FactorGraph parent = getParentGraph();
		final ISolverFactorGraph sparent = parent != null ? parent.getSolver() : null;
		
		SG solverGraph = factory != null ? factory.createFactorGraph(this, sparent) : null;
		_solverFactorGraph = solverGraph;

		if (parent != null)
		{
			final ISolverFactorGraph parentSolverGraph = parent._solverFactorGraph;
			if (parentSolverGraph != null)
			{
				parentSolverGraph.recordDefaultSubgraphSolver(this);
			}
		}
		
		for (FactorGraph fg : _ownedSubGraphs)
			fg.setSolverFactorySubGraphRecursive(solverGraph, factory);

		for (FactorGraph graph : FactorGraphIterables.subgraphs(this))
		{
			ISolverFactorGraph sgraph = graph.getSolver();
			for (Variable var : graph._ownedVariables)
			{
				var.createSolverObject(sgraph);
			}
			
			for (Factor factor : graph._ownedFactors)
			{
				factor.createSolverObject(sgraph);
			}
		}
		
		return solverGraph;
	}

	//========================
	//
	// Tables and Functions
	//
	//========================

	public static boolean allDomainsAreDiscrete(Variable [] vars)
	{
		for (int i = 0; i < vars.length; i++)
		{
			if (!vars[i].getDomain().isDiscrete())
				return false;
		}

		return true;
	}

    public BlastFromThePastFactor addBlastFromPastFactor(Variable var,Port factorPort)
    {
		boolean setVarSolver = false;
		
		// FIXME boundary variable solver logic is hacky
		if (_solverFactorGraph != null && !variableBelongs(var))
		{
			if (var.getSiblingCount() > 0)
				throw new DimpleException("Can't connect a variable to multiple graphs");
			
			setVarSolver = true;
		}

		// Add any variables that do not yet have a parent
    	if (var.getParentGraph() == null)
    	{
    		addVariables(var);
    	}
    	
    	setVariableSolver(var);

		BlastFromThePastFactor f;
		f = new BlastFromThePastFactor(var,factorPort);

		addFactor(f,var);
		
		// Give factor a descriptive name to ease debugging.
		f.setName(String.format("$BFP_%s_to_%s_%s",
			factorPort.getSiblingNode().getName(), var.getName(), f.getReverseSiblingNumber(0)));

		if (setVarSolver)
		{
			var.createSolverObject(_solverFactorGraph);
		}
		
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
				_variableStreams.add(((IVariableStreamSlice<?>) v).getStream());
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
		ISolverFactorGraph solver = requireSolver("advance");
		
		for (VariableStreamBase<?> vs : _variableStreams)
		{
			vs.advanceState();
		}
		for (FactorGraphStream s : _factorGraphStreams)
		{
			s.advance();
		}

		solver.postAdvance();

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

	/**
	 * Adds a new constant value.
	 * <p>
	 * Add a new constant owned by this graph for use with factors also owned by this graph.
	 * <p>
	 * @param value specifies the constant. It will be {@linkplain Value#immutableClone() cloned immutably}
	 * if necessary.
	 * @return the newly added constant object.
	 * @since 0.08
	 */
	public Constant addConstant(Value value)
	{
		Constant constant = new Constant(value);
		_ownedConstants.add(constant);
		return constant;
	}
	
	/**
	 * Adds a new constant value.
	 * <p>
	 * Add a new constant owned by this graph for use with factors also owned by this graph.
	 * <p>
	 * @param object specifies the constant, may be one of:
	 * <ul>
	 * <li>{@link Constant}: if owned by this graph will simply be returned, otherwise a new
	 * constant will be added with the same {@link Constant#value}.
	 * <li>{@link Value}: same as {@link #addConstant(Value)}.
	 * <li>any other {@link Object}: will be wrapped using {@link Value#constant}
	 * </ul>
	 * @return the newly added constant object.
	 * @since 0.08
	 */
	public Constant addConstant(Object object)
	{
		if (object instanceof Constant)
		{
			Constant constant = (Constant)object;
			if (constant.getParentGraph() == this)
			{
				return constant;
			}
			
			return addConstant(constant.value());
		}
		
		if (object instanceof Value)
		{
			return addConstant((Value)object);
		}

		return addConstant(Value.constant(object));
	}
	
	public Factor addFactor(int [][] indices, double [] weights, Discrete ... vars)
	{
		return addFactor(FactorTable.create(indices, weights, vars),vars);
	}

	public Factor addFactor(IFactorTable ft, Variable ... vars)
	{
		return addFactor(new TableFactorFunction("TableFactorFunction",ft),vars);
	}

	public Factor addFactor(FactorFunction factorFunction, Variable ... vars)
	{
		return addFactor(factorFunction, (Object[])vars);
	}

	public Factor addFactor(String factorFunctionName, Object ... vars)
	{
		if (customFactorExists(factorFunctionName))
			return addFactor(new CustomFactorFunctionWrapper(factorFunctionName), vars);
		else
		{
			FactorFunction factorFunction = getEnvironment().factorFunctions().instantiate(factorFunctionName);
			return addFactor(factorFunction, vars);
		}
	}
	
	public Factor addFactor(String factorFunctionName, Variable...vars)
	{
		return addFactor(factorFunctionName, (Object[])vars);
	}
	
	public Factor addFactor(FactorFunction factorFunction, Object ... varsAndConstants)
	{
		final int nArgs = varsAndConstants.length;
		int[] factorArguments = new int[nArgs];
		boolean allVarsDiscrete = true;
		int nVars = 0;
		
		for (int i = 0; i < nArgs; ++i)
		{
			final Object arg = varsAndConstants[i];
			if (arg instanceof Variable)
			{
				++nVars;
				Variable var = (Variable)arg;
				if (!var.hasParentGraph())
				{
					addVariables(var);
				}
				factorArguments[i] = _edges.allocateIndex();
				
				setVariableSolver(var);
				
				if (!var.getDomain().isDiscrete())
				{
					allVarsDiscrete = false;
				}
			}
			else
			{
				factorArguments[i] = addConstant(arg).getLocalId();
			}
		}
		
		if (nVars == 0)
			throw new DimpleException("must pass at least one variable to addFactor");
		
		Factor factor = allVarsDiscrete ? new DiscreteFactor(factorFunction) : new Factor(factorFunction);
		addOwnedFactor(factor, false);
		
		for (int i = 0; i < nArgs; ++i)
		{
			final int id = factorArguments[i];
			if (Ids.typeIndexFromLocalId(id) != Ids.CONSTANT_TYPE)
			{
				addEdge(id, factor, (Variable)varsAndConstants[i]);
			}
		}
		
		if (nVars != nArgs)
		{
			((Node)factor).setFactorArguments(factorArguments);
		}
		
		final ISolverFactorGraph sfg = _solverFactorGraph;
		if (sfg != null)
		{
			factor.createSolverObject(_solverFactorGraph);
			sfg.postAddFactor(factor);
		}

		return factor;
	}
	
	private void setVariableSolver(Variable v)
	{
		if (_solverFactorGraph != null)
		{
			//check to see if variable belongs to this graph
			if (!variableBelongs(v))
			{
				if (v.getSiblingCount() > 0)
					throw new DimpleException(String.format("Can't connect variable %s to multiple graphs", v));

				v.createSolverObject(_solverFactorGraph);
			}
		}
	}

	/**
	 * True if variable is one of this graph's boundary variables or is
	 * owned by this graph or one of its subgraphs.
	 */
	private boolean variableBelongs(Variable v)
	{
		// TODO: apart from the boundary variable case, it seems that it would probably be
		// more efficient to simply walk the ancestor chain from v to see if it hits this graph.
		
		if (_ownedVariables.containsNode(v))
			return true;
		if (_boundaryVariables.contains(v))
			return true;

		for (FactorGraph fg : getNestedGraphs())
			if (fg.variableBelongs(v))
				return true;

		return false;

	}
	
	/**
	 * True if child is owned directly by this graph.
	 *
	 * @param node
	 */
	public boolean ownsDirectly(FactorGraphChild node)
	{
		final boolean owns = node.getParentGraph() == this;
		assert(owns == ownsDirectly_(node));
		return owns;
	}

	/**
	 * Slower version of {@link #OwnsDirectly} just used for
	 * checking correctness in assertion.
	 */
	private boolean ownsDirectly_(FactorGraphChild node)
	{
		switch (Ids.typeIndexFromLocalId(node.getLocalId()))
		{
		case Ids.VARIABLE_TYPE:
			return _ownedVariables.containsNode(node);
		case Ids.FACTOR_TYPE:
			return _ownedFactors.containsNode(node);
		case Ids.GRAPH_TYPE:
			return _ownedSubGraphs.containsNode(node);
		case Ids.VARIABLE_BLOCK_TYPE:
			return _ownedVariableBlocks.containsNode(node);
		}
		
		return false;
	}
	
	/**
	 * Removes variables from the graph.
	 * <p>
	 * This simply invokes {@link #remove(Variable)} on each.
	 * 
	 * @param variables are the variables to be removed.
	 */
	public void removeVariables(Variable ... variables)
	{
		for (Variable v : variables)
		{
			remove(v);
		}
	}

	/**
	 * Remove variable from the graph.
	 * @param v is the variable to remove
	 * @throws DimpleException if the variable is still connected to some factor or if the variable
	 * is not owned by this graph.
	 * @see #remove(Factor)
	 */
	public void remove(Variable v)
	{
		if (v.getSiblingCount() != 0)
			throw new DimpleException("can only remove a variable if it is no longer connected to a factor");

		if (!_ownedVariables.containsNode(v))
		{
			throw new DimpleException("can only currently remove variables that are owned");
		}
		
		v.createSolverObject(null);
		((Node)v).setParentGraph(null);
		_ownedVariables.removeNode(v);
		removeNode(v);
		
		ISolverVariable svar = v.getSolver();
		if (svar != null)
		{
			svar.getParentGraph().removeSolverVariable(svar);
		}
		
		if ((_flags & VARIABLE_REMOVE_EVENT) != 0)
		{
			raiseEvent(new VariableRemoveEvent(this, v));
		}
	}
		
	public void addBoundaryVariables(Variable ... vars)
	{
		for (Variable v : vars)
		{
			boolean setSolver = false;
			
			// FIXME boundary variable solver logic is hacky
			if (_solverFactorGraph != null && !variableBelongs(v))
			{
				if (v.getSiblingCount() > 0)
					throw new DimpleException("Can't connect a variable to multiple graphs");
				
				setSolver = true;
			}
			
			//if (_boundaryVariables.contains(v))
			//	throw new DimpleException("ERROR name [" + v.getName() + "] already a boundary variable");
	
			//being the root, at least for the moment,
			//I'm this variable's owner, if it has no other
			if (v.getParentGraph() == null)
			{
				addOwnedVariable(v, false);
			}

			_boundaryVariables.add(v);
	
			if (v.getParentGraph() != this)
			{
				addName(v);
			}
			
			if (setSolver && _solverFactorGraph != null)
			{
				v.createSolverObject(_solverFactorGraph);
			}
		}

		if ((_flags & BOUNDARY_VARIABLE_ADD_EVENT) != 0)
		{
			for (Variable v : vars)
			{
				raiseEvent(new BoundaryVariableAddEvent(this, v));
			}
		}
	}

	
	public void addVariables(Variable... variables)
	{
		for (Variable v : variables)
		{
			if (_boundaryVariables.contains(v))
			{
				throw new DimpleException("Cannot take ownership of boundary variable [" + v.getLabel() + "]");
			}
			
			final FactorGraph parent = v.getParentGraph();
			if (parent != null)
			{
				throw new DimpleException("Variable [" + v.getLabel() + "] already owned by graph [" + parent.getLabel() + "]");
			}
			addOwnedVariable(v, false);
			v.createSolverObject(_solverFactorGraph);

		}
	}

	/**
	 * Creates a {@link VariableBlock} containing the specified variables.
	 * @param variables are the variables that will comprise the block. The variables will be added in the
	 * order of the {@linkplain Collection#iterator iterator}.
	 * @since 0.08
	 * @throws IllegalArgumentException if a variable does not belong to the same tree of graphs as {@code parent}.
	 */
	public VariableBlock addVariableBlock(Collection<Variable> variables)
	{
		return _ownedVariableBlocks.addBlock(new VariableBlock(this, variables));
	}

	/**
	 * @see #addVariableBlock(Collection)
	 * @since 0.08
	 */
	public VariableBlock addVariableBlock(Variable ... variables)
	{
		return addVariableBlock(Arrays.asList(variables));
	}

	/**
	 * Joining factors replaces all the original factors with one joint factor.
	 * <p>
	 * We take the Cartesian product of the entries of the tables such that the
	 * variables values are consistent. The variable order is determined by taking
	 * all of the variables from the first factor in order, then adding remaining
	 * variables in order from each remaining factor in turn.
	 * <p>
	 * @return the new joint factor
	 * @see #join(Variable[], Factor...)
	 */
	public Factor join(Factor ... factors)
	{
		Set<Variable> variables = new LinkedHashSet<Variable>();
		for (Factor factor : factors)
		{
			final int nVarsInFactor = factor.getSiblingCount();
			for (int i = 0; i < nVarsInFactor; ++i)
			{
				final Variable variable = factor.getSibling(i);
				if (!variables.contains(variable))
				{
					variables.add(variable);
				}
			}
		}
		return join(variables.toArray(new Variable[variables.size()]), factors);
	}
	
	/**
	 * Merges {@code factors} into a single joint factor over the given set of variables.
	 * <p>
	 * @param variables specifies the variables and the order in which they will appear in the new joint factor.
	 * This may include variables that are not in any of the specified {@code factors} but must not omit any
	 * variable that appears in one of the {@code factors} nor should it repeat any variable.
	 * @param factors specifies the factors to be merged. If empty, this will add a new uniform factor over
	 * the specified variables.
	 * @return the new joint factor. If {@code factors} has a single entry with the specified
	 * {@code variables} in the specified order, this will simply return that factor without
	 * modifying the graph.
	 */
	public Factor join(final Variable[] variables, Factor ... factors)
	{
		final int nFactors = factors.length;
		final int nVariables = variables.length;
		
		if (nFactors == 0)
		{
			return addFactor(Uniform.INSTANCE, variables);
		}
		else if (nFactors == 1)
		{
			final Factor factor = factors[0];
			outer:
			if (factor.getSiblingCount() == nVariables)
			{
				for (int i = 0; i < nVariables; ++i)
				{
					if (variables[0] != factor.getSibling(i))
					{
						break outer;
					}
				}
				
				// Factor already in correct form.
				return factor;
			}
		}
	
		// Build map of variables and constants in all factors to its index in the merged factor.
		final Map<IConstantOrVariable, Integer> argToIndex = new LinkedHashMap<>();
		for (int i = 0; i < nVariables; ++i)
		{
			argToIndex.put(variables[i], i);
		}
		for (Factor factor : factors)
		{
			for (Constant constant : factor.getConstants())
			{
				if (!argToIndex.containsKey(constant))
				{
					argToIndex.put(constant, argToIndex.size());
				}
			}
		}
		
		// Build mappings from each factor's variable order to the merged order
		final BitSet varsUsed = new BitSet(nVariables);
		ArrayList<Tuple2<FactorFunction, int[]>> oldToNew = new ArrayList<Tuple2<FactorFunction, int[]>>(nFactors);
		for (Factor factor : factors)
		{
			final int nArgsInFactor = factor.getFactorArgumentCount();
			final int[] oldToNewIndex = new int[nArgsInFactor];
			for (int i = 0; i < nArgsInFactor; ++i)
			{
				final IConstantOrVariable arg = factor.getFactorArgument(i);
				final Integer oldIndex = argToIndex.get(arg);
				if (oldIndex == null)
				{
					throw new DimpleException("Variable %s from factor %s not in variable list for join", arg, factor);
				}
				oldToNewIndex[i] = oldIndex.intValue();
				if (arg instanceof Variable)
				{
					varsUsed.set(oldIndex.intValue());
				}
			}
			oldToNew.add(Tuple2.create(factor.getFactorFunction(), oldToNewIndex));
		}
		
		// If there are variables that are not in any factor, create a virtual uniform
		// factor for those variables
		IntArrayList extraVariables = null;
		for (int i = -1; (i = varsUsed.nextClearBit(i + 1)) < nVariables;)
		{
			if (extraVariables == null)
			{
				extraVariables = new IntArrayList();
			}
			extraVariables.add(i);
		}
		if (extraVariables != null)
		{
			extraVariables.trimToSize();
			oldToNew.add(Tuple2.create((FactorFunction)Uniform.INSTANCE, extraVariables.elements()));
		}
		
		// Create the joint factor function
		FactorGraph root = getRootGraph();
		LoadingCache<Functions,JointFactorFunction> jointFactorCache = root._jointFactorCache;
		if (jointFactorCache == null)
		{
			jointFactorCache = root._jointFactorCache = JointFactorFunction.createCache();
		}
		
		final JointFactorFunction.Functions jointFunctions = new JointFactorFunction.Functions(oldToNew);
		final FactorFunction jointFunction = JointFactorFunction.getFromCache(jointFactorCache, jointFunctions);
		
		// Determine common parent
		final List<FactorGraph> uncommonAncestors = new LinkedList<FactorGraph>();
		FactorGraph parentGraph = factors[0].getParentGraph();
		for (int i = 1; i < nFactors; ++i)
		{
			parentGraph = factors[i - 1].getCommonAncestor(factors[i], uncommonAncestors);
		}

		if (parentGraph == null)
		{
			throw new DimpleException("Cannot join factors because they are not in the same root graph");
		}
		
		// Remove old factors
		for (Factor factor : factors)
		{
			requireNonNull(factor.getParentGraph()).remove(factor);
		}

		// If all factors did not have the same parent, then remove any intermediate subgraphs.
		for (FactorGraph subgraph : uncommonAncestors)
		{
			requireNonNull(subgraph.getParentGraph()).absorbSubgraph(subgraph);
		}
		
		// Add new factor
		if (nVariables == argToIndex.size())
		{
			return parentGraph.addFactor(jointFunction, variables);
		}
		else
		{
			return parentGraph.addFactor(jointFunction, Iterables.toArray(argToIndex.keySet(), Object.class));
		}
	}

	/**
	 * Joining variables creates one joint and discards the originals and modifies
	 * factors to refer to the joints.
	 */
	public Variable join(Variable ... variables)
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
		for (Variable v : variables)
		{
			for (int i = 0, endi = v.getSiblingCount(); i < endi; i++)
			{
				Factor f = (Factor)v.getConnectedNodeFlat(i);
				factors.add(f);
			}
		}

		//Create joint variable
		Variable joint = variables[0].createJointNoFactors(variables);

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

		return joint;
	}

	/*
	 * Splitting a variable creates a copy and an equals node between the two.
	 * @since 0.07
	 */
	public Variable split(Variable variable)
	{
		return split(variable,new Factor[]{});
	}

	/*
	 * Splitting a variable creates a copy and an equals node between the two.
	 * The Factor array specifies which factors should connect to the new variable.
	 * All factors left out of the array remain pointing to the original variable.
	 * @since 0.07
	 */
	public Variable split(Variable variable,Factor ... factorsToBeMovedToCopy)
	{
		return variable.split(this,factorsToBeMovedToCopy);
	}


	private FactorBase addFactor(Factor function, Variable ... variables)
	{
		addOwnedFactor(function, false);
		for (Variable var : variables)
		{
			addEdge(function, var);
		}
		return function;
	}

	public boolean customFactorExists(String funcName)
	{
		final ISolverFactorGraph sfg = _solverFactorGraph;
		if (sfg != null)
			return sfg.customFactorExists(funcName);
		else
			return false;
	}
	
	//==============
	//
	// Scheduling
	//
	//==============

	/**
	 * Sets scheduler options applicable to input scheduler.
	 * 
	 * @param scheduler if non-null, will be set as an option value for all of the option keys
	 * it lists in its {@linkplain IScheduler#applicableSchedulerOptions() applicableSchedulerOptions()}
	 * method. If null and graph has a {@link #getSolver() current solver}, its
	 * {@linkplain ISolverFactorGraph#getSchedulerKey() scheduler key} will be unset, otherwise
	 * no action will be taken.
	 */
	public void setScheduler(@Nullable IScheduler scheduler)
	{
		if (scheduler != null)
		{
			for (SchedulerOptionKey key : scheduler.applicableSchedulerOptions())
			{
				key.set(this, scheduler);
			}
		}
		else
		{
			final ISolverFactorGraph sgraph = getSolver();
			if (sgraph != null)
			{
				SchedulerOptionKey key = sgraph.getSchedulerKey();
				if (key != null)
				{
					unsetOption(key);
				}
			}
		}
	}

	
	
	//============================
	//
	// Nested Graphs
	//
	//============================

	public FactorGraph addFactor(FactorGraph subGraph, Variable ... boundaryVariables)
	{
		return addGraph(subGraph,boundaryVariables);
	}

	/**
	 * Add a new subgraph generated from specified template graph
	 * attached to given boundary variables.
	 * <p>
	 * @param subGraphTemplate
	 * @param boundaryVariables
	 * @return newly created subgraph
	 */
	public FactorGraph addGraph(FactorGraph subGraphTemplate, Variable ... boundaryVariables)
	{

		// FIXME: solver logic is hacky
		// Really we should not try to update solver state while mutating the graph.
		// Instead wait until we are done...
		
		List<Variable> needsSolver = Collections.emptyList();
		if (_solverFactorGraph != null)
		{
			needsSolver = new ArrayList<>(boundaryVariables.length);
			for (Variable v : boundaryVariables)
			{
				if (!variableBelongs(v))
				{
					needsSolver.add(v);
				}
			}
		}

		for (Variable v : boundaryVariables)	// Add variables to owned variable list if not a boundary variable
		{
			if (!_boundaryVariables.contains(v))
			{
				addOwnedVariable(v, false);
			}
		}

		//copy the graph
		FactorGraph subGraphCopy = new FactorGraph(boundaryVariables, subGraphTemplate,this);

		for (Variable v : needsSolver)
		{
			v.createSolverObject(_solverFactorGraph);
		}
		
		//tell us about it
		addOwnedSubgraph(subGraphCopy, false);

		if (_solverFactory != null)
		{
			subGraphCopy.setSolverFactory(_solverFactory);
		}

		return subGraphCopy;
	}


	private void _setParentGraph(@Nullable FactorGraph parentGraph)
	{
		boolean noLongerRoot = parentGraph != null && getParentGraph() == null;
		setParentGraph(parentGraph);

		//If we were root, and are no longer,
		//		stop references names/UUIDs of boundary variables.
		if(noLongerRoot)
		{
			// FIXME: this seems really hacky - we are depending on someone fixing up the boundary variables
			// later.
			for(Variable v : _boundaryVariables)
			{
				String explicitName = v.getExplicitName();
				if(explicitName != null)
				{
					_name2object.remove(explicitName);
				}
				if(v.getParentGraph() == this)
				{
					_ownedVariables.removeNode(v);
					((Node)v).setParentGraph(null);
				}
			}
		}
	}


	private FactorGraph(@Nullable Variable[] boundaryVariables,
			FactorGraph templateGraph,
			@Nullable FactorGraph parentGraph)
			{
		this(boundaryVariables,
				templateGraph,
				parentGraph,
				false,
				new HashMap<>());
			}

	// Copy constructor -- create a graph incorporating all of the variables, functions, and sub-graphs of the template graph
	private FactorGraph(@Nullable Variable[] boundaryVariables,
			FactorGraph templateGraph,
			@Nullable FactorGraph parentGraph,
			boolean copyToRoot,
			Map<Object, Object> old2newObjs)
	{
		this(parentGraph != null ? parentGraph._graphTreeState : null, boundaryVariables,
			templateGraph.getExplicitName(), null);

		// Add mapping from template to this graph
		old2newObjs.put(templateGraph, this);
		
		// Copy owned variables
		for (Variable vTemplate : templateGraph._ownedVariables)
		{
			if (!templateGraph.isBoundaryVariable(vTemplate))
			{
				Variable vCopy = vTemplate.clone();

				//old2newIds.put(vTemplate.getId(), vCopy.getId());
				old2newObjs.put(vTemplate,vCopy);
				addOwnedVariable(vCopy, false);
			}
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
			for (Variable vTemplate : templateGraph._boundaryVariables)
			{
				Variable vBoundary = boundaryVariables[i++];
				if (!vBoundary.getDomain().equals(vTemplate.getDomain()))
					throw new DimpleException("Boundary variable does not have the same domain as template graph.  Index: " + (i-1));

				old2newObjs.put(vTemplate,vBoundary);
			}
		}
		
		// Copy constants
		for (Constant templateConstant : templateGraph._ownedConstants)
		{
			Constant constant = addConstant(templateConstant.value());
			old2newObjs.put(templateConstant, constant);
		}
		
		// Copy blocks
		for (VariableBlock templateBlock : templateGraph._ownedVariableBlocks)
		{
			Variable[] vars = templateBlock.toArray(new Variable[templateBlock.size()]);
			for (int i = vars.length; --i>=0;)
			{
				vars[i] = (Variable)old2newObjs.get(vars[i]);
			}
			VariableBlock block = addVariableBlock(vars);
			old2newObjs.put(templateBlock, block);
		}

		for (FactorGraph subGraph : templateGraph._ownedSubGraphs)
		{
			Variable[] vBoundary = new Variable[subGraph._boundaryVariables.size()];
			{
				int i = 0;
				for (Variable v : subGraph._boundaryVariables)
					vBoundary[i++] = (Variable)old2newObjs.get(v);
			}
			// Add the graph using the appropriate boundary variables
			FactorGraph newGraph = addGraph(subGraph, vBoundary);
			old2newObjs.put(subGraph,newGraph);
		}

		for (Factor fTemplate : templateGraph._ownedFactors)
		{
			Factor fCopy = fTemplate.clone();
			old2newObjs.put(fTemplate,fCopy);

			addName(fCopy);
			fCopy.setParentGraph(this);
			_ownedFactors.add(fCopy);
			for (Variable vTemplate : fTemplate.getSiblings())
			{
				Variable var = (Variable)old2newObjs.get(vTemplate);
				addEdge(fCopy, var);
			}
			
			if (fTemplate.hasConstants())
			{
				final int nArgs = fTemplate.getFactorArgumentCount();
				int[] argids = new int[nArgs];
				for (int i = 0, j = 0; i < nArgs; ++i)
				{
					final IConstantOrVariable arg = fTemplate.getFactorArgument(i);
					if (arg instanceof Constant)
					{
						argids[i] = ((Constant)old2newObjs.get(arg)).getLocalId();
					}
					else
					{
						argids[i] = fCopy.getSiblingEdgeIndex(j++);
					}
				}
				((Node)fCopy).setFactorArguments(argids);
			}
		}

		// Copy options from template
		for (IOption<?> option : templateGraph.getLocalOptions())
		{
			IOptionKey<?> key = option.key();
			
			if (key instanceof SchedulerOptionKey)
			{
				// TODO perhaps we should generalize this copy operation to support other types
				// of special option values.
				SchedulerOptionKey schedulerKey = (SchedulerOptionKey)key;
				IScheduler scheduler = (IScheduler)requireNonNull(option.value());
				scheduler = scheduler.copy(old2newObjs, copyToRoot);
				option = new Option<IScheduler>(schedulerKey, scheduler);
			}

			Option.setOptions(this, option);
		}
		
		_setParentGraph(parentGraph);

	}

	public FactorGraph copyRoot()
	{
		return copyRoot(new HashMap<Object, Object>());
	}
	public FactorGraph copyRoot(Map<Object, Object> old2newObjs)
	{
		FactorGraph root = getRootGraph();

		int numBoundaryVariables = root._boundaryVariables.size();
		Variable[] rootBoundaryVariables = root._boundaryVariables.toArray(new Variable[numBoundaryVariables]);
		Variable[] boundaryVariables = new Variable[numBoundaryVariables];
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

	@Override
	public Port getPort(int i)
	{
		return ((List<Port>)getPorts()).get(i);
	}
	
	/**
	 * Returns ports for edges from factors contained in this graph to its boundary variables.
	 */
	@Override
	public Collection<Port> getPorts()
	{
		if (_boundaryVariables.isEmpty())
		{
			return Collections.emptyList();
		}
		
		ArrayList<Port> ports = new ArrayList<>();

		for (Variable var : _boundaryVariables)
		{
			if (ownsDirectly(var))
			{
				continue;
			}
	
			for (int i = 0, ni = var.getSiblingCount(); i < ni; ++i)
			{
				final Factor factor = var.getSibling(i);
				if (isAncestorOf(factor))
				{
					for (int j = 0, nj = factor.getSiblingCount(); j < nj; ++j)
					{
						if (var == factor.getSibling(j))
						{
							ports.add(factor.getPort(j));
						}
					}
				}
			}
		}

		return ports;
	}

	@Override
	public List<? extends Variable> getSiblings()
	{
		updateSiblings();
		return super.getSiblings();
	}

	@Override
	public int getSiblingCount()
	{
		updateSiblings();
		return _graphSiblings.size();
	}
	
	@Override
	public Variable getSibling(int index)
	{
		updateSiblings();
		return super.getSibling(index);
	}
	
	private void updateSiblings()
	{
		if (_siblingVersionId != _structureVersion)
		{
			// Recompute siblings
			_graphSiblings.clear();
			
			if (!_boundaryVariables.isEmpty())
			{
				for (Variable var : _boundaryVariables)
				{
					final FactorGraph varGraph = requireNonNull(var.getParentGraph());
					if (ownsDirectly(var))
					{
						continue;
					}
					for (int i = 0, ni = var.getSiblingCount(); i < ni; ++i)
					{
						final EdgeState edge = var.getSiblingEdgeState(i);
						
						if (isAncestorOf(edge.getFactor(varGraph)))
						{
							_graphSiblings.add(edge);
						}
					}
				}
			}
			_siblingVersionId = _structureVersion;
		}
	}
	
	@Override
	public EdgeState getSiblingEdgeState(int i)
	{
		updateSiblings();
		return _graphSiblings.get(i);
	}

	@Override
	public int indexOfSiblingEdgeState(EdgeState edge)
	{
		updateSiblings();
		return _graphSiblings.indexOf(edge);
	}
	
	/**
	 * Returns {@link Edge} at given index, if it exists.
	 * <p>
	 * If this returns a non-null {@code edge}, the following will be true:
	 * <blockquote><code>
	 *     edge.{@linkplain Edge#edgeIndex() edgeIndex()} == index &&
	 *     edge.{@linkplain Edge#graph() graph()} == this
	 * </code></blockquote>
	 * 
	 * @param index an integer in the range [0, {@link #getGraphEdgeStateMaxIndex}]
	 * @since 0.08
	 * @throws ArrayIndexOutOfBoundsException if {@code index} is not in valid range
	 */
	public @Nullable Edge getGraphEdge(int index)
	{
		final EdgeState state = getGraphEdgeState(index);
		return state != null ? new Edge(this, state) : null;
	}
	
	/**
	 * Returns collection of {@link Edge}s held by this graph.
	 * @since 0.08
	 */
	public Collection<Edge> getGraphEdges()
	{
		return _edges.allEdges();
	}
	
	/**
	 * Returns collection of all {@link EdgeState} objects held by this graph.
	 * @since 0.08
	 */
	public Collection<EdgeState> getGraphEdgeState()
	{
		return _edges.allEdgeState();
	}
	
	/**
	 * Returns {@link EdgeState} at given index, if it exists.
	 * <p>
	 * If this returns a non-null {@code edgeState}, the following will be true:
	 * <blockquote><code>
	 *     edgeState.{@linkplain EdgeState#edgeIndexInParent(FactorGraph) edgeIndexInParent(this)} == index
	 * </code></blockquote>
	 * 
	 * @param index an integer in the range [0, {@link #getGraphEdgeStateMaxIndex}]
	 * @since 0.08
	 * @throws ArrayIndexOutOfBoundsException if {@code index} is not in valid range
	 */
	public @Nullable EdgeState getGraphEdgeState(int index)
	{
		return _edges.get(index);
	}

	/**
	 * Returns the maximum index of slots currently reserved for {@link EdgeState} belonging to this graph.
	 * <p>
	 * There may not necessarily be an edge state object currently at this index.
	 * <p>
	 * @return Returns max edge index or -1 if there is currently no space allocated for edge state.
	 * @since 0.08
	 */
	public int getGraphEdgeStateMaxIndex()
	{
		return _edges.size() - 1;
	}

	//============================
	//
	// Operations on FactorGraph
	//
	//============================
	
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
		requireSolver("baumWelch").baumWelch(tables, numRestarts, numSteps);

	}

	public void estimateParameters(IFactorTable [] tables,int numRestarts,int numSteps, double stepScaleFactor)
	{
		requireSolver("estimateParameters").estimateParameters(tables, numRestarts, numSteps,  stepScaleFactor);

	}

	private void addOwnedFactor(Factor factor, boolean absorbedFromSubgraph)
	{
		factor.setParentGraph(this);
		_ownedFactors.add(factor);
		addName(factor);
		if ((_flags & FACTOR_ADD_EVENT) != 0)
		{
			raiseEvent(new FactorAddEvent(this, factor, absorbedFromSubgraph));
		}
	}

	private void addOwnedSubgraph(FactorGraph subgraph, boolean absorbedFromSubgraph)
	{
		subgraph.setParentGraph(this);
		_ownedSubGraphs.add(subgraph);
		addName(subgraph);
		
		if ((_flags & SUBGRAPH_ADD_EVENT) != 0)
		{
			raiseEvent(new SubgraphAddEvent(this, subgraph, absorbedFromSubgraph));
		}
	}
	
	private void addOwnedVariable(Variable variable, boolean absorbedFromSubgraph)
	{
		//Only insert if not already there.
		if (!_ownedVariables.containsNode(variable))
		{
			// Tell us about the variable
			_ownedVariables.add(variable);
			// And the variable about us...
			((Node)variable).setParentGraph(this);
			
			if ((_flags & VARIABLE_ADD_EVENT) != 0)
			{
				raiseEvent(new VariableAddEvent(this, variable, absorbedFromSubgraph));
			}
		}
		addName(variable);
	}

	public void recreateMessages()
	{
		for (Variable v : getVariablesFlat())
		{
			final ISolverVariable sv = v.getSolver();
			if (sv != null)
			{
				sv.createNonEdgeSpecificState();
			}
		}
	}

	/**
	 * Initializes components of model, and if a solver is set, also initializes the
	 * solver.
	 * <p>
	 * Does the following:
	 * <ol>
	 * <li>Invokes {@link #notifyListenerChanged()} to reconfigure event notifications.
	 * <li>Initializes non-boundary model variables contained directly in the graph (not in subgraphs)
	 * by calling {@link Variable#initialize()} on each.
	 * <li>If not {@link #hasParentGraph()}, initializes boundary variables in the same fashion.
	 * <li>Initializes all model factors contained directly in the graph by calling
	 * {@link Factor#initialize()} on each.
	 * <li>Initializes nested graphs by invoking this method recursively on each.
	 * <li>Finally, if {@link #getSolver()} is not null, invokes {@link ISolverFactorGraph#initialize()}
	 * on the solver graph to initialize solver state. The solver is responsible for initializing
	 * its component variables, factors and any other state.
	 * </ol>
	 */
	@Override
	public void initialize()
	{
		super.initialize();
		notifyListenerChanged();
		
		for (Variable v : _ownedVariables)
		{
			v.initialize();
		}
		for (Factor f : _ownedFactors)
		{
			f.initialize();
		}
		for (FactorGraph g : _ownedSubGraphs)
		{
			g.initialize();
		}

		final ISolverFactorGraph sfg = _solverFactorGraph;
		if (sfg != null)
		{
			sfg.initialize();
		}
	}
	
	private ISolverFactorGraph checkSolverIsSet()
	{
		ISolverFactorGraph sfg = _solverFactorGraph;
		if (sfg == null)
			throw new DimpleException("solver needs to be set first");
		return sfg;
	}

	public void solve()
	{
		checkSolverIsSet().solve();
	}

	public void solveOneStep()
	{
		checkSolverIsSet().solveOneStep();
	}


	public void continueSolve()
	{
		checkSolverIsSet().continueSolve();
	}

	/**
	 * Absorbs subgraph into parent graph.
	 * <p>
	 * Tranfers variables, factors and subgraphs owned by {@code subgraph} to this
	 * graph and removes subgraph.
	 * <p>
	 * @param subgraph must be a direct subgraph of this graph.
	 */
	public void absorbSubgraph(FactorGraph subgraph)
	{
		if (!ownsDirectly(subgraph))
		{
			throw new DimpleException("Cannot absorb subgraph that is not directly owned.");
		}
		
		final OwnedVariables variables = subgraph._ownedVariables;
		final OwnedFactors factors = subgraph._ownedFactors;
		final OwnedGraphs subgraphs = subgraph._ownedSubGraphs;
		
		// FIXME: may need to rename nodes when they are transferred to avoid conflicts...

		// Reparent owned variables & factors & subgraphs
		for (Variable variable : variables)
		{
			addOwnedVariable(variable, true);
		}
		for (Factor factor : factors)
		{
			addOwnedFactor(factor, true);
		}
		for (FactorGraph subsubgraph : subgraphs)
		{
			addOwnedSubgraph(subsubgraph, true);
		}
		
		// Clear subgraph state
		variables.clear();
		factors.clear();
		subgraphs.clear();
		subgraph._boundaryVariables.clear();

		// Remove subgraph itself
		removeNode(subgraph);
		
		if ((_flags & SUBGRAPH_REMOVE_EVENT) != 0)
		{
			raiseEvent(new SubgraphRemoveEvent(this, subgraph, true));
		}
	}
	
	/**
	 * Remove a subgraph and all of its variables and factors from this graph.
	 * Also remove boundary variables of subgraph if they are no longer connected
	 * to anything.
	 * 
	 * @param subgraph
	 */
	public void remove(FactorGraph subgraph)
	{
		VariableList varList = subgraph.getVariablesFlat();
		IMapList<FactorBase> factors = subgraph.getFactorsTop();

		List<Variable> boundary = subgraph._boundaryVariables;

		Variable [] arr = varList.toArray(new Variable[varList.size()]);

		for (FactorBase f : factors)
		{
			final Factor factor = f.asFactor();
			if (factor != null)
			{
				subgraph.remove(factor);
			}
			else
			{
				FactorGraph subsubgraph = f.asFactorGraph();
				if (subsubgraph != null)
				{
					subgraph.remove(subsubgraph);
				}
			}
		}

		removeVariables(arr);
		removeNode(subgraph);
		_ownedSubGraphs.removeNode(subgraph);
		subgraph.setParentGraph(null);
		_graphTreeState.removeGraph(subgraph);
		subgraph._graphTreeState = new GraphTreeState(subgraph);
		
		for (Variable v : boundary)
		{
			if (v.getSiblingCount() == 0)
				remove(v);
		}

		if ((_flags & SUBGRAPH_REMOVE_EVENT) != 0)
		{
			raiseEvent(new SubgraphRemoveEvent(this, subgraph, false));
		}
	}

	private void removeNode(Node n)
	{
		String explicitName = n.getExplicitName();
		if(explicitName != null)
		{
			_name2object.remove(explicitName);
		}
		structureChanged();
	}

	/**
	 * Removes factor from the graph leaving any variables it was connected to.
	 * 
	 * @param factor
	 * @throws DimpleException if factor is not owned by the graph.
	 */
	public void remove(Factor factor)
	{
		//_ownedFactors;
		if (!_ownedFactors.containsNode(factor))
		{
			throw new DimpleException("Cannot delete factor.  It is not a member of this graph");
		}

		for (int i = factor.getSiblingCount(); --i>=0;)
		{
			removeSiblingEdge(factor.getSiblingEdgeState(i));
		}

		_ownedFactors.removeNode(factor);
		removeNode(factor);
		factor.setParentGraph(null);
		
		ISolverFactor sfactor = factor.getSolver();
		if (sfactor != null)
		{
			sfactor.getParentGraph().removeSolverFactor(sfactor);
		}
		
		if ((_flags & FACTOR_REMOVE_EVENT) != 0)
		{
			raiseEvent(new FactorRemoveEvent(this, factor));
		}
	}

	//===================
	//
	// Graph algorithms
	//
	//===================

	/**
	 * True if graph is comprised of a set of one or more disjoint trees. That is,
	 * given any two nodes in the graph there is at most one unique path between them.
	 * <p>
	 * @see #isTree()
	 * @see #isForest(int)
	 * @since 0.05
	 */
	public boolean isForest()
	{
		return isForest(Integer.MAX_VALUE);
	}
	
	/**
	 * True if the graph is consists of a set of one or more disjoint trees when considering nodes
	 * down to the specified {@code relativeNestingDepth} of subgraphs. That is,
	 * given any two nodes in the graph that are no deeper than the specified depth
	 * below the root graph, there no more than one unique path between them.
	 * <p>
	 * @see #isForest()
	 * @see #isTree(int)
	 * @since 0.05
	 * */
	public boolean isForest(int relativeNestingDepth)
	{
		return isTreeOrForest(relativeNestingDepth, true);
	}
	
	/**
	 * True if the graph is singly connected and not disjoint. That is,
	 * given any two nodes in the graph there is exactly one unique path between them.
	 * <p>
	 * @see #isForest()
	 * @see #isTree(int)
	 */
	public boolean isTree()
	{
		return isTreeFlat();
	}

	/**
	 * Same as {@link #isTree()}}
	 */
	public boolean isTreeFlat()
	{
		return isTree(Integer.MAX_VALUE);
	}

	/**
	 * True if the top-level of the tree -- ignoring the contents of any subgraphs -- is a tree.
	 * <p>
	 * Same as {@link #isTree(int)} with zero argument.
	 */
	public boolean isTreeTop()
	{
		return isTree(0);
	}

	/**
	 * True if the graph is singly connected and not disjoint when considering nodes
	 * down to the specified {@code relativeNestingDepth} of subgraphs. That is,
	 * given any two nodes in the graph that are no deeper than the specified depth
	 * below the root graph, there is exactly one unique path between them.
	 * <p>
	 * @see #isForest(int)
	 * @see #isTree()
	 */
	public boolean isTree(int relativeNestingDepth)
	{
		return isTreeOrForest(relativeNestingDepth, false);
	}
	
	private boolean isTreeOrForest(int relativeNestingDepth, boolean checkForForest)
	{
		FactorGraph g = this;
		// Get all the nodes in the graph and all sub-graphs--both variables and
		// functions (not including boundary variables unless this graph has no
		// parents, since those will be updated only in that case)
		IMapList<FactorBase> allIncludedFunctions = g.getFactors(relativeNestingDepth);
		VariableList allIncludedVariables = g.getVariables(relativeNestingDepth);

		// Determine the total number of edges in the graph, including
		// all sub-graphs. Since this is a bipartite graph, we can just count
		// all ports associated with the variables in the graph
		int numEdges = 0;
		for (Variable v : allIncludedVariables)
			numEdges += v.getSiblingCount();


		// Determine the total number of vertices (variable and function nodes)
		// in the graph, including all sub-graphs
		int numVertices = allIncludedVariables.size() + allIncludedFunctions.size();

		//If there are no variables or functions, this is definitely a tree
		if (numVertices == 0)
			return true;

		// If the number of edges is greater than the number of vertices minus 1, there must be cycles
		if (numEdges > numVertices - 1)
			return false;

		// If the number of edges is less than the number of vertices minus 1, it must not be connected
		if (!checkForForest && numEdges < numVertices - 1)
			return false;
		
		// If it has the right number of edges, the either it's a tree or it
		// isn't a connected graph, and could either be a 'forest' or have cycles.
		
		Set<INode> allIncludedNodes = new LinkedHashSet<INode>(numVertices);
		allIncludedNodes.addAll(allIncludedFunctions);
		allIncludedNodes.addAll(allIncludedVariables);

		FactorGraphWalker walker = null;
		while (!allIncludedNodes.isEmpty())
		{
			// First, pick a node arbitrarily;
			INode n = allIncludedNodes.iterator().next();

			if (walker == null)
			{
				walker = new FactorGraphWalker(this, n).maxRelativeNestingDepth(relativeNestingDepth);
			}
			else
			{
				walker.init(this, n);
			}
			
			while (walker.hasNext())
			{
				INode node = walker.next();
				if (walker.getCycleCount() > 0)
				{
					return false;
				}
				if (!allIncludedNodes.remove(node))
				{
					throw new Error("FactorGraph.isTreeOrForest: found node not in list");
				}
					
			}
			
			if (!checkForForest)
			{
				break;
			}
		}
		
		// No cycles were found, but we might not have visited all of the nodes in the
		// graph. If we have, its a tree/forest.
		return allIncludedNodes.isEmpty();
	}

	public int [][] getAdjacencyMatrix()
	{
		return getAdjacencyMatrixFlat();
	}

	//==============
	//
	// Scheduling
	//
	//==============
	
	public int [][] getAdjacencyMatrix(int relativeNestingDepth)
	{
		IMapList<INode> nodes = getNodes(relativeNestingDepth);
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

	public int [][] getAdjacencyMatrix(Collection<INode> nodes)
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
			INode node = nodes[i];
			node2index.put(node,i);
			retval[i] = new int[nodes.length];
			if (node.getRootGraph() != this.getRootGraph())
				throw new DimpleException("expected nodes that are part of this graph");
		}

		for (int i = 0; i < nodes.length; i++)
		{
			INode node = nodes[i];
			for (int k = 0, endk = node.getSiblingCount(); k < endk; k++)
			{
				ArrayList<INode> connectedNodes = node.getConnectedNodeAndParents(k);

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
	protected IMapList<INode> depthFirstSearchRecursive(
		INode node,
		@Nullable INode previousNode,
		IMapList<INode> foundNodes,
		IMapList<INode> nodeList,
		int currentDepth,
		int maxDepth,
		int relativeNestingDepth)
	{

		foundNodes.add(node);						// This node has been found

		if (currentDepth < maxDepth)
		{
			//Collection<Port> ports = node.getPorts();	// Get all the edges from this node

			for (int i = 0, end = node.getSiblingCount(); i < end; i++)
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


	public IMapList<INode> depthFirstSearch(INode root, int searchDepth,int relativeNestingDepth)
	{
		IMapList<INode> tmp = new MapList<INode>();

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

	public boolean isAncestorOf(@Nullable INode node)
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


	//=================
	//
	// Introspection
	//
	//=================

	@Override
	public @Nullable ISolverFactorGraph getSolver()
	{
		return _solverFactorGraph;
	}

	/**
	 * Returns the number of boundary variables for this graph, if any.
	 * @see #getBoundaryVariable(int)
	 * @since 0.05
	 */
	public int getBoundaryVariableCount()
	{
		return _boundaryVariables.size();
	}
	
	/**
	 * Returns the ith boundary variable for this graph.
	 * 
	 * @param i an index in the range [0, {@link #getBoundaryVariableCount()} - 1].
	 * @since 0.05
	 */
	public Variable getBoundaryVariable(int i)
	{
		return _boundaryVariables.get(i);
	}

	/**
	 * Returns child from graphs and subgraphs with given global id.
	 * @param globalId is the child's {@linkplain IFactorGraphChild#getGlobalId() global id}.
	 * @return child or null if no such child belongs to this graph or any of its direct or indirect
	 * subgraphs.
	 * @since 0.08
	 */
	public @Nullable IFactorGraphChild getChildByGlobalId(long globalId)
	{
		final FactorGraph fg = getSubgraphForGlobalId(globalId);
		return fg != null ? fg.getChildByLocalId(Ids.localIdFromGlobalId(globalId)) : null;
	}
	
	/**
	 * Returns child from same graph tree with given graph tree id.
	 * @param graphTreeId is the child's {@linkplain IFactorGraphChild#getGraphTreeId() graph tree id}.
	 * @return child or null if no such child belongs to this graph's graph tree.
	 * @since 0.08
	 */
	public @Nullable IFactorGraphChild getChildByGraphTreeId(long graphTreeId)
	{
		final FactorGraph fg = getGraphByTreeIndex(Ids.graphTreeIndexFromGraphTreeId(graphTreeId));
		return fg != null ? fg.getChildByLocalId(Ids.localIdFromGlobalId(graphTreeId)) : null;
	}
	
	/**
	 * Get child of graph identified by {@code localId}.
	 * <p>
	 * @param localId
	 * @return child or null
	 * @since 0.08
	 */
	public @Nullable IFactorGraphChild getChildByLocalId(int localId)
	{
		switch (Ids.typeIndexFromLocalId(localId))
		{
		case Ids.FACTOR_TYPE:
			return _ownedFactors.getByLocalId(localId);
		case Ids.GRAPH_TYPE:
			return _ownedSubGraphs.getByLocalId(localId);
		case Ids.VARIABLE_TYPE:
			return _ownedVariables.getByLocalId(localId);
		case Ids.BOUNDARY_VARIABLE_TYPE:
			return _boundaryVariables.get(Ids.indexFromLocalId(localId));
		case Ids.EDGE_TYPE:
			return new Edge(this, _edges.get(Ids.indexFromLocalId(localId)));
		case Ids.FACTOR_PORT_TYPE:
			return new FactorPort(_edges.get(Ids.indexFromLocalId(localId)), this);
		case Ids.VARIABLE_PORT_TYPE:
			return new VariablePort(_edges.get(Ids.indexFromLocalId(localId)), this);
		case Ids.VARIABLE_BLOCK_TYPE:
			return _ownedVariableBlocks.getByLocalId(localId);
		case Ids.CONSTANT_TYPE:
			return _ownedConstants.getByLocalId(localId);
		default:
			return null;
		}
	}
	
	public @Nullable Constant getConstantByLocalId(int localId)
	{
		return Ids.typeIndexFromLocalId(localId) == Ids.CONSTANT_TYPE ? _ownedConstants.getByLocalId(localId) : null;
	}
	
	public Collection<Constant> getOwnedConstants()
	{
		return Collections.unmodifiableCollection(_ownedConstants);
	}
	
	/**
	 * Returns the number of variables contained directly in this graph
	 * (i.e. not in subgraphs).
	 * @since 0.05
	 */
	public int getOwnedVariableCount()
	{
		return _ownedVariables.size();
	}
	
	/**
	 * Unmodifiable collection that enumerates factors whose parent is this graph..
	 * @since 0.08
	 */
	public Collection<Factor> getOwnedFactors()
	{
		return Collections.unmodifiableCollection(_ownedFactors);
	}
	
	/**
	 * Unmodifiable collection that enumerates subgraphs whose parent is this graph..
	 * @since 0.08
	 */
	public Collection<FactorGraph> getOwnedGraphs()
	{
		return Collections.unmodifiableCollection(_ownedSubGraphs);
	}
	
	/**
	 * Unmodifiable collection that enumerates variables whose parent is this graph..
	 * @since 0.08
	 */
	public Collection<Variable> getOwnedVariables()
	{
		return Collections.unmodifiableCollection(_ownedVariables);
	}
	
	/**
	 * Unmodifiable collection that enumerates variable blocks owned by this graph.
	 * @since 0.08
	 */
	public Collection<VariableBlock> getOwnedVariableBlocks()
	{
		return Collections.unmodifiableCollection(_ownedVariableBlocks);
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
		if (relativeNestingDepth == 0)
		{
			return _ownedVariables.size();
		}
		else
		{
			return FactorGraphIterables.variablesDownto(this, relativeNestingDepth).size();
		}
	}

	/**
	 * Returns list of all variables in the graph including those in nested graphs.
	 */
	public VariableList getVariables()
	{
		return new VariableList(FactorGraphIterables.variables(this));
	}

	/**
	 * Returns list of all variables in the graph including those in nested graphs down to
	 * specified {@code relativeNestingDepth} below this graph, where a nesting depth of zero
	 * indicates that nested graphs should not be included.
	 */
	public VariableList getVariables(int relativeNestingDepth)
	{
		return new VariableList(FactorGraphIterables.variablesDownto(this, relativeNestingDepth));
	}

	public VariableList getVariables(int relativeNestingDepth,boolean forceIncludeBoundaryVariables)
	{
		if (forceIncludeBoundaryVariables)
		{
			return new VariableList(FactorGraphIterables.variablesAndBoundaryDownto(this, relativeNestingDepth));
		}
		else
		{
			return new VariableList(FactorGraphIterables.variablesDownto(this, relativeNestingDepth));
		}
	}

	public VariableList getVariablesFlat()
	{
		return getVariables();
	}

	public VariableList getVariablesFlat(boolean forceIncludeBoundaryVariables)
	{
		return getVariables(Integer.MAX_VALUE, forceIncludeBoundaryVariables);
	}

	public VariableList getVariablesTop()
	{
		return new VariableList(_ownedVariables);
	}

	public VariableList getVariablesTop(boolean forceIncludeBoundaryVariables)
	{
		return getVariables(0, forceIncludeBoundaryVariables);
	}


	public VariableList getBoundaryVariables()
	{
		return new VariableList(_boundaryVariables);
	}

	public boolean isBoundaryVariable(Variable mv)
	{
		return _boundaryVariables.contains(mv);
	}

	/**
	 * Looks up variable using its global or graph tree id.
	 * <p>
	 * @param id is either a global id or a graph tree id for the variable.
	 * @return {@code Variable} in same graph tree as this graph that is indexed by given identifier or else
	 * null if there is no object with given identifier in the graph tree.
	 * @throws ClassCastException if {@code id} refers to an object other than a {@code Variable} (such as a
	 * {@code Factor}).
	 */
	public @Nullable Variable getVariable(long id)
	{
		return Ids.isGlobalId(id) ? (Variable)getNodeByGlobalId(id) : (Variable)getNodeByGraphTreeId(id);
	}
	
	public @Nullable Variable getVariableByLocalId(int id)
	{
		switch (id >>> Ids.LOCAL_ID_TYPE_OFFSET)
		{
		case Ids.VARIABLE_TYPE:
			return _ownedVariables.getByLocalId(id);
		case Ids.BOUNDARY_VARIABLE_TYPE:
			return _boundaryVariables.get(Ids.indexFromLocalId(id));
		default:
			return null;
		}
	}
	
	/**
	 * Returns {@link VariableBlock} instance with given local id, if it exists.
	 * @return variable block with given {@code localId} in this graph or else null if id is invalid or
	 * there is no such block.
	 * @see #addVariableBlock(Collection)
	 */
	public @Nullable VariableBlock getVariableBlockByLocalId(int localId)
	{
		return Ids.typeIndexFromLocalId(localId) == Ids.VARIABLE_BLOCK_TYPE ? _ownedVariableBlocks.getByLocalId(localId) : null;
	}

	public FactorList getNonGraphFactors()
	{
		return new FactorList(FactorGraphIterables.factors(this));
	}

	public FactorList getNonGraphFactors(int relativeNestingDepth)
	{
		return new FactorList(FactorGraphIterables.factorsDownto(this, relativeNestingDepth));
	}
	
	public FactorList getNonGraphFactorsFlat()
	{
		return getNonGraphFactors();
	}

	public FactorList getNonGraphFactorsTop()
	{
		return new FactorList(_ownedFactors);
	}

	/**
	 * Returns count of factors that would be returned by {@link #getFactors()}.
	 */
	public int getFactorCount()
	{
		return getFactorCount(Integer.MAX_VALUE);
	}

	/**
	 * Returns count of factors that would be returned by {@link #getFactors(int)}.
	 */
	public int getFactorCount(int relativeNestingDepth)
	{
		if (_ownedSubGraphs.isEmpty() || relativeNestingDepth == 0)
		{
			return _ownedFactors.size();
		}
		
		return FactorGraphIterables.factorsDownto(this, relativeNestingDepth).size();
	}

	/**
	 * Returns a newly constructed collection containing all factors within
	 * the specified nesting depth and subgraphs at the specified depth.
	 *<p>
	 * @see #getFactors(int, IMapList)
	 */
	public IMapList<FactorBase> getFactors(int relativeNestingDepth)
	{
		return new MapList<>(FactorGraphIterables.factorsAndLeafSubgraphsDownto(this, relativeNestingDepth));
	}

	/**
	 * Add factors from this graph down to a specified subgraph nesting level,
	 * <p>
	 * @param relativeNestingDepth is a non-negative number indicating how many levels
	 * of subgraphs will be explored. Factors at the specified relative depth below the
	 * starting graph or less will be included. Subgraphs at the exact relative depth
	 * will be included, but <em>not</em> those at shallower depth.
	 * <p>
	 * @param factors is the collection to which factors will be added.
	 * @return {@code factors} argument.
	 */
	public IMapList<FactorBase> getFactors(int relativeNestingDepth, IMapList<FactorBase> factors)
	{
		factors.addAll(FactorGraphIterables.factorsAndLeafSubgraphsDownto(this, relativeNestingDepth));
		return factors;
	}

	public FactorList getFactors()
	{
		return new FactorList(FactorGraphIterables.factors(this));
	}

	/**
	 * Returns newly constructed collection containing all of the factors
	 * and subgraphs that are directly owned by this graph.
	 */
	public IMapList<FactorBase> getFactorsTop()
	{
		return new MapList<>(FactorGraphIterables.factorsAndLeafSubgraphsDownto(this, 0));
	}

	public @Nullable Factor getFactor(long id)
	{
		return (Factor)getNodeByGlobalId(id);
	}
	
	public @Nullable Factor getFactorByLocalId(int id)
	{
		return Ids.typeIndexFromLocalId(id) == Ids.FACTOR_TYPE ? _ownedFactors.getByLocalId(id) : null;
	}
	
	public @Nullable FactorGraph getGraphByLocalId(int id)
	{
		return Ids.typeIndexFromLocalId(id) == Ids.GRAPH_TYPE ? _ownedSubGraphs.getByLocalId(id) : null;
	}

	@Nullable INode getFirstNode()
	{
		if (!_ownedSubGraphs.isEmpty())
		{
			return _ownedSubGraphs.getNth(0);
		}
		else if (this._ownedFactors.size() > 0)
		{
			return _ownedFactors.getNth(0);
		}
		else if (this._ownedVariables.size() > 0)
		{
			return _ownedVariables.getNth(0);
		}

		return null;
	}

	public IMapList<INode> getNodes()
	{
		return getNodesFlat();
	}

	public IMapList<INode> getNodes(int relativeNestingDepth)
	{
		IMapList<FactorBase> factors = getFactors(relativeNestingDepth);
		VariableList vars = getVariables(relativeNestingDepth);

		IMapList<INode> retval = new MapList<INode>();

		for (Variable v : vars)
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

	/**
	 * Counter that is incremented whenever structure of any graph below shared root changes.
	 * <p>
	 * May be used to verify cached information that depends on the graph structure.
	 * @since 0.08
	 * @see #structureVersion
	 */
	public long graphTreeStructureVersion()
	{
		return _graphTreeState._globalStructureVersion;
	}
	
	/**
	 * Counter that is incremented whenever structure of graph changes.
	 * <p>
	 * May be used to verify cached information that depends on the graph structure.
	 * @since 0.08
	 * @see #graphTreeStructureVersion()
	 */
	public long structureVersion()
	{
		return _structureVersion;
	}
	
	final void structureChanged()
	{
		++_structureVersion;
		++_graphTreeState._globalStructureVersion;
	}

	


	//=========
	//
	// Names
	//
	//=========
	
	private void addName(Node nameable)
	{
		String explicitName = nameable.getExplicitName();

		//Check + insert name if there is one
		if (explicitName != null)
		{
			Object obj = _name2object.get(explicitName);
			if (obj != null && obj != nameable)
			{
				throw new DimpleException("ERROR variable name " + explicitName + " already in graph");
			}

			_name2object.put(explicitName, nameable);
		}
		
		structureChanged();
	}

	void setChildNameInternal(Node child, @Nullable String newName)
	{
		assert(child == getNodeByLocalId(child.getLocalId()));

		//If new name already here, bad
		if (newName != null && getObjectByName(newName) != null)
		{
			throw new DimpleException("ERROR name '%s' already present in parent", newName);
		}

		//remove old name, if there was one
		String oldExplicitName = child.getExplicitName();
		if (oldExplicitName != null)
		{
			_name2object.remove(oldExplicitName);
		}

		//add new name, if there is one
		if(newName != null)
		{
			_name2object.put(newName, child);
		}
	}

	public @Nullable Node getObjectByName(@Nullable String name)
	{
		IFactorGraphChild child = getChildByName(name);
		if (child instanceof Node)
		{
			return (Node)child;
		}
		
		return null;
	}
	
	/**
	 * Looks up child object by string representation of its name or identifier.
	 * @param nameOrNull may be in one of the folling formats:
	 * <ul>
	 * <li>string representation of child's {@linkplain IFactorGraphChild#getUUID UUID}
	 * <li>dot-qualified name where components are either local names or {@linkplain Ids#defaultNameForLocalId(int)
	 * string version of local identifier}.
	 * </ul>
	 * @return child or null if not found.
	 * @since 0.08
	 */
	public @Nullable IFactorGraphChild getChildByName(final @Nullable String nameOrNull)
	{
		IFactorGraphChild obj = null;
		
		if (nameOrNull != null && !nameOrNull.isEmpty())
		{
			String name = nameOrNull;
			
			if (Ids.isUUIDString(name))
			{
				obj = getChildByUUID(UUID.fromString(name));
			}
			else
			{
				String remainder = "";
		
				int dotOffset = name.indexOf('.');
				if (dotOffset >= 0)
				{
					remainder = name.substring(dotOffset + 1);
					name = name.substring(0, dotOffset);
					
					// Check to see if name refers to this graph.
					if (name.equals(_name) || _graphId == Ids.graphIdFromDefaultName(name))
					{
						name = remainder;
						remainder = "";

						dotOffset = name.indexOf('.');
						if (dotOffset >= 0)
						{
							remainder = name.substring(dotOffset + 1);
							name = name.substring(0, dotOffset);
						}
					}
				}
		
				obj = _name2object.get(name);
				if (obj == null)
				{
					obj = getChildByLocalId(Ids.localIdFromDefaultName(name));
				}
		
				if (!remainder.isEmpty() && obj instanceof FactorGraph)
				{
					FactorGraph subgraph = (FactorGraph)obj;
					obj = subgraph.getChildByName(remainder);
				}
			}
		}
		
		return obj;
	}

	public @Nullable Node getObjectByUUID(UUID uuid)
	{
		return getNodeByGlobalId(Ids.globalIdFromUUID(uuid));
	}
	
	/**
	 * Looks up child in this graph by its UUID.
	 * @return null if no such child is a member of this graph or its direct or indirect subgraphs.
	 * @since 0.08
	 */
	public @Nullable IFactorGraphChild getChildByUUID(UUID uuid)
	{
		return getChildByGlobalId(Ids.globalIdFromUUID(uuid));
	}
	
	/**
	 * Looks up child in graph tree using given key.
	 * <p>
	 * Supports flexible lookup of children by a variety of different key types.
	 * <p>
	 * @param key must be one of the following:
	 * <p>
	 * <dl>
	 * <dt>{@code null} or {@link Node}</dt>
	 * <dd> Value will simply be returned (even if node does not belong to this graph)</dd>
	 * <dl>{@link Long}</dl>
	 * <dd>If number appears to be a global id according to {@link Ids#isGlobalId} then
	 * {@link #getNodeByGlobalId} will be used to look up the node, otherwise it will be treated as a graph
	 * tree id and passed to {@link #getNodeByGraphTreeId}.
	 * </dd>
	 * <dt>{@link Integer}
	 * <dd>Will be treated as a local identifier and passed to {@link #getNodeByLocalId}.
	 * <dt>{@link String}
	 * <dd>Will be treated as a qualified name and passed to {@link #getObjectByName}.
	 * <dt>{@link UUID}
	 * <dd>Will be passed to {@link #getObjectByUUID}.
	 * </dl>
	 * @throws IllegalArgumentException if {@code key} is not one of the above types.
	 * @since 0.08
	 */
	public @Nullable IFactorGraphChild getChild(@Nullable Object key)
	{
		if (key == null || key instanceof IFactorGraphChild)
		{
			return (IFactorGraphChild)key;
		}

		if (key instanceof Long)
		{
			final long id = (Long)key;
			if (Ids.isGlobalId(id))
			{
				return getChildByGlobalId(id);
			}
			else
			{
				return getChildByGraphTreeId(id);
			}
		}
		if (key instanceof String)
		{
			return getChildByName((String)key);
		}
		if (key instanceof UUID)
		{
			return getChildByUUID((UUID)key);
		}
		if (key instanceof Integer)
		{
			return getChildByLocalId((Integer)key);
		}
		
		throw new IllegalArgumentException(String.format(
			"Key %s is not one of IFactorGraphChild, String, UUID, Long, or Integer", key));
	}
	
	/**
	 * Returns node in this graph or subgraph with given global id.
	 * @return node with given global id or null if not found or not a
	 * member of this graph or its subgraphs
	 * @since 0.08
	 */
	public @Nullable Node getNodeByGlobalId(long gid)
	{
		final FactorGraph fg = getSubgraphForGlobalId(gid);
		return fg != null ? fg.getNodeByLocalId(Ids.localIdFromGlobalId(gid)) : null ;
	}
	
	/**
	 * Returns node in same graph tree with given {@linkplain INode#getGraphTreeId() graph tree id}, if it exists.
	 * @since 0.08
	 */
	public @Nullable Node getNodeByGraphTreeId(long id)
	{
		FactorGraph graph = getGraphByTreeIndex(Ids.graphTreeIndexFromGraphTreeId(id));
		return graph != null ? graph.getNodeByLocalId(Ids.localIdFromGraphTreeId(id)) : null;
	}
	
	/**
	 * Returns node directly owned by this graph with given local id.
	 * @return node with given id or null if not found.
	 * @since 0.08
	 * @see #getNodeByGlobalId
	 * @see #getFactorByLocalId(int)
	 * @see #getVariableByLocalId(int)
	 * @see #getGraphByLocalId(int)
	 */
	@Internal
	public @Nullable Node getNodeByLocalId(int id)
	{
		switch (id >>> Ids.LOCAL_ID_TYPE_OFFSET)
		{
		case Ids.FACTOR_TYPE:
			return _ownedFactors.getByLocalId(id);
		case Ids.GRAPH_TYPE:
			return _ownedSubGraphs.getByLocalId(id);
		case Ids.VARIABLE_TYPE:
			return _ownedVariables.getByLocalId(id);
		case Ids.BOUNDARY_VARIABLE_TYPE:
			return _boundaryVariables.get(Ids.indexFromLocalId(id));
		default:
			return null;
		}
	}
	
	/**
	 * The index of this graph within the tree of graphs sharing a common {@linkplain #getRootGraph() root graph}.
	 * <p>
	 * @since 0.08
	 * @see #getGraphByTreeIndex(int)
	 */
	public final int getGraphTreeIndex()
	{
		return _graphTreeIndex;
	}
	
	/**
	 * Returns graph in tree with given graph tree index.
	 * @param index is the {@link #getGraphTreeIndex() graph tree index} of the graph within the tree of graphs
	 * sharing the same {@link #getRootGraph() root graph}.
	 * @return graph with given index or else null.
	 * @since 0.08
	 * @see #getMaxGraphTreeIndex()
	 */
	public @Nullable FactorGraph getGraphByTreeIndex(int index)
	{
		return _graphTreeState._graphs.getOrNull(index);
	}
	
	/**
	 * The number of graphs in the tree of graphs below (and including) the {@linkplain #getRootGraph() root graph}.
	 * <p>
	 * @return a positive count of graphs. Equal to one if there are no subgraphs.
	 * @since 0.08
	 */
	public int getGraphHierarchySize()
	{
		return _graphTreeState._nGraphs;
	}
	
	/**
	 * The maximum graph tree index among all graphs in the same graph tree.
	 * <p>
	 * @since 0.08
	 * @see #getGraphByTreeIndex(int)
	 */
	public int getMaxGraphTreeIndex()
	{
		return _graphTreeState._maxGraphTreeIndex;
	}
	
	public @Nullable Variable 	getVariableByName(String name)
	{
		Variable v = null;
		Object o = getObjectByName(name);
		if(o instanceof Variable)
		{
			v = (Variable) o;
		}

		return v;
	}
	public @Nullable Factor 	 	getFactorByName(String name)
	{
		Factor f = null;
		Object o = getObjectByName(name);
		if(o instanceof Factor)
		{
			f = (Factor) o;
		}
		return f;
	}
	public @Nullable FactorGraph getGraphByName(String name)
	{
		FactorGraph fg = null;
		Object o = getObjectByName(name);
		if(o instanceof FactorGraph)
		{
			fg = (FactorGraph) o;
		}
		return fg;
	}
	//==============
	//
	// Scheduling
	//
	//==============
	
	public @Nullable Variable getVariableByUUID(UUID uuid)
	{
		Variable v = null;
		Object o = getObjectByUUID(uuid);
		if (o instanceof Variable)
		{
			v = (Variable) o;
		}
		return v;
	}
	public @Nullable Factor  	getFactorByUUID(UUID uuid)
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
	public @Nullable FactorGraph getGraphByUUID(UUID uuid)
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
		return String.format("[FactorGraph %s]", getQualifiedName());
	}

	public boolean isSolverRunning()
	{
		final ISolverFactorGraph sfg = _solverFactorGraph;
		return sfg != null && sfg.isSolverRunning();
	}

	@Override
	public FactorGraph getRootGraph()
	{
		return _graphTreeState._graphs.get(0);
	}

	public @Nullable IFactorGraphFactory<?> getFactorGraphFactory()
	{
		return _solverFactory;
	}

	public double getBetheFreeEnergy()
	{
		return requireSolver("getBetheFreeEnergy").getBetheFreeEnergy();
	}

	

	//==================
	// FactorGraphDiffs
	//==================

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
	
	/*------------------
	 * Internal methods
	 */
	
	@Override
	protected void addEdge(Factor factor, Variable variable)
	{
		addEdge(_edges.allocateIndex(), factor, variable);
	}
	
	private void addEdge(int edgeIndex, Factor factor, Variable variable)
	{
		assert(factor.getParentGraph() == this);

		final FactorGraph variableGraph = requireNonNull(variable.getParentGraph());
		
		EdgeState edge;
		
		if (this == variableGraph)
		{
			edge = createLocalEdge(edgeIndex, factor, variable);
		}
		else
		{
			edge = createBoundaryEdge(edgeIndex, variableGraph._edges.size(), factor, variable);
			variableGraph._edges.add(edge);
		}
		
		_edges.add(edge);
		((Node)factor).addSiblingEdgeState(edge);
		variable.addSiblingEdgeState(edge);
		
		structureChanged();
		if (!edge.isLocal())
		{
			variableGraph.structureChanged();
		}
	}
	
	private EdgeState createLocalEdge(int edgeIndex, Factor factor, Variable variable)
	{
		final int factorOffset = Ids.indexFromLocalId(factor.getLocalId());
		final int variableOffset = Ids.indexFromLocalId(variable.getLocalId());
		return LocalEdgeState.create(edgeIndex, factorOffset, variableOffset);
	}
	
	private EdgeState createBoundaryEdge(int factorEdgeIndex, int variableEdgeIndex, Factor factor,
		Variable variable)
	{
		final int boundaryOffset = _boundaryVariables.indexOf(variable);
		assert(boundaryOffset >= 0);

		return BoundaryEdge.create(factor, factorEdgeIndex, boundaryOffset, variableEdgeIndex);
	}

	@Override
	@Internal
	protected void removeSiblingEdge(EdgeState edge)
	{
		final Factor factor = edge.getFactor(this);
		final Variable variable = edge.getVariable(this);
		final FactorGraph variableGraph = requireNonNull(variable.getParentGraph());
		
		((Node)factor).removeSiblingEdgeState(edge);
		variable.removeSiblingEdgeState(edge);

		_edges.set(edge.factorEdgeIndex(), null);
		if (!edge.isLocal())
		{
			variableGraph._edges.set(edge.variableEdgeIndex(), null);
		}
		
		structureChanged();
		if (!edge.isLocal())
		{
			variableGraph.structureChanged();
		}
	}
	
	/**
	 * @category internal
	 */
	@Internal
	public void replaceEdge(Factor factor, int edgeIndex, Variable newVariable)
	{
		final FactorGraph factorGraph = requireNonNull(factor.getParentGraph());
		
		final EdgeState oldEdge = factor.getSiblingEdgeState(edgeIndex);
		final Variable oldVariable = oldEdge.getVariable(factorGraph);
		final FactorGraph oldVariableGraph = requireNonNull(oldVariable.getParentGraph());
		final FactorGraph newVariableGraph = requireNonNull(newVariable.getParentGraph());

		final int factorEdgeIndex = oldEdge.factorEdgeIndex();
		final int oldVariableEdgeIndex = oldEdge.variableEdgeIndex();
		
		final EdgeState newEdge;
		
		oldVariable.removeSiblingEdgeState(oldEdge);

		if (factorGraph == newVariableGraph)
		{
			newEdge = createLocalEdge(factorEdgeIndex, factor, newVariable);
		}
		else if (oldVariableGraph == newVariableGraph)
		{
			newEdge = createBoundaryEdge(factorEdgeIndex, oldVariableEdgeIndex, factor, newVariable);
			newVariableGraph._edges.set(oldVariableEdgeIndex, newEdge);
		}
		else
		{
			newEdge = createBoundaryEdge(factorEdgeIndex, newVariableGraph._edges.size(), factor, newVariable);
			oldVariableGraph._edges.set(oldVariableEdgeIndex, null);
			newVariableGraph._edges.add(newEdge);
		}
		
		newVariable.addSiblingEdgeState(newEdge);
		((Node)factor).replaceSiblingEdgeState(oldEdge, newEdge);

		factorGraph._edges.set(factorEdgeIndex, newEdge);
		
		factorGraph.structureChanged();
		if (!oldEdge.isLocal())
		{
			oldVariableGraph.structureChanged();
		}
		if (!newEdge.isLocal())
		{
			newVariableGraph.structureChanged();
		}
	}
	
	/**
	 * @category internal
	 */
	@Internal
	public void setSolver(ISolverFactorGraph solver)
	{
		_solverFactorGraph = solver;
	}
	
	/**
	 * Iterates over all boundary variables in this graph.
	 * @see #externalBoundaryVariableIterator()
	 */
	final Iterator<Variable> boundaryVariableIterator()
	{
		return _boundaryVariables.iterator();
	}
	
	/**
	 * Iterates over boundary variables not owned by this graph.
	 * @see #boundaryVariableIterator()
	 */
	final Iterator<Variable> externalBoundaryVariableIterator()
	{
		final FactorGraph fg = this;
		return Iterators.filter(boundaryVariableIterator(),new Predicate<Variable>() {
			@NonNullByDefault(false)
			@Override
			public boolean apply(Variable var)
			{
				return var.getParentGraph() != fg;
			}});
	}
	
	final Iterator<Factor> ownedFactorIterator()
	{
		return _ownedFactors.iterator();
	}
	
	final int ownedFactorCount()
	{
		return _ownedFactors.size();
	}
	
	final Iterator<FactorGraph> ownedGraphIterator()
	{
		return _ownedSubGraphs.iterator();
	}
	
	final int ownedGraphCount()
	{
		return _ownedSubGraphs.size();
	}
	
	final Iterator<Variable> ownedVariableIterator()
	{
		return _ownedVariables.iterator();
	}
	
	final int ownedVariableCount()
	{
		return _ownedVariables.size();
	}
	
	/**
	 * Returns solver graph or throws an error if null.
	 * <p>
	 * For internal use only.
	 */
	@Override
	@Internal
	public ISolverFactorGraph requireSolver(String method)
	{
		ISolverFactorGraph solver = getSolver();
		if (solver == null)
		{
			throw new NullPointerException(String.format("Solver not set. Required by '%s'", method));
		}
		return solver;
	}
	
	/**********************
	 * Deprecated methods
	 */
	
	@Deprecated
	public void clearNames()
	{
		Helpers.clearNames(this);
	}
	
	/**
	 * @deprecated use {@link #addVariableBlock(Collection)} instead.
	 */
	@Deprecated
	public int defineVariableGroup(ArrayList<Variable> variableList)
	{
		VariableBlock block = addVariableBlock(variableList);
		return block.getLocalId();
	}
	@Matlab
	@Deprecated
	public String getAdjacencyString()
	{
		return Helpers.getAdjacencyString(this);
	}
	
	@Deprecated
	public String getDegreeString()
	{
		return Helpers.getDegreeString(this);
	}
	
	@Deprecated
	public String getDomainSizeString()
	{
		return Helpers.getDomainSizeString(this);
	}
	
	@Deprecated
	public String getDomainString()
	{
		return Helpers.getDomainString(this);
	}
	
	@Deprecated
	public HashMap<Integer, ArrayList<INode>> getFactorsByDegree()
	{
		return Helpers.getFactorsByDegree(this);
	}
	
	@Deprecated
	public FactorList getFactorsFlat()
	{
		return getFactors();
	}
	
	@Matlab
	@Deprecated
	public String getFullString()
	{
		return Helpers.getFullString(this);
	}
	
	/**
	 * @deprecated Use {@link #getOwnedGraphs()} instead.
	 */
	@Deprecated
	public ArrayList<FactorGraph> getNestedGraphs()
	{
		return new ArrayList<>(_ownedSubGraphs);
	}
	
	@Deprecated
	static public HashMap<Integer, ArrayList<INode>> getNodesByDegree(ArrayList<INode> nodes)
	{
		return Helpers.getNodesByDegree(nodes);
	}
	
	@Deprecated
	public HashMap<Integer, ArrayList<INode>> getNodesByDegree()
	{
		return Helpers.getNodesByDegree(this);
	}
	
	@Matlab
	@Deprecated
	public String getNodeString()
	{
		return Helpers.getNodeString(this);
	}
	
	/**
	 * Returns the ith variable contained directly in this graph
	 * (i.e. not in subgraphs).
	 * @param i an index in the range [0,{@link #getOwnedVariableCount()} - 1]
	 * @since 0.05
	 * @deprecated As of 0.08 use {@link #getOwnedVariables()} instead.
	 */
	@Deprecated
	public Variable getOwnedVariable(int i)
	{
		return _ownedVariables.getNth(i);
	}
	
	/**
	 * Gets schedule on current solver
	 * @deprecated instead use {@linkplain ISolverFactorGraph #getSchedule getSchedule} on {@linkplain #getSolver()
	 * solver graph}.
	 */
	@Deprecated
	public ISchedule getSchedule()
	{
		final ISolverFactorGraph sgraph = getSolver();
		return sgraph != null ? sgraph.getSchedule() : EmptySchedule.INSTANCE;
	}
	/**
	 * Returns the scheduler associated with the current solver, if any.
	 * <p>
	 * @deprecated instead either use {@linkplain ISolverFactorGraph#getScheduler() corresponding method on solver}
	 * or look up the corresponding option value (e.g.
	 * {@linkplain com.analog.lyric.dimple.options.BPOptions#scheduler BPOptions.scheduler}
	 * or {@link com.analog.lyric.dimple.solvers.gibbs.GibbsOptions#scheduler GibbsOptions.scheduler}).
	 */
	@Deprecated
	public @Nullable IScheduler getScheduler()
	{
		ISolverFactorGraph sfg = getSolver();
		return sfg != null ? sfg.getScheduler() : null;
	}
	/**
	 * @deprecated instead get {@linkplain ISchedule#scheduleVersion() scheduleVersion} from
	 * {@linkplain #getSolver() solver graph's} {@linkplain ISolverFactorGraph#getSchedule() schedule}.
	 */
	@Deprecated
	public long getScheduleVersionId()
	{
		final ISolverFactorGraph sgraph = getSolver();
		return sgraph != null ? sgraph.getSchedule().scheduleVersion() : -1L;
	}
	@Deprecated
	public HashMap<Integer, ArrayList<INode>> getVariablesByDegree()
	{
		return Helpers.getVariablesByDegree(this);
	}

	@Deprecated
	public TreeMap<Integer, ArrayList<Variable>> getVariablesByDomainSize()
	{
		return Helpers.getVariablesByDomainSize(this);
	}

	/**
	 * @deprecated use {@link #getVariableBlockByLocalId(int)} instead
	 */
	@Deprecated
	public @Nullable List<Variable> getVariableGroup(int variableGroupID)
	{
		return getVariableBlockByLocalId(variableGroupID);
	}
	/**
	 * @deprecated Use {@link #structureVersion() instead}.
	 */

	@Deprecated
	public long getVersionId()
	{
		return _structureVersion;
	}
	
	/**
	 * @deprecated Instead use {@link Node#setName} method on {@code child}.
	 */
	@Deprecated
	public void setChildName(Node child, @Nullable String newName)
	{
		child.setName(newName);
	}
	
	/**
	 * Sets event listener.
	 * <p>
	 * Sets the value to be returned by {@link #getEventListener()}.
	 * This should only be set on a root graph.
	 * <p>
	 * @param listener is the event listener to be used for this graph and all of its
	 * contents. May be set to null to turn off listening.
	 * @deprecated As of release 0.07, this functionality has been moved to {@link DimpleEnvironment#setEventListener}.
	 * @since 0.06
	 */
	@Deprecated
	public void setEventListener(@Nullable DimpleEventListener listener)
	{
		getEnvironment().setEventListener(listener);
		notifyListenerChanged();
	}
	
	@Deprecated
	public void setNamesByStructure()
	{
		Helpers.setNamesByStructure(this);
	}
	
	@Deprecated
	public void setNamesByStructure(String boundaryString,
			String ownedString,
			String factorString,
			String rootGraphString,
			String childGraphString)
	{
		Helpers.setNamesByStructure(this, boundaryString, ownedString, factorString, rootGraphString, childGraphString);
	}
	
	/*-----------------
	 * Private methods
	 */
	
	//==============
	//
	// Scheduling
	//
	//==============
	
	/**
	 * Sets schedule on current solver.
	 * <p>
	 * If {@code schedule} is a {@link FixedSchedule}, then this will create a new
	 * {@link CustomScheduler} containing the specified schedule entries, otherwise
	 * this will set the schedule on the {@linkplain #getSolver current solver graph}.
	 * <p>
	 * @deprecated instead use {@linkplain ISolverFactorGraph#setSchedule setSchedule} on {@linkplain #getSolver()
	 * solver graph}.
	 */
	@Deprecated
	public void setSchedule(@Nullable ISchedule schedule)
	{
		if (schedule instanceof FixedSchedule)
		{
			CustomScheduler scheduler = new CustomScheduler(this);
			scheduler.addAll(schedule);
			setScheduler(scheduler);
		}
		else if (schedule != null)
		{
			requireSolver("setSchedule").setSchedule(schedule);
		}
		else
		{
			// Don't throw exception if solver is not set when schedule is null
			ISolverFactorGraph solver = getSolver();
			if (solver != null)
			{
				solver.setSchedule(schedule);
			}
		}
	}
	/**
	 * Returns subgraph identified by global id
	 * @param globalId
	 * @return subgraph with graph id matching that in {@code globalId} or null if no such graph, or
	 * not a subgraph of this graph.
	 * @since 0.08
	 */
	private @Nullable FactorGraph getSubgraphForGlobalId(long globalId)
	{
		final int graphId = Ids.graphIdFromGlobalId(globalId);
		if (_graphId == graphId)
		{
			return this;
		}
		else
		{
			FactorGraph fg = getEnvironment().factorGraphs().getGraphWithId(graphId);
			if (fg != null && isAncestorOf(fg))
			{
				return fg;
			}
		}
		
		return null;
	}
	
}


