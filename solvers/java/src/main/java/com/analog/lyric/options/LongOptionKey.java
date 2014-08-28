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
 * Option key with type {@link Long}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class LongOptionKey extends OptionKey<Long>
{
	private static final long serialVersionUID = 1L;

	private final Long _defaultValue;
	private final long _lowerBound;
	private final long _upperBound;
	
	/**
	 * Creates a new long-valued option key with default value zero.
	 * <p>
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @since 0.07
	 */
	public LongOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, 0L);
	}

	/**
	 * Creates a new long-valued option key with default value.
	 * <p>
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @since 0.07
	 */
	public LongOptionKey(Class<?> declaringClass, String name, long defaultValue)
	{
		this(declaringClass, name, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	/**
	 * Creates a new long-valued option key with default value and bounds.
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @param lowerBound is the lowest allowable value for the option.
	 * @param upperBound is the highest allowable value for the option.
	 * @since 0.07
	 */
	public LongOptionKey(Class<?> declaringClass, String name, long defaultValue, long lowerBound, long upperBound)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
		_lowerBound = lowerBound;
		_upperBound = upperBound;
		validate(defaultValue, null);
	}

	@Override
	public Long convertToValue(@Nullable Object value)
	{
		if (value instanceof Long)
		{
			return (Long)value;
		}
		else if (value instanceof Number)
		{
			Number number = (Number)value;
			if (number.longValue() == number.doubleValue())
			{
				return number.longValue();
			}
			else
			{
				throw new IllegalArgumentException(String.format("Cannot convert '%s' to a long", number));
			}
		}
		else
		{
			return super.convertToValue(value);
		}
	}

	@Override
	public Class<Long> type()
	{
		return Long.class;
	}

	@Override
	public Long defaultValue()
	{
		return _defaultValue;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @throws ClassCastException if value is not an {@link Long}.
	 * @throws OptionValidationException if value is less than {@link #lowerBound()}
	 * or greater than {@link #upperBound()}.
	 */
	@Override
	public Long validate(Long value, @Nullable IOptionHolder optionHolder)
	{
		value = super.validate(value, optionHolder);
		if (value < _lowerBound || value > _upperBound)
		{
			throw new OptionValidationException("Value %d is not in range [%d,%d].",
				value, _lowerBound, _upperBound);
		}
		return value;
	}
	
	/*-----------------------
	 * LongOptionKey methods
	 */
	
	public final long defaultLongValue()
	{
		return _defaultValue;
	}

	/**
	 * The lowest allowable value for the option.
	 * <p>
	 * If not specified in the constructor, this will default to {@link Long#MIN_VALUE}.
	 * @since 0.07
	 */
	public final long lowerBound()
	{
		return _lowerBound;
	}
	
	/**
	 * The highest allowable value for the option.
	 * <p>
	 * If not specified in the constructor, this will default to {@link Long#MAX_VALUE}.
	 * @since 0.07
	 */
	public final long upperBound()
	{
		return _upperBound;
	}
}
