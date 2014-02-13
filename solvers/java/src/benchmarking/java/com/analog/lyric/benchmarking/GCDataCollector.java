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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

public class GCDataCollector implements DataCollector
{
	ArrayList<GarbageCollectorMXBean> _gcs = new ArrayList<GarbageCollectorMXBean>();
	long _countsPre;
	long _timesPre;
	long _countsPost;
	long _timesPost;

	public GCDataCollector()
	{
		for (GarbageCollectorMXBean gc : ManagementFactory
				.getGarbageCollectorMXBeans())
		{
			_gcs.add(gc);
		}
	}

	@Override
	public void startCollection()
	{
		_countsPre = 0;
		_timesPre = 0;
		_countsPost = 0;
		_timesPost = 0;
		for (GarbageCollectorMXBean gc : _gcs)
		{
			_countsPre += gc.getCollectionCount();
			_timesPre += gc.getCollectionTime();
		}
	}

	@Override
	public void finishCollection()
	{
		_countsPost = 0;
		_timesPost = 0;
		for (GarbageCollectorMXBean gc : _gcs)
		{
			_countsPost += gc.getCollectionCount();
			_timesPost += gc.getCollectionTime();
		}
	}

	@Override
	public void postResults(BenchmarkRunIteration iteration)
	{
		iteration.getProperties().setProperty("gc.collection.count.pre",
				String.valueOf(_countsPre));
		iteration.getProperties().setProperty("gc.collection.time.milli.pre",
				String.valueOf(_timesPre));
		iteration.getProperties().setProperty("gc.collection.count.post",
				String.valueOf(_countsPost));
		iteration.getProperties().setProperty("gc.collection.time.milli.post",
				String.valueOf(_timesPost));
	}
}
