package com.analog.lyric.dimple.solvers.core.multithreading;

import com.analog.lyric.dimple.model.INode;

public class Edge 
{
	public INode from;
	public INode to;
	
	public Edge(INode f, INode t)
	{
		from = f;
		to = t;
	}
	
	@Override
	public int hashCode()
	{
		return from.hashCode()+to.hashCode();
	}
	
	@Override
	public boolean equals(Object other)
	{
		Edge otherEdge = (Edge)other;
		return otherEdge.from == from && otherEdge.to == to;
	}
}
