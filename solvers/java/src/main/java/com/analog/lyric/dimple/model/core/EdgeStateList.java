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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

/**
 * Holds edges for a {@link FactorGraph}
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
class EdgeStateList extends ArrayList<EdgeState>
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	private final FactorGraph _graph;
	private int _nEdges;
//	private int _nLocalEdges;
//	private int _nOuterEdges;
//	private int _nInnerEdges;
	
	/*--------------
	 * Construction
	 */
	
	EdgeStateList(FactorGraph graph)
	{
		super();
		_graph = graph;
	}
	
	/*------------------
	 * Iterable methods
	 */
	
	@Override
	public Iterator<EdgeState> iterator()
	{
		// Skip over null entries
		return Iterators.filter(super.iterator(), Predicates.notNull());
	}

	/*--------------
	 * List methods
	 */
	
	@NonNullByDefault(false)
	@Override
	public boolean add(EdgeState edge)
	{
		assert(edge.edgeIndexInParent(_graph) == size());
		updateCounts(edge, 1);
		return super.add(edge);
	}
	
	@Override
	public EdgeState set(int index, @Nullable EdgeState edge)
	{
		EdgeState oldEdge = get(index);
		if (oldEdge != null)
		{
			updateCounts(oldEdge, -1);
		}
		if (edge != null)
		{
			updateCounts(edge, 1);
		}
		return super.set(index, edge);
	}

	/*--------------------------
	 * Unsupported List methods
	 * 
	 * These should not be used because they would require edges after the
	 * removed/inserted ones to be reindexed.
	 * 
	 * We only implement them to make sure we fail in an obvious way.
	 */

	@Deprecated
	@Override
	public void add(int index, @Nullable EdgeState element)
	{
		throw unsupported();
	}

	@Deprecated
	@Override
	public boolean addAll(int index, @Nullable Collection<? extends EdgeState> c)
	{
		throw unsupported();
	}
	
	@Deprecated
	@Override
	public EdgeState remove(int index)
	{
		throw unsupported();
	}

	@Deprecated
	@Override
	public boolean remove(@Nullable Object o)
	{
		throw unsupported();
	}

	@Deprecated
	@Override
	public boolean removeAll(@Nullable Collection<?> c)
	{
		throw unsupported();
	}

	@Override
	public boolean retainAll(@Nullable Collection<?> c)
	{
		throw unsupported();
	}
	
	/*-----------------------
	 * EdgeStateList methods
	 */
	
	boolean isInnerEdge(EdgeState edge)
	{
		return !edge.isLocal() && edge.getFactorParent(_graph) != _graph;
	}
	
	boolean isOuterEdge(EdgeState edge)
	{
		return !edge.isLocal() && edge.getFactorParent(_graph) == _graph;
	}
	
	int nEdges()
	{
		return _nEdges;
	}
	
//	int nLocalEdges()
//	{
//		return _nLocalEdges;
//	}
//
//	int nOuterEdges()
//	{
//		return _nOuterEdges;
//	}
//
//	int nInnerEdges()
//	{
//		return _nInnerEdges;
//	}
	
	private class EdgeStateCollection extends AbstractCollection<EdgeState>
	{
		private final int _size;
		private final Predicate<? super EdgeState> _predicate;
		
		EdgeStateCollection(int size, Predicate<? super EdgeState> predicate)
		{
			_size = size;
			_predicate = predicate;
		}

		@Override
		public Iterator<EdgeState> iterator()
		{
			return Iterators.filter(EdgeStateList.super.iterator(), _predicate);
		}
		
		@Override
		public int size()
		{
			return _size;
		}
	}

	private class EdgeCollection extends AbstractCollection<Edge>
	{
		private final int _size;
		private final Predicate<? super EdgeState> _predicate;
		
		EdgeCollection(int size, Predicate<? super EdgeState> predicate)
		{
			_size = size;
			_predicate = predicate;
		}

		@Override
		public Iterator<Edge> iterator()
		{
			return new Iterator<Edge> () {

				private final Iterator<EdgeState> _iter = Iterators.filter(EdgeStateList.super.iterator(), _predicate);
				
				@Override
				public boolean hasNext()
				{
					return _iter.hasNext();
				}

				@Override
				public Edge next()
				{
					return new Edge(_graph, _iter.next());
				}

				@Override
				public void remove()
				{
					throw unsupported();
				}
			};
		}
		
		@Override
		public int size()
		{
			return _size;
		}
	}
	
	/**
	 * Returns collection of non-null entries.
	 * @since 0.08
	 */
	Collection<EdgeState> allEdgeState()
	{
		return new EdgeStateCollection(_nEdges, Predicates.notNull());
	}

	Collection<Edge> allEdges()
	{
		return new EdgeCollection(_nEdges, Predicates.notNull());
	}
	
	/*-----------------
	 * Private methods
	 */
	private void updateCounts(EdgeState edge, int increment)
	{
		_nEdges += increment;
//		switch (edge.type(_graph))
//		{
//		case LOCAL:
//			_nLocalEdges += increment;
//			break;
//		case OUTER:
//			_nOuterEdges += increment;
//			break;
//		case INNER:
//			_nInnerEdges += increment;
//			break;
//		}
	}

	private UnsupportedOperationException unsupported()
	{
		return new UnsupportedOperationException();
	}
}
