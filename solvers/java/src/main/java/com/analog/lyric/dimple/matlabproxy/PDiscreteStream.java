package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.repeated.DiscreteStream;

public class PDiscreteStream extends PVariableStreamBase
{

	public PDiscreteStream(PDiscreteDomain domain)  
	{
		super(new DiscreteStream((DiscreteDomain)domain.getModelerObject()));
		// TODO Auto-generated constructor stub
	}

}
