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
import java.util.Comparator;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A unique key for looking up or setting a value from a {@link IOptionHolder}.
 * <p>
 * @param <T> is the type of the value that will be associated with the key. The type
 * should either be {@link String}, a wrapped primitive type (e.g. {@link Double}, {@link Boolean}),
 * or an immutable concrete implementation of {@link IOptionValue}.
 * <p>
 * Concrete instances should be public static final fields, with given {@link #name()}, preferably
 * in a subclass of {@link OptionKeyDeclarer}.
 * <p>
 * {@link OptionKey} contains useful static methods for loading option keys, whether or not
 * they are implemented as subclasses of that class.
 * <p>
 * @see OptionKey#forCanonicalName
 * @see OptionKey#inClass(Class, String)
 * @see OptionKey#canonicalName(IOptionKey)
 * @see OptionKey#qualifiedName(IOptionKey)
 */
@Immutable
public interface IOptionKey<T extends Serializable> extends Serializable
{
	/**
	 * Comparator that orders keys by name.
	 * 
	 * @since 0.07
	 */
	public enum CompareByName implements Comparator<IOptionKey<?>>
	{
		INSTANCE;
		
		@NonNullByDefault(false)
		@Override
		public int compare(IOptionKey<?> key1, IOptionKey<?> key2)
		{
			return key1.name().compareTo(key2.name());
		}
	}
	
	/**
	 * Attempts to convert value to appropriate key value type.
	 * <p>
	 * This method should be able to convert values produced by {@link #convertToExternal}.
	 * <p>
	 * @param value a non-null value to be converted to the option's {@link #type}.
	 * @throws RuntimeException if value cannot be converted
	 * @since 0.07
	 */
	public abstract T convertToValue(@Nullable Object value);
	
	/**
	 * Converts value to an external representation
	 * <p>
	 * Converts value to representation suitable for use in external environments
	 * such as MATLAB or Python. Acceptable output types include boxed primitive
	 * types, Strings, and arrays of the those.
	 * <p>
	 * It is expected that the returned value can be converted back to the original
	 * typed value using {@link #convertToValue}.
	 * <p>
	 * @param value
	 * @since 0.07
	 */
	public abstract @Nullable Object convertToExternal(T value);
	
	/**
	 * Should be the class that contains the declaration of this instance.
	 */
	public abstract Class<?> getDeclaringClass();
	
	/**
	 * The unqualified name of the option.
	 * <p>
	 * Should be a valid Java identifier, and should be the same as the name
	 * of the field that holds this instance.
	 */
	public abstract String name();
	
	/**
	 * The type of the option's value, which should be the same as the type parameter {@code T}.
	 */
	public abstract Class<T> type();
	
	/**
	 * The default value of the option if not set. This must not be null!
	 */
	public abstract T defaultValue();
	
	/**
	 * Returns the value of the option for the given {@code holder} or else its default value, if not set.
	 * <p>
	 * This should be implemented by invoking {@linkplain IOptionHolder#getOptionOrDefault getOptionOrDefault}
	 * on {@code holder}.
	 */
	public abstract T getOrDefault(IOptionHolder holder);
	
	/**
	 * Returns the value of the option for the given {@code holder} or null if not set.
	 * <p>
	 * This should be implemented by invoking {@linkplain IOptionHolder#getOption getOption}
	 * on {@code holder}.
	 */
	public abstract @Nullable T get(IOptionHolder holder);
	
	/**
	 * Set the option value locally on the given {@code holder}.
	 * <p>
	 * This should be implemented by invoking {@linkplain IOptionHolder#setOption(IOptionKey,Serializable) setOption}
	 * on {@code holder}.
	 */
	public abstract void set(IOptionHolder holder, T value);
	
	/**
	 * Unset the option locally on the given {@code holder}.
	 * <p>
	 * This should be implemented by invoking {@linkplain IOptionHolder#unsetOption(IOptionKey) unsetOption}
	 * on {@code holder}.
	 */
	public abstract void unset(IOptionHolder holder);
	
	/**
	 * Validates value for this key.
	 * <p>
	 * This is invoked by implementations of {@link IOptionHolder#setOption} and can be
	 * overridden to put extra constraints on the value.
	 * <p>
	 * @param value is the value to be set on this option.
	 * @param optionHolder is the option holder on which the option is being set, if available.
	 * @return the {@code value} itself if valid otherwise throws an exception.
	 * @throws ClassCastException if {@code value} has the wrong type.
	 * @throws OptionValidationException if value is invalid.
	 * @since 0.07
	 */
	public T validate(T value, @Nullable IOptionHolder optionHolder);
}
