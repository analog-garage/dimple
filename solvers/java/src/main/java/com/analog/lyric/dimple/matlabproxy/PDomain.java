package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.Domain;

public class PDomain 
{
	private Domain _domain;
	
	public PDomain(Domain domain)
	{
		_domain = domain;
	}
	
	public Domain getModelerObject()
	{
		return _domain;
	}
}
