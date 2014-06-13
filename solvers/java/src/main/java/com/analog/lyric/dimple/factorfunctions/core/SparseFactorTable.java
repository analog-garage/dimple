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

import static com.analog.lyric.math.Utilities.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.DiscreteIndicesIterator;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer.Indices;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.util.misc.NonNull;
import com.analog.lyric.util.misc.NonNullByDefault;
import com.analog.lyric.util.misc.Nullable;
import com.google.common.math.DoubleMath;

/**
 * @since 0.05
 * @author Christopher Barber
 */
@NotThreadSafe
public class SparseFactorTable extends SparseFactorTableBase implements IFactorTable
{
	/*-----------
	 * Constants
	 */

	private static final long serialVersionUID = 1L;
	
	/*-------
	 * State
	 */
	
	private IndexEntryComparator _entryComparator;
	
	@NotThreadSafe
	private static class IndexEntry implements Serializable
	{
		private static final long serialVersionUID = 1L;

		int[] _indices;
		int _sparseIndex;
		
		private IndexEntry(int[] indices, int sparseIndex)
		{
			_indices = indices;
			_sparseIndex = sparseIndex;
		}
		
		@Override
		protected IndexEntry clone()
		{
			return new IndexEntry(this._indices, this._sparseIndex);
		}

		@Override
		public boolean equals(@Nullable Object that)
		{
			return that instanceof IndexEntry && Arrays.equals(this._indices, ((IndexEntry)that)._indices);
		}
		
		@Override
		public int hashCode()
		{
			return Arrays.hashCode(_indices);
		}
		
		/**
		 * Just print the indices as a debugging aid.
		 */
		@Override
		public String toString()
		{
			return Arrays.toString(_indices);
		}
	}
	
	@NotThreadSafe
	private static class IndexEntryWithWeight extends IndexEntry
	{
		private static final long serialVersionUID = 1L;
		
		private double _weight;
		
		private IndexEntryWithWeight(int[] indices, int sparseIndex, double weight)
		{
			super(indices, sparseIndex);
			_weight = weight;
		}
	}
	
	@Immutable
	@NonNullByDefault(false)
	private final static class IndexEntryComparator implements Comparator<IndexEntry>, Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Comparator<int[]> _indicesComparator;
		
		IndexEntryComparator(@NonNull JointDomainIndexer domains)
		{
			_indicesComparator = domains.getIndicesComparator();
		}
		
		@Override
		public int compare(IndexEntry entry1, IndexEntry entry2)
		{
			return _indicesComparator.compare(entry1._indices,  entry2._indices);
		}
	}
	
	private IndexEntry[] _indexArray = new IndexEntry[0];
	
	private final Map<IndexEntry,IndexEntry> _indexSet;

	private IndexEntry _scratchEntry = new IndexEntry(ArrayUtil.EMPTY_INT_ARRAY, -1);
	private final int[] _scratchIndices;
	
	/*--------------
	 * Construction
	 */
	
	SparseFactorTable(final JointDomainIndexer domains)
	{
		super(domains);
		_representation = FactorTable.SPARSE_ENERGY;
		_scratchIndices = domains.allocateIndices(null);
		_indexSet = indexMapForDomains(domains, 8);
		_entryComparator = new IndexEntryComparator(domains);
	}
	
	SparseFactorTable(SparseFactorTable that)
	{
		super(that);
		
		_indexArray = new IndexEntry[that._indexArray.length];
		for (int i = _indexArray.length; --i>=-0;)
		{
			_indexArray[i] = that._indexArray[i].clone();
		}
		JointDomainIndexer domains = getDomainIndexer();
		_indexSet = indexMapForDomains(domains, _indexArray.length);
		for (IndexEntry entry : _indexArray)
		{
			_indexSet.put(entry, entry);
		}
		_scratchIndices = domains.allocateIndices(null);
		_entryComparator = new IndexEntryComparator(domains);
	}
	
	SparseFactorTable(IFactorTable other, JointDomainReindexer converter)
	{
		this(other, converter, other.getRepresentation());
	}
	
	SparseFactorTable(IFactorTable other, JointDomainReindexer converter, FactorTableRepresentation representation)
	{
		this(converter.getToDomains());
		
		final FactorTableRepresentation otherRep = other.getRepresentation();

		// Start out with just sparse weights, and convert below if necessary
		_representation = FactorTable.SPARSE_WEIGHT;
		int curRep = 0;
		if (representation.hasWeight() || representation.isDeterministic())
		{
			curRep = FactorTable.SPARSE_WEIGHT;
		}
		if (representation.hasEnergy())
		{
			curRep |= FactorTable.SPARSE_ENERGY;
		}
		if (representation.hasSparseIndices())
		{
			curRep |= FactorTable.SPARSE_INDICES;
		}
		
		Indices scratch = converter.getScratch();

		// Note: this may change the representation of 'other', so we will change it back below.
		double[] otherWeights = other.getWeightsSparseUnsafe();
		
		int oldSparseSize = other.sparseSize();
			
		final DiscreteIndicesIterator addedIterator =
			scratch.addedIndices.length > 0 ?
				new DiscreteIndicesIterator(Objects.requireNonNull(converter.getAddedDomains()), scratch.addedIndices) :
				new DiscreteIndicesIterator(ArrayUtil.EMPTY_INT_ARRAY, scratch.addedIndices);

		for (int i = 0; i < oldSparseSize; ++i)
		{
			other.sparseIndexToIndices(i, scratch.fromIndices);
			double weight = otherWeights[i];
			while (addedIterator.hasNext())
			{
				addedIterator.next();
				converter.convertIndices(scratch);
				if (scratch.toIndices[0] >= 0)
				{
					IndexEntryWithWeight entry = new IndexEntryWithWeight(scratch.toIndices.clone(), i, weight);
					IndexEntryWithWeight prevEntry = (IndexEntryWithWeight)_indexSet.put(entry, entry);
					if (prevEntry != null)
					{
						prevEntry._weight = weight;
					}
				}
			}
			addedIterator.reset();
		}

		scratch.release();

		int size = _indexSet.size();
		_indexArray = _indexSet.values().toArray(new IndexEntry[size]);
		Arrays.sort(_indexArray, _entryComparator);

		double[] weights = new double[size];
		for (int i = 0; i < size; ++i)
		{
			IndexEntryWithWeight entry = ((IndexEntryWithWeight)_indexArray[i]);
			double weight = entry._weight;
			if (weight != 0.0)
			{
				++_nonZeroWeights;
			}
			weights[i] = weight;
			IndexEntry newEntry = new IndexEntry(entry._indices, i);
			_indexArray[i] = newEntry;
			_indexSet.put(newEntry, newEntry);
		}

		_sparseWeights = weights;
		
		other.setRepresentation(otherRep);
		setRepresentation(curRep);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public SparseFactorTable clone()
	{
		return new SparseFactorTable(this);
	}
	
	/*------------------
	 * Iterable methods
	 */
	
	@Override
	public IFactorTableIterator iterator()
	{
		return new SparseFactorTableIterator(this);
	}
	
	@Override
	public FactorTableIterator fullIterator()
	{
		throw notDense("fullIterator");
	}
	
	/*--------------------------
	 * IFactorTableBase methods
	 */

	@Override
	public double density()
	{
		double d = _nonZeroWeights;
		
		for (DiscreteDomain domain : getDomainIndexer())
		{
			d /= domain.size();
		}
		
		return d;
	}

	@Override
	public void evalDeterministic(Object[] arguments)
	{
		throw notDeterministic("evalDeterministic");
	}

	@Override
	public double getEnergyForElements(Object ... elements)
	{
		return getEnergyForIndices(getDomainIndexer().elementsToIndices(elements, _scratchIndices));
	}

	@Override
	public double getEnergyForIndices(int ... indices)
	{
		IndexEntry entry = _indexSet.get(getScratchEntry(indices));
		return entry != null ? getEnergyForSparseIndex(entry._sparseIndex) : Double.POSITIVE_INFINITY;
	}

	@Override
	public double getEnergyForIndicesDense(int... indices)
	{
		throw notDense("getEnergyForIndicesDense");
	}
	
	@Override
	public double getEnergyForValuesDense(Value ... values)
	{
		throw notDense("getEnergyForValuesDense");
	}
	
	@Override
	public double getWeightForIndicesDense(int... indices)
	{
		throw notDense("getWeightForIndicesDense");
	}

	@Override
	public double getWeightForValuesDense(Value ... values)
	{
		throw notDense("getWeightForValuesDense");
	}

	@Override
	public double getEnergyForJointIndex(int jointIndex)
	{
		throw notDense("getEnergyForJointIndex");
	}

	@Override
	public double getEnergyForSparseIndex(int sparseIndex)
	{
		return hasSparseEnergies() ? _sparseEnergies[sparseIndex] : weightToEnergy(_sparseWeights[sparseIndex]);
	}

	@Override
	public double getWeightForElements(Object ... elements)
	{
		return getWeightForIndices(getDomainIndexer().elementsToIndices(elements, _scratchIndices));
	}

	@Override
	public double getWeightForIndices(int ... indices)
	{
		IndexEntry entry = _indexSet.get(getScratchEntry(indices));
		return entry != null ? getWeightForSparseIndex(entry._sparseIndex) : 0.0;
	}

	@Override
	public double getWeightForJointIndex(int jointIndex)
	{
		throw notDense("getWeightForJointIndex");
	}

	@Override
	public double getWeightForSparseIndex(int sparseIndex)
	{
		return hasSparseWeights() ? _sparseWeights[sparseIndex] : energyToWeight(_sparseEnergies[sparseIndex]);
	}

	@Override
	public boolean hasDenseRepresentation()
	{
		return false;
	}

	@Override
	public boolean hasDenseEnergies()
	{
		return false;
	}

	@Override
	public boolean hasDenseWeights()
	{
		return false;
	}
	
	@Override
	public boolean hasMaximumDensity()
	{
		// Because this class is only intended to be used when the joint cardinality is larger than
		// 2^31, and is not designed to hold more than that many elements, it should not be possible
		// for this to ever be true.
		return false;
	}

	@Override
	public boolean hasSparseRepresentation()
	{
		return true;
	}

	@Override
	public boolean isDeterministicDirected()
	{
		return false;
	}

	@Override
	public boolean isConditional()
	{
		if ((_computedMask & CONDITIONAL_COMPUTED) == 0)
		{
			if (isDirected())
			{
				normalizeDirected(true);
			}
			_computedMask |= CONDITIONAL_COMPUTED;
		}
		return (_computedMask & CONDITIONAL) != 0;
	}

	
	@Override
	public void setEnergyForElements(double energy, Object ... elements)
	{
		setEnergyForIndices(energy, getDomainIndexer().elementsToIndices(elements, _scratchIndices));
	}

	@Override
	public void setEnergyForIndices(double energy, int ... indices)
	{
		getDomainIndexer().validateIndices(indices);
		setEnergyForSparseIndex(energy, createSparseIndexForIndices(indices));
	}

	@Override
	public void setWeightForElements(double weight, Object ... elements)
	{
		setWeightForIndices(weight, getDomainIndexer().elementsToIndices(elements, _scratchIndices));
	}

	@Override
	public void setWeightForIndices(double weight, int ... indices)
	{
		getDomainIndexer().validateIndices(indices);
		setWeightForSparseIndex(weight, createSparseIndexForIndices(indices));
	}
	
	@Override
	public int sparseIndexFromElements(Object ... elements)
	{
		return sparseIndexFromIndices(getDomainIndexer().elementsToIndices(elements,  _scratchIndices));
	}
	
	@Override
	public int sparseIndexFromIndices(int ... indices)
	{
		IndexEntry entry = _indexSet.get(getScratchEntry(indices));
		return entry != null ? entry._sparseIndex : -1;
	}
	
	@Override
	public Object[] sparseIndexToElements(int sparseIndex, @Nullable Object[] elements)
	{
		return getDomainIndexer().elementsFromIndices(sparseIndexToIndices(sparseIndex, _scratchIndices), elements);
	}
	
	@Override
	public int[] sparseIndexToIndices(int sparseIndex, @Nullable int[] indices)
	{
		indices = getDomainIndexer().allocateIndices(indices);
		System.arraycopy(_indexArray[sparseIndex]._indices, 0, indices,  0, indices.length);
		return indices;
	}
	
	/**
	 * Like {@link #sparseIndexToIndices(int)} but returns actual internal indices array (which must not
	 * be modified!).
	 */
	int[] sparseIndexToIndicesUnsafe(int sparseIndex)
	{
		return _indexArray[sparseIndex]._indices;
	}
	
	@Override
	public int sparseIndexFromJointIndex(int joint)
	{
		throw notDense("sparseIndexFromJointIndex");
	}

	@Override
	public int sparseIndexToJointIndex(int sparseIndex)
	{
		throw notDense("sparseIndexToJointIndex");
	}

	@Override
	public int sparseSize()
	{
		return _indexArray.length;
	}

	/*----------------------
	 * IFactorTable methods
	 */
	
	@Override
	public int compact()
	{
		int nRemoved = 0;
		final int curSparseSize = sparseSize();
		if (curSparseSize > _nonZeroWeights)
		{
			nRemoved = curSparseSize - _nonZeroWeights;
			
			final boolean hasEnergy = hasSparseEnergies();
			final double[] sparseEnergies = hasEnergy ? new double[_nonZeroWeights] : ArrayUtil.EMPTY_DOUBLE_ARRAY;
			
			final boolean hasWeight = hasSparseWeights();
			final double[] sparseWeights = hasWeight ? new double[_nonZeroWeights] : ArrayUtil.EMPTY_DOUBLE_ARRAY;

			final IndexEntry[] indexArray = new IndexEntry[_nonZeroWeights];
			
			if (hasWeight)
			{
				for (int i = 0, j = 0; i < curSparseSize; ++i)
				{
					IndexEntry entry = _indexArray[i];
					double w = _sparseWeights[i];
					if (w == 0.0)
					{
						_indexSet.remove(entry);
					}
					else
					{
						sparseWeights[j] = w;
						if (hasEnergy)
						{
							sparseEnergies[j] = _sparseEnergies[i];
						}
						entry._sparseIndex = j;
						indexArray[j] = entry;
						++j;
					}
				}
			}
			else
			{
				for (int i = 0, j = 0; i < curSparseSize; ++i)
				{
					IndexEntry entry = _indexArray[i];
					double e = _sparseEnergies[i];

					if (Double.isInfinite(e))
					{
						_indexSet.remove(entry);
					}
					else
					{
						sparseEnergies[j] = e;
						entry._sparseIndex = j;
						indexArray[j] = entry;
						++j;
					}
				}
			}
			
			_sparseEnergies = sparseEnergies;
			_sparseWeights = sparseWeights;
			_indexArray = indexArray;
			recomputeSparseIndices();
		}
		return nRemoved;
	}

	@Override
	public void copy(IFactorTable that)
	{
		if (that == this)
		{
			return;
		}
		
		// REFACTOR: share
		if (!getDomainIndexer().domainsEqual(that.getDomainIndexer()))
		{
			throw new DimpleException("Cannot copy from factor table with different domains");
		}
	
		if (!that.hasSparseRepresentation())
		{
			throw new DimpleException("Cannot copy to SparseFactorTable from table without sparse representation");
		}
		
		int sparseSize = that.sparseSize();
		_indexArray = new IndexEntry[sparseSize];
		_indexSet.clear();
		
		for (int si = 0; si < sparseSize; ++si)
		{
			int[] indices = that.sparseIndexToIndices(si);
			IndexEntry entry = new IndexEntry(indices, si);
			_indexArray[si] = entry;
			_indexSet.put(entry, entry);
		}
		
		_computedMask = 0;
		_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
	
		_representation = 0;
		if (that.hasSparseEnergies())
		{
			_representation |= FactorTable.SPARSE_ENERGY;
			_sparseEnergies = that.getEnergiesSparseUnsafe().clone();
		}
		if (that.hasSparseWeights())
		{
			_representation |= FactorTable.SPARSE_WEIGHT;
			_sparseWeights = that.getWeightsSparseUnsafe().clone();
		}
		if (that.hasSparseIndices())
		{
			getIndicesSparseUnsafe();
		}
		_nonZeroWeights = that.countNonZeroWeights();
	}

	@Override
	public IFactorTable joinVariablesAndCreateNewTable(int[] varIndices,
		int[] indexToJointIndex,
		DiscreteDomain[] allDomains,
		DiscreteDomain jointDomain)
	{
		final JointDomainIndexer domains = getDomainIndexer();
		final JointDomainReindexer converter =
			FactorTable.makeConverterForJoinVariables(domains, varIndices, indexToJointIndex, allDomains, jointDomain);
		return new SparseFactorTable(this, converter);
	}

	@Override
	public boolean hasDeterministicRepresentation()
	{
		return false;
	}

	@Override
	public double[] getEnergiesSparseUnsafe()
	{
		if (_sparseEnergies.length == 0 && !hasSparseEnergies())
		{
			setRepresentation(_representation | FactorTable.SPARSE_ENERGY);
		}
		return _sparseEnergies;
	}

	@Override
	public double[] getEnergySlice(@Nullable double[] slice, int sliceDimension, int... indices)
	{
		final int[] scratchIndices = _scratchIndices;
		System.arraycopy(indices, 0, scratchIndices, 0, scratchIndices.length);
		
		return getEnergySliceImpl(slice, sliceDimension, scratchIndices);
	}
	
	@Override
	public double[] getEnergySlice(@Nullable double[] slice, int sliceDimension, Value ... values)
	{
		final int[] scratchIndices = _scratchIndices;
		for (int i = scratchIndices.length; --i>=0;)
		{
			scratchIndices[i] = values[i].getIndex();
		}
		
		return getEnergySliceImpl(slice, sliceDimension, scratchIndices);
	}
	
	private double[] getEnergySliceImpl(@Nullable double[] slice, int sliceDimension, int[] scratchIndices)
	{
		JointDomainIndexer indexer = getDomainIndexer();
		int size = indexer.getDomainSize(sliceDimension);
		
		if (slice == null || slice.length < size)
		{
			slice = new double[size];
		}

		for (int i = 0; i < size; ++i)
		{
			scratchIndices[sliceDimension] = i;
			slice[i] = getEnergyForIndices(scratchIndices);
		}
		
		return slice;
	}

	@Override
	public int[][] getIndicesSparseUnsafe()
	{
		if (!hasSparseIndices())
		{
			_representation |= FactorTable.SPARSE_INDICES;
			recomputeSparseIndices();
		}
		return _sparseIndices;
	}

	@Override
	public double[] getWeightsSparseUnsafe()
	{
		if (_sparseWeights.length == 0 && !hasSparseWeights())
		{
			setRepresentation(_representation | FactorTable.SPARSE_WEIGHT);
		}
		return _sparseWeights;
	}

	@Override
	public double[] getWeightSlice(@Nullable double[] slice, int sliceDimension, int... indices)
	{
		final int[] scratchIndices = _scratchIndices;
		System.arraycopy(indices, 0, scratchIndices, 0, scratchIndices.length);
		
		return getWeightSliceImpl(slice, sliceDimension, scratchIndices);
	}
	
	@Override
	public double[] getWeightSlice(@Nullable double[] slice, int sliceDimension, Value ... values)
	{
		final int[] scratchIndices = _scratchIndices;
		for (int i = scratchIndices.length; --i>=0;)
		{
			scratchIndices[i] = values[i].getIndex();
		}
		
		return getWeightSliceImpl(slice, sliceDimension, scratchIndices);
	}
	
	private double[] getWeightSliceImpl(@Nullable double[] slice, int sliceDimension, int[] scratchIndices)
	{
		JointDomainIndexer indexer = getDomainIndexer();
		int size = indexer.getDomainSize(sliceDimension);
		
		if (slice == null || slice.length < size)
		{
			slice = new double[size];
		}

		for (int i = 0; i < size; ++i)
		{
			scratchIndices[sliceDimension] = i;
			slice[i] = getWeightForIndices(scratchIndices);
		}
		
		return slice;
	}

	@Override
	public void setEnergiesDense(double[] energies)
	{
		throw notDense("setEnergiesDense");
	}

	@Override
	public void setWeightsDense(double[] weights)
	{
		throw notDense("setWeightsDense");
	}

	@Override
	public void serializeToXML(String serializeName, String targetDirectory)
	{
		throw DimpleException.unsupportedMethod(getClass(), "serializeToXML");
	}

	@Override
	public void setDeterministicOutputIndices(int[] outputIndices)
	{
		throw notDeterministic("setDeterministicOutputIndices");
	}

	@Override
	public void setEnergyForJointIndex(double energy, int jointIndex)
	{
		throw notDense("setEnergyForJointIndex");
	}

	@Override
	public void setEnergyForSparseIndex(double energy, int sparseIndex)
	{
		final double prevEnergy = getEnergyForSparseIndex(sparseIndex);
		if (prevEnergy != energy)
		{
			_computedMask = 0;
			double weight = hasSparseWeights() ? energyToWeight(energy) : 0.0;
			setWeightEnergyForSparseIndex(weight, energy, sparseIndex);
			
			if (Double.isInfinite(prevEnergy))
			{
				++_nonZeroWeights;
			}
			else if (Double.isInfinite(energy))
			{
				--_nonZeroWeights;
			}
		}
	}

	@Override
	public void setWeightForJointIndex(double weight, int jointIndex)
	{
		throw notDense("setWeightForJointIndex");
	}

	@Override
	public void setWeightForSparseIndex(double weight, int sparseIndex)
	{
		final double prevWeight = getWeightForSparseIndex(sparseIndex);
		if (prevWeight != weight)
		{
			_computedMask = 0;
			double energy = hasSparseEnergies() ? weightToEnergy(weight) : Double.POSITIVE_INFINITY;
			setWeightEnergyForSparseIndex(weight, energy, sparseIndex);
			
			if (prevWeight == 0.0)
			{
				++_nonZeroWeights;
			}
			else if (weight == 0.0)
			{
				--_nonZeroWeights;
			}
		}
	}

	@Override
	public void setEnergiesSparse(int[] jointIndices, double[] energies)
	{
		throw notDense("setEnergiesSparse(int[] jointIndices, double[])");
	}

	@Override
	public void setWeightsSparse(int[] jointIndices, double[] weights)
	{
		throw notDense("setWeightsSparse(int[] jointIndices, double[])");
	}

	/*-----------------
	 * Private methods
	 */
	
	private static Map<IndexEntry,IndexEntry> indexMapForDomains(JointDomainIndexer domains, int capacity)
	{
		// TODO: it is possible that for a large domains list with small fixed domain sizes,
		// it might better to use a radix tree, especially when all of the domains are binary.
		return new HashMap<IndexEntry,IndexEntry>(capacity);
	}
	
	private int createSparseIndexForIndices(int[] indices)
	{
		IndexEntry scratchEntry = getScratchEntry(indices);
		IndexEntry entry = _indexSet.get(scratchEntry);
		if (entry != null)
		{
			return entry._sparseIndex;
		}
		
		// Need to insert a new sparse index.
		
		indices = Arrays.copyOf(indices, indices.length);
		
		// Find position by doing a binary search in _indexArray
		int sparseIndex = -Arrays.binarySearch(_indexArray, scratchEntry, _entryComparator) - 1;
		
		entry = new IndexEntry(indices, sparseIndex);

		int newSize = _indexArray.length + 1;
		IndexEntry[] indexArray = new IndexEntry[newSize];
		double[] sparseEnergies = hasSparseEnergies() ? new double[newSize] : _sparseEnergies;
		double[] sparseWeights = hasSparseWeights() ? new double[newSize] : _sparseWeights;
		int[][] sparseIndices = hasSparseIndices() ? new int[newSize][] : _sparseIndices;
		
		if (sparseIndex > 0)
		{
			System.arraycopy(_indexArray, 0, indexArray, 0, sparseIndex);
			if (sparseEnergies.length > 0)
			{
				System.arraycopy(_sparseEnergies, 0, sparseEnergies, 0, sparseIndex);
			}
			if (sparseWeights.length > 0)
			{
				System.arraycopy(_sparseWeights, 0, sparseWeights, 0, sparseIndex);
			}
			if (sparseIndices.length > 0)
			{
				System.arraycopy(_sparseIndices, 0, sparseIndices, 0, sparseIndex);
			}
		}
		indexArray[sparseIndex] = entry;
		if (sparseEnergies.length > 0)
		{
			sparseEnergies[sparseIndex] = Double.POSITIVE_INFINITY;
		}
		// No need to initialize value for sparseWeights because default is 0.0 for new array.
		if (sparseIndices.length > 0)
		{
			sparseIndices[sparseIndex] = indices;
		}
		if (sparseIndex < _indexArray.length)
		{
			int endSize = _indexArray.length - sparseIndex;
			System.arraycopy(_indexArray, sparseIndex, indexArray, sparseIndex + 1, endSize);
			if (sparseEnergies.length > 0)
			{
				System.arraycopy(_sparseEnergies, sparseIndex, sparseEnergies, sparseIndex + 1, endSize);
			}
			if (sparseWeights.length > 0)
			{
				System.arraycopy(_sparseWeights, sparseIndex, sparseWeights, sparseIndex + 1, endSize);
			}
			if (sparseIndices.length > 0)
			{
				System.arraycopy(_sparseIndices, sparseIndex, sparseIndices, sparseIndex + 1, endSize);
			}
			for (int i = sparseIndex + 1; i < newSize; ++i)
			{
				indexArray[i]._sparseIndex = i;
			}
		}
		
		_sparseEnergies = sparseEnergies;
		_sparseWeights = sparseWeights;
		_sparseIndices = sparseIndices;
		_indexArray = indexArray;
		_indexSet.put(entry, entry);
		
		return sparseIndex;
	}

	private void computeNonZeroWeights()
	{
		int count = 0;
		if (hasSparseWeights())
		{
			for (double w : _sparseWeights)
				if (w != 0)
					++count;
		}
		else
		{
			for (double e : _sparseEnergies)
				if (!Double.isInfinite(e))
					++count;
		}
		_nonZeroWeights = count;
	}
	
	private IndexEntry getScratchEntry(int[] indices)
	{
		_scratchEntry._indices = indices;
		return _scratchEntry;
	}
	
	private DimpleException notDense(String method)
	{
		return DimpleException.unsupportedMethod(getClass(), method, "dense representation not supported.");
	}
	
	@Override
	boolean normalizeDirected(boolean justCheck)
	{
		final JointDomainIndexer domains = getDomainIndexer();
		
		boolean computeNormalizedTotal = justCheck;
		double normalizedTotal = 1.0;
		
		// We represent the joint index such that the outputs for the same
		// input are stored consecutively, so we only need to walk through
		// the values in order.
		//
		// When just checking, we allow total to equal something other than one
		// as long as they are all the same.

		final int size = _indexArray.length;
		
		for (int si = 0, nextsi = 1, start = 0; si < size; si = nextsi++)
		{

			if (nextsi == size || !domains.hasSameInputs(_indexArray[si]._indices, _indexArray[nextsi]._indices))
			{
				double totalForInput = 0.0;
				if (hasSparseWeights())
				{
					for (int i = start; i < nextsi; ++i)
					{
						totalForInput += _sparseWeights[i];
					}
				}
				else
				{
					for (int i = start; i < nextsi; ++i)
					{
						totalForInput += energyToWeight(_sparseEnergies[i]);
					}
				}
				if (totalForInput == 0.0)
				{
					return normalizeDirectedHandleZeroForInput(justCheck);
				}
				if (computeNormalizedTotal)
				{
					normalizedTotal = totalForInput;
					computeNormalizedTotal = false;
				}
				else if (!DoubleMath.fuzzyEquals(totalForInput, normalizedTotal, 1e-12))
				{
					if (justCheck)
					{
						return false;
					}
				}
				if (!justCheck)
				{
					if (hasSparseWeights())
					{
						for (int i = start; i < nextsi; ++i)
						{
							_sparseWeights[i] /= totalForInput;
						}
					}
					if (hasSparseEnergies())
					{
						double logTotalForInput = Math.log(totalForInput);
						for (int i = start; i < nextsi; ++i)
						{
							_sparseEnergies[i] += logTotalForInput;
						}
					}
				}
				start = nextsi;
			}
		}
		
		_computedMask |= CONDITIONAL|CONDITIONAL_COMPUTED;
		return true;
	}
	
	@Override
	boolean normalizeUndirected(boolean justCheck)
	{
		if ((_computedMask & NORMALIZED) != 0)
		{
			return true;
		}
		
		double total = 0.0;
		if (hasSparseWeights())
		{
			for (double w : _sparseWeights)
			{
				total += w;
			}
		}
		else
		{
			for (double e: _sparseEnergies)
			{
				total += energyToWeight(e);
			}
		}
		
		if (!DoubleMath.fuzzyEquals(total, 1.0, 1e-12))
		{
			if (justCheck)
			{
				return false;
			}
			
			if (total == 0.0)
			{
				throw normalizeUndirectedHandleZero();
			}
			
			for (int i = _sparseWeights.length; --i>=0;)
			{
				_sparseWeights[i] /= total;
			}
			if (_sparseEnergies.length > 0)
			{
				final double logTotal = Math.log(total);
				for (int i = _sparseEnergies.length; --i>=0;)
				{
					_sparseEnergies[i] += logTotal;
				}
			}
		}

		_computedMask |= NORMALIZED|NORMALIZED_COMPUTED;
		return true;
	}

	private DimpleException notDeterministic(String method)
	{
		return DimpleException.unsupportedMethod(getClass(), method, "deterministic representation not supported.");
	}
	
	/**
	 * If {@link #hasSparseIndices()} this will recompute their values based on the current
	 * index entries.
	 */
	private void recomputeSparseIndices()
	{
		if (hasSparseIndices())
		{
			IndexEntry[] indexArray = _indexArray;
			final int sparseSize = indexArray.length;
			if (sparseSize == 0)
			{
				_sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
			}
			else
			{
				int[][] sparseIndices = _sparseIndices = new int[indexArray.length][];
				for (int i = indexArray.length; --i >=0;)
				{
					sparseIndices[i] = indexArray[i]._indices;
				}
			}
		}
	}

	@Override
	void setDirected(@Nullable BitSet outputSet, boolean assertConditional)
	{
		// REFACTOR: share?
		final JointDomainIndexer oldDomains = getDomainIndexer();
		final JointDomainIndexer newDomains = JointDomainIndexer.create(outputSet, oldDomains);
		if (oldDomains.equals(newDomains))
		{
			if (assertConditional)
			{
				assertIsConditional();
			}
			return;
		}

		_computedMask = 0;
		setDomainIndexer(newDomains);
		_entryComparator = new IndexEntryComparator(newDomains);

		if (!oldDomains.hasCanonicalDomainOrder() | !newDomains.hasCanonicalDomainOrder())
		{
			// Need to reorder the entries and values.
			int sparseSize = sparseSize();
			
			Arrays.sort(_indexArray, _entryComparator);
			
			if (_sparseWeights.length > 0)
			{
				double[] sparseWeights = new double[sparseSize];
				for (int si = 0; si < sparseSize; ++ si)
				{
					sparseWeights[si] = _sparseWeights[_indexArray[si]._sparseIndex];
				}
				_sparseWeights = sparseWeights;
			}
			if (_sparseEnergies.length > 0)
			{
				double[] sparseEnergies = new double[sparseSize];
				for (int si = 0; si < sparseSize; ++ si)
				{
					sparseEnergies[si] = _sparseEnergies[_indexArray[si]._sparseIndex];
				}
				_sparseEnergies = sparseEnergies;
			}
			recomputeSparseIndices();
			for (int si = 0; si < sparseSize; ++ si)
			{
				_indexArray[si]._sparseIndex = si;
			}
		}
		
		if (assertConditional)
		{
			assertIsConditional();
		}
	}
	
	@Override
	void setRepresentation(int newRep)
	{
		if (_representation == newRep)
		{
			return;
		}

		boolean convertFromWeights = false;
		boolean convertFromEnergies = false;
		
		switch (newRep)
		{
		case FactorTable.SPARSE_ENERGY:
		case FactorTable.SPARSE_ENERGY_WITH_INDICES:
			convertFromWeights = !hasSparseEnergies();
			break;
			
		case FactorTable.SPARSE_WEIGHT:
		case FactorTable.SPARSE_WEIGHT_WITH_INDICES:
			convertFromEnergies = !hasSparseWeights();
			break;
			
		case FactorTable.ALL_SPARSE:
		case FactorTable.ALL_SPARSE_WITH_INDICES:
			if (!hasSparseEnergies())
			{
				convertFromWeights = true;
			}
			else
			{
				convertFromEnergies = true;
			}
			break;
			
		default:
			throw new DimpleException(
				"Cannot set representation to '%s' because '%s' does not support dense representations.",
				FactorTableRepresentation.forMask(newRep).name(), getClass().getSimpleName()
				);
		}
		
		if (convertFromWeights)
		{
			double[] sparseWeights = _sparseWeights;
			double[] sparseEnergies = _sparseEnergies = new double[sparseWeights.length];
			for (int i = sparseWeights.length; --i>=0;)
			{
				sparseEnergies[i] = weightToEnergy(sparseWeights[i]);
			}
		}
		else if (convertFromEnergies)
		{
			double[] sparseEnergies = _sparseEnergies;
			double[] sparseWeights = _sparseWeights = new double[sparseEnergies.length];
			for (int i = sparseEnergies.length; --i>=0;)
			{
				sparseWeights[i] = energyToWeight(sparseEnergies[i]);
			}
		}
		
		if (!hasSparseIndices() && (newRep & FactorTable.SPARSE_INDICES) != 0)
		{
			getIndicesSparseUnsafe();
		}
		
		_representation = newRep;
		
		if (!hasSparseEnergies())
		{
			_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		
		if (!hasSparseWeights())
		{
			_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		
		if (!hasSparseIndices())
		{
			_sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
		}
	}
	
	@Override
	void setSparseValues(int[][] indicesArray, double[] values, int representation)
	{
		int size = indicesArray.length;
		if (size != values.length)
		{
			// REFACTOR: share
			throw new IllegalArgumentException(
				String.format("'Arrays have different sizes: %d and %d",
					size, values.length));
		}
		
		final JointDomainIndexer domainIndexer = getDomainIndexer();
		
		final IndexEntry[] indexArray = new IndexEntry[size];
		
		for (int i = 0; i < size; ++i)
		{
			int[] indices = indicesArray[i];
			domainIndexer.validateIndices(indices);
			indexArray[i] = new IndexEntry(Objects.requireNonNull(ArrayUtil.cloneArray(indices)), i);
		}
		
		boolean doSort = false;
		for (int i = 1; i < size; ++i)
		{
			if (0 < _entryComparator.compare(indexArray[i-1], indexArray[i]))
			{
				doSort = true;
				break;
			}
		}

		double[] copiedValues = new double[size];
		
		if (doSort)
		{
			Arrays.sort(indexArray, _entryComparator);
			
			for (int i = 0; i < size; ++i)
			{
				IndexEntry entry = indexArray[i];
				copiedValues[i] = values[entry._sparseIndex];
				entry._sparseIndex = i;
			}
		}
		else
		{
			System.arraycopy(values, 0, copiedValues, 0, size);
		}
		
		for (int i = 1; i < size; ++i)
		{
			IndexEntry entry1 = indexArray[i - 1];
			IndexEntry entry2 = indexArray[i];
			
			if (entry1.equals(entry2))
			{
				throw new IllegalArgumentException(String.format(
					"Multiple entries with same set of indices %s", entry1._indices));
			}
		}

		_indexArray = indexArray;
		_indexSet.clear();
		for (IndexEntry entry : _indexArray)
		{
			_indexSet.put(entry, entry);
		}
		
		switch (representation)
		{
		case FactorTable.SPARSE_ENERGY:
			_sparseEnergies = copiedValues;
			_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			break;
		case FactorTable.SPARSE_WEIGHT:
			_sparseWeights = copiedValues;
			_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			break;
		default:
			assert(false);
		}
		_sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
		_representation = representation;
		recomputeSparseIndices();
		
		_computedMask = 0;
		computeNonZeroWeights();
	}
	
	/**
	 * For implementation of {@link #setWeightForSparseIndex(double, int)} and
	 * {@link #setEnergyForSparseIndex(double, int)}
	 */
	private void setWeightEnergyForSparseIndex(double weight, double energy, int sparseIndex)
	{
		if (hasSparseEnergies())
		{
			_sparseEnergies[sparseIndex] = energy;
		}
		if (hasSparseWeights())
		{
			_sparseWeights[sparseIndex] = weight;
		}
	}
}
