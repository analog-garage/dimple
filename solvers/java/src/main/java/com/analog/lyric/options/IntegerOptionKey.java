package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

@Immutable
public class IntegerOptionKey extends AbstractOptionKey<Integer>
{
	private static final long serialVersionUID = 1L;
	
	private final int _defaultValue;
	
	public IntegerOptionKey(String name)
	{
		this(name, 0);
	}

	public IntegerOptionKey(String name, int defaultValue)
	{
		super(name);
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
