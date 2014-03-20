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

import java.util.Map.Entry;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransform;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransformMap;
import com.analog.lyric.dimple.model.transform.VariableEliminator;
import com.analog.lyric.dimple.model.transform.VariableEliminator.CostFunction;
import com.analog.lyric.dimple.model.transform.VariableEliminator.VariableCost;
import com.analog.lyric.dimple.model.variables.VariableBase;
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
	private final IFactorGraphFactory<?> _solverFactory;
	
	private JunctionTreeTransformMap _transformMap = null;
	
	/*--------------
	 * Construction
	 */

	protected JunctionTreeSolverGraphBase(FactorGraph sourceModel, IFactorGraphFactory<?> solverFactory)
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
		for (VariableBase variable : sourceModel.getVariablesFlat())
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
		for (VariableBase variable : getModelObject().getVariablesFlat())
		{
			energy += variable.getInternalEnergy();
		}
		
		return energy;
	}

	@Override
	public abstract JunctionTreeSolverGraphBase<Delegate> getParentGraph();
	
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
		
		for (VariableBase variable : getModelObject().getVariables())
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
	public Delegate getDelegate()
	{
		if (_transformMap != null)
		{
			return (Delegate) _transformMap.target().getSolver();
		}
		
		return null;
	}
	
	/*----------------------------
	 * ISolverFactorGraph methods
	 */
	
	@Override
	public ISolverBlastFromThePastFactor createBlastFromThePast(BlastFromThePastFactor factor)
	{
		// FIXME
		throw unsupported("createBlastFromThePast");
	}
	
	@Override
	public abstract JunctionTreeSolverGraphBase<Delegate> createSubGraph(FactorGraph subgraph,
		IFactorGraphFactory<?> factory);

	@Override
	public ISolverVariable createVariable(VariableBase var)
	{
		return new JunctionTreeSolverVariable(var, getRootGraph());
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
		if (numIterations != 1)
		{
			throw new DimpleException("Junction tree solver doesn't support multiple iterations.");
		}
		super.setNumIterations(numIterations);
	}
	
	@Override
	public JunctionTreeSolverFactor getSolverFactor(Factor factor)
	{
		return (JunctionTreeSolverFactor)factor.getSolver();
	}
	
	@Override
	public JunctionTreeSolverVariable getSolverVariable(VariableBase variable)
	{
		return (JunctionTreeSolverVariable)variable.getSolver();
	}
	
	@Override
	public void initialize()
	{
		if (isTransformValid())
		{
			// Copy inputs/fixed values to transformed model in case they have changed.
			for (Entry<VariableBase,VariableBase> entry : _transformMap.sourceToTargetVariables().entrySet())
			{
				final VariableBase sourceVar = entry.getKey();
				if (sourceVar != null)
				{
					final VariableBase targetVar = entry.getValue();
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
		
		getDelegate().initialize();
	}
	
	@Override
	public void iterate()
	{
		updateDelegate().iterate();
	}
	
	@Override
	public void solve()
	{
		getModelObject().initialize();
		updateDelegate().solve();
	}
	
	@Override
	public void solveOneStep()
	{
		updateDelegate().solveOneStep();
	}
	
	/*---------------------------------
	 * JunctionTreeSolverGraph methods
	 */
	
	public IFactorGraphFactory<?> getDelegateSolverFactory()
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
	public JunctionTreeTransformMap getTransformMap()
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
		return this;
	}
	
	/**
	 * The cost functions used by {@link VariableEliminator} to determine the variable
	 * elimination ordering. If empty (the default), then all of the standard {@link VariableCost}
	 * functions will be tried.
	 * 
	 * @see #variableEliminatorCostFunctions(CostFunction...)
	 * @see #variableEliminatorCostFunctions(VariableCost...)
	 */
	public CostFunction[] variableEliminatorCostFunctions()
	{
		return _transformer.variableEliminatorCostFunctions();
	}
	
	/**
	 * Sets {@link #variableEliminatorCostFunctions()} to specified value.
	 * @return this
	 * @see #variableEliminatorCostFunctions(VariableCost...)
	 */
	public JunctionTreeSolverGraphBase<Delegate> variableEliminatorCostFunctions(CostFunction ... costFunctions)
	{
		 _transformer.variableEliminatorCostFunctions(costFunctions);
		return this;
	}

	/**
	 * Sets {@link #variableEliminatorCostFunctions()} to specified value.
	 * @return this
	 * @see #variableEliminatorCostFunctions(CostFunction...)
	 */
	public JunctionTreeSolverGraphBase<Delegate> variableEliminatorCostFunctions(VariableCost ... costFunctions)
	{
		 _transformer.variableEliminatorCostFunctions(costFunctions);
		return this;
	}

	@Matlab
	public JunctionTreeSolverGraphBase<Delegate> variableEliminatorCostFunctions(String ... costFunctionNames)
	{
		final int n = costFunctionNames.length;
		VariableCost[] costFunctions = new VariableCost[n];
		for (int i = 0; i < n; ++i)
		{
			costFunctions[i] = VariableCost.valueOf(costFunctionNames[i]);
		}
		 _transformer.variableEliminatorCostFunctions(costFunctions);
		return this;
	}
	
	/**
	 * Specifies the number of iterations of the {@link VariableEliminator} when attempting
	 * to determine the variable elimination ordering. Each iteration will pick a cost function
	 * from {@link #variableEliminatorCostFunctions()} at random and will randomize the order of
	 * variables that have equivalent costs. A higher number of iterations may produce a better
	 * ordering.
	 * <p>
	 * Default value is specified by {@link JunctionTreeTransform#DEFAULT_ELIMINATOR_ITERATIONS}.
	 * <p>
	 * @see #variableEliminatorIterations(int)
	 */
	public int variableEliminatorIterations()
	{
		return _transformer.variableEliminatorIterations();
	}
	
	/**
	 * Sets {@link #variableEliminatorIterations()} to the specified value.
	 * @return this
	 */
	public JunctionTreeSolverGraphBase<Delegate> variableEliminatorIterations(int iterations)
	{
		_transformer.variableEliminatorIterations(iterations);
		return this;
	}

	/*-----------------
	 * Package methods
	 */
	
	ISolverVariable getDelegateSolverVariable(JunctionTreeSolverVariable var)
	{
		if (_transformMap != null)
		{
			return _transformMap.sourceToTargetVariable(var.getModelObject()).getSolver();
		}
		return null;
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private boolean isTransformValid()
	{
		return _transformMap != null && _transformMap.isValid();
	}
	
	private ISolverFactorGraph updateDelegate()
	{
		if (!isTransformValid())
		{
			_transformMap = _transformer.transform(getModelObject());
			_transformMap.target().setSolverFactory(_solverFactory);
		}
		return notifyNewDelegate(getDelegate());
	}

}
