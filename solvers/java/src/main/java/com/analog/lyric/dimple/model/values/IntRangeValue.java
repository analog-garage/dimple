package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.model.domains.IntRangeDomain;

public class IntRangeValue extends IntDiscreteValue
{
	/*--------------
	 * Construction
	 */
	
	IntRangeValue(IntRangeDomain domain)
	{
		this(domain, domain.getLowerBound());
	}
	
	IntRangeValue(IntRangeDomain domain, int value)
	{
		super(domain, value);
	}

	IntRangeValue(IntRangeValue other)
	{
		super(other);
	}
	
	/*---------------
	 * Value methods
	 */
	
	@Override
	public IntRangeValue clone()
	{
		return new IntRangeValue(this);
	}

	@Override
	public IntRangeDomain getDomain()
	{
		return (IntRangeDomain)super.getDomain();
	}

	@Override
	public int getIndex()
	{
		return getDomain().getIndex(_value);
	}

	@Override
	public void setIndex(int index)
	{
		_value = getDomain().getIntElement(index);
	}
}
