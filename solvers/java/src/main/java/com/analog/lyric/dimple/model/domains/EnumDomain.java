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

public final class EnumDomain<E extends Enum<E>> extends TypedDiscreteDomain<E>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private final Class<E> _enumClass;
	private final E[] _enumConstants;
	
	/*--------------
	 * Construction
	 */
	
	public EnumDomain(Class<E> enumClass)
	{
		super(enumClass.hashCode());
		_enumClass = enumClass;
		_enumConstants = enumClass.getEnumConstants();
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object that)
	{
		if (this == that)
		{
			return true;
		}
		
		if (that instanceof EnumDomain)
		{
			return ((EnumDomain<?>)that)._enumClass == _enumClass;
		}
		
		return false;
	}

	/*-----------------
	 * Domain methods
	 */
	
	@Override
	public final boolean inDomain(@Nullable Object value)
	{
		return value != null && value.getClass() == _enumClass;
	}
	
	@Override
	public final boolean isNumber()
	{
		return false;
	}
	
	@Override
	public final boolean isNumeric()
	{
		return false;
	}
	
	/*------------------------
	 * DiscreteDomain methods
	 */

	@Override
	public Class<E> getElementClass()
	{
		return _enumClass;
	}
	
	@Override
	public final E getElement(int i)
	{
		return _enumConstants[i];
	}

	@Override
	public final E[] getElements()
	{
		return _enumConstants.clone();
	}

	@Override
	public int size()
	{
		return _enumConstants.length;
	}

	@Override
	public int getIndex(@Nullable Object value)
	{
		if (value != null && value.getClass() == _enumClass)
		{
			return ((Enum<?>)value).ordinal();
		}
		
		return -1;
	}

}
