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

package com.analog.lyric.dimple.solvers.core.parameterizedMessages;

import Jama.Matrix;

import com.analog.lyric.math.LyricEigenvalueDecomposition;

public class MultivariateNormalParameters implements Cloneable, IParameterizedMessage
{

	private double [] _vector;
	private double [][] _matrix;	
	private boolean _isInInformationForm;
	private double eps = 0.0000001; //minimum value for small eigenvalues or 1/(max value)

	
	// Constructors
	public MultivariateNormalParameters() {}
	public MultivariateNormalParameters(double[] mean, double[][] covariance)
	{
		setMeanAndCovariance(mean.clone(), cloneMatrix(covariance));
	}
	public MultivariateNormalParameters(MultivariateNormalParameters other)		// Copy constructor
	{
		set(other);
	}
	
	public MultivariateNormalParameters clone()
	{
		return new MultivariateNormalParameters(this);
	}
	

	public final void setMeanAndCovariance(double[] mean, double[][] covariance)
	{
		_vector = mean.clone();
		_matrix = cloneMatrix(covariance);
		_isInInformationForm = false;
	}
	
	public final void setInformation(double[] informationVector, double[][] informationMatrix)
	{
		_vector = informationVector.clone();
		_matrix = cloneMatrix(informationMatrix);
		_isInInformationForm = true;
	}

	// Set from another parameter set without first extracting the components or determining which form
	public final void set(MultivariateNormalParameters other)
	{
		_vector = other._vector.clone();
		_matrix = cloneMatrix(other._matrix);
		_isInInformationForm = other._isInInformationForm;
	}
	
	@Override
	public final void setNull()
	{
		_vector = null;
		_matrix = null;
		_isInInformationForm = true;
	}
	
	public final double[] getMeans() {return getMean();}	// For backward compatibility
	public final double[] getMean() 
	{
		if (_isInInformationForm)
			ConvertType();
		return _vector.clone();
	}

	public final double [][] getCovariance() 
	{
		if (_isInInformationForm)
			ConvertType();

		return cloneMatrix(_matrix);
	}
	
	public final double [] getInformationVector() 
	{
		if (!_isInInformationForm)
			ConvertType();
		
		return _vector.clone();
	}

	public final double [][] getInformationMatrix() 
	{
		if (!_isInInformationForm)
			ConvertType();
		
		return cloneMatrix(_matrix);

	}
	
	public final int getVectorLength()
	{
		return _vector.length;
	}
	
	public final boolean isInInformationForm()
	{
		return _isInInformationForm;
	}
	
	public final boolean isNull()
	{
		return _vector == null;
	}
	
	private final double[][] cloneMatrix(double[][] matrix)
	{
		double[][] retval = new double[matrix.length][];
		for (int i = 0; i < retval.length; i++)
			retval[i] = matrix[i].clone();
		
		return retval;
	}


	private final boolean isInfiniteIdentity(Matrix m)
	{
		for (int i = 0; i < m.getColumnDimension(); i++)
		{
			if (!Double.isInfinite(m.get(i,i)))
				return false;
		}
		return true;
	}
	
	private final void ConvertType() 
	{
		int i;
		
		Jama.Matrix mat = new Jama.Matrix(_matrix);
				
		Matrix inv;
		Matrix vec;
				
		if (isInfiniteIdentity(mat))
		{
			//Handle the special case where variances are infinite
			inv = new Matrix(mat.getRowDimension(),mat.getColumnDimension());
			vec = new Matrix(mat.getRowDimension(),1);
		}
		else
		{
			LyricEigenvalueDecomposition eig = new LyricEigenvalueDecomposition(mat);
			
			Matrix D = eig.getD();
			Matrix V = eig.getV();
			
			int N = D.getColumnDimension();
			
			for (i=0; i<N; i++) {
				//Compute inverse of eigenvalues except for those less than eps we set to large constant.

				double d = D.get(i,i);
				d = (d>eps) ? (1/d) : 1/eps;
				D.set(i,i,d);
	
				assert(d > 0); // Eigenvalues should always be positive for positive definite matrices 
			}
			
			inv = V.times(D.times(V.transpose()));
	
	
			vec = new Matrix(new double [][] {_vector}).transpose();
			
			vec = inv.times(vec);
		}

		_matrix = inv.getArray();
		_vector = vec.transpose().getArray()[0];
		
		_isInInformationForm = !_isInInformationForm;

	}

}
