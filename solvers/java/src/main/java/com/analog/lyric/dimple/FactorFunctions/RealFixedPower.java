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


public class RealFixedPower extends FactorFunction
{
	protected double _power;
	protected double _beta;
	public RealFixedPower(double power) {this(power, 1);}
	public RealFixedPower(double power, double smoothing) {super("RealFixedPower"); _power = power; _beta = 1/smoothing;}
	
    @Override
    public double evalEnergy(Object ... arguments)
    {
    	Double result = (Double)arguments[0];
    	Double base = (Double)arguments[1];
    	
    	double computedResult = Math.pow(base, _power);
    	
    	double diff = computedResult - result;
    	double potential = diff*diff;
    	
    	return potential*_beta;
    }
}
