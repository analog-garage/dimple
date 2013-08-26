package com.analog.lyric.dimple.model;

import java.lang.reflect.Array;

import net.jcip.annotations.Immutable;

import com.analog.lyric.collect.ArrayUtil;

/**
 * A discrete domain representing the Cartesian product of other discrete domains.
 * Its elements are Java Object[] arrays with the nth element holding an element
 * of the nth component domain.
 * <p>
 * It is represented efficiently using a {@link DiscreteDomainList}, so the full
 * combinatoric list of elements does not need to be produced unless someone
 * calls {@link #getElements()}.
 * <p>
 * Construct instances using {@link DiscreteDomain#joint(DiscreteDomain...)} or
 * {@link DiscreteDomain#joint(DiscreteDomainList)}.
 */
@Immutable
public class JointDiscreteDomain<Element> extends TypedDiscreteDomain<Element[]>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final DiscreteDomainList _domains;
	private final Class<Element[]> _elementClass;
	
	/*--------------
	 * Construction
	 */
	
	JointDiscreteDomain(DiscreteDomainList domains)
	{
		super(domains.hashCode());
		_domains = domains;
		_elementClass = (Class<Element[]>) Array.newInstance(domains.getElementClass(), 0).getClass();
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(Object that)
	{
		if (this == that)
		{
			return true;
		}
	
		if (that instanceof JointDiscreteDomain)
		{
			return ((JointDiscreteDomain<?>)that)._domains.equals(_domains);
		}
		
		return false;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("JointDiscreteDomain(");
		boolean comma = false;
		for (DiscreteDomain domain : _domains)
		{
			if (comma)
			{
				sb.append(',');
			}
			else
			{
				comma = true;
			}
			sb.append(domain.toString());
		}
		sb.append(")");
		return sb.toString();
	}

	/*------------------------
	 * DiscreteDomain methods
	 */
	
	/**
	 * True if {@code value} is an array of length {@link #getDimensions()} for which
	 * 
	 */
	@Override
	public boolean inDomain(Object value)
	{
		Object[] values = ArrayUtil.toArray(value);
		if (values != null)
		{
			int dimensions = getDimensions();
			if (values.length == dimensions)
			{
				for (int i = 0; i < dimensions; ++i)
				{
					DiscreteDomain discrete = _domains.get(i);
					if (!discrete.inDomain(values[i]))
					{
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean containsValueWithRepresentation(Object value)
	{
		if (value instanceof Number)
		{
			return super.containsValueWithRepresentation(value);
		}
		
		int[] indices = ArrayUtil.toIntArray(value);
		if (indices != null)
		{
			final int dimensions = getDimensions();
			if (indices.length == dimensions)
			{
				for (int i = 0; i < dimensions; ++i)
				{
					int index = indices[i];
					if (index < 0 || index >= _domains.getDomainSize(i))
					{
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public final Class<Element[]> getElementClass()
	{
		return _elementClass;
	}
	
	/**
	 * Returns ith element in domain.
	 * <p>
	 * This allocates a new array. You can copy into an existing array using {@link #getElement(int, Object[])}.
	 * @param i must be in the range [0, {@link #size()}-1].
	 */
	@Override
	public Element[] getElement(int i)
	{
		return getElement(i, null);
	}

	@Override
	public final int size()
	{
		return _domains.getCardinality();
	}

	@Override
	public int getIndex(Object value)
	{
		try
		{
			return getIndexOrThrow(value);
		}
		catch (DimpleException ex)
		{
			return -1;
		}
	}
	
	@Override
	public int getIndexOrThrow(Object value)
	{
		if (value instanceof Object[])
		{
			return getIndexFromSubelements((Object[])value);
		}
		else if (value.getClass().isArray())
		{
			int size = getDimensions();
			Object[] values = new Object[size];
			for (int i = 0; i < size; ++i)
			{
				values[i] = Array.get(value, i);
			}
			return getIndexFromSubelements(values);
		}
		
		throw domainError(value);
	}
	
	/*-----------------------------
	 * JointDiscreteDomain methods
	 */

	/**
	 * Returns the number of dimensions, or subdomains that make up this joint domain.
	 * Same as size of {@link #getDomainList()}.
	 */
	public final int getDimensions()
	{
		return _domains.size();
	}
	
	/**
	 * Returns the underlying {@link DiscreteDomainList} that represents this domain.
	 */
	public final DiscreteDomainList getDomainList()
	{
		return _domains;
	}
	
	/**
	 * Writes the subelements of the ith element in the domain into {@code array} and returns it.
	 * <p>
	 * @param i must be in the range [0, {@link #size()}-1].
	 * @param array will only be used if non-null and no shorter than {@link #size()}, otherwise
	 * a new array will be allocated.
	 * @return array containing ith element, which will be the same as {@code array} provided it was big enough.
	 * @see #getElement(int)
	 * @see #getElementIndices(int, int[])
	 */
	public <T> T[] getElement(int i, T[] array)
	{
		return _domains.jointIndexToElements(i, array);
	}
	
	/**
	 * Returns any array containing the individual subdomain indexes for the
	 * ith element in this domain.
	 * <p>
	 * This is equivalent to calling {@link #getElementIndices(int, int[])} with a null second argument.
	 * <p>
	 * @param i must be in the range [0, {@link #size()}-1].
	 * @return newly allocated array of indices
	 */
	public int[] getElementIndices(int i)
	{
		return _domains.jointIndexToIndices(i, null);
	}

	/**
	 * Writes the individual subdomain indexes for the ith element in this domain into
	 * provided {@code array} and returns it.
	 * <p>
	 * This is equivalent to calling {@link #getDomainList()}.jointIndexToIndices(i, array)
	 * <p>
	 * @param i must be in the range [0, {@link #size()}-1].
	 * @param array must be at least {@link #size()} long or else a new array will be allocated.
	 * @return array containing indices, which will be same as provided {@code array} if it is large enough
	 * @see #getElementIndices(int)
	 * @see #getElement(int, Object[])
	 */
	public int[] getElementIndices(int i, int[] array)
	{
		return _domains.jointIndexToIndices(i, array);
	}

	public final int getIndexFromSubelements(Object ... values)
	{
		return _domains.jointIndexFromElements(values);
	}
	
	public final int getIndexFromIndices(int ... indices)
	{
		return _domains.jointIndexFromIndices(indices);
	}

}
