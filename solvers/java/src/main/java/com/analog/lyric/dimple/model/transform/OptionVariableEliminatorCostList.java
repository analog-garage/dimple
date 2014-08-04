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
}
