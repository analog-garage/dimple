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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.events.SolverEventSource;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.IOptions;
import com.analog.lyric.options.Options;

/**
 * @since 0.0.5
 */
@NotThreadSafe
public abstract class ProxySolverNode extends SolverEventSource implements ISolverNode
{
	/*-------
	 * State
	 */
	
	private ConcurrentMap<IOptionKey<?>,Object> _localOptions = null;
	
	/*--------------
	 * Construction
	 */
	
	protected ProxySolverNode()
	{
	}
	
	/*-----------------------
	 * IOptionHolder methods
	 */
	
	@Override
	public void clearLocalOptions()
	{
		_localOptions = null;
		final ISolverNode delegate = getDelegate();
		if (delegate != null)
		{
			delegate.clearLocalOptions();
		}
	}

	@Override
	public ConcurrentMap<IOptionKey<?>, Object> getLocalOptions(boolean create)
	{
		ConcurrentMap<IOptionKey<?>,Object> localOptions = null;
		final ISolverNode delegate = getDelegate();
		
		if (delegate == null)
		{
			if (create && _localOptions == null)
			{
				_localOptions = new ConcurrentHashMap<IOptionKey<?>,Object>();
			}
			localOptions = _localOptions;
		}
		else if (_localOptions == null)
		{
			localOptions = delegate.getLocalOptions(create);
		}
		else
		{
			localOptions = delegate.getLocalOptions(create||!_localOptions.isEmpty());
			localOptions.putAll(_localOptions);
			_localOptions = null;
		}
		
		return localOptions;
	}

	@Override
	public ISolverFactorGraph getOptionParent()
	{
		return getParentGraph();
	}

	@Override
	public Set<IOptionKey<?>> getRelevantOptionKeys()
	{
		final ISolverNode delegate = getDelegate();
		return delegate != null ? delegate.getRelevantOptionKeys() : Collections.EMPTY_SET;
	}

	@Override
	public IOptions options()
	{
		return new Options(this);
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public double getBetheEntropy()
	{
		return getDelegate().getBetheEntropy();
	}

	@Override
	public double getInternalEnergy()
	{
		return getDelegate().getInternalEnergy();
	}

	@Override
	public Object getInputMsg(int portIndex)
	{
		throw unsupported("getInputMsg");
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		throw unsupported("getOutputMsg");
	}

	@Override
	public ISolverFactorGraph getParentGraph()
	{
		return getModelObject().getParentGraph().getSolver();
	}

	@Override
	public ISolverFactorGraph getRootGraph()
	{
		return getModelObject().getRootGraph().getSolver();
	}

	@Override
	public double getScore()
	{
		return getDelegate().getScore();
	}

	@Override
	public void initialize()
	{
		clearFlags();
		getDelegate().initialize();
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		throw unsupported("moveMessages");
	}

	@Override
	public void resetEdgeMessages(int portNum)
	{
		throw unsupported("resetEdgeMessages");
	}

	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		throw unsupported("setInputMsg");
	}

	@Override
	public void setOutputMsg(int portIndex, Object obj)
	{
		throw unsupported("setOutputMsg");
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		throw unsupported("setInputMsgValues");
	}

	@Override
	public void setOutputMsgValues(int portIndex, Object obj)
	{
		throw unsupported("setOutputMsgValues");
	}

	@Override
	public void update()
	{
		getDelegate().update();
	}

	@Override
	public void updateEdge(int outPortNum)
	{
		throw unsupported("updateEdge");
	}

	/*---------------
	 * Local methods
	 */
	
	public abstract ISolverNode getDelegate();

	protected RuntimeException unsupported(String method)
	{
		return DimpleException.unsupportedMethod(getClass(), method,
			"Not supported for proxy solver because graph topology may be different.");
	}
}
