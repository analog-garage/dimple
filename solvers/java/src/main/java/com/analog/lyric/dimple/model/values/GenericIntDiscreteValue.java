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

package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;

public class GenericIntDiscreteValue extends IntDiscreteValue
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	private int _index;
	
	/*--------------
	 * Construction
	 */
	
	GenericIntDiscreteValue(TypedDiscreteDomain<Integer> domain)
	{
		super(domain, domain.getElement(0));
		_index = 0;
	}
	
	GenericIntDiscreteValue(GenericIntDiscreteValue other)
	{
		super(other);
		_index = other._index;
	}

	/*---------------
	 * Value methods
	 */
	
	@Override
	public IntDiscreteValue clone()
	{
		return new GenericIntDiscreteValue(this);
	}

	@Override
	public final int getIndex()
	{
		return _index;
	}

	@Override
	public void setIndex(int index)
	{
		_index = index;
		_value = getDomain().getElement(index);
	}
	
	@Override
	public void setInt(int value)
	{
		_value = value;
		_index = getDomain().getIndex(value);
	}
}
