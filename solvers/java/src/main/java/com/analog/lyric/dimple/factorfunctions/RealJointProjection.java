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
 * Deterministic conversion of a real-joint variable to a real variable
 * corresponding to one specific element of the real-joint variable.
 * This is a deterministic directed factor (if smoothing is not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (Real)
 * 2) Input (RealJoint)
 * 
 * @since 0.07
 */
public class RealJointProjection extends FactorFunction
{
	private double _beta = 0;
	private boolean _smoothingSpecified = false;
	private int _elementIndex;
	
	public RealJointProjection(int elementIndex) {this(elementIndex, 0);}
	public RealJointProjection(int elementIndex, double smoothing)
	{
		super();
		_elementIndex = elementIndex;
		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	// Input RealJoint
		final double[] joint = arguments[1].getDoubleArray();
    	
    	if (_smoothingSpecified)
    	{
    		final double diff = arguments[0].getDouble() - joint[_elementIndex];
    		final double potential = diff*diff;
    		return potential*_beta;
    	}
    	else
    	{
    		if (arguments[0].getDouble() == joint[_elementIndex])
    			return 0;
    		else
    			return Double.POSITIVE_INFINITY;
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
		arguments[0].setDouble(arguments[1].getDoubleArray()[_elementIndex]);
    }
}
