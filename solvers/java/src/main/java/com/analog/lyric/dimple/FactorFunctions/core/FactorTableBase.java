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
import java.util.HashSet;
import java.util.Random;


public class FactorTableBase
{
	private int [][] _indices = null;
	private double[] _weights = null;
	
	/**
	 * The negative log of the corresponding values from {@link #_weights}.
	 * Must be changed when values in weights changed or nulled out to force
	 * recomputation.
	 */
	private double [] _potentials = null;
	
	public FactorTableBase()
	{
		
	}

	public void copy(FactorTableBase copy)
	{
		_indices = copy._indices.clone();
		_weights = copy._weights.clone();
		//maybe clone potentials if they're not null
		_potentials = null;
		
	}
	
	public FactorTableBase(FactorTableBase copy)
	{
		copy(copy);
	}
	
	public void changeWeight(int index, double weight)
	{
		_weights[index] = weight;
		//TODO: maybe just recompute potential if it exists.
		_potentials = null;
	}
	
	public FactorTableBase(int [][] table, double [] probs)
	{
		this(table,probs,true);
	}
	
	public FactorTableBase(int [][] table, double [] probs,boolean checkTable)
	{
		//Allow some calls to avoid checking the table.
		//We really only need it to be checked when the user modifies it.
		if (checkTable)
			check(table,probs);
		
		_indices = table;
		_weights = probs;
	}
	
	public int [][] getIndices()
	{
		return _indices;
	}
	
	public double [] getWeights()
	{
		return _weights;
	}
	public int getRows()
	{
		return getIndices().length;
	}
	public int getColumns()
	{
		return getIndices()[0].length;
	}
	public int getEntry(int row, int column)
	{
		return getIndices()[row][column];
	}
	public int[] getRow(int row)
	{
		return getIndices()[row];
	}
	public int[] getColumnCopy(int column)
	{
		int[] copy = new int[getRows()];
		for(int i = 0; i < getRows(); ++i)
		{
			copy[i] = getEntry(i, column);
		}
		return copy;
	}
	
	public void changeIndices(int [][] indices) 
	{
		check(indices,_weights);
		_indices = indices;
	}
	
	public void changeWeights(double [] probs) 
	{
		check(_indices,probs);
		_weights = probs;
		_potentials = null;
		
	}
	
	
	private int findLocationFromIndices(int [] indices)
	{
		if (indices.length != _indices[0].length)
			throw new RuntimeException("number of indices is incorrect");
		
		for (int i = 0; i < _indices.length; i++)
		{
			boolean found = true;
			for (int j = 0; j < indices.length; j++)
			{
				if (indices[j] != _indices[i][j])
				{
					found = false;
					break;
				}
			}
			
			if (found)
			{
				return i;
			}
		}
		throw new RuntimeException("invalid indices.  Don't match the domains or were currently not in sparse table");
	}
	
	public double get(int [] indices)
	{
		int loc = findLocationFromIndices(indices);
		return _weights[loc];
	}
	
	public void set(int [] indices, double value)
	{
		int loc = findLocationFromIndices(indices);
		_weights[loc] = value;
		if (_potentials != null)
		{
			_potentials[loc] = -Math.log(value);
		}
	}
	
	public void change(int [][] indices, double [] weights)
	{
		change(indices,weights,true);
	}
	
	public void change(int [][] indices, double [] weights, boolean check)
	{
		if (check)
			check(indices,weights);
		
		_indices = indices;
		_weights = weights;
		_potentials = null;
		
	}

	//Used for getWeightIndexFromTableIndices
	//private HashMap<ArrayList<Integer>,Integer> _indices2weightIndex = null;

	
	


	
	public double [] getPotentials()
	{
		if (_potentials == null)
		{
			_potentials = new double[_weights.length];
			for (int i = 0; i < _weights.length; i++)
			{
				_potentials[i] = -Math.log(_weights[i]);
			}
		}
		
		return _potentials;
	}
	
	
	

	protected void check(int[][] indices, double[] weights)
	{
		//Do some error checking
		if (indices.length < 0)
			throw new RuntimeException("combo table must have at least one row");

		if (indices.length != weights.length)
			throw new RuntimeException("indices and values lengths must match");
		
		//Check that there are no duplicate rows
		HashSet<ArrayList<Integer>> uniqueRows = new HashSet<ArrayList<Integer>>();


		for (int i = 0; i < indices.length; i++)
		{
			ArrayList<Integer> key = new ArrayList<Integer>();
			for (int j = 0; j < indices[i].length; j++)
				key.add(indices[i][j]);

			if (uniqueRows.contains(key))
				throw new RuntimeException("Table Factor contains multiple rows with same set of indices.");
			
			uniqueRows.add(key);
		}
	}
	
	public void randomizeWeights(Random r)
	{
		_potentials = null;
		
		for (int i = 0; i < _weights.length; i++)
			_weights[i] = r.nextDouble();

	}
	
	
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		try
		{
			s.append(
					String.format("ComboTable  r:%d  c:%d  w:%d\n"
							,_indices.length
							,_indices[0].length
							,_weights.length));
			for(int r = 0; r < _indices.length; ++r)
			{
				s.append("\t");
				for(int c = 0; c < _indices[r].length; ++c)
				{
					s.append(String.format("  %d", _indices[r][c]));
				}
				s.append(String.format("   w: %f\n", _weights[r]));
			}
		}
		catch(Exception e)
		{
			s.append("\nERROR caught exception making string.\n[\n" + e.getMessage() + "\n]\nTable probably malformed.");
		}
		return s.toString();
	}
}

