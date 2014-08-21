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
 * Key for double options.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class DoubleOptionKey extends OptionKey<Double>
{
	private static final long serialVersionUID = 1L;
	
	private final Double _defaultValue;
	private final double _lowerBound;
	private final double _upperBound;
	
	/**
	 * Creates a new double-valued option key with default value zero.
	 * <p>
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @since 0.07
	 */
	public DoubleOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, 0.0);
	}

	/**
	 * Creates a new double-valued option key with default value.
	 * <p>
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @since 0.07
	 */
	public DoubleOptionKey(Class<?> declaringClass, String name, double defaultValue)
	{
		this(declaringClass, name, defaultValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Creates a new double-valued option key with default value and bounds.
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @param lowerBound is the lowest allowable value for the option.
	 * @param upperBound is the highest allowable value for the option.
	 * @since 0.07
	 */
	public DoubleOptionKey(Class<?> declaringClass, String name, double defaultValue,
		double lowerBound, double upperBound)
	{
		super(declaringClass, name);
		_defaultValue = defaultValue;
		_lowerBound = lowerBound;
		_upperBound = upperBound;
	}

	/*--------------------
	 * IOptionKey methods
	 */
	
	@Override
	public final Class<Double> type()
	{
		return Double.class;
	}

	@Override
	public final Double defaultValue()
	{
		return defaultDoubleValue();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @throws ClassCastException if value is not an {@link Double}.
	 * @throws OptionValidationException if value is less than {@link #lowerBound()}
	 * or greater than {@link #upperBound()}.
	 */
	@Override
	public Double validate(Double value, @Nullable IOptionHolder optionHolder)
	{
		value = super.validate(value, optionHolder);
		// This is written in this fashion to correctly handle NaN values.
		if (!(value >= _lowerBound && value <= _upperBound))
		{
			throw new OptionValidationException("Value %f is not in range [%f,%f].",
				value, _lowerBound, _upperBound);
		}
		return value;
	}
	
	/**
	 * The default value of the option.
	 * @since 0.07
	 */
	public final double defaultDoubleValue()
	{
		return _defaultValue;
	}

	/**
	 * The lowest allowable value for the option.
	 * <p>
	 * If not specified in the constructor, this will default to {@link Double#NEGATIVE_INFINITY}.
	 * @since 0.07
	 */
	public final double lowerBound()
	{
		return _lowerBound;
	}
	
	/**
	 * The highest allowable value for the option.
	 * <p>
	 * If not specified in the constructor, this will default to {@link Double#POSITIVE_INFINITY}.
	 * @since 0.07
	 */
	public final double upperBound()
	{
		return _upperBound;
	}
}
