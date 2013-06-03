package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

@Immutable
public class DoubleOptionKey extends AbstractOptionKey<Double>
{
	private static final long serialVersionUID = 1L;
	
	private final double _defaultValue;
	
	public DoubleOptionKey(String name)
	{
		this(name, 0.0);
	}

	public DoubleOptionKey(String name, double defaultValue)
	{
		super(name);
		_defaultValue = defaultValue;
	}

	@Override
	public final Class<Double> type()
	{
		return Double.class;
	}

	@Override
	public final Double defaultValue()
	{
		return defaultDoubleValue();
	}

	public final double defaultDoubleValue()
	{
		return _defaultValue;
	}
}
