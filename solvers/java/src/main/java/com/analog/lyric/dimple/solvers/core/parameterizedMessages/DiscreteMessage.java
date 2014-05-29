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
	
	public abstract double getWeight(int i);
	public abstract void setWeight(int i, double weight);
	
	public abstract double getEnergy(int i);
	public abstract void setEnergy(int i, double energy);
	
	/**
	 * True if underlying representation uses weights, false if it uses energies.
	 * @since 0.06
	 */
	public abstract boolean storesWeights();
}
