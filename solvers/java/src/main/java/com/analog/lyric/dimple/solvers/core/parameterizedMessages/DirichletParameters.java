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

import org.apache.commons.math3.special.Gamma;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Dirichlet;
import com.analog.lyric.dimple.model.values.Value;
import com.google.common.math.DoubleMath;

public class DirichletParameters extends ParameterizedMessageBase
{
	private static final long serialVersionUID = 1L;

	private static final double SIMPLEX_THRESHOLD = 1e-12;

	// The parameters used are the natural additive parameters, (alpha-1)
	private double[] _alphaMinusOne;
	private transient byte _symmetric; // <0 not computed, 0 false, > 0 true
	
	/*--------------
	 * Construction
	 */
	
	public DirichletParameters()
	{
		_alphaMinusOne = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	}
	
	public DirichletParameters(int length)
	{
		_alphaMinusOne = new double[length];
		_symmetric = -1;
	}
	
	public DirichletParameters(int length, double alphaMinusOne)
	{
		_alphaMinusOne = new double[length];
		Arrays.fill(_alphaMinusOne, alphaMinusOne);
		_symmetric = 1;
	}
	
	public DirichletParameters(double[] alphaMinusOne)
	{
		_alphaMinusOne = alphaMinusOne.clone();
	}
	
	public DirichletParameters(DirichletParameters other)	// Copy constructor
	{
		super(other);
		_alphaMinusOne = other._alphaMinusOne.clone();
		_symmetric = other._symmetric;
	}
	
	@Override
	public DirichletParameters clone()
	{
		return new DirichletParameters(this);
	}
	
	public static @Nullable DirichletParameters fromDatum(IDatum datum)
	{
		if (datum instanceof DirichletParameters)
		{
			return (DirichletParameters)datum;
		}
		else if (datum instanceof Dirichlet)
		{
			return new DirichletParameters(((Dirichlet)datum).getAlphaMinusOneArray());
		}
		
		return null;
	}
	
	/*---------
	 * IEquals
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (other == this)
		{
			return true;
		}
		
		if (other instanceof DirichletParameters)
		{
			DirichletParameters that = (DirichletParameters)other;
			return super.objectEquals(other) && Arrays.equals(_alphaMinusOne, that._alphaMinusOne);
		}
		
		return false;
	}
	
	/*----------------------
	 * IUnaryFactorFunction
	 */
	
	@Override
	public double evalEnergy(Value value)
	{
		final double[] x = value.getDoubleArray();

		final int n = _alphaMinusOne.length;
		if (x.length != n)
		{
			throw new DimpleException("Argument does not contain %d-dimensional real joint value", n);
		}
		
		double sum = 0.0, xSum = 0.0;
		
		if (isSymmetric())
		{
			for (int i = n; --i>=0;)
			{
				final double xi = x[i];
				if (xi <= 0)
				{
					return Double.POSITIVE_INFINITY;
				}

				sum -= Math.log(xi);
				xSum += xi;
			}
			
			sum *= _alphaMinusOne[0];
		}
		else
		{
			for (int i = n; --i>=0;)
			{
				final double xi = x[i];
				if (xi <= 0)
				{
					return Double.POSITIVE_INFINITY;
				}

				sum -= (_alphaMinusOne[i]) * Math.log(xi);	// -log(x_i ^ (a_i-1))
				xSum += xi;
			}
		}
		
		if (!DoubleMath.fuzzyEquals(xSum, 1, SIMPLEX_THRESHOLD * n))	// Values must be on the probability simplex
		{
			return Double.POSITIVE_INFINITY;
		}

		return sum;
	}
	
	/*---------------
	 * Local methods
	 */
	
	public final int getSize() {return _alphaMinusOne.length;}
	
	public final void setSize(int size)
	{
		setAlphaMinusOne(new double[size]);
	}
	
	public final double getAlpha(int index)
	{
		return _alphaMinusOne[index] + 1.0;
	}
	
	public final double[] getAlphas()
	{
		final int n = _alphaMinusOne.length;
		double[] alphas = new double[n];
		for (int i = n; --i>=0;)
			alphas[i] = _alphaMinusOne[i] + 1;
		return alphas;
	}
	
	public final double[] getAlphaMinusOneArray()
	{
		return _alphaMinusOne.clone();
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
		forgetNormalizationEnergy();
	}
	
	public final void setAlpha(double [] alpha)
	{
		setAlphaMinusOne(alpha);
		for (int i = _alphaMinusOne.length; --i>=0;)
			_alphaMinusOne[i] -= 1.0;
	}
	
	public final void fillAlphaMinusOne(double alphaMinusOne)
	{
		Arrays.fill(_alphaMinusOne, alphaMinusOne);	// Replicate a single value into all entries
		forgetNormalizationEnergy();
		_symmetric = 1;
	}

	// Operations on the parameters
	public final void increment(int index)
	{
		_alphaMinusOne[index]++;
		forgetNormalizationEnergy();
	}
	public final void add(int index, double value)
	{
		_alphaMinusOne[index] += value;
		forgetNormalizationEnergy();
	}
	public final void add(double[] values)
	{
		int length = values.length;
		for (int i = 0; i < length; i++)
			_alphaMinusOne[i] += values[i];
		forgetNormalizationEnergy();
	}
	public final void add(int[] values)
	{
		int length = values.length;
		for (int i = 0; i < length; i++)
			_alphaMinusOne[i] += values[i];
		forgetNormalizationEnergy();
	}
	
	public final void add(DirichletParameters parameters)
	{
		addFrom(parameters);
	}
	
	/**
	 * True if all the parameters are the same.
	 * @since 0.08
	 */
	public final boolean isSymmetric()
	{
		int symmetric = _symmetric;
		
		if (symmetric < 0)
		{
			final double[] params = _alphaMinusOne;
			final int n = params.length;
			
			symmetric = 1;
			
			if (n > 1)
			{
				final double a = params[0];
				for (int i = 1; i < n; ++i)
				{
					if (params[i] != a)
					{
						symmetric = 0;
						break;
					}
				}
			}
		}
		
		return symmetric > 0;
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
	
	@Override
	public void addFrom(IParameterizedMessage other)
	{
		addFrom((DirichletParameters)other);
	}
	
	public void addFrom(DirichletParameters other)
	{
		double[] params = _alphaMinusOne;
		double[] otherParams = other._alphaMinusOne;
		
		if (params.length != otherParams.length)
		{
			throw new IllegalArgumentException("Cannot add from DirichletParameters with different size");
		}
		
		if (_symmetric > 0 && other._symmetric > 0)
		{
			double a = params[0] + otherParams[0];
			Arrays.fill(params, a);
		}
		else
		{
			for (int i = params.length; --i>=0;)
			{
				params[i] += otherParams[i];
			}
		}
		
		forgetNormalizationEnergy();
	}
	
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
			
			assertSameSize(alphasQ.length);

			// To summarize the doc comment in plain ascii:
			//
			// logGamma(sum(alphaPi) - logGamma(sum(alphaQi) + sum(logGamma(alphaQi)) - sum(logGamma(alphaPi))
			// + sum((alphaPi - alphaQi) * (digamma(alphaPi) - digamma(sum(alphaPj)))
			//
			
			double divergence = 0.0;
			if (size > 0)
			{
				// TODO optimize for symmetric case
				
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
	public boolean isNull()
	{
		if (_symmetric > 0)
			return _alphaMinusOne.length == 0 || _alphaMinusOne[0] == 0.0;
		
		for (double alpha : _alphaMinusOne)
			if (alpha != 0.0)
				return false;
		
		_symmetric = 1;
		return true;
	}
	
	@Override
	public void setFrom(IParameterizedMessage other)
	{
		DirichletParameters that = (DirichletParameters)other;
		double[] newAlphaMinusOne = that._alphaMinusOne;
		final int size = newAlphaMinusOne.length;
		assertSameSize(size);
		System.arraycopy(newAlphaMinusOne, 0, _alphaMinusOne, 0, size);
		copyNormalizationEnergy(that);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets all alphas to one.
	 */
	@Override
	public final void setUniform()
	{
		fillAlphaMinusOne(0);
	}
	
	public final void setNull(int size)
	{
		setSize(size);	// Create the array if it isn't already there, or change the size
		setUniform();
	}

	/*-------------------
	 * Protected methods
	 */
	
	protected void assertSameSize(int otherSize)
	{
		final int size = _alphaMinusOne.length;
		
		if (size != otherSize)
		{
			throw new IllegalArgumentException(
				String.format("Incompatible Dirichlet sizes '%d' and '%d'", size, otherSize));
		}
	}
	
	@Override
	protected final double computeNormalizationEnergy()
	{
		final double[] alphaMinusOne = _alphaMinusOne;
		final int n = alphaMinusOne.length;
		final boolean symmetric = isSymmetric() & n > 1;

		double sumAlpha = 0;
		double sumLogGamma = 0;

		final int end = symmetric ? 1 : n;
		
		for (int i = 0; i < end; ++i)
		{
			final double alpha = alphaMinusOne[i] + 1;
			sumAlpha += alpha;
			sumLogGamma += Gamma.logGamma(alpha);
		}
		
		if (symmetric)
		{

			sumAlpha *= n;
			sumLogGamma *= n;
		}

		return -(sumLogGamma - Gamma.logGamma(sumAlpha));
	}
	
	@Override
	protected void forgetNormalizationEnergy()
	{
		super.forgetNormalizationEnergy();
		_symmetric = -1;
	}
}
