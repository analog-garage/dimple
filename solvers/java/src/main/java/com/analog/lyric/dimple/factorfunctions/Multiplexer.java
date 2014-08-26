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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Deterministic multiplexer. This factor has one discrete selector input an
 * arbitrary number of inputs and one output.  The output is equal to the
 * selected input, where the selector is a zero-based integer value.  This
 * factor can be used to generate mixture distributions. This is a
 * deterministic directed factor (if smoothing is not enabled).
 * 
 * Optional smoothing may be applied for inputs that are scalar numeric values,
 * by providing a smoothing value in the constructor. If smoothing is enabled,
 * the distribution is smoothed by exp(-difference^2/smoothing), where difference
 * is the distance between the output value and the deterministic output value for
 * the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (equal to selected input; any data type)
 * 2) Selector input (zero-based integer value)
 * 3...) An arbitrary number of inputs (any data type)
 * 
 * @since 0.05
 */
public class Multiplexer extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public Multiplexer() {this(0);}
	public Multiplexer(double smoothing)
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
    	final Value output = arguments[0];
    	final int selector = arguments[1].getInt();
    	final Value selectedInput = arguments[selector + 2];
    	
    	if (_smoothingSpecified)
    	{
    		if (!selectedInput.getDomain().isNumber())
    		{
    			throw smoothingNonNumber();
    		}
    		final double diff = selectedInput.getDouble() - output.getDouble();
    		return diff*diff*_beta;
    	}
    	else
    	{
    		return output.valueEquals(selectedInput) ? 0 : Double.POSITIVE_INFINITY;
    	}
    }
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
    @Override
	public final boolean isDeterministicDirected() {return true;}
    @Override
    public final void evalDeterministic(Value[] arguments)
    {
    	arguments[0].setFrom(arguments[arguments[1].getInt() + 2]);
    }
    
    // Factor-specific methods
    public final boolean hasSmoothing()
    {
    	return _smoothingSpecified;
    }

    private DimpleException smoothingNonNumber()
    {
    	return new DimpleException("Smoothing allowed only for scalar numeric inputs.");
    }
}
