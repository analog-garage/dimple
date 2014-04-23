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

import com.analog.lyric.dimple.model.domains.IntRangeDomain;

public class IntRangeValue extends IntDiscreteValue
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	IntRangeValue(IntRangeDomain domain)
	{
		this(domain, domain.getLowerBound());
	}
	
	IntRangeValue(IntRangeDomain domain, int value)
	{
		super(domain, value);
	}

	IntRangeValue(IntRangeValue other)
	{
		super(other);
	}
	
	/*---------------
	 * Value methods
	 */
	
	@Override
	public IntRangeValue clone()
	{
		return new IntRangeValue(this);
	}

	@Override
	public IntRangeDomain getDomain()
	{
		return (IntRangeDomain)super.getDomain();
	}

	@Override
	public int getIndex()
	{
		return getDomain().getIndex(_value);
	}

	@Override
	public void setIndex(int index)
	{
		_value = getDomain().getIntElement(index);
	}
}
