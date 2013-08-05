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

package com.analog.lyric.dimple.factorfunctions;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.HybridSampledBPFactorFunction;
import com.analog.lyric.dimple.model.DimpleException;

public class HybridRealFixedPower extends HybridSampledBPFactorFunction 
{
	private int _power;
	
	public HybridRealFixedPower(int power) 
	{
		super();
		_power = power;
	}

    @Override
    public double evalEnergy(Object... arguments)
    {
    	Double result = FactorFunctionUtilities.toDouble(arguments[0]);
    	Double base = FactorFunctionUtilities.toDouble(arguments[1]);
    	
    	double computedResult = Math.pow(base, _power);
    	return (computedResult == result) ? 0 : Double.POSITIVE_INFINITY;	// This version is not smoothed
    }

	@Override
	public double acceptanceRatio(int outPortIndex, Object... arguments) 
	{
		if (arguments.length != 1)
			throw new DimpleException("expect two edges");
		
		if (outPortIndex == 0)
			return 1;
		else
			if (FactorFunctionUtilities.toDouble(arguments[0]) < 0 && _power%2 == 0)
				return 0;
			else
				return 1;
	}

	@Override
	public Object generateSample(int outPortIndex, Object... arguments) 
	{
		if (arguments.length != 1)
			throw new DimpleException("expect two edges");

		if (outPortIndex == 1)
		{
			double input = FactorFunctionUtilities.toDouble(arguments[0]);
			double tmp = Math.pow(Math.abs(input),1.0/_power);
			tmp = Math.signum(input)*tmp;
			
			if (_power%2 == 0 && _random.nextBoolean())
				tmp = -tmp;

			return tmp;
		}
		else
		{
			double tmp = FactorFunctionUtilities.toDouble(arguments[0]);
			
			tmp =  Math.pow(tmp, _power);
			return tmp;
		}
	}

}
