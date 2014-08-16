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

import java.util.Iterator;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.solvers.lp.IntegerEquation.TermIterator;
import com.analog.lyric.util.misc.Matlab;
import org.eclipse.jdt.annotation.Nullable;

@Matlab
@NotThreadSafe
public class MatlabConstraintTermIterator implements TermIterator
{
	/*-------
	 * State
	 */
	
	private final LPVariableConstraint.TermIterator _varIterator;
	private final LPFactorMarginalConstraint.TermIterator _factorIterator;
	private final @Nullable Iterator<IntegerEquation> _constraintIterator;
	private final int _size;
	private @Nullable IntegerEquation.TermIterator _curIterator;
	private int _row;
	
	/*--------------
	 * Construction
	 */
	
	MatlabConstraintTermIterator(@Nullable List<IntegerEquation> constraints, int nTerms)
	{
		_varIterator = new LPVariableConstraint.TermIterator(null);
		_factorIterator = new LPFactorMarginalConstraint.TermIterator(null);
		_constraintIterator = constraints != null ? constraints.iterator() : null;
		_size = nTerms;
		_curIterator = null;
		_row = 0;
	}
	
	/*----------------------
	 * TermIterator methods
	 */
	
	@Override
	public boolean advance()
	{
		IntegerEquation.TermIterator curIterator = _curIterator;

		if (curIterator == null || !curIterator.advance())
		{
			final Iterator<IntegerEquation> constraintIterator = _constraintIterator;
			if (constraintIterator != null && constraintIterator.hasNext())
			{
				IntegerEquation constraint = constraintIterator.next();
				LPVariableConstraint varConstraint = constraint.asVariableConstraint();
				if (varConstraint != null)
				{
					curIterator = _curIterator = _varIterator.reset(varConstraint);
				}
				else
				{
					curIterator = _curIterator = _factorIterator.reset(constraint.asFactorConstraint());
				}
				curIterator.advance(); // assume this will return true
				++_row;
			}
			else
			{
				_curIterator = null;
				_row = -1;
			}
		}
		
		return _curIterator != null;
	}

	@Override
	public int getVariable()
	{
		final IntegerEquation.TermIterator curIterator = _curIterator;
		return curIterator != null ? curIterator.getVariable() + 1 : -1;
	}

	@Override
	public int getCoefficient()
	{
		final IntegerEquation.TermIterator curIterator = _curIterator;
		return curIterator != null ? curIterator.getCoefficient() : 0;
	}

	/*
	 * MatlabConstraintTermIterator methods
	 */
	
	/**
	 * Get row number of current constraint term, where rows are numbered starting from 1.
	 */
	public int getRow()
	{
		return _row;
	}

	/**
	 * @return the number of terms that should be returned by this iterator for use in allocating
	 * the sparse matrix in MATLAB.
	 */
	public int size()
	{
		return _size;
	}
}
