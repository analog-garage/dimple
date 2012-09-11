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
	
	public boolean isFactorTable()
	{
		return true;
	}
}
