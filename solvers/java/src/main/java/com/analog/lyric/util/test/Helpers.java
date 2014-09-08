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
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.XorDelta;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Model;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.IntRangeDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import org.eclipse.jdt.annotation.Nullable;


public class Helpers
{
	static public Random _r = new Random();
	final static public double EPSILON = 0.00001;

	
	static public FactorGraph MakeSimpleThreeLevelGraph()
	{
		return MakeSimpleThreeLevelGraph(Model.getInstance().getDefaultGraphFactory());
	}
	static public FactorGraph MakeSimpleThreeLevelGraph(@Nullable IFactorGraphFactory<?> graphFactory)
	{
		return MakeSimpleThreeLevelGraphs(graphFactory)[0];
	}

	
	static public FactorGraph MakeSimpleGraph(String tag,
											 @Nullable IFactorGraphFactory<?> graphFactory,
											 boolean randomInput)
	{
		Discrete vB1 = new Discrete(0.0, 1.0);
		Discrete vO1 = new Discrete(0.0, 1.0);
		Discrete vO2 = new Discrete(0.0, 1.0);
		vB1.setName(String.format("v%sB1", tag));
		vO1.setName(String.format("v%sO1", tag));
		vO2.setName(String.format("v%sO2", tag));
		FactorGraph fg = new FactorGraph(vB1);
		fg.setName(tag);
		fg.setSolverFactory(graphFactory);
		Factor f = fg.addFactor(new XorDelta(), vB1, vO1, vO2);
		f.setName(String.format("f%s", tag));
		
		if(randomInput)
		{
			VariableList variables = fg.getVariables();
			double[][] trivialRandomCodeword =
				trivialRandomCodeword(variables.size());
			for(int variable = 0; variable < variables.size(); ++variable)
			{
				((Discrete)variables.getByIndex(variable)).setInput(trivialRandomCodeword[variable]);
			}
		}
		return fg;
	}
	static public FactorGraph MakeSimpleGraph(String tag)
	{
		return MakeSimpleGraph(tag, Model.getInstance().getDefaultGraphFactory(), false);
	}
	static public FactorGraph MakeTrivialRandomGraph(String tag,
												 	 @Nullable IFactorGraphFactory<?> graphFactory,
												 	 int variables,
												 	 int extraFactors,
												 	 int maxDegree,
												 	 boolean randomInput)
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(graphFactory);
		fg.setName(tag);
		Discrete[] discretes = new Discrete[variables];
		for(int variable = 0; variable < discretes.length; ++variable)
		{
			discretes[variable] = new Discrete(0.0, 1.0);
		}
		
		XorDelta xorFF = new XorDelta();

		//make sure graph is connected.
		for(int factor = 0; factor < variables; ++factor)
		{
			if(factor < variables - 1)
			{
				fg.addFactor(xorFF, discretes[factor], discretes[factor + 1]);
			}
			else
			{
				fg.addFactor(xorFF, discretes[0], discretes[factor]);
			}
		}
		
		Random r = new Random();
		for(int extra = 0; extra < extraFactors; ++extra)
		{
			ArrayList<Integer> shuffledVariables = new ArrayList<Integer>();
			for(int i = 0; i < variables; ++i)
			{
				shuffledVariables.add(i);
			}
			Collections.shuffle(shuffledVariables);

			int degree = Math.max(r.nextInt(maxDegree), 2);
			Object[] edges = new Variable[degree];
			for(int i = 0; i < edges.length; ++i)
			{
				edges[i] = discretes[shuffledVariables.get(i)];
			}
			
			fg.addFactor(xorFF, edges);
		}

		if(randomInput)
		{
			VariableList variableList = fg.getVariables();
			double[][] trivialRandomCodeword =
				trivialRandomCodeword(variableList.size());
			for(int variable = 0; variable < variableList.size(); ++variable)
			{
				((Discrete)variableList.getByIndex(variable)).setInput(trivialRandomCodeword[variable]);
			}
		}
		
		FixedSchedule fs = (FixedSchedule) fg.getSchedule();
		ArrayList<IScheduleEntry> entries  = fs.getSchedule();
		Collections.shuffle(entries);
		return fg;
	}
	static public FactorGraph MakeSimpleChainGraph(	String tag,
												 	@Nullable IFactorGraphFactory<?> graphFactory,
												 	int factors,
												 	boolean randomInput)
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(graphFactory);
		fg.setName(tag);

		Discrete[] discretes = new Discrete[factors + 1];
		for(int variable = 0; variable < discretes.length; ++variable)
		{
			discretes[variable] = new Discrete(0.0, 1.0);
		}

		XorDelta xorFF = new XorDelta();
		for(int factor = 0; factor < factors; ++factor)
		{
			fg.addFactor(xorFF, discretes[factor], discretes[factor + 1]);
		}
		
		fg.setNamesByStructure();
		
		if(randomInput)
		{
			VariableList variables = fg.getVariables();
			double[][] trivialRandomCodeword =
				trivialRandomCodeword(variables.size());
			for(int variable = 0; variable < variables.size(); ++variable)
			{
				((Discrete)variables.getByIndex(variable)).setInput(trivialRandomCodeword[variable]);
			}
		}
		return fg;
	}
	static public FactorGraph MakeSimpleLoopyGraph(	String tag,
												 	@Nullable IFactorGraphFactory<?> graphFactory,
												 	boolean randomInput)
	{
		IFactorGraphFactory<?> oldFactory = Model.getInstance().getDefaultGraphFactory();
		Model.getInstance().setDefaultGraphFactory(graphFactory);
		FactorGraph fg = null;

		Discrete vB1 = new Discrete(0.0, 1.0);
		Discrete vO1 = new Discrete(0.0, 1.0);
		Discrete vO2 = new Discrete(0.0, 1.0);
		vB1.setName(String.format("v%sB1", tag));
		vO1.setName(String.format("v%sO1", tag));
		vO2.setName(String.format("v%sO2", tag));
		fg = new FactorGraph(vB1);
		fg.setName(tag);
		XorDelta xorFF = new XorDelta();

		Factor f1 = fg.addFactor(xorFF, vB1, vO1, vO2);
		f1.setName(String.format("f1%s", tag));

		Discrete vO3 = new Discrete(0.0, 1.0);
		Discrete vO4 = new Discrete(0.0, 1.0);
		vO3.setName(String.format("v%sO3", tag));
		vO4.setName(String.format("v%sO4", tag));
		Factor f2 = fg.addFactor(xorFF, vO2, vO3, vO4);
		f2.setName(String.format("f2%s", tag));

		Discrete vO5 = new Discrete(0.0, 1.0);
		vO5.setName(String.format("v%sO5", tag));
		Factor f3 = fg.addFactor(xorFF, vO3, vO5, vO1);
		f3.setName(String.format("f3%s", tag));
		
		if(randomInput)
		{
			VariableList variables = fg.getVariables();
			double[][] trivialRandomCodeword =
				trivialRandomCodeword(variables.size());
			for(int variable = 0; variable < variables.size(); ++variable)
			{
				((Discrete)variables.getByIndex(variable)).setInput(trivialRandomCodeword[variable]);
			}
		}

		Model.getInstance().setDefaultGraphFactory(oldFactory);
		return fg;
	}
	static public FactorGraph[] MakeSimpleThreeLevelGraphs()
	{
		return MakeSimpleThreeLevelGraphs(Model.getInstance().getDefaultGraphFactory());
	}
	static public FactorGraph[] MakeSimpleThreeLevelGraphs(@Nullable IFactorGraphFactory<?> factory)
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

		vRootB1.setName("vRootB1");
		vRootO1.setName("vRootO1");
		vRootO2.setName("vRootO2");
		vMidB1.setName("vMidB1");
		vMidO1.setName("vMidO1");
		vMidO2.setName("vMidO2");
		vLeafB1.setName("vLeafB1");
		vLeafO1.setName("vLeafO1");
		vLeafO2.setName("vLeafO2");

		FactorGraph fgRoot = new FactorGraph(vRootB1);
		FactorGraph fgMid  = new FactorGraph(vMidB1);
		FactorGraph fgLeaf = new FactorGraph(vLeafB1);
		
		fgRoot.setSolverFactory(factory);
		fgMid.setSolverFactory(factory);
		fgLeaf.setSolverFactory(factory);

		fgRoot.setName("Root");
		fgMid.setName("Mid");
		fgLeaf.setName("Leaf");

		XorDelta xorFF = new XorDelta();
		Factor fRoot = fgRoot.addFactor(xorFF, vRootB1, vRootO1, vRootO2);
		Factor fMid  = fgMid.addFactor( xorFF, vMidB1,  vMidO1,  vMidO2);
		Factor fLeaf = fgLeaf.addFactor(xorFF, vLeafB1, vLeafO1, vLeafO2);

		fRoot.setName("fRoot");
		fMid.setName("fMid");
		fLeaf.setName("fLeaf");
		
		
		//One sub graph
		fgMid.addGraph(fgLeaf, vMidO2);

		//Two sub graphs
		fgRoot.addGraph(fgMid, vRootO2);
				
		return new FactorGraph[]{fgRoot, fgMid, fgLeaf};
	}
	
	static public double compareBeliefs(double[][] a, double[][] b)
	{
		return compareBeliefs(a, b, EPSILON);
	}
	static public double compareBeliefs(double[][] a, double[][] b, String tag)
	{
		return compareBeliefs(a, b, EPSILON, false, tag);
	}
	static public double compareBeliefs(double[][] a, double[][] b, double epsilon)
	{
		return compareBeliefs(a, b, epsilon, false);
	}
	static public double compareBeliefs(double[][] a, double[][] b, double epsilon, String tag)
	{
		return compareBeliefs(a, b, epsilon, false, tag);
	}
	static public double compareBeliefs(double[][] a, double[][] b, double epsilon, boolean shouldBeSomeMismatch)
	{
		return compareBeliefs(a, b, epsilon, shouldBeSomeMismatch, "kaboom");
	}
	static public double compareBeliefs(double[][] a, double[][] b, double epsilon, boolean shouldBeSomeMismatch, String tag)
	{
 		double diffSum = 0;
 		int diffs = 0;
 		boolean same = a.length == b.length;
 		if(!same)
 		{
 			System.out.println(String.format("ERROR in compareBeliefs: a.length(%d) != b.length (%d), [%s]", a.length, b.length, tag));
 		}
 		assertTrue(tag, same);
 		for(int i = 0; i < a.length; ++i)
 		{
 			same = a[i].length == b[i].length;
 	 		if(!same)
 	 		{
 	 			System.out.println(String.format("ERROR in compareBeliefs: a[i].length(%d) != b[i].length(%d), i:%d, [%s]", a[i].length, b[i].length, i, tag));
 	 		}
 	 		assertTrue(tag, same);
 			for(int j = 0; j < a[i].length; ++j)
 			{
 				double diff = Math.abs(a[i][j] - b[i][j]);
 				diffSum += diff;
 				same = diff < epsilon;
 		 		if(!same)
 		 		{
 		 			diffs++;
 		 			if(!shouldBeSomeMismatch)
 		 			{
	 		 			System.out.println(String.format("ERROR in compareBeliefs: i:%d  j:%d  a[i][j]:%f  b[i][j]:%f  a[i][j] - b[i][j]:%f  abs:%f  epsilon:%f  [%s]"
	 		 					,i
	 		 					,j
	 		 					,a[i][j]
	 		 					,b[i][j]
	 		 					,a[i][j]-b[i][j]
	 		 					,Math.abs(a[i][j] - b[i][j])
	 		 					,epsilon
	 		 					,tag));
 		 			}
 		 		}
 		 		if(!shouldBeSomeMismatch)
 		 		{
 		 			assertTrue(tag, same);
 		 		}
 			}
 		}
 		if(shouldBeSomeMismatch)
 		{
 			if(diffs == 0)
 			{
 	 			System.out.println(String.format("Expecting at least one mismatch, got 0 > epislon. diffSum:%f  epsilon:%f  [%s]", diffSum, epsilon, tag));
 			}
 			assertTrue(tag, diffs > 0);
 		}
 		return diffSum;
	}
	static public void setInputs(FactorGraph fg, double[][] inputs)
	{
		VariableList vs = fg.getVariables();
		for(int i = 0; i < vs.size(); ++i)
		{
			Discrete d = (Discrete) vs.getByIndex(i);
			d.setInput(inputs[i]);
		}
	}
	static public double assertBeliefsDifferent(double[][] a, double[][] b)
	{
		return compareBeliefs(a, b, EPSILON, true);
	}
	static public double[][] beliefs(FactorGraph fg)
	{
		return beliefsOrInputs(fg, false, false, true, true);
	}
	static public double[][] inputs(FactorGraph fg)
	{
		return beliefsOrInputs(fg, false, false, true, false);
	}
	static public double[][] beliefs(FactorGraph fg, boolean byName)
	{
		return beliefsOrInputs(fg, byName, false, false, true);
	}
	static public double[][] beliefs(FactorGraph fg, boolean byName, boolean print)
	{
		return beliefsOrInputs(fg, byName, print, false, true);
	}
	static public double[][] beliefsOrInputs(FactorGraph fg, boolean byName, boolean print, boolean byAddOrder, boolean getbeliefs)
	{
		VariableList vs = fg.getVariables();
		ArrayList<Discrete> orderUsed= new ArrayList<Discrete>();
		double[][] ret = new double[vs.size()][];
		if(byAddOrder)
		{
			for(int i = 0; i < vs.size(); ++i)
			{
				Discrete d = (Discrete) vs.getByIndex(i);
				if(getbeliefs)
				{
					ret[i] = d.getBelief();
				}
				else
				{
					ret[i] = d.getInput();
				}
				orderUsed.add(d);
			}
		}
		else
		{
			TreeMap<String, Discrete> vsSorted = new TreeMap<String, Discrete>();
			for(int i = 0; i < vs.size(); ++i)
			{
				String sortBy = vs.getByIndex(i).getQualifiedName();
				if(!byName)
				{
					sortBy = vs.getByIndex(i).getUUID().toString();
				}
				vsSorted.put(sortBy,
							 (Discrete)(vs.getByIndex(i)));
			}
			
			int i = 0;
			for(Discrete d : vsSorted.values())
			{
				if(getbeliefs)
				{
					ret[i] = d.getBelief();
				}
				else
				{
					ret[i] = d.getInput();
				}
				orderUsed.add(d);
				i++;
			}
		}

		if(print)
		{
			for(int i = 0; i < orderUsed.size(); ++i)
			{
				Discrete d = orderUsed.get(i);
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				sb.append(d.getLabel());
				sb.append("]: {" );
				double[] oneVariablesRet = requireNonNull(getbeliefs ? d.getBelief() : d.getInput());

				for(int j = 0; j < oneVariablesRet.length; j++)
				{
					sb.append(((Double)oneVariablesRet[j]).toString());
					if(j < oneVariablesRet.length - 1)
					{
						sb.append(" ");
					}
				}
				sb.append("}");
				System.out.println(sb.toString());
			}
		}
		return ret;
	}
	static public double compareBeliefs(FactorGraph fgA,
			  						  FactorGraph fgB)
	{
		return compareBeliefs(fgA, fgB, true, EPSILON);
	}
	static public double compareBeliefs(FactorGraph fgA,
									  FactorGraph fgB,
									  double epsilon)
    {
		return compareBeliefs(fgA, fgB, true, epsilon);
    }
	static public double compareBeliefs(FactorGraph fgA,
									  FactorGraph fgB,
									  boolean byName)
	{
		return compareBeliefs(fgA, fgB, byName, EPSILON);
  	}
	static public double compareBeliefs(FactorGraph fgA,
									  FactorGraph fgB,
									  boolean byName,
									  double epsilon)
	{
		double[][] beliefsA = beliefs(fgA, byName);
		double[][] beliefsB = beliefs(fgB, byName);
		return compareBeliefs(beliefsA, beliefsB, epsilon);
	}




	
	public static String getBeliefString(INode node)
	{
		StringBuilder sb = new StringBuilder();
		ArrayList<Discrete> variables = new ArrayList<Discrete>();
		if(node instanceof Discrete)
		{
			for(Port p : node.getPorts())
			{
				for(Port p2 : p.getConnectedNode().getPorts())
				{
					variables.add((Discrete) p2.getConnectedNode());
				}
			}
		}
		else
		{
			for(Port p : node.getPorts())
			{
				variables.add((Discrete) p.getConnectedNode());
			}
		}
		for(Discrete mv : variables)
		{
			getBeliefString(sb, mv);
		}
		return sb.toString();
	}
	
	public static void getBeliefString(StringBuilder sb, Discrete variable)
	{
		try
		{
			double[] belief = requireNonNull(variable.getBelief());
			sb.append(" {" );
			for(int j = 0; j < belief.length; j++)
			{
				sb.append(((Double)belief[j]).toString());
				if(j < belief.length - 1)
				{
					sb.append(" ");
				}
			}
			sb.append("}");
		}
		catch(Exception e)
		{
			
		}
	}
	
	public static void printDifferences(String tag, double[][] a, double[][] b)
	{
 		double epsilon = 0.00001;
 		
 		boolean same = a.length == b.length;
 		if(!same)
 		{
 			throw new DimpleException(String.format("ERROR in check: a.length(%d) != b.length (%d)", a.length, b.length));
 		}
 		for(int i = 0; i < a.length; ++i)
 		{
 			same = a[i].length == b[i].length;
 	 		if(!same)
 	 		{
 	 			throw new DimpleException(String.format("ERROR in compareBeliefs: a[i].length(%d) != b[i].length(%d), i:%d", a[i].length, b[i].length, i));
 	 		}
 			for(int j = 0; j < a[i].length; ++j)
 			{
 				if(Math.abs(a[i][j] - b[i][j]) > epsilon)
 				{
 					same = false;
 				}
 			}
 		}
 		
 		if(!same)
 		{
 	 		StringBuilder sb = new StringBuilder(String.format("check [%s]\n", tag));
 			for(int i = 0; i < a.length; ++i)
 	 		{
 				sb.append(String.format("\ta%3d ", i));
 	 			for(int j = 0; j < a[i].length; ++j)
 	 			{
 	 				sb.append(" ");
 	 				sb.append(((Double)a[i][j]).toString());
 	 			}
 	 			sb.append("\n");

 	 			sb.append(String.format("\tb%3d ", i));
 	 			for(int j = 0; j < b[i].length; ++j)
 	 			{
 	 				sb.append(" ");
 	 				sb.append(((Double)b[i][j]).toString());
 	 			}
 	 			sb.append("\n");
 	 		}
 			
 			System.out.println(sb.toString());
 		}
	}
	static public double[][] trivialDeepCopy(double[][] x)
	{
		double[][] copy = new double[x.length][];
		for(int i = 0; i < copy.length; ++i)
		{
			copy[i] = new double[x[i].length];
			for(int j = 0; j < x[i].length; ++j)
			{
				copy[i][j] = x[i][j];
			}
		}
		return copy;
	}

	static public double[][] zerosCodeWord(int length, int numErrors)
	{
		return zerosCodeWord(length, numErrors, 0.99999);
	}
	static public double[][] zerosCodeWord(int length, int numErrors, double confidence)
	{
		if(numErrors > length)
		{
			throw new DimpleException("more error bits than bits is silly");
		}
		double[][] codeWord = new double[length][];
		for(int i = 0; i < codeWord.length; i++)
		{
			codeWord[i] = new double[2];
			codeWord[i][0] = 1 - confidence;
			codeWord[i][1] = confidence;
		}
		for(int i = 0; i < numErrors; ++i)
		{
			int errorIdx = _r.nextInt(codeWord.length);
			double temp = codeWord[errorIdx][0];
			codeWord[errorIdx][0] = codeWord[errorIdx][1];
			codeWord[errorIdx][1] = temp;
		}
		return codeWord;
	}
	
	static public double[][] trivialRandomCodeword(int length)
	{
		double[][] codeWord = new double[length][];
		for(int i = 0; i < codeWord.length; i++)
		{
			codeWord[i] = new double[2];
			codeWord[i][0] = _r.nextDouble();
			codeWord[i][1] = 1 - codeWord[i][0];
		}
		return codeWord;
	}
	public static double decodeZerosCodeword( double[][] codewordWithErrors,
			FactorGraph fg,
			int iterations,
			IFactorGraphFactory<?> solver,
			ISchedule schedule)
	{
		return decodeZerosCodeword(codewordWithErrors,
							fg,
							iterations,
							solver,
							schedule,
							false);
	}
	public static double decodeZerosCodeword( double[][] codewordWithErrors,
			FactorGraph fg,
											int iterations,
											IFactorGraphFactory<?> solver,
											ISchedule schedule,
											boolean shouldFail)
	{
		double diffSum = 0;
		initFgForDecode(fg, solver, schedule, iterations);
		double beliefs [][] = decodeGeneralCodeword(codewordWithErrors, fg);
		double epsilon = 0.00001;
		if(!shouldFail)
		{
			for(int i = 0; i < beliefs.length; ++i)
			{
				//double d0 = 0 - beliefs[i][0];
				//double d1 = 1 - beliefs[i][1];
				//boolean diff0 = Math.abs(d0) > epsilon;
				//boolean diff1 = Math.abs(d1) > epsilon;
				//System.out.println(String.format("%2d: [0 - %f == %f, diff:%s]  [1 - %f == %f, diff:%s]"
				//		,i
				//		,beliefs[i][0]
				//		,d0
				//		,diff0
				//		,beliefs[i][1]
				//		,d1
				//		,diff1));
				assertEquals(0, beliefs[i][0], epsilon);
				assertEquals(1, beliefs[i][1], epsilon);

				diffSum += Math.abs(0 - beliefs[i][0]);
				diffSum += Math.abs(1 - beliefs[i][1]);
			}
		}
		else
		{
			double sum0 = 0;
			double sum1 = 0;
			for(int i = 0; i < beliefs.length; ++i)
			{
				sum0 += beliefs[i][0];
				sum1 += beliefs[i][1];

				diffSum += Math.abs(0 - beliefs[i][0]);
				diffSum += Math.abs(1 - beliefs[i][1]);
			}
			double maxDiff = beliefs.length * epsilon;
			double sum1OnDecode = beliefs.length;
			
			assertTrue(sum0 > (maxDiff));
			assertTrue((sum1 < (sum1OnDecode - maxDiff)) ||
				       (sum1 > (sum1OnDecode + maxDiff)));
		}
		return diffSum;
	}
	
	static @Nullable Variable[] ordered_vars;
	
	public static void initFgForDecode(FactorGraph fg, IFactorGraphFactory<?> solver, ISchedule schedule, int iterations)
			
	{
		fg.setSolverFactory(solver);
		fg.setSchedule(schedule);
		requireNonNull(fg.getSolver()).setNumIterations(iterations);
	}
	
	public static double[][] decodeGeneralCodeword( double[][] codewordWithErrors,
			FactorGraph fg)
											//int iterations,
											//IFactorGraphFactory solver,
											//ISchedule schedule)
	//		)
	{
		
//		if (!didInits) {
//			fg.setSolverFactory(solver);
//			fg.setSchedule(schedule);
//			fg.getSolver().setNumIterations(iterations);
//			didInits = true;
//		}
		
		VariableList variables = fg.getVariables();
		
		Variable namedVar = fg.getVariableByName("order_vv0");
		// Some graphs have explicitNames of "order_vv#" so that this routine can work on
		// graphs such as fec
		boolean hasOrderNames = (namedVar != null);
		if (hasOrderNames && ordered_vars == null)
		{
			final Variable[] vars = ordered_vars = new Variable[variables.size()];
			for(int i = 0; i < variables.size(); ++i) {
				Variable thisVar;
				String orderName = "order_vv" + i;
				thisVar = fg.getVariableByName(orderName);
				vars[i] = thisVar;
			}
		}


		for(int i = 0; i < variables.size(); ++i)
		{
			Variable thisVar;
			if (hasOrderNames) {
				//String orderName = "order_vv" + i;
				//thisVar = fg.getVariableByName(orderName);
				thisVar = Objects.requireNonNull(ordered_vars)[i];
			} else {
	        	thisVar = variables.getByIndex(i);
	        }
			((Discrete)thisVar).setInput(codewordWithErrors[i]);
			//((Discrete)variables.getByIndex(i)).setInput(codewordWithErrors[i]);
		}
		fg.solve();

		//Verify we decoded!
		VariableList vs = fg.getVariables();
		double[][] beliefs = new double[vs.size()][];
		for(int i = 0; i < vs.size(); ++i)
		{
			Variable thisVar;
			if (hasOrderNames) {
				//String orderName = "order_vv" + i;
				//thisVar = fg.getVariableByName(orderName);
				thisVar = Objects.requireNonNull(ordered_vars)[i];
			} else {
	        	thisVar = variables.getByIndex(i);
	        }
			beliefs[i] = (double[]) ((Discrete)thisVar).getBeliefObject();
			//beliefs[i] = (double[]) ((Discrete)vs.getByIndex(i)).getBeliefObject();
		}
		return beliefs;
	}


	static public IntRangeDomain domainFromRange(int first, int last)
	{
		if(first >= last)
		{
			throw new DimpleException(String.format("first (%d) must be less than last (%d)", first, last));
		}
		
		return DiscreteDomain.range(first, last);
	}
	
	static public Factor addSillyFactor(FactorGraph fg, int rows, Object[] variables)
	{
		return addSillyFactor(fg, rows, variables, false);
	}
	static public Factor addSillyFactor(FactorGraph fg, int rows, Object[] variables, boolean randomWeights)
	{
		AlwaysTrueUpToNRowsFactorFunction atff = new AlwaysTrueUpToNRowsFactorFunction(rows, randomWeights);
		Factor f = fg.addFactor(atff, variables);
		return f;
	}

	
	static public TableFactorFunction createSillyTable(int rows, int columns)
	{
  		int[][] dummyTable = new int[rows][columns];
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
  		
  		double[] dummyValues = new double[rows];
		Arrays.fill(dummyValues, 1.0);
		
		DiscreteDomain[] domains = new DiscreteDomain[columns];
		Arrays.fill(domains, DiscreteDomain.bit());
		
  		return new TableFactorFunction("silly", dummyTable, dummyValues, domains);
	}

	static public void verifyDecode(FactorGraph fg, int errors, int iterations)
	{
		long initialStartDBG = System.currentTimeMillis();
		long diffDBG = 0;

		ArrayList<Variable>	variables = (ArrayList<Variable>) fg.getVariables().values();
		IFactorGraphFactory<?>	solver = Objects.requireNonNull(fg.getFactorGraphFactory());
		ISchedule schedule = fg.getSchedule();
		
		double[][] codewordWithTooManyErrors 	= zerosCodeWord(variables.size(), (int)(variables.size() * 0.9) );
		double[][] codeword 					= zerosCodeWord(variables.size(), errors);

		decodeZerosCodeword(
				codewordWithTooManyErrors,
				fg,
				iterations,
				solver,
				schedule,
				true);
		decodeZerosCodeword(
				codeword,
				fg,
				iterations,
				solver,
				schedule,
				false);


		diffDBG = System.currentTimeMillis() - initialStartDBG;
		System.out.println(String.format("verifyDecode time:%d (total)\n", diffDBG));
	}
	
	static public void setSeed(long seed)
	{
		_r.setSeed(seed);
	}
	
}
