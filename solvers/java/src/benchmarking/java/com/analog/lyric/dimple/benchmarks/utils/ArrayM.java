package com.analog.lyric.dimple.benchmarks.utils;

import java.util.Arrays;
import java.util.Iterator;

import com.google.common.primitives.Doubles;

public class ArrayM implements Iterable<int[]>
{
	private final double[] _data;

	private final int[] _dims;

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

	private static int getCardinality(int... dims)
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
			throw new IllegalArgumentException("Provided dimensions do not have the proper cardinality.");
		}
		return new ArrayM(_data, dims);
	}

	private int getIndex(int... coordinates)
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
	
	private double getRaw(int index)
	{
		return _data[index];
	}

	private void setRaw(int index, double value)
	{
		_data[index] = value;
	}
	
	public double get(int... coordinates)
	{
		if (coordinates.length != _dims.length)
		{
			throw new IllegalArgumentException("Mismatched quantity of coordinates and dimensions.");
		}
		int index = getIndex(coordinates);
		return _data[index];
	}
	
	public void set(double value, int... coordinates)
	{
		if (coordinates.length != _dims.length)
		{
			throw new IllegalArgumentException("Mismatched quantity of coordinates and dimensions.");
		}
		int index = getIndex(coordinates);
		_data[index] = value;
	}

	public ViewM slice(Indexer slice)
	{
		return new ViewM(this, slice);
	}
	
	public ViewM slice()
	{
		return slice(Indexer.All);
	}

	public ViewM slice(int just)
	{
		return slice(new Indexer(just));
	}

	public ViewM slice(int lowIndex, int highIndex)
	{
		return slice(new Indexer(lowIndex, highIndex));
	}

	public ViewM slice(int lowIndex, int stride, int highIndex)
	{
		return slice(new Indexer(lowIndex, stride, highIndex));
	}

	public int[] getDimensions()
	{
		return _dims.clone();
	}	

	public interface MapFunction
	{
		double apply(double v);
	}
	
	public static MapFunction compose(final MapFunction f, final MapFunction g)
	{
		return new MapFunction()
		{
			public double apply(double x)
			{
				return g.apply(f.apply(x));
			}
		};
	}
	
	public ArrayM transform(MapFunction transformer)
	{
		for (int i = 0; i < getCardinality(); i++)
		{
			setRaw(i, transformer.apply(getRaw(i)));
		}
		return this;
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
		if (scale != 1.0) // TODO epsilon?
		{
			for (int i = 0; i < getCardinality(); i++)
			{
				setRaw(i, (getRaw(i) - min) / scale);
			}
		}
		return this;
	}

	@Override
	public Iterator<int[]> iterator()
	{
		return slice().iterator();
	}	
}
