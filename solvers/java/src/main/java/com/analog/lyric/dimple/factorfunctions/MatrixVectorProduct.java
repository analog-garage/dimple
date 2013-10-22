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
 * 1) Output vector
 * 2) Input matrix (outLength x inLength, scanned by columns [because MATLAB assumes this])
 * 3) Input vector
 * 
 */
public class MatrixVectorProduct extends FactorFunction
{
	protected int _inLength;
	protected int _outLength;
	protected double[][] _matrix;
	protected double[] _inVector;
	protected double[] _outVector;
	protected double[] _expectedOutVector;
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	
	public MatrixVectorProduct(int inLength, int outLength) {this(inLength, outLength, 0);}
	public MatrixVectorProduct(int inLength, int outLength, double smoothing)
	{
		super();
		_inLength = inLength;
		_outLength = outLength;
    	_matrix = new double[_outLength][_inLength];
    	_inVector = new double[_inLength];
    	_outVector = new double[_outLength];
    	_expectedOutVector = new double[_outLength];

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
    	
    	// Get the output vector values
    	for (int i = 0; i < _outLength; i++)
    		_outVector[i] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);

		// Get the matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		_matrix = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int col = 0; col < _inLength; col++)
    			for (int row = 0; row < _outLength; row++)
    				_matrix[row][col] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Get the input vector values
    	if (arguments[argIndex] instanceof double[])	// Constant matrix is passed as a single argument
    		_inVector = (double[])arguments[argIndex++];
    	else
    	{
    		for (int i = 0; i < _inLength; i++)
    			_inVector[i] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Compute the expected output
    	for (int row = 0; row < _outLength; row++)
    	{
    		double sum = 0;
    		for (int col = 0; col < _inLength; col++)
    			sum += _matrix[row][col] * _inVector[col];
    		_expectedOutVector[row] = sum;
    	}

    	// Compute the total squared error
    	double error = 0;
    	for (int i = 0; i < _outLength; i++)
    	{
    		double diff = _outVector[i] - _expectedOutVector[i];
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
	public final int[] getDirectedToIndices()
	{
    	int[] indexList = new int[_outLength];
    	for (int i = 0; i < _outLength; i++)
    		indexList[i] = i;
		return indexList;
	}
    @Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
    @Override
	public final void evalDeterministicFunction(Object[] arguments)
    {
    	int argIndex = _outLength;	// Skip the outputs
    	
    	final int inLength = _inLength;
    	final int outLength = _outLength;
    	
    	double[][] matrix = _matrix;
    	
		// Get the matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		_matrix = matrix = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int col = 0; col < inLength; col++)
    			for (int row = 0; row < outLength; row++)
    				matrix[row][col] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Get the input vector values
    	double[] inVector = _inVector;
    	if (arguments[argIndex] instanceof double[])	// Constant matrix is passed as a single argument
    		_inVector = inVector = (double[])arguments[argIndex++];
    	else
    	{
    		for (int i = 0; i < inLength; i++)
    			inVector[i] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Compute the output
    	double[] outVector = _outVector;
    	for (int row = 0; row < outLength; row++)
    	{
    		double sum = 0;
    		double[] rowValues = matrix[row];
    		for (int col = 0; col < inLength; col++)
    			sum += rowValues[col] * inVector[col];
    		outVector[row] = sum;
    	}
    	
    	// Replace the output values
    	int outIndex = 0;
    	for (int i = 0; i < outLength; i++)
    		arguments[outIndex++] = outVector[i];
    }
}
