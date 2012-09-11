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

package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.util.misc.IndexCounter;
import com.analog.lyric.util.misc.Misc;

public class FactorTable extends FactorTableBase
{
	private DiscreteDomain [] _domains;

	
	public FactorTable(int [][] indices, double [] weights, Discrete... variables)
	{
		super(indices,weights,true);
		
		DiscreteDomain[] domains = new DiscreteDomain[variables.length];
		for(int i = 0; i < domains.length; ++i)
		{
			domains[i] = variables[i].getDiscreteDomain();
		}
		_domains = domains;
	}
	
	public FactorTable(Object table, DiscreteDomain [] domains)
	{
		Object [] result = Misc.nDimensionalArray2indicesAndValues(table);
		_domains = domains;
		change((int[][])result[0],(double[])result[1],false);
	}
	
	public FactorTable(DiscreteDomain ... domains)
	{
		int size = 1;
		int [] domainSizes = new int[domains.length];
		for (int i = 0; i < domains.length; i++)
		{
			size *= domains[i].size();
			domainSizes[i] = domains[i].size();
		}
		
		int [][] indices = new int[size][];
		double [] values = new double[size];
		
		
		
		IndexCounter ic = new IndexCounter(domainSizes);
		
		int index = 0;
		for (int [] counter : ic)
		{
			indices[index] = counter.clone();
			index++;
		}
		_domains = domains;		
		change(indices,values,false);
	}

	
	public FactorTable(int [][] indices, double [] weights, boolean checkTable, DiscreteDomain ... domains)
	{
		super(indices,weights,checkTable);
		
		//Allow some calls to avoid checking the table.
		//We really only need it to be checked when the user modifies it.
		if (checkTable)
			check(this,domains);
		
		_domains = domains;
		
	}
	
	
	
	public FactorTable(int [][] indices, double [] weights,DiscreteDomain ... domains)
	{
		this(indices,weights,true,domains);
	}
	
	public DiscreteDomain [] getDomains()
	{
		return _domains;
	}
	
	private void check(FactorTableBase table, DiscreteDomain [] domains) 
	{
		
		int [][] indices = table.getIndices();
		

		
		for (int i = 0; i < indices.length; i++)
		{

			if (indices[i].length != domains.length)
				throw new RuntimeException("Number of Factor variables must match number of columns in combo table");
			
			for (int j = 0; j < indices[i].length; j++)
			{
				if (indices[i][j] < 0 || indices[i][j] >= domains[j].getElements().length)
					throw new RuntimeException("index: " + indices[i][j] + " is larger than domain of variable number " + j);
			}

		}
		
			
	}
	
	
	//////////////////////////////////////////////////////////////////
	// Methods for creating new Combo Tables
	//////////////////////////////////////////////////////////////////
	

	//This method will create a new combo table with some new, unrelated, variables included.
	//This method simply takes the cartesian product of the rows of the original table along
	//with the domain of each variable.  So if we had a table with 20 rows with two new variables
	//where one has a domain of 3 elements and another has a domain of 4 elements, we'll end up with
	//a FactorTable with 3*4*20 rows.
	public FactorTable createTableWithNewVariables(DiscreteDomain [] newDomains) 
	{
		
		int [][] indices = getIndices();
		int [] domainLengths = new int[newDomains.length];
		
		DiscreteDomain [] allDomains = new DiscreteDomain[_domains.length+newDomains.length];
		for (int i = 0; i < _domains.length; i++)
		{
			allDomains[i] = _domains[i];
		}
		for (int i = 0; i < newDomains.length; i++)
			allDomains[_domains.length+i] = newDomains[i];
		
		double [] oldValues = getWeights();
		
		for (int i = 0; i < domainLengths.length; i++)
			domainLengths[i] = newDomains[i].size();
		
		int prodOfLengths = 1;
		for (int i = 0; i < newDomains.length; i++)
			prodOfLengths *= newDomains[i].size();

		int [][] newTable = new int[prodOfLengths*indices.length][];
		double [] values = new double[newTable.length];
	
		IndexCounter ic = new IndexCounter(domainLengths);
		
		int newNumCols = indices[0].length + domainLengths.length;
		
		int curTableRow = 0;
		while (ic.hasNext())
		{
			int [] newVarIndices = ic.next();
			
			for (int i = 0; i < indices.length; i++)
			{
				int [] row = new int[newNumCols];
				
				for (int j = 0; j < indices[i].length; j++)
					row[j] = indices[i][j];
				
				for (int j = 0; j < domainLengths.length; j++)
					row[indices[i].length+j] = newVarIndices[j];
				
				newTable[curTableRow+i] = row;
				values[curTableRow+i] = oldValues[i];
			}
			
			
			curTableRow += indices.length;
		}
		
		
		return new FactorTable(newTable, values, allDomains);
	}

	
	public FactorTable joinVariablesAndCreateNewTable(int [] varIndices,
			int [] indexToJointIndex,
			DiscreteDomain [] allDomains,
			DiscreteDomain jointDomain) 
	{
		int [] allDomainLengths = new int[allDomains.length];
		
		for (int i = 0; i < allDomains.length; i++)
			allDomainLengths[i] = allDomains[i].size();
		
		int [][] indices = getIndices();
		
		DiscreteDomain [] newDomains = new DiscreteDomain[indices[0].length-varIndices.length+1];
		

		
		HashSet<Integer> indexSet = new HashSet<Integer>();
		int [] domainLengths = new int[varIndices.length];
		
		for (int i = 0; i < varIndices.length; i++)
		{
			if (indexSet.contains(varIndices[i]))
				throw new DimpleException("cannot join a variable to itself");
			
			indexSet.add(varIndices[i]);
			
			if (varIndices[i] < 0 || varIndices[i] >= indices[0].length)
				throw new DimpleException("index out of range");
			
			domainLengths[i] = allDomainLengths[varIndices[i]];
		}
		
		
		int index = 0;
		for (int i = 0; i < allDomains.length; i++)
		{
			if (!indexSet.contains(i))
			{
				newDomains[index] = allDomains[i];
				index++;
			}
		}
		newDomains[newDomains.length-1] = jointDomain;
		
		int[][] newtable = new int [indices.length][indices[0].length-varIndices.length+1];
		
		int [] oldIndices = new int[varIndices.length];
		
		
		for (int r = 0; r < newtable.length; r++)
		{
			
			int ctColIndex = 0;
			
			for (int c = 0; c < indices[r].length; c++)
			{
				if (!indexSet.contains(c))
				{
					newtable[r][ctColIndex] = indices[r][c];
					ctColIndex++;				                             
				}
			}
			
			for (int c = 0; c < varIndices.length; c++)
			{
				oldIndices[c] = indices[r][varIndices[c]];
			}
			
			newtable[r][newtable[r].length-1] = convertIndicesToJoint(oldIndices,
					indexToJointIndex,domainLengths);
			
		}
		
		
		return new FactorTable(newtable,getWeights().clone(),false,newDomains);
	}
	
	public int convertIndicesToJoint(int [] oldIndices, int [] indexToJointIndex, int [] domainLengths)
	{
		int newIndex = 0;
		int base = 1;
		
		for (int i = 0; i < indexToJointIndex.length; i++)
		{
			int index = indexToJointIndex[i];
			int tmp = oldIndices[index]*base;
			newIndex += tmp;
			base *= domainLengths[index];
		}
		
		return newIndex;
	}
	
	
	//////////////////////////////////////////////////////////////////
	// Code for evaluating as FactorFunction
	//////////////////////////////////////////////////////////////////


	private HashMap<ArrayList<Object>,Double> _lookupTable = null;
	private ArrayList<HashSet<Object>> _domainSets = null;
	
	
	private void initLookupTable(DiscreteDomain [] domains)
	{
		_lookupTable = new HashMap<ArrayList<Object>, Double>();
		_domainSets = new ArrayList<HashSet<Object>>();
		//For each row of the combo table
		int [][] indices = getIndices();
		double [] values = getWeights();
		
		for (int i = 0; i < indices.length; i++)
		{
			ArrayList<Object> row = new ArrayList<Object>();
			
			for (int j = 0; j < indices[i].length; j++)
			{
				row.add(domains[j].getElements()[indices[i][j]]);
			}
			
			_lookupTable.put(row,values[i]);			
		}
		
		for (int i = 0; i < domains.length; i++)
		{
			HashSet<Object> set = new HashSet<Object>();
			for (int j = 0; j < domains[i].getElements().length; j++)
			{
				set.add(domains[i].getElements()[j]);
			}
			_domainSets.add(set);
		}
	}
	
	public double evalAsFactorFunction(Object ... input)
	{
		if (_lookupTable == null)
			initLookupTable(_domains);
				
		ArrayList<Object> key = new ArrayList<Object>();
		
		for (int i = 0; i < input.length; i++)
		{
			if (!_domainSets.get(i).contains(input[i]))
			{
				throw new RuntimeException("input["+i+"] = " + input[i].toString() + " is not element of domain");
			}

			key.add(input[i]);
		}
		
		if (_lookupTable.containsKey(key))
			return _lookupTable.get(key);
		else
			return 0;	
	}
	
	//////////////////////////////////////////////////////////////////
	// Serialization code
	//////////////////////////////////////////////////////////////////

	
	public void serializeToXML(String serializeName, String targetDirectory) 
	{
		serializeToXML(this, serializeName, targetDirectory);
	}
	
	static public void serializeToXML(FactorTable ct, String serializeName, String targetDirectory) 
	{
		com.analog.lyric.dimple.model.xmlSerializer toXML 
			= new com.analog.lyric.dimple.model.xmlSerializer();
		
		toXML.serializeFactorTableToXML(ct, 
									   serializeName, 
									   targetDirectory);		
	}

	public static FactorTable deserializeFromXML(String docName) 
	{
		com.analog.lyric.dimple.model.xmlSerializer x
			= new com.analog.lyric.dimple.model.xmlSerializer();
		
		FactorTable mct = x.deserializeFactorTableFromXML(docName);		
		
		return mct;
	}

	
}
