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

import static org.apache.commons.math3.special.Gamma.*;

import java.io.PrintStream;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.values.Value;

public class GammaParameters extends ParameterizedMessageBase
{
	private static final long serialVersionUID = 1L;

	private double _alphaMinusOne = 0;
	private double _beta = 0;
	
	/*--------------
	 * Construction
	 */
	
	public GammaParameters() {}
	
	public GammaParameters(double alphaMinusOne, double beta)
	{
		// FIXME - it seems that really this constructor should take alpha, not alpha - 1
		
		_alphaMinusOne = alphaMinusOne;
		_beta = beta;
	}
	
	public GammaParameters(GammaParameters other)		// Copy constructor
	{
		super(other);
		_alphaMinusOne = other._alphaMinusOne;
		_beta = other._beta;
	}
	
	@Override
	public GammaParameters clone()
	{
		return new GammaParameters(this);
	}

	/*---------
	 * IEquals
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof GammaParameters)
		{
			GammaParameters that = (GammaParameters)other;
			return _alphaMinusOne == that._alphaMinusOne && _beta == that._beta && super.objectEquals(other);
		}

		return false;
	}

	/*-----------------------
	 * IUnaryFactorFunctions
	 */
	
	@Override
	public double evalEnergy(Value value)
	{
		final double x = value.getDouble();
		if (x < 0)
		{
			return Double.POSITIVE_INFINITY;
		}
		
		if (_alphaMinusOne == 0.0)
		{
			return x * _beta;
		}
		else
		{
			return x * _beta - Math.log(x) * _alphaMinusOne;
		}
	}
	
	/*---------------
	 * Local methods
	 */

	// Natural parameters are alpha-1 and beta
	public final double getAlphaMinusOne() {return _alphaMinusOne;}
	public final double getBeta() {return _beta;}

	public final void setAlphaMinusOne(double alphaMinusOne)
	{
		_alphaMinusOne = alphaMinusOne;
		forgetNormalizationEnergy();
	}
	
	public final void setBeta(double beta)
	{
		_beta = beta;
		forgetNormalizationEnergy();
	}
	
	// Ordinary alpha parameter
	public final double getAlpha() {return _alphaMinusOne + 1;}
	public final void setAlpha(double alpha)
	{
		setAlphaMinusOne(alpha - 1.0);
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
				fmt = "Gamma(%g,%g)";
				break;
			default:
				fmt = "Gamma(alpha=%g, beta=%g)";
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
	 * Computes KL as follows, where &Gamma;(x) is the gamma function and
	 * &psi;(x) is the digamma function.
	 * <p>
	 * (&alpha;<sub>P</sub>-&alpha;<sub>Q</sub>)&psi;(&alpha;<sub>P</sub>)
	 * - ln(&Gamma;(&alpha;<sub>P</sub>)) + ln(&Gamma;(&alpha;<sub>Q</sub>))
	 * + &alpha;<sub>Q</sub>(ln(&beta;<sub>P</sub>/&beta;<sub>Q</sub>))
	 * + &alpha;<sub>P</sub>(&beta;<sub>Q</sub>-&beta;<sub>P</sub>)/&beta;<sub>P</sub>
	 * 
	 * @see <a href="http://en.wikipedia.org/wiki/Gamma_distribution#Kullback.E2.80.93Leibler_divergence"
	 * >Gamma distribution (Wikipedia)</a>
	 */
	@Override
	public double computeKLDivergence(IParameterizedMessage that)
	{
		if (that instanceof GammaParameters)
		{
			// KL(P|Q) == (ap-aq)*digamma(ap) - log(gamma(ap)) + log(gamma(aq)) + aq*(log(bp)-log(bq)) + ap*(bq-bp)/bp
			
			final GammaParameters P = this, Q = (GammaParameters)that;
			final double ap = P.getAlpha(), aq = Q.getAlpha();
			final double bp = P.getBeta(), bq = Q.getBeta();
			
			double divergence = 0.0;
			if (ap != aq)
			{
				divergence += (ap-aq)*digamma(ap);
				divergence -= logGamma(ap);
				divergence += logGamma(aq);
			}
			
			if (bp != bq)
			{
				divergence += aq*(Math.log(bp)-Math.log(bq)) + ap * ((bq-bp)/bp);
			}
			
			
			return divergence;
		}
		
		throw new IllegalArgumentException(String.format("Expected '%s' but got '%s'", getClass(), that.getClass()));
	}
	@Override
	public boolean isNull()
	{
		return _beta == 0.0 && _alphaMinusOne == 0.0;
	}
	
	
	@Override
	public void setFrom(IParameterizedMessage other)
	{
		GammaParameters that = (GammaParameters)other;
		_alphaMinusOne = that._alphaMinusOne;
		_beta = that._beta;
		copyNormalizationEnergy(that);
	}
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets alpha to one and beta to zero.
	 */
	@Override
	public final void setUniform()
	{
		_alphaMinusOne = 0;
		_beta = 0;
		_normalizationEnergy = 0.0;
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	@Override
	protected double computeNormalizationEnergy()
	{
		final double alpha = _alphaMinusOne + 1;
		final double logBeta = _beta != 0 ? Math.log(_beta) : 0.0;

		return -(org.apache.commons.math3.special.Gamma.logGamma(alpha) - alpha * logBeta);
	}
}
