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

package com.analog.lyric.dimple.model.domains;

import static java.util.Objects.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.Immutable;
import cern.colt.list.IntArrayList;

import org.eclipse.jdt.annotation.Nullable;

/**
 * 
 * @since 0.05
 * @author Christopher Barber
 */
@Immutable
public final class JointDomainIndexConditioner extends JointDomainReindexer
{
	private final int _hashCode;
	private final int[] _conditionedValues;
	private final int _addedJointIndex;
	private final JointDomainIndexConditioner _inverse;
	private final boolean _supportsJointIndexing;
	
	/*--------------
	 * Construction
	 */
	
	private JointDomainIndexConditioner(
		JointDomainIndexer fromDomains,
		@Nullable JointDomainIndexer addedDomains,
		JointDomainIndexer toDomains,
		@Nullable JointDomainIndexer removedDomains,
		int[] conditionedValues,
		@Nullable JointDomainIndexConditioner inverse)
	{
		super(fromDomains, addedDomains, toDomains, removedDomains);
		_conditionedValues = conditionedValues.clone();
		_hashCode = computeHashCode();
		
		int addedJointIndex = 0;
		if (addedDomains != null)
		{
			_supportsJointIndexing = toDomains.supportsJointIndexing();
			if (_supportsJointIndexing)
			{
				final int addedIndex = addedDomains.jointIndexFromIndices(conditionedValues);
				final int fromCardinality = fromDomains.getCardinality();
				addedJointIndex = addedIndex * fromCardinality;
			}
		}
		else
		{
			_supportsJointIndexing = fromDomains.supportsJointIndexing();
			if (_supportsJointIndexing)
			{
				final int toCardinality = toDomains.getCardinality();
				addedJointIndex =
					-requireNonNull(removedDomains).jointIndexFromIndices(conditionedValues) * toCardinality;
			}
		}
		
		_addedJointIndex = addedJointIndex;
		
		if (inverse == null)
		{
			inverse = new JointDomainIndexConditioner(toDomains, removedDomains,
				fromDomains, addedDomains, conditionedValues, this);
		}
		_inverse = inverse;
	}
	
	static JointDomainIndexConditioner _createConditioner(JointDomainIndexer fromDomains, int[] conditionedValues)
	{
		final int nConditioned = conditionedValues.length;
		final int toSize = fromDomains.size() - nConditioned;
		final JointDomainIndexer toDomains = fromDomains.subindexer(0, toSize);
		final JointDomainIndexer removedDomains = fromDomains.subindexer(toSize, nConditioned);

		return new JointDomainIndexConditioner(fromDomains, null, toDomains, removedDomains, conditionedValues, null);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof JointDomainIndexConditioner)
		{
			JointDomainIndexConditioner that = (JointDomainIndexConditioner)other;
			return _addedJointIndex == that._addedJointIndex &&
				Arrays.equals(_conditionedValues, that._conditionedValues) &&
				super.equals(that);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return _hashCode;
	}
	
	/*------------------------------
	 * JointDomainReindexer methods
	 */
	
	@Override
	public JointDomainIndexConditioner getInverse()
	{
		return _inverse;
	}

	@Override
	public void convertIndices(Indices indices)
	{
		final int[] fromIndices = indices.fromIndices;
		final int[] toIndices = indices.toIndices;
		final int fromLength = indices.fromIndices.length;
		final int toLength = toIndices.length;
		
		if (fromLength < toLength)
		{
			System.arraycopy(fromIndices, 0, toIndices, 0, fromLength);
			System.arraycopy(_conditionedValues, 0, toIndices, fromLength, _conditionedValues.length);
		}
		else
		{
			final int[] removedIndices = indices.removedIndices;
			System.arraycopy(indices.fromIndices, indices.toIndices.length, removedIndices, 0, removedIndices.length);
			if (Arrays.equals(_conditionedValues, removedIndices))
			{
				System.arraycopy(fromIndices, 0, toIndices, 0, toLength);
			}
			else
			{
				Arrays.fill(toIndices, -1);
			}
		}
	}
	
	@Override
	public double[] convertDenseEnergies(double[] oldEnergies)
	{
		final double[] values = new double[_toDomains.getCardinality()];
		Arrays.fill(values, Double.POSITIVE_INFINITY);
		return convertDenseValues(oldEnergies, values);
	}

	@Override
	public double[] convertDenseWeights(double[] oldWeights)
	{
		return convertDenseValues(oldWeights, new double[_toDomains.getCardinality()]);
	}
	
	private double[] convertDenseValues(double[] oldValues, double[] newValues)
	{
		assert(_supportsJointIndexing);
		if (_addedDomains != null)
		{
			for (int fromji = oldValues.length; --fromji>=0;)
			{
				final int toji = fromji + _addedJointIndex;
				newValues[toji] = oldValues[fromji];
			}
		}
		else
		{
			assert(_addedJointIndex <= 0);
			for (int toji = newValues.length; --toji>=0;)
			{
				final int fromji = toji - _addedJointIndex;
				newValues[toji] = oldValues[fromji];
			}
		}
		
		return newValues;
	}
	
	@Override
	public int convertJointIndex(int oldJointIndex, int addedJointIndex, @Nullable AtomicInteger removedJointIndex)
	{
		assert(_supportsJointIndexing);
		if (_addedDomains != null)
		{
			final int fromCardinality = _fromDomains.getCardinality();
			if (_addedJointIndex == addedJointIndex * fromCardinality)
			{
				return oldJointIndex + _addedJointIndex;
			}
		}
		else
		{
			final int toCardinality = _toDomains.getCardinality();
			final int newJointIndex = oldJointIndex + _addedJointIndex;
			
			if (0 <= newJointIndex && newJointIndex < toCardinality)
			{
				if (removedJointIndex != null)
				{
					removedJointIndex.set(oldJointIndex / toCardinality);
				}
				return newJointIndex;
			}
		}

		return -1;
	}
	
	@Override
	public double[] convertSparseEnergies(
		double[] oldEnergies, int[] oldSparseIndexToJointIndex, int[] sparseIndexToJointIndex)
	{
		final double[] newEnergies = new double[sparseIndexToJointIndex.length];
		Arrays.fill(newEnergies, Double.POSITIVE_INFINITY);
		
		return convertSparseValues(oldEnergies, oldSparseIndexToJointIndex, sparseIndexToJointIndex, newEnergies);
	}
	
	private double[] convertSparseValues(
		double[] oldValues, int[] oldSparseIndexToJointIndex, int[] sparseIndexToJointIndex, double[] newValues)
	{
		final int size = newValues.length;
		
		if (_addedDomains != null)
		{
			// Because we are adding new fixed values to the end of the domains, it does not
			// change the number or order of the existing values, so we can simply copy them.
			System.arraycopy(oldValues, 0, newValues, 0, size);
		}
		else
		{
			assert(_addedJointIndex < 0);
			final int oldsize = oldSparseIndexToJointIndex.length;
			
			int oldsi = -1;
			for (int newsi = 0; newsi < size; ++newsi)
			{
				final int newji = sparseIndexToJointIndex[newsi];
				final int oldji = newji - _addedJointIndex;
				oldsi = Arrays.binarySearch(oldSparseIndexToJointIndex, oldsi + 1, oldsize, oldji);
				newValues[newsi] = oldValues[oldsi];
			}
		}

		return newValues;
	}
	
	@Override
	public int[] convertSparseToJointIndex(int[] oldSparseToJointIndex)
	{
		assert(hasFastJointIndexConversion());
		
		final int size = oldSparseToJointIndex.length;

		if (_addedDomains != null)
		{
			// Will have same number of entries as the old but with new index values for the added
			// fixed dimensions.
			int[] sparseToJointIndex = new int[size];
			for (int i = size; --i>=0;)
			{
				sparseToJointIndex[i] = oldSparseToJointIndex[i] + _addedJointIndex;
			}
			return sparseToJointIndex;
		}
		else
		{
			final int toCardinality = _toDomains.getCardinality();
			final IntArrayList sparseToJointIndex = new IntArrayList(size);
			for (int oldji : oldSparseToJointIndex)
			{
				final int newji = oldji + _addedJointIndex;
				if (0 <= newji && newji < toCardinality)
				{
					sparseToJointIndex.add(newji);
				}
			}
			sparseToJointIndex.trimToSize();
			return sparseToJointIndex.elements();
		}
	}
	
	@Override
	public double[] convertSparseWeights(
		double[] oldWeights, int[] oldSparseIndexToJointIndex, int[] sparseIndexToJointIndex)
	{
		final double[] newWeights = new double[sparseIndexToJointIndex.length];
		return convertSparseValues(oldWeights, oldSparseIndexToJointIndex, sparseIndexToJointIndex, newWeights);
	}

	@Override
	public boolean hasFastJointIndexConversion()
	{
		return _supportsJointIndexing;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return true
	 */
	@Override
	protected boolean maintainsJointIndexOrder()
	{
		return true;
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	@Override
	protected int computeHashCode()
	{
		return super.computeHashCode() * 17 + Arrays.hashCode(_conditionedValues);
	}

	/*-----------------
	 * Private methods
	 */

}
