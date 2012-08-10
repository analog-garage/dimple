package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.repeated.RealStream;


public class PRealStream extends PVariableStreamBase 
{
	public PRealStream(PRealDomain domain)  
	{
		super(new RealStream((RealDomain)domain.getModelerObject()));
		// TODO Auto-generated constructor stub
	}

}
