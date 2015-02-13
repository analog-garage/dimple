/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model.sugar;

import java.util.ArrayDeque;
import java.util.Deque;

import com.analog.lyric.dimple.factorfunctions.Gamma;
import com.analog.lyric.dimple.factorfunctions.Log;
import com.analog.lyric.dimple.factorfunctions.LogNormal;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.Power;
import com.analog.lyric.dimple.factorfunctions.Square;
import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;


/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class ModelSyntacticSugar
{
	public static class CurrentModel implements AutoCloseable
	{
		public final FactorGraph graph;
		
		CurrentModel(FactorGraph fg)
		{
			graph = fg;
		}
		
		@Override
		public void close()
		{
			Deque<CurrentModel> stack = stateStack();
			if (stack.peek() == this)
			{
				stack.pop();
			}
		}
	}
	
	private static final ThreadLocal<Deque<CurrentModel>> _currentState = new ThreadLocal<>();
	
	private static Deque<CurrentModel> stateStack()
	{
		Deque<CurrentModel> state = _currentState.get();
		if (state == null)
		{
			_currentState.set(state = new ArrayDeque<CurrentModel>());
		}
		return state;
	}

	public static CurrentModel using(FactorGraph fg)
	{
		CurrentModel state = new CurrentModel(fg);
		stateStack().push(state);
		return state;
	}
	
	/**
	 * Current model state used by model configuration functions in this class.
	 * <p>
	 * This is set for the current thread by the {@link #using(FactorGraph)} method. The previous
	 * state is restored when the returned state's {@link CurrentModel#close()} method is called.
	 * @throws IllegalStateException if there is no current model state
	 * @see #using
	 * @since 0.08
	 */
	public static CurrentModel current()
	{
		CurrentModel model = stateStack().peek();
		if (model == null)
		{
			throw new IllegalStateException("No current Dimple model state has been set.");
		}
		return model;
	}
	
	/**
	 * The current {@link FactorGraph} used by model configuration functions in this class.
	 * <p>
	 * This is simply the {@link CurrentModel#graph} member of the {@link #current()} state.
	 * @since 0.08
	 */
	public static FactorGraph graph()
	{
		return current().graph;
	}
	
	/*------------
	 * Variables
	 */
	
	/**
	 * Set name on a model object and return it.
	 * @since 0.08
	 */
	public static <T extends Node> T name(String name, T node)
	{
		node.setName(name);
		return node;
	}
	
	/**
	 * Add a new {@link Discrete} variable with given name and domain to current {@link #graph}.
	 * @since 0.08
	 */
	public static Discrete discrete(String name, DiscreteDomain domain)
	{
		Discrete var = new Discrete(domain);
		var.setName(name);
		graph().addVariables(var);
		return var;
	}
	
	/**
	 * Add a new {@link Real} variable with given name to current {@link #graph}.
	 * @since 0.08
	 */
	public static Real real(String name)
	{
		return real(name, RealDomain.unbounded());
	}
	
	/**
	 * Add a new {@link Real} variable with given name and domain to current {@link #graph}.
	 * @since 0.08
	 */
	public static Real real(String name, RealDomain domain)
	{
		Real var = new Real(domain);
		var.setName(name);
		graph().addVariables(var);
		return var;
	}

	/**
	 * Add a new {@link Real} variable with given name and bounds to current {@link #graph}.
	 * @since 0.08
	 */
	public static Real real(String name, double lowerBound, double upperBound)
	{
		return real(name, RealDomain.create(lowerBound ,upperBound));
	}

	/*------------------
	 * Factor functions
	 */
	
	private static double toDouble(Object obj)
	{
		if (obj instanceof Number)
		{
			return ((Number)obj).doubleValue();
		}
		
		return Double.NaN;
	}
	
	public static Real gamma(Object alpha, Object beta)
	{
		Real var = new Real();
		if (alpha instanceof Number && beta instanceof Number)
		{
			graph().addFactor(new Gamma(toDouble(alpha), toDouble(beta)), var);
		}
		else
		{
			graph().addFactor(new Gamma(), alpha, beta, var);
		}
		return var;
	}
	
	private static Real _log(Object x)
	{
		Real var = new Real();
		graph().addFactor(new Log(), var, x);
		return var;
	}
	
	public static Real log(Variable x)
	{
		return _log(x);
	}

	public static Real log(Number x)
	{
		return _log(x);
	}
	
	private static Real _lognormal(Object mean, Object precision)
	{
		Real var = new Real();
		graph().addFactor(new LogNormal(), mean, precision, var);
		return var;
	}
	
	public static Real lognormal(Number mean, Number precision)
	{
		Real var = new Real();
		graph().addFactor(new LogNormal(mean.doubleValue(), precision.doubleValue()), var);
		return var;
	}
	
	public static Real lognormal(Variable mean, Variable precision)
	{
		return _lognormal(mean, precision);
	}
	
	public static Real lognormal(Variable mean, Number precision)
	{
		return _lognormal(mean, precision);
	}

	public static Real lognormal(Number mean, Variable precision)
	{
		return _lognormal(mean, precision);
	}

	public static Real normal(Object mean, Object precision)
	{
		Real var = new Real();
		if (mean instanceof Number && precision instanceof Number)
		{
			graph().addFactor(new Normal(toDouble(mean), toDouble(precision)), var);
		}
		else
		{
			graph().addFactor(new Normal(), mean, precision, var);
		}
		return var;
	}
	
	public static Real power(Object base, Object exponent)
	{
		if (toDouble(exponent) == 2.0)
		{
			return square(base);
		}

		Real var = new Real();
		graph().addFactor(new Power(), var, base, exponent);
		return var;
	}
	
	public static Real square(Object x)
	{
		Real var = new Real();
		graph().addFactor(new Square(), var, x);
		return var;
	}
	
	public static Real sum(Object x, Object y)
	{
		Real var = new Real();
		graph().addFactor(new Sum(), var, x, y);
		return var;
	}
}
