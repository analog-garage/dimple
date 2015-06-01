/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * AutoCloseable object to build and maintain temporary option settings.
 * <p>
 * Usage:
 * <blockquote>
 * <pre>
 * try (TemporaryOptionSettings tmp = new TemporaryOptionSettings())
 * {
 *     // Set temporary option values
 *     tmp.set(<i>option-holder</i>, <i>option-key</i>, <i>option-value</i>);
 *     ...
 * 
 *     // Use temporary setting
 *     ...
 * 
 * } // Previous values restored when try block is exited
 * </pre>
 * </blockquote>
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class TemporaryOptionSettings implements AutoCloseable
{
	/*-------
	 * State
	 */
	
	private final ArrayList<OptionSetting<?>> _settings = new ArrayList<>();
	
	/*--------------
	 * Construction
	 */
	
	public TemporaryOptionSettings()
	{
	}
	
	/*-----------------------
	 * AutoCloseable methods
	 */
	
	/**
	 * Restores previous option settings by invoking {@link #restore()}.
	 * <p>
	 * This will be invoked automatically if this object is used in a try-with-resources clause.
	 */
	@Override
	public void close()
	{
		restore();
	}
	
	/*----------
	 * Methods
	 */
	
	/**
	 * Returns a view of the saved option settings in the order in which they were saved.
	 * @since 0.08
	 */
	public List<OptionSetting<?>> getSavedSettings()
	{
		return Collections.unmodifiableList(_settings);
	}
	
	/**
	 * Keep temporary option settings instead of restoring previous previous values.
	 * <p>
	 * This will clear all {@linkplain TemporaryOptionSettings#getSavedSettings() saved settings} and
	 * will cause {@link #restore()} to do nothing.
	 * 
	 * @since 0.08
	 */
	public void keep()
	{
		_settings.clear();
	}
	
	/**
	 * Restores old option settings in reverse order in which they were {@link #set}.
	 * <p>
	 * This is invoked by {@link #close()}.
	 * <p>
	 * @since 0.08
	 */
	public void restore()
	{
		for (int i = _settings.size(); --i>=0;)
		{
			
		}
		_settings.clear();
	}
	
	/**
	 * Sets option with given {@code key} on {@code holder} to given {@code value}.
	 * <p>
	 * Saves the previous option setting and will restore it when {@link #restore()} or
	 * {@link #close()} is invoked unless {@link #keep()} is called.
	 * <p>
	 * @return true if this changed any state.
	 * @since 0.08
	 */
	public <T extends Serializable> boolean set(IOptionHolder holder, IOptionKey<T> key, @Nullable T value)
	{
		Option<?> option = Option.lookupLocal(holder, key);
		if (Objects.equals(value, option.value()))
		{
			return false;
		}
		
		_settings.add(new OptionSetting<>(holder, option));
		if (value == null)
		{
			holder.unsetOption(key);
		}
		else
		{
			holder.setOption(key, value);
		}
		return true;
	}
	
	public <T extends Serializable> boolean setIfDifferent(IOptionHolder holder, IOptionKey<T> key, T value)
	{
		if (Objects.equals(value, holder.getOptionOrDefault(key)))
		{
			return false;
		}

		return set(holder, key, value);
	}

	/**
	 * Unsets option with given {@code key} on {@code holder}.
	 * <p>
	 * This is simply shorthand for calling {@link #set} with null value argument.
	 * @return true if this changed any state.
	 * @since 0.08
	 */
	public boolean unset(IOptionHolder holder, IOptionKey<?> key)
	{
		return set(holder, key, null);
	}
}
