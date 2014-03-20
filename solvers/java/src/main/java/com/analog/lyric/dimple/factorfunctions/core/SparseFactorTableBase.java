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

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.model.values.Value;

public abstract class SparseFactorTableBase extends FactorTableBase implements IFactorTable
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Set if table is known to be in normalized form (all weights add up to one).
	 */
	static final int NORMALIZED = 0x01;
	/**
	 * Set if value of {@link #NORMALIZED} bit has been computed.
	 */
	static final int NORMALIZED_COMPUTED = 0x02;
	/**
	 * Set if table is directed and has been conditionally normalized (so that the total weight for any
	 * two inputs is the same).
	 */
	static final int CONDITIONAL = 0x04;
	/**
	 * Set if value of {@link #CONDITIONAL} bit has been computed.
	 */
	static final int CONDITIONAL_COMPUTED = 0x08;
	/**
	 * Bit mask indicating how the contents of the table are represented. Exposed
	 * by {@link #getRepresentation()} and {@link #setRepresentation(FactorTableRepresentation)}.
	 * <p>
	 * This is a combination of the bits: {@link #DENSE_ENERGY}, {@link #DENSE_WEIGHT}, {@link #SPARSE_ENERGY},
	 * {@link #SPARSE_WEIGHT}, {@link #SPARSE_INDICES}.
	 */
	int _representation;
	
	double[] _sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	double[] _sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	/**
	 * Count of table entries with non-zero weight/non-infinite energy.
	 * <p>
	 * When table has no sparse representation, this is returned as the {@link #sparseSize()}.
	 */
	int _nonZeroWeights;
	/**
	 * Same information as {@link #_sparseIndexToJointIndex} but instead of storing joint indices stores
	 * arrays of element indices.
	 */
	int[][] _sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
	
	/**
	 * Information computed about the table based on its values. This field is zeroed out whenever
	 * table weights or energies are changed.
	 * <p>
	 * Consists of the bits {@link #DETERMINISTIC_COMPUTED}, {@link #NORMALIZED}, {@link #NORMALIZED_COMPUTED},
	 * {@link #CONDITIONAL} and {@link #CONDITIONAL_COMPUTED}.
	 */
	int _computedMask = 0;

	/*--------------
	 * Construction
	 */
	
	SparseFactorTableBase(JointDomainIndexer domains)
	{
		super(domains);
		_nonZeroWeights = 0;
		_representation = FactorTable.SPARSE_ENERGY;
	}

	SparseFactorTableBase(SparseFactorTableBase that)
	{
		super(that);
		_nonZeroWeights = that._nonZeroWeights;
		_representation = that._representation;
		_computedMask = that._computedMask;
		_sparseEnergies = ArrayUtil.cloneArray(that._sparseEnergies);
		_sparseWeights = ArrayUtil.cloneArray(that._sparseWeights);
		_sparseIndices = ArrayUtil.cloneArray(that._sparseIndices);
	}

	/*--------------------------
	 * IFactorTableBase methods
	 */
	
	@Override
	public final IFactorTable convert(JointDomainReindexer converter)
	{
		return FactorTable.convert(this, converter);
	}

	@Override
	public final int countNonZeroWeights()
	{
		return _nonZeroWeights;
	}
	
	@Override
	public final boolean hasSparseEnergies()
	{
		return (_representation & FactorTable.SPARSE_ENERGY) != 0;
	}

	@Override
	public final boolean hasSparseIndices()
	{
		return (_representation & FactorTable.SPARSE_INDICES) != 0;
	}
	
	@Override
	public final boolean hasSparseWeights()
	{
		return (_representation & FactorTable.SPARSE_WEIGHT) != 0;
	}

	@Override
	public final boolean isNormalized()
	{
		if ((_computedMask & NORMALIZED_COMPUTED) == 0)
		{
			if (!isDirected())
			{
				normalizeUndirected(true);
			}
			_computedMask |= NORMALIZED_COMPUTED;
		}
		return (_computedMask & NORMALIZED) != 0;
	}
	
	@Override
	public final void normalize()
	{
		if (isDirected())
		{
			throw new UnsupportedOperationException(
				"normalize() not supported for directed factor table. Use normalizeConditional() instead");
		}

		normalizeUndirected(false);
	}

	@Override
	public final void normalizeConditional()
	{
		if (!isDirected())
		{
			throw new UnsupportedOperationException(
				"normalizeConditional() not supported for undirected factor table. Use normalize() instead");
		}

		normalizeDirected(false);
	}
	
	@Override
	public final void setDirected(BitSet outputSet)
	{
		setDirected(outputSet, false);
	}

	/*----------------------
	 * IFactorTable methods
	 */

	@Override
	public final IFactorTable createTableWithNewVariables(DiscreteDomain[] additionalDomains)
	{
		JointDomainIndexer domains = getDomainIndexer();
		JointDomainReindexer converter =
			JointDomainReindexer.createAdder(domains, domains.size(), additionalDomains);

		return FactorTable.convert(this, converter);
	}

	@Override
	public final double[] getEnergySlice(int sliceDimension, int... indices)
	{
		return getEnergySlice(null, sliceDimension, indices);
	}

	@Override
	public final double[] getEnergySlice(int sliceDimension, Value ... values)
	{
		return getEnergySlice(null, sliceDimension, values);
	}

	@Override
	public final FactorTableRepresentation getRepresentation()
	{
		return FactorTableRepresentation.forMask(_representation);
	}

	@Override
	public final double[] getWeightSlice(int sliceDimension, int... indices)
	{
		return getWeightSlice(null, sliceDimension, indices);
	}

	@Override
	public final double[] getWeightSlice(int sliceDimension, Value ... values)
	{
		return getWeightSlice(null, sliceDimension, values);
	}

	@Override
	public final void replaceEnergiesSparse(double[] energies)
	{
		final int size = energies.length;
		if (size != sparseSize())
		{
			throw new IllegalArgumentException(
				String.format("Array size (%d) does not match sparse size (%d).", size, sparseSize()));
		}
	
		for (int si = 0; si < size; ++si)
		{
			setEnergyForSparseIndex(energies[si], si);
		}
	}

	@Override
	public final void replaceWeightsSparse(double[] weights)
	{
		final int size = weights.length;
		if (size != sparseSize())
		{
			throw new IllegalArgumentException(
				String.format("Array size (%d) does not match sparse size (%d).", size, sparseSize()));
		}
	
		for (int si = 0; si < size; ++si)
		{
			setWeightForSparseIndex(weights[si], si);
		}
	}

	@Override
	public final void setConditional(BitSet outputSet)
	{
		if (outputSet == null)
		{
			throw new IllegalArgumentException("setConditional(BitSet) requires non-null argument");
		}
		setDirected(outputSet, true);
	}

	@Override
	public final void setEnergiesSparse(int[][] indices, double[] energies)
	{
		setSparseValues(indices, energies, FactorTable.SPARSE_ENERGY);
	}

	@Override
	public final void setRepresentation(FactorTableRepresentation representation)
	{
		setRepresentation(representation.mask());
	}

	@Override
	public final void setWeightsSparse(int[][] indices, double[] weights)
	{
		setSparseValues(indices, weights, FactorTable.SPARSE_WEIGHT);
	}

	@Override
	public final void makeConditional(BitSet outputSet)
	{
		if (outputSet == null)
		{
			throw new IllegalArgumentException("setConditionalAndNroa(BitSet) requires non-null argument");
		}
		setDirected(outputSet, false);
		normalizeConditional();
	}

	/*-----------------
	 * Package methods
	 */
	
	final void assertIsConditional()
	{
		 if (!isConditional())
		 {
			 throw new DimpleException("weights must be normalized correctly for directed factors");
		 }
	}
	
	abstract boolean normalizeDirected(boolean justCheck);

	/**
	 * Throws exception with message indicating an attempt to normalize a directed table whose weights
	 * for some input adds up to zero.
	 * 
	 * @return false if {@code justCheck} is true, otherwise throws an exception.
	 * @throws DimpleException if {@code justCheck} is false.
	 */
	final boolean normalizeDirectedHandleZeroForInput(boolean justCheck)
	{
		if (!justCheck)
		{
			throw new DimpleException("Cannot normalize directed factor table with zero total weight for some input");
		}
		return false;
	}

	/**
	 * Returns an exception with message indicating an attempt to normalize an undirected table whose weights
	 * add up to zero.
	 */
	final DimpleException normalizeUndirectedHandleZero()
	{
		return new DimpleException("Cannot normalize undirected factor table with zero total weight");
	}
	
	abstract boolean normalizeUndirected(boolean justCheck);
	
	abstract void setDirected(BitSet outputSet, boolean assertConditional);

	abstract void setRepresentation(int newRep);

	abstract void setSparseValues(int[][] indices, double[] values, int representation);
}
