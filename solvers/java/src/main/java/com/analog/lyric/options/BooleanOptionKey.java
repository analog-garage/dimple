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

import net.jcip.annotations.Immutable;

/**
 * Key for boolean options.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class BooleanOptionKey extends OptionKey<Boolean>
{
	private static final long serialVersionUID = 1L;
	
	private final Boolean _defaultValue;
	
	public BooleanOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, false);
	}

	public BooleanOptionKey(Class<?> declaringClass, String name, boolean defaultValue)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
	}

	/**
	 * Construct option key with specified attributes.
	 * @since 0.08
	 */
	public BooleanOptionKey(Class<?> declaringClass, String name, boolean defaultValue, IOptionKey.Lookup lookupMethod)
	{
		super(declaringClass, name, lookupMethod);
		_defaultValue = defaultValue;
	}
	
	@Override
	public final Class<Boolean> type()
	{
		return Boolean.class;
	}

	@Override
	public final Boolean defaultValue()
	{
		return defaultBooleanValue();
	}

	/**
	 * Default value of option.
	 * @since 0.07
	 */
	public final boolean defaultBooleanValue()
	{
		return _defaultValue;
	}
}
