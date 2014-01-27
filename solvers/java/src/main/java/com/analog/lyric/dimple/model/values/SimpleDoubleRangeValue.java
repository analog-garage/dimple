package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.model.domains.DoubleRangeDomain;

/**
 * Implementation of {@link DoubleDiscreteValue} where the index and the value are the same.
 */
public class SimpleDoubleRangeValue extends DoubleDiscreteValue
{
	/*--------------
	 * Construction
	 */
	
	SimpleDoubleRangeValue(DoubleRangeDomain domain)
	{
		this(domain, 0.0);
	}
	
	SimpleDoubleRangeValue(DoubleRangeDomain domain, double value)
	{
		super(domain, value);
		assert(domain.getLowerBound() == 0.0 && domain.getInterval() == 1.0);
	}
	
	SimpleDoubleRangeValue(SimpleDoubleRangeValue other)
	{
		super(other);
	}
	
	@Override
	public SimpleDoubleRangeValue clone()
	{
		return new SimpleDoubleRangeValue(this);
	}

	/*---------------
	 * Value methods
	 */
	
	@Override
	public DoubleRangeDomain getDomain()
	{
		return (DoubleRangeDomain)super.getDomain();
	}
	
	@Override
	public int getIndex()
	{
		return (int)_value;
	}

	@Override
	public void setIndex(int index)
	{
		_value = index;
	}

	@Override
	public int getInt()
	{
		return (int)_value;
	}
}
