package com.analog.lyric.dimple.factorfunctions.core;

import java.util.Arrays;
import java.util.BitSet;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteDomainList;
import com.analog.lyric.math.Utilities;

@NotThreadSafe
public class NewFactorTable extends NewFactorTableBase implements INewFactorTable, IFactorTable
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private INewFactorTable.Representation _representation;
	
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
	private int _computedMask = 0;
	
	/*--------------
	 * Construction
	 */
	
	public NewFactorTable(BitSet directedFrom, DiscreteDomain ... domains)
	{
		super(directedFrom, domains);
		
		_nonZeroWeights = 0;
		_representation = INewFactorTable.Representation.SPARSE_ENERGY;
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
			setRepresentation(_representation.union(Representation.SPARSE_ENERGY));
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
			setRepresentation(_representation.union(Representation.SPARSE_WEIGHT));
			// $FALL-THROUGH$
		case ALL_WEIGHT:
		case NOT_SPARSE_ENERGY:
		case DENSE_ENERGY_SPARSE_WEIGHT:
		case SPARSE_WEIGHT:
			return Utilities.weightToEnergy(_sparseWeights[sparseIndex]);
			
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
			return Utilities.energyToWeight(_denseEnergies[jointIndex]);
			
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
				return Utilities.energyToWeight(_sparseEnergies[sparseIndex]);
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
			setRepresentation(_representation.union(Representation.SPARSE_WEIGHT));
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
			setRepresentation(_representation.union(Representation.SPARSE_ENERGY));
			// $FALL-THROUGH$
		case ALL_ENERGY:
		case SPARSE_ENERGY:
		case NOT_SPARSE_WEIGHT:
		case SPARSE_ENERGY_DENSE_WEIGHT:
			return Utilities.energyToWeight(_sparseEnergies[sparseIndex]);
		}

		return 0.0;
	}
	
	@Override
	public final boolean hasDenseRepresentation()
	{
		return _representation.hasDense();
	}
	
	@Override
	public final boolean hasSparseRepresentation()
	{
		return _representation.hasSparse();
	}
	
	@Override
	public boolean isDeterministicDirected()
	{
		if (_representation == Representation.DETERMINISTIC)
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
			// valid output for each possible input.
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
				_sparseIndexToJointIndex = sparseToJoint;
				_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
				_representation = Representation.DETERMINISTIC;
			}
		}
		_computedMask |= DETERMINISTIC_COMPUTED;
		
		return deterministic;
	}

	@Override
	public final boolean isNormalized()
	{
		return (_computedMask & NORMALIZED) != 0;
	}
	
	@Override
	public final int sparseIndexToJointIndex(int sparseIndex)
	{
		if (!_representation.hasSparse())
		{
			setRepresentation(_representation.union(Representation.SPARSE_ENERGY));
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
			setRepresentation(_representation.union(Representation.SPARSE_ENERGY));
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
		if ((_computedMask & NORMALIZED) != 0)
		{
			return;
		}
			
		if (_domains.isDirected())
		{
			normalize(_domains.getInputSet());
			return;
		}
		
		double total = 0.0;
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
				total += Utilities.energyToWeight(e);
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
				total += Utilities.energyToWeight(e);
			}
			break;
		}
		
		if (total != 0.0)
		{
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
	public final Representation getRepresentation()
	{
		return _representation;
	}
	
	protected void normalize(BitSet directedFrom)
	{
		BitSet inputSet = _domains.getInputSet();
		boolean canonical = inputSet != null && inputSet.equals(directedFrom);
		
		if (canonical && (_computedMask & NORMALIZED) != 0)
		{
			return;
		}
		
		_computedMask &= ~NORMALIZED;
		
		final int nDomains = getDimensions();
		final int[] oldToFromMap = new int[nDomains];
		final int[] directedFromProducts = computeDomainSubsetInfo(_domains, directedFrom, oldToFromMap);
		final int nDirectedFrom = directedFromProducts.length - 1;
		
		final int [] fromToOldMap = new int[nDirectedFrom];
		for (int oldi = nDomains; --oldi >=0;)
		{
			int fromi = oldToFromMap[oldi];
			if (fromi >= 0)
			{
				fromToOldMap[fromi] = oldi;
			}
		}
		
		final double[] normalizers = new double[directedFromProducts[directedFromProducts.length - 1]];
		final int[] indices = new int[nDomains];
		final int[] directedFromIndices = new int[nDirectedFrom];
		for (int i = 0, endi = sparseSize(); i < endi; ++i)
		{
			sparseIndexToIndices(i, indices);
			for (int j = 0, endj = nDirectedFrom; j < endj; ++j)
			{
				directedFromIndices[j] = indices[fromToOldMap[j]];
			}
			int index = locationFromIndices(directedFromIndices,directedFromProducts);
			normalizers[index] += getWeightForSparseIndex(i);
		}
		
		for (int i = 0, endi = sparseSize(); i < endi; ++i)
		{
			sparseIndexToIndices(i, indices);
			for (int j = 0, endj = nDirectedFrom; j < endj; ++j)
			{
				directedFromIndices[j] = indices[fromToOldMap[j]];
			}
			int index = locationFromIndices(directedFromIndices, directedFromProducts);
			final double normalizer = normalizers[index];
			if (normalizer != 0.0)
			{
				setWeightForSparseIndex(getWeightForSparseIndex(i) / normalizers[index], i);
			}
		}
		
		_computedMask |= NORMALIZED;
	}
	@Override
	public void setRepresentation(final Representation newRep)
	{
		Representation oldRep = _representation;
		
		if (oldRep == newRep)
		{
			return;
		}
		
		//
		// Special cases for deterministic conversions
		//
		
		if (newRep == Representation.DETERMINISTIC)
		{
			if (!isDeterministicDirected())
			{
				throw new DimpleException("Cannot set representation to DETERMINISTIC");
			}
			return;
		}
		
		final int jointSize = jointSize();
		
		if (oldRep == Representation.DETERMINISTIC)
		{
			if (newRep.hasSparseWeight())
			{
				_sparseWeights = new double[sparseSize()];
				Arrays.fill(_sparseWeights, 1.0);
			}
			if (newRep.hasSparseEnergy())
			{
				_sparseEnergies = new double[sparseSize()];
			}
			if (newRep.hasDenseWeight())
			{
				_denseWeights = new double[jointSize];
				for (int ji : _sparseIndexToJointIndex)
				{
					_denseWeights[ji] = 1.0;
				}
					
			}
			if (newRep.hasDenseEnergy())
			{
				_denseEnergies = new double[jointSize];
				Arrays.fill(_denseEnergies, Double.POSITIVE_INFINITY);
				for (int ji : _sparseIndexToJointIndex)
				{
					_denseWeights[ji] = 0.0;
				}
			}
			if (!newRep.hasSparse())
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
		
		Representation diff = newRep.difference(oldRep);
		if (diff.hasSparse() && !oldRep.hasSparse())
		{
			if (_nonZeroWeights == jointSize)
			{
				// sparse == dense
				// dense == sparse, use same arrays if possible
				if (diff.hasSparseWeight() && oldRep.hasDenseWeight())
				{
					_sparseWeights = _denseWeights;
					oldRep = oldRep.union(Representation.SPARSE_WEIGHT);
				}
				if (diff.hasSparseEnergy() && oldRep.hasDenseEnergy())
				{
					_sparseEnergies = _denseEnergies;
					oldRep = oldRep.union(Representation.SPARSE_ENERGY);
				}
				diff = newRep.difference(oldRep);
			}
			
			if (diff.hasSparse())
			{
				final int[] sparseToJoint = _sparseIndexToJointIndex = computeSparseToJointIndexMap();
				final int sparseSize = sparseToJoint.length;

				if (diff.hasSparseWeight())
				{
					final double[] sparseWeights = _sparseWeights = new double[sparseSize];
					if (oldRep.hasDenseWeight())
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
					oldRep = oldRep.union(Representation.SPARSE_WEIGHT);
				}
				if (diff.hasSparseEnergy())
				{
					final double[] sparseEnergies = _sparseEnergies = new double[sparseToJoint.length];
					if (oldRep.hasDenseEnergy())
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
					oldRep = oldRep.union(Representation.SPARSE_ENERGY);
				}
			}
		}
		
		final int[] sparseToJoint = _sparseIndexToJointIndex;
		
		//
		// Sparse-to-sparse conversions
		//

		diff = newRep.difference(oldRep);
		if (diff.hasSparse())
		{
			if (diff.hasSparseEnergy() && oldRep.hasSparseWeight())
			{
				final double[] sparseWeights = _sparseWeights;
				final double[] sparseEnergies = _sparseEnergies = new double[sparseWeights.length];
				for (int i = sparseWeights.length; --i >= 0;)
				{
					sparseEnergies[i] = Utilities.weightToEnergy(sparseWeights[i]);
				}
				oldRep = oldRep.union(Representation.SPARSE_ENERGY);
			}
			else if (diff.hasSparseWeight() && oldRep.hasSparseEnergy())
			{
				final double[] sparseEnergies = _sparseEnergies;
				final double[] sparseWeights = _sparseWeights = new double[sparseEnergies.length];
				for (int i = sparseEnergies.length; --i >= 0;)
				{
					sparseWeights[i] = Utilities.energyToWeight(sparseEnergies[i]);
				}
				oldRep = oldRep.union(Representation.SPARSE_WEIGHT);
			}
		}
		
		//
		// *-to-dense conversions
		//
		
		diff = newRep.difference(oldRep);
		if (diff.hasDense())
		{
			if (oldRep.hasSparse())
			{
				if (diff.hasDenseEnergy())
				{
					if (oldRep.hasSparseEnergy())
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
					else // oldRep.hasSparseWeight()
					{
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
					oldRep = oldRep.union(Representation.DENSE_ENERGY);
				}
				if (diff.hasDenseWeight())
				{
					if (oldRep.hasSparseWeight())
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
					else // oldRep.hasSparseEnergy()
					{
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
					oldRep = oldRep.union(Representation.DENSE_WEIGHT);
				}
			}
			else
			{
				if (diff.hasDenseEnergy())
				{
					final double[] denseWeights = _denseWeights;
					final double[] denseEnergies = _denseEnergies = new double[jointSize];
					for (int i = 0; i < jointSize; ++i)
					{
						denseEnergies[i] = Utilities.weightToEnergy(denseWeights[i]);
					}
					oldRep = oldRep.union(Representation.DENSE_ENERGY);
				}
				else
				{
					final double[] denseEnergies = _denseEnergies;
					final double[] denseWeights = _denseWeights = new double[jointSize];
					for (int i = 0; i < jointSize; ++i)
					{
						denseWeights[i] = Utilities.energyToWeight(denseEnergies[i]);
					}
					oldRep = oldRep.union(Representation.DENSE_WEIGHT);
				}
			}
		}
		
		assert(newRep.difference(oldRep).mask() == 0);
		
		//
		// Remove old arrays
		//
		
		if (!newRep.hasSparseEnergy())
		{
			_sparseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		if (!newRep.hasSparseWeight())
		{
			_sparseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		if (!newRep.hasDenseEnergy())
		{
			_denseEnergies = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		if (!newRep.hasDenseWeight())
		{
			_denseWeights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}
		if (!newRep.hasSparse())
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
			if (_representation.isDeterministic())
			{
				setRepresentation(Representation.DENSE_ENERGY);
				_denseEnergies[jointIndex] = energy;
			}
			else
			{
				double weight = _representation.hasWeight() ? Utilities.energyToWeight(energy) : 0.0;
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
			if (_representation.isDeterministic())
			{
				setRepresentation(Representation.SPARSE_ENERGY);
				_sparseEnergies[sparseIndex] = energy;
			}
			else
			{
				double weight = _representation.hasWeight() ? Utilities.energyToWeight(energy) : 0.0;
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
			if (_representation.isDeterministic())
			{
				setRepresentation(Representation.DENSE_WEIGHT);
				_denseWeights[jointIndex] = weight;
			}
			else
			{
				double energy = _representation.hasEnergy() ? Utilities.weightToEnergy(weight) : 0.0;
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
			if (_representation.isDeterministic())
			{
				setRepresentation(Representation.SPARSE_ENERGY);
				_sparseWeights[sparseIndex] = weight;
			}
			else
			{
				double energy = _representation.hasEnergy() ? Utilities.weightToEnergy(weight) : 0.0;
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
			long joint = _domains.jointIndexFromIndices(indices[i]);
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
		
		if (_representation.hasSparse())
		{
			_sparseIndexToJointIndex = jointIndexes;
			
			if (_representation.hasSparseWeight())
			{
				_sparseWeights = orderedWeights;
			}
			if (_representation.hasSparseEnergy())
			{
				_sparseEnergies = new double[newSize];
				for (int i = 0; i < newSize; ++i)
				{
					_sparseEnergies[i] = Utilities.weightToEnergy(orderedWeights[i]);
				}
			}
		}
		
		if (_representation.hasDenseWeight())
		{
			for (int i = 0; i < newSize; ++i)
			{
				_denseWeights[jointIndexes[i]] = orderedWeights[i];
			}
		}
		if (_representation.hasDenseEnergy())
		{
			if (_representation.hasSparseEnergy())
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
					_denseEnergies[jointIndexes[i]] = Utilities.weightToEnergy(orderedWeights[i]);
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
		if (_representation == Representation.DETERMINISTIC)
		{
			double[] weights = new double[sparseSize()];
			Arrays.fill(weights, 1.0);
			return weights;
		}
		else
		{
			setRepresentation(_representation.union(Representation.SPARSE_WEIGHT));
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
			_representation = Representation.DENSE_WEIGHT;
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
		final int nAdditionalDomains = additionalDomains.length;
		final int nOldDomains = getDimensions();
		DiscreteDomain[] domains = _domains.toArray(new DiscreteDomain[nOldDomains + nAdditionalDomains]);
		for (int i = 0, j = nOldDomains, end = nAdditionalDomains; i < end; ++i, ++j)
		{
			domains[j] = additionalDomains[i];
		}
		NewFactorTable newTable = new NewFactorTable(domains);
		
		final int multiplier = newTable.jointSize() / jointSize();
		final int newSize = sparseSize() * multiplier;
		
		newTable._sparseEnergies = ArrayUtil.repeat(_sparseEnergies, multiplier);
		newTable._sparseWeights = ArrayUtil.repeat(_sparseWeights, multiplier);
		newTable._denseEnergies = ArrayUtil.repeat(_denseEnergies, multiplier);
		newTable._denseWeights = ArrayUtil.repeat(_denseWeights, multiplier);
		
		if (_sparseIndexToJointIndex.length > 0)
		{
			final int sparseSize = _sparseIndexToJointIndex.length;
			int[] sparseToDense = new int[newSize];
			for (int i = 0, m = 0; m < multiplier; ++m)
			{
				for (int j = 0; j < sparseSize; ++j, ++i)
				{
					sparseToDense[i] = _sparseIndexToJointIndex[j] + m * sparseSize;
				}
			}
			newTable._sparseIndexToJointIndex = sparseToDense;
		}
		
		newTable._nonZeroWeights = _nonZeroWeights * multiplier;
		
		if (_representation == Representation.DETERMINISTIC)
		{
			newTable._sparseEnergies = new double[newSize];
			newTable._representation = Representation.SPARSE_ENERGY;
		}
		else
		{
			newTable._representation = _representation;
		}
		
		return newTable;
	}
	
	@Override
	public double[] getPotentials()
	{
		if (_representation == Representation.DETERMINISTIC)
		{
			return new double[sparseSize()];
		}
		else
		{
			setRepresentation(_representation.union(Representation.SPARSE_ENERGY));
			return _sparseEnergies.clone();
		}
	}

	// FIXME: what to do if table is directed? Should we assert that the joined
	// variables are all either inputs or outputs?
	@Override
	public NewFactorTable joinVariablesAndCreateNewTable(int[] varIndices,
		int[] indexToJointIndex,
		DiscreteDomain[] allDomains,
		DiscreteDomain jointDomain)
	{
		final int nOldDomains = getDimensions();
		final int nJoinedDomains = varIndices.length;
		final int nNewDomains = nOldDomains + 1 - nJoinedDomains;
		final int jointDomainIndex = nNewDomains - 1;
		
		//
		// Build joined variable index set.
		//
		
		final BitSet varIndexSet = BitSetUtil.bitsetFromIndices(nOldDomains, varIndices);
		
		//
		// Compute new domains and state needed to construct new table.
		//

		final int [] oldToNewMap = new int[nOldDomains];
		int[] oldVarSizeProducts = computeDomainSubsetInfo(_domains, varIndexSet, oldToNewMap);
		
		DiscreteDomain[] newDomains = new DiscreteDomain[nNewDomains];
		newDomains[jointDomainIndex] = jointDomain;
		for (int i = 0, end = nOldDomains; i < end; ++i)
		{
			int j = oldToNewMap[i];
			if (j < 0)
			{
				newDomains[1 - j] = allDomains[i];
			}
		}
		
		//
		// Build the new table.
		//
		
		NewFactorTable joinedTable = new NewFactorTable(newDomains);
		
		// For simplicity, build table using dense weight representation.
		//
		// If we wanted to save memory and do the conversion in sparse representation, we would
		// need to do something much more complicated.
		joinedTable.setRepresentation(Representation.DENSE_WEIGHT);

		final int [] oldIndices = new int[nOldDomains];
		final int [] newIndices = new int[nNewDomains];
		final int [] removedIndices = new int[nJoinedDomains];
			
		final int[] sparseToJoint = _sparseIndexToJointIndex;
		if (sparseToJoint.length > 0)
		{
			for (int si = sparseToJoint.length; --si>=0;)
			{
				_domains.jointIndexToIndices(sparseToJoint[si], oldIndices);
				for (int i = 0; i < nOldDomains; ++i)
				{
					final int oldi = oldIndices[i];
					final int j = oldToNewMap[i];
					if (j < 0)
					{
						removedIndices[j] = oldi;
					}
					else
					{
						newIndices[1-j] = oldi;
					}
				}
				newIndices[jointDomainIndex] = locationFromIndices(removedIndices, oldVarSizeProducts);
				int newJointIndex = _domains.jointIndexFromIndices(newIndices);
				joinedTable.setWeightForJointIndex(getWeightForSparseIndex(si), newJointIndex);
			}
		}
		else if (_nonZeroWeights > 0)
		{
			int jointSize = jointSize();
			for (int ji = 0; ji < jointSize; ++ji)
			{
				double weight = getWeightForJointIndex(ji);
				if (weight != 0.0)
				{
					_domains.jointIndexToIndices(ji, oldIndices);
					for (int i = 0; i < nOldDomains; ++i)
					{
						final int oldi = oldIndices[i];
						final int j = oldToNewMap[i];
						if (j < 0)
						{
							removedIndices[j] = oldi;
						}
						else
						{
							newIndices[1-j] = oldi;
						}
					}
					newIndices[jointDomainIndex] = locationFromIndices(removedIndices, oldVarSizeProducts);
					int newJointIndex = _domains.jointIndexFromIndices(newIndices);
					joinedTable.setWeightForJointIndex(weight, newJointIndex);
				}
			}
		}
		
		// Convert to same representation as current table.
		joinedTable.setRepresentation(_representation);
		
		return joinedTable;
	}

	@Override
	public void normalize(int[] directedTo)
	{
		BitSet fromSet = BitSetUtil.bitsetFromIndices(getDimensions(), directedTo);
		fromSet.flip(0, fromSet.size());
		normalize(fromSet);
	}

	@Override
	public void normalize(int[] directedTo, int[] directedFrom)
	{
		normalize(directedFrom);
	}

	/*-----------------
	 * Private methods
	 */
	
	protected int allocateSparseIndexForJointIndex(int jointIndex)
	{
		final Representation representation = _representation;
		
		int sparseIndex = sparseIndexFromJointIndex(jointIndex);
		if (sparseIndex < 0)
		{
			sparseIndex = -1-sparseIndex;
			if (representation.hasSparseEnergy())
			{
				_sparseEnergies = ArrayUtil.copyArrayForInsert(_sparseEnergies, sparseIndex, 1);
			}
			if (representation.hasSparseWeight())
			{
				_sparseWeights = ArrayUtil.copyArrayForInsert(_sparseWeights, sparseIndex, 1);
			}
			_sparseIndexToJointIndex = ArrayUtil.copyArrayForInsert(_sparseIndexToJointIndex, sparseIndex, 1);
			_sparseIndexToJointIndex[sparseIndex] = jointIndex;
		}
		
		return sparseIndex;
	}
	
	private int[] computeSparseToJointIndexMap()
	{
		if (_sparseIndexToJointIndex.length > 0)
		{
			return _sparseIndexToJointIndex;
		}
		
		final int jointSize = jointSize();
		final int[] map = new int[_nonZeroWeights];
		
		if (_representation.hasDenseWeight())
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
	
	/**
	 * Computes
	 * 
	 * @param domains is an ordered non-empty list of discrete domains
	 * @param domainSubset specifies the indexes of a subset of {@code domains}
	 * @param oldToNewMap is an array with the same length as {@code domains} whose entries
	 * will be set by this function. For each entry in this array: if the domain with
	 * the corresponding index is the nth domain in {@code domainSubset} then this will
	 * contain {@code n}, otherwise if the domain is the nth domain not in {@code domainSubset}
	 * this will contain {@code -n - 1}.
	 * 
	 * @return array of cumulative products of the domain sizes in {@code domainSubset}
	 * where the first entry is 1 and the last entry will be the product of all of the
	 * subset domain sizes.
	 */
	static int[] computeDomainSubsetInfo(DiscreteDomainList domains, BitSet domainSubset, int[] oldToNewMap)
	{
		int nTrue = BitSetUtil.bitsetToIndexMap(domainSubset, oldToNewMap);
		
		int[] products = new int[nTrue + 1];
		products[0] = 1;
		
		for (int i = 0, end = domains.size(); i < end; ++i)
		{
			int j = oldToNewMap[i];
			if (j >= 0)
			{
				products[j+1] = products[j] * domains.get(i).size();
			}
		}
		
		return products;
	}

	/**
	 * For implementation of {@link #setWeightForJointIndex(double, int)} and
	 * {@link #setEnergyForJointIndex(double, int)}
	 */
	private void setWeightEnergyForJointIndex(double weight, double energy, int jointIndex)
	{
		if (_representation.hasSparse())
		{
			final int sparseIndex = allocateSparseIndexForJointIndex(jointIndex);
			if (_representation.hasSparseEnergy())
			{
				_sparseEnergies[sparseIndex] = energy;
			}
			if (_representation.hasSparseWeight())
			{
				_sparseWeights[sparseIndex] = weight;
			}
		}
		
		if (_representation.hasDenseEnergy())
		{
			_denseEnergies[jointIndex] = energy;
		}
		
		if (_representation.hasDenseWeight())
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
		if (_representation.hasDense())
		{
			final int jointIndex =
				_sparseIndexToJointIndex.length == 0 ? sparseIndex : _sparseIndexToJointIndex[sparseIndex];
			if (_representation.hasDenseEnergy())
			{
				_denseEnergies[jointIndex] = energy;
			}
			if (_representation.hasDenseWeight())
			{
				_denseWeights[jointIndex] = weight;
			}
		}
		
		if (_representation.hasSparseEnergy())
		{
			_sparseEnergies[sparseIndex] = energy;
		}
		if (_representation.hasSparseWeight())
		{
			_sparseWeights[sparseIndex] = weight;
		}
	}
}
