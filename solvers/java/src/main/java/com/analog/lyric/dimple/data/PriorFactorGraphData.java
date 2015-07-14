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

import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.PrimitiveIterable;
import com.analog.lyric.collect.PrimitiveIterator;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphIterators;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.model.variables.Variable;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

/**
 * {@link FactorGraphData} implementation for {@link Variable#getPrior() variable priors}.
 * <p>
 * Priors are stored directly in Variable instances, so this class simply maps to those
 * and has no variable-specific local state.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@NotThreadSafe
public class PriorFactorGraphData extends FactorGraphData<Variable, IDatum>
{
	/*--------------
	 * Construction
	 */
	
	enum Constructor implements FactorGraphData.Constructor<Variable, IDatum>
	{
		INSTANCE;
		
		@Override
		public FactorGraphData<Variable, IDatum> apply(DataLayerBase<Variable, ? super IDatum> layer, FactorGraph graph)
		{
			return new PriorFactorGraphData(layer, graph);
		}

		@Override
		public boolean createOnRead()
		{
			return true;
		}
		
		@Override
		public Class<Variable> keyType()
		{
			return Variable.class;
		}

		@Override
		public Class<IDatum> baseType()
		{
			return IDatum.class;
		}
	}
	
	public static FactorGraphData.Constructor<Variable, IDatum> constructor()
	{
		return Constructor.INSTANCE;
	}
		
	public PriorFactorGraphData(DataLayerBase<Variable, ? super IDatum> layer, FactorGraph graph)
	{
		super(layer, graph, Variable.class, IDatum.class);
	}

	@Override
	public FactorGraphData<Variable, IDatum> clone(DataLayerBase<Variable, ? super IDatum> newLayer)
	{
		return new PriorFactorGraphData(newLayer, _graph);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (obj instanceof PriorFactorGraphData)
		{
			// Since this object doesn't directly hold any data, all we need to know is that
			// they refer to the same graph.
			return ((PriorFactorGraphData)obj).graph() == _graph;
		}
		
		return super.equals(obj);
	}
	
	/*-------------
	 * Map methods
	 */
	
	@Override
	public void clear()
	{
		for (Variable var : _graph.getOwnedVariables())
		{
			var.setPrior(null);
		}
	}

	@Override
	public int size()
	{
		int count = 0;
		for (Variable var : _graph.getOwnedVariables())
		{
			if (var.getPrior() != null)
			{
				++count;
			}
		}
		return count;
	}
	
	/*-------------------------
	 * FactorGraphData methods
	 */

	@Override
	public boolean containsLocalIndex(int index)
	{
		return getByLocalIndex(index) != null;
	}

	@Override
	public @Nullable IDatum getByLocalIndex(int index)
	{
		Variable var = _graph.getVariableByLocalId(Ids.localIdFromParts(Ids.VARIABLE_TYPE, index));
		return var != null ? var.getPrior() : null;
	}

	@Override
	public PrimitiveIterable.OfInt getLocalIndices()
	{
		return new LocalIndexIterable();
	}

	/**
	 * Returns true to indicate that this is a view of priors held directly in the {@link Variable} objects.
	 * <p>
	 * Because this is a view, cloning this object does not create a distinct copy of the values.
	 */
	@Override
	public boolean isView()
	{
		return true;
	}

	@Override
	public @Nullable IDatum setByLocalIndex(int index, @Nullable IDatum datum)
	{
		return requireNonNull(_graph.getVariableByLocalId(Ids.localIdFromParts(Ids.VARIABLE_TYPE, index))).setPrior(datum);
	}
	
	/*---------------
	 * Inner classes
	 */
	
	@ThreadSafe
	private class LocalIndexIterable implements PrimitiveIterable.OfInt
	{
		@Override
		public PrimitiveIterator.OfInt iterator()
		{
			return new LocalIndexIterator();
		}
	}
	
	private enum HasPrior implements Predicate<Variable>
	{
		INSTANCE;

		@NonNullByDefault(false)
		@Override
		public boolean apply(Variable var)
		{
			return var.getPrior() != null;
		}
	}
	
	@NotThreadSafe
	private class LocalIndexIterator implements PrimitiveIterator.OfInt
	{
		private final Iterator<Variable> _varIter =
			Iterators.filter(FactorGraphIterators.ownedVariables(_graph), HasPrior.INSTANCE);
		
		@Override
		public boolean hasNext()
		{
			return _varIter.hasNext();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("remove");
		}

		@Override
		public Integer next()
		{
			return nextInt();
		}

		@Override
		public int nextInt()
		{
			Variable var = requireNonNull(_varIter.next());
			return Ids.indexFromLocalId(var.getLocalId());
		}
	}
}
