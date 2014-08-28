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
 * Option key for an enumerated type.
 * <p>
 * @param <E> is an enum type.
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class EnumOptionKey<E extends Enum<E>> extends OptionKey<E>
{
	private static final long serialVersionUID = 1L;

	private final Class<E> _enumClass;
	private final E _defaultValue;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param enumClass is the enum class itself.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @since 0.07
	 */
	public EnumOptionKey(Class<?> declaringClass, String name, Class<E> enumClass, E defaultValue)
	{
		super(declaringClass, name);
		_enumClass = enumClass;
		_defaultValue = defaultValue;
	}

	@Override
	public Class<E> type()
	{
		return _enumClass;
	}

	@Override
	public E defaultValue()
	{
		return _defaultValue;
	}
	
	@Override
	public Object convertToExternal(E value)
	{
		return value.name();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * @throws IllegalArgumentException if value is a string that does not match one of the
	 * enum classes constants.
	 * @throws ClassCastException if value is not a string or an instance of {@code E}.
	 */
	@Override
	public E convertToValue(@Nullable Object value)
	{
		if (value instanceof String)
		{
			value = Enum.valueOf(_enumClass, (String)value);
		}
		
		return super.convertToValue(value);
	}
}
