package com.analog.lyric.dimple.solvers.gibbs;

import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.collect.IKeyed;
import com.analog.lyric.dimple.solvers.gibbs.sample.IndexedSample;
import com.analog.lyric.dimple.solvers.gibbs.sample.ObjectSample;

@NotThreadSafe
public final class SFactorUpdate implements IKeyed<ISolverFactorGibbs>
{
	/*-------
	 * State
	 */
	
	private final ISolverFactorGibbs _sfactor;
	private Set<IndexedSample> _updates;
	private final int _incrementalUpdateThreshold;

	/*--------------
	 * Construction
	 */
	
	SFactorUpdate(ISolverFactorGibbs sfactor)
	{
		_sfactor = sfactor;
		_incrementalUpdateThreshold = sfactor.getModelObject().getFactorFunction().updateDeterministicLimit();
		_updates = _incrementalUpdateThreshold > 0 ? new HashSet<IndexedSample>() : null;
	}
	
	/*----------------
	 * IKeyed methods
	 */
	
	@Override
	public final ISolverFactorGibbs getKey()
	{
		return _sfactor;
	}

	/*-----------------------
	 * SFactorUpdate methods
	 */
	
	void addVariableUpdate(int variableIndex, ObjectSample oldValue)
	{
		if (_updates != null)
		{
			_updates.add(new IndexedSample(variableIndex, oldValue));
			if (_updates.size() > _incrementalUpdateThreshold)
			{
				// Once we have exceeded the threshold, there is no point in
				// saving entries.
				_updates = null;
			}
		}
	}
	
	void performUpdate()
	{
		_sfactor.updateNeighborVariableValuesNow(_updates);
	}
	
	ISolverFactorGibbs sfactor()
	{
		return _sfactor;
	}
}
