package com.analog.lyric.dimple.model;

public final class EnumDomain<E extends Enum<E>> extends TypedDiscreteDomain<E>
{
	/*-------
	 * State
	 */
	
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
		return (that instanceof EnumDomain) && ((EnumDomain<?>)that)._enumClass == _enumClass;
	}

	/*-----------------
	 * Domain methods
	 */
	
	@Override
	public final boolean containsValue(Object value)
	{
		return value.getClass() == _enumClass;
	}
	
	/*------------------------
	 * DiscreteDomain methods
	 */
	
	@Override
	public final E getElement(int i)
	{
		return _enumConstants[i];
	}

	@Override
	public final E[] getElements()
	{
		return _enumConstants;
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

	/**
	 * EnumDomain methods
	 */
	
	public Class<E> getEnumClass()
	{
		return _enumClass;
	}
}
