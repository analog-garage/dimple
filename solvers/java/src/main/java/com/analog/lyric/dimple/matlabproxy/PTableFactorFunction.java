package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.TableFactorFunction;

public class PTableFactorFunction extends PFactorFunction
{

	public PTableFactorFunction(TableFactorFunction tff)
	{
		super(tff);
	}

	public PTableFactorFunction(String name, PDiscreteDomain [] domains)
	{
		super(new TableFactorFunction(name,new FactorTable(PHelpers.convertDomains(domains))));
	}

	public PTableFactorFunction(String name, Object values,PDiscreteDomain [] domains)
	{
		super(new TableFactorFunction(name,new FactorTable(values,PHelpers.convertDomains(domains))));
	}

	public PTableFactorFunction(String name, int [][] indices, double [] values, PDiscreteDomain [] domains)
	{
		super(new TableFactorFunction(name, new FactorTable(indices,values, PHelpers.convertDomains(domains))));
	}

	public PFactorTable getFactorTable()
	{
		return new PFactorTable(((TableFactorFunction)getModelerObject()).getFactorTable());
	}


}
