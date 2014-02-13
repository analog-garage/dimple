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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.google.common.primitives.Doubles;
import com.sun.media.sound.InvalidDataException;

public class ArrayM implements Iterable<int[]>
{
	protected final double[] _data;

	protected final int[] _dims;

	public ArrayM(double[] data, int... dims)
	{
		_data = data;
		_dims = dims;
	}

	public ArrayM(int... dims)
	{
		this(new double[getCardinality(dims)], dims);
	}

	public ArrayM clone()
	{
		return new ArrayM(_data.clone(), _dims.clone());
	}

	public static ArrayM ones(int... dims)
	{
		ArrayM result = new ArrayM(dims);
		for (int i = 0; i < getCardinality(dims); i++)
		{
			result._data[i] = 1.0;
		}
		return result;
	}

	public int getCardinality()
	{
		return getCardinality(_dims);
	}

	protected static int getCardinality(int... dims)
	{
		int result = 1;
		for (int dim : dims)
		{
			result *= dim;
		}
		return result;
	}

	public ArrayM reshape(int... dims)
	{
		if (getCardinality(_dims) != getCardinality(dims))
		{
			throw new IllegalArgumentException(
					"Provided dimensions do not have the proper cardinality.");
		}
		return new ArrayM(_data, dims);
	}

	protected int getIndex(int... coordinates)
	{
		if (coordinates.length != _dims.length)
		{
			throw new IllegalArgumentException(
					"Mismatched quantity of coordinates and dimensions.");
		}
		int index = 0;
		int scale = 1;
		for (int i = 0; i < coordinates.length; i++)
		{
			if ((coordinates[i] < 0) || (coordinates[i] > _dims[i] - 1))
			{
				throw new IllegalArgumentException("Coordinate out of bounds.");
			}
			index += coordinates[i] * scale;
			scale *= _dims[i];
		}
		return index;
	}

	protected double getRaw(int index)
	{
		return _data[index];
	}

	protected void setRaw(int index, double value)
	{
		_data[index] = value;
	}

	public double get(int... coordinates)
	{
		if (coordinates.length != _dims.length)
		{
			throw new IllegalArgumentException(
					"Mismatched quantity of coordinates and dimensions.");
		}
		int index = getIndex(coordinates);
		return _data[index];
	}

	public void set(double value, int... coordinates)
	{
		if (coordinates.length != _dims.length)
		{
			throw new IllegalArgumentException(
					"Mismatched quantity of coordinates and dimensions.");
		}
		int index = getIndex(coordinates);
		_data[index] = value;
	}

	public ViewM index(Indexer index)
	{
		return new ViewM(this, index);
	}

	public ViewM index()
	{
		return index(Indexer.All);
	}

	public ViewM index(int just)
	{
		return index(new Indexer(just));
	}

	public ViewM index(int lowIndex, int highIndex)
	{
		return index(new Indexer(lowIndex, highIndex));
	}

	public ViewM index(int lowIndex, int stride, int highIndex)
	{
		return index(new Indexer(lowIndex, stride, highIndex));
	}

	public int[] getDimensions()
	{
		return _dims.clone();
	}

	public interface MapFunction
	{
		double apply(double v);
	}

	public static MapFunction compose(final MapFunction g, final MapFunction f)
	{
		return new MapFunction()
		{
			public double apply(double x)
			{
				return g.apply(f.apply(x));
			}
		};
	}

	public ArrayM modify(MapFunction transformer)
	{
		for (int i = 0; i < getCardinality(); i++)
		{
			setRaw(i, transformer.apply(getRaw(i)));
		}
		return this;
	}

	public ArrayM map(MapFunction transformer)
	{
		ArrayM result = new ArrayM(getDimensions());
		for (int i = 0; i < getCardinality(); i++)
		{
			result.setRaw(i, transformer.apply(getRaw(i)));
		}
		return result;
	}

	public interface GeneratorFunction
	{
		double apply();
	}

	public static ArrayM generate(GeneratorFunction generator, int... dims)
	{
		ArrayM result = new ArrayM(dims);
		for (int i = 0; i < result.getCardinality(); i++)
		{
			result.setRaw(i, generator.apply());
		}
		return result;
	}

	public interface GeneratorWithCoordinatesFunction
	{
		double apply(int... coordinates);
	}

	public static ArrayM generate(GeneratorWithCoordinatesFunction generator,
			int... dims)
	{
		ArrayM result = new ArrayM(dims);
		for (int[] coordinates : result)
		{
			result.set(generator.apply(coordinates), coordinates);
		}
		return result;
	}

	public interface IterFunctionWithCoordinates
	{
		void apply(double value, int... coordinates);
	}

	public void iter(IterFunctionWithCoordinates fn)
	{
		for (int[] coordinates : this)
		{
			double value = get(coordinates);
			fn.apply(value, coordinates);
		}
	}

	public ArrayM add(ArrayM rhs)
	{
		if (!Arrays.equals(_dims, rhs._dims))
		{
			throw new IllegalArgumentException("ArrayM sizes must match.");
		}
		for (int i = 0; i < getCardinality(); i++)
		{
			_data[i] += rhs._data[i];
		}
		return this;
	}

	public ArrayM normalize()
	{
		double min = Doubles.min(_data);
		double max = Doubles.max(_data);
		double scale = max - min;
		for (int i = 0; i < getCardinality(); i++)
		{
			setRaw(i, (getRaw(i) - min) / scale);
		}
		return this;
	}

	@Override
	public Iterator<int[]> iterator()
	{
		return index().iterator();
	}

	public static ArrayM loadcsv(InputStream is) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		ArrayList<String> lines = new ArrayList<String>();
		while ((line = br.readLine()) != null)
		{
			lines.add(line);
		}
		int rows = lines.size();
		int columns = -1;
		ArrayM result = null;
		if (rows > 0)
		{
			int row = 0;
			for (String line2 : lines)
			{
				String[] tokens = line2.split(",");
				if (columns == -1)
				{
					columns = tokens.length;
					result = new ArrayM(rows, columns);
				}
				else if (columns != tokens.length)
				{
					throw new InvalidDataException(
							"All rows of csv text must have same quantity of columns.");
				}
				for (int column = 0; column < columns; column++)
				{
					double value = Double.parseDouble(tokens[column]);
					result.set(value, row, column);
				}
				row += 1;
			}
		}
		return result;
	}
}
