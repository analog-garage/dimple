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
	private double[][] _matrix;
	private double _beta = 0;
	private boolean _smoothingSpecified = false;
	
	@Matlab
	public MatrixRealJointVectorProduct(int inLength, int outLength) {this(inLength, outLength, 0);}
	public MatrixRealJointVectorProduct(int inLength, int outLength, double smoothing)
	{
		super();
		_inLength = inLength;
		_outLength = outLength;
    	_matrix = new double[_outLength][_inLength];

		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
    @Override
    public double evalEnergy(Object ... arguments)
    {
    	int argIndex = 0;
    	
    	final int inLength = _inLength;
    	final int outLength = _outLength;
    	double[][] matrix = _matrix;

    	// Get the output vector values
		double[] outVector = (double[])arguments[argIndex++];

		// Get the matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		matrix = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int col = 0; col < _inLength; col++)
    			for (int row = 0; row < outLength; row++)
    				matrix[row][col] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Get the input vector values
    	double[] inVector = (double[])arguments[argIndex++];
    	
    	// Compute the expected output and the total error
    	double error = 0;
    	for (int row = 0; row < outLength; row++)
    	{
    		double sum = 0;
    		double[] rowValues = matrix[row];
    		for (int col = 0; col < inLength; col++)
    			sum += rowValues[col] * inVector[col];
    		double diff = outVector[row] - sum;
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
	public final void evalDeterministic(Object[] arguments)
    {
    	int argIndex = 1;	// Skip the outputs
    	
    	final int inLength = _inLength;
    	final int outLength = _outLength;
    	
    	double[][] matrix = _matrix;
    	
		// Get the matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		matrix = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int col = 0; col < inLength; col++)
    			for (int row = 0; row < outLength; row++)
    				matrix[row][col] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Get the input vector values
    	double[] inVector = (double[])arguments[argIndex++];
    	
    	// Compute the output and replace the output values
    	double[] outVector = ((double[])arguments[0]);
    	for (int row = 0; row < outLength; row++)
    	{
    		double sum = 0;
    		double[] rowValues = matrix[row];
    		for (int col = 0; col < inLength; col++)
    			sum += rowValues[col] * inVector[col];
    		outVector[row] = sum;
    	}
    }
    
}
