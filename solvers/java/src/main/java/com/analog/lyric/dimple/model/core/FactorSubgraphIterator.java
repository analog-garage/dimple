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

package com.analog.lyric.dimple.model.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.SingleIterator;
import com.google.common.collect.UnmodifiableIterator;

/**
 * Iterator that visits subgraphs of a {@link FactorGraph} in depth-first order down to specified depth.
 * <p>
 * The iterator will visit nested subgraphs of {@link #root} graph in depth-first order, starting with
 * the root. Only graphs within {@link #maxNestingDepth()} will be included, where
 * immediate children are considered to be at depth 1.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class FactorSubgraphIterator extends  UnmodifiableIterator<FactorGraph>
{
	/*-------
	 * State
	 */
	
	private final FactorGraph _root;
	private final List<Iterator<FactorGraph>> _iteratorStack;
	private final int _maxNestingDepth;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs iterator that will visit all nested subgraphs.
	 * @since 0.08
	 */
	public FactorSubgraphIterator(FactorGraph graph)
	{
		this(graph, Integer.MAX_VALUE);
	}

	/**
	 * Constructs iterator that will visit nested subgraphs down to specified depth.
	 * @param graph
	 * @param maxNestingDepth
	 * @since 0.08
	 */
	public FactorSubgraphIterator(FactorGraph graph, int maxNestingDepth)
	{
		_root = graph;
		_iteratorStack = new ArrayList<>();
		_maxNestingDepth = Math.max(maxNestingDepth,0);
		reset();
	}
	
	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		for (int i = _iteratorStack.size(); --i >= 0;)
		{
			if (_iteratorStack.get(i).hasNext())
			{
				return true;
			}
			_iteratorStack.remove(i);
		}
		
		return false;
	}

	@Override
	public @Nullable FactorGraph next()
	{
		for (int i = _iteratorStack.size(); --i >= 0;)
		{
			final FactorGraph graph = _iteratorStack.get(i).next();
			if (graph != null)
			{
				if (i < _maxNestingDepth)
				{
					_iteratorStack.add(graph.getOwnedGraphs().iterator());
				}
				return graph;
			}
			_iteratorStack.remove(i);
		}
		
		return null;
	}

	/*--------------------------------
	 * FactorSubgraphIterator methods
	 */
	
	/**
	 * Returns the maximum depth below the {@link #root} to include in the iteration.
	 * @since 0.08
	 */
	public int maxNestingDepth()
	{
		return _maxNestingDepth;
	}
	
	/**
	 * Resets iterator back to initial state on construction.
	 * 
	 * @since 0.08
	 */
	public void reset()
	{
		_iteratorStack.clear();
		_iteratorStack.add(new SingleIterator<>(_root));
	}
	
	/**
	 * Returns the root graph visited by this iterator.
	 * @since 0.08
	 */
	public FactorGraph root()
	{
		return _root;
	}
}
