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

import java.io.Serializable;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holds the information for one factor table entry from a {@link IFactorTableIterator}
 */
@Immutable
public final class FactorTableEntry implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final JointDomainIndexer _domains;
	private final int _sparseIndex;
	private final int _jointIndex;
	private final @Nullable int[] _jointIndices;
	private final double _energy;
	private final double _weight;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @since 0.05
	 */
	public FactorTableEntry(JointDomainIndexer domains, int sparseIndex, int jointIndex, double energy, double weight)
	{
		_domains = domains;
		_sparseIndex = sparseIndex;
		_jointIndex = jointIndex;
		_jointIndices = null;
		_energy = energy;
		_weight = weight;
	}
	
	/**
	 * @since 0.05
	 */
	public FactorTableEntry(JointDomainIndexer domains, int sparseIndex, @Nullable int[] jointIndices, double energy,
		double weight)
	{
		_domains = domains;
		_sparseIndex = sparseIndex;
		_jointIndex = -1;
		_jointIndices = jointIndices;
		_energy = energy;
		_weight = weight;
	}

	/*---------
	 * Methods
	 */
	
	public JointDomainIndexer domains()
	{
		return _domains;
	}
	
	/**
	 * The energy value for this table entry.
	 * <p>
	 * Same as the negative log of the {@link #weight}.
	 */
	public double energy()
	{
		return _energy;
	}

	/**
	 * Returns domain indices corresponding to this table entry in newly allocated array.
	 * @see #indices(int[])
	 * @see #values()
	 */
	public int[] indices()
	{
		return indices(null);
	}
	
	/**
	 * Returns domain indices corresponding to this table entry, using provided
	 * {@code indices} array if it is non-null and of the correct length.
	 * 
	 * @see #indices()
	 * @see #values(Object[])
	 */
	public int[] indices(@Nullable int[] indices)
	{
		indices = _domains.allocateIndices(indices);
		final int[] jointIndices = _jointIndices;
		if (jointIndices != null)
		{
			System.arraycopy(jointIndices, 0, indices, 0, jointIndices.length);
		}
		else
		{
			_domains.jointIndexToIndices(_jointIndex, indices);
		}
		return indices;
	}
	
	/**
	 * Returns joint index of entry if available or else -1.
	 */
	public int jointIndex()
	{
		return _jointIndex;
	}

	public int sparseIndex()
	{
		return _sparseIndex;
	}

	public Object[] values()
	{
		return values(null);
	}
	
	public <T> T[] values(@Nullable T[] elements)
	{
		elements = _domains.allocateElements(elements);
		final int[] jointIndices = _jointIndices;
		if (jointIndices != null)
		{
			_domains.elementsFromIndices(jointIndices, elements);
		}
		else
		{
			_domains.jointIndexToElements(_jointIndex, elements);
		}
		return elements;
	}
	
	/**
	 * The weight value for this entry.
	 * @see #energy
	 */
	public double weight()
	{
		return _weight;
	}
}