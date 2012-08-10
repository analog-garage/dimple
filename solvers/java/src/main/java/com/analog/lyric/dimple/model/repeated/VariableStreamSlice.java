package com.analog.lyric.dimple.model.repeated;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.VariableBase;


public class VariableStreamSlice implements IVariableStreamSlice
{
	private double _start;
	private double _increment;
	private double _end;
	private VariableStreamBase _stream;
	private double _currentNext;
	private double _currentFirst;
	
	public VariableStreamSlice(double start, double increment, double end,VariableStreamBase stream)
	{
		_start = start;
		_end = end;
		_stream = stream;
		_increment = increment;
		_currentNext = (int)_start;
		_currentFirst = (int)_start;
	}
	
	public void backup(double howMuch) 
	{
		if (howMuch < 1)
			throw new DimpleException("must backup a positive amount");
		
		double newNext = _currentNext-howMuch;
		
		if (newNext < _currentFirst)
			throw new DimpleException("can't go backwards in time");
		
		for (double i = newNext; i < _currentNext; i++)
			_stream.release(i);
		
		_currentNext = newNext;
	}
	
	public void reset()
	{
		_currentNext = _currentNext-_currentFirst+_start;
		_currentFirst = _start;
	}
	
	public VariableBase getNext() 
	{			
		if (!hasNext())
			throw new DimpleException("out of data");
		
		VariableBase tmp =  _stream.getAndAddReference(_currentNext);
		_currentNext += _increment;
		return tmp;
	}
	
	public ArrayList<VariableBase> releaseFirst() 
	{
		if (_currentFirst == _currentNext)
			throw new DimpleException("can't release before it's used");
		ArrayList<VariableBase> tmp =  _stream.release(_currentFirst);
		
		_currentFirst += _increment;
		
		return tmp;
	}
	
	public boolean hasNext() 
	{
		if (_currentNext > _end)
		{
			return false;
		}
		return _stream.variableAvailableFor(_currentNext);
	}
	
	public IVariableStreamSlice copy()
	{
		return new VariableStreamSlice(_start, _increment, _end,_stream);
	}
	
	public VariableStreamBase getStream()
	{
		return _stream;
	}
}
