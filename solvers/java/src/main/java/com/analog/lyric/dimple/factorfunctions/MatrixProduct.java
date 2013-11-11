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
 * Deterministic matrix product. This is a deterministic directed factor
 * (if smoothing is not enabled).
 * 
 * The constructor has three arguments that specify the sizes of the input and output
 * matrices.  The first two are the number of rows and columns, respectively, of the
 * first input matrix.  The third is the number of columns of the second input matrix.
 * The number of rows of the second input matrix must equal the number of columns of the
 * first input matrix.
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output matrix (Nr x Nc, scanned by columns [because MATLAB assumes this])
 * 2) Input matrix 1 (Nr x Nx, scanned by columns [because MATLAB assumes this])
 * 3) Input matrix 2 (Nx x Nc, scanned by columns [because MATLAB assumes this])
 * 
 */
public class MatrixProduct extends FactorFunction
{
	protected int _Nr;
	protected int _Nx;
	protected int _Nc;
	protected double[][] _in1;
	protected double[][] _in2;
	protected double[][] _out;
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	
	public MatrixProduct(int Nr, int Nx, int Nc) {this(Nr, Nx, Nc, 0);}
	public MatrixProduct(int Nr, int Nx, int Nc, double smoothing)
	{
		super();
		_Nr = Nr;
		_Nx = Nx;
		_Nc = Nc;
		_in1 = new double[Nr][Nx];
		_in2 = new double[Nx][Nc];
		_out = new double[Nr][Nc];

		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
    @Override
    public double evalEnergy(Object ... arguments)
    {
    	final int Nr = _Nr;
    	final int Nx = _Nx;
    	final int Nc = _Nc;
    	final double[][] out = _out;
    	double[][] in1 = _in1;
    	double[][] in2 = _in2;

    	int argIndex = 0;
    	
		// Get the output matrix values
    	for (int c = 0; c < Nc; c++)		// Scan by columns
    		for (int r = 0; r < Nr; r++)
    			out[r][c] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	
		// Get the first input matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		in1 = _in1 = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int x = 0; x < Nx; x++)		// Scan by columns
    			for (int r = 0; r < Nr; r++)
    				in1[r][x] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}

		// Get the second input matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		in2 = _in2 = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int c = 0; c < Nc; c++)		// Scan by columns
    			for (int x = 0; x < Nx; x++)
    				in2[x][c] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Compute the expected output and total error
    	double error = 0;
    	for (int c = 0; c < Nc; c++)
    	{
    		for (int r = 0; r < Nr; r++)
    		{
    			double[] in1r = in1[r];
    			double sum = 0;
    			for (int x = 0; x < Nx; x++)
    				sum += in1r[x] * in2[x][c];
    			double diff = out[r][c] - sum;
    			error += diff*diff;
    		}
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
    	int[] indexList = new int[_Nr * _Nc];
		for (int col = 0, i = 0; col < _Nc; col++)
			for (int row = 0; row < _Nr; row++, i++)
    		indexList[i] = i;
		return indexList;
	}
    @Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
    @Override
	public final void evalDeterministicFunction(Object[] arguments)
    {
    	final int Nr = _Nr;
    	final int Nx = _Nx;
    	final int Nc = _Nc;
    	double[][] in1 = _in1;
    	double[][] in2 = _in2;

    	int argIndex = Nr * Nc;	// Skip the outputs

		// Get the first input matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		in1 = _in1 = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int x = 0; x < Nx; x++)		// Scan by columns
    			for (int r = 0; r < Nr; r++)
    				in1[r][x] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}

		// Get the second input matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		in2 = _in2 = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int c = 0; c < Nc; c++)		// Scan by columns
    			for (int x = 0; x < Nx; x++)
    				in2[x][c] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Compute the output and replace the output values
    	int outIndex = 0;
    	for (int c = 0; c < Nc; c++)		// Scan by columns
    	{
    		for (int r = 0; r < Nr; r++)
    		{
    			double[] in1r = in1[r];
    			double sum = 0;
    			for (int x = 0; x < Nx; x++)
    				sum += in1r[x] * in2[x][c];
    			arguments[outIndex++] = sum;
    		}
    	}

    }
}
