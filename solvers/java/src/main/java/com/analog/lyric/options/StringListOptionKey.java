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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Key for options with list of String values.
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class StringListOptionKey extends OptionKey<OptionStringList>
{
	private static final long serialVersionUID = 1L;
	
	private final OptionStringList _defaultValue;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @param declaringClass
	 * @param name
	 * @since 0.07
	 */
	public StringListOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, OptionStringList.EMPTY);
	}
	
	public StringListOptionKey(Class<?> declaringClass, String name, OptionStringList defaultValue)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
	}

	public StringListOptionKey(Class<?> declaringClass, String name, String ... defaultValue)
	{
		this(declaringClass, name, new OptionStringList(defaultValue));
	}
	
	/*--------------------
	 * IOptionKey methods
	 */

	@Override
	public Object convertToExternal(OptionStringList value)
	{
		return value.toPrimitiveArray();
	}
	
	@Override
	public OptionStringList convertToValue(@Nullable Object value)
	{
		return OptionStringList.fromObject(value);
	}
	
	@Override
	public Class<OptionStringList> type()
	{
		return OptionStringList.class;
	}

	@Override
	public OptionStringList defaultValue()
	{
		return _defaultValue;
	}

	/*-----------------------------
	 * StringListOptionKey methods
	 */
	
	public void set(IOptionHolder holder, String ... values)
	{
		set(holder, new OptionStringList(values));
	}
}
