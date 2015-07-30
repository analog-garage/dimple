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

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Dirichlet;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DirichletParameters;
import com.analog.lyric.util.test.SerializationTester;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestDirichletParameters extends TestParameterizedMessage
{
	@Test
	public void test()
	{
		DirichletParameters msg = new DirichletParameters();
		assertEquals(0, msg.getSize());
		assertInvariants(msg);
		expectThrow(ArrayIndexOutOfBoundsException.class, msg, "getAlpha", 0);
		
		msg = new DirichletParameters(3);
		assertEquals(3, msg.getSize());
		for (int i = 0; i < 3; ++i)
		{
			assertEquals(1.0, msg.getAlpha(i), 0.0);
		}
		assertInvariants(msg);
		
		
		DirichletParameters msg2 = new DirichletParameters(new double[] {2,2,2});
		assertEquals(3, msg2.getSize());
		for (int i = 0; i < 3; ++i)
		{
			assertEquals(3.0, msg2.getAlpha(i), 0.0);
		}
		assertInvariants(msg2);
		
		assertEquals(1.16798581949, msg.computeKLDivergence(msg2), 1e-9);
		assertEquals(0.5248713233626, msg2.computeKLDivergence(msg), 1e-9);
		
		msg2.setSize(4);
		assertEquals(4, msg2.getSize());
		for (int i = 0; i < 4; ++i)
		{
			assertEquals(1.0, msg2.getAlpha(i), 0.0);
		}
		
		expectThrow(IllegalArgumentException.class, "Incompatible Dirichlet sizes.*", msg, "computeKLDivergence", msg2);
		
		msg2.add(0, 1.2);
		assertEquals(2.2, msg2.getAlpha(0), 1e-15);
		assertEquals(1.0, msg2.getAlpha(1), 0.0);
		
		msg2.increment(0);
		msg2.increment(1);
		assertEquals(3.2, msg2.getAlpha(0), 1e-15);
		assertEquals(2, msg2.getAlpha(1), 0.0);
		assertEquals(1, msg2.getAlpha(2), 0.0);
		
		msg2.fillAlphaMinusOne(4);
		for (int i = 0; i < msg2.getSize(); ++i)
		{
			assertEquals(5, msg2.getAlpha(i), 0.0);
		}
		
		expectThrow(IllegalArgumentException.class, msg2, "add", msg);
		msg2.add(new DirichletParameters(4, 1.5));
		for (int i = 0; i < 4; ++i)
		{
			assertEquals(6.5, msg2.getAlpha(i), 1e-15);
		}
		
		msg2.setAlphaMinusOne(new double[] { 10, 11, 12 });
		assertEquals(3, msg2.getSize());
		msg2.add(new int[] { 1, 2, 3});
		assertEquals(11, msg2.getAlphaMinusOne(0), 0.0);
		assertEquals(13, msg2.getAlphaMinusOne(1), 0.0);
		assertEquals(15, msg2.getAlphaMinusOne(2), 0.0);
		assertFalse(msg2.isSymmetric());
		
		msg2.setNull();
		assertEquals(3, msg2.getSize());
		for (int i = 0; i < 3; ++i)
		{
			assertEquals(0.0, msg2.getAlphaMinusOne(i), 0.0);
		}
		assertTrue(msg2.isSymmetric());
		
		msg2.setUniform();
		assertEquals(3, msg2.getSize());
		for (int i = 0; i < 3; ++i)
		{
			assertEquals(0.0, msg2.getAlphaMinusOne(i), 0.0);
		}
		assertTrue(msg2.isSymmetric());
		
		msg2.setAlphaMinusOne(new double[] { 1, 2, 3});
		for (int i = 0; i < 3; ++i)
		{
			assertEquals(i + 1, msg2.getAlphaMinusOne(i), 0.0);
		}
	}
	
	private void assertInvariants(DirichletParameters msg)
	{
		final int n = msg.getSize();
		assertGenericInvariants(msg);
		
		DirichletParameters msg2 = msg.clone();
		assertNotSame(msg2, msg);
		assertEquals(n, msg2.getSize());
		for (int i = 0; i < n; ++i)
		{
			assertEquals(msg.getAlpha(i), msg2.getAlpha(i), 0.0);
		}
		assertEquals(msg.isSymmetric(), msg2.isSymmetric());
		
		DirichletParameters msg3 = SerializationTester.clone(msg);
		assertNotSame(msg3, msg);
		assertEquals(n, msg3.getSize());
		for (int i = 0; i < n; ++i)
		{
			assertEquals(msg.getAlpha(i), msg3.getAlpha(i), 0.0);
		}
		
		for (int i = 0; i < n; ++i)
		{
			assertEquals(msg.getAlpha(i) - 1, msg.getAlphaMinusOne(i), 1e-14);
		}

		if (n > 0)
		{
			Value value = Value.create(RealJointDomain.create(n));
			Dirichlet factor = new Dirichlet(msg.getAlphas());
			
			boolean isNull = msg.isNull();

			for (int i = 0; i < 10; ++i)
			{
				double[] array = value.getDoubleArray();
				double sum = 0.0;
				for (int j = n; --j>=0;)
					sum += array[j] = testRand.nextDouble();
				for (int j = n; --j>=0;)
					array[j] /= sum;
				assertEquals(factor.evalEnergy(value), msg.evalEnergy(value)- msg.getNormalizationEnergy(), 1e-12);
				if (isNull)
					assertEquals(0.0, msg.evalEnergy(value), 0.0);
			}
			
			Arrays.fill(value.getDoubleArray(), 2.0); // not on probability simplex
			assertEquals(Double.POSITIVE_INFINITY, msg.evalEnergy(value), 0.0);
			
			value.getDoubleArray()[0] = -2*n - 1;
			assertEquals(Double.POSITIVE_INFINITY, msg.evalEnergy(value), 0.0);
		}
	}
}
