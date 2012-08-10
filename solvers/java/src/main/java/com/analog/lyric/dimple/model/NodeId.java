package com.analog.lyric.dimple.model;

public class NodeId
{
	private static int nextId = 0;
	
	public static int getNext()
	{
		return nextId++;
	}
	
	public static void initialize()
	{
		nextId = 0;
	}
}
