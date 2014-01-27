package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;

/**
 * Base class for {@link DiscreteValue} implementations for
 * discrete domains whose elements are doubles.
 */
public abstract class DoubleDiscreteValue extends DiscreteValue
{
	final TypedDiscreteDomain<Double> _domain;
	double _value;
	
	/*--------------
	 * Construction
	 */

	DoubleDiscreteValue(TypedDiscreteDomain<Double> domain, double value)
	{
		_domain = domain;
		_value = value;
	}
	
	DoubleDiscreteValue(DoubleDiscreteValue other)
	{
		this(other._domain, other._value);
	}
	
	@Override
	public abstract DoubleDiscreteValue clone();

	/*---------------
	 * Value methods
	 */
	
	@Override
	public TypedDiscreteDomain<Double> getDomain()
	{
		return _domain;
	}

	/**
	 * Returns true if value is not zero.
	 */
	@Override
	public final boolean getBoolean()
	{
		return _value != 0.0;
	}
	
	@Override
	public final double getDouble()
	{
		return _value;
	}
	
	@Override
	public void setDouble(double value)
	{
		_value = value;
	}
	
	@Override
	public int getInt()
	{
		return _domain.hasIntCompatibleValues() ? (int)_value : (int)Math.round(_value);
	}
	
	@Override
	public final void setInt(int value)
	{
		setDouble(value);
	}
	
	@Override
	public Double getObject()
	{
		return _value;
	}

	@Override
	public final void setObject(Object value)
	{
		setDouble(FactorFunctionUtilities.toDouble(value));
	}

	@Override
	public void setFrom(Value other)
	{
		setDouble(other.getDouble());
	}
	
	@Override
	public final boolean valueEquals(Value other)
	{
		return _value == other.getDouble();
	}
}
