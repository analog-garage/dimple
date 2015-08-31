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

package com.analog.lyric.dimple.model.core;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.variables.Variable;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
final class OwnedVariables extends OwnedArray<Variable>
{
	/*--------------
	 * Construction
	 */
	
	OwnedVariables()
	{
		super();
	}

	/*
	 * Methods
	 */
	
	@Override
	int idTypeMask()
	{
		return Ids.VARIABLE_TYPE << Ids.LOCAL_ID_TYPE_OFFSET;
	}
	
	@Override
	void renumberNode(Variable var, int newIndex)
	{
		super.renumberNode(var, newIndex);
		
		for (EdgeState edge : var.getSiblingEdgeState())
		{
			if (edge.isLocal())
			{
				// Only update local edges because the variable index of boundary
				// uses the boundary variable index.
				edge.setVariableIndex(newIndex);
			}
		}
	}
	
	@Override
	Variable[] resize(@Nullable Variable[] array, int length)
	{
		final Variable[] newArray = new Variable[length];
		if (array != null)
		{
			System.arraycopy(array, 0, newArray, 0, Math.min(length, array.length));
		}
		return newArray;
	}
}
