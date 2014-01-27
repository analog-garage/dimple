package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.model.domains.IntRangeDomain;

/**
 * Implementation of {@link IntDiscreteValue} where the index and the value are the same.
 */
public class SimpleIntRangeValue extends IntDiscreteValue
{
	/*--------------
	 * Construction
	 */
	
	SimpleIntRangeValue(IntRangeDomain domain)
	{
		this(domain, 0);
	}

	SimpleIntRangeValue(IntRangeDomain domain, int value)
	{
		super(domain, value);
		assert(domain.getLowerBound() == 0 && domain.getInterval() == 1);
	}
	
	SimpleIntRangeValue(SimpleIntRangeValue other)
	{
		super(other);
	}
	
	/*---------------
	 * Value methods
	 */
	
	@Override
	public SimpleIntRangeValue clone()
	{
		return new SimpleIntRangeValue(this);
	}

	@Override
	public IntRangeDomain getDomain()
	{
		return (IntRangeDomain)super.getDomain();
	}
	
	@Override
	public int getIndex()
	{
		return _value;
	}

	@Override
	public void setIndex(int index)
	{
		_value = index;
	}
}
