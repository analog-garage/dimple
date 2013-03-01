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
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;


public class RealMinus extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public RealMinus() {this(0);}
	public RealMinus(double smoothing)
	{
		super("RealMinus");
		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
    @Override
    public double evalEnergy(Object ... arguments)
    {
    	int length = arguments.length;
    	double out = FactorFunctionUtilities.toDouble(arguments[0]);
    	double posIn = FactorFunctionUtilities.toDouble(arguments[1]);

    	double sum = posIn;
    	for (int i = 2; i < length; i++)
    		sum -= (Double)arguments[i];
    	
    	if (_smoothingSpecified)
    	{
    		double diff = sum - out;
    		double potential = diff*diff;
    		return potential*_beta;
    	}
    	else
    	{
    		return (sum == out) ? 0 : Double.POSITIVE_INFINITY;
    	}
    }
    
    
    @Override
    public final boolean isDirected()	{return true;}
    @Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
    @Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
    @Override
	public final void evalDeterministicFunction(Object ... input)
    {
    	int length = input.length;

    	double posIn = (Double)input[1];
    	double sum = posIn;
    	for (int i = 2; i < length; i++)
    		sum -= (Double)input[i];
    	
    	input[0] = sum;		// Replace the output value
    }
}
