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

package com.analog.lyric.dimple.model.factors;

import java.util.List;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.variables.Variable;
import org.eclipse.jdt.annotation.Nullable;


public abstract class FactorBase extends Node
{
	/*-----------
	 * Constants
	 */
	
	/**
	 * Flags that are reserved for use by this class and should not be
	 * used by subclasses when invoking {@link #setFlags(int)} or {@link #clearFlags()}.
	 */
	@SuppressWarnings("hiding")
	protected static final int RESERVED = 0xFFFF0000;

	/*--------------
	 * Construction
	 */
	
	// FIXME: constructors should probably have package access to enforce intention that all
	// subclasses are either Factors or FactorGraphs.
	
	public FactorBase(int id)
	{
		super(id);
	}
	
	public FactorBase()
	{
		super();
	}

	/*--------------
	 * Node methods
	 */
	
	/** {@inheritDoc} If null {@link #asFactorGraph()} will be non-null. */
	@Override
	public @Nullable Factor asFactor() { return null; }
	
	/** {@inheritDoc} If null {@link #asFactor()} will be non-null. */
	@Override
	public @Nullable FactorGraph asFactorGraph() { return null; }

	//public abstract void initialize();
	
	@Override
	public Variable getConnectedNodeFlat(int i)
	{
		// Factors may only be connected to variables so this cast should be safe.
		return (Variable)super.getConnectedNodeFlat(i);
	}
	
	@Override
	public Variable getSibling(int i)
	{
		// Factors may only be connected to variables so this cast should be safe.
		return (Variable)super.getSibling(i);
	}

	@Override
	public List<? extends Variable> getSiblings()
	{
		@SuppressWarnings("unchecked")
		List<? extends Variable> siblings = (List<Variable>)super.getSiblings();
		return siblings;
	}
	
}

