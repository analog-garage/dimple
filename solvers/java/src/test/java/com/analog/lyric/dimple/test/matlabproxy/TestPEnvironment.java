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

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.matlabproxy.PEnvironment;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Test for {@link PEnvironment} proxy class.
 * @since 0.07
 * @author Christopher Barber
 */
public class TestPEnvironment extends DimpleTestBase
{
	@Test
	public void test()
	{
		// Make sure active environment is different than defaultEnvironment
		DimpleEnvironment active = new DimpleEnvironment();
		DimpleEnvironment.setActive(active);
		assertNotSame(active, DimpleEnvironment.defaultEnvironment());
		
		PEnvironment env = new PEnvironment();
		assertSame(active, env.getEnvironment());
		assertSame(active, env.getDelegate());
		assertSame(active, env.getModelerObject());
		assertEquals(1, env.size());
		assertSame(active, env.getOptionHolder(0));
		
		env = new PEnvironment(DimpleEnvironment.defaultEnvironment());
		assertSame(DimpleEnvironment.defaultEnvironment(), env.getEnvironment());
		
		Object[] keys = env.getOptionKeysMatching("foobarbaz");
		assertEquals(0, keys.length);
		
		keys = env.getOptionKeysMatching("BPOptions.iterations");
		assertArrayEquals(new Object[] { BPOptions.iterations }, keys);
	}
}
