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

package com.analog.lyric.dimple.test.model.core;

import static com.analog.lyric.dimple.model.core.Ids.*;
import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.core.IFactorGraphChild;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.model.core.Ids.Type;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Test for Ids static methods
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class TestNodeId extends DimpleTestBase
{
	@Test
	public void test()
	{
		testForIndex(LOCAL_ID_INDEX_MIN);
		testForIndex(LOCAL_ID_INDEX_MAX);
		for (int i = 0; i < 100; ++i)
		{
			final int index =
				LOCAL_ID_INDEX_MIN + testRand.nextInt(1 + LOCAL_ID_INDEX_MAX + LOCAL_ID_INDEX_MIN);
			testForIndex(index);
		}
		
		int bogusTypeLocalId = 0xF0000003;
		assertEquals("$X3", defaultNameForLocalId(bogusTypeLocalId));
		assertEquals(-1, localIdFromDefaultName(""));
		assertEquals(-1, localIdFromDefaultName("barf"));
		assertEquals(-1, localIdFromDefaultName("$Vxxx"));
		assertEquals(-1, localIdFromDefaultName("$W42"));
		assertEquals(-1, localIdFromDefaultName("$X3"));
		assertEquals(-1, localIdFromDefaultName("$v42"));
		assertEquals(-1, localIdFromDefaultName("$$42"));
		assertEquals(-1, localIdFromDefaultName("$A-42"));
		assertEquals(-1, localIdFromDefaultName("$V268435456")); // index too large
		
		assertEquals(-1, graphIdFromDefaultName(""));
		assertEquals(-1, graphIdFromDefaultName("$Graph"));
		assertEquals(-1, graphIdFromDefaultName("$Graph-42"));
		assertEquals(-1, graphIdFromDefaultName("$Graph1073741824")); // id too large
		
		assertEquals(-1L, envIdFromUUID(UUID.randomUUID()));
		assertEquals(-1L, envIdFromUUID(new UUID(0,0)));
		assertEquals(-1L, globalIdFromUUID(UUID.randomUUID()));
		assertEquals(-1L, globalIdFromUUID(new UUID(0,0)));
		
		assertFalse(isUUIDString(UUID.randomUUID().toString()));
		assertTrue(isUUIDString("00000000-0000-D000-8000-000000000000"));
		assertTrue(isUUIDString("00000000-0000-d000-9000-000000000000"));
		assertTrue(isUUIDString("00000000-0000-D000-A000-000000000000"));
		assertTrue(isUUIDString("00000000-0000-d000-B000-000000000000"));
		assertFalse(isUUIDString("00000000-0000-0000-0000-000000000000"));
		assertFalse(isUUIDString("00000000-0000-0000-D000-000000000000"));
		assertFalse(isUUIDString("00000000=0000-D000-8000-000000000000"));
		assertFalse(isUUIDString("00000000-0000=D000-8000-000000000000"));
		assertFalse(isUUIDString("00000000-0000-D000=8000-000000000000"));
		assertFalse(isUUIDString("00000000-0000-D000-8000=000000000000"));
		assertFalse(isUUIDString("00000000-0000-D000-C000-000000000000"));
		assertFalse(isUUIDString("*0000000-0000-D000-8000-000000000000"));
		assertFalse(isUUIDString("0g000000-0000-D000-8000-000000000000"));
		assertFalse(isUUIDString("00G00000-0000-D000-8000-000000000000"));
		assertFalse(isUUIDString("000@0000-0000-D000-8000-000000000000"));

		// Test exceptions
		expectThrow(IllegalArgumentException.class, Ids.Type.class, "forInstanceClass", Node.class);
		abstract class BogusChildClass implements IFactorGraphChild {}
		expectThrow(IllegalArgumentException.class, Ids.Type.class, "forInstanceClass", BogusChildClass.class);
	}
	
	private void testForIndex(int index)
	{
		for (int typeIndex = TYPE_MIN; typeIndex <= TYPE_MAX; ++typeIndex)
		{
			Ids.Type type = Ids.Type.valueOf(typeIndex);
			assertEquals(typeIndex, type.ordinal());
			assertEquals(typeIndex, type.typeIndex());
			Class<? extends IFactorGraphChild> instanceClass = type.instanceClass();
			if (type == Ids.Type.BOUNDARY_VARIABLE)
			{
				assertSame(Variable.class, instanceClass);
			}
			else
			{
				assertEquals(type, Ids.Type.forInstanceClass(instanceClass));
			}
			
			int localId = localIdFromParts(typeIndex, index);
			assertEquals(index, indexFromLocalId(localId));
			assertEquals(typeIndex, typeIndexFromLocalId(localId));
			assertEquals(Type.valueOf(typeIndex), typeFromLocalId(localId));
			
			String name = defaultNameForLocalId(localId);
			assertFalse(isUUIDString(name));
			assertEquals(localId, localIdFromDefaultName(name));
			
			int graphId = GRAPH_ID_MIN + testRand.nextInt(1 + GRAPH_ID_MAX - GRAPH_ID_MIN);
			long globalId = globalIdFromParts(graphId, localId);
			long globalId2 = globalIdFromParts(graphId, typeIndex, localId);
			assertEquals(globalId, globalId2);
			assertTrue(GLOBAL_ID_MIN <= globalId);
			assertTrue(GLOBAL_ID_MAX >= globalId);
			assertTrue(isGlobalId(globalId));
			
			assertEquals(graphId, graphIdFromGlobalId(globalId));
			assertEquals(localId, localIdFromGlobalId(globalId));
			
			String graphName = defaultNameForGraphId(graphId);
			assertEquals(graphId, graphIdFromDefaultName(graphName));
			
			int graphTreeIndex = testRand.nextInt(Short.MAX_VALUE);
			long graphTreeId = graphTreeIdFromParts(graphTreeIndex, localId);
			assertEquals(graphTreeIndex, graphTreeIndexFromGraphTreeId(graphTreeId));
			assertEquals(localId, localIdFromGraphTreeId(graphTreeId));
			assertFalse(isGlobalId(graphTreeId));
			
			long envId = DimpleEnvironment.active().getEnvId();
			UUID uuid = makeUUID(envId, globalId);
			assertEquals(globalId, globalIdFromUUID(uuid));
			assertEquals(envId, envIdFromUUID(uuid));
			assertTrue(isUUIDString(uuid.toString()));
			assertTrue(isUUIDString(uuid.toString().toLowerCase()));
		}
	}
}
