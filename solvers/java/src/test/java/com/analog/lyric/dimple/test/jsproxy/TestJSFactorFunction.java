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

package com.analog.lyric.dimple.test.jsproxy;

import static org.junit.Assert.*;

import java.awt.HeadlessException;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.jsproxy.DimpleApplet;
import com.analog.lyric.dimple.jsproxy.JSFactorFunction;
import com.analog.lyric.dimple.jsproxy.JSFactorFunctionFactory;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Tests for JSFactorFunction and JSFactorFunctionFactory
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestJSFactorFunction extends DimpleTestBase
{
	@SuppressWarnings("null")
	@Test
	public void test()
	{
		DimpleApplet applet = null;
		JSFactorFunctionFactory functions = null;
		try
		{
			applet = new DimpleApplet();
			functions = applet.functions;
			assertEquals(applet, functions.getApplet());
		}
		catch (HeadlessException ex)
		{
			functions = new JSFactorFunctionFactory(DimpleEnvironment.active().factorFunctions(), null);
		}
		
		JSFactorFunction sum = functions.get("Sum");
		assertEquals("Sum", sum.getName());
		assertTrue(sum.getDelegate() instanceof Sum);
		assertEquals(applet, sum.getApplet());
		assertFalse(sum.isParametric());
		assertNull(sum.getParameter("p"));
		assertTrue(sum.isDeterministicDirected());
		assertFalse(sum.isFactorTable());
		assertArrayEquals(new int[] { 0 }, sum.getDirectedToIndices(4));
		
		// Test functions with parameters
		JSFactorFunction bernoulli = functions.get("Bernoulli");
		assertEquals("Bernoulli", bernoulli.getName());
		assertFalse(((Bernoulli)bernoulli.getDelegate()).hasConstantParameters());

		bernoulli = functions.get("Bernoulli", params("p", .4));
		assertTrue(bernoulli.isParametric());
		assertEquals(.4, bernoulli.getParameter("p"));
		assertNull(bernoulli.getParameter("bogus"));
	}
	
	private Map<String,Object> params(Object ... args)
	{
		Map<String,Object> map = new TreeMap<>();
		for (int i = 0; i < args.length; i += 2)
		{
			map.put((String)args[i], args[i+1]);
		}
		return map;
	}
}
