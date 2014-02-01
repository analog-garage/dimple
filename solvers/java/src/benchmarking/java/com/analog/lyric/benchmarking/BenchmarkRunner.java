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
	private static BenchmarkCreator _benchmarkCreator;
	private static DataCollector _executionTimeCollector = new ExecutionTimeCollector();
	private static DataCollector _gcDataCollector = new GCDataCollector();
	private static MemoryUsageSamplingCollector _memoryUsageSamplingCollector = new MemoryUsageSamplingCollector();

	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, IOException
	{
		System.out.println("Classpath:");
		System.out.println(System.getProperty("java.class.path"));

		if (args.length != 1)
		{
			throw new IllegalArgumentException("Specify benchmark class.");
		}
		
		String benchmarkPackageName = args[0];
		System.out.println("Benchmark package name:");
		System.out.println(benchmarkPackageName);
		runBenchmarkPackage(benchmarkPackageName);

//		String testClassName = args[0];
//		System.out.println("Class name:");
//		System.out.println(testClassName);
//		runBenchmarkClass(testClassName);

		BenchmarkDataset benchmarkDataset = _benchmarkCreator
				.getBenchmarkDataset();

		String configuration = benchmarkDataset.getProperties().getProperty(
				"hostname");
		benchmarkDataset.getProperties().setProperty("configuration",
				configuration);

		BenchmarkDatasetXmlSerializer serializer = new BenchmarkDatasetXmlSerializer();
		String outputFilename = String.format(
				"%s_%s",
				benchmarkPackageName,
				_benchmarkCreator.getBenchmarkDataset().getProperties()
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

	private static void runBenchmarkPackage(String packageName)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException
	{
		ClassPath classpath = ClassPath
				.from(ClassLoader.getSystemClassLoader());
		for (ClassPath.ClassInfo classInfo : classpath
				.getTopLevelClassesRecursive(packageName))
		{
			if (classInfo.getSimpleName().endsWith("Benchmark"))
			{
				// TODO: could pass class instead of its name?
				runBenchmarkClass(classInfo.getName());
			}
		}
	}

	private static void runBenchmarkClass(String benchmarkClassName)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException
	{
		Class<?> testClass = Class.forName(benchmarkClassName);
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

		_benchmarkCreator = new BenchmarkCreator(benchmarkClassName);

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

			_benchmarkCreator.doRun(m.getName(), annotation.warmupIterations(),
					annotation.iterations(), annotation.doGC(), runnable,
					_executionTimeCollector, _gcDataCollector,
					_memoryUsageSamplingCollector);
		}
	}
}
