/**
 * 
 */
package com.analog.lyric.dimple.solvers.interfaces;

import com.analog.lyric.dimple.model.Port;

/**
 * @author schweitz
 *
 */
public interface ISolverNode 
{
	public void update() ;
	public void updateEdge(int outPortNum) ;
	public void initialize() ;
	public void connectPort(Port p) ;
	public ISolverFactorGraph getParentGraph();
	public ISolverFactorGraph getRootGraph();	
}
