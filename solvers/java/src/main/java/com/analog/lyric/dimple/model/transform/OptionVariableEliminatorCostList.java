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

package com.analog.lyric.dimple.model.transform;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.transform.VariableEliminator.CostFunction;
import com.analog.lyric.dimple.model.transform.VariableEliminator.VariableCost;
import com.analog.lyric.options.AbstractOptionValueList;

/**
 * List of cost functions for use with {@link VariableEliminator}.
 * <p>
 * This is used in options for solvers that make use of variable elimination (e.g. junction tree).
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class OptionVariableEliminatorCostList extends AbstractOptionValueList<VariableEliminator.CostFunction>
{
	private static final long serialVersionUID = 1L;

	/**
	 * An empty list.
	 */
	public static final OptionVariableEliminatorCostList EMPTY =
		new OptionVariableEliminatorCostList(new CostFunction[0]);
	
	/*--------------
	 * Construction
	 */
	
	public OptionVariableEliminatorCostList()
	{
		super(CostFunction.class);
	}
	
	/**
	 * @param costs
	 * @since 0.07
	 */
	public OptionVariableEliminatorCostList(CostFunction ... costs)
	{
		super(CostFunction.class, costs);
	}
	
	/**
	 * Construct from list of {@link VariableCost}s.
	 * <p>
	 * @param costs
	 * @since 0.07
	 */
	public OptionVariableEliminatorCostList(VariableCost ... costs)
	{
		this(VariableCost.toFunctions(costs));
	}
	
	public static OptionVariableEliminatorCostList fromObject(@Nullable Object object)
	{
		if (object == null)
		{
			return new OptionVariableEliminatorCostList();
		}
		
		if ((object instanceof OptionVariableEliminatorCostList))
		{
			return (OptionVariableEliminatorCostList)object;
		}
		
		Class<?> valueClass = object.getClass();

		if (valueClass.isArray())
		{
			Class<?> elementType = valueClass.getComponentType();

			if (elementType == CostFunction.class)
			{
				return new OptionVariableEliminatorCostList((CostFunction[])object);
			}
			else if (elementType == VariableCost.class)
			{
				return new OptionVariableEliminatorCostList((VariableCost[])object);
			}
			else if (!elementType.isPrimitive())
			{
				Object[] values = (Object[])object;
				final int size = values.length;
				CostFunction[] costFunctions = new CostFunction[size];
				for (int i = 0; i < size; ++i)
				{
					costFunctions[i] = convertToCostFunction(values[i]);
				}
				return new OptionVariableEliminatorCostList(costFunctions);
			}
		}
		
		return new OptionVariableEliminatorCostList(convertToCostFunction(object));
	}

	private static CostFunction convertToCostFunction(Object value)
	{
		if (value instanceof VariableCost)
		{
			return ((VariableCost)value).function();
		}
		if (value instanceof String)
		{
			String name = (String)value;
			try
			{
				return VariableCost.valueOf(name).function();
			}
			catch (IllegalArgumentException ex)
			{
				// TODO: this is ugly. If we are going to support arbitrary cost functions, then
				// we should probably use a ConstructorRegistry to allow lookup of CostFunction constructors.

				// If not a VariableCost value, try constructing a class instance:
				try
				{
					return (CostFunction) Class.forName(name).newInstance();
				}
				catch (Exception ignore)
				{
					// If that doesn't work, just rethrow the original exception from
					// the VariableCost lookup.
					throw ex;
				}
			}
		}
		return (CostFunction)value;
	}

}
