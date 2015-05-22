/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.data;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.AbstractMap;

import org.junit.Test;

import com.analog.lyric.dimple.data.DataEntry;
import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Unit tests for {@link DataEntry}
 * @since 0.08
 * @author Christopher Barber
 */
public class TestDataEntry extends DimpleTestBase
{
	@Test
	public void test()
	{
		Real a = new Real();
		
		DataEntry<Value> entry = new DataEntry<>(a, null);
		assertInvariants(entry);
		assertSame(a, entry.variable());
		assertNull(entry.getValue());
		
		Value val = RealValue.create(2);
		DataEntry<? extends IDatum> entry2 = new DataEntry<>(a, val);
		assertInvariants(entry2);
		assertNotEquals(entry, entry2);
	}
	
	private <D extends IDatum> void assertInvariants(DataEntry<D> entry)
	{
		assertSame(entry.variable(), entry.getKey());
		
		assertTrue(entry.equals(entry));
		assertFalse(entry.equals("foo"));
		
		assertTrue(entry.equals(new AbstractMap.SimpleEntry<>(entry.variable(), entry.getValue())));
		assertFalse(entry.equals(new AbstractMap.SimpleEntry<>(new Real(), entry.getValue())));
		assertFalse(entry.equals(new AbstractMap.SimpleEntry<>(entry.variable(), RealValue.create(Double.NaN))));
		
		assertEquals(entry.hashCode(), new AbstractMap.SimpleEntry<>(entry.variable(), entry.getValue()).hashCode());
		assertNotEquals(entry.hashCode(), new DataEntry<D>(new Real(), entry.getValue()));
		
		expectThrow(UnsupportedOperationException.class, entry, "setValue", RealValue.create());
	}
}
