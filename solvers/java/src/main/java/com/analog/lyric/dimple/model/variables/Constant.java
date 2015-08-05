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

package com.analog.lyric.dimple.model.variables;

import com.analog.lyric.dimple.model.core.SimpleFactorGraphChild;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class Constant extends SimpleFactorGraphChild implements IConstantOrVariable
{
	/*-------
	 * State
	 */
	
	private final Value _constant;
	
	/*--------------
	 * Construction
	 */
	
	public Constant(Value constant)
	{
		_constant = constant.immutableClone();
	}
	
	public Constant(Domain domain, Object value)
	{
		this(Value.constant(domain, value));
	}
	
	/*-------------------
	 * Constant methods
	 */
	
	public Value value()
	{
		return _constant;
	}
}
