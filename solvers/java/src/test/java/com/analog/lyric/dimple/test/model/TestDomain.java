package com.analog.lyric.dimple.test.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.DoubleRangeDomain;
import com.analog.lyric.dimple.model.domains.EnumDomain;
import com.analog.lyric.dimple.model.domains.IntRangeDomain;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;

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
		
		RealDomain unit = RealDomain.create(0.0, 1.0);
		assertReal(unit, 0.0, 1.0);
		
		RealDomain percentile = RealDomain.create(0.0, 100.0);
		assertReal(percentile, 0.0, 100.0);
		
		//
		// Test discretes
		//
		
		IntRangeDomain bit = DiscreteDomain.bit();
		assertInvariants(bit);
		assertEquals(2, bit.size());
		assertEquals(0, bit.getIntElement(0));
		assertEquals(1, bit.getIntElement(1));
		assertEquals(bit, DiscreteDomain.create(0, 1));
		
		TypedDiscreteDomain<Integer> reverseBit = DiscreteDomain.create(1, 0);
		assertNotEquals(bit, reverseBit);
		assertInvariants(reverseBit);
		
		TypedDiscreteDomain<Boolean> bool = DiscreteDomain.bool();
		assertInvariants(bool);
		assertEquals(2, bool.size());
		assertFalse(bool.getElement(0));
		assertTrue(bool.getElement(1));
		assertNotEquals(bit, bool);
		assertNotEquals(bit.hashCode(), bool.hashCode());
		assertEquals(bool, DiscreteDomain.create(false, true));
		assertNotEquals(bool, DiscreteDomain.create(true, false));
		
		EnumDomain<E> e = DiscreteDomain.forEnum(E.class);
		assertInvariants(e);
		assertNotEquals(e, DiscreteDomain.forEnum(Bogus.class));
		assertEquals(e, DiscreteDomain.create(E.A, E.B, E.C));
		
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
		assertEquals(r1to5, DiscreteDomain.create(1,2,3,4,5));
		
		IntRangeDomain r0to4 = DiscreteDomain.range(0,4);
		assertInvariants(r0to4);
		assertEquals(5, r0to4.size());
		assertNotEquals(r0to4, r1to5);
		assertNotEquals(r0to4.hashCode(), r1to5.hashCode());
		for (int i = 0; i < 5; ++i)
		{
			assertEquals(i, r0to4.getIndex(i));
		}
		assertEquals(r0to4, DiscreteDomain.create(0, 1, 2, 3, 4));
		
		IntRangeDomain evenDigits = DiscreteDomain.range(0, 8, 2);
		assertInvariants(evenDigits);
		assertEquals(5, evenDigits.size());
		assertNotEquals(evenDigits, r0to4);
		for (int i = 0; i <5; ++i)
		{
			assertEquals(i*2, evenDigits.getIntElement(i));
			assertEquals(i, evenDigits.getIndex(i*2));
		}
		assertEquals(evenDigits, DiscreteDomain.create(0,2,4,6,8));
		
		IntRangeDomain r0to3 = DiscreteDomain.range(0, 3);
		assertInvariants(r0to3);
		assertNotEquals(r0to3, r0to4);
		assertEquals(r0to3, DiscreteDomain.create(0,1,2,3));
		assertNotEquals(r0to3, DiscreteDomain.create(0,1.0,2,3));
		
		TypedDiscreteDomain<Integer> r3to0 = DiscreteDomain.create(3,2,1,0);
		assertInvariants(r3to0);
		assertNotEquals(r0to3, r3to0);
		
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
		assertEquals(halves, DiscreteDomain.create(0.0, 0.5, 1.0, 1.5, 2.0, 2.5));
		
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
		
		JointDiscreteDomain<E> joint2 = DiscreteDomain.joint(e, e);
		assertInvariants(joint2);
		assertNotEquals(joint1, joint2);

		JointDiscreteDomain<Integer> joint3 = DiscreteDomain.joint(r1to5, r0to3);
		assertInvariants(joint2);
		assertEquals(0, joint3.getIndex(new int[] { 1, 0 }));
		assertEquals(-1, joint3.getIndex(new int[] { 0, 1 }));
		
		TypedDiscreteDomain<Integer> primes = DiscreteDomain.create(2,3,5,7,11,13);
		assertInvariants(primes);
		
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
	}
	
	public static void assertReal(RealDomain real, double lower, double upper)
	{
		assertInvariants(real);
		assertEquals(lower, real.getLowerBound(), 0.0);
		assertEquals(upper, real.getUpperBound(), 0.0);
	}
	
	@SuppressWarnings("deprecation")
	public static void assertInvariants(Domain domain)
	{
		assertNotEquals(domain, "foo");
		assertEquals(domain, domain);
		
		assertFalse(domain.inDomain(Bogus.X));
		assertFalse(domain.containsValueWithRepresentation(Bogus.X));
		
		if (domain.isReal())
		{
			assertFalse(domain.isDiscrete());
			assertFalse(domain.isRealJoint());
			assertTrue(domain instanceof RealDomain);
			RealDomain real = (RealDomain)domain;
			
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
		
		if (domain.isDiscrete())
		{
			assertFalse(domain.isRealJoint());
			assertTrue(domain instanceof DiscreteDomain);
			DiscreteDomain discrete = (DiscreteDomain)domain;
			
			TypedDiscreteDomain<Object> discreteObj = discrete.asTypedDomain(Object.class);
			assertSame(discreteObj, discrete);
			TypedDiscreteDomain<Number> discreteNum = discrete.asTypedDomain(Number.class);
			if (Number.class.isAssignableFrom(discrete.getElementClass()))
			{
				assertSame(discrete, discreteNum);
			}
			else
			{
				assertNull(discreteNum);
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
			
			for (int i = 0; i < size; ++i)
			{
				Object element = elements[i];
				assertTrue(discrete.inDomain(element));
				assertTrue(discrete.containsValueWithRepresentation(i));
				assertTrue(discrete.isElementOf(element));
				assertElementEquals(element, discrete.getElement(i));
				assertEquals(i, discrete.getIndex(element));
				assertEquals(i, discrete.getIndexOrThrow(element));
				
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
			}
			
			if (discrete instanceof JointDiscreteDomain)
			{
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
		}
		
		if (domain instanceof RealJointDomain)
		{
			RealJointDomain realJoint = (RealJointDomain)domain;
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
