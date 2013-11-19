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

package com.analog.lyric.dimple.factorfunctions;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;


/**
 * Deterministic vector inner product. This is a deterministic directed factor
 * (if smoothing is not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output value
 * 2) First input vector (may be either a list of scalars or a RealJoint vector)
 * 3) Second input vector (may be either a list of scalars or a RealJoint vector)
 * 
 */
public class VectorInnerProduct extends FactorFunction
{
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;

	public VectorInnerProduct() {this(0);}
	public VectorInnerProduct(double smoothing)
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
		double expectedOutValue = 0;
		double[] firstInput = null;
		double[] secondInput = null;
		boolean firstInputIsArray = false;
		boolean secondInputIsArray = false;
		
		// Figure out the type of inputs (array or list of arguments)
		final int numArgs = arguments.length;
		Object firstInputArg = arguments[1];
		Object lastInputArg = arguments[numArgs-1];
		if (firstInputArg instanceof double[])
		{
			firstInput = (double[])firstInputArg;
			firstInputIsArray = true;
		}
		if (lastInputArg instanceof double[])
		{
			secondInput = (double[])lastInputArg;
			secondInputIsArray = true;
		}
		
		// Compute the output
		if (firstInputIsArray && secondInputIsArray)
		{
			int vectorLength = firstInput.length;
			for (int i = 0; i < vectorLength; i++)
				expectedOutValue += firstInput[i] * secondInput[i];
		}
		else if (firstInputIsArray)
		{
			int vectorLength = firstInput.length;
			int secondIndex = 2;
			for (int i = 0; i < vectorLength; i++)
				expectedOutValue += firstInput[i] * FactorFunctionUtilities.toDouble(arguments[secondIndex++]);
		}
		else if (secondInputIsArray)
		{
			int vectorLength = secondInput.length;
			int firstIndex = 1;
			for (int i = 0; i < vectorLength; i++)
				expectedOutValue += FactorFunctionUtilities.toDouble(arguments[firstIndex++]) * secondInput[i];
		}
		else	// Neither input is array
		{
			int vectorLength = (numArgs - 1) >> 1;
			int firstIndex = 1;
			int secondIndex = 1 + vectorLength;
			for (int i = 0; i < vectorLength; i++)
				expectedOutValue += FactorFunctionUtilities.toDouble(arguments[firstIndex++]) * FactorFunctionUtilities.toDouble(arguments[secondIndex++]);
		}

		// Get the output value
		double outValue = FactorFunctionUtilities.toDouble(arguments[0]);

		double diff = (outValue - expectedOutValue);
		double error = diff*diff;

		if (_smoothingSpecified)
			return error*_beta;
		else
			return (error == 0) ? 0 : Double.POSITIVE_INFINITY;
	}


	@Override
	public final boolean isDirected() {return true;}
	@Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
	@Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
	@Override
	public final void evalDeterministicFunction(Object[] arguments)
	{
		double outValue = 0;
		double[] firstInput = null;
		double[] secondInput = null;
		boolean firstInputIsArray = false;
		boolean secondInputIsArray = false;
		
		// Figure out the type of inputs (array or list of arguments)
		final int numArgs = arguments.length;
		Object firstInputArg = arguments[1];
		Object lastInputArg = arguments[numArgs-1];
		if (firstInputArg instanceof double[])
		{
			firstInput = (double[])firstInputArg;
			firstInputIsArray = true;
		}
		if (lastInputArg instanceof double[])
		{
			secondInput = (double[])lastInputArg;
			secondInputIsArray = true;
		}
		
		// Compute the output
		if (firstInputIsArray && secondInputIsArray)
		{
			int vectorLength = firstInput.length;
			for (int i = 0; i < vectorLength; i++)
				outValue += firstInput[i] * secondInput[i];
		}
		else if (firstInputIsArray)
		{
			int vectorLength = firstInput.length;
			int secondIndex = 2;
			for (int i = 0; i < vectorLength; i++)
				outValue += firstInput[i] * FactorFunctionUtilities.toDouble(arguments[secondIndex++]);
		}
		else if (secondInputIsArray)
		{
			int vectorLength = secondInput.length;
			int firstIndex = 1;
			for (int i = 0; i < vectorLength; i++)
				outValue += FactorFunctionUtilities.toDouble(arguments[firstIndex++]) * secondInput[i];
		}
		else	// Neither input is array
		{
			int vectorLength = (numArgs - 1) >> 1;
			int firstIndex = 1;
			int secondIndex = 1 + vectorLength;
			for (int i = 0; i < vectorLength; i++)
				outValue += FactorFunctionUtilities.toDouble(arguments[firstIndex++]) * FactorFunctionUtilities.toDouble(arguments[secondIndex++]);
		}

		// Replace the output values
		arguments[0] = outValue;
	}
}
