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

import java.io.PrintStream;
import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.math.LyricEigenvalueDecomposition;

public class MultivariateNormalParameters extends ParameterizedMessageBase
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private static final double EPS = 0.0000001; //minimum value for small eigenvalues or 1/(max value)
	protected static final double LOG_SQRT_2PI = Math.log(2*Math.PI)*0.5;

	private double [] _infoVector = ArrayUtil.EMPTY_DOUBLE_ARRAY;

	private double [] _mean = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	
	private double [][] _matrix = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;

	private boolean _isInInformationForm;
	
	/*--------------
	 * Constructors
	 */

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

	/**
	 * Multivariate normal with specified number of dimensions, zero mean, and infinite covariance.
	 * @param dimensions a positive number
	 * @since 0.08
	 */
	public MultivariateNormalParameters(int dimensions)
	{
		this(new double[dimensions], new double[dimensions][dimensions]);
		for (double[] row : _matrix)
		{
			Arrays.fill(row, Double.POSITIVE_INFINITY);
		}
	}
	
	/**
	 * Multivariate normal with dimensions matching variable domain, zero mean, and infinite covariance.
	 * @param var
	 * @since 0.08
	 */
	public MultivariateNormalParameters(RealJoint var)
	{
		this(var.getDomain().getDimensions());
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
//		validateMatrix(covariance);
		_infoVector = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_mean = mean.clone();
		_matrix = cloneMatrix(covariance);
		_isInInformationForm = false;
	}
	
	public final void setInformation(double[] informationVector, double[][] informationMatrix)
	{
//		validateMatrix(informationMatrix);
		_infoVector = informationVector.clone();
		_mean = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_matrix = cloneMatrix(informationMatrix);
		_isInInformationForm = true;
		forgetNormalizationEnergy();
	}

	// Set from another parameter set without first extracting the components or determining which form
	public final void set(MultivariateNormalParameters other)
	{
		_mean = other._mean.length == 0 ? other._mean : other._mean.clone();
		_infoVector = other._infoVector.length == 0 ? other._infoVector : other._infoVector.clone();
		_matrix = cloneMatrix(other._matrix);
		_isInInformationForm = other._isInInformationForm;
		copyNormalizationEnergy(other);
	}
	
	/*-----------------
	 * IEquals methods
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}

		if (other instanceof MultivariateNormalParameters)
		{
			MultivariateNormalParameters that = (MultivariateNormalParameters)other;
			if (_isInInformationForm == that._isInInformationForm &&
				Arrays.equals(_infoVector,that._infoVector) && Arrays.equals(_mean, that._mean))
			{
				final double[][] thisMatrix = _matrix;
				final double[][] thatMatrix = that._matrix;
				
				if (thisMatrix.length == thatMatrix.length)
				{
					for (int i = thisMatrix.length; --i>=0;)
					{
						if (!Arrays.equals(thisMatrix[i], thatMatrix[i]))
						{
							return false;
						}
					}
					return true;
				}
			}
		}
		
		return false;
	}
	
	/*----------------------
	 * IUnaryFactorFunction
	 */
	
	@Override
	public double evalEnergy(Value value)
	{
		// TODO - this requires both covariance and information forms. Can we avoid this?
		final double[] x = value.getDoubleArray().clone();
		final double[] mean = getMean();
		
		final int n = mean.length;
		for (int i = n; --i>=0;)
			x[i] -= mean[i];
		
		final double[][] informationMatrix = getInformationMatrix();
		
		double colSum = 0;
		for (int row = 0; row < n; row++)
		{
			double rowSum = 0;
			final double[] informationMatrixRow = informationMatrix[row];
			for (int col = 0; col < n; col++)
				rowSum += informationMatrixRow[col] * x[col];	// Matrix * vector
			colSum += rowSum * x[row];	// Vector * vector
		}

		return colSum * .5;
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

		if (mean.length == 0 && !isNull())
		{
			assert(_isInInformationForm);
			double[][] matrix = _matrix;
			// switch to covariance form to compute the mean
			toCovarianceFormat();
			mean = _mean;
			// switch back to original values.
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
			assertSameSize(Q.getVectorLength());
			
			P.toCovarianceFormat();
			Q.toCovarianceFormat();
			
			final double[] udiff = Q.getMean();
			for (int i = 0; i < K; ++i)
			{
				udiff[i] -= P._mean[i];
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
	public void setFrom(IParameterizedMessage other)
	{
		MultivariateNormalParameters that = (MultivariateNormalParameters)other;
		set(that);
	}
	
	@Override
	public final void setNull()
	{
		_infoVector = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_mean = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_matrix = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
		_isInInformationForm = true;
		_normalizationEnergy = 0.0;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets all means to zero and all covariances to infinity.
	 */
	@Override
	public void setUniform()
	{
		if (_isInInformationForm)
		{
			// TODO: Implement for information form rather than toggling.
			_infoVector = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_isInInformationForm = false;
		}
		Arrays.fill(_mean, 0.0);
		for (double[] array : _matrix)
		{
			Arrays.fill(array, Double.POSITIVE_INFINITY);
		}
		forgetNormalizationEnergy();
	}
	
	public final double[] getMeans() {return getMean();}	// For backward compatibility
	public final double[] getMean()
	{
		if (_mean.length == 0)
		{
			toCovarianceFormat();
		}
		return _mean.clone();
	}

	public final double [][] getCovariance()
	{
		toCovarianceFormat();
		return cloneMatrix(_matrix);
	}
	
	public final double [] getInformationVector()
	{
		if (_infoVector.length == 0)
		{
			toInformationFormat();
		}
		return _infoVector.clone();
	}

	public final double [][] getInformationMatrix()
	{
		toInformationFormat();
		return cloneMatrix(_matrix);

	}
	
	public final int getVectorLength()
	{
		return _matrix.length;
	}
	
	public final boolean isInInformationForm()
	{
		return _isInInformationForm;
	}
	
	@Override
	public final boolean isNull()
	{
		return _matrix.length == 0;
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
	
	@Override
	protected double computeNormalizationEnergy()
	{
		final double[][] informationMatrix = getInformationMatrix();
		final int n = informationMatrix.length;
		return n == 0 ? 0.0 : -(n * LOG_SQRT_2PI - Math.log(new Jama.Matrix(informationMatrix).det()) * .5);
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
	
	public void validate()
	{
		validateMatrix(_matrix);
	}
	
	private void validateMatrix(double[][] m)
	{
		final int n = m.length;
		
		boolean allZero = true;
		
		for (double[] row : m )
		{
			for (double value : row)
			{
				if (value != value)
				{
					throw new DimpleException("Matrix contains a NaN value");
				}

				if (value != 0.0)
				{
					allZero = false;
				}
			}
		}
		
		if (allZero)
		{
			return;
		}

		boolean infiniteDiagonal = true;
		
		for (int i = 0; i < n; ++i)
		{
			final double[] row = m[i];
			if (row.length != n)
			{
				throw new DimpleException("Matrix is not square");
			}
			
			for (int j = 0; j < n; ++j)
			{
				final double vij = row[j];
				if (j == i)
				{
					infiniteDiagonal &= (vij == Double.POSITIVE_INFINITY);
				}
				else
				{
					final double vji = m[j][i];
					if (Math.abs(vji - vij) > 1e-10)
					{
						throw new DimpleException("Matrix is not symmetric at entry (%d,%d)", i, j);
					}
				}
			}
		}
		
		if (infiniteDiagonal)
		{
			return;
		}

		if (n > 0)
		{
			EigenvalueDecomposition eig = new EigenvalueDecomposition(new Matrix(m));
			for (double value : eig.getRealEigenvalues())
			{
				if (value <= 0)
				{
					throw new DimpleException("Matrix is not positive definite");
				}
			}
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
			// TODO: consider using EJML or MTJ library instead of Jama
			
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
					d = Math.min(1/EPS, 1/d);
					D.set(i,i,d);

					assert(d > 0); // Eigenvalues should always be positive for positive definite matrices
				}

				inv = V.times(D.times(V.transpose()));


				vec = new Matrix(new double [][] {_isInInformationForm ? _infoVector : _mean}).transpose();

				vec = inv.times(vec);
			}

			_matrix = inv.getArray();
			final double[] newVector = vec.transpose().getArray()[0];
			if (_isInInformationForm)
			{
				_mean = newVector;
			}
			else
			{
				_infoVector = newVector;
			}
		}
		
		_isInInformationForm = !_isInInformationForm;
	}

	protected void assertSameSize(int otherSize)
	{
		final int size = getVectorLength();
		
		if (size != otherSize)
		{
			throw new IllegalArgumentException(
				String.format("Incompatible vector sizes '%d' and '%d'", size, otherSize));
		}
	}
}
