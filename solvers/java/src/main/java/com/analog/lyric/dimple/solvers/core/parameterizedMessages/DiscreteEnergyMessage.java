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

import java.util.Arrays;

import com.analog.lyric.math.Utilities;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public final class DiscreteEnergyMessage extends DiscreteMessage
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	public DiscreteEnergyMessage(double[] message)
	{
		super(message);
	}
	
	public DiscreteEnergyMessage(int size)
	{
		super(new double[size]);
		setNull();
	}
	
	@Override
	public DiscreteEnergyMessage clone()
	{
		return new DiscreteEnergyMessage(_message);
	}

	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets all energies to zero (i.e. not normalized).
	 */
	@Override
	public void setUniform()
	{
		Arrays.fill(_message, 0.0);
	}

	/*-------------------------
	 * DiscreteMessage methods
	 */

	@Override
	public double getWeight(int i)
	{
		return Utilities.energyToWeight(_message[i]);
	}

	@Override
	public void setWeight(int i, double weight)
	{
		_message[i] = Utilities.weightToEnergy(weight);
	}
	
	@Override
	public void setWeights(double... weights)
	{
		final int length = weights.length;
		assertSameSize(length);

		for (int i = 0; i < length; ++i)
		{
			_message[i] = weightToEnergy(weights[i]);
		}
	}

	@Override
	public double getEnergy(int i)
	{
		return _message[i];
	}

	@Override
	public void setEnergy(int i, double energy)
	{
		_message[i] = energy;
	}

	@Override
	public void setEnergies(double ... energies)
	{
		final int length = energies.length;
		assertSameSize(length);
		
		System.arraycopy(energies, 0, _message, 0, length);
	}
	
	@Override
	public void normalize()
	{
		double sum = 0.0;
		for (double e : _message)
			sum += Utilities.energyToWeight(e);
		double normalizer = Utilities.weightToEnergy(sum);
		for (int i = _message.length; --i >=0;)
			_message[i] += normalizer;
	}
	
	@Override
	public void setWeightsToZero()
	{
		Arrays.fill(_message, Double.POSITIVE_INFINITY);
	}
	
	@Override
	public boolean storesWeights()
	{
		return false;
	}
	
	/*-------------------------------
	 * DiscreteEnergyMessage methods
	 */

	/**
	 * Normalizes energy values by subtracting the min energy from all of them.
	 * <p>
	 * Does nothing if the minimum value is not finite.
	 * @since 0.08
	 */
	public void normalizeEnergy()
	{
		double min = Double.POSITIVE_INFINITY;
		for (double d : _message)
		{
			min = Math.min(min, d);
		}
		
		if (!Double.isInfinite(min))
		{
			for (int i = _message.length; --i >=0;)
			{
				_message[i] -= min;
			}
		}
	}
}
