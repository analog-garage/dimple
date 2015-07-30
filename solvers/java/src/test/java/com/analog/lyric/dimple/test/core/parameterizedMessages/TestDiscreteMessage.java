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
import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import com.analog.lyric.dimple.data.DataRepresentationType;
import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.exceptions.NormalizationException;
import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
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
		DiscreteMessage msg = new DiscreteWeightMessage(10);
		assertInvariants(msg);
		
		for (int i = msg.size(); --i>=0;)
		{
			assertEquals(1.0, msg.getWeight(i), 1e-14);
			msg.setWeight(i,i);
			assertEquals(i, msg.getWeight(i), 0.0);
			assertEquals(weightToEnergy(i), msg.getEnergy(i), 1e-14);
			msg.setEnergy(i,i);
			assertEquals(i, msg.getEnergy(i), 0.0);
		}
		assertEquals(weightToEnergy(msg.sumOfWeights()), msg.getNormalizationEnergy(), 0.0);

		double sum = msg.sumOfWeights();
		msg.setNormalizationEnergy(0.0);
		msg.normalize();
		assertEquals(1.0, msg.sumOfWeights(), 1e-10);
		assertInvariants(msg);
		assertEquals(sum, energyToWeight(msg.getNormalizationEnergy()), 1e-10);
		
		Arrays.fill(msg.representation(), 23);
		msg.setNull();
		for (int i=  msg.size(); --i>= 0;)
		{
			assertEquals(0.0, msg.getEnergy(i), 0.0);
		}

		Arrays.fill(msg.representation(), 23);
		msg.setUniform();
		for (int i = msg.size(); --i>= 0;)
		{
			assertEquals(1.0 / msg.size(), msg.getWeight(i), 1e-14);
		}
		
		msg = new DiscreteWeightMessage(new double[] { 4, 5, 6 });
		assertInvariants(msg);
		assertEquals(4, msg.getWeight(0), 0.0);
		assertEquals(5, msg.getWeight(1), 0.0);
		assertEquals(6, msg.getWeight(2), 0.0);
		
		msg.setDeterministicIndex(1);
		assertEquals(1, msg.toDeterministicValueIndex());
		assertInvariants(msg);
		
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
		
		msg.setDeterministic(Value.create(DiscreteDomain.range(1,  msg.size()),2));
		assertEquals(1, msg.toDeterministicValueIndex());
		assertInvariants(msg);
		
		msg = new DiscreteEnergyMessage(new double[] { 4, 5, 6 });
		assertInvariants(msg);
		assertEquals(4, msg.getEnergy(0), 0.0);
		assertEquals(5, msg.getEnergy(1), 0.0);
		assertEquals(6, msg.getEnergy(2), 0.0);
		
		sum = msg.sumOfWeights();
		msg.setNormalizationEnergy(0.0);
		msg.normalize();
		assertEquals(sum, energyToWeight(msg.getNormalizationEnergy()), 1e-10);
		
		msg = new DiscreteWeightMessage(10);
		DiscreteEnergyMessage msg2 = new DiscreteEnergyMessage(10);
		Random rand = new Random(42);
		for (int i = 0; i < msg.size(); ++i)
		{
			msg.setWeight(i, rand.nextDouble());
			msg2.setWeight(i,  rand.nextDouble());
		}

		assertEquals(expectedKL(msg, msg2), msg.computeKLDivergence(msg2), 1e-14);
		assertInvariants(msg);
		assertInvariants(msg2);
		
		msg2.normalizeEnergy();
		assertInvariants(msg2);
		assertEquals(0.0, Doubles.min(msg2.representation()), 0.0);
		
		expectThrow(IllegalArgumentException.class, msg, "computeKLDivergence", new DiscreteEnergyMessage(3));
	}
	

	/**
	 * Test {@link DiscreteEnergyMessage#createFrom}/{@linkplain DiscreteEnergyMessage#convertFrom convertFrom}.
	 * 
	 * @since 0.08
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateFrom()
	{
		DiscreteDomain domain = DiscreteDomain.range(1,3);
		DiscreteEnergyMessage msg123 = new DiscreteEnergyMessage(new double[] {1,2,3});
		
		List<? extends IDatum> empty = Collections.emptyList();
		assertNull(DiscreteEnergyMessage.convertFrom(domain, empty));
		
		assertSame(msg123, DiscreteEnergyMessage.convertFrom(domain, Arrays.asList(msg123)));
		DiscreteEnergyMessage msg = DiscreteEnergyMessage.createFrom(domain,  Arrays.asList(msg123));
		assertNotSame(msg123, msg);
		assertTrue(msg123.objectEquals(msg));
		
		Value value = Value.createWithIndex(domain, 1);
		msg = DiscreteEnergyMessage.convertFrom(domain, Arrays.asList(value));
		assertEquals(1, requireNonNull(msg).toDeterministicValueIndex());
		assertEquals(0.0, msg.evalEnergy(value), 0.0);
		
		msg = DiscreteEnergyMessage.createFrom(domain, Arrays.asList(msg123, value));
		assertEquals(1, requireNonNull(msg).toDeterministicValueIndex());
		// Still deterministic, but incorporates energy from msg123
		assertEquals(2.0, msg.evalEnergy(value), 0.0);
		
		msg = DiscreteEnergyMessage.createFrom(domain, Arrays.asList(value, msg123));
		assertEquals(1, requireNonNull(msg).toDeterministicValueIndex());
		// Elements after value are ignored:
		assertEquals(0.0, msg.evalEnergy(value), 0.0);
		
		
		DiscreteEnergyMessage msg456 = new DiscreteEnergyMessage(new double[] {4, 5, 6});
		msg = DiscreteEnergyMessage.createFrom(domain, Arrays.asList(msg456, msg123));
		assertArrayEquals(new double[] { 5, 7, 9 }, requireNonNull(msg).getEnergies(), 0.0);
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
		final Value objValue = Value.create("");
		final Value discreteValue = Value.create(DiscreteDomain.range(1, size));
		for (int i = 0; i < size; ++i)
		{
			assertEquals(message.getWeight(i), energyToWeight(message.getEnergy(i)), 1e-15);
			objValue.setObject(i);
			assertEquals(message.getEnergy(i), message.evalEnergy(objValue), 0.0);
			discreteValue.setIndex(i);
			assertEquals(message.getEnergy(i), message.evalEnergy(discreteValue), 0.0);
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
		
		int onlyNonZeroWeightIndex = -1;
		for (int i = 0; i < size; ++i)
		{
			final double w = message.getWeight(i);
			if (w == 0)
			{
				assertTrue(message.hasZeroWeight(i));
			}
			else
			{
				assertFalse(message.hasZeroWeight(i));
				if (onlyNonZeroWeightIndex < 0)
					onlyNonZeroWeightIndex = i;
				else
				{
					onlyNonZeroWeightIndex = -1;
					break;
				}
			}
		}
		if (onlyNonZeroWeightIndex >= 0)
		{
			assertTrue(message.hasDeterministicValue());
			assertEquals(onlyNonZeroWeightIndex, message.toDeterministicValueIndex());
			assertEquals(onlyNonZeroWeightIndex,
				requireNonNull(message.toDeterministicValue(DiscreteDomain.range(1, size))).getIndex());
		}
		else
		{
			assertFalse(message.hasDeterministicValue());
			assertNull(message.toDeterministicValue(DiscreteDomain.range(1, size)));
			assertEquals(-1, message.toDeterministicValueIndex());
		}
		
		
		expectThrow(ArrayIndexOutOfBoundsException.class, message, "getWeight", -1);
		expectThrow(ArrayIndexOutOfBoundsException.class, message, "getWeight", size);
		expectThrow(ClassCastException.class, message, "setFrom", new NormalParameters());
		expectThrow(IllegalArgumentException.class, message, "setWeights", new double[size+1]);
		expectThrow(IllegalArgumentException.class, message, "setEnergies", new double[size+1]);
		expectThrow(IllegalArgumentException.class, ".* is not discrete", message, "setDeterministic",
			Value.create("hi"));
		
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
		
		message2.setNormalizationEnergy(42);
		assertEquals(42, message2.getNormalizationEnergy(), 0.0);
		
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
		assertEquals(Double.POSITIVE_INFINITY, message3.getNormalizationEnergy(), 0.0);
		expectThrow(NormalizationException.class, ".*weights add up to zero", message3, "normalize");
		
		message3.setFrom(message);
		assertTrue(message.objectEquals(message3));
		
		message3.setNormalizationEnergy(Double.NaN); // unset
		message3.normalize();
		assertEquals(1.0, message3.sumOfWeights(), 1e-15);
		assertEquals(0.0, message3.getNormalizationEnergy(), 0.0);
		
		message3.setFrom(message);
		message3.setNormalizationEnergy(0.0);
		message3.normalize();
		assertEquals(1.0, message3.sumOfWeights(), 1e-15);
		assertEquals(weightToEnergy(message.sumOfWeights()), message3.getNormalizationEnergy(), 1e-15);
		
		message3.setNull();
		assertEquals(weightToEnergy(size), message3.getNormalizationEnergy(), 0.0);
		for (int i = 0; i < size; ++i)
		{
			discreteValue.setIndex(i);
			message3.evalEnergy(discreteValue);
		}
		if (message3.storesWeights())
		{
			assertEquals(size, message3.sumOfWeights(), 0.0);
			message3.setWeights(message.representation());
		}
		else
		{
			assertEquals(size, message3.sumOfWeights(), 0.0);
			message3.setEnergies(message.representation());
		}
		
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
		
		message3.addWeightsFrom(message);
		for (int i = size; --i>=0;)
		{
			assertEquals(message.getWeight(i) * 2, message3.getWeight(i), 1e-10);
		}
		
		message3.setNormalizationEnergy(Double.NaN); // unset normalization energy
		message3.normalize();
		assertEquals(0.0, message3.getNormalizationEnergy(), 0.0);
		
		message3.setNull();
		message3.addEnergiesFrom(message);
		assertArrayEquals(message.getEnergies(), message3.getEnergies(), 0.0);
		message3.addFrom(message);
		for (int i = size; --i>=0;)
		{
			assertEquals(message.getEnergy(i) * 2, message3.getEnergy(i), 1e-10);
		}
	
		DiscreteMessage message4 = message.storesWeights() ? new DiscreteEnergyMessage(size) :
			new DiscreteWeightMessage(size);
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
		
		message4.setNull();
		message4.addEnergiesFrom(message);
		assertArrayEquals(message.getEnergies(), message4.getEnergies(), 1e-15);
		message4.addFrom(message);
		for (int i = size; --i>=0;)
		{
			assertEquals(message.getEnergy(i) * 2, message4.getEnergy(i), 1e-10);
		}
		
		DiscreteMessage message5 = new DiscreteEnergyMessage(message4);
		assertArrayEquals(message4.getWeights(), message5.getWeights(), 1e-15);
		message5 = new DiscreteWeightMessage(message4);
		assertArrayEquals(message4.getWeights(), message5.getWeights(), 1e-15);
		
		DiscreteDomain domain = DiscreteDomain.range(1, size);
		message5.setFrom(domain, new IndexPlusOne());
		for (int i = size; --i>=0;)
		{
			assertEquals(i + 1, message5.getEnergy(i), 0.0);
		}
		
		message5.setFrom(domain, Value.createWithIndex(domain, 0));
		assertEquals(1.0, message5.getWeight(0), 0.0);
		for (int i = 1; i < size; ++i)
		{
			assertEquals(0.0, message5.getWeight(i), 0.0);
		}

		message5.setFrom(domain, Value.create(1));
		assertEquals(1.0, message5.getWeight(0), 0.0);
		for (int i = 1; i < size; ++i)
		{
			assertEquals(0.0, message5.getWeight(i), 0.0);
		}
		
		message5.setFrom(domain, message);
		assertArrayEquals(message.getWeights(), message5.getWeights(), 1e-15);
		
		message5 = new DiscreteWeightMessage(domain, null);
		assertEquals(size, message5.size());
		for (double w : message5.getWeights())
			assertEquals(1.0/size, w, 1e-15);
		
		message5 = new DiscreteWeightMessage(domain, new IndexPlusOne());
		for (int i = size; --i>=0;)
		{
			assertEquals(i + 1, message5.getEnergy(i), 0.0);
		}

		message5 = new DiscreteEnergyMessage(domain, new IndexPlusOne());
		for (int i = size; --i>=0;)
		{
			assertEquals(i + 1, message5.getEnergy(i), 0.0);
		}
	}
	
	private static class IndexPlusOne implements IUnaryFactorFunction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public DataRepresentationType representationType()
		{
			return DataRepresentationType.FUNCTION;
		}

		@Override
		public boolean objectEquals(@Nullable Object other)
		{
			return other instanceof IndexPlusOne;
		}

		@Override
		public IUnaryFactorFunction clone()
		{
			return this;
		}

		@Override
		public double evalEnergy(Value value)
		{
			return value.getIndex() + 1;
		}

		@Override
		public double evalEnergy(Object value)
		{
			return Double.POSITIVE_INFINITY;
		}
	}
}
