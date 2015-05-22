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
import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntObjectHashMap;

import com.analog.lyric.collect.IntArrayIterable;
import com.analog.lyric.collect.PrimitiveIterable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.util.misc.Internal;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
@NotThreadSafe
public class SparseFactorGraphData<D extends IDatum> extends FactorGraphData<D>
{
	/*-------
	 * State
	 */
	
	private final OpenIntObjectHashMap _data;
	
	/*--------------
	 * Construction
	 */
	
	public SparseFactorGraphData(DataLayer<? super D> layer, FactorGraph graph, Class<D> baseType)
	{
		super(layer, graph, baseType);
		_data = new OpenIntObjectHashMap();
	}
	
	protected SparseFactorGraphData(DataLayer<? super D> layer, FactorGraphData<D> other)
	{
		this(layer, other._graph, other._baseType);
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
	public SparseFactorGraphData<D> clone(DataLayer<? super D> newLayer)
	{
		return new SparseFactorGraphData<>(newLayer, this);
	}
	
	public static <D extends IDatum> Constructor<D> constructorForType(final Class<D> baseType)
	{
		return new Constructor<D> () {
			@Override
			public FactorGraphData<D> apply(DataLayer<? super D> layer, FactorGraph graph)
			{
				return new SparseFactorGraphData<>(layer, graph, baseType);
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
