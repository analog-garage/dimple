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

import java.io.PrintStream;
import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.math.Utilities;


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
	
	/**
	 * For converting normalized message to unnormalized form.
	 */
	protected double _denormalizer;
	
	protected final double[] _message;
	
	/*--------------
	 * Construction
	 */
	
	DiscreteMessage(double[] message, double denormalizer)
	{
		_message = message.clone();
		_denormalizer = denormalizer;
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
			return _denormalizer == that._denormalizer && Arrays.equals(_message, that._message);
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
	 * Returns the stored denormalization constant using weight representation.
	 * @since 0.08
	 */
	public abstract double getWeightDenormalizer();

	/**
	 * Sets the stored denormalization constant using weight representation.
	 * @since 0.08
	 */
	public abstract void setWeightDenormalizer(double denormalizer);

	/**
	 * Returns the stored denormalization constant using energy (negative log) representation.
	 * @since 0.08
	 */
	public abstract double getEnergyDenormalizer();

	/**
	 * Sets the stored denormalization constant using energy (negative log) representation.
	 * @since 0.08
	 */
	public abstract void setEnergyDenormalizer(double denormalizer);
	
	/**
	 * Resets the denormalization constant to indicate no normalization has been performed:
	 * {@link #getWeightDenormalizer()} will be 1.0 and {@link #getEnergyDenormalizer()} will be 0.0.
	 * 
	 * @since 0.08
	 */
	public abstract void resetDenormalizer();
	
	public abstract double getWeight(int i);
	public abstract void setWeight(int i, double weight);
	
	public abstract double getUnnormalizedWeight(int i);
	
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
	 * normalize the message and update the stored denormalization constant according to the
	 * underlying representation:
	 * <ul>
	 * <li><b>weight representation</b>: Divides all message values by the sum, and multiplies the stored
	 * {@linkplain #getWeightDenormalizer() denormalizer} by the sum.
	 * <li><b>energy representation</b>: Computes the negative log of the sum and adds it the message values
	 * and the stored {@linkplain #getEnergyDenormalizer() denormalizer} constant.
	 * </ul>
	 * </dl>
	 */
	public abstract void normalize();
	
	/**
	 * Convert back to a denormalized form.
	 * <p>
	 * Uses stored denormalization constant to convert message to an unnormalized form.
	 * <p>
	 * @since 0.08
	 */
	public abstract void denormalize();
	
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
		final int n = _message.length;
		
		assertSameSize(other.size());

		System.arraycopy(other.representation(), 0, _message, 0, n);

		if (storesWeights() != other.storesWeights())
		{
			if (storesWeights())
			{
				_denormalizer = other.getWeightDenormalizer();
				for (int i = 0; i < n; ++i)
					_message[i] = Utilities.energyToWeight(_message[i]);
			}
			else
			{
				_denormalizer = other.getEnergyDenormalizer();
				for (int i = 0; i < n; ++i)
					_message[i] = Utilities.weightToEnergy(_message[i]);
			}
		}
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
	
	protected void assertSameSize(int otherSize)
	{
		if (size() != otherSize)
		{
			throw new IllegalArgumentException(String.format("Cannot set from message with different size (%d vs %d)",
				size(), otherSize));
		}
	}
}
