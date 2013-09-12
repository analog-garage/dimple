package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

/**
 * A generic {@link IOptionKey} implementation.
 */
@Immutable
public class GenericOptionKey<T> extends OptionKey<T>
{
	/*-------
	 * State
	 */
	private static final long serialVersionUID = 1L;
	
	private final Class<T> _type;
	private final T _defaultValue;
	
	/*--------------
	 * Construction
	 */
	
	public GenericOptionKey(Class<?> declaringClass, String name, Class<T> type, T defaultValue)
	{
		super(declaringClass, name);
		_type = type;
		_defaultValue = defaultValue;
	}
	
	/*--------------------
	 * IOptionKey methods
	 */
	
	@Override
	public Class<T> type()
	{
		return _type;
	}

	@Override
	public T defaultValue()
	{
		return _defaultValue;
	}

}
