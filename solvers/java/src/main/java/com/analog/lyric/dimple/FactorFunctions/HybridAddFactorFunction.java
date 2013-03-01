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

package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.FactorFunctions.core.HybridSampledBPFactorFunction;

public class HybridAddFactorFunction extends HybridSampledBPFactorFunction
{

	public HybridAddFactorFunction() 
	{
		super("GaussianAdd");
	}

	@Override
	public double acceptanceRatio(int portIndex, Object... arguments) 
	{
		return 1;
	}

	@Override
	public Object generateSample(int portIndex, Object... arguments) 
	{
		if (portIndex == 0)
		{
			double sum = 0;
			for (int i = 0; i < arguments.length; i++)
			{
				sum += FactorFunctionUtilities.toDouble(arguments[i]);
			}
			return sum;
		}
		else
		{
			double sum = (Double)arguments[0];
			for (int i = 1; i < arguments.length; i++)
			{
				sum -= FactorFunctionUtilities.toDouble(arguments[i]);
			}
			return sum;
		}
	}

}
