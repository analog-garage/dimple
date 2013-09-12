package com.analog.lyric.dimple.model;

public abstract class IntDiscreteDomain extends TypedDiscreteDomain<Integer>
{
	private static final long serialVersionUID = 1L;

	protected IntDiscreteDomain(int hashCode)
	{
		super(hashCode);
	}

	/*------------------------
	 * DiscreteDomain methods
	 */
	
	@Override
	public final Class<Integer> getElementClass()
	{
		return Integer.class;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Use {@link #getIntElement(int)} instead of this method to avoid allocating an {@link Integer} object.
	 */
	@Override
	public Integer getElement(int i)
	{
		return getIntElement(i);
	}

	/**
	 * {@inheritDoc}
	 * @see #getIndex(int)
	 */
	@Override
	public int getIndex(Object value)
	{
		if (value instanceof Number)
		{
			Number number = (Number)value;
			int i = number.intValue();
			if (i == number.doubleValue())
			{
				return getIndex(i);
			}
		}
		
		return -1;
	}
	
	/*---------------------------
	 * IntDiscreteDomain methods
	 */
	
	public abstract int getIndex(int value);
	
	public abstract int getIntElement(int i);
}
