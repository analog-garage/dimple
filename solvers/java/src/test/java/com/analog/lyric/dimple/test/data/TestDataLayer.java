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
import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.dimple.data.DataDensity;
import com.analog.lyric.dimple.data.DataLayer;
import com.analog.lyric.dimple.data.DataLayerBase;
import com.analog.lyric.dimple.data.DenseFactorGraphData;
import com.analog.lyric.dimple.data.FactorGraphData;
import com.analog.lyric.dimple.data.GenericDataLayer;
import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.data.SparseFactorGraphData;
import com.analog.lyric.dimple.data.ValueDataLayer;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.IFactorGraphChild;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.google.common.collect.Iterables;

/**
 * Unit tests for {@link DataLayerBase}.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class TestDataLayer extends DimpleTestBase
{
	@Test
	public void test()
	{
		FactorGraph root = new FactorGraph("root"), nested1, nested2, nested3;
		
		try (CurrentModel cur = using(root))
		{
			reals("a", 100);
			
			nested1 = root.addGraph(new FactorGraph());
			nested2 = root.addGraph(new FactorGraph());
			
			try (CurrentModel cur1 = using(nested1))
			{
				reals("b", 50);
				
				nested3 = nested2.addGraph(new FactorGraph());
				try (CurrentModel cur3 = using(nested3))
				{
					reals("c", 50);
				}
			}
			
			try (CurrentModel cur2 = using(nested2))
			{
				reals("d", 50);
			}
		}
		
		test(root, DenseFactorGraphData.constructorForType(Variable.class, IDatum.class));
		test(root, DenseFactorGraphData.constructorForType(Variable.class, Value.class));
		test(root, SparseFactorGraphData.constructorForType(Variable.class, IDatum.class));
		
		ValueDataLayer sampleLayer = ValueDataLayer.dense(root);
		assertSame(Value.class, sampleLayer.baseType());
		test(sampleLayer);

		ValueDataLayer sampleLayer2 = new ValueDataLayer(nested3);
		assertSame(root, sampleLayer2.rootGraph());
		
		DataLayer<RealValue> sampleLayer3 = new DataLayer<>(root, DataDensity.DENSE, RealValue.class);
		assertSame(RealValue.class, sampleLayer3.baseType());
		assertSame(DenseFactorGraphData.class, sampleLayer3.createDataForGraph(root).getClass());
		test(sampleLayer3);
		
		DataLayerBase<?,?> sparse = new DataLayer<>(root, DataDensity.SPARSE, Value.class);
		assertSame(Value.class, sparse.baseType());
		assertSame(SparseFactorGraphData.class, sparse.createDataForGraph(root).getClass());

		test(new GenericDataLayer(root));
	}
	
	<D extends IDatum> void test(FactorGraph graph, FactorGraphData.Constructor<Variable,D> constructor)
	{
		DataLayerBase<Variable,D> layer = new DataLayer<D>(graph, constructor);
		assertSame(constructor, layer.dataConstructor());
		test(layer);
	}
	
	<D extends IDatum> void test(DataLayerBase<Variable, D> layer)
	{
		FactorGraph root = layer.rootGraph();
		
		assertEquals(0, layer.size());
		assertInvariants(layer);

		Set<Variable> dataVars = new HashSet<>();
		final ArrayList<Variable> allVars = new ArrayList<>(root.getVariables());
		for (int i = 0; i < 25; ++i)
		{
			Variable var = allVars.get(testRand.nextInt(allVars.size()));
			if (dataVars.add(var))
			{
				assertFalse(layer.containsKey(var));
				assertNull(layer.get(var));

				assertNull(layer.put(var, null));
				assertNull(layer.get(var));
				
				Value value = RealValue.create(i);
				assertNull(layer.put(var, layer.baseType().cast(value)));
				assertSame(value, layer.get(var));
				
				if (layer instanceof DataLayer)
				{
					DataLayer<D> dataLayer = (DataLayer<D>)layer;
					dataLayer.set(var, null);
					assertNull(layer.get(var));
					
					dataLayer.set(var, value.getObject());
					assertNotSame(value, layer.get(var));
					assertTrue(value.valueEquals(requireNonNull((Value)(layer.get(var)))));
					dataLayer.set(var, value.getObject());
					assertTrue(value.valueEquals(requireNonNull((Value)(layer.get(var)))));
					expectThrow(ClassCastException.class, layer, "set", var, "bogus");
					
					dataLayer.set(var, value);
					assertSame(value, layer.get(var));
				}
			}
			
			assertEquals(dataVars.size(), layer.size());
		}
		
		assertInvariants(layer);

		Map<Variable,D> copy = new HashMap<>(layer);
		assertEquals(layer.size(), copy.size());
		assertEquals(copy, layer);
		
		layer.clear();
		assertTrue(layer.isEmpty());
		assertInvariants(layer);
		
		FactorGraphData<Variable,D> fgd = layer.getDataForGraph(root);
		assertTrue(fgd == null || fgd.isEmpty());
		
		layer.putAll(copy);
		assertEquals(copy, layer);
		
		layer.entrySet().clear();
		assertTrue(layer.isEmpty());
		assertInvariants(layer);
		
		layer.putAll(copy);
		
		layer.keySet().clear();
		assertTrue(layer.isEmpty());
		assertInvariants(layer);
		
		layer.putAll(copy);
		
		Set<Variable> removeVars = new HashSet<>();
		for (Variable var : Iterables.limit(dataVars, 3))
		{
			removeVars.add(var);
		}
		
		for (Variable var : removeVars)
		{
			assertSame(layer.get(var), layer.remove(var));
			assertNull(layer.remove(var));
			assertFalse(layer.containsKey(var));
		}
		assertEquals(copy.size() - 3, layer.size());
		assertInvariants(layer);
		
		for (Variable var : removeVars)
		{
			assertTrue(layer.entrySet().add(new AbstractMap.SimpleEntry<>(var, copy.get(var))));
		}
		assertEquals(copy, layer);
		
		for (Variable var : removeVars)
		{
			assertTrue(layer.keySet().remove(var));
			assertFalse(layer.keySet().remove(var));
		}
		assertEquals(copy.size() - 3, layer.size());
		assertInvariants(layer);

		for (Variable var : removeVars)
		{
			assertNull(layer.put(var, copy.get(var)));
		}
		assertEquals(copy, layer);
		
		for (Variable var : removeVars)
		{
			assertTrue(layer.entrySet().remove(new AbstractMap.SimpleEntry<>(var, copy.get(var))));
		}
		assertEquals(copy.size() - 3, layer.size());
		assertInvariants(layer);
		
		for (Variable var : removeVars)
		{
			assertNull(layer.put(var, copy.get(var)));
		}
		assertEquals(copy, layer);
		
		FactorGraphData<Variable,D> oldData = requireNonNull(layer.getDataForGraph(root));
		assertSame(oldData, layer.removeDataForGraph(root));
		assertNull(layer.removeDataForGraph(root));
		
		for (Variable var : oldData.keySet())
		{
			assertFalse(layer.containsKey(var));
			assertNull(layer.getByGraphTreeId(var.getGraphTreeId()));
		}
		assertEquals(copy.size() - oldData.size(), layer.size());
		assertInvariants(layer);
		
		assertNull(layer.setDataForGraph(oldData));
		assertSame(oldData, layer.setDataForGraph(oldData));
		
		expectThrow(IllegalArgumentException.class, layer, "put", new Real(), null);
		
//		DataLayer<IDatum> layer2 = new DataLayer<>(root);
//		expectThrow(IllegalArgumentException.class, layer2, "setDataForGraph", layer.getDataForGraph(root));
	}
	
	public static <K extends IFactorGraphChild, D extends IDatum> void assertInvariants(DataLayerBase<K,D> layer)
	{
		final Class<K> keyType = layer.keyType();
		final Class<D> baseType = layer.baseType();
		final FactorGraph root = layer.rootGraph();
		assertTrue(layer.sharesRoot(root));
		assertEquals(baseType.isAssignableFrom(Value.class) || Value.class.isAssignableFrom(baseType),
			layer.allowsValues());
		
		assertFalse(layer.containsKey("not a variable"));
		assertNull(layer.getDataForGraph(new FactorGraph()));
		assertNull(layer.removeDataForGraph(new FactorGraph()));
		assertNull(layer.get(new Real()));
		assertNull(layer.remove("not a variable"));
		
		int sizeFromData = 0;
		for (FactorGraphData<?,?> data : layer.getData())
		{
			assertSame(data, layer.getDataForGraph(data.graph()));
			TestFactorGraphData.assertInvariants(data);
			sizeFromData += data.size();
		}
		
		final int size = layer.size();
		assertEquals(sizeFromData, size);
		assertEquals(size == 0, layer.isEmpty());
		
		Set<K> keys = layer.keySet();
		Set<Map.Entry<K, D>> entries = layer.entrySet();
		Collection<D> values = layer.values();
		
		assertEquals(size, keys.size());
		assertEquals(size, entries.size());
		assertEquals(size, values.size());
		
		assertFalse(keys.contains("not a key"));
		assertFalse(keys.contains(new Real()));
		assertFalse(entries.contains("not an entry"));
		assertFalse(keys.remove("not a key"));
		assertFalse(keys.remove(new Real()));
		assertFalse(entries.remove("not an entry"));
		
		int count = 0;
		Iterator<K> keyIter = keys.iterator();
		Iterator<Map.Entry<K,D>> entryIter = entries.iterator();
		Iterator<D> valueIter = values.iterator();
		
		while (entryIter.hasNext())
		{
			++count;
			assertTrue(keyIter.hasNext());
			assertTrue(valueIter.hasNext());
			
			Map.Entry<K,D> entry = entryIter.next();
			K key = entry.getKey();
			IDatum datum = entry.getValue();
			
			assertSame(key, keyIter.next());
			assertSame(datum, valueIter.next());
			
			assertTrue(layer.containsKey(key));
			assertTrue(layer.containsDataFor(key));
			assertTrue(keys.contains(key));
			assertTrue(entries.contains(entry));
			assertTrue(entries.contains(new AbstractMap.SimpleEntry<>(key, datum)));
			assertFalse(entries.contains(new AbstractMap.SimpleEntry<>(key, RealValue.create(Double.NaN))));
			assertFalse(entries.remove(new AbstractMap.SimpleEntry<>(key, RealValue.create(Double.NaN))));
			assertFalse(entries.add(entry));
			
			long graphTreeId = key.getGraphTreeId();
			assertSame(datum, layer.getByGraphTreeId(graphTreeId));
			assertSame(datum, layer.get(graphTreeId));
			assertSame(datum, layer.get(key.getGlobalId()));
			if (Node.class.isAssignableFrom(keyType))
			{
				assertSame(datum, layer.get(((Node)key).getQualifiedName()));
			}
			
			int graphTreeIndex = Ids.graphTreeIndexFromGraphTreeId(graphTreeId);
			int localId = Ids.localIdFromGraphTreeId(graphTreeId);
			int localIndex = Ids.indexFromLocalId(localId);
			assertSame(datum, layer.getByGraphTreeAndLocalIndices(graphTreeIndex, localIndex));
			int bogusLocalId = Ids.localIdFromParts(Ids.BOUNDARY_VARIABLE_TYPE, localIndex);
			assertNull(layer.getByGraphTreeId(Ids.graphTreeIdFromParts(graphTreeIndex, bogusLocalId)));
		}
		assertFalse(keyIter.hasNext());
		assertFalse(valueIter.hasNext());
		
		assertEquals(size, count);
		
		assertTrue(layer.objectEquals(layer));
		assertFalse(layer.objectEquals(null));
		assertFalse(layer.objectEquals("bogus"));
		assertFalse(layer.objectEquals(new GenericDataLayer(new FactorGraph(), DataDensity.DENSE)));
		
		DataLayerBase<K,D> copy = layer.clone();
		assertTrue(layer.objectEquals(copy));
		
		Map<K,D> map = new HashMap<>(layer);
		assertEquals(layer, map);
		
		if (layer.isEmpty())
		{
			assertEquals(copy, layer);
			FactorGraphData<K,D> data = copy.createDataForGraph(root);
			expectThrow(IllegalArgumentException.class, layer, "setDataForGraph", data);
			assertTrue(layer.objectEquals(copy));
			assertTrue(copy.objectEquals(layer));
			
			if (Variable.class.isAssignableFrom(keyType) && !layer.isView())
			{
				Variable var = root.getOwnedVariables().iterator().next();
				copy.put(keyType.cast(var), baseType.cast(RealValue.create(234.234)));
				assertFalse(layer.objectEquals(copy));
				assertFalse(copy.objectEquals(layer));
				}
			}
		else if (!layer.isView())
		{
			assertEquals(copy, layer);
			
			if (Variable.class.isAssignableFrom(keyType))
			{
				Variable var = (Variable)layer.keySet().iterator().next();
				assertNotNull(copy.put(keyType.cast(var), baseType.cast(RealValue.create(234.234))));
				assertFalse(layer.objectEquals(copy));
				assertFalse(copy.objectEquals(layer));
			}
		}
	}
	
}
