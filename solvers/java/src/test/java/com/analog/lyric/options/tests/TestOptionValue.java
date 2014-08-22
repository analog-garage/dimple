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

package com.analog.lyric.options.tests;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;

import org.junit.Test;

import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.options.AbstractOptionValueList;
import com.analog.lyric.options.IOptionValue;
import com.analog.lyric.options.OptionDoubleList;
import com.analog.lyric.options.OptionIntegerList;
import com.analog.lyric.options.OptionStringList;
import com.analog.lyric.util.test.SerializationTester;
import com.google.common.primitives.Primitives;

/**
 * Tests for {@link IOptionValue} implementations in com.analog.lyric.options package.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class TestOptionValue extends DimpleTestBase
{
	@Test
	public void test()
	{
		testList(new OptionStringList("fee", "fie", "foe"), "fee", "fie", "foe");
		testList(new OptionDoubleList(2.3, -4.2, 23.0), 2.3, -4.2, 23.0);
		testList(new OptionDoubleList(new Double[] { 2.3, -4.2, 23.0} ), 2.3, -4.2, 23.0);
		testList(new OptionIntegerList(3,4,5), 3, 4, 5);
		testList(new OptionIntegerList(new Integer[] { 3,4,5 }), 3, 4, 5);
		testList(new OptionStringList());
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Serializable> void testList(AbstractOptionValueList<T> list, T ... expectedValues)
	{
		assertInvariants(list);
		assertArrayEquals(expectedValues, list.toArray());
	}
	
	private void assertInvariants(IOptionValue value)
	{
		assertEquals(value, value);

		IOptionValue value2 = SerializationTester.clone(value);
		assertEquals(value, value2);
		
		if (value instanceof AbstractOptionValueList)
		{
			assertListInvariants((AbstractOptionValueList<?>)value);
		}
	}
	
	private <T extends Serializable> void assertListInvariants(AbstractOptionValueList<T> list)
	{
		int size = list.size();
		
		Class<? extends T> elementType = list.elementType();
		Object[] values = list.toArray();
		assertEquals(size, values.length);
		@SuppressWarnings("unchecked")
		T[] values2 = list.toArray((T[])Array.newInstance(elementType, 0));
		assertEquals(size, values2.length);
		assertSame(elementType, values2.getClass().getComponentType());
		
		Object[] array = new Object[size];
		Object[] values3 = list.toArray(array);
		assertSame(array, values3);
		assertArrayEquals(values, values3);
		
		array = new Object[size + 3];
		Arrays.fill(array, "crap");
		values3 = list.toArray(array);
		assertSame(array, values3);
		assertNull(values3[size]);
		assertEquals("crap", values3[size + 1]);
		
		Object primitive = list.toPrimitiveArray();
		assertTrue(primitive.getClass().isArray());
		if (Primitives.isWrapperType(elementType))
		{
			assertTrue(primitive.getClass().getComponentType().isPrimitive());
			assertEquals(primitive.getClass().getComponentType(), Primitives.unwrap(elementType));
		}
		else
		{
			assertFalse(primitive.getClass().getComponentType().isPrimitive());
		}
		
		for (int i = 0; i < size; ++i)
		{
			Object value = list.get(i);
			assertEquals(values[i], value);
			assertEquals(values2[i], value);
			assertEquals(values3[i], value);
			assertEquals(value, Array.get(primitive, i));
			assertTrue(elementType.isAssignableFrom(value.getClass()));
		}
		
		String str = "{";
		for (int i = 0; i < size; ++i)
		{
			if (i > 0)
			{
				str = str + ",";
			}
			str = str + values[i].toString();
		}
		str = str + '}';
		
		assertEquals(str, list.toString());
	}
}
