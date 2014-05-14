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

package com.analog.lyric.dimple.test.core.parameterizedMessages;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.BetaParameters;
import com.analog.lyric.util.test.SerializationTester;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestBetaParameters extends TestParameterizedMessage
{
	@Test
	public void test()
	{
		BetaParameters msg = new BetaParameters();
		assertInvariants(msg);
		assertEquals(0.0, msg.getAlphaMinusOne(), 0.0);
		assertEquals(0.0, msg.getBetaMinusOne(), 0.0);
		
		msg.setAlpha(3.5);
		assertEquals(3.5, msg.getAlpha(), 0.0);
		msg.setBeta(2.75);
		assertEquals(2.75, msg.getBeta(), 0.0);
		assertInvariants(msg);
		
		msg.setAlphaMinusOne(2.0);
		assertEquals(3.0, msg.getAlpha(), 0.0);
		msg.setBetaMinusOne(2.0);
		assertEquals(3.0, msg.getBeta(), 0.0);
		assertInvariants(msg);
		
		msg.setNull();
		assertEquals(0.0, msg.getAlphaMinusOne(), 0.0);
		assertEquals(0.0, msg.getBetaMinusOne(), 0.0);
		
		//
		// KL divergence tests
		//
		
		// Wikipedia example
		msg.setAlpha(1.0);
		msg.setBeta(1.0);
		BetaParameters msg2 = new BetaParameters();
		msg2.setAlpha(3.0);
		msg2.setBeta(3.0);
		
		assertEquals(0.598803, msg.computeKLDivergence(msg2), 1e-6);
		assertEquals(0.267864, msg2.computeKLDivergence(msg), 1e-6);
		
		// Wikipedia example
		msg.setAlpha(3);
		msg.setBeta(.5);
		msg2.setAlpha(.5);
		msg2.setBeta(3);

		assertEquals(7.21574, msg.computeKLDivergence(msg2), 1e-5);
		assertEquals(7.21574, msg2.computeKLDivergence(msg), 1e-5);
		
		// Hand computed in MATLAB
		msg.setAlpha(1);
		msg.setBeta(2);
		msg2.setAlpha(1);
		msg2.setBeta(3);
		assertEquals(0.09453489, msg.computeKLDivergence(msg2), 1e-7);
		
		msg2.setAlpha(3);
		msg2.setBeta(2);
		assertEquals(1.20824053, msg.computeKLDivergence(msg2), 1e-7);
	}
	
	private void assertInvariants(BetaParameters msg)
	{
		assertGenericInvariants(msg);
		
		assertEquals(msg.getAlpha() - 1, msg.getAlphaMinusOne(), 0.0);
		assertEquals(msg.getBeta() - 1, msg.getBetaMinusOne(), 0.0);

		BetaParameters msg2 = msg.clone();
		assertNotSame(msg, msg2);
		assertEquals(msg.getAlpha(), msg2.getAlpha(), 0.0);
		assertEquals(msg.getBeta(), msg2.getBeta(), 0.0);

		BetaParameters msg3 = SerializationTester.clone(msg);
		assertEquals(msg.getAlpha(), msg3.getAlpha(), 0.0);
		assertEquals(msg.getBeta(), msg3.getBeta(), 0.0);
	}
}
