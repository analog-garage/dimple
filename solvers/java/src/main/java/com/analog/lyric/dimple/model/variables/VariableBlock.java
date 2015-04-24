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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import cern.colt.map.OpenLongObjectHashMap;

import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphChild;
import com.analog.lyric.util.misc.Internal;
import com.google.common.primitives.Longs;

/**
 * Represents a block of {@link Variable}s in a {@link FactorGraph}.
 * <p>
 * The list of variables in this block is immutable and cannot change once it has been created.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 * @see FactorGraph#addVariableBlock(Collection)
 */
@Immutable
public final class VariableBlock extends FactorGraphChild implements List<Variable>, RandomAccess
{
	/*-------
	 * State
	 */
	
	private final long[] _variableGraphTreeIds;
	private final int _hashCode;
	
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
	 * @throws IllegalArgumentException if a variable does not belong to the same tree of graphs as {@code parent}
	 * or the same variable appears more than once.
	 * @category internal
	 */
	@Internal
	public VariableBlock(FactorGraph parent, Collection<Variable> variables)
	{
		super();
		super.setParentGraph(parent);
		
		final int n = variables.size();
		
		if (n <= 0)
		{
			throw new IllegalArgumentException("Cannot create empty VariableBlock");
		}
		
		final OpenLongObjectHashMap varSet = new OpenLongObjectHashMap(n);
		final FactorGraph root = parent.getRootGraph();
		_variableGraphTreeIds = new long[n];
		
		int i = -1;
		for (Variable var : variables)
		{
			if (var.getRootGraph() != root)
			{
				throw new IllegalArgumentException(String.format("Variable '%s' not in graph tree", var));
			}
			
			// TODO - perhaps boundary variables should be stored using their boundary id
			final long id = var.getGraphTreeId();
			
			if (!varSet.put(id, null))
			{
				throw new IllegalArgumentException(String.format("Variable '%s' was specified more than once", var));
			}
			
			_variableGraphTreeIds[++i] = id;
		}
		
		_hashCode = Arrays.hashCode(_variableGraphTreeIds);
	}
	
	/*----------------
	 * Object methods
	 */

	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (obj instanceof VariableBlock && obj.hashCode() == _hashCode)
		{
			return Arrays.equals(_variableGraphTreeIds, ((VariableBlock)obj)._variableGraphTreeIds);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return _hashCode;
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
	
	@SuppressWarnings("null")
	@Override
	public FactorGraph getParentGraph()
	{
		return _parentGraph;
	}
	
	/*---------------
	 * List methods
	 */

	@Override
	public boolean isEmpty()
	{
		return false; // Empty variable block is not allowed, see constructor.
	}

	@Override
	public int size()
	{
		return _variableGraphTreeIds.length;
	}

	@NonNullByDefault(false)
	@Override
	public boolean contains(Object obj)
	{
		return indexOf(obj) >= 0;
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
		final long id = _variableGraphTreeIds[index];
		Variable var = (Variable)requireParentGraph().getNodeByGraphTreeId(id);
		
		if (var == null)
		{
			throw new IllegalStateException(String.format("Variable with graph tree id 0x%X no longer in graph", id));
		}
		
		return var;
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
				return Longs.indexOf(_variableGraphTreeIds, var.getGraphTreeId());
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
				return Longs.lastIndexOf(_variableGraphTreeIds, var.getGraphTreeId());
			}
		}
		
		return -1;
	}

	@Override
	public ListIterator<Variable> listIterator()
	{
		return listIterator(0);
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
		return _variableGraphTreeIds[index];
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
			return _variableGraphTreeIds.length;
		}
	}
	
	private static RuntimeException immutable()
	{
		return new UnsupportedOperationException("VariableBlock is immutable");
	}
}
