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

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionRegistry;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.util.misc.Internal;


/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class JSFactorFunctionFactory extends JSProxyObjectWithApplet<FactorFunctionRegistry>
{
	/*--------------
	 * Construction
	 */
	
	JSFactorFunctionFactory(DimpleApplet applet)
	{
		this(applet.getEnvironment().getDelegate().factorFunctions(), applet);
	}
	
	private JSFactorFunctionFactory(FactorFunctionRegistry registry, @Nullable DimpleApplet applet)
	{
		super(applet, registry);
	}
	
	@Internal
	public JSFactorFunctionFactory()
	{
		this(DimpleEnvironment.active().factorFunctions(), null);
	}
	
	/*---------------------------------
	 * JSFactorFunctionFactory methods
	 */
	
	/**
	 * Creates a factor function instance for given name.
	 * <p>
	 * Returns a new instance of the requested factor function using its default constructor.
	 * For functions that can be parameterized, such as
	 * {@linkplain com.analog.lyric.dimple.factorfunctions.Normal Normal} or
	 * {@linkplain com.analog.lyric.dimple.factorfunctions.Bernoulli Bernoulli}, this will return
	 * a version of the function in which the parameter values will be taken from variables attached
	 * to the factor. For details, see the documentation for the function.
	 * <p>
	 * @param name must match the name of a factor function class known to Dimple.
	 * @since 0.07
	 * @see #create(String, Object)
	 */
	public JSFactorFunction create(String name)
	{
		return wrap(_delegate.instantiate(name));
	}
	
	/**
	 * Creates a factor function instance for given name.
	 * <p>
	 * Returns a new instance of the requested factor function using the form of its constructor
	 * that takes a {@code Map<String,Object>} that specifies the function's parameters, which will
	 * be stored as state in the function itself and will not come from variables attached to the factor.
	 * This function will convert the parameters specified as a {@link JSObject} to a map and invoke the
	 * corresponding factor function constructor. For example, to create a univariate normal (Gaussian) function with
	 * a specified mean and standard deviation you could write this in Javascript several different ways:
	 * <blockquote>
	 * <pre>
	 * dimple.funtions.get('Normal', {mu: 0.0, sigma: 2.0});
	 * dimple.funtions.get('Normal', {mean: 0.0, std: 2.0});
	 * dimple.funtions.get('Normal', {variance: 4.0}); // 0.0 is the default mean
	 * dimple.funtions.get('Normal', {precision: .25});
	 * </pre>
	 * </blockquote>
	 * Consult the documentation for the specific function for details as to what parameters are supported. Here is a
	 * list of functions that currently support this form of construction:
	 * <ul>
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.Bernoulli#Bernoulli(java.util.Map) Bernoulli}
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.Beta#Beta(java.util.Map) Beta}
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.Categorical#Categorical(java.util.Map) Categorical}
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.Dirichlet#Dirichlet(java.util.Map) Dirichlet}
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.Gamma#Gamma(java.util.Map) Gamma}
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.InverseGamma#InverseGamma(java.util.Map) InverseGamma}
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.LogNormal#LogNormal(java.util.Map) LogNormal}
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.Normal#Normal(java.util.Map) Normal}
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.Poisson#Poisson(java.util.Map) Poisson}
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.Rayleigh#Rayleigh(java.util.Map) Rayleigh}
	 * <li>{@linkplain com.analog.lyric.dimple.factorfunctions.VonMises#VonMises(java.util.Map) VonMises}
	 * </ul>
	 * More may be added in future releases or may be available from user provided packages.
	 * <p>
	 * @param name must match the name of a factor function class known to Dimple that has a constructor
	 * taking a single argument of type {@code Map<String,Object>}.
	 * @param parameters is either a {@link JSObject} whose members specify the parameters, or a
	 * {@code Map<String,Object>}.
	 * @since 0.07
	 * @see #create(String)
	 */
	@SuppressWarnings("unchecked")
	public JSFactorFunction create(String name, Object parameters)
	{
		Map<String,Object> parameterMap = convertParametersToMap(_applet, parameters);
		
		if (parameterMap == null)
		{
			throw new JSException(String.format("'%s' is not a valid parameters type",
				parameters.getClass().getSimpleName()));
		}
		
		return wrap(_delegate.instantiateWithParameters(name, parameterMap));
	}
	
	/**
	 * Converts parameters to a Map object or else returns null.
	 * <p>
	 * If {@code parameters} is a {@code JSObject}, {@link IJSOBject} or a {@code Map}, they will
	 * be wrapped up in a map and returned. Otherwise null will be returned.
	 * <p>
	 * @param applet
	 * @param parameters are the potential parameters
	 * @return
	 * @since 0.07
	 */
	@SuppressWarnings("unchecked")
	static @Nullable Map<String,Object> convertParametersToMap(@Nullable DimpleApplet applet, Object parameters)
	{
		if (parameters instanceof JSObject)
		{
			return new JSObjectMap(applet, JSObjectWrapper.wrap(parameters));
		}
		else if (parameters instanceof IJSObject)
		{
			return new JSObjectMap(applet, (IJSObject)parameters);
		}
		else if (parameters instanceof Map)
		{
			return (Map<String,Object>)parameters;
		}
		
		return null;
	}
	
	/**
	 * Creates a table factor function.
	 * <p>
	 * Creates a table-based factor function wrapped around the specified {@code table}.
	 * @since 0.07
	 * @see #createTable(Object[])
	 */
	public JSTableFactorFunction create(JSFactorTable table)
	{
		return new JSTableFactorFunction(this, table);
	}
	
	/**
	 * Creates a factor table.
	 * <p>
	 * Creates a table-based factor function for specified discrete domains/variables. Once created the
	 * table weights should be set before use. Factor tables are expected to be used to create table-based
	 * factor functions.
	 * <p>
	 * @param domainsOrVariables
	 * @since 0.07
	 * @see #create(JSFactorTable)
	 */
	public JSFactorTable createTable(Object[] domainsOrVariables)
	{
		return new JSFactorTable(_applet, domainsOrVariables);
	}
	
	/**
	 * Creates a proxy wrapper for a raw Dimple factor function.
	 * @since 0.07
	 */
	public JSFactorFunction wrap(FactorFunction function)
	{
		if (function instanceof TableFactorFunction)
		{
			return new JSTableFactorFunction(this, (TableFactorFunction)function);
		}
		return new JSFactorFunction(this, function);
	}

}
