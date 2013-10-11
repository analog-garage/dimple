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

import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.util.misc.Matlab;

@Matlab
public class PDiscreteFactorVector extends PFactorVector
{
	public PDiscreteFactorVector(Node [] nodes)
	{
		super(nodes);
	}
	public PDiscreteFactorVector(DiscreteFactor f)
	{
		super(f);
	}

	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public boolean isDiscrete()
	{
		return true;
	}
	
	/*---------------------
	 * PNodeVector methods
	 */

	@Override
	public PNodeVector createNodeVector(Node[] nodes)
	{
		return new PDiscreteFactorVector(nodes);
	}

	/*-------------------------------
	 * PDiscreteFactorVector methods
	 */
	
	private DiscreteFactor getDiscreteFactor(int index)
	{
		return (DiscreteFactor)getModelerNode(0);
	}
	
	public PFactorTable getFactorTable()
	{
		return new PFactorTable(getDiscreteFactor(0).getFactorTable());
	}
	
	public double [][] getDiscreteBeliefs(int [] indices)
	{
		double [][] beliefs = new double[indices.length][];
		
		for (int i = 0; i < indices.length; i++)
		{
			DiscreteFactor df = getDiscreteFactor(indices[i]);
			beliefs[i] = df.getBelief();
		}
		return beliefs;
		
	}
	
	public Object [] getBeliefs(int [] indices)
	{
		Object [] beliefs = new Object[indices.length];
		
		for (int i = 0; i < indices.length; i++)
		{
			DiscreteFactor df = getDiscreteFactor(indices[i]);
			beliefs[i] = df.getBelief();
		}
		return beliefs;
	}

	public int[][] getPossibleBeliefIndices(int index)
	{
		return getDiscreteFactor(index).getPossibleBeliefIndices();
	}

}
