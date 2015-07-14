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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.IntArrayIterable;
import com.analog.lyric.collect.PrimitiveIterable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.IFactorGraphChild;
import com.analog.lyric.util.misc.Internal;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntObjectHashMap;
import net.jcip.annotations.NotThreadSafe;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
@NotThreadSafe
public class SparseFactorGraphData<K extends IFactorGraphChild, D extends IDatum> extends FactorGraphData<K,D>
{
	/*-------
	 * State
	 */
	
	private final OpenIntObjectHashMap _data;
	
	/*--------------
	 * Construction
	 */
	
	public SparseFactorGraphData(DataLayerBase<K, ? super D> layer, FactorGraph graph, Class<K> keyType, Class<D> baseType)
	{
		super(layer, graph, keyType, baseType);
		_data = new OpenIntObjectHashMap();
	}
	
	protected SparseFactorGraphData(DataLayerBase<K, ? super D> layer, FactorGraphData<K,D> other)
	{
		this(layer, other._graph, other._keyType, other._baseType);
		for (int i : other.getLocalIndices())
		{
			setByLocalIndex(i, _baseType.cast(requireNonNull(other.getByLocalIndex(i)).clone()));
		}
	}
	
	/**
	 * @category internal
	 */
	@Internal
	@Override
	public SparseFactorGraphData<K,D> clone(DataLayerBase<K, ? super D> newLayer)
	{
		return new SparseFactorGraphData<>(newLayer, this);
	}
	
	public static <K extends IFactorGraphChild, D extends IDatum> Constructor<K,D> constructorForType(
		final Class<K> keyType, final Class<D> baseType)
	{
		return new Constructor<K,D> () {
			@Override
			public FactorGraphData<K,D> apply(DataLayerBase<K, ? super D> layer, FactorGraph graph)
			{
				return new SparseFactorGraphData<>(layer, graph, keyType, baseType);
			}
			
			@Override
			public boolean createOnRead()
			{
				return false;
			}
			
			@Override
			public Class<K> keyType()
			{
				return keyType;
			}
			
			@Override
			public Class<D> baseType()
			{
				return baseType;
			}
		};
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
	public int size()
	{
		return _data.size();
	}
	
	/*-------------------------
	 * FactorGraphData methods
	 */
	
	@Override
	public boolean containsLocalIndex(int index)
	{
		return _data.containsKey(index);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public @Nullable D getByLocalIndex(int index)
	{
		return (D) _data.get(index);
	}
	
	@Override
	public PrimitiveIterable.OfInt getLocalIndices()
	{
		IntArrayList indices = _data.keys();
		return new IntArrayIterable(indices.elements(), 0, indices.size());
	}
	
	@Override
	public boolean isView()
	{
		return false;
	}

	@Override
	public @Nullable D setByLocalIndex(int index, @Nullable D datum)
	{
		D prev = getByLocalIndex(index);

		if (datum != null)
		{
			_data.put(index, _baseType.cast(datum));
		}
		else
		{
			_data.removeKey(index);
		}

		return prev;
	}
}
