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
 * Deterministic complex product. This is a deterministic directed factor (if smoothing is
 * not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Complex output (product of inputs)
 * 2...) An arbitrary number of inputs (complex or real)
 * 
 */
public class ComplexProduct extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public ComplexProduct() {this(0);}
	public ComplexProduct(double smoothing)
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
    	final double[] out = arguments[0].getDoubleArray();
    	final double rOut = out[0];
    	final double iOut = out[1];

    	double rProduct = 1;
    	double iProduct = 0;
    	for (int i = 1; i < length; i++)
    	{
    		Value arg = arguments[i];
    		if (arg.getObject() instanceof double[])	// Complex input
    		{
    			final double[] in = arg.getDoubleArray();
    			final double rIn = in[0];
    			final double iIn = in[1];
    			final double rProductNext = rIn * rProduct - iIn * iProduct;
    			final double iProductNext = rIn * iProduct + iIn * rProduct;
    			rProduct = rProductNext;
    			iProduct = iProductNext;
    		}
    		else	// Real input
    		{
    			final double in = arg.getDouble();
    			rProduct *= in;
    			iProduct *= in;
    		}
    	}
    	
    	if (_smoothingSpecified)
    	{
    		final double rDiff = rProduct - rOut;
    		final double iDiff = iProduct - iOut;
    		final double potential = rDiff*rDiff + iDiff*iDiff;
    		return potential*_beta;
    	}
    	else
    	{
    		return (rProduct == rOut && iProduct == iOut) ? 0 : Double.POSITIVE_INFINITY;
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

    	double rProduct = 1;
    	double iProduct = 0;
    	for (int i = 1; i < length; i++)
    	{
    		final Value arg = arguments[i];
    		if (arg.getObject() instanceof double[])	// Complex input
    		{
    			final double[] in = arg.getDoubleArray();
    			final double rIn = in[0];
    			final double iIn = in[1];
    			final double rProductNext = rIn * rProduct - iIn * iProduct;
    			final double iProductNext = rIn * iProduct + iIn * rProduct;
    			rProduct = rProductNext;
    			iProduct = iProductNext;
    		}
    		else	// Real input
    		{
    			final Double in = arg.getDouble();
    			rProduct *= in;
    			iProduct *= in;
    		}
    	}
    	
    	final double[] out = arguments[0].getDoubleArray();
		out[0] = rProduct;		// Replace the output value
		out[1] = iProduct;		// Replace the output value
    }
}
