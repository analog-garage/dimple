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

package com.analog.lyric.dimple.test.FactorFunctions.core;

import static com.analog.lyric.math.Utilities.*;
import static com.analog.lyric.util.test.ExceptionTester.*;
import static java.util.Objects.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import cern.colt.map.OpenIntIntHashMap;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.collect.Comparators;
import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableEntry;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTableBase;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTableIterator;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.DiscreteIndicesIterator;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.util.test.SerializationTester;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class TestFactorTable extends DimpleTestBase
{
	final Random rand = new Random(42);
	final DiscreteDomain domain2 = DiscreteDomain.range(0,1);
	final DiscreteDomain domain3 = DiscreteDomain.range(0,2);
	final DiscreteDomain domain6 = DiscreteDomain.range(0,5);
	final DiscreteDomain domain32 = DiscreteDomain.range(0,31);
	final DiscreteDomain domain256 = DiscreteDomain.range(0, 255);
	final DiscreteDomain domainMax = DiscreteDomain.range(0,Integer.MAX_VALUE - 1);
	
	@Test
	public void testFactorTable()
	{
		IFactorTable t2x3 = FactorTable.create(domain2, domain3);
		assertEquals(2, t2x3.getDimensions());
		assertEquals(domain2, t2x3.getDomainIndexer().get(0));
		assertEquals(domain3, t2x3.getDomainIndexer().get(1));
		assertFalse(t2x3.getRepresentation().hasDense());
		assertFalse(t2x3.isDirected());
		assertEquals(FactorTableRepresentation.SPARSE_ENERGY, t2x3.getRepresentation());
		assertInvariants(t2x3);
		assertEquals(0.0, t2x3.density(), 0.0);
		
		t2x3.setRepresentation(FactorTableRepresentation.DENSE_ENERGY);
		assertEquals(FactorTableRepresentation.DENSE_ENERGY, t2x3.getRepresentation());
		assertTrue(t2x3.getRepresentation().hasDense());
		assertInvariants(t2x3);
		
		t2x3.setRepresentation(FactorTableRepresentation.SPARSE_ENERGY);
		assertFalse(t2x3.getRepresentation().hasDense());
		assertEquals(0, t2x3.sparseSize());
		assertInvariants(t2x3);
		
		t2x3.setRepresentation(FactorTableRepresentation.ALL_ENERGY);
		t2x3.randomizeWeights(rand);
		assertInvariants(t2x3);
		assertEquals(1.0, t2x3.density(), 0.0);
		
		assertEquals(FactorTableRepresentation.ALL_ENERGY, t2x3.getRepresentation());
		t2x3.setRepresentation(FactorTableRepresentation.ALL_VALUES);
		assertEquals(FactorTableRepresentation.ALL_VALUES, t2x3.getRepresentation());
		assertInvariants(t2x3);
		
		t2x3.normalize();
		assertInvariants(t2x3);
		
		t2x3.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT);
		assertEquals(FactorTableRepresentation.SPARSE_WEIGHT, t2x3.getRepresentation());
		assertInvariants(t2x3);
		
		try
		{
			t2x3.setWeightsDense(new double[] { 1,2,3 });
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Bad dense length"));
		}
		try
		{
			t2x3.setEnergiesDense(new double[] { 1,2,3 });
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Bad dense length"));
		}
		t2x3.setWeightsDense(new double[] { 1, 2, 3, 4, 5, 6});
		assertEquals(FactorTableRepresentation.DENSE_WEIGHT, t2x3.getRepresentation());
		for (int ji = 0; ji < 6; ++ji)
		{
			assertEquals(ji + 1, t2x3.getWeightForJointIndex(ji), 0.0);
		}
		assertInvariants(t2x3);

		t2x3.setEnergiesDense(new double[] { 2,4,6,8, 10, 12});
		assertEquals(FactorTableRepresentation.DENSE_ENERGY, t2x3.getRepresentation());
		for (int ji = 0; ji < 6; ++ji)
		{
			assertEquals(2 * (ji + 1), t2x3.getEnergyForJointIndex(ji), 0.0);
		}
		assertInvariants(t2x3);
		
		try
		{
			t2x3.setWeightsSparse(new int[] {1}, new double[] {2,3});
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Arrays have different sizes"));
		}
		try
		{
			t2x3.setWeightsSparse(new int[] {1, 6}, new double[] {2,3});
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Joint index 6 is out of range"));
		}
		try
		{
			t2x3.setWeightsSparse(new int[] {1, -1}, new double[] {2,3});
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Joint index -1 is out of range"));
		}

		try
		{
			t2x3.setWeightsSparse(new int[] {1, 2, 1}, new double[] {2,3,4});
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Multiple entries with same set of indices [1, 0]"));
		}

		t2x3.setWeightsSparse(new int[] { 1,2,3}, new double[] { 1,2,3});
		assertEquals(3, t2x3.sparseSize());
		assertEquals(FactorTableRepresentation.SPARSE_WEIGHT, t2x3.getRepresentation());
		for (int i = 0; i < 3; ++i)
		{
			assertEquals(i+1, t2x3.getWeightForSparseIndex(i), 0.0);
			assertEquals(i+1, t2x3.getWeightForJointIndex(i+1), 0.0);
		}
		assertInvariants(t2x3);
		t2x3.setWeightsSparse(new int[] { 3,1,2}, new double[] { 3,1,2});
		assertEquals(3, t2x3.sparseSize());
		assertEquals(FactorTableRepresentation.SPARSE_WEIGHT, t2x3.getRepresentation());
		for (int i = 0; i < 3; ++i)
		{
			assertEquals(i+1, t2x3.getWeightForSparseIndex(i), 0.0);
			assertEquals(i+1, t2x3.getWeightForJointIndex(i+1), 0.0);
		}
		assertInvariants(t2x3);
		t2x3.setEnergiesSparse(new int[] { 1,2,3}, new double[] { 1,2,3});
		assertEquals(3, t2x3.sparseSize());
		assertEquals(FactorTableRepresentation.SPARSE_ENERGY, t2x3.getRepresentation());
		for (int i = 0; i < 3; ++i)
		{
			assertEquals(i+1, t2x3.getEnergyForSparseIndex(i), 0.0);
			assertEquals(i+1, t2x3.getEnergyForJointIndex(i+1), 0.0);
		}
		assertInvariants(t2x3);
		
		BitSet xor3Output = new BitSet(3);
		xor3Output.set(1);
		IFactorTable xor2 = FactorTable.create(xor3Output, domain2, domain2, domain2);
		assertInvariants(xor2);
		assertTrue(xor2.isDirected());
		assertFalse(xor2.isDeterministicDirected());
		
		for (int i = 0; i < 2; ++i)
		{
			for (int j = 0; j < 2; ++j)
			{
				xor2.setWeightForIndices(1.0, i, i^j, j);
			}
		}
		assertTrue(xor2.isDeterministicDirected());
		assertEquals(.5, xor2.density(), 0.0);
		assertInvariants(xor2);
		
		xor2.setDirected(null);
		assertFalse(xor2.isDirected());
		assertInvariants(xor2);
		
		xor2.setConditional(xor3Output);
		assertTrue(xor2.isConditional());
		assertTrue(xor2.isDeterministicDirected());
		xor2.setEnergyForSparseIndex(23.0, 1);
		assertEquals(23.0, xor2.getEnergyForSparseIndex(1), 0.0);
		assertFalse(xor2.isDeterministicDirected());
		xor2.setEnergyForSparseIndex(0.0, 1);
		assertTrue(xor2.isDeterministicDirected());
		assertInvariants(xor2);
		
		xor2.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT);
		xor2.setDirected(null);
		xor2.setDirected(xor3Output);
		assertTrue(xor2.isDeterministicDirected());
		xor2.setWeightForSparseIndex(23.0, 1);
		assertEquals(23.0, xor2.getWeightForSparseIndex(1), 0.0);
		assertFalse(xor2.isDeterministicDirected());
		assertFalse(xor2.isConditional());
		try
		{
			xor2.setConditional(Objects.requireNonNull(xor2.getDomainIndexer().getOutputSet()));
			fail("expected exception");
		}
		catch (DimpleException ex)
		{
			assertThat(ex.getMessage(), containsString("weights must be normalized correctly for directed"));
		}
		xor2.normalizeConditional();
		assertTrue(xor2.isConditional());
		assertTrue(xor2.isDeterministicDirected());
		assertEquals(1.0, xor2.getWeightForSparseIndex(1), 0.0);
		xor2.setWeightForSparseIndex(23.0, 1);
		xor2.makeConditional(Objects.requireNonNull(xor2.getDomainIndexer().getOutputSet()));
		assertTrue(xor2.isDeterministicDirected());
		
		xor2.setRepresentation(FactorTableRepresentation.DENSE_ENERGY);
		xor2.setDirected(null);
		xor2.setDirected(xor3Output);
		assertTrue(xor2.isDeterministicDirected());
		xor2.setEnergyForJointIndex(23.0, 0);
		assertEquals(23.0, xor2.getEnergyForJointIndex(0), 0.0);
		assertFalse(xor2.isDeterministicDirected());
		xor2.setEnergyForJointIndex(0.0, 0);
		assertTrue(xor2.isDeterministicDirected());

		xor2.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
		xor2.setDirected(null);
		xor2.setDirected(xor3Output);
		assertTrue(xor2.isDeterministicDirected());

		testRandomOperations(xor2, 10000);
		
		// Test automatic representation changes by get* methods
		xor2.setRepresentation(FactorTableRepresentation.SPARSE_ENERGY);
		xor2.setEnergyForSparseIndex(2.3, 0);
		assertEquals(energyToWeight(2.3), xor2.getWeightForSparseIndex(0), 1e-12);
		assertEquals(FactorTableRepresentation.SPARSE_ENERGY, xor2.getRepresentation());
		xor2.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
		assertEquals(energyToWeight(2.3), xor2.getWeightForSparseIndex(0), 1e-12);
		assertEquals(FactorTableRepresentation.ALL_WEIGHT, xor2.getRepresentation());
		xor2.setRepresentation(FactorTableRepresentation.DENSE_ENERGY);
		assertEquals(energyToWeight(2.3), xor2.getWeightForSparseIndex(0), 1e-12);
		assertEquals(FactorTableRepresentation.ALL_ENERGY, xor2.getRepresentation());
		
		IFactorTable t2x2x2 = xor2.clone();
		assertBaseEqual(t2x2x2, xor2);
		t2x2x2.setWeightForIndices(.5, 1, 1, 1);
		assertFalse(t2x2x2.isDeterministicDirected());
		assertInvariants(t2x2x2);
		
		IFactorTable t2x3x4 = FactorTable.create(domain2, domain3, domain6);
		t2x3.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
		t2x3x4.randomizeWeights(rand);
		assertInvariants(t2x3x4);
		testRandomOperations(t2x3x4, 10000);
		
		expectThrow(NullPointerException.class, xor2, "setConditional", new Object[] { null } );
		
	}

	@Test
	public void testSparseFactorTable()
	{
		final DiscreteDomain[] d2by32 = new DiscreteDomain[32];
		Arrays.fill(d2by32, domain2);
		
		IFactorTable table = FactorTable.create(d2by32);
		assertEquals(0, table.sparseSize());
		assertFalse(table.hasMaximumDensity());
		assertFalse(table.isConditional());
		assertFalse(table.supportsJointIndexing());
		assertInvariants(table);
		
		testRandomOperations(table, 500);
		
		final DiscreteDomain[] dmaxby2 = new DiscreteDomain[2];
		Arrays.fill(dmaxby2, domainMax);
		
		table = FactorTable.create(dmaxby2);
		assertEquals(0, table.sparseSize());
		assertFalse(table.hasMaximumDensity());
		assertFalse(table.isConditional());
		assertFalse(table.supportsJointIndexing());
		assertInvariants(table);
		
		testRandomOperations(table, 500);
		
		table = FactorTable.create(dmaxby2);
		assertEquals(0, table.sparseSize());
		assertFalse(table.hasMaximumDensity());
		assertFalse(table.isConditional());
		assertFalse(table.supportsJointIndexing());
		assertInvariants(table);
		
		testRandomOperations(table, 1000);
		
		table.setDirected(BitSetUtil.bitsetFromIndices(12, 0));
		assertTrue(table.isDirected());
		assertArrayEquals(new int[] {0}, table.getDomainIndexer().getOutputDomainIndices());
		assertInvariants(table);
		
		int nOutputsPerInput = 3;
		int nInputs = 33;
		int n = nOutputsPerInput * nInputs;
		int[][] sparseIndices = new int[n][];
		for (int i = 0; i < nInputs; ++i)
		{
			int[] indices = table.getDomainIndexer().randomIndices(rand, null);
			for (int j = 0; j < nOutputsPerInput; ++j)
			{
				indices[0] = j;
				sparseIndices[i*nOutputsPerInput + j] = indices.clone();
			}
		}
		double[] sparseWeights = new double[n];
		for (int i = 0; i < n; ++i)
		{
			sparseWeights[i] = rand.nextDouble();
		}
		
		table.setWeightsSparse(sparseIndices, sparseWeights);
		assertTrue(table.isDirected());
		assertFalse(table.isConditional());
		assertFalse(table.isConditional());
		table.normalizeConditional();
		assertTrue(table.isConditional());
		expectThrow(DimpleException.class, ".*weights must be normalized.*", table, "setConditional",
			BitSetUtil.bitsetFromIndices(2, 1));
		assertArrayEquals(new int[] {1}, table.getDomainIndexer().getOutputDomainIndices());
		assertTrue(table.isDirected());
		assertFalse(table.isConditional());
		table.normalizeConditional();
		assertTrue(table.isConditional());
		table.makeConditional(BitSetUtil.bitsetFromIndices(2, 0));
		assertTrue(table.isConditional());
		
		expectNotDense(table, "fullIterator");
		expectNotDense(table, "getEnergyForIndicesDense", 1,2,3);
		expectNotDense(table, "getWeightForIndicesDense", 1,2,3);
		expectNotDense(table, "getEnergyForJointIndex", 42);
		expectNotDense(table, "getWeightForJointIndex", 42);
		expectNotDense(table, "sparseIndexFromJointIndex", 42);
		expectNotDense(table, "sparseIndexToJointIndex", 42);
		expectNotDense(table, "setEnergiesDense", ArrayUtil.EMPTY_DOUBLE_ARRAY);
		expectNotDense(table, "setWeightsDense", ArrayUtil.EMPTY_DOUBLE_ARRAY);
		expectNotDense(table, "setEnergyForJointIndex", 0.0, 42);
		expectNotDense(table, "setWeightForJointIndex", 0.0, 42);
		expectNotDense(table, "setEnergiesSparse", ArrayUtil.EMPTY_INT_ARRAY, ArrayUtil.EMPTY_DOUBLE_ARRAY);
		expectNotDense(table, "setWeightsSparse", ArrayUtil.EMPTY_INT_ARRAY, ArrayUtil.EMPTY_DOUBLE_ARRAY);
	}
	
	/**
	 * Test for {@link FactorTable#product}
	 */
	@Test
	public void testProduct()
	{
		
		final Map<IFactorTable, int[]> tables = new HashMap<IFactorTable, int[]>();
		
		final IFactorTable AxB = FactorTable.create(domain2, domain3);
		AxB.setRepresentation(FactorTableRepresentation.DENSE_ENERGY);
		AxB.randomizeWeights(rand);
		
		final IFactorTable BxC = FactorTable.create(domain3, domain2);
		BxC.setRepresentation(FactorTableRepresentation.ALL);
		BxC.randomizeWeights(rand);
		
		final IFactorTable BxD = FactorTable.create(domain3, domain256);
		BxD.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT);
		for (int n = 20; --n>=0;)
		{
			for (int b = 0; b < 3; ++b)
			{
				int d = rand.nextInt(256);
				BxD.setWeightForIndices(rand.nextDouble(), b, d);
			}
		}

		// Empty table map
		ArrayList<Tuple2<IFactorTable,int[]>> list = new ArrayList<Tuple2<IFactorTable,int[]>>();
		assertNull(FactorTable.product(list, FactorTableRepresentation.SPARSE_WEIGHT));
		
		// Errors
		list.add(Tuple2.create(AxB, new int[] {}));
		expectThrow(IllegalArgumentException.class,
			FactorTable.class, "product", list, FactorTableRepresentation.ALL);
		
		list.set(0, Tuple2.create(AxB, new int[] { 0, 1, 2}));
		expectThrow(IllegalArgumentException.class, ".*does not match table dimensions.*",
			FactorTable.class, "product", list, FactorTableRepresentation.ALL);
		
		list.set(0, Tuple2.create(AxB,  new int[] { 0, -1}));
		expectThrow(IllegalArgumentException.class, "Negative index mapping.*",
			FactorTable.class, "product", list, FactorTableRepresentation.ALL);
		
		list.set(0, Tuple2.create(AxB, new int[] { 0, 1 }));
		list.add(Tuple2.create(BxC, new int[] { 0, 2 }));
		expectThrow(IllegalArgumentException.class, "Conflicting domain mapping for entry.*",
			FactorTable.class, "product", list, FactorTableRepresentation.ALL);

		tables.clear();
		
		// Single table with same domain order produces a clone
		tables.put(AxB, new int[] { 0 , 1 });
		testProduct(tables);
		
		// Try swapping order.
		tables.put(AxB, new int[] { 1, 0 });
		testProduct(tables);
		
		tables.put(AxB, new int[] { 2, 1});
		tables.put(BxC, new int[] { 1, 0});
		testProduct(tables);
		
		// Test a sparse case
		tables.put(BxD, new int[] { 1, 3 });
		testProduct(tables);
	}
	
	private void testProduct(Map<IFactorTable, int[]> entryMap)
	{
		final ArrayList<Tuple2<IFactorTable, int[]>> entries =
			new ArrayList<Tuple2<IFactorTable, int[]>>(entryMap.size());
		for (Map.Entry<IFactorTable, int[]> entry : entryMap.entrySet())
		{
			entries.add(new Tuple2<IFactorTable,int[]>(entry));
		}

		final IFactorTable newTable =
			requireNonNull(FactorTable.product(entries, FactorTableRepresentation.ALL_SPARSE));
		assertEquals(FactorTableRepresentation.ALL_SPARSE, newTable.getRepresentation());
		
		class Tuple {
			final IFactorTable table;
			final int dimension;
			Tuple(IFactorTable table, int dimension)
			{
				this.table = table;
				this.dimension = dimension;
			}
		}
		
		final Map<IFactorTable, int[]> oldTableIndices = new HashMap<IFactorTable, int[]>();
		final Multimap<Integer, Tuple> inverseMap = HashMultimap.create();
		final ArrayList<DiscreteDomain> domains = new ArrayList<DiscreteDomain>();
		for (Tuple2<IFactorTable,int[]> entry : entries)
		{
			final IFactorTable oldTable = entry.first;
			final int[] old2new = entry.second;
			oldTableIndices.put(oldTable, oldTable.getDomainIndexer().allocateIndices(null));
			
			for (int from = 0; from < old2new.length; ++from)
			{
				int to = old2new[from];
				while (domains.size() <= to)
				{
					domains.add(null);
				}
				domains.set(to, oldTable.getDomainIndexer().get(from));
				inverseMap.put(to, new Tuple(oldTable, from));
			}
		}
		
		final JointDomainIndexer newIndexer = newTable.getDomainIndexer();
		assertArrayEquals(newIndexer.toArray(), domains.toArray());
		
		DiscreteIndicesIterator indicesIterator = new DiscreteIndicesIterator(newIndexer);
		while (indicesIterator.hasNext())
		{
			final int[] newIndices = indicesIterator.next();
			
			// Set corresponding indices for old factor tables
			for (int to = 0; to < newIndices.length; ++to)
			{
				for (Tuple tuple : inverseMap.get(to))
				{
					int[] oldIndices = oldTableIndices.get(tuple.table);
					oldIndices[tuple.dimension] = newIndices[to];
				}
			}
			
			double expectedWeight = 1.0;
			for (Tuple2<IFactorTable,int[]> entry : entries)
			{
				final IFactorTable oldTable = entry.first;
				int[] oldIndices = oldTableIndices.get(oldTable);
				expectedWeight *= oldTable.getWeightForIndices(oldIndices);
			}
			
			final double actualWeight = newTable.getWeightForIndices(newIndices);
			assertEquals(expectedWeight, actualWeight, 1e-10);
		}
	}
	
	/**
	 * Test for {@link IFactorTable#createTableConditionedOn(int[])} method.
	 */
	@Test
	public void testConditionOn()
	{
		IFactorTable table = FactorTable.create(domain3, domain3, domain3);
		
		expectThrow(ArrayIndexOutOfBoundsException.class, table, "createTableConditionedOn", new int[] { 1, 1} );
		expectThrow(ArrayIndexOutOfBoundsException.class, table, "createTableConditionedOn", new int[] { 1, 1} );
		expectThrow(IndexOutOfBoundsException.class, table, "createTableConditionedOn", new int[] { -1, 1, 3 } );
		
		table.setEnergyForIndices(2, 0, 0, 0);
		table.setEnergyForIndices(3, 1, 1, 1);
		table.setEnergyForIndices(4, 2, 2, 2);
		table.setEnergyForIndices(5, 0, 1, 2);
		testConditionOn(table, -1, -1, 2);
		testConditionOn(table, 0, -1, -1);
		
		table.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT_WITH_INDICES);
		testConditionOn(table, -1, -1, 2);
		testConditionOn(table, 0, -1, -1);
		
		table.setRepresentation(FactorTableRepresentation.DENSE_ENERGY);
		table.randomizeWeights(rand);
		
		testConditionOn(table, 1, -1, -1);
		testConditionOn(table, -1, 0, -1);
		testConditionOn(table, -1, -1, 2);
		
		table.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT);
		testConditionOn(table, 1, -1, 0);
		testConditionOn(table, -1, 2, -1);
		
		table.setDirected(BitSetUtil.bitsetFromIndices(3, 2));
		table.setDeterministicOutputIndices(new int[] { 0, 1, 2, 0, 1, 2, 0, 1, 2});
		testConditionOn(table, 1, -1, -1);
		testConditionOn(table, -1, 1, -1);
		
		table = FactorTable.create(domain256, domain256, domain256, domain256);
		
		int[][] valueMatrix = new int[4][4];
		for (int i = 0; i < 4; ++i)
		{
			for (int j = 0; j < 4; ++j)
			{
				valueMatrix[i][j] = rand.nextInt(256);
			}
		}
	
		for (int a : valueMatrix[0])
			for (int b : valueMatrix[1])
				for (int c : valueMatrix[2])
					for (int d : valueMatrix[3])
						table.setWeightForIndices(rand.nextDouble(), a, b, c, d);
		
		testConditionOn(table, valueMatrix[0][0], -1, -1, -1);
		testConditionOn(table, -1, valueMatrix[1][1], -1, -1);
		testConditionOn(table, valueMatrix[0][3], -1, valueMatrix[2][2], valueMatrix[3][1]);
		
		table.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT);
		testConditionOn(table, -1, -1, -1, valueMatrix[3][0]);
		testConditionOn(table, -1, valueMatrix[1][3], valueMatrix[2][2], valueMatrix[3][1]);
	}
	
	private void testConditionOn(IFactorTable table, int ... valueIndices)
	{
		IFactorTable newTable = table.createTableConditionedOn(valueIndices);
		
		JointDomainIndexer oldDomains = table.getDomainIndexer();
		assertEquals(oldDomains.size(), valueIndices.length);
		
		JointDomainIndexer newDomains = newTable.getDomainIndexer();
		
		int nRemoved = 0;
		int[] oldToNew = new int[oldDomains.size()];
		int[] newToOld = new int[newDomains.size()];
		for (int i = 0, j = 0; i < valueIndices.length; ++i)
		{
			int valueIndex = valueIndices[i];
			if (valueIndex < 0)
			{
				oldToNew[i] = j;
				newToOld[j] = i;
				++j;
			}
			else
			{
				oldToNew[i] = -1;
				++nRemoved;
			}
		}
		assertEquals(nRemoved, oldDomains.size() - newDomains.size());
		
		int[] oldIndices = oldDomains.allocateIndices(null);
		int[] newIndices = newDomains.allocateIndices(null);
		DiscreteIndicesIterator newIndicesIterator = new DiscreteIndicesIterator(newDomains, newIndices);
		while (newIndicesIterator.hasNext())
		{
			newIndicesIterator.next();
			for (int i = 0; i < oldIndices.length; ++i)
			{
				oldIndices[i] = oldToNew[i] >=0 ? newIndices[oldToNew[i]] : valueIndices[i];
			}
			
			double oldWeight = table.getWeightForIndices(oldIndices);
			double newWeight = newTable.getWeightForIndices(newIndices);
			assertEquals(oldWeight, newWeight, 1e-12);
		}
		
		IFactorTableIterator oldIter = table.iterator();
		outer:
		while (oldIter.hasNext())
		{
			FactorTableEntry entry = oldIter.next();
			requireNonNull(entry);
			entry.indices(oldIndices);
			
			for (int i = 0; i < oldIndices.length; ++i)
			{
				if (oldToNew[i] >= 0)
				{
					newIndices[oldToNew[i]] = oldIndices[i];
				}
				else
				{
					if (valueIndices[i] != oldIndices[i])
					{
						continue outer;
					}
				}
			}
			
			double oldWeight = entry.weight();
			double newWeight = newTable.getWeightForIndices(newIndices);
			assertEquals(oldWeight, newWeight, 1e-12);
		}
	}
	
	/**
	 * Test for {@link FactorTable#createMarginal} constructor.
	 */
	@Test
	public void testCreateMarginal()
	{
		testCreateMarginal(domain2, domain3, domain6);
	}
	
	private void testCreateMarginal(DiscreteDomain ... domains)
	{
		final JointDiscreteDomain<?> jointDomain = DiscreteDomain.joint(domains);
		final JointDomainIndexer jointIndexer = jointDomain.getDomainIndexer();
		final int nDomains = domains.length;
		final int jointSize = jointDomain.size();
		final int[] indices = jointIndexer.allocateIndices(null);
		
		assertEquals(nDomains, jointDomain.getDimensions());
		
		for (int di = 0; di < nDomains; ++di)
		{
			IFactorTable table = FactorTable.createMarginal(di, jointDomain);
			assertTrue(table.isDeterministicDirected());
			assertEquals(jointDomain.size(), table.sparseSize());
			assertSame(jointDomain, table.getDomainIndexer().get(1));
			assertSame(jointIndexer.get(di), table.getDomainIndexer().get(0));
			
			for (int ji = 0; ji < jointSize; ++ji)
			{
				jointDomain.getElementIndices(ji, indices);
				assertEquals(1.0, table.getWeightForIndices(indices[di], ji), 0.0);
			}
		}
	}
	
	@Test
	@Ignore
	public void performanceComparison()
	{
		int iterations = 10000;
		
		DiscreteDomain domain10 = DiscreteDomain.range(0,9);
		DiscreteDomain domain20 = DiscreteDomain.range(0,19);
		DiscreteDomain domain5 = DiscreteDomain.range(0,4);
		DiscreteDomain oneDie = DiscreteDomain.range(1,6);
		DiscreteDomain twoDice = DiscreteDomain.range(2,12);

		IFactorTable newTable = FactorTable.create(domain10, domain20, domain5);
		newTable.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
		newTable.randomizeWeights(rand);
		
		FactorTablePerformanceTester tester = new FactorTablePerformanceTester(newTable, iterations);

		System.out.println("==== dense 10x20x5 table ==== ");
		
		tester.testGetWeightIndexFromTableIndices();

		tester.testGetWeightForIndices();

		tester.testEvalAsFactorFunction();
		
		newTable.setRepresentation(FactorTableRepresentation.DENSE_ENERGY);
		tester.testGibbsUpdateMessage();
		
		newTable.setRepresentation(FactorTableRepresentation.ALL_WEIGHT_WITH_INDICES);
		tester.testSumProductUpdate();

		System.out.println("\n==== sparse 10x20x5 table ==== ");
		
		// Randomly sparsify the tables
		for (int i = newTable.jointSize() / 2; --i>=0;)
		{
			newTable.setWeightForJointIndex(0.0, rand.nextInt(newTable.jointSize()));
		}
		newTable.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT);

		tester.testEvalAsFactorFunction();
		
		newTable.setRepresentation(FactorTableRepresentation.ALL_WEIGHT);
		
		tester.testGetWeightIndexFromTableIndices();
		tester.testGetWeightForIndices();
		
		newTable.setRepresentation(FactorTableRepresentation.ALL_ENERGY);
		tester.testGibbsUpdateMessage();
		
		newTable.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT_WITH_INDICES);
		tester.testSumProductUpdate();
		
		System.out.println("\n==== deterministic 11x6x6 table ====");
		
		newTable = FactorTable.create(BitSetUtil.bitsetFromIndices(3, 0), twoDice, oneDie, oneDie);
		JointDomainIndexer indexer = newTable.getDomainIndexer();
		int[] deterministicIndices = new int[indexer.getInputCardinality()];
		int[] indices = indexer.allocateIndices(null);
		for (int ii = 0, iiend = indexer.getInputCardinality(); ii < iiend; ++ii)
		{
			indexer.inputIndexToIndices(ii, indices);
			deterministicIndices[ii] = indices[1] + indices[2];
		}
		newTable.setDeterministicOutputIndices(deterministicIndices);
		assertTrue(newTable.isDeterministicDirected());
		
		tester = new FactorTablePerformanceTester(newTable, iterations);
		
		tester.testGetWeightIndexFromTableIndices();

		tester.testGetWeightForIndices();

		tester.testEvalAsFactorFunction();

		newTable.setRepresentation(FactorTableRepresentation.DETERMINISTIC_WITH_INDICES);
		newTable.getWeightsSparseUnsafe();
		tester.testSumProductUpdate();
		
		System.out.println("\n==== DONE ====");
	}
	
	/**
	 * Crude speed test of binary search vs. Colt's IntInt hash table to see at which
	 * point the hash table is a win.
	 * <p>
	 * On my machine, the binary search is not much worse than the hash table until the
	 * size gets above 120-150 elements.
	 */
	@Test
	@Ignore
	public void binarySearchVsHashTable()
	{
		binarySearchVsHashTable(100, 1000);
		
		int iterations = 1000;
		for (int size = 10; size < 200; ++size)
		{
			binarySearchVsHashTable(size, iterations);
		}
	}
	
	private void binarySearchVsHashTable(int size, int iterations)
	{
		int[] array = new int[size];
		for (int i = 0; i < size; ++i)
		{
			array[i] = rand.nextInt();
		}
		
		Stopwatch timer = new Stopwatch();
		
		timer.start();
		for (int i = iterations; --i>=0;)
		{
			for (int j = size; --j>=0;)
			{
				Arrays.binarySearch(array, array[j]);
			}
		}
		timer.stop();
		
		long bsns = timer.elapsed(TimeUnit.NANOSECONDS);
		
		timer.reset();
		timer.start();
		OpenIntIntHashMap map = new OpenIntIntHashMap(size);
		for (int i = 0; i < size; ++i)
		{
			map.put(array[i], i);
		}
		for (int i = iterations; --i>=0;)
		{
			for (int j = size; --j>=0;)
			{
				map.get(array[j]);
			}
		}
		timer.stop();
		
		long hmns = timer.elapsed(TimeUnit.NANOSECONDS);
		
		long calls = (long)size * (long)iterations;
		
		long nsPerBS = bsns / calls;
		long nsPerHM = hmns / calls;
		
		System.out.format("Size %d: BS %d vs HT %d ns/call\n", size, nsPerBS, nsPerHM);
	}
	
	private void testRandomOperations(IFactorTable table, int nOperations)
	{
		final boolean supportsJoint = table.supportsJointIndexing();
		final JointDomainIndexer domains = table.getDomainIndexer();
		int[] indices = new int[table.getDimensions()];
		Object[] arguments = new Object[table.getDimensions()];
		
		while (--nOperations >= 0)
		{
			int operation = rand.nextInt(12);
// For debugging:
//			if (nOperations == badIterationNumber)
//			{
//				Misc.breakpoint();
//			}
//			System.out.format("%d: operation %d\n", nOperations, operation);
			switch (operation)
			{
			case 0:
				// Randomly zero out an entry.
				if (table.hasSparseRepresentation())
				{
					if (table.sparseSize() > 0)
					{
						int si = rand.nextInt(table.sparseSize());
						if (table.hasSparseWeights() || table.getRepresentation().isDeterministic())
						{
							table.setWeightForSparseIndex(0.0, si);
						}
						else
						{
							table.setEnergyForSparseIndex(Double.POSITIVE_INFINITY, si);
						
						}
					}
				}
				else // dense
				{
					int ji = rand.nextInt(table.jointSize());
					if (table.hasDenseWeights())
					{
						table.setWeightForJointIndex(0.0, ji);
					}
					else
					{
						table.setEnergyForJointIndex(Double.POSITIVE_INFINITY, ji);
					}
				}
				break;
				
			case 1:
				// Randomly set the representation
				int nReps = FactorTableRepresentation.values().length;
				FactorTableRepresentation oldRep = table.getRepresentation();
				
				FactorTableRepresentation newRep = FactorTableRepresentation.values()[rand.nextInt(nReps)];
				try
				{
					table.setRepresentation(newRep);
					FactorTableRepresentation actualNewRep = table.getRepresentation();
					assertEquals(newRep, actualNewRep);
				}
				catch (DimpleException ex)
				{
					assertEquals(oldRep, table.getRepresentation());
					if (table.supportsJointIndexing())
					{
						assertTrue(newRep.isDeterministic());
					}
					else
					{
						assertTrue(newRep.isDeterministic() || newRep.hasDense());
					}
				}
				break;
				
			case 3:
				// Normalize
				try
				{
					table.normalize();
					assertTrue(table.isNormalized());
				}
				catch (UnsupportedOperationException ex)
				{
					assertTrue(table.isDirected());
					assertFalse(table.isNormalized());
					assertThat(ex.getMessage(), containsString("not supported for directed factor table"));
				}
				catch (DimpleException ex)
				{
					assertThat(ex.getMessage(), containsString("Cannot normalize undirected factor table with zero"));
					assertFalse(table.isNormalized());
				}
				try
				{
					table.normalizeConditional();
					assertTrue(table.isConditional());
				}
				catch (UnsupportedOperationException ex)
				{
					assertFalse(table.isConditional());
					assertFalse(table.isDirected());
				}
				catch (DimpleException ex)
				{
					assertThat(ex.getMessage(), containsString("Cannot normalize directed factor table with zero"));
					assertFalse(table.isConditional());
				}
				break;
				
			case 4:
				// Compact
				int expectedCompacted =
					table.hasSparseRepresentation() ? table.sparseSize() - table.countNonZeroWeights() : 0;
				int actualCompacted = table.compact();
				assertEquals(expectedCompacted, actualCompacted);
				assertEquals(0, table.compact());
				break;
				
			case 5:
			{
				// Test get*Slice methods
				// Randomly select a set of indices to condition on.
				JointDomainIndexer indexer = table.getDomainIndexer();
				if (table.hasSparseRepresentation())
				{
					if (table.sparseSize() == 0)
					{
						continue;
					}
					table.sparseIndexToIndices(rand.nextInt(table.sparseSize()), indices);
				}
				else
				{
					indexer.jointIndexToIndices(rand.nextInt(table.jointSize()), indices);
				}
				for (int i = 0; i < indices.length; ++i)
				{
					final int domainSize = indexer.getDomainSize(i);
					if (domainSize > 100000)
					{
						// Skip test for large domain sizes to avoid taxing heap.
						continue;
					}
					int saved = indices[i];
					
					double[] slice1 = table.getEnergySlice(i, indices);
					for (int j = 0; j < domainSize; ++j)
					{
						indices[i] = j;
						assertEquals(slice1[j], table.getEnergyForIndices(indices), 0.0);
					}
					double[] slice2 = table.getWeightSlice(slice1, i, indices);
					assertSame(slice1, slice2);
					for (int j = 0; j < domainSize; ++j)
					{
						indices[i] = j;
						assertEquals(slice2[j], table.getWeightForIndices(indices), 0.0);
					}
					slice2 = table.getWeightSlice(i, indices);
					assertNotSame(slice1, slice2);
					assertArrayEquals(slice1, slice2, 0.0);
					slice2 = table.getWeightSlice(ArrayUtil.EMPTY_DOUBLE_ARRAY, i, indices);
					assertArrayEquals(slice1, slice2, 0.0);
					slice1 = table.getEnergySlice(ArrayUtil.EMPTY_DOUBLE_ARRAY, i, indices);
					for (int j = 0; j < domainSize; ++j)
					{
						indices[i] = j;
						assertEquals(slice1[j], table.getEnergyForIndices(indices), 0.0);
					}
					assertSame(slice1, table.getEnergySlice(slice1, i, indices));
					
					indices[i] = saved;
				}
				break;
			}
			
			case 6:
				// Replace sparse values
			{
				if (table.hasSparseRepresentation())
				{
					int [][] oldIndices = table.getIndicesSparseUnsafe();
					
					int newSize = oldIndices.length;
					int[][] newIndices = null;
					
					if (oldIndices.length > 1)
					{
						// Remove one at random: FIXME what if empty?
						--newSize;
						int i = rand.nextInt(oldIndices.length);
						newIndices = Arrays.copyOf(oldIndices, newSize);
						System.arraycopy(oldIndices, i + 1, newIndices, i, newIndices.length - i);
					}
					else
					{
						++newSize;
						// Add one at random
						do
						{
							domains.randomIndices(rand, indices);
						} while (table.sparseIndexFromIndices(indices) >= 0);
						newIndices = Arrays.copyOf(oldIndices, newSize);
						newIndices[newSize - 1] = indices.clone();
					}

					// Sort using different comparator than one used by table to test that method
					// handles out-of-order data.
					Arrays.sort(newIndices, Comparators.lexicalIntArray());
					
					double[] newValues = new double[newSize];
					for (int i = 0; i < newSize; ++i)
					{
						newValues[i] = rand.nextDouble();
					}

					FactorTableRepresentation expectedRep = rand.nextBoolean() ?
						FactorTableRepresentation.SPARSE_ENERGY : FactorTableRepresentation.SPARSE_WEIGHT;
					
					if (expectedRep == FactorTableRepresentation.SPARSE_WEIGHT)
					{
						table.setWeightsSparse(newIndices, newValues);
					}
					else
					{
						table.setEnergiesSparse(newIndices, newValues);
					}
					
					assertEquals(newSize, table.sparseSize());
					assertEquals(expectedRep, table.getRepresentation());
					for (int i = 0; i < newSize; ++i)
					{
						if (expectedRep == FactorTableRepresentation.SPARSE_WEIGHT)
						{
							assertEquals(newValues[i], table.getWeightForIndices(newIndices[i]), 1e-12);
						}
						else
						{
							assertEquals(newValues[i], table.getEnergyForIndices(newIndices[i]), 1e-12);
						}
					}
				}
				break;
			}
				
			default:
				// Random assignments
				double weight = rand.nextDouble();
				int jointIndex = 0, location = 0;
				if (supportsJoint)
				{
					jointIndex = domains.randomJointIndex(rand);
					table.setWeightForJointIndex(weight, jointIndex);
					assertWeight(table, weight, jointIndex);
				}
				else
				{
					domains.randomIndices(rand, indices);
					table.setWeightForIndices(weight, indices);
					assertWeight(table, weight, indices);
				}
				
				assertFalse(table.isNormalized());
				
				weight = rand.nextDouble();
				if (supportsJoint)
				{
					table.setEnergyForJointIndex(-Math.log(weight), jointIndex);
					assertWeight(table, weight, jointIndex);
				}
				else
				{
					table.setEnergyForIndices(-Math.log(weight), indices);
					assertWeight(table, weight, indices);
				}
				
				weight = rand.nextDouble();
				if (supportsJoint)
				{
					jointIndex = domains.randomJointIndex(rand);
					location = table.sparseIndexFromJointIndex(jointIndex);
				}
				else
				{
					domains.randomIndices(rand, indices);
					location = table.sparseIndexFromIndices(indices);
				}
				if (location >= 0)
				{
					int si = table.sparseIndexFromJointIndex(jointIndex);
					table.setWeightForSparseIndex(weight, si);
					assertWeight(table, weight, jointIndex);
					
					weight = weight + 1;
					table.setWeightForSparseIndex(weight, si);
					assertWeight(table, weight, jointIndex);
				}
				
				weight = rand.nextDouble();
				if (supportsJoint)
				{
					jointIndex = domains.randomJointIndex(rand);
					location = table.sparseIndexFromJointIndex(jointIndex);
				}
				else
				{
					domains.randomIndices(rand, indices);
					location = table.sparseIndexFromIndices(indices);
				}
				if (location >= 0)
				{
					table.setEnergyForSparseIndex(-Math.log(weight), location);
					if (supportsJoint)
						assertWeight(table, weight, jointIndex);
					else
						assertWeight(table, weight, indices);
				}
				
				weight = rand.nextDouble();
				if (supportsJoint)
				{
					jointIndex = domains.randomJointIndex(rand);
					domains.jointIndexToIndices(jointIndex, indices);
				}
				else
				{
					domains.randomIndices(rand, indices);
				}
				table.setWeightForIndices(weight, indices);
				if (supportsJoint)
					assertWeight(table, weight, jointIndex);
				else
					assertWeight(table, weight, indices);

				weight = weight + 1;
				table.setWeightForIndices(weight, indices);
				if (supportsJoint)
					assertWeight(table, weight, jointIndex);
				else
					assertWeight(table, weight, indices);
				
				weight = rand.nextDouble();
				if (supportsJoint)
				{
					jointIndex = domains.randomJointIndex(rand);
					domains.jointIndexToIndices(jointIndex, indices);
				}
				else
				{
					domains.randomIndices(rand, indices);
				}
				table.setEnergyForIndices(-Math.log(weight), indices);
				if (supportsJoint)
					assertWeight(table, weight, jointIndex);
				else
					assertWeight(table, weight, indices);

				weight = rand.nextDouble();
				if (supportsJoint)
				{
					jointIndex = domains.randomJointIndex(rand);
					domains.jointIndexToElements(jointIndex, arguments);
				}
				else
				{
					domains.randomIndices(rand, indices);
					domains.elementsFromIndices(indices, arguments);
				}
				table.setWeightForElements(weight, arguments);
				if (supportsJoint)
					assertWeight(table, weight, jointIndex);
				else
					assertWeight(table, weight, indices);
				
				weight = rand.nextDouble();
				if (supportsJoint)
				{
					jointIndex = domains.randomJointIndex(rand);
					domains.jointIndexToElements(jointIndex, arguments);
				}
				else
				{
					domains.randomIndices(rand, indices);
					domains.elementsFromIndices(indices, arguments);
				}
				table.setEnergyForElements(-Math.log(weight), arguments);
				if (supportsJoint)
					assertWeight(table, weight, jointIndex);
				else
					assertWeight(table, weight, indices);
			}
			
			assertInvariants(table);
		}
		
	}
	
	private static void assertWeight(IFactorTable table, double weight, int jointIndex)
	{
		double energy = -Math.log(weight);
		assertEquals(energy, table.getEnergyForJointIndex(jointIndex), 1e-12);
		assertEquals(weight, table.getWeightForJointIndex(jointIndex), 1e-12);
		
		int sparseIndex = table.sparseIndexFromJointIndex(jointIndex);
		if (sparseIndex >= 0)
		{
			assertEquals(energy, table.getEnergyForSparseIndex(sparseIndex), 1e-12);
			assertEquals(weight, table.getWeightForSparseIndex(sparseIndex), 1e-12);
		}

		int[] indices = table.getDomainIndexer().jointIndexToIndices(jointIndex, null);
		assertEquals(energy, table.getEnergyForIndices(indices), 1e-12);
		assertEquals(weight, table.getWeightForIndices(indices), 13-12);
		
		Object[] arguments = table.getDomainIndexer().jointIndexToElements(jointIndex, null);
		assertEquals(energy, table.getEnergyForElements(arguments), 1e-12);
		assertEquals(weight, table.getWeightForElements(arguments), 13-12);
	}
	
	private static void assertWeight(IFactorTable table, double weight, int[] indices)
	{
		double energy = -Math.log(weight);
		assertEquals(energy, table.getEnergyForIndices(indices), 1e-12);
		assertEquals(weight, table.getWeightForIndices(indices), 1e-12);
		
		int sparseIndex = table.sparseIndexFromIndices(indices);
		if (sparseIndex >= 0)
		{
			assertEquals(energy, table.getEnergyForSparseIndex(sparseIndex), 1e-12);
			assertEquals(weight, table.getWeightForSparseIndex(sparseIndex), 1e-12);
		}

		Object[] elements = table.getDomainIndexer().elementsFromIndices(indices);
		assertEquals(energy, table.getEnergyForElements(elements), 1e-12);
		assertEquals(weight, table.getWeightForElements(elements), 13-12);
	}

	public static void assertInvariants(IFactorTable table)
	{
		FactorTableRepresentation representation = table.getRepresentation();
		assertBaseInvariants(table);
		table.setRepresentation(representation);
		
		assertEquals(representation, table.getRepresentation());
		
		assertEquals(representation.isDeterministic(), table.hasDeterministicRepresentation());
		
		// Ok to setDirected if it doesn't change anything.
		BitSet outputSet = table.getDomainIndexer().getOutputSet();
		table.setDirected(outputSet);
		assertEquals(outputSet, table.getDomainIndexer().getOutputSet());
		
		table.copy(table); // shouldn't do anything

		if (table.hasSparseEnergies())
		{
			double[] sparseEnergies = table.getEnergiesSparseUnsafe();
			for (int si = table.sparseSize(); --si>=0;)
			{
				assertEquals(table.getEnergyForSparseIndex(si), sparseEnergies[si], 0.0);
			}
		}
		if (table.hasSparseWeights())
		{
			double[] sparseWeights = table.getWeightsSparseUnsafe();
			for (int si = table.sparseSize(); --si>=0;)
			{
				assertEquals(table.getWeightForSparseIndex(si), sparseWeights[si], 0.0);
			}
		}
		
		JointDomainReindexer nullConverter =
			JointDomainReindexer.createPermuter(table.getDomainIndexer(), table.getDomainIndexer());

		IFactorTable table2 = table.convert(nullConverter);
		assertBaseEqual(table, table2);
	}
	
	
	public static void assertBaseInvariants(IFactorTableBase table)
	{
		int nDomains = table.getDimensions();
		assertTrue(nDomains >= 0);
		
		JointDomainIndexer domains = table.getDomainIndexer();
		assertEquals(nDomains, domains.size());
		
		final boolean supportsJoint = table.supportsJointIndexing();
		assertEquals(table.getDomainIndexer().supportsJointIndexing(), supportsJoint);
		
		int expectedJointSize = 1;
		int[] domainSizes = new int[nDomains];
		for (int i = 0; i < nDomains; ++i)
		{
			int domainSize = domains.getDomainSize(i);
			assertTrue(domainSize > 0);
			if (supportsJoint)
			{
				expectedJointSize *= domainSize;
			}
			domainSizes[i] = domainSize;
		}
		
		BitSet fromSet = table.getInputSet();
		if (table.isDirected())
		{
			requireNonNull(fromSet);
			assertTrue(fromSet.cardinality() > 0);
		}
		else
		{
			assertNull(fromSet);
		}
		
		int size = table.sparseSize();
		assertTrue(size >= 0);

		int jointSize = -1;
		if (supportsJoint)
		{
			jointSize = table.jointSize();
			assertTrue(size <= jointSize);
			assertEquals(expectedJointSize, jointSize);
		}
		
		Object[] arguments = new Object[nDomains];
		int[] indices = new int[nDomains];
		
		assertEquals(table.hasDenseRepresentation(), table.hasDenseWeights() || table.hasDenseEnergies());
		assertEquals(table.hasSparseRepresentation(),
			table.hasSparseWeights() || table.hasSparseEnergies() || table.isDeterministicDirected());
		
		// Test iteration
		{
			int i = 0;
			for (FactorTableEntry entry : table)
			{
				final int si = entry.sparseIndex();
				final int ji = supportsJoint ? entry.jointIndex() : -1;

				assertSame(domains, entry.domains());
				assertNotEquals(0.0, entry.weight(), 0.0);
				assertFalse(Double.isInfinite(entry.energy()));
				assertEquals(entry.energy(), -Math.log(entry.weight()), 1e-12);
				if (supportsJoint)
				{
					assertEquals(entry.weight(), table.getWeightForJointIndex(ji), 1e-12);
				}
				
				if (table.hasSparseRepresentation())
				{
					assertTrue(si >= 0);
					assertTrue(si < table.sparseSize());
					assertTrue(i <= si);
					assertEquals(table.getEnergyForSparseIndex(si), entry.energy(), 0.0);
					if (supportsJoint)
					{
						assertEquals(table.sparseIndexToJointIndex(si), ji);
						assertEquals(si, table.sparseIndexFromJointIndex(ji));
					}
					assertArrayEquals(table.sparseIndexToIndices(si, null), entry.indices());
					assertArrayEquals(table.sparseIndexToElements(si, null), entry.values());
				}
				else
				{
					assertTrue(si < 0);
				}
				++i;
			}
			assertEquals(table.countNonZeroWeights(), i);

			double totalWeight = 0.0;
			int nonZeroCount = 0;
			
			IFactorTableIterator iter = null;
			if (supportsJoint)
			{
				i = 0;
				iter = table.fullIterator();
				assertFalse(iter.skipsZeroWeights());
				while (iter.hasNext())
				{
					FactorTableEntry entry = iter.next();
					requireNonNull(entry);
					assertEquals(i, entry.jointIndex());
					assertArrayEquals(entry.indices(), iter.indicesUnsafe());
					if (entry.weight() != 0.0)
					{
						totalWeight += entry.weight();
						++nonZeroCount;
					}
					if (table.hasDenseEnergies())
					{
						assertEquals(entry.energy(), table.getEnergyForIndicesDense(entry.indices()), 1e-12);
					}
					if (table.hasDenseWeights())
					{
						assertEquals(entry.weight(), table.getWeightForIndicesDense(entry.indices()), 1e-12);
					}
					++i;
				}
				assertEquals(table.jointSize(), i);
				assertEquals(nonZeroCount, table.countNonZeroWeights());
				assertEquals((double)nonZeroCount/(double)i, table.density(), 1e-12);
				if (table.isNormalized())
				{
					assertFalse(table.isDirected());
					if (totalWeight != 0.0)
					{
						assertEquals(1.0, totalWeight, 1e-12);
					}
				}
			}
			
			if (table.isConditional())
			{
				assertTrue(table.isDirected());
				if (table.supportsJointIndexing())
				{
					double totalWeightForPrevInput = 0.0;
					for (int ii = 0, isize = domains.getInputCardinality(); ii < isize; ++ii)
					{
						double totalWeightForInput = 0.0;
						for (int oi = 0, osize = domains.getOutputCardinality(); oi < osize; ++oi)
						{
							int ji = domains.jointIndexFromInputOutputIndices(ii, oi);
							totalWeightForInput += table.getWeightForJointIndex(ji);
						}
						if (ii > 0)
						{
							assertEquals(totalWeightForPrevInput, totalWeightForInput, 1e-12);
						}
						totalWeightForPrevInput = totalWeightForInput;
					}
				}
				else
				{
					// FIXME - test invariants for conditional SparseFactorTable
				}
			}
			
			if (supportsJoint)
			{
				assertNull(requireNonNull(iter).next());

				i = 0;
				iter = table.fullIterator();
				while (iter.advance())
				{
					assertEquals(i, iter.jointIndex());
					if (table.hasSparseRepresentation())
					{
						int si = table.sparseIndexFromJointIndex(i);
						if (si < 0) si = -1-si;
						assertEquals(si, iter.sparseIndex());
					}
					else
					{
						assertEquals(-1, iter.sparseIndex());
					}
					assertEquals(table.getWeightForJointIndex(i), iter.weight(), 1e-12);
					assertEquals(table.getEnergyForJointIndex(i), iter.energy(), 1e-12);
					++i;
				}
				assertFalse(iter.advance());
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
		}
		
		if (table.hasSparseRepresentation())
		{
			IFactorTableIterator iter = table.iterator();
			assertTrue(iter.skipsZeroWeights());
			
			for (int si = 0; si < size; ++si)
			{
				table.sparseIndexToIndices(si, indices);
				for (int j = 0; j < nDomains; ++j)
				{
					assertTrue(indices[j] >= 0);
					assertTrue(indices[j] < domainSizes[j]);
				}
				assertEquals(si, table.sparseIndexFromIndices(indices));

				table.sparseIndexToElements(si, arguments);
				for (int j = 0; j < nDomains; ++j)
				{
					assertEquals(arguments[j], domains.get(j).getElement(indices[j]));
				}
				assertEquals(si, table.sparseIndexFromElements(arguments));

				if (supportsJoint)
				{
					int joint = table.sparseIndexToJointIndex(si);
					assertTrue(joint >= 0);
					assertTrue(joint < jointSize);
					assertEquals(si, table.sparseIndexFromJointIndex(joint));
				}

				double energy = table.getEnergyForSparseIndex(si);
				table.setEnergyForSparseIndex(energy, si);
				assertEquals(energy, table.getEnergyForIndices(indices), 1e-12);
				assertEquals(energy, table.getEnergyForElements(arguments), 1e-12);

				double weight = table.getWeightForSparseIndex(si);
				table.setWeightForSparseIndex(weight, si);
				assertEquals(weight, table.getWeightForIndices(indices), 1e-12);
				assertEquals(weight, table.getWeightForElements(arguments), 1e-12);

				assertEquals(energy, -Math.log(weight), 1e-12);
				
				if (weight != 0.0)
				{
					assertTrue(iter.hasNext());
					FactorTableEntry entry = iter.next();
					requireNonNull(entry);
					assertEquals(si, entry.sparseIndex());
					assertEquals(energy, entry.energy(), 1e-12);
					assertEquals(weight, entry.weight(), 1e-12);
					assertArrayEquals(iter.indicesUnsafe(), entry.indices());
				}
			}
		}
		
		if (table.isDeterministicDirected())
		{
			assertTrue(table.isDirected());
			
			for (int inputIndex = 0, end = domains.getInputCardinality(); inputIndex < end; ++inputIndex)
			{
				Arrays.fill(arguments, null);
				domains.inputIndexToElements(inputIndex, arguments);
				table.evalDeterministic(arguments);
				assertEquals(1.0, table.getWeightForElements(arguments), 0.0);
				assertEquals(0.0, table.getEnergyForElements(arguments), 0.0);
				assertEquals(1.0, table.getWeightForSparseIndex(inputIndex), 0.0);
				assertEquals(0.0, table.getEnergyForSparseIndex(inputIndex), 0.0);
				assertEquals(inputIndex, table.sparseIndexFromElements(arguments));
			}
		}
		else
		{
			try
			{
				table.evalDeterministic(arguments);
				fail("expected exception");
			}
			catch (DimpleException ex)
			{
			}
		}
		
		IFactorTableBase table2 = table.clone();
		assertBaseEqual(table, table2);
		
		IFactorTableBase table3 = SerializationTester.clone(table);
		assertBaseEqual(table, table3);
	}
	
	public static void assertEqual(IFactorTable table1, IFactorTable table2)
	{
		assertEqualImpl(table1, table2, true);
	}
	
	private static void assertEqualImpl(IFactorTable table1, IFactorTable table2, boolean checkBaseEqual)
	{
		if (checkBaseEqual)
		{
			assertBaseEqual(table1, table2);
		}
		assertEquals(table1.sparseSize(), table2.sparseSize());
		assertEquals(table1.getDimensions(), table2.getDimensions());
		assertEquals(table1.getRepresentation(), table2.getRepresentation());
		if (table1.hasSparseIndices())
		{
			int[][] indices1 = table1.getIndicesSparseUnsafe();
			int[][] indices2 = table2.getIndicesSparseUnsafe();
			assertEquals(indices1.length, indices2.length);
			for (int i = indices1.length; --i>=0;)
			{
				assertArrayEquals(indices1[i], indices2[i]);
			}
		}
	}
	
	public static void assertBaseEqual(IFactorTableBase table1, IFactorTableBase table2)
	{
		final boolean bothSupportJoint = table1.supportsJointIndexing() && table2.supportsJointIndexing();
		
		assertEquals(table1.getClass(), table2.getClass());

		assertEquals(table1.getInputSet(), table2.getInputSet());
		
		assertEquals(table1.getDimensions(), table2.getDimensions());
		int nDomains = table1.getDimensions();
		for (int i = 0; i < nDomains; ++i)
		{
			assertEquals(table1.getDomainIndexer().getDomainSize(i), table2.getDomainIndexer().getDomainSize(i));
			assertEquals(table1.getDomainIndexer().get(i), table2.getDomainIndexer().get(i));
		}
		
		assertEquals(table1.isDirected(), table2.isDirected());
		assertEquals(table1.isNormalized(), table2.isNormalized());
		assertEquals(table1.isDeterministicDirected(), table2.isDeterministicDirected());
		
		assertEquals(table1.sparseSize(), table2.sparseSize());
		if (table1.hasSparseRepresentation())
		{
			final int size = table1.sparseSize();
			for (int i = 0; i < size; ++i)
			{
				assertEquals(size, table1.sparseSize());
				assertEquals(table1.getWeightForSparseIndex(i), table2.getWeightForSparseIndex(i), 1e-12);
				assertEquals(table1.getEnergyForSparseIndex(i), table2.getEnergyForSparseIndex(i), 1e-12);
				if (bothSupportJoint)
				{
					assertEquals(table1.sparseIndexToJointIndex(i), table2.sparseIndexToJointIndex(i));
				}
			}
		}
		
		if (bothSupportJoint)
		{
			assertEquals(table1.jointSize(), table2.jointSize());
			final int jointSize = table1.jointSize();
			for (int ji = 0; ji < jointSize; ++ji)
			{
				assertEquals(table1.getWeightForJointIndex(ji), table2.getWeightForJointIndex(ji), 1e-12);
				assertEquals(table1.getEnergyForJointIndex(ji), table2.getEnergyForJointIndex(ji), 1e-12);
			}
		}
		assertEquals(table1.countNonZeroWeights(), table2.countNonZeroWeights());
		assertEquals(table1.density(), table2.density(), 1e-12);
		
		if (table1 instanceof IFactorTable)
		{
			assertEqualImpl((IFactorTable)table1, (IFactorTable)table2, false);
		}
	}
	
	private static void expectNotDense(IFactorTable table, String methodName, Object ... args)
	{
		expectThrow(DimpleException.class, ".*" + methodName + ".*dense representation not supported.",
			table, methodName, args);
	}
	

}
