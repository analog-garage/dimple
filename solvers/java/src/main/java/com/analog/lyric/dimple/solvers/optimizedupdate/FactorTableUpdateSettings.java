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

@Internal
final public class FactorTableUpdateSettings
{
	private boolean _automaticOptimizationDecision;

	private @Nullable FactorUpdatePlan _updatePlan;

	private @Nullable UpdateApproach approach = null;

	// quantity of factors employing the associated factor table
	private int count = 0;

	private double executionTimeScalingFactor;

	private int level = -1;

	private double memoryAllocationScalingFactor;

	private double sparseThreshold;

	private boolean _automaticOptimizationDecisionMade = false;

	public @Nullable UpdateApproach getApproach()
	{
		return approach;
	}

	public boolean getAutomaticOptimizationDecision()
	{
		return _automaticOptimizationDecision;
	}

	public int getCount()
	{
		return count;
	}

	public double getExecutionTimeScalingFactor()
	{
		return executionTimeScalingFactor;
	}

	public int getLevel()
	{
		return level;
	}

	public double getMemoryAllocationScalingFactor()
	{
		return memoryAllocationScalingFactor;
	}

	public @Nullable FactorUpdatePlan getOptimizedUpdatePlan()
	{
		return _updatePlan;
	}

	public double getSparseThreshold()
	{
		return sparseThreshold;
	}

	public void setApproach(UpdateApproach approach)
	{
		this.approach = approach;
	}

	public void setAutomaticOptimizationDecision(boolean useOptimizedUpdate)
	{
		_automaticOptimizationDecision = useOptimizedUpdate;
		_automaticOptimizationDecisionMade  = true;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public void setExecutionTimeScalingFactor(double executionTimeScalingFactor)
	{
		this.executionTimeScalingFactor = executionTimeScalingFactor;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public void setMemoryAllocationScalingFactor(double memoryAllocationScalingFactor)
	{
		this.memoryAllocationScalingFactor = memoryAllocationScalingFactor;
	}

	public void setOptimizedUpdatePlan(@Nullable FactorUpdatePlan updatePlan)
	{
		_updatePlan = updatePlan;
	}

	public void setSparseThreshold(double sparseThreshold)
	{
		this.sparseThreshold = sparseThreshold;
	}

	public boolean useOptimizedUpdate()
	{
		if (approach == UpdateApproach.UPDATE_APPROACH_OPTIMIZED)
		{
			return true;
		}
		if (approach == UpdateApproach.UPDATE_APPROACH_AUTOMATIC)
		{
			return getAutomaticOptimizationDecision();
		}
		return false;
	}

	public boolean isAutomaticOptimizationDecisionMade()
	{
		return _automaticOptimizationDecisionMade;
	}
}
