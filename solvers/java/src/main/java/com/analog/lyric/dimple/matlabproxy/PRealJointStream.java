package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.RealJointDomain;
import com.analog.lyric.dimple.model.repeated.RealJointStream;


public class PRealJointStream extends PVariableStreamBase 
{
	public PRealJointStream(PRealJointDomain domain)  
	{
		super(new RealJointStream((RealJointDomain)domain.getModelerObject()));
		// TODO Auto-generated constructor stub
	}

}
