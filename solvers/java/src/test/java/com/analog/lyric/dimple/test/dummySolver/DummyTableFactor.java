package com.analog.lyric.dimple.test.dummySolver;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;

public class DummyTableFactor extends STableFactorBase
{	
	public DummyTableFactor(Factor factor)  
	{
		super(factor);
	}

	public void updateEdge(int outPortNum)
	{
	}
		
	@Override
	public void update()
	{		
	}

	@Override
	public double getEnergy() {
		// TODO Auto-generated method stub
		return 0;
	}	
}
