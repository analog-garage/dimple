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

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents an option holder, key, value triplet.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@Immutable
public class OptionSetting<T extends Serializable>
{
	/*-------
	 * State
	 */
	
	private final IOption<T> _option;
	private final IOptionHolder _holder;
	
	/*--------------
	 * Construction
	 */
	
	public OptionSetting(IOptionHolder holder, IOption<T> option)
	{
		_holder = holder;
		_option = option;
	}
	
	public OptionSetting(IOptionHolder holder, IOptionKey<T> key, @Nullable T value)
	{
		this(holder, new Option<>(key, value));
	}
	
	public static <T extends Serializable> OptionSetting<T> local(IOptionHolder holder, IOptionKey<T> key)
	{
		return new OptionSetting<>(holder, key, holder.getLocalOption(key));
	}
	
	/*-------------------
	 * Attribute methods
	 */
	
	public IOptionHolder holder()
	{
		return _holder;
	}
	
	public IOptionKey<T> key()
	{
		return _option.key();
	}
	
	public IOption<T> option()
	{
		return _option;
	}
	
	public @Nullable T value()
	{
		return _option.value();
	}
	
	/*----------------
	 * Action methods
	 */
	
	/**
	 * Restores option setting represented this object by setting {@link #option()} on {@link #holder()}.
	 * <p>
	 * @since 0.08
	 */
	public void restore()
	{
		_option.setOn(_holder);
	}
}
