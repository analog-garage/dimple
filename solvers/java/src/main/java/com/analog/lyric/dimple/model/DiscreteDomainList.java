package com.analog.lyric.dimple.model;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.BitSet;
import java.util.List;
import java.util.RandomAccess;

import net.jcip.annotations.Immutable;

@Immutable
public class DiscreteDomainList
	extends AbstractList<DiscreteDomain>
	implements List<DiscreteDomain>, RandomAccess, Serializable
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	final DiscreteDomain[] _domains;
	final int[] _products;
	final int _cardinality;
	
	/*--------------
	 * Construction
	 */
	
	DiscreteDomainList(DiscreteDomain[] domains)
	{
		if (domains == null || domains.length == 0)
		{
			throw new DimpleException("Empty domain list");
		}
		
		_domains = domains;
		
		final int nDomains = domains.length;
		
		_products = new int[nDomains];
		int product = 1;
		for (int i = 0; i < nDomains; ++i)
		{
			_products[i] = product;
			product *= domains[i].size();
		}
		_cardinality = product;
	}
	
	public static DiscreteDomainList create(DiscreteDomain ... domains)
	{
		return new DiscreteDomainList(domains);
	}
	
	public static DiscreteDomainList create(BitSet inputs, DiscreteDomain ... domains)
	{
		if (inputs != null)
		{
			return new DirectedDiscreteDomainList(inputs, domains);
		}
		else
		{
			return create(domains);
		}
	}
	
	/*--------------
	 * List methods
	 */
	
	@Override
	public DiscreteDomain get(int i)
	{
		return _domains[i];
	}

	@Override
	public int size()
	{
		return _domains.length;
	}

	/*----------------------------
	 * DiscreteDomainList methods
	 */
	
	public final int getCardinality()
	{
		return _cardinality;
	}
	
	public int getInputCardinality()
	{
		return 1;
	}
	
	public int getInputIndex(int i)
	{
		throw new ArrayIndexOutOfBoundsException();
	}
	
	public int[] getInputIndices()
	{
		return null;
	}
	
	public BitSet getInputSet()
	{
		return null;
	}
	
	public int getInputSize()
	{
		return 0;
	}
	
	public int getOutputCardinality()
	{
		return _cardinality;
	}
	
	public int getOutputIndex(int i)
	{
		if (i < 0 || i >= size())
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		return i;
	}
	
	public int[] getOutputIndices()
	{
		return null;
	}
	
	public final BitSet getOutputSet()
	{
		BitSet set = getInputSet();
		set.flip(0, set.size());
		return set;
	}
	
	public int getOutputSize()
	{
		return size();
	}
	
	public boolean isDirected()
	{
		return false;
	}
	
	public int inputIndexFromElements(Object ... elements)
	{
		return 0;
	}
	
	public int inputIndexFromIndices(int ... indices)
	{
		return 0;
	}
	
	public void inputIndexToElements(int inputIndex, Object[] elements)
	{
	}
	
	public void inputIndexToIndices(int inputIndex, int[] indices)
	{
	}
	
	public int jointIndexFromElements(Object ... elements)
	{
		return undirectedJointIndexFromElements(elements);
	}

	public int jointIndexFromIndices(int ... indices)
	{
		return undirectedJointIndexFromIndices(indices);
	}
	
	public Object[] jointIndexToElements(int jointIndex, Object[] elements)
	{
		return undirectedJointIndexToElements(jointIndex, elements);
	}
	
	public int[] jointIndexToIndices(int jointIndex, int[] indices)
	{
		return undirectedJointIndexToIndices(jointIndex, indices);
	}
	
	public int outputIndexFromElements(Object ... elements)
	{
		return undirectedJointIndexFromElements(elements);
	}
	
	public int outputIndexFromIndices(int ... indices)
	{
		return undirectedJointIndexFromIndices(indices);
	}
	
	public void outputIndexToElements(int outputIndex, Object[] elements)
	{
		undirectedJointIndexToElements(outputIndex, elements);
	}
	
	public void outputIndexToIndices(int outputIndex, int[] indices)
	{
		undirectedJointIndexToIndices(outputIndex, indices);
	}
	
	public final int undirectedJointIndexFromElements(Object ... elements)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _products;
		int joint = 0;
		for (int i = 0, end = products.length; i < end; ++i)
		{
			joint += products[i] * domains[i].getIndex(elements[i]);
		}
		return joint;
	}

	public final int undirectedJointIndexFromIndices(int ... indices)
	{
		final int[] products = _products;
		int joint = 0;
		for (int i = 0, end = products.length; i < end; ++i)
		{
			joint += products[i] * indices[i];
		}
		return joint;
	}
	
	public final Object[] undirectedJointIndexToElements(int jointIndex, Object[] elements)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _products;

		elements = allocateElements(elements);
		
		int product;
		for (int i = products.length; --i >= 0;)
		{
			final int index = jointIndex / (product = products[i]);
			elements[i] = domains[i].getElement(index);
			jointIndex -= index * product;
		}
		return elements;
	}

	public final int[] undirectedJointIndexToIndices(int jointIndex, int[] indices)
	{
		final int[] products = _products;
		
		indices = allocateIndices(indices);
		
		int product;
		for (int i = products.length; --i >= 0;)
		{
			final int index = jointIndex / (product = products[i]);
			indices[i] = index;
			jointIndex -= index * product;
		}
		return indices;
	}
	
	/*
	 * 
	 */
	
	final Object[] allocateElements(Object [] elements)
	{
		if (elements == null || elements.length != _domains.length)
		{
			elements = new Object[_domains.length];
		}
		return elements;
	}

	final int[] allocateIndices(int [] indices)
	{
		if (indices == null || indices.length != _domains.length)
		{
			indices = new int[_domains.length];
		}
		return indices;
	}
}
