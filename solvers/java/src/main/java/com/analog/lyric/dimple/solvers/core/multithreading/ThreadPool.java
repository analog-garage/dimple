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

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleThreadFactory;
import com.analog.lyric.dimple.exceptions.DimpleException;

/**
 * This class is used to create a singleton ThreadPool.  The threadpool
 * must be global so that we don't leak threads when FactorGraphs are destroyed
 * 
 * @author shershey
 *
 */
public class ThreadPool
{
	private static @Nullable ExecutorService _service;
	private static int _numThreads;
	
	private ThreadPool()
	{
		
	}
	

	public static ExecutorService getThreadPool()
	{
		if (_service == null)
		{
			setNumThreadsToDefault();
		}
		
		return Objects.requireNonNull(_service);
	}
	
	public static void setNumThreadsToDefault()
	{
		int cores = Runtime.getRuntime().availableProcessors();
		setNumThreads(cores);
		
	}
	
	public static void setNumThreads(int numThreads)
	{
		cleanupService();
		_numThreads = numThreads;
		ThreadPoolExecutor pool = new ThreadPoolExecutor(numThreads,
				numThreads, 1L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(),
				new DimpleThreadFactory());
		pool.allowCoreThreadTimeOut(true);
		_service = pool;
	}
	
	public static int getNumThreads()
	{
		return _numThreads;
	}
	
	private static void cleanupService()
	{
		final ExecutorService service = _service;
		if (service != null)
		{
			
			service.shutdown();
			try {
				service.awaitTermination(1,TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new DimpleException("Failed to shutdown multithreading service");
			}
		}
		
	}
}
