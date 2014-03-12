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

package com.analog.lyric.dimple.solvers.core.proxy;

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

/**
 * @since 0.05
 */
public abstract class ProxySolverFactorGraph<Delegate extends ISolverFactorGraph>
	extends ProxySolverNode implements ISolverFactorGraph
{
	/*-------
	 * State
	 */
	
	protected final FactorGraph _modelFactorGraph;
	protected boolean _useMultithreading = false;
	protected int _numIterations = 1;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @param modelFactorGraph
	 */
	protected ProxySolverFactorGraph(FactorGraph modelFactorGraph)
	{
		_modelFactorGraph = modelFactorGraph;
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public FactorGraph getModelObject()
	{
		return _modelFactorGraph;
	}

	/*----------------------------
	 * ISolverFactorGraph methods
	 */

	@Override
	public void baumWelch(IFactorTable[] tables, int numRestarts, int numSteps)
	{
		throw unsupported("baumWelch");
	}

	@Override
	public void continueSolve()
	{
		getDelegate().continueSolve();
	}

	@Override
	public boolean customFactorExists(String funcName)
	{
		ISolverFactorGraph sfg = getDelegate();
		return sfg != null && sfg.customFactorExists(funcName);
	}

	@Override
	public void estimateParameters(IFactorTable[] tables, int numRestarts, int numSteps, double stepScaleFactor)
	{
		throw unsupported("estimateParameters");
	}

	@Override
	public double getBetheFreeEnergy()
	{
		return getDelegate().getBetheFreeEnergy();
	}

	@Override
	public String getMatlabSolveWrapper()
	{
		ISolverFactorGraph sfg = getDelegate();
		return sfg != null ? sfg.getMatlabSolveWrapper() : null;
	}

	@Override
	public void setNumIterations(int numIterations)
	{
		if (numIterations != _numIterations)
		{
			_numIterations = numIterations;
			ISolverFactorGraph delegate = getDelegate();
			if (delegate != null)
			{
				delegate.setNumIterations(numIterations);
			}
		}
	}

	@Override
	public int getNumIterations()
	{
		return _numIterations;
	}

	@Override
	public void interruptSolver()
	{
		ISolverFactorGraph sfg = getDelegate();
		if (sfg != null)
		{
			sfg.interruptSolver();
		}
	}

	@Override
	public boolean isSolverRunning()
	{
		ISolverFactorGraph sfg = getDelegate();
		return sfg != null && sfg.isSolverRunning();
	}

	@Override
	public void iterate()
	{
		getDelegate().iterate();
	}

	@Override
	public void iterate(int numIters)
	{
		getDelegate().iterate(numIters);
	}

	@Override
	public void moveMessages(ISolverNode other)
	{
		getDelegate().moveMessages(other);
	}

	@Override
	public void solve()
	{
		getDelegate().solve();
	}

	@Override
	public void solveOneStep()
	{
		getDelegate().solveOneStep();
	}

	@Override
	public void startContinueSolve()
	{
		getDelegate().startContinueSolve();
	}

	@Override
	public void startSolveOneStep()
	{
		getDelegate().startSolveOneStep();
	}

	@Override
	public void startSolver()
	{
		getDelegate().startSolver();
	}

	@Override
	public void postAdvance()
	{
		getDelegate().postAdvance();
	}

	@Override
	public void postAddFactor(Factor f)
	{
		ISolverFactorGraph sfg = getDelegate();
		if (sfg != null)
		{
			sfg.postAddFactor(f);
		}
	}

	@Override
	public void postSetSolverFactory()
	{
		ISolverFactorGraph sfg = getDelegate();
		if (sfg != null)
		{
			sfg.postSetSolverFactory();
		}
	}

	@Override
	public boolean useMultithreading()
	{
		return _useMultithreading;
	}

	@Override
	public void useMultithreading(boolean use)
	{
		if (use != _useMultithreading)
		{
			_useMultithreading = use;
			ISolverFactorGraph delegate = getDelegate();
			if (delegate != null)
			{
				delegate.useMultithreading(use);
			}
		}
	}

	/*-------------------------
	 * ProxySolverNode methods
	 */
	
	@Override
	public abstract Delegate getDelegate();
	
	/**
	 * Should be invoked by implementing subclass when delegate changes.
	 * 
	 * @param delegate is the new value that will be returned by {@link #getDelegate()}. May be null.
	 * @return delegate
	 * @since 0.05
	 */
	protected Delegate notifyNewDelegate(Delegate delegate)
	{
		if (delegate != null)
		{
			// Copy locally saved parameters.
			delegate.useMultithreading(_useMultithreading);
			delegate.setNumIterations(_numIterations);
		}
		return delegate;
	}
}
