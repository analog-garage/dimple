package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.FiniteFieldNumber;

public class FiniteFieldValue extends DiscreteValue
{
	private FiniteFieldNumber _value;

	public FiniteFieldValue(FiniteFieldNumber value)
	{
		_value = value;
	}

	public FiniteFieldValue(FiniteFieldValue other)	// Copy constructor
	{
		_value = ((FiniteFieldNumber)other.getObject()).clone();
	}

	@Override
	public DiscreteValue clone()
	{
		return new FiniteFieldValue(this._value);
	}

	@Override
	public DiscreteDomain getDomain()
	{
		return _value.getDomain();
	}
	
	@Override
	public Object getObject()
	{
		return _value;
	}

	@Override
	public void setObject(Object value)
	{
		_value = (FiniteFieldNumber)value;
	}

	@Override
	public int getIndex()
	{
		return _value.intValue();
	}

	@Override
	public void setIndex(int index)
	{
		_value = _value.cloneWithNewValue(index);
	}
	
	@Override
	public int getInt()
	{
		return _value.intValue();
	}

}
