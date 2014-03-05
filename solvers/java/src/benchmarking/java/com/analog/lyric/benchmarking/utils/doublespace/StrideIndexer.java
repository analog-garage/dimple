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

package com.analog.lyric.benchmarking.utils.doublespace;

import java.security.InvalidParameterException;
import java.util.Iterator;

class StrideIndexer implements Indexer
{
	final int _finish;

	final int _start;

	final int _stride;

	public StrideIndexer(int just)
	{
		this(just, 1, just);
	}

	public StrideIndexer(int start, int finish)
	{
		this(start, (int) Math.signum(finish - start), finish);
	}

	public StrideIndexer(int start, int stride, int finish)
	{
		final boolean positive = finish >= start;
		if (positive && stride < 1 || !positive && stride > -1)
		{
			throw new InvalidParameterException("Sign of stride does not suit start and finish values.");
		}
		if (start < 0 || finish < 0)
		{
			throw new InvalidParameterException("Start and finish values must be non-negative.");
		}
		_start = start;
		_stride = stride;
		_finish = finish;
	}

	@Override
	public int getCardinality()
	{
		return (_finish - _start) / _stride + 1;
	}

	@Override
	public int getNth(int i)
	{
		final int proposal = _start + i * _stride;
		if (_stride >= 0)
		{
			if (proposal <= _finish)
			{
				return proposal;
			}
		}
		else
		{
			if (proposal >= _finish)
			{
				return proposal;
			}
		}
		throw new IllegalArgumentException();
	}

	@Override
	public Iterator<Integer> iterator()
	{
		return new StrideIndexerIterator();
	}

	private class StrideIndexerIterator implements Iterator<Integer>
	{
		private int _current = -1;

		@Override
		public boolean hasNext()
		{
			int proposal;
			if (_current == -1)
			{
				proposal = _start;
			}
			else
			{
				proposal = _current + _stride;
			}
			if (_stride > 0)
			{
				if (proposal <= _finish)
				{
					return true;
				}
			}
			else
			{
				if (proposal >= _finish)
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public Integer next()
		{
			int proposal;
			if (_current == -1)
			{
				proposal = _start;
			}
			else
			{
				proposal = _current + _stride;
			}
			if (_stride > 0)
			{
				if (proposal <= _finish)
				{
					_current = proposal;
				}
			}
			else
			{
				if (proposal >= _finish)
				{
					_current = proposal;
				}
			}
			return _current;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}

}
