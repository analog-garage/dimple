package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.model.domains.DoubleRangeDomain;

public class DoubleRangeValue extends DoubleDiscreteValue
{
	/*--------------
	 * Construction
	 */

	DoubleRangeValue(DoubleRangeDomain domain)
	{
		this(domain, domain.getLowerBound());
	}
	
	DoubleRangeValue(DoubleRangeDomain domain, double value)
	{
		super(domain, value);
	}
	
	DoubleRangeValue(DoubleRangeValue other)
	{
		super(other);
	}
	
	@Override
	public DoubleDiscreteValue clone()
	{
		return new DoubleRangeValue(this);
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
		return getDomain().getIndex(_value);
	}

	@Override
	public void setIndex(int index)
	{
		_value = getDomain().getDoubleElement(index);
	}

}
