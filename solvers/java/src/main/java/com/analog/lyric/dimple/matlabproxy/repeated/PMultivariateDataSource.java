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

import com.analog.lyric.dimple.model.repeated.IDataSource;
import com.analog.lyric.dimple.model.repeated.MultivariateDataSource;

public class PMultivariateDataSource implements IPDataSource 
{
	private MultivariateDataSource [] _dataSources;
	
	public PMultivariateDataSource(int numVars)
	{
		_dataSources = new MultivariateDataSource[numVars];
		
		for (int i = 0; i < _dataSources.length; i++)
		{
			_dataSources[i] = new MultivariateDataSource();
		}
	}
	
	public PMultivariateDataSource(MultivariateDataSource [] sources)
	{
		_dataSources = sources;
	}

	public void add(double [][] means, double [][][] covar)
	{
		for (int i = 0; i < means.length; i++)
		{
			_dataSources[i].add(means[i], covar[i]);
		}
	}
	
//	public void add(Object [] msgs)
//	{
//		for (int i = 0; i < msgs.length; i++)
//		{
//			//_dataSources[i].add
//		}
//	}
	
	
	@Override
	public IDataSource[] getModelObjects() 
	{
		return _dataSources;
	}


}
