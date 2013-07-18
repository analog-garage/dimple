package com.analog.lyric.dimple.test.FactorFunctions.core;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.FactorFunctions.core.IFactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.INewFactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.INewFactorTableBase;
import com.analog.lyric.dimple.FactorFunctions.core.NewFactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;

public class TestFactorTable
{
	final DiscreteDomain domain2 = DiscreteDomain.range(0,1);
	final DiscreteDomain domain3 = DiscreteDomain.range(0,2);
	
	@Test
	public void testNewFactorTable()
	{
		NewFactorTable t2x3 = new NewFactorTable(domain2, domain3);
		assertEquals(2, t2x3.domainCount());
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
		
		Random rand = new Random(42);
		t2x3.densify();
		t2x3.randomizeWeights(rand);
		assertInvariants(t2x3);
		
		t2x3.computeWeights();
		assertEquals(INewFactorTable.Representation.BOTH, t2x3.getRepresentation());
		assertInvariants(t2x3);
		
		t2x3.normalize();
		assertInvariants(t2x3);
		
		t2x3.setRepresentation(INewFactorTable.Representation.WEIGHT);
		assertEquals(INewFactorTable.Representation.WEIGHT, t2x3.getRepresentation());
		assertInvariants(t2x3);
		
		BitSet xor3Input = new BitSet(4);
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
				xor2.setWeight(1.0, i, i^j, j);
			}
		}
		assertTrue(xor2.isDeterministicDirected());
		assertInvariants(xor2);
		
	}
	
	public static void assertInvariants(NewFactorTable table)
	{
		assertBaseInvariants(table);
		assertOldInvariants(table);
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
		int nDomains = table.domainCount();
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
			
			double energy = table.getEnergy(location);
			assertEquals(energy, table.getEnergy(indices), 0.0);
			assertEquals(energy, table.evalEnergy(arguments), 0.0);
			
			double weight = table.getWeight(location);
			assertEquals(weight, table.getWeight(indices), 0.0);
			assertEquals(weight, table.evalWeight(arguments), 0.0);
			
			assertEquals(energy, -Math.log(weight), 1e-12);
		}
		
		if (table.isNormalized())
		{
			
		}
		
		if (table.isDeterministicDirected())
		{
			assertTrue(table.isDirected());
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
		
		assertEquals(table1.domainCount(), table2.domainCount());
		int nDomains = table1.domainCount();
		for (int i = 0; i < nDomains; ++i)
		{
			assertEquals(table1.getDomainSize(i), table2.getDomainSize(i));
			assertSame(table1.getDomain(i), table2.getDomain(i));
		}
		
		assertEquals(table1.size(), table2.size());
		int size = table1.size();
		for (int i = 0; i < size; ++i)
		{
			
		}
	}
}
