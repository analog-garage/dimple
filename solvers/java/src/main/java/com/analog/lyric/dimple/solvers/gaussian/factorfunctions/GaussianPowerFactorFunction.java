/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

package com.analog.lyric.dimple.solvers.gaussian.factorfunctions;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.solvers.gaussian.GaussianFactorFunction;

public class GaussianPowerFactorFunction extends GaussianFactorFunction 
{
	private int _power;
	
	public GaussianPowerFactorFunction(int power) 
	{
		super("GaussianPower" + power);
		
		_power = power;
	}

	@Override
	public double acceptanceRatio(int outPortIndex, Object... inputs) 
	{
		if (inputs.length != 1)
			throw new DimpleException("expect two edges");
		
		if (outPortIndex == 0)
			return 1;
		else
			if ((Double)inputs[0] < 0 && _power%2 == 0)
				return 0;
			else
				return 1;
	}

	@Override
	public Object generateSample(int outPortIndex, Object... inputs) 
	{
		if (inputs.length != 1)
			throw new DimpleException("expect two edges");

		if (outPortIndex == 1)
		{
			double input = (Double)inputs[0];
			double tmp = Math.pow(Math.abs(input),1.0/_power);
			tmp = Math.signum(input)*tmp;
			
			if (_power%2 == 0 && _random.nextBoolean())
				tmp = -tmp;

			
			return tmp;
		}
		else
		{
			
			double tmp = (Double)inputs[0];
			
			tmp =  Math.pow(tmp, _power);
			return tmp;
		}
	}

}
