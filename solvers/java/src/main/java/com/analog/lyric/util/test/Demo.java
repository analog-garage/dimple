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

package com.analog.lyric.util.test;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Arrays;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.XorDelta;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;

public class Demo {
	public static void main(String[] args)  {
		
		//////////////////////////////////////////////////////////
		//SOLVING A SIMPLE GRAPH
		Double[] binary = new Double[]{0.0, 1.0};
		
		//pass in pre-defined array
		Discrete v1 = new Discrete((Object[])binary);
		
		//or 'on the fly'
		Discrete v2 = new Discrete(0.0, 1.0);
		
		//or array created inline
		Discrete v3 = new Discrete((Object[])new Double[]{0.0, 1.0});

		
		v1.setInput(.8, .2);
		v2.setInput(.8, .2);
		v3.setInput(.4, .6);


		FactorGraph fg = new FactorGraph();

		XorDelta xorFF = new XorDelta();
		Factor f = fg.addFactor(xorFF,v1,v2,v3);
				
		((SumProductSolverGraph)requireNonNull(fg.getSolver())).setNumIterations(1);


		fg.solve();

		boolean print = args.length == 0 ||
						!args[0].equals("quiet");
		

		if(print)
		{
			//////////////////////////////////////////////////////////
			//PRINTING GRAPH
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("before naming...\n");
			System.out.println(fg.getFullString());
			System.out.println("-------------------------------------------------------------------------");
			
			
			//////////////////////////////////////////////////////////
			//PRINTING NAMED GRAPH
			v1.setName("bit1");
			v2.setName("bit2");
			v3.setName("bit3");
			f.setName("xorDelta1");
			fg.setName("trivialFG");
			
			System.out.println("\n\n-------------------------------------------------------------------------");
			System.out.println("After naming...\n");
			System.out.println(fg.getFullString());
			
			System.out.println(v1.getName() + " beliefs: " + Arrays.toString((double[])v1.getBeliefObject()));
			System.out.println(v2.getName() + " beliefs: " + Arrays.toString((double[])v2.getBeliefObject()));
			System.out.println(v3.getName() + " beliefs: " + Arrays.toString((double[])v3.getBeliefObject()));
			System.out.println("-------------------------------------------------------------------------");
		}
		
		//////////////////////////////////////////////////////////
		//NESTED GRAPHS
		
		//Variables
		Discrete vRootB1 = new Discrete(0.0, 1.0);
		Discrete vRootO1 = new Discrete(0.0, 1.0);
		Discrete vRootO2 = new Discrete(0.0, 1.0);
		Discrete vMidB1  = new Discrete(0.0, 1.0);
		Discrete vMidO1  = new Discrete(0.0, 1.0);
		Discrete vMidO2  = new Discrete(0.0, 1.0);
		Discrete vLeafB1 = new Discrete(0.0, 1.0);
		Discrete vLeafO1 = new Discrete(0.0, 1.0);
		Discrete vLeafO2 = new Discrete(0.0, 1.0);


		vRootB1.setName("vRootB1");
		vRootO1.setName("vRootO1");
		vRootO2.setName("vRootO2");
		vMidB1.setName("vMidB1");
		vMidO1.setName("vMidO1");
		vMidO2.setName("vMidO2");
		vLeafB1.setName("vLeafB1");
		vLeafO1.setName("vLeafO1");
		vLeafO2.setName("vLeafO2");
		
		
		//Factor graphs
		FactorGraph fgRoot = new FactorGraph(vRootB1);
		FactorGraph fgMid  = new FactorGraph(vMidB1);
		FactorGraph fgLeaf = new FactorGraph(vLeafB1);
		

		fgRoot.setName("fgRoot");
		fgMid.setName("fgMid");
		fgLeaf.setName("fgLeaf");


		//Factors
		Factor fRoot = fgRoot.addFactor(xorFF, vRootB1, vRootO1, vRootO2);
		Factor fMid  = fgMid.addFactor( xorFF, vMidB1,  vMidO1,  vMidO2);
		Factor fLeaf = fgLeaf.addFactor(xorFF, vLeafB1, vLeafO1, vLeafO2);

		
		fRoot.setName("fRoot");
		fMid.setName("fMid");
		fLeaf.setName("fLeaf");

		//Nest the graphs
		fgMid.addGraph(fgLeaf, vMidO2);
		fgRoot.addGraph(fgMid, vRootO2);


		//Find objects by name - cause exception if not found
		ArrayList<Object> temps = new ArrayList<Object>();
		
		//unqualified immediate children
		temps.add(fgRoot.getObjectByName("fgMid"));
		temps.add(fgRoot.getObjectByName("vRootB1"));
		temps.add(fgRoot.getObjectByName("vRootO1"));
		temps.add(fgRoot.getObjectByName("vRootO2"));

		//qualified immediate children
		temps.add(fgRoot.getObjectByName("fgRoot.fgMid"));
		temps.add(fgRoot.getObjectByName("fgRoot.vRootB1"));
		temps.add(fgRoot.getObjectByName("fgRoot.vRootO1"));
		temps.add(fgRoot.getObjectByName("fgRoot.vRootO2"));
		
		//further down
		temps.add(fgRoot.getObjectByName("fgRoot.fgMid.vMidO1"));
		temps.add(fgRoot.getObjectByName("fgRoot.fgMid.vMidO2"));
		temps.add(fgRoot.getObjectByName("fgRoot.fgMid.fgLeaf"));
		temps.add(fgRoot.getObjectByName("fgRoot.fgMid.fgLeaf.vLeafO1"));
		temps.add(fgRoot.getObjectByName("fgRoot.fgMid.fgLeaf.vLeafO2"));

		//sub graphs still have their own tables, and can be searched
		fgMid = requireNonNull((FactorGraph) fgRoot.getObjectByName("fgMid"));
		fgLeaf = requireNonNull((FactorGraph) fgMid.getObjectByName("fgLeaf"));
		temps.add(fgMid.getObjectByName("vMidO1"));
		temps.add(fgMid.getObjectByName("fgMid.fgLeaf.vLeafO1"));
		temps.add(fgLeaf.getObjectByName("vLeafO1"));
		
		//prove it worked
		for(Object o : temps)
		{
			if(o == null)
			{
				throw new DimpleException("WHOOPS didn't find someone");
			}
		}
		if(print)
		{
			System.out.println("\n\n-------------------------------------------------------------------------");
			System.out.println("Nested graphs\n");
			System.out.println(fgRoot.getFullString());
			
			System.out.println("-------------------------------------------------------------------------");
		}
		
	}
}
