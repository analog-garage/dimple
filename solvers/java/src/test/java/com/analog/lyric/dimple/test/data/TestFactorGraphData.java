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
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.collect.PrimitiveIterator;
import com.analog.lyric.dimple.data.DataDensity;
import com.analog.lyric.dimple.data.DataEntry;
import com.analog.lyric.dimple.data.DataLayer;
import com.analog.lyric.dimple.data.DataLayerBase;
import com.analog.lyric.dimple.data.DenseFactorGraphData;
import com.analog.lyric.dimple.data.FactorGraphData;
import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.data.SparseFactorGraphData;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.IFactorGraphChild;
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
		test(DenseFactorGraphData.constructorForType(Variable.class, IDatum.class));
		test(SparseFactorGraphData.constructorForType(Variable.class, IDatum.class));
	}
	
	public <D extends IDatum> void test(FactorGraphData.Constructor<Variable,D> dataConstructor)
	{
		Class<D> type = dataConstructor.baseType();
		
		FactorGraph root = new FactorGraph("root");
		try (CurrentModel cur = using(root))
		{
			Real[] r = reals("r", 100);
			
			DataLayerBase<Variable,D> layer = new DataLayer<>(root, dataConstructor);
			
			FactorGraphData<Variable,D> data = dataConstructor.apply(layer, root);
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
	
	public static <K extends IFactorGraphChild, D extends IDatum> void assertInvariants(FactorGraphData<K,D> data)
	{
		class DL<D2 extends IDatum> extends DataLayerBase<K,D2>
		{
			protected DL(FactorGraph graph, DataDensity density, Class<K> keyType, Class<D2> baseType)
			{
				super(graph, density, keyType, baseType);
			}
			
			public DL(FactorGraph graph, FactorGraphData.Constructor<K,D2> constructor)
			{
				super(graph, constructor);
			}

			private DL(DL<D2> other)
			{
				super(other);
			}
			
			@Override
			public DataLayerBase<K, D2> clone()
			{
				return new DL<>(this);
			}
		}
		
		final FactorGraph graph = data.graph();
		assertTrue(data.layer().sharesRoot(graph));
		
		final Class<K> keyType = data.keyType();
		assertTrue(IFactorGraphChild.class.isAssignableFrom(keyType));
		
		final Class<D> baseType = data.baseType();
		assertTrue(IDatum.class.isAssignableFrom(baseType));
		
		assertTrue(data.objectEquals(data));
		assertFalse(data.objectEquals(null));
		assertFalse(data.objectEquals("bogus"));
		
		FactorGraph otherGraph = new FactorGraph();
		DataLayerBase<K,IDatum> otherLayer = new DL<>(otherGraph, DataDensity.SPARSE, keyType, IDatum.class);
		assertFalse(data.objectEquals(otherLayer.createDataForGraph(otherGraph)));
			
		assertFalse(data.containsKey("not a variable"));
		assertFalse(data.containsLocalIndex(-1));
		assertNull(data.get((Object)null));
		assertNull(data.get("not a variable"));
		assertNull(data.get(new Real()));
		assertNull(data.remove("not a variable"));
		assertNull(data.remove(new Real()));
		
		final int size = data.size();
		final Set<K> keys = data.keySet();
		final Set<Map.Entry<K,D>> entries = data.entrySet();
		final Collection<? extends DataEntry<K,D>> entries2 = data.entries();
		final Collection<D> values = data.values();
		
		assertEquals(size, keys.size());
		assertEquals(size, entries.size());
		assertEquals(size, entries2.size());
		assertEquals(size, values.size());
		assertEquals(size == 0, data.isEmpty());
		
		assertFalse(keys.contains("not a variable"));
		assertFalse(keys.remove("not a variable"));
		assertFalse(entries.contains("not an entry"));
		assertFalse(entries.remove("not an entry"));

		PrimitiveIterator.OfInt indexIter = data.getLocalIndices().iterator();
		PrimitiveIterator.OfInt indexIter2 = data.getLocalIndices().iterator();
		Iterator<K> keyIter = keys.iterator();
		Iterator<D> valueIter = values.iterator();
		Iterator<Map.Entry<K,D>> entryIter = entries.iterator();
		Iterator<? extends DataEntry<K,D>> entryIter2 = entries2.iterator();
		
		int count = 0;
		while (entryIter.hasNext())
		{
			++count;
			assertTrue(keyIter.hasNext());
			assertTrue(valueIter.hasNext());
			assertTrue(entryIter2.hasNext());
			
			Map.Entry<K,D> entry = entryIter.next();
			DataEntry<K,D> entry2 = entryIter2.next();
			assertEquals(entry2, entry);
			K key = entry.getKey();
			D datum = entry.getValue();
			final int localId = key.getLocalId();
			final int localIndex = Ids.indexFromLocalId(localId);
			
			assertEquals(localIndex, indexIter.nextInt());
			assertEquals(localIndex, (int)indexIter2.next());
			assertNotNull(entry);
			assertSame(key, keyIter.next());
			assertSame(datum, valueIter.next());
			
			assertTrue(data.containsKey(key));
			assertTrue(keys.contains(key));
			assertTrue(entries.contains(entry));
			assertTrue(entries.contains(new AbstractMap.SimpleEntry<>(key,datum)));
			assertFalse(entries.contains(new AbstractMap.SimpleEntry<>(key,RealValue.create(Double.NaN))));
			assertFalse(entries.remove(new AbstractMap.SimpleEntry<>(key,RealValue.create(Double.NaN))));
			assertFalse(entries.add(entry));
			assertTrue(values.contains(datum));
			
			assertTrue(data.containsLocalIndex(localIndex));
			assertSame(datum, data.getByLocalIndex(localIndex));
			assertSame(datum, data.getByLocalId(localId));
			assertNull(data.getByLocalId(Ids.localIdFromParts(Ids.BOUNDARY_VARIABLE_TYPE, localIndex)));
			
			assertSame(datum, data.put(key, datum));
			assertSame(datum, data.setByLocalIndex(localIndex, datum));
		}
		try
		{
			indexIter.next();
			fail("expected NoSuchElementException");
		}
		catch (NoSuchElementException ex)
		{
		}
		
		assertEquals(size, count);
		
		DataLayerBase<?,?> oldLayer = data.layer();
		DataLayerBase<K,D> newLayer =
			new DL<>(oldLayer.rootGraph(), DenseFactorGraphData.constructorForType(keyType, baseType));
		FactorGraphData<K,D> copy = data.clone(newLayer);
		assertNotSame(copy, data);
		assertTrue(data.objectEquals(copy));
		
		Map<K,D> map = new HashMap<>(data);
		assertEquals(data, map);
		
		if (data.isEmpty())
		{
			assertEquals(copy, data);
		}
		else if (!data.isView())
		{
			Set<? extends DataEntry<K,D>> copiedEntries = copy.entries();
			DataEntry<K,D> firstEntry = Iterables.getFirst(copiedEntries, null);
			assertTrue(copiedEntries.remove(firstEntry));
			assertFalse(copiedEntries.remove(firstEntry));
			
			assertFalse(data.objectEquals(copy));
			
			if (baseType.isAssignableFrom(RealValue.class))
			{
				copy.put(firstEntry.getKey(), baseType.cast(RealValue.create(4444)));
				assertFalse(data.objectEquals(copy));
			}
		}
	}
}
