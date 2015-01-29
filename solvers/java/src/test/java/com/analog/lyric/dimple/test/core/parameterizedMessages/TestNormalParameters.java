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

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.util.test.SerializationTester;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestNormalParameters extends TestParameterizedMessage
{
	@Test
	public void test()
	{
		NormalParameters msg = new NormalParameters();
		assertInvariants(msg);
		assertEquals(0.0, msg.getMean(), 0.0);
		assertEquals(0.0, msg.getPrecision(), 0.0);
		
		msg.setPrecision(Double.POSITIVE_INFINITY);
		assertEquals(Double.POSITIVE_INFINITY, msg.getPrecision(), 0.0);
		assertInvariants(msg);
		
		msg.setMean(4.2);
		assertEquals(4.2, msg.getMean(), 0.0);
		
		msg.setVariance(1.5);
		assertEquals(1.5, msg.getVariance(), 1e-14);
		assertInvariants(msg);
		
		msg.setStandardDeviation(2.3);
		assertEquals(2.3, msg.getStandardDeviation(), 1e-14);
		
		NormalParameters msg2 = new NormalParameters(10.0, 2.0);
		assertEquals(10.0, msg2.getMean(), 0.0);
		assertEquals(2.0, msg2.getPrecision(), 0.0);
		
		msg.set(msg2);
		assertEquals(10.0, msg.getMean(), 0.0);
		assertEquals(2.0, msg2.getPrecision(), 0.0);
		
		// if precisions are equal then KL is simply one half the precision of the second message
		// times the squared difference of the means.
		msg.setMean(9.0);
		assertEquals(1, msg.computeKLDivergence(msg2), 1e-14);
		assertEquals(1, msg2.computeKLDivergence(msg), 1e-14);
		msg.setMean(12.0);
		assertEquals(4, msg.computeKLDivergence(msg2), 1e-14);

		msg2.setMean(12);
		msg2.setPrecision(10);
		assertEquals((4 - Math.log(5))/2, msg.computeKLDivergence(msg2), 1e-14);

		msg.setNull();
		assertEquals(0.0, msg.getMean(), 0.0);
		assertEquals(0.0, msg.getPrecision(), 0.0);
		
		msg2.setUniform();
		assertEquals(0.0, msg2.getMean(), 0.0);
		assertEquals(0.0, msg2.getPrecision(), 0.0);
		
	}
	
	private void assertInvariants(NormalParameters message)
	{
		assertGenericInvariants(message);
		assertEquals(1.0/message.getPrecision(), message.getVariance(), 1e-14);
		assertEquals(Math.sqrt(message.getVariance()), message.getStandardDeviation(), 1e-14);
		
		NormalParameters message2 = message.clone();
		assertEquals(message.getPrecision(), message2.getPrecision(), 0.0);
		assertEquals(message.getMean(), message2.getMean(), 0.0);
		
		NormalParameters message3 = SerializationTester.clone(message);
		assertEquals(message.getPrecision(), message3.getPrecision(), 0.0);
		assertEquals(message.getMean(), message3.getMean(), 0.0);
	}
}
