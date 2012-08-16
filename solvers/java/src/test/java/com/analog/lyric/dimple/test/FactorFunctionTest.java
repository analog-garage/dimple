/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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


import org.junit.* ;

import com.analog.lyric.dimple.FactorFunctions.NopFactorFunction;
import com.analog.lyric.dimple.FactorFunctions.XorDelta;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.FactorGraph;

import static org.junit.Assert.* ;



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
		
		Discrete[] discretes = new Discrete[]{new Discrete(0.0, 1.0), new Discrete(0.0, 1.0), new Discrete(0.0, 1.0)};
		Discrete[] discretes6 = new Discrete[]{new Discrete(0.0, 1.0), new Discrete(0.0, 1.0), new Discrete(0.0, 1.0),new Discrete(0.0, 1.0), new Discrete(0.0, 1.0), new Discrete(0.0, 1.0)}; 
		DiscreteDomain[] domains = new DiscreteDomain[]{new DiscreteDomain(0.0, 1.0), new DiscreteDomain(0.0, 1.0), new DiscreteDomain(0.0, 1.0)};
		DiscreteDomain[] vDomains = new DiscreteDomain[]{discretes[0].getDiscreteDomain(), discretes[1].getDiscreteDomain(), discretes[2].getDiscreteDomain()};
		
		XorDelta xorFF = new XorDelta();
		FactorTable xorFT = xorFF.getFactorTable(domains);
		int[][] xorIndices = xorFT.getIndices();
		double[] xorWeights = xorFT.getWeights();
		
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
		
		FactorTable ftVar = new FactorTable(table, weights, discretes);
		FactorTable ftVDomain = new FactorTable(table, weights, vDomains);
		FactorTable ftDomain = new FactorTable(table, weights, domains);
		
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
		
		for(int i = 0; i < ftVar.getDomains().length; ++i)
		{
			for(int j = 0; j < ftVar.getDomains()[i].getElements().length; ++j)
			{
				assertEquals(ftVar.getDomains()[i].getElements()[j],ftVDomain.getDomains()[i].getElements()[j]); 
				assertEquals(ftVar.getDomains()[i].getElements()[j],ftDomain.getDomains()[i].getElements()[j]); 
				assertEquals(ftVar.getDomains()[i].getElements()[j],tffVar.getFactorTable(domains).getDomains()[i].getElements()[j]); 
				assertEquals(ftVar.getDomains()[i].getElements()[j],tffDVar.getFactorTable(domains).getDomains()[i].getElements()[j]); 
				assertEquals(ftVar.getDomains()[i].getElements()[j],tffVDomain.getFactorTable(domains).getDomains()[i].getElements()[j]); 
				assertEquals(ftVar.getDomains()[i].getElements()[j],tffDomain.getFactorTable(domains).getDomains()[i].getElements()[j]);
				
				assertEquals(ftVar.getDomains()[i].getElements()[j],fVar2.getFactorTable().getDomains()[i].getElements()[j]); 
			}
		}
		
		for(int i = 0; i < ftVar.getRows(); ++i)
		{
			int[] ftVarRow = ftVar.getRow(i);
			int[] xorFTRow = xorFT.getRow(i);
			for(int j = 0; j < ftVar.getColumns(); ++j)
			{
				int[] ftVarColumn = ftVar.getColumnCopy(j);
				int[] xorFTColumn = xorFT.getColumnCopy(j);
				
				assertEquals(ftVar.getEntry(i, j), xorFT.getEntry(i, j));
				assertEquals(ftVar.getEntry(i, j), ftVarRow[j]);
				assertEquals(ftVar.getEntry(i, j), xorFTRow[j]);
				
				assertEquals(ftVar.getEntry(i, j), ftVarColumn[i]);
				assertEquals(ftVar.getEntry(i, j), xorFTColumn[i]);
				
				assertEquals(ftVar.getEntry(i, j), fVar.getFactorTable().getEntry(i, j));
				assertEquals(ftVar.getEntry(i, j), fVar.getFactorTable().getRow(i)[j]);
				assertEquals(ftVar.getEntry(i, j), fVar.getFactorTable().getColumnCopy(j)[i]);
				assertEquals(ftVar.getEntry(i, j), fVar2.getFactorTable().getEntry(i, j));
				assertEquals(ftVar.getEntry(i, j), fVar2.getFactorTable().getRow(i)[j]);
				assertEquals(ftVar.getEntry(i, j), fVar2.getFactorTable().getColumnCopy(j)[i]);
				assertEquals(ftVar.getEntry(i, j), fxd.getFactorTable().getEntry(i, j));
				assertEquals(ftVar.getEntry(i, j), fxd.getFactorTable().getRow(i)[j]);
				assertEquals(ftVar.getEntry(i, j), fxd.getFactorTable().getColumnCopy(j)[i]);
			}
			
			
		}
		
		DiscreteDomain[] domains6 = new DiscreteDomain[]{new DiscreteDomain(0.0, 1.0), new DiscreteDomain(0.0, 1.0), new DiscreteDomain(0.0, 1.0),
													     new DiscreteDomain(0.0, 1.0), new DiscreteDomain(0.0, 1.0), new DiscreteDomain(0.0, 1.0)};
		
		DiscreteDomain domainThreeEntries = new DiscreteDomain(0.0, 1.0, 2.0);
		
		FactorTable ftThreeBinary = xorFF.getFactorTable(domains); 
		FactorTable ftThreeBinary2 = xorFF.getFactorTable(vDomains);
		assertEquals(ftThreeBinary.hashCode(), ftThreeBinary2.hashCode());
		FactorTable xor6FT = xorFF.getFactorTable(domains6);
		FactorTable xor3AryFT = xorFF.getFactorTable(new DiscreteDomain[]{domains[0], domains[1], domainThreeEntries});
		assertTrue(ftThreeBinary.hashCode() != xor6FT.hashCode());
		assertTrue(ftThreeBinary.hashCode() != xor3AryFT.hashCode());
		
		
		//no kaboom
		assertTrue(ftThreeBinary.toString().length() != 0);
		
	}
}
