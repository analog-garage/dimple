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

/**
 * Implementation of {@link IntDiscreteValue} where the index and the value are the same.
 */
public class SimpleIntRangeValue extends IntDiscreteValue
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	SimpleIntRangeValue(IntRangeDomain domain)
	{
		this(domain, 0);
	}

	SimpleIntRangeValue(IntRangeDomain domain, int value)
	{
		super(domain, value);
		assert(domain.getLowerBound() == 0 && domain.getInterval() == 1);
	}
	
	SimpleIntRangeValue(SimpleIntRangeValue other)
	{
		super(other);
	}
	
	/*---------------
	 * Value methods
	 */
	
	@Override
	public SimpleIntRangeValue clone()
	{
		return new SimpleIntRangeValue(this);
	}

	@Override
	public IntRangeDomain getDomain()
	{
		return (IntRangeDomain)super.getDomain();
	}
	
	@Override
	public int getIndex()
	{
		return _value;
	}

	@Override
	public void setIndex(int index)
	{
		_value = index;
	}
}
