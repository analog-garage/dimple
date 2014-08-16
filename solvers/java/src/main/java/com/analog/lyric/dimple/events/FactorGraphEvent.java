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

import java.io.PrintStream;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base class for model events whose source is a {@link FactorGraph}.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public abstract class FactorGraphEvent extends ModelEvent
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	/**
	 * @param source
	 * @since 0.06
	 */
	protected FactorGraphEvent(FactorGraph source)
	{
		super(source);
	}

	/*---------------------
	 * EventObject methods
	 */

	@Override
	public @Nullable FactorGraph getSource()
	{
		return (FactorGraph)source;
	}

	/*---------------------
	 * DimpleEvent methods
	 */

	@Override
	public @Nullable FactorGraph getModelObject()
	{
		return (FactorGraph)source;
	}
	
	@Override
	protected void printDetails(PrintStream out, int verbosity)
	{
		out.format("%s %s '%s'",
			addRemoveType()._verb,
			nodeType()._description,
			getNodeName());
	}
	
	/*--------------------------
	 * FactorGraphEvent methods
	 */
	
	public abstract @Nullable Node getNode();
	
	public abstract String getNodeName();
	
	protected static enum NodeType
	{
		BOUNDARY("boundary variable"),
		FACTOR("factor"),
		SUBGRAPH("subgraph"),
		VARIABLE("variable");
		
		private final String _description;
		
		private NodeType(String desc)
		{
			_description = desc;
		}
	}
	
	protected static enum AddRemoveType
	{
		ADD("added"),
		REMOVE("removed");
		
		private final String _verb;
		
		private AddRemoveType(String verb)
		{
			_verb = verb;
		}
	}
	
	protected abstract AddRemoveType addRemoveType();
	protected abstract NodeType nodeType();
}
