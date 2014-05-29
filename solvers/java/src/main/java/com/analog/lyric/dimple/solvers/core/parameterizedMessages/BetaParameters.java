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

public class BetaParameters extends ParameterizedMessageBase
{
	private static final long serialVersionUID = 1L;

	// The parameters used are the natural additive parameters, (alpha-1) and (beta-1)
	private double _alphaMinusOne = 0;
	private double _betaMinusOne = 0;
	
	/*--------------
	 * Construction
	 */
	
	public BetaParameters() {}
	public BetaParameters(double alphaMinusOne, double betaMinusOne)
	{
		_alphaMinusOne = alphaMinusOne;
		_betaMinusOne = betaMinusOne;
	}
	public BetaParameters(BetaParameters other)		// Copy constructor
	{
		this(other._alphaMinusOne, other._betaMinusOne);
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
	public final void setNull()
	{
		_alphaMinusOne = 0;
		_betaMinusOne = 0;
	}
	
	/*---------------
	 * Local methods
	 */
	
	// Natural parameters are alpha-1 and beta-1
	public final double getAlphaMinusOne() {return _alphaMinusOne;}
	public final double getBetaMinusOne() {return _betaMinusOne;}

	public final void setAlphaMinusOne(double alphaMinusOne) {_alphaMinusOne = alphaMinusOne;}
	public final void setBetaMinusOne(double betaMinusOne) {_betaMinusOne = betaMinusOne;}
	
	// Ordinary parameters, alpha and beta
	public final double getAlpha() {return _alphaMinusOne + 1;}
	public final double getBeta() {return _betaMinusOne + 1;}

	public final void setAlpha(double alpha) {_alphaMinusOne = alpha - 1;}
	public final void setBeta(double beta) {_betaMinusOne = beta - 1;}
	
}
