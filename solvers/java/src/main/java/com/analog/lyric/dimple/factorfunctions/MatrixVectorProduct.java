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

import java.util.Collection;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.solvers.gibbs.sample.IndexedSample;
import com.analog.lyric.dimple.solvers.gibbs.sample.ObjectSample;


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
    	final double[] outVector = _outVector;
    	for (int i = 0; i < outLength; i++)
    		outVector[i] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);

		// Get the matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		matrix = _matrix = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int col = 0; col < _inLength; col++)
    			for (int row = 0; row < outLength; row++)
    				matrix[row][col] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Get the input vector values
    	double[] inVector = _inVector;
    	if (arguments[argIndex] instanceof double[])	// Constant matrix is passed as a single argument
    		inVector = _inVector = (double[])arguments[argIndex++];
    	else
    	{
    		for (int i = 0; i < inLength; i++)
    			inVector[i] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
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
    
    @Override
    public final int updateDeterministicLimit()
    {
    	// FIXME: is this a good value?
    	return _inLength;
    }
    
    @Override
    public final void updateDeterministic(ObjectSample[] values, Collection<IndexedSample> oldValues)
    {
    	final int inLength = _inLength;
    	final int outLength = _outLength;
    	final int matrixOffset = outLength;
    	
    	final Object objAtMatrixOffset = values[matrixOffset].getObject();
    	final boolean hasConstantMatrix =  objAtMatrixOffset instanceof double[][];
    	final double[][] constantMatrix = hasConstantMatrix ? (double[][])objAtMatrixOffset : null;
    	
    	final int minChangedIndex = hasConstantMatrix ? matrixOffset + 1 : matrixOffset;
    	final int inputOffset = hasConstantMatrix ? matrixOffset + 1 : matrixOffset + inLength * outLength;
    	
    	for (IndexedSample old : oldValues)
    	{
    		final int changedIndex = old.getIndex();
    		
    		if (changedIndex < minChangedIndex)
    		{
    			throw new DimpleException("Changed index value %d does not refer to an input variable.", changedIndex);
    		}
    		
    		final double newInput = values[changedIndex].getDouble();
    		final double oldInput = old.getValue().getDouble();
    		if (newInput == oldInput)
    		{
    			continue;
    		}
    		
    		if (changedIndex >= inputOffset)
    		{
    			// Input vector variable changed
    			final int col = changedIndex - inputOffset;
    			for (int row = 0; row < outLength; ++row)
    			{
    				final double oldOutput = values[row].getDouble();
    				final double matrixValue = hasConstantMatrix ? constantMatrix[row][col] :
    					values[matrixOffset + col * outLength + row].getDouble();
    				
    				values[row].setDouble(oldOutput - matrixValue * oldInput + matrixValue * newInput);
    			}
    		}
    		else
    		{
    			// Matrix value changed
    			int x = changedIndex - matrixOffset;
    			final int col = x / outLength;
    			final int row = x - col * outLength;
    			
    			final double oldOutput = values[row].getDouble();
    			final double inVectorVal = values[inputOffset + col].getDouble();
    			values[row].setDouble(oldOutput - inVectorVal * oldInput + inVectorVal * newInput);
    		}
    	}
    }
}
