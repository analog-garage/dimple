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
import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ReleasableIterator;

/**
 * Interface for object that can hold option values.
 * <p>
 * Concrete implementations will typically extend either {@link LocalOptionHolder} or
 * {@link StatelessOptionHolder}.
 */
public interface IOptionHolder
{
	/**
	 * Clears all local options.
	 * <p>
	 * Unsets all options set directly on this object. This does not affect option settings
	 * on other option holders in the {@linkplain #getOptionDelegates() delegate chain}.
	 */
	public void clearLocalOptions();
	
	/**
	 * Read-only collection describing options that are set directly on this object.
	 * <p>
	 * This does not include options set on {@linkplain #getOptionDelegates() delegates}.
	 * 
	 * @since 0.07
	 */
	public Collection<IOption<? extends Serializable>> getLocalOptions();
	
	/**
	 * Iterates over option holders in order in of lookup precedence.
	 * <p>
	 * This should be the first object returned by the iterator.
	 * <p>
	 * The default implementation provided by {@link AbstractOptionHolder}
	 * returns a {@link OptionParentIterator} instance,
	 * which simply walks up the chain of option parents.
	 * <p>
	 * @since 0.07
	 */
	public ReleasableIterator<? extends IOptionHolder> getOptionDelegates();
	
	/**
	 * The "parent" of this option holder to which option lookup will be delegated for option
	 * keys that are not set on this object. Used by {@link #getOption(IOptionKey)}.
	 * <p>
	 * Implementors should ensure that chain of parents is not circular!
	 * <p>
	 * @return the parent object or null if there is none.
	 */
	public @Nullable IOptionHolder getOptionParent();
	
	/**
	 * Returns value of option with given key or else its default value.
	 * <p>
	 * The same as {@link #getOption} but returns the key's
	 * {@linkplain IOptionKey#defaultValue() defaultValue} instead of null
	 * if option is not set.
	 * <p>
	 * @param key is a non-null option key.
	 * @since 0.07
	 */
	public <T extends Serializable> T getOptionOrDefault(IOptionKey<T> key);
	
	/**
	 * Returns value of option with given key if set, else null.
	 * <p>
	 * Returns the value of the first option setting found in this object or
	 * in its list of delegates. Specifically it invokes {@link #getLocalOption}
	 * on each object in {@link #getOptionDelegates()} and returns the first non-null result.
	 * <p>
	 * @param key is a non-null option key.
	 * @since 0.07
	 */
	@Nullable
	public <T extends Serializable> T getOption(IOptionKey<T> key);
	
	/**
	 * Returns value of option with given key if set directly on this object, else null.
	 * <p>
	 * @param key is a non-null option key.
	 * @since 0.07
	 */
	@Nullable
	public <T extends Serializable> T getLocalOption(IOptionKey<T> key);
	
	/**
	 * Sets option locally with given key to specified value.
	 * <p>
	 * @param key is a non-null option key.
	 * @param value is a non-null value with type compatible with key's {@linkplain IOptionKey#type type}.
	 * @since 0.07
	 * @throws UnsupportedOperationException if implementation does not support local option storage.
	 * @throws IllegalArgumentException if implementation does not support local option storage for given key.
	 */
	public <T extends Serializable> void setOption(IOptionKey<T> key, T value);
	
	/**
	 * Indicates whether object supports local storage of options.
	 * <p>
	 * If true, then option values may be set directly on this object using the {@link #setOption} method;
	 * otherwise that method will throw an exception.
	 * <p>
	 * @since 0.07
	 */
	public boolean supportsLocalOptions();
	
	/**
	 * Unsets local option with given key.
	 * <p>
	 * Removes option setting on this object for given key if such a setting exists. This does not affect
	 * option settings on other objects.
	 * <p>
	 * @param key is a non-null option key.
	 * @since 0.07
	 */
	public void unsetOption(IOptionKey<?> key);

}
