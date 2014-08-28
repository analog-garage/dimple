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

import org.eclipse.jdt.annotation.Nullable;


/**
 * Key for options with list of integer values.
 * @since 0.07
 * @author Christopher Barber
 */
public class IntegerListOptionKey extends OptionKey<OptionIntegerList>
{
	private static final long serialVersionUID = 1L;
	
	private final OptionIntegerList _defaultValue;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @param declaringClass
	 * @param name
	 * @since 0.07
	 */
	public IntegerListOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, OptionIntegerList.EMPTY);
	}
	
	public IntegerListOptionKey(Class<?> declaringClass, String name, OptionIntegerList defaultValue)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
	}

	public IntegerListOptionKey(Class<?> declaringClass, String name, int ... defaultValue)
	{
		this(declaringClass, name, new OptionIntegerList(defaultValue));
	}
	
	/*--------------------
	 * IOptionKey methods
	 */
	
	@Override
	public Object convertToExternal(OptionIntegerList value)
	{
		return value.toPrimitiveArray();
	}

	@Override
	public OptionIntegerList convertToValue(@Nullable Object value)
	{
		return OptionIntegerList.fromObject(value);
	}
	
	@Override
	public Class<OptionIntegerList> type()
	{
		return OptionIntegerList.class;
	}

	@Override
	public OptionIntegerList defaultValue()
	{
		return _defaultValue;
	}

	/*-----------------------------
	 * StringListOptionKey methods
	 */
	
	public void set(IOptionHolder holder, int ... values)
	{
		set(holder, new OptionIntegerList(values));
	}

}
