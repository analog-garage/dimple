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
 * Option key with type {@link Integer}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class IntegerOptionKey extends OptionKey<Integer>
{
	private static final long serialVersionUID = 1L;
	
	private final Integer _defaultValue;
	private final int _lowerBound;
	private final int _upperBound;
	
	/**
	 * Creates a new integer-valued option key with default value zero.
	 * <p>
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @since 0.07
	 */
	public IntegerOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, 0);
	}

	/**
	 * Creates a new integer-valued option key with default value.
	 * <p>
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @since 0.07
	 */
	public IntegerOptionKey(Class<?> declaringClass, String name, int defaultValue)
	{
		this(declaringClass, name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Creates a new integer-valued option key with default value and bounds.
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @param lowerBound is the lowest allowable value for the option.
	 * @param upperBound is the highest allowable value for the option.
	 * @since 0.07
	 */
	public IntegerOptionKey(Class<?> declaringClass, String name, int defaultValue, int lowerBound, int upperBound)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
		_lowerBound = lowerBound;
		_upperBound = upperBound;
		validate(defaultValue, null);
	}

	@Override
	public final Class<Integer> type()
	{
		return Integer.class;
	}

	@Override
	public final Integer defaultValue()
	{
		return defaultIntValue();
	}
	
	@Override
	public Integer convertToValue(@Nullable Object value)
	{
		if (value instanceof Number)
		{
			Number number = (Number)value;
			if (number.intValue() == number.doubleValue())
			{
				return number.intValue();
			}
			else
			{
				throw new IllegalArgumentException(String.format("Cannot convert '%s' to an int", number));
			}
		}
		else
		{
			return super.convertToValue(value);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @throws ClassCastException if value is not an {@link Integer}.
	 * @throws OptionValidationException if value is less than {@link #lowerBound()}
	 * or greater than {@link #upperBound()}.
	 */
	@Override
	public Integer validate(Integer value, @Nullable IOptionHolder optionHolder)
	{
		value = super.validate(value, optionHolder);
		if (value < _lowerBound || value > _upperBound)
		{
			throw new OptionValidationException("Value %d is not in range [%d,%d].",
				value, _lowerBound, _upperBound);
		}
		return value;
	}
	
	/*--------------------------
	 * IntegerOptionKey methods
	 */
	
	/**
	 * @return {@link #defaultValue()} as a primitive integer.
	 * @since 0.07
	 */
	public final int defaultIntValue()
	{
		return _defaultValue;
	}

	/**
	 * The lowest allowable value for the option.
	 * <p>
	 * If not specified in the constructor, this will default to {@link Integer#MIN_VALUE}.
	 * @since 0.07
	 */
	public final int lowerBound()
	{
		return _lowerBound;
	}
	
	/**
	 * The highest allowable value for the option.
	 * <p>
	 * If not specified in the constructor, this will default to {@link Integer#MAX_VALUE}.
	 * @since 0.07
	 */
	public final int upperBound()
	{
		return _upperBound;
	}
}

