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

package com.analog.lyric.util.test;

import java.util.Random;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;

public class AlwaysTrueUpToNRowsFactorFunction extends FactorFunction
{
	private int _maxRows;
	private int _currRows;
	private boolean _randomWeights;
	static private Random _r = new Random();

	public AlwaysTrueUpToNRowsFactorFunction(int maxRows, boolean randomWeights)
	{
		super(String.format("AlwaysTrueUpTo%dRows", maxRows));
		_maxRows = maxRows;
		_currRows = 0;
		_randomWeights = randomWeights;
	}

	@Override
	public final double evalEnergy(Value[] input)
	{
		double value = _randomWeights ? -Math.log(_r.nextDouble()) : 0;
		if(_currRows < _maxRows)
		{
			_currRows++;
		}
		else
		{
			value = Double.POSITIVE_INFINITY;
		}
		return value;
	}
}
