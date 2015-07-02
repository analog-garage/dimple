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

import static java.lang.String.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;

/**
 * DataLayer holds variable data for {@link FactorGraph}s in a graph tree.
 * <p>
 * @param <D> is subclass of the {@link IDatum} interface.
 * @since 0.08
 * @author Christopher Barber
 * @see ValueDataLayer
 * @see GenericDataLayer
 */
public class DataLayer<D extends IDatum> extends DataLayerBase<Variable, D>
{
	/*--------------
	 * Construction
	 */
	
	public DataLayer(FactorGraph graph, FactorGraphData.Constructor<Variable, D> constructor)
	{
		super(graph, constructor);
	}
	
	public DataLayer(FactorGraph graph, DataDensity density, Class<D> baseType)
	{
		super(graph, density, Variable.class, baseType);
	}
	
	protected DataLayer(DataLayer<D> other)
	{
		super(other);
	}
	
	@Override
	public DataLayer<D> clone()
	{
		return new DataLayer<>(this);
	}
	
	/*-------------------
	 * DataLayer methods
	 */
	
	/**
	 * True if layer supports {@link Value} objects.
	 * <p>
	 * True if {@link Value} is a subclass of {@link #baseType()}.
	 * @since 0.08
	 */
	public boolean allowsValues()
	{
		return (baseType().isAssignableFrom(Value.class));
	}
	
	/**
	 * Associate variable with specified value for this layer.
	 * <p>
	 * This is similar to {@linkplain #put put(var, value)} if layer {@link #allowsValues allows values}
	 * and {@code value} is a member of {@code var}'s domain, this will set a corresponding {@link Value}
	 * object.
	 * <p>
	 * @param var a variable that {@link #sharesRoot shares the same root graph} as this layer.
	 * @param value is one of the following:
	 * <ul>
	 * <li>null: existing association for {@code var} in layer will be removed.
	 * <li>an instance that is compatible with the layer's {@link #baseType}.
	 * <li>a member of {@code var}'s domain that will be used to set a corresponding {@link Value}
	 * </ul>
	 * @since 0.08
	 */
	public void set(Variable var, @Nullable Object value)
	{
		if (value == null)
		{
			remove(var);
			return;
		}
		else if (value instanceof IDatum)
		{
			put(var, baseType().cast(value));
			return;
		}
		else if (allowsValues())
		{
			final Domain domain = var.getDomain();
			if (domain.inDomain(value))
			{
				IDatum cur = get(var);
				if (cur instanceof Value)
				{
					Value curValue = (Value)cur;
					if (curValue.isMutable())
					{
						((Value) cur).setObject(value);
						return;
					}
				}
				put(var, baseType().cast(Value.create(domain, value)));
				return;
			}
		}

		throw new ClassCastException(format("'%s' is not a %s or member of domain %s",
			value, baseType().getSimpleName(), var.getDomain()));
	}
}
