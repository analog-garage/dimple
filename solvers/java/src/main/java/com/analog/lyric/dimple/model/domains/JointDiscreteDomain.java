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

import java.lang.reflect.Array;

import net.jcip.annotations.Immutable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A discrete domain representing the Cartesian product of other discrete domains.
 * Its elements are Java Object[] arrays with the nth element holding an element
 * of the nth component domain.
 * <p>
 * It is represented efficiently using a {@link JointDomainIndexer}, so the full
 * combinatoric list of elements does not need to be produced unless someone
 * calls {@link #getElements()}.
 * <p>
 * Construct instances using {@link DiscreteDomain#joint(DiscreteDomain...)} or
 * {@link DiscreteDomain#joint(JointDomainIndexer)}.
 */
@Immutable
public class JointDiscreteDomain<Element> extends TypedDiscreteDomain<Element[]>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final JointDomainIndexer _domains;
	private final Class<Element[]> _elementClass;
	
	/*--------------
	 * Construction
	 */
	
	JointDiscreteDomain(JointDomainIndexer domains)
	{
		super(domains.hashCode());
		_domains = domains;
		@SuppressWarnings("unchecked")
		Class<Element[]> elementClass = (Class<Element[]>) Array.newInstance(domains.getElementClass(), 0).getClass();
		_elementClass = elementClass;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object that)
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
		StringBuilder sb = new StringBuilder();
		boolean comma = false;
		for (DiscreteDomain domain : _domains)
		{
			if (comma)
			{
				sb.append('x');
			}
			else
			{
				comma = true;
			}
			sb.append(domain.toString());
		}
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
	public boolean inDomain(@Nullable Object value)
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
	public int getIndex(@Nullable Object value)
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
	public int getIndexOrThrow(@Nullable Object value)
	{
		if (value instanceof Object[])
		{
			return getIndexFromSubelements((Object[])value);
		}
		else if (value != null && value.getClass().isArray())
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
	
	@Override
	public final boolean isNumber()
	{
		return false;
	}
	
	@Override
	public final boolean isNumeric()
	{
		return Number.class.isAssignableFrom(_elementClass);
	}
	
	@Override
	public final boolean isScalar()
	{
		return false;
	}
	
	/*-----------------------------
	 * JointDiscreteDomain methods
	 */

	/**
	 * Returns the number of dimensions, or subdomains that make up this joint domain.
	 * Same as size of {@link #getDomainIndexer()}.
	 */
	@Override
	public final int getDimensions()
	{
		return _domains.size();
	}
	
	/**
	 * Returns the underlying {@link JointDomainIndexer} that represents this domain.
	 */
	public final JointDomainIndexer getDomainIndexer()
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
	public <T> T[] getElement(int i, @Nullable T[] array)
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
	 * This is equivalent to calling {@link #getDomainIndexer()}.jointIndexToIndices(i, array)
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
