package com.analog.lyric.util.misc;

import java.util.Iterator;
import java.util.NoSuchElementException;


public class IndexCounter implements Iterator<int[]>, Iterable<int[]>
{

	public int [] _domainLengths;
	public int [] _currentLocation;
	
	public IndexCounter(int [] domainLengths)
	{
		_domainLengths = domainLengths;
		iterator();
	}
	
	@Override
	public Iterator<int[]> iterator() 
	{
		_currentLocation = new int[_domainLengths.length];
		_currentLocation[0] = -1;
		return this;
	}

	@Override
	public boolean hasNext() 
	{
		
		for (int i = 0; i < _domainLengths.length; i++)
		{
			if (_currentLocation[i] < (_domainLengths[i]-1))
				return true;
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int [] next() 
	{
		//increment
		boolean carry = true;
		
		for (int i = 0; i < _domainLengths.length; i++)
		{
			_currentLocation[i]++;
			if (_currentLocation[i] == _domainLengths[i])
			{
				carry = true;
				_currentLocation[i] = 0;
			}
			else
			{
				carry = false;
				break;
			}
		}
		
		if (carry)
			throw new NoSuchElementException();

		return  _currentLocation;
	}

	@Override
	public void remove() 
	{
		throw new UnsupportedOperationException();
	}
	
}
