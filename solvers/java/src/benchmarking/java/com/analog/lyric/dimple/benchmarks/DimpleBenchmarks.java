package com.analog.lyric.dimple.benchmarks;

import java.io.IOException;

import com.analog.lyric.benchmarking.BenchmarkRunner;

public class DimpleBenchmarks
{
	public static void main(String[] args)
		throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException
	{
		// Run a package:
		BenchmarkRunner.runBenchmarkPackage("com.analog.lyric.dimple.benchmarks");
		
		// Run a class:
//		BenchmarkRunner.runBenchmarkClass("com.analog.lyric.dimple.benchmarks.imageDenoising.ImageDenoisingBenchmark");
		
		// Run a method:
//		BenchmarkRunner.runBenchmarkMethod("com.analog.lyric.dimple.benchmarks.hmm.hmmBenchmark.hmmGibbs100000x4x4()");
	}
}
