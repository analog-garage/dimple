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

import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.util.misc.Internal;

/**
 * Exposes properties related to the factor update algorithm for a SFactorGraph or for a factor
 * table.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public final class UpdateSettings
{
	private final IOptionHolder _optionHolder;

	/**
	 * The system sets this on factor table properties if the effective optimized update approach
	 * for the factor table is AUTOMATIC, during graph initialization when a decision is made.
	 */
	private boolean _automaticOptimizationDecision;

	public UpdateSettings(IOptionHolder optionHolder)
	{
		_optionHolder = optionHolder;
	}

	public boolean getAutomaticOptimizationDecision()
	{
		return _automaticOptimizationDecision;
	}

	public void setAutomaticOptimizationDecision(boolean automaticOptimizationDecision)
	{
		_automaticOptimizationDecision = automaticOptimizationDecision;
	}

	public UpdateApproach getUpdateApproach()
	{
		return _optionHolder.getOptionOrDefault(UpdateOptions.updateApproach);
	}

	public void setUpdateApproach(UpdateApproach value)
	{
		_optionHolder.setOption(UpdateOptions.updateApproach, value);
	}

	public void unsetUpdateApproach()
	{
		_optionHolder.unsetOption(UpdateOptions.updateApproach);
	}

	public double getAutomaticExecutionTimeScalingFactor()
	{
		return _optionHolder.getOptionOrDefault(UpdateOptions.automaticExecutionTimeScalingFactor);
	}

	public void setAutomaticExecutionTimeScalingFactor(double value)
	{
		_optionHolder.setOption(UpdateOptions.automaticExecutionTimeScalingFactor, value);
	}

	public void unsetAutomaticExecutionTimeScalingFactor()
	{
		_optionHolder.unsetOption(UpdateOptions.automaticExecutionTimeScalingFactor);
	}

	public double getAutomaticMemoryAllocationScalingFactor()
	{
		return _optionHolder.getOptionOrDefault(UpdateOptions.automaticMemoryAllocationScalingFactor);
	}

	public void setAutomaticMemoryAllocationScalingFactor(double value)
	{
		_optionHolder.setOption(UpdateOptions.automaticMemoryAllocationScalingFactor, value);
	}

	public void unsetAutomaticMemoryAllocationScalingFactor()
	{
		_optionHolder.unsetOption(UpdateOptions.automaticMemoryAllocationScalingFactor);
	}

	public double getOptimizedUpdateSparseThreshold()
	{
		return _optionHolder.getOptionOrDefault(UpdateOptions.optimizedUpdateSparseThreshold);
	}

	public void setOptimizedUpdateSparseThreshold(double value)
	{
		_optionHolder.setOption(UpdateOptions.optimizedUpdateSparseThreshold, value);
	}

	public void unsetOptimizedUpdateSparseThreshold()
	{
		_optionHolder.unsetOption(UpdateOptions.optimizedUpdateSparseThreshold);
	}

	public boolean useOptimizedUpdate()
	{
		UpdateApproach approach = _optionHolder.getOptionOrDefault(UpdateOptions.updateApproach);
		switch (approach)
		{
		case UPDATE_APPROACH_AUTOMATIC:
			return _automaticOptimizationDecision;
		case UPDATE_APPROACH_NORMAL:
			return false;
		case UPDATE_APPROACH_OPTIMIZED:
			return true;
		default:
			return false;
		}
	}

	public IOptionHolder getOptionHolder()
	{
		return _optionHolder;
	}
}
