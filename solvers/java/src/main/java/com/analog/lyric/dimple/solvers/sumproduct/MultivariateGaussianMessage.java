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

package com.analog.lyric.dimple.solvers.sumproduct;

import Jama.Matrix;

import com.analog.lyric.math.LyricEigenvalueDecomposition;

public class MultivariateGaussianMessage implements Cloneable
{

	private double [] _vector;
	private double [][] _matrix;	
	private boolean _isInInformationForm;
	
	//private EigenDecomposition eigendecomposition;
	private double eps = 0.0000001; //minimum value for small eigenvalues or 1/(max value)

	
	public MultivariateGaussianMessage clone()
	{
		double [][] m = new double[_matrix.length][];
		for (int i = 0; i < m.length; i++)
			m[i] = _matrix[i].clone();
		
		return new MultivariateGaussianMessage(_vector.clone(),m);
	}
	
	public boolean isInInformationForm()
	{
		return _isInInformationForm;
	}
	public MultivariateGaussianMessage()
	{
		
	}
	
	public MultivariateGaussianMessage(double [] means, double [][] covariance)
	{
		setMeanAndCovariance(means.clone(), cloneMatrix(covariance));
	}

	public void setInformation(double [] informationVector, double [][] informationMatrix)
	{
		_vector = informationVector.clone();
		_matrix = cloneMatrix(informationMatrix);
		_isInInformationForm = true;
	}

	public void setMeanAndCovariance(double [] means, double [][] covariance)
	{
		_vector = means.clone();
		_matrix = cloneMatrix(covariance);
		_isInInformationForm = false;
	}
	
	public void set(MultivariateGaussianMessage message)	// Set from another message without first extracting the components or determining which form
	{
		_vector = message._vector.clone();
		_matrix = cloneMatrix(message._matrix);
		_isInInformationForm = message._isInInformationForm;
	}

	public double [] getInformationVector() 
	{
		if (!_isInInformationForm)
			ConvertType();
		
		return _vector.clone();
	}

	public double [][] getInformationMatrix() 
	{
		if (!_isInInformationForm)
			ConvertType();
		
		return cloneMatrix(_matrix);

	}
	
	private double [][] cloneMatrix(double [][] matrix)
	{
		double [][] retval = new double[matrix.length][];
		for (int i = 0; i < retval.length; i++)
			retval[i] = matrix[i].clone();
		
		return retval;
	}


	public double [] getMeans() 
	{
		if (_isInInformationForm)
			ConvertType();
		return _vector.clone();
	}

	public double [][] getCovariance() 
	{
		if (_isInInformationForm)
			ConvertType();

		return cloneMatrix(_matrix);
	}
	
	public int getVectorLength()
	{
		return _vector.length;
	}

	private boolean isInfiniteIdentity(Matrix m)
	{
		for (int i = 0; i < m.getColumnDimension(); i++)
		{
			if (!Double.isInfinite(m.get(i,i)))
				return false;
		}
		return true;
	}
	
	private void ConvertType() 
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
