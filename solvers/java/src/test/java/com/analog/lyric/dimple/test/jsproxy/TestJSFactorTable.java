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

package com.analog.lyric.dimple.test.jsproxy;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.Random;

import netscape.javascript.JSException;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.core.FactorTableEntry;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTableIterator;
import com.analog.lyric.dimple.jsproxy.JSDiscreteDomain;
import com.analog.lyric.dimple.jsproxy.JSFactorGraph;
import com.analog.lyric.dimple.jsproxy.JSFactorTable;
import com.analog.lyric.dimple.jsproxy.JSVariable;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestJSFactorTable extends JSTestBase
{
	private Random _rand = new Random(23);
	
	@Test
	public void test()
	{
		JSDiscreteDomain bitDomain = state.domains.bit();
		JSFactorTable table = state.functions.createTable(new Object[] { bitDomain, bitDomain } );
		assertEquals(2, table.getDimensions());
		assertEquals("SPARSE_ENERGY", table.getRepresentation());
		assertInvariants(table);
		
		table.setRepresentation("DENSE_ENERGY");
		assertEquals("DENSE_ENERGY", table.getRepresentation());
		table.getDelegate().randomizeWeights(_rand);
		assertInvariants(table);
		
		JSFactorGraph fg = state.createGraph();
		JSVariable var1 = fg.addVariable(bitDomain, "a");
		JSVariable var2 = fg.addVariable(bitDomain, "b");
		JSVariable var3 = fg.addVariable(state.domains.real(), "r");
		
		table = state.functions.createTable(new Object[] { bitDomain.getDelegate(), var1, var2.getDelegate() });
		assertEquals(3, table.getDimensions());
		assertInvariants(table);
		
		table.setRepresentation("DENSE_WEIGHT");
		assertEquals("DENSE_WEIGHT", table.getRepresentation());
		table.getDelegate().randomizeWeights(_rand);
		assertInvariants(table);
		
		assertFalse(table.isDirected());
		table.setDirectedOutputs(new int[] { 0 });
		assertTrue(table.isDirected());
		assertArrayEquals(new int[] { 0 }, table.getDirectedOutputs());
		assertInvariants(table);
		
		// xor
		table.setWeights(
			new int[][] { new int[] { 0, 0, 0 }, new int[] { 1, 0, 1}, new int[] { 1, 1, 0 }, new int[] { 0, 1, 1 }},
			new double[] { 1.0, 1.0, 1.0, 1.0 });
		assertInvariants(table);
		
		expectThrow(JSException.class, ".*is not a discrete domain or variable",
			state.functions, "createTable",	new Object[] { new Object[] { "frob" }});
		expectThrow(JSException.class, ".*is not a discrete domain or variable",
			state.functions, "createTable",	new Object[] { new Object[] { var3 }});
	}
	
	private void assertInvariants(JSFactorTable table)
	{
		final int nDimensions = table.getDimensions();
		assertTrue(nDimensions > 0);
		
		for (int i = 0; i < nDimensions; ++i)
		{
			JSDiscreteDomain domain = table.getDomain(i);
			assertTrue(domain.isDiscrete());
		}
		
		IFactorTable realTable = table.getDelegate();
		IFactorTableIterator iterator = realTable.fullIterator();
		while (iterator.advance())
		{
			assertEquals(iterator.energy(), table.getEnergyForIndices(iterator.indicesUnsafe()), 0.0);
			assertEquals(iterator.weight(), table.getWeightForIndices(iterator.indicesUnsafe()), 0.0);
			FactorTableEntry entry =  requireNonNull(iterator.getEntry());
			assertEquals(iterator.energy(), table.getEnergyForElements(entry.values()), 0.0);
			assertEquals(iterator.weight(), table.getWeightForElements(entry.values()), 0.0);
		}
		
		if (table.isDirected())
		{
			int[] outputs = table.getDirectedOutputs();
			assertNotNull(outputs);
			assertTrue(outputs.length > 0);
			for (int i : outputs)
			{
				assertTrue(i >= 0);
				assertTrue(i < nDimensions);
			}
		}
		else
		{
			assertNull(table.getDirectedOutputs());
		}
		
		JSFactorTable table2 = table.copy();
		assertEquals(table.isDirected(), table2.isDirected());
		assertNotEquals(table, table2);
		iterator = realTable.iterator();
		double totalWeight = 0.0;
		while (iterator.advance())
		{
			int[] indices = iterator.indicesUnsafe();
			assertEquals(iterator.energy(), table2.getEnergyForIndices(indices), 0.0);
			totalWeight += iterator.weight();
			
			table2.setEnergyForIndices(iterator.energy() + 42, indices);
			assertEquals(iterator.energy() + 42, table2.getEnergyForIndices(indices), 0.0);
			
			Object[] elements = requireNonNull(iterator.getEntry()).values();
			table2.setEnergyForElements(iterator.energy() + 2.3, elements);
			assertEquals(iterator.energy() + 2.3, table2.getEnergyForIndices(indices), 0.0);
			
			table2.setWeightForElements(iterator.weight() + 2.3, elements);
			assertEquals(iterator.weight() + 2.3, table2.getWeightForIndices(indices), 1e14);

			table2.setWeightForIndices(iterator.weight() + 1, indices);
			assertEquals(iterator.weight() + 1, table2.getWeightForIndices(indices), 0.0);
		}
		
		if (totalWeight != 0.0)
		{
			table2.normalize();
			assertTrue(table2.isNormalized());
		}
	}
}
