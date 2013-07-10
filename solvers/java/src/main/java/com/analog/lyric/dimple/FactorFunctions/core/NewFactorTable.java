package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.Arrays;
import java.util.Random;

import net.jcip.annotations.NotThreadSafe;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;

@NotThreadSafe
public class NewFactorTable implements INewFactorTable, IFactorTable
{
	/*-------
	 * State
	 */
	
	private final DiscreteDomain[] _domains;
	
	/**
	 * The cumulative product of domain sizes.
	 * <p>
	 * Where _domainProducts[0] == 1 and _domainProducts[n] == _domains[0].size() * ... * _domains[n - 1].size();
	 * <p>
	 * Used for translating between dense master index and individual domain indexes.
	 */
	private final int[] _domainProducts;
	
	// IDEA: if we only allow energies in the range [0,infinity] and weights in the
	// range [0,1] then we can have a single array potentially holding a mixture of
	// weights/energies converting back and forth as needed, where weights are represented
	// using the range [-1,0] by subtracting one.
	
	private double[] _energies;
	private double[] _weights;
	
	private int _size;
	
	/**
	 * Maps sparse locations to dense locations. Null if table is in dense form.
	 * <p>
	 * This will require binary search to find sparse index given dense one.
	 */
	private int[] _sparseToDense;
	
	public static enum Policy
	{
		ENERGY_ONLY,
		WEIGHT_ONLY,
		MIXED,
		WEIGHT_AND_ENERGY;
	}
	
	private static enum FlagValues
	{
		
		NORMALIZED;
		
		private boolean isSet(int mask)
		{
			return 0 != ((1 << ordinal()) & mask);
		}
		
		private int set(int mask)
		{
			return mask | (1 << ordinal());
		}
		
		private int clear(int mask)
		{
			return mask & ~(1 << ordinal());
		}
	}
	
	private int _flags;
	
	/*--------------
	 * Construction
	 */
	
	public NewFactorTable(DiscreteDomain ... domains)
	{
		_domains = domains;
		_domainProducts = new int[domains.length + 1];
		int size = 1;
		int i = 0;
		_domainProducts[i] = 1;
		for (DiscreteDomain domain : domains)
		{
			_domainProducts[++i] = size *= domain.size();
		}
		
		_size = 0;
		_energies = null;
		_weights = null;
		_sparseToDense = null;
	}
	
	public NewFactorTable(NewFactorTable that)
	{
		_domains = that._domains;
		_domainProducts = that._domainProducts;
		_size = that._size;
		
		_energies = that._energies == null ? null : that._energies.clone();
		_weights = that._weights == null ? null : that._weights.clone();
		_sparseToDense = that._sparseToDense == null ? null : that._sparseToDense.clone();
	}
	
	/*--------------------------
	 * New IFactorTable methods
	 */
	
	@Override
	public final void computeEnergies()
	{
		if (_energies == null && _weights != null)
		{
			_energies = new double[_weights.length];
			for (int i = _weights.length; --i>=0;)
			{
				_energies[i] = -Math.log(_weights[i]);
			}
		}
	}
	
	@Override
	public final void computeWeights()
	{
		if (_weights == null && _energies != null)
		{
			_weights = new double[_energies.length];
			for (int i = _energies.length; --i>=0;)
			{
				_weights[i] = Math.exp(-_energies[i]);
			}
		}
	}
	
	@Override
	public final int denseSize()
	{
		return _domainProducts[_domainProducts.length - 1];
	}
	
	@Override
	public final int domainCount()
	{
		return _domains.length;
	}
	
	@Override
	public final DiscreteDomain getDomain(int i)
	{
		return _domains[i];
	}
	
	@Override
	public final int getDomainSize(int i)
	{
		return _domains[i].size();
	}
	
	@Override
	public final double getEnergy(int location)
	{
		double energy = Double.POSITIVE_INFINITY;
		if (location >= 0)
		{
			energy = _energies != null ? _energies[location] : -Math.log(_weights[location]);
		}
		return energy;
	}
	
	@Override
	public final double getEnergy(int ... indices)
	{
		return getEnergy(indicesToLocation(indices));
	}
	
	@Override
	public final double getWeight(int location)
	{
		double weight = 0.0;
		if (location >= 0)
		{
			weight = _weights != null ? _weights[location] : Math.exp(-_energies[location]);
		}
		return weight;
	}
	
	@Override
	public final double getWeight(int ... indices)
	{
		return getWeight(indicesToLocation(indices));
	}
	
	@Override
	public final int locationToDenseLocation(int location)
	{
		return _sparseToDense != null ? _sparseToDense[location] : location;
	}
	
	@Override
	public final int locationFromDenseLocation(int location)
	{
		return _sparseToDense != null ? Arrays.binarySearch(_sparseToDense, location) : location ;
	}
	
	@Override
	public final int argumentsToDenseLocation(Object ... arguments)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _domainProducts;
		
		int location = 1;
		for (int i = 0, end = arguments.length; i < end; ++i)
		{
			int index = domains[i].getIndex(arguments[i]);
			location += products[i] * index;
		}
		
		return location;
	}
	
	@Override
	public final int indicesToDenseLocation(int ... indices)
	{
		final int[] products = _domainProducts;
		assert(indices.length == products.length);
		
		int location = 1;
		
		for (int i = 0, end = products.length; i < end; ++i)
		{
			location += products[i] * indices[i];
		}
		
		return location;
	}
	
	@Override
	public final void indicesFromDenseLocation(int location, int[] indices)
	{
		final int[] products = _domainProducts;
		
		int product, index;
		for (int i = indices.length; --i >= 0;)
		{
			indices[i] = index = location / (product = products[i]);
			location -= index * product;
		}
	}
	
	@Override
	public final int indicesToLocation(int ... indices)
	{
		return locationFromDenseLocation(indicesToDenseLocation(indices));
	}
	
	@Override
	public final void indicesFromLocation(int location, int[] indices)
	{
		indicesFromDenseLocation(locationToDenseLocation(location), indices);
	}
	
	@Override
	public final boolean isDense()
	{
		return _size == denseSize();
	}
	
	@Override
	public void makeDense()
	{
		final int denseSize = this.denseSize();
		if (_size != denseSize)
		{
			double[] newValues = new double[denseSize];
			
			if (_energies != null)
			{
				Arrays.fill(_energies, Double.POSITIVE_INFINITY);
				for (int i = 0; i < _energies.length; ++i)
				{
					newValues[locationToDenseLocation(i)] = _energies[i];
				}
				_energies = newValues;
				_weights = null;
			}
			else if (_weights != null)
			{
				for (int i = 0; i < _energies.length; ++i)
				{
					newValues[locationToDenseLocation(i)] = _weights[i];
				}
				_weights = newValues;
			}
			
			_size = denseSize;
		}
	}
	
	@Override
	public void makeSparse()
	{
		if (isDense())
		{
			IntArrayList newSparseToDense = new IntArrayList();
			DoubleArrayList newValues = new DoubleArrayList();
			
			if (_energies != null)
			{
				for (int i = 0, end = _energies.length; i < end; ++i)
				{
					double logWeight = _energies[i];
					if (Double.isInfinite(logWeight))
					{
						newSparseToDense.add(i);
						newValues.add(logWeight);
					}
				}
				_energies = Arrays.copyOf(newValues.elements(), newValues.size());
				_weights = null;
			}
			else if (_weights != null)
			{
				for (int i = 0, end = _weights.length; i < end; ++i)
				{
					double weight = _weights[i];
					if (weight != 0.0)
					{
						newSparseToDense.add(i);
						newValues.add(weight);
					}
				}
				_weights = Arrays.copyOf(newValues.elements(), newValues.size());
			}
			
			_sparseToDense = Arrays.copyOf(newSparseToDense.elements(), newSparseToDense.size());
			_size = _sparseToDense.length;
		}
	}
	
	@Override
	public void setWeight(int location, double weight)
	{
		getWeights()[location] = weight;
		if (_energies != null)
		{
			_energies[location] = -Math.log(weight);
		}
	}

	@Override
	public final void setWeight(int[] indices, double weight)
	{
		int location = indicesToLocation(indices);
		if (location < 0)
		{
			int newSize = size() + 1;
			location = -1 - location;
			if (_weights != null)
			{
				double[] weights = new double[newSize];
				for (int i = 0; i < location; ++i)
				{
					weights[i] = _weights[i];
				}
				for (int i = location + 1; i < newSize; ++i)
				{
					weights[i] = _weights[i - 1];
				}
				_weights = weights;
			}
			if (_energies != null)
			{
				double[] energies = new double[newSize];
				for (int i = 0; i < location; ++i)
				{
					energies[i] = _energies[i];
				}
				for (int i = location + 1; i < newSize; ++i)
				{
					energies[i] = _energies[i - 1];
				}
				_energies = energies;
			}
			_size = newSize;
		}
		setWeight(location, weight);
	}
	
	@Override
	public final void setWeight(double weight, int ... indices)
	{
		setWeight(indices, weight);
	}
	
	@Override
	public final int size()
	{
		return _size;
	}
	
	/*----------------------
	 * Old IFactorTable methods
	 */
	
	@Override
	public void change(int[][] indices, double[] weights)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void changeIndices(int[][] indices)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public double get(int[] indices)
	{
		return getWeight(indices);
	}

	@Override
	public void set(int[] indices, double value)
	{
		setWeight(indices, value);
	}

	@Override
	public void changeWeights(double[] values)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public DiscreteDomain[] getDomains()
	{
		return _domains;
	}

	@Override
	public int[][] getIndices()
	{
		int[][] indices = new int[size()][];
		for (int i = 0, end = size(); i < end; ++i)
		{
			indices[i] = getRow(i);
		}
		return indices;
	}

	@Override
	public double[] getWeights()
	{
		computeWeights();
		return _weights;
	}

	@Override
	public void normalize(int[] directedTo)
	{
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public IFactorTable createTableWithNewVariables(DiscreteDomain[] newDomains)
	{
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double evalAsFactorFunction(Object... arguments)
	{
		return getWeight(locationFromDenseLocation(argumentsToDenseLocation(arguments)));
	}

	@Override
	public void evalDeterministicFunction(Object... arguments)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public int[] getColumnCopy(int column)
	{
		int[] result = new int[size()];
		int[] indices = new int[domainCount()];
		
		for (int i = 0, end = size(); i < end; ++i)
		{
			indicesFromLocation(i, indices);
			result[i] = indices[column];
		}

		return result;
	}

	@Override
	public int getColumns()
	{
		return domainCount();
	}

	@Override
	public int[] getDirectedFrom()
	{
		return null;
	}

	@Override
	public int[] getDirectedTo()
	{
		return null;
	}

	@Override
	public int getEntry(int row, int column)
	{
		return getRow(row)[column];
	}

	@Override
	public double[] getPotentials()
	{
		if (_energies == null && _weights != null)
		{
			_energies = new double[_weights.length];
			for (int i = _energies.length; --i >= 0;)
			{
				_energies[i] = -Math.log(_weights[i]);
			}
		}
		
		return _energies;
	}

	@Override
	public int[] getRow(int row)
	{
		int[] indices = new int[domainCount()];
		indicesFromLocation(row, indices);
		return indices;
	}

	@Override
	public int getRows()
	{
		return size();
	}

	@Override
	public int getWeightIndexFromTableIndices(int[] indices)
	{
		return indicesToLocation(indices);
	}

	@Override
	public boolean isDeterministicDirected()
	{
		return false;
	}

	@Override
	public boolean isDirected()
	{
		return false;
	}

	@Override
	public IFactorTable joinVariablesAndCreateNewTable(int[] varIndices,
		int[] indexToJointIndex,
		DiscreteDomain[] allDomains,
		DiscreteDomain jointDomain)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void normalize()
	{
		double total = 0.0;
		for (int i = size(); --i>= 0;)
		{
			total += getWeight(i);
		}
		
		if (_weights != null)
		{
			for (int i = size(); --i>=0;)
			{
				_weights[i] /= total;
			}
		}
		if (_energies != null)
		{
			double logTotal = Math.log(total);
			for (int i = size(); --i>=0;)
			{
				_energies[i] -= logTotal;
			}
		}
	}

	@Override
	public void normalize(int[] directedTo, int[] directedFrom)
	{
		// TODO Auto-generated method stub

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

}
