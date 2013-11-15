/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.gibbs.sample;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;

// REFACTOR: move and rename
public class DiscreteSample extends ObjectSample
{
	/*-------
	 * State
	 */
	
	protected final DiscreteDomain _domain;
	protected Object _value;
	protected int _index;

	/*--------------
	 * Construction
	 */
	
	public DiscreteSample(Object value, DiscreteDomain domain, int index)
	{
		this._domain = domain;
		this._value = value;
		this._index = index;
	}
	
	public DiscreteSample(DiscreteDomain domain, int index)
	{
		this(domain.getElement(index), domain, index);
	}
	
	public DiscreteSample(Object value, DiscreteDomain domain)
	{
		this(value, domain, domain.getIndex(value));
	}
	
	public DiscreteSample(DiscreteDomain domain)
	{
		this(domain, 0);
	}
	
	public DiscreteSample(DiscreteSample that)
	{
		this(that._value, that._domain, that._index);
	}
	
	@Override
	public DiscreteSample clone()
	{
		return new DiscreteSample(this);
	}
	
	/*----------------------
	 * ObjectSample methods
	 */
	
	@Override
	public final Object getObject()
	{
		return _value;
	}
	
	@Override
	public final void setObject(Object value)
	{
		_value = value;
		_index = _domain.getIndex(value);
	}
	
	public final int getIndex() {return _index;}
	
	public final void setIndex(int index)
	{
		_index = index;
		_value = _domain.getElement(index);
	}
	
	public final void setObjectAndIndex(Object value, int index)
	{
		assert(value.equals(_domain.getElement(index)));
		_value = value;
		_index = index;
	}
}
