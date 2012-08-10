package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;

public class PFactorTable 
{
	private FactorTable _table;
	
	public PFactorTable(PDiscreteDomain [] domains)
	{
		_table = new FactorTable(PHelpers.convertDomains(domains));
	}
	
	public PFactorTable(Object table, PDiscreteDomain [] domains)
	{
		_table = new FactorTable(table, PHelpers.convertDomains(domains));
	}
	
	public PFactorTable(int [][] indices, double [] values, PDiscreteDomain [] domains)
	{
		_table = new FactorTable(indices,values,PHelpers.convertDomains(domains));
	}
	
	public PFactorTable(FactorTable table)
	{
		_table = table;
	}
	
	public PDiscreteDomain [] getDomains()
	{
		PDiscreteDomain [] pdomains = new PDiscreteDomain[_table.getDomains().length];
		
		for (int i = 0; i < _table.getDomains().length; i++)
		{
			pdomains[i] = new PDiscreteDomain(_table.getDomains()[i]);
		}
		return pdomains;
	}

	public int [][] getIndices()
	{
		return _table.getIndices();
	}
	
	public double [] getWeights()
	{
		return _table.getWeights();
	}
	
	public double get(int [] indices)
	{
		return _table.get(indices);
	}
	
	public void set(int [] indices, double value)
	{
		_table.set(indices, value);
	}
	
	public void changeWeights(double [] values) 
	{
		_table.changeWeights(values);
	}
	
	public void changeIndices(int [][] indices) 
	{
		_table.changeIndices(indices);
	}
	
	public void change(int [][] indices, double [] weights) 
	{
		_table.change(indices,weights);
	}

	public FactorTable getModelerObject()
	{
		return _table;
	}
	
}
