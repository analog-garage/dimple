/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.options;

import java.io.Serializable;
import java.util.Objects;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An immutable option key value pair.
 * <p>
 * @param <T> is the type of the value (see {@link IOptionKey} for details).
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class Option<T extends Serializable> implements IOption<T>
{
	/*-------
	 * State
	 */
	
	private final IOptionKey<T> _key;
	private final @Nullable T _value;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs {@code key} paired with its default value.
	 * <p>
	 * The {@link #value} attribute will be set to the {@linkplain IOptionKey#defaultValue() defaultValue}
	 * attribute of {@code key}.
	 * <p>
	 * @param key a non-null option key.
	 * @since 0.07
	 */
	public Option(IOptionKey<T> key)
	{
		this(key, key.defaultValue());
	}
	
	/**
	 * Constructs key/value pair from corresponding arguments.
	 * @param key a non-null option key.
	 * @param value a non-null value of the correct type for the key.
	 * @since 0.07
	 */
	public Option(IOptionKey<T> key, @Nullable T value)
	{
		_key = key;
		_value = value != null ? key.validate(value, null) : null;
	}
	
	/**
	 * Constructs key/value pair from corresponding arguments.
	 * <p>
	 * @param key is a non-null option key.
	 * @param value is either null or a value that can be converted using {@code key}'s
	 * {@linkplain IOptionKey#convertToValue convertValue} method.
	 * @throws RuntimeException if {@code value} does not have compatible type for key.
	 * @since 0.07
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> Option<T> create(IOptionKey<T> key, @Nullable Object value)
	{
		return new Option<T>(key, value == null ? null : key.convertToValue(value));
	}
	
	/**
	 * Constructs an option key/value pair by looking it up.
	 * <p>
	 * @since 0.07
	 */
	public static <T extends Serializable> Option<T> lookup(IOptionHolder holder, IOptionKey<T> key)
	{
		return new Option<T>(key, holder.getOption(key));
	}
	
	/*----------------
	 * Static methods
	 */
	
	private static <T extends Serializable> void doSet(IOptionHolder holder, IOption<T> option)
	{
		final IOptionKey<T> key = option.key();
		final T value = option.value();
		if (value == null)
		{
			holder.unsetOption(key);
		}
		else
		{
			holder.setOption(key, value);
		}
	}

	/**
	 * Sets multiple options.
	 * <p>
	 * Applies each option to {@code holder} in the specified order. The action
	 * depends on whether the option's {@linkplain #value()} is set: if non-null
	 * {@linkplain IOptionHolder#setOption setOption} will be invoked on the holder with the
	 * specified key and value, otherwise {@linkplain IOptionHolder#unsetOption unsetOption}
	 * will be called.
	 * <p>
	 * @param holder is a non-null option holder.
	 * @param options are zero or more option objects.
	 * @since 0.07
	 */
	public static void setOptions(IOptionHolder holder, IOption<?> ... options)
	{
		for (IOption<?> option : options)
		{
			doSet(holder, option);
		}
	}

	/*-----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof Option)
		{
			Option<?> that = (Option<?>)other;
			return _key == that._key && Objects.equals(_value,  that._value);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return _key.hashCode() * 11 + Objects.hashCode(_value);
	}
	
	@Override
	public String toString()
	{
		return String.format("%s=%s", OptionKey.qualifiedName(_key), Objects.toString(_value));
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
	public final @Nullable T value()
	{
		return _value;
	}
	
	@Override
	public @Nullable Object externalValue()
	{
		T value = _value;
		return value == null ? null : _key.convertToExternal(value);
	}
}
