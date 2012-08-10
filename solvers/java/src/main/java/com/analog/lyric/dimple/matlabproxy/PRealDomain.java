package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.RealDomain;

public class PRealDomain extends PDomain
{
	public PRealDomain(RealDomain domain)
	{
		super(domain);
	}
	
	public double getUpperBound()
	{
		return ((RealDomain)getModelerObject()).getUpperBound();
	}
	public double getLowerBound()
	{
		return ((RealDomain)getModelerObject()).getLowerBound();
	}
}
