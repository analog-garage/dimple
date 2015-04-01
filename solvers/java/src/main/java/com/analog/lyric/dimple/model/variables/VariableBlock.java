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

package com.analog.lyric.dimple.model.variables;

import static java.util.Objects.*;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.LongArrayList;

import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphChild;
import com.analog.lyric.util.misc.Internal;

/**
 * Represents a block of {@link Variable}s in a {@link FactorGraph}.
 * <p>
 * The list of variables in this block is immutable and cannot change once it has been created.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public final class VariableBlock extends FactorGraphChild implements List<Variable>, RandomAccess
{
	/*-------
	 * State
	 */
	
	private final LongArrayList _variableGraphTreeIds;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct a new variable block.
	 * <p>
	 * This constructor is intended to be used internally in the implementation of
	 * {@link FactorGraph#addVariableBlock(Collection)}.
	 * <p>
	 * @param parent is the graph
	 * @param variables are the variables that will comprise the block. The variables will be added in the
	 * order of the {@linkplain Collection#iterator iterator}.
	 * @since 0.08
	 * @throws IllegalArgumentException if a variable does not belong to the same tree of graphs as {@code parent}.
	 * @category internal
	 */
	@Internal
	public VariableBlock(FactorGraph parent, Collection<Variable> variables)
	{
		super();
		super.setParentGraph(parent);
		
		final FactorGraph root = parent.getRootGraph();
		_variableGraphTreeIds = new LongArrayList(variables.size());
		for (Variable var : variables)
		{
			if (var.getRootGraph() != root)
			{
				throw new IllegalArgumentException("");
			}
			
			// TODO - verify varibles are from same graph tree
			_variableGraphTreeIds.add(var.getGraphTreeId());
		}
	}
	
	/*----------------------------
	 * IDimpleEventSource methods
	 */
	
	@Override
	public String getEventSourceName()
	{
		return toString();
	}

	@Override
	public @Nullable IModelEventSource getModelEventSource()
	{
		return getParentGraph();
	}

	@Override
	public void notifyListenerChanged()
	{
	}

	/*--------------------------
	 * FactorGraphChild methods
	 */
	
	@Override
	protected void setParentGraph(@Nullable FactorGraph parentGraph)
	{
		if (parentGraph != _parentGraph)
		{
			throw new UnsupportedOperationException("Cannot invoke setParentGraph on VariableBlock");
		}
	}
	
	/*---------------
	 * List methods
	 */

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public int size()
	{
		return _variableGraphTreeIds.size();
	}

	@NonNullByDefault(false)
	@Override
	public boolean contains(Object obj)
	{
		if (obj instanceof Variable)
		{
			Variable var = (Variable)obj;
			if (var.getRootGraph() == requireParentGraph().getRootGraph())
			{
				return _variableGraphTreeIds.contains(var.getGraphTreeId());
			}
		}
		
		return false;
	}

	@NonNullByDefault(false)
	@Override
	public boolean containsAll(Collection<?> collection)
	{
		for (Object obj : collection)
		{
			if (!contains(obj))
			{
				return false;
			}
		}
		
		return true;
	}

	@Override
	public Variable get(int index)
	{
		long id = _variableGraphTreeIds.get(index);
		return (Variable)requireNonNull(requireParentGraph().getNodeByGraphTreeId(id));
	}

	@NonNullByDefault(false)
	@Override
	public int indexOf(Object obj)
	{
		if (obj instanceof Variable)
		{
			Variable var = (Variable)obj;
			if (var.getRootGraph() == requireParentGraph().getRootGraph())
			{
				return _variableGraphTreeIds.indexOf(var.getGraphTreeId());
			}
		}
		
		return -1;
	}

	@Override
	public Iterator<Variable> iterator()
	{
		return new Wrapper().iterator();
	}

	@NonNullByDefault(false)
	@Override
	public int lastIndexOf(Object obj)
	{
		if (obj instanceof Variable)
		{
			Variable var = (Variable)obj;
			if (var.getRootGraph() == requireParentGraph().getRootGraph())
			{
				return _variableGraphTreeIds.lastIndexOf(var.getGraphTreeId());
			}
		}
		
		return -1;
	}

	@Override
	public ListIterator<Variable> listIterator()
	{
		return new Wrapper().listIterator();
	}

	@Override
	public ListIterator<Variable> listIterator(int index)
	{
		return new Wrapper().listIterator(index);
	}

	@Override
	public List<Variable> subList(int fromIndex, int toIndex)
	{
		return new Wrapper().subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray()
	{
		return new Wrapper().toArray();
	}

	@SuppressWarnings("unchecked")
	@NonNullByDefault(false)
	@Override
	public <T> T[] toArray(T[] array)
	{
		return new Wrapper().toArray(array);
	}
	
	/*-----------------------
	 * VariableBlock methods
	 */

	/**
	 * Get the {@linkplain Variable#getGraphTreeId() graph tree id} of a variable in the block.
	 * <p>
	 * Because this is the underlying representation, this is faster than calling
	 * <blockquote>
	 * {@code get(index).getGraphTreeId()}.
	 * </blockquote>
	 * <p>
	 * @param index is a non-negative value less than {@link #size}.
	 * @since 0.08
	 * @throws IndexOutOfBoundsException if index is not in valid range.
	 */
	public long getVariableGraphTreeId(int index)
	{
		return _variableGraphTreeIds.get(index);
	}

	/*--------------------------
	 * Unsupported list methods
	 */
	
	@NonNullByDefault(false)
	@Override
	public boolean add(Variable e)
	{
		throw immutable();
	}

	@NonNullByDefault(false)
	@Override
	public void add(int index, Variable element)
	{
		throw immutable();
	}

	@NonNullByDefault(false)
	@Override
	public boolean addAll(Collection<? extends Variable> c)
	{
		throw immutable();
	}

	@NonNullByDefault(false)
	@Override
	public boolean addAll(int index, Collection<? extends Variable> c)
	{
		throw immutable();
	}

	@Override
	public void clear()
	{
		throw immutable();
	}

	@NonNullByDefault(false)
	@Override
	public boolean remove(Object o)
	{
		throw immutable();
	}

	@Override
	public Variable remove(int index)
	{
		throw immutable();
	}

	@NonNullByDefault(false)
	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw immutable();
	}

	@NonNullByDefault(false)
	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw immutable();
	}


	@NonNullByDefault(false)
	@Override
	public Variable set(int index, Variable element)
	{
		throw immutable();
	}
	
	/*-----------------
	 * Private methods
	 */

	/**
	 * Simple wrapper for this class that allows us to use AbstractList implementations
	 * for some of the non-trivial methods.
	 * 
	 * @since 0.08
	 * @author Christopher Barber
	 */
	private class Wrapper extends AbstractList<Variable>
	{
		@Override
		public Variable get(int index)
		{
			return VariableBlock.this.get(index);
		}

		@Override
		public int size()
		{
			return _variableGraphTreeIds.size();
		}
	}
	
	private static RuntimeException immutable()
	{
		throw new UnsupportedOperationException("VariableBlock is immutable");
	}
}
