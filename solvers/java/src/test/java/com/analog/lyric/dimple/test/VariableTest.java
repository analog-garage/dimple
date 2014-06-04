/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Discrete;


public class VariableTest {

	@BeforeClass
	public static void setUpBeforeClass()  {
	}

	@AfterClass
	public static void tearDownAfterClass()  {
	}

	@Before
	public void setUp()  {
	}

	@After
	public void tearDown()  {
	}
	
	@Test
	public void test_bit()
	{
		Bit a = new Bit();
		
		assertTrue(a instanceof Discrete);
		DiscreteDomain d = a.getDomain();
		assertArrayEquals(d.getElements(),new Object [] {0,1});
	}

	@Test
	public void test_simpleStuff()
	{
		Double[] domain = new Double[]{0.0, 1.0};
		Discrete v = new Discrete(domain[0], domain[1]);
		Object[] domainGot = v.getDiscreteDomain().getElements();
		assertTrue(Arrays.equals(domain, domainGot));
		
		assertTrue(v.getId() > -1);
		
		double[] input = new double[]{.5, .5};
		v.setInput(input);
		assertTrue(Arrays.equals(v.getInput(), input));
		assertTrue(Arrays.equals(v.getBelief(), input));
		
	}
	
	@SuppressWarnings("unused")
	@Test(expected=Exception.class)
	public void test_BlowUp()
	{
		//should kaboom
		new Discrete();
		
	}
}
