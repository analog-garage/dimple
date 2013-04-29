package com.analog.lyric.collect.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.collect.Supers;
import com.google.common.collect.ImmutableList;

public class TestSupers
{
	static enum E1 { A,B,C; }
	static enum E2 { A,B,C; }
	
	static interface I {}
	static interface J {}
	static interface K extends I {}
	static interface L extends I {}
	static interface M extends K, L {}
	
	static class A {}
	static class B extends A {}
	static class C extends A {}
	static class D extends B {}
	static final class E extends D {}
	static class F extends C implements L {}
	
	@Test
	public void testNarrowArrayOf()
	{
		narrowArrayOfTestCase(A.class, new B(), new C());
		narrowArrayOfTestCase(E.class, new E(), new E());
	}
	
	private <T> void narrowArrayOfTestCase(Class<?> expectedComponentType, T...elements)
	{
		T[] copy = Supers.narrowArrayOf(elements);
		assertArrayEquals(elements, copy);
		assertEquals(expectedComponentType, copy.getClass().getComponentType());
		
		ImmutableList<Class<?>> supers = Supers.superClasses(expectedComponentType);
		for (int depth = 0; depth < supers.size(); ++ depth)
		{
			copy = Supers.narrowArrayOf(Object.class, depth, elements);
			assertArrayEquals(elements, copy);
			assertEquals(supers.get(depth), copy.getClass().getComponentType());
			
			copy = Supers.narrowArrayOf(supers.get(depth), 0, elements);
			assertArrayEquals(elements, copy);
			assertEquals(supers.get(depth), copy.getClass().getComponentType());
		}
		
		copy = Supers.narrowArrayOf(Object.class, supers.size(), elements);
		assertArrayEquals(elements, copy);
		assertEquals(expectedComponentType, copy.getClass().getComponentType());
	}
	
	@Test
	public void testNearestCommonSuperclass()
	{
		assertEquals(A.class, Supers.nearestCommonSuperClass(A.class, A.class));
		assertEquals(A.class, Supers.nearestCommonSuperClass(A.class, B.class));
		assertEquals(A.class, Supers.nearestCommonSuperClass(B.class, A.class));
		assertEquals(Integer.TYPE, Supers.nearestCommonSuperClass(Integer.TYPE, Integer.TYPE));
		assertNull(Supers.nearestCommonSuperClass(Integer.TYPE, Long.TYPE));
		assertNull(Supers.nearestCommonSuperClass(Object.class, Integer.TYPE));
		assertEquals(Object.class, Supers.nearestCommonSuperClass(K.class, L.class));
		assertEquals(Object.class, Supers.nearestCommonSuperClass(F.class, M.class));
		assertEquals(A.class, Supers.nearestCommonSuperClass(B.class, C.class));
		assertEquals(A.class, Supers.nearestCommonSuperClass(F.class, D.class));
		assertEquals(Object.class, Supers.nearestCommonSuperClass(D.class, A[].class));
		
		assertEquals(Object.class, Supers.nearestCommonSuperClass());
		assertEquals(Object.class, Supers.nearestCommonSuperClass(new Integer[0]));
		assertEquals(B.class, Supers.nearestCommonSuperClass(new A[] {new B()} ));
		assertEquals(Object.class, Supers.nearestCommonSuperClass(new Object[] { null, null, null }));
		assertEquals(A.class, Supers.nearestCommonSuperClass(null, null, new A(), new B()));
		assertEquals(A.class, Supers.nearestCommonSuperClass(new B(), new C()));
		assertEquals(F.class, Supers.nearestCommonSuperClass(new I[] {new F(), null}));
		assertEquals(E1.class, Supers.nearestCommonSuperClass(new E1[] { E1.A, E1.B }));
		assertEquals(E.class, Supers.nearestCommonSuperClass(new E[] { new E() }));
		assertEquals(A.class, Supers.nearestCommonSuperClass(new A[] { new A(), new B() }));
	}
	
	@Test
	public void testNumberOfSuperClasses()
	{
		assertEquals(0, Supers.numberOfSuperClasses(Object.class));
		assertEquals(1, Supers.numberOfSuperClasses(Long[].class));
		assertEquals(2, Supers.numberOfSuperClasses(E1.class));
		assertEquals(1, Supers.numberOfSuperClasses(A.class));
		assertEquals(2, Supers.numberOfSuperClasses(B.class));
		assertEquals(4, Supers.numberOfSuperClasses(E.class));
	}

	@Test
	public void testSuperClasses()
	{
		assertEquals(
			ImmutableList.of(),
			Supers.superClasses(Object.class));
		assertEquals(
			ImmutableList.<Class<?>>of(),
			Supers.superClasses(Integer.TYPE));
		assertEquals(
			ImmutableList.of(Object.class),
			Supers.superClasses(A.class));
		assertEquals(
			ImmutableList.of(Object.class, A.class, B.class),
			Supers.superClasses(D.class));
	}
}
