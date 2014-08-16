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

import static java.util.Objects.*;

import java.io.PrintStream;

import Jama.Matrix;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.math.LyricEigenvalueDecomposition;
import org.eclipse.jdt.annotation.Nullable;

public class MultivariateNormalParameters extends ParameterizedMessageBase
{
	private static final long serialVersionUID = 1L;

	private double [] _vector = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	// Cache means to avoid having to recompute
	private @Nullable double [] _mean = null;
	private double [][] _matrix = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
	private boolean _isInInformationForm;
	private double eps = 0.0000001; //minimum value for small eigenvalues or 1/(max value)

	
	// Constructors
	public MultivariateNormalParameters()
	{
		this(ArrayUtil.EMPTY_DOUBLE_ARRAY, ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY);
	}
	
	public MultivariateNormalParameters(double[] mean, double[][] covariance)
	{
		setMeanAndCovariance(mean.clone(), cloneMatrix(covariance));
	}
	
	public MultivariateNormalParameters(double[] vector, double[][] matrix, boolean informationForm)
	{
		this();
		if (informationForm)
		{
			setInformation(vector.clone(), cloneMatrix(matrix));
		}
		else
		{
			setMeanAndCovariance(vector.clone(), cloneMatrix(matrix));
		}
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
		_vector = _mean = mean.clone();
		_matrix = cloneMatrix(covariance);
		_isInInformationForm = false;
	}
	
	public final void setInformation(double[] informationVector, double[][] informationMatrix)
	{
		_vector = informationVector.clone();
		_mean = null;
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
	
	/*--------------------
	 * IPrintable methods
	 */
	
	@Override
	public void print(PrintStream out, int verbosity)
	{
		if (verbosity < 0)
		{
			return;
		}
		
		double[] mean = _mean;

		if (mean == null)
		{
			assert(_isInInformationForm);
			double[] vector = _vector;
			double[][] matrix = _matrix;
			// switch to covariance form to compute the mean
			toCovarianceFormat();
			mean = requireNonNull(_mean);
			// switch back to original values.
			_vector = vector;
			_matrix = matrix;
			_isInInformationForm = true;
		}
		
		out.print("Normal(");

		if (verbosity > 1)
		{
			out.println();
			out.print("    ");
		}
		out.print("mean=[");
		for (int i = 0, end = mean.length; i < end; ++i)
		{
			if (i > 0)
			{
				out.print(',');
				if (verbosity > 0)
				{
					out.print(' ');
				}
			}
			if (verbosity > 0)
			{
				out.format("%d=", i);
			}
			out.format("%g", mean[i]);
		}
		out.print(']');
		if (verbosity > 1)
		{
			out.println();
		}
		else
		{
			out.print(", ");
		}
		
		out.print(_isInInformationForm ? "precision=[" : "covariance=[");
		
		final int n = _matrix.length;
		for (int row = 0; row < n; ++row)
		{
			if (row > 0)
			{
				out.println();
				out.print("        ");
			}
			else
			{
				out.print("; ");
			}
			for (int col = 0; col < n; ++col)
			{
				if (col > 0)
				{
					out.print(',');
				}
				out.format("%g", _matrix[row][col]);
			}
		}
		
		out.print(']');
		
		if (verbosity > 1)
		{
			out.println();
		}
		out.print(')');
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
				udiff[i] -= P._vector[i];
			}

			final Matrix Ut = new Matrix(udiff, 1);
			final Matrix U = Ut.transpose();
			final Matrix CP = new Matrix(P._matrix, K, K);
			final Matrix CQ = new Matrix(Q._matrix, K, K);
			final Matrix CQinv = CQ.inverse();
			
			// FIXME: do we need to worry about singular covariance matrices?
			
			double divergence = -K;
			divergence += CQinv.times(CP).trace();
			divergence += Ut.times(CQinv).times(U).get(0,0);
			divergence -= Math.log(CP.det()/CQ.det());
			return Math.abs(divergence/2); // use abs to guard against precision errors causing this to go negative.
		}
		
		throw new IllegalArgumentException(String.format("Expected '%s' but got '%s'", getClass(), that.getClass()));
	}
	
	@Override
	public final void setNull()
	{
		_vector = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_matrix = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
		_mean = null;
		_isInInformationForm = true;
	}
	
	public final double[] getMeans() {return getMean();}	// For backward compatibility
	public final double[] getMean()
	{
		if (_mean == null)
		{
			toCovarianceFormat();
		}
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
		return _vector.length == 0;
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
	
	/**
	 * Toggles between mean/covariance format and information format, which uses the matrix
	 * inverse of the covariance matrix (this is also known as the precision or concentration matrix)
	 * 
	 * @since 0.06
	 */
	private final void toggleFormat()
	{
		if (!isNull())
		{
			// TODO: consider using EJML library instead of Jama
			
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

				for (int i=0; i<N; i++) {
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
		}
		
		_isInInformationForm = !_isInInformationForm;
		if (!_isInInformationForm)
		{
			_mean = _vector;
		}
	}

}
