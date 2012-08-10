package com.analog.lyric.dimple.solvers.interfaces;

import com.analog.lyric.dimple.model.Port;

public interface ISolverFactor extends ISolverNode
{
	public Object getDefaultMessage(Port port);
	public Object getBelief() ;
	public double getEnergy() ;

	//TODO: really expose this?
	int [][] getPossibleBeliefIndices() ;

}
