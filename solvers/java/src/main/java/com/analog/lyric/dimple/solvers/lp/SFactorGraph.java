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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;
import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.SolverFactory;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.lp.IntegerEquation.TermIterator;
import com.analog.lyric.util.misc.Matlab;

@NotThreadSafe
public class SFactorGraph extends SFactorGraphBase
{
	/*-------
	 * State
	 */
	
	/**
	 * Maps model variables to their corresponding solver variable. Iterators will return
	 * variables in the order in which they were first added to the map.
	 */
	private final LinkedHashMap<VariableBase, SVariable> _varMap;
	
	/**
	 * Maps model factors to their corresponding solver factor. Iterators will return
	 * variables in the order in which they were first added to the map.
	 */
	private final LinkedHashMap<Factor, STableFactor> _factorMap;
	
	/**
	 * Contains the parameters of the linear objective function for the LP solve.
	 * It's length is the number of LP variables.
	 * <p>
	 * Null if not yet computed.
	 */
	private double[] _objectiveFunction = null;

	/**
	 * List of linear constraints describing this graph. The first {@link #getNumberOfVariableConstraints()}
	 * will be {@link LPVariableConstraint}s and the remainder will be {@link LPFactorMarginalConstraint}s.
	 * <p>
	 * Null if not yet computed.
	 */
	private List<IntegerEquation> _constraints = null;
	
	/**
	 * Number of non-zero terms in all of the {@link #_constraints}.
	 */
	private int _nConstraintTerms = 0;
	
	/**
	 * The number of variable constraints, equal to the number of rows in the constraint
	 * list dedicated to variable constraints, which will be at the front of the
	 * {@link _constraints} list.
	 * <p>
	 * Negative if not yet computed.
	 */
	private int _nVariableConstraints = -1;
	
	/**
	 * Name of external LP solver to be used to do the actual solving.
	 */
	private String _lpSolverName = "";
	private String _lpMatlabSolver = ""; // TODO: merge lpSolverName and lpSolver.
	
	
	/*--------------
	 * Construction
	 */

	SFactorGraph(FactorGraph model)
	{
		super(model);
		_varMap = new LinkedHashMap<VariableBase, SVariable>(model.getVariableCount());
		_factorMap = new LinkedHashMap<Factor, STableFactor>(model.getFactorCount());
	}
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public FactorGraph getModelObject()
	{
		return (FactorGraph)super.getModelObject();
	}

	/**
	 * Does nothing for this solver.
	 */
	@Override
	public void update()
	{
	}

	/**
	 * Does nothing for this solver.
	 */
	@Override
	public void updateEdge(int outPortNum)
	{
	}

	@Override
	public void initialize()
	{
	}

	/**
	 * Does nothing for this solver.
	 */
	@Override
	public void resetEdgeMessages(int portNum)
	{
	}

	/**
	 * If the associated model {@link FactorGraph} has {@link FactorGraph#getSolver()} equal to this,
	 * the model's parent graph's solver will be returned. Otherwise null.
	 */
	@Override
	public ISolverFactorGraph getParentGraph()
	{
		ISolverFactorGraph parentSolver = null;

		FactorGraph fg = getModelObject();
		if (fg.getSolver() == this)
		{
			parentSolver = fg.getParentGraph().getSolver();
		}

		return parentSolver;
	}

	/**
	 * If the associated model {@link FactorGraph} has {@link FactorGraph#getSolver()} equal to this,
	 * the model's root graph's solver will be returned. Otherwise this.
	 */
	@Override
	public ISolverFactorGraph getRootGraph()
	{
		ISolverFactorGraph rootSolver = this;

		FactorGraph fg = getModelObject();
		if (fg.getSolver() == this)
		{
			rootSolver = fg.getRootGraph().getSolver();
		}

		return rootSolver;
	}

	/**
	 * Unsupported.
	 */
	@Override
	public Object getInputMsg(int portIndex)
	{
		throw unsupported("getInputMsg");
	}

	/**
	 * Unsupported.
	 */
	@Override
	public Object getOutputMsg(int portIndex)
	{
		throw unsupported("getOutputMsg");
	}

	/**
	 * Unsupported.
	 */
	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		throw unsupported("setInputMsg");
	}

	/**
	 * Unsupported.
	 */
	@Override
	public void setOutputMsg(int portIndex, Object obj)
	{
		throw unsupported("setOutputMsg");
	}

	/**
	 * Unsupported.
	 */
	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		throw unsupported("setInputMsgValues");
	}

	/**
	 * Unsupported.
	 */
	@Override
	public void setOutputMsgValues(int portIndex, Object obj)
	{
		throw unsupported("setOutputMsgValues");
	}

	/**
	 * Unsupported.
	 */
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		throw unsupported("moveMessages");
	}

	/*----------------------------
	 * ISolverFactorGraph methods
	 */
	
	@Override
	public STableFactor createFactor(Factor factor)
	{
		STableFactor sfactor = _factorMap.get(factor);
		
		if (sfactor == null)
		{
			if (!(factor instanceof DiscreteFactor))
			{
				throw new DimpleException("Factor '%s' is not a DiscreteFactor. LP solver only supports discrete factors.",
					factor.getName());
			}

			sfactor = new STableFactor(this, (DiscreteFactor)factor);
			_factorMap.put(factor,  sfactor);
		}
		
		return sfactor;
	}

	@Override
	public SVariable createVariable(VariableBase var)
	{
		return createVariable(var, false);
	}

	private SVariable createVariable(VariableBase var, boolean copyInputs)
	{
		SVariable svar = _varMap.get(var);
		
		if (svar == null)
		{
			if (!(var instanceof Discrete))
			{
				throw new DimpleException("Variable '%s' is not discrete. LP solver only supports discrete variables.",
					var.getName());
			}
			
			// Reuse svar already associated with var if applicable.
			svar = var.getSolverIfTypeAndGraph(SVariable.class, this);
			if (svar == null)
			{
				svar = new SVariable(this, (Discrete)var);
			}
			else
			{
				svar.clearLPState();
				// If variable is already associated, we assume that the
				// inputs are already up-to-date.
				copyInputs = false;
			}
			_varMap.put(var, svar);
			
			if (copyInputs)
			{
				svar.setInputOrFixedValue(var.getInputObject(), var.getFixedValueObject(), var.hasFixedValue());
			}
		}
		
		return svar;
	}

	/**
	 * Always returns false.
	 */
	@Override
	public boolean customFactorExists(String funcName)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns "dimpleLPSolve" if the value of {@link #getLPSolverName()} is
	 * "matlab" and otherwise returns null.
	 */
	@Override
	public String getMatlabSolveWrapper()
	{
		return useMatlabSolver() ? "dimpleLPSolve" : null;
	}
	
	private boolean useMatlabSolver()
	{
		return _lpSolverName.isEmpty() || _lpSolverName.equalsIgnoreCase("matlab");
	}
	
	@Override
	public void iterate(int numIters)
	{
		if (useMatlabSolver())
		{
			throw new DimpleException("Java solve() not supported for LP solver using 'MATLAB' as underlying solver");
		}
		
		net.sf.javailp.Solver solver = null;
		
		try
		{
			Class<SolverFactory> factoryClass =
				(Class<SolverFactory>)Class.forName(String.format("net.sf.javailp.SolverFactory%s", _lpSolverName));
			solver = factoryClass.newInstance().get();
		}
		catch (Exception ex)
		{
			throw new DimpleException("Cannot load underlying LP solver '%s': %s'", _lpSolverName, ex.toString());
		}
		
		buildLPState();

		Problem problem = new Problem();
		
		double[] objectiveCoefficients = getObjectiveFunction();
		Linear objective = new Linear();
		for (int i = 0, end = objectiveCoefficients.length; i < end; ++i)
		{
			objective.add(objectiveCoefficients[i], i);
			problem.setVarBounds(0.0, i, 1.0);
		}
		problem.setObjective(objective);
		
		for (IntegerEquation constraint : getConstraints())
		{
			Linear linear = new Linear();
			TermIterator iter = constraint.getTerms();
			while (iter.advance())
			{
				linear.add(iter.getCoefficient(), iter.getVariable());
			}
			
			problem.add(linear, Operator.EQ, constraint.getRHS());
		}
		
		Result result = solver.solve(problem);
		
		double[] solution = new double[getNumberOfLPVariables()];
		for (int i = 0, end = solution.length; i < end; ++i)
		{
			solution[i] = result.get(i).doubleValue();
		}
		setSolution(solution);
	}

	/**
	 * Does nothing. Input ignored.
	 */
	@Override
	public void setNumIterations(int numIterations)
	{
	}

	/**
	 * Always returns one.
	 */
	@Override
	public int getNumIterations()
	{
		return 1;
	}

	@Override
	public void estimateParameters(FactorTable[] tables, int numRestarts, int numSteps, double stepScaleFactor)
	{
		throw unsupported("estimateParameters");
	}

	@Override
	public void baumWelch(FactorTable[] tables, int numRestarts, int numSteps)
	{
		throw unsupported("baumWelch");
	}

	@Override
	public void moveMessages(ISolverNode other)
	{
		throw unsupported("moveMessages");
	}

	/*-------------------------
	 * LP SFactorGraph methods
	 */
	
	/**
	 * Returns the linear objective function for the underlying LP solver or null if
	 * not yet computed.
	 * @see #buildLPState()
	 */
	@Matlab
	public double[] getObjectiveFunction()
	{
		return _objectiveFunction;
	}
	
	/**
	 * Get constraint linear equations. The first {@link #getNumberOfVariableConstraints()}
	 * constraints will be of type {@link LPVariableConstraint} and the remainder will be of
	 * type {@link LPFactorMarginalConstraint}.
	 */
	public List<IntegerEquation> getConstraints()
	{
		return _constraints;
	}
	
	/**
	 * Returns an object that can iterate over the non-zero terms of the linear
	 * constraint equations for constructing a sparse MATLAB matrix.
	 */
	@Matlab
	public MatlabConstraintTermIterator getMatlabSparseConstraints()
	{
		return new MatlabConstraintTermIterator(_constraints, _nConstraintTerms);
	}
	
	@Matlab
	public String getLPSolverName()
	{
		return _lpSolverName;
	}

	@Matlab
	public String getMatlabLPSolver()
	{
		return _lpMatlabSolver;
	}
	@Matlab
	public void setMatlabLPSolver(String name)
	{
		_lpMatlabSolver = name != null ? name : "";
	}
	@Matlab
	public void setLPSolverName(String name)
	{
		_lpSolverName = name != null ? name : "";
	}
	
	/**
	 * The number of constraints equations returned by {@link #getConstraints}
	 * or -1 if not yet computed.
	 * @see #hasLPState()
	 * @see #getNumberOfVariableConstraints()
	 */
	@Matlab
	public int getNumberOfConstraints()
	{
		return _constraints != null ? _constraints.size() : -1;
	}
	
	/**
	 * Returns the number of unique linear variables in the constraints to be
	 * solved using LP.
	 * <p>
	 * Returns -1 if not yet computed.
	 */
	public int getNumberOfLPVariables()
	{
		return _objectiveFunction != null ? _objectiveFunction.length : -1;
	}
	
	@Matlab
	public int getNumberOfMarginalConstraints()
	{
		return _constraints != null ? getNumberOfConstraints() - getNumberOfVariableConstraints() : -1;
	}
	
	/**
	 * The number of constraint equations describing a single variable or
	 * -1 if not yet computed.
	 * @see #getConstraints()
	 * @see #hasLPState()
	 */
	@Matlab
	public int getNumberOfVariableConstraints()
	{
		return _nVariableConstraints;
	}
	
	/**
	 * Prints constraint equations using model variable names and values for debugging purposes.
	 */
	public void printConstraints(PrintStream out)
	{
		if (_constraints == null)
		{
			out.println("Constraints not yet computed.");
			return;
		}
		
		for (IntegerEquation constraint : _constraints)
		{
			constraint.print(out);
		}
	}
	
	@Matlab
	public void printConstraints()
	{
		printConstraints(System.out);
	}
	
	@Matlab
	public void setSolution(double[] solution)
	{
		for (SVariable svar : _varMap.values())
		{
			svar.setBeliefsFromLPSolution(solution);
		}
	}
	
	/**
	 * Builds the LP description of the problem for the underlying LP solver
	 * to work on.
	 * @see #hasLPState()
	 */
	@Matlab
	public void buildLPState()
	{
		final FactorGraph model = getModelObject();
		
		int nLPVars = 0;
		
		// Create solver variables, if not already created
		for (VariableBase var : model.getVariables())
		{

			SVariable svar = createVariable(var, true);
			nLPVars += svar.computeValidAssignments();
		}

		// Create solver factor tables, if not already created.
		for (Factor factor : model.getFactors())
		{
			STableFactor sfactor = createFactor(factor);
			nLPVars += sfactor.computeValidAssignments();
		}

		double[] objectiveFunction = new double[nLPVars];
		int lpVarIndex = 0;
		
		List<IntegerEquation> constraints = new LinkedList<IntegerEquation>();
		int nTerms = 0;
		
		for (SVariable svar : _varMap.values())
		{
			lpVarIndex = svar.computeObjectiveFunction(objectiveFunction, lpVarIndex);
			nTerms += svar.computeConstraints(constraints);
		}
		
		_nVariableConstraints = constraints.size();
		
		for (STableFactor sfactor : _factorMap.values())
		{
			lpVarIndex = sfactor.computeObjectiveFunction(objectiveFunction, lpVarIndex);
			nTerms += sfactor.computeConstraints(constraints);
		}
		
		_objectiveFunction = objectiveFunction;
		_constraints = constraints;
		_nConstraintTerms = nTerms;
	}
	
	public void clearLPState()
	{
		_objectiveFunction = null;
		_nVariableConstraints = -1;
		_constraints = null;
		_nConstraintTerms = 0;
		for (SVariable svar : _varMap.values())
		{
			svar.clearLPState();
		}
		for (STableFactor sfactor : _factorMap.values())
		{
			sfactor.clearLPState();
		}
		_varMap.clear();
		_factorMap.clear();
	}
	
	/**
	 * Returns solver factor belonging to this solver graph that is
	 * associated with input model factor or else null.
	 */
	public STableFactor getSolverFactor(Factor factor)
	{
		return _factorMap.get(factor);
	}
	
	/**
	 * Returns solver variable belonging to this solver graph that is
	 * associated with input model variable or else null.
	 */
	public SVariable getSolverVariable(VariableBase var)
	{
		return _varMap.get(var);
	}
	
	/**
	 * Returns true if state needed for external LP solver to operate has been computed. If true
	 * the following methods will return valid values:
	 * <ul>
	 * <li>{@link #getConstraints}
	 * <li>{@link #getObjectiveFunction}
	 * <li>{@link #getMatlabSparseConstraints()}
	 * <li>{@link #getNumberOfConstraints}
	 * <li>{@link #getNumberOfMarginalConstraints()}
	 * <li>{@link #getNumberOfVariableConstraints}
	 * </ul>
	 * 
	 * @see #buildLPState()
	 * @see #clearLPState()
	 */
	public boolean hasLPState()
	{
		return _objectiveFunction != null;
	}

	/*-----------------
	 * Private methods
	 */
	
	private DimpleException unsupported(String methodName)
	{
		return DimpleException.unsupportedBySolver("LP", methodName);
	}
}
