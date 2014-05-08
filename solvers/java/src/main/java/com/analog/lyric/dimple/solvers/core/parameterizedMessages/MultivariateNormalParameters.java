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
	private static final long serialVersionUID = 1L;

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
	
	@Override
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
	
	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * For multivariate normal distributions, the formula is given by:
	 * 
	 * <blockquote>
	 * &frac12; {
	 * trace(&Sigma;<sub>Q</sub><sup>-1</sup>&Sigma;<sub>P</sub>) +
	 * (&mu;<sub>Q</sub>-&mu;<sub>P</sub>)<sup>T</sup>&Sigma;<sub>Q</sub><sup>-1</sup>(&mu;<sub>Q</sub>-&mu;<sub>P</sub>)
	 * -K - ln(det(&Sigma;<sub>P</sub>)/det(&Sigma;<sub>Q</sub>)))
	 * }
	 * </blockquote>
	 * Note that this assumes that the determinants of the covariance matrices are non-zero.
	 */
	@Override
	public double computeKLDivergence(IParameterizedMessage that)
	{
		if (that instanceof MultivariateNormalParameters)
		{
			// http://en.wikipedia.org/wiki/Multivariate_normal_distribution#Kullback.E2.80.93Leibler_divergence
			//
			// K: size of vectors, # rows/columns of matrices
			// up, uq: vectors of means for P and Q
			// CP, CQ: covariance matrices for P and Q
			// inv(x): inverse of x
			// det(x): determinant of x
			// tr(x): trace of x
			// x': transpose of x
			//
			// KL(P|Q) == .5 * ( tr(inv(CQ) * CP) + (uq - up)' * inv(CQ) * (uq - up) - K - ln(det(CP)/det(CQ)) )
			//
		
			final MultivariateNormalParameters P = this, Q = (MultivariateNormalParameters)that;
			final int K = P.getVectorLength();
			if (K != Q.getVectorLength())
			{
				throw new IllegalArgumentException(
					String.format("Incompatible vector sizes '%d' and '%d'", K, Q.getVectorLength()));
			}
			
			P.toCovarianceFormat();
			
			final double[] udiff = Q.getMean();
			for (int i = 0; i < K; ++i)
			{
				udiff[i] -= _vector[i];
			}

			final Matrix Ut = new Matrix(udiff, 1);
			final Matrix U = Ut.transpose();
			final Matrix CP = new Matrix(P._matrix, K, K);
			final Matrix CQ = new Matrix(Q._matrix, K, K);
			final Matrix CQinv = CP.inverse();
			
			// FIXME: do we need to worry about singular covariance matrices?
			
			return (CQinv.times(CQ).trace() + Ut.times(CQinv).times(U).get(0,0) - K - Math.log(CP.det()/CQ.det())) / 2;
		}
		
		throw new IllegalArgumentException(String.format("Expected '%s' but got '%s'", getClass(), that.getClass()));
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
		toCovarianceFormat();
		return _vector.clone();
	}

	public final double [][] getCovariance()
	{
		toCovarianceFormat();
		return cloneMatrix(_matrix);
	}
	
	public final double [] getInformationVector()
	{
		toInformationFormat();
		return _vector.clone();
	}

	public final double [][] getInformationMatrix()
	{
		toInformationFormat();
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
	
	/*---------
	 * Private
	 */
	
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
	
	private final void toCovarianceFormat()
	{
		if (_isInInformationForm)
		{
			toggleFormat();
		}
	}
	
	private final void toInformationFormat()
	{
		if (!_isInInformationForm)
		{
			toggleFormat();
		}
	}
	
	private final void toggleFormat()
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
