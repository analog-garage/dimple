package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.matlabproxy.PFactor;
import com.analog.lyric.dimple.model.DiscreteFactor;


/*
 * This proxy wraps the TableFunctionBase class
 */
public class PDiscreteFactor  extends PFactor
{

	public PDiscreteFactor(DiscreteFactor impl) 
	{
		super(impl);
	}
	
	public PFactorTable getFactorTable() 
	{
		return new PFactorTable(((DiscreteFactor)this.getModelerObject()).getFactorTable());
	}
	
	public int[][] getPossibleBeliefIndices() 
	{
		return ((DiscreteFactor)_factor).getPossibleBeliefIndices();
	}

	
}
