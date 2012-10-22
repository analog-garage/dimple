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

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;

public class GreaterThan extends FactorFunction 
{
	public GreaterThan()
	{
		super("GreaterThan");
	}
	
    @Override
    public double evalEnergy(Object ... input)
    {
    	Object indicatorOut = input[0];
    	boolean indicator;
    	if (indicatorOut instanceof Double)
    		indicator = (Math.round((Double)indicatorOut) != 0);
    	else
    		indicator = ((Integer)indicatorOut != 0);
    	double firstVal = (Double)input[1];
    	double secondVal = (Double)input[2];
    	
    	if (indicator == (firstVal > secondVal))
    		return 0;
    	else
    		return Double.POSITIVE_INFINITY;
    }

}
