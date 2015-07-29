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

package com.analog.lyric.dimple.solvers.core.parameterizedMessages;

import java.io.PrintStream;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.exceptions.InvalidDistributionException;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.util.misc.Matlab;
import com.google.common.math.DoubleMath;

@Matlab(wrapper="NormalParameters")
public class NormalParameters extends ParameterizedMessageBase
{
	private static final long serialVersionUID = 1L;

	private static final double LOG_SQRT_2_PI = Math.log(2*Math.PI) * .5;
	
	private double _mean = 0;
	private double _precision = 0;
	
	/*--------------
	 * Construction
	 */
	
	public NormalParameters() {}
	public NormalParameters(double mean, double precision)
	{
		_mean = mean;
		_precision = precision;
	}
	
	public NormalParameters(NormalParameters other)		// Copy constructor
	{
		super(other);
		_mean = other._mean;
		_precision = other._precision;
	}

	/**
	 * Construct with specified parameter values.
	 * <p>
	 * The following parameter keys are supported:
	 * <ul>
	 * <li>mean, mu (default is zero)
	 * <li>precision (default is one)
	 * <li>variance (default is one)
	 * <li>sigma, std (default is one)
	 * </ul>
	 * @param parameters
	 * @since 0.07
	 */
	public NormalParameters(Map<String,Object> parameters)
	{
		_mean = ((Number)FactorFunction.getFirstOrDefault(parameters, 0.0, "mean", "mu")).doubleValue();
		Object value;
		if ((value = parameters.get("precision"))!= null)
		{
			_precision = ((Number)value).doubleValue();
		}
		else if ((value = parameters.get("variance")) != null)
		{
			_precision = 1.0 / ((Number)value).doubleValue();
		}
		else if ((value = FactorFunction.getFirst(parameters, "std", "sigma")) != null)
		{
			double sigma = ((Number)value).doubleValue();
			_precision = 1 / (sigma * sigma);
		}
		else
		{
			_precision = 1.0;
		}
	}
	
	@Override
	public NormalParameters clone()
	{
		return new NormalParameters(this);
	}

	/**
	 * Converts input to a {@link NormalParameters} or return null.
	 * <p>
	 * If {@code datum} is a {@link NormalParameters} or {@link Normal}, this returns
	 * the parameters. If it is a {@link Value} this returns a new instance with mean
	 * set from value and infinite precision. Otherwise returns null.
	 * <p>
	 * @since 0.08
	 */
	public static @Nullable NormalParameters fromDatum(IDatum datum)
	{
		if (datum instanceof NormalParameters)
		{
			return (NormalParameters)datum;
		}
		else if (datum instanceof Normal)
		{
			return ((Normal)datum).getParameters();
		}
		else if (datum instanceof Value)
		{
			return new NormalParameters(((Value)datum).getDouble(), Double.POSITIVE_INFINITY);
		}
		
		return null;
	}
	
	/*----------------
	 * IDatum methods
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof NormalParameters)
		{
			NormalParameters that = (NormalParameters)other;
			return _mean == that._mean && _precision == that._precision && super.objectEquals(other);
		}

		return false;
	}
	
	/*----------------------
	 * IUnaryFactorFunction
	 */
	
	@Override
	public double evalEnergy(Value value)
	{
		final double precision = _precision;
		if (precision == 0.0)
			return 0.0;
		
		final double x = value.getDouble() - _mean;
		return x * x * precision * .5;
	}
	
	/*--------------------
	 * IPrintable methods
	 */
	
	@Override
	public void print(PrintStream out, int verbosity)
	{
		if (verbosity >= 0)
		{
			switch (verbosity)
			{
			case 0:
				out.format("Normal(%g,%g)", getMean(), getPrecision());
				break;
			case 1:
				out.format("Normal(mean=%g, precision=%g)", getMean(), getPrecision());
				break;
			default:
				out.format("Normal(mean=%g, precision=%g, std=%g)", getMean(), getPrecision(), getStandardDeviation());
				break;
			}
		}
	}
	
	/*-----------------------
	 * IParameterizedMessage
	 */

	@Override
	public void addFrom(IParameterizedMessage other)
	{
		addFrom((NormalParameters)other);
	}
	
	/**
	 * Adds natural parameters from another {@link NormalParameters} object
	 * <p>
	 * @throws InvalidDistributionException if both objects have infinite {@linkplain #getPrecision() precision}
	 * with different {@linkplain #getMean() means}.
	 * @since 0.08
	 */
	public void addFrom(NormalParameters other)
	{
		// That natural parameters are: mean*precision and precision
		//
		// Special cases:
		//  - both precisions infinite: average means if close enough, otherwise NaN
		//  - one precision infinite: use corresponding mean
		//  - one precision is zero: use other mean
		
		final double otherPrecision = other._precision;
		
		if (otherPrecision == 0.0)
		{
			// Other message doesn't add any information.
			return;
		}
		
		final double precision = _precision;
		
		if (precision == 0.0)
		{
			// This message doesn't contribute any information
			_mean = other._mean;
			_precision = otherPrecision;
			return;
		}
		
		if (precision == Double.POSITIVE_INFINITY)
		{
			if (otherPrecision == Double.POSITIVE_INFINITY)
			{
				if (!DoubleMath.fuzzyEquals(_mean, other._mean, Math.abs(_mean) / 1e12))
				{
					throw new InvalidDistributionException(
						"Cannot combine NormalParameters with infinite precision and different means (%g and %g",
						_mean, other._mean);
				}
			}
			else
			{
				// Infinite precision overrides other message
			}
			return;
		}
		
		if (otherPrecision == Double.POSITIVE_INFINITY)
		{
			// Infinite precision in other message overrides this one
			_mean = other._mean;
			_precision = otherPrecision;
			return;
		}
		
		// Regular case
		_precision += otherPrecision;
		_mean = (_mean * precision + other._mean * otherPrecision) / _precision;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * For single variable normal distributions, the formula is given by:
	 * <blockquote>
	 * &frac12; { &tau;<sub>Q</sub>(&mu;<sub>Q</sub> - &mu;<sub>P</sub>)<sup>2</sup> +
	 * &tau;<sub>Q</sub>/&tau;<sub>P</sub> - 1 - ln(&tau;<sub>Q</sub>/&tau;<sub>P</sub>) }
	 * </blockquote>
	 */
	@Override
	public double computeKLDivergence(IParameterizedMessage that)
	{
		if (that instanceof NormalParameters)
		{
			final NormalParameters P = this, Q = (NormalParameters)that;
			
			return computeKLDiverence(P._mean, P._precision, Q._mean, Q._precision);
		}
		
		throw new IllegalArgumentException(String.format("Expected '%s' but got '%s'", getClass(), that.getClass()));
		
	}
	
	static double computeKLDiverence(double Pmean, double Pprecision, double Qmean, double Qprecision)
	{
		if (Qprecision == Pprecision && Qmean == Pmean)
		{
			return 0.0;
		}
		
		final double QP_precision = Qprecision / Pprecision;
		final double QP_mean_difference = Qmean - Pmean;
		
		double divergence = -1.0;
		divergence -= Math.log(QP_precision);
		divergence += QP_precision;
		divergence += QP_mean_difference * QP_mean_difference * Qprecision;
		return Math.abs(divergence * .5); // protect against going negative due to precision error when close to 0.
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * True if {@linkplain #getPrecision() precision} is infinite (or {@linkplain #getVariance() variance} is zero).
	 */
	@Override
	public boolean hasDeterministicValue()
	{
		return _precision == Double.POSITIVE_INFINITY;
	}
	
	@Override
	public boolean isNull()
	{
		return _precision == 0.0;
	}
	
	@Override
	public void setDeterministic(Value value)
	{
		setDeterministic(value.getDouble());
	}
	
	@Override
	public void setFrom(IParameterizedMessage other)
	{
		set((NormalParameters)other);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets mean to zero and variance to infinity (i.e. precision to zero).
	 */
	@Override
	public final void setUniform()
	{
		_mean = 0;
		_precision = 0;
		forgetNormalizationEnergy();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Specificially, this will return Value containing the {@linkplain #getMean() mean} if
	 * {@linkplain #getVariance() variance} is zero and otherwise will return null.
	 */
	@Override
	public @Nullable Value toDeterministicValue(Domain domain)
	{
		final double value = toDeterministicValue();
		return value != value ? null : Value.create(domain, value);
	}
	
	/*---------------
	 * Local methods
	 */
	
	@Matlab
	public final double getMean() {return _mean;}
	
	@Matlab
	public final double getPrecision() {return _precision;}
	
	@Matlab
	public final double getVariance() {return 1/_precision;}
	
	@Matlab
	public final double getStandardDeviation() {return 1/Math.sqrt(_precision);}
	
	/**
	 * Sets parameters to represent given deterministic value.
	 * <p>
	 * This will set the {@linkplain #getMean() mean} to the specified {@code value} and the
	 * {@linkplain #getVariance() variance} to zero.
	 * <p>
	 * @param value
	 * @since 0.08
	 */
	public void setDeterministic(double value)
	{
		_mean = value;
		_precision = Double.POSITIVE_INFINITY;
	}
	
	public final void setMean(double mean) {_mean = mean;}
	public final void setPrecision(double precision)
	{
		_precision = precision;
		forgetNormalizationEnergy();
	}

	public final void setVariance(double variance)
	{
		setPrecision(1/variance);
	}
	
	public final void setStandardDeviation(double standardDeviation)
	{
		if (standardDeviation < 0)
			throw new IllegalArgumentException("Expect standard deviation to be >= 0");
		setPrecision(1/(standardDeviation*standardDeviation));
	}

	public final void set(NormalParameters other)	// Set from copy
	{
		_mean = other._mean;
		_precision = other._precision;
		forgetNormalizationEnergy();
	}

	/**
	 * Returns unique non-zero probability value, else NaN.
	 * <p>
	 * If there is a unique value that has non-zero probability given the parameters returns that.
	 * Specifically, if the {@linkplain #getVariance() variance} is zero, this will return the
	 * {@linkplain #getMean() mean} and otherwise will return {@linkplain Double#NaN NaN}.
	 * <p>
	 * @since 0.08
	 * @see #toDeterministicValue(Domain)
	 */
	public double toDeterministicValue()
	{
		return _precision == Double.POSITIVE_INFINITY ? _mean : Double.NaN;
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	@Override
	protected double computeNormalizationEnergy()
	{
		return  _precision == 0.0 ? 0.0 : Math.log(_precision) * .5 - LOG_SQRT_2_PI;
	}
}
