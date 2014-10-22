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

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.jsproxy.IJSObject;
import com.analog.lyric.dimple.jsproxy.JSDiscreteDomain;
import com.analog.lyric.dimple.jsproxy.JSFactorFunction;
import com.analog.lyric.dimple.jsproxy.JSFactorFunctionFactory;
import com.analog.lyric.dimple.jsproxy.JSFactorTable;
import com.analog.lyric.dimple.jsproxy.JSTableFactorFunction;

/**
 * Tests for JSFactorFunction and JSFactorFunctionFactory
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestJSFactorFunction extends JSTestBase
{
	@SuppressWarnings("null")
	@Test
	public void test()
	{
		JSFactorFunctionFactory functions = state.functions;
		
		JSFactorFunction sum = functions.create("Sum");
		assertEquals("Sum", sum.getName());
		assertTrue(sum.getDelegate() instanceof Sum);
		assertEquals(state.applet, sum.getApplet());
		assertFalse(sum.isParametric());
		assertNull(sum.getParameter("p"));
		assertTrue(sum.isDeterministicDirected());
		assertFalse(sum.isTableFactor());
		assertArrayEquals(new int[] { 0 }, sum.getDirectedToIndices(4));
		assertEquals(1.0, sum.evalWeight(new Object[] {3.0, 1.0, 2.0}), 0.0);
		assertEquals(0.0, sum.evalEnergy(new Object[] {3.0, 1.0, 2.0}), 0.0);
		assertEquals(0.0, sum.evalWeight(new Object[] {4.0, 1.0, 2.0}), 0.0);
		assertEquals(Double.POSITIVE_INFINITY, sum.evalEnergy(new Object[] {4.0, 1.0, 2.0}), 0.0);
		assertInvariants(sum);
		
		// Test functions with parameters
		JSFactorFunction bernoulli = functions.create("Bernoulli");
		assertEquals("Bernoulli", bernoulli.getName());
		assertFalse(((Bernoulli)bernoulli.getDelegate()).hasConstantParameters());
		assertInvariants(bernoulli);

		bernoulli = functions.create("Bernoulli", params("p", .4));
		assertTrue(bernoulli.isParametric());
		assertEquals(.4, bernoulli.getParameter("p"));
		assertNull(bernoulli.getParameter("bogus"));
		assertInvariants(bernoulli);
		
		IJSObject jsobj = createJSObject();
		if (jsobj != null)
		{
			jsobj.setMember("p", .6);
			bernoulli = functions.create("Bernoulli", jsobj);
			assertTrue(bernoulli.isParametric());
			assertEquals(.6, bernoulli.getParameter("p"));
			assertNull(bernoulli.getParameter("bogus"));
			assertInvariants(bernoulli);
		}

		// Test factor table creation
		JSDiscreteDomain bitDomain = state.domains.bit();
		JSFactorTable table1 = functions.createTable(new Object[] { bitDomain, bitDomain });
		assertEquals(2, table1.getDimensions());
		for (int i = table1.getDimensions(); --i>=0;)
		{
			assertEquals(bitDomain, table1.getDomain(i));
		}
		
		JSFactorFunction tableFunction1 = functions.create(table1);
		assertTrue(tableFunction1.isTableFactor());
		assertSame(table1, ((JSTableFactorFunction)tableFunction1).getTable());
		assertInvariants(tableFunction1);
	}
	
	private void assertInvariants(JSFactorFunction function)
	{
		assertEquals(function, function);
		assertEquals(function, state.functions.wrap(function.getDelegate()));
		assertEquals(state.applet, function.getApplet());
		assertEquals(function.getDelegate().getName(), function.getName());
		assertEquals(function.getDelegate().isDeterministicDirected(), function.isDeterministicDirected());
		assertNull(function.getParameter("noSuchParameter"));
		Map<String,Object> parameters = function.getParameters();
		if (function.hasParameters())
		{
			assertTrue(function.isParametric());
			assertNotNull(parameters);
			for (String key : parameters.keySet())
			{
				assertEquals(parameters.get(key), function.getParameter(key));
			}
		}
		else
		{
			assertNull(parameters);
		}
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
