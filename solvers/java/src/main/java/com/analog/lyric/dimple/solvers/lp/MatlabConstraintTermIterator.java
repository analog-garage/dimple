package com.analog.lyric.dimple.solvers.lp;

import java.util.Iterator;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.solvers.lp.IntegerEquation.TermIterator;
import com.analog.lyric.util.misc.Matlab;

@Matlab
@NotThreadSafe
public class MatlabConstraintTermIterator implements TermIterator
{
	/*-------
	 * State
	 */
	
	private final LPVariableConstraint.TermIterator _varIterator;
	private final LPFactorMarginalConstraint.TermIterator _factorIterator;
	private final Iterator<IntegerEquation> _constraintIterator;
	private final int _size;
	private IntegerEquation.TermIterator _curIterator;
	private int _row;
	
	/*--------------
	 * Construction
	 */
	
	MatlabConstraintTermIterator(List<IntegerEquation> constraints, int nTerms)
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
		if (_curIterator == null || !_curIterator.advance())
		{
			if (_constraintIterator != null && _constraintIterator.hasNext())
			{
				IntegerEquation constraint = _constraintIterator.next();
				LPVariableConstraint varConstraint = constraint.asVariableConstraint();
				if (varConstraint != null)
				{
					_curIterator = _varIterator.reset(varConstraint);
				}
				else
				{
					_curIterator = _factorIterator.reset(constraint.asFactorConstraint());
				}
				_curIterator.advance(); // assume this will return true
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
		return _curIterator != null ? _curIterator.getVariable() + 1 : -1;
	}

	@Override
	public int getCoefficient()
	{
		return _curIterator != null ? _curIterator.getCoefficient() : 0;
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
