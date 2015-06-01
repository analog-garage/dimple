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
		setUniform();
	}
	
	@Override
	public DiscreteWeightMessage clone()
	{
		return new DiscreteWeightMessage(_message);
	}

	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation normalizes all weight values to 1 / {@link #size()}.
	 */
	@Override
	public void setUniform()
	{
		Arrays.fill(_message, 1.0 / _message.length);
		setNormalizationEnergyIfSupported(0.0);
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
	}
	
	@Override
	public void setWeights(double... weights)
	{
		final int length = weights.length;
		assertSameSize(length);
		
		System.arraycopy(weights, 0, _message, 0, length);
		setNormalizationEnergyIfSupported(0.0);
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
		setNormalizationEnergyIfSupported(0.0);
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
		if (storesNormalizationEnergy())
		{
			incrementNormalizationEnergy(weightToEnergy(sum));
		}
	}
	
	@Override
	public void setWeightsToZero()
	{
		Arrays.fill(_message, 0.0);
		setNormalizationEnergyIfSupported(0.0);
	}
	
	@Override
	public boolean storesWeights()
	{
		return true;
	}
	
}
