/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

import static com.analog.lyric.math.Utilities.*;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.PriorAndCondition;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableBase;
import com.analog.lyric.dimple.solvers.core.SVariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;

import net.jcip.annotations.NotThreadSafe;

/**
 * Solver variable for Discrete variables under LP solver.
 * 
 * @since 0.07
 */
@NotThreadSafe
public class LPDiscrete extends SDiscreteVariableBase
{
	
	/*-------
	 * State
	 */
	
	/**
	 * The LP solver factor graph that owns this instance.
	 */
	private final LPSolverGraph _solverGraph;
	
	/**
	 * The index of the first marginal LP variable associated with the model variable.
	 * There will be one LP variable for each valid assignment to the variable, i.e. for
	 * each value of the variable with non-zero probability.
	 * <p>
	 * Set to negative value if not yet computed or if variable does not have any associated LP
	 * variables (because it has a fixed value).
	 */
	private int _lpVarIndex = -1;
	
	/**
	 * Represents the invalid assignments to the variable: which discrete values the variable
	 * is not allowed to take based on the input probabilities. If all values are allowed or if not yet computed, this
	 * will simply be null.
	 */
	private @Nullable BitSet _invalidAssignments;
	
	/**
	 * The number of valid assignments to the variable, or negative if not yet computed.
	 */
	private int _nValidAssignments = -1;
	
	private @Nullable double[] _beliefs = null;
	
	/*--------------
	 * Construction
	 */

	LPDiscrete(LPSolverGraph solverGraph, Discrete var)
	{
		super(var, solverGraph);
		_solverGraph = solverGraph;
	}
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	/**
	 * Returns the LP solver graph object to which this variable instance belongs.
	 * Note that unlike the default implementation provided by {@link SVariableBase#getParentGraph()},
	 * this method returns the graph that was used to construct this instance even
	 * if the solver on the associated model variable has changed.
	 */
	@Override
	public LPSolverGraph getParentGraph()
	{
		return _solverGraph;
	}
	
	/**
	 * Does nothing.
	 */
	@Override
	protected void doUpdateEdge(int outPortNum)
	{
	}

	/*-------------------------
	 * ISolverVariable methods
	 */
	
	@Override
	public double[] getBelief()
	{
		double[] beliefs = _beliefs;
		
		if (beliefs == null)
		{
			final int size = getModelObject().getDomain().size();
			beliefs = new double[size];
	
			PriorAndCondition known = getPriorAndCondition();
			Value fixedValue = known.value();
			if (fixedValue != null)
			{
				beliefs[fixedValue.getIndex()] = 1.0;
			}
			else
			{
				DiscreteMessage prior = toEnergyMessage(known);
				if (prior != null)
				{
					prior.getWeights(beliefs);
					normalize(beliefs);
				}
				else
				{
					Arrays.fill(beliefs,1.0/size);
				}
			}
			known.release();
		}

		return beliefs;
	}
	
	/*--------------------
	 * LPDiscrete methods
	 */
	
	/**
	 * Returns the index of the domain element for this variable for the given lp variable.
	 */
	public int lpVarToDomainIndex(int lpVar)
	{
		int domainIndex = -1;
		
		if (hasLPVariable(lpVar))
		{
			domainIndex = lpVar - _lpVarIndex;

			final BitSet invalidAssignments = _invalidAssignments;
			if (invalidAssignments != null)
			{
				domainIndex = -1;

				while (lpVar-- >= _lpVarIndex)
				{
					// Find next clear (valid assignment) bit
					domainIndex = invalidAssignments.nextClearBit(domainIndex + 1);
				}
			}
		}
		
		return domainIndex;
	}
	
	/**
	 * Convert index into variable domain into index of corresponding LP variable.
	 * Returns negative value if {@link #computeObjectiveFunction} not yet called
	 * of if there is no LP variable for the given {@code domainIndex} (because
	 * the value has been pruned from the equations because of zero weight).
	 */
	public int domainIndexToLPVar(int domainIndex)
	{
		int lpVar = _lpVarIndex;
		
		if (lpVar >= 0)
		{
			final BitSet invalidAssignments = _invalidAssignments;
			if (invalidAssignments == null)
			{
				lpVar += domainIndex;
			}
			else
			{
				if (invalidAssignments.get(domainIndex))
				{
					return -1;
				}
				for (int i = 0; (i = invalidAssignments.nextClearBit(i)) < domainIndex; ++i)
				{
					++lpVar;
				}

			}
		}
		
		return lpVar;
	}
	
	/**
	 * Discovers the non-zero input weights, computes their sum for normalization, and
	 * returns the number of LP variables to generate for this variable.
	 * <p>
	 * @returns the number of LP variables to generate, which is zero if the variable
	 * has a fixed value (i.e. only one non-zero input weight), and otherwise should
	 * equal the number of non-zero weights.
	 */
	int computeValidAssignments()
	{
		final int domlength = getDomain().size();
		
		PriorAndCondition known = getPriorAndCondition();
		final Value fixedValue = known.value();
		
		if (fixedValue != null)
		{
			_nValidAssignments = 1;
			BitSet invalidAssignments = new BitSet(domlength);
			invalidAssignments.set(fixedValue.getIndex());
			invalidAssignments.flip(0, domlength);
			_invalidAssignments = invalidAssignments;
			known.release();
			return 0;
		}
		
		DiscreteMessage prior = toEnergyMessage(known);
		known = known.release();
		
		int cardinality = 0;
		if (prior != null)
		{
			for (int i = domlength; --i >=0 ;)
			{
				if (prior.hasZeroWeight(i))
				{
					BitSet invalidAssignments = _invalidAssignments;
					if (invalidAssignments == null)
					{
						invalidAssignments = _invalidAssignments = new BitSet(i);
					}
					invalidAssignments.set(i, true);
				}
				else
				{
					++cardinality;
				}
			}

			_nValidAssignments = cardinality;

			return cardinality > 1 ? cardinality : 0;
		}
		else
		{
		
			_invalidAssignments = new BitSet(domlength);
			_nValidAssignments = domlength;
			return domlength;

		}

	}

	
	/**
	 * Compute the objective function parameters for this variable. This is simply
	 * the log probabilities of each possible variable value.
	 * 
	 * @param objectiveFunction is the array containing the objective function.
	 * @param start is the index of the first available slot in {@code objectiveFunction}.
	 * @return the index of the next available slot in {@code objectiveFunction}. The
	 * difference between this value and {@code start} should be equal to the number of
	 * valid variable assignments unless there is only one valid assignment.
	 */
	int computeObjectiveFunction(double[] objectiveFunction, int start)
	{
		if (_nValidAssignments > 1)
		{
			_lpVarIndex = start;

			PriorAndCondition known = getPriorAndCondition();
			final Value value = known.value();
			if (value != null)
			{
				objectiveFunction[start++] = 0;
			}
			else
			{
				DiscreteMessage prior = toEnergyMessage(known);
				if (prior != null)
				{
					for (double energy : prior.getEnergies())
					{
						if (energy < Double.POSITIVE_INFINITY)
						{
							objectiveFunction[start++] = -energy;
						}
					}
				}
				else
				{
					final int size = getDomain().size();
					Arrays.fill(objectiveFunction, start, start + size, 0.0);
					start += size;
				}
			}
			known = known.release();
		}
		else
		{
			_lpVarIndex = -1;
		}
		
		return start;
	}
	
	/**
	 * Computes constraint equation for this variable and adds to {@code constraints}.
	 * @return the number of non-zero terms in the constraint.
	 * <p>
	 * Call after {@link #computeObjectiveFunction}.
	 */
	int computeConstraints(List<IntegerEquation> constraints)
	{
		int nTerms = 0;
		
		if (_lpVarIndex >= 0)
		{
			LPVariableConstraint constraint = new LPVariableConstraint(this);
			constraints.add(constraint);
			nTerms = constraint.size();
		}
		
		return nTerms;
	}
		
	void printConstraintEquation(PrintStream out)
	{
		final Discrete mvar = getModelObject();
		final String varName = mvar.getName();
		DiscreteDomain domain = mvar.getDomain();
		final BitSet invalidAssignments = _invalidAssignments;
		
		boolean first = true;
		
		for (int i = 0, end = domain.size(); i < end; ++i)
		{
			if (invalidAssignments != null)
			{
				if ((i = invalidAssignments.nextClearBit(i)) >= end)
				{
					break;
				}
			}
			
			if (first)
			{
				first = false;
			}
			else
			{
				out.print(" + ");
			}
			
			out.format("p(%s=%s)", varName, domain.getElement(i));
		}
		
		out.println(" = 1");
	}
	
	void setBeliefsFromLPSolution(double[] solution)
	{
		final int beliefSize = getModelObject().getDomain().size();
		final double[] beliefs = new double[beliefSize];
		final int start = _lpVarIndex;
		
		if (start >= 0)
		{
			final BitSet invalidAssignments = _invalidAssignments;
			
			for (int i = 0, j = start; i < beliefSize; ++i, ++j)
			{
				if (invalidAssignments != null)
				{
					if ((i = invalidAssignments.nextClearBit(i)) >= beliefSize)
					{
						break;
					}
				}
				beliefs[i] = solution[j];
			}
			_beliefs = beliefs;
		}
		else
		{
			// Fixed value
			Value value = getKnownValue();
			if (value != null)
			{
				beliefs[value.getIndex()] = 1.0;
				_beliefs = beliefs;
			}
		}
	}
	
	void clearLPState()
	{
		_lpVarIndex = -1;
		_invalidAssignments = null;
		_nValidAssignments = -1;
	}
	
	/**
	 * Returns the index of the first LP variable associated with the values of this variable.
	 * Returns negative value if not yet computed or if there are no associated LP variables
	 * (because there is only one valid value).
	 */
	public int getLPVarIndex()
	{
		return _lpVarIndex;
	}
	
	public int getNumberOfValidAssignments()
	{
		return _nValidAssignments;
	}
	
	public boolean hasFixedValue()
	{
		return getKnownValue() != null;
	}
	
	/**
	 * True if {@link #computeObjectiveFunction} has been called and
	 * variable has at least one associated LP variable,
	 * i.e. {@link #getLPVarIndex} is non-negative.
	 */
	public boolean hasLPVariable()
	{
		return _lpVarIndex >= 0;
	}
	
	/**
	 * True if {@link #computeObjectiveFunction} has been called and
	 * {@code lpVar} index refers to one of the LP variables used by
	 * this variable.
	 */
	boolean hasLPVariable(int lpVar)
	{
		return _lpVarIndex >= 0 && lpVar >= _lpVarIndex && lpVar < _lpVarIndex + _nValidAssignments;
	}
	
	/**
	 * True if variable cannot have given index given its priors/fixed value.
	 * @since 0.08
	 */
	boolean hasZeroWeight(int index)
	{
		PriorAndCondition known = getPriorAndCondition();
		Value fixedValue = known.value();
		
		boolean result = false;
		
		if (fixedValue != null)
		{
			result = index != fixedValue.getIndex();
		}
		else
		{
			DiscreteMessage prior = toEnergyMessage(known);
			if (prior != null)
			{
				result = prior.hasZeroWeight(index);
			}
		}
		
		known.release();
		
		return result;
	}
}
