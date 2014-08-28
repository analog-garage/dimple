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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.IndexedValue;
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
	private final int _updateDeterministicLimit;
	
	@Matlab
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
			_updateDeterministicLimit = 0;
		}
		else
		{
			// A full update requires inLength*outLength multiply/adds. An incremental update
			// will take either 2 for changes to input matrix, or outLength*2 for changes to
			// input vector. So for matrix input changes the limit should be inLength*outLength/2
			// and for vector input changes the limit should be inLength/2. We will use the min of
			// these two for now:
			_updateDeterministicLimit = inLength / 2;
		}
	}
	
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	// Compute the expected output
		final Value[] expectedResult = evalDeterministicToCopy(arguments);

		// Compare the output to the expected output
		final int outLength = _outLength;
		double error = 0;
		for (int i = 0; i < outLength; i++)
		{
			final double diff = arguments[i].getDouble() - expectedResult[i].getDouble();
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
    public final @Nullable int[] getDirectedToIndicesForInput(Factor factor, int inputEdge)
    {
    	final FactorFunction function = factor.getFactorFunction();
    	
    	final int outLength = _outLength;
    	final int inLength = _inLength;
    	
    	final int nEdges = factor.getSiblingCount() + function.getConstantCount();
    	final int nInputEdges = nEdges - outLength;
    	
    	final int matrixSize = outLength * inLength;
    	final int vectorSize = inLength;
    		
    	final int matrixOffset = outLength;
    	int vectorOffset = matrixOffset;
    	
    	if (nInputEdges == matrixSize + vectorSize ||
    		nInputEdges == matrixSize + 1 && function.getConstantByIndex(nEdges - 1) instanceof double[])
    	{
    		vectorOffset += matrixSize;
    	}
    	else if (nInputEdges == 2 ||
    		nInputEdges == vectorSize + 1 && function.getConstantByIndex(matrixOffset) instanceof double[][])
    	{
    		vectorOffset += 1;
    	}
    	else
    	{
    		throw new DimpleException("Bad number of edges %d for MatrixVectorProduct (inLength=%d, outLength=%d)",
    			nEdges, inLength, outLength);
    	}
    	
    	if (inputEdge >= vectorOffset)
    	{
    		// Same as full output edges
    		return null;
    	}
    	else
    	{
			return new int[] { (inputEdge - matrixOffset) % outLength };
    	}
    	
    }
    
    @Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
    @Override
	public final void evalDeterministic(Value[] arguments)
    {
    	int argIndex = _outLength;	// Skip the outputs
    	
    	final int inLength = _inLength;
    	final int outLength = _outLength;
    	
    	double[][] matrix = _matrix;
    	
		// Get the matrix values
    	if (arguments[argIndex].getObject() instanceof double[][])	// Constant matrix is passed as a single argument
    		matrix = (double[][])requireNonNull(arguments[argIndex++].getObject());
    	else
    	{
    		for (int col = 0; col < inLength; col++)
    			for (int row = 0; row < outLength; row++)
    				matrix[row][col] = arguments[argIndex++].getDouble();
    	}
    	
    	// Get the input vector values
    	double[] inVector = _inVector;
    	if (arguments[argIndex].getObject() instanceof double[])	// Constant matrix is passed as a single argument
    		inVector = arguments[argIndex++].getDoubleArray();
    	else
    	{
    		for (int i = 0; i < inLength; i++)
    			inVector[i] = arguments[argIndex++].getDouble();
    	}
    	
    	// Compute the output
    	double[] outVector = _outVector;
    	for (int row = 0; row < outLength; row++)
    	{
    		double sum = 0;
    		final double[] rowValues = matrix[row];
    		for (int col = 0; col < inLength; col++)
    			sum += rowValues[col] * inVector[col];
    		outVector[row] = sum;
    	}
    	
    	// Replace the output values
    	int outIndex = 0;
    	for (int i = 0; i < outLength; i++)
    		arguments[outIndex++].setDouble(outVector[i]);
    }
    
    @Override
    public final int updateDeterministicLimit(int numEdges)
    {
    	return _updateDeterministicLimit;
    }
    
    @Override
    public final boolean updateDeterministic(Value[] values, Collection<IndexedValue> oldValues, AtomicReference<int[]> changedOutputsHolder)
    {
    	final int inLength = _inLength;
    	final int outLength = _outLength;
    	final int matrixOffset = outLength;
    	
    	final Object objAtMatrixOffset = values[matrixOffset].getObject();
    	final double[][] matrix = objAtMatrixOffset instanceof double[][] ? (double[][])objAtMatrixOffset : null;
    	
    	final int vectorOffset = matrixOffset + (matrix != null ? 1 : inLength * outLength);
    	final Object objAtVectorOffset = values[vectorOffset].getObject();
    	final double[] vector = objAtVectorOffset instanceof double[] ? (double[])objAtVectorOffset : null;
    	
    	final int minSupportedIndex = matrix == null ? matrixOffset : (vector == null ? vectorOffset : values.length);
    	final int maxSupportedIndex = vector == null ? values.length : (matrix == null ? vectorOffset : matrixOffset);
    	
    	boolean incremental = false;
    	
    	doIncremental:
    	{
    		for (IndexedValue old : oldValues)
    		{
    			final int changedIndex = old.getIndex();

    			if (changedIndex < matrixOffset || changedIndex >= values.length)
    			{
					throw new IndexOutOfBoundsException();
    			}

    			if (changedIndex < minSupportedIndex || changedIndex >= maxSupportedIndex)
    			{
    				break doIncremental;
    			}

    			final double newInput = values[changedIndex].getDouble();
    			final double oldInput = old.getValue().getDouble();
    			if (newInput == oldInput)
    			{
    				continue;
    			}

    			if (changedIndex >= vectorOffset)
    			{
    				// Input vector variable changed
    				final int col = changedIndex - vectorOffset;
    				if (matrix != null)
    				{
    					for (int row = 0; row < outLength; ++row)
    					{
    						final Value outputValue = values[row];
    						final double oldOutput = outputValue.getDouble();
    						final double matrixValue = matrix[row][col];
    						outputValue.setDouble(oldOutput - matrixValue * oldInput + matrixValue * newInput);
    					}
    				}
    				else
    				{
    					int matrixIndex = matrixOffset + col * outLength;
    					for (int row = 0; row < outLength; ++row, ++matrixIndex)
    					{
    						final Value outputValue = values[row];
    						final double oldOutput = outputValue.getDouble();
    						final double matrixValue = values[matrixIndex].getDouble();
    						outputValue.setDouble(oldOutput - matrixValue * oldInput + matrixValue * newInput);
    					}
    				}
    			}
    			else
    			{
    				// Matrix value changed
    				int x = changedIndex - matrixOffset;
    				final int col = x / outLength;
    				final int row = x - col * outLength;

    				Value outputValue = values[row];
    				final double oldOutput = outputValue.getDouble();
    				final double inVectorVal = vector != null ? vector[col] : values[vectorOffset + col].getDouble();
    				outputValue.setDouble(oldOutput - inVectorVal * oldInput + inVectorVal * newInput);
    			}
    		}
    		incremental = true;
    	}
    	
    	return incremental || super.updateDeterministic(values, oldValues, changedOutputsHolder);
    }
}
