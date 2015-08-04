/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import net.jcip.annotations.Immutable;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
@Immutable
final class ConstantRealValue extends RealValue
{
	private static final long serialVersionUID = 1L;

	ConstantRealValue(double value)
	{
		super(value);
	}

	@Override
	public RealValue clone()
	{
		return this;
	}

	@Override
	public RealValue mutableClone()
	{
		return Value.createReal(_value);
	}

	@Override
	public boolean isMutable()
	{
		return false;
	}
	
	@Override
	public void setDouble(double value)
	{
		throw notMutable();
	}
}
