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

package com.analog.lyric.dimple.solvers.core.proxy;

import java.util.Objects;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * @since 0.05
 */
public abstract class ProxySolverFactor<Delegate extends ISolverFactor>
	extends ProxySolverNode<Delegate> implements ISolverFactor
{
	protected final Factor _modelFactor;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @param modelFactor
	 */
	protected ProxySolverFactor(Factor modelFactor)
	{
		_modelFactor = modelFactor;
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public Factor getModelObject()
	{
		return _modelFactor;
	}
	
	@Override
	public ISolverVariable getSibling(int edge)
	{
		return Objects.requireNonNull(getModelObject().getSibling(edge).getSolver());
	}

	/*-----------------------
	 * ISolverFactor methods
	 */
	
	@Override
	public Object getBelief()
	{
		return requireDelegate("getBelief").getBelief();
	}

	@Override
	public void createMessages()
	{
		ISolverFactor delegate = getDelegate();
		if (delegate != null)
		{
			delegate.createMessages();
		}
	}

	@Override
	public void moveMessages(ISolverNode other)
	{
		if (other instanceof ProxySolverNode)
		{
			other = Objects.requireNonNull(((ProxySolverNode<?>)other).getDelegate());
		}
		requireDelegate("moveMessages").moveMessages(other);
	}

	@Override
	public int[][] getPossibleBeliefIndices()
	{
		throw unsupported("getPossibleBeliefIndices");
	}

	@Override
	public void setDirectedTo(int[] indices)
	{
		throw unsupported("setDirectedTo");
	}
}
