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

import com.analog.lyric.collect.IEquals;
import com.analog.lyric.collect.PrimitiveIterable;
import com.analog.lyric.collect.PrimitiveIterator;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.IFactorGraphChild;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.util.misc.Internal;
import com.google.common.collect.UnmodifiableIterator;

import net.jcip.annotations.NotThreadSafe;

/**
 * Holds data for children directly owned by a single {@link FactorGraph}.
 * <p>
 * Most users will not need to use this interface and will instead interact with the
 * containing {@link DataLayerBase} object.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@NotThreadSafe
public abstract class FactorGraphData<K extends IFactorGraphChild, D extends IDatum>
	extends AbstractMap<K, D> implements IEquals
{
	/*-------
	 * State
	 */
	
	protected final Class<K> _keyType;
	protected final Class<D> _baseType;
	protected final DataLayerBase<K, ? super D> _layer;
	protected final FactorGraph _graph;
	protected final int _keyTypeIndex;
	
	/*---------
	 * Classes
	 */
	
	public interface Constructor<K extends IFactorGraphChild, D extends IDatum> // JAVA8: extends BiFunction<DataLayer,FactorGraph,FactorGraphData>
	{
		public FactorGraphData<K,D> apply(DataLayerBase<K, ? super D> layer, FactorGraph graph);
		
		public Class<K> keyType();
		public Class<D> baseType();
	}
	
	private class KeyIter extends UnmodifiableIterator<K>
	{
		private final PrimitiveIterator.OfInt _indicesIter = getLocalIndices().iterator();
		
		@Override
		public boolean hasNext()
		{
			return _indicesIter.hasNext();
		}

		@Override
		public @Nullable K next()
		{
			int index = _indicesIter.nextInt();
			return _keyType.cast(_graph.getChildByLocalId(Ids.localIdFromParts(_keyTypeIndex, index)));
		}
	}
	
	private class KeySet extends AbstractSet<K>
	{
		@Override
		public void clear()
		{
			FactorGraphData.this.clear();
		}
		
		@NonNullByDefault(false)
		@Override
		public boolean contains(Object obj)
		{
			return FactorGraphData.this.containsKey(obj);
		}
		
		@Override
		public Iterator<K> iterator()
		{
			return new KeyIter();
		}
		
		@NonNullByDefault(false)
		@Override
		public boolean remove(Object obj)
		{
			return FactorGraphData.this.remove(obj) != null;
		}
		
		@Override
		public int size()
		{
			return FactorGraphData.this.size();
		}
	}
	
	private class EntrySetIter<E extends Map.Entry<K, D>> extends UnmodifiableIterator<E>
	{
		private final PrimitiveIterator.OfInt _indicesIter = getLocalIndices().iterator();
		
		@Override
		public boolean hasNext()
		{
			return _indicesIter.hasNext();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public E next()
		{
			int index = _indicesIter.nextInt();
			K var = (K)_graph.getChildByLocalId(Ids.localIdFromParts(_keyTypeIndex, index));
			return (E)new DataEntry<K,D>(requireNonNull(var), getByLocalIndex(index));
		}
	}
	
	private class EntrySet<E extends Map.Entry<K,D>> extends AbstractSet<E>
	{
		@NonNullByDefault(false)
		@Override
		public boolean add(E entry)
		{
			final K key = entry.getKey();
			final D value = entry.getValue();
			return !Objects.equals(value, FactorGraphData.this.put(key, value));
		}
		
		@Override
		public void clear()
		{
			FactorGraphData.this.clear();
		}

		@NonNullByDefault(false)
		@Override
		public boolean contains(Object obj)
		{
			if (obj instanceof Map.Entry)
			{
				Map.Entry<?,?> entry = (Map.Entry<?,?>)obj;
				return Objects.equals(FactorGraphData.this.get(entry.getKey()), entry.getValue());
			}
			
			return false;
		}
		
		@Override
		public Iterator<E> iterator()
		{
			return new EntrySetIter<E>();
		}

		@NonNullByDefault(false)
		@Override
		public boolean remove(Object obj)
		{
			if (obj instanceof Map.Entry)
			{
				Map.Entry<?,?> entry = (Map.Entry<?,?>)obj;
				IDatum value = FactorGraphData.this.get(entry.getKey());
				if (Objects.equals(value, entry.getValue()))
				{
					FactorGraphData.this.remove(entry.getKey());
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public int size()
		{
			return FactorGraphData.this.size();
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	protected FactorGraphData(DataLayerBase<K, ? super D> layer, FactorGraph graph, Class<K> keyType, Class<D> baseType)
	{
		layer.assertSharesRoot(graph);
		_baseType = baseType;
		_graph = graph;
		_layer = layer;
		_keyType = keyType;
		_keyTypeIndex = layer._keyTypeIndex;
	}
	
	public static <K extends IFactorGraphChild, D extends IDatum> Constructor<K,D> constructorForType(
		DataDensity density, final Class<K> keyType, final Class<D> baseType)
	{
		switch (density)
		{
		case SPARSE:
			return SparseFactorGraphData.constructorForType(keyType, baseType);
		case DENSE:
		default:
			return DenseFactorGraphData.constructorForType(keyType, baseType);
		}
	}
	
	/**
	 * @category internal
	 */
	@Internal
	public abstract FactorGraphData<K,D> clone(DataLayerBase<K, ? super D> newLayer);
	
	/*-----------------
	 * IEquals methods
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj instanceof FactorGraphData)
		{
			final FactorGraphData<?,?> other = (FactorGraphData<?,?>)obj;
			if (other._graph == _graph && size() == other.size())
			{
				for (int index : getLocalIndices())
				{
					IDatum thisDatum = requireNonNull(getByLocalIndex(index));
					IDatum thatDatum = other.getByLocalIndex(index);
	
					if (!thisDatum.objectEquals(thatDatum))
					{
						return false;
					}
				}
	
				return true;
			}
		}
		
		return false;
	}

	/*-------------
	 * Map methods
	 */
	
	@Override
	public abstract void clear();
	
	@Override
	public @Nullable D get(@Nullable Object obj)
	{
		return _keyType.isInstance(obj) ? get(_keyType.cast(obj)) : null;
	}

	public @Nullable D get(K key)
	{
		return getByLocalIndex(localIndex(key));
	}

	@NonNullByDefault(false)
	@Override
	public boolean containsKey(Object key)
	{
		if (key instanceof IFactorGraphChild)
		{
			return containsLocalIndex(localIndex((IFactorGraphChild)key));
		}

		return false;
	}
	
	/**
	 * Modifiable set view of contents of map.
	 * <p>
	 * Adding/removing entries from the returned set will be reflected in the underlying map.
	 * However, the returned set's iterator does not support the {@linkplain Iterator#remove} operation.
	 * <p>
	 * @see Map#entrySet()
	 * @see #entries()
	 */
	@Override
	public Set<Map.Entry<K, D>> entrySet()
	{
		return new EntrySet<Map.Entry<K,D>>();
	}
	
	/**
	 * Modifiable view of keys.
	 * <p>
	 * Removing keys from the returned set will remove the corresponding entries from this object.
	 * Adding keysis not supported.
	 * <p>
	 * The returned set's iterator does not support the {@linkplain Iterator#remove} operation.
	 */
	@Override
	public Set<K> keySet()
	{
		return new KeySet();
	}
	
	@NonNullByDefault(false)
	@Override
	public @Nullable D put(K key, D datum)
	{
		int index = localIndex(key);
		if (index < 0)
		{
			throw new IllegalArgumentException(String.format("%s does not belong to %s", key, _graph));
		}
		return setByLocalIndex(index, datum);
	}
	
	@Override
	public @Nullable D remove(@Nullable Object key)
	{
		if (key instanceof IFactorGraphChild)
		{
			int index = localIndex((IFactorGraphChild)key);
			if (index >= 0)
			{
				return setByLocalIndex(index, null);
			}
		}

		return null;
	}
	
	/*-------------------------
	 * DataFactorGraph methods
	 */

	public Class<D> baseType()
	{
		return _baseType;
	}
	
	public abstract boolean containsLocalIndex(int index);
	
	/**
	 * Modifiable set view of contents of map.
	 * <p>
	 * This is the same as {@link #entrySet()} but with a more precise return type.
	 * @since 0.08
	 */
	public Set<? extends DataEntry<K,D>> entries()
	{
		return new EntrySet<DataEntry<K,D>>();
	}
	
	/**
	 * The {@link FactorGraph} whose data is represented by this object.
	 * @since 0.08
	 */
	public final FactorGraph graph()
	{
		return _graph;
	}
	
	public Class<K> keyType()
	{
		return _keyType;
	}
	
	/**
	 * The {@link DataLayerBase} that contains this object.
	 * @since 0.08
	 */
	public final DataLayerBase<K, ? super D> layer()
	{
		return _layer;
	}
	
	public @Nullable D getByLocalId(int id)
	{
		return Ids.typeIndexFromLocalId(id) == _keyTypeIndex ? getByLocalIndex(Ids.indexFromLocalId(id)) : null;
	}
	
	public abstract @Nullable D getByLocalIndex(int index);
	
	/**
	 * Return iterable over the local indices for which there are entries.
	 * <p>
	 * The indices may be returned in any order but must not repeat. If this object has
	 * not been modified since the indices were returned then each index will produce a
	 * non-null return value when passed to {@link #getByLocalIndex(int)}.
	 * @since 0.08
	 */
	public abstract PrimitiveIterable.OfInt getLocalIndices();
	
	public abstract @Nullable D setByLocalIndex(int index, @Nullable D datum);
	
	/*-----------------
	 * Private methods
	 */
	
	private int localIndex(IFactorGraphChild child)
	{
		if (Ids.typeIndexFromLocalId(child.getLocalId()) == _keyTypeIndex && child.getParentGraph() == _graph)
		{
			return Ids.indexFromLocalId(child.getLocalId());
		}
		
		return -1;
	}
}
