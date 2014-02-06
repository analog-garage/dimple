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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import com.analog.lyric.dimple.benchmarks.utils.ArrayM;
import com.analog.lyric.dimple.benchmarks.utils.ArrayM.GeneratorWithCoordinatesFunction;
import com.analog.lyric.dimple.benchmarks.utils.ArrayM.IterFunctionWithCoordinates;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Bit;
import com.google.common.primitives.Doubles;

public class ImageDenoisingGraph
{
	private Bit[][] _vs;
	private int _rows;
	private int _cols;

	ImageDenoisingGraph(final FactorGraph fg, final String factorFileName,
			final int xImageSize, final int yImageSize, final int xBlockSize,
			final int yBlockSize)
	{
		int blockSize = xBlockSize * yBlockSize;

		double[] factorTableValues = loadFactorTableValues(factorFileName);

		_rows = yImageSize;
		_cols = xImageSize;
		int blockRows = _rows - yBlockSize + 1;
		int blockCols = _cols - xBlockSize + 1;

		_vs = new Bit[_rows][_cols];
		for (int row = 0; row < _rows; row++)
		{
			for (int col = 0; col < _cols; col++)
			{
				_vs[row][col] = new Bit();
				_vs[row][col].setName(String.format("V_row%d_vv%d", row, col));
			}
		}

		DiscreteDomain[] domains = new DiscreteDomain[blockSize];
		for (int i = 0; i < domains.length; i++)
		{
			domains[i] = DiscreteDomain.bit();
		}
		IFactorTable factorTable = FactorTable.create(domains);
		factorTable.setWeightsDense(factorTableValues);

		Bit[] varPatch = new Bit[blockSize];
		for (int yList = 0; yList < blockRows; yList++)
		{
			for (int xList = 0; xList < blockCols; xList++)
			{
				int blockOffset = 0;
				for (int yb = 0; yb < yBlockSize; yb++)
				{
					for (int xb = 0; xb < xBlockSize; xb++)
					{
						varPatch[blockOffset] = _vs[yb + yList][xb + xList];
						blockOffset = blockOffset + 1;
					}
				}
				fg.addFactor(factorTable, varPatch);
			}
		}
	}

	private double[] loadFactorTableValues(String name)
	{
		InputStream i = this.getClass().getResourceAsStream(name);
		try
		{
			Scanner scanner = new Scanner(i);
			ArrayList<Double> m = new ArrayList<Double>();
			while (scanner.hasNextDouble())
			{
				m.add(scanner.nextDouble());
			}
			double[] data = Doubles.toArray(m);
			return data;
		}
		finally
		{
			if (i != null)
			{
				try
				{
					i.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	public void setInput(ArrayM likelihoods)
	{
		likelihoods.iter(new IterFunctionWithCoordinates()
		{
			public void apply(double value, int... coordinates)
			{
				int row = coordinates[0];
				int col = coordinates[1];
				_vs[row][col].setInput(value);
			}
		});
	}

	public double getValue(int... coordinates)
	{
		int row = coordinates[0];
		int col = coordinates[1];
		return (Integer) (_vs[row][col].getValue());
	}

	public ArrayM getValue()
	{
		return ArrayM.generate(new GeneratorWithCoordinatesFunction()
		{
			public double apply(int... coordinates)
			{
				int row = coordinates[0];
				int col = coordinates[1];
				return (Double) _vs[row][col].getValue();
			}
		}, _rows, _cols);
	}
}
