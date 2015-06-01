/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

import static com.analog.lyric.math.Utilities.*;

import java.io.PrintStream;
import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.NormalizationException;


/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public abstract class DiscreteMessage extends ParameterizedMessageBase
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	protected final double[] _message;
	
	/*--------------
	 * Construction
	 */
	
	DiscreteMessage(double[] message)
	{
		_message = message.clone();
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
		
		if (other != null && other.getClass() == getClass())
		{
			DiscreteMessage that = (DiscreteMessage)other;
			return getNormalizationEnergy() == that.getNormalizationEnergy() &&
				Arrays.equals(_message, that._message);
		}
		
		return false;
	}
	
	/*--------------------
	 * IPrintable methods
	 */
	
	@Override
	public void print(PrintStream out, int verbosity)
	{
		if (verbosity >= 0)
		{
			out.print(storesWeights() ? "weights" : "energies");
			out.print('(');
			for (int i = 0, end = _message.length; i < end; ++i)
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
					out.format("%d=", i);
				}
				out.format("%g", _message[i]);
			}
			out.print(')');
			
			final double normalizationEnergy = getNormalizationEnergy();
			if (normalizationEnergy != 0.0)
			{
				if (storesWeights())
				{
					out.format(" [* %g]", energyToWeight(normalizationEnergy));
				}
				else
				{
					out.format(" [+ %g]", -normalizationEnergy);
				}
			}
		}
	}
	
	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	@Override
	public abstract DiscreteMessage clone();
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Discrete messages compute KL using:
	 * <blockquote>
	 * <big>&Sigma;</big> ln(P<sub>i</sub> / Q<sub>i</sub>) P<sub>i</sub>
	 * </blockquote>
	 */
	@Override
	public double computeKLDivergence(IParameterizedMessage that)
	{
		if (that instanceof DiscreteMessage)
		{
			// KL(P|Q) == sum(log(Pi/Qi) * Pi)
			//
			// To normalize you need to divide Pi by sum(Pi) and Qi by sum(Qi), denote these
			// by Ps and Qs:
			//
			//  ==> sum(log((Pi/Ps)/(Qi/Qs)) * Pi/Ps)
			//
			//  ==> 1/Ps * sum(log(Pi/Qi) * Pi + log(Qs/Ps) * Pi)
			//
			//  ==> sum(Pi*(log(Pi) - log(Qi)))/Ps + log(Qs/Ps)
			//
			// This formulation allows you to perform the computation using a single loop.
			
			final DiscreteMessage P = this;
			final DiscreteMessage Q = (DiscreteMessage)that;
			
			final int size = P.size();
			
			if (size != Q.size())
			{
				throw new IllegalArgumentException(
					String.format("Mismatched domain sizes '%d' and '%d'", P.size(), Q.size()));
			}
			
			double Ps = 0.0, Qs = 0.0, unnormalizedKL = 0.0;
			
			for (int i = 0; i < size; ++i)
			{
				final double pw = P.getWeight(i);
				final double qw = Q.getWeight(i);
				
				Ps += pw;
				Qs += qw;
				
				final double pe = P.getEnergy(i);
				final double qe = Q.getEnergy(i);
				
				unnormalizedKL += pw * (qe - pe);
			}
			
			return unnormalizedKL / Ps + Math.log(Qs/Ps);
		}
		
		throw new IllegalArgumentException(String.format("Expected '%s' but got '%s'", getClass(), that.getClass()));
	}
	
	@Override
	public void setFrom(IParameterizedMessage other)
	{
		setFrom((DiscreteMessage)other);
	}
	
	/*-------------------------
	 * DiscreteMessage methods
	 */
	
	/**
	 * The size of the message, i.e. the number of discrete elements of the domain.
	 * 
	 * @since 0.06
	 */
	public final int size()
	{
		return _message.length;
	}
	
	public void addWeightsFrom(DiscreteMessage other)
	{
		assertSameSize(other.size());
		
		for (int i = _message.length; --i>=0;)
		{
			setWeight(i, getWeight(i) + other.getWeight(i));
		}
	}
	
	/**
	 * Returns the stored normalization energy for the message, if supported.
	 * <p>
	 * This is an optional feature and is only supported by subclasses for which
	 * {@link #storesNormalizationEnergy()} is true.
	 * <p>
	 * The normalization energy is the energy (negative log) of the normalization factor that
	 * was used to normalize values. This value is set explicitly by {@link #setNormalizationEnergy(double)}
	 * and implicitly by the following methods:
	 * <ul>
	 * <li>{@link #setUniform} resets it to zero.
	 * <li>{@link #setEnergies} resets it to zero
	 * <li>{@link #setWeights} resets it to zero.
	 * <li>{@link #setWeightsToZero()} resets it to zero
	 * <li>{@link #normalize} adds the energy of the sum of the weights prior to the call.
	 * </ul>
	 * <p>
	 * @returns stored normalization energy value if supported, and 0.0 otherwise.
	 * <p>
	 * @since 0.08
	 */
	public double getNormalizationEnergy()
	{
		return 0.0;
	}

	/**
	 * Sets the stored normalization energy for the message, if supported.
	 * <p>
	 * This is an optional feature and is only supported by subclasses for which
	 * {@link #storesNormalizationEnergy()} is true.
	 * <p>
	 * @throws UnsupportedOperationException if not supported by this implementation.
	 * @since 0.08
	 * @see #getNormalizationEnergy()
	 */
	public void setNormalizationEnergy(double normalizationEnergy)
	{
		throw new UnsupportedOperationException(String.format("%s does not store normalization energy",
			getClass().getSimpleName()));
	}
	
	/**
	 * True if implementation stores normalization energy values.
	 * <p>
	 * The default implementation returns false.
	 * @since 0.08
	 * @see #getNormalizationEnergy()
	 * @see #setNormalizationEnergy(double)
	 */
	public boolean storesNormalizationEnergy()
	{
		return false;
	}
	
	public abstract double getWeight(int i);
	public abstract void setWeight(int i, double weight);
	
	public abstract double getEnergy(int i);
	public abstract void setEnergy(int i, double energy);
	
	public abstract void setWeights(double ... weights);
	public abstract void setEnergies(double ... energies);
	
	public abstract void setWeightsToZero();
	
	/**
	 * Compute sum of all weights in message.
	 * @since 0.08
	 */
	public abstract double sumOfWeights();
	
	/**
	 * Normalize so that weights sum to one.
	 * <p>
	 * This will compute the {@linkplain #sumOfWeights() sum of the weights} and use that to
	 * normalize the message.
	 * <p>
	 * If the message {@linkplain #storesNormalizationEnergy() stores normalization energy}, then this
	 * will update the normalization energy by adding the energy of the sum of weights.
	 * <P>
	 * @throws NormalizationException if {@link #sumOfWeights()} is zero.
	 */
	public abstract void normalize();
	
	/**
	 * Sets values from another message of the same size.
	 * 
	 * @param other is another message with the same {@link #size()} as this one but not necessarily
	 * the same representation.
	 * @since 0.08
	 * @throws IllegalArgumentException if {@code other} does not have the same size.
	 */
	public void setFrom(DiscreteMessage other)
	{
		final double[] otherRep = other.representation();
		
		if (other.storesWeights())
		{
			setWeights(otherRep);
		}
		else
		{
			setEnergies(otherRep);
		}
		
		setNormalizationEnergyIfSupported(other.getNormalizationEnergy());
	}
	
	/**
	 * Returns underlying message representation.
	 * @since 0.08
	 */
	public final double[] representation()
	{
		return _message;
	}
	
	/**
	 * True if underlying representation uses weights, false if it uses energies.
	 * @since 0.06
	 */
	public abstract boolean storesWeights();
	
	/*-------------------
	 * Protected methods
	 */
	
	protected void assertSameSize(int otherSize)
	{
		if (size() != otherSize)
		{
			throw new IllegalArgumentException(String.format("Cannot set from message with different size (%d vs %d)",
				size(), otherSize));
		}
	}
	
	protected double assertNonZeroSumOfWeights()
	{
		double sum = sumOfWeights();
		if (sum == 0.0)
		{
			throw new NormalizationException("Cannot normalize message because weights add up to zero");
		}
		return sum;
	}

	/**
	 * Set {@linkplain #getNormalizationEnergy() normalization energy} to specified value, if supported.
	 * <p>
	 * Unlike {@link #setNormalizationEnergy(double)} this will not throw an exception if not supported.
	 * @param normalizationEnergy TODO
	 * @since 0.08
	 */
	protected void setNormalizationEnergyIfSupported(double normalizationEnergy)
	{
	}

	/**
	 * Adds to {@linkplain #getNormalizationEnergy() normalization energy}, if supported.
	 * <p>
	 * Unlike {@link #setNormalizationEnergy(double)} this will simply do nothing if not supported.
	 * @since 0.08
	 */
	protected void incrementNormalizationEnergy(double additionalNormalizationEnergy)
	{
	}
}
