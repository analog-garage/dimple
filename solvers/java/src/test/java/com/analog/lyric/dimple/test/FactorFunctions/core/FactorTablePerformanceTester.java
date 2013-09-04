package com.analog.lyric.dimple.test.FactorFunctions.core;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.NewFactorTable;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.google.common.base.Stopwatch;

public class FactorTablePerformanceTester
{
	private final IFactorTable _table;
	private final Stopwatch _timer;
	private final int _iterations;
	private final Random _random;
	public boolean showLog = true;
	
	/*---------------
	 * Construction
	 */
	
	FactorTablePerformanceTester(IFactorTable table, int iterations)
	{
		_table = table;
		_iterations = iterations;
		_random = new Random(42);
		_timer = new Stopwatch();
	}
	
	/*------------
	 * Test cases
	 */
	
	public void testAll()
	{
		testEvalAsFactorFunction();
		testGet();
		testGetWeightIndexFromTableIndices();
		testGetWeightForIndices();
		testSumProductUpdate();
	}
	
	public void testEvalAsFactorFunction()
	{
		_random.setSeed(23);
		final Object [][] argrows = new Object[_iterations][];
		for (int i = 0; i < _iterations; ++i)
		{
			argrows[i] = randomArguments();
		}

		Runnable test = new Runnable() {
			@Override
			public void run()
			{
				for (Object[] args : argrows)
				{
					_table.evalAsFactorFunction(args);
				}
			}
		};
		
		runTest("evalAsFactorFunction", test);
	}
	
	public void testGet()
	{
		_random.setSeed(42);
		final int [][] rows = new int[_iterations][];
		for (int i = 0; i < _iterations; ++i)
		{
			rows[i] = randomIndices();
		}
		
		Runnable test = new Runnable() {
			@Override
			public void run()
			{
				for (int[] indices : rows)
				{
					_table.get(indices);
				}
			}
		};
		
		runTest("get", test);
	}

	public void testGetWeightIndexFromTableIndices()
	{
		_random.setSeed(42);
		final int [][] rows = new int[_iterations][];
		for (int i = 0; i < _iterations; ++i)
		{
			rows[i] = randomIndices();
		}
		
		Runnable test = new Runnable() {
			@Override
			public void run()
			{
				for (int[] indices : rows)
				{
					_table.getWeightIndexFromTableIndices(indices);
				}
			}
		};
		
		 runTest("getWeightIndexFromTableIndices", test);
	}
	
	public void testGetWeightForIndices()
	{
		_random.setSeed(43);
		final int [][] rows = new int[500][];
		for (int i = 0; i < rows.length; ++i)
		{
			rows[i] = randomIndices();
		}

		Runnable test = new Runnable() {
			@SuppressWarnings("unused")
			double total = 0.0;

			@Override
			public void run()
			{
				for (int i = _iterations; --i>=0;)
				{
					for (int[] indices : rows)
					{
						total += _table.getWeightForIndices(indices);
					}
				}
			}
		};
		
		 runTest("getWeightForIndices", rows.length, "call", 42, test);
	}
	
	public void testSumProductUpdate()
	{
		final DiscreteDomain[] domains = _table.getDomains();
		final int numPorts_ = domains.length;
		final double[][] outMsgs_ = new double[domains.length][];
		final double[][] inMsgs_ = new double[domains.length][];
		
		for (int i = 0; i < domains.length; ++i)
		{
			int domainSize = domains[i].size();
			outMsgs_[i] = new double[domainSize];
			inMsgs_[i] = new double[domainSize];
			for (int j = 0; j < domainSize; ++j)
			{
				double d = _random.nextDouble();
				inMsgs_[i][j] = d;
			}
		}
		
		if (getNewTable() == null)
		{
			Runnable original = new Runnable() {
				@Override
				public void run()
				{
					final int numPorts = numPorts_;
					final double[][] outMsgs = outMsgs_;
					final double[][] inMsgs = inMsgs_;

					for (int iteration = _iterations; --iteration>=0;)
					{
						int[][] table = _table.getIndices();
						double[] values = _table.getWeights();
						int tableLength = table.length;

						for (int outPortNum = 0; outPortNum < numPorts; outPortNum++)
						{
							double[] outputMsgs = outMsgs[outPortNum];

							int outputMsgLength = outputMsgs.length;
							for (int i = 0; i < outputMsgLength; i++) outputMsgs[i] = 0;

							for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
							{
								double prob = values[tableIndex];
								int[] tableRow = table[tableIndex];
								int outputIndex = tableRow[outPortNum];

								for (int inPortNum = 0; inPortNum < numPorts; inPortNum++)
									if (inPortNum != outPortNum)
									{
										prob *= inMsgs[inPortNum][tableRow[inPortNum]];
									}
								outputMsgs[outputIndex] += prob;
							}

							double sum = 0;
							for (int i = 0; i < outputMsgLength; i++) sum += outputMsgs[i];

							for (int i = 0; i < outputMsgLength; i++)
								outputMsgs[i] /= sum;
						}
					}
				}
			};

			runTest("sumProductUpdateOriginal", "call", 42, original);

			Runnable faster = new Runnable() {
				@Override
				public void run()
				{
					final int numPorts = numPorts_;
					final double[][] outMsgs = outMsgs_;
					final double[][] inMsgs = inMsgs_;

					for (int iteration = _iterations; --iteration>=0;)
					{
						final int[][] table = _table.getIndices();
						final double[] values = _table.getWeights();
						final int tableLength = table.length;

						for (int outPortNum = 0; outPortNum < numPorts; ++outPortNum)
						{
							double[] outputMsgs = outMsgs[outPortNum];

							int outputMsgLength = outputMsgs.length;
							Arrays.fill(outputMsgs,  0);

							for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
							{
								double prob = values[tableIndex];
								int[] tableRow = table[tableIndex];
								int outputIndex = tableRow[outPortNum];

								for (int inPortNum = 0; inPortNum < outPortNum; ++inPortNum)
									prob *= inMsgs[inPortNum][tableRow[inPortNum]];
								for (int inPortNum = outPortNum + 1; inPortNum < numPorts; inPortNum++)
									prob *= inMsgs[inPortNum][tableRow[inPortNum]];
								outputMsgs[outputIndex] += prob;
							}

							double sum = 0;
							for (double w : outputMsgs)
								sum += w;

							for (int i = 0; i < outputMsgLength; ++i)
								outputMsgs[i] /= sum;
						}
					}
				}
			};

			runTest("sumProductUpdateFaster", "call", 42, faster);
		}

		if (getNewTable() != null)
		{
			Runnable unsafeIndices = new Runnable() {
				@Override
				public void run()
				{
					final NewFactorTable ftable = getNewTable();
					final int numPorts = numPorts_;
					final double[][] outMsgs = outMsgs_;
					final double[][] inMsgs = inMsgs_;

					for (int iteration = _iterations; --iteration>=0;)
					{
						final int[][] table = ftable.getIndicesSparseUnsafe();
						final int tableLength = table.length;
						
						final double[] values = ftable.getWeightsSparseUnsafe();

						for (int outPortNum = 0; outPortNum < numPorts; ++outPortNum)
						{
							double[] outputMsgs = outMsgs[outPortNum];

							int outputMsgLength = outputMsgs.length;
							Arrays.fill(outputMsgs, 0);

							for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
							{
								double prob = values[tableIndex];
								int[] tableRow = table[tableIndex];
								int outputIndex = tableRow[outPortNum];

								for (int inPortNum = 0; inPortNum < outPortNum; ++inPortNum)
									prob *= inMsgs[inPortNum][tableRow[inPortNum]];
								for (int inPortNum = outPortNum + 1; inPortNum < numPorts; inPortNum++)
									prob *= inMsgs[inPortNum][tableRow[inPortNum]];
								outputMsgs[outputIndex] += prob;
							}

							double sum = 0;
							for (double w : outputMsgs)
								sum += w;

							for (int i = 0; i < outputMsgLength; ++i)
								outputMsgs[i] /= sum;
						}
					}
				}
			};

			runTest("sumProductUpdateNew", "call", 42, unsafeIndices);
		}
	}
	
	public void testGibbsUpdateMessage()
	{
		final DiscreteDomain[] domains = _table.getDomains();
		final int numPorts_ = domains.length;
		final double[][] outMsgs_ = new double[domains.length][];
		final int[] inMsgs_ = new int[domains.length];
		
		for (int i = 0; i < numPorts_; ++i)
		{
			int domainSize = domains[i].size();
			outMsgs_[i] = new double[domainSize];
			inMsgs_[i] = _random.nextInt(domainSize);
		}

		// Modified version of gibbs.STableFactor.updateEdgeMessage(int)
		Runnable original = new Runnable() {
			@Override
			public void run()
			{
				final int numPorts = numPorts_;
				final double[][] outMsgs = outMsgs_;
				final int[] inMsgs = inMsgs_;
				final IFactorTable factorTable = _table;
				
				for (int iteration = _iterations; --iteration>=0;)
				{
					for (int outPortNum = 0; outPortNum < numPorts; ++outPortNum)
					{
						double[] outMessage = outMsgs[outPortNum];
						int outputMsgLength = outMessage.length;

						double[] factorTableWeights = factorTable.getPotentials();

						int[] inPortMsgs = new int[numPorts];
						for (int port = 0; port < numPorts; port++)
							inPortMsgs[port] = inMsgs[port];

						for (int outIndex = 0; outIndex < outputMsgLength; outIndex++)
						{
							inPortMsgs[outPortNum] = outIndex;

							int weightIndex = factorTable.getWeightIndexFromTableIndices(inPortMsgs);
							if (weightIndex >= 0)
								outMessage[outIndex] = factorTableWeights[weightIndex];
							else
								outMessage[outIndex] = Double.POSITIVE_INFINITY;

						}
					}
				}
			}
		};
		
		runTest("gibbsUpdateMessageOriginal", numPorts_, "call", 42, original);

		Runnable modified = new Runnable() {
			@Override
			public void run()
			{
				final int numPorts = numPorts_;
				final double[][] outMsgs = outMsgs_;
				final int[] inMsgs = inMsgs_;
				final IFactorTable factorTable = _table;
				
				for (int iteration = _iterations; --iteration>=0;)
				{
					for (int outPortNum = 0; outPortNum < numPorts; ++outPortNum)
					{
						double[] outMessage = outMsgs[outPortNum];
						int outputMsgLength = outMessage.length;

						int[] inPortMsgs = new int[numPorts];
						for (int port = 0; port < numPorts; port++)
							inPortMsgs[port] = inMsgs[port];

						for (int outIndex = 0; outIndex < outputMsgLength; outIndex++)
						{
							inPortMsgs[outPortNum] = outIndex;
							outMessage[outIndex] = factorTable.getWeightForIndices(inPortMsgs);
						}
					}
				}
			}
		};
		
		runTest("gibbsUpdateMessageModified", numPorts_, "call", 42, modified);

	}
	
	/*-----------------
	 * Private methods
	 */
	
	private NewFactorTable getNewTable()
	{
		if (_table instanceof NewFactorTable)
		{
			return (NewFactorTable)_table;
		}
		
		return null;
	}
	
	private Object[] randomArguments()
	{
		DiscreteDomain[] domains = _table.getDomains();
		Object[] arguments = new Object[domains.length];
		for (int i = 0; i < domains.length; ++i)
		{
			arguments[i] = domains[i].getElement(_random.nextInt(domains[i].size()));
		}
		
		return arguments;
	}
	
	private int[] randomIndices()
	{
		DiscreteDomain[] domains = _table.getDomains();
		int[] indices = new int[domains.length];
		for (int i = 0; i < domains.length; ++i)
		{
			indices[i] = _random.nextInt(domains[i].size());
		}
		return indices;
	}
	
	private double runTest(String name, int unitMultiplier, String unit, int seed, Runnable test)
	{
		_random.setSeed(seed);
		
		// Warmup
		test.run();
		
		_random.setSeed(seed);
		_timer.reset();
		_timer.start();
		test.run();
		_timer.stop();
		
		 long ns = _timer.elapsed(TimeUnit.NANOSECONDS);
		 return logTime(name, ns / (_iterations * unitMultiplier), unit);
	}
	
	private double runTest(String name, String unit, int seed, Runnable test)
	{
		return runTest(name, 1, unit, seed, test);
	}

	private double runTest(String name, Runnable test)
	{
		return runTest(name, "call", 42, test);
	}
	
	private double logTime(String name, double time, String unit)
	{
		if (showLog)
		{
			System.out.format("%s.%s: %f/%s\n", _table.getClass().getSimpleName(), name, time, unit);
		}
		return time;
	}
}
