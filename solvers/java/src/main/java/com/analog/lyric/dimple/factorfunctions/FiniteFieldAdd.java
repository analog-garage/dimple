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
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Deterministic finite field (GF(2^n)) addition. This is a deterministic directed factor
 * (if smoothing is not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (FiniteFieldVariable; Output = input1 + input2)
 * 2) Input1 (FiniteFieldVariable)
 * 3) Input2 (FiniteFieldVariable)
 * 
 */
public class FiniteFieldAdd extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public FiniteFieldAdd() {this(0);}
	public FiniteFieldAdd(double smoothing)
	{
		super();
		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
    @Override
    public double evalEnergy(Object ... arguments)
    {
    	int result = FactorFunctionUtilities.toInteger(arguments[0]);
    	int input1 = FactorFunctionUtilities.toInteger(arguments[1]);
    	int input2 = FactorFunctionUtilities.toInteger(arguments[2]);
    	
    	double computedResult = input1 ^ input2;
    	
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
    public double evalEnergy(Value[] values)
    {
    	int result = values[0].getInt();
    	int input1 = values[1].getInt();
    	int input2 = values[2].getInt();
    	
    	int computedResult = input1 ^ input2;
    	
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
	public final void evalDeterministic(Object[] arguments)
    {
    	Integer input1 = FactorFunctionUtilities.toInteger(arguments[1]);
    	Integer input2 = FactorFunctionUtilities.toInteger(arguments[2]);
    	arguments[0] = input1 ^ input2;		// Replace the output value
    }
}
