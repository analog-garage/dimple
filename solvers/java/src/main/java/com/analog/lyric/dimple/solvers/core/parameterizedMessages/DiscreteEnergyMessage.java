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
public class DiscreteEnergyMessage extends DiscreteMessage
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
		this(new double[size]);
		setNull();
	}
	
	public DiscreteEnergyMessage(DiscreteEnergyMessage other)
	{
		super(other);
	}
	
	@Override
	public DiscreteEnergyMessage clone()
	{
		return new DiscreteEnergyMessage(this);
	}

	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	@Override
	public boolean isNull()
	{
		for (double e : _message)
			if (e != 0.0)
				return false;
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets all energies to zero (i.e. not normalized).
	 */
	@Override
	public void setUniform()
	{
		Arrays.fill(_message, 0.0);
		_normalizationEnergy = weightToEnergy(_message.length);
	}

	/*-------------------------
	 * DiscreteMessage methods
	 */

	@Override
	public void addEnergiesFrom(DiscreteMessage other)
	{
		assertSameSize(other.size());
		
		final double[] message = _message;
		
		if (other.storesWeights())
		{
			for (int i = _message.length; --i >= 0; )
			{
				message[i] += other.getEnergy(i);
			}
		}
		else
		{
			final double[] otherMessage = other._message;
			for (int i = _message.length; --i >= 0; )
			{
				message[i] += otherMessage[i];
			}
		}
		
		forgetNormalizationEnergy();
	}
	
	@Override
	public double[] getEnergies()
	{
		return _message.clone();
	}
	
	@Override
	public double[] getWeights()
	{
		double[] weights = new double[_message.length];
		for (int i = weights.length; --i>=0;)
			weights[i] = energyToWeight(_message[i]);
		return weights;
	}
	
	@Override
	public final double getWeight(int i)
	{
		return Utilities.energyToWeight(_message[i]);
	}

	@Override
	public final void setWeight(int i, double weight)
	{
		_message[i] = Utilities.weightToEnergy(weight);
		forgetNormalizationEnergy();
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
		forgetNormalizationEnergy();
	}

	@Override
	public final double getEnergy(int i)
	{
		return _message[i];
	}

	@Override
	public final void setEnergy(int i, double energy)
	{
		_message[i] = energy;
		forgetNormalizationEnergy();
	}

	@Override
	public void setEnergies(double ... energies)
	{
		final int length = energies.length;
		assertSameSize(length);
		
		System.arraycopy(energies, 0, _message, 0, length);
	
		forgetNormalizationEnergy();
	}
	
	@Override
	public final double sumOfWeights()
	{
		double sum = 0.0;
		for (double e : _message)
			sum += Utilities.energyToWeight(e);
		return sum;
	}
	
	@Override
	public void normalize()
	{
		double normalizer = Utilities.weightToEnergy(assertNonZeroSumOfWeights());
		for (int i = _message.length; --i >=0;)
			_message[i] += normalizer;
		
		if (_normalizationEnergy != _normalizationEnergy) // NaN
		{
			_normalizationEnergy = 0.0;
		}
		else
		{
			_normalizationEnergy += normalizer;
		}
	}
	
	@Override
	public void setWeightsToZero()
	{
		Arrays.fill(_message, Double.POSITIVE_INFINITY);
		forgetNormalizationEnergy();
	}
	
	@Override
	public final boolean storesWeights()
	{
		return false;
	}
	
	/*-------------------------------
	 * DiscreteEnergyMessage methods
	 */

	/**
	 * Returns the minimum energy value in the distribution.
	 * @since 0.08
	 */
	public final double minEnergy()
	{
		double min = Double.POSITIVE_INFINITY;
		for (double d : _message)
		{
			min = Math.min(min, d);
		}
		return min;
	}
	
	/**
	 * Normalizes energy values by subtracting the min energy from all of them.
	 * <p>
	 * Does nothing if the minimum value is not finite.
	 * @since 0.08
	 */
	public void normalizeEnergy()
	{
		final double min = minEnergy();
		
		if (!Double.isInfinite(min))
		{
			for (int i = _message.length; --i >=0;)
			{
				_message[i] -= min;
			}
			
			if (_normalizationEnergy == _normalizationEnergy) // not NaN
			{
				_normalizationEnergy += min * _message.length;
			}
		}
	}
	
	@Override
	public int toDeterministicValueIndex()
	{
		int index = -1;
		for (int i = _message.length; --i>=0;)
		{
			if (_message[i] != Double.POSITIVE_INFINITY)
			{
				if (index >= 0)
				{
					index = -1;
					break;
				}
				index = i;
			}
		}
		return index;
	}
}
