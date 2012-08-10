package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.DiscreteDomain;

public class PDiscreteDomain extends PDomain
{
	public PDiscreteDomain(DiscreteDomain domain)
	{
		super(domain);
	}
	
	public Object [] getElements()
	{
		return ((DiscreteDomain)getModelerObject()).getElements();
	}
}
