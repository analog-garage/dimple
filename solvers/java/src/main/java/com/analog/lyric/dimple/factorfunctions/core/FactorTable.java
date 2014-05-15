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

import static com.analog.lyric.dimple.model.domains.JointDomainReindexer.*;
import static com.analog.lyric.math.Utilities.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.Objects;

import net.jcip.annotations.NotThreadSafe;
import cern.colt.map.OpenIntDoubleHashMap;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer.Indices;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.math.Utilities;
import com.analog.lyric.util.misc.Misc;
import com.analog.lyric.util.misc.Nullable;
import com.google.common.math.DoubleMath;
import com.google.common.primitives.Ints;

@NotThreadSafe
public class FactorTable extends SparseFactorTableBase
{
	/*-----------
	 * Constants
	 */
	
	private static final long serialVersionUID = 1L;
	
	// _representation values
	
	/**
	 * If the low-order four-bits are zero, then the table represents a directed deterministic
	 * function, with non-zero entries indicated by the values in {@link #_sparseIndexToJointIndex}
	 * but weights and values are not stored explicitly because all the non-zero weights will be one.
	 * <p>
	 * May be combined with {@link #SPARSE_INDICES}.
	 */
	static final int DETERMINISTIC = 0x0;
	
	/**
	 * If set, the table stores energies in dense representation in {@link #_denseEnergies}.
	 */
	static final int DENSE_ENERGY = 0x1;
	
	/**
	 * If set, the table stores weights in dense representation in {@link #_denseEnergies}.
	 */
	static final int DENSE_WEIGHT = 0x2;
	
	/**
	 * If set, the table stores energies in sparse representation in {@link #_sparseEnergies}
	 * and mapping from sparse to joint indices in {@link #_sparseIndexToJointIndex}.
	 */
	static final int SPARSE_ENERGY = 0x4;

	/**
	 * If set, the table stores weights in sparse representation in {@link #_sparseWeights}
	 * and mapping from sparse to joint indices in {@link #_sparseIndexToJointIndex}.
	 */
	static final int SPARSE_WEIGHT = 0x8;
	
	/**
	 * If set, the table stores mapping from sparse indices to joint table indices in {@link #_sparseIndices}.
	 */
	static final int SPARSE_INDICES = 0x10;
	
	static final int ALL_DENSE = DENSE_ENERGY | DENSE_WEIGHT;
	static final int ALL_SPARSE = SPARSE_ENERGY | SPARSE_WEIGHT;
	static final int ALL_WEIGHT = DENSE_WEIGHT | SPARSE_WEIGHT;
	static final int ALL_ENERGY = DENSE_ENERGY | SPARSE_ENERGY;
	static final int ALL_VALUES = ALL_DENSE | ALL_SPARSE;
	static final int ALL_DENSE_WITH_INDICES = ALL_DENSE | SPARSE_INDICES; // Invalid?
	static final int ALL_SPARSE_WITH_INDICES = ALL_SPARSE | SPARSE_INDICES;
	static final int ALL_WEIGHT_WITH_INDICES = ALL_WEIGHT | SPARSE_INDICES;
	static final int ALL_ENERGY_WITH_INDICES = ALL_ENERGY | SPARSE_INDICES;
	static final int ALL = ALL_VALUES | SPARSE_INDICES;
	
	static final int SPARSE_ENERGY_DENSE_WEIGHT = SPARSE_ENERGY | DENSE_WEIGHT;
	static final int DENSE_ENERGY_SPARSE_WEIGHT = DENSE_ENERGY | SPARSE_WEIGHT;
	static final int NOT_SPARSE_WEIGHT = ALL_ENERGY | DENSE_WEIGHT;
	static final int NOT_SPARSE_ENERGY = ALL_WEIGHT | DENSE_ENERGY;
	static final int NOT_DENSE_WEIGHT = ALL_ENERGY | SPARSE_WEIGHT;
	static final int NOT_DENSE_ENERGY = ALL_WEIGHT | SPARSE_ENERGY;
	
	static final int DETERMINISTIC_WITH_INDICES = SPARSE_INDICES;
	static final int DENSE_ENERGY_WITH_INDICES = DENSE_ENERGY | SPARSE_INDICES; // Invalid?
	static final int DENSE_WEIGHT_WITH_INDICES = DENSE_WEIGHT | SPARSE_INDICES; // Invalid?
	static final int SPARSE_ENERGY_WITH_INDICES = SPARSE_ENERGY | SPARSE_INDICES;
	static final int SPARSE_WEIGHT_WITH_INDICES = SPARSE_WEIGHT | SPARSE_INDICES;
	static final int SPARSE_ENERGY_DENSE_WEIGHT_WITH_INDICES = SPARSE_ENERGY_DENSE_WEIGHT | SPARSE_INDICES;
	static final int DENSE_ENERGY_SPARSE_WEIGHT_WITH_INDICES = DENSE_ENERGY_SPARSE_WEIGHT | SPARSE_INDICES;
	static final int NOT_SPARSE_WEIGHT_WITH_INDICES = NOT_SPARSE_WEIGHT | SPARSE_INDICES;
	static final int NOT_SPARSE_ENERGY_WITH_INDICES = NOT_SPARSE_ENERGY | SPARSE_INDICES;
	static final int NOT_DENSE_WEIGHT_WITH_INDICES = NOT_DENSE_WEIGHT | SPARSE_INDICES;
	static final int NOT_DENSE_ENERGY_WITH_INDICES = NOT_DENSE_ENERGY | SPARSE_INDICES;

	// _computedMask values
	
	/**
	 * Set if {@link #isDeterministicDirected()} has been invoked since the last time the values or
	 * representation of the table were changed.
	 */
	static final int DETERMINISTIC_COMPUTED = 0x10;
	
	/*-------
	 * State
	 */

	private double[] _denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	private double[] _denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	/**
	 * Maps sparse indexes to joint indexes. Empty if table is in dense form
	 * (because in that case the location and joint index are the same).
	 * <p>
	 * If not dense or deterministic (&directed) this lookup will require a
	 * binary search.
	 */
	int[] _sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
	
	/*--------------
	 * Construction
	 */
	
	FactorTable(JointDomainIndexer domains)
	{
		super(domains);
		_representation = SPARSE_ENERGY;
	}
	
	/**
	 * Construct as a copy of another table instance.
	 */
	FactorTable(FactorTable that)
	{
		super(that);
		
		_denseEnergies = Objects.requireNonNull(ArrayUtil.cloneArray(that._denseEnergies));
		_denseWeights = Objects.requireNonNull(ArrayUtil.cloneArray(that._denseWeights));
		_sparseIndexToJointIndex = Objects.requireNonNull(ArrayUtil.cloneArray(that._sparseIndexToJointIndex));
	}

	/**
	 * Constructs a new table by converting the contents of {@code other} table using
	 * {@code converter} whose "from" domains must match {@code other}'s domains.
	 * New table will have same representation as {@code other}.
	 */
	FactorTable(IFactorTable other, JointDomainReindexer converter)
	{
		this(other, converter, other.getRepresentation());
	}
	
	/**
	 * Constructs a new table with given representation by converting the contents of {@code other} table using
	 * {@code converter} whose "from" domains must match {@code other}'s domains.
	 */
	FactorTable(IFactorTable other, JointDomainReindexer converter, FactorTableRepresentation representation)
	{
		super(converter.getToDomains());
		final JointDomainIndexer domains = getDomainIndexer();
		
		_representation = representation.mask();
		
		_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
		_sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
		_computedMask = 0;
		
		//
		// Convert using single representation, then switch to desired representation.
		//
		
		if (other instanceof FactorTable)
		{
			FactorTable ft = (FactorTable)other;
			
			if (ft._representation == DETERMINISTIC)
			{
				_sparseIndexToJointIndex = converter.convertSparseToJointIndex(ft._sparseIndexToJointIndex);
				_representation = DETERMINISTIC;
			}
			else if ((ft._representation & ALL_SPARSE) != 0)
			{
				_sparseIndexToJointIndex = converter.convertSparseToJointIndex(ft._sparseIndexToJointIndex);

				boolean denseSparse = ft.sparseSize() == ft.jointSize();

				// FIXME - convert sparse indices

				if ((ft._representation & SPARSE_WEIGHT) != 0)
				{
					_sparseWeights = denseSparse ?
						converter.convertDenseWeights(ft._sparseWeights) :
							converter.convertSparseWeights(ft._sparseWeights, ft._sparseIndexToJointIndex,
								_sparseIndexToJointIndex);
						_representation = SPARSE_WEIGHT;
				}
				else // SPARSE_ENERGY
				{
					_sparseEnergies = denseSparse ?
						converter.convertDenseEnergies(ft._sparseEnergies) :
							converter.convertSparseEnergies(ft._sparseEnergies, ft._sparseIndexToJointIndex,
								_sparseIndexToJointIndex);
						_representation = SPARSE_ENERGY;
				}
			}
			else if ((ft._representation & DENSE_WEIGHT) != 0)
			{
				_denseWeights = converter.convertDenseWeights(ft._denseWeights);
				_representation = DENSE_WEIGHT;
			}
			else // DENSE_ENERGY
			{
				_denseEnergies = converter.convertDenseEnergies(ft._denseEnergies);
				_representation = DENSE_ENERGY;
			}

			if (converter.getRemovedDomains() == null)
			{
				_nonZeroWeights = ft.countNonZeroWeights() * converter.getAddedCardinality();
			}
			else if (ft.hasMaximumDensity())
			{
				_nonZeroWeights = domains.getCardinality();
			}
			else
			{
				// Need to count them explicitly
				computeNonZeroWeights();
			}
		}
		else
		{
			// SparseFactorTable
			
			// Do initial conversion to SPARSE_WEIGHT representation.
			_representation = SPARSE_WEIGHT;
			
			final Indices scratch = converter.getScratch();
			final int otherSparseSize = other.sparseSize();
			final OpenIntDoubleHashMap jointIndexToWeight = new OpenIntDoubleHashMap(otherSparseSize);
			for (int si = 0; si < otherSparseSize; ++si)
			{
				other.sparseIndexToIndices(si, scratch.fromIndices);
				converter.convertIndices(scratch);
				if (scratch.toIndices[0] >= 0)
				{
					int ji = domains.jointIndexFromIndices(scratch.toIndices);
					// OpenIntDoubleHashMap.get returns 0.0 if no entry is found, which is exactly what we want here.
					jointIndexToWeight.put(ji, jointIndexToWeight.get(ji) + other.getWeightForSparseIndex(si));
				}
			}
			scratch.release();
			
			final int sparseSize = jointIndexToWeight.size();
			final int[] sparseToJoint = Arrays.copyOf(jointIndexToWeight.keys().elements(), sparseSize);
			Arrays.sort(sparseToJoint);
			
			final double[] sparseWeights = new double[sparseSize];
			for (int si = 0; si < sparseSize; ++ si)
			{
				double weight = jointIndexToWeight.get(sparseToJoint[si]);
				if (weight != 0.0)
				{
					++_nonZeroWeights;
				}
				sparseWeights[si] = weight;
			}
			
			_sparseIndexToJointIndex = sparseToJoint;
			_sparseWeights = sparseWeights;
		}
		
		setRepresentation(representation);
	}
	
	public static IFactorTable create(Object table, DiscreteDomain[] domains)
	{
		Object [] result = Misc.nDimensionalArray2indicesAndValues(table);
		return FactorTable.create((int[][])result[0], (double[])result[1], domains);
	}

	public static IFactorTable create(int[][] indices, double[] weights, Discrete... variables)
	{
		DiscreteDomain[] domains = new DiscreteDomain[variables.length];
		for(int i = 0; i < domains.length; ++i)
		{
			domains[i] = variables[i].getDiscreteDomain();
		}
		return create(indices, weights, domains);
	}

	public static IFactorTable create(int[][] indices, double[] weights, DiscreteDomain... domains)
	{
		IFactorTable table = create(domains);
		table.setWeightsSparse(indices, weights);
		return table;
	}

	public static IFactorTable create(JointDomainIndexer domains)
	{
		return domains.supportsJointIndexing() ? new FactorTable(domains) : new SparseFactorTable(domains);
	}
	public static IFactorTable create(DiscreteDomain... domains)
	{
		return create(JointDomainIndexer.create(domains));
	}
	
	public static IFactorTable create(@Nullable BitSet outputSet, DiscreteDomain ... domains)
	{
		return create(JointDomainIndexer.create(outputSet, domains));
	}
	
	/**
	 * Create a directed deterministic factor table that marginalizes out the specified subdomain
	 * of the joint input domain.
	 * <p>
	 * @param outputDomainIndex a number in the range [0, inputDomain.getDimensions() - 1] that specifies
	 * which subdomain to produce as the first dimension and output.
	 * @param inputDomain will be the second dimension in the table and the input.
	 * @since 0.05
	 */
	public static IFactorTable createMarginal(int outputDomainIndex, JointDiscreteDomain<?> inputDomain)
	{
		final JointDomainIndexer inputDomains = inputDomain.getDomainIndexer();
		final DiscreteDomain outputDomain = inputDomains.get(outputDomainIndex);
		
		final int inputSize = inputDomain.size();
		final int outputSize = outputDomain.size();
		
		// The joint cardinality of the domains prior to the output subdomain (one if empty)
		final int innerCardinality = inputDomains.getStride(outputDomainIndex);
		// The joint cardinality of the domains after the output subdomain (one if empty)
		final int outerCardinality = inputSize / (innerCardinality * outputSize);

		final int[] indices = new int[inputSize];
		for (int i = 0, outer = 0; outer < outerCardinality; ++outer)
			for (int out = 0; out < outputSize; ++out)
				for (int inner = 0; inner < innerCardinality; ++inner)
					indices[i++] = out;
		
		// Build the actual table
		final IFactorTable table = create(BitSetUtil.bitsetFromIndices(2, 0), outputDomain, inputDomain);
		table.setDeterministicOutputIndices(indices);
		
		return table;
	}
	
	public static IFactorTable convert(IFactorTable oldTable, JointDomainReindexer converter)
	{
		if (converter.getToDomains().supportsJointIndexing())
		{
			return new FactorTable(oldTable, converter);
		}
		else
		{
			return new SparseFactorTable(oldTable, converter);
		}
	}
	
	public static IFactorTable convert(
		IFactorTable oldTable,
		JointDomainReindexer converter,
		FactorTableRepresentation representation)
	{
		if (converter.getToDomains().supportsJointIndexing())
		{
			return new FactorTable(oldTable, converter, representation);
		}
		else
		{
			return new SparseFactorTable(oldTable, converter, representation);
		}
	}

	/**
	 * Constructs a new factor table that is the product of all of the given tables.
	 * <p>
	 * Invokes {@link #product(ArrayList, FactorTableRepresentation)} with null representation
	 * argument.
	 */
	public static @Nullable IFactorTable product(ArrayList<Tuple2<IFactorTable,int[]>> entries)
	{
		return product(entries, null);
	}
	
	/**
	 * Constructs a new factor table that is the product of all of the given tables.
	 * 
	 * @param entries maps factor tables to an array of index mappings that indicates where
	 * each dimension in the table is located in the table to be constructed. Thus the
	 * index mapping array for a given factor table must have length equal to the factor table's dimensions
	 * and each entry must be in the range [0,N] where N is one less than the size of the new table. Every
	 * value from 0 to N must be represented in at least one index mapping array. N can be any number between
	 * the sum of the number of dimensions of all factors when the factors do not share any dimensions in common,
	 * or the number of dimensions of the largest factor. When two tables both map a dimension to the same
	 * dimension in the target table, the domains must match.
	 * <p>
	 * @representation is the representation to use for the table to be constructed. If null, the representation
	 * will be set to either {@link FactorTableRepresentation#DENSE_ENERGY} or
	 * {@link FactorTableRepresentation#SPARSE_ENERGY} based on the density of the tables.
	 * <p>
	 * @return Newly constructed table.
	 */
	public static @Nullable IFactorTable product(
		ArrayList<Tuple2<IFactorTable,int[]>> entries, @Nullable FactorTableRepresentation representation)
	{
		final int nFactors = entries.size();
		if (nFactors < 1)
		{
			return null;
		}
		
		int nDimensions = 0;
		for (int i = 0; i < nFactors; ++i)
		{
			nDimensions = Math.max(nDimensions, 1 + Ints.max(entries.get(i).second));
		}
		
		// Build target domain list and estimate density of new table.
		final DiscreteDomain[] toDomains = new DiscreteDomain[nDimensions];
		for (Tuple2<IFactorTable, int[]> tuple : entries)
		{
			final IFactorTable table = tuple.getKey();
			final int[] old2New = tuple.getValue();
			final JointDomainIndexer tableDomains = table.getDomainIndexer();
			
			if (old2New.length != tableDomains.size())
			{
				throw new IllegalArgumentException(
					String.format("Index mapping for %s does not match table dimensions", table));
			}
			
			for (int i = 0, end = tableDomains.size(); i < end; ++i)
			{
				final int j = old2New[i];
				
				if (j < 0)
				{
					throw new IllegalArgumentException(
						String.format("Negative index mapping for %s", table));
				}
				
				final DiscreteDomain domain = tableDomains.get(i);
				final DiscreteDomain curDomain = toDomains[j];
				
				if (curDomain == null)
				{
					toDomains[j] = domain;
				}
				else if (!curDomain.equals(domain))
				{
					throw new IllegalArgumentException(
						String.format("Conflicting domain mapping for entry %d of index map for %s", j, table));
				}
			}
		}
		
		final JointDomainIndexer toIndexer = JointDomainIndexer.create(toDomains);
		final boolean supportsJoint = toIndexer.supportsJointIndexing();
		final int toCardinality = supportsJoint ? toIndexer.getCardinality() : -1;
		
		FactorTableRepresentation tableRep = FactorTableRepresentation.SPARSE_ENERGY;
		
		if (supportsJoint)
		{
			// The sparsest table is going to put an upper bound
			// on the sparsity of the final table. If the minimum
			// is high enough, we use a dense representation when
			// building the table.
			
			int minNonZeroWeights = Integer.MAX_VALUE;
			
			for (int i = 0; i < nFactors; ++i)
			{
				IFactorTable table = entries.get(i).first;
				final int oldNzw = table.countNonZeroWeights();
				final int oldCardinality = table.jointSize();
				final int newNzw  = oldNzw * (toCardinality / oldCardinality);
				minNonZeroWeights = Math.min(minNonZeroWeights, newNzw);
			}
			
			// If table looks like it will be dense enough, use a dense representation.
			if (minNonZeroWeights >= toCardinality * .90)
			{
				tableRep = FactorTableRepresentation.DENSE_ENERGY;
			}
		}
		
		IFactorTable newTable = null;
		
		for (Map.Entry<IFactorTable, int[]> entry : entries)
		{
			final IFactorTable oldTable = entry.getKey();
			final int[] old2New = entry.getValue();
			
			// Convert factor table to new format.
			final JointDomainIndexer fromIndexer = oldTable.getDomainIndexer();
			
			final JointDomainReindexer converter = JointDomainReindexer.createPermuter(fromIndexer, toIndexer, old2New);
			
			final IFactorTable convertedTable =	FactorTable.convert(oldTable, converter, tableRep);
			
			if (newTable == null)
			{
				newTable = convertedTable;
			}
			else
			{
				// Merge results by adding energies (i.e. multiplying weights)
				if (tableRep.hasDense())
				{
					for (int ji = 0; ji < toCardinality; ++ji)
					{
						double energy = newTable.getEnergyForJointIndex(ji);
						energy += convertedTable.getEnergyForJointIndex(ji);
						newTable.setEnergyForJointIndex(energy, ji);
					}
				}
				else
				{
					final int[] indices = toIndexer.allocateIndices(null);
					for (int si = 0, end = newTable.sparseSize(); si < end; ++si)
					{
						double energy = newTable.getEnergyForSparseIndex(si);
						newTable.sparseIndexToIndices(si, indices);
						energy += convertedTable.getEnergyForIndices(indices);
						newTable.setEnergyForIndices(energy, indices);
					}
					// Compact table if it became more sparse
					newTable.compact();
				}
			}
		}
		
		// Convert to target representation
		if (representation != null && newTable != null)
		{
			newTable.setRepresentation(representation);
		}
		
		return newTable;
	}
	
	/*---------------
	 * Serialization
	 */
	
	/**
	 * Override the default serialization to decrease size of serialized representation.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeInt(_representation);
		out.writeInt(_nonZeroWeights);
		out.writeInt(_computedMask);
		
		// Choose transmission representation
		int transmitRep = -1;
		double[] values = null;
		
		if (hasSparseRepresentation())
		{
			if (hasSparseWeights())
			{
				transmitRep = SPARSE_WEIGHT;
				values = _sparseWeights;
			}
			else if (hasSparseEnergies())
			{
				transmitRep = SPARSE_ENERGY;
				values = _sparseEnergies;
			}
			else
			{
				transmitRep = DETERMINISTIC;
			}
		}
		else
		{
			if (hasDenseWeights())
			{
				transmitRep = DENSE_WEIGHT;
				values = _denseWeights;
			}
			else if (hasDenseEnergies())
			{
				transmitRep = DENSE_ENERGY;
				values = _denseEnergies;
			}
		}
		
		assert(transmitRep >= 0);
		
		out.writeInt(transmitRep);
		
		if (values != null)
		{
			out.writeInt(values.length);
			for (double d : values)
			{
				out.writeDouble(d);
			}
		}
		else
		{
			out.writeInt(sparseSize());
		}
		
		if (_sparseIndexToJointIndex.length < jointSize())
		{
			for (int i : _sparseIndexToJointIndex)
			{
				out.writeInt(i);
			
			}
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException
	{
		_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
		_sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
		
		int representation = in.readInt();
		_nonZeroWeights = in.readInt();
		_computedMask = in.readInt();
		_representation = in.readInt();
		int size = in.readInt();
		
		double[] values = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		
		if (!hasDeterministicRepresentation())
		{
			values = new double[size];
			for (int i = 0; i < size; ++i)
			{
				values[i] = in.readDouble();
			}
		}
		
		if (hasSparseRepresentation() && size < jointSize())
		{
			// Read sparse to joint index map
			_sparseIndexToJointIndex = new int[size];
			for (int si = 0; si < size; ++si)
			{
				_sparseIndexToJointIndex[si] = in.readInt();
			}
		}
		
		switch (_representation)
		{
		case DETERMINISTIC:
			break;
		case DENSE_ENERGY:
			_denseEnergies = values;
			break;
		case DENSE_WEIGHT:
			_denseWeights = values;
			break;
		case SPARSE_ENERGY:
			_sparseEnergies = values;
			break;
		case SPARSE_WEIGHT:
			_sparseWeights = values;
			break;
		default:
			assert(false);
			break;
		}
		
		// Switch to the real representation.
		setRepresentation(representation);
	}
	
	@Deprecated
	@Override
	public void serializeToXML(String serializeName, String targetDirectory)
	{
		serializeToXML(this, serializeName, targetDirectory);
	}
	
	@Deprecated
	static public void serializeToXML(FactorTable ct, String serializeName, String targetDirectory)
	{
		com.analog.lyric.dimple.model.core.xmlSerializer toXML
			= new com.analog.lyric.dimple.model.core.xmlSerializer();
		
		toXML.serializeFactorTableToXML(ct,
									   serializeName,
									   targetDirectory);
	}

	@Deprecated
	public static @Nullable IFactorTable deserializeFromXML(String docName)
	{
		com.analog.lyric.dimple.model.core.xmlSerializer x
			= new com.analog.lyric.dimple.model.core.xmlSerializer();
		
		IFactorTable mct = x.deserializeFactorTableFromXML(docName);
		
		return mct;
	}

	/*----------------
	 * Object methods
	 */

	@Override
	public FactorTable clone()
	{
		return new FactorTable(this);
	}
	
	/*-----------------------------
	 * INewFactorTableBase methods
	 */
	
	@Override
	public void evalDeterministic(Object[] arguments)
	{
		if (!isDeterministicDirected())
		{
			throw new DimpleException("Table is not deterministic");
		}
		
		final JointDomainIndexer domains = getDomainIndexer();
		int outputSize = domains.getOutputCardinality();
		int inputIndex = domains.inputIndexFromElements(arguments);
		int jointIndex = _sparseIndexToJointIndex[inputIndex];
		int outputIndex = jointIndex - inputIndex * outputSize;
		domains.outputIndexToElements(outputIndex, arguments);
	}
	
	@Override
	public final double getEnergyForIndicesDense(int ... indices)
	{
		return _denseEnergies[getDomainIndexer().jointIndexFromIndices(indices)];
	}
	
	@Override
	public final double getEnergyForValuesDense(Value ... values)
	{
		return _denseEnergies[getDomainIndexer().jointIndexFromValues(values)];
	}

	@Override
	public final double getWeightForIndicesDense(int ... indices)
	{
		return _denseWeights[getDomainIndexer().jointIndexFromIndices(indices)];
	}
	
	@Override
	public final double getWeightForValuesDense(Value ... values)
	{
		return _denseWeights[getDomainIndexer().jointIndexFromValues(values)];
	}

	@Override
	public final double getEnergyForJointIndex(int jointIndex)
	{
		int sparseIndex;
		
		// Optimized for speed. Using a single switch instead of multiple if/else's ensures
		// there is only a single branching instruction. Switching over the enum may allow the
		// JIT compiler to infer that an unconditional branch may be used. This code assumes
		// that converting from weight to energy is cheaper than looking up the sparse index
		// from the joint one.
		
		switch (_representation)
		{
		case DETERMINISTIC:
		case DETERMINISTIC_WITH_INDICES:
			final int expectedJoint = _sparseIndexToJointIndex[getDomainIndexer().inputIndexFromJointIndex(jointIndex)];
			return expectedJoint == jointIndex ? 0.0 : Double.POSITIVE_INFINITY;
			
		case ALL_VALUES:
		case ALL_DENSE:
		case ALL_ENERGY:
		case DENSE_ENERGY:
		case NOT_DENSE_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case NOT_SPARSE_WEIGHT:
		case ALL:
		case ALL_DENSE_WITH_INDICES:
		case ALL_ENERGY_WITH_INDICES:
		case DENSE_ENERGY_WITH_INDICES:
		case NOT_DENSE_WEIGHT_WITH_INDICES:
		case NOT_SPARSE_ENERGY_WITH_INDICES:
		case DENSE_ENERGY_SPARSE_WEIGHT_WITH_INDICES:
		case NOT_SPARSE_WEIGHT_WITH_INDICES:
			return _denseEnergies[jointIndex];
			
		case ALL_WEIGHT:
		case DENSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case SPARSE_ENERGY_DENSE_WEIGHT:
		case ALL_WEIGHT_WITH_INDICES:
		case DENSE_WEIGHT_WITH_INDICES:
		case NOT_DENSE_ENERGY_WITH_INDICES:
		case SPARSE_ENERGY_DENSE_WEIGHT_WITH_INDICES:
			return Utilities.weightToEnergy(_denseWeights[jointIndex]);
			
		case ALL_SPARSE:
		case SPARSE_ENERGY:
		case ALL_SPARSE_WITH_INDICES:
		case SPARSE_ENERGY_WITH_INDICES:
			sparseIndex = sparseIndexFromJointIndex(jointIndex);
			if (sparseIndex >= 0)
			{
				return _sparseEnergies[sparseIndex];
			}
			break;
			
		case SPARSE_WEIGHT:
		case SPARSE_WEIGHT_WITH_INDICES:
			sparseIndex = sparseIndexFromJointIndex(jointIndex);
			if (sparseIndex >= 0)
			{
				return Utilities.weightToEnergy(_sparseWeights[sparseIndex]);
			}
			break;
		}
		
		return Double.POSITIVE_INFINITY;
	}
	
	@Override
	public final double getEnergyForSparseIndex(int sparseIndex)
	{
		switch (_representation)
		{
		case DETERMINISTIC:
		case DETERMINISTIC_WITH_INDICES:
			return 0.0;
			
		case ALL_DENSE:
		case DENSE_ENERGY:
		case ALL_DENSE_WITH_INDICES:
		case DENSE_ENERGY_WITH_INDICES:
			setRepresentation(_representation | SPARSE_ENERGY);
			// $FALL-THROUGH$
		case ALL_VALUES:
		case ALL_ENERGY:
		case ALL_SPARSE:
		case NOT_DENSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case SPARSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY_DENSE_WEIGHT:
		case ALL:
		case ALL_ENERGY_WITH_INDICES:
		case ALL_SPARSE_WITH_INDICES:
		case NOT_DENSE_WEIGHT_WITH_INDICES:
		case NOT_DENSE_ENERGY_WITH_INDICES:
		case SPARSE_ENERGY_WITH_INDICES:
		case NOT_SPARSE_WEIGHT_WITH_INDICES:
		case SPARSE_ENERGY_DENSE_WEIGHT_WITH_INDICES:
			return _sparseEnergies[sparseIndex];
			
		case DENSE_WEIGHT:
		case DENSE_WEIGHT_WITH_INDICES:
			setRepresentation(_representation | SPARSE_WEIGHT);
			// $FALL-THROUGH$
		case ALL_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case SPARSE_WEIGHT:
		case ALL_WEIGHT_WITH_INDICES:
		case NOT_SPARSE_ENERGY_WITH_INDICES:
		case DENSE_ENERGY_SPARSE_WEIGHT_WITH_INDICES:
		case SPARSE_WEIGHT_WITH_INDICES:
			return weightToEnergy(_sparseWeights[sparseIndex]);
			
		}
		
		return Double.POSITIVE_INFINITY;
	}
	
	@Override
	public final double getWeightForJointIndex(int jointIndex)
	{
		int sparseIndex;

		// See comment in getEnergyForJointIndex

		switch (_representation)
		{
		case DETERMINISTIC:
		case DETERMINISTIC_WITH_INDICES:
			final int expectedJoint = _sparseIndexToJointIndex[jointIndex /  getDomainIndexer().getOutputCardinality()];
			return expectedJoint == jointIndex ? 1.0 : 0.0;

		case ALL_VALUES:
		case ALL_DENSE:
		case ALL_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case DENSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY_DENSE_WEIGHT:
		case ALL:
		case ALL_DENSE_WITH_INDICES:
		case ALL_WEIGHT_WITH_INDICES:
		case NOT_SPARSE_ENERGY_WITH_INDICES:
		case DENSE_WEIGHT_WITH_INDICES:
		case NOT_DENSE_ENERGY_WITH_INDICES:
		case NOT_SPARSE_WEIGHT_WITH_INDICES:
		case SPARSE_ENERGY_DENSE_WEIGHT_WITH_INDICES:
			return _denseWeights[jointIndex];

		case ALL_ENERGY:
		case DENSE_ENERGY:
		case NOT_DENSE_WEIGHT:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case ALL_ENERGY_WITH_INDICES:
		case DENSE_ENERGY_WITH_INDICES:
		case NOT_DENSE_WEIGHT_WITH_INDICES:
		case DENSE_ENERGY_SPARSE_WEIGHT_WITH_INDICES:
			return energyToWeight(_denseEnergies[jointIndex]);

		case ALL_SPARSE:
		case SPARSE_WEIGHT:
		case ALL_SPARSE_WITH_INDICES:
		case SPARSE_WEIGHT_WITH_INDICES:
			sparseIndex = sparseIndexFromJointIndex(jointIndex);
			if (sparseIndex >= 0)
			{
				return _sparseWeights[sparseIndex];
			}
			break;

		case SPARSE_ENERGY:
		case SPARSE_ENERGY_WITH_INDICES:
			sparseIndex = sparseIndexFromJointIndex(jointIndex);
			if (sparseIndex >= 0)
			{
				return energyToWeight(_sparseEnergies[sparseIndex]);
			}
			break;
		}

		return 0.0;
	}
	
	@Override
	public final double getWeightForSparseIndex(int sparseIndex)
	{
		switch (_representation)
		{
		case DETERMINISTIC:
		case DETERMINISTIC_WITH_INDICES:
			return 1.0;
			
		case ALL_DENSE:
		case DENSE_WEIGHT:
		case ALL_DENSE_WITH_INDICES:
		case DENSE_WEIGHT_WITH_INDICES:
			setRepresentation(_representation | SPARSE_WEIGHT);
			return _sparseWeights[sparseIndex];
			// $FALL-THROUGH$
		case ALL_VALUES:
		case ALL_WEIGHT:
		case ALL_SPARSE:
		case NOT_SPARSE_ENERGY:
		case NOT_DENSE_WEIGHT:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case SPARSE_WEIGHT:
		case ALL:
		case ALL_WEIGHT_WITH_INDICES:
		case ALL_SPARSE_WITH_INDICES:
		case NOT_SPARSE_ENERGY_WITH_INDICES:
		case NOT_DENSE_WEIGHT_WITH_INDICES:
		case DENSE_ENERGY_SPARSE_WEIGHT_WITH_INDICES:
		case NOT_DENSE_ENERGY_WITH_INDICES:
		case SPARSE_WEIGHT_WITH_INDICES:
			return _sparseWeights[sparseIndex];
			
		case DENSE_ENERGY:
		case DENSE_ENERGY_WITH_INDICES:
			setRepresentation(_representation | SPARSE_ENERGY);
			// $FALL-THROUGH$
			return energyToWeight(_sparseEnergies[sparseIndex]);
		case ALL_ENERGY:
		case SPARSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY_DENSE_WEIGHT:
		case ALL_ENERGY_WITH_INDICES:
		case SPARSE_ENERGY_WITH_INDICES:
		case NOT_SPARSE_WEIGHT_WITH_INDICES:
		case SPARSE_ENERGY_DENSE_WEIGHT_WITH_INDICES:
			return energyToWeight(_sparseEnergies[sparseIndex]);
		}

		return 0.0;
	}
	
	@Override
	public final boolean hasDenseRepresentation()
	{
		return (_representation & ALL_DENSE) != 0;
	}
	
	@Override
	public final boolean hasDenseEnergies()
	{
		return (_representation & DENSE_ENERGY) != 0;
	}
	
	@Override
	public final boolean hasDenseWeights()
	{
		return (_representation & DENSE_WEIGHT) != 0;
	}

	@Override
	public boolean hasMaximumDensity()
	{
		return _nonZeroWeights == jointSize();
	}
	
	@Override
	public final boolean hasSparseRepresentation()
	{
		switch (_representation)
		{
		case DENSE_ENERGY:
		case DENSE_WEIGHT:
		case ALL_DENSE:
		case DENSE_ENERGY_WITH_INDICES:
		case DENSE_WEIGHT_WITH_INDICES:
		case ALL_DENSE_WITH_INDICES:
			return false;
			
		default:
			return true;
		}
	}
	
	@Override
	public final boolean isConditional()
	{
		if ((_computedMask & CONDITIONAL_COMPUTED) == 0)
		{
			if (isDirected())
			{
				normalizeDirected(true);
			}
			if ((_computedMask & CONDITIONAL) == 0)
			{
				// If its not conditional, it cannot be deterministic directed.
				_computedMask |= DETERMINISTIC_COMPUTED;
			}
			_computedMask |= CONDITIONAL_COMPUTED;
		}
		return (_computedMask & CONDITIONAL) != 0;
	}
	
	@Override
	public boolean isDeterministicDirected()
	{
		if ((_representation & ALL_VALUES) == DETERMINISTIC)
		{
			return true;
		}
		
		if ((_computedMask & DETERMINISTIC_COMPUTED) != 0)
		{
			return false;
		}
		
		boolean deterministic = false;
		
		final JointDomainIndexer domains = getDomainIndexer();
		if (isDirected() && _nonZeroWeights == domains.getInputCardinality())
		{
			// Table can only be deterministic if there is exactly one
			// valid output for each possible input and all outputs have the
			// same weight.
			final int[] sparseToJoint = computeSparseToJointIndexMap();
			deterministic = true;
			final int outputSize = domains.getOutputCardinality();
			int prevInputIndex = -1;
			for (int joint : sparseToJoint)
			{
				int inputIndex = joint / outputSize;
				if (inputIndex == prevInputIndex)
				{
					deterministic = false;
					break;
				}
				prevInputIndex = inputIndex;
			}

			if (deterministic && (_computedMask & CONDITIONAL) == 0)
			{
				// Ensure that weights are the same. No need to do this if CONDITIONAL.
				final double tolerance = 1e-12;
				if (hasSparseEnergies())
				{
					deterministic = ArrayUtil.allFuzzyEqual(_sparseEnergies, tolerance);
				}
				else if (hasSparseWeights())
				{
					deterministic = ArrayUtil.allFuzzyEqual(_sparseWeights, tolerance);
				}
				else if (hasDenseEnergies())
				{
					deterministic = ArrayUtil.subsetFuzzyEqual(_denseEnergies, sparseToJoint,  tolerance);
				}
				else
				{
					deterministic = ArrayUtil.subsetFuzzyEqual(_denseWeights, sparseToJoint,  tolerance);
				}
			}
			
			if (deterministic)
			{
				_sparseIndexToJointIndex = sparseToJoint;
				_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_representation = DETERMINISTIC | (_representation & SPARSE_INDICES);
				// deterministic directed is a special case of conditional
				_computedMask |= CONDITIONAL|CONDITIONAL_COMPUTED;
			}
		}
		_computedMask |= DETERMINISTIC_COMPUTED;
		
		return deterministic;
	}

	@Override
	public final void setEnergyForJointIndex(double energy, int jointIndex)
	{
		final double prevEnergy = getEnergyForJointIndex(jointIndex);
		if (prevEnergy != energy)
		{
			_computedMask = 0;
			if ((_representation & ALL_VALUES) == DETERMINISTIC)
			{
				// If we have sparse indices, then presumably a sparse representation is still wanted.
				setRepresentation(hasSparseIndices() ? ALL_ENERGY_WITH_INDICES : DENSE_ENERGY);
				_denseEnergies[jointIndex] = energy;
			}
			else
			{
				double weight = (_representation & ALL_WEIGHT) == 0 ? 0.0 : Utilities.energyToWeight(energy);
				setWeightEnergyForJointIndex(weight, energy, jointIndex);
			}
			
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
	public void setEnergyForSparseIndex(double energy, int sparseIndex)
	{
		final double prevEnergy = getEnergyForSparseIndex(sparseIndex);
		if (prevEnergy != energy)
		{
			_computedMask = 0;
			if ((_representation & ALL_VALUES) == DETERMINISTIC)
			{
				setRepresentation(_representation | SPARSE_ENERGY);
				_sparseEnergies[sparseIndex] = energy;
			}
			else
			{
				double weight = (_representation & ALL_WEIGHT) == 0 ? 0.0 : energyToWeight(energy);
				setWeightEnergyForSparseIndex(weight, energy, sparseIndex);
			}
			
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
		final double prevWeight = getWeightForJointIndex(jointIndex);
		if (prevWeight != weight)
		{
			_computedMask = 0;
			if ((_representation & ALL_VALUES) == DETERMINISTIC)
			{
				// If we have sparse indices, then presumably a sparse representation is still wanted.
				setRepresentation(hasSparseIndices() ? ALL_WEIGHT_WITH_INDICES : DENSE_WEIGHT);
				_denseWeights[jointIndex] = weight;
			}
			else
			{
				double energy = (_representation & ALL_ENERGY) == 0.0 ? 0.0 : weightToEnergy(weight);
				setWeightEnergyForJointIndex(weight, energy, jointIndex);
			}
			
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
	public void setWeightForSparseIndex(double weight, int sparseIndex)
	{
		final double prevWeight = getWeightForSparseIndex(sparseIndex);
		if (prevWeight != weight)
		{
			_computedMask = 0;
			if ((_representation & ALL_VALUES) == DETERMINISTIC)
			{
				setRepresentation(_representation | SPARSE_WEIGHT);
				_sparseWeights[sparseIndex] = weight;
			}
			else
			{
				double energy = (_representation & ALL_ENERGY) == 0 ? 0.0 : weightToEnergy(weight);
				setWeightEnergyForSparseIndex(weight, energy, sparseIndex);
			}
			
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
	public int[] sparseIndexToIndices(int sparseIndex, @Nullable int[] indices)
	{
		JointDomainIndexer indexer = getDomainIndexer();
		if ((_representation & SPARSE_INDICES) != 0)
		{
			indices = indexer.allocateIndices(indices);
			System.arraycopy(_sparseIndices[sparseIndex], 0, indices,  0, indices.length);
		}
		else
		{
			indices = indexer.jointIndexToIndices(sparseIndexToJointIndex(sparseIndex), indices);
		}
		return indices;
	}

	@Override
	public final int sparseIndexToJointIndex(int sparseIndex)
	{
		if (!hasSparseRepresentation())
		{
			if (hasDenseWeights())
			{
				setRepresentation(_representation | SPARSE_WEIGHT);
			}
			else
			{
				setRepresentation(_representation | SPARSE_ENERGY);
			}
		}
		
		if (sparseIndex < _sparseIndexToJointIndex.length)
		{
			sparseIndex = _sparseIndexToJointIndex[sparseIndex];
		}
		return sparseIndex;
	}
	
	@Override
	public final int sparseIndexFromJointIndex(int jointIndex)
	{
		if (sparseSize() == jointSize())
		{
			return jointIndex;
		}

		int sparseIndex = jointIndex;
		
		switch (_representation & ALL_VALUES)
		{
		case DETERMINISTIC:
			// Optimize deterministic case. Since there is exactly one entry per distinct
			// set of outputs, we can simply check to see if the jointIndex is found at
			// the corresponding location for the output indices.
			sparseIndex /= getDomainIndexer().getOutputCardinality();
			final int prevJointIndex = _sparseIndexToJointIndex[sparseIndex];
			if (prevJointIndex != jointIndex)
			{
				if (jointIndex > prevJointIndex)
				{
					++sparseIndex;
				}
				sparseIndex = -1-sparseIndex;
			}
			break;
		
		case ALL_DENSE:
		case DENSE_ENERGY:
		case DENSE_WEIGHT:
			setRepresentation(_representation | SPARSE_ENERGY);
			// $FALL-THROUGH$

		case ALL_VALUES:
		case ALL_SPARSE:
		case ALL_ENERGY:
		case ALL_WEIGHT:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case NOT_DENSE_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY:
		case SPARSE_ENERGY_DENSE_WEIGHT:
		case SPARSE_WEIGHT:
			sparseIndex = Arrays.binarySearch(_sparseIndexToJointIndex, jointIndex);
			break;
		}
		
		return sparseIndex;
	}
	
	@Override
	public final int sparseSize()
	{
		switch (_representation)
		{
		case DETERMINISTIC:
		case DETERMINISTIC_WITH_INDICES:
			return _sparseIndexToJointIndex.length;

		case DENSE_ENERGY:
		case DENSE_WEIGHT:
		case ALL_DENSE:
		case DENSE_ENERGY_WITH_INDICES:
		case DENSE_WEIGHT_WITH_INDICES:
		case ALL_DENSE_WITH_INDICES:
			return _nonZeroWeights;
			
		case SPARSE_ENERGY:
		case ALL_ENERGY:
		case SPARSE_ENERGY_DENSE_WEIGHT:
		case NOT_SPARSE_WEIGHT:
		case ALL_SPARSE:
		case NOT_DENSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case ALL_VALUES:
		case SPARSE_ENERGY_WITH_INDICES:
		case ALL_ENERGY_WITH_INDICES:
		case SPARSE_ENERGY_DENSE_WEIGHT_WITH_INDICES:
		case NOT_SPARSE_WEIGHT_WITH_INDICES:
		case ALL_SPARSE_WITH_INDICES:
		case NOT_DENSE_WEIGHT_WITH_INDICES:
		case NOT_DENSE_ENERGY_WITH_INDICES:
		case ALL:
			return _sparseEnergies.length;
			
		case SPARSE_WEIGHT:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case ALL_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case SPARSE_WEIGHT_WITH_INDICES:
		case DENSE_ENERGY_SPARSE_WEIGHT_WITH_INDICES:
		case ALL_WEIGHT_WITH_INDICES:
		case NOT_SPARSE_ENERGY_WITH_INDICES:
			return _sparseWeights.length;
		}
		
		return 0;
	}
	
	
	/*--------------------------
	 * INewFactorTable methods
	 */
	
	@Override
	public int compact()
	{
		int nRemoved = 0;
		
		if ((_representation & ALL_SPARSE) != 0)
		{
			final int curSparseSize = sparseSize();
			if (curSparseSize > _nonZeroWeights)
			{
				nRemoved = curSparseSize - _nonZeroWeights;
				
				final boolean wasDense = curSparseSize == jointSize();
				final int[] sparseToJoint = new int[_nonZeroWeights];
				
				final boolean hasEnergy = hasSparseEnergies();
				final double[] sparseEnergies = hasEnergy ? new double[_nonZeroWeights] : ArrayUtil.EMPTY_DOUBLE_ARRAY;
				
				final boolean hasWeight = hasSparseWeights();
				final double[] sparseWeights = hasWeight ? new double[_nonZeroWeights] : ArrayUtil.EMPTY_DOUBLE_ARRAY;
				
				final boolean hasIndices = hasSparseIndices();
				final int[][] sparseIndices = hasIndices ? new int[_nonZeroWeights][] : ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
				
				if (hasWeight)
				{
					for (int i = 0, j = 0; i < curSparseSize; ++i)
					{
						double w = _sparseWeights[i];
						if (w != 0.0)
						{
							sparseWeights[j] = w;
							if (hasEnergy)
							{
								sparseEnergies[j] = _sparseEnergies[i];
							}
							if (hasIndices)
							{
								sparseIndices[j] = _sparseIndices[i];
							}
							sparseToJoint[j] = wasDense? i : _sparseIndexToJointIndex[i];
							++j;
						}
					}
				}
				else
				{
					for (int i = 0, j = 0; i < curSparseSize; ++i)
					{
						double e = _sparseEnergies[i];
						if (!Double.isInfinite(e))
						{
							sparseEnergies[j] = e;
							sparseToJoint[j] = wasDense? i : _sparseIndexToJointIndex[i];
							if (hasIndices)
							{
								sparseIndices[j] = _sparseIndices[i];
							}
							++j;
						}
					}
				}
				
				_sparseEnergies = sparseEnergies;
				_sparseWeights = sparseWeights;
				_sparseIndexToJointIndex = sparseToJoint;
				_sparseIndices = sparseIndices;
			}
		}
		
		return nRemoved;
	}
	
	@Override
	public final double[] getEnergiesSparseUnsafe()
	{
		if (_sparseEnergies.length == 0 && !hasSparseEnergies())
		{
			if (hasDeterministicRepresentation())
			{
				_sparseEnergies = new double[getDomainIndexer().getInputCardinality()];
			}
			else
			{
				setRepresentation(_representation | SPARSE_ENERGY);
			}
		}
		return _sparseEnergies;
	}
	
	@Override
	public final double[] getEnergySlice(@Nullable double[] slice, int sliceDimension, int ... indices)
	{
		JointDomainIndexer indexer = getDomainIndexer();
		final int savedIndex = indices[sliceDimension];
		indices[sliceDimension] = 0;
		final int start = indexer.jointIndexFromIndices(indices);
		indices[sliceDimension] = savedIndex;

		return getEnergySliceImpl(slice, sliceDimension, start);
	}
	
	@Override
	public final double[] getEnergySlice(@Nullable double[] slice, int sliceDimension, Value ... values)
	{
		JointDomainIndexer indexer = getDomainIndexer();
		final int savedIndex = values[sliceDimension].getIndex();
		values[sliceDimension].setIndex(0);
		final int start = indexer.jointIndexFromValues(values);
		values[sliceDimension].setIndex(savedIndex);

		return getEnergySliceImpl(slice, sliceDimension, start);
	}

	private final double[] getEnergySliceImpl(@Nullable double[] slice, int sliceDimension, int start)
	{
		final JointDomainIndexer indexer = getDomainIndexer();
		final int size = indexer.getDomainSize(sliceDimension);
		final int stride = indexer.getStride(sliceDimension);

		if (slice == null || slice.length < size)
		{
			slice = new double[size];
		}
		
		if (hasDenseEnergies())
		{
			for (int i = 0, ji = start; i < size; ++i, ji += stride)
			{
				slice[i] = _denseEnergies[ji];
			}
		}
		else
		{
			for (int i = 0, ji = start; i < size; ++i, ji += stride)
			{
				slice[i] = getEnergyForJointIndex(ji);
			}
		}
		
		return slice;
	}
	
	@Override
	public final double[] getWeightSlice(@Nullable double[] slice, int sliceDimension, int ... indices)
	{
		JointDomainIndexer indexer = getDomainIndexer();
		final int savedIndex = indices[sliceDimension];
		indices[sliceDimension] = 0;
		final int start = indexer.jointIndexFromIndices(indices);
		indices[sliceDimension] = savedIndex;

		return getWeightSliceImpl(slice, sliceDimension, start);
	}
	
	@Override
	public final double[] getWeightSlice(@Nullable double[] slice, int sliceDimension, Value ... values)
	{
		JointDomainIndexer indexer = getDomainIndexer();
		final int savedIndex = values[sliceDimension].getIndex();
		values[sliceDimension].setIndex(0);
		final int start = indexer.jointIndexFromValues(values);
		values[sliceDimension].setIndex(savedIndex);

		return getEnergySliceImpl(slice, sliceDimension, start);
	}

	private final double[] getWeightSliceImpl(@Nullable double[] slice, int sliceDimension, int start)
	{
		final JointDomainIndexer indexer = getDomainIndexer();
		final int size = indexer.getDomainSize(sliceDimension);
		final int stride = indexer.getStride(sliceDimension);

		if (slice == null || slice.length < size)
		{
			slice = new double[size];
		}
		
		if (hasDenseWeights())
		{
			for (int i = 0, ji = start; i < size; ++i, ji += stride)
			{
				slice[i] = _denseWeights[ji];
			}
		}
		else
		{
			for (int i = 0, ji = start; i < size; ++i, ji += stride)
			{
				slice[i] = getWeightForJointIndex(ji);
			}
		}
		
		return slice;
	}

	@Override
	public final double[] getWeightsSparseUnsafe()
	{
		if (_sparseWeights.length == 0 && !hasSparseWeights())
		{
			if (hasDeterministicRepresentation())
			{
				_sparseWeights = new double[getDomainIndexer().getInputCardinality()];
				Arrays.fill(_sparseWeights, 1.0);
			}
			else
			{
				setRepresentation(_representation | SPARSE_WEIGHT);
			}
		}
		return _sparseWeights;
	}

	@Override
	public final double[] getWeightsDenseUnsafe()
	{
		if (!hasDenseWeights())
		{
			setRepresentation(DENSE_WEIGHT);
		}	
		return _denseWeights;
	}
	
	@Override
	public final int[][] getIndicesSparseUnsafe()
	{
		if (!hasSparseIndices())
		{
			if (hasSparseRepresentation())
			{
				setRepresentation(_representation | SPARSE_INDICES);
			}
			else if (hasDenseWeights())
			{
				setRepresentation(_representation | SPARSE_WEIGHT_WITH_INDICES);
			}
			else
			{
				setRepresentation(_representation | SPARSE_ENERGY_WITH_INDICES);
			}
		}
		return _sparseIndices;
	}
	
	@Override
	public boolean hasDeterministicRepresentation()
	{
		return (_representation & ALL_VALUES) == DETERMINISTIC;
	}
	
	@Override
	public void setEnergiesDense(double[] energies)
	{
		setDenseValues(energies, DENSE_ENERGY);
	}
	
	@Override
	public void setWeightsDense(double[] weights)
	{
		setDenseValues(weights, DENSE_WEIGHT);
	}
	
	@Override
	public void setDeterministicOutputIndices(int[] outputIndices)
	{
		final JointDomainIndexer domains = getDomainIndexer();
		final int size = domains.getInputCardinality();
		
		if (!isDirected())
		{
			throw new UnsupportedOperationException(
				"'setDeterministicOuputIndices' not supported on non-directed table");
		}
		
		if (size != outputIndices.length)
		{
			throw new IllegalArgumentException(
				String.format("'ouputIndices' array length %d does not match size of possible inputs %d",
				outputIndices.length, size));
		}
		
		int[] sparseToJoint = new int[size];
		final int outputCardinality = domains.getOutputCardinality();
		for (int inputIndex = 0; inputIndex < size; ++inputIndex)
		{
			final int outputIndex = outputIndices[inputIndex];
			if (outputIndex < 0 || outputIndex >= outputCardinality)
			{
				throw new IllegalArgumentException(String.format("Output index %d is out of range", outputIndex));
			}
			sparseToJoint[inputIndex] = domains.jointIndexFromInputOutputIndices(inputIndex, outputIndex);
		}
		
		_sparseIndexToJointIndex = sparseToJoint;
		_representation = DETERMINISTIC;
		_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_nonZeroWeights = size;
		_computedMask = NORMALIZED | DETERMINISTIC_COMPUTED;
	}
	
	@Override
	public void setEnergiesSparse(int[] jointIndices, double[] energies)
	{
		setSparseValues(jointIndices, energies, SPARSE_ENERGY);
	}
	
	@Override
	public void setWeightsSparse(int[] jointIndices, double[] weights)
	{
		setSparseValues(jointIndices, weights, SPARSE_WEIGHT);
	}
	
	@Override
	void setRepresentation(int newRep)
	{
		int oldRep = _representation;
		
		if (oldRep == newRep)
		{
			return;
		}
		
		//
		// Disallow non-sparse rep combined with sparse indices
		//
		
		if ((newRep & ALL_DENSE_WITH_INDICES) == newRep && (newRep & ALL_VALUES) != DETERMINISTIC)
		{
			newRep &= ALL_DENSE;
		}
		
		//
		// Special cases for deterministic conversions
		//
		
		if ((newRep & ALL_VALUES) == DETERMINISTIC)
		{
			if (!isDeterministicDirected())
			{
				throw new DimpleException("Cannot set representation to DETERMINISTIC*");
			}
			if ((newRep & SPARSE_INDICES) != 0)
			{
				_sparseIndices = computeSparseIndices();
			}
			else
			{
				_sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
			}
			_representation = newRep;
			return;
		}
		
		final int jointSize = jointSize();
		
		if ((oldRep & ALL_VALUES) == DETERMINISTIC)
		{
			if ((newRep & SPARSE_WEIGHT) != 0)
			{
				_sparseWeights = new double[sparseSize()];
				Arrays.fill(_sparseWeights, 1.0);
			}
			if ((newRep & SPARSE_ENERGY) != 0)
			{
				_sparseEnergies = new double[sparseSize()];
			}
			if ((newRep & DENSE_WEIGHT) != 0)
			{
				_denseWeights = new double[jointSize];
				for (int ji : _sparseIndexToJointIndex)
				{
					_denseWeights[ji] = 1.0;
				}
					
			}
			if ((newRep & DENSE_ENERGY) != 0)
			{
				_denseEnergies = new double[jointSize];
				Arrays.fill(_denseEnergies, Double.POSITIVE_INFINITY);
				for (int ji : _sparseIndexToJointIndex)
				{
					_denseEnergies[ji] = 0.0;
				}
			}
			if ((newRep & SPARSE_INDICES) != 0 && (oldRep & SPARSE_INDICES) == 0)
			{
				_sparseIndices = getIndicesSparseUnsafe();
			}
			if ((newRep & ALL_SPARSE) == 0)
			{
				_sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
			}
			if ((newRep & SPARSE_INDICES) == 0)
			{
				_sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
			}
			_representation = newRep;
			_computedMask = 0;
			return;
		}
		
		//
		// Dense-to-sparse conversion
		//
		
		int diff = newRep & ~oldRep;
		if ((diff & ALL_SPARSE) != 0 && (oldRep & ALL_SPARSE) == 0)
		{
			if (_nonZeroWeights == jointSize)
			{
				// sparse == dense
				// dense == sparse, use same arrays if possible
				if ((diff & SPARSE_WEIGHT) != 0 && (oldRep & DENSE_WEIGHT) != 0)
				{
					_sparseWeights = _denseWeights;
					oldRep |= SPARSE_WEIGHT;
				}
				if ((diff & SPARSE_ENERGY) != 0 && (oldRep & DENSE_ENERGY) != 0)
				{
					_sparseEnergies = _denseEnergies;
					oldRep |= SPARSE_ENERGY;
				}
				diff = newRep & ~oldRep;
			}
			
			if ((diff & ALL_SPARSE) != 0)
			{
				final int[] sparseToJoint = _sparseIndexToJointIndex = computeSparseToJointIndexMap();
				final int sparseSize = sparseToJoint.length;

				if ((diff & SPARSE_WEIGHT) != 0)
				{
					final double[] sparseWeights = _sparseWeights = new double[sparseSize];
					if ((oldRep & DENSE_WEIGHT) != 0)
					{
						final double[] denseWeights = _denseWeights;
						for (int si = sparseSize; --si>=0;)
						{
							sparseWeights[si] = denseWeights[sparseToJoint[si]];
						}
					}
					else
					{
						final double[] denseEnergies = _denseEnergies;
						for (int si = sparseSize; --si>=0;)
						{
							sparseWeights[si] = Utilities.energyToWeight(denseEnergies[sparseToJoint[si]]);
						}
					}
					oldRep |= SPARSE_WEIGHT;
				}
				if ((diff & SPARSE_ENERGY) != 0)
				{
					final double[] sparseEnergies = _sparseEnergies = new double[sparseToJoint.length];
					if ((oldRep & DENSE_ENERGY) != 0)
					{
						final double[] denseEnergies = _denseEnergies;
						for (int si = sparseSize; --si>=0;)
						{
							sparseEnergies[si] = denseEnergies[sparseToJoint[si]];
						}
					}
					else
					{
						final double[] denseWeights = _denseWeights;
						for (int si = sparseSize; --si>=0;)
						{
							sparseEnergies[si] = Utilities.weightToEnergy(denseWeights[sparseToJoint[si]]);
						}
					}
					oldRep |= SPARSE_ENERGY;
				}
			}
		}
		
		final int[] sparseToJoint = _sparseIndexToJointIndex;
		
		//
		// Compute sparse indices
		//
		
		if ((newRep & SPARSE_INDICES) != 0 && (oldRep & SPARSE_INDICES) == 0)
		{
			
			_sparseIndices = computeSparseIndices();
			oldRep |= SPARSE_INDICES;
		}
		
		//
		// Sparse-to-sparse conversions
		//

		diff &= ~oldRep;
		if ((diff & ALL_SPARSE) != 0)
		{
			if ((diff & SPARSE_ENERGY) != 0 & (oldRep & SPARSE_WEIGHT) != 0)
			{
				final double[] sparseWeights = _sparseWeights;
				final double[] sparseEnergies = _sparseEnergies = new double[sparseWeights.length];
				for (int i = sparseWeights.length; --i >= 0;)
				{
					sparseEnergies[i] = Utilities.weightToEnergy(sparseWeights[i]);
				}
				oldRep |= SPARSE_ENERGY;
			}
			else if ((diff & SPARSE_WEIGHT) != 0 & (oldRep & SPARSE_ENERGY) != 0)
			{
				final double[] sparseEnergies = _sparseEnergies;
				final double[] sparseWeights = _sparseWeights = new double[sparseEnergies.length];
				for (int i = sparseEnergies.length; --i >= 0;)
				{
					sparseWeights[i] = Utilities.energyToWeight(sparseEnergies[i]);
				}
				oldRep |= SPARSE_WEIGHT;
			}
		}
		
		//
		// *-to-dense conversions
		//
		
		diff &= ~oldRep;
		if ((diff & ALL_DENSE) != 0)
		{
			if ((oldRep & ALL_SPARSE) != 0)
			{
				if ((diff & DENSE_ENERGY) != 0)
				{
					if ((oldRep & SPARSE_ENERGY) != 0)
					{
						if (jointSize == sparseSize())
						{
							_denseEnergies = _sparseEnergies;
						}
						else
						{
							final double[] sparseEnergies = _sparseEnergies;
							final double[] denseEnergies = _denseEnergies = new double[jointSize];
							Arrays.fill(denseEnergies, Double.POSITIVE_INFINITY);
							for (int si = sparseToJoint.length; --si >= 0;)
							{
								denseEnergies[sparseToJoint[si]] = sparseEnergies[si];
							}
						}
					}
					else
					{
						assert((oldRep & SPARSE_WEIGHT) != 0);
						final double[] sparseWeights = _sparseWeights;
						final double[] denseEnergies = _denseEnergies = new double[jointSize];
						Arrays.fill(_denseEnergies, Double.POSITIVE_INFINITY);
						if (denseEnergies.length == sparseWeights.length)
						{
							for (int di = denseEnergies.length; --di >=0;)
							{
								denseEnergies[di] = Utilities.weightToEnergy(sparseWeights[di]);
							}
						}
						else
						{
							for (int si = sparseToJoint.length; --si >= 0;)
							{
								denseEnergies[sparseToJoint[si]] = Utilities.weightToEnergy(sparseWeights[si]);
							}
						}
					}
					oldRep |= DENSE_ENERGY;
				}
				if ((diff & DENSE_WEIGHT) != 0)
				{
					if ((oldRep & SPARSE_WEIGHT) != 0)
					{
						if (jointSize == sparseSize())
						{
							_denseWeights = _sparseWeights;
						}
						else
						{
							final double[] sparseWeights = _sparseWeights;
							final double[] denseWeights = _denseWeights = new double[jointSize];
							for (int si = sparseToJoint.length; --si >= 0;)
							{
								denseWeights[sparseToJoint[si]] = sparseWeights[si];
							}
						}
					}
					else
					{
						assert((oldRep & SPARSE_ENERGY) != 0);
						final double[] sparseEnergies = _sparseEnergies;
						final double[] denseWeights = _denseWeights = new double[jointSize];
						if (denseWeights.length == sparseEnergies.length)
						{
							for(int di = denseWeights.length; --di>=0;)
							{
								denseWeights[di] = Utilities.energyToWeight(sparseEnergies[di]);
							}
						}
						else
						{
							for (int si = sparseToJoint.length; -- si >= 0;)
							{
								denseWeights[sparseToJoint[si]] = Utilities.energyToWeight(sparseEnergies[si]);
							}
						}
					}
					oldRep |= DENSE_WEIGHT;
				}
			}
			else
			{
				if ((diff & DENSE_ENERGY) != 0)
				{
					final double[] denseWeights = _denseWeights;
					final double[] denseEnergies = _denseEnergies = new double[jointSize];
					for (int i = 0; i < jointSize; ++i)
					{
						denseEnergies[i] = Utilities.weightToEnergy(denseWeights[i]);
					}
					oldRep |= DENSE_ENERGY;
				}
				else
				{
					final double[] denseEnergies = _denseEnergies;
					final double[] denseWeights = _denseWeights = new double[jointSize];
					for (int i = 0; i < jointSize; ++i)
					{
						denseWeights[i] = Utilities.energyToWeight(denseEnergies[i]);
					}
					oldRep |= DENSE_WEIGHT;
				}
			}
		}
		
		assert((newRep & ~oldRep) == 0);
		
		//
		// Remove old arrays
		//
		
		if ((newRep & SPARSE_ENERGY) == 0)
		{
			_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		if ((newRep & SPARSE_WEIGHT) == 0)
		{
			_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		if ((newRep & DENSE_ENERGY) == 0)
		{
			_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		if ((newRep & DENSE_WEIGHT) == 0)
		{
			_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		if ((newRep & ALL_SPARSE) == 0)
		{
			_sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
		}
		if ((newRep & SPARSE_INDICES) == 0)
		{
			_sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
		}
		
		_representation = newRep;
	}
	

	/*----------------------
	 * Old IFactorTable methods
	 */
	
	public FactorTable copy()
	{
		return clone();
	}

	@Override
	public void copy(IFactorTable that)
	{
		if (that == this)
		{
			return;
		}

		if (!getDomainIndexer().domainsEqual(that.getDomainIndexer()))
		{
			throw new DimpleException("Cannot copy from factor table with different domains");
		}
		
		if (that instanceof FactorTable)
		{
			FactorTable other = (FactorTable)that;
			_nonZeroWeights = other._nonZeroWeights;
			_representation = other._representation;
			_denseEnergies = Objects.requireNonNull(ArrayUtil.cloneArray(other._denseEnergies));
			_denseWeights = Objects.requireNonNull(ArrayUtil.cloneArray(other._denseWeights));
			_sparseEnergies = Objects.requireNonNull(ArrayUtil.cloneArray(other._sparseEnergies));
			_sparseWeights = Objects.requireNonNull(ArrayUtil.cloneArray(other._sparseWeights));
			_sparseIndexToJointIndex = Objects.requireNonNull(ArrayUtil.cloneArray(other._sparseIndexToJointIndex));
			_sparseIndices = Objects.requireNonNull(ArrayUtil.cloneArray(other._sparseIndices));
			_computedMask = other._computedMask;
		}
		else
		{
			_representation = that.getRepresentation().mask();
			_nonZeroWeights = that.countNonZeroWeights();
			_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
			_sparseIndices = ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
			_computedMask = 0;

			if (that.hasSparseRepresentation())
			{
				final int size = that.sparseSize();
				_sparseIndexToJointIndex = new int[size];
				for (int si = 0; si < size; ++si)
				{
					_sparseIndexToJointIndex[si] = that.sparseIndexToJointIndex(si);
				}
				if (that.hasSparseEnergies())
				{
					_sparseEnergies = that.getEnergiesSparseUnsafe().clone();
				}
				if (that.hasSparseWeights())
				{
					_sparseWeights = that.getWeightsSparseUnsafe().clone();
				}
				if (that.hasSparseIndices())
				{
					_sparseIndices = that.getIndicesSparseUnsafe().clone();
				}
			}
			
			if (that.hasDenseRepresentation())
			{
				final int size = that.jointSize();
				if (that.hasDenseEnergies())
				{
					_denseEnergies = new double[size];
					for (int ji = 0; ji < size; ++ji)
					{
						_denseEnergies[ji] = that.getEnergyForJointIndex(ji);
					}
				}
				if (that.hasDenseWeights())
				{
					_denseWeights = new double[size];
					for (int ji = 0; ji < size; ++ji)
					{
						_denseEnergies[ji] = that.getWeightForJointIndex(ji);
					}
				}
			}
		}
	}

	// FIXME: what to do if table is directed? Should we assert that the joined
	// variables are all either inputs or outputs?
	@Override
	public FactorTable joinVariablesAndCreateNewTable(
		int[] varIndices,
		int[] indexToJointIndex,
		DiscreteDomain[] allDomains,
		DiscreteDomain jointDomain)
	{
		final JointDomainIndexer domains = getDomainIndexer();
		final JointDomainReindexer converter =
			makeConverterForJoinVariables(domains, varIndices, indexToJointIndex, allDomains, jointDomain);
		return new FactorTable(this, converter);
	}
	
	static JointDomainReindexer makeConverterForJoinVariables(
		JointDomainIndexer domains,
		int[] varIndices,
		int[] indexToJointIndex,
		DiscreteDomain[] allDomains,
		DiscreteDomain jointDomain)
	{
		assert(Arrays.equals(allDomains, domains.toArray()));
		assert(varIndices.length == indexToJointIndex.length);
		
		// Build a domain converter by first permuting joined domains to proper order at
		// end of domain list, and then by doing the join.
		
		final int joinedSize = varIndices.length;
		final int unjoinedSize = domains.size() - joinedSize;
		final int[] permutation = new int[domains.size()];
		Arrays.fill(permutation, -1);
		
		// Compute the mappings for the joined variables to the end of the list
		for (int i = 0; i < joinedSize; ++i)
		{
			permutation[varIndices[i]] = unjoinedSize + indexToJointIndex[i];
		}
		
		// and the remaining unjoined variables at the front of the list.
		int to = 0;
		for (int i = 0; i < permutation.length; ++i)
		{
			if (permutation[i] < 0)
			{
				permutation[i] = to++;
			}
		}
		
		// See if permutation actually changes anything.
		boolean identityMap = true;
		for (int i = permutation.length; --i>=0;)
		{
			if (permutation[i] != i)
			{
				identityMap = false;
				break;
			}
		}
		
		JointDomainReindexer converter = null;
		if (!identityMap)
		{
			DiscreteDomain[] toDomains = new DiscreteDomain[permutation.length];
			for (int i = permutation.length; --i>=0;)
			{
				toDomains[permutation[i]] = domains.get(i);
			}
			converter = createPermuter(domains, JointDomainIndexer.create(toDomains), permutation);
		}
		
		if (converter != null)
		{
			converter = converter.combineWith(createJoiner(converter.getToDomains(), unjoinedSize, joinedSize));
		}
		else
		{
			converter = createJoiner(domains, unjoinedSize, joinedSize);
		}

		return converter;
	}

	/*-----------------
	 * Private methods
	 */
	
	private int allocateSparseIndexForJointIndex(int jointIndex)
	{
		final int representation = _representation;
		
		int sparseIndex = sparseIndexFromJointIndex(jointIndex);
		if (sparseIndex < 0)
		{
			sparseIndex = -1-sparseIndex;
			if ((representation & SPARSE_ENERGY) != 0)
			{
				_sparseEnergies = ArrayUtil.copyArrayForInsert(_sparseEnergies, sparseIndex, 1);
			}
			if ((representation & SPARSE_WEIGHT) != 0)
			{
				_sparseWeights = ArrayUtil.copyArrayForInsert(_sparseWeights, sparseIndex, 1);
			}
			if ((representation & SPARSE_INDICES) != 0)
			{
				_sparseIndices = ArrayUtil.copyArrayForInsert(_sparseIndices,  sparseIndex, 1);
				_sparseIndices[sparseIndex] = getDomainIndexer().jointIndexToIndices(jointIndex);
			}
			_sparseIndexToJointIndex = ArrayUtil.copyArrayForInsert(_sparseIndexToJointIndex, sparseIndex, 1);
			_sparseIndexToJointIndex[sparseIndex] = jointIndex;
		}
		
		return sparseIndex;
	}
	
	private void computeNonZeroWeights()
	{
		int count = 0;
		switch (_representation & ALL_VALUES)
		{
		case DETERMINISTIC:
			_nonZeroWeights = _sparseIndexToJointIndex.length;
			break;
			
		case SPARSE_WEIGHT:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case ALL_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case ALL_SPARSE:
		case NOT_DENSE_ENERGY:
		case NOT_DENSE_WEIGHT:
		case ALL_VALUES:
			for (double w : _sparseWeights)
				if (w != 0)
					++count;
			break;
			
		case SPARSE_ENERGY:
		case ALL_ENERGY:
		case SPARSE_ENERGY_DENSE_WEIGHT:
		case NOT_SPARSE_WEIGHT:
			for (double e : _sparseEnergies)
				if (!Double.isInfinite(e))
					++count;
			break;
			
		case DENSE_WEIGHT:
		case ALL_DENSE:
			for (double w : _denseWeights)
				if (w != 0)
					++count;
			break;
			
		case DENSE_ENERGY:
			for (double e : _denseEnergies)
				if (!Double.isInfinite(e))
					++count;
			break;
		}
		
		_nonZeroWeights = count;
	}
	
	private int[][] computeSparseIndices()
	{
		final JointDomainIndexer indexer = getDomainIndexer();
		final int jointSize = indexer.getCardinality();
		final int sparseSize = this.sparseSize();
		final int[] sparseToJoint = _sparseIndexToJointIndex;
		final int[][] sparseIndices = new int[sparseSize][];

		if (sparseSize < jointSize)
		{
			for (int si = 0; si < sparseSize; ++si)
			{
				sparseIndices[si] = indexer.jointIndexToIndices(sparseToJoint[si]);
			}
		}
		else
		{
			for (int ji = 0; ji < jointSize; ++ji)
			{
				sparseIndices[ji] = indexer.jointIndexToIndices(ji);
			}
		}
		return sparseIndices;
	}
	
	private int[] computeSparseToJointIndexMap()
	{
		if (_sparseIndexToJointIndex.length > 0)
		{
			return _sparseIndexToJointIndex;
		}
		
		final int jointSize = jointSize();
		final int[] map = new int[_nonZeroWeights];

		if ((_representation & ALL_WEIGHT) != 0)
		{
			final double[] denseWeights = hasDenseWeights() ? _denseWeights : _sparseWeights;
			assert(denseWeights.length == jointSize);
			for (int di = 0, si = 0; si < map.length; ++di)
			{
				if (denseWeights[di] != 0.0)
				{
					map[si++] = di;
				}
			}
		}
		else
		{
			final double[] denseEnergies = hasDenseEnergies() ? _denseEnergies : _sparseEnergies;
			assert(denseEnergies.length == jointSize);
			for (int di = 0, si = 0; di < jointSize; ++di)
			{
				if (!Double.isInfinite(denseEnergies[di]))
				{
					map[si++] = di;
				}
			}
		}
		
		return map;
	}
	
	@Override
	boolean normalizeDirected(boolean justCheck)
	{
		final JointDomainIndexer domains = getDomainIndexer();
		final int inputSize = domains.getInputCardinality();
		final int outputSize = domains.getOutputCardinality();
		final boolean hasSparseToJoint = _sparseIndexToJointIndex.length > 0;
		
		boolean computeNormalizedTotal = justCheck;
		double normalizedTotal = 1.0;
		double totalForInput = 0.0;
		
		final double[] normalizers = justCheck ? null : new double[inputSize];
		
		// We represent the joint index such that the outputs for the same
		// input are stored consecutively, so we only need to walk through
		// the values in order.
		//
		// When just checking, we allow total to equal something other than one
		// as long as they are all the same.
		
		switch (_representation & ALL_VALUES)
		{
		case DETERMINISTIC:
			break;
			
		case ALL_VALUES:
		case ALL_WEIGHT:
		case ALL_SPARSE:
		case NOT_DENSE_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case SPARSE_WEIGHT:
			for (int ii = 0, si = 0, size = _sparseWeights.length; ii < inputSize; ++ii)
			{
				final int maxji = domains.jointIndexFromInputOutputIndices(ii, outputSize-1);

				totalForInput = 0.0;
				for (; si < size && maxji >= (hasSparseToJoint ? _sparseIndexToJointIndex[si] : si); ++si)
				{
					totalForInput += _sparseWeights[si];
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
				if (normalizers != null)
				{
					normalizers[ii] = totalForInput;
				}
			}
			if (normalizers != null)
			{
				for (int si = 0, size = _sparseWeights.length; si < size; ++si)
				{
					final int ji = hasSparseToJoint ? _sparseIndexToJointIndex[si] : si;
					final int ii = domains.inputIndexFromJointIndex(ji);
					setWeightForSparseIndex(_sparseWeights[si] / normalizers[ii], si);
				}
			}
			break;
			
		case ALL_ENERGY:
		case SPARSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY_DENSE_WEIGHT:
			// TODO: if sparse size is large enough, it would be faster to iterate over the dense weights
			for (int ii = 0, si = 0, size = _sparseEnergies.length; ii < inputSize; ++ii)
			{
				final int maxji = domains.jointIndexFromInputOutputIndices(ii, outputSize-1);

				totalForInput = 0.0;
				for (; si < size && maxji >= (hasSparseToJoint ? _sparseIndexToJointIndex[si] : si); ++si)
				{
					totalForInput += energyToWeight(_sparseEnergies[si]);
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
				if (normalizers != null)
				{
					normalizers[ii] = Math.log(totalForInput);
				}
			}
			if (normalizers != null)
			{
				for (int si = 0, size = _sparseEnergies.length; si < size; ++si)
				{
					final int ji = hasSparseToJoint ? _sparseIndexToJointIndex[si] : si;
					final int ii = domains.inputIndexFromJointIndex(ji);
					setEnergyForSparseIndex(_sparseEnergies[si] + normalizers[ii], si);
				}
			}
			break;
			
			
		case ALL_DENSE:
		case DENSE_WEIGHT:
			for (int jointIndex = 0, inputIndex = 0; inputIndex < inputSize; ++inputIndex, jointIndex += outputSize)
			{
				totalForInput = 0.0;
				for (int outputIndex = 0; outputIndex < outputSize; ++outputIndex)
				{
					totalForInput += _denseWeights[jointIndex + outputIndex];
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
				if (normalizers != null)
				{
					normalizers[inputIndex] = totalForInput;
				}
			}
			if (normalizers != null)
			{
				for (int jointIndex = 0, inputIndex = 0; inputIndex < inputSize; ++inputIndex, jointIndex += outputSize)
				{
					totalForInput = normalizers[inputIndex];
					for (int outputIndex = 0; outputIndex < outputSize; ++outputIndex)
					{
						int ji = jointIndex + outputIndex;
						setWeightForJointIndex(_denseWeights[ji] / totalForInput, ji);
					}
				}
			}
			break;
			
		case DENSE_ENERGY:
			for (int jointIndex = 0, inputIndex = 0; inputIndex < inputSize; ++inputIndex, jointIndex += outputSize)
			{
				totalForInput = 0.0;
				for (int outputIndex = 0; outputIndex < outputSize; ++outputIndex)
				{
					totalForInput += energyToWeight(_denseEnergies[jointIndex + outputIndex]);
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
				if (normalizers != null)
				{
					normalizers[inputIndex] = Math.log(totalForInput);
				}
			}
			if (normalizers != null)
			{
				for (int jointIndex = 0, inputIndex = 0; inputIndex < inputSize; ++inputIndex, jointIndex += outputSize)
				{
					for (int outputIndex = 0; outputIndex < outputSize; ++outputIndex)
					{
						int ji = jointIndex + outputIndex;
						setEnergyForJointIndex(_denseEnergies[ji] + normalizers[inputIndex], ji);
					}
				}
			}
			break;
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
		switch (_representation & ALL_VALUES)
		{
		case ALL_VALUES:
		case ALL_WEIGHT:
		case ALL_SPARSE:
		case NOT_DENSE_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case SPARSE_WEIGHT:
			for (double w : _sparseWeights)
			{
				total += w;
			}
			break;
			
		case ALL_ENERGY:
		case SPARSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY_DENSE_WEIGHT:
			// TODO: if sparse size is large enough, it would be faster to iterate over the dense weights
			for (double e: _sparseEnergies)
			{
				total += energyToWeight(e);
			}
			break;
			
		case ALL_DENSE:
		case DENSE_WEIGHT:
			for (double w : _denseWeights)
			{
				total += w;
			}
			break;
			
		case DENSE_ENERGY:
			for (double e : _denseEnergies)
			{
				total += energyToWeight(e);
			}
			break;
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
			if (_sparseWeights != _denseWeights)
			{
				for (int i = _denseWeights.length; --i>=0;)
				{
					_denseWeights[i] /= total;
				}
			}
			final double logTotal = Math.log(total);
			for (int i = _sparseEnergies.length; --i>=0;)
			{
				_sparseEnergies[i] += logTotal;
			}
			if (_sparseEnergies != _denseEnergies)
			{
				for (int i = _denseEnergies.length; --i>=0;)
				{
					_denseEnergies[i] += logTotal;
				}
			}
		}

		_computedMask |= NORMALIZED|NORMALIZED_COMPUTED;
		return true;
	}

	private void setDenseValues(double[] values, int representation)
	{
		final JointDomainIndexer domains = getDomainIndexer();
		if (values.length != domains.getCardinality())
		{
			throw new IllegalArgumentException(String.format("Bad dense length: was %d, expected %d",
				values.length, domains.getCardinality()));
		}
		
		_computedMask = 0;
		
		switch(representation)
		{
		case DENSE_ENERGY:
			_denseEnergies = values.clone();
			_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			break;
		case DENSE_WEIGHT:
			_denseWeights = values.clone();
			_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			break;
		default:
			assert(false);
		}
		_representation = representation;
		
		_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
		computeNonZeroWeights();
	}

	@Override
	void setDirected(@Nullable BitSet outputSet, boolean assertConditional)
	{
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
		
		final int oldComputedMask = _computedMask;
		final double[] oldDenseEnergies = _denseEnergies;
		final double[] oldDenseWeights = _denseWeights;
		final double[] oldSparseEnergies = _sparseEnergies;
		final double[] oldSparseWeights = _sparseWeights;
		final int[] oldSparseToJoint = _sparseIndexToJointIndex;
		final int[][] oldSparseIndices = _sparseIndices;
		
		boolean ok = false;
		
		// FIXME: I don't think this is quite right if starting from DETERMINISTIC*
		
		try
		{
			_computedMask = 0;
			setDomainIndexer(newDomains);

			if (!oldDomains.hasCanonicalDomainOrder() | !newDomains.hasCanonicalDomainOrder())
			{
				JointDomainReindexer converter =
					JointDomainReindexer.createPermuter(oldDomains, newDomains);

				if (hasDenseEnergies())
				{
					_denseEnergies = converter.convertDenseEnergies(_denseEnergies);
				}
				if (hasDenseWeights())
				{
					_denseWeights = converter.convertDenseWeights(_denseWeights);
				}
				if (_sparseIndexToJointIndex.length > 0)
				{
					_sparseIndexToJointIndex = converter.convertSparseToJointIndex(oldSparseToJoint);
					if (hasSparseEnergies())
					{
						_sparseEnergies =
							converter.convertSparseEnergies(_sparseEnergies, oldSparseToJoint, _sparseIndexToJointIndex);
					}
					if (hasSparseWeights())
					{
						_sparseWeights =
							converter.convertSparseWeights(_sparseWeights, oldSparseToJoint, _sparseIndexToJointIndex);
					}
					if (hasSparseIndices())
					{
						_sparseIndices =
							converter.convertSparseIndices(_sparseIndices, oldSparseToJoint, _sparseIndexToJointIndex);
					}
				}
			}
			
			if (outputSet == null)
			{
				if ((_representation & ALL_VALUES) == DETERMINISTIC)
				{
					// If direction removed, then convert DETERMINISTIC format to SPARSE_ENERGY.
					_sparseEnergies = new double[_sparseIndexToJointIndex.length];
					_representation = hasSparseIndices() ? SPARSE_ENERGY_WITH_INDICES : SPARSE_ENERGY;
				}
			}
			else if (assertConditional)
			{
				assertIsConditional();
			}
			
			ok = true;
		}
		finally
		{
			if (!ok)
			{
				_computedMask = oldComputedMask;
				_denseEnergies = oldDenseEnergies;
				_denseWeights = oldDenseWeights;
				_sparseEnergies = oldSparseEnergies;
				_sparseWeights = oldSparseWeights;
				_sparseIndexToJointIndex = oldSparseToJoint;
				_sparseIndices = oldSparseIndices;
			}
		}
	}
	
	@Override
	void setSparseValues(int[][] indices, double[] values, int representation)
	{
		final JointDomainIndexer domains = getDomainIndexer();
		final int[] jointIndices = new int[indices.length];
		for (int i = indices.length; --i>=0;)
		{
			jointIndices[i] = domains.jointIndexFromIndices(domains.validateIndices(indices[i]));
		}
		setSparseValues(jointIndices, values, representation);
	}
	
	private void setSparseValues(int[] jointIndices, double[] values, int representation)
	{
		final int size= jointIndices.length;
		if (size != values.length)
		{
			throw new IllegalArgumentException(
				String.format("'Arrays have different sizes: %d and %d",
					size, values.length));
		}
		
		int[] jointIndices2 = null;
		double[] values2 = null;
		
		boolean doSort = false;
		final JointDomainIndexer domains = getDomainIndexer();
		int cardinality = domains.getCardinality();
		for (int i = size; --i>=1;)
		{
			int jointIndex = jointIndices[i];
			if (jointIndex < 0 || jointIndex >= cardinality)
			{
				throw new IllegalArgumentException(String.format("Joint index %d is out of range", jointIndex));
			}
			if (jointIndex < jointIndices[i-1])
			{
				doSort = true;
				break;
			}
		}
		
		if (doSort)
		{
			jointIndices2 = new int[size];
			values2 = new double[size];

			long[] sortedIndices = new long[size];
			for (int i = size; --i>=0;)
			{
				sortedIndices[i] = ((long)jointIndices[i] << 32) | i;
			}
			Arrays.sort(sortedIndices);
			
			for (int i = size; --i>=0;)
			{
				int jointIndex = (int)(sortedIndices[i] >>> 32);
				jointIndices2[i] = jointIndex;
				values2[i] = values[(int)sortedIndices[i]];
			}
		}
		else
		{
			jointIndices2 = jointIndices.clone();
			values2 = values.clone();
		}
		
		int prev = -1;
		for (int jointIndex : jointIndices2)
		{
			if (jointIndex == prev)
			{
				throw new IllegalArgumentException(String.format(
					"Multiple entries with same set of indices %s (joint index %d)",
					Arrays.toString(domains.jointIndexToIndices(jointIndex)), jointIndex));
			}
			prev = jointIndex;
		}
		
		switch (representation)
		{
		case SPARSE_ENERGY:
			_sparseEnergies = values2;
			_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			break;
		case SPARSE_WEIGHT:
			_sparseWeights = values2;
			_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			break;
		default:
			assert(false);
		}
		_representation = representation;
		_sparseIndexToJointIndex = jointIndices2;
		
		_computedMask = 0;
		_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		computeNonZeroWeights();
	}

	/**
	 * For implementation of {@link #setWeightForJointIndex(double, int)} and
	 * {@link #setEnergyForJointIndex(double, int)}
	 */
	private void setWeightEnergyForJointIndex(double weight, double energy, int jointIndex)
	{
		if ((_representation & ALL_SPARSE) != 0)
		{
			final int sparseIndex = allocateSparseIndexForJointIndex(jointIndex);
			if ((_representation & SPARSE_ENERGY) != 0)
			{
				_sparseEnergies[sparseIndex] = energy;
			}
			if ((_representation & SPARSE_WEIGHT) != 0)
			{
				_sparseWeights[sparseIndex] = weight;
			}
		}
		
		if ((_representation & DENSE_ENERGY) != 0)
		{
			_denseEnergies[jointIndex] = energy;
		}
		
		if ((_representation & DENSE_WEIGHT) != 0)
		{
			_denseWeights[jointIndex] = weight;
		}
	}
	
	/**
	 * For implementation of {@link #setWeightForSparseIndex(double, int)} and
	 * {@link #setEnergyForSparseIndex(double, int)}
	 */
	private void setWeightEnergyForSparseIndex(double weight, double energy, int sparseIndex)
	{
		if ((_representation & ALL_DENSE) != 0)
		{
			final int jointIndex =
				_sparseIndexToJointIndex.length == 0 ? sparseIndex : _sparseIndexToJointIndex[sparseIndex];
			if ((_representation & DENSE_ENERGY) != 0)
			{
				_denseEnergies[jointIndex] = energy;
			}
			if ((_representation & DENSE_WEIGHT) != 0)
			{
				_denseWeights[jointIndex] = weight;
			}
		}
		
		if ((_representation & SPARSE_ENERGY) != 0)
		{
			_sparseEnergies[sparseIndex] = energy;
		}
		if ((_representation & SPARSE_WEIGHT) != 0)
		{
			_sparseWeights[sparseIndex] = weight;
		}
	}

}
