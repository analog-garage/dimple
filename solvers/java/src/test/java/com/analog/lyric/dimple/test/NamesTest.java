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

import java.util.ArrayList;
import java.util.UUID;

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
import com.analog.lyric.util.misc.Nullable;

public class NamesTest {

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

	public void testObjectName(	INameable nameable,
								String expectedName,
								@Nullable String expectedExplicitName,
								String expectedQualifiedName)
	{
		UUID uuidGot = nameable.getUUID();
		String nameGot = nameable.getName();
		String explcititNameGot = nameable.getExplicitName();
		String qualifiedNameGot = nameable.getQualifiedName();
		assertTrue(uuidGot != null);
		assertTrue(nameGot.equals(expectedName));
		if(expectedExplicitName == null)
		{
			assertTrue(explcititNameGot == null);
		}
		else
		{
			assertTrue(requireNonNull(explcititNameGot).equals(expectedExplicitName));
		}
		assertTrue(qualifiedNameGot.equals(expectedQualifiedName));
		//no guarantee on what these are, they should just be something
		assertTrue(nameable.getLabel().length() != 0);
		assertTrue(nameable.getQualifiedLabel().length() != 0);
	}
	
	@Test
	public void test_names()
	{
		//Here we are only testing that
		//name functions pass through to solver correctly,
		//not their functionality, which is
		//tested in solver's names test
		
		Discrete v1 = new Discrete(0.0, 1.0);
		Discrete v2 = new Discrete(0.0, 1.0);
		testObjectName(v1, v1.getUUID().toString(), null, v1.getUUID().toString());
		testObjectName(v2, v2.getUUID().toString(), null, v2.getUUID().toString());

		FactorGraph fg = new FactorGraph();
		testObjectName(fg, fg.getUUID().toString(), null, fg.getUUID().toString());
		assertTrue(fg.toString().length() != 0);
		assertTrue(fg.getNodeString().length() != 0);
		assertTrue(fg.getAdjacencyString().length() != 0);
		assertTrue(fg.getFullString().length() != 0);
		
		Factor f = fg.addFactor(new XorDelta(), v1, v2);

		testObjectName(f, f.getUUID().toString(), null, fg.getUUID().toString() + "." + f.getUUID().toString());
		testObjectName(v1, v1.getUUID().toString(), null, fg.getUUID().toString() + "." + v1.getUUID().toString());
		testObjectName(v2, v2.getUUID().toString(), null, fg.getUUID().toString() + "." + v2.getUUID().toString());
	
		v1.setName("v1");
		v2.setName("v2");
		f.setName("f");
		fg.setName("fg");

		testObjectName(fg, "fg", "fg", "fg");
		testObjectName(f, "f", "f", "fg.f");
		testObjectName(v1, "v1", "v1", "fg.v1");
		testObjectName(v2, "v2", "v2", "fg.v2");
	}
	
	@SuppressWarnings("null")
	public void test_parentGraphNameStuff()
	{
		//3 new variables
		int[] variableIds = new int[]
		{
		};
		Discrete variables[] = new Discrete[]
		{
			new Discrete(0.0, 1.0),
			new Discrete(0.0, 1.0),
			new Discrete(0.0, 1.0)
		};

		//simple checks on variables
		for(int i = 0; i < variableIds.length; ++i)
		{
			assertTrue(variables[i].getName().equals(variables[i].getUUID().toString()));
		}
		
		//new graph
		FactorGraph fg = new FactorGraph();
	
		//simple checks on graph
		assertTrue(fg.getName().equals(fg.getUUID().toString()));
		
		Factor fn = fg.addFactor(new XorDelta(), (Object[])variables);
		assertTrue(fn.getName().equals(fn.getUUID().toString()));

				//Check UUIDs
		Factor fnNotFound = (Factor) fg.getObjectByName(fn.getExplicitName());
		Factor fnFound = (Factor) fg.getObjectByName(fn.getName());
		Factor fnFoundUUID = (Factor) fg.getObjectByUUID(fn.getUUID());
		assertTrue(fnNotFound == null);
		assertTrue(fnFound == fn);
		assertTrue(fnFoundUUID == fn);
		
		for(int i = 0; i < variableIds.length; ++i)
		{
			Discrete vNotFound = (Discrete) fg.getObjectByName(variables[i].getExplicitName());
			Discrete vFound = (Discrete) fg.getObjectByName(variables[i].getName());
			Discrete vFoundUUID = (Discrete) fg.getObjectByUUID(variables[i].getUUID());
			assertTrue(vNotFound == null);
			assertTrue(vFound == variables[i]);
			assertTrue(vFoundUUID == variables[i]);
		}
		
		
		//verify can add name
		//also test qualified names
		String fgNameSet = "fgName";
		fg.setName(fgNameSet);
		assertTrue(fg.getName().equals(fgNameSet));
		assertTrue(fg.getExplicitName().equals(fgNameSet));
		assertTrue(fg.getQualifiedName().equals(fg.getName()));
		
//System.out.println(fg.fullString());
		String functionBaseName = "fName";
		fn.setName(functionBaseName);
		assertTrue(fn.getName().equals(functionBaseName));
		assertTrue(fn.getExplicitName().equals(functionBaseName));

		fnNotFound = (Factor) fg.getObjectByName(fn.getUUID().toString());
		fnFound = (Factor) fg.getObjectByName(fn.getName());
		Factor fnFoundQualified = (Factor) fg.getObjectByName(fn.getQualifiedName());
		assertTrue(fnNotFound == null);
		assertTrue(fnFound == fn);
		assertTrue(fnFoundQualified == fn);
		
		String VariableName = "vName";
		for(int i = 0; i < variableIds.length; ++i)
		{
			String VariableBaseName = VariableName + Integer.toString(i);
			variables[i].setName(VariableBaseName);
			assertTrue(variables[i].getName().equals(VariableBaseName));
			String qualifiedName = fg.getName() + "." + VariableBaseName;
			String variableQualifiedName = variables[i].getQualifiedName();
			assertTrue(variableQualifiedName.equals(qualifiedName));
			
			Discrete vNotFound = (Discrete) fg.getObjectByName(variables[i].getUUID().toString());
			Discrete vFound = (Discrete) fg.getObjectByName(variables[i].getName());
			Discrete vFoundQualified = (Discrete) fg.getObjectByName(variables[i].getQualifiedName());
			assertTrue(vNotFound == null);
			assertTrue(vFound == variables[i]);
			assertTrue(vFoundQualified == variables[i]);
		}
		
		//invalid names
		boolean excepted = false;
		try{fg.setName("aaa.");}catch(Exception e){excepted = true;}
		assertTrue(excepted);
		
		excepted = false;
		try{fn.setName("aaa.");}catch(Exception e){excepted = true;}
		assertTrue(excepted);

		excepted = false;
		try{variables[0].setName("aaa.");}catch(Exception e){excepted = true;}
		assertTrue(excepted);
		
		//Change names
		//Factor
		String functionName2 = functionBaseName + "2";
		fn.setName(functionName2);
		assertTrue(fn.getName().equals(functionName2));
		assertTrue(fn.getExplicitName().equals(functionName2));

		fnNotFound = (Factor) fg.getObjectByName(fn.getUUID().toString());
		fnFound = (Factor) fg.getObjectByName(fn.getName());
		fnFoundQualified = (Factor) fg.getObjectByName(fn.getQualifiedName());
		assertTrue(fnNotFound == null);
		assertTrue(fnFound == fn);
		assertTrue(fnFoundQualified == fn);
		
		//fg
		String fgName2 = fgNameSet + "2";
		fg.setName(fgName2);
		assertTrue(fgName2.equals(fg.getName()));
		assertTrue(fgName2.equals(fg.getQualifiedName()));
		assertTrue(fgName2.equals(fg.getExplicitName()));
		
		//variable
		String vName2 = variables[0].getName() + "2";
		variables[0].setName(vName2);
		Discrete vNotFound = (Discrete) fg.getObjectByName(variables[0].getUUID().toString());
		Discrete vFound = (Discrete) fg.getObjectByName(variables[0].getName());
		Discrete vFoundQualified = (Discrete) fg.getObjectByName(variables[0].getQualifiedName());
		assertTrue(vNotFound == null);
		assertTrue(vFound == variables[0]);
		assertTrue(vFoundQualified == variables[0]);
		
		//no more names
		fn.setName(null);
		fg.setName(null);
		variables[0].setName(null);

		fnNotFound = (Factor) fg.getObjectByName(fn.getExplicitName());
		fnFound = (Factor) fg.getObjectByName(fn.getName());
		fnFoundUUID = (Factor) fg.getObjectByUUID(fn.getUUID());
		assertTrue(fg.getName().equals(fg.getUUID().toString()));
		assertTrue(fg.getExplicitName() == null);
		assertTrue(fnNotFound == null);
		assertTrue(fnFound == fn);
		assertTrue(fnFoundUUID == fn);
		
		vNotFound = (Discrete) fg.getObjectByName(variables[0].getExplicitName());
		vFound = (Discrete) fg.getObjectByName(variables[0].getName());
		Discrete vFoundUUID = (Discrete) fg.getObjectByUUID(variables[0].getUUID());
		assertTrue(vNotFound == null);
		assertTrue(vFound == variables[0]);
		assertTrue(vFoundUUID == variables[0]);
	}

	@Test
	public void test_SearchByUUID()
	{
		Discrete vRootB1 = new Discrete(0.0, 1.0);
		Discrete vRootO1 = new Discrete(0.0, 1.0);
		Discrete vRootO2 = new Discrete(0.0, 1.0);

		Discrete vMidB1 = new Discrete(0.0, 1.0);
		Discrete vMidO1 = new Discrete(0.0, 1.0);
		Discrete vMidO2 = new Discrete(0.0, 1.0);

		Discrete vLeafB1 = new Discrete(0.0, 1.0);
		Discrete vLeafO1 = new Discrete(0.0, 1.0);
		Discrete vLeafO2 = new Discrete(0.0, 1.0);

		FactorGraph fgRoot = new FactorGraph(vRootB1);
		FactorGraph fgMid  = new FactorGraph(vMidB1);
		FactorGraph fgLeaf = new FactorGraph(vLeafB1);

		XorDelta xorFF = new XorDelta();
		Factor fRoot = fgRoot.addFactor(xorFF, vRootB1, vRootO1, vRootO2);
		Factor fMid  = fgMid.addFactor( xorFF, vMidB1,  vMidO1,  vMidO2);
		Factor fLeaf = fgLeaf.addFactor(xorFF, vLeafB1, vLeafO1, vLeafO2);
		
		vRootB1.setName("vRootB1");
		vRootO1.setName("vRootO1");
		vRootO2.setName("vRootO2");
		vMidB1.setName("vMidB1");
		vMidO1.setName("vMidO1");
		vMidO2.setName("vMidO2");
		vLeafB1.setName("vLeafB1");
		vLeafO1.setName("vLeafO1");
		vLeafO2.setName("vLeafO2");
		fRoot.setName("fRoot");
		fMid.setName("fMid");
		fLeaf.setName("fLeaf");
		fgRoot.setName("Root");
		fgMid.setName("Mid");
		fgLeaf.setName("Leaf");
							
		//One sub graph
		fgMid.addGraph(fgLeaf, new Discrete[]{vMidO2});
		
		//Two sub graphs
		fgRoot.addGraph(fgMid, new Discrete[]{vRootO2});
		fgMid = requireNonNull((FactorGraph)fgRoot.getObjectByName("Mid"));
		fgLeaf = requireNonNull((FactorGraph)fgRoot.getObjectByName("Mid.Leaf"));

		
		{
			ArrayList<INameable> nameables = new ArrayList<INameable>();
			
			nameables.add((INameable)fgRoot.getObjectByName("vRootB1"));
			nameables.add((INameable)fgRoot.getObjectByName("vRootB1"));
			nameables.add((INameable)fgRoot.getObjectByName("vRootB1"));
			nameables.add((INameable)fgRoot.getObjectByName("Mid"));
			nameables.add((INameable)fgRoot.getObjectByName("fRoot"));

			for(INameable named : nameables)
			{
				INameable byUUID = (INameable) fgRoot.getObjectByUUID(named.getUUID());
				assertTrue(byUUID != null);
				assertTrue(byUUID.getUUID() == named.getUUID());
				assertTrue(byUUID.getQualifiedName().equals(named.getQualifiedName()));
			}
		}
		
		{
			ArrayList<INameable> nameables = new ArrayList<INameable>();
			
			nameables.add((INameable)fgRoot.getObjectByName("Root.Mid.vMidO1"));
			nameables.add((INameable)fgRoot.getObjectByName("Root.Mid.vMidO1"));
			nameables.add((INameable)fgRoot.getObjectByName("Root.Mid.Leaf"));
			nameables.add((INameable)fgRoot.getObjectByName("Root.Mid.fMid"));

			for(INameable named : nameables)
			{
				INameable byUUID = (INameable) fgMid.getObjectByUUID(named.getUUID());
				assertTrue(byUUID != null);
				assertTrue(byUUID.getUUID() == named.getUUID());
				assertTrue(byUUID.getQualifiedName().equals(named.getQualifiedName()));
			}
		}
		{
			ArrayList<INameable> nameables = new ArrayList<INameable>();

			nameables.add((INameable)fgRoot.getObjectByName("Mid.Leaf.fLeaf"));
			nameables.add((INameable)fgRoot.getObjectByName("Root.Mid.Leaf.vLeafO1"));
			nameables.add((INameable)fgRoot.getObjectByName("Root.Mid.Leaf.vLeafO2"));
			
			for(INameable named : nameables)
			{
				INameable byUUID = (INameable) fgLeaf.getObjectByUUID(named.getUUID());
				assertTrue(byUUID != null);
				assertTrue(byUUID.getUUID() == named.getUUID());
				assertTrue(byUUID.getQualifiedName().equals(named.getQualifiedName()));
			}
		}
		
	}

	@SuppressWarnings("null")
	@Test
	public void test_NameNestingStuff()
	{
		Discrete vRootB1 = new Discrete(0.0, 1.0);
		Discrete vRootO1 = new Discrete(0.0, 1.0);
		Discrete vRootO2 = new Discrete(0.0, 1.0);

		Discrete vMidB1 = new Discrete(0.0, 1.0);
		Discrete vMidO1 = new Discrete(0.0, 1.0);
		Discrete vMidO2 = new Discrete(0.0, 1.0);

		Discrete vLeafB1 = new Discrete(0.0, 1.0);
		Discrete vLeafO1 = new Discrete(0.0, 1.0);
		Discrete vLeafO2 = new Discrete(0.0, 1.0);

		FactorGraph fgRoot = new FactorGraph(vRootB1);
		FactorGraph fgMid  = new FactorGraph(vMidB1);
		FactorGraph fgLeaf = new FactorGraph(vLeafB1);
		
		XorDelta xorFF = new XorDelta();

		Factor fRoot = fgRoot.addFactor(xorFF, vRootB1, vRootO1, vRootO2);
		Factor fMid  = fgMid.addFactor( xorFF, vMidB1,  vMidO1,  vMidO2);
		Factor fLeaf = fgLeaf.addFactor(xorFF, vLeafB1, vLeafO1, vLeafO2);
		
		//Checks UUID names, parentage
		assertTrue(vRootB1.getName().equals(vRootB1.getUUID().toString()));
		assertTrue(vRootO1.getName().equals(vRootO1.getUUID().toString()));
		assertTrue(vRootO2.getName().equals(vRootO2.getUUID().toString()));
		assertTrue(vMidB1.getName().equals(vMidB1.getUUID().toString()));
		assertTrue(vMidO1.getName().equals(vMidO1.getUUID().toString()));
		assertTrue(vMidO2.getName().equals(vMidO2.getUUID().toString()));
		assertTrue(vLeafB1.getName().equals(vLeafB1.getUUID().toString()));
		assertTrue(vLeafO1.getName().equals(vLeafO1.getUUID().toString()));
		assertTrue(vLeafO2.getName().equals(vLeafO2.getUUID().toString()));
		
		assertTrue(fRoot.getName().equals(fRoot.getUUID().toString()));
		assertTrue(fMid.getName().equals(fMid.getUUID().toString()));
		assertTrue(fLeaf.getName().equals(fLeaf.getUUID().toString()));
		
		assertTrue(((Discrete)fgRoot.getObjectByName(vRootB1.getName())).getUUID() == vRootB1.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName(vRootO1.getName())).getUUID() == vRootO1.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName(vRootO2.getName())).getUUID() == vRootO2.getUUID());
		assertTrue(((Discrete)fgMid.getObjectByName(vMidB1.getName())).getUUID() == vMidB1.getUUID());
		assertTrue(((Discrete)fgMid.getObjectByName(vMidO1.getName())).getUUID() == vMidO1.getUUID());
		assertTrue(((Discrete)fgMid.getObjectByName(vMidO2.getName())).getUUID() == vMidO2.getUUID());
		assertTrue(((Discrete)fgLeaf.getObjectByName(vLeafB1.getName())).getUUID() == vLeafB1.getUUID());
		assertTrue(((Discrete)fgLeaf.getObjectByName(vLeafO1.getName())).getUUID() == vLeafO1.getUUID());
		assertTrue(((Discrete)fgLeaf.getObjectByName(vLeafO2.getName())).getUUID() == vLeafO2.getUUID());

		assertTrue(((Factor)fgRoot.getObjectByName(fRoot.getName())).getUUID() == fRoot.getUUID());
		assertTrue(((Factor)fgMid.getObjectByName(fMid.getName())).getUUID() == fMid.getUUID());
		assertTrue(((Factor)fgLeaf.getObjectByName(fLeaf.getName())).getUUID() == fLeaf.getUUID());
		
		//set names
		vRootB1.setName("vRootB1");
		vRootO1.setName("vRootO1");
		vRootO2.setName("vRootO2");
		vMidB1.setName("vMidB1");
		vMidO1.setName("vMidO1");
		vMidO2.setName("vMidO2");
		vLeafB1.setName("vLeafB1");
		vLeafO1.setName("vLeafO1");
		vLeafO2.setName("vLeafO2");
		fRoot.setName("fRoot");
		fMid.setName("fMid");
		fLeaf.setName("fLeaf");
		fgRoot.setName("Root");
		fgMid.setName("Mid");
		fgLeaf.setName("Leaf");
		
		//Checks real names
		assertTrue(vRootB1.getName().equals("vRootB1"));
		assertTrue(vRootO1.getName().equals("vRootO1"));
		assertTrue(vRootO2.getName().equals("vRootO2"));
		assertTrue(vMidB1.getName().equals("vMidB1"));
		assertTrue(vMidO1.getName().equals("vMidO1"));
		assertTrue(vMidO2.getName().equals("vMidO2"));
		assertTrue(vLeafB1.getName().equals("vLeafB1"));
		assertTrue(vLeafO1.getName().equals("vLeafO1"));
		assertTrue(vLeafO2.getName().equals("vLeafO2"));
		
		assertTrue(fRoot.getName().equals("fRoot"));
		assertTrue(fMid.getName().equals("fMid"));
		assertTrue(fLeaf.getName().equals("fLeaf"));

		assertTrue(fgRoot.getName().equals("Root"));
		assertTrue(fgMid.getName().equals("Mid"));
		assertTrue(fgLeaf.getName().equals("Leaf"));
		
		assertTrue(((Discrete)fgRoot.getObjectByName(vRootB1.getName())).getUUID() == vRootB1.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName(vRootO1.getName())).getUUID() == vRootO1.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName(vRootO2.getName())).getUUID() == vRootO2.getUUID());
		assertTrue(((Discrete)fgMid.getObjectByName(vMidB1.getName())).getUUID() == vMidB1.getUUID());
		assertTrue(((Discrete)fgMid.getObjectByName(vMidO1.getName())).getUUID() == vMidO1.getUUID());
		assertTrue(((Discrete)fgMid.getObjectByName(vMidO2.getName())).getUUID() == vMidO2.getUUID());
		assertTrue(((Discrete)fgLeaf.getObjectByName(vLeafB1.getName())).getUUID() == vLeafB1.getUUID());
		assertTrue(((Discrete)fgLeaf.getObjectByName(vLeafO1.getName())).getUUID() == vLeafO1.getUUID());
		assertTrue(((Discrete)fgLeaf.getObjectByName(vLeafO2.getName())).getUUID() == vLeafO2.getUUID());

		assertTrue(((Factor)fgRoot.getObjectByName(fRoot.getName())).getUUID() == fRoot.getUUID());
		assertTrue(((Factor)fgMid.getObjectByName(fMid.getName())).getUUID() == fMid.getUUID());
		assertTrue(((Factor)fgLeaf.getObjectByName(fLeaf.getName())).getUUID() == fLeaf.getUUID());
					
		//One sub graph
		fgMid.addGraph(fgLeaf, new Discrete[]{vMidO2});
		
		//owned variables of parent same
		assertTrue(((Discrete)fgMid.getObjectByName("vMidB1")).getUUID() == vMidB1.getUUID());
		assertTrue(((Discrete)fgMid.getObjectByName("vMidO1")).getUUID() == vMidO1.getUUID());
		assertTrue(((Discrete)fgMid.getObjectByName("vMidO2")).getUUID() == vMidO2.getUUID());
		//new child
		fgLeaf = (FactorGraph) fgMid.getObjectByName("Leaf");
		assertTrue(fgLeaf != null);
		//child's nodes
		vLeafB1 = (Discrete) fgLeaf.getObjectByName("vLeafB1");
		vLeafO1 = (Discrete) fgLeaf.getObjectByName("vLeafO1");
		vLeafO2 = (Discrete) fgLeaf.getObjectByName("vLeafO2");
		fLeaf = (Factor) fgLeaf.getObjectByName("fLeaf");
		assertTrue(vLeafB1 == null &&
				   vLeafO1 != null &&
				   vLeafO2 != null &&
				   fLeaf != null);
		vLeafB1 = (Discrete) fgMid.getObjectByName("Mid.vMidO2");
		assertTrue(vLeafB1.getUUID() == vMidO2.getUUID());
		assertTrue(((FactorGraph)fgMid.getObjectByName("Mid.Leaf")).getUUID() == fgLeaf.getUUID());
		assertTrue(((FactorGraph)fgMid.getObjectByName("Leaf")).getUUID() == fgLeaf.getUUID());
		assertTrue(((Discrete)fgMid.getObjectByName("Mid.Leaf.vLeafO1")).getUUID() == vLeafO1.getUUID());
		assertTrue(((Discrete)fgMid.getObjectByName("Mid.Leaf.vLeafO2")).getUUID() == vLeafO2.getUUID());
		

		//Two sub graphs
		fgRoot.addGraph(fgMid, new Discrete[]{vRootO2});
		//new children
		FactorGraph fgMidOld = fgMid;
		fgMid = (FactorGraph) fgRoot.getObjectByName("Mid");
		fgLeaf = (FactorGraph) fgMid.getObjectByName("Leaf");
		//child's nodes
		vMidB1 = (Discrete) fgRoot.getObjectByName("vRootO2");
		vMidO1 = (Discrete) fgMid.getObjectByName("vMidO1");
		vMidO2 = (Discrete) fgMid.getObjectByName("vMidO2");
		vLeafB1 = (Discrete) fgMid.getObjectByName("vMidO2");
		vLeafO1 = (Discrete) fgLeaf.getObjectByName("vLeafO1");
		vLeafO2 = (Discrete) fgLeaf.getObjectByName("vLeafO2");
		fMid = (Factor) fgMid.getObjectByName("fMid");
		fLeaf = (Factor) fgLeaf.getObjectByName("fLeaf");

		//owned variables of parent same
		assertTrue(((Discrete)fgRoot.getObjectByName("vRootB1")).getUUID() == vRootB1.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName("vRootO1")).getUUID() == vRootO1.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName("vRootO2")).getUUID() == vRootO2.getUUID());
		
		assertTrue(fgMid != null &&
				   fgLeaf != null &&
				   vMidB1 != null &&
				   vMidO1 != null &&
				   vMidO2 != null &&
				   vLeafB1 != null &&
				   vLeafO1 != null &&
				   vLeafO2 != null &&
				   fMid != null &&
				   fLeaf != null);
		
		assertTrue(((FactorGraph)fgRoot.getObjectByName("Root.Mid")).getUUID() == fgMid.getUUID());
		assertTrue(((FactorGraph)fgRoot.getObjectByName("Root.Mid.Leaf")).getUUID() == fgLeaf.getUUID());

		assertTrue(((Factor)fgRoot.getObjectByName("Root.fRoot")).getUUID() == fRoot.getUUID());
		assertTrue(((Factor)fgRoot.getObjectByName("Root.Mid.fMid")).getUUID() == fMid.getUUID());
		assertTrue(((Factor)fgRoot.getObjectByName("Root.Mid.Leaf.fLeaf")).getUUID() == fLeaf.getUUID());
		
		assertTrue(((Discrete)fgRoot.getObjectByName("Root.vRootB1")).getUUID() == vRootB1.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName("Root.vRootO1")).getUUID() == vRootO1.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName("Root.vRootO2")).getUUID() == vRootO2.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName("Root.Mid.vMidO1")).getUUID() == vMidO1.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName("Root.Mid.vMidO2")).getUUID() == vMidO2.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName("Root.Mid.Leaf.vLeafO1")).getUUID() == vLeafO1.getUUID());
		assertTrue(((Discrete)fgRoot.getObjectByName("Root.Mid.Leaf.vLeafO2")).getUUID() == vLeafO2.getUUID());
		

		assertTrue(fgMid.getQualifiedName().equals("Root.Mid"));
		assertTrue(fgLeaf.getQualifiedName().equals("Root.Mid.Leaf"));

		assertTrue(fRoot.getQualifiedName().equals("Root.fRoot"));
		assertTrue(fMid.getQualifiedName().equals("Root.Mid.fMid"));
		assertTrue(fLeaf.getQualifiedName().equals("Root.Mid.Leaf.fLeaf"));
		
		assertTrue(vRootB1.getQualifiedName().equals("Root.vRootB1"));
		assertTrue(vRootO1.getQualifiedName().equals("Root.vRootO1"));
		assertTrue(vRootO2.getQualifiedName().equals("Root.vRootO2"));
		assertTrue(vMidO1.getQualifiedName().equals("Root.Mid.vMidO1"));
		assertTrue(vMidO2.getQualifiedName().equals("Root.Mid.vMidO2"));
		assertTrue(vLeafO1.getQualifiedName().equals("Root.Mid.Leaf.vLeafO1"));
		assertTrue(vLeafO2.getQualifiedName().equals("Root.Mid.Leaf.vLeafO2"));

		///Test setting names that already exist
		//graph
		//by add
		boolean excepted = false;
		try{fgRoot.addGraph(fgMidOld, new Discrete[]{vRootO2});}catch(Exception e){excepted = true;}
		assertTrue(excepted);
		
		//by change name
		fgMidOld.setName("Mid2");
		fgRoot.addGraph(fgMidOld, new Discrete[]{vRootO2});
		FactorGraph fgMid2 = (FactorGraph) fgRoot.getObjectByName("Mid2");
		excepted = false;
		try{fgMid2.setName("Mid");}catch(Exception e){excepted = true;}
		
		//variable - by change name
		excepted = false;
		try{vRootB1.setName("vRootO1");}catch(Exception e){excepted = true;}
		assertTrue(excepted);
		
		Discrete vRootO3 = new Discrete(0.0, 1.0);
		Discrete vRootO4 = new Discrete(0.0, 1.0);
		vRootO3.setName("vRootO3");
		vRootO4.setName("vRootO4");
		
		//Factor - by change name
		Factor fRoot2 = fgRoot.addFactor(xorFF, vRootO1, vRootO3, vRootO4);
		excepted = false;
		try{fRoot2.setName("fRoot");}catch(Exception e){excepted = true;}
		assertTrue(excepted);
		
		//variable via addFactor...
//TODO: this seems to leave  graph in bad state - ... print afterward crashes...
//		vRootO3.setName("vRootO1");
//		excepted = false;
//		try
//		{
//			int iRootF3  = _solver.createCustomFunc(iRootG,customFunctionName,new int[]{iRootO1, iRootO3, iRootO4});
//		}
//		catch(Exception e){excepted = true;}

			
		//found by new name
		vRootO3.setName("xxx");
		assertTrue(((Discrete)fgRoot.getObjectByName("xxx")).getUUID() == vRootO3.getUUID());
		//no name ok
		vRootO3.setName(null);
		assertTrue(fgRoot.getObjectByName("xxx") == null);
		assertTrue(vRootO3.getExplicitName() == null);
		assertTrue(((Discrete)fgRoot.getObjectByName(vRootO3.getName())).getUUID() == vRootO3.getUUID());
		
		//System.out.println(fgRoot.getFullString());
		

	}
	
	void typeByName(FactorGraph fg, @Nullable Object expected, boolean equals, String id, String type)
	{
		Object got = null;
		if(type.equals("variable"))
		{
			got = fg.getVariableByName(id);
		}
		else if(type.equals("factor"))
		{
			got = fg.getFactorByName(id);
			
		}
		else if(type.equals("graph"))
		{
			got = fg.getGraphByName(id);
		}
		if(got == null)
		{
			if(equals)
			{
				assertTrue(expected == null);
			}
			else
			{
				assertTrue(expected != null);
			}
		}
		else
		{
			if(equals)
			{
				assertTrue(got.equals(expected));
			}
			else
			{
				assertTrue(!got.equals(expected));
			}
		}
	}
	void typeByUUID(FactorGraph fg, @Nullable Object expected, boolean equals, UUID id, String type)
	{
		Object got = null;
		if(type.equals("variable"))
		{
			got = fg.getVariableByUUID(id);
		}
		else if(type.equals("factor"))
		{
			got = fg.getFactorByUUID(id);
			
		}
		else if(type.equals("graph"))
		{
			got = fg.getGraphByUUID(id);
		}
		if(got == null)
		{
			if(equals)
			{
				assertTrue(expected == null);
			}
			else
			{
				assertTrue(expected != null);
			}
		}
		else
		{
			if(equals)
			{
				assertTrue(got.equals(expected));
			}
			else
			{
				assertTrue(!got.equals(expected));
			}
		}
	}
	
	@Test
	public void test_getObjectByType()
	{
		Discrete variables[] = new Discrete[]
  		{
  			new Discrete(0.0, 1.0),
  			new Discrete(0.0, 1.0),
  			new Discrete(0.0, 1.0),
  		};
  		//new graph
  		Discrete[] dummyVars = new Discrete[0];
  		FactorGraph fg = new FactorGraph(dummyVars);
  		XorDelta xorFF = new XorDelta();
  		Factor fn = fg.addFactor(xorFF, variables);


		Discrete variablesSub[] = new Discrete[]
  		{
  			new Discrete(0.0, 1.0),
  			new Discrete(0.0, 1.0),
  			new Discrete(0.0, 1.0),
  		};
		
		//sub graph graph
		Discrete[] dummyVarsSub = new Discrete[0];
		FactorGraph fgSub = new FactorGraph(dummyVarsSub);
  		fg.addFactor(xorFF, variablesSub);
 		
		fgSub = fg.addGraph(fgSub, dummyVars);
		
		fg.setName("fg");
		fgSub.setName("fgSub");
		variables[0].setName("v0");
		variables[1].setName("v1");
		variables[2].setName("v2");
		fn.setName("fn");
		
		//missing name
		typeByName(fg, null, true, "v3", "variable");
		//wrong name
		typeByName(fg, variables[1], false, "v0", "variable");
		//wrong type
		typeByName(fg, null, true, "fn", "variable");
		typeByName(fg, null, true, "v0", "factor");
		typeByName(fg, null, true, "fn", "graph");
		
		//match
		typeByName(fg, variables[0], true, "v0", "variable");
		typeByName(fg, fn, true, "fn", "factor");
		typeByName(fg, fgSub, true, "fgSub", "graph");
		
		//missing UUID
		typeByUUID(fg, null, true, java.util.UUID.randomUUID(), "variable");
		//wrong UUID
		typeByUUID(fg, variables[1], false, variables[0].getUUID(), "variable");
		//wrong UUID
		typeByUUID(fg, null, true, fn.getUUID(), "variable");
		typeByUUID(fg, null, true, variables[0].getUUID(), "factor");
		typeByUUID(fg, null, true, fn.getUUID(), "graph");
		
		//UUID
		typeByUUID(fg, variables[0], true, variables[0].getUUID(), "variable");
		typeByUUID(fg, fn, true, fn.getUUID(), "factor");
		typeByUUID(fg, fgSub, true, fgSub.getUUID(), "graph");
	}
	
	public void test_getObjectByTypeMatlabProxy()
	{
		assertTrue(false);
		/*
		ModelFactory mf = new ModelFactory();
		PVariableVector variables = mf.createVariableVector("PVariable",new PDiscreteDomain( new DiscreteDomain(0.0, 1.0)), 3);
		PVariableVector dummyVars = mf.createVariableVector("PVariable", new PDiscreteDomain( new DiscreteDomain(0.0, 1.0)), 1);
  		PFactorGraph fg = mf.createGraph(dummyVars);
  		
  		PDiscreteFactor fn = fg.createTableFactor(0,variables,"dummy");


		PVariableVector variablesSub = mf.createVariableVector("PVariable",new PDiscreteDomain( new DiscreteDomain(0.0, 1.0)), 3);
		PVariableVector dummyVarsSub = mf.createVariableVector("PVariable", new PDiscreteDomain( new DiscreteDomain(0.0, 1.0)), 1);
  		PFactorGraph fgSub = mf.createGraph(dummyVarsSub);
  		fgSub.createTableFactor(0,variablesSub,"dummy");
 		
		fgSub = fg.addGraph(fgSub, dummyVars);
		
		fg.setName("fg");
		fgSub.setName("fgSub");
		variables.getVariables()[0].setName("v0");
		variables.getVariables()[1].setName("v1");
		variables.getVariables()[2].setName("v2");
		fn.setName("fn");
		
		//missing name
		typeByName(fg.getModelerObject(), null, true, "v3", "variable");
		//wrong name
		typeByName(fg.getModelerObject(), variables.getVariables()[1], false, "v0", "variable");
		//wrong type
		typeByName(fg.getModelerObject(), null, true, "fn", "variable");
		typeByName(fg.getModelerObject(), null, true, "v0", "factor");
		typeByName(fg.getModelerObject(), null, true, "fn", "graph");
		
		//match
		typeByName(fg.getModelerObject(), variables.getVariables()[0], true, "v0", "variable");
		typeByName(fg.getModelerObject(), fn, true, "fn", "factor");
		typeByName(fg.getModelerObject(), fgSub, true, "fgSub", "graph");
		
		//missing UUID
		typeByUUID(fg.getModelerObject(), null, true, java.util.UUID.randomUUID(), "variable");
		//wrong UUID
		typeByUUID(fg.getModelerObject(), variables.getVariables()[1], false, variables.getVariables()[0].getUUID(), "variable");
		//wrong UUID
		typeByUUID(fg.getModelerObject(), null, true, fn.getUUID(), "variable");
		typeByUUID(fg.getModelerObject(), null, true, variables.getVariables()[0].getUUID(), "factor");
		typeByUUID(fg.getModelerObject(), null, true, fn.getUUID(), "graph");
		
		//UUID
		typeByUUID(fg.getModelerObject(), variables.getVariables()[0], true, variables.getVariables()[0].getUUID(), "variable");
		typeByUUID(fg.getModelerObject(), fn, true, fn.getUUID(), "factor");
		typeByUUID(fg.getModelerObject(), fgSub, true, fgSub.getUUID(), "graph");
		
		Object o = fg.getVariableByName("v1");
		assertTrue(o instanceof Discrete);

		o = fg.getFactorByName("fn");
		assertTrue(o instanceof PFactor);
		
		o = fg.getGraphByName("fgSub");
		assertTrue(o instanceof PFactorGraph);

		PVariableVector vv = fg.getVariableVectorByName("v0");
		assertTrue(vv != null);
		assertTrue(vv.getVariables().length == 1);
		*/
	}

	@Test
	public void testSetLabel()
	{
		FactorGraph fg = new FactorGraph();
		fg.setName("fg");
		Discrete[] discretes = new Discrete[10];
		for(int i =0; i < discretes.length; ++i)
		{
			discretes[i] = new Discrete(0.0, 1.0);
			discretes[i].setName(String.format("d%d", i));
		}
		Factor f1 = fg.addFactor(new XorDelta(), discretes[0], discretes[1],discretes[2]);
		f1.setName("f1");
		Factor f2 = fg.addFactor(new XorDelta(), discretes[2], discretes[3],discretes[4]);
		f2.setName("f2");
		Factor f3 = fg.addFactor(new XorDelta(), discretes[4], discretes[5],discretes[0]);
		f3.setName("f3");
		
		FactorGraph fg2 = fg.copyRoot();
		fg2.setName("fg1C");
		
		FactorGraph fg1C = fg.addGraph(fg2);

		fg2.setName("fg2C");
		FactorGraph fg2C = fg.addGraph(fg2);

		assertEquals(discretes[0].getName(), discretes[0].getLabel());
		assertEquals(discretes[0].getName(), "d0");
		
		assertEquals(f1.getName(), "f1");
		
		assertEquals(fg1C.getName(), fg1C.getLabel());
		assertEquals(fg1C.getName(), "fg1C");
		
		discretes[0].setLabel("d");
		discretes[1].setLabel("d");
		f1.setLabel("f");
		f2.setLabel("f");
		fg1C.setLabel("fgC");
		fg2C.setLabel("fgC");

		assertTrue(discretes[0].getName() != discretes[0].getLabel());
		assertEquals(discretes[0].getName(), "d0");
		assertEquals(discretes[0].getLabel(), discretes[1].getLabel());
		assertEquals(discretes[0].getLabel(), "d");

		
		assertTrue(f1.getName() != f1.getLabel());
		assertEquals(f1.getName(), "f1");
		assertEquals(f1.getLabel(), f2.getLabel());
		assertEquals(f1.getLabel(), "f");
		
		assertTrue(fg1C.getName() != fg1C.getLabel());
		assertEquals(fg1C.getName(), "fg1C");
		assertEquals(fg1C.getLabel(), fg2C.getLabel());
		assertEquals(fg1C.getLabel(), "fgC");
		
		
	}
}
