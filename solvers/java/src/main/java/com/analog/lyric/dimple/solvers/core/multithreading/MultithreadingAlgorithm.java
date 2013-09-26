package com.analog.lyric.dimple.solvers.core.multithreading;

public abstract class MultithreadingAlgorithm 
{
	private MultiThreadingManager _manager;
	
	public MultithreadingAlgorithm(MultiThreadingManager manager)
	{
		_manager = manager;
	}
	
	public MultiThreadingManager getManager()
	{
		return _manager;
	}
	
	public abstract void iterate(int numIters);
}
