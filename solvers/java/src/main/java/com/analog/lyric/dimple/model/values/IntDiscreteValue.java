package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;

public abstract class IntDiscreteValue extends DiscreteValue
{
	private final TypedDiscreteDomain<Integer> _domain;
	int _value;
	
	/*--------------
	 * Construction
	 */
	
	IntDiscreteValue(TypedDiscreteDomain<Integer> domain, int value)
	{
		_domain = domain;
		_value = value;
	}
	
	IntDiscreteValue(IntDiscreteValue other)
	{
		this(other._domain, other._value);
	}
	
	/*---------------
	 * Value methods
	 */

	@Override
	public TypedDiscreteDomain<Integer> getDomain()
	{
		return _domain;
	}

	@Override
	public abstract IntDiscreteValue clone();

	@Override
	public final boolean getBoolean()
	{
		return getInt() != 0;
	}
	
	@Override
	public final double getDouble()
	{
		return getInt();
	}
	
	@Override
	public final void setDouble(double value)
	{
		// FIXME is this what we want?
		setInt((int)Math.round(value));
	}
	
	@Override
	public final int getInt()
	{
		return _value;
	}
	
	@Override
	public void setInt(int value)
	{
		_value = value;
	}
	
	@Override
	public final Integer getObject()
	{
		return getInt();
	}

	@Override
	public final void setObject(Object value)
	{
		setInt(FactorFunctionUtilities.toInteger(value));
	}

	@Override
	public void setFrom(Value value)
	{
		setInt(value.getInt());
	}
	
	@Override
	public final boolean valueEquals(Value other)
	{
		return _value == other.getInt();
	}
}
