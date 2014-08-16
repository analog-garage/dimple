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

package com.analog.lyric.dimple.model.domains;

import org.eclipse.jdt.annotation.Nullable;

public abstract class DoubleDiscreteDomain extends TypedDiscreteDomain<Double>
{
	private static final long serialVersionUID = 1L;

	public DoubleDiscreteDomain(int hashCode)
	{
		super(hashCode);
	}

	/*------------------------
	 * DiscreteDomain methods
	 */
	
	@Override
	public final Class<Double> getElementClass()
	{
		return Double.class;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Use {@link #getDoubleElement(int)} instead of this method to avoid allocating an {@link Double} object.
	 */
	@Override
	public Double getElement(int i)
	{
		return getDoubleElement(i);
	}

	/**
	 * {@inheritDoc}
	 * @see #getIndex(double)
	 */
	@Override
	public int getIndex(@Nullable Object value)
	{
		if (value instanceof Number)
		{
			return getIndex(((Number)value).doubleValue());
		}
		
		return -1;
	}
	
	/*------------------------------
	 * DoubleDiscreteDomain methods
	 */

	public abstract double getDoubleElement(int i);
	
	public abstract int getIndex(double value);
	
}