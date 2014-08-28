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

import org.eclipse.jdt.annotation.Nullable;

/**
 * An immutable option key value pair.
 * <p>
 * @param <T> is the type of the value (see {@link IOptionKey} for details).
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public interface IOption<T extends Serializable>
{
	/**
	 * The key that identifies the option.
	 * @since 0.07
	 */
	public abstract IOptionKey<T> key();

	/**
	 * The value associated with the option with the given {@link #key}.
	 * <p>
	 * Null value can be used to indicate option that was not set.
	 * @since 0.07
	 */
	public abstract @Nullable T value();

	/**
	 * The value associated with the option with the given {@link #key} converted to an external representation.
	 * <p>
	 * Converts {@link #value} using {@link #key}'s {@linkplain IOptionKey#convertToExternal convertToExternal}
	 * method.
	 * @since 0.07
	 */
	public abstract @Nullable Object externalValue();
}
