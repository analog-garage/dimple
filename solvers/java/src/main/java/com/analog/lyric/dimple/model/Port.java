package com.analog.lyric.dimple.model;

public class Port 
{
	public INode node;
	public int index;
	public Port(INode node, int index)
	{
		this.node = node;
		this.index = index;
	}
	
	@Override
	public int hashCode()
	{
		return node.hashCode()+index;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Port)
		{
			Port p = (Port)obj;
			return p.node == this.node && p.index == this.index;
		}
		return false;
	}
	
	@Override
	public String toString() 
	{
		return node.toString() + " index: " + index;
	}

}
