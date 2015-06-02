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
import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransition;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransitionEnergyParameters;
import com.analog.lyric.dimple.factorfunctions.DiscreteTransitionUnnormalizedParameters;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Tests for {@link DiscreteTransition} factor functions.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class TestDiscreteTransition extends DimpleTestBase
{
	@Test
	public void test()
	{
		final DiscreteDomain abDomain = DiscreteDomain.create('a','b');
		DiscreteTransition dt = new DiscreteTransition();
		
		expectThrow(DimpleException.class, dt, "evalEnergy");
		expectThrow(DimpleException.class, dt, "evalEnergy", Value.createReal(0.0));
		
		assertEvalEnergy(dt,
			weightToEnergy(.7),
			Value.create(abDomain, 'b'), // y
			Value.create(abDomain, 'a'), // x
			Value.createRealJoint(.3,.7), // x=a ->
			Value.createRealJoint(.4,.6)); // x=b->

		assertEvalEnergy(dt,
			weightToEnergy(.4),
			Value.create(abDomain, 'a'), // y
			Value.create(abDomain, 'b'), // x
			Value.createRealJoint(.3,.7), // x=a ->
			Value.createRealJoint(.4,.6)); // x=b->
		
		final DiscreteTransitionUnnormalizedParameters dtu2x2 = new DiscreteTransitionUnnormalizedParameters(2);
		assertEquals(2, dtu2x2.getXDimension());
		assertEquals(2, dtu2x2.getYDimension());
		assertEquals(4, dtu2x2.getNumParameters());

		expectThrow(DimpleException.class, dtu2x2, "evalEnergy");
		expectThrow(DimpleException.class, dtu2x2, "evalEnergy", Value.createReal(0.0));

		assertEvalEnergy(dtu2x2,
			weightToEnergy(.4),
			Value.create(abDomain, 'a'), // y
			Value.create(abDomain, 'b'), // x
			Value.createReal(.3), Value.createReal(.7),
			Value.createReal(.8), Value.createReal(1.2));
		assertEvalEnergy(dtu2x2,
			Double.POSITIVE_INFINITY,
			Value.create(abDomain, 'a'), // y
			Value.create(abDomain, 'a'), // x
			Value.createReal(.3), Value.createReal(-.7),
			Value.createReal(.8), Value.createReal(1.2));

		final DiscreteTransitionEnergyParameters dte2x2 = new DiscreteTransitionEnergyParameters(2);
		assertEquals(2, dte2x2.getXDimension());
		assertEquals(2, dte2x2.getYDimension());
		assertEquals(4, dte2x2.getNumParameters());

		expectThrow(DimpleException.class, dte2x2, "evalEnergy");
		expectThrow(DimpleException.class, dte2x2, "evalEnergy", Value.createReal(0.0));

		assertEvalEnergy(dte2x2,
			weightToEnergy(.4),
			Value.create(abDomain, 'a'), // y
			Value.create(abDomain, 'b'), // x
			Value.createReal(weightToEnergy(.3)), Value.createReal(weightToEnergy(.7)),
			Value.createReal(weightToEnergy(.8)), Value.createReal(weightToEnergy(1.2)));
	}
}
