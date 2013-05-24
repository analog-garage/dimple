package com.analog.lyric.dimple.solvers.lp;

import java.io.PrintStream;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * Represents a linear equality describing the constraint that the probabilities
 * of a discrete random variable's values add up to one. The assumption is that the
 * variable's possible values are represented by n consecutive variables px to px+n,
 * so the equation is of the form:
 * <p>
 * px + ... + px+n = 1
 * </p>
 */
@Immutable
public final class LPVariableConstraint extends IntegerEquation
{
	@NotThreadSafe
	public static class TermIterator implements IntegerEquation.TermIterator
	{
		private int _cur;
		private int _last;
		
		TermIterator(LPVariableConstraint varConstraint)
		{
			reset(varConstraint);
		}
		
		/**
		 * Reset the iterator from a new variable constraint.
		 */
		public TermIterator reset(LPVariableConstraint varConstraint)
		{
			if (varConstraint == null)
			{
				_cur = -1;
				_last = -1;
			}
			else
			{
				_cur = varConstraint._firstLpVar - 1;
				_last = _cur + varConstraint._size;
			}
			
			return this;
		}
		
		@Override
		public boolean advance()
		{
			return ++_cur <= _last;
		}

		@Override
		public int getVariable()
		{
			return _cur;
		}

		@Override
		public int getCoefficient()
		{
			return 1;
		}
	}

	private final SVariable _svar;
	private final int _firstLpVar;
	private final int _size;
	
	/*--------------
	 * Construction
	 */
	
	LPVariableConstraint(SVariable svar)
	{
		_svar = svar;
		_firstLpVar = svar.getLPVarIndex();
		_size = svar.getNumberOfValidAssignments();
	}
	
	/*-------------------------
	 * IntegerEquation methods
	 */
	
	/**
	 * Returns non-null if this is a {@link LPVariableConstraint}.
	 */
	@Override
	public LPVariableConstraint asVariableConstraint()
	{
		return this;
	}
	
	@Override
	public int getCoefficient(int variable)
	{
		return hasCoefficient(variable) ? 1 : 0 ;
	}
	
	@Override
	public int getRHS()
	{
		return 1;
	}
	
	@Override
	public boolean hasCoefficient(int variable)
	{
		return variable >= _firstLpVar && variable < (_firstLpVar + _size);
	}
	
	@Override
	public TermIterator getTerms()
	{
		return new TermIterator(this);
	}
	
	@Override
	public int[] getVariables()
	{
		int[] vars = new int[_size];
		for (int i = 0; i < _size; ++i)
		{
			vars[i] = _firstLpVar + i;
		}
		return vars;
	}
	
	@Override
	public void print(PrintStream out)
	{
		_svar.printConstraintEquation(out);
	}
	
	@Override
	public int size()
	{
		return _size;
	}
	
	/*------------------------------
	 * LPVariableConstraint methods
	 */
	
	/**
	 * Returns the Dimple solver variable for which this constraint was generated.
	 */
	public SVariable getSolverVariable()
	{
		return _svar;
	}
 }
