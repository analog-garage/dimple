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

import com.analog.lyric.dimple.model.domains.DoubleRangeDomain;

/**
 * Implementation of {@link DoubleDiscreteValue} where the index and the value are the same.
 */
public class SimpleDoubleRangeValue extends DoubleDiscreteValue
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	SimpleDoubleRangeValue(DoubleRangeDomain domain)
	{
		this(domain, 0.0);
	}
	
	SimpleDoubleRangeValue(DoubleRangeDomain domain, double value)
	{
		super(domain, value);
		assert(domain.getLowerBound() == 0.0 && domain.getInterval() == 1.0);
	}
	
	SimpleDoubleRangeValue(SimpleDoubleRangeValue other)
	{
		super(other);
	}
	
	@Override
	public SimpleDoubleRangeValue clone()
	{
		return new SimpleDoubleRangeValue(this);
	}

	/*---------------
	 * Value methods
	 */
	
	@Override
	public DoubleRangeDomain getDomain()
	{
		return (DoubleRangeDomain)super.getDomain();
	}
	
	@Override
	public int getIndex()
	{
		return (int)_value;
	}

	@Override
	public void setIndex(int index)
	{
		_value = index;
	}

	@Override
	public int getInt()
	{
		return (int)_value;
	}
}
