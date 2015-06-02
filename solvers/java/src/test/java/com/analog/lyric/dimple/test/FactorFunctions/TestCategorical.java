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

package com.analog.lyric.dimple.test.FactorFunctions;

import static com.analog.lyric.dimple.test.FactorFunctions.FactorFunctionTester.*;
import static com.analog.lyric.math.Utilities.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Categorical;
import com.analog.lyric.dimple.factorfunctions.CategoricalBase;
import com.analog.lyric.dimple.factorfunctions.CategoricalEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.CategoricalUnnormalizedParameters;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestCategorical extends DimpleTestBase
{
	@SuppressWarnings({ "unused", "deprecation" })
	@Test
	public void test()
	{
		Categorical c0 = new Categorical();
		assertInvariants(c0);
		assertArrayEquals(new int[] {1}, c0.getDirectedToIndices(2));
		assertArrayEquals(new int[] {1,2}, c0.getDirectedToIndices(3));
		assertFalse(c0.hasConstantParameters());
		
		assertEvalEnergy(c0, 0.0, Value.createRealJoint(1.0, 2.0), Value.createReal(0.0));
		assertEvalEnergy(c0, weightToEnergy(2.0), Value.createRealJoint(1.0, 2.0), Value.createReal(1));
		assertEvalEnergy(c0, 0.0, Value.createRealJoint(2.0, 1.0), Value.create(DiscreteDomain.bool(), true));
		
		Categorical c2 = new Categorical(new double[] { .6, 1.4 });
		assertInvariants(c2);
		assertArrayEquals(new double[] { .3, .7 }, c2.getParameters(), 0.0);
		assertTrue(c2.hasConstantParameters());
		assertArrayEquals(new int[] { 0 , 1 }, c2.getDirectedToIndices(2));
		assertEvalEnergyBit(c2, weightToEnergy(.3), 0);
		assertEvalEnergyBit(c2, weightToEnergy(.7), 1);
		assertEvalEnergyBit(c2, weightToEnergy(.3 * .7), 0, 1);
		
		try
		{
			new Categorical(new double[] { .3, -.4 });
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
		}

		CategoricalUnnormalizedParameters cup = new CategoricalUnnormalizedParameters(2.0);
		assertInvariants(cup);
		assertEquals(2, cup.getDimension());
		assertArrayEquals(new int[] {2}, cup.getDirectedToIndices(3));
		assertFalse(cup.hasConstantParameters());
		
		assertEvalEnergyReal(cup, weightToEnergy(.3), .6, 1.4, 0);
		assertEvalEnergyReal(cup, Double.POSITIVE_INFINITY, .6, -1.4, 0);
		assertEvalEnergyReal(cup, weightToEnergy(.3 * .7), .6, 1.4, 1, 0);
		
		CategoricalUnnormalizedParameters cup2 = new CategoricalUnnormalizedParameters(2, new double[] {.2, .3});
		assertInvariants(cup2);
		assertTrue(cup2.hasConstantParameters());
		assertArrayEquals(new double[] { .4, .6 }, cup2.getParameters(), 0.0);
		
		assertEvalEnergyBool(cup2, weightToEnergy(.4), false);
		
		
		CategoricalEnergyParameters cep = new CategoricalEnergyParameters(2.0);
		assertInvariants(cep);
		assertEquals(2, cep.getDimension());
		assertArrayEquals(new int[] {2}, cep.getDirectedToIndices(3));
		assertFalse(cep.hasConstantParameters());
		
		assertEvalEnergyReal(cep, weightToEnergy(.3), weightToEnergy(.6), weightToEnergy(1.4), 0);
		assertEvalEnergyReal(cep, weightToEnergy(.3 * .7), weightToEnergy(.6), weightToEnergy(1.4), 1, 0);

		CategoricalEnergyParameters cep2 =
			new CategoricalEnergyParameters(2, new double[] {weightToEnergy(.2), weightToEnergy(.3)});
		assertInvariants(cep2);
		assertTrue(cep2.hasConstantParameters());
		assertArrayEquals(new double[] { weightToEnergy(.4), weightToEnergy(.6) }, cep2.getParameters(), 1e-10);
		
		assertEvalEnergyBool(cep2, weightToEnergy(.4), false);
		assertEvalEnergyBool(cep2, weightToEnergy(.4 * .6), false, true);
	}
	
	private void assertInvariants(CategoricalBase c)
	{
		assertTrue(c.objectEquals(c));
		assertFalse(c.objectEquals("bogus"));
		
		assertTrue(c.isDirected());
		assertEquals(c.getParameters().length, c.getDimension());
		if (c.hasConstantParameters())
		{
			assertArrayEquals(c.getParameters(), c.getParameter("alpha"), 0.0);
			assertArrayEquals(c.getParameters(), c.getParameter("alphas"), 0.0);
			assertNull(c.getParameter("beta"));
		}
		else
		{
			assertNull(c.getParameter("alpha"));
			assertNull(c.getParameter("alphas"));
		}
		
		CategoricalBase clone = (CategoricalBase)c.clone();
		assertTrue(c.objectEquals(clone));
	}
}
