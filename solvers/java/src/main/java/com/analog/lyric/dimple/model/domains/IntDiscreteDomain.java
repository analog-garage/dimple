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

public abstract class IntDiscreteDomain extends TypedDiscreteDomain<Integer>
{
	private static final long serialVersionUID = 1L;

	protected IntDiscreteDomain(int hashCode)
	{
		super(hashCode);
	}

	/*----------------
	 * Domain methods
	 */
	
	@Override
	public boolean hasIntCompatibleValues()
	{
		return true;
	}
	
	@Override
	public boolean isIntegral()
	{
		return true;
	}
	
	/*------------------------
	 * DiscreteDomain methods
	 */
	
	@Override
	public final Class<Integer> getElementClass()
	{
		return Integer.class;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Use {@link #getIntElement(int)} instead of this method to avoid allocating an {@link Integer} object.
	 */
	@Override
	public Integer getElement(int i)
	{
		return getIntElement(i);
	}

	/**
	 * {@inheritDoc}
	 * @see #getIndex(int)
	 */
	@Override
	public int getIndex(@Nullable Object value)
	{
		if (value instanceof Number)
		{
			Number number = (Number)value;
			int i = number.intValue();
			if (i == number.doubleValue())
			{
				return getIndex(i);
			}
		}
		
		return -1;
	}
	
	/*---------------------------
	 * IntDiscreteDomain methods
	 */
	
	public abstract int getIndex(int value);
	
	public abstract int getIntElement(int i);
}
