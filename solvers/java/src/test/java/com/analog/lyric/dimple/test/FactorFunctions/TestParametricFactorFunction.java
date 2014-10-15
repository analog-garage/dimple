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

package com.analog.lyric.dimple.test.FactorFunctions;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.Beta;
import com.analog.lyric.dimple.factorfunctions.Categorical;
import com.analog.lyric.dimple.factorfunctions.Dirichlet;
import com.analog.lyric.dimple.factorfunctions.Gamma;
import com.analog.lyric.dimple.factorfunctions.InverseGamma;
import com.analog.lyric.dimple.factorfunctions.LogNormal;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.Poisson;
import com.analog.lyric.dimple.factorfunctions.Rayleigh;
import com.analog.lyric.dimple.factorfunctions.VonMises;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Test implementations of IParametricFactorFunction interface.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class TestParametricFactorFunction extends DimpleTestBase
{
	@Test
	public void test()
	{
		Map<String,Object> emptyMap = Collections.emptyMap();
		
		// Bernoulli
		Bernoulli bernoulli = new Bernoulli(.4);
		assertEquals(.4, bernoulli.getParameter(), 0.0);
		assertEquals(.4, bernoulli.getParameter("p"));
		assertInvariants(bernoulli);
		bernoulli = new Bernoulli(emptyMap);
		assertEquals(.5, bernoulli.getParameter(), 0.0);
		assertInvariants(bernoulli);
		assertInvariants(new Bernoulli());
		
		// Beta
		Beta beta = new Beta(3,4);
		assertEquals(2, beta.getAlphaMinusOne(), 0.0);
		assertEquals(3, beta.getBetaMinusOne(), 0.0);
		assertEquals(3.0, beta.getParameter("alpha"));
		assertEquals(4.0, beta.getParameter("beta"));
		assertInvariants(beta);
		beta = new Beta(emptyMap);
		assertEquals(1.0, beta.getParameter("alpha"));
		assertEquals(1.0, beta.getParameter("beta"));
		assertInvariants(new Beta());
		
		// Categorical
		Categorical categorical = new Categorical(new double[] { .6, .8 ,.6 }); // will be normalized
		assertArrayEquals(new double[] {.3,.4,.3}, (double[])categorical.getParameter("alpha"), 0.0);
		assertArrayEquals(new double[] {.3,.4,.3}, (double[])categorical.getParameter("alphas"), 0.0);
		assertInvariants(categorical);
		assertInvariants(new Categorical());
		
		// Dirichlet
		Dirichlet dirichlet= new Dirichlet(new double[] { .6, .8 ,.6 }); // not normalized
		assertArrayEquals(new double[] {.6,.8,.6}, (double[])dirichlet.getParameter("alpha"), 0.0);
		assertArrayEquals(new double[] {.6,.8,.6}, (double[])dirichlet.getParameter("alphas"), 0.0);
		assertInvariants(dirichlet);
		assertInvariants(new Dirichlet());
		
		// Gamma
		Gamma gamma = new Gamma(2,3);
		assertEquals(2.0, gamma.getParameter("alpha"));
		assertEquals(1.0, gamma.getAlphaMinusOne(), 0.0);
		assertEquals(3.0, gamma.getParameter("beta"));
		assertEquals(3.0, gamma.getBeta(), 0.0);
		assertInvariants(gamma);
		gamma = new Gamma(emptyMap);
		assertEquals(1.0, gamma.getParameter("alpha"));
		assertEquals(1.0, gamma.getParameter("beta"));
		assertInvariants(new Gamma());

		// InverseGamma
		InverseGamma inverseGamma = new InverseGamma(2,3);
		assertEquals(2.0, inverseGamma.getParameter("alpha"));
		assertEquals(3.0, inverseGamma.getParameter("beta"));
		assertInvariants(inverseGamma);
		inverseGamma = new InverseGamma(emptyMap);
		assertEquals(1.0, inverseGamma.getParameter("alpha"));
		assertEquals(1.0, inverseGamma.getParameter("beta"));
		assertInvariants(new Gamma());
		
		// LogNormal
		LogNormal logNormal = new LogNormal(2.0, .5);
		assertEquals(2.0, logNormal.getMean(), 0.0);
		assertEquals(.5, logNormal.getPrecision(), 0.0);
		assertInvariants(logNormal);
		logNormal = new LogNormal(newMap("mu", 1.5, "sigma", 2.0));
		assertEquals(1.5, logNormal.getParameter("mean"));
		assertEquals(.25, logNormal.getParameter("precision"));
		assertEquals(2.0, logNormal.getParameter("sigma"));
		assertInvariants(logNormal);
		logNormal = new LogNormal(newMap("variance", 9.0));
		assertEquals(9.0, logNormal.getParameter("variance"));
		assertEquals(3.0, logNormal.getParameter("std"));
		assertInvariants(logNormal);
		logNormal = new LogNormal(emptyMap);
		assertEquals(0.0, logNormal.getMean(), 0.0);
		assertEquals(1.0, logNormal.getPrecision(), 0.0);
		assertInvariants(new LogNormal());

		// Normal
		Normal normal = new Normal(2.0, .5);
		assertEquals(2.0, normal.getMean(), 0.0);
		assertEquals(.5, normal.getPrecision(), 0.0);
		assertInvariants(normal);
		normal = new Normal(newMap("mu", 1.5, "sigma", 2.0));
		assertEquals(1.5, normal.getParameter("mean"));
		assertEquals(.25, normal.getParameter("precision"));
		assertEquals(2.0, normal.getParameter("sigma"));
		assertInvariants(normal);
		normal = new Normal(newMap("variance", 9.0));
		assertEquals(3.0, normal.getStandardDeviation(), 0.0);
		assertEquals(9.0, normal.getParameter("variance"));
		assertEquals(3.0, normal.getParameter("std"));
		assertInvariants(normal);
		normal = new Normal(emptyMap);
		assertEquals(0.0, normal.getMean(), 0.0);
		assertEquals(1.0, normal.getPrecision(), 0.0);
		assertInvariants(new Normal());
		
		// Poisson
		Poisson poisson = new Poisson(.3);
		assertEquals(.3, poisson.getLambda(), 0.0);
		assertEquals(.3, poisson.getParameter("lambda"));
		assertInvariants(poisson);
		poisson = new Poisson(emptyMap);
		assertEquals(1.0, poisson.getParameter("lambda"));
		assertInvariants(new Poisson());
		
		// Rayleigh
		Rayleigh rayleigh = new Rayleigh(2.3);
		assertInvariants(rayleigh);
		rayleigh = new Rayleigh(emptyMap);
		assertEquals(1.0, rayleigh.getParameter("sigma"));
		assertInvariants(new Rayleigh());
		
		// VonMises
		VonMises vonMises = new VonMises(2.0, .5);
		assertEquals(2.0, vonMises.getParameter("mean"));
		assertEquals(.5, vonMises.getParameter("precision"));
		assertInvariants(vonMises);
		vonMises = new VonMises(newMap("mu", 1.5, "sigma", 2.0));
		assertEquals(1.5, vonMises.getParameter("mean"));
		assertEquals(.25, vonMises.getParameter("precision"));
		assertEquals(2.0, vonMises.getParameter("sigma"));
		assertInvariants(vonMises);
		vonMises = new VonMises(newMap("variance", 9.0));
		assertEquals(9.0, vonMises.getParameter("variance"));
		assertEquals(3.0, vonMises.getParameter("std"));
		assertInvariants(vonMises);
		vonMises = new VonMises(emptyMap);
		assertEquals(0.0, vonMises.getParameter("mean"));
		assertEquals(1.0, vonMises.getParameter("precision"));
		assertInvariants(new VonMises());
	}
	
	private void assertInvariants(IParametricFactorFunction function)
	{
		assertTrue(function.isParametric());
		
		Map<String,Object> parameters = new TreeMap<>();
		int nCopied = function.copyParametersInto(parameters);
		assertEquals(nCopied, parameters.size());
		if (function.hasConstantParameters())
		{
			assertTrue(nCopied > 0);
		}
		else
		{
			assertEquals(0, nCopied);
		}
		for (String name : parameters.keySet())
		{
			Object val1 = parameters.get(name);
			Object val2 = function.getParameter(name);
			assertNotNull(val1);
			assertNotNull(val2);
			assertReallyEquals(val1,val2);
		}
		assertNull(function.getParameter("bogusParameterName"));
		
		try
		{
			Constructor<? extends IParametricFactorFunction> constructor =
				function.getClass().getConstructor(Map.class);
			
			IParametricFactorFunction function2 = constructor.newInstance(parameters);
			assertTrue(function2.hasConstantParameters());
			
			Map<String,Object> parameters2 = new TreeMap<>();
			function2.copyParametersInto(parameters2);
			assertFalse(parameters2.isEmpty());
			assertTrue(parameters2.size() >= parameters.size());
			if (function.hasConstantParameters())
			{
				assertEquals(parameters.keySet(), parameters2.keySet());
				for (String name : parameters.keySet())
				{
					assertReallyEquals(parameters.get(name), parameters2.get(name));
				}
			}
			else
			{
				for (String name : parameters2.keySet())
				{
					assertNull(parameters.get(name));
				}
			}
		}
		catch (InvocationTargetException ex)
		{
			// Not all parametric functions have default values, so constructing using an empty
			// constructor will not always work.
		}
		catch (Exception ex)
		{
			fail(ex.toString());
		}
	}
	
	private void assertReallyEquals(Object val1, Object val2)
	{
		if (val1.getClass().isArray())
		{
			final int length1 = Array.getLength(val1);
			final int length2 = Array.getLength(val2);
			assertEquals(length1, length2);
			for (int i = length1; --i>=0;)
			{
				assertEquals(Array.get(val1,i), Array.get(val2,i));
			}
		}
		else
		{
			assertEquals(val1,val2);
		}
	}
	
	private Map<String,Object> newMap(Object ... args)
	{
		Map<String,Object> map = new TreeMap<>();
		for (int i = 0; i < args.length; i += 2)
		{
			map.put((String)args[i], args[i+1]);
		}
		return map;
	}
}
