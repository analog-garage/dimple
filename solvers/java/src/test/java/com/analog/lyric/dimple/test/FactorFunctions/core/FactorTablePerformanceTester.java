package com.analog.lyric.dimple.test.FactorFunctions.core;

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
	
	FactorTablePerformanceTester(IFactorTable table, int iterations)
	{
		_table = table;
		_iterations = iterations;
		_random = new Random(42);
		_timer = new Stopwatch();
	}
	
	public double testEvalAsFactorFunction()
	{
		_random.setSeed(23);
		Object [][] argrows = new Object[_iterations][];
		for (int i = 0; i < _iterations; ++i)
		{
			argrows[i] = randomArguments();
		}

		// warmup
		for (Object[] args : argrows)
		{
			_table.evalAsFactorFunction(args);
		}

		_timer.reset();
		_timer.start();

		for (Object[] args : argrows)
		{
			_table.evalAsFactorFunction(args);
		}

		 _timer.stop();
		 long ns = _timer.elapsed(TimeUnit.NANOSECONDS);
		 return logTime("evalAsFactorFunction", ns / _iterations, "call");
	}
	
	public double testGet()
	{
		_random.setSeed(42);
		int [][] rows = new int[_iterations][];
		for (int i = 0; i < _iterations; ++i)
		{
			rows[i] = randomIndices();
		}
		
		// warmup
		for (int[] indices : rows)
		{
			_table.get(indices);
		}

		_timer.reset();
		_timer.start();

		for (int[] indices : rows)
		{
			_table.get(indices);
		}

		_timer.stop();
		 long ns = _timer.elapsed(TimeUnit.NANOSECONDS);
		 return logTime("get", ns / _iterations, "call");
	}

	public double testGetWeightIndexFromTableIndices()
	{
		_random.setSeed(42);
		int [][] rows = new int[_iterations][];
		for (int i = 0; i < _iterations; ++i)
		{
			rows[i] = randomIndices();
		}
		
		// warmup
		for (int[] indices : rows)
		{
			_table.getWeightIndexFromTableIndices(indices);
		}

		 _timer.reset();
		 _timer.start();
		 
		 for (int[] indices : rows)
		 {
			 _table.getWeightIndexFromTableIndices(indices);
		 }
		 
		 _timer.stop();
		 long ns = _timer.elapsed(TimeUnit.NANOSECONDS);
		 return logTime("getWeightIndexFromTableIndices", ns / _iterations, "call");
	}
	
	public double testGetWeightForIndices()
	{
		_random.setSeed(43);
		int [][] rows = new int[500][];
		for (int i = 0; i < rows.length; ++i)
		{
			rows[i] = randomIndices();
		}

		@SuppressWarnings("unused")
		double total = 0.0;
		
		// warmup
		final NewFactorTable newTable = getNewTable();
		if (newTable != null)
		{
			for (int[] indices : rows)
			{
				total += newTable.getWeightForIndices(indices);
			}
		}
		else
		{
			double[] weights = _table.getWeights();
			for (int[] indices : rows)
			{
				total += weights[_table.getWeightIndexFromTableIndices(indices)];
			}
		}
		
		total = 0.0;
		 _timer.reset();
		 if (newTable != null)
		 {
			 _timer.start();
			 for (int i = _iterations; --i>=0;)
			 {
				 for (int[] indices : rows)
				 {
					 total += newTable.getWeightForIndicesDense(indices);
				 }
			 }
		 }
		 else
		 {
			 _timer.start();
			 for (int i = _iterations; --i>=0;)
			 {
				 double[] weights = _table.getWeights();
				 for (int[] indices : rows)
				 {
					 total += weights[_table.getWeightIndexFromTableIndices(indices)];
				 }
			 }
		 }
		 
		 _timer.stop();
		 long ns = _timer.elapsed(TimeUnit.NANOSECONDS);
		 return logTime("getWeightForIndices", ns / (_iterations * rows.length), "call");
	}

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
	
	private double logTime(String name, double time, String unit)
	{
		if (showLog)
		{
			System.out.format("%s.%s: %f/%s\n", _table.getClass().getSimpleName(), name, time, unit);
		}
		return time;
	}
}
