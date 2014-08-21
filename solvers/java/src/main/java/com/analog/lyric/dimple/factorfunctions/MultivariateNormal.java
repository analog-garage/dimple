/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;


/**
 * Factor for an exchangeable set of multivariate Normally distributed variables associated
 * with a vector representing the mean parameter and a matrix (in the form of an array of vectors)
 * representing the precision, or alternatively a vector representing the information vector and
 * a matrix representing the information matrix.
 * 
 * TODO: THE CURRENT IMPLEMENTATION OF THIS FACTOR FUNCTION SUPPORTS ONLY CONSTANT PARAMETRES,
 * SPECIFIED IN THE CONSTRUCTOR.  VARIABLE PARAMETER SUPPORT SHOULD BE ADDED.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1...) An arbitrary number of RealJoint variables
 * 
 * @since 0.05
 */
public class MultivariateNormal extends FactorFunction
{
	private MultivariateNormalParameters _parameters;
	private double[][] _informationMatrix;
	private double[] _mean;
	private double[] _diff;
	private boolean _parametersConstant;
	private int _firstDirectedToIndex;
	private int _dimension;
	private double _normalizationConstant;
	protected static final double _logSqrt2pi = Math.log(2*Math.PI)*0.5;


	// Constructors
//	public MultivariateNormal() {super();}	// TODO: Implement variable parameters case
	public MultivariateNormal(double[] mean, double[][] covariance)
	{
		this(new MultivariateNormalParameters(mean, covariance));
	}
	public MultivariateNormal(double[] vector, double[][] matrix, boolean informationForm)
	{
		this(new MultivariateNormalParameters(vector, matrix, informationForm));
	}

	public MultivariateNormal(MultivariateNormalParameters parameters)
	{
		super();
		_parameters = parameters;
		_informationMatrix = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
		_mean = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_diff = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		initializeConstantParameters(parameters);
	}
	
	// Common initialization method used when parameters are constant
	private void initializeConstantParameters(MultivariateNormalParameters parameters)
	{
		_parameters = parameters;
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
		_dimension = parameters.getVectorLength();
		_diff = new double[_dimension];
		if (parameters.isInInformationForm())
		{
			_informationMatrix = parameters.getInformationMatrix();
			_mean = parameters.getMean();	// Requires conversion, do this last
		}
		else	// Mean and covariance form
		{
			_mean = parameters.getMean();
			_informationMatrix = parameters.getInformationMatrix();	// Requires conversion, do this last
		}
		Jama.Matrix informationMatrix = new Jama.Matrix(_informationMatrix);
		double determinant = informationMatrix.det();
		_normalizationConstant = (_dimension * _logSqrt2pi) - (Math.log(determinant) * 0.5);
	}
	

    @Override
	public final double evalEnergy(Value[] arguments)
	{
    	int index = 0;
    	final int length = arguments.length;
    	final int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		final double[] x = arguments[index].getDoubleArray();	// Remaining inputs are multivariate Normal random variable vectors
    		if (x.length != _dimension)
	    		throw new DimpleException("Dimension of variable does not equal to the dimension of the parameter vector.");
    		for (int i = 0; i < _dimension; i++)
    			_diff[i] = x[i] - _mean[i];
    		double colSum = 0;
    		for (int row = 0; row < _dimension; row++)
    		{
    			double rowSum = 0;
    			final double[] informationMatrixRow = _informationMatrix[row];
    			for (int col = 0; col < _dimension; col++)
    				rowSum += informationMatrixRow[col] * _diff[col];	// Matrix * vector
    			colSum += rowSum * _diff[row];	// Vector * vector
    		}
    		sum += colSum;
    	}
    	return sum * 0.5 + N * _normalizationConstant;
	}
    
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices(int numEdges)
	{
    	// All edges except the parameter edges (if present) are directed-to edges
		return FactorFunctionUtilities.getListOfIndices(_firstDirectedToIndex, numEdges-1);
	}
    
    
    // Factor-specific methods
    public final boolean hasConstantParameters()
    {
    	return _parametersConstant;
    }
    public final MultivariateNormalParameters getParameters()
    {
    	return _parameters.clone();
    }
    public final void setParameters(MultivariateNormalParameters parameters)
    {
    	initializeConstantParameters(parameters);
    }
}
