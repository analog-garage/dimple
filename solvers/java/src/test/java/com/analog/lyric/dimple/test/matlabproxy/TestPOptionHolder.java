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

package com.analog.lyric.dimple.test.matlabproxy;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.lang.reflect.Array;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.matlabproxy.POptionHolder;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.dimple.options.SolverOptions;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductOptions;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.OptionKey;

/**
 * Test for {@link POptionHolder} proxy class.
 * @since 0.07
 * @author Christopher Barber
 */
public class TestPOptionHolder extends DimpleTestBase
{
	@Test
	public void test()
	{
		DimpleEnvironment env = new DimpleEnvironment();
		DimpleEnvironment.setActive(env);
		
		FactorGraph fg = new FactorGraph();
		Real r1 = new Real();
		Real r2 = new Real();
		fg.addVariables(r1,r2);
		
		DimpleEnvironment.setActive(DimpleEnvironment.defaultEnvironment());
		assertSame(env, r1.getEnvironment());
		assertNotSame(env, DimpleEnvironment.active());
	
		POptionHolder proxy = new Subclass();
		assertEquals(0, proxy.size());
		proxy.unsetOption("SolverOptions.iterations"); // should do nothing
		proxy.setOptionOnAll("SolverOptions.iterations", 42);
		proxy.setOptionAcrossAll("SolverOptions.iterations", new Object[] {});
		proxy.setOptionsOnAll(new Object[] {"SolverOptions.iterations"}, new Object[] { 42});
		proxy.setOptionsAcrossAll(new Object[] {});
		assertInvariants(proxy);
		
		proxy = new Subclass(r1,r2);
		assertEquals(2, proxy.size());
		assertSame(r1, proxy.getOptionHolder(0));
		assertSame(r2, proxy.getOptionHolder(1));
		assertInvariants(proxy);
		
		fg.setOption(SolverOptions.iterations, 10);
		r1.setOption(SolverOptions.iterations, 12);
		r2.setOption(SumProductOptions.damping, .95);
		assertInvariants(proxy);
		assertArrayEquals(new Object[] { 12, 10 }, proxy.getOption("SolverOptions.iterations"));
		
		proxy.unsetOption("SolverOptions.iterations");
		assertInvariants(proxy);
		assertArrayEquals(new Object[] { 10, 10 }, proxy.getOption("SolverOptions.iterations"));
		
		proxy.setOptionOnAll("GibbsOptions.enableTempering", true);
		assertInvariants(proxy);
		assertArrayEquals(new Object[] { true, true }, proxy.getOption("GibbsOptions.enableTempering"));
		
		proxy.clearOptions();
		assertArrayEquals(new Object[] { false, false }, proxy.getOption("GibbsOptions.enableTempering"));
		assertInvariants(proxy);
		
		proxy.setOptionAcrossAll("SolverOptions.iterations", new Object[] { 14, 23 });
		assertInvariants(proxy);
		assertArrayEquals(new Object[] { 14, 23 }, proxy.getOption("SolverOptions.iterations"));
		
		proxy.setOptionsAcrossAll(new Object[] {
			new Object[] {
				"SolverOptions.iterations", 99,
				"JunctionTreeOptions.variableEliminatorCostFunctions", new Object[] { "MIN_FILL", "MIN_NEIGHBORS" }
			},
			new Object[][] {
				new Object[] { "SolverOptions.iterations", 9},
				new Object[] { "GibbsOptions.enableTempering", true },
			}
		});
		assertInvariants(proxy);
	}
	
	private void assertInvariants(POptionHolder proxy)
	{
		final int size = proxy.size();
		if (size == 0)
		{
			assertNull(proxy.getEnvironment());
			assertEquals(0,  proxy.getOption("SolverOptions.iterations").length);
			return;
		}
		
		DimpleEnvironment env = requireNonNull(proxy.getEnvironment());
		assertSame(proxy.getOptionHolder(0).getEnvironment(), proxy.getEnvironment());
		
		Object[][] options = proxy.getLocallySetOptions();
		assertEquals(size, options.length);
		
		for (int i = 0; i < size; ++i)
		{
			DimpleOptionHolder holder = proxy.getOptionHolder(i);
			Object[] optionsForHolder = options[i];

			assertEquals(0, optionsForHolder.length % 2);
			int nOptions = optionsForHolder.length / 2;
			
			assertEquals(holder.getLocalOptions().size(), nOptions);
			
			for (int j = 0; j < optionsForHolder.length; j += 2)
			{
				Object keyObj = optionsForHolder[j];
				Object value = optionsForHolder[j+1];
				
				assertTrue(keyObj instanceof String);
				
				IOptionKey<?> key = env.optionRegistry().asKey(keyObj);
				Object convertedValue = key.convertToValue(value);
				
				assertEquals(convertedValue, holder.getLocalOption(key));
				
				Object[] values = proxy.getOption(OptionKey.qualifiedName(key));
				assertEquals(size, values.length);
				if (value.getClass().isArray())
				{
					assertTrue(values[i].getClass().isArray());
					assertEquals(Array.getLength(value), Array.getLength(values[i]));
					for (int k = 0, endk = Array.getLength(value); k < endk; ++k)
					{
						assertEquals(Array.get(value, k), Array.get(values[i], k));
					}
				}
				else
				{
					assertEquals(value, values[i]);
				}
			}
		}
	}
	
	private static class Subclass extends POptionHolder
	{
		private final DimpleOptionHolder[] _holders;
		
		private Subclass(DimpleOptionHolder ... holders)
		{
			_holders = holders;
		}
		
		@Override
		public DimpleOptionHolder getOptionHolder(int i)
		{
			return _holders[i];
		}

		@Override
		public int size()
		{
			return _holders.length;
		}

		@Override
		public @Nullable Object getDelegate()
		{
			return _holders;
		}
		
	}
}
