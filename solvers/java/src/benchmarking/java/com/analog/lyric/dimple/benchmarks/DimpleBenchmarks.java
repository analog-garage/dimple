package com.analog.lyric.dimple.benchmarks;

import java.io.IOException;

import com.analog.lyric.benchmarking.BenchmarkRunner;

public class DimpleBenchmarks
{
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException
	{
		BenchmarkRunner.runBenchmarkPackage("com.analog.lyric.dimple.benchmarks");
//		BenchmarkRunner.runBenchmarkClass("com.analog.lyric.dimple.benchmarks.imageDenoising.ImageDenoisingBenchmark");
//		BenchmarkRunner.runBenchmarkClass("com.analog.lyric.dimple.benchmarks.stereoVision.StereoVisionBenchmark");
//		BenchmarkRunner.runBenchmarkClass("com.analog.lyric.dimple.benchmarks.hmm.hmmBenchmark");
	}
}
