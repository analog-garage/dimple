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


/**
 * Deterministic complex exponent. This is a deterministic directed factor (if smoothing is
 * not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (exp of input)
 * 2) Input
 * 
 */
public class ComplexExp extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public ComplexExp() {this(0);}
	public ComplexExp(double smoothing)
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
		double[] out = ((double[])arguments[0]);
		double rOut = out[0];
		double iOut = out[1];

		double[] in = ((double[])arguments[1]);
		double rIn = in[0];
		double iIn = in[1];
		double magnitude = Math.exp(rIn);
		double rExp = magnitude * Math.cos(iIn);
		double iExp = magnitude * Math.sin(iIn);
    	
    	if (_smoothingSpecified)
    	{
    		double rDiff = rExp - rOut;
    		double iDiff = iExp - iOut;
    		double potential = rDiff*rDiff + iDiff*iDiff;
    		return potential*_beta;
    	}
    	else
    	{
    		return (rExp == rOut && iExp == iOut) ? 0 : Double.POSITIVE_INFINITY;
    	}
    }
    
    
    @Override
    public final boolean isDirected()	{return true;}
    @Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
    @Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
    @Override
	public final void evalDeterministicFunction(Object... arguments)
    {
		double[] in = ((double[])arguments[1]);
		double rIn = in[0];
		double iIn = in[1];
		double magnitude = Math.exp(rIn);
		double rExp = magnitude * Math.cos(iIn);
		double iExp = magnitude * Math.sin(iIn);

		double[] out = ((double[])arguments[0]);
		out[0] = rExp;		// Replace the output value
		out[1] = iExp;		// Replace the output value
    }
}
