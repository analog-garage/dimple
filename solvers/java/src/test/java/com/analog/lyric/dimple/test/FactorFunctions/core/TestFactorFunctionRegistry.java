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

package com.analog.lyric.dimple.test.FactorFunctions.core;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionRegistry;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestFactorFunctionRegistry extends DimpleTestBase
{
	@Test
	public void test()
	{
		FactorFunctionRegistry registry = new FactorFunctionRegistry();
		
		assertArrayEquals(new String[] { "com.analog.lyric.dimple.factorfunctions" }, registry.getPackages());
		
		assertTrue(registry.instantiate("Sum") instanceof Sum);

		Bernoulli bernoulli = instantiate(registry, "Bernoulli", "p", .4);
		assertEquals(.4, bernoulli.getParameter(), 0.0);
		
		expectThrow(RuntimeException.class, registry, "instantiateWithParameters", "Sum", Collections.emptyMap());
		expectThrow(RuntimeException.class, ".*Invalid parameter value.*", registry,
			"instantiateWithParameters", "Bernoulli", keyValueToMap("p", 2.3));
	}
	
	@SuppressWarnings("unchecked")
	private <T extends FactorFunction> T instantiate(FactorFunctionRegistry reg, String name, Object ... parameters)
	{
		return (T) reg.instantiateWithParameters(name, keyValueToMap(parameters));
	}
	
	private Map<String,Object> keyValueToMap(Object ... args)
	{
		Map<String,Object> map = new TreeMap<String,Object>();
		for (int i = 0; i < args.length; i +=2)
		{
			map.put((String)args[i], args[i + 1]);
		}
		return map;
	}
}
