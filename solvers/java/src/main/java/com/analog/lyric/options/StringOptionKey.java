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
 * Key for string-valued options.
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class StringOptionKey extends OptionKey<String>
{
	private static final long serialVersionUID = 1L;
	
	private final String _defaultValue;
	
	public StringOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, "");
	}
	
	public StringOptionKey(Class<?> declaringClass, String name, String defaultValue)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
	}

	/*--------------------
	 * IOptionKey methods
	 */
	@Override
	public final Class<String> type()
	{
		return String.class;
	}

	@Override
	public final String defaultValue()
	{
		return defaultStringValue();
	}

	public final String defaultStringValue()
	{
		return _defaultValue;
	}
}
