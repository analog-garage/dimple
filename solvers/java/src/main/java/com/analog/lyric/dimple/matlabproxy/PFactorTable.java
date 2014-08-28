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

import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.util.misc.Matlab;

@Matlab
public class PFactorTable extends PObject
{
	private IFactorTable _table;
	
	/*--------------
	 * Construction
	 */
	
	public PFactorTable(PDiscreteDomain [] domains)
	{
		_table = FactorTable.create(PHelpers.convertDomains(domains));
	}
	
	public PFactorTable(Object table, PDiscreteDomain [] domains)
	{
		_table = FactorTable.create(table, PHelpers.convertDomains(domains));
	}
	
	public PFactorTable(int [][] indices, double [] values, PDiscreteDomain [] domains)
	{
		_table = FactorTable.create(indices, values, PHelpers.convertDomains(domains));
	}
	
	public PFactorTable(IFactorTable table)
	{
		_table = table;
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public IFactorTable getDelegate()
	{
		return _table;
	}
	
	@Override
	public IFactorTable getModelerObject()
	{
		return _table;
	}
	
	@Override
	public boolean isDiscrete()
	{
		return true;
	}
	
	@Override
	public boolean isFactorTable()
	{
		return true;
	}
	
	/*----------------------
	 * PFactorTable methods
	 */
	
	public void normalize(int [] directedTo)
	{
		_table.makeConditional(BitSetUtil.bitsetFromIndices(_table.getDimensions(), directedTo));
	}
	
	public PDiscreteDomain [] getDomains()
	{
		JointDomainIndexer domains = _table.getDomainIndexer();
		PDiscreteDomain [] pdomains = new PDiscreteDomain[domains.size()];
		
		for (int i = 0; i < pdomains.length; i++)
		{
			pdomains[i] = new PDiscreteDomain(domains.get(i));
		}
		return pdomains;
	}

	public int [][] getIndices()
	{
		return _table.getIndicesSparseUnsafe();
	}
	
	public double [] getWeights()
	{
		return _table.getWeightsSparseUnsafe();
	}
	
	public double get(int [] indices)
	{
		return _table.getWeightForIndices(indices);
	}
	
	public void set(int [] indices, double value)
	{
		_table.setWeightForIndices(value, indices);
	}
	
	public void changeWeights(double [] values)
	{
		_table.replaceWeightsSparse(values);
	}
	
	public void change(int [][] indices, double [] weights)
	{
		_table.setWeightsSparse(indices,weights);
	}

}
