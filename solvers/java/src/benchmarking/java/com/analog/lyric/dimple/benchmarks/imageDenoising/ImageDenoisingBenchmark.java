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
	@Benchmark(warmupIterations = 0, iterations = 2)
	public boolean imageDenoisingSumProduct() throws IOException
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		int iterations = 10;
		fg.getSolver().setNumIterations(iterations);

		int imageDimension = 100;

		String imageFileName = "images/1202.4002.3.png";
		URL urlImage = this.getClass().getResource(imageFileName);
		String factorFileName = "imageStats/factorTableValues300dpi.csv";

		int xImageOffset = 800;
		int yImageOffset = 1925;
		int xImageSize = imageDimension;
		int yImageSize = imageDimension;
		int xBlockSize = 4;
		int yBlockSize = 4;
		double noiseSigma = 1.0;

		// % Get input images and plot them
		ArrayM likelihoods = noisyImageInput(urlImage, noiseSigma,
				xImageOffset, yImageOffset, xImageSize, yImageSize);
		
		// figure(1);
		// screenSize = get(0,'ScreenSize');
		// figure('Position',[screenSize(3)/8 screenSize(4)/8 3*screenSize(3)/4
		// 3*screenSize(4)/4])
		// subplot(2,2,1);
		// imagesc(scaledImage);
		// colormap(gray);
		// title('Original binary image');
		// subplot(2,2,2);
		// imagesc(noisyImage);
		// colormap(gray);
		// title('Noisy image');
		// subplot(2,2,3);
		// imagesc(noisyImage > 0);
		// colormap(gray);
		// title('Noisy binary image');
		// drawnow;

		boolean verbose = true;
		
		ImageDenoisingGraph imageDenoisingGraph = new ImageDenoisingGraph(fg, factorFileName,
				xImageSize, yImageSize, xBlockSize,
				yBlockSize, verbose); 
		
		imageDenoisingGraph.setInput(likelihoods);
		
		
		// fprintf('Starting solver\n');
		// t=tic;
		// if (~showIntermedateResults) % Solve without showing intermediate
		// results
		 fg.solve();
		// else % Solve showing intermediate results
		// fg.Solver.useMultithreading(true);
		// fg.initialize();
		//
		// for i=1:iterations
		// fprintf('Iteration: %d\n', i);
		// fg.Solver.iterate();
		// output = Vs.Value;
		// subplot(2,2,4);
		// imagesc(output);
		// colormap(gray);
		// title('Intermediate result');
		// drawnow;
		// end
		// end
		// solveTime = toc(t);
		// fprintf('Solve time: %.1f seconds\n', solveTime);
		//
		// % Show the result
		// output = Vs.Value;
		 
		GeneratorFunction generator = new GeneratorFunction() {

			int y;
			int x;
			private ImageDenoisingGraph _imageDenoisingGraph;
			
			@Override
			public double apply()
			{
				return _imageDenoisingGraph.getValue(y, x);
			}

			private GeneratorFunction init(ImageDenoisingGraph imageDenoisingGraph)
			{
				_imageDenoisingGraph = imageDenoisingGraph;
				return this;
			}
		}.init(imageDenoisingGraph);
		 
		ArrayM output = ArrayM.generate(generator, new int[] { yImageSize, xImageSize } );
		Image.saveImage("denoise.png", output.normalize().transform(Image.contrastCurve));
		 
		// subplot(2,2,4);
		// imagesc(output);
		// title('Final result');
		// colormap(gray);
		//
		// % Compute the score of the result
		// score = fg.Score;
		// fprintf('Score of result (BP): %f\n', score);
		//
		//
		//
		//
		//
		//
		//
		//
		//

		// imageDenoising(fg, "images", 75, "sumproduct");
		return false;
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

	private MapFunction threshold(double level)
	{
		return new Threshold(level);
	}

	private ArrayM noisyImageInput(URL urlImage, double noiseSigma,
			int xImageOffset, int yImageOffset, int xImageSize, int yImageSize)
			throws IOException
	{
		ArrayM image = Image.loadImage(urlImage);
		image = image.slice(yImageOffset, yImageOffset + yImageSize - 1)
				.slice(xImageOffset, xImageOffset + xImageSize - 1).get();
		image.transform(ArrayM.compose(threshold(0.5), new MapFunction()
		{
			public double apply(double v)
			{
				return v * 2.0 - 1.0;
			}
		}));
		ArrayM noiseImage = ArrayM.generate(
				new NoiseGenerator(0.0, noiseSigma), yImageSize, xImageSize);
		ArrayM noisyImage = image.clone().add(noiseImage);
		final double noiseVariance = Math.pow(noiseSigma, 2.0);
		ArrayM llr = noisyImage.clone().transform(new MapFunction()
		{
			public double apply(double v)
			{
				return -2.0 * v / noiseVariance;
			}
		});
		ArrayM likelihoods = llr.clone().transform(new MapFunction()
		{
			public double apply(double v)
			{
				return 1.0 / (1.0 + Math.exp(v));
			}
		});
		return likelihoods;
	}
}
