package com.analog.lyric.dimple.model.repeated;

import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.VariableBase;

public class RealStream extends VariableStreamBase 
{

	public RealStream(RealDomain domain)  
	{
		super(domain);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected VariableBase instantiateVariable(Domain domain)  
	{
		// TODO Auto-generated method stub
		return new Real((RealDomain)domain);
	}

}
