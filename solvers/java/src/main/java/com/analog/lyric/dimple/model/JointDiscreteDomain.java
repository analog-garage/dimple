package com.analog.lyric.dimple.model;

import java.lang.reflect.Array;

import net.jcip.annotations.Immutable;

import com.analog.lyric.collect.ArrayUtil;

@Immutable
public class JointDiscreteDomain extends TypedDiscreteDomain<Object[]>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final DiscreteDomainList _domains;
	
	/*--------------
	 * Construction
	 */
	
	JointDiscreteDomain(DiscreteDomainList domains)
	{
		super(domains.hashCode());
		_domains = domains;
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
			return ((JointDiscreteDomain)that)._domains.equals(_domains);
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
	public Object[] getElement(int i)
	{
		return getElement(i, null);
	}

	@Override
	public Object[][] getElements()
	{
		final int size = size();
		Object[][] elements = new Object[size][];
		for (int i = 0; i < size; ++i)
		{
			elements[i] = getElement(i);
		}
		return elements;
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

	public final int getDimensions()
	{
		return _domains.size();
	}
	
	public final DiscreteDomainList getDomainList()
	{
		return _domains;
	}
	
	public Object[] getElement(int i, Object[] array)
	{
		return _domains.jointIndexToElements(i, array);
	}
	
	public int[] getElementIndices(int i)
	{
		return _domains.jointIndexToIndices(i, null);
	}

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
