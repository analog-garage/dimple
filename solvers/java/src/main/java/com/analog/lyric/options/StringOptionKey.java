package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

@Immutable
public class StringOptionKey extends OptionKey<String>
{
	private static final long serialVersionUID = 1L;
	
	private final String _defaultValue;
	
	public StringOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, "");
	}
	
	public StringOptionKey(Class<?> declaringClass, String name, String defaultValue)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
	}

	/*--------------------
	 * IOptionKey methods
	 */
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
