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

import static org.apache.commons.math3.special.Beta.*;
import static org.apache.commons.math3.special.Gamma.*;

import java.io.PrintStream;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.values.Value;

public class BetaParameters extends ParameterizedMessageBase
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	// The parameters used are the natural additive parameters, (alpha-1) and (beta-1)
	private double _alphaMinusOne;
	private double _betaMinusOne;
	
	/*--------------
	 * Construction
	 */
	
	public BetaParameters()
	{
		
	}
	
	public BetaParameters(double alphaMinusOne, double betaMinusOne)
	{
		_alphaMinusOne = alphaMinusOne;
		_betaMinusOne = betaMinusOne;
	}
	public BetaParameters(BetaParameters other)		// Copy constructor
	{
		super(other);
		_alphaMinusOne = other._alphaMinusOne;
		_betaMinusOne = other._betaMinusOne;
	}
	
	@Override
	public BetaParameters clone()
	{
		return new BetaParameters(this);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return String.format("Beta(%g,%g)", getAlpha(), getBeta());
	}
	
	/*-----------------
	 * IEquals methods
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (other == this)
		{
			return true;
		}
		
		if (other instanceof BetaParameters)
		{
			BetaParameters that = (BetaParameters)other;
			return _alphaMinusOne == that._alphaMinusOne && _betaMinusOne == that._betaMinusOne &&
				super.objectEquals(other);
		}
		
		return false;
	}

	/*----------------------
	 * IUnaryFactorFunction
	 */
	
	@Override
	public double evalEnergy(Value value)
	{
		final double x = value.getDouble();
		
		if (x < 0 | x > 1)
		{
			return Double.POSITIVE_INFINITY;
		}
		
		double y;
		
		if (_alphaMinusOne == 0.0)
		{
			if (_betaMinusOne == 0.0)
			{
				return 0.0;
			}
			else
			{
				y = Math.log(1 - x) * _betaMinusOne;
			}
		}
		else if (_betaMinusOne == 0.0)
		{
			y = Math.log(x) * _alphaMinusOne;
		}
		else
		{
			y = _alphaMinusOne * Math.log(x) + _betaMinusOne * Math.log(1 - x);
		}
		
		return -y;
	}
	
	/*--------------------
	 * IPrintable methods
	 */
	
	@Override
	public void print(PrintStream out, int verbosity)
	{
		if (verbosity >= 0)
		{
			String fmt;
			switch (verbosity)
			{
			case 0:
				fmt = "Beta(%g,%g)";
				break;
			default:
				fmt = "Beta(alpha=%g, beta=%g)";
				break;
			}
			out.format(fmt, getAlpha(), getBeta());
		}
	}
	
	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Computes divergences as follows, where P is this, Q is that, and &Beta;(x) and &psi;(x) refer to the
	 * beta and digamma functions respectively.
	 * <p>
	 * ln(&Beta;(&alpha;<sub>Q</sub>, &beta;<sub>Q</sub>))
	 * - ln(&Beta;(&alpha;<sub>P</sub>, &beta;<sub>P</sub>))
	 * + (&alpha;<sub>P</sub>-&alpha;<sub>Q</sub>)&psi;(&alpha;<sub>P</sub>)
	 * + (&beta;<sub>P</sub>-&beta;<sub>Q</sub>)&psi;(&beta;<sub>P</sub>)
	 * + (&alpha;<sub>Q</sub>-&alpha;<sub>P</sub>+&beta;<sub>Q</sub>-&beta;<sub>P</sub>)
	 *    &psi;(&alpha;<sub>P</sub>+&beta;<sub>P</sub>)
	 * 
	 * @see <a href="http://en.wikipedia.org/wiki/Beta_distribution#Quantities_of_information_.28entropy.29">
	 * Beta distribution (Wikipedia)</a>
	 * @since 0.06
	 */
	@Override
	public double computeKLDivergence(IParameterizedMessage that)
	{
		if (that instanceof BetaParameters)
		{
			// http://en.wikipedia.org/wiki/Beta_distribution#Quantities_of_information_.28entropy.29
			//
			// KL(P|Q) == log(beta(aq,bq)/beta(ap,bp)) - (aq-ap)*digamma(ap) - (bq-bp)*digamma(bp) +
			//            (aq - ap + bq - bp)*digamma(ap + aq)
			
			final BetaParameters P = this, Q = (BetaParameters)that;
			
			final double ap = P.getAlpha(), aq = Q.getAlpha();
			final double bp = P.getBeta(), bq = Q.getBeta();
			final double adiff = ap - aq;
			final double bdiff = bp - bq;
			
			double divergence = 0.0;
			
			if (adiff != 0 | bdiff !=0)
			{
				divergence += logBeta(aq,bq);
				divergence -= logBeta(ap,bp);

				if (adiff != 0.0)
				{
					divergence += adiff * digamma(ap);
				}

				if (bdiff != 0.0)
				{
					divergence += bdiff * digamma(bp);
				}

				final double ndiff = -adiff - bdiff;
				if (ndiff != 0.0)
				{
					divergence += ndiff * digamma(ap+bp);
				}
			}

			return divergence;
		}
		
		throw new IllegalArgumentException(String.format("Expected '%s' but got '%s'", getClass(), that.getClass()));
	}
	
	@Override
	public boolean isNull()
	{
		return _alphaMinusOne == 0 && _betaMinusOne == 0;
	}
	
	@Override
	public void setFrom(IParameterizedMessage other)
	{
		BetaParameters that = (BetaParameters)other;
		_alphaMinusOne = that._alphaMinusOne;
		_betaMinusOne = that._betaMinusOne;
		copyNormalizationEnergy(that);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets alpha and beta parameters both to 1.
	 */
	@Override
	public final void setUniform()
	{
		_alphaMinusOne = 0;
		_betaMinusOne = 0;
		_normalizationEnergy = 0.0;
	}
	
	/*---------------
	 * Local methods
	 */
	
	// Natural parameters are alpha-1 and beta-1
	public final double getAlphaMinusOne() {return _alphaMinusOne;}
	public final double getBetaMinusOne() {return _betaMinusOne;}

	public final void setAlphaMinusOne(double alphaMinusOne)
	{
		_alphaMinusOne = alphaMinusOne;
		forgetNormalizationEnergy();
	}
	
	public final void setBetaMinusOne(double betaMinusOne)
	{
		_betaMinusOne = betaMinusOne;
		forgetNormalizationEnergy();
	}
	
	// Ordinary parameters, alpha and beta
	public final double getAlpha() {return _alphaMinusOne + 1;}
	public final double getBeta() {return _betaMinusOne + 1;}

	public final void setAlpha(double alpha)
	{
		setAlphaMinusOne(alpha - 1);
	}
	
	public final void setBeta(double beta)
	{
		setBetaMinusOne(beta - 1);
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	@Override
	protected double computeNormalizationEnergy()
	{
		return -org.apache.commons.math3.special.Beta.logBeta(_alphaMinusOne + 1, _betaMinusOne + 1);
	}
}
