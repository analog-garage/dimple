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

package com.analog.lyric.dimple.jsproxy;

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.Node;

/**
 * JavaScript proxy for a Dimple {@link Node} object.
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public abstract class JSNode<Delegate extends Node> extends JSOptionHolder<Delegate>
{
	@Nullable private final JSFactorGraph _parent;
	
	JSNode(@Nullable JSFactorGraph parent, Delegate node)
	{
		super(node);
		_parent = parent;
	}
	
	public enum Type
	{
		FACTOR,
		GRAPH,
		VARIABLE;
	}
	
	/*-----------------------
	 * JSProxyObject methods
	 */
	
	@Override
	public DimpleApplet getApplet()
	{
		return requireNonNull(getParent()).getApplet();
	}

	/*----------------------
	 * JSNProxyNode methods
	 */
	
	public double getBetheEntropy()
	{
		return _delegate.getBetheEntropy();
	}
	
	public int getId()
	{
		return _delegate.getId();
	}
	
	public String getName()
	{
		return _delegate.getName();
	}
	
	public @Nullable JSFactorGraph getParent()
	{
		return _parent;
	}
	
	public JSFactorGraph getGraph()
	{
		return requireNonNull(_parent);
	}
	
	public double getInternalEnergy()
	{
		return _delegate.getInternalEnergy();
	}
	
	public int getSiblingCount()
	{
		return _delegate.getSiblingCount();
	}
	
	public JSNode<?>[] getSiblings()
	{
		final JSFactorGraph graph = getGraph();
		final int n = _delegate.getSiblingCount();
		JSNode<?>[] siblings = new JSNode<?>[n];
		for (int i = n; --i>=0;)
		{
			siblings[i] = graph.wrap(_delegate.getSibling(i));
		}
		return siblings;
	}
	
	public abstract Type getNodeType();
	
	public double getScore()
	{
		return _delegate.getScore();
	}
	
	public boolean isFactor()
	{
		return getNodeType() == Type.FACTOR;
	}
	
	public boolean isGraph()
	{
		return getNodeType() == Type.GRAPH;
	}
	
	public boolean isVariable()
	{
		return getNodeType() == Type.VARIABLE;
	}
}

