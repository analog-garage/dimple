/*******************************************************************************
 *   Copyright 2015 Analog Devices, Inc.
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

import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.options.DimpleOptionHolder;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class FactorGraphChild extends DimpleOptionHolder implements IFactorGraphChild
{
	/*-------
	 * State
	 */
	
	protected int _id;
	
	protected @Nullable FactorGraph _parentGraph;

	/*--------------
	 * Construction
	 */
	
	/**
	 * 
	 * @since 0.08
	 */
	protected FactorGraphChild()
	{
		super();
	}

	/**
	 * @param other
	 * @since 0.08
	 */
	public FactorGraphChild(DimpleOptionHolder other)
	{
		super(other);
	}

	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return Ids.defaultNameForLocalId(_id);
	}
	
	/*---------------------------
	 * IFactorGraphChild methods
	 */
	
	@Override
	@Nullable
	public FactorGraph getContainingGraph()
	{
		return _parentGraph;
	}

	@Override
	@Nullable
	public IDimpleEventSource getEventParent()
	{
		return _parentGraph;
	}

	protected void setParentGraph(@Nullable FactorGraph parentGraph)
	{
		// TODO: combine with adding to owned list?
		_parentGraph = parentGraph;
	}

	/**
	 * Returns the graph that immediately contains this node, or null if node does not belong to any graph
	 * or this is the root graph.
	 * <p>
	 * @see #requireParentGraph()
	 */
	@Override
	@Nullable
	public FactorGraph getParentGraph()
	{
		return _parentGraph;
	}

	/**
	 * Returns the outermost graph containing this node. Returns the node itself if it is the root graph.
	 */
	@Override
	@Nullable
	public FactorGraph getRootGraph()
	{
		FactorGraph parent = _parentGraph;
		return parent != null ? parent.getRootGraph() : null;
	}

	void setLocalId(int id)
	{
		_id = id;
	}

	/**
	 * A local identifier that uniquely identifies the node within its containing graph.
	 * <p>
	 * This id can be used to retrieve the node from its graph using {@link FactorGraph#getNodeByLocalId(int)}.
	 * <p>
	 * @since 0.08
	 * @see #getGlobalId()
	 * @see Ids
	 */
	@Override
	public int getLocalId()
	{
		return _id;
	}

	/**
	 * A global identifier that uniquely identifies the node within the containing Dimple environment.
	 * <p>
	 * This id can be used to retrieve the node from its graph using {@link FactorGraph#getNodeByGlobalId(long)}
	 * or from the environment using {@link DimpleEnvironment#getNodeByGlobalId}.
	 * <p>
	 * @since 0.08
	 * @see #getLocalId()
	 * @see Ids
	 */
	@Override
	public long getGlobalId()
	{
		final FactorGraph parent = _parentGraph;
		return Ids.globalIdFromParts(parent != null ? parent.getGraphId() : 0, _id);
	}

	@Override
	public long getGraphTreeId()
	{
		final FactorGraph parent = _parentGraph;
		return Ids.graphTreeIdFromParts(parent != null ? parent.getGraphTreeIndex() : 0, _id);
	}
	
	@Deprecated
	@Override
	public final long getId()
	{
		return getGlobalId();
	}

	@Override
	public UUID getUUID()
	{
		return Ids.makeUUID(getEnvironment().getEnvId(), getGlobalId());
	}

	/**
	 * Returns parent {@link FactorGraph} or throws an exception if none.
	 * @since 0.08
	 * @throws IllegalStateException if {@link #getParentGraph()} is null.
	 */
	public FactorGraph requireParentGraph()
	{
		final FactorGraph parent = _parentGraph;
		
		if (parent != null)
		{
			return parent;
		}
		
		throw new IllegalStateException(String.format("%s '%s' does not belong to a graph.",
			getClass().getSimpleName(), this));
	}
}