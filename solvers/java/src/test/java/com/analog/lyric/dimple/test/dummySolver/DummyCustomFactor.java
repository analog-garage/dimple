package com.analog.lyric.dimple.test.dummySolver;

import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.SFactorBase;

public class DummyCustomFactor extends SFactorBase
{
	
	public DummyCustomFactor(com.analog.lyric.dimple.model.Factor factor) 
	{
		super(factor);
	}	

	public void updateEdge(int outPortNum) 
	{
		
	}

	@Override
	public Object getDefaultMessage(Port port) 
	{
		com.analog.lyric.dimple.model.Discrete var = (com.analog.lyric.dimple.model.Discrete)port.getConnectedNode();
		DummyVariable v = (DummyVariable)var.getSolver();
		return v.getDefaultMessage(port);
	}

	@Override
	public void initialize() 
	{
		
	}

	@Override
	public double getEnergy() {
		// TODO Auto-generated method stub
		return 0;
	}
}
