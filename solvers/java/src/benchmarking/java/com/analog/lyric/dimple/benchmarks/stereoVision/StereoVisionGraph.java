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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Discrete;

public class StereoVisionGraph
{
	static final double ed = 0.01;
	static final double ep = 0.05;
	static final double sigmaD = 8;
	static final double sigmaP = 0.6;
	static final double sigmaF = 0.3;
	
	private int _height;
	private int _width;
	private int _depthRange;
	private Discrete[][] _variables;

	public StereoVisionGraph(FactorGraph fg, int depthRange, Image imageL,
			Image imageR)
	{
		_height = imageL.getHeight();
		_width = imageL.getWidth();
		_depthRange = depthRange;

		// Create variables
		DiscreteDomain depthDomain = DiscreteDomain.range(1, depthRange);
		_variables = new Discrete[_height][_width];		
		for (int y = 0; y < _height; y++)
		{
			for (int x = 0; x < _width; x++)
			{
				Discrete variable = new Discrete(depthDomain);
				variable.setInput(rho_d(x, y, depthRange, imageL, imageR));
				_variables[y][x] = variable;
			}
		}

		// Create factors
		rho_p factorFunction = new rho_p();
		for (int y = 0; y < _height; y++)
		{
			for (int x = 0; x < _width - 1; x++)
			{
				fg.addFactor(factorFunction, _variables[y][x],
						_variables[y][x + 1]);
			}
		}
		for (int y = 0; y < _height - 1; y++)
		{
			for (int x = 0; x < _width; x++)
			{
				fg.addFactor(factorFunction, _variables[y][x],
						_variables[y + 1][x]);
			}
		}
	}

	public Image getValueImage()
	{
		// The image includes a 10-pixel gray scale reference on the right.
		Image image = new Image(_width + 10, _height);
		for (int y = 0; y < _height; y++)
		{
			for (int x = 0; x < _width; x++)
			{
				int value = (Integer) _variables[y][x].getValue();
				image.setPixel(x, y, (double) value / _depthRange);
			}
			double depth = ((double) ((int) ((double) y / _height * _depthRange)))
					/ _depthRange;
			for (int x = 0; x < 10; x++)
			{
				image.setPixel(_width + x, y, depth);
			}
		}
		return image;
	}

	private static double[] rho_d(int x, int y, int depthRange, Image imageL,
			Image imageR)
	{
		double[] result = new double[depthRange];
		double[] birchfieldTomasi = birchfieldTomasi(x, y, depthRange, imageL,
				imageR);
		for (int depth = 0; depth < depthRange; depth++)
		{
			result[depth] = (1 - ed)
					* Math.exp(-birchfieldTomasi[depth] / sigmaD) + ed;
		}
		return result;
	}

	private static double[] birchfieldTomasi(int x, int y, int depthRange,
			Image imageL, Image imageR)
	{
		double[] result = new double[depthRange];
		double il1 = imageL.getPixel(x, y);
		for (int depth = 0; depth < depthRange; depth++)
		{
			if (x - depth < 0)
			{
				result[depth] = 5000.0;
			}
			else
			{
				double ir1 = imageR.getPixel(x - depth, y);
				result[depth] = Math.abs(il1 - ir1) / sigmaF;
			}
		}
		return result;
	}

	private static class rho_p extends FactorFunction
	{
		@Override
		public double eval(Object... args)
		{
			int ds = (Integer) args[0];
			int dt = (Integer) args[1];
			return (1 - ep) * Math.exp(-Math.abs(ds - dt) / sigmaP) + ep;
		}
	}

}
