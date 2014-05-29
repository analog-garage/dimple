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
import java.util.Arrays;

public class DirichletParameters extends ParameterizedMessageBase
{
	private static final long serialVersionUID = 1L;

	// The parameters used are the natural additive parameters, (alpha-1)
	private double[] _alphaMinusOne;
	
	public DirichletParameters()
	{
		_alphaMinusOne = new double[0];
	}
	public DirichletParameters(int length)
	{
		_alphaMinusOne = new double[length];
	}
	public DirichletParameters(double[] alphaMinusOne)
	{
		_alphaMinusOne = alphaMinusOne.clone();
	}
	public DirichletParameters(DirichletParameters other)	// Copy constructor
	{
		this(other._alphaMinusOne);
	}
	
	@Override
	public DirichletParameters clone()
	{
		return new DirichletParameters(this);
	}
	
	
	public final int getSize() {return _alphaMinusOne.length;}
	public final void setSize(int size) {_alphaMinusOne = new double[size];}
	
	public final double getAlpha(int index)
	{
		return _alphaMinusOne[index] + 1.0;
	}
	
	public final double getAlphaMinusOne(int index) {return _alphaMinusOne[index];}
	
	public final void setAlphaMinusOne(double[] alphaMinusOne)
	{
		int length = alphaMinusOne.length;
		if (length != _alphaMinusOne.length)
		{
			_alphaMinusOne = alphaMinusOne.clone();
	}
		else
		{
			System.arraycopy(alphaMinusOne, 0, _alphaMinusOne, 0, length);
		}
	}
	
	public final void fillAlphaMinusOne(double alphaMinusOne)
	{
		Arrays.fill(_alphaMinusOne, alphaMinusOne);	// Replicate a single value into all entries
	}

	// Operations on the parameters
	public final void increment(int index)
	{
		_alphaMinusOne[index]++;
	}
	public final void add(int index, double value)
	{
		_alphaMinusOne[index] += value;
	}
	public final void add(double[] values)
	{
		int length = values.length;
		for (int i = 0; i < length; i++)
			_alphaMinusOne[i] += values[i];
	}
	public final void add(int[] values)
	{
		int length = values.length;
		for (int i = 0; i < length; i++)
			_alphaMinusOne[i] += values[i];
	}
	public final void add(DirichletParameters parameters)
	{
		add(parameters._alphaMinusOne);
	}

	/*--------------------
	 * IPrintable methods
	 */
	
	@Override
	public void print(PrintStream out, int verbosity)
	{
		if (verbosity >= 0)
		{
			out.print("Dirichlet(");
			for (int i = 0, end = getSize(); i < end; ++i)
			{
				if (i > 0)
				{
					out.print(',');
					if (verbosity > 1)
					{
						out.print(' ');
					}
				}
				if (verbosity > 1)
				{
					out.format("a%d=", i);
				}
				out.format("%g", getAlpha(i));
			}
			out.print(')');
		}
	}
	
	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Dirichlet parameter messages are computed using:
	 * <blockquote>
	 * log &Beta;(<b>&alpha;<sub>Q</sub></b>) - log &Beta;(<b>&alpha;<sub>P</sub></b>)
	 * + <big><big>&Sigma;</big></big>(&alpha;<sub>Q<sub>i</sub></sub>-&alpha;<sub>P<sub>i</sub></sub>)
	 * (&psi;(&alpha;<sub>P<sub>i</sub></sub>) - &psi;(&Sigma;&alpha;<sub>P<sub>j</sub></sub>))
	 * </blockquote>
	 * where &Beta;(x) is the multinomial beta function:
	 * <blockquote>
	 * &Beta;(<b>&alpha;</b>) =
	 * <big>&Pi;</big>&Gamma;&alpha;<sub>i</sub> <big>/</big> <big>&Gamma;</big>&Sigma;&alpha;<sub>i</sub>
	 * </blockquote>
	 * so
	 * <blockquote>
	 * log &Beta;(<b>&alpha;</b>) =
	 * <big>&Sigma;</big>log &Gamma;&alpha;<sub>i</sub> - log <big>&Gamma;</big>&Sigma;&alpha;<sub>i</sub>
	 * </blockquote>
	 *  and &psi;(x) is the digamma function.
	 */
	@Override
	public double computeKLDivergence(IParameterizedMessage that)
	{
		if (that instanceof DirichletParameters)
		{
			final DirichletParameters P = this, Q = (DirichletParameters)that;
			final double[] alphasP = P._alphaMinusOne, alphasQ = Q._alphaMinusOne;
			final int size = alphasP.length;
			
			if (size != alphasQ.length)
			{
				throw new IllegalArgumentException(
					String.format("Incompatible Dirichlet sizes '%d' and '%d'", size, alphasQ.length));
			}

			// To summarize the doc comment in plain ascii:
			//
			// logGamma(sum(alphaPi) - logGamma(sum(alphaQi) + sum(logGamma(alphaQi)) - sum(logGamma(alphaPi))
			// + sum((alphaPi - alphaQi) * (digamma(alphaPi) - digamma(sum(alphaPj)))
			//
			
			double divergence = 0.0;
			if (size > 0)
			{
			double alphaSumP = size, alphaSumQ = size;
			for (int i = 0; i < size; ++i)
			{
				alphaSumP += alphasP[i];
				alphaSumQ += alphasQ[i];
			}

			final double digammaAlphaSumP = digamma(alphaSumP);
			
				if (alphaSumP != alphaSumQ)
				{
					divergence += logGamma(alphaSumP) - logGamma(alphaSumQ);
				}
			
			for (int i = 0; i < size; ++i)
			{
					final double alphaP = alphasP[i] + 1, alphaQ = alphasQ[i] + 1;
					if (alphaP != alphaQ)
					{
				divergence += logGamma(alphaQ);
				divergence -= logGamma(alphaP);
				divergence += (alphaP-alphaQ) * (digamma(alphaP) - digammaAlphaSumP);
			}
				}
			}
			
			return divergence;
		}
		
		throw new IllegalArgumentException(String.format("Expected '%s' but got '%s'", getClass(), that.getClass()));
	}
	
	@Override
	public final void setNull()
	{
		Arrays.fill(_alphaMinusOne, 0);
	}
	public final void setNull(int size)
	{
		setSize(size);	// Create the array if it isn't already there, or change the size
		Arrays.fill(_alphaMinusOne, 0);
	}

}
