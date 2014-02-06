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

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.google.common.reflect.ClassPath;

public class BenchmarkRunner
{
	private static DataCollector _executionTimeCollector = new ExecutionTimeCollector();
	private static DataCollector _gcDataCollector = new GCDataCollector();
	private static MemoryUsageSamplingCollector _memoryUsageSamplingCollector = new MemoryUsageSamplingCollector();

	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, IOException
	{
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Specify benchmark class.");
		}

		String benchmarkPackageName = args[0];
		runBenchmarkPackage(benchmarkPackageName);

		// String testClassName = args[0];
		// runBenchmarkClass(testClassName);
	}

	public static void runBenchmarkPackage(String packageName)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException
	{
		applyStrategy(packageName, new BenchmarkPackageRunnerStrategy(
				packageName));
	}

	public static void runBenchmarkClass(String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException
	{
		applyStrategy(className, new BenchmarkClassRunnerStrategy(className));
	}

	private interface Strategy
	{
		void apply(BenchmarkCreator benchmarkCreator)
				throws ClassNotFoundException, InstantiationException,
				IllegalAccessException, IOException;
	}

	private static void applyStrategy(String label, Strategy strategy)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException
	{
		System.out.println("Classpath:");
		System.out.println(System.getProperty("java.class.path"));

		BenchmarkCreator benchmarkCreator = new BenchmarkCreator(label);
		BenchmarkDataset benchmarkDataset = benchmarkCreator
				.getBenchmarkDataset();
		String configuration = benchmarkDataset.getProperties().getProperty(
				"hostname");
		benchmarkDataset.getProperties().setProperty("configuration",
				configuration);

		strategy.apply(benchmarkCreator);

		BenchmarkDatasetXmlSerializer serializer = new BenchmarkDatasetXmlSerializer();
		String outputFilename = String.format(
				"%s_%s",
				configuration,
				benchmarkCreator.getBenchmarkDataset().getProperties()
						.getProperty("create.date")).replaceAll("\\W+", "_")
				+ ".xml";

		try
		{
			serializer.serialize(new FileWriter(outputFilename),
					benchmarkDataset);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static class BenchmarkPackageRunnerStrategy implements Strategy
	{
		private final String _packageName;

		public BenchmarkPackageRunnerStrategy(String packageName)
		{
			_packageName = packageName;
		}

		public void apply(BenchmarkCreator benchmarkCreator)
				throws IOException, ClassNotFoundException,
				InstantiationException, IllegalAccessException
		{
			ClassPath classpath = ClassPath.from(ClassLoader
					.getSystemClassLoader());
			for (ClassPath.ClassInfo classInfo : classpath
					.getTopLevelClassesRecursive(_packageName))
			{
				if (classInfo.getSimpleName().endsWith("Benchmark"))
				{
					BenchmarkClassRunnerStrategy classRunner = new BenchmarkClassRunnerStrategy(
							classInfo.getName());
					classRunner.apply(benchmarkCreator);
				}
			}
		}
	}

	private static class BenchmarkClassRunnerStrategy implements Strategy
	{
		private final String _className;

		public BenchmarkClassRunnerStrategy(String className)
		{
			_className = className;
		}

		public void apply(BenchmarkCreator benchmarkCreator)
				throws ClassNotFoundException, InstantiationException,
				IllegalAccessException
		{
			Class<?> testClass = Class.forName(_className);
			final Object testClassInstance = testClass.newInstance();

			Method[] methods = testClass.getMethods();
			ArrayList<Method> annotated = new ArrayList<Method>();
			for (Method m : methods)
			{
				if (m.isAnnotationPresent(Benchmark.class))
				{
					annotated.add(m);
				}
			}

			for (Method m : annotated)
			{
				System.out.printf("Benchmark '%s'...\n", m.getName());

				Benchmark annotation = m.getAnnotation(Benchmark.class);

				final Method mf = m;
				IterationRunner runnable = new IterationRunner()
				{
					@Override
					public boolean run()
					{
						boolean abort = true;
						try
						{
							mf.invoke(testClassInstance);
							abort = false;
						}
						catch (IllegalArgumentException e)
						{
							e.printStackTrace();
						}
						catch (IllegalAccessException e)
						{
							e.printStackTrace();
						}
						catch (InvocationTargetException e)
						{
							e.printStackTrace();
						}
						return abort;
					}
				};

				benchmarkCreator.doRun(m.getName(),
						annotation.warmupIterations(), annotation.iterations(),
						annotation.doGC(), runnable, _executionTimeCollector,
						_gcDataCollector, _memoryUsageSamplingCollector);
			}
		}
	}
}
