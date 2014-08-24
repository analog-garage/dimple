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

import java.util.Arrays;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Deterministic sum for real-joint variables. This is a deterministic directed factor (if smoothing is
 * not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (sum of inputs)
 * 2...) An arbitrary number of inputs (RealJoint)
 * 
 * @since 0.05
 */
public class RealJointSum extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public RealJointSum() {this(0);}
	public RealJointSum(double smoothing)
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
    	// Output variable
    	final int length = arguments.length;
    	final double[] out = arguments[0].getDoubleArray();
    	final int dimension = out.length;

		// Sum input variables
    	final double[] sum = new double[dimension];	// Zero filled
    	for (int i = 1; i < length; i++)
    	{
    		final double[] arg = arguments[i].getDoubleArray();
    		if (dimension != arg.length) throw new DimpleException("Argument variables must all have the same dimension");
    		for (int d = 0; d < dimension; d++)
    			sum[d] += arg[d];
    	}
    	
    	if (_smoothingSpecified)
    	{
    		double potential = 0;
    		for (int d = 0; d < dimension; d++)
    		{
    			final double diff = sum[d] - out[d];
    			potential += diff*diff;
    		}
    		return potential*_beta;
    	}
    	else
    	{
    		boolean equal = true;
    		for (int d = 0; d < dimension; d++)
    			if (sum[d] != out[d])
    				equal = false;
    		return (equal) ? 0 : Double.POSITIVE_INFINITY;
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
    	// Output variable
    	final int length = arguments.length;
    	final double[] out = arguments[0].getDoubleArray();
    	final int dimension = out.length;

    	// Sum input variables
    	Arrays.fill(out, 0);
    	for (int i = 1; i < length; i++)
    	{
    		final double[] arg = arguments[i].getDoubleArray();
    		if (dimension != arg.length) throw new DimpleException("Argument variables must all have the same dimension");
    		for (int d = 0; d < dimension; d++)
    			out[d] += arg[d];
    	}
    }
}
