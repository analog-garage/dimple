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

package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.options.DimpleOptionHolder;

/**
 * MATLAB proxy for accessing DimpleEnvironment
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class PEnvironment extends POptionHolder
{
	private final DimpleEnvironment _env;
	
	/*---------------
	 * Construction
	 */
	
	public PEnvironment(DimpleEnvironment env)
	{
		_env = env;
	}
	
	public PEnvironment()
	{
		this(DimpleEnvironment.active());
	}
	
	/*------------------
	 * PObject methods
	 */
	
	@Override
	public DimpleEnvironment getDelegate()
	{
		return getEnvironment();
	}
	
	@Override
	public DimpleEnvironment getModelerObject()
	{
		return getEnvironment();
	}
	
	/*-----------------------
	 * POptionHolder methods
	 */
	
	@Override
	public DimpleOptionHolder getOptionHolder(int i)
	{
		return _env;
	}
	
	@Override
	public int size()
	{
		return 1;
	}
	
	/*----------------------
	 * PEnvironment methods
	 */
	
	@Override
	public DimpleEnvironment getEnvironment()
	{
		return _env;
	}
	
	public Object[] getOptionKeysMatching(String regexp)
	{
		return _env.optionRegistry().getAllMatching(regexp).toArray();
	}
}
