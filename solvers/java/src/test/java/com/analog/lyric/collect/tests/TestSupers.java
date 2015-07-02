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

package com.analog.lyric.collect.tests;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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
		narrowArrayOfTestCase(A.class, new Object[] {new B(), new C()});
		narrowArrayOfTestCase(E.class, new Object[] {new E(), new E()});
		
		A[] a = (A[]) Supers.narrowArrayOf(A.class, 0, new Object[0]);
		assertEquals(A.class, a.getClass().getComponentType());
		A[] a2 = Supers.narrowArrayOf(A.class, 0, a);
		assertSame(a, a2);
		
		A[] a3 = new A[] { new A(), new B(), new C() };
		assertSame(a3, Supers.narrowArrayOf(A.class, a3));
		assertNull(Supers.narrowArrayOf(B.class, a3));
		Object[] o3 = new Object[] { a3[0], a3[1], a3[2] };
		A[] a4 = Supers.narrowArrayOf(A.class, o3);
		assertArrayEquals(a3, a4);
		assertEquals(A.class, requireNonNull(a4).getClass().getComponentType());
	}
	
	private <T> void narrowArrayOfTestCase(Class<?> expectedComponentType, T[] elements)
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
	public void testInvokeMethod()
	{
		try
		{
			assertEquals(3, Supers.invokeMethod("foo", "length"));
			assertEquals(3, Supers.invokeMethod("foobar", String.class, "indexOf", "bar"));
			assertEquals("foobar", Supers.invokeMethod(String.class, "format", "foo%s", "bar"));
			assertEquals("foobar", Supers.invokeMethod(String.class, "format", "foo%s", new Object[] {"bar"}));
			assertEquals("foobar", Supers.invokeMethod(String.class, "format", "foobar"));
			assertEquals("foobar", Supers.invokeMethod(String.class, "format", "foobar", new Object[] {}));
// FIXME - Supers.invokeMethod support for auto boxing of primitives
//			Supers.invokeMethod(Arrays.class, "asList", 1,2,3);
			List<?> list = (List<?>)Supers.invokeMethod(Arrays.class, "asList", "1", "2", "3");
			requireNonNull(list);
			assertArrayEquals(new Object[] {"1", "2", "3",}, list.toArray());
		}
		catch (Exception ex)
		{
			fail(ex.toString());
		}
	}
	
	/**
	 * Tests {@link Supers#isSubclassOf} and {@link Supers#isStrictSubclassOf}.
	 */
	@Test
	public void testIsSubclassOf()
	{
		assertTrue(Supers.isSubclassOf(A.class, A.class));
		assertFalse(Supers.isStrictSubclassOf(A.class, A.class));
		assertTrue(Supers.isSubclassOf(D.class, A.class));
		assertTrue(Supers.isStrictSubclassOf(D.class, A.class));
		assertFalse(Supers.isSubclassOf(A.class, D.class));
		assertFalse(Supers.isStrictSubclassOf(A.class, D.class));
	}
	
	@Test
	public void testLookupMethod()
	{
		try
		{
			Method m;
			
			try
			{
				m = Supers.lookupMethod("foo", "bar");
				fail("expected NoSuchMethodException");
			}
			catch (NoSuchMethodException ex)
			{
			}
			
			try
			{
				m = Supers.lookupMethod("foo", "indexOf", 2.35);
				fail("expected NoSuchMethodException");
			}
			catch (NoSuchMethodException ex)
			{
			}
			
			m = Supers.lookupMethod("foo", "length");
			assertEquals("length", m.getName());
			m = Supers.lookupMethod("foo", "indexOf", 0);
			assertEquals("indexOf", m.getName());
			assertArrayEquals(new Object[] { Integer.TYPE }, m.getParameterTypes());
			m = Supers.lookupMethod("foo", "indexOf", "f");
			assertEquals("indexOf", m.getName());
			assertArrayEquals(new Object[] { String.class }, m.getParameterTypes());
			m = Supers.lookupMethod("foo", "indexOf", new Object[] { null } );
			assertEquals("indexOf", m.getName());
			assertArrayEquals(new Object[] { String.class }, m.getParameterTypes());
			m = Supers.lookupMethod("foo", "format", "%s", "x");
			assertEquals("format", m.getName());
			assertArrayEquals(new Object[] { String.class, Object[].class }, m.getParameterTypes());
			m = Supers.lookupMethod(String.class, "format", "hi");
			assertEquals("format", m.getName());
			assertArrayEquals(new Object[] { String.class, Object[].class }, m.getParameterTypes());
			m = Supers.lookupMethod(Arrays.class, "asList", 1, 2, 3);
			assertEquals("asList", m.getName());
		}
		catch (Exception ex)
		{
			fail(ex.toString());
		}
		
		expectThrow(NoSuchMethodException.class,
			"No method in String with signature compatible with doesNotExist\\(\\)",
			Supers.class, "lookupMethod", "foo", "doesNotExist");
		expectThrow(NoSuchMethodException.class,
			".*with size\\(null,\\s?int\\)",
			Supers.class, "lookupMethod", "foo", "size", null, 23);
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
