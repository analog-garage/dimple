package com.analog.lyric.dimple.test;

//See 'JUnit4' / 'JUnit3' comments to change to back to JUnit4.
//JUnit4
//import static org.junit.Assert.assertTrue;
//import org.junit.* ;

//JUnit3
import java.util.Arrays;
import java.util.BitSet;
import java.util.UUID;

import junit.framework.*;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.NodeId;

//JUnit3
public class SolverNamesTest extends TestCase {
	
	//JUnit4	
	//@BeforeClass
	//public static void setUpBeforeClass()  
	//{
	//}

	//JUnit4
	//@AfterClass
	//public static void tearDownAfterClass()  {
	//}
	
	//JUnit4
	//@Before
	public void setUp()  
	{
	}

	//JUnit4
	//@After
	public void tearDown()  {
	}

	//JUnit4
	//@Test
	public void test_simpleNameStuff() 
	{
		Discrete variable = new Discrete(NodeId.getNext(), new Object[]{0.0, 1.0}, "Variable");
		
		assertTrue(variable.getParentGraph() == null);
		assertTrue(variable.getName().equals(variable.getUUID().toString()));
		
		FactorGraph fg = new FactorGraph(new Discrete[]{variable});
		assertTrue(fg.getParentGraph() == null);
		assertTrue(fg.getName().equals(fg.getUUID().toString()));
		assertTrue(variable.getParentGraph() == fg);
				
		String fgNameSet = "FactorGraphName";
		fg.setName(fgNameSet);
		assertTrue(fg.getName().equals(fgNameSet));
		
		String variableNameSet = "VariableGraphName";
		variable.setName(variableNameSet);
		assertTrue(variable.getName().equals(variableNameSet));		
	}

	//JUnit4
	//@Test
	public void test_parentGraphNameStuff() 
	{
		//3 new variables
		Discrete variables[] = new Discrete[]
		{
			new Discrete(0,1),
			new Discrete(0,1),
			new Discrete(0,1)
		};
		int[] variableIds = new int[]
		{
			variables[0].getId(),
			variables[1].getId(),
			variables[2].getId(),
		};
		//simple checks on variables
		for(int i = 0; i < variableIds.length; ++i)
		{
			assertTrue(variables[i].getParentGraph() == null);
			assertTrue(variables[i].getName().equals(variables[i].getUUID().toString()));
		}
		
		//new graph
		Discrete[] dummyVars = new Discrete[0];
		FactorGraph fg = new FactorGraph(dummyVars);
	
		//simple checks on graph
		assertTrue(fg.getParentGraph() == null);
		assertTrue(fg.getName().equals(fg.getUUID().toString()));
		
		int[][] dummyTable = new int[3][3];
  		for(int i = 0; i < dummyTable.length; ++i)
  		{
  			BitSet bits = new BitSet(16);
  			int index = 0;
  			int value = i + 1;
  			while (value != 0) {
  			  if ((value & 1) != 0) {
  			    bits.set(index);
  			  }
  			  ++index;
  			  value = value >>> 1;
  			}
 			
  			for(int j = 0; j < dummyTable[i].length; ++j)
  			{
  				dummyTable[i][j] = bits.get(j) ? 0 : 1; 
  			}
  		}
		double[] dummyValues = new double[3];
		Arrays.fill(dummyValues, 1.0);
		
		TableFactorFunction factorFunc = new TableFactorFunction("table", 
				new FactorTable(dummyTable,dummyValues,new DiscreteDomain(0,1),new DiscreteDomain(0,1),new DiscreteDomain(0,1)));
		//fg.createTable(dummyTable, dummyValues,);
		Factor fn = fg.addFactor(factorFunc, variables);
		assertTrue(fn.getName().equals(fn.getUUID().toString()));
		
		//check parent
		assertTrue(fn.getParentGraph() == fg);
		for(int i = 0; i < variableIds.length; ++i)
		{
			assertTrue(variables[i].getParentGraph() == fg);
		}
		
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
		
		String variableBaseName = "vName";
		for(int i = 0; i < variableIds.length; ++i)
		{
			String VariableName = variableBaseName + Integer.toString(i); 
			variables[i].setName(VariableName);	
			assertTrue(variables[i].getName().equals(VariableName));
			String qualifiedName = fg.getName() + "." + VariableName;
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
		//Function
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
	
	void typeByName(FactorGraph fg, Object expected, boolean equals, String id, String type)
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
	void typeByUUID(FactorGraph fg, Object expected, boolean equals, UUID id, String type)
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
	
	//JUnit4
	//@Test
	public void test_getObjectByType() 
	{
		
		Discrete variables[] = new Discrete[]
  		{
  			new Discrete(0,1),
  			new Discrete(0,1),
  			new Discrete(0,1)
  		};
  		//new graph
  		Discrete[] dummyVars = new Discrete[0];
  		FactorGraph fg = new FactorGraph(dummyVars);
  	
  		
  		int[][] dummyTable = new int[3][3];
  		for(int i = 0; i < dummyTable.length; ++i)
  		{
  			BitSet bits = new BitSet(16);
  			int index = 0;
  			int value = i + 1;
  			while (value != 0) {
  			  if ((value & 1) != 0) {
  			    bits.set(index);
  			  }
  			  ++index;
  			  value = value >>> 1;
  			}
 			
  			for(int j = 0; j < dummyTable[i].length; ++j)
  			{
  				dummyTable[i][j] = bits.get(j) ? 0 : 1; 
  			}
  		}
  		double[] dummyValues = new double[3];
		Arrays.fill(dummyValues, 1.0);
		
		TableFactorFunction factorFunc = new TableFactorFunction("table", new FactorTable(dummyTable,dummyValues,
				new DiscreteDomain(0,1),new DiscreteDomain(0,1),new DiscreteDomain(0,1)));
  		//fg.createTable(dummyTable, dummyValues);
  		Factor fn = fg.addFactor(factorFunc,variables);


		Discrete variablesSub[] = new Discrete[]
		{
			new Discrete(0,1),
			new Discrete(0,1),
			new Discrete(0,1)
		};
		
		//sub graph graph
		Discrete[] dummyVarsSub = new Discrete[0];
		FactorGraph fgSub = new FactorGraph(dummyVarsSub);
	
		int[][] dummyTableSub = new int[3][3];
		double[] dummyValuesSub = new double[3];
  		for(int i = 0; i < dummyTableSub.length; ++i)
  		{
  			BitSet bits = new BitSet(16);
  			int index = 0;
  			int value = i;
  			while (value != 0L) {
  			  if ((value & 1L) != 0) {
  			    bits.set(index);
  			  }
  			  ++index;
  			  value = value >>> 1;
  			}
 			
  			for(int j = 0; j < dummyTableSub[i].length; ++j)
  			{
  				dummyTableSub[i][j] = bits.get(j) ? 0 : 1; 
  			}
  		}
		Arrays.fill(dummyValuesSub, 1.0);
		
		
		TableFactorFunction otherFactorFunc = new TableFactorFunction("table", new FactorTable(dummyTableSub,dummyValuesSub,
				new DiscreteDomain(0,1),new DiscreteDomain(0,1),new DiscreteDomain(0,1)));
		
		fgSub.addFactor(otherFactorFunc, variablesSub);
 		
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
}

