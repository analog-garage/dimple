/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.VariableBase;

public abstract class SDiscreteVariableBase extends SVariableBase
{
	protected int _guessIndex = 0;
	protected boolean _guessWasSet = false;

    
	public SDiscreteVariableBase(VariableBase var)
	{
		super(var);
	}

	@Override
	public void resetMessages()
	{
		super.resetMessages();
		_guessWasSet = false;
	}
	
	@Override
	public Object getValue()
	{
		int index = getValueIndex();
		return ((Discrete)_var).getDiscreteDomain().getElements()[index];
	}
	
	public int getValueIndex()
	{
		if (_var.hasFixedValue())	// If there's a fixed value set, use that instead of the belief
			return ((Discrete)_var).getFixedValueIndex();
					
		double[] belief = (double[])getBelief();
		int numValues = belief.length;
		double maxBelief = Double.NEGATIVE_INFINITY;
		int maxBeliefIndex = -1;
		for (int i = 0; i < numValues; i++)
		{
			double b = belief[i];
			if (b > maxBelief)
			{
				maxBelief = b;
				maxBeliefIndex = i;
			}
		}
		return maxBeliefIndex;
	}
	
	@Override
	public Object getGuess()
	{
		int index = getGuessIndex();
		return ((DiscreteDomain)_var.getDomain()).getElements()[index];
	}
	
	@Override
	public void setGuess(Object guess) 
	{
		DiscreteDomain domain = (DiscreteDomain)_var.getDomain();
		int guessIndex = domain.getIndex(guess);
		if (guessIndex == -1)
			throw new DimpleException("Guess is not a valid value");
		
		setGuessIndex(guessIndex);
	}
	
	public int getGuessIndex()
	{
		int index = 0;
		if (_guessWasSet)
			index = _guessIndex;
		else
			index = getValueIndex();
		
		return index;
	}
	

	public void setGuessIndex(int index) 
	{
		if (index < 0 || index >= ((DiscreteDomain)_var.getDomain()).size())
			throw new DimpleException("illegal index");
		
		_guessWasSet = true;
		_guessIndex = index;
	}
	

	
}
