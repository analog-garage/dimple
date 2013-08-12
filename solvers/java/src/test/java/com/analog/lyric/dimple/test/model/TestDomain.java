package com.analog.lyric.dimple.test.model;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.DoubleRangeDomain;
import com.analog.lyric.dimple.model.EnumDomain;
import com.analog.lyric.dimple.model.IntRangeDomain;
import com.analog.lyric.dimple.model.JointDiscreteDomain;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.TypedDiscreteDomain;

public class TestDomain
{
	enum Bogus { X }
	
	enum E { A, B, C }
	
	@SuppressWarnings("unchecked")
	@Test
	public void test()
	{
		//
		// Test reals
		//
		
		assertReal(RealDomain.full(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		assertSame(RealDomain.full(), RealDomain.create(Double.NEGATIVE_INFINITY,  Double.POSITIVE_INFINITY));
		assertReal(RealDomain.nonNegative(), 0.0, Double.POSITIVE_INFINITY);
		assertSame(RealDomain.nonNegative(), RealDomain.create(0.0, Double.POSITIVE_INFINITY));
		assertReal(RealDomain.nonPositive(), Double.NEGATIVE_INFINITY, 0.0);
		assertSame(RealDomain.nonPositive(), RealDomain.create(Double.NEGATIVE_INFINITY, 0.0));
		assertReal(RealDomain.probability(), 0.0, 1.0);
		assertSame(RealDomain.probability(), RealDomain.create(0.0, 1.0));
		
		assertNotEquals(RealDomain.full(), RealDomain.nonNegative());
		assertNotEquals(RealDomain.full(), RealDomain.nonPositive());
		
		assertNotEquals(RealDomain.full().hashCode(), RealDomain.nonNegative().hashCode());
		
		RealDomain percentile = RealDomain.create(0.0, 100.0);
		assertReal(percentile, 0.0, 100.0);
		
		//
		// Test discretes
		//
		
		IntRangeDomain bit = DiscreteDomain.forBit();
		assertInvariants(bit);
		assertEquals(2, bit.size());
		assertEquals(0, bit.getIntElement(0));
		assertEquals(1, bit.getIntElement(1));
		assertEquals(bit, DiscreteDomain.fromElements(0, 1));
		
		TypedDiscreteDomain<Boolean> bool = DiscreteDomain.forBoolean();
		assertInvariants(bool);
		assertEquals(2, bool.size());
		assertFalse(bool.getElement(0));
		assertTrue(bool.getElement(1));
		assertNotEquals(bit, bool);
		assertNotEquals(bit.hashCode(), bool.hashCode());
		assertEquals(bool, DiscreteDomain.fromElements(false, true));
		assertNotEquals(bool, DiscreteDomain.fromElements(true, false));
		
		EnumDomain<E> e = DiscreteDomain.forEnum(E.class);
		assertInvariants(e);
		assertNotEquals(e, DiscreteDomain.forEnum(Bogus.class));
		assertEquals(e, DiscreteDomain.fromElements(E.A, E.B, E.C));
		
		IntRangeDomain r1to5 = DiscreteDomain.range(1,5);
		assertInvariants(r1to5);
		assertTrue(r1to5.getIndex(1.01) < 0);
		assertTrue(r1to5.getIndex(6) < 0);
		for (int i = 0; i < 5; ++i)
		{
			assertEquals(i + 1, r1to5.getIntElement(i));
		}
		assertEquals(r1to5, DiscreteDomain.fromElements(1,2,3,4,5));
		
		IntRangeDomain r0to4 = DiscreteDomain.intRangeFromSize(5);
		assertInvariants(r0to4);
		assertEquals(5, r0to4.size());
		assertNotEquals(r0to4, r1to5);
		assertNotEquals(r0to4.hashCode(), r1to5.hashCode());
		for (int i = 0; i < 5; ++i)
		{
			assertEquals(i, r0to4.getIndex(i));
		}
		assertEquals(r0to4, DiscreteDomain.fromElements(0, 1, 2, 3, 4));
		
		IntRangeDomain evenDigits = DiscreteDomain.intRangeFromSizeStartAndInterval(5, 0, 2);
		assertInvariants(evenDigits);
		assertEquals(5, evenDigits.size());
		assertNotEquals(evenDigits, r0to4);
		for (int i = 0; i <5; ++i)
		{
			assertEquals(i*2, evenDigits.getIntElement(i));
			assertEquals(i, evenDigits.getIndex(i*2));
		}
		assertEquals(evenDigits, DiscreteDomain.fromElements(0,2,4,6,8));
		
		IntRangeDomain r0to3 = DiscreteDomain.intRangeFromSizeAndStart(4, 0);
		assertInvariants(r0to3);
		assertNotEquals(r0to3, r0to4);
		assertEquals(r0to3, DiscreteDomain.fromElements(0,1,2,3));
		assertNotEquals(r0to3, DiscreteDomain.fromElements(0,1.0,2,3));
		
		DoubleRangeDomain halves = DiscreteDomain.doubleRangeFromSizeStartAndInterval(6,  0.0, .5);
		assertInvariants(halves);
		assertEquals(halves, DiscreteDomain.fromElements(0.0, 0.5, 1.0, 1.5, 2.0, 2.5));
		
		DoubleRangeDomain d0to3 = DiscreteDomain.doubleRangeFromSize(4);
		assertInvariants(d0to3);
		
		JointDiscreteDomain joint1 = DiscreteDomain.joint(e, evenDigits);
		assertInvariants(joint1);
		
		JointDiscreteDomain joint2 = DiscreteDomain.joint(e, e);
		assertInvariants(joint2);
		assertNotEquals(joint1, joint2);

		JointDiscreteDomain joint3 = DiscreteDomain.joint(r1to5, r0to3);
		assertInvariants(joint2);
		assertEquals(0, joint3.getIndex(new int[] { 1, 0 }));
		assertEquals(-1, joint3.getIndex(new int[] { 0, 1 }));
		
		DiscreteDomain primes = DiscreteDomain.fromElements(2,3,5,7,11,13);
		assertInvariants(primes);
		
		// Test for -0.0
		DiscreteDomain doublesWithZero = DiscreteDomain.fromElements(0.0, 1.0, 3.0);
		assertInvariants(doublesWithZero);
		assertEquals(0, doublesWithZero.getIndex(0.0));
		assertEquals(0, doublesWithZero.getIndex(-0.0));
		DiscreteDomain floatsWithZero = DiscreteDomain.fromElements((float)0.0, (float)1.0, (float)3.0);
		assertInvariants(floatsWithZero);
		assertEquals(0, floatsWithZero.getIndex((float)0.0));
		assertEquals(0, floatsWithZero.getIndex((float)-0.0));
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
			assertFalse(domain.isJoint());
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
			assertFalse(domain.isJoint());
			assertTrue(domain instanceof DiscreteDomain);
			DiscreteDomain discrete = (DiscreteDomain)domain;
			
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
			
			if (discrete instanceof EnumDomain)
			{
				EnumDomain<?> e = (EnumDomain<?>)discrete;
				Class<?> eclass = e.getEnumClass();
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
				JointDiscreteDomain joint = (JointDiscreteDomain)discrete;
				assertEquals(joint.getDomainList().size(), joint.getDimensions());
				
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
