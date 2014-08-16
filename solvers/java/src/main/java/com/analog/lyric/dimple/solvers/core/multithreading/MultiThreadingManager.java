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

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraph;
import com.analog.lyric.dimple.solvers.core.multithreading.phasealgorithm.PhaseMultithreadingAlgorithm;
import com.analog.lyric.dimple.solvers.core.multithreading.singlequeuealgorithm.SingleQueueMutlithreadingAlgorithm;
import org.eclipse.jdt.annotation.Nullable;

/*
 * The MultiThreading Manager handles the collection of multithreading algorithms
 * Dimple currently supports.
 */
public class MultiThreadingManager
{
	private int _numWorkers;
	private FactorGraph _factorGraph;
	private long _cachedVersionId = -1;
	private @Nullable StaticDependencyGraph _cachedDependencyGraph;
	private HashMap<MultithreadingMode,MultithreadingAlgorithm> _mode2alg = new HashMap<MultithreadingMode, MultithreadingAlgorithm>();
	private MultithreadingMode _whichAlg = MultithreadingMode.Phase;
	private @Nullable ExecutorService _service;

	public MultiThreadingManager(FactorGraph fg, @Nullable ExecutorService service)
	{
		_service = service;
		setNumWorkersToDefault();
		_mode2alg.put(MultithreadingMode.Phase,new PhaseMultithreadingAlgorithm(this));
		_mode2alg.put(MultithreadingMode.SingleQueue,new SingleQueueMutlithreadingAlgorithm(this));
		_factorGraph = fg;
	}

	
	public MultiThreadingManager(FactorGraph fg)
	{
		this(fg,null);
	}
	
	public ExecutorService getService()
	{
		final ExecutorService service = _service;
		if (service == null)
			return ThreadPool.getThreadPool();
		else
			return service;
	}
	
	public MultithreadingMode [] getModes()
	{
		return MultithreadingMode.values();
	}
	
	public void setMode(String mode)
	{
		setMode(MultithreadingMode.valueOf(mode));
	}
	
	public void setMode(MultithreadingMode mode)
	{
		_whichAlg = mode;
	}
	
	public FactorGraph getFactorGraph()
	{
		return _factorGraph;
	}
	
	/*
	 * Create the threads in the thread pool.
	 */
	public void setNumWorkers(int numWorkers)
	{
		if (numWorkers < 1)
			throw new DimpleException("can't set this less than 1");
		
		_numWorkers = numWorkers;
	}
	
	public void setNumWorkersToDefault()
	{
		_numWorkers = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
	}
	
	
	public int getNumWorkers()
	{
		return _numWorkers;
	}
	
	public void iterate(int numIters)
	{
		_mode2alg.get(_whichAlg).iterate(numIters);
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
		
		return Objects.requireNonNull(_cachedDependencyGraph);
	}
	
	
	

}
