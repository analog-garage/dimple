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


public class RealPower extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public RealPower() {this(0);}
	public RealPower(double smoothing)
	{
		super("RealPower");
		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
    @Override
    public double evalEnergy(Object ... arguments)
    {
    	Double result = FactorFunctionUtilities.toDouble(arguments[0]);
    	Double base = FactorFunctionUtilities.toDouble(arguments[1]);
    	Double power = FactorFunctionUtilities.toDouble(arguments[2]);
    	
    	double computedResult = Math.pow(base, power);
    	
    	if (_smoothingSpecified)
    	{
        	double diff = computedResult - result;
        	double potential = diff*diff;
    		return potential*_beta;
    	}
    	else
    	{
    		return (computedResult == result) ? 0 : Double.POSITIVE_INFINITY;
    	}
    }
    
    
    @Override
    public final boolean isDirected()	{return true;}
    @Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
    @Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
    @Override
	public final void evalDeterministicFunction(Object ... arguments)
    {
    	Double base = FactorFunctionUtilities.toDouble(arguments[1]);
    	Double power = FactorFunctionUtilities.toDouble(arguments[2]);
    	arguments[0] = Math.pow(base, power);		// Replace the output value
    }
}
