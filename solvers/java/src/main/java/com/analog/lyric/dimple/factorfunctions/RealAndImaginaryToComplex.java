/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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
 * Deterministic conversion of a vector of two real variables to a complex variable.
 * This is a deterministic directed factor (if smoothing is not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (Complex vector)
 * 2) Input real-part (Real)
 * 3) Input imaginary-part (Real)
 * 
 * @since 0.07
 */
public class RealAndImaginaryToComplex extends FactorFunction
{
	private double _beta = 0;
	private boolean _smoothingSpecified = false;
	
	public RealAndImaginaryToComplex() {this(0);}
	public RealAndImaginaryToComplex(double smoothing)
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
    	// Output Complex
		final double[] complex = arguments[0].getDoubleArray();
    	
    	if (_smoothingSpecified)
    	{
    		final double diffR = arguments[1].getDouble() - complex[0];
    		final double diffI = arguments[2].getDouble() - complex[1];
    		return _beta * ((diffR*diffR) + (diffI*diffI));
    	}
    	else
    	{
			if (arguments[1].getDouble() != complex[0])
				return Double.POSITIVE_INFINITY;
			else if (arguments[2].getDouble() != complex[1])
				return Double.POSITIVE_INFINITY;
			else
				return 0;
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
    	// Output Complex
		final double[] complex = arguments[0].getDoubleArray();

		complex[0] = arguments[1].getDouble();
		complex[1] = arguments[2].getDouble();
    }
}
