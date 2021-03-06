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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

/**
 * @since 0.05
 */
public abstract class SRealJointVariableBase extends SVariableBase<RealJoint>
{
	protected double[] _guessValue = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	protected boolean _guessWasSet = false;

    
	protected SRealJointVariableBase(RealJoint var, ISolverFactorGraph parent)
	{
		super(var, parent);
	}

	@Override
	public void initialize()
	{
		super.initialize();
		setGuess(null);
	}
	
	/*-------------------------
	 * ISolverVariable methods
	 */
	
	@Override
	public RealJointDomain getDomain()
	{
		return getModelObject().getDomain();
	}
	
	@Override
	public boolean guessWasSet()
	{
		return _guessWasSet;
	}
	
	@Override
	public Object getGuess()
	{
		if (_guessWasSet)
			return _guessValue;
		
		Object value = getKnownValueObject();
		if (value != null)
		{
			// If there's a fixed value set, use that
			// Note: we could also look at value from default conditioning layer, but
			// since we intend to deprecate the guess mechanism, it is probably not worth it.
			return value;
		}

		return getValue();
	}
		
	@Override
	public void setGuess(@Nullable Object guess)
	{
		if (guess == null)
		{
			_guessWasSet = false;
			_guessValue = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		else
		{
			_guessValue = (double[])guess;

			// Make sure the number is within the domain of the variable
			if (!_model.getDomain().inDomain(_guessValue))
				throw new DimpleException("Guess is not within the domain of the variable");

			_guessWasSet = true;
		}
	}

}
