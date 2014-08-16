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

package com.analog.lyric.dimple.factorfunctions.core;

import java.util.BitSet;
import java.util.Random;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.model.values.Value;
import org.eclipse.jdt.annotation.Nullable;

@NotThreadSafe
public abstract class FactorTableBase implements IFactorTableBase, IFactorTable
{
	/*--------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private JointDomainIndexer _domains;
	
	/*--------------
	 * Construction
	 */
	
	protected FactorTableBase(JointDomainIndexer domains)
	{
		_domains = domains;
	}
	
	protected FactorTableBase(@Nullable BitSet directedTo, DiscreteDomain ... domains)
	{
		_domains = JointDomainIndexer.create(directedTo, domains);
	}
	
	protected FactorTableBase(FactorTableBase that)
	{
		_domains = that._domains;
	}
	
	/*----------------
	 * Object methods
	 */

	@Override
	public abstract FactorTableBase clone();
	
	/*------------------
	 * Iterable methods
	 */
	
	@Override
	public IFactorTableIterator iterator()
	{
		return new FactorTableIterator(this, false);
	}
	
	@Override
	public FactorTableIterator fullIterator()
	{
		return new FactorTableIterator(this, true);
	}
	
	/*--------------------------
	 * IFactorTableBase methods
	 */

	@Override
	public double density()
	{
		return (double)countNonZeroWeights() / (double)jointSize();
	}
	
	@Override
	public final int getDimensions()
	{
		return _domains.size();
	}
	
	@Override
	public final JointDomainIndexer getDomainIndexer()
	{
		return _domains;
	}
	
	protected final void setDomainIndexer(JointDomainIndexer newDomains)
	{
		assert(_domains.domainsEqual(newDomains));
		_domains = newDomains;
	}
	
	@Override
	public double getEnergyForElements(Object ... elements)
	{
		return getEnergyForJointIndex(_domains.jointIndexFromElements(elements));
	}
	
	@Override
	public double getWeightForElements(Object ... elements)
	{
		return getWeightForJointIndex(_domains.jointIndexFromElements(elements));
	}
	
	@Override
	public final @Nullable BitSet getInputSet()
	{
		return _domains.getInputSet();
	}
	
	@Override
	public final @Nullable BitSet getOutputSet()
	{
		return _domains.getOutputSet();
	}
	
	@Override
	public double getEnergyForIndices(int ... indices)
	{
		return getEnergyForJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public double getWeightForIndices(int ... indices)
	{
		return getWeightForJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public double getEnergyForValues(Value ... values)
	{
		return getEnergyForJointIndex(_domains.jointIndexFromValues(values));
	}
	
	@Override
	public double getWeightForValues(Value ... values)
	{
		return getWeightForJointIndex(_domains.jointIndexFromValues(values));
	}
	
	@Override
	public IFactorTable createTableConditionedOn(int[] valueIndices)
	{
		JointDomainReindexer conditioner = JointDomainReindexer.createConditioner(getDomainIndexer(), valueIndices);
		return convert(conditioner);
	}
	
	@Override
	public int sparseIndexFromElements(Object ... elements)
	{
		return sparseIndexFromJointIndex(_domains.jointIndexFromElements(elements));
	}
	
	@Override
	public int sparseIndexFromIndices(int ... indices)
	{
		return sparseIndexFromJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public int sparseIndexFromValues(Value ... values)
	{
		return sparseIndexFromJointIndex(_domains.jointIndexFromValues(values));
	}
	
	@Override
	public Object[] sparseIndexToElements(int sparseIndex, @Nullable Object[] elements)
	{
		return _domains.jointIndexToElements(sparseIndexToJointIndex(sparseIndex), elements);
	}
	
	@Override
	public int[] sparseIndexToIndices(int sparseIndex, @Nullable int[] indices)
	{
		return _domains.jointIndexToIndices(sparseIndexToJointIndex(sparseIndex), indices);
	}
	
	@Override
	public final int[] sparseIndexToIndices(int sparseIndex)
	{
		return sparseIndexToIndices(sparseIndex, null);
	}
	
	@Override
	public boolean isDirected()
	{
		return _domains.isDirected();
	}

	@Override
	public int jointSize()
	{
		return _domains.getCardinality();
	}
	
	@Override
	public void setEnergyForElements(double energy, Object ... elements)
	{
		setEnergyForJointIndex(energy, _domains.jointIndexFromElements(elements));
	}

	@Override
	public void setEnergyForIndices(double energy, int ... indices)
	{
		_domains.validateIndices(indices);
		setEnergyForJointIndex(energy, _domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public void setEnergyForValues(double energy, Value ... values)
	{
		_domains.validateValues(values);
		setEnergyForJointIndex(energy, _domains.jointIndexFromValues(values));
	}
	
	@Override
	public void setWeightForElements(double weight, Object ... elements)
	{
		setWeightForJointIndex(weight, _domains.jointIndexFromElements(elements));
	}

	@Override
	public void setWeightForIndices(double weight, int ... indices)
	{
		_domains.validateIndices(indices);
		setWeightForJointIndex(weight, _domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public void setWeightForValues(double energy, Value ... values)
	{
		_domains.validateValues(values);
		setWeightForJointIndex(energy, _domains.jointIndexFromValues(values));
	}
	
	@Override
	public final boolean supportsJointIndexing()
	{
		return _domains.supportsJointIndexing();
	}
	
	/*-----------------------
	 * IFactorTable methods
	 */
	
	@Override
	public void randomizeWeights(Random rand)
	{
		if (hasDenseRepresentation())
		{
			for (int i = jointSize(); --i >= 0;)
			{
				// nextDouble() produces range [0,1). Subtract that from 1.0 to get (0,1].
				setWeightForJointIndex(1.0 - rand.nextDouble(), i);
			}
		}
		else
		{
			for (int i = sparseSize(); --i >= 0;)
			{
				setWeightForSparseIndex(1.0 - rand.nextDouble(), i);
			}
		}
	}

}