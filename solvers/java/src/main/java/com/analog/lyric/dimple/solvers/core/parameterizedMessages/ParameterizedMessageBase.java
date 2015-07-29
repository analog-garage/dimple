/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.solvers.core.parameterizedMessages;

import static java.lang.String.*;

import java.io.PrintStream;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.DataRepresentationType;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.util.misc.IPrintable;
import com.analog.lyric.util.misc.Matlab;

/**
 * Base implementation of {@link IParameterizedMessage}
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public abstract class ParameterizedMessageBase implements IParameterizedMessage
{
	private static final long serialVersionUID = 2L;

	/*-------
	 * State
	 */
	
	/**
	 * Cached value of {@link #getNormalizationEnergy()}, valid if not {@code NaN}.
	 */
	protected double _normalizationEnergy = Double.NaN;
	
	/*--------------
	 * Construction
	 */
	
	protected ParameterizedMessageBase()
	{
	}

	protected ParameterizedMessageBase(ParameterizedMessageBase other)
	{
		_normalizationEnergy = other._normalizationEnergy;
	}
	
	@Override
	public abstract ParameterizedMessageBase clone();
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return toString(0);
	}
	
	/*---------
	 * IEquals
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (other instanceof ParameterizedMessageBase)
		{
			ParameterizedMessageBase that = (ParameterizedMessageBase)other;
			// Unlike regular ==, this is true if both values are NaN:
			return Double.doubleToLongBits(_normalizationEnergy) == Double.doubleToLongBits(that._normalizationEnergy);
		}
		
		return false;
	}
	
	/*--------------------
	 * IPrintable methods
	 */
	
	@Override
	public final void println(PrintStream out, int verbosity)
	{
		IPrintable.Methods.println(this, out, verbosity);
	}
	
	@Override
	public String toString(int verbosity)
	{
		return IPrintable.Methods.toString(this, verbosity);
	}

	/*----------------
	 * IDatum methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns {@link DataRepresentationType#MESSAGE}.
	 */
	@Override
	public DataRepresentationType representationType()
	{
		return DataRepresentationType.MESSAGE;
	}

	/*------------------------------
	 * IUnaryFactorFunction methods
	 */
	
	@Override
	@Matlab
	public double evalEnergy(Object value)
	{
		final double energy = evalEnergy(Value.create(value));
		return energy == energy ? energy : Double.POSITIVE_INFINITY;
	}

	public double evalNormalizedEnergy(Value ... values)
	{
		final int n = values.length;
		double energy = -n * getNormalizationEnergy();
		for (Value value : values)
		{
			energy += evalEnergy(value);
		}
		return energy;
	}
	
	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	@Override
	public void addFrom(IParameterizedMessage other)
	{
		throw new UnsupportedOperationException(format("%s does not support addFrom method",
			getClass().getSimpleName()));
	}
	
	@Override
	public double addNormalizationEnergy(double additionalEnergy)
	{
		final double energy = getNormalizationEnergy() + additionalEnergy;
		_normalizationEnergy = energy;
		return energy;
	}
	
	@Override
	public final double getNormalizationEnergy()
	{
		double energy = _normalizationEnergy;
		
		if (energy != energy) // NaN
		{
			_normalizationEnergy = energy = computeNormalizationEnergy();
		}
		
		return energy;
	}
	
	@Override
	public boolean hasDeterministicValue()
	{
		return false;
	}
	
	@Override
	public void setDeterministic(Value value)
	{
		throw new UnsupportedOperationException(format("%s does not support deterministic parameterization",
			getClass().getSimpleName()));
	}

	@Override
	public void setNormalizationEnergy(double energy)
	{
		_normalizationEnergy = energy;
	}
	
	@Override
	public void setNull()
	{
		setUniform();
	}

	@Override
	public @Nullable Value toDeterministicValue(Domain domain)
	{
		return null;
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	/**
	 * Invoked by {@link #getNormalizationEnergy()} to compute the energy based on parameters.
	 * <p>
	 * Must not return {@link Double#NaN}.
	 * @since 0.08
	 */
	protected abstract double computeNormalizationEnergy();
	
	/**
	 * Copies normalization energy value from {@code other} message.
	 * @since 0.08
	 */
	protected void copyNormalizationEnergy(ParameterizedMessageBase other)
	{
		_normalizationEnergy = other._normalizationEnergy;
	}
	
	/**
	 * Forget current normalization energy setting, forcing it to be recomputed by {@link #getNormalizationEnergy()}.
	 * <p>
	 * Simply sets the stored energy to {@link Double#NaN}.
	 * @since 0.08
	 */
	protected void forgetNormalizationEnergy()
	{
		_normalizationEnergy = Double.NaN;
	}
}
