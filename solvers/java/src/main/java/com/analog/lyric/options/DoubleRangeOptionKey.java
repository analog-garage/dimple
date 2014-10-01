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
 * Key for options holding a pair of double values that define a range.
 * <p>
 * This requires that the values have exactly two elements and that the second is greater than or equal to the first.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class DoubleRangeOptionKey extends DoubleListOptionKey
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	public DoubleRangeOptionKey(Class<?> declaringClass, String name)
	{
		this(declaringClass, name, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public DoubleRangeOptionKey(Class<?> declaringClass, String name,
		double defaultLowerBound, double defaultUpperBound)
	{
		super(declaringClass, name, defaultLowerBound, defaultUpperBound);
	}
	
	/*--------------------
	 * IOptionKey methods
	 */
	
	@Override
	public OptionDoubleList validate(OptionDoubleList value, @Nullable IOptionHolder optionHolder)
	{
		value = super.validate(value, optionHolder);
		
		if (!(value.size() == 2 && value.get(0) <= value.get(1)))
		{
			throw new OptionValidationException("Expected valid double range but got '%s'", value);
		}
		
		return value;
	}
}
