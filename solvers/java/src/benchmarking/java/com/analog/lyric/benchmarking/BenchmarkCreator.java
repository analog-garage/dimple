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

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

public class BenchmarkCreator
{
	private BenchmarkDataset _benchmarkDataset = new BenchmarkDataset();

	public BenchmarkCreator(String label)
	{
		Properties properties = getBenchmarkDataset().getProperties();
		properties.setProperty("label", label);
		addJavaSystemProperty("java.class.path");
		addJavaSystemProperty("java.vendor");
		addJavaSystemProperty("java.version");
		addJavaSystemProperty("os.arch");
		addJavaSystemProperty("os.name");
		addJavaSystemProperty("os.version");
		addJavaSystemProperty("user.name");
		Runtime runtime = Runtime.getRuntime();
		properties.setProperty("java.memory.free",
				String.valueOf(runtime.freeMemory()));
		properties.setProperty("java.memory.total",
				String.valueOf(runtime.totalMemory()));
		properties.setProperty("java.memory.max",
				String.valueOf(runtime.maxMemory()));
		properties.setProperty("java.processors.available",
				String.valueOf(runtime.availableProcessors()));
		Date creationDate = new Date();
		properties.setProperty("create.date", creationDate.toString());
		properties.setProperty("create.date.milliseconds",
				String.valueOf(creationDate.getTime()));
		java.net.InetAddress localMachine;
		try
		{
			localMachine = java.net.InetAddress.getLocalHost();
			String hostName = localMachine.getHostName();
			properties.setProperty("hostname", hostName);
		}
		catch (UnknownHostException e)
		{
			// ignore
		}
	}

	public void doRun(String label, int warmupIterations, int iterations,
			boolean doGC, IterationRunner runnable,
			DataCollector... dataCollectors)
	{
		BenchmarkRun run = new BenchmarkRun();
		getBenchmarkDataset().getBenchmarkRuns().add(run);
		Properties runProperties = run.getProperties();
		runProperties.setProperty("label", label);
		runProperties.setProperty("warmupIterations",
				String.valueOf(warmupIterations));
		runProperties.setProperty("iterations", String.valueOf(iterations));
		runProperties.setProperty("GC", String.valueOf(doGC));
		Runtime.getRuntime().gc();
		boolean abort = false;
		for (int i = 0; !abort && i < warmupIterations; i++)
		{
			abort = runnable.run();
			if (doGC)
			{
				Runtime.getRuntime().gc();
			}
		}
		for (int i = 0; !abort && i < iterations; i++)
		{
			for (DataCollector dataCollector : dataCollectors)
			{
				dataCollector.startCollection();
			}
			abort = runnable.run();
			for (DataCollector dataCollector : dataCollectors)
			{
				dataCollector.finishCollection();
			}
			BenchmarkRunIteration iteration = new BenchmarkRunIteration();
			iteration.setIteration(i);
			for (DataCollector dataCollector : dataCollectors)
			{
				dataCollector.postResults(iteration);
			}
			run.getIterations().add(iteration);
			if (doGC)
			{
				Runtime.getRuntime().gc();
			}
		}
	}

	private void addJavaSystemProperty(String key)
	{
		getBenchmarkDataset().getProperties().setProperty(key,
				System.getProperty(key));
	}

	public BenchmarkDataset getBenchmarkDataset()
	{
		return _benchmarkDataset;
	}
}
