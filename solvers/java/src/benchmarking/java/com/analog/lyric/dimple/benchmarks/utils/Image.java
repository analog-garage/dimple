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

package com.analog.lyric.dimple.benchmarks.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.analog.lyric.benchmarking.utils.doublespace.DoubleSpace;
import com.analog.lyric.benchmarking.utils.doublespace.DoubleSpaceFactory;
import com.analog.lyric.benchmarking.utils.functional.Functions;
import com.analog.lyric.benchmarking.utils.functional.TransformFunction;

public class Image
{
	public static DoubleSpace array2dOfImage(BufferedImage image)
	{
		DoubleSpace result = DoubleSpaceFactory.create(image.getHeight(), image.getWidth());
		for (int x = 0; x < image.getWidth(); x++)
		{
			for (int y = 0; y < image.getHeight(); y++)
			{
				int rgb = image.getRGB(x, y);
				Color color = new Color(rgb);
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				double v = 0.2989 * r + 0.5870 * g + 0.1140 * b;
				result.put(v, y, x);
			}
		}
		return result;
	}

	public static DoubleSpace loadImage(URL urlImage) throws IOException
	{
		BufferedImage image = ImageIO.read(urlImage);
		return array2dOfImage(image);
	}

	// TODO rename
	public static BufferedImage imageOfArray2d(DoubleSpace image)
	{
		image = DoubleSpaceFactory.copy(image);
		Functions.normalize(image);
		int[] dims = image.getDimensions();
		int width = dims[1];
		int height = dims[0];
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				double v = image.get(y, x);
				int level = (int) (255.0 * v);
				Color color = new Color(level, level, level);
				int rgb = color.getRGB();
				result.setRGB(x, y, rgb);
			}
		}
		return result;
	}

	public static void save(String path, DoubleSpace image) throws IOException
	{
		BufferedImage result = imageOfArray2d(image);
		ImageIO.write(result, "png", new File(path));
	}

	public final static TransformFunction contrastCurve = new TransformFunction()
	{
		final double s = 0.99;
		final double bp = (1 + s) / 2;
		final double b = Math.log(bp / (1 - bp));
		final double s2 = 2.0;

		public double apply(double v)
		{
			double a = v * s + (1 - s) / 2;
			double c = Math.log(a / (1 - a));
			double vp = (c + b) / 2 / b;
			return (1.0 - Math.exp(-s2 * vp)) / (1.0 - Math.exp(-s2));
		}
	};
}
