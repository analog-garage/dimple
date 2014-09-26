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

package com.analog.lyric.dimple.solvers.optimizedupdate;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.util.misc.Internal;

/**
 * Stores information related to the optimized update algorithm for a given factor table. Various
 * factors in a graph that share a factor table may have different option settings. During solver
 * graph initialization, the schedule is iterated to find the factors that shall have update applied
 * to them, and their option values. These option values are analyzed in order to set up an instance
 * of this class for each unique factor table. Later, during solver factor initialization, the
 * information is used to configure each factor's update.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
final public class FactorTableUpdateSettings
{
	/**
	 * The update approach for the factor as specified in the options, which may include
	 * {@link UpdateApproach#AUTOMATIC}.
	 */
	private @Nullable UpdateApproach _approach = null;

	/**
	 * Valid (non-null) only for those factors that have their update approach set to
	 * {@link UpdateApproach#AUTOMATIC}, and only after the solver graph has
	 * initialized. Indicates the update approach selected.
	 */
	private @Nullable UpdateApproach _automaticApproachDecision = null;

	/**
	 * Valid (non-null) only for those factors that employ the optimized update algorithm, and only
	 * after the solver graph has been initialized.
	 */
	private @Nullable FactorUpdatePlan _updatePlan;

	/**
	 * Quantity of factors that share the represented factor table.
	 */
	private int _factorCount = 0;

	/**
	 * Execution time scaling factor for weighing execution time and memory allocation costs.
	 */
	private double _executionTimeScalingFactor;

	/**
	 * Users may set options at various levels, such as on the solver factor, the solver graph, the
	 * model factor, the model graph, or perhaps others. When multiple factors share a factor table,
	 * we track the level in order to retain the most specific settings. The lower the value, the
	 * more specific it is.
	 */
	private int _optionLevel = -1;

	/**
	 * Memory allocation scaling factor for weighing execution time and memory allocation costs.
	 */
	private double _memoryAllocationScalingFactor;

	/**
	 * Density, below which the optimized update algorithm will use a sparse representation for its
	 * auxiliary factor tables.
	 */
	private double _sparseThreshold;

	public @Nullable UpdateApproach getApproach()
	{
		return _approach;
	}

	public @Nullable UpdateApproach getAutomaticUpdateApproach()
	{
		return _automaticApproachDecision;
	}

	public int getCount()
	{
		return _factorCount;
	}

	public double getExecutionTimeScalingFactor()
	{
		return _executionTimeScalingFactor;
	}

	public int getLevel()
	{
		return _optionLevel;
	}

	public double getMemoryAllocationScalingFactor()
	{
		return _memoryAllocationScalingFactor;
	}

	public @Nullable FactorUpdatePlan getOptimizedUpdatePlan()
	{
		return _updatePlan;
	}

	public double getSparseThreshold()
	{
		return _sparseThreshold;
	}

	public void setApproach(UpdateApproach approach)
	{
		this._approach = approach;
	}

	public void setAutomaticUpdateApproach(@Nullable UpdateApproach updateApproach)
	{
		_automaticApproachDecision = updateApproach;
	}

	public void setCount(int count)
	{
		this._factorCount = count;
	}

	public void setExecutionTimeScalingFactor(double executionTimeScalingFactor)
	{
		this._executionTimeScalingFactor = executionTimeScalingFactor;
	}

	public void setLevel(int level)
	{
		this._optionLevel = level;
	}

	public void setMemoryAllocationScalingFactor(double memoryAllocationScalingFactor)
	{
		this._memoryAllocationScalingFactor = memoryAllocationScalingFactor;
	}

	public void setOptimizedUpdatePlan(@Nullable FactorUpdatePlan updatePlan)
	{
		_updatePlan = updatePlan;
	}

	public void setSparseThreshold(double sparseThreshold)
	{
		this._sparseThreshold = sparseThreshold;
	}
}
