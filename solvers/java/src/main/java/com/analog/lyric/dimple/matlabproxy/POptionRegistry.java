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

/**
 * MATLAB proxy for accessing Dimple option registry.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class POptionRegistry
{
	public static Object[] getOptionKeysMatching(String regexp)
	{
		return DimpleEnvironment.active().optionRegistry().getAllMatching(regexp).toArray();
	}
}
