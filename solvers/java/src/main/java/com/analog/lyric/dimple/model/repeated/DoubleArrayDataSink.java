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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;


public class DoubleArrayDataSink extends GenericDataSink<double[]>
{

	
	public double [][] getArray()
	{
		double [][] retval = new double[_data.size()][];
		int i = 0;
		for (double [] data : _data)
		{
			retval[i] = data;
			i++;
		}
		return retval;
	}
	
	
	// This is a hack to support backward compatibility with using double-arrays to represent
	// beliefs represented as Gaussian parameters.  For beliefs of this type, a new class of
	// data sink should be created that explicitly supports this representation (as in the
	// multivariate case).  Then this hack can be removed at some point, and getNext from
	// the base class can be used directly.
	@Override
	public double[] getNext()
	{
		if (_data.size() <= 0)
			throw new DimpleException("Data sink is empty.");
		
		Object value = _data.pollFirst();
		if (value instanceof NormalParameters)
		{
			NormalParameters belief = (NormalParameters)value;
			return new double[] {belief.getMean(), belief.getStandardDeviation()};
		}
		else
			return (double[])value;
	}

}
