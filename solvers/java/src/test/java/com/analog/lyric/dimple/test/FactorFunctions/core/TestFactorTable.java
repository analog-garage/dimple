package com.analog.lyric.dimple.test.FactorFunctions.core;

import static com.analog.lyric.math.Utilities.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import cern.colt.map.OpenIntIntHashMap;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableEntry;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableIterator;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTableBase;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.util.test.SerializationTester;
import com.google.common.base.Stopwatch;

public class TestFactorTable
{
	final Random rand = new Random(42);
	final DiscreteDomain domain2 = DiscreteDomain.range(0,1);
	final DiscreteDomain domain3 = DiscreteDomain.range(0,2);
	final DiscreteDomain domain5 = DiscreteDomain.range(0,5);
	
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
			xor2.setConditional(xor2.getDomainIndexer().getOutputSet());
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
		xor2.makeConditional(xor2.getDomainIndexer().getOutputSet());
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
		
		IFactorTable t2x3x4 = FactorTable.create(domain2, domain3, domain5);
		t2x3.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
		t2x3x4.randomizeWeights(rand);
		assertInvariants(t2x3x4);
		testRandomOperations(t2x3x4, 10000);
		
		try
		{
			xor2.setConditional(null);
			fail("expected exception");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("requires non-null argument"));
		}
		try
		{
			xor2.makeConditional(null);
			fail("expected exception");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("requires non-null argument"));
		}
	}
	
	@Test
	public void performanceComparison()
	{
		int iterations = 10000;
		
		Random rand = new Random(13);
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
		Random rand = new Random(123);
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
		int[] indices = new int[table.getDimensions()];
		Object[] arguments = new Object[table.getDimensions()];
		
		while (--nOperations >= 0)
		{
			int operation = rand.nextInt(11);
// For debugging:
//			if (nOperations == 9863)
//			{
//				Misc.breakpoint();
//			}
//			System.out.format("operation %d\n", operation);
			switch (operation)
			{
			case 0:
				// Randomly zero out an entry.
				if (table.hasSparseRepresentation())
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
					assertTrue(newRep.isDeterministic());
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
				indexer.jointIndexToIndices(rand.nextInt(table.jointSize()), indices);
				for (int i = 0; i < indices.length; ++i)
				{
					final int domainSize = indexer.getDomainSize(i);
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
				
			default:
				// Random assignments
				int jointIndex = rand.nextInt(table.jointSize());
				double weight = rand.nextDouble();
				
				table.setWeightForJointIndex(weight, jointIndex);
				assertWeight(table, weight, jointIndex);
				assertFalse(table.isNormalized());
				
				weight = rand.nextDouble();
				table.setEnergyForJointIndex(-Math.log(weight), jointIndex);
				assertWeight(table, weight, jointIndex);
				
				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				int location = table.sparseIndexFromJointIndex(jointIndex);
				if (location >= 0)
				{
					int si = table.sparseIndexFromJointIndex(jointIndex);
					table.setWeightForSparseIndex(weight, si);
					assertWeight(table, weight, jointIndex);
					
					weight = weight + 1;
					table.setWeightForSparseIndex(weight, si);
					assertWeight(table, weight, jointIndex);
				}
				
				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				location = table.sparseIndexFromJointIndex(jointIndex);
				if (location >= 0)
				{
					table.setEnergyForSparseIndex(-Math.log(weight), location);
					assertWeight(table, weight, jointIndex);
				}
				
				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.getDomainIndexer().jointIndexToIndices(jointIndex, indices);
				table.setWeightForIndices(weight, indices);
				assertWeight(table, weight, jointIndex);

				weight = weight + 1;
				table.setWeightForIndices(weight, indices);
				assertWeight(table, weight, jointIndex);
				
				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.getDomainIndexer().jointIndexToIndices(jointIndex, indices);
				table.setEnergyForIndices(-Math.log(weight), indices);
				assertWeight(table, weight, jointIndex);

				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.getDomainIndexer().jointIndexToElements(jointIndex, arguments);
				table.setWeightForElements(weight, arguments);
				assertWeight(table, weight, jointIndex);
				
				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.getDomainIndexer().jointIndexToElements(jointIndex, arguments);
				table.setEnergyForElements(-Math.log(weight), arguments);
				assertWeight(table, weight, jointIndex);
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
	
	public static void assertInvariants(IFactorTable table)
	{
		FactorTableRepresentation representation = table.getRepresentation();
		assertBaseInvariants(table);
		table.setRepresentation(representation);
		
		assertEquals(representation, table.getRepresentation());
		
		// Ok to setDirected if it doesn't change anything.
		BitSet outputSet = table.getDomainIndexer().getOutputSet();
		table.setDirected(outputSet);
		assertEquals(outputSet, table.getDomainIndexer().getOutputSet());
		
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
		
		int expectedJointSize = 1;
		int[] domainSizes = new int[nDomains];
		for (int i = 0; i < nDomains; ++i)
		{
			int domainSize = domains.getDomainSize(i);
			assertTrue(domainSize > 0);
			expectedJointSize *= domainSize;
			domainSizes[i] = domainSize;
		}
		
		BitSet fromSet = table.getInputSet();
		if (table.isDirected())
		{
			assertNotNull(fromSet != null);
			assertTrue(fromSet.cardinality() > 0);
		}
		else
		{
			assertNull(fromSet);
		}
		
		int size = table.sparseSize();
		assertTrue(size >= 0);
		int jointSize = table.jointSize();
		assertTrue(size <= jointSize);
		assertEquals(expectedJointSize, jointSize);
		
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
				final int ji = entry.jointIndex();

				assertSame(domains, entry.domains());
				assertNotEquals(0.0, entry.weight(), 0.0);
				assertFalse(Double.isInfinite(entry.energy()));
				assertEquals(entry.energy(), -Math.log(entry.weight()), 1e-12);
				assertEquals(entry.weight(), table.getWeightForJointIndex(ji), 1e-12);
				
				if (table.hasSparseRepresentation())
				{
					assertTrue(si >= 0);
					assertTrue(si < table.sparseSize());
					assertTrue(i <= si);
					assertEquals(table.getEnergyForSparseIndex(si), entry.energy(), 0.0);
					assertEquals(table.sparseIndexToJointIndex(si), ji);
					assertEquals(si, table.sparseIndexFromJointIndex(ji));
					assertArrayEquals(table.sparseIndexToIndices(si, null), entry.indices());
					assertArrayEquals(table.sparseIndexToElements(si, null), entry.values());
				}
				else
				{
					assertTrue(si < 0);
				}
				++i;
			}

			double totalWeight = 0.0;
			int nonZeroCount = 0;
			
			i = 0;
			FactorTableIterator iter = table.fullIterator();
			while (iter.hasNext())
			{
				FactorTableEntry entry = iter.next();
				assertEquals(i, entry.jointIndex());
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
			if (table.isConditional())
			{
				assertTrue(table.isDirected());
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
			
			assertNull(iter.next());

			i = 0;
			iter = table.fullIterator();
			while (iter.advance())
			{
				assertFalse(iter.done());
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
			assertTrue(iter.done());
			
			try
			{
				iter.remove();
				fail("should not get here");
			}
			catch (DimpleException ex)
			{
			}
		}
		
		if (table.hasSparseRepresentation())
		{
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

				int joint = table.sparseIndexToJointIndex(si);
				assertTrue(joint >= 0);
				assertTrue(joint < jointSize);
				assertEquals(si, table.sparseIndexFromJointIndex(joint));

				double energy = table.getEnergyForSparseIndex(si);
				table.setEnergyForSparseIndex(energy, si);
				assertEquals(energy, table.getEnergyForIndices(indices), 1e-12);
				assertEquals(energy, table.getEnergyForElements(arguments), 1e-12);

				double weight = table.getWeightForSparseIndex(si);
				table.setWeightForSparseIndex(weight, si);
				assertEquals(weight, table.getWeightForIndices(indices), 1e-12);
				assertEquals(weight, table.getWeightForElements(arguments), 1e-12);

				assertEquals(energy, -Math.log(weight), 1e-12);
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
		assertEquals(table1.getClass(), table2.getClass());

		assertEquals(table1.isDirected(), table2.isDirected());
		assertEquals(table1.isDeterministicDirected(), table2.isDeterministicDirected());
		
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
				assertEquals(table1.sparseIndexToJointIndex(i), table2.sparseIndexToJointIndex(i));
			}
		}
		
		assertEquals(table1.jointSize(), table2.jointSize());
		final int jointSize = table1.jointSize();
		for (int ji = 0; ji < jointSize; ++ji)
		{
			assertEquals(table1.getWeightForJointIndex(ji), table2.getWeightForJointIndex(ji), 1e-12);
			assertEquals(table1.getEnergyForJointIndex(ji), table2.getEnergyForJointIndex(ji), 1e-12);
		}
		
		assertEquals(table1.countNonZeroWeights(), table2.countNonZeroWeights());
		assertEquals(table1.density(), table2.density(), 1e-12);
		
		if (table1 instanceof IFactorTable)
		{
			assertEqualImpl((IFactorTable)table1, (IFactorTable)table2, false);
		}
	}
}
