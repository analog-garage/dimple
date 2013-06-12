package com.analog.lyric.options;

public class BooleanOptionKey extends OptionKey<Boolean>
{
	private static final long serialVersionUID = 1L;
	
	private final boolean _defaultValue;
	
	public BooleanOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, false);
	}

	public BooleanOptionKey(Class<?> declaringClass, String name, boolean defaultValue)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
	}

	@Override
	public final Class<Boolean> type()
	{
		return Boolean.class;
	}

	@Override
	public final Boolean defaultValue()
	{
		return defaultBooleanValue();
	}

	public final boolean defaultBooleanValue()
	{
		return _defaultValue;
	}
}
