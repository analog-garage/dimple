package com.analog.lyric.dimple.model.repeated;

import java.util.ArrayList;
import java.util.LinkedList;

import com.analog.lyric.dimple.model.DimpleException;

public class DoubleArrayDataSource implements IDataSource 
{
	private LinkedList<double[]> _data;

	public DoubleArrayDataSource()
	{
		_data = new LinkedList<double[]>();
	}

	public DoubleArrayDataSource(double [][] arr)
	{
		_data = new LinkedList<double[]>();
		
		add(arr);
	}

	public DoubleArrayDataSource(ArrayList<double[]> arr)
	{
		_data = new LinkedList<double[]>();
		
		for (double [] data : arr)
			_data.add(data);
	}

	public  void add(double[][] data)
	{
		for (int i = 0; i < data.length; i++)
			_data.add(data[i]);		
	}

	public  void add(double[] data)
	{
		_data.add(data);
	}
	
	public boolean hasNext()
	{
		return _data.size() > 0;
			
	}
	
	public Object getNext() 
	{
		if (_data.size() <= 0)
			throw new DimpleException("ACK!");
		
		return _data.pollFirst();
	}
	
	double [] peek()
	{
		return _data.peek();
	}

	
}
