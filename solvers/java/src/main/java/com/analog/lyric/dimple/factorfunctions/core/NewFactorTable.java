package com.analog.lyric.dimple.factorfunctions.core;

import static com.analog.lyric.dimple.model.DiscreteDomainListConverter.*;
import static com.analog.lyric.math.Utilities.*;

import java.util.Arrays;
import java.util.BitSet;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteDomainList;
import com.analog.lyric.dimple.model.DiscreteDomainListConverter;
import com.analog.lyric.math.Utilities;
import com.google.common.math.DoubleMath;

@NotThreadSafe
public class NewFactorTable extends NewFactorTableBase implements INewFactorTable, IFactorTable
{
	/*-------
	 * State
	 */
	
	// _representation values
	static final int DETERMINISTIC = 0x0;
	static final int DENSE_ENERGY = 0x1;
	static final int DENSE_WEIGHT = 0x2;
	static final int SPARSE_ENERGY = 0x4;
	static final int SPARSE_WEIGHT = 0x8;
	static final int ALL_DENSE = DENSE_ENERGY | DENSE_WEIGHT;
	static final int ALL_SPARSE = SPARSE_ENERGY | SPARSE_WEIGHT;
	static final int ALL_WEIGHT = DENSE_WEIGHT | SPARSE_WEIGHT;
	static final int ALL_ENERGY = DENSE_ENERGY | SPARSE_ENERGY;
	static final int ALL = ALL_DENSE | ALL_SPARSE;
	static final int SPARSE_ENERGY_DENSE_WEIGHT = SPARSE_ENERGY | DENSE_WEIGHT;
	static final int DENSE_ENERGY_SPARSE_WEIGHT = DENSE_ENERGY | SPARSE_WEIGHT;
	static final int NOT_SPARSE_WEIGHT = ALL_ENERGY | DENSE_WEIGHT;
	static final int NOT_SPARSE_ENERGY = ALL_WEIGHT | DENSE_ENERGY;
	static final int NOT_DENSE_WEIGHT = ALL_ENERGY | SPARSE_WEIGHT;
	static final int NOT_DENSE_ENERGY = ALL_WEIGHT | SPARSE_ENERGY;
	
	private static final long serialVersionUID = 1L;

	private int _representation;
	
	private double[] _denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	private double[] _denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	private double[] _sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	private double[] _sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	
	/**
	 * Count of table entries with non-zero weight/non-infinite energy.
	 * <p>
	 * When table has no sparse representation, this is returned as the {@link #sparseSize()}.
	 */
	private int _nonZeroWeights;
	
	/**
	 * Maps sparse indexes to joint indexes. Empty if table is in dense form
	 * (because in that case the location and joint index are the same).
	 * <p>
	 * If not dense or deterministic (&directed) this lookup will require a
	 * binary search.
	 */
	private int[] _sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
	
	private static final int DETERMINISTIC_COMPUTED = 0x01;
	private static final int NORMALIZED = 0x02;
	private static final int NORMALIZED_COMPUTED = 0x04;
	private int _computedMask = 0;
	
	/*--------------
	 * Construction
	 */
	
	public NewFactorTable(DiscreteDomainList domains)
	{
		super(domains);
		_nonZeroWeights = 0;
		_representation = SPARSE_ENERGY;
	}
	
	public NewFactorTable(BitSet directedTo, DiscreteDomain ... domains)
	{
		this(DiscreteDomainList.create(directedTo, domains));
	}
	
	/**
	 * Creates empty sparse factor table over given discrete domains.
	 */
	public NewFactorTable(DiscreteDomain ... domains)
	{
		this(null, domains);
	}
	
	/**
	 * Construct as a copy of another table instance.
	 */
	public NewFactorTable(NewFactorTable that)
	{
		super(that);
		_nonZeroWeights = that._nonZeroWeights;
		_computedMask = that._computedMask;
		
		_representation = that._representation;
		_denseEnergies = ArrayUtil.cloneArray(that._denseEnergies);
		_denseWeights = ArrayUtil.cloneArray(that._denseWeights);
		_sparseEnergies = ArrayUtil.cloneArray(that._sparseEnergies);
		_sparseWeights = ArrayUtil.cloneArray(that._sparseWeights);
		_sparseIndexToJointIndex = ArrayUtil.cloneArray(that._sparseIndexToJointIndex);
	}

	/**
	 * Constructs a new table by converting the contents of {@code other} table using
	 * {@code converter} whose "from" domains must match {@code other}'s domains.
	 */
	public NewFactorTable(NewFactorTable other, DiscreteDomainListConverter converter)
	{
		super(converter.getToDomains());
		_representation = other._representation;
		convertFrom(other, converter);
	}
	
	/**
	 * Copies table values from {@code other} table using given {@code converter} whose
	 * "from" domains must match {@code other}'s domains and whose "to" domains must match
	 * this table's domains.
	 */
	public void convertFrom(NewFactorTable other, DiscreteDomainListConverter converter)
	{
		final int representation = _representation;
		
		_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
		_computedMask = 0;
		
		//
		// Convert using single representation, then switch to desired representation.
		//
		
		if ((other._representation & DENSE_WEIGHT) != 0)
		{
			_denseWeights = converter.convertDenseWeights(other._denseWeights);
			_representation = DENSE_WEIGHT;
		}
		else if ((other._representation & DENSE_ENERGY) != 0)
		{
			_denseEnergies = converter.convertDenseEnergies(other._denseEnergies);
			_representation = DENSE_ENERGY;
		}
		else // DETERMINISTIC or sparse
		{
			_sparseIndexToJointIndex = converter.convertSparseToJointIndex(other._sparseIndexToJointIndex);
			
			if ((_representation & SPARSE_WEIGHT) != 0)
			{
				_sparseWeights = converter.convertSparseWeights(other._sparseWeights, other._sparseIndexToJointIndex,
					_sparseIndexToJointIndex);
				_representation = SPARSE_WEIGHT;
			}
			else if ((_representation & SPARSE_ENERGY) != 0)
			{
				_sparseEnergies = converter.convertSparseEnergies(other._sparseEnergies, other._sparseIndexToJointIndex,
					_sparseIndexToJointIndex);
				_representation = SPARSE_ENERGY;
			}
		}
		
		if (converter.getRemovedDomains() == null)
		{
			_nonZeroWeights = other._nonZeroWeights * converter.getAddedCardinality();
		}
		else if (other._nonZeroWeights == other.getDomainList().getCardinality())
		{
			_nonZeroWeights = _domains.getCardinality();
		}
		else
		{
			// Need to count them explicitly
			computeNonZeroWeights();
		}
		
		setRepresentation(representation);
	}
	
	@Override
	public NewFactorTable convert(DiscreteDomainListConverter converter)
	{
		return new NewFactorTable(this, converter);
	}
	
	/*----------------
	 * Object methods
	 */

	@Override
	public NewFactorTable clone()
	{
		return new NewFactorTable(this);
	}
	
	/*-----------------------------
	 * INewFactorTableBase methods
	 */
	
	@Override
	public int computeMinSparseSize()
	{
		return _nonZeroWeights;
	}
	
	@Override
	public void evalDeterministic(Object[] arguments)
	{
		if (!isDeterministicDirected())
		{
			throw new DimpleException("Table is not deterministic");
		}
		
		int outputSize = _domains.getOutputCardinality();
		int inputIndex = _domains.inputIndexFromElements(arguments);
		int jointIndex = _sparseIndexToJointIndex[inputIndex];
		int outputIndex = jointIndex - inputIndex * outputSize;
		_domains.outputIndexToElements(outputIndex, arguments);
	}
	
	@Override
	public final double getDenseEnergyForIndices(int ... indices)
	{
		return _denseEnergies[_domains.jointIndexFromIndices(indices)];
	}

	@Override
	public final double getDenseWeightForIndices(int ... indices)
	{
		return _denseWeights[_domains.jointIndexFromIndices(indices)];
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
			final int expectedJoint = _sparseIndexToJointIndex[jointIndex /  _domains.getOutputCardinality()];
			return expectedJoint == jointIndex ? 0.0 : Double.POSITIVE_INFINITY;
			
		case ALL:
		case ALL_DENSE:
		case ALL_ENERGY:
		case DENSE_ENERGY:
		case NOT_DENSE_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case NOT_SPARSE_WEIGHT:
			return _denseEnergies[jointIndex];
			
		case ALL_WEIGHT:
		case DENSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case SPARSE_ENERGY_DENSE_WEIGHT:
			return Utilities.weightToEnergy(_denseWeights[jointIndex]);
			
		case ALL_SPARSE:
		case SPARSE_ENERGY:
			sparseIndex = sparseIndexFromJointIndex(jointIndex);
			if (sparseIndex >= 0)
			{
				return _sparseEnergies[sparseIndex];
			}
			break;
			
		case SPARSE_WEIGHT:
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
			return 0.0;
			
		case ALL_DENSE:
		case DENSE_ENERGY:
			setRepresentation(_representation | SPARSE_ENERGY);
			// $FALL-THROUGH$
		case ALL:
		case ALL_ENERGY:
		case ALL_SPARSE:
		case NOT_DENSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case SPARSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY_DENSE_WEIGHT:
			return _sparseEnergies[sparseIndex];
			
		case DENSE_WEIGHT:
			setRepresentation(_representation | SPARSE_WEIGHT);
			// $FALL-THROUGH$
		case ALL_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case SPARSE_WEIGHT:
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
			final int expectedJoint = _sparseIndexToJointIndex[jointIndex /  _domains.getOutputCardinality()];
			return expectedJoint == jointIndex ? 1.0 : 0.0;

		case ALL:
		case ALL_DENSE:
		case ALL_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case DENSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY_DENSE_WEIGHT:
			return _denseWeights[jointIndex];

		case ALL_ENERGY:
		case DENSE_ENERGY:
		case NOT_DENSE_WEIGHT:
		case DENSE_ENERGY_SPARSE_WEIGHT:
			return energyToWeight(_denseEnergies[jointIndex]);

		case ALL_SPARSE:
		case SPARSE_WEIGHT:
			sparseIndex = sparseIndexFromJointIndex(jointIndex);
			if (sparseIndex >= 0)
			{
				return _sparseWeights[sparseIndex];
			}
			break;

		case SPARSE_ENERGY:
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
			return 1.0;
			
		case ALL_DENSE:
		case DENSE_WEIGHT:
			setRepresentation(_representation | SPARSE_WEIGHT);
			// $FALL-THROUGH$
		case ALL:
		case ALL_WEIGHT:
		case ALL_SPARSE:
		case NOT_SPARSE_ENERGY:
		case NOT_DENSE_WEIGHT:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case SPARSE_WEIGHT:
			return _sparseWeights[sparseIndex];
			
		case DENSE_ENERGY:
			setRepresentation(_representation | SPARSE_ENERGY);
			// $FALL-THROUGH$
		case ALL_ENERGY:
		case SPARSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY_DENSE_WEIGHT:
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
	public final boolean hasSparseRepresentation()
	{
		return (_representation & ALL_SPARSE) != 0;
	}
	
	@Override
	public final boolean hasSparseEnergies()
	{
		return (_representation & SPARSE_ENERGY) != 0;
	}
	
	@Override
	public final boolean hasSparseWeights()
	{
		return (_representation & SPARSE_WEIGHT) != 0;
	}

	@Override
	public boolean isDeterministicDirected()
	{
		if (_representation == DETERMINISTIC)
		{
			return true;
		}
		
		if ((_computedMask & DETERMINISTIC_COMPUTED) != 0)
		{
			return false;
		}
		
		boolean deterministic = false;
		
		if (isDirected() && _nonZeroWeights == _domains.getInputCardinality())
		{
			// Table can only be deterministic if there is exactly one
			// valid output for each possible input and all outputs have the
			// same weight.
			final int[] sparseToJoint = computeSparseToJointIndexMap();
			deterministic = true;
			final int outputSize = _domains.getOutputCardinality();
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

			if (deterministic)
			{
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
					double first = _denseEnergies[sparseToJoint[0]];
					for (int i = 1, end = sparseToJoint.length; i < end; ++i)
					{
						if (!DoubleMath.fuzzyEquals(first, _denseEnergies[sparseToJoint[i]], tolerance))
						{
							deterministic = false;
							break;
						}
					}
				}
				else
				{
					assert(hasDenseWeights());
					double first = _denseWeights[sparseToJoint[0]];
					for (int i = 1, end = sparseToJoint.length; i < end; ++i)
					{
						if (!DoubleMath.fuzzyEquals(first, _denseWeights[sparseToJoint[i]], tolerance))
						{
							deterministic = false;
							break;
						}
					}
				}
			}
			
			if (deterministic)
			{
				_sparseIndexToJointIndex = sparseToJoint;
				_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_representation = DETERMINISTIC;
			}
		}
		_computedMask |= DETERMINISTIC_COMPUTED;
		
		return deterministic;
	}

	@Override
	public final boolean isNormalized()
	{
		if ((_computedMask & NORMALIZED_COMPUTED) == 0)
		{
			normalizeInternal(true);
			_computedMask |= NORMALIZED_COMPUTED;
		}
		return (_computedMask & NORMALIZED) != 0;
	}
	
	@Override
	public final int sparseIndexToJointIndex(int sparseIndex)
	{
		if ((_representation & ALL_SPARSE) == 0)
		{
			setRepresentation(_representation | SPARSE_ENERGY);
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
		
		switch (_representation)
		{
		case DETERMINISTIC:
			// Optimize deterministic case. Since there is exactly one entry per distinct
			// set of outputs, we can simply check to see if the jointIndex is found at
			// the corresponding location for the output indices.
			sparseIndex /= _domains.getOutputCardinality();
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

		case ALL:
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
	public void normalize()
	{
		normalizeInternal(false);
	}
	
	@Override
	public final int sparseSize()
	{
		switch (_representation)
		{
		case DETERMINISTIC:
			return _sparseIndexToJointIndex.length;

		case DENSE_ENERGY:
		case DENSE_WEIGHT:
		case ALL_DENSE:
			return _nonZeroWeights;
			
		case SPARSE_ENERGY:
		case ALL_ENERGY:
		case SPARSE_ENERGY_DENSE_WEIGHT:
		case NOT_SPARSE_WEIGHT:
		case ALL_SPARSE:
		case NOT_DENSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case ALL:
			return _sparseEnergies.length;
			
		case SPARSE_WEIGHT:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case ALL_WEIGHT:
		case NOT_SPARSE_ENERGY:
			return _sparseWeights.length;
		}
		
		return 0;
	}
	
	/*--------------------------
	 * New IFactorTable methods
	 */
	
	@Override
	public final NewFactorTableRepresentation getRepresentation()
	{
		return NewFactorTableRepresentation.forOrdinal(_representation);
	}
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#DENSE_ENERGY} with
	 * provided energies.
	 * @param energies specifies the energies of the table in dense joint-index order. Must have length
	 * equal to {@link #getDomainList()}.getCardinality().
	 */
	public void setDenseEnergies(double[] energies)
	{
		setDenseValues(energies, DENSE_ENERGY);
	}
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#DENSE_WEIGHT} with
	 * provided weights.
	 * @param weights specifies the weights of the table in dense joint-index order. Must have length
	 * equal to {@link #getDomainList()}.getCardinality().
	 */
	public void setDenseWeights(double[] weights)
	{
		setDenseValues(weights, DENSE_WEIGHT);
	}
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#DETERMINISTIC} with given set of
	 * outputs.
	 * <p>
	 * @param outputIndices any array mapping input indices, representing the joint value of all input
	 * values, to output indices, representing the joint value of all outputs. The length of the array
	 * must be equal to the value of {@link DiscreteDomainList#getInputCardinality()} on {@link #getDomainList()}.
	 * @throws UnsupportedOperationException if not {@link #isDirected()}.
	 */
	public void setDeterministicOuputIndices(int[] outputIndices)
	{
		final int size = _domains.getInputCardinality();
		
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
		final int outputCardinality = _domains.getOutputCardinality();
		for (int inputIndex = 0; inputIndex < size; ++inputIndex)
		{
			final int outputIndex = outputIndices[inputIndex];
			if (outputIndex < 0 || outputIndex >= outputCardinality)
			{
				throw new IllegalArgumentException(String.format("Output index %d is out of range", outputIndex));
			}
			sparseToJoint[inputIndex] = _domains.jointIndexFromInputOutputIndices(inputIndex, outputIndex);
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
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#SPARSE_ENERGY} with
	 * provided energies for each joint index.
	 * <p>
	 * @param jointIndices are the joint indexes of the entries to put in the table.
	 * @param energies specifies the energies of the table in the same order as {@code jointIndices}.
	 * @throws IllegalArgumentException if {@code jointIndices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 */
	public void setSparseEnergies(int[] jointIndices, double[] energies)
	{
		setSparseValues(jointIndices, energies, SPARSE_ENERGY);
	}
	
	
	/**
	 * Sets representation to {@link NewFactorTableRepresentation#SPARSE_WEIGHT} with
	 * provided weights for each joint index.
	 * <p>
	 * @param jointIndices are the joint indexes of the entries to put in the table.
	 * @param weights specifies the weights of the table in the same order as {@code jointIndices}.
	 * @throws IllegalArgumentException if {@code jointIndices} and {@code energies} have different lengths,
	 * if there are duplicate indices or any of the indices is not in a valid range for the table.
	 */
	public void setSparseWeights(int[] jointIndices, double[] weights)
	{
		setSparseValues(jointIndices, weights, SPARSE_WEIGHT);
	}
	
	@Override
	public void setRepresentation(final NewFactorTableRepresentation newRep)
	{
		setRepresentation(newRep.ordinal());
	}
	
	private void setRepresentation(final int newRep)
	{
		int oldRep = _representation;
		
		if (oldRep == newRep)
		{
			return;
		}
		
		//
		// Special cases for deterministic conversions
		//
		
		if (newRep == DETERMINISTIC)
		{
			if (!isDeterministicDirected())
			{
				throw new DimpleException("Cannot set representation to DETERMINISTIC");
			}
			return;
		}
		
		final int jointSize = jointSize();
		
		if (oldRep == DETERMINISTIC)
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
					_denseWeights[ji] = 0.0;
				}
			}
			if ((newRep & ALL_SPARSE) == 0)
			{
				_sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
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
		// Sparse-to-sparse conversions
		//

		diff &= ~oldRep;
		if ((diff & ALL_SPARSE) != 0)
		{
			if ((diff & SPARSE_ENERGY) != 0 && (oldRep & SPARSE_WEIGHT) != 0)
			{
				final double[] sparseWeights = _sparseWeights;
				final double[] sparseEnergies = _sparseEnergies = new double[sparseWeights.length];
				for (int i = sparseWeights.length; --i >= 0;)
				{
					sparseEnergies[i] = Utilities.weightToEnergy(sparseWeights[i]);
				}
				oldRep |= SPARSE_ENERGY;
			}
			else if ((diff & SPARSE_WEIGHT) != 0 && (oldRep & SPARSE_ENERGY) != 0)
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
		
		_representation = newRep;
	}
	
	@Override
	public final void setEnergyForJointIndex(double energy, int jointIndex)
	{
		final double prevEnergy = getEnergyForJointIndex(jointIndex);
		if (prevEnergy != energy)
		{
			_computedMask = 0;
			if (_representation == DETERMINISTIC)
			{
				setRepresentation(DENSE_ENERGY);
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
			if (_representation == DETERMINISTIC)
			{
				setRepresentation(SPARSE_ENERGY);
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
			if (_representation == DETERMINISTIC)
			{
				setRepresentation(DENSE_WEIGHT);
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
			if (_representation == DETERMINISTIC)
			{
				setRepresentation(SPARSE_ENERGY);
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

	/*----------------------
	 * Old IFactorTable methods
	 */
	
	@Override
	public void change(int[][] indices, double[] weights)
	{
		_computedMask = 0;
		int newSize = indices.length;
		
		if (indices.length != weights.length)
		{
			throw new IllegalArgumentException(
				String.format("indices and weights lenghts differ (%d vs %d)", indices.length, weights.length));
		}
		
		Arrays.fill(_denseEnergies, Double.POSITIVE_INFINITY);
		Arrays.fill(_denseWeights, 0.0);

		if (indices.length == 0)
		{
			_sparseEnergies = _sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
			_nonZeroWeights = 0;
			return;
		}
		
		// Encode indices as array of jointIndex/weightIndex tuples with the
		// jointIndex in the high-order 32-bits so that we can order it trivially
		// with standard sort.
		long[] jointTuples = new long[newSize];
		for (int i = 0; i < newSize; ++i)
		{
			final int[] row = indices[i];
			_domains.validateIndices(row);
			long joint = _domains.jointIndexFromIndices(row);
			jointTuples[i] = (joint << 32) | i;
		}
		Arrays.sort(jointTuples);
		
		int prevJoint = -1, nNonZero = 0;
		int[] jointIndexes = new int[newSize];
		double[] orderedWeights = new double[newSize];
		for (int i = 0; i < newSize; ++i)
		{
			long jointTuple = jointTuples[i];
			int joint = (int)(jointTuple >>> 32);
			double weight = weights[(int)jointTuple];
			if (joint == prevJoint)
			{
				throw new DimpleException("Table Factor contains multiple rows with same set of indices.");
			}
			if (weight != 0.0)
			{
				++nNonZero;
			}
			prevJoint = joint;
			jointIndexes[i] = joint;
			orderedWeights[i] = weight;
		}
		
		_nonZeroWeights = nNonZero;
		
		if ((_representation & ALL_SPARSE) != 0)
		{
			_sparseIndexToJointIndex = jointIndexes;
			
			if ((_representation & SPARSE_WEIGHT) != 0)
			{
				_sparseWeights = orderedWeights;
			}
			if ((_representation & SPARSE_ENERGY) != 0)
			{
				_sparseEnergies = new double[newSize];
				for (int i = 0; i < newSize; ++i)
				{
					_sparseEnergies[i] = weightToEnergy(orderedWeights[i]);
				}
			}
		}
		
		if ((_representation & DENSE_WEIGHT) != 0)
		{
			for (int i = 0; i < newSize; ++i)
			{
				_denseWeights[jointIndexes[i]] = orderedWeights[i];
			}
		}
		if ((_representation & DENSE_ENERGY) != 0)
		{
			if ((_representation & SPARSE_ENERGY) != 0)
			{
				for (int i = 0; i < newSize; ++i)
				{
					_denseEnergies[jointIndexes[i]] = _sparseEnergies[i];
				}
			}
			else
			{
				for (int i = 0; i < newSize; ++i)
				{
					_denseEnergies[jointIndexes[i]] = weightToEnergy(orderedWeights[i]);
				}
			}
		}
	}

	@Override
	public void changeWeights(double[] values)
	{
		for (int si = sparseSize(); --si>=0;)
		{
			setWeightForSparseIndex(values[si], si);
		}
	}

	@Override
	public double[] getWeights()
	{
		if (_representation == DETERMINISTIC)
		{
			double[] weights = new double[sparseSize()];
			Arrays.fill(weights, 1.0);
			return weights;
		}
		else
		{
			setRepresentation(_representation | SPARSE_WEIGHT);
			return _sparseWeights.clone();
		}
	}

	@Override
	public IFactorTable copy()
	{
		return new NewFactorTable(this);
	}

	@Override
	public void copy(IFactorTable that)
	{
		if (that == this)
		{
			return;
		}

		if (!Arrays.equals(_domains.toArray(), that.getDomains()))
		{
			throw new DimpleException("Cannot copy from factor table with different domains");
		}
		
		if (that instanceof NewFactorTable)
		{
			NewFactorTable other = (NewFactorTable)that;
			_nonZeroWeights = other._nonZeroWeights;
			_representation = other._representation;
			_denseEnergies = ArrayUtil.cloneArray(other._denseEnergies);
			_denseWeights = ArrayUtil.cloneArray(other._denseWeights);
			_sparseEnergies = ArrayUtil.cloneArray(other._sparseEnergies);
			_sparseWeights = ArrayUtil.cloneArray(other._sparseWeights);
			_sparseIndexToJointIndex = ArrayUtil.cloneArray(other._sparseIndexToJointIndex);
			_computedMask = other._computedMask;
		}
		else
		{
			_representation = DENSE_WEIGHT;
			_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
			_denseWeights = new double[jointSize()];
			_sparseIndexToJointIndex = ArrayUtil.EMPTY_INT_ARRAY;
			_nonZeroWeights = 0;
			
			int[][] indices = that.getIndices();
			double[] weights = that.getWeights();
			for (int i = 0, end = weights.length; i < end; ++i)
			{
				setWeightForIndices(weights[i], indices[i]);
			}
		}
	}

	@Override
	public NewFactorTable createTableWithNewVariables(DiscreteDomain[] additionalDomains)
	{
		DiscreteDomainListConverter converter =
			DiscreteDomainListConverter.createAdder(_domains, _domains.size(), additionalDomains);

		return new NewFactorTable(this, converter);
	}
	
	@Override
	public double[] getPotentials()
	{
		if (_representation == DETERMINISTIC)
		{
			return new double[sparseSize()];
		}
		else
		{
			setRepresentation(_representation | SPARSE_ENERGY);
			return _sparseEnergies.clone();
		}
	}

	// FIXME: what to do if table is directed? Should we assert that the joined
	// variables are all either inputs or outputs?
	@Override
	public NewFactorTable joinVariablesAndCreateNewTable(
		int[] varIndices,
		int[] indexToJointIndex,
		DiscreteDomain[] allDomains,
		DiscreteDomain jointDomain)
	{
		assert(Arrays.equals(allDomains, _domains.toArray()));
		assert(varIndices.length == indexToJointIndex.length);
		
		// Build a domain converter by first permuting joined domains to proper order at
		// end of domain list, and then by doing the join.
		
		final int joinedSize = varIndices.length;
		final int unjoinedSize = _domains.size() - joinedSize;
		final int[] permutation = new int[_domains.size()];
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
		
		DiscreteDomainListConverter converter = null;
		if (!identityMap)
		{
			DiscreteDomain[] toDomains = new DiscreteDomain[permutation.length];
			for (int i = permutation.length; --i>=0;)
			{
				toDomains[permutation[i]] = _domains.get(i);
			}
			converter = createPermuter(_domains, DiscreteDomainList.create(toDomains), permutation);
		}
		
		if (converter != null)
		{
			converter = converter.combineWith(createJoiner(converter.getToDomains(), unjoinedSize, joinedSize));
		}
		else
		{
			converter = createJoiner(_domains, unjoinedSize, joinedSize);
		}

		return new NewFactorTable(this, converter);
	}

	@Override
	public void normalize(int[] directedTo)
	{
		// TODO: eventually get rid of normalize w/ directed arguments, but in the meantime allow
		// if arguments matches domain list.
		
		BitSet toSet = BitSetUtil.bitsetFromIndices(_domains.size(), directedTo);
		if (!toSet.equals(_domains.getOutputSet()))
		{
			throw DimpleException.unsupportedMethod(getClass(), "normalize(int[] directedTo)");
		}
	
		normalize();
	}

	@Override
	public void normalize(int[] directedTo, int[] directedFrom)
	{
		normalize(directedTo);
	}

	@Override
	public boolean supportsSetDirected()
	{
		return false;
	}
	
	/*-----------------
	 * Private methods
	 */
	
	protected int allocateSparseIndexForJointIndex(int jointIndex)
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
			_sparseIndexToJointIndex = ArrayUtil.copyArrayForInsert(_sparseIndexToJointIndex, sparseIndex, 1);
			_sparseIndexToJointIndex[sparseIndex] = jointIndex;
		}
		
		return sparseIndex;
	}
	
	private void computeNonZeroWeights()
	{
		int count = 0;
		switch (_representation)
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
		case ALL:
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
	
	private int[] computeSparseToJointIndexMap()
	{
		if (_sparseIndexToJointIndex.length > 0)
		{
			return _sparseIndexToJointIndex;
		}
		
		final int jointSize = jointSize();
		final int[] map = new int[_nonZeroWeights];
		
		if ((_representation & DENSE_WEIGHT) != 0)
		{
			for (int di = 0, si = 0; si < map.length; ++di)
			{
				if (_denseWeights[di] != 0.0)
				{
					map[si++] = di;
				}
			}
		}
		else
		{
			for (int di = 0, si = 0; di < jointSize; ++di)
			{
				if (!Double.isInfinite(_denseEnergies[di]))
				{
					map[si++] = di;
				}
			}
		}
		
		return map;
	}
	
	private boolean normalizeDirected(boolean justCheck)
	{
		final int inputSize = _domains.getInputCardinality();
		final int outputSize = _domains.getOutputCardinality();
		final boolean hasSparseToJoint = _sparseIndexToJointIndex.length > 0;
		
		boolean computeNormalizedTotal = justCheck;
		double normalizedTotal = 1.0;
		double totalForInput = 0.0;
		
		// We represent the joint index such that the outputs for the same
		// input are stored consecutively, so we only need to walk through
		// the values in order.
		//
		// When just checking, we allow total to equal something other than one
		// as long as they are all the same.
		
		switch (_representation)
		{
		case DETERMINISTIC:
			break;
			
		case ALL:
		case ALL_WEIGHT:
		case ALL_SPARSE:
		case NOT_DENSE_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case NOT_DENSE_ENERGY:
		case SPARSE_WEIGHT:
			for (int si = 0, nextsi = 0, size = _sparseWeights.length; si < size; si = nextsi)
			{
				final int ji = hasSparseToJoint ? _sparseIndexToJointIndex[si] : si;
				final int ii = _domains.inputIndexFromJointIndex(ji);
				final int maxji = _domains.jointIndexFromInputOutputIndices(ii, outputSize-1);
				
				totalForInput = _sparseWeights[si];
				
				for (nextsi = si + 1;
					nextsi < size && maxji >= (hasSparseToJoint ? _sparseIndexToJointIndex[nextsi] : nextsi);
					++nextsi)
				{
					totalForInput += _sparseWeights[nextsi];
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
					for (int si2 = si; si2 < nextsi; ++si2)
					{
						setWeightForSparseIndex(_sparseWeights[si2] / totalForInput, si2);
					}
				}
			}
			break;
			
		case ALL_ENERGY:
		case SPARSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY_DENSE_WEIGHT:
			// TODO: if sparse size is large enough, it would be faster to iterate over the dense weights
			for (int si = 0, nextsi = 0, size = _sparseEnergies.length; si < size; si = nextsi)
			{
				final int ji = hasSparseToJoint ? _sparseIndexToJointIndex[si] : si;
				final int ii = _domains.inputIndexFromJointIndex(ji);
				final int maxji = _domains.jointIndexFromInputOutputIndices(ii, outputSize-1);
				
				totalForInput = energyToWeight(_sparseEnergies[si]);
				
				for (nextsi = si + 1;
					nextsi < size && maxji >= (hasSparseToJoint ? _sparseIndexToJointIndex[nextsi] : nextsi);
					++nextsi)
				{
					totalForInput += energyToWeight(_sparseEnergies[nextsi]);
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
					double logTotalForInput = Math.log(totalForInput);
					for (int si2 = si; si2 < nextsi; ++si2)
					{
						setEnergyForSparseIndex(_sparseEnergies[si2] + logTotalForInput, si2);
					}
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
					double logTotalForInput = Math.log(totalForInput);
					for (int outputIndex = 0; outputIndex < outputSize; ++outputIndex)
					{
						int ji = jointIndex + outputIndex;
						setEnergyForJointIndex(_denseEnergies[ji] + logTotalForInput, ji);
					}
				}
			}
			break;
		}
		
		_computedMask |= NORMALIZED;
		return true;
	}
	
	private boolean normalizeInternal(boolean justCheck)
	{
		if ((_computedMask & NORMALIZED) != 0)
		{
			return true;
		}
			
		if (_domains.isDirected())
		{
			return normalizeDirected(justCheck);
		}
		
		double total = 0.0;
		switch (_representation)
		{
		case ALL:
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

		_computedMask |= NORMALIZED;
		return true;
	}

	private void setDenseValues(double[] values, int representation)
	{
		if (values.length != _domains.getCardinality())
		{
			throw new IllegalArgumentException(String.format("Bad dense length: was %d, expected %d",
				values.length, _domains.getCardinality()));
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
		int cardinality = _domains.getCardinality();
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
				throw new IllegalArgumentException(String.format("Duplicate joint index %d", jointIndex));
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
