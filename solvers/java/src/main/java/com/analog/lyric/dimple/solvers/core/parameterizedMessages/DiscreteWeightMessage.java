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
		super(new double[size]);
		setNull();
	}
	
	@Override
	public DiscreteWeightMessage clone()
	{
		return new DiscreteWeightMessage(_message);
	}

	/*-------------------------------
	 * IParameterizedMessage methods
	 */
	
	@Override
	public void setNull()
	{
		Arrays.fill(_message, 1.0 / _message.length);
	}

	/*-------------------------
	 * DiscreteMessage methods
	 */

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
	public boolean storesWeights()
	{
		return true;
	}
}
