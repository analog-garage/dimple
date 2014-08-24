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
 * Deterministic complex division. This is a deterministic directed factor (if smoothing is
 * not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (quotient = dividend / divisor)
 * 2) Dividend
 * 3) Divisor
 * 
 */
public class ComplexDivide extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public ComplexDivide() {this(0);}
	public ComplexDivide(double smoothing)
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
		final double[] quotient = arguments[0].getDoubleArray();
		final double rQuotient = quotient[0];
		final double iQuotient = quotient[1];
		
		double rDividend = 0;
		double iDividend = 0;
		final Value argdd = arguments[1];
		if (argdd.getObject() instanceof double[])	// Complex dividend
		{
			final double[] dividend = argdd.getDoubleArray();
			rDividend = dividend[0];
			iDividend = dividend[1];
		}
		else	// Real dividend
			rDividend = argdd.getDouble();

		double rDivisor = 0;
		double iDivisor = 0;
		final Value argdr = arguments[2];
		if (argdr.getObject() instanceof double[])	// Complex divisor
		{
			final double[] divisor = argdr.getDoubleArray();
			rDivisor = divisor[0];
			iDivisor = divisor[1];
		}
		else	// Real divisor
			rDivisor = argdr.getDouble();

		final double normalizer = 1 / (rDivisor*rDivisor + iDivisor*iDivisor);
    	if (Double.isNaN(normalizer))
    		return Double.POSITIVE_INFINITY;
    	if (Double.isInfinite(normalizer))
    		return Double.POSITIVE_INFINITY;
		
		final double rExpectedQuotient = (rDividend * rDivisor + iDividend * iDivisor) * normalizer;
		final double iExpectedQuotient = (iDividend * rDivisor - rDividend * iDivisor) * normalizer;
    	
    	if (_smoothingSpecified)
    	{
    		final double rDiff = rExpectedQuotient - rQuotient;
    		final double iDiff = iExpectedQuotient - iQuotient;
    		final double potential = rDiff*rDiff + iDiff*iDiff;
    		return potential*_beta;
    	}
    	else
    	{
    		return (rExpectedQuotient == rQuotient && iExpectedQuotient == iQuotient) ? 0 : Double.POSITIVE_INFINITY;
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
		double rDividend = 0;
		double iDividend = 0;
		final Value argdd = arguments[1];
		if (argdd.getObject() instanceof double[])	// Complex dividend
		{
			final double[] dividend = argdd.getDoubleArray();
			rDividend = dividend[0];
			iDividend = dividend[1];
		}
		else	// Real dividend
			rDividend = argdd.getDouble();

		double rDivisor = 0;
		double iDivisor = 0;
		final Value argdr = arguments[2];
		if (argdr.getObject() instanceof double[])	// Complex divisor
		{
			final double[] divisor = argdr.getDoubleArray();
			rDivisor = divisor[0];
			iDivisor = divisor[1];
		}
		else	// Real divisor
			rDivisor = argdr.getDouble();
		
		final double normalizer = 1 / (rDivisor*rDivisor + iDivisor*iDivisor);
		double rQuotient = (rDividend * rDivisor + iDividend * iDivisor) * normalizer;
		double iQuotient = (iDividend * rDivisor - rDividend * iDivisor) * normalizer;
		
    	if (Double.isNaN(normalizer))
    	{
    		rQuotient = 0;
    		iQuotient = 0;
    	}
    	
    	final double[] out = arguments[0].getDoubleArray();
		out[0] = rQuotient;		// Replace the output value
		out[1] = iQuotient;		// Replace the output value
    }
}
