/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import java.util.HashMap;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;

/*
 * The factor info is used to build an empirical distribution over the samples
 * connected to it.
 */
public class FactorInfo extends NodeInfo
{

	//Given a variable-to-index map, create the indices of interest.
	public static int [] getVarIndices(Factor f,HashMap<Variable,Integer> var2index )
	{
		final int nVars = f.getSiblingCount();
		int [] retval = new int[nVars];
		
		for (int i = 0; i < nVars; i++)
		{
			retval[i] = var2index.get(f.getSibling(i));
		}
		
		return retval;
	}
	
	public FactorInfo(Factor f, HashMap<Variable,Integer> var2index)
	{
		super(getVarIndices(f, var2index));
	}
	
}

