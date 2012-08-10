package com.analog.lyric.dimple.model;


public class Edge implements Comparable<Edge>
{
	private INameable _left;
	private INameable _right;
	
	public Edge(INameable left, INameable right)
	{
		_left = left;
		_right = right;
	}
	
	public INameable getLeft(){return _left;}
	public INameable getRight(){return _right;}
	
	public int compareTo(Edge e)
	{
		int diff = 0;
		if(this != e)
		{
			diff = _left.getUUID().compareTo(e._left.getUUID());
			if(diff == 0)
			{
				diff = _right.getUUID().compareTo(e._right.getUUID());
			}
		}
		return diff;
	}
	public int hashCode()
	{
		return (_left.getUUID().toString() + 
				_right.getUUID().toString()).hashCode();
	}
	public boolean equals(Object o)
	{
		return this == o ||
			   (o instanceof Edge && 
				compareTo((Edge)o) == 0);
	}

	public String toString()
	{
		return String.format("Edge [%s] <-> [%s]", _left.getLabel(), _right.getLabel());
	}
}
