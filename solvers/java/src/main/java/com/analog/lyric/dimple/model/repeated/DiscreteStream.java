package com.analog.lyric.dimple.model.repeated;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.VariableBase;

public class DiscreteStream extends VariableStreamBase 
{

	public DiscreteStream(DiscreteDomain domain)  
	{
		super(domain);
		// TODO Auto-generated constructor stub
	}
	public DiscreteStream(Object ... domain)  
	{
		this(new DiscreteDomain(domain));
		// TODO Auto-generated constructor stub
	}
	@Override
	protected VariableBase instantiateVariable(Domain domain)  
	{
		// TODO Auto-generated method stub
		return new Discrete((DiscreteDomain)domain);
	}

}
