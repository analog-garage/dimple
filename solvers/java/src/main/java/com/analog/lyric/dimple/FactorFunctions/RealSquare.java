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


public class RealSquare extends FactorFunction
{
	protected double _beta;
	public RealSquare() {this(1);}
	public RealSquare(double smoothing) {super("RealFixedPower"); _beta = 1/smoothing;}
    public double eval(Object ... arguments)
    {
    	Double result = (Double)arguments[0];
    	Double input = (Double)arguments[1];
    	
    	double computedResult = input*input;
    	
    	double diff = computedResult - result;
    	double potential = diff*diff;
    	
    	return Math.exp(-potential*_beta);
    }
}
