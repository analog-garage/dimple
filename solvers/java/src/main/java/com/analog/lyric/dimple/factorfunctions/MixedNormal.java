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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;


public class MixedNormal extends FactorFunction
{
	protected double _mean0 = 0;
	protected double _precision0 = 1;
	protected double _mean1 = 0;
	protected double _precision1 = 1;
	public MixedNormal(double mean0, double precision0, double mean1, double precision1)
	{
		super();
		_mean0 = mean0;
		_precision0 = precision0;
		_mean1 = mean1;
		_precision1 = precision1;
	}
	
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	final double a = arguments[0].getDouble();
    	final int b = arguments[1].getInt();
    	if (b == 0)
    	{
    		final double aRel = a - _mean0;
    		return (aRel*aRel*_precision0 - Math.log(_precision0) ) * 0.5;
    	}
    	else
    	{
    		final double aRel = a - _mean1;
    		return (aRel*aRel*_precision1 - Math.log(_precision1) ) * 0.5;
    	}
    }
}
