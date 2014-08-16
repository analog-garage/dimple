/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.events;

import java.io.IOException;
import java.io.ObjectOutputStream;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import org.eclipse.jdt.annotation.Nullable;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public abstract class FactorGraphSubgraphEvent extends FactorGraphEvent
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	private transient final FactorGraph _subgraph;
	private final @Nullable String _subgraphName;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @param source of the event
	 * @param subgraph involved in the event
	 * @since 0.06
	 */
	protected FactorGraphSubgraphEvent(FactorGraph source, FactorGraph subgraph)
	{
		super(source);
		_subgraph = subgraph;
		_subgraphName = null;
	}

	/*---------------
	 * Serialization
	 */
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		// Write out _factorName field with non-null value.
		out.writeObject(getSubgraphName());
	}
	
	// The default readObject method should work...
	
	/*--------------------------
	 * FactorGraphEvent methods
	 */
	
	@Override
	public @Nullable Node getNode()
	{
		return _subgraph;
	}
	
	@Override
	public String getNodeName()
	{
		return getSubgraphName();
	}
	
	@Override
	protected final NodeType nodeType()
	{
		return NodeType.SUBGRAPH;
	}
	
	/*--------------------------------
	 * FactorGraphFactorEvent methods
	 */
	
	/**
	 * The subgraph involved in the event.
	 * <p>
	 * Will be null if event was obtained through deserialization.
	 * @since 0.06
	 * @see #getSubgraphName()
	 */
	public @Nullable FactorGraph getSubgraph()
	{
		return _subgraph;
	}
	
	/**
	 * The name of the subgraph involved in the event.
	 * <p>
	 * The value of {@link Factor#getEventSourceName()} on the
	 * factor. Unlike {@link #getSubgraph}, this is preserved by
	 * serialization.
	 * @since 0.06
	 */
	public String getSubgraphName()
	{
		final String name = _subgraphName;
		return name == null ? _subgraph.getEventSourceName() : name;
	}

}
