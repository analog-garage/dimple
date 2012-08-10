package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.DimpleException;

public class NopFactorFunction extends FactorFunction 
{

	public NopFactorFunction(String name) 
	{
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double eval(Object ... input) 
	{
		throw new DimpleException("not implemented");
	}

}
