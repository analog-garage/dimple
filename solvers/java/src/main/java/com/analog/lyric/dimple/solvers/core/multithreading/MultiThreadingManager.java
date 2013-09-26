/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.solvers.core.multithreading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraph;
import com.analog.lyric.dimple.solvers.core.multithreading.alg0.PhaseMultithreadingAlgorithm;
import com.analog.lyric.dimple.solvers.core.multithreading.alg1.SingleQueueCrossIterationMultithreadingAlgorithm;
import com.analog.lyric.dimple.solvers.core.multithreading.alg2.SingleQueueMutlithreadingAlgorithm;

/*
 * The MultiThreading Manager handles the collection of multithreading algorithms
 * Dimple currently supports.
 */
public class MultiThreadingManager 
{
	private int _numThreads;
	private ExecutorService _service; // = Executors.newFixedThreadPool(numThreads);
	private FactorGraph _factorGraph;
	private long _cachedVersionId = -1;
	private StaticDependencyGraph _cachedDependencyGraph;
	private MultithreadingAlgorithm [] _algorithms;
	private int _whichAlg = 0;
	private int _numAlgs = 3;
	
	public MultiThreadingManager(FactorGraph fg)
	{
		_numThreads = 1;
		_algorithms = new MultithreadingAlgorithm[_numAlgs];
		_factorGraph = fg;
		
	}
	
	public ExecutorService getService()
	{
		return _service;
	}
	
	public void setThreadingMode(int mode)
	{
		if (mode < 0 || mode >= _numAlgs)
			throw new DimpleException("invalid mode");
		_whichAlg = mode;
	}
	
	public FactorGraph getFactorGraph()
	{
		return _factorGraph;
	}
	
	/*
	 * Create the threads in the thread pool.
	 */
	public void setNumThreads(int numThreads)
	{
		if (numThreads < 1)
			throw new DimpleException("can't set this less than 1");
		
		_numThreads = numThreads;
		
		if (_service != null)
			_service.shutdownNow();
		if (numThreads != 1)
			_service = Executors.newFixedThreadPool(_numThreads);
	}
	
	public int getNumThreads()
	{
		return _numThreads;
	}
	
	
	public void iterate(int numIters)
	{
		getAlgorithm(_whichAlg).iterate(numIters);
	}
	
	/*
	 * Provide dependency graph caching.
	 */
	public StaticDependencyGraph getDependencyGraph()
	{
		long id = _factorGraph.getVersionId();
		if (id != _cachedVersionId)
		{
			_cachedVersionId = id;
			_cachedDependencyGraph = new StaticDependencyGraph(_factorGraph);
		}
		
		return _cachedDependencyGraph;
	}
	
	/*
	 * Lazily create the algorithm.
	 */
	private MultithreadingAlgorithm getAlgorithm(int mode)
	{
		if (_algorithms[mode] == null)
		{
			switch (mode)
			{
			case 0:
				_algorithms[mode] = new PhaseMultithreadingAlgorithm(this);
				break;
			case 1:
				_algorithms[mode] = new SingleQueueCrossIterationMultithreadingAlgorithm(this);
				break;
			case 2:
				_algorithms[mode] = new SingleQueueMutlithreadingAlgorithm(this);
				break;
			default:
				throw new DimpleException("unsupported mode");
			}
		}
		
		return _algorithms[mode];
	}

}
