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


/**
 * Deterministic complex subtraction. This is a deterministic directed factor (if smoothing is
 * not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Complex output (difference = positive input - sum of subtracted inputs)
 * 2) Positive input (complex or real)
 * 3...) An arbitrary number of subtracted inputs (complex or real)
 * 
 */
public class ComplexSubtract extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public ComplexSubtract() {this(0);}
	public ComplexSubtract(double smoothing)
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
    	int length = arguments.length;
		double[] out = ((double[])arguments[0]);
		double rOut = out[0];
		double iOut = out[1];

		double rSum = 0;
		double iSum = 0;

		// Positive input
		Object argPosIn = arguments[1];
		if (argPosIn instanceof double[])	// Complex input
		{
			double[] posIn = ((double[])argPosIn);
			rSum = posIn[0];
			iSum = posIn[1];
		}
		else	// Real input
			rSum = FactorFunctionUtilities.toDouble(argPosIn);
		
		// Negative inputs
    	for (int i = 2; i < length; i++)
    	{
    		Object arg = arguments[i];
    		if (arg instanceof double[])	// Input is complex
    		{
    			double[] in = ((double[])arg);
    			rSum -= in[0];
    			iSum -= in[1];
    		}
    		else	// Input is real
    			rSum -= FactorFunctionUtilities.toDouble(arg);
    	}
    	
    	if (_smoothingSpecified)
    	{
    		double rDiff = rSum - rOut;
    		double iDiff = iSum - iOut;
    		double potential = rDiff*rDiff + iDiff*iDiff;
    		return potential*_beta;
    	}
    	else
    	{
    		return (rSum == rOut && iSum == iOut) ? 0 : Double.POSITIVE_INFINITY;
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
    	int length = arguments.length;

		double rSum = 0;
		double iSum = 0;

		// Positive input
		Object argPosIn = arguments[1];
		if (argPosIn instanceof double[])	// Complex input
		{
			double[] posIn = ((double[])argPosIn);
			rSum = posIn[0];
			iSum = posIn[1];
		}
		else	// Real input
			rSum = FactorFunctionUtilities.toDouble(argPosIn);
		
		// Negative inputs
    	for (int i = 2; i < length; i++)
    	{
    		Object arg = arguments[i];
    		if (arg instanceof double[])	// Input is complex
    		{
    			double[] in = ((double[])arg);
    			rSum -= in[0];
    			iSum -= in[1];
    		}
    		else	// Input is real
    			rSum -= FactorFunctionUtilities.toDouble(arg);
    	}
    	
		double[] out = ((double[])arguments[0]);
		out[0] = rSum;		// Replace the output value
		out[1] = iSum;		// Replace the output value
    }
}
