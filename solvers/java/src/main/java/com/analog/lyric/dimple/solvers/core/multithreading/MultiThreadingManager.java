package com.analog.lyric.dimple.solvers.core.multithreading;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;

public class MultiThreadingManager 
{
	private int _numThreads;
	private ExecutorService _service; // = Executors.newFixedThreadPool(numThreads);
	private FactorGraph _factorGraph;
	private long _cachedVersionId = -1;
	private StaticDependencyGraph _cachedDependencyGraph;
	private ArrayList<MultithreadingAlgorithm> _algorithms;
	private int _whichAlg = 0;
	
	public MultiThreadingManager(FactorGraph fg)
	{
		_numThreads = 1;
		_algorithms = new ArrayList<MultithreadingAlgorithm>();
		_factorGraph = fg;
		_algorithms.add(new BillMultithreadingAlgorithm(this));
		_algorithms.add(new JeffMultithreadingAlgorithm(this));
	}
	
	public ExecutorService getService()
	{
		return _service;
	}
	
	public void setThreadingMode(int mode)
	{
		if (mode < 0 || mode > _algorithms.size()-1)
			throw new DimpleException("invalid mode");
		_whichAlg = mode;
	}
	
	public FactorGraph getFactorGraph()
	{
		return _factorGraph;
	}
	
	public void setNumThreads(int numThreads)
	{
		if (numThreads < 1)
			throw new DimpleException("can't set this less than 1");
		
		_numThreads = numThreads;
		if (_service != null)
			_service.shutdown();
		if (numThreads != 1)
		{
			_service = Executors.newFixedThreadPool(_numThreads);
		}
	}
	
	public int getNumThreads()
	{
		return _numThreads;
	}
	
	public void iterate(int numIters)
	{
		_algorithms.get(_whichAlg).iterate(numIters);
	}
	
	
	public StaticDependencyGraph getDependencyGraph()
	{
		long id = _factorGraph.getVersionId();
		//create dependency graph
		if (id != _cachedVersionId)
		{
			_cachedVersionId = id;
			_cachedDependencyGraph = new StaticDependencyGraph(_factorGraph);
		}
		
		return _cachedDependencyGraph;
	}
	
	public ArrayList<ArrayList<IScheduleEntry>> getPhases()
	{
		return getDependencyGraph().getPhases();
	}

}
