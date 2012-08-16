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

package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class ConstantProduct extends FactorFunction
{
	protected double _beta;
	protected double _constant;
	public ConstantProduct() {this(1,1);}
	public ConstantProduct(double constant) {this(constant,1);}
	public ConstantProduct(double constant, double smoothing) {super("ConstantProduct"); _beta = 1/smoothing; _constant=constant;}
    public double eval(Object ... input)
    {
    	
    	double out = (Double)input[0];

    	double product= _constant * (Double) input[1];
    	double diff = product - out;
    	double potential = diff*diff;
    	
    	return Math.exp(-potential*_beta);
    }
}
