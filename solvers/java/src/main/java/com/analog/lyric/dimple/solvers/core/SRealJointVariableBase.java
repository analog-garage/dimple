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
			return getValue();
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
