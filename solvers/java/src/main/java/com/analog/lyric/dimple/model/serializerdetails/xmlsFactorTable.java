package com.analog.lyric.dimple.model.serializerdetails;

import com.analog.lyric.dimple.model.DiscreteDomain;

class xmlsFactorTable
{
	public String _functionName;
	public int _ephemeralId;
	public int[][] _indices;
	public double[] _values;
	public DiscreteDomain[] _domains;
	
	public xmlsFactorTable(){}
	public xmlsFactorTable(String functionName, int ephemeralId, int[][] indices, double[] values,DiscreteDomain [] domains)
	{
		_functionName = functionName;
		_ephemeralId = ephemeralId;
		_indices = indices;
		_values = values;
		_domains = domains;
	}
}

