package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.VariableBase;

public abstract class SRealVariableBase extends SVariableBase
{
    protected double _guessValue = 0;
    protected boolean _guessWasSet = false;

    
	public SRealVariableBase(VariableBase var)
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
		if (_guessWasSet)
			return Double.valueOf(_guessValue);
		else
			return (Double)getValue();
	}
	
	@Override
	public void setGuess(Object guess) 
	{
		// Convert the guess to a number
		if (guess instanceof Double)
			_guessValue = (Double)guess;
		else if (guess instanceof Integer)
			_guessValue = (Integer)guess;
		else
			throw new DimpleException("Guess is not a value type (must be Double or Integer)");

		// Make sure the number is within the domain of the variable
		RealDomain domain = (RealDomain)_var.getDomain();
		if ((_guessValue > domain.getUpperBound()) || (_guessValue < domain.getLowerBound()))
			throw new DimpleException("Guess is not within the domain of the variable");
		
		_guessWasSet = true;
	}
	
}
