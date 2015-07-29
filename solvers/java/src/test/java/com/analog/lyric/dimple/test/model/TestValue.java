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

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.IntDomain;
import com.analog.lyric.dimple.model.domains.ObjectDomain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.dimple.model.values.DoubleRangeValue;
import com.analog.lyric.dimple.model.values.GenericDiscreteValue;
import com.analog.lyric.dimple.model.values.GenericIntDiscreteValue;
import com.analog.lyric.dimple.model.values.IntRangeValue;
import com.analog.lyric.dimple.model.values.IntValue;
import com.analog.lyric.dimple.model.values.RealJointValue;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.values.SimpleIntRangeValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.util.test.SerializationTester;

public class TestValue extends DimpleTestBase
{
	
	@Test
	public void test()
	{
		//
		// Test creation
		//
		
		Domain domain = ObjectDomain.instance();
		Value value = Value.create(domain);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		
		domain = DiscreteDomain.bit();
		value = Value.create(domain);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(0, value.getInt());
		assertEquals(0, value.getIndex());
		assertEquals(0, value.getObject());
		assertTrue(value instanceof SimpleIntRangeValue);
		testRealsFromDiscrete((DiscreteDomain)domain);
		
		domain = DiscreteDomain.range(2, 5);
		value = Value.create(domain);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(2, value.getInt());
		assertEquals(0, value.getIndex());
		assertTrue(value instanceof IntRangeValue);
		testRealsFromDiscrete((DiscreteDomain)domain);
		
		domain = DiscreteDomain.range(2.0, 5.0);
		value = Value.create(domain);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(2, value.getInt());
		assertEquals(0, value.getIndex());
		assertTrue(value instanceof DoubleRangeValue);
		testRealsFromDiscrete((DiscreteDomain)domain);
		
		domain = DiscreteDomain.range(0, 4, 2);
		value = Value.create(domain);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(0, value.getInt());
		assertEquals(0, value.getIndex());
		assertTrue(value instanceof IntRangeValue);
		testRealsFromDiscrete((DiscreteDomain)domain);
		
		domain = DiscreteDomain.create(1,2,3,5,8,13);
		value = Value.create(domain);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(1, value.getInt());
		assertEquals(0, value.getIndex());
		assertTrue(value instanceof GenericIntDiscreteValue);
		testRealsFromDiscrete((DiscreteDomain)domain);
		
		domain = DiscreteDomain.create(1,2,3.5);
		value = Value.create(domain);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(1, value.getObject());
		assertEquals(0, value.getIndex());
		assertTrue(value instanceof GenericDiscreteValue);
		testRealsFromDiscrete((DiscreteDomain)domain);
		
		domain = RealDomain.unbounded();
		value = Value.create(domain);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(0.0, value.getDouble(), 0.0);
		assertTrue(value instanceof RealValue);
		
		domain = RealJointDomain.create(2);
		value = Value.create(domain);
		assertInvariants(value);
		assertEquals(domain, value.getDomain());
		assertTrue(value instanceof RealJointValue);
		
		domain = IntDomain.unbounded();
		value = Value.create(domain);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertTrue(value instanceof IntValue);
		
		domain = DiscreteDomain.range(0, 9);
		value = Value.create(domain, 3);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(3, value.getInt());
		assertEquals(3, value.getIndex());
		
		domain = DiscreteDomain.range(-4,4);
		value = Value.create(domain, 3);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(3, value.getInt());
		assertEquals(7, value.getIndex());
		testRealsFromDiscrete((DiscreteDomain)domain);
		
		domain = DiscreteDomain.range(0,6,3);
		value = Value.create(domain, 3);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(3, value.getInt());
		assertEquals(1, value.getIndex());
		
		domain = DiscreteDomain.create(1,2,3,5);
		value = Value.create(domain,3);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(3, value.getInt());
		assertEquals(2, value.getIndex());
		testRealsFromDiscrete((DiscreteDomain)domain);
		
		domain = DiscreteDomain.create("rabbit", 3, 4.2);
		value = Value.create(domain, 3);
		assertInvariants(value);
		assertSame(domain, value.getDomain());
		assertEquals(3, value.getInt());
		assertEquals(3, value.getObject());
		assertEquals(1, value.getIndex());
		testRealsFromDiscrete((DiscreteDomain)domain);
		
		value = Value.create(42);
		assertInvariants(value);
		assertSame(IntDomain.unbounded(), value.getDomain());
		assertEquals(42, value.getObject());
		assertEquals(42, value.getInt());
		
		value = Value.create((short)42);
		assertInvariants(value);
		assertEquals(42, value.getObject());
		assertSame(IntDomain.unbounded(), value.getDomain());
		
		value = Value.create((byte)42);
		assertInvariants(value);
		assertEquals(42, value.getObject());
		assertSame(IntDomain.unbounded(), value.getDomain());
		
		value = Value.create(42.0);
		assertInvariants(value);
		assertSame(RealDomain.unbounded(), value.getDomain());
		assertEquals(42.0, value.getObject());
		
		double[] array = new double[] { 1.0, 3.0 };
		value = Value.create(array);
		assertInvariants(value);
		assertEquals(RealJointDomain.create(2), value.getDomain());
		assertArrayEquals(array, (double[])value.getObject(), 0.0);
		
		value = Value.create("foo");
		assertInvariants(value);
		assertSame(ObjectDomain.instance(), value.getDomain());
		assertEquals("foo", value.getObject());
		
		//
		// Test integral values
		//
		
		value = Value.create(42);
		assertEquals(42, value.getInt());
		assertEquals(-1, value.getIndex());
		value.setInt(23);
		expectThrow(UnsupportedOperationException.class, value, "setIndex", 0);
		assertEquals(23, value.getInt());
		value.setDouble(1.6);
		assertEquals(2, value.getInt());
		assertEquals(2.0, value.getDouble(), 0.0);
		value.setObject(-40);
		assertEquals(-40, value.getInt());
		assertTrue(value.valueEquals(Value.create(-40.0)));
		assertFalse(value.valueEquals(Value.create(39)));
		assertEquals(Double.POSITIVE_INFINITY, value.evalEnergy(Value.create(39)), 0.0);
		assertInvariants(value);
		
		Domain digit = DiscreteDomain.range(0,9);
		value = Value.create(digit, 3);
		assertEquals(3, value.getInt());
		assertEquals(3, value.getIndex());
		value.setInt(4);
		assertEquals(4, value.getInt());
		value.setDouble(5.2);
		assertEquals(5, value.getInt());
		assertTrue(value.valueEquals(Value.create(5)));
		value.setIndex(2);
		assertEquals(2, value.getIndex());
		assertEquals(2, value.getObject());
		assertInvariants(value);
		
		Domain oddDigits = DiscreteDomain.range(1, 9, 2);
		value = Value.create(oddDigits, 3);
		assertEquals(3, value.getInt());
		assertEquals(1, value.getIndex());
		value.setInt(5);
		assertEquals(5, value.getObject());
		assertEquals(2, value.getIndex());
		value.setIndex(0);
		assertEquals(0, value.getIndex());
		assertEquals(1, value.getInt());
		value.setObject(9);
		assertEquals(9.0, value.getDouble(), 0.0);
		assertEquals(4, value.getIndex());
		assertFalse(value.valueEquals(Value.create(10)));
		assertTrue(value.valueEquals(Value.create(9.1)));
		
		Domain primeDigits = DiscreteDomain.create(2,3,5,7);
		value = Value.create(primeDigits, 5);
		assertEquals(5, value.getInt());
		value.setInt(2);
		assertEquals(2, value.getInt());
		assertEquals(0, value.getIndex());
		value.setIndex(3);
		assertEquals(3, value.getIndex());
		assertEquals(7, value.getInt());
		
		/*
		 * Test generic discrete values
		 */
		
		DiscreteDomain stooges = DiscreteDomain.create("moe", "joe", "curly");
		value = Value.create(stooges);
		assertEquals("moe", value.getObject());
		assertEquals(0, value.getIndex());
		assertInvariants(value);
		value.setObject("joe");
		assertEquals("joe", value.getObject());
		assertEquals(1, value.getIndex());
		value.setIndex(2);
		assertEquals("curly", value.getObject());
		assertEquals(2, value.getIndex());
		assertFalse(value.valueEquals(Value.create(stooges, "moe")));
		assertTrue(value.valueEquals(Value.create("curly")));
		value.setFrom(Value.create("joe"));
		assertEquals("joe", value.getObject());
		
		/*
		 * Test real values
		 */
		
		value = Value.create(3.14159);
		assertEquals(3.14159, value.getDouble(), 0.0);
		value.setInt(42);
		assertEquals(42.0, value.getDouble(), 0.0);
		value.setDouble(2.3);
		assertEquals(2.3, value.getDouble(), 0.0);
		value.setObject(-123.4);
		assertEquals(-123.4, value.getDouble(), 0.0);
		assertFalse(value.valueEquals(Value.create(-123.4002)));
		
		Domain halves = DiscreteDomain.range(0, 10, .5);
		value = Value.create(halves, 3);
		assertEquals(3, value.getInt());
		assertEquals(3, value.getDouble(), 0.0);
		assertEquals(6, value.getIndex());
		assertInvariants(value);
		value.setIndex(1);
		assertEquals(.5, value.getDouble(), 0.0);
		assertEquals(1, value.getIndex());
		
		Domain realDigits = DiscreteDomain.range(0.0, 9.0);
		value = Value.create(realDigits, 3);
		assertEquals(3, value.getInt());
		assertEquals(3, value.getIndex());
		assertInvariants(value);
		value.setIndex(5);
		assertEquals(5, value.getInt());
		
		Domain powersOfTwo = DiscreteDomain.create(.125, .25, .5, 1.0, 2.0, 4.0, 8.0);
		value = Value.create(powersOfTwo);
		assertEquals(.125, value.getDouble(), 0.0);
		assertInvariants(value);
		value.setIndex(2);
		assertEquals(.5, value.getDouble(), 0.0);
		value.setFrom(Value.create(.25));
		assertEquals(1, value.getIndex());
		value.setFrom(Value.create(DiscreteDomain.create(2.3, 4.0, 5.0), 4.0));
		assertEquals(4.0, value.getDouble(), 0.0);
		
		/*
		 * Test object values
		 */
		
		value = Value.create("foo");
		assertEquals("foo", value.getObject());
		expectThrow(DimpleException.class, value, "getInt");
		value.setInt(42);
		assertEquals(42, value.getObject());
		assertEquals(42, value.getInt());
		assertEquals(42.0, value.getDouble(), 0.0);
		value.setDouble(23.1);
		assertEquals(23.1, value.getObject());
		assertEquals(23, value.getInt());
		assertEquals(23.1, value.getDouble(), 0.0);
		double[] doubles = new double[] { 1.2, 3.4 } ;
		value.setObject(doubles);
		assertSame(doubles, value.getDoubleArray());
		
		value = Value.create((Domain)null);
		assertNull(value.getObject());
		assertSame(ObjectDomain.instance(), value.getDomain());
		
		/*
		 * 
		 */
	}
	
	private void testRealsFromDiscrete(DiscreteDomain domain)
	{
		try
		{
			RealValue[] values = RealValue.createFromDiscreteDomain(domain);
		
			assertEquals(domain.size(), values.length);
			for (int i = values.length; --i >=0;)
			{
				assertEquals(((Number)domain.getElement(i)).doubleValue(), values[i].getDouble(), 0.0);
			}
		}
		catch (ClassCastException ex)
		{
			assertFalse(Number.class.isAssignableFrom(domain.getElementClass()));
		}
	}

	private void assertInvariants(Value value)
	{
		Domain domain = value.getDomain();
		assertNotNull(domain);
		
		Object objValue = value.getObject();
		
		domain.inDomain(objValue);

		if (domain.isIntegral())
		{
			assertEquals(value.getInt(), value.getDouble(), 0.0);
			assertEquals(value.getInt(), objValue);
		}
		if (domain.isReal())
		{
			assertEquals(value.getDouble(), objValue);
		}
		
		if (objValue instanceof Number)
		{
			Number number = (Number)objValue;
			
			assertEquals(number.intValue(), value.getInt());
			assertEquals(number.doubleValue(), value.getDouble(), 0.0);
			assertEquals(number.doubleValue() != 0.0, value.getBoolean());
			assertArrayEquals(new double[] { number.doubleValue() }, value.getDoubleArray(), 0.0);
		}
		else if (objValue instanceof Boolean)
		{
			Boolean bool = (Boolean)objValue;
			assertEquals(bool, value.getBoolean());
			assertEquals(bool, value.getInt() == 1);
			assertEquals(bool, value.getDouble() == 1.0);
		}
		else if (objValue instanceof double[])
		{
			assertArrayEquals((double[])objValue, value.getDoubleArray(), 0.0);
		}
		
		if (value instanceof DiscreteValue)
		{
			assertEquals(value.getIndex(), value.getIndexOrInt());
		}
		else if (objValue instanceof Number)
		{
			assertEquals(value.getInt(), value.getIndexOrInt());
		}
		
		Value value2 = value.clone();
		assertNotSame(value, value2);
		assertSame(value.getClass(), value2.getClass());
		assertEquals(value.getIndex(), value2.getIndex());
		assertTrue(value.valueEquals(value2));
		
		assertEquals(0.0, value.evalEnergy(value), 0.0);
		assertEquals(0.0, value.evalEnergy(value2), 0.0);
		
		Value value3 = Value.create(domain);
		assertEquals(value.getClass(), value3.getClass());
		value3.setFrom(value);
		assertTrue(value3.valueEquals(value));
		assertEquals(objValue, value3.getObject());
		assertEquals(value.getIndex(), value3.getIndex());
		
		Value value4 = Value.create(domain, objValue);
		assertEquals(objValue, value4.getObject());
		assertEquals(value.getIndex(), value4.getIndex());
		
		Value value5 = SerializationTester.clone(value);
		assertNotSame(value, value5);
		assertSame(value.getClass(), value5.getClass());
		assertEquals(value.getIndex(), value5.getIndex());
		assertTrue(value.valueEquals(value5));
	}
}
