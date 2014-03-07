/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

public class GenericDataSink<Type> implements IDataSink 
{

	protected LinkedList<Type> _data = new LinkedList<Type>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void push(Object data) 
	{
		_data.add((Type)data);
	}

	public Type getNext()
	{
		if (_data.size() <= 0)
			throw new DimpleException("Data sink is empty.");
		
		return _data.pollFirst();
	}

	public boolean hasNext()
	{
		return _data.size() > 0;
	}
	

}
