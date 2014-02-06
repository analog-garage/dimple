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

package com.analog.lyric.dimple.benchmarks.imageDenoising;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

import com.analog.lyric.benchmarking.Benchmark;
import com.analog.lyric.dimple.benchmarks.utils.ArrayM;
import com.analog.lyric.dimple.benchmarks.utils.Image;
import com.analog.lyric.dimple.benchmarks.utils.ArrayM.GeneratorFunction;
import com.analog.lyric.dimple.benchmarks.utils.ArrayM.MapFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;

public class ImageDenoisingBenchmark
{
	private final boolean saveResult = false;

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean imageDenoisingGibbs() throws IOException
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		com.analog.lyric.dimple.solvers.gibbs.SFactorGraph solver = (com.analog.lyric.dimple.solvers.gibbs.SFactorGraph)fg.getSolver();
		solver.setNumSamples(1600);

		int imageDimension = 100;
		int xImageOffset = 800;
		int yImageOffset = 1925;
		int xImageSize = imageDimension;
		int yImageSize = imageDimension;
		double noiseSigma = 1.0;
		imageDenoisingInference(fg, "images/1202.4002.3.png", "gibbs",
				imageDimension, xImageOffset, yImageOffset, xImageSize,
				yImageSize, noiseSigma);
		return false;
	}
	
	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean imageDenoisingSumProduct() throws IOException
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		fg.getSolver().setNumIterations(1);
		
		int imageDimension = 100;
		int xImageOffset = 800;
		int yImageOffset = 1925;
		int xImageSize = imageDimension;
		int yImageSize = imageDimension;
		double noiseSigma = 1.0;
		imageDenoisingInference(fg, "images/1202.4002.3.png", "sumproduct",
				imageDimension, xImageOffset, yImageOffset, xImageSize,
				yImageSize, noiseSigma);
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean imageDenoisingMinSum() throws IOException
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.minsum.Solver());
		fg.getSolver().setNumIterations(2);

		int imageDimension = 100;
		int xImageOffset = 800;
		int yImageOffset = 1925;
		int xImageSize = imageDimension;
		int yImageSize = imageDimension;
		double noiseSigma = 1.0;
		imageDenoisingInference(fg, "images/1202.4002.3.png", "minsum",
				imageDimension, xImageOffset, yImageOffset, xImageSize,
				yImageSize, noiseSigma);
		return false;
	}

	@SuppressWarnings("unused")
	public void imageDenoisingInference(FactorGraph fg, String imageFileName,
			String saveLabel, int imageDimension, int xImageOffset,
			int yImageOffset, int xImageSize, int yImageSize, double noiseSigma)
			throws IOException
	{
		final String factorFileName = "imageStats/factorTableValues300dpi.csv";
		final int xBlockSize = 4;
		final int yBlockSize = 4;
		URL urlImage = this.getClass().getResource(imageFileName);
		ArrayM likelihoods = noisyImageInput(urlImage, noiseSigma,
				xImageOffset, yImageOffset, xImageSize, yImageSize);
		ImageDenoisingGraph imageDenoisingGraph = new ImageDenoisingGraph(fg,
				factorFileName, xImageSize, yImageSize, xBlockSize, yBlockSize);
		imageDenoisingGraph.setInput(likelihoods);
		fg.solve();

		if (saveResult && saveLabel != null)
		{
			ArrayM output = imageDenoisingGraph.getValue();
			Image.saveImage("denoise.png",
					output.normalize().modify(Image.contrastCurve));
		}

		double score = fg.getScore();
	}

	private class Threshold implements MapFunction
	{
		private final double _threshold;

		public Threshold(double threshold)
		{
			_threshold = threshold;
		}

		@Override
		public double apply(double v)
		{
			return (v >= _threshold) ? 1.0 : 0.0;
		}
	}

	private class NoiseGenerator implements GeneratorFunction
	{
		private final double _mean;
		private final double _sigma;
		private final Random _random = new Random();

		public NoiseGenerator(double mean, double sigma)
		{
			_mean = mean;
			_sigma = sigma;
		}

		public double apply()
		{
			return _random.nextGaussian() * _sigma + _mean;
		}
	}

	private ArrayM noisyImageInput(URL urlImage, double noiseSigma,
			int xImageOffset, int yImageOffset, int xImageSize, int yImageSize)
			throws IOException
	{
		ArrayM image = Image.loadImage(urlImage);

		image = image.index(yImageOffset, yImageOffset + yImageSize - 1)
				.index(xImageOffset, xImageOffset + xImageSize - 1).get();

		ArrayM noiseImage = ArrayM.generate(
				new NoiseGenerator(0.0, noiseSigma), yImageSize, xImageSize);
		final double noiseVariance = Math.pow(noiseSigma, 2.0);

		image.modify(new Threshold(128));
		image.modify(new MapFunction()
		{
			public double apply(double v)
			{
				return v * 2.0 - 1.0;
			}
		});
		image.add(noiseImage);
		image.modify(new MapFunction()
		{
			// LLR
			public double apply(double v)
			{
				return -2.0 * v / noiseVariance;
			}
		});
		image.modify(new MapFunction()
		{
			// likelihood
			public double apply(double v)
			{
				return 1.0 / (1.0 + Math.exp(v));
			}
		});

		return image;
	}
}
