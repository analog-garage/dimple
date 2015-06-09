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
public class DiscreteWeightMessage extends DiscreteMessage
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	public DiscreteWeightMessage(double[] message)
	{
		super(message);
	}
	
	public DiscreteWeightMessage(int size)
	{
		this(new double[size]);
		setNull();
	}
	
	public DiscreteWeightMessage(DiscreteWeightMessage other)
	{
		super(other);
	}
	
	@Override
	public DiscreteWeightMessage clone()
	{
		return new DiscreteWeightMessage(this);
	}

	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	@Override
	public boolean isNull()
	{
		for (double w : _message)
			if (w != 1.0)
				return false;
		return true;
	}

	@Override
	public void setNull()
	{
		Arrays.fill(_message, 1.0);
		_normalizationEnergy = weightToEnergy(_message.length);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets all weight values to 1 / N (normalized).
	 */
	@Override
	public void setUniform()
	{
		Arrays.fill(_message, 1.0 / _message.length);
		_normalizationEnergy = 0.0;
	}
	
	/*-------------------------
	 * DiscreteMessage methods
	 */

	@Override
	public void addWeightsFrom(DiscreteMessage other)
	{
		assertSameSize(other.size());
		
		final double[] message = _message;
		
		if (other.storesWeights())
		{
			final double[] otherMessage = other._message;
			for (int i = _message.length; --i >= 0; )
			{
				message[i] += otherMessage[i];
			}
		}
		else
		{
			for (int i = _message.length; --i >= 0; )
			{
				message[i] += other.getWeight(i);
			}
		}
		
		forgetNormalizationEnergy();
	}
	
	@Override
	public double getWeight(int i)
	{
		return _message[i];
	}

	@Override
	public void setWeight(int i, double weight)
	{
		_message[i] = weight;
		forgetNormalizationEnergy();
	}
	
	@Override
	public void setWeights(double... weights)
	{
		final int length = weights.length;
		assertSameSize(length);
		
		System.arraycopy(weights, 0, _message, 0, length);
		forgetNormalizationEnergy();
	}

	@Override
	public double getEnergy(int i)
	{
		return Utilities.weightToEnergy(_message[i]);
	}

	@Override
	public void setEnergy(int i, double energy)
	{
		_message[i] = Utilities.energyToWeight(energy);
		forgetNormalizationEnergy();
	}
	
	@Override
	public void setEnergies(double... energies)
	{
		final int length = energies.length;
		assertSameSize(length);

		for (int i = 0; i < length; ++i)
		{
			_message[i] = energyToWeight(energies[i]);
		}
		forgetNormalizationEnergy();
	}
	
	@Override
	public double sumOfWeights()
	{
		double sum = 0.0;
		for (double w : _message)
			sum += w;
		return sum;
	}
	
	@Override
	public void normalize()
	{
		final double sum = assertNonZeroSumOfWeights();
		
		for (int i = _message.length; --i >=0;)
			_message[i] /= sum;
		
		final double normalizer = weightToEnergy(sum);
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
		Arrays.fill(_message, 0.0);
		forgetNormalizationEnergy();
	}
	
	@Override
	public final boolean storesWeights()
	{
		return true;
	}
	
}
