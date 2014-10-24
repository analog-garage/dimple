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
import java.io.PrintStream;
import java.util.BitSet;
import java.util.List;
import java.util.SortedSet;

import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorTableBase;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.dimple.solvers.core.SVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

/**
 * Solver table factor under LP solver.
 * 
 * @since 0.07
 * @author Christopher Barber
 */
@NotThreadSafe
public class LPTableFactor extends STableFactorBase
{
	/*-------
	 * State
	 */
	
	/**
	 * The LP solver factor graph that owns this instance.
	 */
	private final LPSolverGraph _solverGraph;
	
	/**
	 * The index of the first LP variable associated with the model factor.
	 * There will be one LP variable for each valid (non-zero probability) joint assignment of the variables
	 * input to the factor table.
	 * <p>
	 * Set to negative value if not yet computed.
	 */
	private int _lpVarIndex = -1;

	private int _nLpVars = 0;
	
	/**
	 * Represents the invalid joint assignments of the variables input to the factor table
	 * as an index into a factor table's {@link FactorTableBase#getWeightsSparseUnsafe()} array. An assignment is invalid
	 * either if the weight in the table is zero (which normally would only happen if it was explicitly
	 * set to zero after the table was constructed) or if one of the input parameters has a zero input
	 * probability.
	 * <p>
	 * This will be null if all assignments in the weights list are valid.
	 * <p>
	 * NOTE: for large non-sparse factor tables, we would want a representation that does not take
	 * O(size-of-factor-table) space.
	 */
	private @Nullable BitSet _invalidAssignments = null;
	
	/*--------------
	 * Construction
	 */
	
	LPTableFactor(LPSolverGraph solverGraph, DiscreteFactor factor)
	{
		super(factor);
		_solverGraph = solverGraph;
	}
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public DiscreteFactor getModelObject()
	{
		return (DiscreteFactor)super.getModelObject();
	}
	
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
	
	/*----------------------
	 * ISolverFactor methods
	 */

	/**
	 * Does nothing.
	 */
	@Override
	public void createMessages()
	{
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void doUpdateEdge(int outPortNum)
	{
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void resetEdgeMessages(int portNum)
	{
	}

	/**
	 * Always returns null.
	 */
	@Override
	public @Nullable Object getInputMsg(int portIndex)
	{
		return null;
	}

	/**
	 * Always returns null.
	 */
	@Override
	public @Nullable Object getOutputMsg(int portIndex)
	{
		return null;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
	}
	
	/*--------------------------
	 * STableFactorBase methods
	 */
	
	@Override
	protected void setTableRepresentation(IFactorTable table)
	{
		table.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT_WITH_INDICES);
	}
	
	/*-------------------------
	 * LP STableFactor methods
	 */

	/**
	 * It is assumed that {@link LPDiscrete#computeValidAssignments()} has already been invoked on the variables
	 * connected to this factor.
	 */
	int computeValidAssignments()
	{
		final DiscreteFactor factor = getModelObject();
		final IFactorTable factorTable = factor.getFactorTable();
		final double[] weights = factorTable.getWeightsSparseUnsafe();
		final LPDiscrete[] svariables = getSVariables();

		int cardinality = 0;
	
		boolean hasNonFixedVariable = true;
		for (LPDiscrete svar : svariables)
		{
			if (!svar.hasFixedValue())
			{
				hasNonFixedVariable = true;
				break;
			}
		}
		
		if (hasNonFixedVariable)
		{
			for (int i = weights.length; --i >= 0;)
			{
				double weight = weights[i];
				boolean skipEntry = true;
				if (weight != 0)
				{
					// If any of the variable input weights for these values
					// is zero, then we can skip this entry.
					skipEntry = false;
					final int[] indices = factorTable.sparseIndexToIndices(i);
					for (int j = 0, endj = indices.length; j < endj; ++j)
					{
						LPDiscrete svar = svariables[j];
						double[] allinputs = svar.getInputs();
						if (allinputs != null && 0.0 == svar.getInput(indices[j]))
						{
							skipEntry = true;
							break;
						}
					}
				}

				if (skipEntry)
				{
					BitSet invalidAssignments = _invalidAssignments;
					if (invalidAssignments == null)
					{
						invalidAssignments = _invalidAssignments = new BitSet(i);
					}
					invalidAssignments.set(i);
				}
				else
				{
					++cardinality;
				}
			}
		}
		
		_nLpVars = cardinality;
		
		return cardinality;
	}
	
	/*
	 */
	int computeObjectiveFunction(double[] objectiveFunction, int start)
	{
		if (_nLpVars > 0)
		{
			_lpVarIndex = start;

			final DiscreteFactor factor = getModelObject();
			final IFactorTable factorTable = factor.getFactorTable();
			final double[] weights = factorTable.getWeightsSparseUnsafe();
			final int nWeights = weights.length;

			final BitSet invalidAssignments = _invalidAssignments;
			for (int i = 0; i < nWeights; ++i)
			{
				if (invalidAssignments != null && nWeights <= (i = invalidAssignments.nextClearBit(i)))
				{
					break;
				}
				objectiveFunction[start++] = Math.log(weights[i]);
			}
		}
		
		return start;
	}
	
	private LPDiscrete[] getSVariables()
	{
		// Build array of solver variables for input variables.
		final Factor factor = getModelObject();
		final int nVars = factor.getSiblingCount();
		final LPDiscrete[] svariables = new LPDiscrete[nVars];
		for (int i = nVars; --i >= 0;)
		{
			// Getting this from the solver graph instead of from the model variable allows
			// the solver to operate even when it is detached from the model.
			svariables[i] = _solverGraph.getSolverVariable(factor.getSibling(i));
		}
		return svariables;
	}
	
	/**
	 * Computes constraint equations for this factor table and adds to {@code constraints}.
	 * @return the total number of non-zero terms in added constraints.
	 * <p>
	 * Call after {@link #computeObjectiveFunction}.
	 */
	int computeConstraints(List<IntegerEquation> constraints)
	{
		if (_nLpVars <= 0)
		{
			return 0;
		}
		
		final DiscreteFactor factor = getModelObject();
		final IFactorTable factorTable = factor.getFactorTable();
		final int[][] rows = factorTable.getIndicesSparseUnsafe();
		final int nRows = rows.length;

		final LPDiscrete[] svariables = getSVariables();
		
		// Table of the marginal constraints for this factor where key is the index of the LP variable for
		// the marginal variable value, and the associated values are the indexes of the LP
		// variables in this factor that have the same variable value.
		final SortedSetMultimap<Integer, Integer> marginalConstraints = TreeMultimap.create();
		
		final BitSet invalidAssignments = _invalidAssignments;
		for (int i = 0, lpFactor = _lpVarIndex; i < nRows; ++i, ++lpFactor)
		{
			if (invalidAssignments != null && nRows <= (i = invalidAssignments.nextClearBit(i)))
			{
				break;
			}
			
			int [] indices = rows[i];
			for (int j = 0, endj = indices.length; j < endj; ++j)
			{
				LPDiscrete svar = svariables[j];
				if (svar.hasLPVariable())
				{
					// Only build marginal constraints for variables that have LP variables
					// (i.e. don't have fixed values).
					int valueIndex = indices[j];
					int lpVar = svar.domainIndexToLPVar(valueIndex);
					marginalConstraints.put(lpVar,  lpFactor);
				}
			}
		}
		
		int nTerms = 0;
		
		for (int lpVar : marginalConstraints.keySet())
		{
			// This expresses the constraint that the marginal probability of a particular variable value
			// is equal to the sum of the non-zero factor table entries for the same variable value.
			
			SortedSet<Integer> lpFactorVars = marginalConstraints.get(lpVar);
			
			int[] lpVars = new int[1 + lpFactorVars.size()];
			lpVars[0] = lpVar;
			int i = 0;
			for (int lpFactorVar : lpFactorVars)
			{
				lpVars[++i] = lpFactorVar;
			}
			
			constraints.add(new LPFactorMarginalConstraint(this, lpVars));
			nTerms += lpVars.length;
		}
		
		return nTerms;
	}
	
	void clearLPState()
	{
		_lpVarIndex = -1;
		_invalidAssignments = null;
	}
	
	/**
	 * Returns the index of the first LP variable for this factor, or else a negative value if
	 * LP state has not yet been computed or if factor is not included in LP representation
	 * (e.g. because all its variables are fixed).
	 */
	public int getLPVarIndex()
	{
		return _lpVarIndex;
	}

	public int getNumberOfValidAssignments()
	{
		return _nLpVars;
	}
	
	/**
	 * Underlying implementation of {@link LPFactorMarginalConstraint#print}.
	 */
	void printConstraintEquation(PrintStream out, int[] lpVars)
	{
		final int lpVar = lpVars[0];

		final DiscreteFactor factor = getModelObject();
		final IFactorTable factorTable = factor.getFactorTable();
		final int[][] rows = factorTable.getIndicesSparseUnsafe();
		final int nRows = rows.length;

		// Build array of solver variables for input variables.
		final LPDiscrete[] svariables = getSVariables();

		// Find the marginal variable from its lpVar index
		for (LPDiscrete svar : svariables)
		{
			// Linear search could be replaced by a binary search.
			if (svar.hasLPVariable(lpVar))
			{
				// Print out term for marginal variable value
				Discrete var = svar.getModelObject();
				String varName = var.getName();
				int varValueIndex = svar.lpVarToDomainIndex(lpVar);
				Object varValue = var.getDomain().getElement(varValueIndex);
				out.format("-p(%s=%s)", varName, varValue);
				break;
			}
		}

		final BitSet invalidAssignments = _invalidAssignments;

		int lpVarsIndex = 1;
		
		for (int rowIndex = 0, lpVarForRow = _lpVarIndex; rowIndex < nRows; ++rowIndex, ++lpVarForRow)
		{
			if (invalidAssignments != null && nRows <= (rowIndex = invalidAssignments.nextClearBit(rowIndex)))
			{
				break;
			}
			
			if (lpVarForRow == lpVars[lpVarsIndex])
			{
				int[] indices = rows[rowIndex];
				
				out.print(" + p(");
				for (int i = 0, end = indices.length; i < end ; ++i)
				{
					LPDiscrete svar = svariables[i];
					Discrete var = svar.getModelObject();
					Object[] elements = var.getDomain().getElements();
					if (i > 0)
					{
						out.print(",");
					}
					out.format("%s=%s", var.getName(), elements[indices[i]]);
				}
				out.print(")");
				
				if (++lpVarsIndex == lpVars.length)
				{
					break;
				}
			}
		}

		out.println(" = 0");
	}
	
}
