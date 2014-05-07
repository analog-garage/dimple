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

import java.util.Arrays;

public class DirichletParameters implements IParameterizedMessage
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
		setNull();
	}
	public DirichletParameters(double[] alphaMinusOne)
	{
		setAlphaMinusOne(alphaMinusOne);
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
<<<<<<< HEAD
	public final void setAlphaMinusOne(double[] alphaMinusOne)
	{ 
		int length = alphaMinusOne.length;
=======
	public final void setAlphaMinusOne(double[] alpha)
	{
		int length = alpha.length;
>>>>>>> Added KL divergence calculation to IParameterizedMessage implementations
		if (_alphaMinusOne == null || length != _alphaMinusOne.length)
			_alphaMinusOne = new double[length];
		System.arraycopy(alphaMinusOne, 0, _alphaMinusOne, 0, length);
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

	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Dirichlet parameter messages are computed using:
	 * <blockquote>
	 * ln(&Beta;(<b>&alpha;<sub>Q</sub></b>)) - ln(&Beta;(<b>&alpha;<sub>P</sub></b>))
	 * + <big><big>&Sigma;</big></big>(&alpha;<sub>Q<sub>i</sub></sub>-&alpha;<sub>P<sub>i</sub></sub>)
	 * (&psi;(&alpha;<sub>P<sub>i</sub></sub>) - &psi;(<b>&alpha;<sub>P</sub></b>))
	 * </blockquote>
	 * where &Beta;(x) is the multinomial beta function and &psi;(x) is the digamma function.
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

			double alphaSumP = size, alphaSumQ = size;
			for (int i = 0; i < size; ++i)
			{
				alphaSumP += alphasP[i];
				alphaSumQ += alphasQ[i];
			}
			
			final double digammaAlphaSumP = digamma(alphaSumP);
			
			double divergence = logGamma(alphaSumQ) - logGamma(alphaSumP);
			
			for (int i = 0; i < size; ++i)
			{
				final double alphaP = alphasP[i], alphaQ = alphasQ[i];
				divergence += logGamma(alphaQ);
				divergence -= logGamma(alphaP);
				divergence += (alphaP-alphaQ) * (digamma(alphaP) - digammaAlphaSumP);
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
