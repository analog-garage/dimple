package com.analog.lyric.dimple.benchmarks.utils;

public class Indexer
{
	private final int _lowIndex;
	private final int _stride;
	private final int _highIndex;

	public Indexer(int lowIndex, int stride, int highIndex)
	{
		if (stride < 1)
		{
			throw new IllegalArgumentException("Stride must be positive.");
		}
		if (highIndex < lowIndex)
		{
			throw new IllegalArgumentException(
					"High index may not be less than low index.");
		}
		_lowIndex = lowIndex;
		_stride = stride;
		_highIndex = highIndex;
	}

	public Indexer(int lowIndex, int highIndex)
	{
		this(lowIndex, 1, highIndex);
	}

	public Indexer(int index)
	{
		this(index, index);
	}

	// Only used by All
	private Indexer(boolean x)
	{
		_lowIndex = 0;
		_stride = 0;
		_highIndex = 0;
	}

	public static final Indexer All = new Indexer(true);

	public int getLowIndex()
	{
		return _lowIndex;
	}

	public int getHighIndex()
	{
		return _highIndex;
	}

	public int getStride()
	{
		return _stride;
	}

	public int indexAfter(int index)
	{
		int proposal;
		if (index == -1)
		{
			proposal = _lowIndex;
		}
		else
		{
			proposal = index + _stride;
		}
		if (proposal <= _highIndex)
		{
			return proposal;
		}
		return -1;
	}
	
	public int order()
	{
		if (this == All)
		{
			return -1;
		}
		else
		{
			return (_highIndex - _lowIndex) / _stride + 1;
		}
	}
}
