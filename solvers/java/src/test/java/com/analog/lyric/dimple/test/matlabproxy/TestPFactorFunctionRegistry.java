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

package com.analog.lyric.dimple.test.matlabproxy;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionRegistry;
import com.analog.lyric.dimple.matlabproxy.PEnvironment;
import com.analog.lyric.dimple.matlabproxy.PFactorFunctionRegistry;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Unit test for {@link PFactorFunctionRegistry}
 * @since 0.08
 * @author Christopher Barber
 */
public class TestPFactorFunctionRegistry extends DimpleTestBase
{
	@Test
	public void test()
	{
		FactorFunctionRegistry ffr = DimpleEnvironment.active().factorFunctions();
		PFactorFunctionRegistry pffr = new PEnvironment().factorFunctions();
		assertSame(ffr, pffr.getDelegate());
		
		final String standardPackage = Xor.class.getPackage().getName();
		assertArrayEquals(new String[] { standardPackage }, pffr.getPackages());
		assertArrayEquals(new String[0], pffr.getClasses());
		assertEquals(Xor.class.getName(), pffr.getClass("Xor"));
		assertArrayEquals(new String[] {"Xor", Xor.class.getName()}, pffr.getClasses());
	
		pffr.loadAll();
		String[] classes = pffr.getClasses();
		assertTrue(classes.length > 170); // We only expect this number to grow as we implement new functions.
		assertTrue(Arrays.binarySearch(classes, "Normal") >=0 );
		assertTrue(Arrays.binarySearch(classes, Normal.class.getName()) >=0 );
		
		String[] classes2 = classes.clone();
		Arrays.sort(classes2);
		assertArrayEquals(classes, classes2);
		
		assertNull(pffr.getClass("MyFactorFunction"));
		
		final String thisPackage = getClass().getPackage().getName();
		pffr.addPackage(thisPackage);
		assertArrayEquals(new String[] { standardPackage, thisPackage}, pffr.getPackages());
		assertEquals(MyFactorFunction.class.getName(), pffr.getClass("MyFactorFunction"));
		
		pffr.reset();
		assertArrayEquals(new String[] { standardPackage }, pffr.getPackages());
		assertNull(pffr.getClass("MyFactorFunction"));
	}
}
