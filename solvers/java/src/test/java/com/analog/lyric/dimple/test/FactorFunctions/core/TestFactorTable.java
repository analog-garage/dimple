package com.analog.lyric.dimple.test.FactorFunctions.core;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import cern.colt.map.OpenIntIntHashMap;

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.INewFactorTableBase;
import com.analog.lyric.dimple.factorfunctions.core.NewFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.NewFactorTableEntry;
import com.analog.lyric.dimple.factorfunctions.core.NewFactorTableIterator;
import com.analog.lyric.dimple.factorfunctions.core.NewFactorTableRepresentation;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteDomainList;
import com.google.common.base.Stopwatch;

public class TestFactorTable
{
	final Random rand = new Random(42);
	final DiscreteDomain domain2 = DiscreteDomain.range(0,1);
	final DiscreteDomain domain3 = DiscreteDomain.range(0,2);
	final DiscreteDomain domain5 = DiscreteDomain.range(0,5);
	
	@Test
	public void testNewFactorTable()
	{
		NewFactorTable t2x3 = new NewFactorTable(domain2, domain3);
		assertEquals(2, t2x3.getDimensions());
		assertEquals(domain2, t2x3.getDomainList().get(0));
		assertEquals(domain3, t2x3.getDomainList().get(1));
		assertFalse(t2x3.getRepresentation().hasDense());
		assertFalse(t2x3.isDirected());
		assertEquals(NewFactorTableRepresentation.SPARSE_ENERGY, t2x3.getRepresentation());
		assertInvariants(t2x3);
		
		t2x3.setRepresentation(NewFactorTableRepresentation.DENSE_ENERGY);
		assertEquals(NewFactorTableRepresentation.DENSE_ENERGY, t2x3.getRepresentation());
		assertTrue(t2x3.getRepresentation().hasDense());
		assertInvariants(t2x3);
		
		t2x3.setRepresentation(NewFactorTableRepresentation.SPARSE_ENERGY);
		assertFalse(t2x3.getRepresentation().hasDense());
		assertEquals(0, t2x3.sparseSize());
		assertInvariants(t2x3);
		
		t2x3.setRepresentation(NewFactorTableRepresentation.ALL_ENERGY);
		t2x3.randomizeWeights(rand);
		assertInvariants(t2x3);
		
		assertEquals(NewFactorTableRepresentation.ALL_ENERGY, t2x3.getRepresentation());
		t2x3.setRepresentation(NewFactorTableRepresentation.ALL);
		assertEquals(NewFactorTableRepresentation.ALL, t2x3.getRepresentation());
		assertInvariants(t2x3);
		
		t2x3.normalize();
		assertInvariants(t2x3);
		
		t2x3.setRepresentation(NewFactorTableRepresentation.SPARSE_WEIGHT);
		assertEquals(NewFactorTableRepresentation.SPARSE_WEIGHT, t2x3.getRepresentation());
		assertInvariants(t2x3);
		
		BitSet xor3Input = new BitSet(3);
		xor3Input.set(0);
		xor3Input.set(2);
		NewFactorTable xor2 = new NewFactorTable(xor3Input, domain2, domain2, domain2);
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
		assertInvariants(xor2);
		testRandomOperations(xor2, 10000);
		
		NewFactorTable t2x2x2 = xor2.clone();
		assertBaseEqual(t2x2x2, xor2);
		t2x2x2.setWeightForIndices(.5, 1, 1, 1);
		assertFalse(t2x2x2.isDeterministicDirected());
		assertInvariants(t2x2x2);
		
		NewFactorTable t2x3x4 = new NewFactorTable(domain2, domain3, domain5);
		t2x3.setRepresentation(NewFactorTableRepresentation.DENSE_WEIGHT);
		t2x3x4.randomizeWeights(rand);
		assertInvariants(t2x3x4);
		testRandomOperations(t2x3x4, 10000);
	}
	
	@Test
	public void performanceComparison()
	{
		int iterations = 100000;
		
		FactorTable.useNewFactorTable = false;
		
		Random rand = new Random(13);
		DiscreteDomain domain10 = DiscreteDomain.intRangeFromSize(10);
		DiscreteDomain domain20 = DiscreteDomain.intRangeFromSize(20);
		DiscreteDomain domain5 = DiscreteDomain.intRangeFromSize(5);

		NewFactorTable newTable = new NewFactorTable(domain10, domain20, domain5);
		newTable.setRepresentation(NewFactorTableRepresentation.DENSE_WEIGHT);
		newTable.randomizeWeights(rand);
		
		IFactorTable oldTable = FactorTable.create(domain10, domain20, domain5);
		oldTable.change(newTable.getIndices(), newTable.getWeights());
		
		FactorTablePerformanceTester oldTester = new FactorTablePerformanceTester(oldTable, iterations);
		FactorTablePerformanceTester newTester = new FactorTablePerformanceTester(newTable, iterations);

		@SuppressWarnings("unused")
		double oldNs, newNs;

		System.out.println("==== dense 10x20x5 table ==== ");
		
		oldNs = oldTester.testGet();
		newNs = newTester.testGet();
		
		oldNs = oldTester.testGetWeightIndexFromTableIndices();
		newNs = newTester.testGetWeightIndexFromTableIndices();

		oldNs = oldTester.testGetWeightForIndices();
		newNs = newTester.testGetWeightForIndices();

		oldNs = oldTester.testEvalAsFactorFunction();
		newNs = newTester.testEvalAsFactorFunction();
		
		System.out.println("==== sparse 10x20x5 table ==== ");
		
		// Randomly sparsify the tables
		for (int i = newTable.jointSize() / 2; --i>=0;)
		{
			newTable.setWeightForJointIndex(0.0, rand.nextInt(newTable.jointSize()));
		}
		newTable.setRepresentation(NewFactorTableRepresentation.SPARSE_WEIGHT);
		oldTable.change(newTable.getIndices(), newTable.getWeights());
		
		oldNs = oldTester.testGet();
		newNs = newTester.testGet();

		oldNs = oldTester.testEvalAsFactorFunction();
		newNs = newTester.testEvalAsFactorFunction();
		
		newTable.setRepresentation(NewFactorTableRepresentation.ALL_WEIGHT);
		
		oldNs = oldTester.testGetWeightIndexFromTableIndices();
		newNs = newTester.testGetWeightIndexFromTableIndices();

		oldNs = oldTester.testGetWeightForIndices();
		newNs = newTester.testGetWeightForIndices();
		
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
//		OpenIntIntHashMap map = new OpenIntIntHashMap(size * 2);
		for (int i = 0; i < size; ++i)
		{
			array[i] = rand.nextInt();
//			map.put(array[i], i);
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
	
	private void testRandomOperations(NewFactorTable table, int nOperations)
	{
		int[] indices = new int[table.getDimensions()];
		Object[] arguments = new Object[table.getDimensions()];
		
		while (--nOperations >= 0)
		{
			if (nOperations == 9810)
			{
				Math.log(1);
			}
			int operation = rand.nextInt(10);
			switch (operation)
			{
			case 0:
				// Randomly zero out an entry.
				table.setWeightForJointIndex(0.0, rand.nextInt(table.jointSize()));
				break;
			case 1:
				// Randomly set the representation
				NewFactorTableRepresentation oldRep = table.getRepresentation();
				NewFactorTableRepresentation newRep = NewFactorTableRepresentation.values()[rand.nextInt(16)];
				try
				{
					table.setRepresentation(newRep);
					assertEquals(newRep, table.getRepresentation());
				}
				catch (DimpleException ex)
				{
					assertEquals(oldRep, table.getRepresentation());
					assertTrue(newRep.isDeterministic());
				}
				break;
			case 3:
				table.normalize();
				assertTrue(table.isNormalized());
				break;
			default:
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
					table.setWeightForSparseIndex(weight, table.sparseIndexFromJointIndex(jointIndex));
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
				table.getDomainList().jointIndexToIndices(jointIndex, indices);
				table.setWeightForIndices(weight, indices);
				assertWeight(table, weight, jointIndex);

				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.getDomainList().jointIndexToIndices(jointIndex, indices);
				table.setEnergyForIndices(-Math.log(weight), indices);
				assertWeight(table, weight, jointIndex);

				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.getDomainList().jointIndexToElements(jointIndex, arguments);
				table.setWeightForArguments(weight, arguments);
				assertWeight(table, weight, jointIndex);
				
				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.getDomainList().jointIndexToElements(jointIndex, arguments);
				table.setEnergyForArguments(-Math.log(weight), arguments);
				assertWeight(table, weight, jointIndex);
			}
			
			assertInvariants(table);
		}
		
	}
	
	private static void assertWeight(NewFactorTable table, double weight, int jointIndex)
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

		int[] indices = table.getDomainList().jointIndexToIndices(jointIndex, null);
		assertEquals(energy, table.getEnergyForIndices(indices), 1e-12);
		assertEquals(weight, table.getWeightForIndices(indices), 13-12);
		
		Object[] arguments = table.getDomainList().jointIndexToElements(jointIndex, null);
		assertEquals(energy, table.getEnergyForArguments(arguments), 1e-12);
		assertEquals(weight, table.getWeightForArguments(arguments), 13-12);
	}
	
	public static void assertInvariants(NewFactorTable table)
	{
		NewFactorTableRepresentation representation = table.getRepresentation();
		assertBaseInvariants(table);
		assertOldInvariants(table);
		table.setRepresentation(representation);
	}
	
	public static void assertOldInvariants(IFactorTable table)
	{
		int nRows = table.getRows();
		assertTrue(nRows >= 0);
		
		int nCols = table.getColumns();
		assertTrue(nCols > 0);
		
		DiscreteDomain[] domains = table.getDomains();
		assertEquals(nCols,  domains.length);
		
		int[][] indices = table.getIndices();
		assertEquals(nRows, indices.length);
		
		double[] weights = table.getWeights();
		assertEquals(nRows, weights.length);
		
		Object[] arguments = new Object[nCols];
		
		for (int row = 0; row < nRows; ++row)
		{
			int[] rowValues = table.getRow(row);
			assertArrayEquals(rowValues, indices[row]);
			
			for (int col = 0; col < nCols; ++col)
			{
				assertEquals(rowValues[col], table.getEntry(row, col));
			}

			assertEquals(weights[row], table.get(rowValues), 1e-12);
		}
		
		for (int col = 0; col < nCols; ++col)
		{
			int[] colValues = table.getColumnCopy(col);
			for (int row = 0; row < nRows; ++row)
			{
				assertEquals(indices[row][col], colValues[row]);
			}
		}

		double[] energies = table.getPotentials();
		assertEquals(nRows, energies.length);
		for (int row = 0; row < nRows; ++row)
		{
			assertEquals(weights[row], Math.exp(-energies[row]), 1e-12);
		}
		
		for (int row = 0; row < nRows; ++row)
		{
			int[] rowValues = table.getRow(row);
			assertEquals(row, table.getWeightIndexFromTableIndices(rowValues));
			
			for (int col = 0; col < nCols; ++col)
			{
				arguments[col] = domains[col].getElement(rowValues[col]);
			}
			assertEquals(table.getWeights()[row], table.evalAsFactorFunction(arguments), 0.0);
		}
		
		if (table.getRows() != indices.length)
		{
			// Table density changed, probably due to call to getWeightIndexFromTableIndices.
			// Recompute indices:
			indices = table.getIndices();
			weights = table.getWeights();
		}
		
		if (table.isDirected())
		{
			int[] fromIndices = table.getDirectedFrom();
			assertTrue(fromIndices.length < nCols);
			assertTrue(fromIndices.length > 0);
			
			int[] toIndices = table.getDirectedTo();
			assertTrue(toIndices.length < nCols);
			assertTrue(toIndices.length > 0);
			
			assertEquals(nCols, toIndices.length + fromIndices.length);
			
			BitSet indexSet = new BitSet(nCols);
			for (int i : fromIndices)
			{
				assertTrue(i >= 0);
				assertTrue(i < nCols);
				assertFalse(indexSet.get(i));
				indexSet.set(i);
			}
			for (int i : toIndices)
			{
				assertTrue(i >= 0);
				assertTrue(i < nCols);
				assertFalse(indexSet.get(i));
				indexSet.set(i);
			}
			
			if (table.isDeterministicDirected())
			{
				for (int row = 0; row < nRows; ++row)
				{
					if (weights[row] != 0.0)
					{
						Arrays.fill(arguments, null);
						int[] rowValues = indices[row];
						for (int col : fromIndices)
						{
							arguments[col] = domains[col].getElement(rowValues[col]);
						}
						table.evalDeterministicFunction(arguments);
						for (int col : toIndices)
						{
							assertEquals(domains[col].getElement(rowValues[col]), arguments[col]);
						}
					}
				}
			}
		}
	}
	
	public static void assertBaseInvariants(INewFactorTableBase table)
	{
		int nDomains = table.getDimensions();
		assertTrue(nDomains >= 0);
		
		DiscreteDomainList domains = table.getDomainList();
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
		assertEquals(table.hasSparseRepresentation(), table.hasSparseWeights() || table.hasSparseEnergies());
		
		// Test iteration
		{
			int i = 0;
			for (NewFactorTableEntry entry : table)
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
					assertArrayEquals(table.sparseIndexToIndices(si, null), entry.indices());
					assertArrayEquals(table.sparseIndexToArguments(si, null), entry.values());
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
			NewFactorTableIterator iter = table.fullIterator();
			while (iter.hasNext())
			{
				NewFactorTableEntry entry = iter.next();
				assertEquals(i, entry.jointIndex());
				if (entry.weight() != 0.0)
				{
					totalWeight += entry.weight();
					++nonZeroCount;
				}
				if (table.hasDenseEnergies())
				{
					assertEquals(entry.energy(), table.getDenseEnergyForIndices(entry.indices()), 1e-12);
				}
				if (table.hasDenseWeights())
				{
					assertEquals(entry.weight(), table.getDenseWeightForIndices(entry.indices()), 1e-12);
				}
				++i;
			}
			assertEquals(table.jointSize(), i);
			assertEquals(nonZeroCount, table.computeMinSparseSize());
			if (table.isNormalized())
			{
				if (table.isDirected())
				{
					// TODO
				}
				else
				{
					if (totalWeight != 0.0)
					{
						assertEquals(1.0, totalWeight, 1e-12);
					}
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
			assertTrue(table.hasSparseEnergies() || table.hasSparseWeights());
			for (int si = 0; si < size; ++si)
			{
				table.sparseIndexToIndices(si, indices);
				for (int j = 0; j < nDomains; ++j)
				{
					assertTrue(indices[j] >= 0);
					assertTrue(indices[j] < domainSizes[j]);
				}
				assertEquals(si, table.sparseIndexFromIndices(indices));

				table.sparseIndexToArguments(si, arguments);
				for (int j = 0; j < nDomains; ++j)
				{
					assertEquals(arguments[j], domains.get(j).getElement(indices[j]));
				}
				assertEquals(si, table.sparseIndexFromArguments(arguments));

				int joint = table.sparseIndexToJointIndex(si);
				assertTrue(joint >= 0);
				assertTrue(joint < jointSize);
				assertEquals(si, table.sparseIndexFromJointIndex(joint));

				double energy = table.getEnergyForSparseIndex(si);
				assertEquals(energy, table.getEnergyForIndices(indices), 1e-12);
				assertEquals(energy, table.getEnergyForArguments(arguments), 1e-12);

				double weight = table.getWeightForSparseIndex(si);
				assertEquals(weight, table.getWeightForIndices(indices), 1e-12);
				assertEquals(weight, table.getWeightForArguments(arguments), 1e-12);

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
				assertEquals(1.0, table.getWeightForArguments(arguments), 0.0);
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
		
		INewFactorTableBase table2 = table.clone();
		assertBaseEqual(table, table2);
	}
	
	public static void assertBaseEqual(INewFactorTableBase table1, INewFactorTableBase table2)
	{
		assertEquals(table1.getClass(), table2.getClass());
		
		assertEquals(table1.isDirected(), table2.isDirected());
		assertEquals(table1.isDeterministicDirected(), table2.isDeterministicDirected());
		
		assertEquals(table1.getInputSet(), table2.getInputSet());
		
		assertEquals(table1.getDimensions(), table2.getDimensions());
		int nDomains = table1.getDimensions();
		for (int i = 0; i < nDomains; ++i)
		{
			assertEquals(table1.getDomainList().getDomainSize(i), table2.getDomainList().getDomainSize(i));
			assertSame(table1.getDomainList().get(i), table2.getDomainList().get(i));
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
	}
}
