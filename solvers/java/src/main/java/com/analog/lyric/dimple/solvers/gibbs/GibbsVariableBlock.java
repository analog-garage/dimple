/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.gibbs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.dimple.solvers.core.SChild;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariableBlock;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;

/**
 * Solver variable block state for Gibbs solver.
 * @since 0.08
 * @author Christopher Barber
 */
public class GibbsVariableBlock extends SChild<VariableBlock> implements ISolverVariableBlock
{
	/*-------
	 * State
	 */
	
	/**
	 * The owner of this object.
	 */
	private final GibbsSolverGraph _parent;
	
	/**
	 * The root Gibbs solver graph above the parent.
	 */
	private final GibbsSolverGraph _root;
	
	/**
	 * The solver variables in the same order as in the model block.
	 */
	private final ISolverVariableGibbs[] _vars;

	/**
	 * The domains of the variables in order.
	 */
	private final Domain[] _domains;
	
	/**
	 * Copies of the appropriate {@link Value} objects for the variables in order.
	 */
	private final Value[] _values;
	
	/**
	 * The neighbor solver nodes for the block (i.e. the Markov blanket)
	 */
	private final ISolverNodeGibbs[] _neighbors;
	
	/**
	 * Count of the number of updates performed since last reset.
	 */
	private long _updateCount;
	
	/**
	 * Count of number of rejected updates since the last reset.
	 */
	private long _rejectCount;
	
	/*--------------
	 * Construction
	 */
	
	GibbsVariableBlock(final VariableBlock block, GibbsSolverGraph parent)
	{
		super(block);
		_parent = parent;
		// FIXME - assumes that Gibbs is the root, this will change when we support nesting solvers
		_root = (GibbsSolverGraph)_parent.getRootSolverGraph();
		
		final int nVars = block.size();
		_vars = new ISolverVariableGibbs[nVars];
		_values = new Value[nVars];
		_domains = new Domain[nVars];
		final Set<ISolverNodeGibbs> neighborSet = new HashSet<ISolverNodeGibbs>();
		for (int i = 0; i < nVars; ++i)
		{
			Variable var = block.get(i);
			ISolverVariableGibbs svar = parent.getSolverVariable(block.get(i));
			_values[i] = svar.getCurrentSampleValue().clone();
			_vars[i] = svar;
			_domains[i] = var.getDomain();
			
			GibbsNeighbors neighbors = GibbsNeighbors.create(svar);
			if (neighbors == null)	// No deterministic dependents, neighbors are same as siblings
			{
				for (Factor f : var.getSiblings())
					neighborSet.add((ISolverNodeGibbs)f.getSolver());
			}
			else	// Has deterministic dependents
			{
				for (ISolverNodeGibbs neighbor : neighbors)
					neighborSet.add(neighbor);
			}
		}
		_neighbors = neighborSet.toArray(new ISolverNodeGibbs[neighborSet.size()]);

	}
	
	/*---------------------------------
	 * ISolverFactorGraphChild methods
	 */
	
	@Override
	@Nullable
	public ISolverFactorGraph getParentGraph()
	{
		return _parent;
	}

	@Override
	public ISolverFactorGraph getRootSolverGraph()
	{
		return _parent.getRootSolverGraph();
	}

	@Override
	public SolverNodeMapping getSolverMapping()
	{
		return _parent.getSolverMapping();
	}

	@Override
	public ISolverFactorGraph getContainingSolverGraph()
	{
		return _parent;
	}

	@Override
	public void initialize()
	{
	}
	
	/*------------------------------
	 * ISolverVariableBlock methods
	 */

	@Override
	public List<ISolverVariableGibbs> getSolverVariables()
	{
		return Collections.unmodifiableList(Arrays.asList(_vars));
	}

	/*----------------------------
	 * GibbsVariableBlock methods
	 */
	
	/**
	 * Computes the sample score of the block.
	 * <p>
	 * This is the sum of the {@linkplain ISolverNodeGibbs#getPotential() potentials} of
	 * the block's solver {@linkplain #getSolverVariables() variables} and
	 * {@linkplain #getSolverNeighbors() neighbors}.
	 * @since 0.08
	 */
	public double getCurrentSampleScore()
	{
		double score = 0;
		
		for (ISolverVariableGibbs v : _vars)
		{
			score += v.getPotential();
		}
		for (ISolverNodeGibbs v : _neighbors)
		{
			score += v.getPotential();
		}
			
		return score;
	}
	
	/**
	 * Array of domains for variables in block.
	 * <p>
	 * The array should be treated as immutable!
	 * @since 0.08
	 */
	public Domain[] getDomains()
	{
		return _domains;
	}

	/**
	 * The count of rejections since the last reset.
	 * <p>
	 * Counts the number of times that {@link #updateReject} have
	 * been invoked since last call to {@link #resetCounts()}.
	 * @since 0.08
	 */
	public final long getRejectionCount()
	{
		return _rejectCount;
	}

	/**
	 * Computes the sample score of the block and its neighbors given the specified values.
	 * <p>
	 * This will set the sample values on the graph and return its {@linkplain #getCurrentSampleScore() score}.
	 * Since the sample values are expected to be set again when the update finishes, this method does not
	 * restore the variables to their previous values.
	 * <p>
	 * @param sampleValues
	 * @since 0.08
	 */
	public double getSampleScore(Value[] sampleValues)
	{
		// WARNING: Side effect is that the current sample value changes to this sample value
		// Could change back but less efficient to do this, since we'll be updating the sample value anyway
		setCurrentSample(sampleValues);

		return getCurrentSampleScore();
	}

	/**
	 * Immutable view of neighbor solver nodes of the block.
	 * @since 0.08
	 */
	public List<ISolverNodeGibbs> getSolverNeighbors()
	{
		return Collections.unmodifiableList(Arrays.asList(_neighbors));
	}
	
	/**
	 * The count of updates since the last reset.
	 * <p>
	 * Counts the number of times that {@link #updateFinish} and {@link #updateReject} have
	 * been invoked since last call to {@link #resetCounts()}.
	 * @since 0.08
	 */
	public final long getUpdateCount()
	{
		return _updateCount;
	}
	
	/**
	 * Clear the rejection rate statistics
	 * <p>
	 * Resets counts of {@linkplain #getUpdateCount() updates} and {@linkplain #getRejectionCount() rejections}
	 * to zero.
	 * @since 0.08
	 */
	public final void resetCounts()
	{
		_updateCount = 0;
		_rejectCount = 0;
	}
	
	/**
	 * Initiate update of variable block.
	 * <p>
	 * This saves a copy of the current sample values of variables in the block and returns
	 * it. The update should be finished by calling one of {@link #updateFinish(Value[])} or
	 * {@link #updateReject()}.
	 * <p>
	 * @since 0.08
	 */
	public Value[] updateStart()
	{
		for (int i = 0, n = _vars.length; i < n; ++i)
		{
			_values[i].setFrom(_vars[i].getCurrentSampleValue());
		}
		
		return _values;
	}

	/**
	 * Terminate update of variable block with rejection.
	 * <p>
	 * This will restore the sample values of variables in the block back to
	 * the values saved by {@link #updateStart()}. This will also increment
	 * the {@linkplain #getUpdateCount() update} and {@linkplain #getRejectionCount() rejection}
	 * counts.
	 * 
	 * @since 0.08
	 */
	public void updateReject()
	{
		++_updateCount;
		++_rejectCount;
		setCurrentSample(_values);
	}
	
	/**
	 * Terminate update of variable block with final sample values.
	 * <p>
	 * This will set the sample values of variables in the block to the specifed values.
	 * This will also increment the {@linkplain #getUpdateCount() update} count.
	 * @param values must contain non-null {@link Value} compatible with the corresponding
	 * variables in the block.
	 * @since 0.08
	 */
	public void updateFinish(Value[] values)
	{
		++_updateCount;
		setCurrentSample(values);
	}
	
	/*-----------------
	 * Private methods
	 */

	private void setCurrentSample(Value[] sampleValues)
	{
		_root.deferDeterministicUpdates();
		final ISolverVariableGibbs[] vars = _vars;
		for (int i = 0, n = vars.length; i < n; i++)
		{
			vars[i].setCurrentSample(sampleValues[i]);
		}
		_root.processDeferredDeterministicUpdates();
	}
}
