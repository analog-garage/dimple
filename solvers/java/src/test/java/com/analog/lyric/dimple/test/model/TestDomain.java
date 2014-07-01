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

package com.analog.lyric.dimple.test.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.ComplexDomain;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.DoubleRangeDomain;
import com.analog.lyric.dimple.model.domains.EnumDomain;
import com.analog.lyric.dimple.model.domains.IntDomain;
import com.analog.lyric.dimple.model.domains.IntRangeDomain;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.domains.ObjectDomain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;
import com.analog.lyric.util.test.SerializationTester;

public class TestDomain
{
	enum Bogus { X }
	
	enum E { A, B, C }
	
	@Test
	public void test()
	{
		//
		// Test reals
		//
		
		assertReal(RealDomain.unbounded(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		assertSame(RealDomain.unbounded(), RealDomain.create(Double.NEGATIVE_INFINITY,  Double.POSITIVE_INFINITY));
		assertReal(RealDomain.nonNegative(), 0.0, Double.POSITIVE_INFINITY);
		assertSame(RealDomain.nonNegative(), RealDomain.create(0.0, Double.POSITIVE_INFINITY));
		assertReal(RealDomain.nonPositive(), Double.NEGATIVE_INFINITY, 0.0);
		assertSame(RealDomain.nonPositive(), RealDomain.create(Double.NEGATIVE_INFINITY, 0.0));
		
		assertNotEquals(RealDomain.unbounded(), RealDomain.nonNegative());
		assertNotEquals(RealDomain.unbounded(), RealDomain.nonPositive());
		
		assertNotEquals(RealDomain.unbounded().hashCode(), RealDomain.nonNegative().hashCode());
		assertFalse(RealDomain.unbounded().hasIntCompatibleValues());
		
		RealDomain unit = RealDomain.create(0.0, 1.0);
		assertReal(unit, 0.0, 1.0);
		
		RealDomain percentile = RealDomain.create(0.0, 100.0);
		assertSame(percentile, RealDomain.create(0.0, 100.0));
		assertReal(percentile, 0.0, 100.0);
		
		//
		// Test discretes
		//
		
		IntRangeDomain bit = DiscreteDomain.bit();
		assertInvariants(bit);
		assertEquals(2, bit.size());
		assertEquals(0, bit.getIntElement(0));
		assertEquals(1, bit.getIntElement(1));
		assertSame(bit, DiscreteDomain.create(0, 1));
		assertTrue(bit.isIntegral());
		assertEquals("{0,1}", bit.toString());
		
		TypedDiscreteDomain<Integer> reverseBit = DiscreteDomain.create(1, 0);
		assertNotEquals(bit, reverseBit);
		assertInvariants(reverseBit);
		assertTrue(reverseBit.isIntegral());
		assertEquals("{1,0}", reverseBit.toString());
		
		TypedDiscreteDomain<Boolean> bool = DiscreteDomain.bool();
		assertInvariants(bool);
		assertEquals(2, bool.size());
		assertFalse(bool.getElement(0));
		assertTrue(bool.getElement(1));
		assertNotEquals(bit, bool);
		assertNotEquals(bit.hashCode(), bool.hashCode());
		assertSame(bool, DiscreteDomain.create(false, true));
		assertNotEquals(bool, DiscreteDomain.create(true, false));
		assertFalse(bool.isIntegral());
		assertEquals("{false,true}", bool.toString());
		
		EnumDomain<E> e = DiscreteDomain.forEnum(E.class);
		assertInvariants(e);
		assertNotEquals(e, DiscreteDomain.forEnum(Bogus.class));
		assertSame(e, DiscreteDomain.create(E.A, E.B, E.C));
		assertEquals("{A,B,C}", e.toString());
		
		try
		{
			DiscreteDomain.range(1,2,0);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Non-positive interval"));
		}
		
		try
		{
			DiscreteDomain.range(2,1);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("upper bound lower than lower bound"));
		}
		
		IntRangeDomain r1to5 = DiscreteDomain.range(1,5);
		assertInvariants(r1to5);
		assertTrue(r1to5.getIndex(1.01) < 0);
		assertTrue(r1to5.getIndex(6) < 0);
		for (int i = 0; i < 5; ++i)
		{
			assertEquals(i + 1, r1to5.getIntElement(i));
		}
		assertSame(r1to5, DiscreteDomain.create(1,2,3,4,5));
		assertEquals("{1,2,3,4,5}", r1to5.toString());
		
		IntRangeDomain r0to4 = DiscreteDomain.range(0,4);
		assertInvariants(r0to4);
		assertEquals(5, r0to4.size());
		assertNotEquals(r0to4, r1to5);
		assertNotEquals(r0to4.hashCode(), r1to5.hashCode());
		for (int i = 0; i < 5; ++i)
		{
			assertEquals(i, r0to4.getIndex(i));
		}
		assertSame(r0to4, DiscreteDomain.create(0, 1, 2, 3, 4));
		
		IntRangeDomain evenDigits = DiscreteDomain.range(0, 8, 2);
		assertInvariants(evenDigits);
		assertEquals(5, evenDigits.size());
		assertNotEquals(evenDigits, r0to4);
		for (int i = 0; i <5; ++i)
		{
			assertEquals(i*2, evenDigits.getIntElement(i));
			assertEquals(i, evenDigits.getIndex(i*2));
		}
		assertSame(evenDigits, DiscreteDomain.create(0,2,4,6,8));
		assertEquals("{0,2,4,6,8}", evenDigits.toString());
		
		IntRangeDomain r0to3 = DiscreteDomain.range(0, 3);
		assertInvariants(r0to3);
		assertNotEquals(r0to3, r0to4);
		assertSame(r0to3, DiscreteDomain.create(0,1,2,3));
		assertNotEquals(r0to3, DiscreteDomain.create(0,1.0,2,3));
		
		TypedDiscreteDomain<Integer> r3to0 = DiscreteDomain.create(3,2,1,0);
		assertInvariants(r3to0);
		assertNotEquals(r0to3, r3to0);
		
		IntRangeDomain r1to99 = DiscreteDomain.range(1,99);
		assertInvariants(r1to99);
		assertEquals("{1,2,3,4,5,6,7,8,9,...,99}", r1to99.toString());
		
		try
		{
			DiscreteDomain.range(1.0, 2.0, 0.0);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Non-positive interval"));
		}
		
		try
		{
			DiscreteDomain.range(2.0,1.0);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("upper bound lower than lower bound"));
		}
		
		try
		{
			DiscreteDomain.range(1.0, 5.0, 1.0, .5);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("too large for interval"));
		}
		
		try
		{
			DiscreteDomain.range(1.0, 5.0, 1.0, -1);
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Negative tolerance"));
		}

		try
		{
			DiscreteDomain.range(0, Integer.MAX_VALUE);
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("more than MAX_INTEGER"));
		}
		
		DoubleRangeDomain halves = DiscreteDomain.range(0.0, 2.5, .5);
		assertInvariants(halves);
		assertSame(halves, DiscreteDomain.create(0.0, 0.5, 1.0, 1.5, 2.0, 2.5));
		
		DoubleRangeDomain d0to3 = DiscreteDomain.range(0.0, 3.0);
		assertInvariants(d0to3);
		assertNotEquals(d0to3, halves);
		
		DoubleRangeDomain d0to3Loose = DiscreteDomain.range(0.0, 3.0, 1.0, .3);
		assertInvariants(d0to3Loose);
		assertNotEquals(d0to3, d0to3Loose);
		
		TypedDiscreteDomain<Double> d3to0 = DiscreteDomain.create(3.0, 2.0, 1.0);
		assertInvariants(d3to0);
		assertNotEquals(d0to3, d3to0);
		
		JointDiscreteDomain<?> joint1 = DiscreteDomain.joint(e, evenDigits);
		assertInvariants(joint1);
		assertEquals("{A,B,C}x{0,2,4,6,8}", joint1.toString());
		
		JointDiscreteDomain<E> joint2 = DiscreteDomain.joint(e, e);
		assertInvariants(joint2);
		assertNotEquals(joint1, joint2);

		JointDiscreteDomain<Integer> joint3 = DiscreteDomain.joint(r1to5, r0to3);
		assertInvariants(joint2);
		assertEquals(0, joint3.getIndex(new int[] { 1, 0 }));
		assertEquals(-1, joint3.getIndex(new int[] { 0, 1 }));
		
		TypedDiscreteDomain<Integer> primes = DiscreteDomain.create(2,3,5,7,11,13);
		assertInvariants(primes);
		
		TypedDiscreteDomain<Double> powerOfTwo = DiscreteDomain.create(.125, .25, .5, 1, 2, 4, 8);
		assertInvariants(powerOfTwo);
		
		// Test for -0.0
		TypedDiscreteDomain<Double> doublesWithZero = DiscreteDomain.create(0.0, 1.0, 3.0);
		assertInvariants(doublesWithZero);
		assertEquals(0, doublesWithZero.getIndex(0.0));
		assertEquals(0, doublesWithZero.getIndex(-0.0));
		TypedDiscreteDomain<Float> floatsWithZero = DiscreteDomain.create((float)0.0, (float)1.0, (float)3.0);
		assertInvariants(floatsWithZero);
		assertEquals(0, floatsWithZero.getIndex((float)0.0));
		assertEquals(0, floatsWithZero.getIndex((float)-0.0));
		
		//
		// Test RealJoint
		//
		
		try
		{
			RealJointDomain.create();
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("requires at least one domain"));
		}
		
		
		RealJointDomain realPlane = RealJointDomain.create(RealDomain.unbounded(), RealDomain.unbounded());
		assertInvariants(realPlane);
		
		RealJointDomain realPlane2 = RealJointDomain.create(RealDomain.unbounded(), RealDomain.unbounded());
		assertEquals(realPlane, realPlane2);
		
		RealJointDomain unitCube = RealJointDomain.create(unit, unit, unit);
		assertInvariants(unitCube);
		assertNotEquals(unitCube, realPlane);
		assertFalse(unitCube.inDomain(.5, 0.0, 1.5));
		assertFalse(unitCube.inDomain(new Object[] { .5, 0.0, 1.5}));
		
		//
		// Test IntDomain
		//
		
		IntDomain intDomain = IntDomain.unbounded();
		assertSame(intDomain, IntDomain.unbounded());
		assertInvariants(intDomain);
		assertTrue(intDomain.isIntegral());
		assertFalse(intDomain.isDiscrete());
		assertTrue(intDomain.inDomain(42));
		assertTrue(intDomain.inDomain((short)23));
		assertTrue(intDomain.inDomain(-23.0));
		assertFalse(intDomain.inDomain(1.5));
		
		//
		// Test ObjectDomain
		//
		
		ObjectDomain objDomain = ObjectDomain.instance();
		assertSame(objDomain, ObjectDomain.instance());
		assertInvariants(objDomain);
		assertTrue(objDomain.inDomain(42));
		assertTrue(objDomain.inDomain("foo"));
		assertTrue(objDomain.inDomain(null));
		
		//
		// Test static helper methods
		//
		
		assertTrue(Domain.isIntCompatibleClass(Integer.class));
		assertTrue(Domain.isIntCompatibleClass(Short.class));
		assertTrue(Domain.isIntCompatibleClass(Byte.class));
		assertFalse(Domain.isIntCompatibleClass(Character.class));
		assertFalse(Domain.isIntCompatibleClass(Integer.TYPE));
		assertFalse(Domain.isIntCompatibleClass(Long.class));
		assertFalse(Domain.isIntCompatibleClass(Float.class));
		
		assertTrue(Domain.isIntCompatibleValue(4.0));
		assertFalse(Domain.isIntCompatibleValue(4.01));
		assertFalse(Domain.isIntCompatibleValue((double)(Integer.MAX_VALUE + 1)));
		assertFalse(Domain.isIntCompatibleValue((long)Integer.MAX_VALUE + 1));
		assertFalse(Domain.isIntCompatibleValue(new Long(Integer.MAX_VALUE + 1)));
		assertTrue(Domain.isIntCompatibleValue(BigInteger.ONE));
	}
	
	public static void assertReal(RealDomain real, double lower, double upper)
	{
		assertInvariants(real);
		assertEquals(lower, real.getLowerBound(), 0.0);
		assertEquals(upper, real.getUpperBound(), 0.0);
		assertFalse(real.isIntegral());
		assertFalse(real.isDiscrete());
	}
	
	@SuppressWarnings("deprecation")
	public static void assertInvariants(Domain domain)
	{
		assertNotEquals(domain, "foo");
		assertEquals(domain, domain);
		
		assertEquals(domain.isNumber(), domain.isNumeric() && domain.isScalar());
		
		Domain serializedCopy = SerializationTester.clone(domain);
		assertSame(domain, serializedCopy);
		
		if (domain != ObjectDomain.instance())
		{
			assertFalse(domain.inDomain(Bogus.X));
			assertFalse(domain.containsValueWithRepresentation(Bogus.X));
		}
		
		if (domain.isReal())
		{
			assertFalse(domain.isDiscrete());
			assertFalse(domain.isRealJoint());
			assertTrue(domain instanceof RealDomain);
			assertTrue(domain.isScalar());
			assertTrue(domain.isNumeric());
			assertFalse(domain.isIntegral());
			assertFalse(domain.hasIntCompatibleValues());
			RealDomain real = (RealDomain)domain;
			assertSame(real, domain.asReal());
			
			double lower = real.getLowerBound();
			double upper = real.getUpperBound();
			
			assertEquals(real, RealDomain.create(lower, upper));
			assertEquals(real.hashCode(), RealDomain.create(lower, upper).hashCode());
			
			assertTrue(real.inDomain(lower));
			assertTrue(real.inDomain(upper));
			assertTrue(real.inDomain(lower));
			assertTrue(real.inDomain(upper));
			
			double tooLow = lower - 1;
			if (!Double.isInfinite(tooLow))
			{
				assertFalse(real.inDomain(tooLow));
			}
			
			double tooHigh = upper + 1;
			if (!Double.isInfinite(tooHigh))
			{
				assertFalse(real.inDomain(tooHigh));
			}
		}
		else
		{
			assertNull(domain.asReal());
		}
		
		if (domain.isDiscrete())
		{
			assertFalse(domain.isRealJoint());
			DiscreteDomain discrete = (DiscreteDomain)domain;
			assertSame(discrete, domain.asDiscrete());
			
			TypedDiscreteDomain<Object> discreteObj = discrete.asTypedDomain(Object.class);
			assertSame(discreteObj, discrete);
			TypedDiscreteDomain<Number> discreteNum = discrete.asTypedDomain(Number.class);
			if (Number.class.isAssignableFrom(discrete.getElementClass()))
			{
				assertSame(discrete, discreteNum);
				assertTrue(domain.isNumeric());
			}
			else
			{
				assertNull(discreteNum);
				assertFalse(domain.isNumeric());
			}
			
			final int size = discrete.size();
			assertTrue(size >= 0);
			final Object[] elements = discrete.getElements();
			assertEquals(size, elements.length);
			
			assertFalse(discrete.containsValueWithRepresentation(-1));
			assertFalse(discrete.containsValueWithRepresentation(size));
			
			assertFalse(discrete.isElementOf(Bogus.X));
			assertTrue(discrete.getIndex(Bogus.X) < 0);
			try
			{
				discrete.getIndexOrThrow(Bogus.X);
				fail("should not get here");
			}
			catch (DimpleException ex)
			{
			}
			
			Iterator<?> iter = null;
			if (discrete instanceof Iterable)
			{
				iter = ((Iterable<?>)discrete).iterator();
			}
			
			boolean allInt = true;
			for (int i = 0; i < size; ++i)
			{
				Object element = elements[i];
				assertTrue(discrete.inDomain(element));
				assertTrue(discrete.containsValueWithRepresentation(i));
				assertTrue(discrete.isElementOf(element));
				assertElementEquals(element, discrete.getElement(i));
				assertEquals(i, discrete.getIndex(element));
				assertEquals(i, discrete.getIndexOrThrow(element));
				allInt &= Domain.isIntCompatibleValue(element);
				
				if (iter != null)
				{
					assertTrue(iter.hasNext());
					assertElementEquals(element, iter.next());
				}
			}
			if (iter != null)
			{
				assertFalse(iter.hasNext());
				try
				{
					iter.remove();
					fail("should not get here");
				}
				catch (UnsupportedOperationException ex)
				{
				}
			}
			assertEquals(allInt, discrete.hasIntCompatibleValues());
			
			try
			{
				discrete.getElement(-1);
				fail("expected exception");
			}
			catch (IndexOutOfBoundsException ex)
			{
			}
			try
			{
				discrete.getElement(size);
				fail("expected exception");
			}
			catch (IndexOutOfBoundsException ex)
			{
			}
			
			if (discrete instanceof IntRangeDomain)
			{
				IntRangeDomain range = (IntRangeDomain)discrete;
				assertTrue(range.getLowerBound() <= range.getUpperBound());
				assertEquals(range.getLowerBound(), range.getIntElement(0));
				assertTrue((range.size()-1) * range.getInterval() + range.getLowerBound() <= range.getUpperBound());
				assertTrue(range.size() * range.getInterval() + range.getLowerBound() > range.getUpperBound());
				assertTrue(discrete.hasIntCompatibleValues());
			}
			
			if (discrete instanceof DoubleRangeDomain)
			{
				DoubleRangeDomain range = (DoubleRangeDomain)discrete;
				assertTrue(range.getLowerBound() <= range.getUpperBound());
				assertEquals(range.getLowerBound(), range.getDoubleElement(0), 0.0);
				assertTrue(range.getTolerance() > 0.0);
				assertTrue(range.getTolerance() < range.getInterval()/2);
				assertTrue((range.size()-1) * range.getInterval() + range.getLowerBound() <= range.getUpperBound());
				assertTrue(range.size() * range.getInterval() + range.getLowerBound() > range.getUpperBound());
				for (int i = range.size(); --i>=0;)
				{
					double d = range.getDoubleElement(i);
					assertEquals(i, range.getIndex(d));
					assertEquals(i, range.getIndex(d + range.getTolerance()/2));
					assertEquals(i, range.getIndex(d - range.getTolerance()/2));
					assertEquals(-1, range.getIndex(d + range.getInterval()/2));
				}
			}

			if (discrete instanceof EnumDomain)
			{
				EnumDomain<?> e = (EnumDomain<?>)discrete;
				Class<?> eclass = e.getElementClass();
				assertTrue(eclass.isEnum());
				Object[] evalues = eclass.getEnumConstants();
				assertEquals(evalues.length, e.size());
				assertArrayEquals(evalues, e.getElements());
				for (int i = 0; i < size; ++i)
				{
					assertEquals(i, e.getIndex(evalues[i]));
				}
				assertFalse(discrete.hasIntCompatibleValues());
			}
			
			if (discrete instanceof JointDiscreteDomain)
			{
				assertFalse(discrete.isScalar());
				assertFalse(discrete.hasIntCompatibleValues());
				JointDiscreteDomain<?> joint = (JointDiscreteDomain<?>)discrete;
				assertEquals(joint.getDomainIndexer().size(), joint.getDimensions());
				
				Object[] element = new Object[joint.getDimensions()];
				int[] indices = new int[joint.getDimensions()];
				for (int i = 0, end = joint.size(); i < end; ++i)
				{
					assertSame(element, joint.getElement(i, element));
					assertArrayEquals(element, joint.getElement(i));
					assertEquals(i, joint.getIndexFromSubelements(element));
					assertEquals(indices, joint.getElementIndices(i, indices));
					assertArrayEquals(indices, joint.getElementIndices(i));
					assertEquals(i, joint.getIndexFromIndices(indices));
					assertTrue(joint.containsValueWithRepresentation(indices));
					
					assertFalse(joint.inDomain(Arrays.copyOf(elements, elements.length - 1)));
					assertFalse(joint.containsValueWithRepresentation(Arrays.copyOf(indices, indices.length - 1)));
				}
				
				Arrays.fill(element, Bogus.X);
				assertFalse(joint.inDomain(element));
				
				Arrays.fill(indices, -1);
				assertFalse(joint.containsValueWithRepresentation(indices));
				Arrays.fill(indices, joint.size());
				assertFalse(joint.containsValueWithRepresentation(indices));
			}
			else
			{
				assertTrue(discrete.isScalar());
			}
		}
		else // Not Discrete
		{
			assertNull(domain.asDiscrete());
		}
		
		if (domain instanceof RealJointDomain)
		{
			assertFalse(domain.isScalar());
			assertFalse(domain.hasIntCompatibleValues());
			RealJointDomain realJoint = (RealJointDomain)domain;
			assertSame(realJoint, domain.asRealJoint());
			assertTrue(realJoint.isRealJoint());
			assertFalse(realJoint.isReal());
			assertFalse(realJoint.isDiscrete());
			assertSame(realJoint, domain.asRealJoint());
			
			int size = realJoint.getDimensions();
			assertTrue(size > 1);
			assertEquals(size, realJoint.getRealDomains().length);
			assertEquals(size, realJoint.getNumVars());
			
			double[] lower = new double[size];
			double[] upper = new double[size];
			for (int i = realJoint.getDimensions(); --i>=0;)
			{
				RealDomain subdomain = realJoint.getRealDomain(i);
				assertSame(realJoint.getRealDomains()[i], subdomain);
				lower[i] = subdomain.getLowerBound();
				upper[i] = subdomain.getUpperBound();
			}
			
			assertTrue(domain.inDomain(lower));
			assertTrue(domain.inDomain(upper));
			assertTrue(realJoint.inDomain(lower));
			assertTrue(realJoint.inDomain(upper));
			
			double[] lowerTooLong = Arrays.copyOf(lower, lower.length + 1);
			assertFalse(domain.inDomain(lowerTooLong));
			assertFalse(realJoint.inDomain(lowerTooLong));
			
			if (domain instanceof ComplexDomain)
			{
				ComplexDomain complex = (ComplexDomain)domain;
				assertSame(complex, domain.asComplex());
				assertTrue(domain.isComplex());
			}
			else
			{
				assertNull(domain.asComplex());
				assertFalse(domain.isComplex());
			}
		}
		else
		{
			assertNull(domain.asRealJoint());
			assertNull(domain.asComplex());
			assertFalse(domain.isComplex());
		}
	}
	
	private static void assertElementEquals(Object elt1, Object elt2)
	{
		if (elt1.getClass().isArray() && elt2.getClass().isArray())
		{
			assertEquals(Array.getLength(elt1), Array.getLength(elt2));
			for (int i = 0, end = Array.getLength(elt1); i < end; ++i)
			{
				assertEquals(Array.get(elt1, i), Array.get(elt2, i));
			}
		}
		else
		{
			assertEquals(elt1, elt2);
		}
	}
}
