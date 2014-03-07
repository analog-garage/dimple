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

import com.analog.lyric.dimple.model.repeated.IDataSink;
import com.analog.lyric.dimple.model.repeated.MultivariateDataSink;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;

public class PMultivariateDataSink implements IPDataSink 
{
	
	private MultivariateDataSink [] _dataSinks;
	
	public PMultivariateDataSink(MultivariateDataSink [] dataSinks)
	{
		_dataSinks=dataSinks;
	}
	

	public PMultivariateDataSink(int numVars)
	{
		_dataSinks = new MultivariateDataSink[numVars];
		
		for (int i = 0; i < numVars; i++)
			_dataSinks[i] = new MultivariateDataSink();
	}
		
	public MultivariateNormalParameters [] getNext()
	{
		MultivariateNormalParameters [] retval = new MultivariateNormalParameters[_dataSinks.length];
		
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
}
