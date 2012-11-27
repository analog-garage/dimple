package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.model.DimpleException;
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
	public void initialize()
	{
		super.initialize();
		_guessWasSet = false;
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
		int domainLength = domain.size();
		int guessIndex = -1;
		for (int i = 0; i < domainLength; i++)
		{
			if (domain.getElements()[i].equals(guess))
			{
				guessIndex = i;
				break;
			}
		}
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
	
	public int getValueIndex()
	{
		throw new DimpleException("This solver doesn't provide a default value. Must set guesses for all variables.");
	}

}
