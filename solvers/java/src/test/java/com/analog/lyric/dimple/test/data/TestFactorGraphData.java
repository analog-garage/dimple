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

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.collect.PrimitiveIterator;
import com.analog.lyric.dimple.data.DataEntry;
import com.analog.lyric.dimple.data.DataLayer;
import com.analog.lyric.dimple.data.DenseFactorGraphData;
import com.analog.lyric.dimple.data.FactorGraphData;
import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.data.SparseFactorGraphData;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.google.common.collect.Iterables;

/**
 * Unit tests for {@link FactorGraphData} classes.
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestFactorGraphData extends DimpleTestBase
{
	@Test
	public void test()
	{
		test(DenseFactorGraphData.constructorForType(IDatum.class));
		test(SparseFactorGraphData.constructorForType(IDatum.class));
	}
	
	public <D extends IDatum> void test(FactorGraphData.Constructor<D> dataConstructor)
	{
		Class<D> type = dataConstructor.baseType();
		
		FactorGraph root = new FactorGraph("root");
		try (CurrentModel cur = using(root))
		{
			Real[] r = reals("r", 100);
			
			DataLayer<D> layer = new DataLayer<D>(root, dataConstructor);
			
			FactorGraphData<D> data = dataConstructor.apply(layer, root);
			assertInvariants(data);
			assertEquals(0, data.size());
			
			data.put(r[3], type.cast(RealValue.create(1.23)));
			data.put(r[78], type.cast(RealValue.create(4.5)));
			assertInvariants(data);
			assertEquals(2, data.size());
			
			data.entrySet().add(new AbstractMap.SimpleEntry<Variable,D>(r[23], type.cast(RealValue.create(1.0))));
			assertInvariants(data);

			assertFalse(data.containsLocalIndex(Ids.indexFromLocalId(r[4].getLocalId())));
			
			Map<Variable,D> copy = new HashMap<>(data);
			
			data.clear();
			assertInvariants(data);
			assertTrue(data.isEmpty());
			
			data.putAll(copy);
			assertInvariants(data);
			assertEquals(3, data.size());
			
			data.entrySet().clear();
			assertInvariants(data);
			assertTrue(data.isEmpty());
			
			data.putAll(copy);
			assertInvariants(data);
			assertEquals(3, data.size());
			
			data.keySet().clear();
			assertInvariants(data);
			assertEquals(0, data.size());
				
			data.putAll(copy);
			assertInvariants(data);
			assertEquals(3, data.size());
			
			assertNull(data.remove(r[2]));
			assertSame(data.get(r[3]), data.remove(r[3]));
			assertNull(data.get(r[3]));
			assertInvariants(data);
			assertEquals(2, data.size());
			
			assertTrue(data.keySet().remove(r[78]));
			assertFalse(data.keySet().remove(r[78]));
			assertEquals(1, data.size());
			
			data.putAll(copy);
			assertInvariants(data);
			assertEquals(3, data.size());

			assertTrue(data.entrySet().remove(new AbstractMap.SimpleEntry<>(r[78], data.get(r[78]))));
			assertEquals(2, data.size());
			
			expectThrow(IllegalArgumentException.class, data, "put", new Bit(), RealValue.create());
		}
		
	}
	
	
	public static <D extends IDatum> void assertInvariants(FactorGraphData<D> data)
	{
		final FactorGraph graph = data.graph();
		assertTrue(data.layer().sharesRoot(graph));
		
		final Class<D> baseType = data.baseType();
		assertTrue(IDatum.class.isAssignableFrom(baseType));
		
		assertTrue(data.objectEquals(data));
		assertFalse(data.objectEquals(null));
		assertFalse(data.objectEquals("bogus"));
		
		FactorGraph otherGraph = new FactorGraph();
		DataLayer<IDatum> otherLayer = DataLayer.createSparse(otherGraph);
		assertFalse(data.objectEquals(otherLayer.createDataForGraph(otherGraph)));
			
		assertFalse(data.containsKey("not a variable"));
		assertNull(data.get((Object)null));
		assertNull(data.get("not a variable"));
		assertNull(data.get(new Real()));
		assertNull(data.remove("not a variable"));
		assertNull(data.remove(new Real()));
		
		final int size = data.size();
		final Set<Variable> vars = data.keySet();
		final Set<Map.Entry<Variable,D>> entries = data.entrySet();
		final Collection<? extends DataEntry<D>> entries2 = data.entries();
		final Collection<D> values = data.values();
		
		assertEquals(size, vars.size());
		assertEquals(size, entries.size());
		assertEquals(size, entries2.size());
		assertEquals(size, values.size());
		assertEquals(size == 0, data.isEmpty());
		
		assertFalse(vars.contains("not a variable"));
		assertFalse(vars.remove("not a variable"));
		assertFalse(entries.contains("not an entry"));
		assertFalse(entries.remove("not an entry"));

		PrimitiveIterator.OfInt indexIter = data.getLocalIndices().iterator();
		Iterator<Variable> varIter = vars.iterator();
		Iterator<D> valueIter = values.iterator();
		Iterator<Map.Entry<Variable,D>> entryIter = entries.iterator();
		Iterator<? extends DataEntry<D>> entryIter2 = entries2.iterator();
		
		int count = 0;
		while (entryIter.hasNext())
		{
			++count;
			assertTrue(varIter.hasNext());
			assertTrue(valueIter.hasNext());
			assertTrue(entryIter2.hasNext());
			
			Map.Entry<Variable,D> entry = entryIter.next();
			DataEntry<D> entry2 = entryIter2.next();
			assertEquals(entry2, entry);
			Variable var = entry.getKey();
			D datum = entry.getValue();
			final int localId = var.getLocalId();
			final int localIndex = Ids.indexFromLocalId(localId);
			
			assertEquals(localIndex, indexIter.nextInt());
			assertNotNull(entry);
			assertSame(var, varIter.next());
			assertSame(datum, valueIter.next());
			
			assertTrue(data.containsKey(var));
			assertTrue(vars.contains(var));
			assertTrue(entries.contains(entry));
			assertTrue(entries.contains(new AbstractMap.SimpleEntry<>(var,datum)));
			assertFalse(entries.contains(new AbstractMap.SimpleEntry<>(var,RealValue.create(Double.NaN))));
			assertFalse(entries.remove(new AbstractMap.SimpleEntry<>(var,RealValue.create(Double.NaN))));
			assertFalse(entries.add(entry));
			assertTrue(values.contains(datum));
			
			assertTrue(data.containsLocalIndex(localIndex));
			assertSame(datum, data.getByLocalIndex(localIndex));
			assertSame(datum, data.getByLocalId(localId));
			assertNull(data.getByLocalId(Ids.localIdFromParts(Ids.BOUNDARY_VARIABLE_TYPE, localIndex)));
			
			assertSame(datum, data.put(var, datum));
			assertSame(datum, data.setByLocalIndex(localIndex, datum));
		}
		
		assertEquals(size, count);
		
		DataLayer<?> oldLayer = data.layer();
		DataLayer<D> newLayer =
			new DataLayer<>(oldLayer.rootGraph(), DenseFactorGraphData.constructorForType(baseType));
		FactorGraphData<D> copy = data.clone(newLayer);
		assertNotSame(copy, data);
		assertTrue(data.objectEquals(copy));
		
		if (data.isEmpty())
		{
			assertEquals(copy, data);
		}
		else
		{
			assertNotEquals(copy, data);
			
			Set<? extends DataEntry<D>> copiedEntries = copy.entries();
			DataEntry<D> firstEntry = Iterables.getFirst(copiedEntries, null);
			assertTrue(copiedEntries.remove(firstEntry));
			assertFalse(copiedEntries.remove(firstEntry));
			
			assertFalse(data.objectEquals(copy));
			
			copy.put(firstEntry.variable(), baseType.cast(RealValue.create(4444)));
			assertFalse(data.objectEquals(copy));
		}
	}
}
