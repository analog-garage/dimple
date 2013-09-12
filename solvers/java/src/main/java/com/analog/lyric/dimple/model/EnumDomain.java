package com.analog.lyric.dimple.model;

public final class EnumDomain<E extends Enum<E>> extends TypedDiscreteDomain<E>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private final Class<E> _enumClass;
	private final E[] _enumConstants;
	
	/*--------------
	 * Construction
	 */
	
	public EnumDomain(Class<E> enumClass)
	{
		super(enumClass.hashCode());
		_enumClass = enumClass;
		_enumConstants = enumClass.getEnumConstants();
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
		
		if (that instanceof EnumDomain)
		{
			return ((EnumDomain<?>)that)._enumClass == _enumClass;
		}
		
		return false;
	}

	/*-----------------
	 * Domain methods
	 */
	
	@Override
	public final boolean inDomain(Object value)
	{
		return value.getClass() == _enumClass;
	}
	
	/*------------------------
	 * DiscreteDomain methods
	 */

	@Override
	public Class<E> getElementClass()
	{
		return _enumClass;
	}
	
	@Override
	public final E getElement(int i)
	{
		return _enumConstants[i];
	}

	@Override
	public final E[] getElements()
	{
		return _enumConstants.clone();
	}

	@Override
	public int size()
	{
		return _enumConstants.length;
	}

	@Override
	public int getIndex(Object value)
	{
		if (value.getClass() == _enumClass)
		{
			return ((Enum<?>)value).ordinal();
		}
		
		return -1;
	}

}
