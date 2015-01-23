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
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.IntArrayList;

import com.analog.lyric.collect.IndexedArrayList;
import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Uniform;
import com.analog.lyric.dimple.factorfunctions.core.CustomFactorFunctionWrapper;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.JointFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.JointFactorFunction.Functions;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.factors.FactorList;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.repeated.FactorGraphStream;
import com.analog.lyric.dimple.model.repeated.IVariableStreamSlice;
import com.analog.lyric.dimple.model.repeated.VariableStreamBase;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.schedulers.DefaultScheduler;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.FactorGraphDiffs;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.Internal;
import com.analog.lyric.util.misc.MapList;
import com.analog.lyric.util.misc.Matlab;
import com.analog.lyric.util.test.Helpers;
import com.google.common.base.Predicate;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterators;


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
	
	private static class RootState
	{
		private final FactorGraph _root;
		private long _globalStructureVersion = 0;
		
		private RootState(FactorGraph root)
		{
			_root = root;
		}
	}

	private RootState _rootState;
	
	// TODO : some state only needs to be in root graph. Put it in common object.
	
	private @Nullable ISchedule _schedule = null;
	private @Nullable IScheduler _explicitlyAssociatedScheduler = null;
	private @Nullable IScheduler _solverSpecificDefaultScheduler = null;
	
	/**
	 * Incremented for every change to the structure of this graph.
	 */
	private long _structureVersion = 0;
	
	/**
	 * If not equal to _structureVersion, indicates that the graph siblings list is out-of-date.
	 */
	private long _siblingVersionId = -1;

	private long _scheduleVersionId = 0;
	private long _scheduleAssociatedGraphVerisionId = -1;
	private boolean _hasCustomScheduleSet = false;
	private @Nullable Class<? extends ISolverFactorGraph> _schedulerSolverClass;
	private @Nullable IFactorGraphFactory<?> _solverFactory;
	private @Nullable ISolverFactorGraph _solverFactorGraph;
	private @Nullable LoadingCache<Functions, JointFactorFunction> _jointFactorCache = null;
	private final HashSet<VariableStreamBase> _variableStreams = new HashSet<VariableStreamBase>();
	private final ArrayList<FactorGraphStream> _factorGraphStreams = new ArrayList<FactorGraphStream>();
	private int _numSteps = 1;
	private boolean _numStepsInfinite = true;
	
	//new identity related members
	private final HashMap<String, Node> _name2object = new HashMap<>();
	
	private final ArrayList<FactorGraphEdgeState> _edges;
	
	/**
	 * Edges defining the graph's siblings.
	 * <p>
	 * This replaces the sibling indexes from Node, which cannot be used for this due to the
	 * fact the edges may not be a member of this graph's edge list.
	 */
	private final ArrayList<FactorGraphEdgeState> _graphSiblings = new ArrayList<>();
	
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
	private static abstract class LocalEdgeState extends FactorGraphEdgeState
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
			return String.format("[LocalEdgeState #d: %d -> %d]", factorEdgeIndex(), factorIndex(), variableIndex());
		}
		
		@Override
		public final Factor getFactor(FactorGraph fg)
		{
			return fg._ownedFactors.get(factorIndex());
		}
		
		@Override
		public final Variable getVariable(FactorGraph fg)
		{
			return fg._ownedVariables.get(variableIndex());
		}

		@Override
		public final boolean isLocal()
		{
			return true;
		}

		@Override
		public final int variableLocalId()
		{
			return NodeId.localIdFromParts(NodeId.VARIABLE_TYPE, variableIndex());
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
	static abstract class BoundaryEdge extends FactorGraphEdgeState
	{
		private final Factor _factor;
		
		private BoundaryEdge(Factor factor)
		{
			_factor = factor;
		}
		
		@Override
		public String toString()
		{
			return String.format("[BoundaryEdgeState #d: %d (%s) -> %d (%s)]",
				factorEdgeIndex(), factorIndex(), _factor, variableIndex(), getVariable());
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
		public final int factorLocalId()
		{
			return _factor.getLocalId();
		}
		
		@Override
		public final int factorIndex()
		{
			return NodeId.indexFromLocalId(_factor.getLocalId());
		}
		
		@Override
		public final boolean isLocal()
		{
			return false;
		}

		@Override
		public int variableLocalId()
		{
			return NodeId.localIdFromParts(NodeId.BOUNDARY_VARIABLE_TYPE, variableIndex());
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
		@Nullable RootState rootState,
		@Nullable Variable[] boundaryVariables,
		@Nullable String name,
		@Nullable IFactorGraphFactory<?> solver)
	{
		super(NodeId.INITIAL_GRAPH_ID);
		
		_env = DimpleEnvironment.active();
		_graphId = _env.factorGraphs().registerIdForFactorGraph(this);
		_eventAndOptionParent = _env;
		_rootState = rootState != null ? rootState : new RootState(this);
		
		_edges = new ArrayList<>();
		
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
	 * Will be in the range from {@link NodeId#GRAPH_ID_MIN} to {@link NodeId#GRAPH_ID_MAX}.
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
			return  NodeId.globalIdFromParts(0, NodeId.GRAPH_TYPE, _graphId);
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
	 * the implicitly generated name will be computed by {@link NodeId#defaultNameForGraphId(int)}
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
			return NodeId.defaultNameForGraphId(_graphId);
		}
		else
		{
			return NodeId.defaultNameForLocalId(getLocalId());
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

		// First, clear out solver-specific state
		_solverSpecificDefaultScheduler = null;
		
		_solverFactory = factory;

		SG solverGraph = factory != null ? factory.createFactorGraph(this) : null;
		_solverFactorGraph = solverGraph;

		final FactorGraph parent = getParentGraph();
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
		}
		
		createSolverFactors(solverGraph);
	
		return solverGraph;
	}

	private void createSolverFactors(@Nullable ISolverFactorGraph solverGraph)
	{
		// FIXME bug 404
		// It is currently necessary to create solver factors from bottom up
		// because of a dependency in message creation for Gibbs "BlastFromThePast" solver factors
		
		for (FactorGraph subgraph : _ownedSubGraphs)
		{
			subgraph.createSolverFactors(subgraph.getSolver());
		}
		
		for (Factor factor : _ownedFactors)
		{
			factor.createSolverObject(solverGraph);
		}
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
		ISolverFactorGraph solver = requireSolver("advance");
		
		for (VariableStreamBase vs : _variableStreams)
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
	
	public Factor addFactor(FactorFunction factorFunction, Object ... vars)
	{
		int numConstants = 0;

		for (int i = 0; i < vars.length; i++)
		{
			if (!(vars[i] instanceof Variable))
				numConstants++;
		}

		Variable [] newvars = new Variable[vars.length - numConstants];
		Object [] constants = new Object[numConstants];
		int [] constantIndices = new int[numConstants];

		int constantIndex = 0;
		int varIndex = 0;

		for (int i = 0; i < vars.length; i++)
		{
			if (vars[i] instanceof Variable)
			{
				newvars[varIndex] = (Variable)vars[i];
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

	private Factor addFactorNoConstants(FactorFunction factorFunction, Variable ... vars)
	{
		if (vars.length == 0)
			throw new DimpleException("must pass at least one variable to addFactor");

		// Add any variables that do not yet have a parent
		for (Variable v : vars)
		{
			if (v.getParentGraph() == null)
			{
				addVariables(v);
			}
		}

		for (Variable v : vars)
			setVariableSolver(v);

		//TODO: where did the name go?

		Factor f;
		if (allDomainsAreDiscrete(vars))
			f = new DiscreteFactor(factorFunction);
		else
			f = new Factor(factorFunction);

		addFactor(f,vars);

		final ISolverFactorGraph sfg = _solverFactorGraph;
		if (sfg != null)
		{
			f.createSolverObject(_solverFactorGraph);
			sfg.postAddFactor(f);
		}



		return f;

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
	 * True if node is owned directly by this graph.
	 *
	 * @param node
	 */
	public boolean ownsDirectly(Node node)
	{
		final boolean owns = node.getParentGraph() == this;
		assert(owns == ownsDirectly_(node));
		return owns;
	}

	/**
	 * Slower version of {@link #OwnsDirectly} just used for
	 * checking correctness in assertion.
	 */
	private boolean ownsDirectly_(Node node)
	{
		switch (node.getNodeType())
		{
		case VARIABLE:
			return _ownedVariables.containsNode(node);
		case FACTOR:
			return _ownedFactors.containsNode(node);
		case GRAPH:
			return _ownedSubGraphs.containsNode(node);
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

		if (!_ownedVariables.removeNode(v))
		{
			throw new DimpleException("can only currently remove variables that are owned");
		}
		
		v.createSolverObject(null);
		v.setParentGraph(null);
		removeNode(v);
		
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
	 * Joining factors replaces all the original factors with one joint factor.
	 * <p>
	 * We take the cartesian product of the entries of the tables such that the
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
	public Factor join(Variable[] variables, Factor ... factors)
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
	
		// Build map of variables in all factors to its index in the merged factor.
		final Map<Variable, Integer> varToIndex = new HashMap<Variable, Integer>();
		for (int i = 0; i < nVariables; ++i)
		{
			varToIndex.put(variables[i], i);
		}
		
		// Build mappings from each factor's variable order to the merged order
		final BitSet varsUsed = new BitSet(nVariables);
		ArrayList<Tuple2<FactorFunction, int[]>> oldToNew = new ArrayList<Tuple2<FactorFunction, int[]>>(nFactors);
		for (Factor factor : factors)
		{
			final int nVarsInFactor = factor.getSiblingCount();
			final int[] oldToNewIndex = new int[nVarsInFactor];
			for (int i = 0; i < nVarsInFactor; ++i)
			{
				final Variable variable = factor.getSibling(i);
				final Integer oldIndex = varToIndex.get(variable);
				if (oldIndex == null)
				{
					throw new DimpleException("Variable %s from factor %s not in variable list for join");
				}
				oldToNewIndex[i] = oldIndex.intValue();
				varsUsed.set(oldIndex.intValue());
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
		return parentGraph.addFactor(jointFunction, variables);
	}

	/*
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


	// This is the method to use when specifically setting a custom schedule to override an automatic scheduler
	// Setting the schedule to null removes any previously set custom schedule
	public void setSchedule(@Nullable ISchedule schedule)
	{
		_hasCustomScheduleSet = (schedule != null);
		_setSchedule(schedule);
	}
	
	// Get the schedule if one exists or create one if not already created
	public ISchedule getSchedule()
	{
		return _createScheduleIfNeeded();
	}

	// This is the method to use when specifically setting the scheduler, overriding any default scheduler that would otherwise be used
	// Setting the scheduler to null removes any previously set scheduler
	public void setScheduler(@Nullable IScheduler scheduler)
	{
		// Associate the scheduler with the factor graph.
		// This association is maintained by a FactorGraph object so that it can use
		// it when a graph is cloned in the copy constructor used by addGraph.
		_explicitlyAssociatedScheduler = scheduler;
		_createSchedule();								// Create the schedule using this scheduler
	}

	// Get the scheduler that would be used if no custom schedule is set
	public @Nullable IScheduler getScheduler()
	{
		if (_hasCustomScheduleSet)
			return null;
		else if (_explicitlyAssociatedScheduler != null)
			return _explicitlyAssociatedScheduler;
		else if (_solverSpecificDefaultScheduler != null)
			return _solverSpecificDefaultScheduler;
		else
			return new DefaultScheduler();
	}
	
	// Has a custom schedule been set
	public boolean hasCustomSchedule()
	{
		return _hasCustomScheduleSet;
	}
	
	// Has a scheduler been explicitly set
	public boolean hasExplicitlyAssociatedScheduler()
	{
		return _explicitlyAssociatedScheduler != null;
	}

	// Get the scheduler that has been explicitly set, if any
	public @Nullable IScheduler getExplicitlySetScheduler()
	{
		return _explicitlyAssociatedScheduler;
	}
	

	// Allow a specific solver to set a default scheduler that overrides the normal default
	// This would be used if the client doesn't otherwise specify a schedule
	// This should be set by the solver factor-graph's constructor
	public void setSolverSpecificDefaultScheduler(@Nullable IScheduler scheduler)
	{
		_solverSpecificDefaultScheduler = scheduler;
	}
	

	private void _setSchedule(@Nullable ISchedule schedule)
	{
		if (schedule != null)
		{
			schedule.attach(this);
		}
		_schedule = schedule;
		_scheduleVersionId++;
		_scheduleAssociatedGraphVerisionId = _structureVersion;
		final ISolverFactorGraph sfg = _solverFactorGraph;
		_schedulerSolverClass = (sfg == null) ? null : sfg.getClass();
	}

	private ISchedule _createSchedule()
	{
		IScheduler scheduler = getScheduler();	// Get the scheduler or create one if not already set
		if (scheduler == null)
			throw new DimpleException("Custom schedule has been set, but is not up-to-date with the current graph.");
		final ISchedule schedule = scheduler.createSchedule(this);
		_setSchedule(schedule);
		return schedule;
	}

	private ISchedule _createScheduleIfNeeded()
	{
		// If there's no schedule yet, or if it isn't up-to-date, then create one
		// Otherwise, don't bother to spend the time since there's already a perfectly good schedule
		final ISchedule schedule = _schedule;
		return schedule != null && isUpToDateSchedulePresent() ? schedule : _createSchedule();
	}

	
	// Has the solver changed since last time the schedule has been created
	private boolean _solverHasChanged()
	{
		final ISolverFactorGraph sfg = _solverFactorGraph;
		final Class<? extends ISolverFactorGraph> sfgClass = _schedulerSolverClass;
		
		if (sfgClass == null && sfg == null)
			return false;
		else if (sfgClass != null && sfg != null && sfgClass.equals(sfg.getClass()))
			return false;
		else
			return true;
	}


	// This allows the caller to determine if the scheduler will be run in solve
	public boolean isUpToDateSchedulePresent()
	{
		if (_schedule == null)											// Not up-to-date if no current schedule
			return false;
		else if (_scheduleAssociatedGraphVerisionId != _structureVersion)		// Not up-to-date if graph has changed
			return false;
		else if (_hasCustomScheduleSet)									// If custom schedule has been set, then keep that regardless of whether the solver has changed
			return true;
		else if (_solverHasChanged())									// Not up-to-date if solver has changed
			return false;
		else
			return true;
		
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
					v.setParentGraph(null);
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
				new HashMap<Node, Node>());
			}

	// Copy constructor -- create a graph incorporating all of the variables, functions, and sub-graphs of the template graph
	private FactorGraph(@Nullable Variable[] boundaryVariables,
			FactorGraph templateGraph,
			@Nullable FactorGraph parentGraph,
			boolean copyToRoot,
			Map<Node, Node> old2newObjs)
	{
		this(parentGraph != null ? parentGraph._rootState : null, boundaryVariables,
			templateGraph.getExplicitName(), null);

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

				//old2newIds.put(vTemplate.getId(), vBoundary.getId());
				old2newObjs.put(vTemplate,vBoundary);
			}
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
		}

		// If there's a scheduler associated with the template graph, copy it
		// for use to this graph. But leave the creation of a schedule for later
		// when the parent graph's schedule is created.
		_explicitlyAssociatedScheduler = templateGraph._explicitlyAssociatedScheduler;

		_setParentGraph(parentGraph);

		//Now that we've copied the graph, let's copy the Schedule if it's
		//already been created.
		ISchedule templateSchedule = templateGraph._schedule;
		if (templateSchedule != null)
		{
			ISchedule scheduleCopy = copyToRoot ?
					templateSchedule.copyToRoot(old2newObjs) :
						templateSchedule.copy(old2newObjs);
			_setSchedule(scheduleCopy);	// Might or might not be a custom schedule
			_hasCustomScheduleSet = templateGraph._hasCustomScheduleSet;
		}

	}

	public FactorGraph copyRoot()
	{
		return copyRoot(new HashMap<Node, Node>());
	}
	public FactorGraph copyRoot(Map<Node, Node> old2newObjs)
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
							ports.add(new Port(factor, j));
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
						final FactorGraphEdgeState edge = var.getSiblingEdgeState(i);
						
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
	public FactorGraphEdgeState getSiblingEdgeState(int i)
	{
		updateSiblings();
		return _graphSiblings.get(i);
	}

	@Override
	public int indexOfSiblingEdgeState(FactorGraphEdgeState edge)
	{
		updateSiblings();
		return _graphSiblings.indexOf(edge);
	}
	
	public FactorGraphEdgeState getGraphEdgeState(int i)
	{
		return _edges.get(i);
	}

	public int getGraphEdgeCount()
	{
		return _edges.size();
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
			//Tell variable about us...
			variable.setParentGraph(this);
			//...and us about the variable
			_ownedVariables.add(variable);
			
			if ((_flags & VARIABLE_ADD_EVENT) != 0)
			{
				raiseEvent(new VariableAddEvent(this, variable, absorbedFromSubgraph));
			}
		}
		addName(variable);
	}

	@Override
	public void initialize(int portNum)
	{
		throw new DimpleException("not supported");
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

		for (Factor f : getNonGraphFactorsFlat() )
		{
			final ISolverFactor sf = f.getSolver();
			if (sf != null)
			{
				sf.createMessages();
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
		subgraph._rootState = new RootState(subgraph);
		
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

	public @Nullable Variable getVariable(long id)
	{
		return (Variable)getNodeByGlobalId(id);
	}
	
	public @Nullable Variable getVariableByLocalId(int id)
	{
		switch (id >>> NodeId.LOCAL_ID_NODE_TYPE_OFFSET)
		{
		case NodeId.VARIABLE_TYPE:
			return _ownedVariables.getByLocalId(id);
		case NodeId.BOUNDARY_VARIABLE_TYPE:
			return _boundaryVariables.get(NodeId.indexFromLocalId(id));
		default:
			return null;
		}
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
		return NodeId.nodeTypeFromLocalId(id) == NodeType.FACTOR ? _ownedFactors.getByLocalId(id) : null;
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
	public long globalStructureVersion()
	{
		return _rootState._globalStructureVersion;
	}
	
	/**
	 * Counter that is incremented whenever structure of graph changes.
	 * <p>
	 * May be used to verify cached information that depends on the graph structure.
	 * @since 0.08
	 * @see #globalStructureVersion()
	 */
	public long structureVersion()
	{
		return _structureVersion;
	}
	
	final void structureChanged()
	{
		++_structureVersion;
		++_rootState._globalStructureVersion;
	}

	public long getScheduleVersionId()
	{
		return _scheduleVersionId;
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
		Node childFound = getNodeByGlobalId(child.getGlobalId());

		//If it's not our child, bad
		if(childFound == null)
		{
			throw new DimpleException("ERROR child UUID not found");
		}
		//If new name already here, bad
		else if (getObjectByName(newName) != null)
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

	@SuppressWarnings("null")
	public @Nullable Node getObjectByName(@Nullable String name)
	{
		Node obj = null;
		
		if (name != null && !name.isEmpty())
		{
			if (NodeId.isUUIDString(name))
			{
				obj = getObjectByUUID(UUID.fromString(name));
			}
			else
			{
				String remainder = null;
		
				int dotOffset = name.indexOf('.');
				if (dotOffset >= 0)
				{
					remainder = name.substring(dotOffset + 1);
					name = name.substring(0, dotOffset);
					
					// Check to see if name refers to this graph.
					if (name.equals(_name) || _graphId == NodeId.graphIdFromDefaultName(name))
					{
						name = remainder;
						remainder = null;

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
					obj = getNodeByLocalId(NodeId.localIdFromDefaultName(name));
				}
		
				if (remainder != null && obj instanceof FactorGraph)
				{
					FactorGraph subgraph = (FactorGraph)obj;
					obj = subgraph.getObjectByName(remainder);
				}
			}
		}
		
		return obj;
	}

	public @Nullable Node getObjectByUUID(UUID uuid)
	{
		return getNodeByGlobalId(NodeId.globalIdFromUUID(uuid));
	}
	
	/**
	 * Returns node in this graph or subgraph with given global id.
	 * @return node with given global id or null if not found or not a
	 * member of this graph or its subgraphs
	 * @since 0.08
	 */
	public @Nullable Node getNodeByGlobalId(long gid)
	{
		final int graphId = NodeId.graphIdFromGlobalId(gid);
		final int id = NodeId.localIdFromGlobalId(gid);
		if (_graphId == graphId)
		{
			return getNodeByLocalId(id);
		}
		else
		{
			FactorGraph fg = getEnvironment().factorGraphs().getGraphWithId(graphId);
			if (fg != null && isAncestorOf(fg))
			{
				return fg.getNodeByLocalId(id);
			}
		}
		
		return null;
	}
	
	/**
	 * Returns node directly owned by this graph with given local id.
	 * @return node with given id or null if not found.
	 * @since 0.08
	 * @see #getNodeByGlobalId
	 */
	@Internal
	public @Nullable Node getNodeByLocalId(int id)
	{
		switch (id >>> NodeId.LOCAL_ID_NODE_TYPE_OFFSET)
		{
		case NodeId.FACTOR_TYPE:
			return _ownedFactors.getByLocalId(id);
		case NodeId.GRAPH_TYPE:
			return _ownedSubGraphs.getByLocalId(id);
		case NodeId.VARIABLE_TYPE:
			return _ownedVariables.getByLocalId(id);
		case NodeId.BOUNDARY_VARIABLE_TYPE:
			return _boundaryVariables.get(NodeId.indexFromLocalId(id));
		default:
			return null;
		}
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
		return _rootState._root;
	}

	public @Nullable IFactorGraphFactory<?> getFactorGraphFactory()
	{
		return _solverFactory;
	}

	@Override
	public double getScore()
	{
		return requireSolver("getScore").getScore();
	}

	public double getBetheFreeEnergy()
	{
		return requireSolver("getBetheFreeEnergy").getBetheFreeEnergy();
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


	// For operating collectively on groups of variables that are not already part of a variable vector
	protected @Nullable HashMap<Integer, ArrayList<Variable>> _variableGroups;
	protected int _variableGroupID = 0;
	public int defineVariableGroup(ArrayList<Variable> variableList)
	{
		HashMap<Integer, ArrayList<Variable>> variableGroups = _variableGroups;
		if (variableGroups == null)
		{
			variableGroups = _variableGroups = new HashMap<Integer, ArrayList<Variable>>();
		}
		variableGroups.put(_variableGroupID, variableList);
		return _variableGroupID++;
	}

	public @Nullable ArrayList<Variable> getVariableGroup(int variableGroupID)
	{
		final HashMap<Integer, ArrayList<Variable>> groups = _variableGroups;
		return groups != null ? groups.get(variableGroupID) : null;
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
		assert(factor.getParentGraph() == this);

		final FactorGraph variableGraph = requireNonNull(variable.getParentGraph());
		
		FactorGraphEdgeState edge;
		
		if (this == variableGraph)
		{
			edge = createLocalEdge(_edges.size(), factor, variable);
		}
		else
		{
			edge = createBoundaryEdge(_edges.size(), variableGraph._edges.size(), factor, variable);
			variableGraph._edges.add(edge);
		}
		
		_edges.add(edge);
		factor.addSiblingEdgeState(edge);
		variable.addSiblingEdgeState(edge);
		
		structureChanged();
		if (!edge.isLocal())
		{
			variableGraph.structureChanged();
		}
	}
	
	private FactorGraphEdgeState createLocalEdge(int edgeIndex, Factor factor, Variable variable)
	{
		final int factorOffset = NodeId.indexFromLocalId(factor.getLocalId());
		final int variableOffset = NodeId.indexFromLocalId(variable.getLocalId());
		return LocalEdgeState.create(edgeIndex, factorOffset, variableOffset);
	}
	
	private FactorGraphEdgeState createBoundaryEdge(int factorEdgeIndex, int variableEdgeIndex, Factor factor,
		Variable variable)
	{
		final int boundaryOffset = _boundaryVariables.indexOf(variable);
		assert(boundaryOffset >= 0);

		return BoundaryEdge.create(factor, factorEdgeIndex, boundaryOffset, variableEdgeIndex);
	}

	@Override
	@Internal
	protected void removeSiblingEdge(FactorGraphEdgeState edge)
	{
		final Factor factor = edge.getFactor(this);
		final Variable variable = edge.getVariable(this);
		final FactorGraph variableGraph = requireNonNull(variable.getParentGraph());
		
		factor.removeSiblingEdgeState(edge);
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
		
		final FactorGraphEdgeState oldEdge = factor.getSiblingEdgeState(edgeIndex);
		final Variable oldVariable = oldEdge.getVariable(factorGraph);
		final FactorGraph oldVariableGraph = requireNonNull(oldVariable.getParentGraph());
		final FactorGraph newVariableGraph = requireNonNull(newVariable.getParentGraph());

		// Old sibling code
		factor.replaceSibling(oldVariable, newVariable);
		
		final int factorEdgeIndex = oldEdge.factorEdgeIndex();
		final int oldVariableEdgeIndex = oldEdge.variableEdgeIndex();
		
		final FactorGraphEdgeState newEdge;
		
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
		factor.replaceSiblingEdgeState(oldEdge, newEdge);

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
	@Internal
	public ISolverFactorGraph requireSolver(String method)
	{
		ISolverFactorGraph solver = getSolver();
		if (solver == null)
		{
			throw new DimpleException("Solver not set. Required by '%s'", method);
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
		setChildNameInternal(child, newName);
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
}

