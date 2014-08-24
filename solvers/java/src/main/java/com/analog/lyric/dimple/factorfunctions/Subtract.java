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


/**
 * Deterministic subtraction. This is a deterministic directed factor (if
 * smoothing is not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (difference = positive input - sum of subtracted inputs)
 * 2) Positive input (double or integer)
 * 3...) An arbitrary number of subtracted inputs (double or integer)
 * 
 */
public class Subtract extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public Subtract() {this(0);}
	public Subtract(double smoothing)
	{
		super();
		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	final int length = arguments.length;
    	final double out = arguments[0].getDouble();
    	final double posIn = arguments[1].getDouble();

    	double sum = posIn;
    	for (int i = 2; i < length; i++)
    		sum -= arguments[i].getDouble();
    	
    	if (_smoothingSpecified)
    	{
    		final double diff = sum - out;
    		final double potential = diff*diff;
    		return potential*_beta;
    	}
    	else
    	{
    		return (sum == out) ? 0 : Double.POSITIVE_INFINITY;
    	}
    }
    
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
    @Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
    @Override
	public final void evalDeterministic(Value[] arguments)
    {
    	final int length = arguments.length;

    	final double posIn = arguments[1].getDouble();
    	double sum = posIn;
    	for (int i = 2; i < length; i++)
    		sum -= arguments[i].getDouble();
    	
    	arguments[0].setDouble(sum);		// Replace the output value
    }
}
