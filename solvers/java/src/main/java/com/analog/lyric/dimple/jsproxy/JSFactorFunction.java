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

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;

/**
 * Javascript API representation of a Dimple factor function
 * <p>
 * Delegates to underlying {@link FactorFunction} object.
 * <p>
 * Construct factor function objects using {@link DimpleApplet#functions} factory.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class JSFactorFunction extends JSProxyObject<FactorFunction>
{
	final JSFactorFunctionFactory _factory;
	
	/*--------------
	 * Construction
	 */
	
	JSFactorFunction(JSFactorFunctionFactory factory, FactorFunction function)
	{
		super(function);
		_factory = factory;
	}

	/*-----------------------
	 * JSProxyObject methods
	 */
	
	@Override
	public @Nullable DimpleApplet getApplet()
	{
		return _factory._applet;
	}

	/*---------
	 * Methods
	 */
	
	/**
	 * Computes the weight of the function given the specified values.
	 * <p>
	 * This is equivalent to <big>e<sup>-{@link #evalEnergy}(values)</sup></big>
	 * @since 0.07
	 */
	public double evalWeight(Object[] values)
	{
		return _delegate.eval(values);
	}
	
	/**
	 * Computes the energy of the function given the specified values.
	 * <p>
	 * This is equivalent to -log({@link #evalWeight}(values))
	 * @since 0.07
	 */
	public double evalEnergy(Object[] values)
	{
		return _delegate.evalEnergy(values);
	}
	
	/**
	 * The name of the factor function.
	 * <p>
	 * This is typically the same as the string used to construct the function.
	 * <p>
	 * @since 0.07
	 */
	public String getName()
	{
		return _delegate.getName();
	}
	
	/**
	 * Returns indices of directed output edges, if any.
	 * @param numEdges is the number of edges (variables) attached to the factor to which the
	 * factor function is applied.
	 * @return array of indices or null if function is not directed.
	 * @since 0.07
	 */
	public @Nullable int[] getDirectedToIndices(int numEdges)
	{
		return _delegate.getDirectedToIndices(numEdges);
	}
	
	/**
	 * Returns the value of the named parameter.
	 * <p>
	 * Returns null if function does not {@linkplain #hasParameters() have parameters} or {@code name}
	 * does not refer to a valid parameter of this function.
	 * @since 0.07
	 */
	public @Nullable Object getParameter(String name)
	{
		FactorFunction function = _delegate;
		if (function instanceof IParametricFactorFunction)
		{
			return ((IParametricFactorFunction)function).getParameter(name);
		}
		return null;
	}
	
	/**
	 * Returns a Java MAP containing the values of internal parameters.
	 * @return null if function is not {@linkplain #isParametric() parametric} or does not
	 * have internal parameters.
	 * @since 0.07
	 */
	public @Nullable Map<String,Object> getParameters()
	{
		Map<String,Object> parameters = null;
		if (hasParameters())
		{
			parameters = new TreeMap<>();
			((IParametricFactorFunction)_delegate).copyParametersInto(parameters);
		}
		return parameters;
	}
	
	/**
	 * True if function is parametric with internal parameters.
	 * <p>
	 * Only true if {@link #isParametric()} is true and function was created with internal parameters
	 * (i.e. parameters are not represented by Dimple variables).
	 * <p>
	 * @since 0.07
	 * @see #getParameters()
	 */
	public boolean hasParameters()
	{
		return _delegate.isParametric() && ((IParametricFactorFunction)_delegate).hasConstantParameters();
	}
	
	/**
	 * True if function is inherently deterministic and directed.
	 * @since 0.07
	 */
	public boolean isDeterministicDirected()
	{
		return _delegate.isDeterministicDirected();
	}

	/**
	 * True if this is a {@link JSTableFactorFunction}.
	 * @since 0.07
	 */
	public boolean isTableFactor()
	{
		return false;
	}

	/**
	 * True if function has parameters.
	 * @since 0.07
	 * @see #getParameter
	 */
	public boolean isParametric()
	{
		return _delegate.isParametric();
	}
}
