package com.analog.lyric.dimple.solvers.optimizedupdate;

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.util.misc.Internal;

/**
 * Implements optimized update functionality for a STableFactor class. A STableFactor object should
 * hold an instance of this class and delegate to it.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public final class STableFactorOptimizedUpdateImpl
{
	/**
	 * Implementation object for the associated solver graph.
	 */
	private final SFactorGraphOptimizedUpdateImpl _sFactorGraphOptimizedUpdateImpl;

	/**
	 * The factor table.
	 */
	private final IFactorTable _factorTable;

	/**
	 * Optimization settings for the factor table.
	 */
	private final UpdateSettings _factorTableUpdateSettings;

	public STableFactorOptimizedUpdateImpl(SFactorGraphOptimizedUpdateImpl sFactorGraphOptimizedUpdateImpl,
		IFactorTable factorTable)
	{
		_sFactorGraphOptimizedUpdateImpl = sFactorGraphOptimizedUpdateImpl;
		_factorTable = factorTable;
		_factorTableUpdateSettings = sFactorGraphOptimizedUpdateImpl.getUpdateSettingsForFactorTable(_factorTable);
	}

	/**
	 * Returns true if the optimized update algorithm should be used for the factor.
	 * 
	 * @since 0.07
	 */
	public boolean useOptimizedUpdate()
	{
		return _factorTableUpdateSettings.useOptimizedUpdate() && _factorTable.getDimensions() > 1;
	}

	public FactorUpdatePlan getOptimizedUpdatePlan()
	{
		return _sFactorGraphOptimizedUpdateImpl.getOptimizedUpdatePlan(_factorTable);
	}

	public boolean getAutomaticOptimizationDecision()
	{
		return _factorTableUpdateSettings.getAutomaticOptimizationDecision();
	}

	public UpdateApproach getUpdateApproach()
	{
		return _factorTableUpdateSettings.getUpdateApproach();
	}

	public void setUpdateApproach(UpdateApproach approach)
	{
		_factorTableUpdateSettings.setUpdateApproach(approach);
	}

	public void unsetUpdateApproach()
	{
		_factorTableUpdateSettings.unsetUpdateApproach();
	}

	public double getOptimizedUpdateSparseThreshold()
	{
		return _factorTableUpdateSettings.getOptimizedUpdateSparseThreshold();
	}

	public void setOptimizedUpdateSparseThreshold(double value)
	{
		_factorTableUpdateSettings.setOptimizedUpdateSparseThreshold(value);
	}

	public void unsetOptimizedUpdateSparseThreshold()
	{
		_factorTableUpdateSettings.unsetOptimizedUpdateSparseThreshold();
	}

	public double getAutomaticMemoryAllocationScalingFactor()
	{
		return _factorTableUpdateSettings.getAutomaticMemoryAllocationScalingFactor();
	}

	public void setAutomaticMemoryAllocationScalingFactor(double value)
	{
		_factorTableUpdateSettings.setAutomaticMemoryAllocationScalingFactor(value);
	}

	public void unsetAutomaticMemoryAllocationScalingFactor()
	{
		_factorTableUpdateSettings.unsetAutomaticMemoryAllocationScalingFactor();
	}

	public double getAutomaticExecutionTimeScalingFactor()
	{
		return _factorTableUpdateSettings.getAutomaticExecutionTimeScalingFactor();
	}

	public void setAutomaticExecutionTimeScalingFactor(double value)
	{
		_factorTableUpdateSettings.setAutomaticExecutionTimeScalingFactor(value);
	}

	public void unsetAutomaticExecutionTimeScalingFactor()
	{
		_factorTableUpdateSettings.unsetAutomaticExecutionTimeScalingFactor();
	}
}
