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

package com.analog.lyric.dimple.solvers.lp;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a linear equality describing the constraint that the marginal probability
 * of a particular value of a variable is equal to the sum of of the joint probabilities
 * that include that variable value. The equation is of the form:
 * <p>
 * -pv + pjv1 + pjv2 + ... + pjv2 = 0
 * </p>
 * Unlike {@link LPVariableConstraint} the variable identifiers are not expected to
 * be contiguous, but the first variable identifier always refers to the variable
 * value's marginal probability.
 */
public final class LPFactorMarginalConstraint extends IntegerEquation
{
	@NotThreadSafe
	public static class TermIterator implements IntegerEquation.TermIterator
	{
		private int _index;
		private @Nullable int[] _lpVars;
		
		TermIterator(@Nullable LPFactorMarginalConstraint constraint)
		{
			reset(constraint);
		}
		
		/**
		 * Reset the iterator from a new variable constraint.
		 */
		public TermIterator reset(@Nullable LPFactorMarginalConstraint constraint)
		{
			_index = -1;
			_lpVars = constraint != null ? constraint._lpVars : null;
			return this;
		}
		
		@Override
		public boolean advance()
		{
			final int[] lpVars = _lpVars;
			return lpVars != null && ++_index < lpVars.length;
		}

		@Override
		public int getVariable()
		{
			return Objects.requireNonNull(_lpVars)[_index];
		}

		@Override
		public int getCoefficient()
		{
			return _index == 0 ? -1 : 1;
		}
	}
	
	/*-------
	 * State
	 */
	
	private final LPTableFactor _sfactor;
	private final int[] _lpVars;
	
	/*---------------
	 * Construction
	 */
	
	public LPFactorMarginalConstraint(LPTableFactor sfactor, int...lpVars)
	{
		_sfactor = sfactor;
		_lpVars = lpVars;
	}
	
	/*-------------------------
	 * IntegerEquation methods
	 */
	
	/**
	 * Returns non-null if this is a {@link LPFactorMarginalConstraint}.
	 */
	@Override
	public LPFactorMarginalConstraint asFactorConstraint()
	{
		return this;
	}

	@Override
	public int getCoefficient(int variable)
	{
		int i = Arrays.binarySearch(_lpVars, variable);
		if (i >= 0)
		{
			return i == 0 ? -1 : 1;
		}
		return 0;
	}

	@Override
	public int getRHS()
	{
		return 0;
	}

	@Override
	public TermIterator getTerms()
	{
		return new TermIterator(this);
	}
	
	@Override
	public int[] getVariables()
	{
		return _lpVars.clone();
	}

	@Override
	public boolean hasCoefficient(int variable)
	{
		return Arrays.binarySearch(_lpVars, variable) >= 0;
	}
	
	@Override
	public void print(PrintStream out)
	{
		_sfactor.printConstraintEquation(out, _lpVars);
	}

	@Override
	public int size()
	{
		return _lpVars.length;
	}
	
	/**
	 * Returns the Dimple solver factor for which this constraint was generated.
	 */
	public LPTableFactor getSolverFactor()
	{
		return _sfactor;
	}

}
