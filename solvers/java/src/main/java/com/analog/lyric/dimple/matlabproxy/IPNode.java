package com.analog.lyric.dimple.matlabproxy;


import com.analog.lyric.dimple.model.INameable;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;

public interface IPNode extends INameable
{
    public Port[] getPorts();
	public INode getModelerObject();
	
    public void update() ;
	public void updateEdge(int outPortNum) ;
	
	public PFactorGraph getParentGraph() ;
	public PFactorGraph getRootGraph() ;
	public boolean hasParentGraph();
	public boolean isFactor();
	public boolean isVariable();
	public boolean isGraph();
}
