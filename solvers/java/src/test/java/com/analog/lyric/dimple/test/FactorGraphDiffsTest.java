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


import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.XorDelta;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INameable;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.util.misc.FactorGraphDiffs;
import com.analog.lyric.util.test.Helpers;

public class FactorGraphDiffsTest extends DimpleTestBase
{

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
	
	FactorGraphDiffs check(FactorGraph a, FactorGraph b, boolean expectedResult, String tag)
	{
		return check(a, b, expectedResult, true, tag, false);
	}
	FactorGraphDiffs check(FactorGraph a, FactorGraph b, boolean noDiffsExpected, boolean byName, String tag, boolean bPrint)
	{
		FactorGraphDiffs diffs = null;
		try
		{
			diffs = a.getFactorGraphDiffs(b, false, byName);
			if(noDiffsExpected != diffs.noDiffs())
			{
				diffs.print(tag + "_ERROR");
				System.out.println(Helpers.getAdjacencyString(a));
				System.out.println(Helpers.getAdjacencyString(b));
			}
			assertTrue(noDiffsExpected == diffs.noDiffs());
			if(bPrint){diffs.print(tag);}
		}
		catch(Exception e)
		{
			System.out.println("Exception diffing [" + tag + "]");
			System.out.println(Helpers.getFullString(a));
			System.out.println(Helpers.getFullString(b));
			throw new RuntimeException(e);
		}
		if(diffs.noDiffs())
		{
			
		}
		return diffs;
	}
	
	void compareToSelf(FactorGraph fg)
	{
		FactorGraphDiffs fgvsfgName
			= fg.getFactorGraphDiffsByName(fg);
		
		if(!fgvsfgName.noDiffs())
		{
			System.out.println(fgvsfgName);
		}

		assertTrue(fgvsfgName.noDiffs());

		FactorGraphDiffs fgvsfgNameRoot
			= fg.getFactorGraphDiffsByName(fg.copyRoot());
	
		if(!fgvsfgNameRoot.noDiffs())
		{
			System.out.println(fgvsfgNameRoot);
		}

		assertTrue(fgvsfgName.noDiffs());
		
		
		FactorGraphDiffs fgvsfgUUID
			= fg.getFactorGraphDiffsByUUID(fg);
		
		fgvsfgUUID.toString();
		if(!fgvsfgUUID.noDiffs())
		{
			System.out.println(fgvsfgUUID);
		}
		
		assertTrue(fgvsfgUUID.noDiffs());
	}
	
	public void checkNameChange( INameable n,
			 FactorGraph g1,
			 FactorGraph g2,
			 @Nullable FactorGraph pg1,
			 @Nullable FactorGraph pg2)
	{
		checkNameChange(n,
				 null,
				 g1,
				 g2,
				 pg1,
				 pg2);
	}
	public void checkNameChange( INameable n,
								 @Nullable INameable n2,
								 FactorGraph g1,
								 FactorGraph g2,
								 @Nullable FactorGraph pg1,
								 @Nullable FactorGraph pg2)
	{
		String oldName = n.getName();
		n.setName("x");
		
		String oldName2 = "";
		if(n2 != null)
		{
			oldName2 = n2.getName();
			n2.setName("y");
		}

		FactorGraphDiffs diffs
			= g1.getFactorGraphDiffsByName(g2);
		assertTrue(!diffs.noDiffs());

		if(pg1 != null)
		{
			diffs = pg1.getFactorGraphDiffsByName(Objects.requireNonNull(pg2));
			diffs.toString();
			assertTrue(!diffs.noDiffs());
		}
		
		n.setName(oldName);
		
		if(n2 != null)
		{
			diffs = g1.getFactorGraphDiffsByName(g2);
			assertTrue(!diffs.noDiffs());
	
			if(pg1 != null)
			{
				diffs = pg1.getFactorGraphDiffsByName(Objects.requireNonNull(pg2));
				diffs.toString();
				assertTrue(!diffs.noDiffs());
			}
			n2.setName(oldName2);
		}
		
		diffs = g1.getFactorGraphDiffsByName(g2);
		assertTrue(diffs.noDiffs());
		if(pg1 != null)
		{
			diffs = pg1.getFactorGraphDiffsByName(Objects.requireNonNull(pg2));
			assertTrue(diffs.noDiffs());
		}
	}

	@Test
	public void test_simple_one_level_compare_names_only()
	{
		FactorGraph[] fgs1
			= Helpers.MakeSimpleThreeLevelGraphs();
		FactorGraph[] fgs2
			= Helpers.MakeSimpleThreeLevelGraphs();
		
		FactorGraph fgLeaf1 = fgs1[fgs1.length - 1];
		FactorGraph fgLeaf2 = fgs2[fgs2.length - 1];
		
		compareToSelf(fgLeaf1);
		compareToSelf(fgLeaf2);
		
		Discrete vB12 = requireNonNull((Discrete) fgLeaf2.getObjectByName("vLeafB1"));
		Discrete vO12 = requireNonNull((Discrete) fgLeaf2.getObjectByName("vLeafO1"));
		Discrete vO22 = requireNonNull((Discrete) fgLeaf2.getObjectByName("vLeafO2"));
		Factor fLeaf2 = requireNonNull((Factor) fgLeaf2.getObjectByName("fLeaf"));
		
		//Name change, change back, boundary
		vB12.setName("x");
		FactorGraphDiffs diffs = fgLeaf1.getFactorGraphDiffsByName(fgLeaf2);
		assertTrue(!diffs.noDiffs());
		vB12.setName("vLeafB1");
		diffs = fgLeaf1.getFactorGraphDiffsByName(fgLeaf2);
		diffs.toString();
		assertTrue(diffs.noDiffs());
		
		
		//Name change, change back, owned
		vO12.setName("x");
		diffs = fgLeaf1.getFactorGraphDiffsByName(fgLeaf2);
		diffs.toString();
		assertTrue(!diffs.noDiffs());
		vO12.setName("vLeafO1");
		diffs = fgLeaf1.getFactorGraphDiffsByName(fgLeaf2);
		assertTrue(diffs.noDiffs());

		//Name change, change back, factor
		fLeaf2.setName("x");
		diffs = fgLeaf1.getFactorGraphDiffsByName(fgLeaf2);
		diffs.toString();
		assertTrue(!diffs.noDiffs());
		fLeaf2.setName("fLeaf");
		diffs = fgLeaf1.getFactorGraphDiffsByName(fgLeaf2);
		assertTrue(diffs.noDiffs());
		
		//Name change, change back, multiple
		vB12.setName("1");
		vO12.setName("2");
		vO22.setName("3");
		fLeaf2.setName("4");
		diffs = fgLeaf1.getFactorGraphDiffsByName(fgLeaf2);
		diffs.toString();
		assertTrue(!diffs.noDiffs());
		vB12.setName("vLeafB1");
		vO12.setName("vLeafO1");
		vO22.setName("vLeafO2");
		fLeaf2.setName("fLeaf");
		diffs = fgLeaf1.getFactorGraphDiffsByName(fgLeaf2);
		diffs.toString();
		assertTrue(diffs.noDiffs());
	}

	public void leaf_connectivity(FactorGraph fgLeaf1, FactorGraph fgLeaf2,
		@Nullable Discrete vB1, @Nullable Discrete vB2)
	{
		check(fgLeaf1, fgLeaf2, true, "leaf_connectivity 1");
		check(fgLeaf1.copyRoot(), fgLeaf2.copyRoot(), true, "leaf_connectivity 1 root");
		XorDelta xorFF = new XorDelta();
		//add factor to one to difference
		Discrete vLeafO3 = new Discrete(0.0, 1.0);
		vLeafO3.setName("vLeafO3");
		Factor fLeafNew11
			= fgLeaf1.addFactor(xorFF,
								vB1,
								vLeafO3);
		fLeafNew11.setName("fLeafNew1");
		check(fgLeaf1, fgLeaf2, false, "leaf_connectivity 2");
		
		//add factor to other to same
		vLeafO3 = new Discrete(0.0, 1.0);
		vLeafO3.setName("vLeafO3");
		Factor fLeafNew12
			= fgLeaf2.addFactor(xorFF,
								vB2,
								vLeafO3);
		fLeafNew12.setName("fLeafNew1");
		check(fgLeaf1, fgLeaf2, true, "leaf_connectivity 3");
		check(fgLeaf1.copyRoot(), fgLeaf2.copyRoot(), true, "leaf_connectivity 3 root");
		
		//same factor name, different number of variables should still be different
		Discrete vLeafO5 = new Discrete(0.0, 1.0);
		Discrete vLeafO6 = new Discrete(0.0, 1.0);
		vLeafO5.setName("vLeafO5");
		vLeafO6.setName("vLeafO6");
		Factor fLeafNew21
			= fgLeaf1.addFactor(xorFF,
								vB1,
								vLeafO5,
								vLeafO6);
		fLeafNew21.setName("fLeafNew2");
		check(fgLeaf1, fgLeaf2, false, "leaf_connectivity 4");
		
		vLeafO5 = new Discrete(0.0, 1.0);
		vLeafO6 = new Discrete(0.0, 1.0);
		vLeafO5.setName("vLeafO5");
		vLeafO6.setName("vLeafO6");
		Factor fLeafNew22
			= fgLeaf2.addFactor(xorFF,
								vB2,
								vLeafO5);
		fLeafNew22.setName("fLeafNew2");
		check(fgLeaf1, fgLeaf2, false, "leaf_connectivity 5");
		
		
		//fix up both
		fLeafNew22.setName("fLeafNew2x");
		Factor fix
			= fgLeaf2.addFactor(xorFF,
							vB2,
							vLeafO5,
							vLeafO6);
		fix.setName("fLeafNew2");
		
		vLeafO5 = (Discrete) fgLeaf1.getObjectByName("vLeafO5");
		fix
			= fgLeaf1.addFactor(xorFF,
						vB1,
						vLeafO5);
		fix.setName("fLeafNew2x");
		check(fgLeaf1, fgLeaf2, true, "leaf_connectivity 6");
		check(fgLeaf1.copyRoot(), fgLeaf2.copyRoot(), true, "leaf_connectivity 6 root");

		//add graph to one, get difference
		FactorGraph fgOther = Helpers.MakeSimpleGraph("Other");
		fgLeaf1.addGraph(fgOther, vB1);
		check(fgLeaf1, fgLeaf2, false, "leaf_connectivity 7");
	
		//add graph to other, get same
		fgLeaf2.addGraph(fgOther, vB2);
		check(fgLeaf1, fgLeaf2, true, "leaf_connectivity 8");
		check(fgLeaf1.copyRoot(), fgLeaf2.copyRoot(), true, "leaf_connectivity 8 root");

		//add different 2 graphs with different connectivity, same name
		FactorGraph fgOtherA = Helpers.MakeSimpleGraph("Other2");
		Factor fOtherA = fgOtherA.addFactor(xorFF,
				 (Discrete) fgOtherA.getObjectByName("vOther2B1"),
				 (Discrete) fgOtherA.getObjectByName("vOther2O1"));
		fOtherA.setName("fOtherA");
		
		fgLeaf1.addGraph(fgOtherA, vB1);
		check(fgLeaf1, fgLeaf2, false, "leaf_connectivity 9");
		
		FactorGraph fgOtherB = Helpers.MakeSimpleGraph("Other2");
		Factor fOtherB = fgOtherB.addFactor(xorFF,
				 (Discrete) fgOtherB.getObjectByName("vOther2B1"),
				 (Discrete) fgOtherB.getObjectByName("vOther2O1"),
				 (Discrete) fgOtherB.getObjectByName("vOther2O2"));
		fOtherB.setName("fOtherA");

		fgLeaf2.addGraph(fgOtherB, vB2);
		check(fgLeaf1, fgLeaf2, false, "leaf_connectivity 10");
		
		//fix up
		FactorGraph ownedFgOtherB = requireNonNull((FactorGraph) fgLeaf2.getObjectByName("Other2"));
		ownedFgOtherB.setName("OtherB");
		Factor ownedFOtherB = requireNonNull((Factor) ownedFgOtherB.getObjectByName("fOtherA"));
		ownedFOtherB.setName("fOtherB");
		fgLeaf2.addGraph(fgOtherA, vB2);
		
		fgOtherB.setName("OtherB");
		fOtherB.setName("fOtherB");
		fgLeaf1.addGraph(fgOtherB, vB1);
		
		check(fgLeaf1, fgLeaf2, true, "leaf_connectivity 11");
		check(fgLeaf1.copyRoot(), fgLeaf2.copyRoot(), true, "leaf_connectivity 11 root");
		
	}
	
	public void mid_connectivity(FactorGraph fgMid1,
								 FactorGraph fgMid2,
								 Discrete vB1,
								 Discrete vB2,
								 String boundaryName,
								 String leafName)
	{
		FactorGraph fgLeaf1 = requireNonNull((FactorGraph)fgMid1.getObjectByName(leafName));
		FactorGraph fgLeaf2 = requireNonNull((FactorGraph)fgMid2.getObjectByName(leafName));
		leaf_connectivity(fgLeaf1,
						  fgLeaf2,
						  (Discrete) fgMid1.getObjectByName(boundaryName),
						  (Discrete) fgMid2.getObjectByName(boundaryName));
		leaf_connectivity(fgMid1,
						  fgMid2,
						  vB1,
						  vB2);
	}
	
	@Test
	public void test_simple_one_level_compare_connectivity()
	{
		FactorGraph[] fgs1
			= Helpers.MakeSimpleThreeLevelGraphs();
		FactorGraph[] fgs2
			= Helpers.MakeSimpleThreeLevelGraphs();
		
		FactorGraph fgLeaf1 = fgs1[fgs1.length - 1];
		FactorGraph fgLeaf2 = fgs2[fgs2.length - 1];
		String boundaryName = "vLeafB1";
		leaf_connectivity(fgLeaf1, fgLeaf2,
						  (Discrete) fgLeaf1.getObjectByName(boundaryName),
						  (Discrete) fgLeaf2.getObjectByName(boundaryName));
	}
	
	@Test
	public void test_simple_two_level_compare_connectivity()
	{
		FactorGraph[] fgs1
			= Helpers.MakeSimpleThreeLevelGraphs();
		FactorGraph[] fgs2
			= Helpers.MakeSimpleThreeLevelGraphs();
	
		FactorGraph fgMid1 = fgs1[fgs1.length - 2];
		FactorGraph fgMid2 = fgs2[fgs2.length - 2];

		mid_connectivity(fgMid1,
						 fgMid2,
						 requireNonNull((Discrete)fgMid1.getObjectByName("vMidB1")),
						 requireNonNull((Discrete)fgMid2.getObjectByName("vMidB1")),
						 "vMidO2",
						 "Leaf");
		
	}

	@Test
	public void test_simple_three_level_compare_connectivity()
	{
		FactorGraph[] fgs1
			= Helpers.MakeSimpleThreeLevelGraphs();
		FactorGraph[] fgs2
			= Helpers.MakeSimpleThreeLevelGraphs();
		for(int i = 0; i < fgs1.length; ++i)
		{
			compareToSelf(fgs1[i]);
			compareToSelf(fgs2[i]);
		}

		FactorGraph fgRoot1 = fgs1[0];
		FactorGraph fgRoot2 = fgs2[0];
		
		FactorGraph fgMid1 = requireNonNull((FactorGraph)fgRoot1.getObjectByName("Mid"));
		FactorGraph fgMid2 = requireNonNull((FactorGraph)fgRoot2.getObjectByName("Mid"));
		
		String boundaryName = "vRootO2";
		mid_connectivity(fgMid1, fgMid2,
						  requireNonNull((Discrete) fgRoot1.getObjectByName(boundaryName)),
						  requireNonNull((Discrete) fgRoot2.getObjectByName(boundaryName)),
						  "vMidO2",
						  "Leaf");
		
		fgs1 = Helpers.MakeSimpleThreeLevelGraphs();
		fgs2 = Helpers.MakeSimpleThreeLevelGraphs();
		fgRoot1 = fgs1[0];
		fgRoot2 = fgs2[0];

		String leafName = "Mid.Leaf";
		boundaryName = "Mid.vMidO2";
		FactorGraph fgLeaf1 = requireNonNull((FactorGraph)fgRoot1.getObjectByName(leafName));
		FactorGraph fgLeaf2 = requireNonNull((FactorGraph)fgRoot2.getObjectByName(leafName));
		Discrete vB1 = (Discrete)fgRoot1.getObjectByName(boundaryName);
		Discrete vB2 = (Discrete)fgRoot2.getObjectByName(boundaryName);
		leaf_connectivity(fgLeaf1,
						  fgLeaf2,
						  vB1,
						  vB2);

		leafName = "Mid";
		boundaryName = "vRootO2";
		fgMid1 = requireNonNull((FactorGraph)fgRoot1.getObjectByName(leafName));
		fgMid2 = requireNonNull((FactorGraph)fgRoot2.getObjectByName(leafName));
		vB1 = (Discrete)fgRoot1.getObjectByName(boundaryName);
		vB2 = (Discrete)fgRoot2.getObjectByName(boundaryName);
		leaf_connectivity(fgMid1,
						  fgMid2,
						  vB1,
						  vB2);

		
		leaf_connectivity(fgRoot1,
		 		 fgRoot2,
				 (Discrete)fgRoot1.getObjectByName(boundaryName),
				 (Discrete)fgRoot2.getObjectByName(boundaryName));
	
	}
	
	@Test
	public void test_simple_three_level_compare()
	{
		FactorGraph[] fgs1
			= Helpers.MakeSimpleThreeLevelGraphs();
		FactorGraph[] fgs2
			= Helpers.MakeSimpleThreeLevelGraphs();
		
		for(int i = 0; i < fgs1.length; ++i)
		{
			compareToSelf(fgs1[i]);
			compareToSelf(fgs2[i]);
		}
		
		FactorGraph fgLeaf1 		= fgs1[2];
		//FactorGraph fgLeaf2 		= fgs2[2];
		FactorGraph fgMid1 			= fgs1[1];
		FactorGraph fgMid2 			= fgs2[1];
		FactorGraph fgRoot1 		= fgs1[0];
		FactorGraph fgRoot2 		= fgs2[0];
		
		FactorGraph fgMid1Leaf 		= requireNonNull((FactorGraph) fgMid1.getObjectByName("Leaf"));
		FactorGraph fgMid2Leaf 		= requireNonNull((FactorGraph) fgMid2.getObjectByName("Leaf"));
		FactorGraph fgRoot1Mid 		= requireNonNull((FactorGraph) fgRoot1.getObjectByName("Mid"));
		//FactorGraph fgRoot2Mid 		= (FactorGraph) fgRoot2.getObjectByName("Mid");
		FactorGraph fgRoot1Leaf 	= requireNonNull((FactorGraph) fgRoot1.getObjectByName("Mid.Leaf"));
		//FactorGraph fgRoot2Leaf 	= (FactorGraph) fgRoot2.getObjectByName("Mid.Leaf");
		FactorGraph fgRoot1MidLeaf 	= requireNonNull((FactorGraph) fgRoot1Mid.getObjectByName("Leaf"));
		//FactorGraph fgRoot2MidLeaf 	= (FactorGraph) fgRoot2Mid.getObjectByName("Leaf");
		
		check(fgLeaf1, 	fgMid1Leaf, 	false, 	"3compare 1");
		check(fgLeaf1, 	fgRoot1Leaf, 	false, 	"3compare 1");
		check(fgLeaf1, 	fgRoot1MidLeaf, false, 	"3compare 1");
		check(fgMid1, 	fgRoot1Mid, 	false, 	"3compare 2");
		check(fgRoot1Leaf, 	fgRoot1MidLeaf, 	true, 	"3compare 2");
			
		Factor fRoot = requireNonNull((Factor) fgRoot1.getObjectByName("fRoot"));
		Factor fMid = requireNonNull((Factor) fgMid1.getObjectByName("fMid"));
		Factor fMidLeaf = requireNonNull((Factor) fgMid1.getObjectByName("Mid.Leaf.fLeaf"));
		Factor fLeaf = requireNonNull((Factor) fgMid1Leaf.getObjectByName("fLeaf"));
		
		Discrete vMidB1 = requireNonNull((Discrete) fgMid1.getObjectByName("vMidB1"));
		Discrete vMidO1 = requireNonNull((Discrete) fgMid1.getObjectByName("vMidO1"));
		Discrete vMidO2 = (Discrete) fgMid1.getObjectByName("vMidO2");

		Discrete vRootB1 = (Discrete) fgRoot1.getObjectByName("vRootB1");
		Discrete vRootO1 = (Discrete) fgRoot1.getObjectByName("vRootO1");
		Discrete vRootO2 = (Discrete) fgRoot1.getObjectByName("vRootO2");
		
		Discrete vMidLeafO1 = requireNonNull((Discrete) fgMid1.getObjectByName("Mid.Leaf.vLeafO1"));
		Discrete vMidLeafO2 = (Discrete) fgMid1.getObjectByName("Mid.Leaf.vLeafO2");
		Discrete vLeafO1 = requireNonNull((Discrete) fgMid1Leaf.getObjectByName("vLeafO1"));
		Discrete vLeafO2 = (Discrete) fgMid1Leaf.getObjectByName("vLeafO2");
		
		checkNameChange(fMid,
				fgMid1, fgMid2,
				null, null);

		checkNameChange(vMidB1,
				fgMid1, fgMid2,
				null, null);

		checkNameChange(vMidO1,
				fgMid1, fgMid2,
				null, null);

		checkNameChange(vMidO1, vMidO2, fgMid1, fgMid2, null, null);
		
		checkNameChange(fMidLeaf,
						fgMid1Leaf, fgMid2Leaf,
						fgMid1, fgMid2);

		checkNameChange(fLeaf,
				fgMid1Leaf, fgMid2Leaf,
				fgMid1, fgMid2);
	
		checkNameChange(vMidLeafO1,
				fgMid1Leaf, fgMid2Leaf,
				fgMid1, fgMid2);
		
		checkNameChange(vMidLeafO1, vMidLeafO2,
						fgMid1, fgMid2, null, null);
				
		checkNameChange(vLeafO1,
				fgMid1Leaf, fgMid2Leaf,
				fgMid1, fgMid2);
		
		
		checkNameChange(vLeafO1, vLeafO2,
				fgMid1Leaf, fgMid2Leaf,
				fgMid1, fgMid2);

		fRoot = requireNonNull((Factor) fgRoot1.getObjectByName("fRoot"));
		fMid = requireNonNull((Factor) fgRoot1.getObjectByName("Mid.fMid"));
		fLeaf = requireNonNull((Factor) fgRoot1.getObjectByName("Mid.Leaf.fLeaf"));
		
		vRootB1 = requireNonNull((Discrete) fgRoot1.getObjectByName("vRootB1"));
		vRootO1 = requireNonNull((Discrete) fgRoot1.getObjectByName("vRootO1"));
		vRootO2 = requireNonNull((Discrete) fgRoot1.getObjectByName("vRootO2"));

		vMidO1 = requireNonNull((Discrete) fgRoot1.getObjectByName("Mid.vMidO1"));
		vMidO2 = requireNonNull((Discrete) fgRoot1.getObjectByName("Mid.vMidO2"));
		
		vLeafO1 = requireNonNull((Discrete) fgRoot1.getObjectByName("Mid.Leaf.vLeafO1"));
		vLeafO2 = requireNonNull((Discrete) fgRoot1.getObjectByName("Mid.Leaf.vLeafO2"));

		checkNameChange(fgRoot1, 	fgRoot1, fgRoot2, null, null);
		checkNameChange(fgRoot1Mid, fgRoot1, fgRoot2, null, null);
		checkNameChange(fgRoot1Leaf,fgRoot1, fgRoot2, null, null);
		checkNameChange(fRoot, 		fgRoot1, fgRoot2, null, null);
		checkNameChange(fMid, 		fgRoot1, fgRoot2, null, null);
		checkNameChange(fLeaf, 		fgRoot1, fgRoot2, null, null);
		checkNameChange(vRootB1, 	fgRoot1, fgRoot2, null, null);
		checkNameChange(vRootO1, 	fgRoot1, fgRoot2, null, null);
		checkNameChange(vRootO2, 	fgRoot1, fgRoot2, null, null);
		checkNameChange(vMidO1, 	fgRoot1, fgRoot2, null, null);
		checkNameChange(vMidO2, 	fgRoot1, fgRoot2, null, null);
		checkNameChange(vLeafO1, 	fgRoot1, fgRoot2, null, null);
		checkNameChange(vLeafO2, 	fgRoot1, fgRoot2, null, null);
	}

	@Test
	public void test_empty_compare()
	{
		FactorGraph fg1 = new FactorGraph();
		FactorGraph fg2 = new FactorGraph();
		
		FactorGraphDiffs fg1vsfg1Name
			= fg1.getFactorGraphDiffsByName(fg1);
		FactorGraphDiffs fg1vsfg2Name
			= fg1.getFactorGraphDiffsByName(fg2);
		FactorGraphDiffs fg1vsfg1UUID
			= fg1.getFactorGraphDiffsByUUID(fg1);
		FactorGraphDiffs fg1vsfg2UUID
			= fg1.getFactorGraphDiffsByUUID(fg2);
		
		assertTrue(fg1vsfg1Name.noDiffs());
		assertTrue(!fg1vsfg2Name.noDiffs());
		assertTrue(fg1vsfg1UUID.noDiffs());
		assertTrue(!fg1vsfg2UUID.noDiffs());
		
		fg1.setName("x");
		fg2.setName("x");
		fg1vsfg1Name
			= fg1.getFactorGraphDiffsByName(fg1);
		fg1vsfg2Name
			= fg1.getFactorGraphDiffsByName(fg2);
		fg1vsfg1UUID
			= fg1.getFactorGraphDiffsByUUID(fg1);
		fg1vsfg2UUID
			= fg1.getFactorGraphDiffsByUUID(fg2);

		assertTrue(fg1vsfg1Name.noDiffs());
		assertTrue(fg1vsfg2Name.noDiffs());
		assertTrue(fg1vsfg1UUID.noDiffs());
		assertTrue(!fg1vsfg2UUID.noDiffs());

		fg2.setName("y");
		fg1vsfg1Name
			= fg1.getFactorGraphDiffsByName(fg1);
		fg1vsfg2Name
			= fg1.getFactorGraphDiffsByName(fg2);
		fg1vsfg1UUID
			= fg1.getFactorGraphDiffsByUUID(fg1);
		fg1vsfg2UUID
			= fg1.getFactorGraphDiffsByUUID(fg2);

		assertTrue(fg1vsfg1Name.noDiffs());
		assertTrue(!fg1vsfg2Name.noDiffs());
		assertTrue(fg1vsfg1UUID.noDiffs());
		assertTrue(!fg1vsfg2UUID.noDiffs());
		
	}
}
