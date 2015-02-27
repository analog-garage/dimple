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

package com.analog.lyric.dimple.benchmarks.stereoVision;

import java.io.IOException;

import com.analog.lyric.benchmarking.Benchmark;
import com.analog.lyric.benchmarking.utils.doublespace.DoubleSpace;
import com.analog.lyric.benchmarking.utils.functional.Functions;
import com.analog.lyric.dimple.benchmarks.utils.Image;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;

@SuppressWarnings({"null", "deprecation"})
public class StereoVisionBenchmark
{
	private final boolean saveResult = false;
	
	@Benchmark(warmupIterations = 0, iterations = 2)
	public boolean stereoVisionArtScaledGibbs() throws IOException
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		fg.setOption(GibbsOptions.numSamples, 100);
		depthInference(fg, "art_scaled", 75, "gibbs");
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 2)
	public boolean stereoVisionArtScaledSumProduct() throws IOException
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		fg.setOption(BPOptions.iterations, 10);
		depthInference(fg, "art_scaled", 75, "sumproduct");
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 2)
	public boolean stereoVisionArtScaledMinSum() throws IOException
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.minsum.Solver());
		fg.setOption(BPOptions.iterations, 10);
		depthInference(fg, "art_scaled", 75, "minsum");
		return false;
	}

	@SuppressWarnings("unused")
	private void depthInference(FactorGraph fg, String dataSetName, int depth, String saveLabel) throws IOException
	{
		Dataset dataset = new Dataset(dataSetName);
		
		StereoVisionGraph stereoVisionGraph = new StereoVisionGraph(fg, depth, dataset.getImageL(), dataset.getImageR());
		fg.solve();

		if (saveResult && saveLabel != null)
		{
			DoubleSpace result = stereoVisionGraph.getValueImage();
			Functions.normalize(result).transform(Image.contrastCurve);
			String resultPath = String.format("%s_%s.png", dataSetName, saveLabel);
			Image.save(resultPath, result);
		}

		double score = fg.getScore();
	}
}
