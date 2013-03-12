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

import java.util.ArrayList;
import java.util.LinkedList;

public class DoubleArrayDataSource extends GenericDataSource<double[]> 
{

	public DoubleArrayDataSource()
	{
		_data = new LinkedList<double[]>();
	}

	public DoubleArrayDataSource(double [][] arr)
	{
		_data = new LinkedList<double[]>();
		
		add(arr);
	}

	public DoubleArrayDataSource(ArrayList<double[]> arr)
	{
		_data = new LinkedList<double[]>();
		
		for (double [] data : arr)
			_data.add(data);
	}

	public  void add(double[][] data)
	{
		for (int i = 0; i < data.length; i++)
			_data.add(data[i]);		
	}

	public  void add(double[] data)
	{
		_data.add(data);
	}
	
	double [] peek()
	{
		return _data.peek();
	}

	
}
