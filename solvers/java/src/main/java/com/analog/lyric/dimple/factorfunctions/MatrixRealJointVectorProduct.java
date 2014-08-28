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

import static java.util.Objects.*;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.util.misc.Matlab;


/**
 * Deterministic matrix-vector product. This is a deterministic directed factor
 * (if smoothing is not enabled).
 * 
 * The constructor has two arguments that specify the length of the input and output
 * vectors, respectively.
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output vector (RealJoint)
 * 2) Input matrix (outLength x inLength, scanned by columns [because MATLAB assumes this])
 * 3) Input vector (RealJoint)
 * 
 * @since 0.05
 */
public class MatrixRealJointVectorProduct extends FactorFunction
{
	private int _inLength;
	private int _outLength;
	private double _beta = 0;
	private boolean _smoothingSpecified = false;
	
	@Matlab
	public MatrixRealJointVectorProduct(int inLength, int outLength) {this(inLength, outLength, 0);}
	public MatrixRealJointVectorProduct(int inLength, int outLength, double smoothing)
	{
		super();
		_inLength = inLength;
		_outLength = outLength;

		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	// Compute the expected output
		final Value[] expectedResult = evalDeterministicToCopy(arguments);

		// Compare the output to the expected output
		final double[] outValue = arguments[0].getDoubleArray();
		final double[] expectedOutValue = expectedResult[0].getDoubleArray();
		final int outLength = _outLength;
		double error = 0;
		for (int i = 0; i < outLength; i++)
		{
			final double diff = outValue[i] - expectedOutValue[i];
			error += diff*diff;
		}

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
	public void evalDeterministic(Value[] arguments)
    {
    	int argIndex = 0;
    	double[] outVector = arguments[argIndex++].getDoubleArray();

    	final int inLength = _inLength;
    	final int outLength = _outLength;

    	// How is the matrix passed?
    	if (arguments[argIndex].getObject() instanceof double[][])
    	{
    		// Constant matrix is passed as a single argument; get the matrix values
    		final double[][] matrix = (double[][])requireNonNull(arguments[argIndex++].getObject());

        	// Get the input vector values
    		final double[] inVector = arguments[argIndex++].getDoubleArray();

        	// Compute the output and replace the output values
        	for (int row = 0; row < outLength; row++)
        	{
        		double sum = 0;
        		final double[] rowValues = matrix[row];
        		for (int col = 0; col < inLength; col++)
        			sum += rowValues[col] * inVector[col];
        		outVector[row] = sum;
        	}
    	}
    	else
    	{
    		// Variable matrix is passed as individual elements
    		final int numMatrixElements = inLength * outLength;

        	// Get the input vector values
    		final double[] inVector = arguments[argIndex + numMatrixElements].getDoubleArray();

        	// Compute the output and replace the output values
        	for (int row = 0, rowOffset = argIndex; row < outLength; row++, rowOffset++)
        	{
        		double sum = 0;
        		for (int col = 0, offset = rowOffset; col < inLength; col++, offset += outLength)
        			sum += arguments[offset].getDouble() * inVector[col];
        		outVector[row] = sum;
        	}
    	}
    }

}
