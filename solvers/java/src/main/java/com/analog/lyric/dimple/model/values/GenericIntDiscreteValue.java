package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;

public class GenericIntDiscreteValue extends IntDiscreteValue
{
	/*-------
	 * State
	 */
	
	private int _index;
	
	/*--------------
	 * Construction
	 */
	
	GenericIntDiscreteValue(TypedDiscreteDomain<Integer> domain)
	{
		super(domain, domain.getElement(0));
		_index = 0;
	}
	
	GenericIntDiscreteValue(TypedDiscreteDomain<Integer> domain, int value, int index)
	{
		super(domain, value);
		_index = index;
	}
	
	GenericIntDiscreteValue(GenericIntDiscreteValue other)
	{
		super(other);
		_index = other._index;
	}

	/*---------------
	 * Value methods
	 */
	
	@Override
	public IntDiscreteValue clone()
	{
		return new GenericIntDiscreteValue(this);
	}

	@Override
	public final int getIndex()
	{
		return _index;
	}

	@Override
	public void setIndex(int index)
	{
		_index = index;
		_value = getDomain().getElement(index);
	}
	
	@Override
	public void setInt(int value)
	{
		_value = value;
		_index = getDomain().getIndex(value);
	}
}
