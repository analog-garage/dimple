/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test;


import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.NopFactorFunction;
import com.analog.lyric.dimple.factorfunctions.XorDelta;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.dimple.model.variables.Discrete;



public class FactorFunctionTest {

	@BeforeClass
	public static void setUpBeforeClass()  {
	}

	@AfterClass
	public static void tearDownAfterClass()  {
	}

	@Before
	public void setUp()  {
	}

	@After
	public void tearDown()  {
	}
	
	
	@Test(expected=Exception.class)
	public void test_simpleStuff()
	{
		String name = "name";
		FactorFunction ff = new NopFactorFunction(name);
		assertTrue(ff.getName() == name);
		//should kaboom
		ff.eval(new Object[]{.5, .5});
	}
	
	@Test
	public void test_variable_constructor()
	{
//		public FactorTable(int [][] indices, double [] weights, Discrete... variables)
//		public FactorTable(int [][] indices, double [] weights,DiscreteDomain ... domains)
		
		Discrete[] discretes = new Discrete[]{new Discrete(0, 1), new Discrete(0, 1), new Discrete(0, 1)};
		Discrete[] discretes6 = new Discrete[]{new Discrete(0, 1), new Discrete(0, 1), new Discrete(0, 1),new Discrete(0, 1), new Discrete(0, 1), new Discrete(0, 1)};
		DiscreteDomain[] domains = new DiscreteDomain[]{DiscreteDomain.bit(),  DiscreteDomain.bit(), DiscreteDomain.bit()};
		DiscreteDomain[] vDomains = new DiscreteDomain[]{discretes[0].getDiscreteDomain(), discretes[1].getDiscreteDomain(), discretes[2].getDiscreteDomain()};
		
		XorDelta xorFF = new XorDelta();
		IFactorTable xorFT = xorFF.getFactorTable(domains);
		int[][] xorIndices = xorFT.getIndicesSparseUnsafe();
		double[] xorWeights = xorFT.getWeightsSparseUnsafe();
		
		int[][] table = new int[xorIndices.length][];
		double[] weights = new double[xorWeights.length];
		for(int i = 0; i < table.length; ++i)
		{
			table[i] = new int[xorIndices[i].length];
			for(int j = 0; j < table[i].length; ++j)
			{
				table[i][j] = xorIndices[i][j];
			}
			weights[i] = xorWeights[i];
		}
		
		IFactorTable ftVar = FactorTable.create(table, weights, discretes);
		IFactorTable ftVDomain = FactorTable.create(table, weights, vDomains);
		IFactorTable ftDomain = FactorTable.create(table, weights, domains);
		
		TableFactorFunction tffVar = new TableFactorFunction("tffVar", table, weights, discretes);
		TableFactorFunction tffDVar = new TableFactorFunction("tffDVar", ftVar);
		TableFactorFunction tffVDomain = new TableFactorFunction("tffVar", table, weights, vDomains);
		TableFactorFunction tffDomain = new TableFactorFunction("tffVar", table, weights, domains);
		
		FactorGraph fg = new FactorGraph();

		
		DiscreteFactor fVar = (DiscreteFactor) fg.addFactor(tffVar, (Object[])discretes);
		DiscreteFactor fDomain = (DiscreteFactor) fg.addFactor(tffDomain, (Object[])discretes);
		DiscreteFactor fxd = (DiscreteFactor) fg.addFactor(xorFF, (Object[])discretes);

		DiscreteFactor fVar2 = (DiscreteFactor) fg.addFactor(tffVar, (Object[])discretes);
		DiscreteFactor fDomain2 = (DiscreteFactor) fg.addFactor(tffDomain, (Object[])discretes);
		DiscreteFactor fxd2 = (DiscreteFactor) fg.addFactor(xorFF, (Object[])discretes);
		
		DiscreteFactor fxd6 = (DiscreteFactor) fg.addFactor(xorFF, (Object[])discretes6);
		DiscreteFactor fxd6_2 = (DiscreteFactor) fg.addFactor(xorFF, (Object[])discretes6);
		
		assertEquals(fVar.getFactorTable().hashCode(), fVar2.getFactorTable().hashCode());
		assertEquals(fDomain.getFactorTable().hashCode(), fDomain2.getFactorTable().hashCode());
		assertEquals(fxd.getFactorTable().hashCode(), fxd2.getFactorTable().hashCode());
		assertEquals(fxd6.getFactorTable().hashCode(), fxd6_2.getFactorTable().hashCode());
		assertTrue(fVar.getFactorTable().hashCode() != fDomain.getFactorTable().hashCode());
		assertTrue(fxd.getFactorTable().hashCode() != fxd6.getFactorTable().hashCode());
		
		JointDomainIndexer ftDomains = ftVar.getDomainIndexer();
		for(int i = 0; i < ftDomains.size(); ++i)
		{
			DiscreteDomain idomain = ftDomains.get(i);
			for(int j = 0; j < idomain.size(); ++j)
			{
				Object ijelt = idomain.getElement(j);
				assertEquals(ijelt ,ftVDomain.getDomainIndexer().get(i).getElement(j));
				assertEquals(ijelt, ftDomain.getDomainIndexer().get(i).getElement(j));
				assertEquals(ijelt, tffVar.getFactorTable(domains).getDomainIndexer().get(i).getElement(j));
				assertEquals(ijelt, tffDVar.getFactorTable(domains).getDomainIndexer().get(i).getElement(j));
				assertEquals(ijelt, tffVDomain.getFactorTable(domains).getDomainIndexer().get(i).getElement(j));
				assertEquals(ijelt, tffDomain.getFactorTable(domains).getDomainIndexer().get(i).getElement(j));
				
				assertEquals(ijelt, fVar2.getFactorTable().getDomainIndexer().get(i).getElement(j));
			}
		}
		
		for(int i = 0; i < ftVar.sparseSize(); ++i)
		{
			int[] ftVarRow = ftVar.sparseIndexToIndices(i);
			int[] xorFTRow = xorFT.sparseIndexToIndices(i);
			assertArrayEquals(ftVarRow, xorFTRow);
		}
		
		DiscreteDomain[] domains6 = new DiscreteDomain[6];
		Arrays.fill(domains6, DiscreteDomain.bit());
		
		DiscreteDomain domainThreeEntries = DiscreteDomain.range(0.0, 2.0);
		
		IFactorTable ftThreeBinary = xorFF.getFactorTable(domains);
		IFactorTable ftThreeBinary2 = xorFF.getFactorTable(vDomains);
		assertSame(ftThreeBinary, ftThreeBinary2);
		assertEquals(ftThreeBinary.hashCode(), ftThreeBinary2.hashCode());
		IFactorTable xor6FT = xorFF.getFactorTable(domains6);
		IFactorTable xor3AryFT = xorFF.getFactorTable(new DiscreteDomain[]{domains[0], domains[1], domainThreeEntries});
		assertNotSame(ftThreeBinary, xor6FT);
		assertTrue(ftThreeBinary.hashCode() != xor6FT.hashCode());
		assertNotSame(ftThreeBinary, xor3AryFT);
		assertTrue(ftThreeBinary.hashCode() != xor3AryFT.hashCode());
		
		
		//no kaboom
		assertTrue(ftThreeBinary.toString().length() != 0);
		
	}
}
