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

package com.analog.lyric.dimple.factorfunctions;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;

/**
 * Uniform distribution. Returns zero energy for all combination of arguments.
 * 
 * @since 0.05
 */
public class Uniform extends FactorFunction
{
	public static Uniform INSTANCE = new Uniform();
	
	public Uniform()
	{
		super("Uniform");
	}
	
	@Override
	public final double evalEnergy(Value[] values)
	{
		return 0.0;
	}

	@Override
	public final double evalEnergy(Object... arguments)
	{
		return 0.0;
	}
	
	@Override
	public final double eval(Value[] values)
	{
		return 1.0;
	}

	@Override
	public final double eval(Object... arguments)
	{
		return 1.0;
	}
}
