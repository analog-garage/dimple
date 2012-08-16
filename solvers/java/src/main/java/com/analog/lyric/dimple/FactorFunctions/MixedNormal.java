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


public class MixedNormal extends FactorFunction
{
	protected double _mean0 = 0;
	protected double _invSigmaSquared0 = 1;
	protected double _invSigma0 = 1;
	protected double _mean1 = 0;
	protected double _invSigmaSquared1 = 1;
	protected double _invSigma1 = 1;
	public MixedNormal(double mean0, double sigma0, double mean1, double sigma1)
	{
		super("MixedNormal");
		_mean0 = mean0;
		_invSigma0 = 1/sigma0;
		_invSigmaSquared0 = 1/(sigma0*sigma0);
		_mean1 = mean1;
		_invSigma1 = 1/sigma1;
		_invSigmaSquared1 = 1/(sigma1*sigma1);
	}
    public double eval(Object ... input)
    {
    	double a = (Double)input[0];
    	Object bIn = input[1];
    	int b;
    	if (bIn instanceof Double)
    		b = (int)Math.round((Double)bIn);
    	else
    		b = (Integer)bIn;
    	if (b == 0)
    	{
    		double aRel = a - _mean0;
    		return Math.exp(-aRel*aRel*_invSigmaSquared0)*_invSigma0;
    	}
    	else
    	{
    		double aRel = a - _mean1;
    		return Math.exp(-aRel*aRel*_invSigmaSquared1)*_invSigma1;
    	}
    }
}
