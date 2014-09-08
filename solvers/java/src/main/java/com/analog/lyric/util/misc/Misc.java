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

package com.analog.lyric.util.misc;

import java.util.ArrayList;
import java.util.Collection;

import com.analog.lyric.dimple.factorfunctions.XorDelta;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INameable;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;

public class Misc
{
	/**
	 * A do-nothing function you can use for in-code conditional breakpoints when debugging.
	 * <p>
	 * Just set a method breakpoint on this method in Eclipse and leave it there and
	 * insert this
	 */
	public static void breakpoint()
	{
	}
	
	public static Object [] nDimensionalArray2indicesAndValues(Object obj)
	{
		Object tmpObj = obj;
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		int size = 1;
		while (tmpObj.getClass().isArray())
		{
			if (tmpObj instanceof double [])
			{
				int sz = ((double[])tmpObj).length;
				size*=sz;
				sizes.add(sz);
				break;
			}
			else
			{
				Object [] tmp = (Object[])tmpObj;
				sizes.add(tmp.length);
				size*=tmp.length;
				tmpObj = tmp[0];
			}
		}
		
		int [][] indices = new int[size][];
		double [] values = new double[size];
		
		int [] domainSizes = new int[sizes.size()];
		for (int i = 0; i < domainSizes.length; i++)
			domainSizes[i] = sizes.get(i);
		
		IndexCounter ic = new IndexCounter(domainSizes);
		int index = 0;
		
		for ( int [] counter : ic)
		{
			indices[index] = counter.clone();
			
			tmpObj = obj;
			int i = 0;
			while (tmpObj.getClass().isArray())
			{
				if (tmpObj instanceof double[])
					tmpObj = ((double[])tmpObj)[counter[i]];
				else
					tmpObj = ((Object[])tmpObj)[counter[i]];
				i++;
			}
			values[index] = (Double)tmpObj;
			
			
			index++;
		}
		
		return new Object[]{indices,values};
		
	}
	
	public static int [] 			convertToIds(Collection<?> nameables)
	{
		//ArrayList<Integer> result = new ArrayList<Integer>();
		int [] ids = new int[nameables.size()];
		
		int i = 0;
		for (Object n : nameables)
		{
    		ids[i] = ((INameable)n).getId();
    		i++;
			//result.add(((INameable)n).getId());
    	}
		
		
		return ids;
	}

	
	public static FactorGraph 	makeXorGraphFromMatrix(byte[][] matrix, IFactorGraphFactory<?> solver)
    {
    	return makeGraphFromMatrix(matrix,
    							   new Double[]{0.0, 1.0},
    							   new XorDelta(),
    							   solver);
    }
	
    public static FactorGraph 	makeGraphFromMatrix(byte[][] matrix,
    												Object[] domain,
    												FactorFunction f,
    												IFactorGraphFactory<?> solver)
    {
    	
    	//Make all variables first
    	Discrete[] variables = new Discrete[matrix[0].length];
    	for(int i = 0; i < variables.length; ++i)
    	{
    		variables[i] = new Discrete(domain);
    	}

    	FactorGraph fg = new FactorGraph();
    	fg.setSolverFactory(solver);
    	fg.addVariables(variables);
	
    	//Now go function by function (row by row)
    	Object[] oneFactorsArgs = null;
    	for(int i = 0; i < matrix.length; ++i)
    	{
    		//###########################################################
    		//## Variables for this function
    		//###########################################################
    		//First build up array of the variable args to the function.
    		//count args for *this* factor (this row)
    		int numArgs = 0;
    		for(int j = 0; j < matrix[i].length; ++j)
    		{
    			if(matrix[i][j] != 0)
    			{
    				numArgs++;
    			}
    		}
    		//if num args is different than last iteration,
    		//make a new array with appropriate size
    		if(oneFactorsArgs == null ||
    		   numArgs != oneFactorsArgs.length)
    		{
        		oneFactorsArgs = new Discrete[numArgs];
    		}
    		//Actually store the variable args
    		int nextArgIdx = 0;
    		for(int j = 0; j < matrix[i].length; ++j)
    		{
    			if(matrix[i][j] != 0)
    			{
    				oneFactorsArgs[nextArgIdx] = variables[j];
    				nextArgIdx++;
    			}
    		}
    		
    		//###########################################################
    		//## The function
    		//###########################################################
    		fg.addFactor(f, oneFactorsArgs);
    	}
    	
    	return fg;
    }
	static public double[][] getBeliefs(FactorGraph fg)
	{
		VariableList vs = fg.getVariablesFlat();
		ArrayList<Variable> vbs = (ArrayList<Variable>)vs.values();
		double[][] beliefs = new double[vbs.size()][];
		for(int i = 0; i < vbs.size(); ++i)
		{
			beliefs[i] = ((Discrete)vbs.get(i)).getBelief();
		}
		return beliefs;
	}

	static public FixedSchedule nodeFloodingSchedule(FactorGraph fg)
	{
		FixedSchedule fs = new FixedSchedule();
		for(Variable vb : fg.getVariablesFlat().values())
		{
			fs.add(new NodeScheduleEntry(vb));
		}
		for(Factor f : fg.getNonGraphFactorsFlat().values())
		{
			fs.add(new NodeScheduleEntry(f));
		}
		return fs;
	}
}
