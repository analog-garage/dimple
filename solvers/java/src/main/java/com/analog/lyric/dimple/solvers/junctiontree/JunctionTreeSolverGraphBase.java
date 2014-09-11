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

package com.analog.lyric.dimple.solvers.junctiontree;

import static java.util.Objects.*;

import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransform;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransformMap;
import com.analog.lyric.dimple.model.transform.OptionVariableEliminatorCostList;
import com.analog.lyric.dimple.model.transform.VariableEliminator;
import com.analog.lyric.dimple.model.transform.VariableEliminator.CostFunction;
import com.analog.lyric.dimple.model.transform.VariableEliminator.VariableCost;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.options.DimpleOptions;
import com.analog.lyric.dimple.solvers.core.proxy.ProxySolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverBlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.Matlab;
import com.analog.lyric.util.misc.Misc;

/**
 * Base class for solver graphs using junction tree algorithm to transform graph into a tree
 * for exact inference using belief propagation.
 * 
 * @param <Delegate> specifies the type of the solver that will be used on the transformed graph and to
 * which this solver will delegate.
 * 
 * @since 0.05
 * @author Christopher Barber
 */
public abstract class JunctionTreeSolverGraphBase<Delegate extends ISolverFactorGraph>
	extends ProxySolverFactorGraph<Delegate>
{
	private final JunctionTreeTransform _transformer;
	private final @Nullable IFactorGraphFactory<?> _solverFactory;
	
	private @Nullable JunctionTreeTransformMap _transformMap = null;
	
	/*--------------
	 * Construction
	 */

	protected JunctionTreeSolverGraphBase(FactorGraph sourceModel, @Nullable IFactorGraphFactory<?> solverFactory)
	{
		super(sourceModel);
		_transformer = new JunctionTreeTransform();
		_solverFactory = solverFactory;
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public double getBetheEntropy()
	{
		final FactorGraph sourceModel = getModelObject();
		
		double entropy = 0;
		
		// Sum up factor entropy
		for (Factor factor : sourceModel.getFactorsFlat())
		{
			entropy += factor.getBetheEntropy();
		}
		
		// The following would be unnecessary if we implemented inputs as single node factors
		for (Variable variable : sourceModel.getVariablesFlat())
		{
			entropy -= variable.getBetheEntropy() * (variable.getSiblingCount() - 1);
		}
		
		return entropy;
	}

	@Override
	public double getBetheFreeEnergy()
	{
		return getInternalEnergy() - getBetheEntropy();
	}
	
	@Override
	public double getInternalEnergy()
	{
		final JunctionTreeTransformMap transformMap = getTransformMap();
		if (transformMap == null)
		{
			return Double.NaN;
		}

		double energy = 0;
		
		//Sum up factor internal energy
		for (Factor factor : transformMap.target().getFactorsFlat())
		{
			energy += factor.getInternalEnergy();
		}
		
		//The following would be unnecessary if we implemented inputs as single node factors
		for (Variable variable : getModelObject().getVariablesFlat())
		{
			energy += variable.getInternalEnergy();
		}
		
		return energy;
	}

	@Override
	public abstract @Nullable JunctionTreeSolverGraphBase<Delegate> getParentGraph();
	
	@Override
	public double getScore()
	{
		final JunctionTreeTransformMap transformMap = getTransformMap();
		if (transformMap == null)
		{
			return Double.NaN;
		}

		transformMap.updateGuesses();
		
		double energy = 0.0;

		for (Variable variable : getModelObject().getVariables())
		{
			energy += variable.getScore();
		}

		for (Factor factor : transformMap.target().getFactors())
		{
			energy += factor.getScore();
			if (Double.isInfinite(energy))
			{
				Misc.breakpoint();
			}
		}

		return energy;
	}
	
	@Override
	public abstract JunctionTreeSolverGraphBase<Delegate> getRootGraph();

	/*-------------------------
	 * ProxySolverNode methods
	 */
	
	@Override
	public @Nullable Delegate getDelegate()
	{
		final JunctionTreeTransformMap transformMap = _transformMap;
		if (transformMap != null)
		{
			@SuppressWarnings("unchecked")
			Delegate delegate = (Delegate) transformMap.target().getSolver();
			return delegate;
		}
		
		return null;
	}
	
	/*----------------------------
	 * ISolverFactorGraph methods
	 */
	
	@Override
	public ISolverBlastFromThePastFactor createBlastFromThePast(BlastFromThePastFactor factor)
	{
		// FIXME - blast from the past factor in junction tree
		throw unsupported("createBlastFromThePast");
	}
	
	@Override
	public abstract JunctionTreeSolverGraphBase<Delegate> createSubGraph(FactorGraph subgraph,
		@Nullable IFactorGraphFactory<?> factory);

	@Override
	public ISolverVariable createVariable(Variable var)
	{
		if (var instanceof Discrete)
		{
			return new JunctionTreeDiscreteSolverVariable((Discrete)var, getRootGraph());
		}
		else
		{
			return new JunctionTreeSolverVariable(var, getRootGraph());
		}
	}

	@Override
	public ISolverFactor createFactor(Factor factor)
	{
		return new JunctionTreeSolverFactor(factor, this);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * For the junction tree solver, the number of iterations is always one. Additional iterations
	 * should not modify the result.
	 */
	@Override
	public int getNumIterations()
	{
		return super.getNumIterations();
	}
	
	@Override
	public void setNumIterations(int numIterations)
	{
		super.setNumIterations(1);
	}
	
	@Override
	public @Nullable JunctionTreeSolverFactor getSolverFactor(Factor factor)
	{
		return (JunctionTreeSolverFactor)factor.getSolver();
	}
	
	@Override
	public @Nullable JunctionTreeSolverVariable getSolverVariable(Variable variable)
	{
		return (JunctionTreeSolverVariable)variable.getSolver();
	}
	
	@Override
	public void initialize()
	{
		// Configure settings from options.
		_transformer.useConditioning(getOptionOrDefault(JunctionTreeOptions.useConditioning));
		_transformer.maxTransformationAttempts(getOptionOrDefault(JunctionTreeOptions.maxTransformationAttempts));
		OptionVariableEliminatorCostList costFunctions =
			getOptionOrDefault(JunctionTreeOptions.variableEliminatorCostFunctions);
		_transformer.variableEliminatorCostFunctions(costFunctions.toArray(new CostFunction[costFunctions.size()]));
		
		Long seed = getOption(DimpleOptions.randomSeed);
		if (seed != null)
		{
			_transformer.random().setSeed(seed);
		}
		
		if (isTransformValid())
		{
			final JunctionTreeTransformMap transformMap = requireNonNull(_transformMap);
			
			// Copy inputs/fixed values to transformed model in case they have changed.
			for (Entry<Variable,Variable> entry : transformMap.sourceToTargetVariables().entrySet())
			{
				final Variable sourceVar = entry.getKey();
				if (sourceVar != null)
				{
					final Variable targetVar = entry.getValue();
					if (sourceVar.hasFixedValue())
					{
						targetVar.setFixedValueObject(sourceVar.getFixedValueObject());
					}
					else
					{
						targetVar.setInputObject(sourceVar.getInputObject());
					}
				}
			}
		}
		else
		{
			updateDelegate();
			// FIXME: update proxy factor mappings
		}
		
		requireNonNull(getDelegate()).initialize();
	}
	
	@Override
	public void iterate()
	{
		updateDelegate();
		requireDelegate("iterate").iterate();
	}
	
	@Override
	public void solve()
	{
		getModelObject().initialize();
		updateDelegate();
		requireDelegate("solve").solve();
	}
	
	@Override
	public void solveOneStep()
	{
		updateDelegate();
		requireDelegate("solveOneStep").solveOneStep();
	}
	
	@Override
	public void startSolver()
	{
		updateDelegate();
		requireDelegate("startSolver").startSolver();
	}
	
	/*---------------------------------
	 * JunctionTreeSolverGraph methods
	 */
	
	public @Nullable IFactorGraphFactory<?> getDelegateSolverFactory()
	{
		return _solverFactory;
	}

	/**
	 * The object that implements the junction tree transformation.
	 */
	public JunctionTreeTransform getTransformer()
	{
		return _transformer;
	}
	
	/**
	 * Returns transformed graph and accompanying mapping data. May be null if not yet computed
	 * (i.e. {@link #initialize()} not yet run.
	 */
	public @Nullable JunctionTreeTransformMap getTransformMap()
	{
		return _transformMap;
	}
	
	/**
	 * If true, then the transformation will condition out any variables that have a fixed value.
	 * This will produce a more efficient graph but will prevent it from being reused if the fixed
	 * value changes.
	 * <p>
	 * False by default.
	 * @see #useConditioning(boolean)
	 */
	public boolean useConditioning()
	{
		return _transformer.useConditioning();
	}
	
	/**
	 * Sets {@link #useConditioning()} to specified value.
	 * @return this
	 */
	public JunctionTreeSolverGraphBase<Delegate> useConditioning(boolean yes)
	{
		_transformer.useConditioning(yes);
		setOption(JunctionTreeOptions.useConditioning, yes);
		return this;
	}
	
	/**
	 * The cost functions used by {@link VariableEliminator} to determine the variable
	 * elimination ordering. If empty (the default), then all of the standard {@link VariableCost}
	 * functions will be tried.
	 * 
	 * @see #variableEliminatorCostFunctions(VariableEliminator.CostFunction...)
	 * @see #variableEliminatorCostFunctions(VariableEliminator.VariableCost...)
	 */
	public CostFunction[] variableEliminatorCostFunctions()
	{
		return _transformer.variableEliminatorCostFunctions();
	}
	
	/**
	 * Sets {@link #variableEliminatorCostFunctions()} to specified value.
	 * @return this
	 * @see #variableEliminatorCostFunctions(VariableEliminator.VariableCost...)
	 */
	public JunctionTreeSolverGraphBase<Delegate> variableEliminatorCostFunctions(CostFunction ... costFunctions)
	{
		 _transformer.variableEliminatorCostFunctions(costFunctions);
		 setOption(JunctionTreeOptions.variableEliminatorCostFunctions,
			 new OptionVariableEliminatorCostList(costFunctions));
		return this;
	}
	
	/**
	 * Sets {@link #variableEliminatorCostFunctions()} to specified value.
	 * @return this
	 * @see #variableEliminatorCostFunctions(VariableEliminator.CostFunction...)
	 */
	public JunctionTreeSolverGraphBase<Delegate> variableEliminatorCostFunctions(VariableCost ... costFunctions)
	{
		return variableEliminatorCostFunctions(VariableCost.toFunctions(costFunctions));
	}

	@Matlab
	public JunctionTreeSolverGraphBase<Delegate> variableEliminatorCostFunctions(String ... costFunctionNames)
	{
		final int n = costFunctionNames.length;
		CostFunction[] costFunctions = new CostFunction[n];
		for (int i = 0; i < n; ++i)
		{
			costFunctions[i] = VariableCost.valueOf(costFunctionNames[i]).function();
		}
		return variableEliminatorCostFunctions(costFunctions);
	}
	
	/**
	 * Specifies the maximum number of times to attempt to determine an optimal junction tree
	 * transformation.
	 * <p>
	 * Specifies the number of iterations of the {@link VariableEliminator} algorithm when
	 * attempting to determine the variable elimination ordering that determines the junction tree
	 * transformation. Each iteration will pick a cost function from
	 * {@link #variableEliminatorCostFunctions()} at random and will randomize the order of
	 * variables that have equivalent costs. A higher number of iterations may produce a better
	 * ordering.
	 * <p>
	 * Default value is specified by
	 * {@link JunctionTreeTransform#DEFAULT_MAX_TRANSFORMATION_ATTEMPTS}.
	 * <p>
	 * 
	 * @see #maxTransformationAttempts(int)
	 */
	public int maxTransformationAttempts()
	{
		return _transformer.maxTransformationAttempts();
	}
	
	/**
	 * Sets {@link #maxTransformationAttempts()} to the specified value.
	 * @return this
	 */
	public JunctionTreeSolverGraphBase<Delegate> maxTransformationAttempts(int iterations)
	{
		_transformer.maxTransformationAttempts(iterations);
		setOption(JunctionTreeOptions.maxTransformationAttempts, iterations);
		return this;
	}

	/*-----------------
	 * Package methods
	 */
	
	@Nullable ISolverVariable getDelegateSolverVariable(IJunctionTreeSolverVariable<?> var)
	{
		final JunctionTreeTransformMap transformMap = _transformMap;
		if (transformMap != null)
		{
			return transformMap.sourceToTargetVariable(var.getModelObject()).getSolver();
		}
		return null;
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private boolean isTransformValid()
	{
		final JunctionTreeTransformMap transformMap = _transformMap;
		return transformMap != null && transformMap.isValid();
	}
	
	private @Nullable ISolverFactorGraph updateDelegate()
	{
		if (!isTransformValid())
		{
			final JunctionTreeTransformMap transformMap = _transformMap = _transformer.transform(getModelObject());
			transformMap.target().setSolverFactory(_solverFactory);
		}
		return notifyNewDelegate(getDelegate());
	}

	@Override
	public boolean checkAllEdgesAreIncludedInSchedule()
	{
		return true;
	}

}
