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

import com.analog.lyric.dimple.model.repeated.DoubleArrayDataSink;
import com.analog.lyric.dimple.model.repeated.IDataSink;
import com.analog.lyric.math.Functions;

public class PDoubleArrayDataSink implements IPDataSink 
{

	private DoubleArrayDataSink [] _dataSinks;
	
	public PDoubleArrayDataSink(DoubleArrayDataSink [] dataSinks)
	{
		_dataSinks=dataSinks;
	}
	

	public PDoubleArrayDataSink(int numVars)
	{
		_dataSinks = new DoubleArrayDataSink[numVars];
		
		for (int i = 0; i < numVars; i++)
			_dataSinks[i] = new DoubleArrayDataSink();
	}
		
	public double [][] getNext()
	{
		double [][] retval = new double[_dataSinks.length][];
		for (int i = 0; i < retval.length; i++)
			retval[i] = _dataSinks[i].getNext();
		
		return retval;
	}

	public boolean hasNext()
	{
		return _dataSinks[0].hasNext();
	}
	

	@Override
	public IDataSink[] getModelObjects() 
	{
		return _dataSinks;
	}
	
	public double [][][] getArray()
	{
		double [][][] retval = new double[_dataSinks.length][][];
		for (int i = 0; i < _dataSinks.length; i++)
		{			
			retval[i] = Functions.transpose(_dataSinks[i].getArray());
		}
		
		return retval;
	}

}
