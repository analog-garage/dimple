package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;

public class GenericDoubleDiscreteValue extends DoubleDiscreteValue
{
	/*-------
	 * State
	 */
	
	private int _index;
	
	/*--------------
	 * Construction
	 */
	
	GenericDoubleDiscreteValue(TypedDiscreteDomain<Double> domain)
	{
		super(domain, domain.getElement(0));
		_index = 0;
	}
	
	GenericDoubleDiscreteValue(GenericDoubleDiscreteValue other)
	{
		super(other);
		_index = other._index;
	}
	
	@Override
	public GenericDoubleDiscreteValue clone()
	{
		return new GenericDoubleDiscreteValue(this);
	}

	/*---------------
	 * Value methods
	 */
	
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
	public void setDouble(double value)
	{
		_value = value;
		_index = getDomain().getIndex(value);
	}
	
	@Override
	public void setFrom(Value other)
	{
		if (other instanceof GenericDoubleDiscreteValue)
		{
			GenericDoubleDiscreteValue discrete = (GenericDoubleDiscreteValue)other;
			if (discrete.getDomain().equals(getDomain()))
			{
				_value = discrete._value;
				_index = discrete._index;
				return;
			}
		}
		setDouble(other.getDouble());
	}
}
