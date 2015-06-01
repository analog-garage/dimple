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

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteNormalizedEnergyMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteNormalizedWeightMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.util.test.SerializationTester;
import com.google.common.primitives.Doubles;

/**
 * Unit test for {@link DiscreteMessage} implementations
 * @since 0.06
 * @author Christopher Barber
 */
public class TestDiscreteMessage extends TestParameterizedMessage
{
	@Test
	public void test()
	{
		DiscreteMessage msg = new DiscreteNormalizedWeightMessage(10);
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
		assertEquals(0.0, msg.getNormalizationEnergy(), 0.0);
		
		double sum = msg.sumOfWeights();
		msg.normalize();
		assertEquals(1.0, msg.sumOfWeights(), 1e-10);
		assertInvariants(msg);
		assertEquals(sum, energyToWeight(msg.getNormalizationEnergy()), 1e-10);
		
		Arrays.fill(msg.representation(), 23);
		msg.setNull();
		for (int i=  msg.size(); --i>= 0;)
		{
			assertEquals(.1, msg.getWeight(i), 1e-14);
		}

		Arrays.fill(msg.representation(), 23);
		msg.setUniform();
		for (int i=  msg.size(); --i>= 0;)
		{
			assertEquals(.1, msg.getWeight(i), 1e-14);
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
		
		Arrays.fill(msg.representation(), 23);
		msg.setNull();
		for (int i=  msg.size(); --i>= 0;)
		{
			assertEquals(0, msg.getEnergy(i), 0.0);
		}

		Arrays.fill(msg.representation(), 23);
		msg.setUniform();
		for (int i=  msg.size(); --i>= 0;)
		{
			assertEquals(0, msg.getEnergy(i), 0.0);
		}
		
		msg = new DiscreteNormalizedEnergyMessage(new double[] { 4, 5, 6 });
		assertInvariants(msg);
		assertEquals(4, msg.getEnergy(0), 0.0);
		assertEquals(5, msg.getEnergy(1), 0.0);
		assertEquals(6, msg.getEnergy(2), 0.0);
		
		sum = msg.sumOfWeights();
		msg.normalize();
		assertEquals(sum, energyToWeight(msg.getNormalizationEnergy()), 1e-10);
		
		msg = new DiscreteWeightMessage(10);
		DiscreteMessage msg2 = new DiscreteEnergyMessage(10);
		Random rand = new Random(42);
		for (int i = 0; i < msg.size(); ++i)
		{
			msg.setWeight(i, rand.nextDouble());
			msg2.setWeight(i,  rand.nextDouble());
		}

		assertEquals(expectedKL(msg, msg2), msg.computeKLDivergence(msg2), 1e-14);
		assertInvariants(msg);
		assertInvariants(msg2);
		
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
		
		assertTrue(KL >= 0.0);
		
		return KL;
	}
	
	private void assertInvariants(DiscreteMessage message)
	{
		assertGenericInvariants(message);
		
		assertFalse(message.objectEquals(null));
		assertFalse(message.objectEquals("bogus"));
		assertTrue(message.objectEquals(message));
		
		final int size = message.size();
		for (int i = 0; i < size; ++i)
		{
			assertEquals(message.getWeight(i), energyToWeight(message.getEnergy(i)), 1e-15);
		}
		
		if (message.storesWeights())
		{
			for (int i = 0; i < size; ++i)
			{
				assertEquals(message.getWeight(i), message.representation()[i], 0.0);
			}
		}
		else
		{
			for (int i = 0; i < size; ++i)
			{
				assertEquals(message.getEnergy(i), message.representation()[i], 0.0);
			}
			
			if (message instanceof DiscreteEnergyMessage)
			{
				assertEquals(Doubles.min(message.representation()), ((DiscreteEnergyMessage)message).minEnergy(), 0.0);
			}
		}
		
		if (!message.storesNormalizationEnergy())
		{
			assertEquals(0.0, message.getNormalizationEnergy(), 0.0);
			expectThrow(UnsupportedOperationException.class, message, "setNormalizationEnergy", 0.0);
		}
		
		expectThrow(ArrayIndexOutOfBoundsException.class, message, "getWeight", -1);
		expectThrow(ArrayIndexOutOfBoundsException.class, message, "getWeight", size);
		expectThrow(ClassCastException.class, message, "setFrom", new NormalParameters());
		expectThrow(IllegalArgumentException.class, message, "setWeights", new double[size+1]);
		expectThrow(IllegalArgumentException.class, message, "setEnergies", new double[size+1]);
		
		DiscreteMessage message2 = message.clone();
		assertTrue(message.objectEquals(message2));
		assertEquals(message.size(), message2.size());
		for (int i = 0; i < size; ++i)
		{
			assertEquals(message.getWeight(i), message2.getWeight(i), 0.0);
		}
		assertEquals(message.getNormalizationEnergy(), message2.getNormalizationEnergy(), 0.0);
		
		double prevDenormalizer = message2.getNormalizationEnergy();
		
		if (prevDenormalizer != message2.getNormalizationEnergy())
		{
			assertFalse(message.objectEquals(message2));
		}
		
		if (message2.storesNormalizationEnergy())
		{
			message2.setNormalizationEnergy(42);
			assertEquals(42, message2.getNormalizationEnergy(), 0.0);
		}
		else
		{
			expectThrow(UnsupportedOperationException.class, message2, "setNormalizationEnergy", 42.0);
		}
		
		DiscreteMessage message3 = SerializationTester.clone(message);
		assertTrue(message.objectEquals(message));
		assertEquals(message.size(), message3.size());
		for (int i = 0; i < size; ++i)
		{
			assertEquals(message.getWeight(i), message3.getWeight(i), 0.0);
		}
		assertEquals(message.getNormalizationEnergy(), message3.getNormalizationEnergy(), 0.0);
		
		message3.setWeightsToZero();
		assertEquals(0.0, message3.sumOfWeights(), 0.0);
		for (int i = message3.size(); --i>=0;)
		{
			assertEquals(0.0, message3.getWeight(i), 0.0);
			assertEquals(Double.POSITIVE_INFINITY, message3.getEnergy(i), 0.0);
		}
		assertEquals(0.0, message3.getNormalizationEnergy(), 0.0);
		
		message3.setFrom(message);
		assertTrue(message.objectEquals(message3));
		
		message3.setUniform();
		assertEquals(0.0, message3.getNormalizationEnergy(), 0.0);
		if (message3.storesWeights())
		{
			assertEquals(1.0, message3.sumOfWeights(), 1e-10);
			message3.setWeights(message.representation());
		}
		else
		{
			assertEquals(size, message3.sumOfWeights(), 0.0);
			message3.setEnergies(message.representation());
		}
		assertEquals(0.0, message3.getNormalizationEnergy(), 0.0);
		
		if (message3.storesNormalizationEnergy())
		{
			message3.setNormalizationEnergy(message.getNormalizationEnergy());
			assertTrue(message3.objectEquals(message));
			
			message3.setNormalizationEnergy(message3.getNormalizationEnergy() + 1.0);
			assertFalse(message.objectEquals(message3));
			assertEquals(message.getNormalizationEnergy() + 1.0, message3.getNormalizationEnergy(), 0.0);
			
			message3.setWeight(0, message3.getWeight(0) + 1.0);
			assertFalse(message.objectEquals(message3));
			message3.setNormalizationEnergy(message.getNormalizationEnergy());
			assertFalse(message.objectEquals(message3));
			
			message3.setFrom((IParameterizedMessage)message);
			assertTrue(message.objectEquals(message3));
		}
		
		message3.addWeightsFrom(message);
		for (int i = size; --i>=0;)
		{
			assertEquals(message.getWeight(i) * 2, message3.getWeight(i), 1e-10);
		}

		DiscreteMessage message4 = message.storesWeights() ? new DiscreteNormalizedEnergyMessage(size) :
			new DiscreteNormalizedWeightMessage(size);
		assertFalse(message.objectEquals(message4));
		message4.setFrom(message);
		assertFalse(message.objectEquals(message4));
		assertEquals(message.getNormalizationEnergy(), message4.getNormalizationEnergy(), 0.0);
		assertEquals(0.0, message.computeKLDivergence(message4), 1e-10);
		
		message4.addWeightsFrom(message);
		for (int i = size; --i>=0;)
		{
			assertEquals(message.getWeight(i) * 2, message4.getWeight(i), 1e-10);
		}
	}
}
