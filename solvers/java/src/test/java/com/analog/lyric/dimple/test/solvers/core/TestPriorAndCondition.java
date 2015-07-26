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

package com.analog.lyric.dimple.test.solvers.core;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.PriorAndCondition;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Unit test for {@link PriorAndCondition} class
 * @since 0.08
 * @author Christopher Barber
 */
public class TestPriorAndCondition extends DimpleTestBase
{
	@Test
	public void test()
	{
		PriorAndCondition pandc = PriorAndCondition.create(null, null);
		assertInvariants(pandc);
		assertEquals(0, pandc.size());
		assertNull(pandc.prior());
		assertNull(pandc.condition());
		
		Value value1 = Value.create(1), value2 = Value.create(2);
		
		PriorAndCondition pandc2 = PriorAndCondition.create(value1, null);
		assertInvariants(pandc2);
		assertNotSame(pandc, pandc2);
		assertEquals(value1, pandc2.prior());
		assertNull(pandc2.condition());

		assertNull(pandc.release());
		assertTrue(pandc.size() < 0);
		
		PriorAndCondition pandc3 = PriorAndCondition.create(null, value2);
		assertInvariants(pandc3);
		assertSame(pandc, pandc3);
		assertEquals(value2, pandc3.condition());
		assertNull(pandc3.prior());
		
		PriorAndCondition pandc4 = PriorAndCondition.create(value1, value2);
		assertInvariants(pandc4);
		assertEquals(value1, pandc4.prior());
		assertEquals(value2, pandc4.condition());
		
		assertNull(pandc4.release());
		assertTrue(pandc4.size() < 0);
		assertNull(pandc4.prior());
		assertNull(pandc4.condition());
		
		DiscreteMessage msg = new DiscreteWeightMessage(2);
		PriorAndCondition pandc5 = PriorAndCondition.create(msg, value1);
		assertSame(pandc4, pandc5);
		assertInvariants(pandc5);
		assertSame(msg, pandc5.prior());
		assertSame(value1, pandc5.condition());
	}
	
	private void assertInvariants(PriorAndCondition pandc)
	{
		IDatum prior = pandc.prior();
		IDatum condition = pandc.condition();
		
		assertEquals((prior == null ? 0 : 1) + (condition == null ? 0 : 1), pandc.size());
		assertEquals(pandc.size() == 0, pandc.isEmpty());
		
		if (prior instanceof Value)
		{
			assertEquals(prior, pandc.value());
		}
		else if (condition instanceof Value)
		{
			assertEquals(condition, pandc.value());
		}
		else
		{
			assertNull(pandc.value());
		}
		
		if (prior != null)
		{
			assertEquals(prior, pandc.get(0));
			if (condition != null)
			{
				assertEquals(condition, pandc.get(1));
			}
		}
		else if (condition != null)
		{
			assertEquals(condition, pandc.get(0));
		}
		
		// Try a couple of list modification methods
		if (pandc.size() > 0)
		{
			expectThrow(UnsupportedOperationException.class, pandc, "clear");
		}
		expectThrow(UnsupportedOperationException.class, pandc, "add", Value.create(42));
	}
}
