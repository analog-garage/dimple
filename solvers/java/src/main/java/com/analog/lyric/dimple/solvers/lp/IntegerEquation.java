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

import org.eclipse.jdt.annotation.Nullable;


/**
 * Represents a linear equation with integer coefficients of the form:
 * <p>
 * c1*x1 + c2*x2 + ... + cn*xn = c
 * <p>
 */
public abstract class IntegerEquation
{
	/**
	 * Iterates over the terms of the linear equation.
	 * <p>
	 * Usage:
	 * <pre>
	 *    while (iterator.advance())
	 *    {
	 *        int v = iterator.getVariable();
	 *        int c = iterator.getCoefficient();
	 *        ...
	 *    }
	 * </pre>
	 */
	public static interface TermIterator
	{
		/**
		 * Advances the iterator to the next variable/coefficient pair.
		 * You must invoke this at least once before accessing
		 * {@link #getVariable()} or {@link #getCoefficient()}.
		 * 
		 * @return false if there are no more terms, in which case the
		 * variable and coefficient values are not defined.
		 */
		public boolean advance();
		
		/**
		 * @return variable identifier for current term. Undefined if {@link #advance()}
		 * was not invoked or returned false.
		 */
		public int getVariable();

		/**
		 * @return coefficient value for current term. Undefined if {@link #advance()}
		 * was not invoked or returned false.
		 */
		public int getCoefficient();
	}
	
	/*-------------------------
	 * IntegerEquation methods
	 */
	
	/**
	 * Returns non-null if this is a {@link LPVariableConstraint}.
	 */
	public @Nullable LPVariableConstraint asVariableConstraint()
	{
		return null;
	}
	
	/**
	 * Returns non-null if this is a {@link LPFactorMarginalConstraint}.
	 */
	public @Nullable LPFactorMarginalConstraint asFactorConstraint()
	{
		return null;
	}

	/**
	 * @return the coefficient of the variable with the given identifier or zero if
	 * {@code variable} is not in the equation.
	 */
	public abstract int getCoefficient(int variable);
	
	/**
	 * Gets the right-hand side of the equation.
	 */
	public abstract int getRHS();
	
	/**
	 * @return an iterator over the terms on the left side of the equation.
	 */
	public abstract TermIterator getTerms();
	
	/**
	 * @return an array of the identifiers of the variables on the left side of the
	 * equation.
	 */
	public abstract int[] getVariables();
	
	/**
	 * @return true if {@code variable} has a coefficient in the equation.
	 */
	public abstract boolean hasCoefficient(int variable);
	
	/**
	 * Prints out a representation of the constraint equation to {@code out}.
	 */
	public abstract void print(PrintStream out);
	
	/**
	 * @return the number of variables in the equation.
	 */
	public abstract int size();
}
