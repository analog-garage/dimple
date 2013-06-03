package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

@Immutable
public class Option<T> implements IOption<T>
{
	/*-------
	 * State
	 */
	
	private final IOptionKey<T> _key;
	private final T _value;
	
	/*--------------
	 * Construction
	 */
	
	public Option(IOptionKey<T> key)
	{
		this(key, key.defaultValue());
	}
	
	public Option(IOptionKey<T> key, T value)
	{
		_key = key;
		_value = key.type().cast(value);
	}
	
	/*-----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return String.format("%s=%s", _key.toString(), _value.toString());
	}
	
	/*-----------------
	 * IOption methods
	 */
	
	@Override
	public final IOptionKey<T> key()
	{
		return _key;
	}

	@Override
	public final T value()
	{
		return _value;
	}
}
