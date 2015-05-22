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

package com.analog.lyric.dimple.data;

import static java.util.Objects.*;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ExtendedArrayList;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.model.variables.Variable;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

/**
 * DataLayer holds data for {@link FactorGraph}s in a graph tree.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class DataLayer<D extends IDatum> extends AbstractMap<Variable, D> implements Cloneable
{
	/*-------
	 * State
	 */
	
	private final Class<D> _baseType;
	private final FactorGraph _rootGraph;
	private final ExtendedArrayList<FactorGraphData<D>> _data;
	private final FactorGraphData.Constructor<D> _constructor;
	
	/*---------
	 * Classes
	 */
	
	private class VarIter extends UnmodifiableIterator<Variable>
	{
		private Iterator<FactorGraphData<D>> _dataIter = Iterators.filter(_data.iterator(), Predicates.notNull());
		private Iterator<Variable> _iter = Iterators.emptyIterator();

		@Override
		public boolean hasNext()
		{
			while (!_iter.hasNext())
			{
				if (_dataIter.hasNext())
				{
					_iter = _dataIter.next().keySet().iterator();
				}
				else
				{
					return false;
				}
			}
			
			return true;
		}
		
		@Override
		public @Nullable Variable next()
		{
			hasNext();
			return _iter.next();
		}
	}
	
	private class VarSet extends AbstractSet<Variable>
	{
		@Override
		public void clear()
		{
			DataLayer.this.clear();
		}
		
		@Override
		public boolean contains(@Nullable Object obj)
		{
			return DataLayer.this.containsKey(obj);
		}
		
		@Override
		public Iterator<Variable> iterator()
		{
			return new VarIter();
		}
		
		@Override
		public boolean remove(@Nullable Object obj)
		{
			return DataLayer.this.remove(obj) != null;
		}
		
		@Override
		public int size()
		{
			return DataLayer.this.size();
		}
	}
	
	private class EntryIter extends UnmodifiableIterator<Map.Entry<Variable,D>>
	{
		private Iterator<FactorGraphData<D>> _dataIter = Iterators.filter(_data.iterator(), Predicates.notNull());
		private Iterator<Map.Entry<Variable,D>> _iter = Iterators.emptyIterator();
		
		@Override
		public boolean hasNext()
		{
			while (!_iter.hasNext())
			{
				if (_dataIter.hasNext())
				{
					_iter = _dataIter.next().entrySet().iterator();
				}
				else
				{
					return false;
				}
			}
			
			return true;
		}
		
		@Override
		public @Nullable Map.Entry<Variable, D> next()
		{
			hasNext();
			return _iter.next();
		}
	}
	
	private class EntrySet extends AbstractSet<Map.Entry<Variable, D>>
	{
		@NonNullByDefault(false)
		@Override
		public boolean add(Map.Entry<Variable, D> entry)
		{
			final Variable var = entry.getKey();
			final D value = entry.getValue();
			return !Objects.equals(value, DataLayer.this.put(var, value));
		}
		
		@Override
		public void clear()
		{
			DataLayer.this.clear();
		}
		
		@Override
		public boolean contains(@Nullable Object obj)
		{
			if (obj instanceof Map.Entry)
			{
				Map.Entry<?,?> entry = (Map.Entry<?,?>)obj;
				return Objects.equals(DataLayer.this.get(entry.getKey()), entry.getValue());
			}
			
			return false;
		}
		
		@Override
		public Iterator<Map.Entry<Variable, D>> iterator()
		{
			return new EntryIter();
		}
		
		@Override
		public boolean remove(@Nullable Object obj)
		{
			if (obj instanceof Map.Entry)
			{
				Map.Entry<?,?> entry = (Map.Entry<?,?>)obj;
				IDatum value = DataLayer.this.get(entry.getKey());
				if (Objects.equals(value, entry.getValue()))
				{
					DataLayer.this.remove(entry.getKey());
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public int size()
		{
			return DataLayer.this.size();
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	public DataLayer(FactorGraph graph, FactorGraphData.Constructor<D> constructor, Class<D> baseType)
	{
		_baseType = baseType;
		_rootGraph = graph.getRootGraph();
		_data = new ExtendedArrayList<>();
		_constructor = constructor;
	}
	
	public DataLayer(FactorGraph graph, FactorGraphData.Constructor<D> constructor)
	{
		this(graph, constructor, constructor.baseType());
	}
	
	protected DataLayer(DataLayer<D> other)
	{
		this(other._rootGraph, other._constructor, other._baseType);
		for (int i = other._data.size(); --i>=0;)
		{
			FactorGraphData<D> data = other._data.getOrNull(i);
			if (data != null)
			{
				_data.set(i, data.clone(this));
			}
		}
	}
	
	public static <D extends IDatum> DataLayer<D> createDense(FactorGraph graph, Class<D> baseType)
	{
		return new DataLayer<D>(graph, DenseFactorGraphData.constructorForType(baseType), baseType);
	}
	
	public static GenericDataLayer createDense(FactorGraph graph)
	{
		return new GenericDataLayer(graph, DenseFactorGraphData.constructorForType(IDatum.class));
	}
	
	public static <D extends IDatum> DataLayer<D> createSparse(FactorGraph graph, Class<D> baseType)
	{
		return new DataLayer<D>(graph, SparseFactorGraphData.constructorForType(baseType), baseType);
	}
	
	public static GenericDataLayer createSparse(FactorGraph graph)
	{
		return new GenericDataLayer(graph);
	}
	
	public static SampleDataLayer createSample(FactorGraph graph)
	{
		return new SampleDataLayer(graph);
	}
	
	@Override
	public DataLayer<D> clone()
	{
		return new DataLayer<>(this);
	}

	/*-----------------
	 * IEquals methods
	 */
	
	public boolean objectEquals(@Nullable Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj instanceof DataLayer<?>)
		{
			DataLayer<?> other = (DataLayer<?>)obj;
		
			if (_rootGraph != other._rootGraph)
			{
				return false;
			}
	
			Iterator<? extends FactorGraphData<D>> iter1 = getData().iterator();
			Iterator<? extends FactorGraphData<?>> iter2 = other.getData().iterator();
	
			while (iter1.hasNext())
			{
				FactorGraphData<D> data1 = iter1.next();
				if (!iter2.hasNext())
				{
					if (data1.isEmpty())
					{
						continue;
					}
					else
					{
						return false;
					}
				}
	
				FactorGraphData<?> data2 = iter2.next();
				if (!data1.objectEquals(data2))
				{
					return false;
				}
			}
	
			while (iter2.hasNext())
			{
				if (!iter2.next().isEmpty())
				{
					return false;
				}
			}
	
			return true;
		}
		
		return false;
	}

	/*-------------
	 * Map methods
	 */
	
	@Override
	public void clear()
	{
		_data.clear();
	}
	
	@Override
	public boolean containsKey(@Nullable Object key)
	{
		final FactorGraphData<D> data = getDataForKey(key);
		return data != null && data.containsKey(key);
	}
	
	@Override
	public Set<Map.Entry<Variable, D>> entrySet()
	{
		return new EntrySet();
	}
	
	@Override
	public @Nullable D get(@Nullable Object key)
	{
		final FactorGraphData<D> data = getDataForKey(key);
		return data != null ? data.get(key) : null;
	}
	
	@Override
	public Set<Variable> keySet()
	{
		return new VarSet();
	}
	
	@NonNullByDefault(false)
	@Override
	public @Nullable D put(Variable var, @Nullable D value)
	{
		assertSharesRoot(var);
		final FactorGraph graph = requireNonNull(var.getParentGraph());
		final FactorGraphData<D> data = createDataForGraph(graph);
		return data.put(var, value);
	}
	
	@Override
	public @Nullable D remove(@Nullable Object key)
	{
		final FactorGraphData<D> data = getDataForKey(key);
		return data != null ? data.remove(key) : null;
	}
	
	@Override
	public int size()
	{
		int n = 0;
		for (FactorGraphData<?> data : _data)
		{
			if (data != null)
			{
				n += data.size();
			}
		}
		return n;
	}
	
	/*-------------------
	 * DataLayer methods
	 */
	
	public final Class<D> baseType()
	{
		return _baseType;
	}
	
	public FactorGraphData<D> createDataForGraph(FactorGraph graph)
	{
		assertSharesRoot(graph);
		
		FactorGraphData<D> data = _data.getOrNull(graph.getGraphTreeIndex());
		if (data == null)
		{
			FactorGraphData<D> newData = _constructor.apply(this, graph);
			setDataForGraph(newData);
			data = newData;
		}

		return data;
	}
	
	public final FactorGraphData.Constructor<D> dataConstructor()
	{
		return _constructor;
	}
	
	public @Nullable D getByGraphTreeId(long id)
	{
		int localId = Ids.localIdFromGraphTreeId(id);
		int graphTreeIndex = Ids.graphTreeIndexFromGraphTreeId(id);
		
		if (Ids.typeIndexFromLocalId(localId) == Ids.VARIABLE_TYPE)
		{
			return getByGraphTreeAndLocalIndices(graphTreeIndex, Ids.indexFromLocalId(localId));
		}
		
		return null;
	}
	
	public @Nullable D getByGraphTreeAndLocalIndices(int graphTreeIndex, int localIndex)
	{
		FactorGraphData<D> data = _data.getOrNull(graphTreeIndex);
		return data != null ? data.getByLocalIndex(localIndex) : null;
	}
	
	public @Nullable FactorGraphData<D> getDataForGraph(FactorGraph graph)
	{
		return sharesRoot(graph) ? _data.getOrNull(graph.getGraphTreeIndex()) : null;
	}
	
	public Iterable<? extends FactorGraphData<D>> getData()
	{
		return Iterables.filter(_data, Predicates.notNull());
	}
	
	public @Nullable FactorGraphData<D> removeDataForGraph(FactorGraph graph)
	{
		return sharesRoot(graph) ? _data.set(graph.getGraphTreeIndex(), null) : null;
	}
	
	public FactorGraph rootGraph()
	{
		return _rootGraph;
	}
	
	public @Nullable FactorGraphData<D> setDataForGraph(FactorGraphData<D> data)
	{
		if (data.layer() != this)
		{
			throw new IllegalArgumentException(String.format("Data belongs to a different layer"));
		}
		
		return _data.set(data.graph().getGraphTreeIndex(), data);
	}
	
	public boolean sharesRoot(INode node)
	{
		return node.getRootGraph() == _rootGraph;
	}
	
	/*-----------------
	 * Package methods
	 */
	
	void assertSharesRoot(INode node)
	{
		if (!sharesRoot(node))
		{
			throw new IllegalArgumentException(String.format("%s does not share root graph with %s", node, this));
		}
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private @Nullable FactorGraphData<D> getDataForKey(@Nullable Object obj)
	{
		if (obj instanceof Variable)
		{
			return getDataForVariable((Variable)obj);
		}
		
		return null;
	}
	
	private @Nullable FactorGraphData<D> getDataForVariable(Variable var)
	{
		FactorGraph graph = var.getParentGraph();
		return graph != null ? getDataForGraph(graph) : null;
	}
}
