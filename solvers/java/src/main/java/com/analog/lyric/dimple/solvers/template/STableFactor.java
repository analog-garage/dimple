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

package com.analog.lyric.dimple.solvers.template;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.STableFactorDoubleArray;

/**
 * The Solver Factor object is responsible for performing the message passing
 * math of the Factor.  This toy factor implementation inherits from 
 * STableFactorDoubleArray. The base class takes care of telling variables to 
 * create messages and caching the results.
 * 
 * The actual updateEdge math is simplistic and not useful since it ignores the
 * FactorTable associated  with this graph.
 * 
 * @author shershey
 *
 */
public class STableFactor  extends STableFactorDoubleArray 
{

	/**
	 * This constructor does a check to make sure all variables have the same domain
	 * size.  This shouldn't be included in most solvers but is necessary for this toy solver
	 * since this solver assumes all variable messages are the same size.
	 * 
	 * @param factor
	 */
	public STableFactor(Factor factor) 
	{
		super(factor);
		
		int firstLen = -1;
		
		for (INode sibling : _factor.getSiblings())
		{
			Discrete d = ((Discrete)sibling);
			int size = d.getDiscreteDomain().size();
			
			if (firstLen == -1)
				firstLen = size;
			else
			{
				if (size != firstLen)
					throw new DimpleException("all domains must be the same size");
			}
		}
	}

	/**
	 * The updateEdge method is the work horse of the solver.  This toy solver
	 * simply adds all the messages together (excluding the port it's updating)
	 * and ignores the FactorTable.  
	 * 
	 * Developers can also override update() if they want to speed up computation
	 * when terms can be reused.
	 */
	@Override
	public void doUpdateEdge(int outPortNum) 
	{
		double [] out = _outputMsgs[outPortNum];
		for (int i = 0; i < out.length; i++)
		{
			out[i] = 0;
		}
		
		for (int i = 0; i < _inputMsgs.length; i++)
		{
			if (outPortNum != i)
			{
				for (int j = 0; j < out.length; j++)
				{
					out[j] += _inputMsgs[i][j];
				}
			}
		}
		
	}

	/**
	 * This method is called when the factor table is retrieved.  If users want their
	 * solver to support multi-threading, they have to specify what factor table representation
	 * they want in this method so that the representation is constructed in advance of solving.
	 */
	@Override
	protected void setTableRepresentation(IFactorTable table) 
	{
		table.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT_WITH_INDICES);
	}

}
