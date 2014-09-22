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
 * Deterministic absolute value of a complex variable.
 * This is a deterministic directed factor (if smoothing is not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (absolute value of the complex input) (Real)
 * 2) Input (Complex)
 * 
 * @since 0.07
 */
public class ComplexAbs extends FactorFunction
{
	private double _beta = 0;
	private boolean _smoothingSpecified = false;
	
	public ComplexAbs() {this(0);}
	public ComplexAbs(double smoothing)
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
    	// Input Complex
		final double[] complex = arguments[1].getDoubleArray();
		final double real = complex[0];
		final double imag = complex[1];
		final double abs = Math.sqrt(real*real + imag*imag);
		
    	if (_smoothingSpecified)
    	{
    		final double diff = arguments[0].getDouble() - abs;
    		return _beta * (diff*diff);
    	}
    	else
    	{
			if (arguments[0].getDouble() != abs)
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
    	// Input Complex
		final double[] complex = arguments[1].getDoubleArray();
		final double real = complex[0];
		final double imag = complex[1];
		
		arguments[0].setDouble(Math.sqrt(real*real + imag*imag));
    }
}
