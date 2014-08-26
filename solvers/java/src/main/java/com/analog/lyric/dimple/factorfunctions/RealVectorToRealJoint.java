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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Deterministic conversion of a vector of real variables to a real-joint variable.
 * This is a deterministic directed factor (if smoothing is not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (RealJoint vector - dimension equal to the number of Real input variables)
 * 2) Input (vector of Real variables)
 * 
 * @since 0.07
 */
public class RealVectorToRealJoint extends FactorFunction
{
	private double _beta = 0;
	private boolean _smoothingSpecified = false;
	
	public RealVectorToRealJoint() {this(0);}
	public RealVectorToRealJoint(double smoothing)
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
    	final int dimension = arguments.length - 1;

    	// Output RealJoint
		final double[] joint = arguments[0].getDoubleArray();
		if (dimension != joint.length) throw new DimpleException("RealJoint argument does not have the correct dimension");
    	
    	if (_smoothingSpecified)
    	{
    		double potential = 0;
    		for (int d = 0; d < dimension; d++)
    		{
    			final double diff = arguments[d + 1].getDouble() - joint[d];
    			potential += diff*diff;
    		}
    		return potential*_beta;
    	}
    	else
    	{
    		boolean equal = true;
    		for (int d = 0; d < dimension; d++)
    			if (arguments[d + 1].getDouble() != joint[d])
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
    	final int dimension = arguments.length - 1;

    	// Output RealJoint
		final double[] joint = arguments[0].getDoubleArray();
		if (dimension != joint.length) throw new DimpleException("RealJoint argument does not have the correct dimension");

		for (int d = 0; d < dimension; d++)
			joint[d] = arguments[d + 1].getDouble();
    }
}
