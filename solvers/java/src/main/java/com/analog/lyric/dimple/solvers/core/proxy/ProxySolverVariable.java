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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * @since 0.05
 */
public abstract class ProxySolverVariable<Delegate extends ISolverVariable>
	extends ProxySolverNode<Delegate>
	implements IProxySolverVariable<Delegate>
{
	protected final Variable _modelVariable;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @param modelVariable
	 */
	protected ProxySolverVariable(Variable modelVariable)
	{
		_modelVariable = modelVariable;
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public Domain getDomain()
	{
		return _modelVariable.getDomain();
	}
	
	@Override
	public Variable getModelObject()
	{
		return _modelVariable;
	}
	
	@Override
	public ISolverFactor getSibling(int edge)
	{
		return Objects.requireNonNull(getModelObject().getSibling(edge).getSolver());
	}

	/*-------------------------
	 * ISolverVariable methods
	 */
	
	@Override
	public Object[] createMessages(ISolverFactor factor)
	{
		throw unsupported("createMessages");
	}

	@Override
	public void createNonEdgeSpecificState()
	{
		ISolverVariable delegate = getDelegate();
		if (delegate != null)
		{
			delegate.createNonEdgeSpecificState();
		}
	}

	@Override
	public @Nullable Object getBelief()
	{
		final ISolverVariable delegate = getDelegate();
		return delegate != null ? delegate.getBelief() : null;
	}

	@Override
	public boolean guessWasSet()
	{
		return requireDelegate("guessWasSet").guessWasSet();
	}
	
	@Override
	public Object getGuess()
	{
		return requireDelegate("getGuess").getGuess();
	}

	@Override
	public void setGuess(@Nullable Object guess)
	{
		requireDelegate("setGuess").setGuess(guess);
	}

	@Override
	public Object getValue()
	{
		return requireDelegate("getValue").getValue();
	}

	@Override
	public void moveNonEdgeSpecificState(ISolverNode other)
	{
		if (other instanceof ProxySolverNode)
		{
			other = Objects.requireNonNull(((ProxySolverNode<?>)other).getDelegate());
		}
		requireDelegate("moveNonEdgeSpecificState").moveNonEdgeSpecificState(other);
	}

	@Override
	public Object resetInputMessage(Object message)
	{
		throw unsupported("resetInputMessage");
	}

	@Override
	public Object resetOutputMessage(Object message)
	{
		throw unsupported("resetOutputMessage");
	}

	/*-------------------------
	 * ISolverVariable methods
	 */
	
	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue, boolean hasFixedValue)
	{
		ISolverVariable delegate = getDelegate();
		if (delegate != null)
		{
			delegate.setInputOrFixedValue(input, fixedValue, hasFixedValue);
		}
	}
}
