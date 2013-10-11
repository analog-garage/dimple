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

package com.analog.lyric.dimple.model.repeated;

import java.util.LinkedList;

import com.analog.lyric.dimple.exceptions.DimpleException;

public class GenericDataSource<Type> implements IDataSource 
{
	protected LinkedList<Type> _data = new LinkedList<Type>();
	
	public void add(Type data) 
	{
		_data.add(data);
	}

	public void add(Type [] data)
	{
		for (int i = 0; i < data.length; i++)
			_data.add(data[i]);
	}
	
	public Type getNext()
	{
		if (_data.size() <= 0)
			throw new DimpleException("ACK!");
		
		return _data.pollFirst();
	}

	public boolean hasNext()
	{
		return _data.size() > 0;
	}
	
	
}
