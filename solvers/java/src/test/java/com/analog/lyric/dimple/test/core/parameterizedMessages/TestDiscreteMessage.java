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

import static com.analog.lyric.math.Utilities.*;
import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;
import com.analog.lyric.util.test.SerializationTester;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestDiscreteMessage extends TestParameterizedMessage
{
	@Test
	public void test()
	{
		DiscreteMessage msg = new DiscreteWeightMessage(10);
		assertInvariants(msg);
		
		for (int i = msg.size(); --i>=0;)
		{
			assertEquals(.1, msg.getWeight(i), 1e-14);
			msg.setWeight(i,i);
			assertEquals(i, msg.getWeight(i), 0.0);
			assertEquals(weightToEnergy(i), msg.getEnergy(i), 1e-14);
			msg.setEnergy(i,i);
			assertEquals(i, msg.getEnergy(i), 0.0);
		}
		
		msg = new DiscreteWeightMessage(new double[] { 4, 5, 6 });
		assertInvariants(msg);
		assertEquals(4, msg.getWeight(0), 0.0);
		assertEquals(5, msg.getWeight(1), 0.0);
		assertEquals(6, msg.getWeight(2), 0.0);
		
		msg = new DiscreteEnergyMessage(10);
		assertInvariants(msg);
		
		for (int i = msg.size(); --i>=0;)
		{
			assertEquals(1.0, msg.getWeight(i), 1e-14);
			msg.setWeight(i,i);
			assertEquals(i, msg.getWeight(i), 1e-14);
			assertEquals(weightToEnergy(i), msg.getEnergy(i), 1e-14);
			msg.setEnergy(i,i);
			assertEquals(i, msg.getEnergy(i), 0.0);
		}
		
		msg = new DiscreteEnergyMessage(new double[] { 4, 5, 6 });
		assertInvariants(msg);
		assertEquals(4, msg.getEnergy(0), 0.0);
		assertEquals(5, msg.getEnergy(1), 0.0);
		assertEquals(6, msg.getEnergy(2), 0.0);
		
		msg = new DiscreteWeightMessage(10);
		DiscreteMessage msg2 = new DiscreteEnergyMessage(10);
		Random rand = new Random(42);
		for (int i = 0; i < msg.size(); ++i)
		{
			msg.setWeight(i, rand.nextDouble());
			msg2.setWeight(i,  rand.nextDouble());
		}

		assertEquals(expectedKL(msg, msg2), msg.computeKLDivergence(msg2), 1e-14);
		
		expectThrow(IllegalArgumentException.class, msg, "computeKLDivergence", new DiscreteEnergyMessage(3));
	}
	
	private double expectedKL(DiscreteMessage msg1, DiscreteMessage msg2)
	{
		double KL = 0.0;
		
		double total1 = 0.0, total2 = 0.0;
		for (int i = msg1.size(); --i>=0;)
		{
			total1 += msg1.getWeight(i);
			total2 += msg2.getWeight(i);
		}
		
		for (int i = msg1.size(); --i>=0;)
		{
			double p = msg1.getWeight(i)/total1;
			double q = msg2.getWeight(i)/total2;
			KL += p * Math.log(p/q);
		}
		
		return KL;
	}
	
	private void assertInvariants(DiscreteMessage message)
	{
		assertGenericInvariants(message);
		
		final int size = message.size();
		for (int i = 0; i < size; ++i)
		{
			assertEquals(message.getWeight(i), energyToWeight(message.getEnergy(i)), 1e-15);
		}
		
		expectThrow(ArrayIndexOutOfBoundsException.class, message, "getWeight", -1);
		expectThrow(ArrayIndexOutOfBoundsException.class, message, "getWeight", size);
		
		DiscreteMessage message2 = message.clone();
		assertEquals(message.size(), message2.size());
		for (int i = 0; i < size; ++i)
		{
			assertEquals(message.getWeight(i), message2.getWeight(i), 0.0);
		}

		DiscreteMessage message3 = SerializationTester.clone(message);
		assertEquals(message.size(), message3.size());
		for (int i = 0; i < size; ++i)
		{
			assertEquals(message.getWeight(i), message3.getWeight(i), 0.0);
		}
	}
}
