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

import static java.util.Objects.*;

import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.FiniteFieldNumber;
import com.analog.lyric.dimple.model.variables.FiniteFieldVariable;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.util.misc.Matlab;



@Matlab
public class PFiniteFieldVariableVector extends PDiscreteVariableVector
{
	/*---------------
	 * Construction
	 */
	
	public PFiniteFieldVariableVector(Node node)
	{
		super(new Node[] {node});
	}
	
	public PFiniteFieldVariableVector(Node[] nodes)
	{
		super(nodes);
	}
	
	public PFiniteFieldVariableVector(PFiniteFieldDomain domain, int numElements)
	{
		Node[] nodes  = new Node[numElements];
		
		for (int i = 0; i < numElements; i++)
		{
			FiniteFieldVariable v = new FiniteFieldVariable(domain.getModelerObject());
			nodes[i] = v;
		}
		
		setNodes(nodes);
	}
	
	public PFiniteFieldVariableVector(Variable [] variables)
	{
		super(variables);
	}
	
	/*-----------------
	 * PObject methods
	 */

	@Override
	public PNodeVector createNodeVector(Node [] nodes)
	{
		return new PFiniteFieldVariableVector(nodes);
	}
	
	@Override
	public PDomain getDomain()
	{
		return new PFiniteFieldDomain(getFiniteFieldVariable(0).getFiniteFieldDomain());
	}
	
	/*-----------------
	 * PVariableVector methods
	 */

	@Override
	public Object [] getGuess()
	{
		Object[] retval = new Object[size()];
		Variable [] vars = getVariableArray();
		for (int i = 0; i < vars.length; i++)
		{
			retval[i] = ((FiniteFieldNumber)requireNonNull(vars[i].getGuess())).intValue();
		}
		return retval;
	}

	/*-----------------
	 * PFiniteFieldVariableVector methods
	 */

	private FiniteFieldVariable getFiniteFieldVariable(int index)
	{
		return (FiniteFieldVariable)getModelerNode(index);
	}

}
