package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

@Immutable
public class DoubleOptionKey extends OptionKey<Double>
{
	private static final long serialVersionUID = 1L;
	
	private final double _defaultValue;
	
	public DoubleOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, 0.0);
	}

	public DoubleOptionKey(Class<?> declaringClass, String name, double defaultValue)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
	}
	
	/*--------------------
	 * IOptionKey methods
	 */
	
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
