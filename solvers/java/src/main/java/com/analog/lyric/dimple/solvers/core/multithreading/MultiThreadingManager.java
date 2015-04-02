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

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraph;
import com.analog.lyric.dimple.solvers.core.multithreading.phasealgorithm.PhaseMultithreadingAlgorithm;
import com.analog.lyric.dimple.solvers.core.multithreading.singlequeuealgorithm.SingleQueueMutlithreadingAlgorithm;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

/*
 * The MultiThreading Manager handles the collection of multithreading algorithms
 * Dimple currently supports.
 */
public class MultiThreadingManager
{
	private final ISolverFactorGraph _sgraph;
	private Map<MultithreadingMode,MultithreadingAlgorithm> _mode2alg =
		new EnumMap<MultithreadingMode, MultithreadingAlgorithm>(MultithreadingMode.class);
	private final @Nullable ExecutorService _service;

	private int _numWorkers;
	private long _cachedVersion = -1;
	private @Nullable StaticDependencyGraph _cachedDependencyGraph;
	private MultithreadingMode _whichAlg = MultithreadingMode.Phase;

	public MultiThreadingManager(ISolverFactorGraph sfg, @Nullable ExecutorService service)
	{
		_sgraph = sfg;
		_service = service;
		setNumWorkersToDefault();
		_mode2alg.put(MultithreadingMode.Phase,new PhaseMultithreadingAlgorithm(this));
		_mode2alg.put(MultithreadingMode.SingleQueue,new SingleQueueMutlithreadingAlgorithm(this));
	}

	
	public MultiThreadingManager(ISolverFactorGraph sfg)
	{
		this(sfg,null);
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
		return _sgraph.getModelObject();
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
		final FactorGraph fg = _sgraph.getModelObject();
		long version = fg.structureVersion();
		if (version != _cachedVersion)
		{
			_cachedVersion = version;
			_cachedDependencyGraph = new StaticDependencyGraph(fg);
		}
		
		return Objects.requireNonNull(_cachedDependencyGraph);
	}
	
	
	

}
