package com.analog.lyric.dimple.FactorFunctions.core;

import com.analog.lyric.dimple.model.Domain;

public abstract class FactorFunctionBase 
{
	private String _name;

	public FactorFunctionBase(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return _name;
	}

	public abstract double eval(Object ... input);

	public abstract FactorTable getFactorTable(Domain [] domainList);

}
