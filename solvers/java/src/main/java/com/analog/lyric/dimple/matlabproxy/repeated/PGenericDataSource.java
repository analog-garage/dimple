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

package com.analog.lyric.dimple.matlabproxy.repeated;

import java.lang.reflect.Array;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.repeated.GenericDataSource;
import com.analog.lyric.dimple.model.repeated.IDataSource;

public class PGenericDataSource<Type extends GenericDataSource<Type2>,Type2> implements IPDataSource
{

	private Type [] _dataSources;
	
	public PGenericDataSource(Type [] dads)
	{
		_dataSources = dads;
	}

	public PGenericDataSource(Class<Type> c, int numVars)
	{
		@SuppressWarnings("unchecked")
		Type[] dataSources = _dataSources = (Type[])Array.newInstance(c,numVars);
		
		for (int i = 0; i < numVars; i++)
			try {
				dataSources[i] = c.newInstance();
			} catch (Exception e) {
				throw new DimpleException("ack");
			}
		}
		
	public void add(Type2 [] data)
	{
		for (int i = 0; i < data.length; i++)
			_dataSources[i].add(data[i]);
	}
	
	public void addMultiple(Type2 [][] data)
	{
		if (data.length != _dataSources.length)
			throw new DimpleException("variable size mismatch: " + data.length + " " + _dataSources.length);
		
		for (int i = 0; i < data.length; i++)
			_dataSources[i].add(data[i]);
		
	}
	
	@Override
	public IDataSource[] getModelObjects()
	{
		return _dataSources;
	}
	
	public boolean hasNext()
	{
		return _dataSources[0].hasNext();
	}

}
