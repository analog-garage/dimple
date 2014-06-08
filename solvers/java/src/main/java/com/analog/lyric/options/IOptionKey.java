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

import net.jcip.annotations.Immutable;

import com.analog.lyric.util.misc.Nullable;

/**
 * A unique key for looking up or setting a value from a {@link IOptionHolder}.
 * <p>
 * @param <T> is the type of the value that will be associated with the key.
 * <p>
 * Conforming concrete implementations can either consist of an enum that implements
 * this interface, or a subclass of {@link OptionKey}. Concrete instances should either
 * be public enum instance values or public static final fields, with given {@link #name()}.
 * <p>
 * {@link OptionKey} also has useful static methods for loading option keys, whether or not
 * they are implemented as subclasses of that class.
 * <p>
 * @see OptionKey#forQualifiedName
 */
@Immutable
public interface IOptionKey<T> extends Serializable
{
	/*-------------------------
	 * Enum-compatible methods
	 * 
	 * These do not need to be defined when implementation is an enum type.
	 * <p>
	 * Should be the class that contains the declaration of this instance.
	 * <p>
	 * For enum implementations, this should be the default implementation provided by the enum.
	 * <p>
	 * @see Enum#getDeclaringClass
	 */
	public abstract @Nullable Class<?> getDeclaringClass();
	
	/**
	 * The unqualified name of the option.
	 * <p>
	 * Should be a valid Java identifier, and should be the same as the name
	 * of the field or enum member that holds this instance.
	 * <p>
	 * For enum implementations, this should be the default implementation provided by the enum.
	 * <p>
	 * @see Enum#name
	 */
	public abstract @Nullable String name();
	
	/*---------------
	 * Other methods
	 */
	
	/**
	 * The type of the option's value, which should be the same as the type parameter {@code T}.
	 */
	public abstract Class<T> type();
	
	/**
	 * The default value of the option if not set. This must not be null!
	 */
	public abstract T defaultValue();
	
	/**
	 * Lookup the value of the option in the given {@code holder}.
	 * <p>
	 * This can be implemented by:
	 * <pre>
	 * holder.options().lookup(this)
	 * </pre>
	 */
	public abstract @Nullable T lookup(IOptionHolder holder);
	
	/**
	 * Set the option value locally on the given {@code holder}.
	 * <p>
	 * This can be implemented by:
	 * <pre>
	 * holder.options.set(this, value)
	 * </pre>
	 */
	public abstract void set(IOptionHolder holder, T value);
	
	/**
	 * Unset the option locally on the given {@code holder}.
	 * <p>
	 * This can be implemented by:
	 * <pre>
	 * holder.options.unset(this)
	 * </pre>
	 */
	public abstract void unset(IOptionHolder holder);
}
