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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.collect.ReleasableIterators;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.solvers.sumproduct.TableFactorEngineOptimized;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.LocalOptionHolder;
import com.analog.lyric.util.misc.Internal;
import com.analog.lyric.util.misc.Nullable;

/**
 * Implements optimized update functionality for a SFactorGraph class. Each SFactorGraph object
 * should hold an instance of this class, and delegate automatic update algorithm selection and
 * optimized update algorithm implementation to it.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public final class SFactorGraphOptimizedUpdateImpl
{
	/**
	 * Optimization settings for the SFactorGraph. These values are used for any factor that does
	 * not have other values explicitly set.
	 */
	private final UpdateSettings _optimizationSettings;

	/**
	 * Stores the update plan for each unique factor table.
	 */
	private final Map<IFactorTable, FactorUpdatePlan> _updatePlans;

	/**
	 * Stores the optimization settings for each factor table. Though the language is exposed by the
	 * solver factors, the property values are stored by factor table.
	 */
	private final Map<IFactorTable, UpdateSettings> _factorTableOptimizationSettings;

	public SFactorGraphOptimizedUpdateImpl(IOptionHolder graphOptionHolder)
	{
		_optimizationSettings = new UpdateSettings(graphOptionHolder);
		_updatePlans = new HashMap<IFactorTable, FactorUpdatePlan>();
		_factorTableOptimizationSettings = new WeakHashMap<IFactorTable, UpdateSettings>();
	}

	private final class FactorTableOptionHolder extends LocalOptionHolder
	{
		private final List<IOptionHolder> _delegatesList;

		public FactorTableOptionHolder()
		{
			_delegatesList = new ArrayList<IOptionHolder>(4);
			_delegatesList.add(this);
			// TODO
			// IOptionHolder model_factor;
			// c.add(model_factor);
			ReleasableIterator<? extends IOptionHolder> iter =
				_optimizationSettings.getOptionHolder().getOptionDelegates();
			while (iter.hasNext())
			{
				_delegatesList.add(iter.next());
			}
			iter.release();
		}

		@Override
		public @Nullable IOptionHolder getOptionParent()
		{
			return _optimizationSettings.getOptionHolder();
		}

		@Override
		public ReleasableIterator<? extends IOptionHolder> getOptionDelegates()
		{
			return ReleasableIterators.iteratorFor(_delegatesList);
		}
	}

	/**
	 * Returns the update settings for a given factor table.
	 * 
	 * @since 0.07
	 */
	public UpdateSettings getUpdateSettingsForFactorTable(IFactorTable factorTable)
	{
		UpdateSettings settings = _factorTableOptimizationSettings.get(factorTable);
		if (settings == null)
		{
			settings = new UpdateSettings(new FactorTableOptionHolder());
			_factorTableOptimizationSettings.put(factorTable, settings);
		}
		return settings;
	}

	/**
	 * Clears any previously-stored update plans.
	 * 
	 * @since 0.07
	 */
	public void clearOptimizedUpdatePlans()
	{
		_updatePlans.clear();
	}

	/**
	 * Gets the update plan for a given factor table. Creates the plan if it does not yet exist.
	 * 
	 * @since 0.07
	 */
	public FactorUpdatePlan getOptimizedUpdatePlan(IFactorTable factorTable)
	{
		FactorUpdatePlan updatePlan = _updatePlans.get(factorTable);
		if (updatePlan == null)
		{
			UpdateSettings factorTableOptimizationSettings = _factorTableOptimizationSettings.get(factorTable);
			updatePlan =
				FactorUpdatePlan.create(factorTable, factorTableOptimizationSettings,
					TableFactorEngineOptimized.getHelper(factorTableOptimizationSettings));
			_updatePlans.put(factorTable, updatePlan);
		}
		return updatePlan;
	}

	public UpdateApproach getUpdateApproach()
	{
		return _optimizationSettings.getUpdateApproach();
	}

	public void setUpdateApproach(UpdateApproach approach)
	{
		_optimizationSettings.setUpdateApproach(approach);
	}

	public void setOptimizedUpdateSparseThreshold(double value)
	{
		_optimizationSettings.setOptimizedUpdateSparseThreshold(value);
	}

	public double getOptimizedUpdateSparseThreshold()
	{
		return _optimizationSettings.getOptimizedUpdateSparseThreshold();
	}

	public void setAutomaticMemoryAllocationScalingFactor(double value)
	{
		_optimizationSettings.setAutomaticMemoryAllocationScalingFactor(value);
	}

	public double getAutomaticMemoryAllocationScalingFactor()
	{
		return _optimizationSettings.getAutomaticMemoryAllocationScalingFactor();
	}

	public double getAutomaticExecutionTimeScalingFactor()
	{
		return _optimizationSettings.getAutomaticExecutionTimeScalingFactor();
	}

	public void setAutomaticExecutionTimeScalingFactor(double value)
	{
		_optimizationSettings.setAutomaticExecutionTimeScalingFactor(value);
	}
}
