package com.analog.lyric.dimple.model;

public abstract class DoubleDiscreteDomain extends TypedDiscreteDomain<Double>
{
	private static final long serialVersionUID = 1L;

	public DoubleDiscreteDomain(int hashCode)
	{
		super(hashCode);
	}

	/*------------------------
	 * DiscreteDomain methods
	 */
	
	@Override
	public final Class<Double> getElementClass()
	{
		return Double.class;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Use {@link #getDoubleElement(int)} instead of this method to avoid allocating an {@link Double} object.
	 */
	@Override
	public Double getElement(int i)
	{
		return getDoubleElement(i);
	}

	/**
	 * {@inheritDoc}
	 * @see #getIndex(double)
	 */
	@Override
	public int getIndex(Object value)
	{
		if (value instanceof Number)
		{
			return getIndex(((Number)value).doubleValue());
		}
		
		return -1;
	}
	
	/*------------------------------
	 * DoubleDiscreteDomain methods
	 */

	public abstract double getDoubleElement(int i);
	
	public abstract int getIndex(double value);
	
}