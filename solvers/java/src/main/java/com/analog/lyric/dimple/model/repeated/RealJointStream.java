package com.analog.lyric.dimple.model.repeated;

import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.RealJointDomain;
import com.analog.lyric.dimple.model.VariableBase;

public class RealJointStream extends VariableStreamBase 
{
	public RealJointStream(int numVars)  
	{
		super(new RealJointDomain(numVars));
		// TODO Auto-generated constructor stub
	}

	public RealJointStream(RealJointDomain domain)  
	{
		super(domain);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected VariableBase instantiateVariable(Domain domain)  
	{
		// TODO Auto-generated method stub
		return new RealJoint((RealJointDomain)domain);
	}

}
