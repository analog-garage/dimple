/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.util.misc;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;


public class IndexCounter implements Iterator<int[]>, Iterable<int[]>
{

	public int [] _domainLengths;
	public @Nullable int [] _currentLocation;
	
	public IndexCounter(int [] domainLengths)
	{
		_domainLengths = domainLengths;
		iterator();
	}
	
	@Override
	public Iterator<int[]> iterator()
	{
		final int[] cur = _currentLocation = new int[_domainLengths.length];
		cur[0] = -1;
		return this;
	}

	@Override
	public boolean hasNext()
	{
		final int[] cur = _currentLocation;
		if (cur != null)
		{
			for (int i = 0; i < _domainLengths.length; i++)
			{
				if (cur[i] < (_domainLengths[i]-1))
					return true;
			}
		}
		return false;
	}

	@Override
	public @Nullable int [] next()
	{
		//increment
		boolean carry = true;
		
		final int[] cur = _currentLocation;
		
		if (cur != null)
		{
			for (int i = 0; i < _domainLengths.length; i++)
			{
				cur[i]++;
				if (cur[i] == _domainLengths[i])
				{
					carry = true;
					cur[i] = 0;
				}
				else
				{
					carry = false;
					break;
				}
			}
		}
		
		if (carry)
			throw new NoSuchElementException();

		return cur;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
	
}
