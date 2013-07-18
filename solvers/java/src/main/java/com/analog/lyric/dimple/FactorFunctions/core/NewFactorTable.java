package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;

@NotThreadSafe
public class NewFactorTable extends NewFactorTableBase implements INewFactorTable, IFactorTable
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private INewFactorTable.Representation _representation;
	
	private double[] _energies = EMPTY_DOUBLE_ARRAY;
	private double[] _weights = EMPTY_DOUBLE_ARRAY;
	
	/**
	 * @see #size()
	 */
	private int _size;
	
	/**
	 * Maps sparse locations to dense locations. Empty if table is in dense form.
	 * <p>
	 * This will require binary search to find sparse index given dense one.
	 */
	private int[] _sparseToDense = EMPTY_INT_ARRAY;
	
	private static final int DETERMINISTIC = 0x01;
	private static final int DETERMINISTIC_COMPUTED = 0x02;
	private static final int NORMALIZED = 0x04;
	private int _computedMask = 0;
	
	/*--------------
	 * Construction
	 */
	
	public NewFactorTable(BitSet directedFrom, DiscreteDomain ... domains)
	{
		super(directedFrom, domains);
		
		_size = 0;
		_representation = INewFactorTable.Representation.ENERGY;
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
		_size = that._size;
		_computedMask = that._computedMask;
		
		_representation = that._representation;
		if (that._energies.length > 0)
		{
			_energies = that._energies.clone();
		}
		if (that._weights.length > 0)
		{
			_weights = that._weights.clone();
		}
		if (that._sparseToDense.length > 0)
		{
			_sparseToDense = that._sparseToDense.clone();
		}
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
	public final double getEnergy(int location)
	{
		double energy = Double.POSITIVE_INFINITY;
		if (location >= 0)
		{
			switch (_representation)
			{
			case ENERGY:
			case BOTH:
				energy = _energies[location];
				break;
			case WEIGHT:
				energy = weightToEnergy(_weights[location]);
				break;
			}
		}
		return energy;
	}
	
	@Override
	public final double getWeight(int location)
	{
		double weight = 0.0;
		if (location >= 0)
		{
			switch (_representation)
			{
			case ENERGY:
				weight = energyToWeight(_energies[location]);
				break;
			case WEIGHT:
			case BOTH:
				weight = _weights[location];
			}
		}
		return weight;
	}
	
	@Override
	public void evalDeterministic(Object[] arguments)
	{
		if (!isDeterministicDirected())
		{
			throw new DimpleException("Table is not deterministic");
		}
		
		sparsify();
		
		int outputSize = getOutputIndexSize();
		int inputIndex = inputIndexFromArguments(arguments);
		int jointIndex = inputIndex * outputSize;
		
		int i = Arrays.binarySearch(_sparseToDense, jointIndex);
		if (i < 0)
		{
			i = -1-i;
		}
		
		int outputIndex = _sparseToDense[i] % outputSize;
		outputIndexToArguments(outputIndex, arguments);
	}
	
	@Override
	public boolean isDeterministicDirected()
	{
		if ((_computedMask & DETERMINISTIC_COMPUTED) != 0)
		{
			return (_computedMask & DETERMINISTIC) != 0;
		}
		
		boolean deterministic = false;
		
		if (isDirected())
		{
			sparsify();
			// Table can only be deterministic if there is exactly one
			// valid output for each possible input.
			if (_size == getInputIndexSize())
			{
				deterministic = true;
				final int outputSize = getOutputIndexSize();
				int prevInputIndex = -1;
				for (int joint : _sparseToDense)
				{
					int inputIndex = joint / outputSize;
					if (inputIndex == prevInputIndex)
					{
						deterministic = false;
						break;
					}
					prevInputIndex = inputIndex;
				}
			}
		}
		
		if (deterministic)
		{
			_computedMask |= DETERMINISTIC;
		}
		else
		{
			_computedMask &= ~DETERMINISTIC;
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
	public final int locationToJointIndex(int location)
	{
		if (location < _sparseToDense.length)
		{
			location = _sparseToDense[location];
		}
		return location;
	}
	
	@Override
	public final int locationFromJointIndex(int location)
	{
		if (_sparseToDense.length == _size)
		{
			location = Arrays.binarySearch(_sparseToDense, location);
		}
		return location;
	}
	
	@Override
	public void normalize()
	{
		if ((_computedMask & NORMALIZED) != 0)
		{
			return;
		}
			
		if (_inputSet != null)
		{
			normalize(_inputSet);
			return;
		}
		
		double total = 0.0;
		for (int i = size(); --i>= 0;)
		{
			total += getWeight(i);
		}
		
		if (total != 0.0)
		{
			for (int i = _weights.length; --i>=0;)
			{
				_weights[i] /= total;
			}
			double logTotal = Math.log(total);
			for (int i = _energies.length; --i>=0;)
			{
				_energies[i] += logTotal;
			}
		}

		_computedMask |= NORMALIZED;
	}

	@Override
	public final int size()
	{
		return _size;
	}
	
	/*--------------------------
	 * New IFactorTable methods
	 */
	
	@Override
	public void computeEnergies()
	{
		if (_representation == Representation.WEIGHT)
		{
			int size = _size;
			_energies = new double[size];
			for (int i = 0; i < size; ++i)
			{
				_energies[i] = weightToEnergy(_weights[i]);
			}
			_representation = Representation.BOTH;
		}
	}
	
	@Override
	public void computeWeights()
	{
		if (_representation == Representation.ENERGY)
		{
			int size = _size;
			_weights = new double[size];
			for (int i = 0; i < size; ++i)
			{
				_weights[i] = energyToWeight(_energies[i]);
			}
			_representation = Representation.BOTH;
		}
	}
	
	@Override
	public final Representation getRepresentation()
	{
		return _representation;
	}
	
	protected void normalize(BitSet directedFrom)
	{
		boolean canonical = _inputSet != null && _inputSet.equals(directedFrom);
		
		if (canonical && (_computedMask & NORMALIZED) != 0)
		{
			return;
		}
		
		_computedMask &= ~NORMALIZED;
		
		final int nDomains = getDomainCount();
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
		for (int i = 0, endi = _size; i < endi; ++i)
		{
			locationToIndices(i, indices);
			for (int j = 0, endj = nDirectedFrom; j < endj; ++j)
			{
				directedFromIndices[j] = indices[fromToOldMap[j]];
			}
			int index = locationFromIndices(directedFromIndices, EMPTY_INT_ARRAY, directedFromProducts);
			normalizers[index] += getWeight(i);
		}
		
		for (int i = 0, endi = _size; i < endi; ++i)
		{
			locationToIndices(i, indices);
			for (int j = 0, endj = nDirectedFrom; j < endj; ++j)
			{
				directedFromIndices[j] = indices[fromToOldMap[j]];
			}
			int index = locationFromIndices(directedFromIndices, EMPTY_INT_ARRAY, directedFromProducts);
			setWeight(i, getWeight(i) / normalizers[index]);
		}
		
		_computedMask |= NORMALIZED;
	}
	
	@Override
	public final boolean isDense()
	{
		return _size == jointSize();
	}
	
	@Override
	public boolean densify()
	{
		final int denseSize = this.jointSize();
		if (_size == denseSize)
		{
			return false;
		}
		
		boolean doEnergies = false, doWeights = false;
		
		switch (_representation)
		{
		case ENERGY:
			doEnergies = true;
			break;
		case WEIGHT:
			doWeights = true;
			break;
		case BOTH:
			doEnergies = true;
			doWeights = true;
			break;
		}
		
		if (doEnergies)
		{
			double[] energies = new double[denseSize];
			Arrays.fill(energies, Double.POSITIVE_INFINITY);
			for (int i = 0; i < _energies.length; ++i)
			{
				energies[locationToJointIndex(i)] = _energies[i];
			}
			_energies = energies;
		}
		
		if (doWeights)
		{
			double[] weights = new double[denseSize];
			for (int i = 0; i < _weights.length; ++i)
			{
				weights[locationToJointIndex(i)] = _weights[i];
			}
			_weights = weights;
		}
		
		_sparseToDense = EMPTY_INT_ARRAY;
		_size = denseSize;
		
		return true;
	}
	
	@Override
	public void setRepresentation(Representation representation)
	{
		if (representation == _representation)
		{
			return;
		}
		
		switch (representation)
		{
		case ENERGY:
			computeEnergies();
			_weights = EMPTY_DOUBLE_ARRAY;
			break;
		case WEIGHT:
			computeWeights();
			_energies = EMPTY_DOUBLE_ARRAY;
			break;
		case BOTH:
			computeWeights();
			computeEnergies();
			break;
		}
		
		_representation = representation;
	}
	
	@Override
	public boolean sparsify()
	{
		final double[] oldWeights = _weights;
		final double[] oldEnergies = _energies;
		
		//
		// Compute number of values to retain and index of first value to remove.
		//
		int sparseSize = 0;
		int first = -1;
		switch (_representation)
		{
		case ENERGY:
			for (int i = oldEnergies.length; --i>=0;)
			{
				if (Double.isInfinite(oldEnergies[i])) { first = i; } else { ++sparseSize; }
			}
			break;
		case WEIGHT:
		case BOTH:
			for (int i = oldWeights.length; --i>=0;)
			{
				if (oldWeights[i] == 0.0) { first = i; } else { ++sparseSize; }
			}
			break;
		}
		
		if (first < 0) return false;
		
		//
		// Special case empty table
		//
		
		if (sparseSize == 0)
		{
			_energies = EMPTY_DOUBLE_ARRAY;
			_weights = EMPTY_DOUBLE_ARRAY;
			_sparseToDense = EMPTY_INT_ARRAY;
			_size = 0;
			return true;
		}
		
		//
		// Make copies of value array(s) without zero-weight values.
		//
		
		final boolean wasDense = isDense();
		final int[] oldSparseToDense = _sparseToDense;
		final int[] newSparseToDense = new int[sparseSize];
	
		double newEnergies[] = oldEnergies;
		if (newEnergies.length > 0)
		{
			newEnergies = new double[sparseSize];
			for (int i = 0; i < first; ++i) newEnergies[i] = oldEnergies[i];
		}
		
		double newWeights[] = oldWeights;
		if (newWeights.length > 0)
		{
			newWeights = new double[sparseSize];
			for (int i = 0; i < first; ++i) newWeights[i] = oldWeights[i];
		}
			
		if (wasDense)
		{
			for (int i = 0; i < first; ++i) newSparseToDense[i] = i;
		}
		else
		{
			for (int i = 0; i < first; ++i) newSparseToDense[i] = oldSparseToDense[i];
		}

		int newi = first;
		
		switch (_representation)
		{
		case ENERGY:
		{
			for (int oldi = first + 1; oldi < _size; ++oldi)
			{
				double energy = oldEnergies[oldi];
				if (!Double.isInfinite(energy))
				{
					newEnergies[newi] = energy;
					newSparseToDense[newi] = wasDense ? oldi : oldSparseToDense[oldi];
					++newi;
				}
			}
			
			break;
		}
		case WEIGHT:
		{
			for (int oldi = first + 1; oldi < _size; ++oldi)
			{
				double weight = oldWeights[oldi];
				if (weight != 0.0)
				{
					newWeights[newi] = weight;
					newSparseToDense[newi] = wasDense ? oldi : newSparseToDense[oldi];
					++newi;
				}
			}
			
			break;
		}
		case BOTH:
		{
			for (int oldi = first + 1; oldi < _size; ++oldi)
			{
				double weight = oldWeights[oldi];
				if (weight != 0.0)
				{
					newWeights[newi] = weight;
					newEnergies[newi] = oldEnergies[oldi];
					newSparseToDense[newi] = wasDense ? oldi : newSparseToDense[oldi];
					++newi;
				}
			}
			
			break;
		}
		}

		_size = sparseSize;
		_energies = newEnergies;
		_weights = newWeights;
		_sparseToDense = newSparseToDense;
			
		return true;
	}
	
	@Override
	public void setEnergy(int location, double energy)
	{
		clearComputed();
		switch (_representation)
		{
		case ENERGY:
			_energies[location] = energy;
			break;
		case WEIGHT:
			_weights[location] = energyToWeight(energy);
			break;
		case BOTH:
			_energies[location] = energy;
			_weights[location] = energyToWeight(energy);
			break;
		}
	}

	@Override
	public final void setEnergy(int[] indices, double energy)
	{
		int jointLocation = jointIndexFromIndices(indices);
		int location = locationFromJointIndex(jointLocation);
		if (location < 0)
		{
			location = -1-location;
			allocateLocation(location, jointLocation);
		}
		setEnergy(location, energy);
	}
	
	@Override
	public final void setEnergy(double energy, int ... indices)
	{
		setEnergy(indices, energy);
	}
	
	@Override
	public void setWeight(int location, double weight)
	{
		clearComputed();
		switch (_representation)
		{
		case ENERGY:
			_energies[location] = weightToEnergy(weight);
			break;
		case WEIGHT:
			_weights[location] = weight;
			break;
		case BOTH:
			_energies[location] = weightToEnergy(weight);
			_weights[location] = weight;
			break;
		}
	}

	@Override
	public final void setWeight(int[] indices, double weight)
	{
		int jointLocation = jointIndexFromIndices(indices);
		int location = locationFromJointIndex(jointLocation);
		if (location < 0)
		{
			location = -1-location;
			allocateLocation(location, jointLocation);
		}
		setWeight(location, weight);
	}
	
	@Override
	public final void setWeight(double weight, int ... indices)
	{
		setWeight(indices, weight);
	}
	
	/*----------------------
	 * Old IFactorTable methods
	 */
	
	@Override
	public void change(int[][] indices, double[] weights)
	{
		clearComputed();
		int newSize = indices.length;
		
		if (indices.length != weights.length)
		{
			throw new IllegalArgumentException(
				String.format("indices and weights lenghts differ (%d vs %d)", indices.length, weights.length));
		}
		
		if (indices.length == 0)
		{
			_energies = _weights = EMPTY_DOUBLE_ARRAY;
			_sparseToDense = EMPTY_INT_ARRAY;
			return;
		}
		
		int denseSize = jointSize();
		
		// Not sure what makes the most sense for a cutoff point.
		// Given the current implementation using just a integer sparseToDense
		// array, the sparse representation will be smaller if the sparse size is
		// <2/3 the dense size, but the cost of sorting the indexes below may
		// suggest a lower threshold.
		boolean makeDense = ((double)newSize / (double)denseSize) > .5 ;
		
		_representation = Representation.WEIGHT;
		_energies = EMPTY_DOUBLE_ARRAY;
		_weights = new double[newSize];
		_sparseToDense = makeDense ? EMPTY_INT_ARRAY : new int[newSize];
		
		if (makeDense)
		{
			densify();
			for (int i = 0; i < newSize; ++i)
			{
				setWeight(indices[i], weights[i]);
			}
		}
		else
		{
			class Entry implements Comparable<Entry>
			{
				final int location;
				final double weight;
				
				Entry(int location, double weight)
				{
					this.location = location;
					this.weight = weight;
				}

				@Override
				public int compareTo(Entry that)
				{
					return this.location - that.location;
				}
			}
			
			ArrayList<Entry> newEntries = new ArrayList<Entry>(newSize);
			for (int i = 0; i < newSize; ++i)
			{
				newEntries.add(new Entry(jointIndexFromIndices(indices[i]), weights[i]));
			}
			Collections.sort(newEntries);
			
			for (int i = 0; i < newSize; ++i)
			{
				Entry entry = newEntries.get(i);
				_weights[i] = entry.weight;
				_sparseToDense[i] = entry.location;
			}
		}
	}

	@Override
	public void changeIndices(int[][] indices)
	{
		clearComputed();
		change(indices, getWeights());
	}

	@Override
	public void set(int[] indices, double value)
	{
		setWeight(indices, value);
	}

	@Override
	public void changeWeights(double[] values)
	{
		for (int i = 0; i < _size; ++i)
		{
			setWeight(i, values[i]);
		}
	}

	@Override
	public double[] getWeights()
	{
		
		computeWeights();
		return _weights;
	}

	@Override
	public void changeWeight(int index, double weight)
	{
		setWeight(index, weight);
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

		if (!Arrays.equals(_domains, that.getDomains()))
		{
			throw new DimpleException("Cannot copy from factor table with different domains");
		}
		
		if (that instanceof NewFactorTable)
		{
			NewFactorTable other = (NewFactorTable)that;
			_size = other._size;
			_representation = other._representation;
			_energies = other._energies.length > 0 ? other._energies.clone() : EMPTY_DOUBLE_ARRAY;
			_weights = other._weights.length > 0 ? other._weights.clone() : EMPTY_DOUBLE_ARRAY;
			_sparseToDense = other._sparseToDense.length > 0 ? other._sparseToDense.clone() : EMPTY_INT_ARRAY;
			_computedMask = other._computedMask;
		}
		else
		{
			_representation = Representation.WEIGHT;
			_energies = EMPTY_DOUBLE_ARRAY;
			_weights = new double[jointSize()];
			_sparseToDense = EMPTY_INT_ARRAY;
			_size = 0;
			
			int[][] indices = that.getIndices();
			double[] weights = that.getWeights();
			for (int i = 0, end = weights.length; i < end; ++i)
			{
				setWeight(weights[i], indices[i]);
			}
		}
	}

	@Override
	public NewFactorTable createTableWithNewVariables(DiscreteDomain[] additionalDomains)
	{
		DiscreteDomain[] domains = Arrays.copyOf(_domains, _domains.length + additionalDomains.length);
		NewFactorTable newTable = new NewFactorTable(domains);
		
		int multiplier = newTable.jointSize() / jointSize();
		int newSize = size() * multiplier;
		
		if (_energies.length > 0)
		{
			double[] energies = new double[newSize];
			for (int i = 0, m = 0; m < multiplier; ++m)
			{
				for (int j = 0; j < _size; ++j, ++i)
				{
					energies[i] = _energies[j];
				}
			}
			newTable._energies = energies;
		}
		
		if (_weights.length > 0)
		{
			double[] weights = new double[newSize];
			for (int i = 0, m = 0; m < multiplier; ++m)
			{
				for (int j = 0; j < _size; ++j, ++i)
				{
					weights[i] = _weights[j];
				}
			}
			newTable._weights = weights;
		}
		
		if (_sparseToDense.length > 0)
		{
			int[] sparseToDense = new int[newSize];
			for (int i = 0, m = 0; m < multiplier; ++m)
			{
				for (int j = 0; j < _size; ++j, ++i)
				{
					sparseToDense[i] = _sparseToDense[j] + m * _size;
				}
			}
			newTable._sparseToDense = sparseToDense;
		}
		
		newTable._size = newSize;
		
		return newTable;
	}

	@Override
	public double evalAsFactorFunction(Object... arguments)
	{
		return getWeight(locationFromJointIndex(jointIndexFromArguments(arguments)));
	}

	@Override
	public void evalDeterministicFunction(Object... arguments)
	{
		evalDeterministic(arguments);
	}

	@Override
	public double[] getPotentials()
	{
		computeEnergies();
		return _energies;
	}

	@Override
	public int getWeightIndexFromTableIndices(int[] indices)
	{
		// Force table into dense representation to maximize lookup speed.
		densify();
		return locationFromIndices(indices);
	}

	// FIXME: what to do if table is directed? Should we assert that the joined
	// variables are all either inputs or outputs?
	@Override
	public NewFactorTable joinVariablesAndCreateNewTable(int[] varIndices,
		int[] indexToJointIndex,
		DiscreteDomain[] allDomains,
		DiscreteDomain jointDomain)
	{
		final int nOldDomains = _domains.length;
		final int nJoinedDomains = varIndices.length;
		final int nNewDomains = nOldDomains + 1 - nJoinedDomains;
		final int jointDomainIndex = nNewDomains - 1;
		
		//
		// Build joined variable index set.
		//
		
		final BitSet varIndexSet = bitsetFromIndices(nOldDomains, varIndices);
		
		//
		// If all of the removed variables are at the end of the list, then the
		// order of the values will not be changed, so we can simply copy the state and
		// just change the domains.
		//

		if (varIndexSet.nextSetBit(0) == jointDomainIndex && !isDirected())
		{
			DiscreteDomain[] newDomains = Arrays.copyOf(_domains, nNewDomains);
			newDomains[jointDomainIndex] = jointDomain;
			
			NewFactorTable newTable = new NewFactorTable(newDomains);
			newTable._representation = _representation;
			newTable._size = _size;
			if (_energies.length > 0)
			{
				newTable._energies = _energies.clone();
			}
			if (_weights.length > 0)
			{
				newTable._weights = _weights.clone();
			}
			if (_sparseToDense.length > 0)
			{
				newTable._sparseToDense = _sparseToDense.clone();
			}
			return newTable;
		}
		
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
		
		// Build table using dense representation with only one value representation,
		// preferring energy over weight.
		//
		// If we wanted to save memory and do the conversion in sparse representation, we would
		// need to do something much more complicated.
		boolean useWeight = (_representation == Representation.WEIGHT);
		if (useWeight)
		{
			joinedTable.setRepresentation(Representation.WEIGHT);
		}
		joinedTable.densify();

		final int [] oldIndices = new int[nOldDomains];
		final int [] newIndices = new int[nNewDomains];
		final int [] removedIndices = new int[nJoinedDomains];
			
		for (int oldLocation = 0, end = _size; oldLocation < end; ++oldLocation)
		{
			locationToIndices(oldLocation, oldIndices);
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
			newIndices[jointDomainIndex] = locationFromIndices(removedIndices, EMPTY_INT_ARRAY, oldVarSizeProducts);
			int newDenseLocation = jointIndexFromIndices(newIndices);
			if (useWeight)
			{
				_weights[newDenseLocation] = _weights[oldLocation];
			}
			else
			{
				_energies[newDenseLocation] = _energies[oldLocation];
			}
		}
		
		// Convert to same representation as current table.
		if (!isDense())
		{
			joinedTable.sparsify();
		}
		joinedTable.setRepresentation(_representation);
		
		return joinedTable;
	}

	@Override
	public void normalize(int[] directedTo)
	{
		BitSet fromSet = bitsetFromIndices(getDomainCount(), directedTo);
		fromSet.flip(0, fromSet.size());
		normalize(fromSet);
	}

	@Override
	public void normalize(int[] directedTo, int[] directedFrom)
	{
		normalize(directedFrom);
	}

	@Override
	public void randomizeWeights(Random rand)
	{
		for (int i = size(); --i >= 0;)
		{
			setWeight(i, rand.nextDouble());
		}
	}

	@Override
	public void serializeToXML(String serializeName, String targetDirectory)
	{
		throw DimpleException.unsupported("serializeToXML");
	}

	@Override
	public void setDirected(int[] directedTo, int[] directedFrom)
	{
		throw DimpleException.unsupported("setDirected");
	}

	/*-----------------
	 * Private methods
	 */
	
	private void allocateLocation(int location, int jointLocation)
	{
		if (!isDense())
		{
			switch (_representation)
			{
			case ENERGY:
				_energies = copyArrayForInsert(_energies, location, 1);
				break;
			case WEIGHT:
				_weights = copyArrayForInsert(_weights, location, 1);
				break;
			case BOTH:
				_energies = copyArrayForInsert(_energies, location, 1);
				_weights = copyArrayForInsert(_weights, location, 1);
				break;
			}
			_sparseToDense = copyArrayForInsert(_sparseToDense, location, 1);
			_sparseToDense[location] = jointLocation;
			++_size;
		}
	}
	
	private void clearComputed()
	{
		_computedMask = 0;
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
	static int[] computeDomainSubsetInfo(DiscreteDomain[] domains, BitSet domainSubset, int[] oldToNewMap)
	{
		int nTrue = bitsetToIndexMap(domainSubset, oldToNewMap);
		
		int[] products = new int[nTrue + 1];
		products[0] = 1;
		
		for (int i = 0, end = domains.length; i < end; ++i)
		{
			int j = oldToNewMap[i];
			if (j >= 0)
			{
				products[j+1] = products[j] * domains[i].size();
			}
		}
		
		return products;
	}
}
