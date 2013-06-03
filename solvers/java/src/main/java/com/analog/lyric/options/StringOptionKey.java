package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

@Immutable
public class StringOptionKey extends AbstractOptionKey<String>
{
	private static final long serialVersionUID = 1L;
	
	private final String _defaultValue;
	
	public StringOptionKey(String name)
	{
		this(name, "");
	}
	
	public StringOptionKey(String name, String defaultValue)
	{
		super(name);
		_defaultValue = defaultValue;
	}

	@Override
	public final Class<String> type()
	{
		return String.class;
	}

	@Override
	public final String defaultValue()
	{
		return defaultStringValue();
	}

	public final String defaultStringValue()
	{
		return _defaultValue;
	}

}
