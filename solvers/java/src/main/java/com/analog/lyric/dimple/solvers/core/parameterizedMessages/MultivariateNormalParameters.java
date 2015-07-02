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

import static java.lang.String.*;
import static java.util.Objects.*;

import java.io.PrintStream;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.math.LyricEigenvalueDecomposition;
import com.analog.lyric.util.misc.Matlab;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

@Matlab(wrapper="MultivariateNormalParameters")
public class MultivariateNormalParameters extends ParameterizedMessageBase
{
	/*-------
	 * State
	 */
	
	// TODO : store eigendecomposition
	// TODO : use Apache math Matrix implementation
	
	private static final long serialVersionUID = 1L;
 
	// FIXME : can we make this smaller? I set this experimentally on the amount of error
	// produced when running MATLAB test alogRolledupGraphs/testMultivariateDataSource
	/**
	 * Determines min eigenvalue of covariance/information matrix.
	 */
	public static final double MIN_EIGENVALUE = 1e-9;
	
	protected static final double LOG_2PI = Math.log(2*Math.PI);

	private int _size = 0;
	private double [] _infoVector = ArrayUtil.EMPTY_DOUBLE_ARRAY;

	private double [] _mean = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	
	private double [][] _matrix = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;

	/**
	 * If known to be diagonal, this is the precision values along the diagonal.
	 * <p>
	 * Only valid if {@link #_isDiagonal} && {@link #_isDiagonalComputed}.
	 */
	private double[] _precision = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	private double[] _variance = ArrayUtil.EMPTY_DOUBLE_ARRAY;

	private boolean _isInInformationForm;
	private boolean _isDiagonal = false;
	private boolean _isDiagonalComputed = false;
	
	/*--------------
	 * Constructors
	 */

	public MultivariateNormalParameters(double[] mean, double[][] covariance)
	{
		setMeanAndCovariance(mean.clone(), cloneMatrix(covariance));
	}
	
	public MultivariateNormalParameters(double[] vector, double[][] matrix, boolean informationForm)
	{
		if (informationForm)
		{
			setInformation(vector.clone(), cloneMatrix(matrix));
		}
		else
		{
			setMeanAndCovariance(vector.clone(), cloneMatrix(matrix));
		}
	}
	
	public MultivariateNormalParameters(double[] mean, double[] variance)
	{
		setMeanAndVariance(mean, variance);
	}
	
	public MultivariateNormalParameters(List<NormalParameters> normals)
	{
		setDiagonal(normals);
	}

	/**
	 * Multivariate normal with specified number of dimensions, zero mean, and infinite covariance.
	 * @param dimensions a positive number
	 * @since 0.08
	 */
	public MultivariateNormalParameters(int dimensions)
	{
		this(new double[dimensions], arrayOf(dimensions, Double.POSITIVE_INFINITY));
	}
	
	/**
	 * Multivariate normal with dimensions matching variable domain, zero mean, and very large covariance.
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
		_size = mean.length;
		_infoVector = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_mean = mean.clone();
		_matrix = cloneMatrix(covariance);
		_precision = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_variance = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_isInInformationForm = false;
		_isDiagonalComputed = false;
		forgetNormalizationEnergy();
	}
	
	public final void setMeanAndVariance(double[] mean, double[] variance)
	{
		set(mean.clone(), variance.clone(), false);
	}
	
	private final void set(double[] meanOrInfo, double[] varianceOrPrecision, boolean informationForm)
	{
		final int n = _size = meanOrInfo.length;
		
		final double[] infoOrMean = new double[n];
		final double[] precisionOrVariance = new double[n];
		
		if (ArrayUtil.onlyContains(varianceOrPrecision, 0.0))
		{
			// All zero => inverse is infinite
			Arrays.fill(precisionOrVariance, Double.POSITIVE_INFINITY);
			Arrays.fill(infoOrMean, Double.POSITIVE_INFINITY);
		}
		else if (ArrayUtil.onlyContains(varianceOrPrecision, Double.POSITIVE_INFINITY))
		{
			// Infinite => inverse is zero
		}
		else
		{
			// Except for the all zero/infinite case, condition eigenvalues to not be too small/large FIXME hacky
			for (int i = 0; i < _size; ++i)
			{
				double x = varianceOrPrecision[i], inv = 1/x;
				if (x < MIN_EIGENVALUE)
				{
					varianceOrPrecision[i] = MIN_EIGENVALUE;
					inv = 1/MIN_EIGENVALUE;
				}
				else if (inv < MIN_EIGENVALUE)
				{
					varianceOrPrecision[i] = 1/MIN_EIGENVALUE;
					inv = MIN_EIGENVALUE;
				}

				precisionOrVariance[i] = inv;
				infoOrMean[i] = meanOrInfo[i] * inv;
			}
		}
		
		if (informationForm)
		{
			_infoVector = meanOrInfo;
			_mean = infoOrMean;
			_precision = varianceOrPrecision;
			_variance = precisionOrVariance;
		}
		else
		{
			_mean = meanOrInfo;
			_infoVector = infoOrMean;
			_variance = varianceOrPrecision;
			_precision = precisionOrVariance;
		}
			
		_matrix = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
		_isInInformationForm = informationForm;
		_isDiagonal = true;
		_isDiagonalComputed = true;
		forgetNormalizationEnergy();
	}
	
	public final void setDiagonal(List<NormalParameters> normals)
	{
		final int n = normals.size();
		double[] means = new double[n];
		double[] variances = new double[n];
		
		for (int i = 0; i < n; ++i)
		{
			final NormalParameters normal = normals.get(i);
			means[i] = normal.getMean();
			variances[i] = normal.getVariance();
		}
		
		setMeanAndVariance(means, variances);
	}
	
	public final void setInformation(double[] informationVector, double[][] informationMatrix)
	{
//		validateMatrix(informationMatrix);
		_size = informationVector.length;
		_infoVector = informationVector.clone();
		_mean = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_matrix = cloneMatrix(informationMatrix);
		_isInInformationForm = true;
		_precision = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_variance = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_isDiagonalComputed = false;
		forgetNormalizationEnergy();
	}

	// Set from another parameter set without first extracting the components or determining which form
	public final void set(MultivariateNormalParameters other)
	{
		_size = other._size;
		_mean = ArrayUtil.cloneNonNullArray(other._mean);
		_infoVector = ArrayUtil.cloneNonNullArray(other._infoVector);
		_precision = ArrayUtil.cloneNonNullArray(other._precision);
		_variance = ArrayUtil.cloneNonNullArray(other._variance);
		_matrix = cloneMatrix(other._matrix);
		_isInInformationForm = other._isInInformationForm;
		_isDiagonal = other._isDiagonal;
		_isDiagonalComputed = other._isDiagonalComputed;
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

			if (!(super.objectEquals(other) && _size == that._size && isDiagonal() == that.isDiagonal()))
				return false;
			
			if (isDiagonal())
			{
				return Arrays.equals(_mean, that._mean) && Arrays.equals(_variance, that._variance);
			}
			else if (isInInformationForm() == that.isInInformationForm())
			{
				final double[][] thisMatrix = _matrix;
				final double[][] thatMatrix = that._matrix;
				
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
		
		return false;
	}
	
	/*----------------------
	 * IUnaryFactorFunction
	 */
	
	@Override
	public double evalEnergy(Value value)
	{
		final int n = _size;
		final double[] mean = getMean();
		double[] x = value.getDoubleArray();

		if (isDiagonal())
		{
			double energy = 0.0;
			final double[] precisions = _precision;
			
			for (int i = n; --i>=0;)
			{
				final double precision = precisions[i];
				if (precision != 0.0)
				{
					final double diff = x[i] - mean[i];
					energy += diff * diff * precision;
				}
			}
			
			return energy * .5;
		}
		
		// TODO - support degenerate covariance case
		
		x = x.clone();
		
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
		
		String vectorLabel = "mean";
		double[] vector = _mean;
		if (vector.length == 0)
		{
			vector = _infoVector;
			vectorLabel="info";
		}

		out.print("Normal(");

		if (verbosity > 1)
		{
			out.println();
			out.print("    ");
		}
		out.format("%s=[", vectorLabel);
		for (int i = 0, end = vector.length; i < end; ++i)
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
			out.format(verbosity > 1 ? "%.12g" : "%g", vector[i]);
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
		
		if (isDiagonal())
		{
			double[] diagonal = _isInInformationForm ? _precision : _variance;
			
			for (int i = 0, end = diagonal.length; i < end; ++i)
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
				out.format("%g", diagonal[i]);
			}
		}
		else
		{
			final int n = _matrix.length;
			for (int row = 0; row < n; ++row)
			{
				if (row > 0)
				{
					out.print(";");
				}
				out.print("\n        ");
				for (int col = 0; col < n; ++col)
				{
					if (col > 0)
					{
						out.print(',');
					}
					out.format("%g", _matrix[row][col]);
				}
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
	
	@Override
	public void addFrom(IParameterizedMessage other)
	{
		addFrom((MultivariateNormalParameters)other);
	}
	
	public void addFrom(MultivariateNormalParameters other)
	{
		// That natural parameters are the information vector and matrix
		//
		// Special cases:
		//  - one message has all infinite precision - use corresponding means
		//  - both messages have all infinite precision - use existing mean if close enough
		//  - one message has all zero precision: use other means
		
		if (other.isNull())
		{
			return;
		}
		
		if (isNull())
		{
			set(other);
			return;
		}
		
		final int n = _size;
		if (n != other._size)
		{
			throw new IllegalArgumentException(format("Cannot add from %s with different size",
				other.getClass().getSimpleName()));
		}
		
		final boolean otherDiagonal = other.isDiagonal();

		if (otherDiagonal && (other._precision.length == 0 || other._precision[0] == 0.0))
		{
			// Other message adds no information
			return;
		}
		
		final boolean diagonal = isDiagonal();
		
		if (diagonal && (_precision.length == 0 || _precision[0] == 0.0))
		{
			// This message provide no information, copy from other
			set(other);
			return;
		}

		final double[] value = toDeterministicValueUnsafe();
		final double[] otherValue = other.toDeterministicValueUnsafe();
		if (value != null)
		{
			if (otherValue != null)
			{
				// FIXME - compare values
			}
			else
			{
				// Deterministic value in this message overrides other message
			}
			return;
		}
		
		if (otherValue != null)
		{
			// Other message is deterministic. Set from that
			setDeterministic(otherValue);
			return;
		}
		
		forgetNormalizationEnergy();

		if (diagonal && otherDiagonal)
		{
			// Just add diagonally
			for (int i = 0; i < n; ++i)
			{
				_infoVector[i] += other._infoVector[i];
				_precision[i] += other._precision[i];
				_variance[i] = 1.0 / _precision[i];
				_mean[i] = _infoVector[i] * _variance[i];
			}
			_matrix = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
			return;
		}
		
		if (!diagonal && otherDiagonal)
		{
			toInformationFormat();
			for (int i = 0; i < n; ++i)
			{
				_infoVector[i] += other._infoVector[i];
				_matrix[i][i] += other._precision[i];
			}
			_mean = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			return;
		}
		
		if (diagonal) // && !otherDiagonal
		{
			_matrix = other.getInformationMatrix();
			for (int i = 0; i < n; ++i)
			{
				_infoVector[i] += other._infoVector[i];
				_matrix[i][i] += _precision[i];
			}
			_mean = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_precision = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_variance = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_isDiagonal = false;
			_isDiagonalComputed = false;
			_isInInformationForm = true;
			return;
		}
		
		toInformationFormat();
		other.toInformationFormat();
		for (int i = 0; i <n; ++i)
		{
			_infoVector[i] += other._infoVector[i];
			double[] row = _matrix[i];
			double[] otherRow = other._matrix[i];
			for (int j = 0; j < n; ++j)
			{
				row[j] += otherRow[j];
			}
		}
		_mean = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	}
	
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

			if (P.isDiagonal() && Q.isDiagonal())
			{
				// If both are diagonal, we can simply add up the KL for the univariate cases along the diagonal.
				
				final double[] Pmeans = P._mean, Qmeans = Q._mean;
				final double[] Pprecisions = P._precision, Qprecisions = Q._precision;
				
				double kl = 0.0;
				
				for (int i = 0; i < K; ++i)
				{
					kl += NormalParameters.computeKLDiverence(Pmeans[i], Pprecisions[i], Qmeans[i], Qprecisions[i]);
				}
				
				return kl;
			}
			
			P.toCovarianceFormat();
			P.instantiateMatrix();
			Q.toCovarianceFormat();
			Q.instantiateMatrix();
			
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
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * True if {@link #isDiagonal()} and {@link #getDiagonalVariance()} contains only zeros.
	 * @since 0.08
	 */
	@Override
	public boolean hasDeterministicValue()
	{
		// Only need to check first element of variance, because setting diagonal format only allows zeros if all
		// are zero.
		return isDiagonal() && _variance.length > 0 && _variance[0] == 0.0;
	}
	
	@Override
	public void setDeterministic(Value value)
	{
		setDeterministic(value.getDoubleArray());
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * If {@link #isDiagonal()} and {@link #getDiagonalVariance()} contains only zeros, this returns
	 * Value containing the {@linkplain #getMean() mean}.
	 * @since 0.08
	 * @see #toDeterministicValue()
	 * @see #toDeterministicValueUnsafe()
	 */
	@Override
	public @Nullable Value toDeterministicValue(Domain domain)
	{
		double[] value = toDeterministicValue();
		return value != null ? Value.create((RealJointDomain)domain, value) : null;
	}
	
	@Override
	public void setFrom(IParameterizedMessage other)
	{
		MultivariateNormalParameters that = (MultivariateNormalParameters)other;
		set(that);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets all means to zero and all covariances to infinity
	 */
	@Override
	public final void setUniform()
	{
		setMeanAndVariance(new double[_size], arrayOf(_size, Double.POSITIVE_INFINITY));
		toInformationFormat();
	}
	
	/*---------------
	 * Local methods
	 */
	
	public final double[] getMeans() {return getMean();}	// For backward compatibility
	
	@Matlab
	public final double[] getMean()
	{
		if (_mean.length == 0)
		{
			toCovarianceFormat();
		}
		return ArrayUtil.cloneNonNullArray(_mean);
	}

	@Matlab
	public final double [][] getCovariance()
	{
		toCovarianceFormat();
		instantiateMatrix();
		return cloneMatrix(_matrix);
	}
	
	
	/**
	 * If information/covariance matrices are diagonal, return the parameters for the indexed diagonal entry.
	 * @since 0.08
	 * @return {@link Normal} containing mean and precision with given index.
	 * @see #getDiagonalPrecision()
	 * @see #getDiagonalVariance()
	 */
	@Matlab
	public @Nullable Normal getDiagonalNormal(int index)
	{
		return isDiagonal() ? new Normal(_mean[index], _precision[index]) : null;
	}
	
	public @Nullable List<Normal> getDiagonalNormals()
	{
		if (isDiagonal())
		{

			return new AbstractList<Normal>() {
				@Override
				public Normal get(int index)
				{
					return requireNonNull(getDiagonalNormal(index));
				}

				@Override
				public int size()
				{
					return _size;
				}
			};
		}
		
		return null;
	}
	
	/**
	 * If information matrix is diagonal, returns its elements, else a zero length array.
	 * @since 0.08
	 */
	public final double[] getDiagonalPrecision()
	{
		isDiagonal();
		return ArrayUtil.cloneNonNullArray(_precision);
	}
	
	/**
	 * If covariance matrix is diagonal, returns its elements, else a zero length array.
	 * @since 0.08
	 */
	public final double[] getDiagonalVariance()
	{
		isDiagonal();
		return ArrayUtil.cloneNonNullArray(_variance);
	}

	@Matlab
	public final double [] getInformationVector()
	{
		if (_infoVector.length == 0)
		{
			toInformationFormat();
		}
		return ArrayUtil.cloneNonNullArray(_infoVector);
	}

	@Matlab
	public final double [][] getInformationMatrix()
	{
		toInformationFormat();
		instantiateMatrix();
		return cloneMatrix(_matrix);
	}
	
	public final int getVectorLength()
	{
		return _size;
	}
	
	/**
	 * True if information/covariance matrix only contains diagonal entries.
	 * <p>
	 * That is, the only non-zero matrix entries have matching column/row indices.
	 * <p>
	 * @since 0.08
	 */
	public final boolean isDiagonal()
	{
		if (!_isDiagonalComputed)
		{
			final double[][] matrix = _matrix;
			final int n = _size;
			boolean isDiagonal = true;
			
			// NOTE: assumes matrix is symmetric
			
			outer:
			for (int i = 0; i < n; ++i)
			{
				double[] row = matrix[i];
				for (int j = 0; j < i; ++j)
				{
					if (row[j] != 0.0)
					{
						isDiagonal = false;
						break outer;
					}
				}
			}
			
			if (isDiagonal)
			{
				// Convert to compact diagonal form
				double[] array = new double[n];
				for (int i = 0; i < n; ++i)
				{
					array[i] = matrix[i][i];
				}
				set(_isInInformationForm ? _infoVector : _mean, array, _isInInformationForm);
				return true;
			}
			
			_isDiagonal = false;
			_isDiagonalComputed = true;
		}
		
		return _isDiagonal;
	}
	
	public final boolean isInInformationForm()
	{
		return _isInInformationForm;
	}
	
	@Override
	public final boolean isNull()
	{
		return _size == 0 || isDiagonal() && _precision[0] == 0.0;
	}
	
	/**
	 * Sets the {@linkplain #getMean() mean} (and {@linkplain #getInformationVector()} information vector} to its
	 * negation.
	 * 
	 * @since 0.08
	 */
	public void negateMean()
	{
		for (int i = _mean.length; --i>=0;)
			_mean[i] = -_mean[i];
		for (int i = _infoVector.length; --i>=0;)
			_infoVector[i] = -_infoVector[i];
		// Normalization does not depend on mean, so no need to reset it.
	}
	
	public void setDeterministic(double[] value)
	{
		setMeanAndVariance(value, new double[value.length]);
	}
	
	/**
	 * Returns unique deterministic value if any.
	 * <p>
	 * If {@link #isDiagonal()} and {@link #getDiagonalVariance()} contains only zeros, this returns
	 * the {@linkplain #getMean() mean}.
	 * @since 0.08
	 */
	public @Nullable double[] toDeterministicValue()
	{
		return hasDeterministicValue() ? getMean() : null;
	}
	
	/**
	 * Returns unique deterministic value if any without copying.
	 * <p>
	 * This is the same as {@link #toDeterministicValue()} but returns a pointer to the internal copy of
	 * the mean instead of copying it. The caller must make sure not to modify the array!
	 * @since 0.08
	 */
	public @Nullable double[] toDeterministicValueUnsafe()
	{
		return hasDeterministicValue() ? _mean : null;
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

	/**
	 * Force instantiation of {@link #_matrix} if not already done and in diagonal form.
	 */
	private final void instantiateMatrix()
	{
		final int n = _size;
		if (_matrix.length != n && _isDiagonal && _isDiagonalComputed)
		{
			_matrix = new double[n][n];
			final double[] diagonal = _isInInformationForm ? _precision : _variance;
			for (int i = 0; i < n; ++i)
				_matrix[i][i] = diagonal[i];
		}
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
		double energy = 0;
		
		if (isDiagonal())
		{
			// Simply add up the energies of the diagonals, skipping the zero entries
			for (double tau : _precision)
			{
				if (tau != 0.0)
				{
					energy += Math.log(tau) - LOG_2PI;
				}
			}
		}
		else
		{
			// TODO - support degenerate covariance
			
			double logdet = Math.log(new Jama.Matrix(_matrix).det());
			
			if (!_isInInformationForm)
				logdet = -logdet;

			energy = logdet - _size * LOG_2PI;
		}
		
		return energy / 2;
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
		// _variance will only be non-empty if diagonal
		for (double v : _variance)
		{
			if (v <= 0)
			{
				throw notPositiveDefinite();
			}
		}
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
					throw notPositiveDefinite();
				}
			}
		}
	}
	
	private RuntimeException notPositiveDefinite()
	{
		return new DimpleException("Matrix is not positive definite");
	}
	
	/**
	 * Toggles between mean/covariance format and information format, which uses the matrix
	 * inverse of the covariance matrix (this is also known as the precision or concentration matrix)
	 * 
	 * @since 0.06
	 */
	private final void toggleFormat()
	{
		outer:
		{
			if (isDiagonal())
			{
				// Only need to update the matrix, if present
				if (_matrix.length != 0)
				{
					final double[] diagonal = _isInInformationForm ? _variance : _precision;
					for (int i = 0, n = _size; i < n; ++i)
						_matrix[i][i] = diagonal[i];
				}
				
				break outer;
			}
			
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
					d = invertEigenvalue(d);
					D.set(i,i, d);

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

	private static double[] arrayOf(int size, double value)
	{
		final double[] array = new double[size];
		Arrays.fill(array, value);
		return array;
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

	private double invertEigenvalue(double eigenvalue)
	{
		return Math.min(1/MIN_EIGENVALUE, 1/eigenvalue);
	}
}
