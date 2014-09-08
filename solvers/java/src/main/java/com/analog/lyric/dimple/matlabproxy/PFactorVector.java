/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.matlabproxy;

import java.util.ArrayList;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.util.misc.Matlab;

@Matlab
public class PFactorVector extends PNodeVector
{
	/*--------------
	 * Construction
	 */
	
	public PFactorVector()
	{
	}
	
	public PFactorVector(Factor f)
	{
		this(new Node[] {f});
	}
	public PFactorVector(Node [] nodes)
	{
		super(nodes);
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public boolean isFactor()
	{
		return true;
	}

	/*---------------------
	 * PNodeVector methods
	 */

	@Override
	public PNodeVector createNodeVector(Node[] nodes)
	{
		return new PFactorVector(nodes);
	}

	/*-----------------------
	 * PFactorVector methods
	 */
	
	private Factor getFactor(int index)
	{
		return (Factor)getModelerNode(index);
	}
	
	public FactorFunction getFactorFunction()
	{
		return getFactor(0).getFactorFunction();
	}
	
	public PVariableVector getVariables()
	{
		ArrayList<Variable> retval = new ArrayList<Variable>();
		
		for (Node v : getModelerNodes())
		{
			final Factor f = v.asFactor();
			if (f != null)
			{
				for (int i = 0, nVars = f.getSiblingCount(); i < nVars; ++i)
					retval.add(f.getSibling(i));
			}
		}
		
		Variable [] realRetVal = new Variable[retval.size()];
		retval.toArray(realRetVal);
		return PHelpers.convertToVariableVector(realRetVal);
		
		//return PHelpers.
	}

	private Factor getFactor()
	{
		if (size() != 1)
			throw new DimpleException("only works with a single factor for now");
		return getFactor(0);
	}

	public PVariableVector getDirectedToVariables()
	{
		VariableList vl = getFactor().getDirectedToVariables();
		return PHelpers.convertToVariableVector(vl);
	}
	
	public void setDirectedTo(Object [] vars, Object [] indices)
	{
		int[][][] intIndices;
		if (size() > 1)
			intIndices = PHelpers.extractIndicesVectorized(indices);		// Vectorized factor, second index dimension is vectorized dimension
		else
			intIndices = PHelpers.extractIndicesNonVectorized(indices);		// Single factor
		PNodeVector [] vec = PHelpers.convertObjectArrayToNodeVectorArray(vars);
		PNodeVector[][] nodeVectors = PHelpers.extractVectorization(vec, intIndices);
		
		for (int i = 0; i < nodeVectors.length; i++)
		{
			Factor f = getFactor(i);
			
			PNodeVector[] nodeVectorsi = nodeVectors[i];
			int vlsize = nodeVectorsi.length;
			VariableList vl = new VariableList(vlsize);
				
			for (int j = 0; j < vlsize; j++)
			{
				Variable [] tmp = ((PVariableVector)nodeVectorsi[j]).getVariableArray();
				vl.addAll(tmp);
			}
			f.setDirectedTo(vl);
		}
		
	}
	

}
