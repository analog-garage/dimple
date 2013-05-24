package com.analog.lyric.dimple.solvers.lp;

import java.io.PrintStream;
import java.util.Arrays;

import net.jcip.annotations.NotThreadSafe;

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
		private int[] _lpVars;
		
		TermIterator(LPFactorMarginalConstraint constraint)
		{
			reset(constraint);
		}
		
		/**
		 * Reset the iterator from a new variable constraint.
		 */
		public TermIterator reset(LPFactorMarginalConstraint constraint)
		{
			_index = -1;
			_lpVars = constraint != null ? constraint._lpVars : null;
			return this;
		}
		
		@Override
		public boolean advance()
		{
			return _lpVars != null && ++_index < _lpVars.length;
		}

		@Override
		public int getVariable()
		{
			return _lpVars[_index];
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
	
	private final STableFactor _sfactor;
	private final int[] _lpVars;
	
	/*---------------
	 * Construction
	 */
	
	public LPFactorMarginalConstraint(STableFactor sfactor, int...lpVars)
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
	public STableFactor getSolverFactor()
	{
		return _sfactor;
	}

}
