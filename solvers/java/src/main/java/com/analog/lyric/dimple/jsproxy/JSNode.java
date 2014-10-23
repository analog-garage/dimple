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

	/*--------------
	 * Construction
	 */
	
	JSNode(@Nullable JSFactorGraph parent, Delegate node)
	{
		super(node);
		_parent = parent;
	}
	
	/**
	 * Specifies the type of node.
	 */
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
	public @Nullable DimpleApplet getApplet()
	{
		return requireNonNull(_parent).getApplet();
	}

	/*----------------------
	 * JSNProxyNode methods
	 */
	
	public double getBetheEntropy()
	{
		return _delegate.getBetheEntropy();
	}

	/**
	 * The numeric identifier of the node.
	 * @since 0.07
	 */
	public int getId()
	{
		return _delegate.getId();
	}
	
	/**
	 * The name of the node.
	 * @since 0.07
	 */
	public String getName()
	{
		return _delegate.getName();
	}
	
	/**
	 * The factor graph that contains this node.
	 * <p>
	 * May be null if not contained in another graph.
	 * @since 0.07
	 */
	public @Nullable JSFactorGraph getParent()
	{
		return _parent;
	}
	
	/**
	 * The factor graph that most closely contains this object.
	 * <p>
	 * If this object is a {@link JSFactorGraph}, this will just return the object itself, otherwise
	 * this is the same as {@link #getParent()}.
	 * @since 0.07
	 */
	public JSFactorGraph getGraph()
	{
		return requireNonNull(_parent);
	}
	
	public double getInternalEnergy()
	{
		return _delegate.getInternalEnergy();
	}
	
	/**
	 * The number of siblings currently connected to the node.
	 * @since 0.07
	 * @see #getSibling(int)
	 */
	public int getSiblingCount()
	{
		return _delegate.getSiblingCount();
	}
	
	/**
	 * Returns the nth sibling connected to this node.
	 * <p>
	 * The order in which they siblings occur is significant for factors, since it specifies the order in which the
	 * arguments are passed to the underlying factor function.
	 * <p>
	 * @since 0.07
	 */
	public JSNode<?> getSibling(int n)
	{
		final JSFactorGraph graph = getGraph();
		return  graph.wrap(_delegate.getSibling(n));
	}
	
	/**
	 * Indicates the type of node.
	 * @since 0.07
	 */
	public abstract Type getNodeType();
	
	public double getScore()
	{
		return _delegate.getScore();
	}
	
	/**
	 * True if this is a {@link JSFactor} object.
	 * @since 0.07
	 */
	public boolean isFactor()
	{
		return getNodeType() == Type.FACTOR;
	}
	
	/**
	 * True if this is a {@link JSFactorGraph} object.
	 * @since 0.07
	 */
	public boolean isGraph()
	{
		return getNodeType() == Type.GRAPH;
	}
	
	/**
	 * True if this is a {@link JSVariable} object.
	 * @since 0.07
	 */
	public boolean isVariable()
	{
		return getNodeType() == Type.VARIABLE;
	}
}

