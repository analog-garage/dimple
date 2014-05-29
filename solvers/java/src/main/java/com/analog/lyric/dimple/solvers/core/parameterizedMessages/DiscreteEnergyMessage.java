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
	
	@Override
	public void setNull()
	{
		Arrays.fill(_message, Utilities.weightToEnergy(1.0 / _message.length));
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
	public boolean storesWeights()
	{
		return false;
	}
}
