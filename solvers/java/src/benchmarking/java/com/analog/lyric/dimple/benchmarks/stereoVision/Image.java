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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class Image
{
	private BufferedImage _image;

	public Image(int width, int height)
	{
		_image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
	}

	public Image(BufferedImage image)
	{
		_image = image;
	}

	public static Image load(URL urlImageL) throws IOException
	{
		return new Image(ImageIO.read(urlImageL));
	}

	public void save(String path) throws IOException
	{
		ImageIO.write(_image, "png", new File(path));
	}

	public double getPixel(int x, int y)
	{
		int rgb = _image.getRGB(x, y);
		Color color = new Color(rgb);
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		double result = 0.2989 * r + 0.5870 * g + 0.1140 * b;
		return result;
	}

	public void setPixel(int x, int y, double v)
	{
		// Contrast curve...

		// Scaled sigmoid
		final double s = 0.99;
		final double bp = (1 + s) / 2;
		final double b = Math.log(bp / (1 - bp));
		double a = v * s + (1 - s) / 2;
		double c = Math.log(a / (1 - a));
		double vp = (c + b) / 2 / b;

		// Skew out the blacks
		double s2 = 2.0;
		vp = (1.0 - Math.exp(-s2 * vp)) / (1.0 - Math.exp(-s2));

		int level = (int) (255.0 * vp);
		Color color = new Color(level, level, level);
		int rgb = color.getRGB();
		_image.setRGB(x, y, rgb);
	}

	public int getWidth()
	{
		return _image.getWidth();
	}

	public int getHeight()
	{
		return _image.getHeight();
	}
}
