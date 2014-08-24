/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.factorfunctions.core;

import com.analog.lyric.dimple.model.values.Value;

public abstract class DeterministicRealUnaryFactorFunction extends FactorFunction
{
	/**
	 * Deterministic unary factor function. This is a deterministic directed factor
	 * (if smoothing is not enabled).
	 * 
	 * Optional smoothing may be applied, by providing a smoothing value in
	 * the constructor. If smoothing is enabled, the distribution is
	 * smoothed by exp(-difference^2/smoothing), where difference is the
	 * distance between the output value and the deterministic output value
	 * for the corresponding inputs.
	 * 
	 * The variables are ordered as follows in the argument list:
	 * 
	 * 1) Output
	 * 2) Input
	 * 
	 */
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public DeterministicRealUnaryFactorFunction() {this(0);}
	public DeterministicRealUnaryFactorFunction(double smoothing)
	{
		super();
		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
	// Abstract method--override this
	protected abstract double myFunction(double in);

	@Override
	public double evalEnergy(Value[] arguments)
	{
		double out = arguments[0].getDouble();
		double in = arguments[1].getDouble();
		double value = myFunction(in);
		
		if (Double.isNaN(value))
			return Double.POSITIVE_INFINITY;

		if (_smoothingSpecified)
		{
			double diff = value - out;
			double potential = diff*diff;
			return potential*_beta;
		}
		else
		{
			return (value == out) ? 0 : Double.POSITIVE_INFINITY;
		}
	}


	@Override
	public final boolean isDirected()	{return true;}
	@Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
	@Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
	@Override
	public final void evalDeterministic(Value[] arguments)
	{
		arguments[0].setDouble(myFunction(arguments[1].getDouble()));		// Replace the output value
	}

}
