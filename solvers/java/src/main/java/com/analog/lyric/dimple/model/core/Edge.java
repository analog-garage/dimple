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

package com.analog.lyric.dimple.model.core;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;


public class Edge implements Comparable<Edge>
{
	private INameable _left;
	private INameable _right;
	
	public Edge(INameable left, INameable right)
	{
		_left = left;
		_right = right;
	}
	
	public INameable getLeft(){return _left;}
	public INameable getRight(){return _right;}
	
	@Override
	@NonNullByDefault(false)
	public int compareTo(Edge e)
	{
		int diff = 0;
		if(this != e)
		{
			diff = _left.getUUID().compareTo(e._left.getUUID());
			if(diff == 0)
			{
				diff = _right.getUUID().compareTo(e._right.getUUID());
			}
		}
		return diff;
	}
	@Override
	public int hashCode()
	{
		return (_left.getUUID().toString() +
				_right.getUUID().toString()).hashCode();
	}
	@Override
	public boolean equals(@Nullable Object o)
	{
		return this == o ||
			   (o instanceof Edge &&
				compareTo((Edge)o) == 0);
	}

	@Override
	public String toString()
	{
		return String.format("Edge [%s] <-> [%s]", _left.getLabel(), _right.getLabel());
	}
}
