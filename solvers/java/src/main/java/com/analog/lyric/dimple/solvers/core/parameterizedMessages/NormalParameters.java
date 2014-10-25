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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;


public class NormalParameters extends ParameterizedMessageBase
{
	private static final long serialVersionUID = 1L;

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
		this(other._mean, other._precision);
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
			
			if (Q._precision == P._precision && Q._mean == P._mean)
			{
				return 0.0;
			}
			
			final double QP_precision = Q._precision / P._precision;
			final double QP_mean_difference = Q._mean - P._mean;
			
			double divergence = -1.0;
			divergence -= Math.log(QP_precision);
			divergence += QP_precision;
			divergence += QP_mean_difference * QP_mean_difference * Q._precision;
			return Math.abs(divergence * .5); // protect against going negative due to precision error when close to 0.
		}
		
		throw new IllegalArgumentException(String.format("Expected '%s' but got '%s'", getClass(), that.getClass()));
		
	}

	@Override
	public final void setNull()
	{
		_mean = 0;
		_precision = 0;
	}

	/*---------------
	 * Local methods
	 */
	
	public final double getMean() {return _mean;}
	public final double getPrecision() {return _precision;}
	public final double getVariance() {return 1/_precision;}
	public final double getStandardDeviation() {return 1/Math.sqrt(_precision);}
	
	public final void setMean(double mean) {_mean = mean;}
	public final void setPrecision(double precision) {_precision = precision;}
	public final void setVariance(double variance) {_precision = 1/variance;}
	public final void setStandardDeviation(double standardDeviation) {_precision = 1/(standardDeviation*standardDeviation);}

	public final void set(NormalParameters other)	// Set from copy
	{
		_mean = other._mean;
		_precision = other._precision;
	}
}
