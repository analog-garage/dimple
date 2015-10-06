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

package com.analog.lyric.dimple.test.model.variables;

import static com.analog.lyric.dimple.model.variables.VariablePredicates.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.VariablePredicates;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Unit test for {@link VariablePredicates}
 * @since 0.08
 * @author Christopher Barber
 */
public class TestVariablePredicates extends DimpleTestBase
{
	@Test
	public void test()
	{
		Real unboundedReal = new Real();
		Real boundedReal = new Real(-1.0,1.0);
		Bit bit = new Bit();
		RealJoint unboundedR2 = new RealJoint(2);
		RealJoint boundedR2 = new RealJoint(RealJointDomain.create(RealDomain.nonNegative(), 2));
		
		// isDiscrete
		assertTrue(isDiscrete().apply(bit));
		assertFalse(isDiscrete().apply(null));
		assertFalse(isDiscrete().apply(unboundedReal));
		assertFalse(isDiscrete().apply(boundedReal));
		assertFalse(isDiscrete().apply(unboundedR2));
		assertFalse(isDiscrete().apply(boundedR2));
		
		// isUnboundedReal
		assertTrue(isUnboundedReal().apply(unboundedReal));
		assertFalse(isUnboundedReal().apply(null));
		assertFalse(isUnboundedReal().apply(boundedReal));
		assertFalse(isUnboundedReal().apply(bit));
		assertFalse(isUnboundedReal().apply(unboundedR2));
		assertFalse(isUnboundedReal().apply(boundedR2));
		
		// isUnboundedRealJoint
		assertTrue(isUnboundedRealJoint().apply(unboundedR2));
		assertFalse(isUnboundedRealJoint().apply(boundedR2));
		assertFalse(isUnboundedRealJoint().apply(null));
		assertFalse(isUnboundedRealJoint().apply(unboundedReal));
		assertFalse(isUnboundedRealJoint().apply(boundedReal));
		assertFalse(isUnboundedRealJoint().apply(bit));
		
	}
}
