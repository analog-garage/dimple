package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

@Immutable
public class IntegerOptionKey extends OptionKey<Integer>
{
	private static final long serialVersionUID = 1L;
	
	private final int _defaultValue;
	
	public IntegerOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, 0);
	}

	public IntegerOptionKey(Class<?> declaringClass, String name, int defaultValue)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
	}

	@Override
	public final Class<Integer> type()
	{
		return Integer.class;
	}

	@Override
	public final Integer defaultValue()
	{
		return defaultIntValue();
	}

	public final int defaultIntValue()
	{
		return _defaultValue;
	}

}
