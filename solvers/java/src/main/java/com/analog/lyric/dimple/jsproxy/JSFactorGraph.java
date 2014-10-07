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

package com.analog.lyric.dimple.jsproxy;

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class JSFactorGraph extends JSNode<FactorGraph>
{
	/*-------
	 * State
	 */
	
	/**
	 * The applet that created this graph.
	 */
	private final DimpleApplet _applet;
	
	/**
	 * Cache of proxy objects owned by this graph.
	 */
	private final Cache<Object, JSNode<?>> _proxyCache;
	
	/*--------------
	 * Construction
	 */
	
	JSFactorGraph(DimpleApplet applet, FactorGraph graph)
	{
		super(null, graph);
		_applet = applet;
		_proxyCache = CacheBuilder.newBuilder().build();
		_proxyCache.put(graph, this);
	}

	/*-----------------------
	 * JSProxyObject methods
	 */
	
	@Override
	public DimpleApplet getApplet()
	{
		return _applet;
	}
	
	/*-------------
	 * JSProxyNode
	 */
	
	@Override
	public JSFactorGraph getGraph()
	{
		return this;
	}
	
	@Override
	public JSNode.Type getNodeType()
	{
		return JSNode.Type.GRAPH;
	}
	
	/*----------------------
	 * JSFactorGraph methods
	 */
	
	public JSFactor addFactor(JSFactorFunction function, JSVariable ... args)
	{
		Object[] unwrapped = new Object[args.length];
		for (int i = args.length; --i>=0;)
		{
			unwrapped[i] = args[i].getDelegate();
		}
		
		return wrap(_delegate.addFactor(function._delegate, unwrapped));
	}
	
	public JSFactor addFactor(JSFactorFunction function, JSVariable arg1)
	{
		return addFactor(function, new JSVariable[] { arg1 });
	}

	public JSFactor addFactor(JSFactorFunction function, JSVariable arg1, JSVariable arg2)
	{
		return addFactor(function, new JSVariable[] { arg1, arg2 });
	}

	public JSFactor addFactor(JSFactorFunction function, JSVariable arg1, JSVariable arg2, JSVariable arg3)
	{
		return addFactor(function, new JSVariable[] { arg1, arg2, arg3 });
	}

	public JSFactor addFactor(String functionName, JSVariable ... args)
	{
		return addFactor(_applet.functions.get(functionName), args);
	}
	
	public JSFactor addFactor(String function, JSVariable arg1)
	{
		return addFactor(function, new JSVariable[] { arg1 });
	}

	public JSFactor addFactor(String function, JSVariable arg1, JSVariable arg2)
	{
		return addFactor(function, new JSVariable[] { arg1, arg2 });
	}

	public JSFactor addFactor(String function, JSVariable arg1, JSVariable arg2, JSVariable arg3)
	{
		return addFactor(function, new JSVariable[] { arg1, arg2, arg3 });
	}

	public JSVariable addVariable(String name, JSDomain<?> domain)
	{
		Variable variable = null;
		
		switch (domain.getDomainType())
		{
		case DISCRETE:
			variable = new Discrete(requireNonNull(domain._delegate.asDiscrete()));
			break;
		case REAL:
			variable = new Real(requireNonNull(domain._delegate.asReal()));
			break;
		case REAL_JOINT:
			variable = new RealJoint(requireNonNull(domain._delegate.asRealJoint()));
			break;
		}
		
		requireNonNull(variable).setName(name);
		_delegate.addVariables(variable);
		
		return wrap(variable);
	}
	
	public @Nullable JSSolver getSolver()
	{
		IFactorGraphFactory<?> solver = _delegate.getFactorGraphFactory();
		return solver != null ? new JSSolver(getApplet(), solver) : null;
	}
	
	public @Nullable JSVariable getVariable(int id)
	{
		Variable variable = _delegate.getVariable(id);
		return variable != null ? wrap(variable) : null;
	}
	
	public @Nullable JSVariable getVariable(String name)
	{
		Variable variable = _delegate.getVariableByName(name);
		return variable != null ? wrap(variable) : null;
	}
	
	public void initialize()
	{
		_delegate.initialize();
	}
	
	public void setSolver(String solver)
	{
		setSolver(new JSSolver(_applet, solver));
	}
	
	public void setSolver(JSSolver solver)
	{
		_delegate.setSolverFactory(solver.getDelegate());
	}

	public void solve()
	{
		_delegate.solve();
	}

	public void solveOneStep()
	{
		_delegate.solveOneStep();
	}
	
	/*-----------------
	 * Internal methods
	 */
	
	@SuppressWarnings("unchecked")
	JSNode<?> wrap(INode node)
	{
		JSNode<?> jsnode = _proxyCache.getIfPresent(node);
		if (jsnode == null)
		{
			switch (node.getNodeType())
			{
			case FACTOR:
				jsnode = new JSFactor(this, (Factor)node);
				break;
			case GRAPH:
				jsnode = new JSFactorGraph(_applet, (FactorGraph)node);
				break;
			case VARIABLE:
				jsnode = new JSVariable(this, (Variable)node);
				break;
			}
		}
		return requireNonNull(jsnode);
	}
	
	JSFactor wrap(Factor factor)
	{
		JSFactor jsfactor = (JSFactor)_proxyCache.getIfPresent(factor);
		if (jsfactor == null)
		{
			jsfactor = new JSFactor(this, factor);
			_proxyCache.put(factor, jsfactor);
		}
		return jsfactor;
	}

	JSVariable wrap(Variable variable)
	{
		JSVariable jsvariable = (JSVariable)_proxyCache.getIfPresent(variable);
		if (jsvariable == null)
		{
			jsvariable = new JSVariable(this, variable);
			_proxyCache.put(variable, jsvariable);
		}
		return jsvariable;
	}
}
