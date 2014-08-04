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

import com.analog.lyric.dimple.model.transform.VariableEliminator.CostFunction;
import com.analog.lyric.dimple.model.transform.VariableEliminator.VariableCost;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.OptionKey;

/**
 * Key for options with list of variable eliminator cost functions.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class VariableEliminatorCostListOptionKey extends OptionKey<OptionVariableEliminatorCostList>
{
	private static final long serialVersionUID = 1L;

	private final OptionVariableEliminatorCostList _defaultValue;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * 
	 * @since 0.07
	 */
	public VariableEliminatorCostListOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, OptionVariableEliminatorCostList.EMPTY);
	}

	public VariableEliminatorCostListOptionKey(Class<?> declaringClass, String name,
		OptionVariableEliminatorCostList defaultValue)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
	}

	public VariableEliminatorCostListOptionKey(Class<?> declaringClass, String name, CostFunction ... costFunctions)
	{
		this(declaringClass, name, new OptionVariableEliminatorCostList(costFunctions));
	}

	public VariableEliminatorCostListOptionKey(Class<?> declaringClass, String name, VariableCost ... costFunctions)
	{
		this(declaringClass, name, new OptionVariableEliminatorCostList(costFunctions));
	}
	
	/*--------------------
	 * IOptionKey methods
	 */
	
	/**
	 * Converts value to cost list representation.
	 * <p>
	 * Apart from {@link OptionVariableEliminatorCostList} itself, supports the following types of input:
	 * <ul>
	 * <li>{@link CostFunction}
	 * <li>{@link VariableCost} - value taken from its {@linkplain VariableCost#function() function()} method.
	 * <li>{@link String} matching name of {@link VariableCost} enum instance.
	 * <li>An array of any of the above.
	 * </ul>
	 */
	@Override
	public OptionVariableEliminatorCostList convertValue(Object value)
	{
		if (!(value instanceof OptionVariableEliminatorCostList))
		{
			Class<?> valueClass = value.getClass();

			if (valueClass.isArray())
			{
				Class<?> elementType = valueClass.getComponentType();

				if (elementType == CostFunction.class)
				{
					return new OptionVariableEliminatorCostList((CostFunction[])value);
				}
				else if (elementType == VariableCost.class)
				{
					return new OptionVariableEliminatorCostList((VariableCost[])value);
				}
				else if (!elementType.isPrimitive())
				{
					Object[] values = (Object[])value;
					final int size = values.length;
					CostFunction[] costFunctions = new CostFunction[size];
					for (int i = 0; i < size; ++i)
					{
						costFunctions[i] = convertToCostFunction(values[i]);
					}
					return new OptionVariableEliminatorCostList(costFunctions);
				}
			}
			else
			{
				return new OptionVariableEliminatorCostList(convertToCostFunction(value));
			}
			
		}
		
		return (OptionVariableEliminatorCostList)value;
	}
	
	private static CostFunction convertToCostFunction(Object value)
	{
		if (value instanceof VariableCost)
		{
			return ((VariableCost)value).function();
		}
		if (value instanceof String)
		{
			return VariableCost.valueOf((String)value).function();
		}
		return (CostFunction)value;
	}
	
	@Override
	public Class<OptionVariableEliminatorCostList> type()
	{
		return OptionVariableEliminatorCostList.class;
	}
	
	@Override
	public OptionVariableEliminatorCostList defaultValue()
	{
		return _defaultValue;
	}
	
	/*-------------
	 * New methods
	 */
	
	public void set(IOptionHolder holder, CostFunction ... costFunctions)
	{
		set(holder, new OptionVariableEliminatorCostList(costFunctions));
	}
	
	public void set(IOptionHolder holder, VariableCost ... costFunctions)
	{
		set(holder, new OptionVariableEliminatorCostList(costFunctions));
	}
	
}
