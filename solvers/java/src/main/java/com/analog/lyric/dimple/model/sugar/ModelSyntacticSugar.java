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

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import com.analog.lyric.dimple.factorfunctions.ACos;
import com.analog.lyric.dimple.factorfunctions.ASin;
import com.analog.lyric.dimple.factorfunctions.ATan;
import com.analog.lyric.dimple.factorfunctions.Abs;
import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.Beta;
import com.analog.lyric.dimple.factorfunctions.Binomial;
import com.analog.lyric.dimple.factorfunctions.ComplexAbs;
import com.analog.lyric.dimple.factorfunctions.ComplexExp;
import com.analog.lyric.dimple.factorfunctions.ComplexNegate;
import com.analog.lyric.dimple.factorfunctions.ComplexProduct;
import com.analog.lyric.dimple.factorfunctions.ComplexSum;
import com.analog.lyric.dimple.factorfunctions.ConstantPower;
import com.analog.lyric.dimple.factorfunctions.ConstantProduct;
import com.analog.lyric.dimple.factorfunctions.Cos;
import com.analog.lyric.dimple.factorfunctions.Cosh;
import com.analog.lyric.dimple.factorfunctions.Exp;
import com.analog.lyric.dimple.factorfunctions.Gamma;
import com.analog.lyric.dimple.factorfunctions.InverseGamma;
import com.analog.lyric.dimple.factorfunctions.Log;
import com.analog.lyric.dimple.factorfunctions.LogNormal;
import com.analog.lyric.dimple.factorfunctions.Negate;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.Power;
import com.analog.lyric.dimple.factorfunctions.Product;
import com.analog.lyric.dimple.factorfunctions.Sin;
import com.analog.lyric.dimple.factorfunctions.Sqrt;
import com.analog.lyric.dimple.factorfunctions.Square;
import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.factorfunctions.Tan;
import com.analog.lyric.dimple.factorfunctions.Tanh;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.ComplexDomain;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Complex;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;


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
	
	/*-------
	 * Nodes
	 */

	/**
	 * Set label on a model object and return it.
	 * @since 0.08
	 */
	public static <T extends Node> T label(String label, T node)
	{
		node.setLabel(label);
		return node;
	}
	
	/**
	 * Set label for all nodes in array and return.
	 * <p>
	 * @param labelPrefix is a prefix to which the array index will be added to form the name.
	 * <p>
	 * @since 0.08
	 */
	public static <T extends Node> T[] label(String labelPrefix, T[] nodes)
	{
		for (int i = nodes.length; --i>=0;)
		{
			nodes[i].setLabel(labelPrefix + i);
		}
		return nodes;
	}
	
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
	 * Set name for all nodes in array and return.
	 * <p>
	 * @param namePrefix is a prefix to which the array index will be added to form the name.
	 * <p>
	 * @since 0.08
	 */
	public static <T extends Node> T[] name(String namePrefix, T[] nodes)
	{
		for (int i = nodes.length; --i>=0;)
		{
			nodes[i].setName(namePrefix + i);
		}
		return nodes;
	}
	
	/*------------
	 * Variables
	 */
	
	/**
	 * Adds a new variable block containing the specified variables.
	 * @since 0.08
	 */
	@SafeVarargs
	public static VariableBlock block(Variable ... vars)
	{
		return graph().addVariableBlock(vars);
	}
	
	/**
	 * Adds variable as boundary variable to current {@link #graph}.
	 * @param var
	 * @return {@code var}
	 * @since 0.08
	 */
	public static <V extends Variable> V boundary(V var)
	{
		graph().addBoundaryVariables(var);
		return var;
	}
	
	/**
	 * Add a new {@link Discrete} variable with given name and domain to current {@link #graph}.
	 * @since 0.08
	 */
	public static Discrete discrete(String name, DiscreteDomain domain)
	{
		return nameAndAdd(name, new Discrete(domain));
	}
	
	public static Discrete[] discretes(String namePrefix, DiscreteDomain domain, int n)
	{
		final Discrete[] vars = new Discrete[n];
		for (int i = 0; i < n; ++i)
		{
			vars[i] = discrete(namePrefix + i, domain);
		}
		return vars;
	}
	
	public static Real fixed(String name, double value)
	{
		Real var = nameAndAdd(name, new Real());
		var.setFixedValue(value);
		return var;
	}
	
	public static Real[] fixed(String name, double ... values)
	{
		final int size = values.length;
		Real[] vars = new Real[size];
		for (int i = 0; i < size; ++i)
		{
			vars[i] = fixed(name + i, values[i]);
		}
		return vars;
	}
	
	public static RealJoint fixedJoint(String name, double ... value)
	{
		RealJoint var = realjoint(name, value.length);
		var.setFixedValue(value);
		return var;
	}
	
	public static Discrete fixed(String name, DiscreteDomain domain, Object value)
	{
		Discrete var = discrete(name, domain);
		var.setFixedValue(value);
		return var;
	}
	
	public static Complex complex(String name)
	{
		return complex(name, ComplexDomain.create());
	}
	
	public static Complex complex(String name, ComplexDomain domain)
	{
		return nameAndAdd(name, new Complex(domain));
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

	public static Real[] reals(String namePrefix, int size, RealDomain domain)
	{
		Real[] vars = new Real[size];
		for (int i = 0; i < size; ++i)
		{
			vars[i] = nameAndAdd(namePrefix + i, new Real(domain));
		}
		return vars;
	}
	
	public static Real[] reals(String namePrefix, int size)
	{
		return reals(namePrefix, size, RealDomain.unbounded());
	}

	public static RealJoint realjoint(String name, RealJointDomain domain)
	{
		return nameAndAdd(name, new RealJoint(domain));
	}
	
	public static RealJoint realjoint(String name, int size)
	{
		return realjoint(name, RealJointDomain.create(size));
	}
	
	/*---------
	 * Factors
	 */
	
	public static Factor addFactor(FactorFunction function, Object ... args)
	{
		int argCount = args.length;
		
		for (Object arg : args)
		{
			Class<?> argType = arg.getClass();
			if (argType.isArray() && Variable.class.isAssignableFrom(argType.getComponentType()))
			{
				argCount += Array.getLength(arg) - 1;
			}
		}
		
		Object[] expandedArgs = args;
		
		if (argCount > args.length)
		{
			expandedArgs = new Object[argCount];
			int i = 0;
			for (Object arg : args)
			{
				Class<?> argType = arg.getClass();
				if (argType.isArray() && Variable.class.isAssignableFrom(argType.getComponentType()))
				{
					Variable[] vars = (Variable[])arg;
					for (Variable var : vars)
					{
						expandedArgs[i++] = var;
					}
				}
				else
				{
					expandedArgs[i++] = arg;
				}
			}
			args = expandedArgs;
		}
		
		return graph().addFactor(function, expandedArgs);
	}
	
	/*------------------
	 * Factor functions
	 */
	
	public static Real abs(Real x)
	{
		return addFactorWithRealFirst(new Abs(), x);
	}
	
	public static Complex abs(Complex x)
	{
		return addFactorWithComplexFirst(new ComplexAbs(), x);
	}
	
	public static Real acos(Real x)
	{
		return addFactorWithRealFirst(new ACos(), x);
	}
	
	public static Real asin(Real x)
	{
		return addFactorWithRealFirst(new ASin(), x);
	}
	
	public static Real atan(Real x)
	{
		return addFactorWithRealFirst(new ATan(), x);
	}
	
	public static Bit bernoulli(Object p)
	{
		Bit bit = new Bit();
		
		if (p instanceof Number)
		{
			graph().addFactor(new Bernoulli(toDouble(p)), bit);
		}
		else
		{
			graph().addFactor(new Bernoulli(), p, bit);
		}
		
		return bit;
	}
	
	public static Real beta(Object alpha, Object beta)
	{
		if (alpha instanceof Number && beta instanceof Number)
		{
			return addFactorWithRealLast(new Beta(toDouble(alpha), toDouble(beta)));
		}
		else
		{
			return addFactorWithRealLast(new Beta(), alpha, beta);
		}
	}
	
	public static Discrete binomial(int N, Object p)
	{
		Discrete var = new Discrete(DiscreteDomain.range(0, N));
		graph().addFactor(new Binomial(N), p, var);
		return var;
	}
	
	public static Real binomial(Variable N, Object p)
	{
		return addFactorWithRealLast(new Binomial(), N, p);
	}
	
	public static Real cos(Real x)
	{
		return addFactorWithRealFirst(new Cos(), x);
	}
	
	public static Real cosh(Real x)
	{
		return addFactorWithRealFirst(new Cosh(), x);
	}
	
	public static Real exp(Real x)
	{
		return addFactorWithRealFirst(new Exp(), x);
	}
	
	public static Complex exp(Complex x)
	{
		return addFactorWithComplexFirst(new ComplexExp(), x);
	}
	
	public static Real gamma(Object alpha, Object beta)
	{
		if (alpha instanceof Number && beta instanceof Number)
		{
			return addFactorWithRealLast(new Gamma(toDouble(alpha), toDouble(beta)));
		}
		else
		{
			return addFactorWithRealLast(new Gamma(), alpha, beta);
		}
	}
	
	public static Real inversegamma(Object alpha, Object beta)
	{
		if (alpha instanceof Number && beta instanceof Number)
		{
			return addFactorWithRealLast(new InverseGamma(toDouble(alpha), toDouble(beta)));
		}
		else
		{
			return addFactorWithRealLast(new InverseGamma(), alpha, beta);
		}
	}
	
	public static Real log(Real x)
	{
		return addFactorWithRealFirst(new Log(), x);
	}
	
	public static Real lognormal(Object mean, Object precision)
	{
		if (mean instanceof Number && precision instanceof Number)
		{
			return addFactorWithRealLast(new LogNormal(toDouble(mean), toDouble(precision)));
		}
		else
		{
			return addFactorWithRealLast(new LogNormal(), mean, precision);
		}
	}
	
	public static Real negate(Real x)
	{
		return addFactorWithRealFirst(new Negate(), x);
	}

	public static Complex negate(Complex x)
	{
		return addFactorWithComplexFirst(new ComplexNegate(), x);
	}

	public static Real normal(Object mean, Object precision)
	{
		if (mean instanceof Number && precision instanceof Number)
		{
			return addFactorWithRealLast(new Normal(toDouble(mean), toDouble(precision)));
		}
		else
		{
			return addFactorWithRealLast(new Normal(), mean, precision);
		}
	}
	
	public static void normal(Object mean, Object precision, Real ... vars)
	{
		if (mean instanceof Number && precision instanceof Number)
		{
			graph().addFactor(new Normal(toDouble(mean), toDouble(precision)), vars);
		}
		else
			
		{
			graph().addFactor(new Normal(), mean, precision, vars);
		}
	}
	
	public static Real power(Real base, double exponent)
	{
		if (exponent == 2.0)
		{
			return square(base);
		}

		return addFactorWithRealFirst(new ConstantPower(exponent), base);
	}
	
	public static Real power(Real base, Real exponent)
	{
		return addFactorWithRealFirst(new Power(), base, exponent);
	}
	
	public static Real product(Real x, Real y)
	{
		return addFactorWithRealFirst(new Product(), x, y);
	}
	
	public static Real product(Real x, double y)
	{
		return addFactorWithRealFirst(new ConstantProduct(y), x);
	}
	
	public static Real product(double x, Real y)
	{
		return addFactorWithRealFirst(new ConstantProduct(x), y);
	}
	
	public static Complex product(Complex x, Variable y)
	{
		return addFactorWithComplexFirst(new ComplexProduct(), x, y);
	}
	
	public static Complex product(Variable x, Complex y)
	{
		return addFactorWithComplexFirst(new ComplexProduct(), x, y);
	}

	public static Real square(Real x)
	{
		return addFactorWithRealFirst(new Square(), x);
	}
	
	public static Real sin(Real x)
	{
		return addFactorWithRealFirst(new Sin(), x);
	}
	
	public static Real sinh(Real x)
	{
		return addFactorWithRealFirst(new Sin(), x);
	}
	
	public static Real sqrt(Real x)
	{
		return addFactorWithRealFirst(new Sqrt(), x);
	}
	
	public static Real sum(Real x, Real y)
	{
		return addFactorWithRealFirst(new Sum(), x, y);
	}
	
//	public static Real sum(Real ... vars)
//	{
//		return addFactorWithRealFirstOutput(new Sum(), vars);
//	}
	
	public static Real sum(Real x, double y)
	{
		return addFactorWithRealFirst(new Sum(), x, y);
	}

	public static Real sum(double x, Real y)
	{
		return addFactorWithRealFirst(new Sum(), x, y);
	}
	
	public static Complex sum(Complex x, Variable y)
	{
		return addFactorWithComplexFirst(new ComplexSum(), x, y);
	}
	
	public static Complex sum(Variable x, Complex y)
	{
		return addFactorWithComplexFirst(new ComplexSum(), x, y);
	}

	public static Real tan(Real x)
	{
		return addFactorWithRealFirst(new Tan(), x);
	}
	
	public static Real tanh(Real x)
	{
		return addFactorWithRealFirst(new Tanh(), x);
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private static <V extends Variable> V nameAndAdd(String name, V var)
	{
		graph().addVariables(var);
		var.setName(name);
		return var;
	}
	
	private static Real addFactorWithRealFirst(FactorFunction function, Object ... args)
	{
		Real var = new Real();
		Object[] expandedArgs = new Object[args.length + 1];
		expandedArgs[0] = var;
		System.arraycopy(args, 0, expandedArgs, 1, args.length);
		graph().addFactor(function, expandedArgs);
		return var;
	}
	
	private static Real addFactorWithRealLast(FactorFunction function, Object ... args)
	{
		Real var = new Real();
		Object[] expandedArgs = Arrays.copyOf(args, args.length + 1);
		expandedArgs[args.length] = var;
		graph().addFactor(function, expandedArgs);
		return var;
	}
	
	private static Complex addFactorWithComplexFirst(FactorFunction function, Object ... args)
	{
		Complex var = new Complex();
		Object[] expandedArgs = new Object[args.length + 1];
		expandedArgs[0] = var;
		System.arraycopy(args, 0, expandedArgs, 1, args.length);
		graph().addFactor(function, expandedArgs);
		return var;
	}
	
	private static double toDouble(Object obj)
	{
		if (obj instanceof Number)
		{
			return ((Number)obj).doubleValue();
		}
		
		return Double.NaN;
	}
	
}
