/*******************************************************************************
 *   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.benchmarking;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;

public class MemoryUsageSamplingCollector implements DataCollector
{
	private ArrayList<MemoryPoolMXBean> _memoryPools = new ArrayList<MemoryPoolMXBean>();
	private Poller _poller = new Poller();
	public long _lastUsage = -1;
	public long _totalAllocation = 0;

	public MemoryUsageSamplingCollector()
	{
		for (MemoryPoolMXBean memoryPool : ManagementFactory
				.getMemoryPoolMXBeans())
		{
			if (memoryPool.getType().equals(MemoryType.HEAP))
			{
				_memoryPools.add(memoryPool);
			}
		}
		_poller.start();
	}

	enum PollerState
	{
		STOPPED, START_REQUESTED, POLLING, STOP_REQUESTED
	}

	class Poller extends Thread
	{
		private volatile PollerState _pollerState = PollerState.STOPPED;

		public Poller()
		{
			super();
			this.setDaemon(true);
		}

		@Override
		public void run()
		{
			while (true)
			{
				synchronized (this)
				{
					// Wait until should be polling
					while (_pollerState != PollerState.START_REQUESTED)
					{
						try
						{
							wait();
						}
						catch (InterruptedException e)
						{
						}
					}
					// Notify other threads that this thread is now polling.
					_pollerState = PollerState.POLLING;
					notifyAll();
				}

				while (_pollerState != PollerState.STOP_REQUESTED)
				{
					try
					{
						long usage = getMemoryUsage();
						if ((_lastUsage != -1) && (usage > _lastUsage))
						{
							_totalAllocation += (usage - _lastUsage);
						}
						_lastUsage = usage;
						Thread.sleep(0, 100000); // ~100us
					}
					catch (InterruptedException e)
					{
					}
				}

				// Notify other threads that this thread is no longer polling.
				synchronized (this)
				{
					_pollerState = PollerState.STOPPED;
					notifyAll();
				}
			}
		}

		// Called by other thread to cause this thread to begin polling.
		// Does not return until polling.
		public void startPolling()
		{
			synchronized (this)
			{
				_pollerState = PollerState.START_REQUESTED;
				notifyAll();
			}

			synchronized (this)
			{
				while (_pollerState != PollerState.POLLING)
				{
					try
					{
						wait();
					}
					catch (InterruptedException e)
					{
					}
				}
				notifyAll();
			}
		}

		// Called by other thread to cause this thread to stop polling.
		// Does not return until polling stopped.
		public synchronized void stopPolling()
		{
			_pollerState = PollerState.STOP_REQUESTED;
			while (_pollerState != PollerState.STOPPED)
			{
				try
				{
					wait();
				}
				catch (InterruptedException e)
				{
				}
			}
			notifyAll();
		}
	}

	private long getMemoryUsage()
	{
		long result = 0;
		if (_memoryPools != null)
		{
			for (MemoryPoolMXBean memoryPool : _memoryPools)
			{
				long poolUsage = memoryPool.getUsage().getUsed();
				result += poolUsage;
			}
		}
		return result;
	}

	@Override
	public void startCollection()
	{
		_poller.startPolling();
	}

	@Override
	public void finishCollection()
	{
		_poller.stopPolling();
	}

	@Override
	public void postResults(BenchmarkRunIteration iteration)
	{
		String key = String.format("heap.allocation");
		String value = String.valueOf(_totalAllocation);
		iteration.getProperties().setProperty(key, value);
		_lastUsage = -1;
		_totalAllocation = 0;
	}
}
