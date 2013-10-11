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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.repeated.DoubleArrayDataSource;
import com.analog.lyric.dimple.model.repeated.IDataSource;
import com.analog.lyric.math.Functions;

public class PDoubleArrayDataSource implements IPDataSource 
{

	private DoubleArrayDataSource [] _dataSources;
	
	public PDoubleArrayDataSource(DoubleArrayDataSource [] dads)
	{
		_dataSources = dads;
	}

	public PDoubleArrayDataSource(int numVars)
	{
		_dataSources = new DoubleArrayDataSource[numVars];
		
		for (int i = 0; i < numVars; i++)
			_dataSources[i] = new DoubleArrayDataSource();
	}
		
	public void add(double [][] data)
	{
		if (data.length != _dataSources.length)
			throw new DimpleException("variable size mismatch"  + data.length + " " + _dataSources.length);
		
		for (int i = 0; i < data.length; i++)
			_dataSources[i].add(data[i]);
	}
	
	public void addMultiple(double [][][] data)
	{
		if (data.length != _dataSources.length)
			throw new DimpleException("variable size mismatch: " + data.length + " " + _dataSources.length);
		
		for (int i = 0; i < data.length; i++)
			_dataSources[i].add(Functions.transpose(data[i]));
		
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
