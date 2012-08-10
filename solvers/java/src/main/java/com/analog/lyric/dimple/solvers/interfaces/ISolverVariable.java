package com.analog.lyric.dimple.solvers.interfaces;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;

public interface ISolverVariable extends ISolverNode
{
	public void setInput(Object input) ;
	public Object getDefaultMessage(Port port);
	public Object getBelief() ;
	public void remove(Factor factor);
	public double getEnergy() ;
    public void setGuess(Object guess) ;
    public Object getGuess() ;
    //public void updateBlastFromPast(ArrayList<Port> ports) ;

}
