package com.analog.lyric.dimple.test.FactorFunctions.core;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.IFactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.INewFactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.INewFactorTableBase;
import com.analog.lyric.dimple.FactorFunctions.core.NewFactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.NewFactorTableEntry;
import com.analog.lyric.dimple.FactorFunctions.core.NewFactorTableIterator;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;

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
		assertEquals(2, t2x3.getDomainCount());
		assertEquals(domain2, t2x3.getDomain(0));
		assertEquals(domain3, t2x3.getDomain(1));
		assertFalse(t2x3.isDense());
		assertFalse(t2x3.isDirected());
		assertEquals(INewFactorTable.Representation.ENERGY, t2x3.getRepresentation());
		assertInvariants(t2x3);
		
		t2x3.densify();
		assertTrue(t2x3.isDense());
		assertInvariants(t2x3);
		assertEquals(t2x3.jointSize(), t2x3.size());
		
		t2x3.sparsify();
		assertFalse(t2x3.isDense());
		assertEquals(0, t2x3.size());
		assertInvariants(t2x3);
		
		t2x3.densify();
		t2x3.randomizeWeights(rand);
		assertInvariants(t2x3);
		
		assertEquals(INewFactorTable.Representation.ENERGY, t2x3.getRepresentation());
		t2x3.computeWeights();
		assertEquals(INewFactorTable.Representation.BOTH, t2x3.getRepresentation());
		assertInvariants(t2x3);
		
		t2x3.normalize();
		assertInvariants(t2x3);
		
		t2x3.setRepresentation(INewFactorTable.Representation.WEIGHT);
		assertEquals(INewFactorTable.Representation.WEIGHT, t2x3.getRepresentation());
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
		
		NewFactorTable t2x2x2 = xor2.clone();
		assertBaseEqual(t2x2x2, xor2);
		t2x2x2.setWeightForIndices(.5, 1, 1, 1);
		assertFalse(t2x2x2.isDeterministicDirected());
		assertInvariants(t2x2x2);
		
		NewFactorTable t2x3x4 = new NewFactorTable(domain2, domain3, domain5);
		t2x3x4.densify();
		t2x3x4.randomizeWeights(rand);
		assertInvariants(t2x3x4);
		testRandomOperations(t2x3x4, 10000);
	}
	
	@Test
	public void performanceComparison()
	{
		int iterations = 10000;
		
		Random rand = new Random(13);
		DiscreteDomain domain10 = DiscreteDomain.intRangeFromSize(10);
		DiscreteDomain domain20 = DiscreteDomain.intRangeFromSize(20);
		DiscreteDomain domain5 = DiscreteDomain.intRangeFromSize(5);

		NewFactorTable newTable = new NewFactorTable(domain10, domain20, domain5);
		newTable.densify();
		newTable.setRepresentation(INewFactorTable.Representation.WEIGHT);
		newTable.randomizeWeights(rand);
		
		FactorTable oldTable = new FactorTable(domain10, domain20, domain5);
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

		oldNs = oldTester.testEvalAsFactorFunction();
		newNs = newTester.testEvalAsFactorFunction();
		
		System.out.println("==== sparse 10x20x5 table ==== ");
		
		// Randomly sparsify the tables
		for (int i = newTable.jointSize() / 2; --i>=0;)
		{
			newTable.setWeightForJointIndex(0.0, rand.nextInt(newTable.jointSize()));
		}
		newTable.sparsify();
		oldTable.change(newTable.getIndices(), newTable.getWeights());
		
//		oldNs = oldTester.testGet();
//		newNs = newTester.testGet();

		newTable.densify();
		oldNs = oldTester.testGetWeightIndexFromTableIndices();
		newNs = newTester.testGetWeightIndexFromTableIndices();

		oldNs = oldTester.testEvalAsFactorFunction();
		newNs = newTester.testEvalAsFactorFunction();
		
	}
	
	private void testRandomOperations(NewFactorTable table, int nOperations)
	{
		int[] indices = new int[table.getDomainCount()];
		Object[] arguments = new Object[table.getDomainCount()];
		
		while (--nOperations >= 0)
		{
			int operation = rand.nextInt(10);
			switch (operation)
			{
			case 0:
				// Make sure there is at least one zero entry and sparsify
				table.setWeightForJointIndex(0.0, rand.nextInt(table.jointSize()));
				table.sparsify();
				break;
			case 1:
				table.densify();
				assertTrue(table.isDense());
				break;
			case 2:
				table.setRepresentation(INewFactorTable.Representation.ENERGY);
				assertEquals(INewFactorTable.Representation.ENERGY, table.getRepresentation());
				break;
			case 3:
				table.setRepresentation(INewFactorTable.Representation.WEIGHT);
				assertEquals(INewFactorTable.Representation.WEIGHT, table.getRepresentation());
				break;
			case 4:
				table.setRepresentation(INewFactorTable.Representation.BOTH);
				assertEquals(INewFactorTable.Representation.BOTH, table.getRepresentation());
				break;
			case 5:
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
				int location = table.locationFromJointIndex(jointIndex);
				if (location >= 0)
				{
					table.setWeightForLocation(weight, table.locationFromJointIndex(jointIndex));
					assertWeight(table, weight, jointIndex);
				}
				
				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				location = table.locationFromJointIndex(jointIndex);
				if (location >= 0)
				{
					table.setEnergyForLocation(-Math.log(weight), location);
					assertWeight(table, weight, jointIndex);
				}
				
				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.jointIndexToIndices(jointIndex, indices);
				table.setWeightForIndices(weight, indices);
				assertWeight(table, weight, jointIndex);

				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.jointIndexToIndices(jointIndex, indices);
				table.setEnergyForIndices(-Math.log(weight), indices);
				assertWeight(table, weight, jointIndex);

				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.jointIndexToArguments(jointIndex, arguments);
				table.setWeightForArguments(weight, arguments);
				assertWeight(table, weight, jointIndex);
				
				jointIndex = rand.nextInt(table.jointSize());
				weight = rand.nextDouble();
				table.jointIndexToArguments(jointIndex, arguments);
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
		
		int location = table.locationFromJointIndex(jointIndex);
		assertEquals(energy, table.getEnergyForLocation(location), 1e-12);
		assertEquals(weight, table.getWeightForLocation(location), 1e-12);

		int[] indices = table.jointIndexToIndices(jointIndex, null);
		assertEquals(energy, table.getEnergyForIndices(indices), 1e-12);
		assertEquals(weight, table.getWeightForIndices(indices), 13-12);
		
		Object[] arguments = table.jointIndexToArguments(jointIndex, null);
		assertEquals(energy, table.getEnergyForArguments(arguments), 1e-12);
		assertEquals(weight, table.getWeightForArguments(arguments), 13-12);
	}
	
	public static void assertInvariants(NewFactorTable table)
	{
		INewFactorTable.Representation representation = table.getRepresentation();
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

			assertEquals(weights[row], table.get(rowValues), 0.0);
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
		int nDomains = table.getDomainCount();
		assertTrue(nDomains >= 0);
		
		int expectedJointSize = 1;
		int[] domainSizes = new int[nDomains];
		for (int i = 0; i < nDomains; ++i)
		{
			int domainSize = table.getDomainSize(i);
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
		
		int size = table.size();
		assertTrue(size >= 0);
		int jointSize = table.jointSize();
		assertTrue(size <= jointSize);
		assertEquals(expectedJointSize, jointSize);
		
		Object[] arguments = new Object[nDomains];
		int[] indices = new int[nDomains];
		
		// Test iteration
		{
			int i = 0;
			for (NewFactorTableEntry entry : table)
			{
				assertEquals(i, entry.location());
				assertSame(table, entry.table());
				assertEquals(table.getEnergyForLocation(i), entry.energy(), 0.0);
				assertEquals(entry.energy(), -Math.log(entry.weight()), 1e-12);
				
				assertEquals(table.locationToJointIndex(i), entry.jointIndex());
				
				assertArrayEquals(table.locationToIndices(i, null), entry.indices());
				assertArrayEquals(table.locationToArguments(i, null), entry.values());
				
				++i;
			}
			assertEquals(table.size(), i);
			
			i = 0;
			NewFactorTableIterator iter = table.jointIndexIterator();
			while (iter.hasNext())
			{
				NewFactorTableEntry entry = iter.next();
				assertEquals(i, entry.jointIndex());
				assertEquals(entry.jointIndex(), iter.jointIndex());
				assertEquals(entry.energy(), iter.energy(), 0.0);
				assertEquals(entry.weight(), iter.weight(), 0.0);
				assertEquals(entry.location(), iter.location());
				++i;
			}
			assertEquals(table.jointSize(), i);
			
			assertNull(iter.next());
			
			try
			{
				iter.remove();
				fail("should not get here");
			}
			catch (DimpleException ex)
			{
			}
		}
		
		for (int location = 0; location < size; ++location)
		{
			table.locationToIndices(location, indices);
			for (int j = 0; j < nDomains; ++j)
			{
				assertTrue(indices[j] >= 0);
				assertTrue(indices[j] < domainSizes[j]);
			}
			assertEquals(location, table.locationFromIndices(indices));
			
			table.locationToArguments(location, arguments);
			for (int j = 0; j < nDomains; ++j)
			{
				assertEquals(arguments[j], table.getDomain(j).getElement(indices[j]));
			}
			assertEquals(location, table.locationFromArguments(arguments));
			
			int joint = table.locationToJointIndex(location);
			assertTrue(joint >= 0);
			assertTrue(joint < jointSize);
			assertEquals(location, table.locationFromJointIndex(joint));
			
			double energy = table.getEnergyForLocation(location);
			assertEquals(energy, table.getEnergyForIndices(indices), 0.0);
			assertEquals(energy, table.getEnergyForArguments(arguments), 0.0);
			
			double weight = table.getWeightForLocation(location);
			assertEquals(weight, table.getWeightForIndices(indices), 0.0);
			assertEquals(weight, table.getWeightForArguments(arguments), 0.0);
			
			assertEquals(energy, -Math.log(weight), 1e-12);
		}
		
		// Test some bogus inputs
		assertEquals(Double.POSITIVE_INFINITY, table.getEnergyForLocation(-1), 0.0);
		assertEquals(0.0, table.getWeightForLocation(-1), 0.0);
		
		if (table.isNormalized())
		{
			
		}
		
		if (table.isDeterministicDirected())
		{
			assertTrue(table.isDirected());
			table.evalDeterministic(arguments);
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
		
		assertEquals(table1.getDomainCount(), table2.getDomainCount());
		int nDomains = table1.getDomainCount();
		for (int i = 0; i < nDomains; ++i)
		{
			assertEquals(table1.getDomainSize(i), table2.getDomainSize(i));
			assertSame(table1.getDomain(i), table2.getDomain(i));
		}
		
		assertEquals(table1.isDirected(), table2.isDirected());
		assertEquals(table1.isNormalized(), table2.isNormalized());
		assertEquals(table1.isDeterministicDirected(), table2.isDeterministicDirected());
		
		assertEquals(table1.size(), table2.size());
		int size = table1.size();
		for (int i = 0; i < size; ++i)
		{
			assertEquals(table1.getWeightForLocation(i), table2.getWeightForLocation(i), 1e-12);
			assertEquals(table1.getEnergyForLocation(i), table2.getEnergyForLocation(i), 1e-12);
			assertEquals(table1.locationToJointIndex(i), table2.locationToJointIndex(i));
		}
	}
}
