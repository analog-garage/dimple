package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.VariableBase;

public abstract class SRealJointVariableBase extends SVariableBase
{
	protected double[] _guessValue;
	protected boolean _guessWasSet = false;

    
	public SRealJointVariableBase(VariableBase var)
	{
		super(var);
	}

	@Override
	public void initialize()
	{
		super.initialize();
		_guessWasSet = false;
	}
	
	/*---------------
	 * INode objects
	 */
	
	@Override
	public RealJoint getModelObject()
	{
		return (RealJoint)_var;
	}

	
	/*-------------------------
	 * ISolverVariable methods
	 */
	
	@Override
	public Object getGuess()
	{
		if (_guessWasSet)
			return _guessValue;
		else if (_var.hasFixedValue())		// If there's a fixed value set, use that
			return ((RealJoint)_var).getFixedValue();
		else
			return (double[])getValue();
	}
	
	@Override
	public void setGuess(Object guess)
	{
		_guessValue = (double[])guess;

		// Make sure the number is within the domain of the variable
		if (!_var.getDomain().inDomain(_guessValue))
			throw new DimpleException("Guess is not within the domain of the variable");
		
		_guessWasSet = true;
	}

}
